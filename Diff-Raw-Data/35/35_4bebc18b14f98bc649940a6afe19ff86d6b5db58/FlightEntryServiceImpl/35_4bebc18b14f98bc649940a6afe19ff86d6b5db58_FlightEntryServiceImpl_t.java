 package ch.ubx.startlist.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import ch.ubx.startlist.client.FlightEntryService;
 import ch.ubx.startlist.shared.FeDate;
 import ch.ubx.startlist.shared.FeFlightEntry;
 import ch.ubx.startlist.shared.FePlace;
 import ch.ubx.startlist.shared.FeYear;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class FlightEntryServiceImpl extends RemoteServiceServlet implements FlightEntryService {
 	private static final long serialVersionUID = 1L;
 	private FlightEntryDAO2 flightEntryDAO = new FlightEntryDAOobjectify2();
 
 	@Override
 	public List<FeFlightEntry> listFlightEntrie(FeDate date, int startIndex, int maxCount) {
 		List<FeFlightEntry> listFlightEntrys = flightEntryDAO.listflightEntry(date, startIndex, maxCount);
 		return new ArrayList<FeFlightEntry>(listFlightEntrys);
 	}
 
 	@Override
 	public FeFlightEntry removeFlightEntry(FeFlightEntry flightEntry) {
 		return flightEntryDAO.removeFlightEntry(flightEntry);
 
 	}
 
 	@Override
 	public FeFlightEntry createOrUpdateFlightEntry(FeFlightEntry flightEntry) {
 		return flightEntryDAO.createOrUpdateFlightEntry(flightEntry);
 	}
 
 	@Override
 	public List<FeDate> listDate(FePlace place) {
 		List<FeDate> list = flightEntryDAO.listDate(place);
 		Collections.sort(list);
 		return list;
 	}
 
 	@Override
 	public List<FePlace> listPlace(FeYear year) {
 		List<FePlace> list = flightEntryDAO.listAirfield(year);
 		Collections.sort(list);
 		return list;
 	}
 
 	@Override
 	public List<FeYear> listYear() {
 		List<FeYear> list = flightEntryDAO.listYear();
 		Collections.sort(list);
 		return list;
 	}
 
 }
