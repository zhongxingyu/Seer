 package com.ai.myplugin.sensor;
 
 import com.ai.bayes.plugins.BNSensorPlugin;
 import com.ai.bayes.scenario.TestResult;
 import com.ai.myplugin.util.EmptyTestResult;
 import com.ai.myplugin.util.Rest;
 import com.ai.util.resource.TestSessionContext;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.simple.JSONObject;
 
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * Created by User: veselin
  * On Date: 21/10/13
  */
 
 /*
 http://www.overstromingsvoorspeller.be/default.aspx?path=NL/Actuele_Info/PluviograafTabel&KL=nl&mode=P
 <td style="border: 1px solid #dddddd"><a title="Link naar grafieken" href="&#xD;&#xA;default.aspx?path=NL/Kaarten/puntdetail&amp;XMLFileArg=HYD-P05">HYD-P05-R</a></td>
       <td style="border: 1px solid #dddddd">Neerslag Vinderhoute</td>
       <td style="border: 1px solid #dddddd">Gentse Kanalen</td>
       <td style="border: 1px solid #dddddd">1.21 mm</td>
       <td style="border: 1px solid #dddddd">0.40 mm</td>
       <td style="border: 1px solid #dddddd">19:45 21/10/2013</td>
     </tr>
 
 */
 
 @PluginImplementation
 public class WaterLevelSensor implements BNSensorPlugin{
     private static final Log log = LogFactory.getLog(WaterLevelSensor.class);
     public static final String LOCATION = "location";
     private String location = null;
     public static final String DAILY_THRESHOLD = "daily_threshold";
     private Integer dailyThreshold = null;
     public static final String TOTAL_THRESHOLD = "total_threshold";
     private Integer totalThreshold = null;
     private Integer dailyForecastThreshold = null;
     private Integer totalForecastThreshold = null;
 
     @Override
     public String[] getRequiredProperties() {
         return new String[]{LOCATION, DAILY_THRESHOLD, TOTAL_THRESHOLD};
     }
 
     @Override
     public void setProperty(String s, Object o) {
         if(LOCATION.equals(s))
             location = o.toString();
         else if(TOTAL_THRESHOLD.equals(s))
             totalThreshold = Integer.parseInt(o.toString());
         else if(DAILY_THRESHOLD.equals(s))
             dailyThreshold = Integer.parseInt(o.toString());
         else
             throw new RuntimeException("Property "+ s + " not in the required settings");
     }
 
     @Override
     public Object getProperty(String s) {
         if(LOCATION.equals(s))
             return location;
         else if(DAILY_THRESHOLD.equals(s))
             return dailyThreshold;
         else if(TOTAL_THRESHOLD.equals(s))
             return totalThreshold;
         else
             throw new RuntimeException("Property "+ s + " not in the required settings");
     }
 
     @Override
     public String getDescription() {
         return "Water level";
     }
 
     @Override
     public TestResult execute(TestSessionContext testSessionContext) {
         log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
 
         for(String property : getRequiredProperties()){
             if(getProperty(property) == null)
                 throw new RuntimeException("Required property "+property + " not defined");
         }
 
         boolean testSuccess = true;
         String stringToParse = "";
         double total = Double.MAX_VALUE;
         double daily = Double.MAX_VALUE;
         double totalForecast = Double.MAX_VALUE;
         double dailyForecast = Double.MAX_VALUE;
 
         String pathURL = "http://www.overstromingsvoorspeller.be/default.aspx?path=NL/Actuele_Info/Neerslagtabellen&XSLTArg_TableID=benedenschelde&XSLTArg_ShowAll=1";
         try{
             stringToParse = Rest.httpGet(pathURL);
             log.debug(stringToParse);
         } catch (Exception e) {
             testSuccess = false;
         }
         if(testSuccess){
             int len = stringToParse.indexOf(location);
             if(len > 0){
                 String tmp = stringToParse.substring(len);
                 tmp = tmp.replaceAll("#dddddd\">", "|");
                 StringTokenizer stringTokenizer = new StringTokenizer(tmp, "|");
                 if(stringTokenizer.countTokens() > 6){
                     stringTokenizer.nextToken();
                     stringTokenizer.nextToken();
                     stringTokenizer.nextToken();
                     String totalString = stringTokenizer.nextToken();
                     String dailyString = stringTokenizer.nextToken();
                     String dailyForecastString = stringTokenizer.nextToken();
                     String totalForecastString = stringTokenizer.nextToken();
                     total = Double.parseDouble(totalString.substring(0, totalString.indexOf("mm")).trim());
                     daily = Double.parseDouble(dailyString.substring(0, dailyString.indexOf("mm")).trim());
                    totalForecast = Double.parseDouble(totalForecastString.substring(0, totalForecastString.indexOf("mm")).trim());
                    dailyForecast = Double.parseDouble(dailyForecastString.substring(0, dailyForecastString.indexOf("mm")).trim());
                     log.info("DAILY: "+daily + ", TOTAL:" + total + ", DAILY FORECAST: "+daily + ", TOTAL FORECAST:" + total);
                 }
             }
 
         }
         if(testSuccess && daily != Double.MAX_VALUE && daily != Double.MAX_VALUE)  {
             String ret = "No Alarm";
             if(daily > Integer.parseInt(getProperty(DAILY_THRESHOLD).toString()) ||
                     total > Integer.parseInt(getProperty(TOTAL_THRESHOLD).toString()))
                 ret = "Alarm";
             final String finalRet = ret;
             final double finalDaily = daily;
             final double finalTotal = total;
             final double finalForecastDaily = dailyForecast;
             final double finalForecastTotal = totalForecast;
             return new TestResult() {
                 @Override
                 public boolean isSuccess() {
                     return true;
                 }
 
                 @Override
                 public String getName() {
                     return "Water level result";
                 }
 
                 @Override
                 public String getObserverState() {
                     return finalRet;
                 }
 
                 @Override
                 public List<Map<String, Number>> getObserverStates() {
                     return null;
                 }
 
                 @Override
                 public String getRawData() {
                     JSONObject jsonObject = new JSONObject();
                     jsonObject.put("dailyLevel", new Double(finalDaily));
                     jsonObject.put("totalLevel", new Double(finalTotal));
                     jsonObject.put("dailyForecastLevel", new Double(finalForecastDaily));
                     jsonObject.put("totalForecastLevel", new Double(finalForecastTotal));
                     return jsonObject.toJSONString();
                 }
             };
 
 
         }
         else return new EmptyTestResult();
     }
 
     @Override
     public String getName() {
         return "WaterLevelSensor";
     }
 
     @Override
     public String[] getSupportedStates() {
         return new String[] {"No Alarm", "Alarm"};
     }
 
     @Override
     public void shutdown(TestSessionContext testSessionContext) {
         log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
     }
 
     public static void main(String []args){
         WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
         waterLevelSensor.setProperty(LOCATION, "PDM-438-R");
         waterLevelSensor.setProperty(DAILY_THRESHOLD, 15);
         waterLevelSensor.setProperty(TOTAL_THRESHOLD, 1);
         TestResult testResult = waterLevelSensor.execute(null);
         log.info(testResult.getObserverState());
         log.info(testResult.getRawData());
     }
 }
 
