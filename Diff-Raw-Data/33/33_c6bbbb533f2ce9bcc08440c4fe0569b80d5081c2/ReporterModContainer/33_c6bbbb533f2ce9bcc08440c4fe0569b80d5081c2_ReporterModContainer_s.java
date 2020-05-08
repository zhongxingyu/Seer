 package jk_5.nailed.crashreporter;
 
 import com.google.common.eventbus.EventBus;
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.LoadController;
 import cpw.mods.fml.common.ModMetadata;
 
 import java.util.Arrays;
 
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
 }
