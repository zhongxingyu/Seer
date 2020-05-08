 package data;
 
 import models.store.Organisation;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class AdaptedEvent {
 
   static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd mm");
   public final String firstSide;
   public final String secondSide;
   public final double firstKof;
   public final double secondKof;
   public final Organisation organisation;
   public final Date date;
  public final Date adoptedDate;
   public final String code;
 
   public AdaptedEvent(String firstSide, String secondSide, double firstKof, double secondKof, Organisation organisation, Date date, Object firstSideCode,
                       String secondSideCode) {
     this.firstSide = firstSide;
     this.secondSide = secondSide;
     this.firstKof = firstKof;
     this.secondKof = secondKof;
     this.organisation = organisation;
     this.date = date;
    this.adoptedDate = new Date();
     this.code = firstSideCode + "_" + secondSideCode + "_" + DATE_FORMAT.format(date);
   }
 }
