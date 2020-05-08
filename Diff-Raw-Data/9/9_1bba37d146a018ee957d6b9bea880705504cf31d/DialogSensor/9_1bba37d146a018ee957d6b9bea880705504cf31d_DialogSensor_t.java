 /**
  * created by: Veselin Pizurica
  * Date: 11/03/12
  */
 
 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.model.BayesianNetwork;
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.util.resource.NodeSessionParams;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import java.util.List;
 import java.util.Map;
 
 @PluginImplementation
 public class DialogSensor implements BNSensorPlugin {
     private static final Log log = LogFactory.getLog(DialogSensor.class);
 
     private String question = null;
     private static final String NAME = "Dialog";
    private static final String property = "Question";
 
     public String[] getRequiredProperties() {
        return new String[] {property};
     }
 
     @Override
     public String[] getRuntimeProperties() {
         return new String[]{};
     }
 
     public void setProperty(String string, Object obj) {
        if(property.equals(string))
            question = obj.toString();
     }
 
     public Object getProperty(String string) {
         return question;
     }
 
     public String getDescription() {
         return "Swing Sensor that let you define the return state in GUI";
     }
 
     private String getQuestion(String node){
         return question == null || question.startsWith("No Value")? "What is the result of " + node: question;
     }
 
     public TestResult execute(TestSessionContext testSessionContext) {
         final String nodeName = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
         final BayesianNetwork bayesianNetwork = (BayesianNetwork) testSessionContext.getAttribute(NodeSessionParams.BN_NETWORK);
         final int index = JOptionPane.showOptionDialog(null,
                 getQuestion(nodeName), "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                 bayesianNetwork.getStates(nodeName), bayesianNetwork.getStates(nodeName)[0]);
 
         return new TestResult() {
 
             public boolean isSuccess() {
                 return true;
             }
 
             public String getName() {
                 return "Swing Result";
             }
 
             public String getObserverState() {
                 return bayesianNetwork.getStates(nodeName)[index];
             }
 
             @Override
             public List<Map<String, Number>> getObserverStates() {
                 return null;
             }
 
             public String getRawData(){
                 return bayesianNetwork.toJSONString();
             }
 
         };
 
     }
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
         log.debug("Shutdown : " + getName() + ", sensor : " + this.getClass().getName());
     }
 
     public String getName() {
         return NAME;
     }
 
     public String[] getSupportedStates() {
         return new String[] {};
     }
 }
