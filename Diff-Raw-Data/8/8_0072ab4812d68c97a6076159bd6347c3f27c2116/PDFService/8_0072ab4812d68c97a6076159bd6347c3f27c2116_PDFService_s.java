 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.tripbrush.service;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfImportedPage;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.google.gson.*;
 import com.itextpdf.text.*;
 import com.sun.net.ssl.internal.ssl.Debug;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import uk.tripbrush.model.core.Plan;
 import uk.tripbrush.model.pdf.Header;
 import uk.tripbrush.model.travel.Attraction;
 import uk.tripbrush.model.travel.Event;
 import uk.tripbrush.util.DateUtil;
 import uk.tripbrush.util.StringUtil;
 import uk.tripbrush.view.AttractionOpenView;
 import uk.tripbrush.view.MResult;
 
 /**
  *
  * @author Samir
  */
 public class PDFService {
 
 
     public static String createPlan(Plan plan) throws Exception {
         createTitlePage(plan);
         createCalendar(plan);
         for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
             createSubTitlePage(plan,datecounter);
             createEvents(plan,datecounter);
         }
         createLastPage(plan);
         return merge(plan);
     }
 
     public static void createTitlePage(Plan plan) throws Exception {
         FileOutputStream fos = new FileOutputStream(new File(ConfigService.getRoot() + "title" + plan.getId() + ".pdf"));
         Document document = new Document(PageSize.A4, 50, 50, 50, 50);
         document.open();
         Rectangle area = new Rectangle(36, 24, 559, 802);
         PdfWriter writer = PdfWriter.getInstance(document, fos);
         writer.setBoxSize("art", area);
         //writer.setPDFXConformance(PdfWriter.PDFX1A2001);
         Header event = new Header(plan.getTitle(),true);
         writer.setPageEvent(event);
 
         document.open();
 
         int tableheight = 740;
         int tablewidth = 500;
 
         //create first page
         PdfPTable table = new PdfPTable(1);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.setTotalWidth(tablewidth);
         table.setLockedWidth(true);
 
         int[] widths = {1};
         table.setWidths(widths);
 
         // TODO: add Logo here
         Paragraph destination = new Paragraph("Trip to " + plan.getLocation().getName());
         destination.setAlignment(Element.ALIGN_CENTER);
         destination.getFont().setFamily("Arial");
         destination.getFont().setSize(20);
         destination.getFont().setStyle("bold");
 
         Paragraph date = new Paragraph("\n\n" + DateUtil.getFullDay(plan.getStartdate()) + "\nto\n" + DateUtil.getFullDay(plan.getEnddate()));
         date.getFont().setFamily("Arial");
         date.setAlignment(Element.ALIGN_CENTER);
         date.getFont().setSize(12);
 
 
         PdfPCell titlecell = new PdfPCell();
         titlecell.setBorder(Rectangle.BOX);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         titlecell.setFixedHeight(tableheight);
 
         titlecell.addElement(destination);
         titlecell.addElement(date);
 
         table.addCell(titlecell);
         document.add(table);
 
         document.close();
     }
     
     public static void createLastPage(Plan plan) throws Exception {
         FileOutputStream fos = new FileOutputStream(new File(ConfigService.getRoot() + "last" + plan.getId() + ".pdf"));
         Document document = new Document(PageSize.A4, 50, 50, 50, 50);
         document.open();
         Rectangle area = new Rectangle(36, 24, 559, 802);
         PdfWriter writer = PdfWriter.getInstance(document, fos);
         writer.setBoxSize("art", area);
         //writer.setPDFXConformance(PdfWriter.PDFX1A2001);
         Header event = new Header(plan.getTitle(),true);
         writer.setPageEvent(event);
 
         document.open();
 
        int tableheight = 1000;
         int tablewidth = 500;
 
         //create first page
         PdfPTable table = new PdfPTable(1);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.setTotalWidth(tablewidth);
         table.setLockedWidth(true);
 
         int[] widths = {1};
         table.setWidths(widths);
 
         //TODO: Add logo here instead
         URL url = new URL("http://www.tripbrush.com/includes/images/TripBrushLogo.jpg");
         Image image = Image.getInstance(url);
 
         
         PdfPCell titlecell = new PdfPCell(image);
         titlecell.setBorder(Rectangle.NO_BORDER);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titlecell.setFixedHeight(tableheight-300);        
         table.addCell(titlecell);
        
         Paragraph dis = new Paragraph("DISCLAIMER: While we have done our best to ensure that the information in this booklet is up to date and accurate, we sometimes rely on data from 3rd parties (such as Google maps) and there may be last minute changes. Please double check that the destinations and public transport options are available during the times you intend to visit them. We cannot assume any liability in case of delays / unavailability. \n\n If you find an error in our data, please email errors@tripbrush.com and we will fix it");
         
         PdfPCell disclaimer = new PdfPCell();
         disclaimer.setBorder(Rectangle.NO_BORDER);
         disclaimer.setHorizontalAlignment(Element.ALIGN_CENTER);
         disclaimer.setVerticalAlignment(Element.ALIGN_BOTTOM);
        disclaimer.setFixedHeight(tableheight);
         disclaimer.addElement(dis);
 
         table.addCell(disclaimer);        
         
         document.add(table);
 
         document.close();
     }
     
     public static void createSubTitlePage(Plan plan,int datecounter) throws Exception {
         FileOutputStream fos = new FileOutputStream(new File(ConfigService.getRoot() + "stitle" + plan.getId() + datecounter + ".pdf"));
         Document document = new Document(PageSize.A4, 50, 50, 50, 50);
         document.open();
         Rectangle area = new Rectangle(36, 24, 559, 802);
         PdfWriter writer = PdfWriter.getInstance(document, fos);
         writer.setBoxSize("art", area);
         //writer.setPDFXConformance(PdfWriter.PDFX1A2001);
         Header event = new Header(plan.getTitle());
         writer.setPageEvent(event);
 
         document.open();
 
         int tableheight = 740;
         int tablewidth = 500;
 
         //create first page
         PdfPTable table = new PdfPTable(1);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.setTotalWidth(tablewidth);
         table.setLockedWidth(true);
 
         int[] widths = {1};
         table.setWidths(widths);
 
         Paragraph destination = new Paragraph("Trip to " + plan.getLocation().getName());
         destination.setAlignment(Element.ALIGN_CENTER);
         destination.getFont().setFamily("Arial");
         destination.getFont().setSize(20);
         destination.getFont().setStyle("bold");
         
         Calendar cal = Calendar.getInstance();
         cal.setTime(plan.getStartdate().getTime());
         cal.add(Calendar.DAY_OF_MONTH, datecounter);
         
         Paragraph date = new Paragraph("\n\nDay" + (datecounter+1) + "\n" + DateUtil.getFullDay(cal));
         date.getFont().setFamily("Arial");
         date.setAlignment(Element.ALIGN_CENTER);
         date.getFont().setSize(12);
 
         PdfPCell titlecell = new PdfPCell();
         titlecell.setBorder(Rectangle.NO_BORDER);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         titlecell.setFixedHeight(tableheight);
 
         titlecell.addElement(destination);
         titlecell.addElement(date);
 
         table.addCell(titlecell);
         document.add(table);
 
         document.close();
     }
     
     public static void createEvents(Plan plan,int datecounter) throws Exception {
         FileOutputStream fos = new FileOutputStream(new File(ConfigService.getRoot() + "events" + plan.getId() + datecounter + ".pdf"));
         Document document = new Document(PageSize.A4, 50, 50, 50, 50);
         document.open();
         Rectangle area = new Rectangle(36, 24, 559, 802);
         PdfWriter writer = PdfWriter.getInstance(document, fos);
         writer.setBoxSize("art", area);
         //writer.setPDFXConformance(PdfWriter.PDFX1A2001);
         Header event = new Header(plan.getTitle());
         writer.setPageEvent(event);
 
         document.open();
 
         int tableheight = 740;
         int tablewidth = 500;
 
         //create first page
         
         
         Calendar cal = Calendar.getInstance();
         cal.setTime(plan.getStartdate().getTime());
         cal.add(Calendar.DAY_OF_MONTH, datecounter);
         String date = DateUtil.getFullDay(cal);
         
         List<Event> todaysevents = plan.getEvents(cal);
         PdfPTable table;
         if (!todaysevents.isEmpty()) {
             int counter = 0;
             for (Event sevent: todaysevents) {
                 table = new PdfPTable(2);
                 table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                 table.setTotalWidth(tablewidth);
                 table.setLockedWidth(true);
 
                 int[] widths = {280,220};
                 table.setWidths(widths);    
                 
                 Chunk datetitle = new Chunk(DateUtil.getFullDay(cal));
                 //datetitle.setUnderline(0.1f,-2f);
                 datetitle.getFont().setFamily("Arial");
                 datetitle.getFont().setSize(12);
                 datetitle.getFont().setColor(51,102,00);
                 datetitle.getFont().setStyle("bold");
                 
                 PdfPCell datecell = new PdfPCell();
                 datecell.setBorder(Rectangle.NO_BORDER);
                 datecell.addElement(datetitle);
                 datecell.setColspan(2);
 
                 
                 Paragraph time = new Paragraph(sevent.getDuration() + "\n");
                 time.getFont().setFamily("Arial");
                 time.getFont().setSize(12);
                 
                 PdfPCell timecell = new PdfPCell(time);
                 timecell.setBorder(Rectangle.NO_BORDER);
                 timecell.setColspan(2);
                 
                 Attraction attraction = sevent.getAttraction();
                 
                 Paragraph eventname = new Paragraph(attraction.getName() + "\n");
                 eventname.getFont().setFamily("Arial");
                 eventname.getFont().setSize(12);
                 eventname.getFont().setStyle("bold");
                
                 Paragraph eventdesc = new Paragraph(attraction.getDescription());
                 eventdesc.getFont().setFamily("Arial");
                 eventdesc.getFont().setSize(10);                
                 
                 PdfPCell eventcell = new PdfPCell();
                 eventcell.addElement(eventname);
                 eventcell.addElement(eventdesc);
                 eventcell.setBorder(Rectangle.NO_BORDER);
                 eventcell.setColspan(1);
                 
                 
                 URL url = new URL(ConfigService.getUrl()+ "/includes/images/data/"+attraction.getImageFileName());
                 PdfPCell piccell = new PdfPCell();
                 try{
                     Image image = Image.getInstance(url);
                     image.scaleToFit(250,250);
                     piccell = new PdfPCell(image);
                     piccell.setBorder(Rectangle.NO_BORDER);
                     piccell.setColspan(1);
                 }
                 catch(Exception e){
                 }
                 
                 Paragraph eventhours = new Paragraph("Opening Hours");
                 eventhours.getFont().setFamily("Arial");
                 eventhours.getFont().setSize(12);                   
                 eventhours.getFont().setStyle("bold");
 
                 
                 CalendarService.loadAttractionTimes(plan, attraction);
                 PdfPCell description = new PdfPCell();
                 description.addElement(eventhours);
                 description.setBorder(Rectangle.NO_BORDER);
                 description.setColspan(2);        
                 
                 for (AttractionOpenView view: attraction.getOpeningTimes()) {
                     Paragraph eventtimes = new Paragraph(DateUtil.getDay(view.getFrom())+" " + DateUtil.getTime(view.getFrom()) + "-"+ DateUtil.getTime(view.getTo()));
                     eventtimes.getFont().setFamily("Arial");
                     eventtimes.getFont().setSize(10);
                     description.addElement(eventtimes);
                 }
                 
                 
 
                 
                 if (!StringUtil.isEmpty(sevent.getAttraction().getPhone())) {
                     Paragraph phone_label = new Paragraph("\nPhone Number");
                     phone_label.getFont().setFamily("Arial");
                     phone_label.getFont().setSize(12);                   
                     phone_label.getFont().setStyle("bold");
                     description.addElement(phone_label);
 
                     Paragraph eventphone = new Paragraph(attraction.getPhone());
                     eventphone.getFont().setFamily("Arial");
                     eventphone.getFont().setSize(10);  
                     description.addElement(eventphone);
                 }
                 
                 if (!StringUtil.isEmpty(sevent.getAttraction().getAddress())) {
                     Paragraph address_label = new Paragraph("\nAddress");
                     address_label.getFont().setFamily("Arial");
                     address_label.getFont().setSize(12);                   
                     address_label.getFont().setStyle("bold");
                     description.addElement(address_label);
                     
                     Paragraph eventaddress = new Paragraph(attraction.getAddress());
                     eventaddress.getFont().setFamily("Arial");
                     eventaddress.getFont().setSize(10);      
                     description.addElement(eventaddress);
                 }
                 
                 table.addCell(datecell);
                 table.addCell(timecell);
                 table.addCell(eventcell);
                 table.addCell(piccell);
                 table.addCell(description);
                 
                 document.add(table); 
                 document.newPage();
                 
                 if (counter!=todaysevents.size()-1) {
                     Event nextevent = todaysevents.get(counter+1);
                     Attraction nextatt = nextevent.getAttraction();
                     //add directions page
                     PdfPTable table1 = new PdfPTable(1);
                     table1.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                     table1.setTotalWidth(tablewidth);
                     table1.setLockedWidth(true);
 
                     int[] widths3 = {1};
                     table1.setWidths(widths3);    
 
                     table1.addCell(datecell);
                     
                     Paragraph directions = new Paragraph("Directions from A:" + attraction.getName() + " to B:" + nextatt.getName() + "\n\n");
                     directions.getFont().setFamily("Arial");
                     directions.getFont().setSize(10);
 
                     PdfPCell dcell = new PdfPCell(directions);
                     dcell.setBorder(Rectangle.NO_BORDER);
                     dcell.setColspan(1);
                     table1.addCell(dcell);
                     
                     String fpostcode = attraction.getPostcode().replaceAll(" ","")+",UK";
                     String tpostcode = nextatt.getPostcode().replaceAll(" ","")+",UK";
                     
                     // Manu's Code
                     long departure_time = sevent.getEnddate().getTimeInMillis();
                     departure_time = departure_time / 1000;
                     String d_t = String.valueOf(departure_time);
                     
                     String json_string;
                     json_string = Browser.getPage("http://maps.googleapis.com/maps/api/directions/json?origin=" + fpostcode + "&destination=" + tpostcode + "&sensor=false&mode=walking").toString();
                     JsonParser parser = new JsonParser();
                     JsonObject json = parser.parse(json_string).getAsJsonObject();                    
                     JsonArray routes = json.get("routes").getAsJsonArray();
                     
                     String status = stringify(json.get("status"));
                     
                     if (!status.equals("OK") || routes.get(0).getAsJsonObject().get("legs").getAsJsonArray().get(0).getAsJsonObject().get("duration").getAsJsonObject().get("value").getAsInt() > 60*(ConfigService.getMaxWalking())){
                         json_string = Browser.getPage("http://maps.googleapis.com/maps/api/directions/json?origin=" + fpostcode + "&destination=" + tpostcode + "&sensor=false&mode=transit&departure_time=" + d_t).toString();                    
                         parser = new JsonParser();
                         json = parser.parse(json_string).getAsJsonObject();                    
                         routes = json.get("routes").getAsJsonArray();
                     }
                     
                     status = stringify(json.get("status"));
                     if (status.equals("OK")){
                         addPublicTransportDirections(table1,routes);
                     }
                     else{
                         table1.addCell("Sorry there seems to be a bug where Google doesn't return certain directions. We are currently working with Google to address this. Apologies for the inconvenience. Please check back soon for a fix.");
                     }
                         
                                         
                     document.add(table1); 
                     document.newPage();
                 }
                 counter++;
             }            
         }
         else {
             table = new PdfPTable(1);
             table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
             table.setTotalWidth(tablewidth);
             table.setLockedWidth(true);
 
             int[] widths2 = {1};
             table.setWidths(widths2);
 
             Paragraph noact = new Paragraph("No Activities Planned");
             noact.getFont().setFamily("Arial");
             noact.setAlignment(Element.ALIGN_CENTER);
             noact.getFont().setSize(16);
 
 
             PdfPCell titlecell = new PdfPCell();
             titlecell.setBorder(Rectangle.NO_BORDER);
             titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
             titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
             titlecell.setFixedHeight(tableheight);
 
             titlecell.addElement(noact);
 
             table.addCell(titlecell);  
             document.add(table); 
         }
                  
         document.close();
     }
 
     public static void addPublicTransportDirections(PdfPTable table, JsonArray routes) throws Exception{
 
         JsonArray legs = routes.get(0).getAsJsonObject().get("legs").getAsJsonArray();
         JsonArray steps = legs.get(0).getAsJsonObject().get("steps").getAsJsonArray();                    
 
         PdfPTable directions_table = new PdfPTable(3);
         int[] widthArray = {20,200,400};
         directions_table.setWidths(widthArray);
 
         String marker_text;
         String logo_str,
                directions_str,
                image_str,
                duration_str;
         ArrayList markers = new ArrayList();
         
         JsonElement overview_polyline_points = routes.get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject().get("points");
         
         for (int i=0;i<steps.size();i++){
             JsonElement travel_mode, 
                     start_lat, 
                     start_lng, 
                     end_lat, 
                     end_lng, 
                     polyline_points, 
                     html_instructions, 
                     duration, 
                     line, 
                     line_name = null,
                     vehicle, 
                     local_icon = null, 
                     vehicle_type = null, 
                     vehicle_name = null, 
                     short_name = null, 
                     headsign = null, 
                     departure_stop_name = null, 
                     arrival_stop_name = null, 
                     departure_time = null, 
                     arrival_time = null, 
                     num_stops = null;
             
             travel_mode = steps.get(i).getAsJsonObject().get("travel_mode");
             start_lat = steps.get(i).getAsJsonObject().get("start_location").getAsJsonObject().get("lat");
             start_lng = steps.get(i).getAsJsonObject().get("start_location").getAsJsonObject().get("lng");
             end_lat = steps.get(i).getAsJsonObject().get("end_location").getAsJsonObject().get("lat");
             end_lng = steps.get(i).getAsJsonObject().get("end_location").getAsJsonObject().get("lng");
             polyline_points = steps.get(i).getAsJsonObject().get("polyline").getAsJsonObject().get("points");
             html_instructions = steps.get(i).getAsJsonObject().get("html_instructions");
             duration = steps.get(i).getAsJsonObject().get("duration").getAsJsonObject().get("text");
 
             JsonElement transit_details = steps.get(i).getAsJsonObject().get("transit_details");
             if (transit_details != null){
                 line = transit_details.getAsJsonObject().get("line");
                 line_name = line.getAsJsonObject().get("name");
                 vehicle = line.getAsJsonObject().get("vehicle");
                 local_icon = vehicle.getAsJsonObject().get("local_icon");
                 vehicle_type = vehicle.getAsJsonObject().get("type");
                 vehicle_name = vehicle.getAsJsonObject().get("name");
                 short_name = line.getAsJsonObject().get("short_name");
                 headsign = transit_details.getAsJsonObject().get("headsign");
                 departure_stop_name = transit_details.getAsJsonObject().get("departure_stop").getAsJsonObject().get("name");
                 arrival_stop_name = transit_details.getAsJsonObject().get("arrival_stop").getAsJsonObject().get("name");
                 departure_time = transit_details.getAsJsonObject().get("departure_time").getAsJsonObject().get("text");
                 arrival_time = transit_details.getAsJsonObject().get("arrival_time").getAsJsonObject().get("text");
                 num_stops = transit_details.getAsJsonObject().get("num_stops");
             }
             
             // *****************  Logo *************************
             if (stringify(travel_mode).equals("TRANSIT")){
                 if (stringify(vehicle_type).equals("BUS") || 
                     stringify(vehicle_type).equals("INTERCITY_BUS") || 
                     stringify(vehicle_type).equals("TROLLEYBUS")){
                     logo_str = ConfigService.getUrl()+ "/includes/images/bus.png";
                     marker_text = "&markers=icon:http://chart.apis.google.com/chart?chst=d_map_pin_icon%26chld=bus%257CFFFFFF%257Ctest%257CFFFFFF%257C000000%7C" + start_lat + "," + start_lng;
                 }
                 else{
                     logo_str = ConfigService.getUrl()+ "/includes/images/rail.png";
                      marker_text = "&markers=icon:http://chart.apis.google.com/chart?chst=d_map_pin_icon%26chld=train%257CFFFFFF%257Ctest%257CFFFFFF%257C000000%7C" + start_lat + "," + start_lng;
                 }
                 
                 //if (local_icon != null)
                     //logo_str = "http:" + stringify(local_icon);
             }
             else if (stringify(travel_mode).equals("DRIVING")){
                 logo_str = ConfigService.getUrl()+ "/includes/images/drive.png";
                 marker_text = "&markers=icon:http://chart.apis.google.com/chart?chst=d_map_pin_icon%26chld=taxi%257CFFFFFF%257Ctest%257CFFFFFF%257C000000%7C" + start_lat + "," + start_lng;
              }
             else{
                 logo_str = ConfigService.getUrl()+ "/includes/images/walk.png";                
                 //marker_text = "&markers=icon:http://chart.apis.google.com/chart?chst=d_map_pin_icon%26chld=wc-male%257CFFFFFF%257Ctest%257CFFFFFF%257C000000%7C" + start_lat + "," + start_lng;
                 marker_text = "";
             }
             
             // First marker should be A and last marker should be B
             if (i == 0){
                 marker_text = "&markers=color:blue|label:A|" + start_lat + "," + start_lng;
             }
             else if (i == (steps.size()-1)){
                 marker_text += "&markers=color:blue|label:B|" + end_lat + "," + end_lng;                
             }
             
             markers.add(marker_text);
 
             
             // *****************  Directions text *************************
             directions_str = "";
             if (stringify(travel_mode).equals("TRANSIT")){   
                 directions_str += "Take ";                    
                 directions_str += stringify(vehicle_name) +" (";
                 directions_str += stringify(short_name) + " "
                                + stringify(line_name) + "), ";
                 directions_str += "towards ";
                 directions_str += stringify(headsign);
                 directions_str += ", from ";
                 directions_str += stringify(departure_stop_name);
                 directions_str += " to ";
                 directions_str += stringify(arrival_stop_name);
             }
             else{
                 directions_str = clean(stringify(html_instructions));
             }
             
             
             // *****************  image *************************
             // Beginning marker
             image_str = "http://maps.googleapis.com/maps/api/staticmap?size=400x200&scale=2";
             image_str += "&markers=color:blue|label:A|" + start_lat + "," + start_lng;
             
             // Ending marker text and path
             image_str += "&markers=color:blue|label:B|" + end_lat + "," + end_lng;
             image_str += "&path=color:0x000000|weight:5|enc:" + stringify(polyline_points);
             image_str += "&sensor=false";
 
             
             // *****************  duration / stops *************************
             duration_str = "";
             if (stringify(travel_mode).equals("TRANSIT")){
                     duration_str += stringify(departure_time) + " - " + stringify(arrival_time);
                     duration_str += "\n\n";
                     duration_str += "(" + stringify(duration) + ", " + stringify(num_stops) + " stops )";
                 }
             else{
                     duration_str = "(About " + stringify(duration) + ")";
                 }
             
             
             // ***************** add to table *************************
             PdfPCell c1 = new PdfPCell();
             PdfPCell c2 = new PdfPCell();
             PdfPCell c3 = new PdfPCell();
             
             c1.setBorder(Rectangle.NO_BORDER);
             c2.setBorder(Rectangle.NO_BORDER);
             c3.setBorder(Rectangle.NO_BORDER);
             
             Image image;
             URL url;
             
             if (logo_str != null){
                 url = new URL(logo_str);
                 image = Image.getInstance(url);
                 c1.setImage(image);
             }
             
             Paragraph p = new Paragraph(directions_str + "\n\n" + duration_str);
             p.getFont().setSize(10);
             c2.setPhrase(p);
             
             url = new URL(image_str);
             image = Image.getInstance(url);
             c3.setImage(image);
                         
             directions_table.addCell(c1);
             directions_table.addCell(c2);
             directions_table.addCell(c3);
         }
         
         //big map
         String main_map_src = "http://maps.googleapis.com/maps/api/staticmap?size=900x3600&scale=2";
         for (int i=0;i< markers.size();i++){
             main_map_src += markers.get(i);
         }
         main_map_src += "&path=color:0x000000|weight:5|enc:" + stringify(overview_polyline_points);
         main_map_src += "&sensor=false";
         URL url = new URL(main_map_src);
         Image image = Image.getInstance(url);
         
         table.addCell(image);
         table.addCell(new Phrase("\n"));
         table.addCell(directions_table);
     }
     
     public static String stringify(JsonElement ob){
         if (ob!=null){
             return ob.getAsString();
         }
         else{
             return "";
         }
     }
     
     public static void createCalendar(Plan plan) throws Exception {
         FileOutputStream fos = new FileOutputStream(new File(ConfigService.getRoot() + "calendar" + plan.getId() + ".pdf"));
         Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
         document.open();
         Rectangle area = new Rectangle(36, 24, 559, 802);
         PdfWriter writer = PdfWriter.getInstance(document, fos);
         writer.setBoxSize("art", area);
         //writer.setPDFXConformance(PdfWriter.PDFX1A2001);
         Header header = new Header(plan.getTitle());
         writer.setPageEvent(header);
 
         document.open();
 
         int tableheight = 490;
         int tablewidth = 740;
 
         //create first page
         PdfPTable table = new PdfPTable(plan.getLength() + 1);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.setTotalWidth(tablewidth);
         table.setLockedWidth(true);
 
         int[] widths = new int[table.getNumberOfColumns()];
         widths[0] = 10;
         for (int counter = 1; counter < widths.length; counter++) {
             widths[counter] = (100 - 10) / plan.getLength();
         }
         table.setWidths(widths);
 
         Paragraph destination = new Paragraph("Trip to " + plan.getLocation().getName());
         destination.setAlignment(Element.ALIGN_CENTER);
         destination.getFont().setFamily("Arial");
         destination.getFont().setSize(20);
 
         Paragraph date = new Paragraph("\n\n" + DateUtil.getFullDay(plan.getStartdate()) + "\n\nto\n\n" + DateUtil.getFullDay(plan.getEnddate()));
         date.getFont().setFamily("Arial");
         date.setAlignment(Element.ALIGN_CENTER);
         date.getFont().setSize(16);
 
         table.addCell(getCaldendarAllCell(""));
 
         Calendar today = Calendar.getInstance();
         today.setTime(plan.getStartdate().getTime());
         while (true) {
             today.add(Calendar.DAY_OF_MONTH, 1);
             if (today.after(plan.getEnddate())) {
                 break;
             }
             table.addCell(getDateCell(today));
 
         }
         today.setTime(plan.getStartdate().getTime());
 
         int minhour = 9;
         int maxhour = 17;
         for (Event event : plan.getEvents()) {
             int s = event.getStartdate().get(Calendar.HOUR_OF_DAY);
             int e = event.getEnddate().get(Calendar.HOUR_OF_DAY);
             if (s < minhour) {
                 minhour = s;
             }
             if (e > maxhour) {
                 maxhour = e;
             }
         }
 
         int eachheight = 464 / (maxhour - minhour);
 
         for (int counter = minhour; counter < maxhour; counter++) {
             table.addCell(getTimeCell(counter + ":00", eachheight, counter == maxhour - 1));
             for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
                 PdfPCell cell = getEvent(plan,datecounter, counter, 0,eachheight/4);
                 if (cell!=null) {
                     table.addCell(cell);
                 }
             }
             for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
                 PdfPCell cell = getEvent(plan,datecounter, counter, 1,eachheight/4);
                 if (cell!=null) {
                     table.addCell(cell);
                 }
             }
             for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
                 PdfPCell cell = getEvent(plan,datecounter, counter, 2,eachheight/4);
                 if (cell!=null) {
                     table.addCell(cell);
                 }
             }
             for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
                 PdfPCell cell = getEvent(plan,datecounter, counter, 3,eachheight/4);
                 if (cell!=null) {
                     table.addCell(cell);
                 }
             }
         }
 
         document.add(table);
 
         document.close();
     }
 
     public static PdfPCell getDateCell(Calendar date) {
         PdfPCell datecell = getCaldendarAllCell(DateUtil.getFullDay(date));
         datecell.setFixedHeight(20);
         return datecell;
     }
 
     public static PdfPCell getTimeCell(String input, int height, boolean lastone) {
         PdfPCell time = getCaldendarTopCell(input);
         if (lastone) {
             time.setBorder(Rectangle.BOX);
         }
         time.setRowspan(4);
         time.setFixedHeight(height);
         return time;
     }
 
     public static PdfPCell getCaldendarAllCell(String text) {
         PdfPCell titlecell = new PdfPCell(new Paragraph(text));
         titlecell.setBorder(Rectangle.BOX);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         return titlecell;
     }
 
     public static PdfPCell getCaldendarTopCell(String text) {
         PdfPCell titlecell = new PdfPCell(new Paragraph(text));
         titlecell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         return titlecell;
     }
 
     public static PdfPCell getCaldendarBottomCell(String text) {
         PdfPCell titlecell = new PdfPCell(new Paragraph(text));
         titlecell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         return titlecell;
     }
 
     public static PdfPCell getCaldendarCell(String text) {
         PdfPCell titlecell = new PdfPCell(new Paragraph(text));
         titlecell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         return titlecell;
     }
 
     public static String merge(Plan plan) {
         try {
             List<InputStream> pdfs = new ArrayList<InputStream>();
             pdfs.add(new FileInputStream(ConfigService.getRoot() + "title" + plan.getId() + ".pdf"));
             pdfs.add(new FileInputStream(ConfigService.getRoot() + "calendar" + plan.getId() + ".pdf"));
             for (int datecounter = 0; datecounter < plan.getLength(); datecounter++) {
                 pdfs.add(new FileInputStream(ConfigService.getRoot() + "stitle" + plan.getId() + datecounter+ ".pdf"));
                 pdfs.add(new FileInputStream(ConfigService.getRoot() + "events" + plan.getId() + datecounter+".pdf"));
             }
             pdfs.add(new FileInputStream(ConfigService.getRoot() + "last" + plan.getId() + ".pdf"));
             OutputStream output = new FileOutputStream(ConfigService.getRoot() + "plan" + plan.getId() + ".pdf");
             concatPDFs(pdfs, output, true);
             return ConfigService.getRoot() + "plan" + plan.getId() + ".pdf";
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "";
     }
 
     public static void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, boolean paginate) {
 
         Document document = new Document();
         try {
             List<InputStream> pdfs = streamOfPDFFiles;
             List<PdfReader> readers = new ArrayList<PdfReader>();
             int totalPages = 0;
             Iterator<InputStream> iteratorPDFs = pdfs.iterator();
 
             // Create Readers for the pdfs.
             while (iteratorPDFs.hasNext()) {
                 InputStream pdf = iteratorPDFs.next();
                 PdfReader pdfReader = new PdfReader(pdf);
                 readers.add(pdfReader);
                 totalPages += pdfReader.getNumberOfPages();
             }
             // Create a writer for the outputstream
             PdfWriter writer = PdfWriter.getInstance(document, outputStream);
 
             document.open();
             BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
             PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
             // data
 
             PdfImportedPage page;
             int currentPageNumber = 0;
             int pageOfCurrentReaderPDF = 0;
             Iterator<PdfReader> iteratorPDFReader = readers.iterator();
 
             // Loop through the PDF files and add to the output.
             while (iteratorPDFReader.hasNext()) {
                 PdfReader pdfReader = iteratorPDFReader.next();
 
                 // Create a new page in the target for each source page.
                 while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                     document.newPage();
                     pageOfCurrentReaderPDF++;
                     currentPageNumber++;
                     page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                     cb.addTemplate(page, 0, 0);
 
                     // Code for pagination.
                     if (paginate) {
                         cb.beginText();
                         cb.setFontAndSize(bf, 9);
                         cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + currentPageNumber, 300, 20, 0);
                         cb.endText();
                     }
                 }
                 pageOfCurrentReaderPDF = 0;
             }
             outputStream.flush();
             document.close();
             outputStream.close();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (document.isOpen()) {
                 document.close();
             }
             try {
                 if (outputStream != null) {
                     outputStream.close();
                 }
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
     }
 
     public static PdfPCell getEvent(Plan plan,int datecounter, int hour, int slot,int eachheight) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(plan.getStartdate().getTime());
         cal.add(Calendar.DAY_OF_MONTH, datecounter);
         PdfPCell pcell = null; 
         int numslot = 1;
         PlanService.loadEventsForPlan(plan);
         for (Event event: plan.getEvents()) {
             if (startSlot(event.getStartdate(),cal,hour,slot)) {
                numslot = event.getNumberSlots();
                if (numslot>1) {
                    pcell = new PdfPCell(getEventParagraph(event.getAttraction().getName()+"\n"+DateUtil.getTime(event.getStartdate())+"-"+DateUtil.getTime(event.getEnddate())));
                }
                else {
                    pcell = new PdfPCell(getEventParagraph(event.getAttraction().getName()));
                }
                pcell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pcell.setRowspan(numslot);
             }
             else if (inSlot(event,cal,hour,slot)) {
                 return null;
             }
         }
         if (pcell==null) {
             switch (slot) {
                 case 0: pcell = getCaldendarTopCell("");
                     break;
                 case 3: pcell = getCaldendarBottomCell("");
                     break;
                 default:pcell = getCaldendarCell("");
             }
         }
         pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
         pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         pcell.setFixedHeight(eachheight*numslot);
         return pcell;
     }
     
     public static Paragraph getEventParagraph(String text) {
         Paragraph result = new Paragraph(text);
         result.getFont().setFamily("Arial");
         result.getFont().setSize(12);
         result.setAlignment(Element.ALIGN_CENTER);
         return result;
     }
     
     public static boolean startSlot(Calendar start,Calendar today,int hour,int slot) {
         return (start.get(Calendar.YEAR)==today.get(Calendar.YEAR) && 
             start.get(Calendar.MONTH)==today.get(Calendar.MONTH) && 
             start.get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH) && 
             start.get(Calendar.HOUR_OF_DAY)==hour && 
             start.get(Calendar.MINUTE)==(15*slot));
     }
     
     public static boolean inSlot(Event event,Calendar today,int hour,int slot) {
         if (event.getStartdate().get(Calendar.YEAR)==today.get(Calendar.YEAR) && 
             event.getStartdate().get(Calendar.MONTH)==today.get(Calendar.MONTH) && 
             event.getStartdate().get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH)) {
                 int bhour = event.getStartdate().get(Calendar.HOUR_OF_DAY)*60+event.getStartdate().get(Calendar.MINUTE);
                 int ehour = event.getEnddate().get(Calendar.HOUR_OF_DAY)*60+event.getEnddate().get(Calendar.MINUTE);
                 int current = hour*60+(slot*15) ;
                 return (bhour<=current && current<ehour);
         }
         return false;
     }
     
     public static String getData(String response,String prefix,String from,String to) {
         int index = response.indexOf(prefix);
         int findex = response.indexOf(from,index);
         int findexe = response.indexOf(to,findex);
         return response.substring(findex+from.length(),findexe);
     }
 
     public static String getLastData(String response,String prefix,String from,String to) {
         int index = response.lastIndexOf(prefix);
         int findex = response.indexOf(from,index);
         int findexe = response.indexOf(to,findex);
         return response.substring(findex+from.length(),findexe);
     }
 
     public static String clean(String line) {
         line = line.replaceAll("&lt;b&gt;","").replaceAll("&lt;/b&gt;","").replaceAll("&lt;i&gt;","").replaceAll("&lt;/i&gt;","");
         while (true) {
             int index = line.indexOf("&lt;div");
             if (index!=-1) {
                 int indexa = line.indexOf("&gt;",index);
                 int indexe = line.indexOf("&lt;/div&gt;",indexa);
                 line = line.substring(0,index)+" " + line.substring(indexa+4,indexe) + " " + line.substring(indexe+12);
             }
             else {
                 break;
             }
         }
         return line;
     }    
 
     public static void main(String[] args) throws Exception {
         Database.beginTransaction();
         MResult plan = PlanService.getPlan("BMJMJCZQB");
         createPlan((Plan) plan.getObject());
         Database.commitTransaction();
     }
 }
