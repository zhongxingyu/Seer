 
 package jmxMultiQuery;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 import javax.management.OperationsException;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.CompositeType;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 public class jmxMonitor {
 	//private final PrintStream out;
 	private String url;
 	private int verbatim;
 	private JMXConnector connector;
 	private MBeanServerConnection connection;
 	private String warning, critical;
 	private String attributeName, infoAttribute, replaceName, useAttr;
 	private Long totalTime;
 	private String attributeKey, infoKey;
     private String methodName;
 	private String object;
 	private String username, password;
 	private List additionalArgs = new ArrayList<String>();
 	private List <String>calc = new ArrayList<String>();
 	private String [] samples;
 	private boolean disableWarnCrit = false;
     private Object defaultValue;
     private static final int RETURN_OK = 0; // 	 The plugin was able to check the service and it appeared to be functioning properly
 	private static final String OK_STRING = "JMX OK -"; 
 	private static final int RETURN_WARNING = 1; // The plugin was able to check the service, but it appeared to be above some "warning" threshold or did not appear to be working properly
 	private static final String WARNING_STRING = "JMX WARNING -"; 
 	private static final int RETURN_CRITICAL = 2; // The plugin detected that either the service was not running or it was above some "critical" threshold
 	private static final String CRITICAL_STRING = "JMX CRITICAL -"; 
 	private static final int RETURN_UNKNOWN = 3; // Invalid command line arguments were supplied to the plugin or low-level failures internal to the plugin (such as unable to fork, or open a tcp socket) that prevent it from performing the specified operation. Higher-level errors (such as name resolution errors, socket timeouts, etc) are outside of the control of plugins and should generally NOT be reported as UNKNOWN states.
 	private static final String UNKNOWN_STRING = "JMX UNKNOWN";
 	
 	private Object checkData;
 	private Object infoData;
 	List<Map<String,String>> jmxAttrList = new ArrayList <Map<String,String>>();
 	
 	public jmxMonitor(){
 	}
 	public jmxMonitor(jmxMonitor monitor, List jmxAttrList){
 		object = monitor.getObject();
 		url = monitor.getUrl();
 		disableWarnCrit = monitor.getdisableWarnCrit();
 		this.jmxAttrList = jmxAttrList;
 	}
 	public void printHelp() {
 		InputStream is = JMXMultiQuery.class.getClassLoader().getResourceAsStream("resources/jmxMultiQuery/HELP");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder help = new StringBuilder();
 		try{
 			while(true){
 				String s = reader.readLine();
 				if(s==null)
 					break;
 				help.append(s +"\n");
 			}
 		} catch (IOException e) {
 			help.append(e);
 		}finally{
 			try {
 				reader.close();
 			} catch (IOException e) {
 				help.append(e);
 			}
 			System.out.print(help.toString());
 		}	
 	}
 	public String getUrl() {
 		return url;
 	}
 	public String getUserName() {
 		return username;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public String getObject() {
 		return object;
 	}
 	public String getAttribute() {
 		return attributeName;
 	}
 	public boolean getdisableWarnCrit() {
 		return disableWarnCrit;
 	}
 	public String getValue() {
 		return checkData.toString();
 	}
 	public List searchForAdditionalMonitors () {
 		return additionalArgs;
 	}
 	
 	public void parseArgs(String [] args) throws ParseError, Exception {
 		try{
 			for(int i=0;i<args.length;i++){
 				String option = args[i];
 				if(option.equals("-help"))
 				{
 					printHelp();
 					System.exit(RETURN_UNKNOWN);
 				}
                 else if(option.equals("-U")) {
 					url = args[++i];
 				}
                 else if(option.equals("-O")) {
                 	StringBuilder sb = new StringBuilder(args[++i]);
                 	for(int j=++i;j<args.length;j++){
                 		if(!args[j].equals("-A")){
                 			sb.append(" "+ args[j]);
 	                		i++;
                 		} else {
                 			object = sb.toString();
                 			i=j-1;
                 			break;
                 		}
                 	}
 				}
                 else if(option.equals("-A")) {
 					attributeName = args[++i];
 				}
                 else if(option.equals("-I")) {
 					infoAttribute = args[++i];
 				}
                 else if(option.equals("-J")) {
 					infoKey = args[++i];
 				}
                 else if(option.equals("-K")) {
 					attributeKey = args[++i];
 				}
                 else if(option.equals("-M")) {
                     methodName = args[++i];
                 }
                 else if(option.startsWith("-v")) {
 					verbatim = option.length()-1;
 				}
                 else if(option.equals("-w")) {
 					warning = args[++i];
 				}
                 else if(option.equals("-c")) {
 					critical = args[++i];
 				}
                 else if(option.equals("-x")) {
                 	disableWarnCrit = true;
 				}
                 else if(option.equals("-r")) {
                 	replaceName = args[++i];
                 }
                 else if(option.equals("-samples")) {
                 	samples = args[++i].split(",");
                 }
                 else if(option.equals("-calc")) {
                 	calc.add(args[++i]);
                 }
                 else if(option.equals("-attrCalc")) {
                 	useAttr = args[++i];
                 }
                 else if(option.equals("-add")) {
                 	additionalArgs.add(args[++i]);
                 }
                 else if(option.equals("-username")) {
 					username = args[++i];
 				}
                 else if(option.equals("-password")) {
 					password = args[++i];
 				}
                 else if(option.equals("-default")) {
                     String strValue = args[++i];
                     try {
                         defaultValue = Long.valueOf(strValue);
                     }
                     catch(NumberFormatException e)
                     {
                         defaultValue = strValue;
                     } 
                 }
 			}
             if(url == null || object == null || (attributeName == null && methodName == null)){
             	printHelp();
             	throw new Exception("New Monitor - required options are missing");
             }        
 		}
         catch(Exception e) {
         	report(e);
 		} 
 	}
 	//Connect
 	public MBeanServerConnection connect(String url) {
     	try {
     		HashMap<String,String[]> env = new HashMap<String,String[]>();
     		String[] creds = new String[2];
     		creds[0] = username;
     		creds[1] = password;
     		env.put(JMXConnector.CREDENTIALS, creds);
     		JMXServiceURL serviceUrl = new JMXServiceURL(url);
     		connector = JMXConnectorFactory.connect(serviceUrl, env);
     		//JMXServiceURL serviceUrl = new JMXServiceURL("rmi", "", 0, url);
     	    //this.jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
     	    connection = connector.getMBeanServerConnection(); 
     	    return connection;
     	} catch (MalformedURLException e) {
     		System.err.println("\nURL Exception: " + e.getMessage());
     	} catch (IOException e) {
     		System.err.println("\nCommunication error: " + e.getMessage());
     		System.exit(1);
     	}
 		return null;
     }
 	// pull attribute value from jmx connection and object name
 	void check(MBeanServerConnection connection) throws Exception {
         Object value = null;
         try {
         	if (samples != null && samples.length == 3){
         		totalTime = (long) 0;
         		int sample = Integer.parseInt(samples[0]);
         		int delay = Integer.parseInt(samples[1]);
         		String algorithm = samples[2];
         		List<Long> values = new ArrayList();
         		for(int i=0; i<sample; i++){
 	        		Long startTime = System.nanoTime();
 	        		value = attributeName != null
 	                        ? connection.getAttribute(new ObjectName(object), attributeName)
 	                        : connection.invoke(new ObjectName(object), methodName, null, null);
 	                Thread.currentThread().sleep(delay);
 	                Long endTime = System.nanoTime();
 	                totalTime = endTime - startTime + totalTime;
 	                values.add((Long) value);
         		}
         		if(algorithm.matches("getDiffOverTime")){
         			value = getDiffOverTime(values, totalTime);
         		}
         	} else {
         		value = attributeName != null
         				? connection.getAttribute(new ObjectName(object), attributeName)
                         : connection.invoke(new ObjectName(object), methodName, null, null);
         	}
        }
        catch(Exception e)
        {
            if(defaultValue != null)
                value = defaultValue;
            else
                throw e;
        }

 		if(value instanceof CompositeDataSupport) {
            if(attributeKey ==null) {
                 throw new ParseError("Attribute key is null for composed data "+object);
             }
             checkData = ((CompositeDataSupport) value).get(attributeKey);
 		}
        else {
 			checkData = value;
 		}
 		
 		if(infoAttribute !=null){
 			Object infoValue = infoAttribute.equals(attributeName)
                     ? value :
                     connection.getAttribute(new ObjectName(object), infoAttribute);
 			if(infoKey !=null && (infoValue instanceof CompositeDataSupport) && verbatim<4){
                 infoData = ((CompositeDataSupport) value).get(infoKey); // todo: Possible bug? value <=> infoValue
 			}
             else {
 				infoData = infoValue;
 			}
 		}
 	}
 	
 	public Map<String, String> validate(){
 		int status;
 		String keyName, monitorValue = null;
 		if (!calc.isEmpty() && checkData instanceof Number){
 			for ( String c : calc){
 	    		String operator = (String) c.subSequence(0,1);
 	    		String expr = c.substring(1);
 	    		if (isInteger(expr))
 	    			checkData = calc(operator,expr);
 	    		else
 	    			checkData = calc(operator,findAttrValue(expr));
 			}
     	}		
 		Map<String,String> jmxAttr = new HashMap<String,String>();
 		if(critical != null && compare( critical )){
 			status = RETURN_CRITICAL;	
 			jmxAttr.put("status",String.valueOf(status));
 			jmxAttr.put("statusDesc",CRITICAL_STRING);
 		}else if (warning != null && compare( warning)){
 			status = RETURN_WARNING;
 			jmxAttr.put("status",String.valueOf(status));
 			jmxAttr.put("statusDesc",WARNING_STRING);
 		}else{
 			status = RETURN_OK;
 			jmxAttr.put("status",String.valueOf(status));
 			jmxAttr.put("statusDesc",OK_STRING);
 		}
 
         boolean shown = false;
         if(infoData==null || verbatim>=2){
             String thresholds;
         	if (!disableWarnCrit){
 	            if (warning != null){thresholds = ";"+ warning;} else{thresholds = ";";}
 	            if (critical != null){thresholds = thresholds +";"+ critical;} else{thresholds = thresholds +";";}
         	} else {
         		//append nothing for warning/critical thresholds to perf data.
         		thresholds = ";;";
         	}
         	
             if(attributeKey !=null) {
             	if (replaceName != null){
             		keyName = replaceName;
             	} else {
             		keyName = attributeName.toString()+'.'+attributeKey.toString();
             	}
             	jmxAttr.put("Attribute", keyName);
             	jmxAttr.put("Value", checkData.toString());
             	jmxAttr.put(keyName, checkData.toString());
                 if ( checkData instanceof Number) {
                     jmxAttr.put("perfData", keyName +"="+ checkData.toString() +thresholds+";;");
                 }
             }
             else {
             	keyName = replaceName != null ? replaceName : attributeName.toString();
             	jmxAttr.put("Attribute", keyName);
             	jmxAttr.put("Value", checkData.toString());
                 jmxAttr.put(keyName, checkData.toString());
                 if ( checkData instanceof Number) {
                     jmxAttr.put("perfData", keyName +"="+ checkData.toString()+thresholds+";;");
                 }
                 shown=true;
             }
         }
 
 		if(!shown && infoData!=null){
 			if(infoData instanceof CompositeDataSupport)
 				report((CompositeDataSupport)infoData);
 			else
 				jmxAttr.put("infoData", infoData.toString());
 		}
 		return jmxAttr;
 	}
 	
 	private boolean compare(String level) {		
 		if (warning != null && critical != null){
 			if(checkData instanceof Number && Double.parseDouble(warning) > Double.parseDouble(critical) ) {
 				Number check = (Number)checkData;
 				if(check.doubleValue()==Math.floor(check.doubleValue())) {
 					return check.doubleValue()<=Double.parseDouble(level);
 				} else {
 					return check.longValue()>=Long.parseLong(level);
 				}
 			}
 			if(checkData instanceof Number && Double.parseDouble(warning) < Double.parseDouble(critical) ) {
 				Number check = (Number)checkData;
 				if(check.doubleValue()==Math.floor(check.doubleValue())) {
 					return check.doubleValue()>=Double.parseDouble(level);
 				} else {
 					return check.longValue()>=Long.parseLong(level);
 				}
 			}
 		}
 			if(checkData instanceof Number ) {
 				Number check = (Number)checkData;
 				if(check.doubleValue()==Math.floor(check.doubleValue())) {
 					return check.doubleValue()>=Double.parseDouble(level);
 				} else {
 					return check.longValue()>=Long.parseLong(level);
 				}
 			}
 			if(checkData instanceof String) {
 				return checkData.equals(level);
 			}
 			if(checkData instanceof Boolean) {
 				return checkData.equals(Boolean.parseBoolean(level));
 			}
 			throw new RuntimeException(level + "is not of type Number,String or Boolean");
 		}
 
 	public int report(Exception ex) {
 		StringBuilder eReport = new StringBuilder();
 		if(ex instanceof ParseError){
 			eReport.append(UNKNOWN_STRING+" ");
 			eReport.append(reportException(ex, eReport));		
 			eReport.append(" Usage: check_jmx -help");
 			printHelp();
 			System.out.println(eReport.toString());
 			return RETURN_UNKNOWN;
 		}else{
 			eReport.append(CRITICAL_STRING+" ");
 			eReport.append(reportException(ex, eReport));		
 			eReport.append("");
 			printHelp();
 			System.out.println(eReport.toString());
 			return RETURN_CRITICAL;
 		}
 	}
 	
 	private String report(CompositeDataSupport data) {
 		CompositeType type = data.getCompositeType();
 		StringBuilder cData = new StringBuilder();
 		for(Iterator it = type.keySet().iterator();it.hasNext();){
 			String key = (String) it.next();
 			if(data.containsKey(key))
 				cData.append(key+'='+data.get(key));
 			if(it.hasNext())
 				cData.append(';');
 		}
 		return cData.toString();
 	}
 	private String reportException(Exception ex, StringBuilder eReport)
     {
         eReport.append(verbatim < 2
                 ? rootCause(ex).getMessage()
                 : ex.getMessage() + " connecting to " + object + " by URL " + url);
 
 		if(verbatim>=3)		
 			return eReport.append(ex.getStackTrace().toString()).toString();
 		return eReport.toString();
 	}
 	
 	private static Throwable rootCause(Throwable ex)
     {
         return ex.getCause() == null ? ex : rootCause(ex.getCause());
     }
 	
 	public boolean isInteger( String input ) {   
 	   try {   
 	      Integer.parseInt( input );   
 	      return true;   
 	   }   
 	   catch( Exception e){   
 	      return false;   
 	   }   
 	}  
 	
 	public Object calc(String operator, String number){
 		double newData = 0;
 		if(operator.equals("/")){
 			newData = Double.parseDouble(checkData.toString()) / Double.parseDouble(number);  
 		} else if(operator.equals("*")) {
 			newData = Double.parseDouble(checkData.toString()) * Double.parseDouble(number);
 		} else if(operator.equals("-")) {
 			newData = Double.parseDouble(checkData.toString()) - Double.parseDouble(number); 
 		} else if(operator.equals("+")) {
 			newData = Double.parseDouble(checkData.toString()) + Double.parseDouble(number); 
 		} else newData = 0;
 		return (Object)Double.parseDouble(decimalPlaces((float)newData));
 	}
 	public String findAttrValue(String attribute) {
 		for (Map<String, String> a: jmxAttrList){
 		    for (Map.Entry<String, String> entry : a.entrySet()){
 	    		if (entry.getKey().equals("Attribute") && entry.getValue().equals(attribute)){
 	    			return(a.get("Value"));
 	    		}
 	    	}
     	}
 		return "0";
 	}
 
 	private double getDiffOverTime(List<Long> values, Long time){
 		Float value = null;
 		value = (float) (values.get(values.size()-1) - values.get(0));
 		value = value/time;
 		return Double.parseDouble(decimalPlaces(value));
 	}
 	private String decimalPlaces(Float num){
 		DecimalFormat df = new DecimalFormat("###.##");
 		return df.format(num);
 	}
 }
