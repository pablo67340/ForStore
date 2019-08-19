# ForStore

ForStore is designed to help streamline your development process within a Minecraft Server infrastructure. It will package your plugin with it's dependencies & reload your server on the fly. 

# How to use

1. Download the jar from the [Releases](https://github.com/pablo67340/forstore/releases)

2. Move into a seperate folder. E.G desktop/Store

3. Copy all the dependencies you would like to inject into your plugin into your ForStore folder.

4. Copy your target plugin into your ForStore folder. It's best to set your IDE to export to this folder so your server can automatically restart after you export!

Your folder structure should look like:

-Store  
--ForStore.jar  
--Dependency1.jar  
--Dependency2.jar  
--TargetPlugin.jar  

5. Open CMD and navigate into your Store folder. (cd desktop/store)

6. Run 	`java -jar ForStore.jar TargetPlugin.jar`

ForStore will now inject your dependency classes into your target plugin. 

# Arguments
-d {destination path} : Sets ForStore to copy the finished product to this destination. (NOTE: This will automatically copy the plugin into your destination/plugins. This means its best to set this to your server's root so the plugin is put into your plugins folder automatically.)

-w : Enables watchdog process. This is a process you can enable that will automatically trigger injections.

-s : Enables Server rebooting inside the Watchdog process. After a build is triggered, your server will be halted and rebooted for fast testing.

In order to successfully setup the server process, you must have a run.bat located inside your destination folder. ForStore will run this in order to run your server. 

NOTE: Your server's run.bat MUST contain the line:
`title MServer`
This will allow ForStore to be able to target the process to be killed later. 

Example run.bat:
`title MServer
java -jar minecraft.jar !Xmx2G !Xms2G nogui`