 package ruby.togglesneak.asm;
 
 import java.io.File;
 import java.util.Map;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.settings.GameSettings;
 import net.minecraft.util.MovementInputFromOptions;
 
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
 import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
 
@MCVersion(value = "1.6.2")
 public class ToggleSneak implements IFMLLoadingPlugin
 {
     static File loc;
     @Override
     @Deprecated
     public String[] getLibraryRequestClass()
     {
         return null;
     }
 
     @Override
     public String[] getASMTransformerClass()
     {
         return new String[] {MovementInputFromOptionsTransformer.class.getName()};
     }
 
     @Override
     public String getModContainerClass()
     {
         return ToggleSneakContainer.class.getName();
     }
 
     @Override
     public String getSetupClass()
     {
         return null;
     }
 
     @Override
     public void injectData(Map<String, Object> data)
     {
         if (data.containsKey("coremodLocation"))
         {
             loc = (File) data.get("coremodLocation");
         }
     }
     
     private static int pressTime;
     private static int time;
 
     public static void hook(MovementInputFromOptions mifo,GameSettings gameSettings)
     {
         mifo.moveStrafe = 0.0F;
         mifo.moveForward = 0.0F;
 
         if (gameSettings.keyBindForward.pressed)
         {
             ++mifo.moveForward;
         }
 
         if (gameSettings.keyBindBack.pressed)
         {
             --mifo.moveForward;
         }
 
         if (gameSettings.keyBindLeft.pressed)
         {
             ++mifo.moveStrafe;
         }
 
         if (gameSettings.keyBindRight.pressed)
         {
             --mifo.moveStrafe;
         }
 
         mifo.jump = gameSettings.keyBindJump.pressed;
 
         if (mifo.sneak && gameSettings.keyBindSneak.pressed)
         {
             ++time;
         }
         else
         {
             if (mifo.sneak && time > 10)
             {
                 mifo.sneak = false;
             }
 
             time = 0;
         }
 
         if (!Minecraft.getMinecraft().thePlayer.capabilities.isFlying
                 && gameSettings.keyBindSneak.pressed
                 && pressTime != gameSettings.keyBindSneak.pressTime)
         {
             mifo.sneak = !mifo.sneak;
             pressTime = gameSettings.keyBindSneak.pressTime;
         }
         else if (Minecraft.getMinecraft().thePlayer.capabilities.isFlying)
         {
             mifo.sneak = gameSettings.keyBindSneak.pressed;
         }
 
         if (mifo.sneak)
         {
             mifo.moveStrafe = (float)((double) mifo.moveStrafe * 0.3D);
             mifo.moveForward = (float)((double) mifo.moveForward * 0.3D);
         }
     }
 }
