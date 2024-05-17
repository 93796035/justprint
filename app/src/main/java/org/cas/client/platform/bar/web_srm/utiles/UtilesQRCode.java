package org.cas.client.platform.bar.web_srm.utiles;

import com.just.print.util.L;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;

public class UtilesQRCode {
	
//    public static BufferedImage getQRCodeImage(String data, int height, int width, int margin) throws WriterException {
//        Map<EncodeHintType, Object> hints = new HashMap<>();
//        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // 7% error correction
//
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
//
//        BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        qrCodeImage.createGraphics();
//
//        Graphics2D graphics = (Graphics2D) qrCodeImage.getGraphics();
//        graphics.setColor(java.awt.Color.WHITE);
//        graphics.fillRect(0, 0, width, height);
//        graphics.setColor(java.awt.Color.BLACK);
//
//        int matrixWidth = bitMatrix.getWidth();
//        for (int x = 0; x < matrixWidth; x++) {
//            for (int y = 0; y < matrixWidth; y++) {
//                if (bitMatrix.get(x, y)) {
//                    graphics.fillRect(x + margin, y + margin, 1, 1);
//                }
//            }
//        }
//
//        return qrCodeImage;
//    }
    
    // Creation of the hyperlink to be encoded in the QR code
    // <param name="transaction">Transaction to be used to create the hyperlink
    // <param name="CertificateSerialNumberWebSRM">Serial number of the WEB-SRM certificate to extract its public key
    // <returns>Hyperlink to be encoded in the QR code
//    public static String createUrlQRCode(Transaction transaction, String certificateSerialNumberWebSRM) {
//        StringBuilder texteConcatener = new StringBuilder();
//        texteConcatener.append(transaction.EmprCertifSEV);
//        texteConcatener.append(transaction.DatTrans);
//        texteConcatener.append(transaction.TPS);
//        texteConcatener.append(transaction.TVQ);
//        texteConcatener.append(transaction.ApresTax);
//        texteConcatener.append(transaction.MTDU);
//        texteConcatener.append(transaction.NoTPS);
//        texteConcatener.append(transaction.NoTVQ);
//        texteConcatener.append(transaction.ModImpr);
//        texteConcatener.append(transaction.ModTrans);
//        texteConcatener.append(transaction.signa.Actu);
//        texteConcatener.append(transaction.signa.Preced);
//        texteConcatener.append(String.format("%1$10s", transaction.NoTrans).replace(' ', '='));
//
//        // Reading the public key contained in the WEB-SRM's certificate
//        RSAPublicKey publicKeyWEBSRM = KeyStoreUtils.GetRSAPublicKeyWEBSRM(certificateSerialNumberWebSRM);
//
//        // Encrypting information
//        byte[] bUrlChiffree = encryptData(publicKeyWEBSRM, texteConcatener.toString());
//
//        // Base64 encoding
//        String strUrlChiffreeBase64 = Base64.getEncoder().encodeToString(bUrlChiffree);
//
//        // Url safe encoding and return the hyperlink
//        try {
//            return ValuePossible.UrlMEVWEB.QRCode + URLEncoder.encode(strUrlChiffreeBase64, StandardCharsets.UTF_8.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            L.e("UtilesQRCode", "exception when createUrlQRCode for " + BarOption.getShopName(), e);
//            return "";
//        }
//    }

    // Method to encrypt data using the RSA public key
    private static byte[] encryptData(RSAPublicKey publicKey, String data) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            L.e("UtilesQRCode", "Exception when encryptDate with publicKey:" + publicKey + " and datta:" + data, e);
            return null;
        }
    }
}
