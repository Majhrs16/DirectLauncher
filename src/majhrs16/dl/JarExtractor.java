package majhrs16.dl;

import majhrs16.lib.utils.files.FilePath;

import java.util.zip.ZipInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.io.OutputStream;
import java.io.IOException;

public class JarExtractor {
    public static void extract(String jarFilePath, String destinationFolderPath) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                String filePath = FilePath.join(destinationFolderPath, entryName);

                if (entry.isDirectory()) {
                    FilePath.makedirs(filePath);

                } else {
                    try (OutputStream outputStream = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }
}