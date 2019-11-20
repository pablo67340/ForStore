package website.bryces.forstore.main;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bryce Wilkinson
 */
public class Main {

    private Boolean isLoading = false, watchDog = false;
    private String destination = "";
    private String[] args = new String[7];
    private long oldSize;
    private File target = null;
    private String workingDir = "";

    public Main(String[] input) {
        args = input;
        //args[0] = "GUIShop.jar";
        //args[1] = "-w";
        //args[2] = "-d";
        //args[3] = "C:/Users/Bryce/Desktop/1.14.4";
        //args[4] = "-s";
        //args[5] = "-wd";
        //args[6] = "C:/Users/Bryce/Desktop/GStore/";
    }

    public void launch() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //do your stuff
                if (serverProcess != null) {
                    serverProcess.destroyForcibly();
                }
            }
        });

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

        if (Arrays.stream(args).anyMatch("-wd"::equals)) {
            int dataValue = Arrays.asList(args).indexOf("-wd");
            dataValue += 1;
            this.workingDir = args[dataValue]+"/";
        } else {
            this.workingDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("ForStore.jar", "");
        }

        // TODO: change to take in param
        System.out.println("Selected File: " + workingDir + args[0]);
        File folder = new File(workingDir);
        File tmp = new File(workingDir + "/tmp");
        this.target = new File(workingDir + args[0]);

        if (!this.target.canRead()) {
            System.out.println("Target file was null!");
            System.exit(1);
            return;
        }

        try {
            FileUtils.deleteDirectory(tmp);
        } catch (Exception ex) {
            System.out.println("Error deleting temp folder: " + ex.getMessage());
        }
        tmp.mkdir();

        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            try {
                for (File file : listOfFiles) {
                    String ext = file.getName();
                    ext = ext.substring(ext.lastIndexOf(".") + 1);

                    if (ext.contains("jar") && ext.length() > 3) {
                        System.out.println("Discovered leftover temporary zip. Deleting...");
                        file.delete();
                    }

                    if (file.isFile() && ext.equalsIgnoreCase("jar") && !file.getName().equalsIgnoreCase(args[0]) && !file.getName().equalsIgnoreCase("ForStore.jar")) {
                        System.out.println("Discovered File: " + file.getName());
                        ZipFile zipFile = new ZipFile(workingDir + "/" + file.getName());
                        zipFile.extractAll(tmp.getPath());
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
            System.out.println("Injecting Resources into target jar...");
            ZipFile file = new ZipFile(zipFile);
            ZipParameters params = new ZipParameters();
            params.setIncludeRootFolder(false);
            file.addFolder(tempFile, params);
            System.out.println("Injection complete.");
            oldSize = this.target.length();
        } catch (ZipException ex) {
            System.out.println("Error adding Folder: " + ex.getMessage());
        }

        // Move the archive to the target location
        if (Arrays.asList(args).contains("-d")) {
            copyArtifact(this.target);
        } else {
            runWatcher();
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    // This is being run in a seperate thread
    // thus, halting the thread is valid.
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
        // Remove the loading cursor from the previous animation.
        System.out.print("\r");
    }

    public void runWatcher() {
        System.out.println("Watchdog process Started...");
        //Thread t1 = new Thread(() -> {
        //    runAnim();
        //});
        //t1.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                File refreshedValues = new File(target.getPath());

                if (refreshedValues.length() != oldSize) {
                    stopAnim();

                    System.out.println("Filesize change detected. Halting Server...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Process result = serverProcess.destroyForcibly();

                    System.out.println("Server Halted.");

                    timer.cancel();
                    launch();
                }
                refreshedValues = null;
            }
        }, 0, 1000);
    }

    Process serverProcess;

    public void launchServer(String path) {
        System.out.println("Starting Server located in: " + path);
        Thread t1 = new Thread(() -> {
            System.out.println("cd " + path);
            ProcessBuilder builder2 = new ProcessBuilder("java", "-jar", "minecraft.jar", "!Xms2G !Xmx2G nogui");
            File directory = new File(path);
            builder2.directory(directory);
            builder2.redirectOutput(Redirect.INHERIT);
            builder2.redirectError(Redirect.INHERIT);
            try {
                serverProcess = builder2.start();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        t1.start();
        System.out.println("java -jar " + path + "/minecraft.jar !Xmx2G !Xms2G");
        runWatcher();
    }

    public void copyArtifact(File input) {
        System.out.println("Copying artifact...");
        File dest = new File(this.destination + "/plugins/" + args[0]);

        try {
            FileUtils.copyFile(input, dest);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Copy Complete.");

        if (Arrays.asList(args).contains("-s") && Arrays.asList(args).contains("-d")) {
            int dataValue = Arrays.asList(args).indexOf("-d");
            dataValue += 1;
            launchServer(args[dataValue]);
        } else {
            runWatcher();
        }
    }
}
