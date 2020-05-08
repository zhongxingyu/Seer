 package application;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import data.Database;
 
 import exceptions.ParserException;
 
 public class TimeTableManager {
 	private Controller 		controller;
 	private Parser 			parser;
 	private Database		database;
 	private List<Transport> timeTable;
 	
 	public TimeTableManager(Controller controller) throws SQLException {
 		this.controller = controller;
 		this.parser 	= controller.getParser();
 		this.database	= controller.getDatabase();
 		
 		if (this.controller.getConfiguration().isOffline()) {
 			this.controller.getDisplay().setOfflineMode(true);
 			this.loadNewOfflineTimeTable();
 		} else {
 			try {
 				this.loadNewTimeTable();
 				this.controller.getDisplay().setOfflineMode(false);
 			} catch (ParserException e) {
 				this.controller.getDisplay().setOfflineMode(true);
 				this.loadNewOfflineTimeTable();
 			}
 		}
 	}
 	
 	private void loadNewTimeTable() throws ParserException {
 		List<Transport> parsedTimeTable = this.parser.loadTimeTable();
 		List<Transport> newTimeTable 	= new LinkedList<Transport>();
 		
 		for (Transport transport : parsedTimeTable) {
 			Long etc = transport.calcEtcSec();
 			if (
 				!"S Potsdam Hbf".equals(transport.getDestination())
 				&& !"Potsdam, Lange Brücke".equals(transport.getPlatform())
 				&& !"S Potsdam Hbf Nord".equals(transport.getPlatform())
 				&& !"Potsdam, Hbf/H.-Mann-Allee".equals(transport.getPlatform())
 				&& !"Potsdam, Hauptbahnhof/Hafen".equals(transport.getPlatform())
 				&& etc > 0
 				&& etc < 82800
 			) {
 				newTimeTable.add(transport);
 			}
 		}			
 		
 		this.timeTable = newTimeTable;
 	}
 	
 	public List<Transport> getCurrentTimeTable() throws SQLException {
 		List<Transport> newTimeTable 		= new LinkedList<Transport>();
 		List<Transport> currentTimeTable 	= new LinkedList<Transport>();
 		
 		for (Transport transport : this.timeTable) {
 			int minTime = this.controller.getConfiguration().getMinTime();
 			if (transport.calcEtcSec() > minTime) newTimeTable.add(transport);
 		}
 		
 		this.timeTable = newTimeTable;
 
 		int displayTransports = this.controller.getConfiguration().getDisplayTransports();
 		
 		if (this.timeTable.size() < displayTransports) {
 			 try {
 				this.loadNewTimeTable();
 				this.controller.getDisplay().setOfflineMode(false);
 			} catch (ParserException e) {
 				this.controller.getDisplay().setOfflineMode(true);
 				this.loadNewOfflineTimeTable();
 			}
 			newTimeTable = this.timeTable;
 		}		
 		
 		for (Transport transport : newTimeTable) {
 			if (currentTimeTable.size() < displayTransports) {
 				currentTimeTable.add(transport);
 			} else {
 				break;
 			}
 		}
 
 		return currentTimeTable;
 	}
 	
 	private void loadNewOfflineTimeTable() throws SQLException {
 		this.database.connect();
 		
 		this.loadNewOfflineTimeTable(new Date(), this.controller.getConfiguration().getLoadTransports());
 		
 		this.database.disconnect();
 	}
 	
 	private void loadNewOfflineTimeTable(Date date, int nOfTransports) throws SQLException {		
 		List<Transport> transportDayList = this.database.loadDayList();
 
 		Date queryDate 		= this.calcSimilarDate(transportDayList, date);
 		Date queryDateTime 	= Transport.mergeDate(queryDate, date);
 		
 		this.timeTable = this.database.loadSpecificTransportList(queryDateTime, nOfTransports);
 
 		for (Transport transport : this.timeTable) {
 			transport.setDeparture(Transport.mergeDate(date, transport.getDeparture()));
 		}
 		
 		if (this.timeTable.size() < nOfTransports) {
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(date);
 			cal.add(Calendar.DATE, 1);
 			queryDateTime.setTime(cal.getTimeInMillis());
 			
 			queryDate 		= this.calcSimilarDate(transportDayList, date);
 			
 			queryDateTime 	= Transport.mergeDate(queryDate, "00:00");
 			
 			List<Transport> listDayAfter = this.database.loadSpecificTransportList(queryDateTime, nOfTransports - this.timeTable.size());
 			
 			for (Transport transport : listDayAfter) {
 				transport.setDeparture(Transport.mergeDate(date, transport.getDeparture()));
 			}
 			this.timeTable.addAll(listDayAfter);
 		}
 	}
 	
 	private Date calcSimilarDate(List<Transport> transportList, Date searchDate) {
 		List<Transport> 	rightDow 	= new LinkedList<Transport>();
 		
 		SimpleDateFormat	dowFormat 	= new SimpleDateFormat("E");
 		String 				searchDow	= dowFormat.format(searchDate);
 		
 		for (Transport transport : transportList) {
 			String transportDow = dowFormat.format(transport.getDeparture());
 			if (
 				searchDow.equals(transportDow)
 				|| (
 					searchDow.equals("Mon")
 					|| searchDow.equals("Tue")
 					|| searchDow.equals("Wed")
 					|| searchDow.equals("Thu")
 					|| searchDow.equals("Fri")
 				) && (
 					transportDow.equals("Mon")
 					|| transportDow.equals("Tue")
 					|| transportDow.equals("Wed")
 					|| transportDow.equals("Thu")
 					|| transportDow.equals("Fri")
 				)
 			) {
 				rightDow.add(transport);
 			}
 		}
 		if (rightDow.size() == 0) return null;
 		
 		Date closestInPast = null;
 		for (Transport transport : rightDow) {
 			if (
 				(
 					(closestInPast == null)
 					&& !transport.getDeparture().after(searchDate)
				) || ( 
					closestInPast.before(transport.getDeparture())
 					&& !transport.getDeparture().after(searchDate) 
 				)
 			) {
 				closestInPast = transport.getDeparture();
 			}
 		}
 		if (closestInPast != null) return closestInPast;
 		
 		Date closestInFuture = null;
 		for (Transport transport : rightDow) {
 			if (
 				(
 					(closestInFuture == null)
 					&& transport.getDeparture().after(searchDate)
 				) || ( 
 					closestInFuture.after(transport.getDeparture())
 					&& transport.getDeparture().after(searchDate) 
 				)
 			) {
 				closestInFuture = transport.getDeparture();
 			}
 		}
 		return closestInFuture;
 	}
  }
