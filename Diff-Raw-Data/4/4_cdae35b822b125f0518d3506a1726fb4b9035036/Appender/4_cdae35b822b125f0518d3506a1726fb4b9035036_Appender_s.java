 package cz.muni.pb138.log4j.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dom4j.DocumentFactory;
 import org.dom4j.Element;
 
 import cz.muni.pb138.log4j.AppUtils;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import org.apache.log4j.Level;
 
 public class Appender {
 
     private String name;
     private String className;
     private String layoutClassName = null;
     private boolean hasLayoutAlready = false;
     private String threshold;
     private Map<String, String> params = new HashMap<String, String>();
     private Map<String, String> layoutParams = new HashMap<String, String>();
     private ErrorHandler errorHandler = null;
     private List<String> appenderRefs = new ArrayList<String>();
     // nutne kôli (podla mna nepotrebnemu) logovaniu vo verify, martin by mohol povedat ci to nezmazeme
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Appender.class);
 
     public ErrorHandler createErrorHandler(
             String className,
             Map<String, String> params,
             List<String> loggers,
             String appender) {
 
         ErrorHandler e = new ErrorHandler();
 
         e.setClassName(className);
         if (params != null) {
             for (Map.Entry<String, String> p : params.entrySet()) {
                 e.addParam(p.getKey(), p.getValue());
             }
         }
         if (loggers != null) {
             for (String s : loggers) {
                 e.addLogger(s);
             }
         }
 
         e.setAppender(appender);
         return e;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getClassName() {
         return className;
     }
 
     public void setClassName(String className) {
         this.className = className;
     }
 
     public String getLayoutClassName() {
         return layoutClassName;
     }
 
     public void setLayoutClassName(String layoutClassName) {
         this.layoutClassName = layoutClassName;
     }
 
     public void setThreshold(String threshold) {
         this.threshold = threshold;
     }
 
     public String getThreshold() {
         return threshold;
     }
 
     public Map<String, String> getLayoutParams() {
         return layoutParams;
     }
 
     public void addLayoutParam(String key, String value) {
         if (layoutParams.get(key) == null) {
             layoutParams.put(key, value);
         } else {
             AppUtils.crash("Appender: '" + name + "' with two same layout params: " + key);
         }
     }
 
     public Map<String, String> getParams() {
         return params;
     }
 
     public void addParam(String key, String value) {
         if (params.get(key) == null) {
             params.put(key, value);
         } else {
             AppUtils.crash("Appender: '" + name + "' with two same params: " + key);
         }
     }
 
     public ErrorHandler getErrorHandler() {
         return errorHandler;
     }
 
     public void setErrorHandler(ErrorHandler errHand) {
         errorHandler = errHand;
     }
 
     public List<String> getAppenderRefs() {
         return appenderRefs;
     }
 
     public void addAppenderRef(String ref) {
         if (!appenderRefs.contains(ref)) {
             appenderRefs.add(ref);
         } else {
             AppUtils.crash("Appender: '" + name + "' with two same appender-ref: " + ref);
         }
     }
 
     public void addConfig(String key, String value) {
         if (key.equals(name)) {
             className = value;
         } else {
             String newKey = key.substring(name.length() + 1); // removing appenderName. from key
             if (newKey.toLowerCase().startsWith("layout")) {
                 // each appender has max. one "private" layout
                 String layout = newKey.substring(6);      // removing "layout" part without "."
                 if (layout.contains(".")) {
                     layoutParams.put(layout.substring(1), value);
                 } else {
                     if (hasLayoutAlready) {
                         AppUtils.crash("This appender already has a layout defined.");
                     }
                     layoutClassName = value;
                     hasLayoutAlready = true;
                 }
             } else if (newKey.toLowerCase().startsWith("errorhandler")) {
                 if (errorHandler == null) {
                     errorHandler = new ErrorHandler();      // there is only one for each appender
                 }
                 String errH = newKey.substring(12);         // removing "errorhandler" part without "."
                 if (errH.contains(".")) {
                     if (errH.substring(1).equalsIgnoreCase("appender-ref")) {
                         errorHandler.setAppender(value);
                     } else if (errH.substring(1).equalsIgnoreCase("logger-ref")) {
                         if (value.contains(",")) {
                             String[] values = value.replaceAll("\\s", "").split(",");
                             for (String logger : values) {
                                 errorHandler.addLogger(logger);
                             }
                         } else {
                             errorHandler.addLogger(value);
                         }
                     } else if (errH.substring(1).equalsIgnoreCase("root-ref")) {
                         errorHandler.setRoot(Boolean.valueOf(value));
                     } else {
                         errorHandler.addParam(errH.substring(1), value);
                     }
                 } else {
                     errorHandler.setClassName(value);       // className = compulsory by DTD
                 }
             } else if (newKey.toLowerCase().startsWith("appender-ref")) {
                 if (value.contains(",")) {
                     String[] values = value.replaceAll("\\s", "").split(",");
                     for (String appRef : values) {
                         addAppenderRef(appRef);
                     }
                 } else {
                     addAppenderRef(value);
                 }
             } else {
                 params.put(newKey, value);
                 // those values are Bean-type based, they are
                 // params: file, append, buffersize, target... it also
                 // depends on appender class :((
             }
         }
     }
 
     public Element toXmlElement() {
         Element appenderElement = DocumentFactory.getInstance().createElement("appender");
         appenderElement.addAttribute("name", name);
         appenderElement.addAttribute("class", className);
 
         if (errorHandler != null) {
             Element errorHandlerElement = appenderElement.addElement("errorHandler");
             errorHandlerElement.addAttribute("class", errorHandler.getClassName());
             for (String paramKey : errorHandler.getParams().keySet()) {
                 Element paramElement = errorHandlerElement.addElement("param");
                 paramElement.addAttribute("name", paramKey);
                 paramElement.addAttribute("value", errorHandler.getParams().get(paramKey));
             }
             if (errorHandler.isRoot()) {
                 errorHandlerElement.addElement("root-ref");
             }
             for (String logger : errorHandler.getLoggers()) {
                 Element logger_refElement = errorHandlerElement.addElement("logger-ref");
                 logger_refElement.addAttribute("ref", logger);
             }
             if (errorHandler.getAppender() != null) {
                 Element appender_refElement = errorHandlerElement.addElement("appender-ref");
                 appender_refElement.addAttribute("ref", errorHandler.getAppender());
             }
         }
 
         for (String paramKey : params.keySet()) {
             Element paramElement = appenderElement.addElement("param");
             paramElement.addAttribute("name", paramKey);
             paramElement.addAttribute("value", params.get(paramKey));
         }
 
         if (layoutClassName != null) {
             Element layoutElement = appenderElement.addElement("layout");
             layoutElement.addAttribute("class", layoutClassName);
             for (String layoutParamKey : layoutParams.keySet()) {
                 Element layoutParamElement = layoutElement.addElement("param");
                 layoutParamElement.addAttribute("name", layoutParamKey);
                 layoutParamElement.addAttribute("value", layoutParams.get(layoutParamKey));
             }
         }
         
         for (String appenderRef : appenderRefs) {
             Element appenderRefElement = appenderElement.addElement("appender-ref");
             appenderRefElement.addAttribute("ref", appenderRef);
         }
         return appenderElement;
     }
 
     public void setUpFromElement(Element element) {
         name = element.attributeValue("name");
         className = element.attributeValue("class");
 
         if (element.element("errorHandler") != null) {
             errorHandler = new ErrorHandler();
             errorHandler.setUpFromElement(element.element("errorHandler"));
         }
 
         for (Element e : (List<Element>) element.elements("param")) {
             addParam(e.attributeValue("name"), e.attributeValue("value"));
         }
 
         if (element.element("layout") != null) {
             layoutClassName = element.element("layout").attributeValue("class");
             for (Element e : (List<Element>) element.element("layout").elements("param")) {
                 addLayoutParam(e.attributeValue("name"), e.attributeValue("value"));
             }
         }
 
         for (Element e : (List<Element>) element.elements("appender-ref")) {
             addAppenderRef(e.attributeValue("ref"));
         }
     }
 
     private boolean checkParamSuported(String param) {
         if (className.startsWith("org.apache.log4j.")) {
             String[] classNameArr = className.split("\\.");
 
             if (classNameArr.length == 4) {
                 try {
                     AppenderParams params = AppenderParams.valueOf(classNameArr[3].toLowerCase(Locale.ENGLISH));
 
                     for (String par : params.getParams()) {
                         if (param.equalsIgnoreCase(par)) {
                             return true;
                         }
                     }
                     return false;
                 } catch (Exception e) {
                     // unnknow appender, we can't check validity
                 }
             }
         }
 
         return true;
     }
 
     private boolean checkLayoutParamSuported(String param) {
         for (Layout layout : Layout.values()) {
             if (getLayoutClassName().equalsIgnoreCase(layout.toString())) {
                 if (!layout.getParam1().equalsIgnoreCase(param)
                         && (layout.getParam2() != null && !layout.getParam2().equalsIgnoreCase(param))) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     public List<String> toProperty(List<String> prop) {
 
         prop.add(AppUtils.prefix("appender." + name + " = " + className));
 
         if (threshold != null && !threshold.isEmpty()) {
             prop.add(AppUtils.prefix("appender.threshold = " + threshold));
         }
 
         //params 
         AppUtils.addParams(prop, "appender." + name, params);
 
         if (layoutClassName != null) {
             prop.add(AppUtils.prefix("appender." + name + ".layout = " + layoutClassName));
 
             AppUtils.addParams(prop, "appender." + name + ".layout", layoutParams);
         }
 
         if (errorHandler != null) {
             errorHandler.toProperty(prop, "appender." + name);
         }
 
         if (appenderRefs != null && !appenderRefs.isEmpty()) {
             //logger refs
             String appenderRefsString = AppUtils.join(appenderRefs, ", ");
             prop.add(AppUtils.prefix("appender." + name + ".appender-ref = " + appenderRefsString));
         }
 
         return prop;
     }
 
     private boolean checkStandardLayoutName(String name) {
         for (Layout layout : Layout.values()) {
             if (name.equalsIgnoreCase(layout.getFullName())) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkStandardLayoutParam(String name, String param) {
         for (Layout layout : Layout.values()) {
             if (name.equalsIgnoreCase(layout.getFullName())) {
 
                 if (param.equalsIgnoreCase(layout.getParam1()) || param.equalsIgnoreCase(layout.getParam2())) {
                     return true;
                 }
 
             }
         }
         return false;
     }
 
     public void verify() {
         //name contains a space
         if (name.contains(" ")) {
             AppUtils.crash("Appender name " + name + " contanins a space");
         }
         
         if ((className == null) || ("".equals(className)) || (className.contains(" "))) {
             AppUtils.crash("Appender " + name + " contains incorrect class name: " + className);
         }
 
         // verify layouts
         if (layoutClassName != null) {
             //containing space
             if (layoutClassName.contains(" ")) {
                 AppUtils.crash("Appender's class name " + name + " contanins a space - layoutClassName: " + layoutClassName);
             }
             
             for (String key: layoutParams.keySet()) {
                 if (!checkLayoutParamSuported(key)) {
                     AppUtils.crash("Unsupported layout param: " + key + " in layout: " + layoutClassName);
                 }
             }
 
             //checking name
             if (!checkStandardLayoutName(layoutClassName)) {
                 log.debug("custom layout: " + layoutClassName);
                 // custom-defined layout is possible     
             } else {
                 // if predefined layout: checking params            
                 for (String layoutParam : layoutParams.keySet()) {
                     if (!checkStandardLayoutParam(layoutClassName, layoutParam)) {
                         AppUtils.crash("You have entered wrong layout param. Layout param was " + layoutParam);
                     }
                 }
             }
         }
 
         //verify params
         if (!AppUtils.testParams(params)) {
             AppUtils.crash("Param's name or value contains a space. Appender: " + name);
         }
 
         for (Map.Entry<String, String> param : params.entrySet()) {
             
             String key = param.getKey();
             
             if (!checkParamSuported(key)) {
                 AppUtils.crash("Unsupported param: " + key + " in appender: " + name);
             }
             
             
             //threshold
             if (param.getKey().equals("threshold")) {
                 try {
                     Level.toLevel(param.getValue()); // FOR MARTIN: Nahrada za Threshold.valueOf(paramCouple.getValue())
                 } catch (IllegalArgumentException ex) {
                     AppUtils.crash("You have entered wrong threshold for appender " + name);
                 }
             } else {
                 //other params than threshold (common for every appender)
                 try {
                     boolean paramFound = false;
                     
                     String log4jShortClassName = "customtype";
                     
                     if (className.startsWith("org.apache.log4j.")) {
                         String[] classNameArr = className.split("\\.");
                         if (classNameArr.length == 4) {
                                 log4jShortClassName = classNameArr[3].toLowerCase();
                         }
                     }
                     
                     for (String paramName : AppenderParams.valueOf(log4jShortClassName).getParams()) {
                         if (paramName.equalsIgnoreCase(param.getKey())) {
                             paramFound = true;
                             // checking validity of values (only where possible to check)
                             if (paramName.equalsIgnoreCase("Target")) {
                                 if (!param.getValue().equalsIgnoreCase("System.out")
                                         && !param.getValue().equalsIgnoreCase("System.err")) {
                                     AppUtils.crash("You have entered wrong value for "
                                             + param.getKey() + ", only System.out or System.err possible.");
                                 }
                             } else if (paramName.equalsIgnoreCase("ImmediateFlush") || paramName.equalsIgnoreCase("Append")
                                     || paramName.equalsIgnoreCase("BufferedIO") || paramName.equalsIgnoreCase("LocationInfo")) {
                                 if (!param.getValue().equalsIgnoreCase("true") && !param.getValue().equalsIgnoreCase("false")) {
                                     AppUtils.crash("You have entered wrong value for "
                                             + param.getKey() + ", only true/false is possible.");
                                 }
                             } else if (paramName.equalsIgnoreCase("MaxBackupIndex") || paramName.equalsIgnoreCase("BufferSize")
                                     || paramName.equalsIgnoreCase("ReconnectionDelay") || paramName.equalsIgnoreCase("Port")) {
                                 try {
                                     int i = Integer.valueOf(param.getValue());
                                 } catch (NumberFormatException ex) {
                                     AppUtils.crash("You have entered wrong value for "
                                             + param.getKey() + ", only integer value is possible.");
                                 }
                             }
                             break;
                         }
                     }
                     if (!paramFound) {
                         AppUtils.crash("You have entered wrong parameter " + param.getKey() + " for appender: " + name);
                     }
                 } catch (IllegalArgumentException ex) {
                     // custom defined appender: possible & it can have any parameter
                 }
             }
         }
 
         // + verify correctness of some values for defined parameters
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 31 * hash + (this.className != null ? this.className.hashCode() : 0);
         hash = 31 * hash + (this.layoutClassName != null ? this.layoutClassName.hashCode() : 0);
         hash = 31 * hash + (this.hasLayoutAlready ? 1 : 0);
         hash = 31 * hash + (this.threshold != null ? this.threshold.hashCode() : 0);
         hash = 31 * hash + (this.params != null ? this.params.hashCode() : 0);
         hash = 31 * hash + (this.layoutParams != null ? this.layoutParams.hashCode() : 0);
         hash = 31 * hash + (this.errorHandler != null ? this.errorHandler.hashCode() : 0);
         hash = 31 * hash + (this.appenderRefs != null ? this.appenderRefs.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Appender other = (Appender) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
             return false;
         }
         if ((this.layoutClassName == null) ? (other.layoutClassName != null) : !this.layoutClassName.equals(other.layoutClassName)) {
             return false;
         }
         if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
             return false;
         }
         if (this.layoutParams != other.layoutParams && (this.layoutParams == null || !this.layoutParams.equals(other.layoutParams))) {
             return false;
         }
         if (this.errorHandler != other.errorHandler && (this.errorHandler == null || !this.errorHandler.equals(other.errorHandler))) {
             return false;
         }
         if (this.appenderRefs != other.appenderRefs && (this.appenderRefs == null || !this.appenderRefs.equals(other.appenderRefs))) {
             return false;
         }
         return true;
     }
 
     private enum AppenderParams {
 
         consoleappender("Encoding", "ImmediateFlush", "Target", "Threshold"),
         fileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush", "Threshold"),
         rollingfileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush",
         "MaxBackupIndex", "MaxFileSize", "Threshold", "LocationInfo"),
         dailyrollingfileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush",
         "DatePattern", "Threshold", "ReconnectionDelay"),
         writerappender("Encoding", "ImmediateFlush", "Threshold"),
         asyncappender("BufferSize", "Threshold");
         private List<String> params = new ArrayList<String>();
 
         AppenderParams(String... params) {
             this.params.addAll(Arrays.asList(params));
         }
 
         public List<String> getParams() {
             return params;
         }
     }
 
     private enum Layout {
 
         SimpleLayout("org.apache.log4j.SimpleLayout", null, null),
         PatternLayout("org.apache.log4j.PatternLayout", "ConversionPattern", null),
         EnhancedPatternLayout("org.apache.log4j.EnhancedPatternLayout", "ConversionPattern", null),
         DateLayout("org.apache.log4j.DateLayout", "DateFormat", null),
         HTMLLayout("org.apache.log4j.HTMLLayout", "LocationInfo", "Title"),
         XMLLayout("org.apache.log4j.XMLLayout", "LocationInfo", null);
         private String fullName;
         private String param1;
         private String param2;
 
         Layout(String fullName, String param1, String param2) {
             this.fullName = fullName;
             this.param1 = param1;
             this.param2 = param2;
         }
 
         public String getParam1() {
             return param1;
         }
 
         public String getParam2() {
             return param2;
         }
 
         public String getFullName() {
             return fullName;
         }
 
         @Override
         public String toString() {
             return fullName;
         }
     }
 
     private class ErrorHandler {
 
         private String className;
         private Map<String, String> params = new HashMap<String, String>();
         private List<String> loggers = new ArrayList<String>();
         private String appender;
         private boolean root = false;
 
         public boolean isRoot() {
             return root;
         }
 
         public void setRoot(boolean root) {
             this.root = root;
         }
 
         public String getClassName() {
             return className;
         }
 
         public void setClassName(String className) {
             this.className = className;
         }
 
         public Map<String, String> getParams() {
             return params;
         }
 
         public void addParam(String key, String value) {
             if (params.get(key) == null) {
                 params.put(key, value);
             } else {
                 AppUtils.crash("ErrorHandler: '" + className + "' with two same params: " + key);
             }
         }
 
         public List<String> getLoggers() {
             return loggers;
         }
 
         public void addLogger(String logger) {
             if (!loggers.contains(logger)) {
                 loggers.add(logger);
             } else {
                 AppUtils.crash("Two same logger-ref:'" + logger + "' in ErrorHanfler: " + className);
             }
         }
 
         public String getAppender() {
             return appender;
         }
 
         public void setAppender(String appender) {
             this.appender = appender;
         }
 
         public void setUpFromElement(Element element) {
             className = element.attributeValue("class");
 
             for (Element e : (List<Element>) element.elements("param")) {
                 addParam(e.attributeValue("name"), e.attributeValue("value"));
             }
 
             for (Element e : (List<Element>) element.elements("logger-ref")) {
                 addLogger(e.attributeValue("ref"));
             }
 
             if (element.element("appender-ref") != null) {
                 appender = element.element("appender-ref").attributeValue("ref");
             }
 
         }
 
         public List<String> toProperty(List<String> prop, String prefix) {
             prop.add(AppUtils.prefix(prefix + ".errorHandler = " + className));
             AppUtils.addParams(prop, prefix + ".errorHandler", params);
 
             //logger refs
             if (loggers.size() != 0) {
                 String loggerRefs = AppUtils.join(loggers, ", ");
                 prop.add(AppUtils.prefix(prefix + ".errorHandler.logger-ref = " + loggerRefs));
             }
 
             if (appender != null) {
                 prop.add(AppUtils.prefix(prefix + ".errorHandler.appender-ref = " + appender));
             }
             
             if (root) {
                 prop.add(AppUtils.prefix(prefix + ".errorHandler.root-ref = " + root));
             }
 
             return prop;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 23 * hash + (this.className != null ? this.className.hashCode() : 0);
             hash = 23 * hash + (this.params != null ? this.params.hashCode() : 0);
             hash = 23 * hash + (this.loggers != null ? this.loggers.hashCode() : 0);
             hash = 23 * hash + (this.appender != null ? this.appender.hashCode() : 0);
             return hash;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final ErrorHandler other = (ErrorHandler) obj;
             if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
                 return false;
             }
             if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
                 return false;
             }
             if (this.loggers != other.loggers && (this.loggers == null || !this.loggers.equals(other.loggers))) {
                 return false;
             }
             if ((this.appender == null) ? (other.appender != null) : !this.appender.equals(other.appender)) {
                 return false;
             }
             return true;
         }
     }
 }
