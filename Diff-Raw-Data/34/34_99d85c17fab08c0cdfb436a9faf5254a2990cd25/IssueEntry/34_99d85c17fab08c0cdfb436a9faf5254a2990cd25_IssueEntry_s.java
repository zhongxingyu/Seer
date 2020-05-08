 package org.hackystat.sensor.ant.issue;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * An IssueEntry represent an issue in issue tracking system, always reflect the most current state.
  * @author Shaoxuan Zhang
  *
  */
 public class IssueEntry {
 
   /** property key of ID. */
   public static final String ID_PROPERTY_KEY = "IssueId";
   /** property key of TYPE. */
   public static final String TYPE_PROPERTY_KEY = "Type";
   /** property key of STATUS. */
   public static final String STATUS_PROPERTY_KEY = "Status";
   /** property key of PRIORITY. */
   public static final String PRIORITY_PROPERTY_KEY = "Priority";
   /** property key of MILESTONE. */
   public static final String MILESTONE_PROPERTY_KEY = "Milestone";
   /** property key of OWNER. */
   public static final String OWNER_PROPERTY_KEY = "Owner";
 
   /** timestamp separator in property value. */
   public static final String TIMESTAMP_SEPARATOR = "--";
   
   private int issueId;
   private String type = "";
   private String status = "";
   private String priority = "";
   private String milestone = "";
   private String owner = "";
   /** open time of this issue, or the earliest time this issue being detected.*/
   private XMLGregorianCalendar openedTime = null;
   /** close time of this issue, null if this issue is still open. */
   private XMLGregorianCalendar closedTime = null;
   private XMLGregorianCalendar modifiedTime = null;
   
   private SensorData sensorData = null;
   
   /**
    * Update this IssueEntry as well as the associated sensordata to the given issue table column.
    * @param line the content of the issue table column.
    * @param runTimestamp the run time.
    * @param isVerbose if do the update verbosely.
    * @return true if the sensordata is modified.
    */
   public boolean upToDate(String[] line, XMLGregorianCalendar runTimestamp, boolean isVerbose) {
     boolean modified = false;
     modified |= checkFieldUpdate(TYPE_PROPERTY_KEY, line[1], runTimestamp, isVerbose); 
     modified |= checkFieldUpdate(STATUS_PROPERTY_KEY, line[2], runTimestamp, isVerbose); 
     modified |= checkFieldUpdate(PRIORITY_PROPERTY_KEY, line[3], runTimestamp, isVerbose); 
     modified |= checkFieldUpdate(MILESTONE_PROPERTY_KEY, line[4], runTimestamp, isVerbose);
     modified |= checkFieldUpdate(OWNER_PROPERTY_KEY, line[5], runTimestamp, isVerbose);
     if (modified) {
       sensorData.setLastMod(runTimestamp);
     }
     return modified;
   }
 
   /**
    * Check field update with the new value.
    * @param fieldName name of the field.
    * @param value new value.
    * @param runTimestamp timestamp of the new value.
    * @param isVerbose if do the update verbosely.
    * @return true if the new value is different from the current value.
    */
   private boolean checkFieldUpdate(String fieldName, String value, 
       XMLGregorianCalendar runTimestamp, boolean isVerbose)  {
     Method getMethod = null;
     Method setMethod = null;
     try {
       getMethod = this.getClass().getMethod("get" + fieldName, new Class[]{});
       setMethod = this.getClass().getMethod("set" + fieldName, new Class[]{String.class});
       String oldValue = "";
       String newValue = value;
       oldValue = (String)getMethod.invoke(this, new Object[]{});
       if (oldValue.equals(newValue)) {
         return false;
       }
       if (isVerbose) {
         System.out.println(fieldName + " has been changed from [" + oldValue + 
           "] to [" + newValue + "].");
       }
       sensorData.addProperty(fieldName, newValue + TIMESTAMP_SEPARATOR + runTimestamp);
       setMethod.invoke(this, new Object[]{newValue});
       return true;
     }
     catch (IllegalArgumentException e) {
       e.printStackTrace();
     }
     catch (IllegalAccessException e) {
       e.printStackTrace();
     }
     catch (InvocationTargetException e) {
       e.printStackTrace();
     }
     catch (SecurityException e) {
       e.printStackTrace();
     }
     catch (NoSuchMethodException e) {
       e.printStackTrace();
     }
     return false;
   }
   /**
    * @param data The associated SensorData
    * @throws Exception if error
    */
   public IssueEntry(final SensorData data) throws Exception {
     this.sensorData = data;
     this.issueId = getIssueId(data);
     if (this.issueId < 0) {
       throw new Exception("Issue Id not found, " +
      		"probably because it is not Issue SensorData or malformatted");
     }
     this.type = getLatestValueWithKey(this.sensorData, TYPE_PROPERTY_KEY);
     this.status = getLatestValueWithKey(this.sensorData, STATUS_PROPERTY_KEY);
     this.priority = getLatestValueWithKey(this.sensorData, PRIORITY_PROPERTY_KEY);
     this.milestone = getLatestValueWithKey(this.sensorData, MILESTONE_PROPERTY_KEY);
     this.owner = getLatestValueWithKey(this.sensorData, OWNER_PROPERTY_KEY);
   }
 
   /**
    * Return the issue id of the issue sensordata.
    * @param data the issue sensordata.
    * @return the issue id. -1 if there is no property with key = IssueId.
    */
   public static int getIssueId(final SensorData data) {
     for (Property property : data.getProperties().getProperty()) {
       if (ID_PROPERTY_KEY.equals(property.getKey())) {
         return Integer.valueOf(property.getValue());
       }
     }
     return -1;
   }
 
   /**
    * Get the last update time of this issue sensor data.
    * If LastModification time is available, it will be return.
    * Otherwise, it will process through properties to find the 
    * lastest timestamp in properties' timestamp.
    * @return the timestamp of last udpate. NULL if no relative information found.
    */
   public XMLGregorianCalendar getLastUpdateTime() {
     if (sensorData.getLastMod() != null) {
       return sensorData.getLastMod();
     }
     XMLGregorianCalendar timestamp = null;
     List<String> keys = Arrays.asList(new String[]{TYPE_PROPERTY_KEY, STATUS_PROPERTY_KEY, 
         PRIORITY_PROPERTY_KEY, MILESTONE_PROPERTY_KEY, OWNER_PROPERTY_KEY});
     for (Property property : sensorData.getProperties().getProperty()) {
       if (keys.contains(property.getKey())) {
         try {
           XMLGregorianCalendar valueTimestamp = extractTimestamp(property.getValue());
           if (timestamp == null || Tstamp.greaterThan(valueTimestamp, timestamp)) {
             timestamp = valueTimestamp;
           }
         }
         catch (Exception e) {
           System.out.println("Error when extracting timestamp from " + property.getValue() +
               " Exception message: " + e.getMessage());
         }
       }
     }
     return timestamp;
   }
   
   /**
    * Extract timestamp from formatted string.
    * @param value the string.
    * @return the timestamp.
    * @throws Exception if the string is not formatted.
    */
   private static XMLGregorianCalendar extractTimestamp(String value) throws Exception {
     int startIndex = value.indexOf(TIMESTAMP_SEPARATOR) + TIMESTAMP_SEPARATOR.length();
     return Tstamp.makeTimestamp(value.substring(startIndex));
   }
 
   /**
    * Extract value from formatted string.
    * @param string the string.
    * @return the value.
    */
   private static String extractValue(String string) {
     return string.substring(0, string.indexOf(TIMESTAMP_SEPARATOR));
   }
   
   /**
    * Return the latest value with the given key from the SensorData.
    * @param sensorData the SensorData.
    * @param key the property key
    * @return the latest value, null if not found.
    * @throws Exception if error when parsing property values.
    */
   public static final String getLatestValueWithKey(SensorData sensorData, String key) 
       throws Exception {
     XMLGregorianCalendar latestTime = null;
     String value = "";
     for (Property property : sensorData.getProperties().getProperty()) {
       if (key.equals(property.getKey())) {
         XMLGregorianCalendar newTimestamp = extractTimestamp(property.getValue());
         if (latestTime == null || 
             latestTime.compare(newTimestamp) == DatatypeConstants.LESSER) {
           latestTime = newTimestamp;
           value = extractValue(property.getValue());
         }
       }
     }
     return value;
   }
 
   
   /**
    * @param id the issueId to set
    */
   protected void setIssueId(int id) {
     this.issueId = id;
   }
   /**
    * @return the issueId
    */
   public int getIssueId() {
     return issueId;
   }
   /**
    * @param type the type to set
    */
   public void setType(String type) {
     this.type = type;
   }
   /**
    * @return the type
    */
   public String getType() {
     return type;
   }
   /**
    * @param status the status to set
    */
   public void setStatus(String status) {
     this.status = status;
   }
   /**
    * @return the status
    */
   public String getStatus() {
     return status;
   }
   /**
    * @param priority the priority to set
    */
   public void setPriority(String priority) {
     this.priority = priority;
   }
   /**
    * @return the priority
    */
   public String getPriority() {
     return priority;
   }
   /**
    * @param milestone the milestone to set
    */
   public void setMilestone(String milestone) {
     this.milestone = milestone;
   }
   /**
    * @return the milestone
    */
   public String getMilestone() {
     return milestone;
   }
   /**
    * @param owner the owner to set
    */
   public void setOwner(String owner) {
     this.owner = owner;
   }
   /**
    * @return the owner
    */
   public String getOwner() {
     return owner;
   }
   /**
    * @param openedTime the openedTime to set
    */
   public void setOpenedTime(XMLGregorianCalendar openedTime) {
     this.openedTime = openedTime;
   }
   /**
    * @return the openedTime
    */
   public XMLGregorianCalendar getOpenedTime() {
     return openedTime;
   }
   /**
    * @param closedTime the closedTime to set
    */
   protected void setClosedTime(XMLGregorianCalendar closedTime) {
     this.closedTime = closedTime;
   }
   /**
    * @return the closedTime
    */
   public XMLGregorianCalendar getClosedTime() {
     return closedTime;
   }
 
   /**
    * @param sensorData the sensorData to set
    */
   protected void setSensorData(SensorData sensorData) {
     this.sensorData = sensorData;
   }
 
   /**
    * @return the sensorData
    */
   public SensorData getSensorData() {
     return sensorData;
   }
 
 
   /**
    * @param modifiedTime the modifiedTime to set
    */
   protected void setModifiedTime(XMLGregorianCalendar modifiedTime) {
     this.modifiedTime = modifiedTime;
   }
 
 
   /**
    * @return the modifiedTime
    */
   public XMLGregorianCalendar getModifiedTime() {
     return modifiedTime;
   }
 
 
 }
