package majhrs16.dl;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {
    public static String sha1(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }

        byte[] digest = md.digest();
        StringBuilder hexString = new StringBuilder();

        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1)
                hexString.append('0');

            hexString.append(hex);
        }

        return hexString.toString();
    }
}