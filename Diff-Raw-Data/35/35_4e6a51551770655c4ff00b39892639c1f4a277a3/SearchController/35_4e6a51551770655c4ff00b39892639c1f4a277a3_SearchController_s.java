 package web.controller;
 
 import java.security.Principal;
 import java.util.List;
 import org.joda.time.LocalDateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import web.model.SearchResult;
 import web.model.User;
 import web.service.PropertyService;
 import web.service.SearchService;
 import web.service.UserService;
 import web.utils.StaticMap;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 
 @Controller
 public class SearchController {
 
     @Autowired
     PropertyService propertyService;
     
     @Autowired
     private UserService userService;
     
     @Autowired
     private SearchService searchService;
     
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public String searchView(Model model, Principal current, @RequestParam("location") String location, @RequestParam("latlong") String latlong, @RequestParam("checkin") String checkin, @RequestParam("checkout") String checkout, @RequestParam("guests_number") String guests) {
         // Specified if the current page is active and set the tab in the navbar.
         model.addAttribute("home", true);
                 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             if (u_log != null) {
                 model.addAttribute("current", u_log);
             }
         }
         
         // Check results from Search Service.
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         LocalDateTime s_checkin = formatter.parseLocalDateTime(checkin + " 00:00:00");
         LocalDateTime s_checkout = formatter.parseLocalDateTime(checkout + " 00:00:00");
         Integer s_guests = Integer.parseInt(guests);
         
         // Define which kind of request must be call and execute it.
         List<SearchResult> search;
         String[] dest = location.split(", ");
         if (dest.length > 1) {
             // City and country are known. We request by using the lat/lng coordinates.
             String[] coords = latlong.split(",");
            search = searchService.searchInRadius25(Math.ceil(Float.parseFloat(coords[0])), 
                    Math.ceil(Float.parseFloat(coords[1])), s_checkin, s_checkout, s_guests, null, null, null, null);
         } else {
             // Only country are known. We request by using the country.
             search = searchService.searchForSpecificCountry(dest[0], s_checkin, s_checkout, s_guests);
         }
        
         // Check if search found results.
        Integer[] specs = null;
        String pathMap = null;
         if (!search.isEmpty()) {
             // Get the number of different types, options and the price range (min and max).
             // SetUp the Google Static Maps.
             specs = processSpecs(search);
             pathMap = StaticMap.buildMapURL(search, "700x350");
         } // else offer other extra results
         
         model.addAttribute("specs", specs);
         model.addAttribute("map", pathMap);
         model.addAttribute("results", search);
         model.addAttribute("params", new String[]{location, latlong, checkin, checkout, guests});
         return "search";
     }
     
    /* Utils methods */
     private Integer[] processSpecs(List<SearchResult> search) {
         Integer[] specs = {0, 0, 0, 0, 0, 0, 0, search.get(0).getPrice(), search.get(0).getPrice()}; // Where index are respectively : FLAT, HOUSE, LOFT, Options(4) [...], min_price, max_price.
         for (SearchResult s : search) {
             // Property types
             if (s.getType().equals("FLAT")) {
                 specs[0]++;
             } else if (s.getType().equals("HOUSE")) {
                 specs[1]++;
             } else {
                 specs[2]++;
             }
             
             // Property options
             if (s.getPoParking()) { specs[3]++; }
             if (s.getPoSwim()) { specs[4]++; }
             if (s.getPoWifi()) { specs[5]++; }
             if (s.getPoLaundry()) { specs[6]++; }
             
             // Min/Max prices
            if (s.getPrice() < specs[7]) { specs[7] = s.getPrice(); }
            if (s.getPrice() > specs[8]) { specs[8] = s.getPrice(); }
         }
         return specs;
     }
     
     @RequestMapping(value = "/search", method = RequestMethod.POST)
     public @ResponseBody 
     String searchViewProcess(Model model, Principal current, @RequestParam("location") String location, @RequestParam("latlong") String latlong, @RequestParam("checkin") String checkin, @RequestParam("checkout") String checkout, @RequestParam("guests_number") Integer guests, @RequestParam(value="property_type", required=false) String[] p_type, @RequestParam(value="property_options", required=false) String[] p_options, @RequestParam("min_price") Integer min, @RequestParam("max_price") Integer max) {
         
         // Check results from Search Service.
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         LocalDateTime s_checkin = formatter.parseLocalDateTime(checkin + " 00:00:00");
         LocalDateTime s_checkout = formatter.parseLocalDateTime(checkout + " 00:00:00");
         
         // Prepare and execute the query
         List<SearchResult> search;
         String[] coords = latlong.split(",");
        search = searchService.searchInRadius25(Math.ceil(Float.parseFloat(coords[0])), 
                   Math.ceil(Float.parseFloat(coords[1])), s_checkin, s_checkout, guests, min, max, p_type, p_options);
         
         // Format the SearchResult List in JSON and return
         if (search != null) {
             return "{\"status\": \"success\", \"data\": " + formatInJSON(search) + "}";
         }
         return "{\"status\": \"no-result\"}";
     }
     
     private String formatInJSON(List<SearchResult> search) {
         String res = "[";
         for (int i = 0; i < search.size(); i++) {
             SearchResult s = search.get(i);
             res += "{";
             res += "\"property_id\": " + s.getPropertyId() + ",";
             res += "\"title\": \"" + s.getTitle() + "\",";
             res += "\"city\": \"" + s.getCity() + "\",";
             res += "\"country\": \"" + s.getCountry() + "\",";
             res += "\"short_desc\": \"" + s.getShortDesc() + "\",";
             res += "\"price\": " + s.getPrice();
             res += (i < search.size() - 1) ? "}, " : "}";
         }
         res += "]";
         return res;
     }
 }
