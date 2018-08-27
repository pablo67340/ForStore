/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 *
 * @author Bryce
 */
public class Main {

    public Main() {

    }

    public void launch(String target) {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("ForStore.jar", "");
        System.out.println(path);
        File folder = new File(path);
        File tmp = new File(path + "/tmp");
        File targ = new File(path + target);
        tmp.mkdir();

        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            try {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().contains(".jar") && !file.getName().equalsIgnoreCase(target)) {
                        System.out.println("File " + file.getName());
                        ZipFile zipFile = new ZipFile(path + "/" + file.getName());
                        zipFile.extractAll(tmp.getPath());
                    } else if (file.isDirectory()) {
                        System.out.println("Directory " + file.getName());
                    }
                }
                ArrayList<File> filesFix = new ArrayList<>(Arrays.asList(tmp.listFiles()));
                System.out.println(path + target);

                addFilesToExistingZip(targ, tmp.listFiles());

            } catch (IOException | ZipException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();
        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk) {
            throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (File f : files) {
                if (f.getName().equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles) {
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams		
        zin.close();
        // Compress the files
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(files[i].getName()));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
        // Complete the ZIP file
    }
}
