 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.model.BayesianNetwork;
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.NodeSessionParams;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 @PluginImplementation
 public class RandomSensor implements BNSensorPlugin {
 
     private static final String NAME = "Random";
     private double res;
 
     private Map<String, Object> map = new ConcurrentHashMap<String, Object>();
 
     public TestResult execute(TestSessionContext testSessionContext) {
         String nodeName = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
         BayesianNetwork bayesianNetwork = (BayesianNetwork) testSessionContext.getAttribute(NodeSessionParams.BN_NETWORK);
 
         Map<String, Double> probs = bayesianNetwork.getPriors(nodeName);
 
         double [] coins = new double[probs.size()];
         double incr = 0;
         double value;
 
         System.out.println("assign priors for the game");
         int i = 0;
        String [] states = new String [probs.size()];
         for(String key : probs.keySet()){
             System.out.println("state " + key + " width probability " + probs.get(key));
             value = probs.get(key) ;
             coins[i] = value + incr;
             incr += value;
             System.out.println("for state " + key + " assign the coin value " + coins[i]);
            states[i] = key;
             i++;
         }
 
         res = Math.random();
        final String observedState = findStateForIndex(res, coins, states);
         System.out.println("state that will be injected is " + observedState);
         return new TestResult() {
             public boolean isSuccess() {
                 return true;
             }
 
             public String getName() {
                 return "Random Result";
             }
 
             public String getObserverState() {
                 return observedState;
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;
             }
 
             public String getRawData(){
                 return "{" +
                         "\"observedState\" : \"" +  observedState + "\" ," +
                         "\"randomValue\" : " +res +
                         "}";
             }
         } ;
     }
 
     private String findStateForIndex(double val, double[] coins, String[] states) {
         for(int i = 0; i< coins.length; i ++){
             if(val < coins [i]) {
                 return states[i];
             }
         }
         return states[coins.length - 1];
     }
 
 
     public String getName() {
         return NAME;
     }
 
     public String[] getSupportedStates() {
         return new String[] {};
     }
 
     public String[] getRequiredProperties() {
         return new String[]{};
     }
 
     public void setProperty(String string, Object obj) {
         map.put(string, obj);
     }
 
     public Object getProperty(String string) {
         return map.get(string);
     }
 
     public String getDescription() {
         return "Random Plugin Sensor that generates random states (with prob. distribution according to the priors)";
     }
 }
