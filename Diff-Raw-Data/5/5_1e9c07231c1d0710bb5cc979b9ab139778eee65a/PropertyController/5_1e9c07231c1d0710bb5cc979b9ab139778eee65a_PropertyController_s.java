 package web.controller;
 
 import java.security.Principal;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import web.utils.StaticMap;
 import web.model.Comment;
 import web.model.Property;
 import web.model.PropertyOptions;
 import web.model.PropertyType;
 import web.model.User;
 import web.service.CommentService;
 import web.service.PropertyOptionsService;
 import web.service.PropertyService;
 import web.service.UserService;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>
  */
 @Controller
 public class PropertyController {
 
     @Autowired
     private PropertyService propertyService;
     @Autowired
     private PropertyOptionsService propertyOptionsService;
     @Autowired
     private CommentService comService;
     @Autowired
     private UserService userService;
 
     @RequestMapping(value = "/property/{id}", method = RequestMethod.GET)
     public String propertyView(@PathVariable Integer id, Model model, Principal current) {
 
         // Get Property
         Property property = propertyService.findById(id);
         PropertyOptions options = propertyOptionsService.findByProperty(property);
         List<Comment> comments = comService.findByProperty(property);
         Integer evaluation = property.getNote();
         String pathMap;
         pathMap = StaticMap.buildMapURL(property);
         List<String> pictures = new LinkedList<String>();
         for (int i = 1; i < 4; i++) {
             pictures.add(new Integer(i).toString() + ".jpg");
         }
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
         }
         model.addAttribute("property", property);
         model.addAttribute("options", options);
         model.addAttribute("evaluation", evaluation);
         model.addAttribute("comments", comments);
         model.addAttribute("map", pathMap);
         model.addAttribute("pictures", pictures);
 
         return "property";
     }
 
     @RequestMapping(value = "/s/property/new", method = RequestMethod.GET)
     public String newView(Model model, Principal current) {
         Property property = new Property();
         PropertyOptions options = new PropertyOptions();
         String rentStart = null;
         String rentStop = null;
 
         List<PropertyType> types = Arrays.asList(PropertyType.values());
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
            Boolean isOwnerCurrent = (u_log.getId() == null ? property.getOwner().getId() == null : u_log.getId().equals(property.getOwner().getId()));
            model.addAttribute("isOwnerCurrent", isOwnerCurrent);
         }
         model.addAttribute("property", property);
         model.addAttribute("options", options);
         model.addAttribute("types", types);
         model.addAttribute("rentStart", rentStart);
         model.addAttribute("rentStop", rentStop);
         return "new_property";
     }
 
     @RequestMapping(value = "/s/property/new", method = RequestMethod.POST)
     public String saveProperty(final Property property, final PropertyOptions options, final String rentStart, final String rentStop,
             final BindingResult bindingResult, final Model model, Principal current) {
 
         if (current != null) {
             property.setOwner(userService.findByUsername(current.getName()));
         }
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         DateTime rentStartTmp = formatter.parseDateTime(rentStart + " 00:00:00");
         DateTime rentStopTmp = formatter.parseDateTime(rentStop + " 00:00:00");
         property.setRentPeriodStart(rentStartTmp.toLocalDateTime());
         property.setRentPeriodStop(rentStopTmp.toLocalDateTime());
 
 //        handle new property
         propertyService.saveProperty(property);
         options.setProperty(property);
         propertyOptionsService.savePropertyOptions(options);
         return "redirect:/property/" + property.getId();
     }
 
     @RequestMapping(value = "/s/property/{id}/update", method = RequestMethod.POST)
     public String updateProperty(final Property property, final PropertyOptions options, final String rentStart, final String rentStop,
             final BindingResult bindingResult, final Model model, Principal current) {
 
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
         DateTime rentStartTmp = formatter.parseDateTime(rentStart + " 00:00:00");
         DateTime rentStopTmp = formatter.parseDateTime(rentStop + " 00:00:00");
         property.setRentPeriodStart(rentStartTmp.toLocalDateTime());
         property.setRentPeriodStop(rentStopTmp.toLocalDateTime());
 
 //        handle new property
         propertyService.saveProperty(property);
         propertyOptionsService.savePropertyOptions(options);
         return "redirect:/property/" + property.getId();
     }
 }
