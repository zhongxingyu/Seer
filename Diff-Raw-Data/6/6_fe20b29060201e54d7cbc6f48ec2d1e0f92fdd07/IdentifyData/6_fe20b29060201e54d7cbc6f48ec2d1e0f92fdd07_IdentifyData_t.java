 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class IdentifyData extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	public static final int MAX_COLUMNS = 10;
 	private int[] counters;
 	private String[] ids;
 	int sampleRate = 1;
 	int sampleSize = -1;
 	// TO SUPPORT NEW DATE FORMAT, ADD THE PATTERN STRING IN FOLLOWING ARRAY
 	public static final String []DATE_FORMAT_ARRAY = {"dd-MMM-yy", "yyyy.MM.dd G 'at' HH:mm:ss z", "EEE, MMM d, ''yy", "h:mm a", "hh 'o''clock' a, zzzz",
 		"K:mm a, z", "yyyyy.MMMMM.dd GGG hh:mm aaa", "EEE, d MMM yyyy HH:mm:ss Z", "dd.MM.yy",
 		"yyyy.MM.dd G 'at' hh:mm:ss z", "EEE d MMM yy", "yyyy-mm-dd", "yyyy/mm/dd", "yyyy-MM-dd HH:mm:ss,z", 
 		"EEE, dd MMM yyyy HH:mm:ss z", "yyyy-MM-dd'T'HH:mm:ssz"};  
 	private static final double PERCENTILE = 0.8;
 	Hashtable<String, Values> histogramData  =  new Hashtable<String, Values>();
 	int maxIntValue , minIntValue;
 	int initilizeflag=0;
 	private Date maxDateValue ;   
 	private Date minDateValue;  
 	private long colWidth ;
 	private int overallRange;
 	private double overallRangeDouble;
 	private long DateRange;
 	private double maxDoubleValue;
 	private double minDoubleValue;
 	private double colWidthDouble;
 	private String dateType;
 
 	enum jsonkeys {
 		Value,
 		Frequency,
 		IDs
 	};
 	
 	public static final int TOTAL_DATATYPES = 7;
 	enum dataType
 	{ 
 		Boolean,
 		Date,
 		Integer,
 		Double,
 		DayOfWeek,
 		String,
 		Empty_String
 	}
 
 	private static double mean(int[] m) {
 		double sum = 0;
 		for (int i = 0; i < m.length; i++) {
 			sum += m[i];
 		}
 		return sum / m.length;
 	}
 
 	private static void preferredLengthForNonStrings(int[] m, int[] stat) {
 		Arrays.sort(m);
 		int middle = m.length/2;
 		if (m.length%2 == 1) {
 			stat[0] = m[middle];
 		}
 		else {
 			stat[0] = (int) ((m[middle-1] + m[middle]) / 2.0);
 		}
 		stat[1] = m[(int) (PERCENTILE * m.length)];
 	}
 
 	private static int mode(int m[]) {  
 		int maxValue = m[0], maxCount = 0;
 
 		for (int i = 0; i < m.length; ++i) {
 			int count = 0;
 			for (int j = 0; j < m.length; ++j) {
 				if (m[j] == m[i]) ++count;
 			}
 			if (count > maxCount) {
 				maxCount = count;
 				maxValue = m[i];
 			}
 		}
 		return maxValue;
 	}
 
 	private int findMin(int[] m) {
 		Arrays.sort(m);
 		return m[0];
 	}
 
 	private int findMax(int[] m) {
 		Arrays.sort(m);
 		return m[m.length-1];
 	}
 
 	@SuppressWarnings("deprecation")
 	private void calFrequency(JSONObject jsonObject, int isValid, String datatype) throws JSONException, ParseException{  
 		String currentId = jsonObject.getString("id");
 		String str = null ;
 		if (1 == isValid) {
 			if (datatype == "DayOfWeek" || datatype == "Boolean") { 
 				str = jsonObject.getString("value").toLowerCase();
 				String firstChar = str.substring(0, 1).toUpperCase();
 				firstChar = firstChar + str.substring(1, str.length());
 				str = firstChar;
 
 				if (histogramData.containsKey(str)){
 					((Values)histogramData.get(str)).counter++;
 					((Values)histogramData.get(str)).IDs += ", "+ currentId;
 				}
 				else {
 					histogramData.put(str, new Values(currentId, 1));
 				}
 			}
 			if (datatype == "String") { 
 				str = jsonObject.getString("value").toLowerCase();
 				String firstChar = str.substring(0, 1).toUpperCase();
 				firstChar = firstChar + str.substring(1, str.length());
 				str = firstChar;
 
 				if (histogramData.containsKey(str)){
 					((Values)histogramData.get(str)).counter++;
 					((Values)histogramData.get(str)).IDs += ", "+ currentId;
 				}
 				else {
 					histogramData.put(str, new Values(currentId, 1));
 				}
 			}
 			if (datatype == "Integer" || datatype == "Double") {
 				int iValue;
 				double fValue;
 				int whichCol = 0 ;
 				if (datatype == "Integer" ) {
 					iValue = Integer.parseInt( jsonObject.getString("value"));
 					whichCol = (int) ((iValue-minIntValue) / colWidth);
 					str = minIntValue  + colWidth * whichCol  +"";
 				}
 				if (datatype == "Double") {
 					fValue = Double.parseDouble( jsonObject.getString("value"));
 					whichCol = (int) ((fValue-minDoubleValue) / colWidthDouble);
 					float f = (float) (minDoubleValue  + colWidthDouble * whichCol) ;
 					str = f +"";
 				}
 
 
 				if (histogramData.containsKey(str)){
 					((Values)histogramData.get(str)).counter++;
 					((Values)histogramData.get(str)).IDs += ", "+ currentId;
 				}
 				else {
 					histogramData.put(str, new Values(currentId, 1));
 				}
 
 			}
 			if (datatype == "Date" ) { 
 				str = jsonObject.getString("value");
 				Date dateValue = null ;
 				int isDateValid = 0, count = 0;
 				while(isDateValid  == 0 && count < DATE_FORMAT_ARRAY.length) {
 					try {
 						SimpleDateFormat tm = new SimpleDateFormat(DATE_FORMAT_ARRAY[count++]);
 						tm.setLenient(false);
 						dateValue = tm.parse(str);
 						isDateValid = 1;
 					}
 					catch(Exception e){}
 				}
 				int whichCol = (int) ((dateValue.getTime()-minDateValue.getTime())/colWidth); 
 				if(colWidth>31536000000l)
 				{
 					dateType = "Year";
 					str =  "" + (new Date((long) (minDateValue.getTime()) + colWidth*whichCol).getYear() + 1900);
 				}
 				else if(colWidth<=31536000000l && colWidth >2592000000l)
 				{
 					dateType = "Month";
 					str =  "" + new Date((long) (minDateValue.getTime()) + colWidth*whichCol).getMonth();		
 				}
 				else if(colWidth<=2592000000l && colWidth >=86400000l)
 				{
 					dateType = "Day";
 					str =  "" + new Date((long) (minDateValue.getTime()) + colWidth*whichCol).getDate() ;
 				}
 				else
 					str =  new Date((long) (minDateValue.getTime()) + colWidth*whichCol).toString();
 				if (histogramData.containsKey(str)){
 					((Values)histogramData.get(str)).counter++;
 					((Values)histogramData.get(str)).IDs += ", "+ currentId;
 				}
 				else {
 					histogramData.put(str, new Values(currentId, 1));
 				}
 			}
 		}
 		else if (2 == isValid) { // For Missing values
 
 			str = "MISSING";
 			if (histogramData.containsKey(str)){
 				((Values)histogramData.get(str)).counter++;
 				((Values)histogramData.get(str)).IDs += ", "+ currentId;
 			}
 			else {
 				histogramData.put(str, new Values(currentId, 1));
 			}
 		}
 		else {  //  For Garbage : isValid = 0
 			str = "INVALID";
 			if (histogramData.containsKey(str)){
 				((Values)histogramData.get(str)).counter++;
 				((Values)histogramData.get(str)).IDs += ", "+ currentId;
 			}
 			else {
 				histogramData.put(str, new Values(currentId, 1));
 			}
 		}
 
 	}
 
 	private JSONObject getJSONObjectbyID(JSONArray jArray, String strId) throws JSONException {
 
 		for (int i =0; i< jArray.length(); i++)
 		{
 			if (strId.compareToIgnoreCase(jArray.getJSONObject(i).getString("id").toString())== 0)
 				return jArray.getJSONObject(i);
 		}
 		return null;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private String printHistogram(final String datatype) {
 
 		String output = "[]" ;
 		Iterator<Entry<String, Values>>  it;
 		Map.Entry            entry, entryInvalid = null, entryMissing = null; 
 		Entry<String, Values> restStringEntry = null;
 
 		int ientryInvalid=-1, ientryMissing=-1;
 
 		Map<String, Values> map = new Hashtable<String, Values>(histogramData);
 
 		List<Entry<String,Values>> entryList = 
 				new ArrayList<Entry<String,Values>>(map.entrySet());
 
 		for (int i =0; i< entryList.size(); i++)
 		{
 			if (entryList.get(i).getKey() == "INVALID" )
 			{
 				ientryInvalid = i;
 			}
 			if (entryList.get(i).getKey() == "MISSING" )
 			{
 				ientryMissing = i;
 			}
 		}
 		if(ientryInvalid!=-1)
 		{
 			entryInvalid = entryList.get(ientryInvalid);
 		}
 		if(ientryMissing!=-1)
 		{
 			entryMissing= entryList.get(ientryMissing);
 		}
 		if(ientryInvalid!=-1) entryList.remove(entryInvalid);
 		if(ientryMissing!=-1) entryList.remove(entryMissing);
 
 
 		if (datatype == "DayOfWeek") {
 			ArrayList<Entry<String,Values>> weekentryList = 
 					new ArrayList<Entry<String,Values>>(entryList);
 			weekentryList.clear();
 			String []alldays= {"MONDAY", "TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY", "Sunday"};
 			for(int i=0; i< alldays.length; i++)
 			{
 				for (int j = 0; j< entryList.size(); j++)
 				{
 					Map.Entry<String, Values> e = entryList.get(j);
 					if (e.getKey().compareToIgnoreCase(alldays[i]) ==0)		
 						weekentryList.add( e);
 
 				}
 			}
 			entryList = new ArrayList<Entry<String,Values>>(weekentryList);
 		}
 
 		Collections.sort(entryList, new Comparator<Entry<String,Values>>() {
 			public int compare(Entry<String, Values> first, Entry<String, Values> second) {
 				if (datatype == "Integer" || datatype == "Date") {
 					int firstnum = Integer.parseInt(first.getKey());
 					int secnum = Integer.parseInt(second.getKey());
 					return  firstnum - secnum;
 				}
 				if (datatype == "Double" ) {
 					double firstnum = Double.parseDouble(first.getKey());
 					double secnum = Double.parseDouble(second.getKey());
 					if(firstnum > secnum) return 1;
 					else return -1;
 				}
 				if (datatype == "Boolean") {
 					if (first.getKey() == "True")
 						return -1;
 					else return 1;
 				}
 				if (datatype == "String") {
 					if (first.getValue().counter > second.getValue().counter )
 						return -1;
 					else return 1;
 				}
 				return 0;
 			}
 		});
 		int restEntries = 0;
 		String restIDs = "";
 		if (datatype == "String") {
 
 			for(int i=MAX_COLUMNS; i < entryList.size(); )
 			{
 				restEntries += entryList.get(i).getValue().counter;
 				restIDs += entryList.get(i).getValue().IDs + ",";
 				entryList.remove(i);
 			}
 			restIDs = removeTrailingCommas(restIDs);
 
 		}
 		if (restEntries !=0)
 		{
 			Hashtable< String, Values>restHash = new Hashtable<String, Values>();
 			restHash.put("Remaining", new Values(restIDs, restEntries));
 			List<Entry<String,Values>>tempList = new ArrayList<Entry<String,Values>>(restHash.entrySet());
 			restStringEntry = tempList.get(0);
 			entryList.add(restStringEntry);
 		}
 
 		if(ientryMissing!=-1)
 			entryList.add(entryMissing);
 		if(ientryInvalid!=-1)
 			entryList.add(entryInvalid);
 
 		it = entryList.iterator();
 		JSONArray outputJsonArr = new JSONArray();
 		try {
 			outputJsonArr = new JSONArray();
 			while (it.hasNext()) {
 				entry = it.next();
 
 				JSONObject jsonObject = new JSONObject();
 				jsonObject.put(jsonkeys.Value.name(), entry.getKey().toString());
 				String [] freq_and_Ids = entry.getValue().toString().split(":");
 				jsonObject.put(jsonkeys.Frequency.name(), freq_and_Ids.length>0? freq_and_Ids[0]:"");
 				jsonObject.put(jsonkeys.IDs.name(), freq_and_Ids.length>1? freq_and_Ids[1]:"");
 				outputJsonArr.put(jsonObject);
 			}
 			output = outputJsonArr.toString(); 
 		} 
 		catch(Exception e) {
 			return "[]";
 		}
 		return output;
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
 
 		try{
 			initilizeData();
 			PrintWriter out = response.getWriter(); 
 			//@SuppressWarnings("unchecked")
 			//Map<String, String> requestMap = request.getParameterMap();
 			String jsonString="", filePath="" ;
			//System.out.println(request.toString());
			/*System.out.println("Content length:" + request.getContentLength());
			System.out.println("Content query:" + request.getQueryString());
			System.out.println("Content type:" + request.getContentType());*/
 			String reqFileJSON = request.getParameter("file");
 			String reqStringJSON  = request.getParameter("json");
 			if(reqStringJSON != null) {
 				jsonString = request.getParameter("json");
 			}
 			else if (reqFileJSON !=null) {
 				filePath = request.getParameter("file");
 
 				FileReader rd = new FileReader(filePath);
 				BufferedReader bf = new BufferedReader(rd);
 				StringBuilder sb = new StringBuilder();
 
 				while((jsonString = bf.readLine())!=null) {
 					sb.append(jsonString);
 				}
 
 				jsonString = sb.toString();
 				bf.close();
 				rd.close();
 			}
 			else {
 				out.println("Usage:- \n http://localhost:8080/myWS/IdentifyData?json={JSON_STRING}[&sampleSize={NUMBER}&sampleRate={NUMBER}]\n OR \n http://localhost:8080/myWS/IdentifyData?file={FULL_JSON_File_PATH}[&sampleSize={NUMBER}&sampleRate={NUMBER}]");
 				return;
 			}
 
 			JSONArray jArray = null;
 			try{
 				jArray = new JSONArray(jsonString);
 			}
 			catch(Exception e) {
 				out.println("Malformed JSON");
 				return;
 			}
 
 			//sampling start
 			int maxSampleSize = 0;
 			int sampleStart = 0;
 			if(request.getParameter("sampleRate") != null && request.getParameter("sampleSize") != null) {
 				sampleRate = Integer.parseInt(request.getParameter("sampleRate"));
 				sampleSize = Integer.parseInt(request.getParameter("sampleSize"));
 			}
 			else{
 				sampleSize = jArray.length()/ sampleRate;
 			}
 
 			maxSampleSize = jArray.length()/ sampleRate;
 
 			if(sampleRate > jArray.length() || sampleSize > jArray.length()) {
 				out.println("SampleSize or SampleRate is greater than the JSON Array Size");
 				return;
 			}
 
 			if (sampleSize > maxSampleSize)
 				sampleSize = maxSampleSize;
 
 			counters = new int[TOTAL_DATATYPES];
 			ids =  new String[TOTAL_DATATYPES] ;
 
 			for (int j = sampleStart; j<sampleRate; j++) {
 				sampleStart = j * maxSampleSize;
 
 				for( int i = sampleStart; i< sampleStart+sampleSize;i++){
 					identifyDataType(jArray.getJSONObject(i));
 				}
 			}
 
 			for (int i =0; i< TOTAL_DATATYPES; i++)  
 			{
 				ids[i] = removeTrailingCommas(ids[i]);
 			}
 
 			// Create Output Statistic
 			int maxCounterValue=0, TotalCounterValues=0, maxCounterIndex=-1, invalidIDcount = 0, missingCount=counters[TOTAL_DATATYPES-1];
 			String inValidIDs = "";
 			for(int i=0; i<TOTAL_DATATYPES; i++)	
 			{
 				if(counters[i]>maxCounterValue) 
 				{
 					maxCounterIndex = i;
 					maxCounterValue = counters[i];
 				}
 				TotalCounterValues += counters[i];
 			}
 			invalidIDcount = TotalCounterValues - maxCounterValue - missingCount;
 
 			// Listing invalid values except for missing values
 			for(int i=0; i<TOTAL_DATATYPES-1; i++)	{
 				if(i != maxCounterIndex && ids[i] !=null) {
 					inValidIDs += ids[i] + ", ";
 				}							
 			}
 			inValidIDs = removeTrailingCommas(inValidIDs);
 
 			JSONObject outputJSON = new JSONObject();
 
 			if(maxCounterValue>0) {
 
 				outputJSON.put("Category", dataType.values()[maxCounterIndex]);
 				outputJSON.put("Valid_ID_Count", maxCounterValue+"");
 				outputJSON.put("yLabel", "Count");
 				outputJSON.put("Invalid_ID_Count", invalidIDcount+"");
 				outputJSON.put("Total_ID_Count", jArray.length()+"");
 				outputJSON.put("Total_ID_Sampled", TotalCounterValues+"");
 				outputJSON.put("Sampling_Rate", sampleRate+"");
 				outputJSON.put("Sample_Size", sampleSize+"");
 				if (counters[maxCounterIndex]> 0)  			// counters [maxCounterIndex] >0
 					outputJSON.put("Valid_ID", ids[maxCounterIndex]);
 				else 
 					outputJSON.put("Valid_ID", "");
 				if (inValidIDs.length() > 0){
 					outputJSON.put("Invalid_ID", inValidIDs);
 				}
 				else {
 					outputJSON.put("Invalid_ID", "");
 				}
 				if (ids[TOTAL_DATATYPES-1] != null)
 					outputJSON.put("Missing_Value_ID", ids[TOTAL_DATATYPES-1]);
 				else 
 					outputJSON.put("Missing_Value_ID", "");
 			}
 			else {
 				outputJSON.put("No Data!!", "No Data !!");
 			}
 
 			// Calculate frequency of data which is valid only		
 			String validIDs = ids[maxCounterIndex];
 			String [] id_list = validIDs.split(", ");
 
 			if (dataType.values()[maxCounterIndex].toString() == "Integer" )  
 			{
 				overallRange = ( maxIntValue - minIntValue);  
 				colWidth =  (long) Math.ceil((float)overallRange /  Math.min( MAX_COLUMNS, id_list.length));
 				if (colWidth == 0)
 					colWidth = 1;
 				outputJSON.put("histogram_Colwidth", colWidth+"");
 
 			}
 			if (dataType.values()[maxCounterIndex].toString() == "Double" )
 			{
 				overallRangeDouble = ( maxDoubleValue - minDoubleValue);  
 				colWidthDouble = overallRangeDouble /  Math.min( MAX_COLUMNS, id_list.length);
 				if (colWidthDouble == 0)
 					colWidthDouble = 1;
 				outputJSON.put("histogram_Colwidth", colWidthDouble+"");
 
 			}
 			if (dataType.values()[maxCounterIndex].toString() == "Date" )  
 			{
 				DateRange = ( maxDateValue.getTime() - minDateValue.getTime());  
 				colWidth = (long) (DateRange /  Math.min( MAX_COLUMNS/2, id_list.length));
 
 				if(colWidth<2592000000l && colWidth >86400000l)
 				{
 					colWidth = colWidth - colWidth % 86400000l;
 				}
 
 				if (colWidth==0)
 					colWidth = 1;
 			}
 
 			//	Calculate the frequency of Valid IDs
 			for( int i = 0; i< id_list.length; i++) {
 				if (id_list[i]!= "" )
 					calFrequency(getJSONObjectbyID(jArray,id_list[i]), 1, dataType.values()[maxCounterIndex].toString());
 			}
 
 
 			// Input length
 			if (dataType.values()[maxCounterIndex].toString().compareTo("Integer") == 0 
 					|| dataType.values()[maxCounterIndex].toString().compareTo("Double") == 0
 					|| dataType.values()[maxCounterIndex].toString().compareTo("Boolean") == 0 
 					|| dataType.values()[maxCounterIndex].toString().compareTo("Date") == 0		
 					|| dataType.values()[maxCounterIndex].toString().compareTo("DayOfWeek") == 0 )
 			{
 
 				int m[] = null;
 				m = fillValueLength(jArray);
 
 				int[] stat= {-1,-1};
 				preferredLengthForNonStrings(m, stat);
 				outputJSON.put("mean", mean(m));
 				outputJSON.put("median", stat[0]);
 				outputJSON.put("Preferred_Length", stat[1]);
 				outputJSON.put("mode", mode(m));
 				outputJSON.put("Max_Token_Length", findMax(m));
 				outputJSON.put("Min_Token_Length", findMin(m));
 			}
 			else if (dataType.values()[maxCounterIndex].toString().compareTo("String") == 0 ||dataType.values()[maxCounterIndex].toString().compareTo("Empty_String") == 0 )
 			{
 				int[] stat= {-1,-1};
 				preferredLengthForStrings(jArray, stat);
 				outputJSON.put("Preferred_Length", stat[0]);
 				outputJSON.put("Max_Token_Length", stat[1]);
 			}
 
 			// To fill empty spaces in histogram
 			if (dataType.values()[maxCounterIndex].toString() == "Integer")
 			{
 				for (int i = 0; (colWidth * i + minIntValue)< maxIntValue ; i++){
 					String str = minIntValue  + colWidth * i  +"";
 					if (histogramData.containsKey(str) == false)
 					{
 						histogramData.put(str, new Values("", 0));
 					}
 				}
 			}
 
 			//	Calculate the frequency of invalid IDs
 			id_list = inValidIDs.split(", ");
 			for( int i = 0; i< id_list.length ;i++) {
 				if (id_list[i]!= "" )
 					calFrequency(getJSONObjectbyID(jArray,id_list[i]), 0, dataType.values()[maxCounterIndex].toString()); 
 			}
 
 			//Calculate the frequency of missing IDs
 			if (ids[TOTAL_DATATYPES-1] != null) {
 				String missingIDs = ids[TOTAL_DATATYPES-1];
 				id_list= missingIDs.split(", ");
 
 				for( int i = 0; i< id_list.length;i++) {
 					if (id_list[i]!= "" )
 						calFrequency(getJSONObjectbyID(jArray,id_list[i]), 2, dataType.values()[maxCounterIndex].toString());
 				}
 			}	
 			outputJSON.put("histogram", printHistogram(dataType.values()[maxCounterIndex].toString()));
 
 			if (dataType.values()[maxCounterIndex].toString().compareTo("Date") == 0)
 				outputJSON.put("xLabel", dateType);
 			else if (dataType.values()[maxCounterIndex].toString().compareTo("DayOfWeek") == 0)
 				outputJSON.put("xLabel", "Weekdays");
 			else
 				outputJSON.put("xLabel", dataType.values()[maxCounterIndex].toString());
 			out.println(outputJSON.toString(4));
 
 			histogramData.clear();
 
 		}catch (JSONException e) {
 			e.printStackTrace();
 
 		}
 		catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void preferredLengthForStrings(JSONArray jArray, int [] stat) {
 		try {
 			int tokenLengths [] = new int [jArray.length()];
 			for (int i =0; i<jArray.length(); i++) {
 				JSONObject j = jArray.getJSONObject(i);
 				String value = j.getString("value");
 				String tokens [] = value.split(" ");
 				int maxTokenLength = 0;
 				for (int i1 =0;i1< tokens.length; i1++) {
 					if (maxTokenLength < tokens[i1].length())
 					{
 						maxTokenLength = tokens[i1].length(); 
 					}
 				}
 				tokenLengths[i] = maxTokenLength;
 			}
 
 			Arrays.sort( tokenLengths);
 			if (tokenLengths.length > 1) {
 				stat[0] = tokenLengths[(int) (PERCENTILE * tokenLengths.length)];
 				stat[1] = tokenLengths[tokenLengths.length-1];
 			}
 		} catch (JSONException e) {
 			System.out.println(e);
 		}
 	}
 
 	private int[] fillValueLength(JSONArray jArray) {
 
 		int m[] = new int [jArray.length()];
 		try {
 			for (int i =0; i<jArray.length(); i++) {
 				JSONObject j = jArray.getJSONObject(i);
 				m[i] = j.getString("value").length();
 			}
 		} catch (JSONException e) {
 			System.out.println(e);
 			return null;
 		}
 		return m;
 	}
 
 	private void initilizeData() {
 		initilizeflag = 0;
 		histogramData.clear();
 	}
 
 	private String removeTrailingCommas(String inputString) {
 
 		if (inputString == null || inputString == "" || inputString.length() <0)
 			return inputString;
 
 		int lastComma = inputString.lastIndexOf(",");
 		inputString = inputString.substring(0, lastComma);
 		return inputString;
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 	private void identifyDataType(JSONObject jObj) throws JSONException { 
 
 		// Place checks higher in if-else statement to give higher priority to that type and Modify the enum dataType sequence
 
 		String currentId = jObj.getString("id");
 		String str = jObj.getString("value");
 		try{
 			if(Boolean.parseBoolean(str) || str.compareToIgnoreCase("false") == 0) {
 
 				counters[0]++;
 				if(ids[0] !=null)
 					ids[0] += currentId + ", ";
 				else 
 					ids[0] = currentId + ", ";
 				return;
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		// Date Parsing with DATE_FORMAT_ARRAY
 		try{
 			Date dateValue = null ;
 			int isDateValid = 0, count = 0;
 			while(isDateValid  == 0 && count < DATE_FORMAT_ARRAY.length) {
 				try {
 					SimpleDateFormat tm = new SimpleDateFormat(DATE_FORMAT_ARRAY[count++]);
 					tm.setLenient(false);
 					dateValue = tm.parse(str);
 					isDateValid = 1;
 				}
 				catch(Exception e){}
 			}
			if (isDateValid==0) throw new Exception();
 			if (initilizeflag ==0) 
 			{
 				maxDateValue = minDateValue = dateValue; 
 				initilizeflag =1;
 			}
 			if (dateValue.after(maxDateValue))
 				maxDateValue = dateValue;
 			if (dateValue.before(minDateValue))
 				minDateValue = dateValue;
 
 			counters[1]++;
 			if(ids[1] !=null)
 				ids[1] += currentId + ", ";
 			else 
 				ids[1] = currentId + ", ";
 			return;
 		}
 		catch(Exception e){}
 
 		// Integer Parsing
 		try{
 			int intValue = Integer.parseInt(str);
 
 			if (initilizeflag==0) {	maxIntValue = minIntValue = intValue;			initilizeflag =1;} 
 			if (intValue > maxIntValue)
 				maxIntValue = intValue;
 			if (intValue < minIntValue)
 				minIntValue = intValue;
 
 			counters[2]++;
 			if(ids[2] != null)
 				ids[2] += currentId + ", ";
 			else 
 				ids[2] = currentId + ", ";
 			return;
 		}
 		catch(Exception e){} 
 
 		// Double Parsing
 		try{
 
 			double doubleValue = Double.parseDouble(str);
 			if (initilizeflag==0) {	maxDoubleValue = minDoubleValue = doubleValue;			initilizeflag =1;}
 			if (doubleValue > maxDoubleValue)
 				maxDoubleValue= doubleValue;
 			if (doubleValue < minDoubleValue)
 				minDoubleValue = doubleValue;
 
 			counters[3]++;
 			if(ids[3] !=null)
 				ids[3] += currentId + ", ";
 			else 
 				ids[3] = currentId + ", ";
 			return;
 		}
 		catch(Exception e){}
 
 
 
 		// Parsing Days of the week
 		try{
 			List<String> day = new  ArrayList<String>();
 			day.add("SUNDAY");day.add("MONDAY");day.add("TUESDAY");day.add("WEDNESDAY");day.add("THURSDAY");day.add("FRIDAY");day.add("SATURDAY");
 			if(day.contains(str.toUpperCase())) {
 				counters[4]++;
 				if(ids[4] !=null)
 					ids[4] += currentId + ", ";
 				else 
 					ids[4] = currentId + ", ";
 				return;
 			}
 		}
 		catch(Exception e){}
 
 		// Everything else :- a String or an Empty String
 		try{
 			if (str.length()!=0) {
 				counters[5]++;
 				if(ids[5] !=null)
 					ids[5] += currentId + ", ";
 				else 
 					ids[5] = currentId + ", ";
 			}
 			else {
 				counters[6]++;
 				if(ids[6] !=null)
 					ids[6] += currentId + ", ";
 				else 
 					ids[6] = currentId + ", ";
 			}
 			return;
 		}
 		catch(Exception e){}
 		return ;
 	}
 }
