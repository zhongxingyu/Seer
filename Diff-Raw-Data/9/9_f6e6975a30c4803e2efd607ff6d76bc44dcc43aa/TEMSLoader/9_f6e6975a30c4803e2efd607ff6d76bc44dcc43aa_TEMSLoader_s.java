 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 
import org.amanzi.net.loader.internal.NeoLoaderPlugin;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 
 public class TEMSLoader {	
 	public static enum MeasurementRelationshipTypes implements RelationshipType {
 		FIRST,
 		LAST,
 		NEXT,
 		CHILD,
 		SOURCE,
 		POINT
 	}
 	private NeoService neo;
 	private String filename = null;
 	private String basename = null;
 	private Node file = null;
 	private Node point = null;
 	private String[] headers = null;
 	private HashMap<String,Integer> headerIndex = null;
 	private int line_number = 0;
     private int first_line = 0;
     private int last_line = 0;
     private String previous_ms = null;
     private String previous_time = null;
     private int previous_pn_code = -1;
     private String previous_latlong = null;
     private String latlong = null;
     private long started = System.currentTimeMillis();
     private String time = null;
     private HashMap<Integer,int[]> stats = new HashMap<Integer,int[]>();
     private HashMap<String,float[]> signals = new HashMap<String,float[]>();
     private int count_valid_message = 0;
     private int count_valid_location = 0;
     private int count_valid_changed = 0;
     private int limit = 0;
     private static int[] times = new int[2];
 
 	public TEMSLoader(String filename) {
 		this(null,filename);
 	}
 
 	public TEMSLoader(NeoService neo, String filename) {
 		this.neo = neo;
 		if(this.neo == null) this.neo = org.neo4j.neoclipse.Activator.getDefault().getNeoServiceSafely();
 		this.filename = filename;
 		this.basename = (new File(filename)).getName();
 	}
 
 	public void run() throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(filename));
 		try{
 			String line;
 			while((line = reader.readLine())!=null) {
 				line_number++;
 				if(headers==null) parseHeader(line);
 				else parseLine(line);
 			}
 		}finally{
 			reader.close();
 			saveData();
 		}
 	}
 
     private String status(){
     	if(started<=0) started = System.currentTimeMillis();
         return (line_number>0 ? "line:"+line_number : ""+((System.currentTimeMillis()-started)/1000.0)+"s");
     }
 
     private void debug(String line){
 		NeoLoaderPlugin.debug("TEMS:"+basename+":"+status()+": "+line);
 	}
 	
 	private void info(String line){
 		NeoLoaderPlugin.info("TEMS:"+basename+":"+status()+": "+line);
 	}
 	
 	private void notify(String line){
 		NeoLoaderPlugin.notify("TEMS:"+basename+":"+status()+": "+line);
 	}
 	
 	private void error(String line){
 		NeoLoaderPlugin.error("TEMS:"+basename+":"+status()+": "+line);
 	}
 	
 	public int getLimit(){
 		return limit;
 	}
 
 	public void setLimit(int value){
 		this.limit = value;
 	}
 	
 	/**
 	 * Converts to lower case and replaces all illegal characters with '_' and removes training '_'
 	 * @param original header String
 	 * @return edited String
 	 */
 	private static String cleanHeader(String header) {
     	return header.replaceAll("[\\s\\-\\[\\]\\(\\)\\/\\.]+", "_").replaceAll("\\_$", "").toLowerCase();
     }
 
 	private void parseHeader(String line){
 		debug(line);
 		String fields[] = line.split("\\t");
 		if(fields.length<2) return;
 		headers = fields;
 		headerIndex = new HashMap<String,Integer>();
 		int index=0;
 		for(String header:headers){
 			header = cleanHeader(header);
 			debug("Added header["+index+"] = "+header);
 			headerIndex.put(header,index++);
 		}
 	}
 	private int i_of(String header){
 		return headerIndex.get(header);
 	}
 	
     private double dbm2mw(int dbm){
     	return Math.pow(10.0, (((float)dbm)/10.0));
     }
     private float mw2dbm(double mw){
       return (float)(10.0*Math.log10(mw));
     }
 	
 	private void parseLine(String line){
 		//debug(line);
 		String fields[] = line.split("\\t");
 		if(fields.length<2) return;
 
         this.time = fields[i_of("time")];
         String ms = fields[i_of("ms")];
         String event = fields[i_of("event")];    // currently only getting this as a change marker
         String message_type = fields[i_of("message_type")];    // need this to filter for only relevant messages
         //message_id = fields[i_of("message_id")];    // parsing this is not faster
         if(!"EV-DO Pilot Sets Ver2".equals(message_type)) return;
         this.count_valid_message += 1;
         //return unless message_id == '27019'    // not faster
         //return unless message_id.to_i == 27019    // not faster
         
         // TODO: Ignore lines with Event=~/Idle/ since these generally contain invalid All-RX-Power
         // TODO: Also be careful of any All-RX-Power of -63 (since it is most often invalid data)
         // TODO: If number of PN codes does not match number of EC-IO make sure to align correct values to PNs
 
         String latitude = fields[i_of("all_latitude")];
         String longitude = fields[i_of("all_longitude")];
         this.latlong = latitude+"\t"+longitude;
         if(!this.latlong.equals(this.previous_latlong)){
           saveData();	// persist the current data to database
           this.previous_latlong = this.latlong;
         }
         if(latitude.length()==0 || longitude.length()==0) return;
         this.count_valid_location += 1;
 
         int channel = 0;
         int pn_code = 0;
         int ec_io = 0;
         int measurement_count = 0;
         try{
         channel = Integer.parseInt(fields[i_of("all_active_set_channel_1")]);
         pn_code = Integer.parseInt(fields[i_of("all_active_set_pn_1")]);
         ec_io = Integer.parseInt(fields[i_of("all_active_set_ec_io_1")]);
         measurement_count = Integer.parseInt(fields[i_of("all_pilot_set_count")]);
         }catch(NumberFormatException e){
         	error("Failed to parse a field on line "+line_number+": "+e.getMessage());
         	e.printStackTrace(System.err);
         }
         if(measurement_count > 12){
             error("Measurement count "+measurement_count+" > 12");
             measurement_count = 12;
         }
         boolean changed = false;
         if(!ms.equals(this.previous_ms)){
             changed = true;
             this.previous_ms = ms;
         }
         if(!this.time.equals(this.previous_time)){
             changed = true;
             this.previous_time = this.time;
         }
         if(pn_code!=this.previous_pn_code){
         	if(this.previous_pn_code>=0){
         		error("SERVER CHANGED");
         	}
        		changed = true;
             this.previous_pn_code = pn_code;
         }
         if(measurement_count > 0 && (changed || event.length()>0)){
           if(this.limit>0 && this.count_valid_changed > this.limit) return;
           if(first_line==0) first_line = line_number;
           last_line = line_number;
           this.count_valid_changed += 1;
           debug(time+": server channel["+channel+"] pn["+pn_code+"] Ec/Io["+ec_io+"]\t"+event+"\t"+this.latlong);
           for(int i=1;i<=measurement_count;i++){
             // Delete invalid data, as you can have empty ec_io
             // zero ec_io is correct, but empty ec_io is not
         	try{
         		ec_io = Integer.parseInt(fields[i_of("all_pilot_set_ec_io_"+i)]);
 	            channel = Integer.parseInt(fields[i_of("all_pilot_set_channel_"+i)]);
 	            pn_code = Integer.parseInt(fields[i_of("all_pilot_set_pn_"+i)]);
 	            debug("\tchannel["+channel+"] pn["+pn_code+"] Ec/Io["+ec_io+"]");
 	            if(!stats.containsKey(pn_code)) this.stats.put(pn_code,new int[2]);
 	            stats.get(pn_code)[0]+=1;
 	            stats.get(pn_code)[1]+=ec_io;
 	            String chan_code = ""+channel+"\t"+pn_code;
 	            if(!signals.containsKey(chan_code)) signals.put(chan_code,new float[2]);
 	            signals.get(chan_code)[0] += dbm2mw(ec_io);
 	            signals.get(chan_code)[1] += 1;
             }catch(Exception e){
             	error("Error parsing column "+i+" for EC/IO, Channel or PN: "+e.getMessage());
             	e.printStackTrace(System.err);
             }
           }
         }
 	}
 
 	/**
 	 * This method is called to dump the current cache of signals as one located point
 	 * linked to a number of signal strength measurements.
 	 */
 	private void saveData() {
 		if (signals.size() > 0) {
 			Transaction tx = neo.beginTx();
 			try {
 				Node mp = neo.createNode();
 				mp.setProperty("type", "mp");
 				mp.setProperty("time", this.time);
 				mp.setProperty("first_line", first_line);
 				mp.setProperty("last_line", last_line);
 				String[] ll = latlong.split("\\t");
 				mp.setProperty("lat", ll[0]);
 				mp.setProperty("long", ll[1]);
 				if (file == null) {
 					Node reference = neo.getReferenceNode();
 					for (Relationship relationship : reference.getRelationships(MeasurementRelationshipTypes.CHILD,
 							Direction.OUTGOING)) {
 						Node node = relationship.getEndNode();
 						if (node.hasProperty("type") && node.getProperty("type").equals("file") && node.hasProperty("name")
 								&& node.getProperty("name").equals(basename))
 							file = node;
 					}
 					if (file == null) {
 						file = neo.createNode();
 						file.setProperty("type", "file");
 						file.setProperty("name", basename);
 						file.setProperty("filename", filename);
 						reference.createRelationshipTo(file, MeasurementRelationshipTypes.CHILD);
 					}
 					file.createRelationshipTo(mp, MeasurementRelationshipTypes.FIRST);
 					debug("Added '" + mp.getProperty("time") + "' as first measurement of '" + file.getProperty("filename"));
 				}
 				debug("Added measurement point: " + propertiesString(mp));
 				if (point != null) {
 					point.createRelationshipTo(mp, MeasurementRelationshipTypes.NEXT);
 				}
 				point = mp;
 				for (String chanCode : signals.keySet()) {
 					float[] signal = signals.get(chanCode);
 					double mw = signal[0] / signal[1];
 					Node ms = neo.createNode();
 					String[] cc = chanCode.split("\\t");
 					ms.setProperty("type", "ms");
 					ms.setProperty("channel", cc[0]);
 					ms.setProperty("code", cc[1]);
 					ms.setProperty("dbm", mw2dbm(mw));
 					ms.setProperty("mw", mw);
 					debug("\tAdded measurement: " + propertiesString(ms));
 					point.createRelationshipTo(ms, MeasurementRelationshipTypes.CHILD);
 				}
 				tx.success();
 			} finally {
 				tx.finish();
 			}
 		}
 		signals.clear();
 		first_line = 0;
 		last_line = 0;
 	}
 
 	private static String propertiesString(Node node){
 		StringBuffer properties = new StringBuffer();
 		for(String property:node.getPropertyKeys()) {
 			if(properties.length()>0) properties.append(", ");
 			properties.append(property).append(" => ").append(node.getProperty(property));
 		}
 		return properties.toString();
 	}
 
 	private static int addTimes(long taken) {
 		times[0] += 1;
 		times[1] += taken;
 		return times[0];
 	}
 
 	private static void printTimesStats() {
 		System.err.println("Finished " + times[0] + " loads in " + times[1] / 60000.0 + " minutes (average "
 				+ (times[1] / times[0]) / 1000.0 + " seconds per load)");
 	}
 	
 	public void printStats(){
         long taken = System.currentTimeMillis()-started;
         addTimes(taken);
         notify("Finished loading "+basename+" data in "+(taken/1000.0)+" seconds");
         notify("Read "+(line_number-1)+" data lines and then filtered down to:");
         notify("\t"+count_valid_message+" with valid messages");
         notify("\t"+count_valid_location+" with known locations");
         notify("\t"+count_valid_changed+" with changed data");
         notify("Read "+stats.keySet().size()+" unique PN codes:");
         for(int pn_code:stats.keySet()){
         	int[] pn_counts = stats.get(pn_code);
         	notify("\t"+pn_code+" measured "+pn_counts[0]+" times (average Ec/Io = "+pn_counts[1]/pn_counts[0]+")");
         }
 		if(file!=null){
 			printMeasurements(file);
 		}else{
 			error("No measurement file node found");
 		}
 	}
 
 	private void printMeasurement(Node measurement){
 		info("Found measurement: "+propertiesString(measurement)+" --- "+childrenString(measurement));
 	}
 
 	private String childrenString(Node node){
 		StringBuffer sb = new StringBuffer();
 		for(Relationship relationship:node.getRelationships(MeasurementRelationshipTypes.CHILD,Direction.OUTGOING)){
 			if(sb.length()>0) sb.append(", ");
 			Node ms = relationship.getEndNode();
 			sb.append(ms.getProperty("channel")).append(":");
 			sb.append(ms.getProperty("code")).append("=");
 			sb.append((ms.getProperty("dbm").toString()+"000000").substring(0,6));
 		}
 		return sb.toString();		
 	}
 	private void printMeasurements(Node file){
 		if (file == null)
 			return;
 		Transaction tx = neo.beginTx();
 		try {
 			for (Relationship relationship : file.getRelationships(MeasurementRelationshipTypes.FIRST, Direction.OUTGOING)) {
 				Node measurement = relationship.getEndNode();
 				printMeasurement(measurement);
 				Iterator<Relationship> relationships = measurement.getRelationships(MeasurementRelationshipTypes.NEXT,
 						Direction.OUTGOING).iterator();
 				while (relationships.hasNext()) {
 					relationship = relationships.next();
 					measurement = relationship.getEndNode();
 					printMeasurement(measurement);
 					relationships = measurement.getRelationships(MeasurementRelationshipTypes.NEXT, Direction.OUTGOING)
 							.iterator();
 				}
 			}
 			tx.success();
 		} finally {
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length<1) args = new String[]{"amanzi/test.FMT","amanzi/0904_90.FMT","amanzi/0905_22.FMT","amanzi/0908_44.FMT"};
 		EmbeddedNeo neo = new EmbeddedNeo("var/neo");
 		try{
 			for(String filename:args){
 				TEMSLoader temsLoader = new TEMSLoader(neo,filename);
 				temsLoader.setLimit(100);
 				temsLoader.run();
 				temsLoader.printStats();	// stats for this load
 			}
 			printTimesStats();	// stats for all loads
 		} catch (IOException e) {
 			System.err.println("Error loading TEMS data: "+e);
 			e.printStackTrace(System.err);
 		}finally{
 			neo.shutdown();
 		}
 	}
 }
