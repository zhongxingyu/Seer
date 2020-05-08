 package gov.usgs.cida.watersmart.parse.file;
 
 import gov.usgs.cida.netcdf.dsg.RecordType;
 import gov.usgs.cida.netcdf.dsg.Variable;
 import gov.usgs.cida.netcdf.jna.NCUtil;
 import gov.usgs.cida.watersmart.parse.StationLookup;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.stream.XMLStreamException;
 import org.joda.time.Instant;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Parses an individual file of the SYE R type (stations) need to be the filenames
  * 
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class SYEParser extends StationPerFileDSGParser {
     
     private static final Logger LOG = LoggerFactory.getLogger(SYEParser.class);
     
     // looking for gageId.txt but may have extra file stuff before it (in folder) use non-greedy for that
    private static final Pattern stationIdPattern = Pattern.compile("^(?:[^/]*/)*(\\d+)\\.txt$");
     
    private static final Pattern headerLinePattern = Pattern.compile("^[Dd]ate((?:\\s+\\w+)+)$");
     private static final Pattern headerVariablePattern = Pattern.compile("\\s+(\\w+)");
     
     // Line looks like '"x" "mm/dd/yyyy" val1 val2 ...'
     private static final Pattern dataLinePattern = Pattern.compile("^(\\d+/\\d+/\\d{4})((?:\\s+\\S+)+)$");
     // Could have split on spaces but using this regex instead
     private static final Pattern dataValuePattern = Pattern.compile("\\s+(\\S+)");
     
     public static final DateTimeFormatter inputDateFormatter = new DateTimeFormatterBuilder()
             .appendMonthOfYear(1)
             .appendLiteral('/')
             .appendDayOfMonth(1)
             .appendLiteral('/')
             .appendYear(4,4)
             .toFormatter()
             .withZoneUTC();
     
     private String filename;
     
     /**
      * Define all the regular expressions needed for parsing here
      * Should check that all necessary patterns are defined at some point
      * @param input 
      * @param name 
      * @param wfsUrl
      * @param layerName
      * @param commonAttr
      * @throws IOException
      * @throws XMLStreamException  
      */
     public SYEParser(InputStream input, String name, StationLookup lookup) throws IOException, XMLStreamException {
         super(input, lookup);
         this.filename = name;
     }
     
     /**
      * Create a pattern that captures the variable names and gives null for
      * non-variable column headers
      * @param headerLine Line to parse which has been identified as a header
      * @return String array of variable names
      */
     private List<Variable> headerVariables(String headerLine) {
         LinkedList<Variable> vars = new LinkedList<Variable>();
         Matcher matcher = headerVariablePattern.matcher(headerLine);
         while (matcher.find()) {
             String varname = matcher.group(1);
             if (null != varname) {
                 Map<String,Object> attrs = new TreeMap<String,Object>();
                 // assuming float for all var for now
                 Variable var = new Variable(varname, NCUtil.XType.NC_FLOAT, attrs);
                 vars.add(var);
             }
         }
         return vars;
     }
     
     @Override
     public RecordType parse() throws IOException {
         // define what we need for metadata
         this.stationIndex = stationLookup.lookup(getStationId(this.filename));
         if (this.stationIndex < 0) {
             throw new RuntimeException("Station not found during lookup. "
                     + "If this is a valid station ID, the geoserver"
                     + " instance could be down.");
         }
 
         reader.mark(READ_AHEAD_LIMIT);
         String line = null;
         boolean headerRead = false;
         List<Variable> vars = null;
         while (null != (line = reader.readLine())) {
             Matcher matcher = headerLinePattern.matcher(line);
             if (matcher.matches()) {
                 vars = headerVariables(matcher.group(1));
                 reader.mark(READ_AHEAD_LIMIT);
                 headerRead = true;
             }
             else if (headerRead) {
                 matcher = dataLinePattern.matcher(line);
                 if (matcher.matches()) {
                     String date = matcher.group(1);
                     Instant timestep = Instant.parse(date, getInputDateFormatter());
                     this.baseDate = timestep;
 
                     RecordType recordType = new RecordType("days since " + baseDate.toString());
                     for (Variable var : vars) {
                         recordType.addType(var);
                     }
 
                     reader.reset();
                     return recordType;
                 }
             }
         }
         return null;
     }
 
     @Override
     protected Pattern getDataLinePattern() {
         return dataLinePattern;
     }
 
     @Override
     protected Pattern getDataValuePattern() {
         return dataValuePattern;
     }
 
     @Override
     protected Pattern getHeaderLinePattern() {
         return headerLinePattern;
     }
 
     @Override
     protected Pattern getHeaderVariablePattern() {
         return headerVariablePattern;
     }
 
     @Override
     protected DateTimeFormatter getInputDateFormatter() {
         return inputDateFormatter;
     }
 
     @Override
     protected Pattern getStationIdPattern() {
         return stationIdPattern;
     }
 }
