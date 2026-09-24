param(
    [string]$ServerRoot = 'C:\AI\workspace\security',
    [string]$JavaRoot = 'C:\Program Files\Java\jdk1.8.0_202'
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$sourceRoot = Join-Path $ServerRoot 'simonstyle\src\main\java'
$clientAlgorithm = Join-Path $projectRoot 'app\src\main\java\org\cas\shared\inventory\MovingPlan.java'
$serverAlgorithm = Join-Path $sourceRoot 'org\cas\shared\inventory\MovingPlan.java'
if ((Get-FileHash $clientAlgorithm).Hash -ne (Get-FileHash $serverAlgorithm).Hash) {
    throw 'Phone and server MovingPlan sources differ.'
}
$outputDir = Join-Path $projectRoot '.gradle\moving-check'
New-Item -ItemType Directory -Force $outputDir | Out-Null
$classPath = "$outputDir;$ServerRoot\simonstyle\target\classes;$ServerRoot\simonstyle\target\libs\*;$ServerRoot\simonstyle\lib\*"
$sources = @(
    $clientAlgorithm,
    (Join-Path $projectRoot 'app\src\main\java\com\just\print\simclient\LocalMovingInputs.java'),
    (Join-Path $PSScriptRoot 'LocalMovingInputsCheck.java'),
    (Join-Path $PSScriptRoot 'MovingPlanCheck.java'),
    (Join-Path $sourceRoot 'org\cas\client\platform\bar\net\inventory\MobileMovingStore.java'),
    (Join-Path $sourceRoot 'org\cas\client\platform\bar\net\inventory\InventoryCommandServer.java'),
    (Join-Path $sourceRoot 'org\cas\client\platform\bar\net\inventory\StockProductStore.java'),
    (Join-Path $sourceRoot 'org\cas\client\platform\bar\dialog\todo\TodoDialog.java'),
    (Join-Path $sourceRoot 'org\cas\client\platform\bar\action\Cmd_OpenDrawer.java')
)
& "$JavaRoot\bin\javac.exe" -encoding UTF-8 -cp $classPath -d $outputDir @sources
if ($LASTEXITCODE -ne 0) { throw 'Moving check compilation failed.' }
& "$JavaRoot\bin\java.exe" '-Djava.awt.headless=true' -cp $classPath MovingPlanCheck
if ($LASTEXITCODE -ne 0) { throw 'Moving checks failed.' }
& "$JavaRoot\bin\java.exe" '-Djava.awt.headless=true' -cp $classPath LocalMovingInputsCheck
if ($LASTEXITCODE -ne 0) { throw 'Local-only moving checks failed.' }
