package org.cas.client.platform.bar.web_srm.utiles;

import org.cas.client.platform.casutil.L;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.HashMap;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.web_srm.MevUtils;

public class KeyStoreUtils {
	
    public static KeyPair getContentFromKeystore(String keystoreFilePath, String keystorePassword, String alias,
                                                 char[] privateKeyPassword) {
        try {
            // Load the KeyStore from the file
            KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
            FileInputStream fis = new FileInputStream(keystoreFilePath);
            keyStore.load(fis, keystorePassword.toCharArray());
            fis.close();

            // Get the KeyPair Entry from the KeyStore
            KeyStore.PasswordProtection privateKeyPasswordProtection = new KeyStore.PasswordProtection(privateKeyPassword);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, privateKeyPasswordProtection);

            // Retrieve the Public Key and Private Key from the KeyPair Entry
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            L.e("KeyStoreUtiles", "Exception when called getContentFromKeystore", e);
        }
        return null;
    }
    
    public static void saveContentToKeystore(String keystoreFilePath, String keystorePassword, String alias,
            char[] privateKeyPassword, Certificate certificate, PrivateKey privateKey) {
		try {
			// Create the KeyStore and load an existing or create a new KeyStore
			KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
            keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
			
			// Set the KeyPair Entry to save the content into the KeyStore
			KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificate});
			keyStore.setEntry(alias, privateKeyEntry, new KeyStore.PasswordProtection(privateKeyPassword));
			
			// Save the KeyStore to a file
			FileOutputStream fos = new FileOutputStream(keystoreFilePath);
			keyStore.store(fos, keystorePassword.toCharArray());
			fos.close();
			
			System.out.println("Content saved to KeyStore successfully.");
		} catch (Exception e) {
            L.e("KeyStoreUtiles", "Exception when called saveContentToKeystore", e);
			e.printStackTrace();
		}
	}
    
	private static HashMap<String, X509Certificate> certificates = new HashMap<>();
	/// Reading a certificate from the Windows certificate store
	/// <param name="CertificateSerialNumberSRS">Serial number of the certificate
	/// <returns>Certificate</returns>
	public static X509Certificate getCertificate(String certificateSerialNumberSRS) {
		if(certificateSerialNumberSRS == null) {
			return null;
		}
		X509Certificate certificate = certificates.get(certificateSerialNumberSRS);
		if(certificate != null)
			return certificate;
		
        try {
            KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
            keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                if (cert != null && certificateSerialNumberSRS.endsWith(cert.getSerialNumber().toString(16))) {
                    certificate = cert;
                    break;
                }
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            L.e("KeyStoreUtils", BarOption.getAddress() + "Failed to get certificate from KEYSTORE_FILE!", e);
        } 

        certificates.put(certificateSerialNumberSRS, certificate);
        return certificate;
	}

	/// Reading the thumbprint of a certificate
	/// <param name="CertificateSerialNumberSRS">Serial number of the certificate
	/// <returns>Certificate's thumbprint
	public static String GetCertificateThumbprint(String certificateSerialNumberSRS) {
        X509Certificate certificate = getCertificate(certificateSerialNumberSRS);

        if (certificate != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] thumbprintBytes = md.digest(certificate.getEncoded());
                StringBuilder thumbprintBuilder = new StringBuilder(); 

                for (byte b : thumbprintBytes) {
                    thumbprintBuilder.append(String.format("%02x", b));
                }

                return thumbprintBuilder.toString().toUpperCase();
            } catch (NoSuchAlgorithmException | CertificateException e) {
                L.e("KeyStoreUtiles", "Exception when called saveContentToKeystore", e);
                e.printStackTrace();
            }
        }

        return null;
	}

	/// Returns the RSA public key contained in the WEB-SRM's certificate.
	/// <param name="CertificateSerialNumberWebSRM">Serial number of the certificate
	/// <returns>Public key
	public static RSAPublicKey GetRSAPublicKeyWEBSRM(String certificateSerialNumberWebSRM) {
		X509Certificate certificate = getCertificate(certificateSerialNumberWebSRM);

        if (certificate != null) {
            PublicKey publicKey = certificate.getPublicKey();
            if (publicKey instanceof RSAPublicKey) {
                return (RSAPublicKey) publicKey;
            }
        }

        return null;
	}

//	/// Returns the expiration date of a certificate
//	/// <param name="CertificateSerialNumber">Serial number of the certificate
//	/// <returns>Expiration date
//	public static Date GetDateExpirationCertificate(String certificateSerialNumber) {
//        X509Certificate certificate = getCertificate(certificateSerialNumber);
//
//        if (certificate != null) {
//        	return Date.from(certificate.getNotAfter().toInstant());
//        }
//
//        return null;
//	}
//
//	/// Returns the number of remaining days of validity of a certificate
//	/// <param name="CertificateSerialNumber">Serial number of the certificate
//	/// <returns>Number of remaining days of validity
//	public static int GetRemainingDaysValidityCertificate(String certificateSerialNumber) {
//		X509Certificate certificate = getCertificate(certificateSerialNumber);
//
//        if (certificate != null) {
//            LocalDate now = LocalDate.now();
//            LocalDate expirationDate = certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            long remainingDays = ChronoUnit.DAYS.between(now, expirationDate);
//
//            return (int) remainingDays;
//        }
//
//        return -1; // Indicate certificate not found
//	}
//
//	/// Check if the validity of a certificate, according to its NotBefore and
//	/// NotAfter dates
//	/// <param name="CertificateSerialNumber">Serial number of the certificate
//	/// <returns>true if the current date is between the validity dates of the
//	/// certificate
//	public static boolean IsCertificatValideDate(String certificateSerialNumber) {
//        X509Certificate certificate = getCertificate(certificateSerialNumber);
//
//        if (certificate != null) {
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime notBefore = certificate.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            LocalDateTime notAfter = certificate.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//
//            return now.isAfter(notBefore) && now.isBefore(notAfter);
//        }
//
//        return false;
//	}
//
//	/// Time zone according to Universal Time Coordinated (UTC) - Eastern Daylight
//	/// Time (A) or Eastern Standard Time (N)
//	/// <returns>Time zone representation (-04:00A / -04:00N or -05:00A / -05:00N)
//	public static String GetTimeZoneDaylightStandardTime() {
//		ZoneId zoneId = ZoneId.systemDefault();
//        ZonedDateTime now = ZonedDateTime.now(zoneId);
//
//        boolean isDaylightSavingTime = zoneId.getRules().isDaylightSavings(now.toInstant());
//
//        String offset = zoneId.getRules().getOffset(now.toInstant()).toString();
//        offset = offset.substring(0, 6);
//
//        return offset + (isDaylightSavingTime ? "A" : "N");
//	}
//
//	/// Time zone according to Universal Time Coordinated (UTC)
//	/// <returns>Time zone representation (-04:00 or -05:00)
//	public static String GetTimeZoneHeure() {
//        OffsetDateTime utcDateTime = OffsetDateTime.now(ZoneOffset.UTC);
//        ZoneOffset offset = utcDateTime.getOffset();
//
//        int hours = offset.getTotalSeconds() / 3600;
//        int minutes = (Math.abs(offset.getTotalSeconds()) % 3600) / 60;
//
//        return String.format("%s%02d:%02d", offset.compareTo(ZoneOffset.UTC) < 0 ? "-" : "+", hours, minutes);
//	}
}
