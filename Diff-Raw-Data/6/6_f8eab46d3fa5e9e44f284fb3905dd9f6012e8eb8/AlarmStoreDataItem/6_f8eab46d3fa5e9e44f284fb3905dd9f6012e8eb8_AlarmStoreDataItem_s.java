 package org.bitrepository.alarm;
 import java.util.StringTokenizer;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.bitrepository.bitrepositorymessages.AlarmMessage;
 import org.bitrepository.bitrepositoryelements.AlarmCode;
 import org.bitrepository.bitrepositoryelements.Alarm;
 
 
 
 /** 
  * Class to contain the data of a received alarm. Contains methods to serialize/deserialize the object 
  * so it can be persisted in a flat file.  
  */
 public class AlarmStoreDataItem {
 
 	private XMLGregorianCalendar date;
 	private String raiserID;
 	private AlarmCode alarmCode;
 	private String alarmText;
 	
 	/**
 	 * Private constructor to be used when creating a AlarmStoreDataItem by deserializing from a string. 
 	 */
 	private AlarmStoreDataItem(XMLGregorianCalendar date, String raiser, AlarmCode alarmCode,
 			String alarmText) {
 		this.date = date;
 		this.raiserID = raiser;
 		this.alarmCode = alarmCode;
 		this.alarmText = alarmText;
 	}
 	
 	/**
 	 * Publicly accessible  constructor.
 	 * @param Alarm Alarm obbject to build the AlarmStoreDataItem from.
 	 * @return The fresh AlarmStoreDataItem object. 
 	 */
 	public AlarmStoreDataItem(AlarmMessage message) {
 		Alarm alarm = message.getAlarm();
 		date = alarm.getOrigDateTime();
 		raiserID = alarm.getAlarmRaiser();
 		alarmCode = alarm.getAlarmCode();
 		alarmText = alarm.getAlarmText();
 	}
 	
 	/**
 	 * toString method to deliver a HTML representation of the alarm. 
 	 * @return A string containing a HTML table row presenting the alarm.  
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<tr><td>");
 		sb.append(date.toString());
 		sb.append("</td><td>");
 		sb.append(raiserID);
 		sb.append("</td><td>");
 		sb.append(alarmCode.toString());
 		sb.append("</td><td>");
 		sb.append(alarmText);
 		sb.append("</td></tr>");
 		return sb.toString();
 	}
 	
 	/**
 	 * Method for serializing the object to a string representation so it can be persisted to disk.
 	 * @return The string representation of the object.   
 	 */
 	public String serialize() {
 		return date.toString() + " #!# " + raiserID + " #!# " + alarmCode.toString() + " #!# " + alarmText;
 	}
 	
 	/**
 	 * Method for deserializing the string representation of a AlarmStoreDataItem object to a java object. 
 	 * @param String String representation of a AlarmStoreDataItem
 	 * @return The AlarmStoreDataItem object corresponding to the data. 
 	 */
 	public static AlarmStoreDataItem deserialize(String data) throws IllegalArgumentException {
 		XMLGregorianCalendar date;
 		String raiser;
 		AlarmCode alarmCode;
 		String alarmText;
 		String dateStr;
 		
 		StringTokenizer st = new StringTokenizer(data, "#!#");
 		if(st.countTokens() != 4) {
 			throw new IllegalArgumentException("The input string did not contain excatly 4 tokens");
 		}
 		try {
 			dateStr = st.nextToken();
 			date = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr.trim());
 			raiser = st.nextToken();
<<<<<<< HEAD
			alarmCode = AlarmcodeType.valueOf(st.nextToken().trim());
=======
			alarmCode = AlarmCode.valueOf(st.nextToken());
>>>>>>> 5af44301f764e32d5b5a0fc5be554f9cdfecf176
 			alarmText = st.nextToken();
 			return new AlarmStoreDataItem(date, raiser, alarmCode, alarmText);
 		} catch (DatatypeConfigurationException e) {
 			throw new IllegalArgumentException("The date token is invalid");
 		}
 	}
 	
 }
