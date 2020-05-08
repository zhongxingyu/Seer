 package warsjawa;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import softwareart.booking.BookingService;
 import softwareart.booking.Participant;
 import softwareart.booking.Workshop;
 import softwareart.booking.persistence.FilePersistenceService;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 @Controller
 @RequestMapping
 public class EventBookingController {
 
     private final FilePersistenceService persistenceService;
     private BookingService bookingService;
 
     public EventBookingController() throws IOException {
         File file = new File(System.getProperty("user.home") + "/warsjawa-rezerwacje.txt");
         persistenceService = new FilePersistenceService(file);
         bookingService = new BookingService(persistenceService);
         Workshops.initWorkshops(bookingService);
         if (!file.createNewFile()) {
             bookingService.reloadBookings();
         }
     }
 
     @RequestMapping(value = "workshops", method = RequestMethod.GET)
     @ResponseBody
     public Collection<Collection<Workshop>> getAllWorkshops(HttpServletResponse response) {
         response.addHeader("Access-Control-Allow-Origin", "*");
         Collection<Collection<Workshop>> allWorkshops = new ArrayList<Collection<Workshop>>(3);
         for (int i = 0; i < 3; i++) {
             allWorkshops.add(bookingService.getWorkshopsStartingAtSlot(i + 1));
         }
         return allWorkshops;
     }
 
     // Zrobiłem GET ze względu na problem z cross-domain POST ( http://stackoverflow.com/questions/298745/how-do-i-send-a-cross-domain-post-request-via-javascript )
     @RequestMapping(value = "book/{email}/{name}/{workshops}", method = RequestMethod.GET)
     public void makeBooking(HttpServletResponse response, @PathVariable String email, @PathVariable String name, @PathVariable String workshops) {
         response.addHeader("Access-Control-Allow-Origin", "*");
         String[] workshopsArray = workshops.split(",");
         Integer[] workshopIds = new Integer[workshopsArray.length];
         for (int i = 0; i < workshopIds.length; i++) {
             workshopIds[i] = Integer.parseInt(workshopsArray[i]);
         }
 
         makeBooking(new Participant(email, name), workshopIds);
     }
 
     private String generateLink(String email, Integer[] workshopIds) {
         StringBuilder sb = new StringBuilder();
         sb.append(email);
         for (Integer workshopId : workshopIds) {
             sb.append(";");
             sb.append(workshopId);
         }
         String decoded = Base64.encode(sb.toString().getBytes());
         return "http://warsjawa.pl/confirm.html?v=" + decoded;
     }
 
     @RequestMapping(value = "confirm/{code}", method = RequestMethod.GET)
    public void getAllWorkshops(HttpServletResponse response, @PathVariable String code) {
         response.addHeader("Access-Control-Allow-Origin", "*");
 
         String encoded = new String(Base64.decode(code));
         String[] split = encoded.split(";");
        Integer[] workshops = new Integer[split.length];
         for (int i = 0; i < workshops.length; i++) {
             workshops[i] = Integer.parseInt(split[i+1]);
         }
         bookingService.confirm(split[0], workshops);
     }
 
     private String listTitles(Collection<Workshop> workshopsById) {
         StringBuilder sb = new StringBuilder();
         for (Workshop workshop : workshopsById) {
             sb.append("- ");
             sb.append(workshop.getTitle());
             sb.append("\n");
         }
         return sb.toString();
     }
 
     private synchronized void makeBooking(Participant participant, Integer[] workshopsArray) {
         bookingService.book(participant, workshopsArray);
     }
 }
