 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jmxdatamart.Extractor.MXBean;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeData;
 import javax.management.openmbean.TabularData;
 import org.jmxdatamart.Extractor.Setting.Attribute;
 import org.jmxdatamart.common.DataType;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class MultiLayeredAttribute {
     
     private List<String> layers;
     private MXNameParser mxnp;
     private MBeanServer mbs;
     private ObjectName baseMbeanName;
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(MultiLayeredAttribute.class);
     
     public MultiLayeredAttribute(String attrStr, MBeanServer mbs, ObjectName baseMBeanName) {
         mxnp = new MXNameParser(attrStr);
         layers = mxnp.getMatches();
         if (layers == null || layers.isEmpty()) {
             throw new RuntimeException("Can not parse " + attrStr);
         }
         for (int i = 0; i < layers.size(); ++i) {
             String temp = layers.get(i);
             if (isPattern(temp)) { // is *? pattern
                 StringBuilder sb = new StringBuilder(temp.length());
                 for (int j = 0; j < temp.length(); ++j) {
                     switch (temp.charAt(j)) {   // turn into regex pattern
                         case '.':
                             sb.append("\\.");
                             break;
                         case '?':
                             sb.append(".");
                             break;
                         case '*':
                             sb.append(".*");
                             break;
                         default:
                             sb.append(temp.charAt(j));
                     }
                 }
             }
         }
         this.mbs = mbs;
         this.baseMbeanName = baseMBeanName;
     }
     
     public Map<Attribute, Object> getAll() throws Exception{
         Map<Attribute, Object> resultSoFar = new HashMap<Attribute, Object>();
         for (MBeanAttributeInfo mbai : mbs.getMBeanInfo(baseMbeanName).getAttributes()) {
             if (mbai.getName().matches(layers.get(0))){
                 getAllHelper(
                         1,
                         layers.size(),
                         mbs.getAttribute(baseMbeanName, mbai.getName()),
                         mbai.getName() + ".",
                         resultSoFar);
             }
         }
         return resultSoFar;
     }
     
     private DataType getSupportedDataType (Object obj) {
         for (DataType dt : DataType.values()) {
             if (dt.supportTypeOf(obj)) {
                 return dt;
             }
         }
         return null;
     }
     
     private void getAllHelper(
             int currDepth,
             int total,
             Object curr,
             String currName,
             Map<Attribute, Object> soFar) {
         if (curr == null) {
             logger.error("Null pointer in MX chain at " + currName);
             return;
         } else if (currDepth == total) {
             DataType dt = getSupportedDataType(curr);
             if (dt == null ) {
                 logger.error("Doesn't support type " + curr.getClass() + 
                                " from " + currName);
                 return;
             } else {
             soFar.put(
                    new Attribute(null, currName, dt),
                     curr);
             }
         } else {
             if (CompositeData.class.isAssignableFrom(curr.getClass())) {
                 CompositeData cd = (CompositeData) curr;
                 for (String s : cd.getCompositeType().keySet()) {
                     if (s.matches(layers.get(currDepth))) {
                         getAllHelper(
                                 currDepth + 1,
                                 total,
                                 cd.get(s),
                                 currName + s + ".",
                                 soFar);
                     }
                 }
             } else if (TabularData.class.isAssignableFrom(curr.getClass())) {
                 TabularData td = (TabularData) curr;
                 for (Object obj : td.keySet()) {
                     List l = (List) obj;    // magic <-|- more magic
                     for (Object o : l) {
                         if (!String.class.isAssignableFrom(o.getClass())) {
                             logger.error(o.getClass().toString() + " in " +
                                     currName + " is not supported as Tabular key");
                             continue;
                         } else {
                             String s = (String) o;
                             if (s.matches(layers.get(currDepth))) {
                                 getAllHelper(
                                         currDepth + 1,
                                         total,
                                         td.get(new Object[] {s}).get("value"),  // magic -|-> more magic
                                         currName + s + ".",
                                         soFar);
                             }
                         }
                     }
                 }
             } else {
                 logger.error("Doesn't support type " + curr.getClass() + 
                             " amid the MXBeanChain " + " at " + currName.toString());
             }
         }
     }
     
     private boolean isPattern(String name) {
         return name.contains("?") || name.contains("*");
     }
     
     private String name2alias(CharSequence name) {
         StringBuilder sb = new StringBuilder(name.length());
         for (int i = 0; i < name.length(); ++i) {
             if (Character.isLetterOrDigit(name.charAt(i))) {
                 sb.append(name.charAt(i));
             } else {
                 sb.append('_');
             }
         }
         return sb.toString();
     }
 }
