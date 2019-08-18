/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package website.bryces.forstore.main;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 *
 * @author Bryce
 */
public class Main {

    private Boolean watchDog = false;
    private Boolean isLoading = false;
    private String destination = "";
    private Process server;
    private final String[] args;
    private long oldSize;
    private File target = null;

    public Main(String[] input) {
        args = input;
    }

    public void launch() {

        if (watchDog != true) {
            watchDog = Arrays.stream(args).anyMatch("-w"::equals);
            if (watchDog) {
                System.out.println("Injection launched with watchdog enabled. Builds will automatically recompile 1 second after a file size change is detected.");
            }
        }

        if (Arrays.stream(args).anyMatch("-d"::equals)) {
            int dataValue = Arrays.asList(args).indexOf("-d");
            dataValue += 1;
            this.destination = args[dataValue];
        }

        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("ForStore.jar", "");
        System.out.println("Selected File: " + path + args[0]);
        File folder = new File(path);
        File tmp = new File(path + "/tmp");
        this.target = new File(path + args[0]);

        if (!this.target.canRead()) {
            System.out.println("Target file was null!");
            System.exit(1);
            return;
        }

        tmp.mkdir();

        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            try {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().contains(".jar") && !file.getName().equalsIgnoreCase(args[0])) {
                        System.out.println("Discovered File: " + file.getName());
                        ZipFile zipFile = new ZipFile(path + "/" + file.getName());
                        zipFile.extractAll(tmp.getPath());
                    } else if (file.isDirectory()) {
                        System.out.println("Directory: " + file.getName());
                    }
                }
                addFilesToExistingZip(this.target, tmp);

            } catch (ZipException e) {
                System.out.println("Error adding file: " + e);
            }
        }
    }

    public void addFilesToExistingZip(File zipFile, File tempFile) {

        try {
            System.out.println("Adding folder to zip!");
            ZipFile file = new ZipFile(zipFile);
            ZipParameters params = new ZipParameters();
            params.setIncludeRootFolder(false);
            file.addFolder(tempFile, params);
            System.out.println("Operation complete.");
            oldSize = this.target.length();
        } catch (ZipException ex) {
            System.out.println("Error adding Folder: " + ex.getMessage());
        }

        // Complete the ZIP file
        if (Arrays.asList(args).contains("-d")) {
            copyArtifact(this.target);
        } else {
            runWatcher();
        }

    }

    public void runAnim() {

        isLoading = true;
        int index = 0;
        try {
            while (isLoading) {
                switch (index) {
                    case 0:
                        System.out.print("\r|");
                        Thread.sleep(100);
                        break;
                    case 1:

                        System.out.print("\r/");
                        Thread.sleep(100);
                        break;
                    case 2:

                        System.out.print("\r-");
                        Thread.sleep(100);

                        break;
                    case 3:

                        System.out.print("\r\\");
                        Thread.sleep(100);

                        index = -1;
                        break;
                    default:
                        break;
                }
                index += 1;
            }
        } catch (InterruptedException ex) {
            System.out.println("Error playing animation: " + ex.getMessage());
        }

    }

    public void stopAnim() {
        isLoading = false;
    }

    public void runWatcher() {
        System.out.println("Watchdog Started...");
        Thread t1 = new Thread(() -> {

            runAnim();

        });
        t1.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                File refreshedValues = new File(target.getPath());

                if (refreshedValues.length() != oldSize) {
                    stopAnim();
                    // Recompile
                    System.out.println("Filesize change detected.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    timer.cancel();
                    getServer().destroy();
                    launch();
                }
                refreshedValues = null;
            }
        }, 0, 1000);
    }

    public void launchServer(String path) {
        System.out.println("Starting Server located in: "+path);
        Thread t1 = new Thread(() -> {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", "java -jar " + path + "\\minecraft.jar !Xmx2G !Xms2G", "start");
            //builder.redirectErrorStream(true);
            File directory = new File(path);
            builder.directory(directory);
            try {
                Process process = builder.start();
                setServer(process);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        t1.start();
        System.out.println("java -jar " + path + "\\minecraft.jar !Xmx2G !Xms2G");
        runWatcher();
    }

    public void copyArtifact(File input) {
        System.out.println("Copying artifact...");
        File dest = new File(this.destination+"/"+args[0]);
        input.renameTo(dest);
        System.out.println("Move Complete.");

        if (Arrays.asList(args).contains("-s") && Arrays.asList(args).contains("-d")) {
            int dataValue = Arrays.asList(args).indexOf("-d");
            dataValue += 1;
            launchServer(args[dataValue]);
        } else {
            runWatcher();
        }

    }

    public void setServer(Process input) {
        this.server = input;
    }

    public Process getServer() {
        return this.server;
    }
}
