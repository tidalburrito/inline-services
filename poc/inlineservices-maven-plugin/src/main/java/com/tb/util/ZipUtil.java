package com.tb.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipUtil {
    public static void updateFileInZip(String zipFilePath, String fileToUpdatePath, String updatedFilePath) throws IOException {
        File zipFile = new File(zipFilePath);
        File tempFile = File.createTempFile(zipFile.getName(), null);
        boolean fileUpdated = false;

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
             ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempFile))) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String currentEntryName = entry.getName();

                // Check if the entry matches the file to update
                if (currentEntryName.equals(fileToUpdatePath)) {
                    // Add the updated file to the archive
                    File updatedFile = new File(updatedFilePath);
                    addFileToZip(zipOutputStream, updatedFile, currentEntryName);
                    fileUpdated = true;
                } else {
                    // Copy the existing entry to the new archive
                    zipOutputStream.putNextEntry(new ZipEntry(currentEntryName));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, length);
                    }
                }
            }
        }

        // If the file was not updated, throw an exception
        if (!fileUpdated) {
            throw new FileNotFoundException("File to update not found in the ZIP archive");
        }

        // Replace the original ZIP file with the updated one
        Files.move(tempFile.toPath(), zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void addFileToZip(ZipOutputStream zipOutputStream, File file, String entryName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, length);
        }
        fileInputStream.close();
    }


    public static void appendToZip(String existingZipFilePath, String fileToAddPath, String entryName) throws IOException {
        // Create a temporary ZIP file
        Path tempZipFilePath = Files.createTempFile(null, null);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFilePath.toFile()))) {
            // Copy the contents of the existing ZIP file to the temporary ZIP file
            try (ZipFile existingZipFile = new ZipFile(existingZipFilePath)) {
                Enumeration<? extends ZipEntry> entries = existingZipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    zipOutputStream.putNextEntry(entry);

                    InputStream inputStream = existingZipFile.getInputStream(entry);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, bytesRead);
                    }

                    zipOutputStream.closeEntry();
                    inputStream.close();
                }
            }

            // Append the new file to the temporary ZIP file
            File fileToAdd = new File(fileToAddPath);
            if (fileToAdd.exists()) {
                ZipEntry newEntry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(newEntry);

                FileInputStream fileInputStream = new FileInputStream(fileToAdd);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }

                zipOutputStream.closeEntry();
                fileInputStream.close();
            }
        }

        // Replace the existing ZIP file with the updated one
        Files.move(tempZipFilePath, Paths.get(existingZipFilePath), StandardCopyOption.REPLACE_EXISTING);
    }
}
