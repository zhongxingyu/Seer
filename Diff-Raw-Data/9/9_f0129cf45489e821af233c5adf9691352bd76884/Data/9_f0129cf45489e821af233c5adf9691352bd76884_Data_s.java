 
 import java.util.ArrayList;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 class Data {
     private static final Logger LOG = Logger.getLogger(Data.class.getName());
 
     ArrayList<Date>      dates = new ArrayList<Date>();
     ArrayList<String>   labels = new ArrayList<String>();
     ArrayList<Integer>   items = new ArrayList<Integer>();
     ArrayList<String> comments = new ArrayList<String>();
 
     public void addEntry(Date d, String l, Integer i, String c) {
 	dates.add(d); labels.add(l); items.add(i); comments.add(c);
     }
 
 
     public void setDateFormat(String df) {
 	dateFormat = df;
     }
 
     protected String dateFormat = "yyyy-MM-dd";
 
     public void loadFile(String filename) throws 
 	java.io.IOException,
 	java.io.FileNotFoundException {
 	String strLine;
 	BufferedReader buf = new BufferedReader(new FileReader(filename));
 
 	int lineNum = 0;
 	Date d; String l; Integer i; String c;
 	String tmp[];
         SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 
 	while ((strLine = buf.readLine()) != null) {
 	    lineNum++;
 	    try {
 		// parse stuffs here
 
 		tmp = strLine.split("\\s+");
 		d = sdf.parse(tmp[0]);
 		l = tmp[1];
 		i = Integer.parseInt(tmp[2]);
 		c = tmp[3];
 	    } catch (java.text.ParseException e) {
 		LOG.warning("Unable to parse line " + lineNum);
 		continue;
 	    }
 	    dates.add(d);
 	    labels.add(l);
 	    items.add(i);
 	    comments.add(c);
 	}
 	buf.close();
     }
 
     public void saveFile(String filename) throws java.io.IOException {
 
 	PrintWriter out = new PrintWriter(new FileWriter(filename));
	
	for(int i = 1; i < items.size(); i++) {
 	    out.println(dates.get(i) + "\t" + labels.get(i) + "\t" +
 			items.get(i) + "\t" + comments.get(i));
 	}
 	out.close();

     }
 
     
 }
