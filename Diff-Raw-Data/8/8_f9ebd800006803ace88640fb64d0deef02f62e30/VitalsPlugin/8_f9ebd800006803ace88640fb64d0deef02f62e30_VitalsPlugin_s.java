 package com.volumetricpixels.vitals;
 
 import org.spout.api.command.annotated.AnnotatedCommandRegistrationFactory;
 import org.spout.api.command.annotated.SimpleInjector;
 import org.spout.api.plugin.CommonPlugin;
 
 import com.volumetricpixels.vitals.commands.AdminCommands;
 import com.volumetricpixels.vitals.commands.FunCommands;
 import com.volumetricpixels.vitals.commands.GeneralCommands;
 import com.volumetricpixels.vitals.commands.ProtectionCommands;
 import com.volumetricpixels.vitals.commands.WorldCommands;
 import com.volumetricpixels.vitals.configuration.VitalsConfiguration;
 
 public class VitalsPlugin extends CommonPlugin {
 
     private VitalsConfiguration config;
 
     @Override
     public void onEnable() {
         config = new VitalsConfiguration(getDataFolder());
 
         // Register commands.
         AnnotatedCommandRegistrationFactory commandRegistration = new AnnotatedCommandRegistrationFactory(new SimpleInjector(this));
         
         getEngine().getRootCommand().addSubCommands(this, AdminCommands.class, commandRegistration);
         getEngine().getRootCommand().addSubCommands(this, GeneralCommands.class, commandRegistration);
 
        if(VitalsConfiguration.ENABLE_PROTECTIONS.getBoolean(false)) {
             getEngine().getRootCommand().addSubCommands(this, ProtectionCommands.class, commandRegistration);
         }
 
        if(VitalsConfiguration.ENABLE_WORLD_MANAGEMENT.getBoolean(false)) {
             getEngine().getRootCommand().addSubCommands(this, WorldCommands.class, commandRegistration);
         }
 
        if(VitalsConfiguration.ENABLE_FUN_COMMANDS.getBoolean(true)) {
             getEngine().getRootCommand().addSubCommands(this, FunCommands.class, commandRegistration);
         }
 
         getLogger().info("[Vitals] v" + getDescription().getVersion() + " enabled!");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("[Vitals] v" + getDescription().getVersion() + " disabled!");
     }
 
     public VitalsConfiguration getConfig() {
         return config;
     }
 
 }
