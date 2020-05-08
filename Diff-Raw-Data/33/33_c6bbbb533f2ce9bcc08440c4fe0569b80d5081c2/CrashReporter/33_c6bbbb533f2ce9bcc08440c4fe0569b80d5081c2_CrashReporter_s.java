 package jk_5.nailed.crashreporter;
 
 import lombok.Getter;
 import net.minecraft.crash.CrashReport;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class CrashReporter {
 
     @Getter
     private static final Logger logger = LogManager.getLogger("CrashReporter");
 
     public static void report(CrashReport report){
         logger.info("Received report: " + report.getCrashCause().getMessage());
         report(report.getDescription(), report.getCompleteReport());
     }
 
     public static void report(String title, String text){
         try{
             kickAllPlayers();
         }catch(Exception e){
 
         }
 
         String link = null;
         for(PasteProvider provider : HandlerRegistry.getPasteProviders()){
             try{
                 link = provider.paste(title, text);
                 break;
             }catch(Throwable e){
                 e.printStackTrace();
             }
         }
 
         if(link == null){
             logger.error("No pastebin providers could handle the request");
             link = "<No link>";
         }
 
         logger.info("Posted the crashreport to " + link);
 
         for(NotificationHandler handler : HandlerRegistry.getNotificationHandlers()){
             try{
                 handler.notify(title, text, link);
             }catch(Throwable e){
                 e.printStackTrace();
             }
         }
     }
 
     private static void kickAllPlayers(){
         ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
         while(!manager.playerEntityList.isEmpty()){
            ((EntityPlayerMP) manager.playerEntityList.get(0)).playerNetServerHandler.func_147231_a(new ChatComponentText("Server Crashed!"));
         }
     }
 }
