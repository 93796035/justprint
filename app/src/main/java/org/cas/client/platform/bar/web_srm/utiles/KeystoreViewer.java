package org.cas.client.platform.bar.web_srm.utiles;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Enumeration;

import org.cas.client.platform.bar.web_srm.MevUtils;

public class KeystoreViewer {

    public static void main(String[] args) {
        try {
            // Keystore的路径
            String keystorePath = "c:/aikapos/jre/lib/jckconfig";

            // Keystore的密码
            char[] keystorePassword = MevUtils.KEYSTORE_KEY.toCharArray();

            // 加载Keystore
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream is = new FileInputStream(keystorePath)) {
                keystore.load(is, keystorePassword);
            }

            // 列出Keystore中的条目
            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();

                // 显示别名
                System.out.println("Alias: " + alias);

                // 检查条目类型
                if (keystore.isKeyEntry(alias)) {
                    System.out.println("  Type: Key Entry");
                } else if (keystore.isCertificateEntry(alias)) {
                    System.out.println("  Type: Certificate Entry");
                    System.out.println("  Certificate: " + keystore.getCertificate(alias).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
