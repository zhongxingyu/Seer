 package jk_5.nailed.crashreporter;
 
 import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.LoadController;
 import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLModDisabledEvent;
 
 import java.util.Arrays;
import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class ReporterModContainer extends DummyModContainer {
 
     public ReporterModContainer(){
         super(new ModMetadata());
         ModMetadata meta = this.getMetadata();
         meta.modId = "CrashReporter";
         meta.name = "CrashReporter";
         meta.version = "0.1";
         meta.authorList = Arrays.asList("jk-5");
     }
 
     @Override
     public boolean registerBus(EventBus bus, LoadController controller){
         bus.register(this);
         return true;
     }

    @Subscribe
    public void onDisable(FMLModDisabledEvent event){
        CrashReporter.getLogger().info("-----");
        CrashReporter.getLogger().info("Disabling CrashReporter...");
        CrashReporter.getLogger().info("NOTE: This is not supported yet");
        CrashReporter.getLogger().info("-----");
    }

    @Override
    public Disableable canBeDisabled(){
        return Disableable.YES;
    }

    @Override
    public List<String> getOwnedPackages(){
        return Arrays.asList("jk_5.nailed.crashreporter", "jk_5.nailed.crashreporter.notificationhandlers", "jk_5.nailed.crashreporter.pasteproviders", "jk_5.nailed.crashreporter.transformer", "jk_5.nailed.crashreporter.transformer.asm");
    }
 }
