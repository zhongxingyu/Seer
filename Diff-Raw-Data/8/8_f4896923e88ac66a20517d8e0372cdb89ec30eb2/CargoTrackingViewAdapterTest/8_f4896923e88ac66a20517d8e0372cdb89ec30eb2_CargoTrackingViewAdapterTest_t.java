 package se.citerus.dddsample.interfaces.tracking;
 
 import junit.framework.TestCase;
 import org.springframework.context.support.StaticApplicationContext;
 import se.citerus.dddsample.domain.model.cargo.Cargo;
 import se.citerus.dddsample.domain.model.cargo.RouteSpecification;
 import se.citerus.dddsample.domain.model.cargo.TrackingId;
 import se.citerus.dddsample.domain.model.handling.HandlingEvent;
 import se.citerus.dddsample.domain.model.handling.HandlingHistory;
 import se.citerus.dddsample.domain.model.weather.Weather;
 
 import java.util.*;
 
 import static se.citerus.dddsample.domain.model.location.SampleLocations.HANGZOU;
 import static se.citerus.dddsample.domain.model.location.SampleLocations.HELSINKI;
 import static se.citerus.dddsample.domain.model.voyage.SampleVoyages.CM001;
 
 public class CargoTrackingViewAdapterTest extends TestCase {
 
     private static final String GERMANY_TIMEZONE = "GMT+01:00";
 
     public void testCreate() {
         TimeZone.setDefault(TimeZone.getTimeZone(GERMANY_TIMEZONE));
         Cargo cargo = new Cargo(new TrackingId("XYZ"), new RouteSpecification(HANGZOU, HELSINKI, new Date()));
 
         List<HandlingEvent> events = new ArrayList<HandlingEvent>();
         events.add(new HandlingEvent(cargo, new Date(1), new Date(2), HandlingEvent.Type.RECEIVE, HANGZOU));
 
         events.add(new HandlingEvent(cargo, new Date(3), new Date(4), HandlingEvent.Type.LOAD, HANGZOU, CM001));
         events.add(new HandlingEvent(cargo, new Date(5), new Date(6), HandlingEvent.Type.UNLOAD, HELSINKI, CM001));
 
         cargo.deriveDeliveryProgress(new HandlingHistory(events));
 
         StaticApplicationContext applicationContext = new StaticApplicationContext();
         applicationContext.addMessage("cargo.status.IN_PORT", Locale.GERMAN, "In port {0}");
         applicationContext.refresh();
 
         Weather weather = new Weather(-10, "Snow", 80);
 
         CargoTrackingViewAdapter adapter = new CargoTrackingViewAdapter(cargo, applicationContext, Locale.GERMAN, events, weather);
 
         assertEquals("XYZ", adapter.getTrackingId());
         assertEquals("Hangzhou", adapter.getOrigin());
         assertEquals("Helsinki", adapter.getDestination());
         assertEquals("In port Helsinki", adapter.getStatusText());
         assertEquals("Snow (-10°C) Humidity: 80%", adapter.getWeather());
 
         Iterator<CargoTrackingViewAdapter.HandlingEventViewAdapter> it = adapter.getEvents().iterator();
 
         CargoTrackingViewAdapter.HandlingEventViewAdapter event = it.next();
         assertEquals("RECEIVE", event.getType());
         assertEquals("Hangzhou", event.getLocation());
        assertEquals("1970-01-01", event.getTime());
         assertEquals("", event.getVoyageNumber());
         assertTrue(event.isExpected());
 
         event = it.next();
         assertEquals("LOAD", event.getType());
         assertEquals("Hangzhou", event.getLocation());
        assertEquals("1970-01-01", event.getTime());
         assertEquals("CM001", event.getVoyageNumber());
         assertTrue(event.isExpected());
 
         event = it.next();
         assertEquals("UNLOAD", event.getType());
         assertEquals("Helsinki", event.getLocation());
        assertEquals("1970-01-01", event.getTime());
         assertEquals("CM001", event.getVoyageNumber());
         assertTrue(event.isExpected());
         resetTimeZone();
     }
 
     private void resetTimeZone() {
         TimeZone.setDefault(null);
     }
 
 }
