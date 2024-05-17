package org.cas.client.platform.bar.web_srm.utiles;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.web_srm.MevUtils;

public class UtilesECDSA {

//	/// Creating the key pair and storing the key pair in the Windows Key Store
//	/// <param name="KeyName">Name of the new key pair (Could be the nickname of the certificate)
//	/// <param name="DeleteExistingKeys"></param>
//	/// [Obsolete( "KeyPairCreation( String KeyName, boolean DeleteExistingKeys) is
//	/// deprecated, please use KeyPairCreation( String KeyName, String AppInit,
//	/// boolean DeleteExistingKeys = false ) instead." )]
//
//	public static void KeyPairCreation( String KeyName, boolean DeleteExistingKeys){
//        // Configuration of the new key pair
//        CngKeyCreationParameters KeyParameters = new CngKeyCreationParameters
//        {
//            ExportPolicy = CngExportPolicies.AllowPlaintextExport,
//            KeyUsage = CngKeyUsages.Signing
//        };
//
//        // Delete the existing key pair with the same name, if necessary
//        if( DeleteExistingKeys && CngKey.Exists( KeyName ) )
//            CngKey.Open( KeyName ).Delete( );
//
//        // Creation and storage of the key pair
//        CngKey.Create( CngAlgorithm.ECDsaP256, KeyName, KeyParameters );
//    }
//
//	/// Creating the key pair and storing the key pair in the Windows Key Store
//
//	/// <param name="KeyName">Name of the new key pair (Could be the nickname of the
//	/// certificate)
//	/// <param name="AppInit"></param>
//	/// <param name="DeleteExistingKeys"></param>
//	public static void KeyPairCreation( String KeyName, String AppInit, boolean DeleteExistingKeys = false )
//    {
//        // Configuration of the new key pair
//         CngKeyCreationParameters KeyParameters = new CngKeyCreationParameters
//        {
//            KeyUsage = CngKeyUsages.Signing
//        };
//
//        // Export policy if the KeyPair if for remote server
//        if( AppInit == ValeursPossibles.ApprlInit.SRV )
//            KeyParameters.ExportPolicy = CngExportPolicies.AllowPlaintextExport;
//
//        // Delete the existing key pair with the same name, if necessary
//        if( DeleteExistingKeys && CngKey.Exists( KeyName ) )
//            CngKey.Open( KeyName ).Delete( );
//
//        // Creation and storage of the key pair
//        CngKey.Create( CngAlgorithm.ECDsaP256, KeyName, KeyParameters );
//    }
//
//	/// Preparation of the certificate signing request (CSR)
//	/// <param name="InfosCertificates">Concatenated information used for the
//	/// certificate application
//	/// <param name="NomCle">Nickname of the certificate and keys
//	/// <returns>CSR encoded in Base64
//	public static String CsrEcdsaPreparation( String InfosCertificates, String KeyName )
//    {
//        CngKey PublicPrivateKeys;
//
//        // Reading the key pair in the key store
//        if( CngKey.Exists( KeyName ) )
//            PublicPrivateKeys = CngKey.Open( KeyName );
//        else
//            throw new Exception( "The key " + KeyName + " does not exist. You have to create it first - La clé " + KeyName + " n'existe pas. Vous devez la créer d'abord." );
//
//        // Creating the CSR
//        using( ECDsaCng ecdsa = new ECDsaCng( PublicPrivateKeys ) )
//        {
//            CertificateRequest req = new CertificateRequest( InfosCertificates, ecdsa, HashAlgorithmName.SHA256 );
//            req.CertificateExtensions.Add( new X509BasicConstraintsExtension( false, false, 0, false ) );
//            req.CertificateExtensions.Add( new X509EnhancedKeyUsageExtension( new OidCollection { new Oid( "1.3.6.1.5.5.7.3.8" ) }, true ) );
//            req.CertificateExtensions.Add( new X509KeyUsageExtension( X509KeyUsageFlags.DigitalSignature | X509KeyUsageFlags.NonRepudiation, false ) );
//            req.CertificateExtensions.Add( new X509SubjectKeyIdentifierExtension( req.PublicKey, false ) );
//
//            return Convert.ToBase64String( req.CreateSigningRequest( ), Base64FormattingOptions.InsertLineBreaks );
//        }
//    }

	/// Generates a digital signature
	/// <param name="TextToSign">Text to be signed
	/// <param name="CertificateSerialNumberSRS">Serial number of the certificate to
	/// be used
	/// <returns>Digital signature
	public static byte[ ] getSignedText(String textToSign)
    {
        byte[] bytesToSign = textToSign.trim().getBytes(StandardCharsets.UTF_8);			//the text to sign need to be utf-8 first
        PrivateKey privateKey = getPrivateKeyFromKeyStore(MevUtils.ALIAS_CERTIFICAT_SEV, MevUtils.KEYSTORE_KEY);	//get out the private key

        if (privateKey == null) {
            throw new IllegalArgumentException("The private key could not be retrieved.");
        }
		try {
			Signature signature = Signature.getInstance("SHA256withECDSA"); // are we supposed to use this "SHA256withECDSA" type signature?
			signature.initSign(privateKey); 								// init the Signature class with the private key.
			signature.update(bytesToSign);									// update the signature with the private key.

			return convertirSignatureASN1AndroidVersP1363(signature.sign());										// ok, encript it.
		} catch (Exception e) {
			throw new IllegalArgumentException("The signature could not be signed.");
		}
    }
	
	private static PrivateKey getPrivateKeyFromKeyStore( String alias, String keyPassword) {
			//throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
		try {
			KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
			keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
			
			return (PrivateKey)keyStore.getKey(alias, MevUtils.KEYSTORE_KEY.toCharArray());
//			// Get the KeyPair Entry from the KeyStore
//			KeyStore.PasswordProtection privateKeyPasswordProtection = new KeyStore.PasswordProtection("".toCharArray());
//			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("_ALIAS_CERTIFICAT.SEV", privateKeyPasswordProtection);
//			
//			keyStore.store(new FileOutputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
//			return privateKeyEntry.getPrivateKey();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	 private static PrivateKey getPirvateKey() {
		try {
			KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
			keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
			
			// Get the KeyPair Entry from the KeyStore
			KeyStore.PasswordProtection privateKeyPasswordProtection = new KeyStore.PasswordProtection("".toCharArray());
			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("_ALIAS_CERTIFICAT.SEV", privateKeyPasswordProtection);
			
			keyStore.store(new FileOutputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
			return privateKeyEntry.getPrivateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	 
	/// Validate a digital signature
	/// <param name="Text">Text to be validated
	/// <param name="Signature">Signature to be verified
	/// <param name="Certificate">Certificate to be used
	/// <returns>true if the signature is valid, otherwise false
	public static boolean validateSignature(String text, byte[] signature, X509Certificate certificate) {
        byte[] bytesToVerify = text.trim().getBytes(StandardCharsets.UTF_8);
        PublicKey publicKey = certificate.getPublicKey();
        if (publicKey == null) {
            throw new IllegalArgumentException("The certificate must have a public key.");
        }
        
        try {
	        Signature verifier = Signature.getInstance("SHA256withECDSA");
	        verifier.initVerify(publicKey);
	        verifier.update(bytesToVerify);
	
	        return verifier.verify(signature);
        }catch (Exception e) {
        	return false;
        }
    }
	
	////////////////////
	/**
	 * Convertir une signature numérique du format ASN.1 vers le format P1363
	 * 
	 * @param bSignatureOriginale Signature formatée en ASN.1
	 * @return Signature formatée en P1363
	 */
	private static byte[] convertirSignatureASN1AndroidVersP1363(final byte[] bSignatureOriginale) {
		byte[] bSignature = new byte[64];
		// Extraction des parties R et S de la signature originale
		final byte[] bR = extraireR(bSignatureOriginale);
		final byte[] bS = extraireS(bSignatureOriginale);
		// Assemblage des parties R et S pour former la signature numérique
		System.arraycopy(bR, 0, bSignature, 0, 32);
		System.arraycopy(bS, 0, bSignature, 32, 32);
		return bSignature;
	}

	/**
	 * Extraction de la partie 'R' d’une signature
	 * 
	 * @param signature Signature numérique
	 * @return Partie 'R' de la signature
	 */
	private static byte[] extraireR(final byte[] signature) {
		byte[] bR = new byte[32];
		final int debutR = (signature[1] & 0x80) != 0 ? 3 : 2;
		final int longueurR = signature[debutR + 1];
		final byte[] bTemp = Arrays.copyOfRange(signature, debutR + 2, debutR + 2 + longueurR);
		if (bTemp.length == 33)
			System.arraycopy(bTemp, 1, bR, 0, 32);
		else if (bTemp.length <= 32)
			// Remplissage à gauche avec des 0 s’il y a 32 byte
			System.arraycopy(bTemp, 0, bR, 32 - bTemp.length, bTemp.length);
		return bR;
	}

	/**
	 * Extraction de la partie 'S’ d’une signature
	 * 
	 * @param signature Signature numérique
	 * @return Partie 'S’ de la signature
	 */
	private static byte[] extraireS(final byte[] signature) {
		byte[] bS = new byte[32];
		final int debutR = (signature[1] & 0x80) != 0 ? 3 : 2;
		final int longueurR = signature[debutR + 1];
		final int debutS = debutR + 2 + longueurR;
		final int longueurS = signature[debutS + 1];
		final byte[] bTemp = Arrays.copyOfRange(signature, debutS + 2, debutS + 2 + longueurS);
		if (bTemp.length == 33)
			System.arraycopy(bTemp, 1, bS, 0, 32);
		else if (bTemp.length <= 32)
			// Remplissage à gauche avec des 0 s’il y a 32 byte
			System.arraycopy(bTemp, 0, bS, 32 - bTemp.length, bTemp.length);
		return bS;
	}

}