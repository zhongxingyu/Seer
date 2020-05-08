 /*
  * Copyright 2007, 2008, 2009, 2010, 2011, 2012 GoogleTransitDataFeed
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package transxchange2GoogleTransit.handler;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXParseException;
 
 /*
  * This class handles the TransXChange xml input file under the aspect of
  * 	calendar dates associates with services
  */
 public class TransxchangeCalendar extends TransxchangeDataAspect {
 
 	// xml keys and output field fillers
 	static final String[] key_calendar__service_id = new String[] {"Service", "ServiceCode", "OpenRequired"}; // GTFS required
 	static final String[] key_calendar__monday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__tuesday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__wednesday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__thursday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__friday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__saturday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
 	static final String[] key_calendar__sunday = new String[] {"__transxchange2GTFS_drawDefault", "", "0"}; // GTFS required
	static final String[] key_calendar__start_date = new String[] {"Service", "StartDate", "20060901"}; // GTFS required
	static final String[] key_calendar__end_date = new String[] {"Service", "EndDate", "20091231"}; // GTFS required
 
 	// Parsed data
 	List<ValueList> listCalendar__service_id;
 	List<ValueList> listCalendar__monday;
 	List<ValueList> listCalendar__tuesday;
 	List<ValueList> listCalendar__wednesday;
 	List<ValueList> listCalendar__thursday;
 	List<ValueList> listCalendar__friday;
 	List<ValueList> listCalendar__saturday;
 	List<ValueList> listCalendar__sunday;
 	List<ValueList> listCalendar__start_date;
 	List<ValueList> listCalendar__end_date;
 
 	String service = "";
 	static final String[] _key_daytype_mofr = {"Service", "RegularDayType", "DaysOfWeek", "MondayToFriday"};
 	static final String[] _key_daytype_mosa = {"Service", "RegularDayType", "DaysOfWeek", "MondayToSaturday"};
 	static final String[] _key_daytype_mosu = {"Service", "RegularDayType", "DaysOfWeek", "MondayToSunday"};
 	static final String[] _key_daytype_sasu = {"Service", "RegularDayType", "DaysOfWeek", "SaturdayToSunday"};
 	static final String[] _key_daytype_mo = {"Service", "RegularDayType", "DaysOfWeek", "Monday"};
 	static final String[] _key_daytype_tu = {"Service", "RegularDayType", "DaysOfWeek", "Tuesday"};
 	static final String[] _key_daytype_we = {"Service", "RegularDayType", "DaysOfWeek", "Wednesday"};
 	static final String[] _key_daytype_th = {"Service", "RegularDayType", "DaysOfWeek", "Thursday"};
 	static final String[] _key_daytype_fr = {"Service", "RegularDayType", "DaysOfWeek", "Friday"};
 	static final String[] _key_daytype_sa = {"Service", "RegularDayType", "DaysOfWeek", "Saturday"};
 	static final String[] _key_daytype_su = {"Service", "RegularDayType", "DaysOfWeek", "Sunday"};
 	static final String[] _key_daytype_not_mo = {"Service", "RegularDayType", "DaysOfWeek", "NotMonday"};
 	static final String[] _key_daytype_not_tu = {"Service", "RegularDayType", "DaysOfWeek", "NotTuesday"};
 	static final String[] _key_daytype_not_we = {"Service", "RegularDayType", "DaysOfWeek", "NotWednesday"};
 	static final String[] _key_daytype_not_th = {"Service", "RegularDayType", "DaysOfWeek", "NotThursday"};
 	static final String[] _key_daytype_not_fr = {"Service", "RegularDayType", "DaysOfWeek", "NotFriday"};
 	static final String[] _key_daytype_not_sa = {"Service", "RegularDayType", "DaysOfWeek", "NotSaturday"};
 	static final String[] _key_daytype_not_su = {"Service", "RegularDayType", "DaysOfWeek", "NotSunday"};
 	static final String[] _key_operatingperiod = {"Service", "OperatingPeriod"};
 	String keyDaysOfWeek = "";
 	boolean inOperatingPeriod = false;
 	String serviceStartDate = "";
 	String serviceEndDate = "";
 	String monday = "";
 	String tuesday = "";
 	String wednesday = "";
 	String thursday = "";
 	String friday = "";
 	String saturday = "";
 	String sunday = "";
 	boolean watchForNotDays = false;
 
 	public List<ValueList> getListCalendar__service_id() {
 		return listCalendar__service_id;
 	}
 	public List<ValueList> getListCalendar__monday() {
 		return listCalendar__monday;
 	}
 	public List<ValueList> getListCalendar__tuesday() {
 		return listCalendar__tuesday;
 	}
 	public List<ValueList> getListCalendar__wednesday() {
 		return listCalendar__wednesday;
 	}
 	public List<ValueList> getListCalendar__thursday() {
 		return listCalendar__thursday;
 	}
 	public List<ValueList> getListCalendar__friday() {
 		return listCalendar__friday;
 	}
 	public List<ValueList> getListCalendar__saturday() {
 		return listCalendar__saturday;
 	}
 	public List<ValueList> getListCalendar__sunday() {
 		return listCalendar__sunday;
 	}
 	public List<ValueList> getListCalendar__start_date() {
 		return listCalendar__start_date;
 	}
 	public List<ValueList> getListCalendar__end_date() {
 		return listCalendar__end_date;
 	}
 
 	// v1.6.3: get start year of service
 	public int getStartYear() {
 		String					date = "";
 		int						lowestYear = 999999999;
 		int						testYear;
 
 		Iterator<ValueList> i = listCalendar__start_date.iterator();
 		while (i.hasNext()) {
 			date = ((i.next())).getValue(0);
 			testYear = Integer.parseInt(date.substring(0, 4));
 			if (testYear < lowestYear)
 				lowestYear = testYear;
 
 		}
 		if (lowestYear == 999999999)
 			return -1;
 
 		return lowestYear;
 	}
 	public int getEndYear() {
 		String					date = "";
 		int						highestYear = -1;
 		int						testYear;
 
 		Iterator<ValueList> i = listCalendar__end_date.iterator();
 		while (i.hasNext()) {
 			date = ((i.next())).getValue(0);
 			testYear = Integer.parseInt(date.substring(0, 4));
 			if (testYear > highestYear)
 				highestYear = testYear;
 
 		}
 		return highestYear;
 	}
 
 	public String getService() {
 		return service;
 	}
 
    	@Override
 	public void startElement(String uri, String name, String qName, Attributes atts)
 		throws SAXParseException {
 	    super.startElement(uri, name, qName, atts);
 
     	if (qName.equals(key_calendar__service_id[0]))
 			key = key_calendar__service_id[0];
 	    if (key.equals(key_calendar__service_id[0]) && qName.equals(key_calendar__service_id[1])) { // && keyOperationDays.length() == 0) {
 	    	keyNested = key_calendar__service_id[1];
 	    	niceString = "";
 	    }
 	    if (key.equals(key_calendar__start_date[0]) && qName.equals(key_calendar__start_date[1])) { // && keyOperationDays.length() == 0) {
 			keyNested = key_calendar__start_date[1];
 	    	niceString = "";
 	    }
 	    if (key.equals(key_calendar__end_date[0]) && qName.equals(key_calendar__end_date[1])) { // && keyOperationDays.length() == 0) {
 			keyNested = key_calendar__end_date[1];
 	    	niceString = "";
 	    }
 	    if (qName.equals(_key_daytype_mofr[0])) // also covers all other day types
 	    	key = _key_daytype_mofr[0];
 	    if (key.equals(_key_daytype_mofr[0]) && qName.equals(_key_daytype_mofr[1])) // also covers all other day types
 	    	keyNested = _key_daytype_mofr[1];
 	    if (key.equals(_key_daytype_mofr[0]) && keyNested.equals(_key_daytype_mofr[1]) && qName.equals(_key_daytype_mofr[2])) { // also covers all other day types
 	    	keyDaysOfWeek = _key_daytype_mofr[2];
 	    	monday = key_calendar__monday[2];
 	    	tuesday = key_calendar__tuesday[2];
 	    	wednesday = key_calendar__wednesday[2];
 	    	thursday = key_calendar__thursday[2];
 	    	friday = key_calendar__friday[2];
 	    	saturday = key_calendar__saturday[2];
 	    	sunday = key_calendar__sunday[2];
 	    	watchForNotDays = true;
 	    }
 	    if (key.equals(_key_operatingperiod[0]) && qName.equals(_key_operatingperiod[1]))
 	    	inOperatingPeriod = true;
 	}
 
    	@Override
 	public void endElement(String uri, String name, String qName) {
 		if (niceString == null || niceString.length() > 0) {
 			if (key.equals(key_calendar__service_id[0]) && keyNested.equals(key_calendar__service_id[1])) {
 				service = niceString;
 				ValueList newCalendar__service_id = new ValueList(key_calendar__service_id[0]);
 				listCalendar__service_id.add(newCalendar__service_id);
 				newCalendar__service_id.addValue(service);
 			}
 			if (key.equals(key_calendar__start_date[0]) && keyNested.equals(key_calendar__start_date[1])) {
 				ValueList newCalendar__start_date = new ValueList(key_calendar__start_date[1]);
 				if (inOperatingPeriod)
 					serviceStartDate = niceString;
 				listCalendar__start_date.add(newCalendar__start_date);
 				newCalendar__start_date.addValue(readTransxchangeDate(serviceStartDate));
 			}
 			if (key.equals(key_calendar__end_date[0]) && keyNested.equals(key_calendar__end_date[1])) {
 			  ValueList newCalendar__end_date = new ValueList(key_calendar__end_date[1]);
 				if (inOperatingPeriod)
 					serviceEndDate = niceString;
 				listCalendar__end_date.add(newCalendar__end_date);
 				newCalendar__end_date.addValue(readTransxchangeDate(serviceEndDate));
 			}
 		    if (key.equals(_key_operatingperiod[0]) && qName.equals(_key_operatingperiod[1]))
 		    	inOperatingPeriod = false;
 		}
 
 		if (keyDaysOfWeek.equals(_key_daytype_mofr[2]) && qName.equals(_key_daytype_mofr[3])) {
 			monday = "1";
 			tuesday = "1";
 			wednesday = "1";
 			thursday = "1";
 			friday = "1";
 			saturday = "0";
 			sunday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_mosa[2]) && qName.equals(_key_daytype_mosa[3])) {
 			monday = "1";
 			tuesday = "1";
 			wednesday = "1";
 			thursday = "1";
 			friday = "1";
 			saturday = "1";
 			sunday = "0";
 	    }
 		if (keyDaysOfWeek.equals(_key_daytype_mosu[2]) && qName.equals(_key_daytype_mosu[3])) {
 			monday = "1";
 			tuesday = "1";
 			wednesday = "1";
 			thursday = "1";
 			friday = "1";
 			saturday = "1";
 			sunday = "1";
 	    }
 		if (keyDaysOfWeek.equals(_key_daytype_mo[2]) && qName.equals(_key_daytype_mo[3])) {
 			monday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_tu[2]) && qName.equals(_key_daytype_tu[3])) {
 			tuesday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_we[2]) && qName.equals(_key_daytype_we[3])) {
 			wednesday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_th[2]) && qName.equals(_key_daytype_th[3])) {
 			thursday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_fr[2]) && qName.equals(_key_daytype_fr[3])) {
 			friday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_sa[2]) && qName.equals(_key_daytype_sa[3])) {
 			saturday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_su[2]) && qName.equals(_key_daytype_su[3])) {
 			sunday = "1";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_mo[2]) && qName.equals(_key_daytype_not_mo[3])) {
 			if (watchForNotDays) {
 				tuesday = "1";
 				wednesday = "1";
 				thursday = "1";
 				friday = "1";
 				saturday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			monday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_tu[2]) && qName.equals(_key_daytype_not_tu[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				wednesday = "1";
 				thursday = "1";
 				friday = "1";
 				saturday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			tuesday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_we[2]) && qName.equals(_key_daytype_not_we[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				tuesday = "1";
 				thursday = "1";
 				friday = "1";
 				saturday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			wednesday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_th[2]) && qName.equals(_key_daytype_not_th[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				tuesday = "1";
 				wednesday = "1";
 				friday = "1";
 				saturday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			thursday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_fr[2]) && qName.equals(_key_daytype_not_fr[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				tuesday = "1";
 				wednesday = "1";
 				thursday = "1";
 				saturday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			friday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_sa[2]) && qName.equals(_key_daytype_not_sa[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				tuesday = "1";
 				wednesday = "1";
 				thursday = "1";
 				friday = "1";
 				sunday = "1";
 				watchForNotDays = false;
 			}
 			saturday = "0";
 		}
 		if (keyDaysOfWeek.equals(_key_daytype_not_su[2]) && qName.equals(_key_daytype_not_su[3])) {
 			if (watchForNotDays) {
 				monday = "1";
 				tuesday = "1";
 				wednesday = "1";
 				thursday = "1";
 				friday = "1";
 				saturday = "1";
 				watchForNotDays = false;
 			}
 			sunday = "0";
 		}
 		if (qName.equals(keyDaysOfWeek)) {
 			watchForNotDays = false;
 			ValueList newCalendar__monday = new ValueList(key_calendar__monday[0]);
 	   		listCalendar__monday.add(newCalendar__monday);
 	   		newCalendar__monday.addValue(monday);
 	   		newCalendar__monday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__monday.addValue(service); // and service
 	   		ValueList newCalendar__tuesday = new ValueList(key_calendar__tuesday[0]);
 	   		listCalendar__tuesday.add(newCalendar__tuesday);
 	   		newCalendar__tuesday.addValue(tuesday);
 	   		newCalendar__tuesday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__tuesday.addValue(service); // and service
 	   		ValueList newCalendar__wednesday = new ValueList(key_calendar__wednesday[0]);
 	   		listCalendar__wednesday.add(newCalendar__wednesday);
 	   		newCalendar__wednesday.addValue(wednesday);
 	   		newCalendar__wednesday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__wednesday.addValue(service); // and service
 	   		ValueList newCalendar__thursday = new ValueList(key_calendar__thursday[0]);
 	   		listCalendar__thursday.add(newCalendar__thursday);
 	   		newCalendar__thursday.addValue(thursday);
 	   		newCalendar__thursday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__thursday.addValue(service); // and service
 	   		ValueList newCalendar__friday = new ValueList(key_calendar__friday[0]);
 	   		listCalendar__friday.add(newCalendar__friday);
 	   		newCalendar__friday.addValue(friday);
 	   		newCalendar__friday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__friday.addValue(service); // and service
 	   		ValueList newCalendar__saturday = new ValueList(key_calendar__saturday[0]);
 	   		listCalendar__saturday.add(newCalendar__saturday);
 	   		newCalendar__saturday.addValue(saturday);
 	   		newCalendar__saturday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__saturday.addValue(service); // and service
 	   		ValueList newCalendar__sunday = new ValueList(key_calendar__sunday[0]);
 	   		listCalendar__sunday.add(newCalendar__sunday);
 	   		newCalendar__sunday.addValue(sunday);
 	   		newCalendar__sunday.addValue(handler.getTrips().getJourneyPattern());
 	   		newCalendar__sunday.addValue(service); // and service
 		}
 	}
 
    	@Override
 	public void clearKeys(String qName) {
 		if (qName.equals(key_calendar__service_id[1]))
 			keyNested = "";
 		if (qName.equals(key_calendar__start_date[1]))
 			keyNested = "";
 		if (qName.equals(key_calendar__end_date[1]))
 			keyNested = "";
 		if (qName.equals(key_calendar__service_id[0])) {
 			key = "";
 			service = "";
 			handler.getTrips().setJourneyPattern("");
 		}
 		if (qName.equals(keyDaysOfWeek)) {
 			keyDaysOfWeek = "";
 			watchForNotDays = false;
 		}
 	}
 
    	@Override
 	public void completeData() {
 		int i;
 
 		for (i = 0; i < listCalendar__service_id.size(); i++) {
 			if (i >= listCalendar__end_date.size()) {
 			  ValueList newCalendar__end_date = new ValueList(key_calendar__end_date[1]);
 				listCalendar__end_date.add(newCalendar__end_date);
 				newCalendar__end_date.addValue(key_calendar__end_date[2]);
 			}
 			if (i >= listCalendar__monday.size()) {
 			  ValueList newCalendar__monday = new ValueList(key_calendar__monday[1]);
 				listCalendar__monday.add(newCalendar__monday);
 				newCalendar__monday.addValue(key_calendar__monday[2]);
 			}
 			if (i >= listCalendar__tuesday.size()) {
 			  ValueList newCalendar__tuesday = new ValueList(key_calendar__tuesday[1]);
 				listCalendar__tuesday.add(newCalendar__tuesday);
 				newCalendar__tuesday.addValue(key_calendar__tuesday[2]);
 			}
 			if (i >= listCalendar__wednesday.size()) {
 			  ValueList newCalendar__wednesday = new ValueList(key_calendar__wednesday[1]);
 				listCalendar__wednesday.add(newCalendar__wednesday);
 				newCalendar__wednesday.addValue(key_calendar__wednesday[2]);
 			}
 			if (i >= listCalendar__thursday.size()) {
 			  ValueList newCalendar__thursday = new ValueList(key_calendar__thursday[1]);
 				listCalendar__thursday.add(newCalendar__thursday);
 				newCalendar__thursday.addValue(key_calendar__thursday[2]);
 			}
 			if (i >= listCalendar__friday.size()) {
 			  ValueList newCalendar__friday = new ValueList(key_calendar__friday[1]);
 				listCalendar__friday.add(newCalendar__friday);
 				newCalendar__friday.addValue(key_calendar__friday[2]);
 			}
 			if (i >= listCalendar__saturday.size()) {
 			  ValueList newCalendar__saturday = new ValueList(key_calendar__saturday[1]);
 				listCalendar__saturday.add(newCalendar__saturday);
 				newCalendar__saturday.addValue(key_calendar__saturday[2]);
 			}
 			if (i >= listCalendar__sunday.size()) {
 			  ValueList newCalendar__sunday = new ValueList(key_calendar__sunday[1]);
 				listCalendar__sunday.add(newCalendar__sunday);
 				newCalendar__sunday.addValue(key_calendar__sunday[2]);
 			}
 		}
 
 		// Add quotes as needed
 		csvProofList(listCalendar__service_id);
  	    csvProofList(listCalendar__monday);
  	    csvProofList(listCalendar__tuesday);
  	    csvProofList(listCalendar__wednesday);
  	    csvProofList(listCalendar__thursday);
  	    csvProofList(listCalendar__friday);
  	    csvProofList(listCalendar__saturday);
  	    csvProofList(listCalendar__sunday);
  	    csvProofList(listCalendar__start_date);
  	    csvProofList(listCalendar__end_date);
 	}
 
    	@Override
 	public void dumpValues() {
 		int i;
 		ValueList iterator;
 
 	    System.out.println("*** Calendar");
 	    System.out.println("***** Service ID");
 	    for (i = 0; i < listCalendar__service_id.size(); i++) {
 	    	iterator = listCalendar__service_id.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Start date");
 	    for (i = 0; i < listCalendar__start_date.size(); i++) {
 	    	iterator = listCalendar__start_date.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** End date");
 	    for (i = 0; i < listCalendar__end_date.size(); i++) {
 	    	iterator = listCalendar__end_date.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Mondays");
 	    for (i = 0; i < listCalendar__monday.size(); i++) {
 	    	iterator = listCalendar__monday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Tuesdays");
 	    for (i = 0; i < listCalendar__tuesday.size(); i++) {
 	    	iterator = listCalendar__tuesday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Wednesdays");
 	    for (i = 0; i < listCalendar__wednesday.size(); i++) {
 	    	iterator = listCalendar__wednesday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Thursdays");
 	    for (i = 0; i < listCalendar__thursday.size(); i++) {
 	    	iterator = listCalendar__thursday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Fridays");
 	    for (i = 0; i < listCalendar__friday.size(); i++) {
 	    	iterator = listCalendar__friday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Saturdays");
 	    for (i = 0; i < listCalendar__saturday.size(); i++) {
 	    	iterator = listCalendar__saturday.get(i);
 	    	iterator.dumpValues();
 	    }
 	    System.out.println("***** Sundays");
 	    for (i = 0; i < listCalendar__sunday.size(); i++) {
 	    	iterator = listCalendar__sunday.get(i);
 	    	iterator.dumpValues();
 	    }
 	}
 
 	public void calendarDuplicateService(String existingServiceId, String newServiceId) {
 		int i = 0;
 		boolean found = false;
 		ValueList iterator;
 
 		while (i < listCalendar__service_id.size() && !found) {
 	    	iterator = listCalendar__service_id.get(i);
 			if ((iterator.getValue(0)).equals(existingServiceId))
 				found = true;
 			else
 				i++;
 		}
 		if (!found)
 			return;
 
 		// Create duplicate with new service ID
 		ValueList newCalendar__service_id = new ValueList(key_calendar__service_id[0]);
 		listCalendar__service_id.add(newCalendar__service_id);
 		newCalendar__service_id.addValue(newServiceId);
 
 		if (listCalendar__monday.size() >= i){
 		  ValueList newCalendar__monday = new ValueList(key_calendar__monday[0]);
 
 		  listCalendar__monday.add(newCalendar__monday);
 		  newCalendar__monday.addValue((listCalendar__monday.get(i)).getValue(0));
 		  newCalendar__monday.addValue((listCalendar__monday.get(i)).getValue(1));
 		  newCalendar__monday.addValue(newServiceId);
 		}
 		if (listCalendar__tuesday.size() >= i){
 		  ValueList newCalendar__tuesday = new ValueList(key_calendar__tuesday[0]);
 		  listCalendar__tuesday.add(newCalendar__tuesday);
 		  newCalendar__tuesday.addValue((listCalendar__tuesday.get(i)).getValue(0));
 		  newCalendar__tuesday.addValue((listCalendar__tuesday.get(i)).getValue(1));
 		  newCalendar__tuesday.addValue(newServiceId);
 		}
 		if (listCalendar__wednesday.size() >= i){
    		ValueList newCalendar__wednesday = new ValueList(key_calendar__wednesday[0]);
    		listCalendar__wednesday.add(newCalendar__wednesday);
    		newCalendar__wednesday.addValue((listCalendar__wednesday.get(i)).getValue(0));
    		newCalendar__wednesday.addValue((listCalendar__wednesday.get(i)).getValue(1));
   		newCalendar__wednesday.addValue(newServiceId);
 		}
 		if (listCalendar__thursday.size() >= i){
   		ValueList newCalendar__thursday = new ValueList(key_calendar__thursday[0]);
    		listCalendar__thursday.add(newCalendar__thursday);
    		newCalendar__thursday.addValue((listCalendar__thursday.get(i)).getValue(0));
    		newCalendar__thursday.addValue((listCalendar__thursday.get(i)).getValue(1));
   		newCalendar__thursday.addValue(newServiceId);
 		}
 		if (listCalendar__friday.size() >= i){
   		ValueList newCalendar__friday = new ValueList(key_calendar__friday[0]);
    		listCalendar__friday.add(newCalendar__friday);
    		newCalendar__friday.addValue((listCalendar__friday.get(i)).getValue(0));
    		newCalendar__friday.addValue((listCalendar__friday.get(i)).getValue(1));
   		newCalendar__friday.addValue(newServiceId);
 		}
 		if (listCalendar__saturday.size() >= i){
   		ValueList newCalendar__saturday = new ValueList(key_calendar__saturday[0]);
    		listCalendar__saturday.add(newCalendar__saturday);
    		newCalendar__saturday.addValue((listCalendar__saturday.get(i)).getValue(0));
    		newCalendar__saturday.addValue((listCalendar__saturday.get(i)).getValue(1));
   		newCalendar__saturday.addValue(newServiceId);
 		}
 		if (listCalendar__sunday.size() >= i){
   		ValueList newCalendar__sunday = new ValueList(key_calendar__sunday[0]);
    		listCalendar__sunday.add(newCalendar__sunday);
    		newCalendar__sunday.addValue((listCalendar__sunday.get(i)).getValue(0));
    		newCalendar__sunday.addValue((listCalendar__sunday.get(i)).getValue(1));
   		newCalendar__sunday.addValue(newServiceId);
 		}
 		if (listCalendar__start_date.size() >= i){
   		ValueList newCalendar__start_date = new ValueList(key_calendar__start_date[1]);
    		listCalendar__start_date.add(newCalendar__start_date);
    		newCalendar__start_date.addValue((listCalendar__start_date.get(i)).getValue(0));
 		}
 		if (listCalendar__end_date.size() >= i){
    		ValueList newCalendar__end_date = new ValueList(key_calendar__end_date[1]);
    		listCalendar__end_date.add(newCalendar__end_date);
    		newCalendar__end_date.addValue((listCalendar__end_date.get(i)).getValue(0));
 		}
 	}
 
 
 	public TransxchangeCalendar(TransxchangeHandlerEngine owner) {
 		super(owner);
 		listCalendar__service_id = new ArrayList<ValueList>();
 		listCalendar__monday = new ArrayList<ValueList>();
 		listCalendar__tuesday = new ArrayList<ValueList>();
 		listCalendar__wednesday = new ArrayList<ValueList>();
 		listCalendar__thursday = new ArrayList<ValueList>();
 		listCalendar__friday = new ArrayList<ValueList>();
 		listCalendar__saturday = new ArrayList<ValueList>();
 		listCalendar__sunday = new ArrayList<ValueList>();
 		listCalendar__start_date = new ArrayList<ValueList>();
 		listCalendar__end_date = new ArrayList<ValueList>();
 	}
 }
