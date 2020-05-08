 package my.triviagame.xmcd;
 
 import java.util.regex.Pattern;
 
 /**
  * Regular expressions for parsing xmcd files.
  */
 public class XmcdRegEx {
 
     /** Valid xmcd signature */
     public static Pattern SIGNATURE = Pattern.compile("^# xmcd");
     /** Beginning of track frame offsets */
     public static Pattern TRACK_FRAME_OFFSETS_START = Pattern.compile("^# Track frame offsets:", Pattern.MULTILINE);
     /** Group 1 will have a track frame offset */
     public static Pattern TRACK_FRAME_OFFSET = Pattern.compile("#\\s*(\\d+)?\r?\n");
     /** Group 1 will have disc length in seconds */
     public static Pattern DISC_LENGTH = Pattern.compile("^# Disc length: (\\d+)", Pattern.MULTILINE);
     /** Group 1 will have revision # */
     public static Pattern REVISION = Pattern.compile("^# Revision: (\\d+)", Pattern.MULTILINE);
     /** Group 1 will have FreeDB disc ID (additional IDs are ignored since this functionality is deprecated) */
     public static Pattern DISC_ID = Pattern.compile("^DISCID=([a-f0-9]+)", Pattern.MULTILINE);
     /** Group 1 will have disc title (may be an empty string) */
    public static Pattern DISC_TITLE = Pattern.compile("^DTITLE=(.*)", Pattern.MULTILINE);
     /**
      * Group 1 will have year (empty if year is not specified).
      * The spec required that years appear in a four-digit format, but sometimes they don't.
      * For instance I found a Japanese Pop album (I swear I'm not making this up) that has the year field set to
      * 19970925. Therefore we take at most four digits and prey.
      */
     public static Pattern YEAR = Pattern.compile("^DYEAR=(\\d{1,4}+)?", Pattern.MULTILINE);
     /** Group 1 will have genre in title-case */
     public static Pattern GENRE = Pattern.compile("^DGENRE=((?:[A-Z][a-z]* ?)+)", Pattern.MULTILINE);
     /** Group 1 will have track #, group 2 will have track title */
    public static Pattern TRACK_TITLE = Pattern.compile("^TTITLE(\\d+)=(.*)", Pattern.MULTILINE);
 }
