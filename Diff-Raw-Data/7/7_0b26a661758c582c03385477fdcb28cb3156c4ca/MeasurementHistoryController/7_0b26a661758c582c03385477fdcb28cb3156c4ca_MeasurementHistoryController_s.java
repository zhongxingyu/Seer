 package fws_master;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 /**
  * This class handles the collection of HistoryValues. It has two collections one for recent history an one for the history of the last year.
  * For each Station and each Parameter both histories are saved. 
  * @author Johannes Kasberger
  *
  */
 public class MeasurementHistoryController 
 implements Serializable {
 	private static Logger log = Logger.getLogger("fws_master.historycontroller");
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6349167137559366556L;
 	private HashMap<String,MeasurementHistory> lastHours;
 	private HashMap<String,MeasurementHistory> lastYear;
 	
 	/**
 	 * Class Constructor
 	 */
 	public MeasurementHistoryController() {
 		lastHours = new HashMap<String,MeasurementHistory>();
 		lastYear = new HashMap<String,MeasurementHistory>();
 	}
 	
 	/**
 	 * Adds the measurements to the recent history
 	 * @param station Name of the Station of which the measurements are
 	 * @param parameter Name of the Parameter
 	 * @param data Vector of Measurements
 	 */
 	public boolean addData(String station,String parameter,Vector<Measurement> data) {
 		//Find Data in recent history
 		String key = generateKey(station,parameter);
 		MeasurementHistory tmpHours = lastHours.get(key);
 		
 		if (data.size() == 0)
 			return false;
 		
 		log.fine("Adding new Data to Station "+station+"/"+parameter);
 		if (tmpHours == null) {	
 			tmpHours = new MeasurementHistory(station,parameter, data.firstElement().getParameter().getUnit());
 			lastHours.put(key, tmpHours);
 		}
 		
 		MeasurementHistoryEntry lastEntry;
 		boolean isnewDay = false;
 		
 		try {
 			lastEntry = tmpHours.getValues().getLast();
 			Calendar now,last;
 			last  = Calendar.getInstance();
 			last.setTime(lastEntry.getTimestamp());
 			
 			now = Calendar.getInstance();
 			if (last.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR) || last.get(Calendar.YEAR) != last.get(Calendar.YEAR)) {
 				this.changeDay(station, parameter, data.firstElement().getParameter().getHistory_function(), lastEntry.getTimestamp());
 				isnewDay = true;
 			}
 		}
 		catch (Exception ex) {
 			
 		}
 		tmpHours = lastHours.get(key);
 		tmpHours.addMeasurements(data);
 		
 		return isnewDay;
 	}
 	
 	
 	/**
 	 * Gets the measurement that are max hours hours old
 	 * @param station
 	 * @param parameter
 	 * @param hours maximum age of measurements
 	 * @return History of values
 	 */
 	public MeasurementHistory getLastHistory(String station,String parameter,int hours) {
 		if (hours > 24)
 			return null;
 		
 		String key = generateKey(station,parameter);
 		MeasurementHistory tmp = lastHours.get(key);
 		
 		if (tmp == null)
 			return null;
 		
 		MeasurementHistory result = new MeasurementHistory(tmp.getStation(),tmp.getParameter(),tmp.getUnit());
 		
 		Date now = new Date();
 		Date border = new Date((now.getTime()-(long)hours*60*60*1000));
 		
 		//Add the History if not older than hours
 		for(MeasurementHistoryEntry m:tmp.getValues()) {
 			
 			if (m.getTimestamp().after(border)) {
 				result.addMeasurement(m);
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Get values of the last days
 	 * @param station
 	 * @param parameter
 	 * @param days age in days of the values
 	 * @return History of values
 	 */
 	public MeasurementHistory getLastHistoryDays(String station,String parameter, int days) {
 		String key = generateKey(station,parameter);
 		MeasurementHistory tmp =  lastYear.get(key);
 		if(tmp == null)
 			return null;
 		MeasurementHistory newHist = new MeasurementHistory(tmp.getStation(),tmp.getParameter(),tmp.getUnit());
 		
 		LinkedList<MeasurementHistoryEntry> list = tmp.getValues();
 		
 		
 		
 		if (days>list.size())
 			return tmp;
 		
 		Iterator<MeasurementHistoryEntry> it = list.descendingIterator();
 		int i = 0;
 		while (it.hasNext() && i<days) {
 			MeasurementHistoryEntry e = it.next();
 			newHist.addMeasurement(e);
 		}
 		return newHist;
 	}
 	
 	/**
 	 * Is called when the day has changed. The representing value for the oldDate is calculated and saved to the long term history.
 	 * @param station
 	 * @param parameter
 	 * @param historyFunction
 	 * @param oldDate
 	 */
 	private void changeDay(String station,String parameter,HistoryFunctions historyFunction, Date oldDate) {
 		log.fine("change day");
 		
 		String key = generateKey(station,parameter);
 		
 		//Calculate the Begin and End of the oldDate
 		Calendar tmpDay = Calendar.getInstance();
 		tmpDay.setTime(oldDate);
 		tmpDay.add(Calendar.HOUR_OF_DAY , -tmpDay.get(Calendar.HOUR_OF_DAY ));
 		tmpDay.add(Calendar.MINUTE, -tmpDay.get(Calendar.MINUTE));
 		tmpDay.add(Calendar.SECOND, -tmpDay.get(Calendar.SECOND));
 		tmpDay.add(Calendar.MILLISECOND, -tmpDay.get(Calendar.MILLISECOND));
 		
 		Date beginDay,endDay, histDate;
 		
 		beginDay = tmpDay.getTime();
 		
 		tmpDay.add(Calendar.HOUR_OF_DAY , 23);
 		tmpDay.add(Calendar.MINUTE, 59);
 		tmpDay.add(Calendar.SECOND, 59);
 		
 		endDay = tmpDay.getTime();
 		
 		
 		tmpDay.add(Calendar.HOUR_OF_DAY, -11);
 		tmpDay.add(Calendar.MINUTE, -59);
 		tmpDay.add(Calendar.SECOND, -59);
 		
 		histDate = tmpDay.getTime();
 		
 		
 		//Get the current values of this station/parameter combination
 		MeasurementHistory current = lastHours.get(key);
 		if (current == null)
 			return;
 		
 		MeasurementHistory year = lastYear.get(key);
 		
 		if (year == null) {
 			year = new MeasurementHistory(current.getStation(),current.getParameter(), current.getUnit());
 			lastYear.put(key, year);
 		}
 		
 		//Depending on HistoryFunction init the value
 		double calc = 0;
 		switch(historyFunction) {
 		case AVG: calc = 0.0; break;
 		case MIN: calc = Double.MAX_VALUE; break;
 		case MAX: calc = Double.MIN_VALUE;break;
 		}
 		
 		
 		MeasurementHistory newCurrent = new MeasurementHistory(station,parameter,current.getUnit());
		
 		for(MeasurementHistoryEntry m:current.getValues()) {
 			
 			//Keep the last day in the recent history but remove older values
 			if (m.getTimestamp().after(beginDay))
 				newCurrent.addMeasurement(m);
 			
 			//If value was created on oldDate aggregate it with its history function
 			if (m.getTimestamp().after(beginDay) && m.getTimestamp().before(endDay)) {
 				switch(historyFunction) {
 				case AVG: calc += m.getValue(); break;
 				case MIN: if ( m.getValue() < calc) calc = m.getValue(); break;
 				case MAX: if (m.getValue() > calc ) calc = m.getValue(); break;
 				}
 			}
 		}
 		lastHours.remove(key);
 		newCurrent.sort();
 		//Save the cleaned up recent history
 		lastHours.put(key,newCurrent);
 		
 		if (historyFunction == HistoryFunctions.AVG) {
			calc /= current.getValues().size();
 		}
 		
 		MeasurementHistoryEntry entry = new MeasurementHistoryEntry(calc,histDate);
 		year.addMeasurement(entry);
 		while (year.getValues().size() > 355) {
 			year.removeFirstEntry();
 		}
 	}
 	
 	/**
 	 * Generate the Key for the HashMap
 	 * @param station
 	 * @param parameter
 	 * @return key for HashMap
 	 */
 	private String generateKey(String station,String parameter) {
 		return station+parameter;
 	}
 
 	public HashMap<String,MeasurementHistory>  getDataHours() {
 		return this.lastHours;
 		
 	}
 	
 	public HashMap<String,MeasurementHistory>  getDataDays() {
 		return this.lastYear;
 		
 	}
 }
