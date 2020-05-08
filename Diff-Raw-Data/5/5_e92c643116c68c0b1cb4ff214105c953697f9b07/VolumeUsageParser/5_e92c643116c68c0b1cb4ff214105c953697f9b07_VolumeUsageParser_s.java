 package au.id.teda.broadband.usage.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Xml;
 
 public class VolumeUsageParser {
 	
 	//private static final String DEBUG_TAG = "bbusage";
 	
 	private static final String ns = null; // We don't use namespaces
 	private static final String FEED_TAG = "ii_feed";
 	private static final String VOLUME_USAGE_TAG = "volume_usage";
 	private static final String DAY_HOUR_TAG = "day_hour";
 	private static final String USAGE_TAG = "usage";
 	private static final String PERIOD_ATT	= "period";
 	private static final String TYPE_ATT = "type";
 	private static final String PEAK = "peak";
 	private static final String OFFPEAK = "offpeak";
 	private static final String UPLOADS = "uploads";
 	private static final String FREEZONE = "freezone";
 	
	private static final String FORMAT_YYYYDD = "yyyydd";
 	private static final String FORMAT_YYYY_MM_DD  = "yyyy-MM-dd";
 	
 	// Flag to make sure we set the month only once during parsing
 	private boolean monthSetFlag = false;
 	private String mDataMonth = null;
 	
 	// This class represents the account info in the XML feed.
 	public static class VolumeUsage {
 		public final Calendar day;
 		public final String month;
 	    public final Long peak;
 	    public final Long offpeak;
 	    public final Long uploads;
 	    public final Long freezone;
 
 	    private VolumeUsage(Calendar day, String month
 	    		, Long peak, Long offpeak
 	    		, Long uploads, Long freezone) {
 	    	
 	    	this.month = month;
 	    	this.day = day;
 	        this.peak = peak;
 	        this.offpeak = offpeak;
 	        this.uploads = uploads;
 	        this.freezone = freezone;
 	    }
 	}
 	
 	public List<VolumeUsage> parse (InputStream inputStream) throws XmlPullParserException, IOException {
 		try {
 			XmlPullParser parser = Xml.newPullParser();
 	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 	        parser.setInput(inputStream, null);
 	        parser.nextTag();
 	        return readFeed(parser);
 	    } finally {
 	    	inputStream.close();
 	    }
 	}
 	
 	private List<VolumeUsage> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
 		
 		List<VolumeUsage> usage = new ArrayList<VolumeUsage>();
 		
 	    parser.require(XmlPullParser.START_TAG, ns, FEED_TAG);
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    		
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(VOLUME_USAGE_TAG)) {
 	    		usage = readVolumeUsage(parser);
 	    	} else {
 	    		skip(parser);
 	    	}
 	    }
 	    return usage;
 	}
 	
     public List<VolumeUsage> readVolumeUsage(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
     	List<VolumeUsage> usage = new ArrayList<VolumeUsage>();
     	
 	    parser.require(XmlPullParser.START_TAG, ns, VOLUME_USAGE_TAG);
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    	
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(VOLUME_USAGE_TAG)){
 	    		usage = readVolumeUsage2(parser);
 	    	} else {
                 skip(parser);
             }
 	    }
 	    return usage;   
     }
     
     public List<VolumeUsage> readVolumeUsage2(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
     	List<VolumeUsage> usage = new ArrayList<VolumeUsage>();
     	
 	    parser.require(XmlPullParser.START_TAG, ns, VOLUME_USAGE_TAG);
         
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    	
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(DAY_HOUR_TAG)){
 	    		usage.add(readDayHour(parser));
 	    	} else {
                 skip(parser);
             }
 	    }
 	    return usage;
     }
     
     public VolumeUsage readDayHour(XmlPullParser parser) throws XmlPullParserException, IOException {
     	Calendar mPeriod = getPeriodValue(parser.getAttributeValue(null, PERIOD_ATT));
     	
     	parser.require(XmlPullParser.START_TAG, ns, DAY_HOUR_TAG);
     	
     	if (!monthSetFlag){
     		mDataMonth = getMonthString(mPeriod);
     		monthSetFlag = true;
     	}
     	
     	
     	Long mPeak = null;
     	Long mOffPeak = null;
     	Long mUploads  = null;
     	Long mFreezone = null;
     	
     	while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             
             String tagName = parser.getName();
             String tagAtt = parser.getAttributeValue(null, TYPE_ATT);
             if (tagName.equals(USAGE_TAG)){
             	if (tagAtt.equals(PEAK)){
             		mPeak = readUsage(parser);
 	    		} else if (tagAtt.equals(OFFPEAK)){
 	    			mOffPeak = readUsage(parser);
 	    		} else if (tagAtt.equals(UPLOADS)){
 	    			mUploads = readUsage(parser);
 	    		} else if (tagAtt.equals(FREEZONE)){
 	    			mFreezone = readUsage(parser);
 	    		}
 	    	} else {
                 skip(parser);
             }
     	}
     	
     	return new VolumeUsage(mPeriod, mDataMonth, mPeak, mOffPeak, mUploads, mFreezone );
     }
 	
     private Long readUsage(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, USAGE_TAG);
         String usage = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, USAGE_TAG);
         return stringToLong(usage);
     }
     
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String text = null;
         if (parser.next() == XmlPullParser.TEXT) {
         	text = parser.getText();
             parser.nextTag();
         }
         return text;
     }
     
     private Calendar getPeriodValue(String period){
     	SimpleDateFormat hourMintueFormat = new SimpleDateFormat(FORMAT_YYYY_MM_DD);
     	Calendar timeValue = Calendar.getInstance();
 		try {
 			timeValue.setTime(hourMintueFormat.parse(period));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return timeValue;
     }
     
     private String getMonthString(Calendar period){
     	period.add(Calendar.DATE, 27 );
    	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYYDD);
     	String dataMonth = sdf.format(period.getTime());
     	return dataMonth;
     }
 	
     private Long stringToLong(String s){
     	Long l = Long.parseLong(s);
     	return l;
     }
 	
     // Skips tags the parser isn't interested in. Uses depth to handle nested tags.
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
         	switch (parser.next()) {
             case XmlPullParser.END_TAG:
                     depth--;
                     break;
             case XmlPullParser.START_TAG:
                     depth++;
                     break;
             }
         }
     }
 
 }
