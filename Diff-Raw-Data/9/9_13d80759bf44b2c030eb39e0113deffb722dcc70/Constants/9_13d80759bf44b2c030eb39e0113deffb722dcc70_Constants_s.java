 /*
  * Created On: May 25, 2006 10:46:50 PM
  * $Id$
  */
 package com.thinkparity;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.SimpleTimeZone;
 import java.util.TimeZone;
 
 /**
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 public final class Constants {
 
     /** Create Constants. */
     private Constants() { super(); }
 
     public static final class DateFormats {
         public static final SimpleDateFormat ImageLastRun = new SimpleDateFormat("yyyy MM dd HH:mm:ss.SSS");
         static { ImageLastRun.setTimeZone(TimeZones.GMT); }
     }
 
     public static final class Directories {
         public static final File ParityInstall = new File(System.getProperty(PropertyNames.ParityInstall));
     }
 
     public static final class FileNames {
         public static final String ThinkParityImageProperties = "thinkParityImage.properties";
         public static final String ThinkParityProperties = "thinkParity.properties";
     }
 
     public static final class PropertyNames {
         public static final String JavaLibraryPath = "java.library.path";
         public static final String ParityImageClassPath = "parity.image.classpath";
         public static final String ParityImageLastRun = "parity.image.lastrun";
         public static final String ParityImageLibraryPath = "parity.image.librarypath";
         public static final String ParityImageMain = "parity.image.main";
         public static final String ParityImageMainArgs = "parity.image.mainargs";
         public static final String ParityImageName = "parity.image.name";
         public static final String ParityInstall = "parity.install";
     }
 
     public static final class Sundry {
        public static final String ThinKParityHeader = "thinkParity:  Configuration";
         public static final String ThinkParityImageHeader = "thinkParity:  Image Configuration";
     }
 
     public static final class TimeZones {
         public static final TimeZone GMT = new SimpleTimeZone(0, "GMT");
     }
 }
