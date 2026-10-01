param(
    [Parameter(Mandatory = $true)][string]$ApkPath,
    [Parameter(Mandatory = $true)][string]$ApkSigner,
    [switch]$SkipSignatureVerification,
    [string]$Profile = 'C:\DropBox\bk\a2.tlp',
    [string]$CredentialFile = 'C:\DropBox\bk\a2-deploy-credential.xml'
)

$ErrorActionPreference = 'Stop'
# Gradle may inherit PSModulePath from PowerShell 7 while launching Windows PowerShell 5.
# Load its own standard modules rather than relying on cross-version auto-discovery.
Import-Module (Join-Path $PSHOME 'Modules\Microsoft.PowerShell.Utility\Microsoft.PowerShell.Utility.psd1') -Force
Import-Module (Join-Path $PSHOME 'Modules\Microsoft.PowerShell.Management\Microsoft.PowerShell.Management.psd1') -Force
$remoteDirectory = '/root/apache-tomcat-8.5/webapps/ROOT/images'
$publicUrl = 'http://216.137.179.176/resources/images/SimClient.apk'
$sftp = 'C:\Program Files (x86)\Bitvise SSH Client\sftpc.exe'
$ssh = 'C:\Program Files (x86)\Bitvise SSH Client\sexec.exe'
foreach ($required in @($ApkPath, $ApkSigner, $Profile, $CredentialFile, $sftp, $ssh)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Required file not found: $required" }
}
$apk = (Get-Item -LiteralPath $ApkPath).FullName
if ([IO.Path]::GetFileName($apk) -cne 'SimClient.apk') { throw 'Upload artifact must be named SimClient.apk.' }
if ($apk.Contains('"') -or $apk.Contains(';') -or $apk.Contains("`n") -or $apk.Contains("`r")) {
    throw 'APK path contains characters unsupported by the SFTP command parser.'
}
if (-not $SkipSignatureVerification) {
    & $ApkSigner verify $apk
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed. Unsigned releases cannot be uploaded.' }
} else {
    Write-Host 'Skipping jarsigner verification: this APK is signed by Gradle using Android APK v2/v3 signing.'
}
$hash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
$size = (Get-Item -LiteralPath $apk).Length
$token = [guid]::NewGuid().ToString('N')
$remoteTemporary = ".SimClient-$token.upload"
$download = Join-Path ([IO.Path]::GetTempPath()) "SimClient-verify-$token.apk"
$credential = Import-Clixml -LiteralPath $CredentialFile
$passwordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($credential.Password)
try { $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPtr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPtr) }

try {
    # Follow Security's Bitvise profile/DPAPI credential convention. Never log the arguments.
    $connection = @("-profile=$Profile", "-user=$($credential.UserName)", "-pw=$plainPassword", '-unat=y')
    Write-Host "Uploading SimClient.apk ($size bytes)..."
    $uploadCommand = "cd `"$remoteDirectory`"; put -o `"$apk`" `"$remoteTemporary`""
    & $sftp @connection -progress=none "-cmd=$uploadCommand"
    if ($LASTEXITCODE -ne 0) { throw 'SFTP upload failed; the published APK was not replaced.' }

    # All substituted values here are fixed constants, generated hex, or a SHA256 hash.
    # Verify before promotion, retain the previous different APK, then atomically rename.
    $promote = @'
set -eu
cd '__DIRECTORY__'
printf '%s  %s\n' '__HASH__' '__TEMP__' | sha256sum -c -
if [ -f SimClient.apk ] && printf '%s  %s\n' '__HASH__' 'SimClient.apk' | sha256sum -c - >/dev/null 2>&1; then
    rm -- '__TEMP__'
else
    if [ -f SimClient.apk ]; then cp -p -- SimClient.apk SimClient.apk.previous; fi
    chmod 644 '__TEMP__'
    mv -f -- '__TEMP__' SimClient.apk
fi
sha256sum SimClient.apk
'@
    $promote = $promote.Replace('__DIRECTORY__', $remoteDirectory).Replace('__HASH__', $hash).Replace('__TEMP__', $remoteTemporary).Replace("`r", '')
    & $ssh @connection -exitZero "-cmd=$promote"
    if ($LASTEXITCODE -ne 0) { throw 'Server checksum verification or publication failed.' }

    Invoke-WebRequest -UseBasicParsing -Uri ($publicUrl + '?sha256=' + $hash) -OutFile $download -TimeoutSec 120
    if ((Get-FileHash -LiteralPath $download -Algorithm SHA256).Hash.ToLowerInvariant() -ne $hash) {
        throw 'Public download does not match the built APK. Publication is not verified.'
    }
    @{
        url = $publicUrl
        sha256 = $hash
        bytes = $size
        verifiedAtUtc = [DateTime]::UtcNow.ToString('o')
    } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path (Split-Path -Parent $apk) 'upload-receipt.json') -Encoding UTF8
    Write-Host "Published and verified: $publicUrl"
    Write-Host "SHA256: $hash"
} finally {
    $plainPassword = $null
    $connection = $null
    if (Test-Path -LiteralPath $download) { Remove-Item -LiteralPath $download -Force }
}
