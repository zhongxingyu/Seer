 package de.metalab.namnam.export.ical;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import de.metalab.namnam.export.NamNamExportException;
 import de.metalab.namnam.export.NamNamExporter;
 import namnam.model.Mensaessen;
 import namnam.model.Tagesmenue;
 import net.fortuna.ical4j.data.CalendarOutputter;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Date;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.TimeZoneRegistry;
 import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
 import net.fortuna.ical4j.model.ValidationException;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.property.CalScale;
 import net.fortuna.ical4j.model.property.Description;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.Uid;
 import net.fortuna.ical4j.model.property.Version;
 import net.fortuna.ical4j.util.UidGenerator;
 
 /**
 * module to export a mensa object to an ical ics file
  * @author fake
  */
 public class NamNamICALExporter extends NamNamExporter {
 
     private static Logger logger = Logger.getLogger(NamNamICALExporter.class.getName());
     private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 
     protected void doExport(OutputStream os) throws NamNamExportException{
         try {
             // Create a TimeZone
             TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
             TimeZone timezone = registry.getTimeZone("Europe/Berlin");
             VTimeZone tz = timezone.getVTimeZone();
 
             Calendar calendar = new Calendar();
             calendar.getProperties().add(new ProdId("-//NamNam//iCal4j 1.0//DE"));
             calendar.getProperties().add(Version.VERSION_2_0);
             calendar.getProperties().add(CalScale.GREGORIAN);
 
             UidGenerator ug = new UidGenerator("1");
 
 
             Iterator<Tagesmenue> dMit = mensa.getDayMenues().iterator();
 
             while(dMit.hasNext()) {
                 Tagesmenue t = dMit.next();
 
                 Iterator<Mensaessen> eIt = t.getMenues().iterator();
                 while(eIt.hasNext()) {
                     Mensaessen m = eIt.next();
                     java.util.Calendar date = new GregorianCalendar(timezone, Locale.GERMAN);
                     date.setTime(t.getTag());
                     // i really jave no idea why this is needed. all dates are offset back by one day
                     // when construcing below "ical4j Date" . then again, i'm using rc2 here.
                     date.add(java.util.Calendar.DAY_OF_MONTH, 1); // XXX TODO FIXME
                     
                     Date d = new Date(date.getTime());
                     VEvent happa = new VEvent(d, m.getBeschreibung());
                     happa.getProperties().add(tz.getTimeZoneId());
                     String preis = "Studenten: " + m.getStudentenPreis() + " €\n" + "Regulär: "+ m.getPreis() + " €";
 
                     String descStr = "";
                     if(m.isBeef()) {
                         descStr = "(mit Rindfleisch)\n";
                     } else if(m.isMoslem()) {
                         descStr = "(kein Schweinefleisch)\n";
                     } else if(m.isVegetarian()) {
                         descStr = "(vegetarisch)\n";
                     }
                     descStr += preis;
 
                     Description desc = new Description(descStr);
                     happa.getProperties().add(desc);
 
                     happa.getProperties().add(genUUID(d,m));
 
                     calendar.getComponents().add(happa);
                 }
             }
 
 
             CalendarOutputter outputter = new CalendarOutputter();
             outputter.output(calendar, os);
         } catch  (ValidationException vdex) {
             logger.log(Level.SEVERE, "Validation Exception while exporting", vdex);
             throw new NamNamExportException("Validation error while exporting",vdex);
         } catch  (IOException ioex) {
             logger.log(Level.SEVERE, "IO Exception while exporting", ioex);
             throw new NamNamExportException("IO error while exporting",ioex);
         }
 
     }
 
     private Uid genUUID(Date tag, Mensaessen essen) {
         String uid = tag.toString();
         uid+=essen.getBeschreibung().hashCode()+"";
         uid+=(essen.isBeef()?"R":"")+(essen.isMoslem()?"X":"")+(essen.isVegetarian()?"V":"");
         return new Uid(uid);
     }
 
     @Override
     public String getFileName() {
         return mensa.getName()+".ics";
     }
 
 }
