 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.tripbrush.service;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Chunk;
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
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
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
 
         Paragraph destination = new Paragraph("Trip to " + plan.getLocation().getName());
         destination.setAlignment(Element.ALIGN_CENTER);
         destination.getFont().setFamily("Arial");
         destination.getFont().setSize(20);
 
         Paragraph date = new Paragraph("\n\n" + DateUtil.getFullDay(plan.getStartdate()) + "\n\nto\n\n" + DateUtil.getFullDay(plan.getEnddate()));
         date.getFont().setFamily("Arial");
         date.setAlignment(Element.ALIGN_CENTER);
         date.getFont().setSize(16);
 
 
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
 
         int tableheight = 740;
         int tablewidth = 500;
 
         //create first page
         PdfPTable table = new PdfPTable(1);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.setTotalWidth(tablewidth);
         table.setLockedWidth(true);
 
         int[] widths = {1};
         table.setWidths(widths);
 
         Paragraph destination = new Paragraph("TripBrush. Let's Paint!");
         destination.setAlignment(Element.ALIGN_CENTER);
         destination.getFont().setFamily("Arial");
         destination.getFont().setSize(20);
 
         PdfPCell titlecell = new PdfPCell();
         titlecell.setBorder(Rectangle.BOX);
         titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
         titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         titlecell.setFixedHeight(tableheight);
 
         titlecell.addElement(destination);
 
         table.addCell(titlecell);
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
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(plan.getStartdate().getTime());
         cal.add(Calendar.DAY_OF_MONTH, datecounter);
         
         Paragraph date = new Paragraph("\n\nDay" + (datecounter+1) + "\n\n" + DateUtil.getFullDay(cal));
         date.getFont().setFamily("Arial");
         date.setAlignment(Element.ALIGN_CENTER);
         date.getFont().setSize(16);
 
 
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
         PdfPTable table = null;
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
                 datetitle.setUnderline(0.1f,-2f);
                 datetitle.getFont().setFamily("Arial");
                 datetitle.getFont().setSize(16);
                 datetitle.getFont().setColor(51,102,00);
                 
                 PdfPCell datecell = new PdfPCell();
                 datecell.setBorder(Rectangle.NO_BORDER);
                 datecell.addElement(datetitle);
                 datecell.setColspan(2);
 
                 
                 Paragraph time = new Paragraph(sevent.getDuration());
                 time.getFont().setFamily("Arial");
                 time.getFont().setSize(14);
                 
                 PdfPCell timecell = new PdfPCell(time);
                 timecell.setBorder(Rectangle.NO_BORDER);
                 timecell.setColspan(2);
                 
                 Attraction attraction = sevent.getAttraction();
                 
                 Paragraph eventname = new Paragraph(attraction.getName());
                 eventname.getFont().setFamily("Arial");
                 eventname.getFont().setSize(14);
                
                 Paragraph eventdesc = new Paragraph(attraction.getDescription());
                 eventdesc.getFont().setFamily("Arial");
                 eventdesc.getFont().setSize(12);                
                 
                 PdfPCell eventcell = new PdfPCell();
                 eventcell.addElement(eventname);
                 eventcell.addElement(eventdesc);
                 eventcell.setBorder(Rectangle.NO_BORDER);
                 eventcell.setColspan(1);
                 
                 
                 URL url = new URL(ConfigService.getUrl()+ "/includes/images/data/"+attraction.getImageFileName());
                 Image image = Image.getInstance(url);
                 PdfPCell piccell = new PdfPCell(image);
                 piccell.setBorder(Rectangle.NO_BORDER);
                 piccell.setColspan(1);
                 
                 Paragraph eventhours = new Paragraph("Opening Hours");
                 eventhours.getFont().setFamily("Arial");
                 eventhours.getFont().setSize(14);                   
 
                 
                 CalendarService.loadAttractionTimes(plan, attraction);
                 PdfPCell description = new PdfPCell();
                 description.addElement(eventhours);
                 description.setBorder(Rectangle.NO_BORDER);
                 description.setColspan(2);        
                 
                 for (AttractionOpenView view: attraction.getOpeningTimes()) {
                     Paragraph eventtimes = new Paragraph(DateUtil.getDay(view.getFrom())+" " + DateUtil.getTime(view.getFrom()) + "-"+ DateUtil.getTime(view.getTo()));
                     eventtimes.getFont().setFamily("Arial");
                     eventtimes.getFont().setSize(12);
                     description.addElement(eventtimes);
                 }
                 
                 
 
                 
                 if (!StringUtil.isEmpty(sevent.getAttraction().getPhone())) {
                     Paragraph eventphone = new Paragraph("Telephone: " + attraction.getPhone());
                     eventphone.getFont().setFamily("Arial");
                     eventphone.getFont().setSize(14);  
                     description.addElement(eventphone);
                 }
                 
                 if (!StringUtil.isEmpty(sevent.getAttraction().getAddress())) {
                     Paragraph eventaddress = new Paragraph("Address: " + attraction.getAddress());
                     eventaddress.getFont().setFamily("Arial");
                     eventaddress.getFont().setSize(14);      
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
                     
                     Paragraph directions = new Paragraph("Directions from A. " + attraction.getName() + " to B. " + nextatt.getName());
                     directions.getFont().setFamily("Arial");
                     directions.getFont().setSize(14);
 
                     PdfPCell dcell = new PdfPCell(directions);
                     dcell.setBorder(Rectangle.NO_BORDER);
                     dcell.setColspan(1);
                     table1.addCell(dcell);
                     
                     String fpostcode = attraction.getPostcode().replaceAll(" ","")+",UK";
                     String tpostcode = nextatt.getPostcode().replaceAll(" ","")+",UK";
     
         
                     String wsearchUrl = "http://maps.googleapis.com/maps/api/directions/xml?origin=" + fpostcode + "&destination=" + tpostcode + "&sensor=false&mode=walking";
                     String response = Browser.getPage(wsearchUrl).toString();
                     int duration = Integer.parseInt(getLastData(response, "<duration>", "<value>", "</value>"));
                     String durationt = "Walking Time: " + getLastData(response, "<duration>", "<text>", "</text>");
                     if (duration>60*(ConfigService.getMaxWalking())) {
                        String dsearchUrl = "http://maps.googleapis.com/maps/api/directions/xml?origin=" + fpostcode + "&destination=" + tpostcode + "&sensor=false&mode=transit";
                         response = Browser.getPage(dsearchUrl).toString();
                         durationt = "Driving Time: " + getLastData(response, "<duration>", "<text>", "</text>");
                     }
 
                     double latfrom = Double.parseDouble(getData(response,"<start_location>","<lat>","</lat>"));
                     double lngfrom = Double.parseDouble(getData(response,"<start_location>","<lng>","</lng>"));
                     double latto= Double.parseDouble(getLastData(response,"<end_location>","<lat>","</lat>"));
                     double lngto = Double.parseDouble(getLastData(response,"<end_location>","<lng>","</lng>"));
                     String polyline = getData(response,"<overview_polyline>","<points>","</points>");
 
                     PdfPCell instructions = new PdfPCell();
                     instructions.setBorder(Rectangle.NO_BORDER);
                    Paragraph p = new Paragraph("Written Directions (" + durationt + ")");
                     instructions.addElement(p);                    
                     String header = "<html_instructions>";
                     String headere = "</html_instructions>";
                     int index = 0;
                     while (true) {
                         int indexp = response.indexOf(header,index);
                         int indexpe = response.indexOf(headere,indexp);
                         if (indexp<index) break;
                         String line = clean(response.substring(indexp+header.length(),indexpe));
                         p = new Paragraph(line);
                         instructions.addElement(p);
                         index = indexpe+1;
                     }
 
 
                     URL google = new URL("http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=500x250&path=weight:3|color:red|enc:" + polyline + "&markers=label:A|" + latfrom + "," + lngfrom + "&markers=label:B|" + latto + "," + lngto);
                     Image image1 = Image.getInstance(google);
                     PdfPCell smap = new PdfPCell(image1);
                     smap.setBorder(Rectangle.NO_BORDER);
                     smap.setColspan(1);
                     
                     table1.addCell(smap);
                     table1.addCell(instructions);
                     
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
             titlecell.setBorder(Rectangle.BOX);
             titlecell.setHorizontalAlignment(Element.ALIGN_CENTER);
             titlecell.setVerticalAlignment(Element.ALIGN_MIDDLE);
             titlecell.setFixedHeight(tableheight);
 
             titlecell.addElement(noact);
 
             table.addCell(titlecell);  
             document.add(table); 
         }
                  
         document.close();
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
