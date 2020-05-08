 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RSLBench.Algorithms.DSA;
 
 import RSLBench.Assignment.DCOP.DCOPAgent;
 import RSLBench.Assignment.DCOP.DCOPSolver;
 import RSLBench.Constants;
 import java.util.List;
 import rescuecore2.standard.entities.StandardEntityURN;
 
 /**
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class DSA extends DCOPSolver {
 
     /**
      * The probability that an agent changes his assigned target.
      */
     public static final String KEY_DSA_PROBABILITY = "dsa.probability";
 
     @Override
     public String getIdentifier() {
         return "DSA";
     }
 
     @Override
     protected DCOPAgent buildAgent(StandardEntityURN type) {
         switch(type) {
             case FIRE_BRIGADE:
                return new DSAFireAgent();
             case POLICE_FORCE:
                return new DSAPoliceAgent();
             default:
                 throw new UnsupportedOperationException("The DSA solver does not support agents of type " + type);
         }
     }
 
     @Override
     public List<String> getUsedConfigurationKeys() {
         List<String> keys = super.getUsedConfigurationKeys();
         keys.add(Constants.KEY_INTERTEAM_COORDINATION);
         keys.add(Constants.KEY_BLOCKED_FIRE_PENALTY);
         keys.add(Constants.KEY_BLOCKED_POLICE_PENALTY);
         keys.add(KEY_DSA_PROBABILITY);
         return keys;
     }
 
 
 
 }
