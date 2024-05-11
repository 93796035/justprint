package org.cas.client.platform.casutil;

import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;

/**
 * This class provides the possibility to encrypt a clear text value.
 */
public class TaoEncrypt {

    private static final String ENCODING = "UTF-8";

    /**
     * Algorithm used for the secure random util
     */
    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * Cipher transformation
     */
    private static final String CIPHER_TRANSFORMATION = "PBEWithMD5AndDES/CBC/PKCS5Padding";

    /**
     * The algorithm used by the secret key factory
     */
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBEWithMD5AndDES";

    /**
     * Default password used for generating the 'secret'
     */
    private static final String DEFAULT_SECRET_GENERATION_PASSWORD = "iXuWGgrXe4o=";

    /**
     * Salt length. <!!> The length for the salt has to be 8 for the current selected encryption algorithm
     */
    private static final int SALT_LENGTH = 8;
    public static final String SALT = "dmfsJiaJdwz";

    /**
     * Password used in generating the 'secret' used for encryption
     */
    private static String secretGenerationPassword = TaoEncrypt.DEFAULT_SECRET_GENERATION_PASSWORD;

    /**
     * Generate a 'salt' that can be used later on for encryption
     * 
     * @return salt value
     * @throws NoSuchAlgorithmException
     */
    public String generateSalt() throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstance(TaoEncrypt.SECURE_RANDOM_ALGORITHM);
        final byte[] salt = new byte[TaoEncrypt.SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    public static void main(
            String[] args) {
        TaoEncrypt a = new TaoEncrypt();
        String salt = "dmfsJiaJdwz=";
        try {
            String password = a.encryptPassword("1234");
            //System.out.println(password);
            System.out.println(a.decrypt("zjID+4SAebvf2RJ0MSMlIg==", salt, 1));
        } catch (Exception e) {
        	e.printStackTrace();
        }

    }

    /**
     * Encrypt the provided clear text <code>value</code>.
     * <p>
     * If the value is <code>null</code> then an empty string will be returned
     * 
     * @param value
     *            clear text value to be encrypted
     * @return encrypted value
     */
    public static String encryptPassword(
            final String value) {
        try {
            final Cipher encrypter = createCipher(SALT, 1, Cipher.ENCRYPT_MODE);
            final byte[] encyptedValue = encrypter.doFinal(value.getBytes(TaoEncrypt.ENCODING));
            return Base64.encodeToString(encyptedValue, Base64.DEFAULT);
        } catch (BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | IOException
                | InvalidParameterSpecException e) {
            return value;
        }
    }

    public static String decrypt(
            final String value,
            final int iterationsCount) throws Exception {
    	return decrypt(value, "dmfsJiaJdwz=", iterationsCount);
    }
    /**
     * Decrypt the provided <code>value</code>
     * <p>
     * If the value is <code>null</code> then an empty string will be returned
     * 
     * @param value
     *            value to be decrypted
     * @param salt
     *            salt to be used for decryption
     * @param iterationsCount
     *            number of iterations used for decryption
     * @return clear text representation of the decrypted <code>value</code>
     */
    public static String decrypt(
            final String value,
            final String salt,
            final int iterationsCount) throws Exception {
        if (value == null || value.trim().length() == 0) {
            return "";
        }
        try {
            final int iterationsNumber = iterationsCount <= 0 ? 1 : iterationsCount;
            final Cipher decrypter = createCipher(salt, iterationsNumber, Cipher.DECRYPT_MODE);
            final byte[] decodedValue = Base64.decode(value, Base64.DEFAULT);
            final byte[] decryptedValue = decrypter.doFinal(decodedValue);
            return new String(decryptedValue, TaoEncrypt.ENCODING);
        } catch (BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | IOException
                | InvalidParameterSpecException e) {
            throw new Exception(e);
        }
    }

    /**
     * Creates a cipher. The cipher type (encrypter/decrypter) is dictated by the <code>mode</code> parameter. See
     * {@link Cipher} for possible values (ex: {@link Cipher#ENCRYPT_MODE}, {@link Cipher#DECRYPT_MODE})
     * 
     * @param salt
     *            salt to be used by the created cipher
     * @param iterationsNumber
     *            iterations number to be used by cipher in the encryption/decryption operations
     * @param mode
     *            cipher mode
     * @return the created cipher
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidParameterSpecException
     */
    private static Cipher createCipher(
            final String salt,
            final int iterationsNumber,
            final int mode)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        final PBEParameterSpec parameterSpec =
                new PBEParameterSpec(Base64.decode(salt, Base64.DEFAULT), iterationsNumber);
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(TaoEncrypt.SECRET_KEY_FACTORY_ALGORITHM);
        final SecretKey secretKey = secretKeyFactory
                .generateSecret(new javax.crypto.spec.PBEKeySpec(secretGenerationPassword.toCharArray()));
        final Cipher cipher = Cipher.getInstance(TaoEncrypt.CIPHER_TRANSFORMATION);
        cipher.init(mode, secretKey, parameterSpec);
        return cipher;
    }

    
}
