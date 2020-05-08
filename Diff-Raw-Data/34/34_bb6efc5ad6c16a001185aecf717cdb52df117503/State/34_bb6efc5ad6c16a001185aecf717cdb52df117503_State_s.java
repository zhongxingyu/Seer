 package model_example;
 
 import java.util.*;
 
 import edu.berkeley.path.model_elements.*;
 import edu.berkeley.path.model_base.LogSink;
 
 public class State extends edu.berkeley.path.model_base.State {
   public long dummyState = 0;
 
   public Network nw;
   public DensityProfile denIn;
   public DensityProfile denOut;
   
   public DensitySource densrc;
   public NetworkSource netsrc;
   public DensitySink densnk;
   public LogSink logsnk;
   
   public State(edu.berkeley.path.model_base.Run run) {
     super(run);
 
     netsrc = (NetworkSource)(run.getSource("network"));
     densrc = (DensitySource)(run.getSource("density"));
     densnk = (DensitySink)(run.getSink("density"));
     logsnk = (LogSink)(run.getSink("log"));
     
     nw = netsrc.getNetwork();
     
     logsnk.debug("Initialized model_example.State", null);
   }
   
   public State update() {
     dummyState += 1;
 
     denIn = densrc.getDensityProfile();
     denOut = new DensityProfile();
    denOut.setId(3);
   
     for (Link link: (List<Link>)(List<?>)nw.getLinks()) {
       Double in = denIn.getVehiclesPerMeterOnLink(link);
     
       if (in != null) {
         denOut.setVehiclesPerMeterOnLink(link, in + 0.1);
       }
     }
 
     densnk.putDensityProfile(denOut);
     logsnk.debug("Updated model_example.State", null);
     
     return this;
   }
 
   public boolean valid() {
     return true;
   }
 }
