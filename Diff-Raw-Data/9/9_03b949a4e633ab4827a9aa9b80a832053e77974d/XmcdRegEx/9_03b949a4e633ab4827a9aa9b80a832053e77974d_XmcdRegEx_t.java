 package my.triviagame.xmcd;
 
 import java.util.regex.Pattern;
 
 /**
  * Regular expressions for parsing xmcd files.
  */
 public class XmcdRegEx {
 
     /** Find a match against the xmcd signature */
    public static Pattern SIGNATURE = Pattern.compile("^# xmcd\\r?\\n");
     /** Group 0 will have disc length in seconds */
     public static Pattern DISC_LENGTH = Pattern.compile("^# Disc length: (\\d+) seconds$", Pattern.MULTILINE);
     /** Group 0 will have track frame offset */
     public static Pattern TRACK_FRAME_OFFSET = Pattern.compile("^#\\s*(\\d+)$", Pattern.MULTILINE);
     /** Group 0 will have revision # */
     public static Pattern REVISION = Pattern.compile("^# Revision: (\\d+)$", Pattern.MULTILINE);
     /** Group 0 will have FreeDB disc ID */
     public static Pattern DISC_ID = Pattern.compile("^DISCID=([a-f,0-9]+)$", Pattern.MULTILINE);
     /** Group 0 will have disc title */
     public static Pattern DISC_TITLE = Pattern.compile("^DTITLE=(.+)$", Pattern.MULTILINE);
     /** Group 0 will have year */
     public static Pattern YEAR = Pattern.compile("^DYEAR=(\\d+)$", Pattern.MULTILINE);
     /** Group 0 will have genre in title-case */
     public static Pattern GENRE = Pattern.compile("^DGENRE=((?:[A-Z][a-z]* ?)+)$", Pattern.MULTILINE);
     /** Group 0 will have track #, group 1 will have track title */
     public static Pattern TRACK_TITLE = Pattern.compile("^TTITLE(\\d+)=(.+)$", Pattern.MULTILINE);
 }
