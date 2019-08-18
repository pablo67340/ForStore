/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package website.bryces.forstore.main;

import java.io.File;

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
        runWatcher();

    }

    public void runAnim() throws InterruptedException {
        isLoading = true;
        int index = 0;
        Timer timer = new Timer();
        while (isLoading) {
            switch (index) {
                case 0:
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.print("\r|");
                        }
                    }, 250);
                    break;
                case 1:
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.print("\r/");
                        }
                    }, 250);
                    break;
                case 2:
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.print("\r-");
                        }
                    }, 250);
                    break;
                case 3:
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.print("\r\\");
                        }
                    }, 250);
                    index = -1;
                    break;
                default:
                    break;
            }
            index += 1;
        }
    }

    public void stopAnim() {
        isLoading = false;
    }

    public void runWatcher() {
        System.out.println("Watchdog Started...");

        Thread t1 = new Thread(() -> {
            try {
                runAnim();
            } catch (InterruptedException ex) {
                System.out.println("Animation Interrupted: " + ex.getMessage());
            }
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
                    launch();
                }
                refreshedValues = null;
            }
        }, 0, 1000);
    }
}
