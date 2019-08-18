/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package website.bryces.forstore.main;

/**
 *
 * @author Bryce
 */
public class ForStore {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main(args);

        String dest = "";
        if (args.length == 0) {
            System.out.println("Proper Usage: java -jar ForStore.jar {destination.jar}");
            System.out.println("=== Optional Arguments ===");
            System.out.println("-w    Watchdog Mode");
        } 
        main.launch();

    }

}
