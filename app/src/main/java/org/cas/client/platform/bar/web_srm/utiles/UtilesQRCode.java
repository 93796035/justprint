package org.cas.client.platform.bar.web_srm.utiles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.web_srm.Transaction;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.casutil.L;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UtilesQRCode {

    public static Bitmap getQRCodeImage(String data, int height, int width, int margin) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, margin);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // 7% error correction

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width - margin * 2, height - margin * 2, hints);

        Bitmap qrCodeImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(qrCodeImage);

        // 设置背景颜色为白色
        canvas.drawColor(Color.WHITE);

        // 设置二维码颜色为黑色
        int matrixWidth = bitMatrix.getWidth();
        for (int x = 0; x < matrixWidth; x++) {
            for (int y = 0; y < matrixWidth; y++) {
                if (bitMatrix.get(x, y)) {
                    canvas.drawRect(x + margin, y + margin, x + margin + 1, y + margin + 1, new android.graphics.Paint(Color.BLACK));
                }
            }
        }

        return qrCodeImage;
    }

    
    // Creation of the hyperlink to be encoded in the QR code
    // <param name="transaction">Transaction to be used to create the hyperlink
    // <param name="CertificateSerialNumberWebSRM">Serial number of the WEB-SRM certificate to extract its public key
    // <returns>Hyperlink to be encoded in the QR code
    public static String createUrlQRCode(Transaction transaction, String certificateSerialNumberWebSRM) {
        StringBuilder texteConcatener = new StringBuilder();
        texteConcatener.append(transaction.EmprCertifSEV);
        texteConcatener.append(transaction.DatTrans);
        texteConcatener.append(transaction.TPS);
        texteConcatener.append(transaction.TVQ);
        texteConcatener.append(transaction.ApresTax);
        texteConcatener.append(transaction.MTDU);
        texteConcatener.append(transaction.NoTPS);
        texteConcatener.append(transaction.NoTVQ);
        texteConcatener.append(transaction.ModImpr);
        texteConcatener.append(transaction.ModTrans);
        texteConcatener.append(transaction.signa.Actu);
        texteConcatener.append(transaction.signa.Preced);
        texteConcatener.append(String.format("%1$10s", transaction.NoTrans).replace(' ', '='));

        // Reading the public key contained in the WEB-SRM's certificate
        RSAPublicKey publicKeyWEBSRM = KeyStoreUtils.GetRSAPublicKeyWEBSRM(certificateSerialNumberWebSRM);

        // Encrypting information
        byte[] bUrlChiffree = encryptData(publicKeyWEBSRM, texteConcatener.toString());

        // Base64 encoding
        String strUrlChiffreeBase64 = Base64.getEncoder().encodeToString(bUrlChiffree);

        // Url safe encoding and return the hyperlink
        try {
            return ValuePossible.UrlMEVWEB.QRCode + URLEncoder.encode(strUrlChiffreeBase64, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            L.e("UtilesQRCode", "exception when createUrlQRCode for " + BarOption.getShopName(), e);
            return "";
        }
    }

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
