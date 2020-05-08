 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientgroup22;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 //chart imports
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartFrame;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.CombinedDomainCategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 import org.json.JSONException;
 
 /**
  *
  * @author matthew
  */
 public class ClientGroup22 {
 
     /**
      * @param args the command line arguments
      */
     static final String host = "197.85.191.195"; //nightmare@cs.uct.ac.za
 
     public static void main(String[] args) throws FileNotFoundException, IOException {
 
         System.out.println("Welcome to group 22's client");
         System.out.println("Authors Matthew Wood, Wesley Robinson, and Ezrom Chijoriga");
         System.out.println("==========================================================");
 
         Socket s = Connect();
 
         System.out.println("Server connection established on: " + host);
 
         String input;
         Scanner sc = new Scanner(System.in);
 
         while (true) {
             System.out.println();
             System.out.println("1: Ping server");
             System.out.println("2: Upload data");
             System.out.println("3: Query specific data");
             System.out.println("4: Get data statistics");
             System.out.println("5: Graph data statistics");
             System.out.println("6: Get data summary");
             System.out.println("7: Query the logs");
             System.out.println("8: Exit");
            System.out.print("Please select a choice:");
 
             input = sc.nextLine();
 
             System.out.println();
 
             if (isInteger(input) && Integer.parseInt(input) != 8) {
                 switch (Integer.parseInt(input)) {
                     case 1:
                         Ping(s);
                         break;
                     case 2:
                         UploadData(s);
                         break;
                     case 3:
                         DisplayQueryResults(QueryData(s));
                         break;
                     case 4:
                         System.out.println("Please enter which types of reading you want to query (\"light\", \"temperature\", \"humidity\"):");
                         String type = sc.nextLine();
                         System.out.println("Please enter which types of statistic you want to query(\"count\", \"average\", \"min\", \"max\", \"stddev\", \"mode\", \"median\"):");
                         String agg = sc.nextLine();
                         System.out.println(agg + " of " + type + ": " + AggregatedData(s, agg, type).get("result"));
                         break;
                     case 5:
                         GraphStatData(s);
                         break;
                     case 6:
                         GetDataSumm(s);
                         break;
                     case 7:
                         QueryLogs(s);
                         break;
                 }
             } else if (isInteger(input) && Integer.parseInt(input) == 8) {
                 break;
             } else {
                 System.out.println("Not a valid input!");
             }
         }
 
         s.close();
         System.out.println("Server connection closed");
 
     }
 
     public ClientGroup22() {
     }
 
     public static void QueryLogs(Socket s) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONArray group_ids = new JSONArray();
 
         /*Group ids*/
         System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
         String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
 //        String[] group_idArr = {"22"};
         for (String i : group_idArr) {
             if (isInteger(i)) {
                 group_ids.put(Integer.parseInt(i));
             } else {
                 System.out.println(i + " is not an integer, ignoring.");
             }
         }
         params.put("group_ids", group_ids);
 
         /*Time from*/
         System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_from", new Scanner(System.in).nextLine());
         params.put("time_from", "2013-01-01 01:01:01");
 
         /*Time to*/
         System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         params.put("time_to", "2014-01-01 00:00:00");
 
         /*Limit number of logs*/
         System.out.println("Please enter the limit of the number of logs you want returned:");
 //        params.put("limit", new Scanner(System.in).nextLine());
         params.put("limit", 30);
 
         /*Construct query JSONObject*/
         query.put("method", "query_logs");
         query.put("group_id", 22);
         query.put("params", params);
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         JSONArray lines = reply.getJSONObject("result").getJSONArray("lines");
         JSONObject line = new JSONObject();
         for (int i = 0; i < lines.length(); i++) {
             line = lines.optJSONObject(i);
             System.out.println("Action " + line.get("action") + " run by group " + line.get("group_id") + " at time " + line.get("time"));
         }
     }
 
     public static void Ping(Socket s) {
         JSONObject query = new JSONObject();
         query.put("method", "ping");
         query.put("group_id", "22");
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         System.out.println("Reply from Server:" + reply.get("result") + " Time(ms):" + reply.getInt("elapsed"));
     }
 
     public static void UploadData(Socket s) throws FileNotFoundException, IOException {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         ArrayList<String> dataArr = new ArrayList<String>();
 
         System.out.println("How would you like to enter the readings?");
         System.out.println("1: From a file");
         System.out.println("2: Enter readings");
         Scanner sc = new Scanner(System.in);
 
         char choice = sc.next().toUpperCase().charAt(0);
 
         switch (choice) {
             case '1':
                 dataArr = ReadFromFile();
                 break;
 
             case '2':
                System.out.println("Please enter the readings you would like to submit, separated by a $ (eg: Temp 34.93 1365350128000 $ Humidity 43 1365350126542)");
                String[] readingsArr = ((new Scanner(System.in)).nextLine()).split("$");    //WHY THE FUCK IS IT NOT SPLITTING ON THE $?????????????????????
                 String[] temp = new String[3];
                 //        String[] group_idArr = {"101"};
                 for (String i : readingsArr) {
 //                    System.out.println(i);
                     if (isReading(i)) {
                         dataArr.add(i);
                     } else {
                         System.out.println(i + " is not a valid reading, ignoring.");
                     }
 
                 }
                 break;
 
             default:
                 System.out.println("ERROR: Invalid selection.");
                 return;
         }
 
 
         JSONObject reading;
         JSONArray readings = new JSONArray();
         String type = "";
         String value = "";
         long time = 0;
         for (int i = 0; i < dataArr.size(); i++) {
             System.out.println(i);
             reading = new JSONObject();
 
             type = dataArr.get(i).split(" ")[0];
             value = dataArr.get(i).split(" ")[1];
             time = Long.parseLong(dataArr.get(i).split(" ")[2]);
 
             if (type.equals("Temp")) {
                 type = "temperature";
             } else if (type.equals("Light")) {
                 type = "light";
             } else if (type.equals("Humidity")) {
                 type = "humidity";
             }
 
             if (type.equals("light") || type.equals("temperature") || type.equals("humidity")) {
                 reading.put("type", type);
                 reading.put("value", value);
                 reading.put("time", time);
 
                 readings.put(reading);
             }
         }
 
         params.put("readings", readings);
 
         query.put("group_id", "101");
         query.put("params", params);
         query.put("method", "new_readings");
 
 //        System.out.println(finalSend.toString());
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         try {
             System.out.println("Reply from Server:\nERROR: " + reply.get("error"));
         } catch (JSONException e) {
             System.out.println("Reply from Server:\nElapsed: " + reply.get("elapsed") + "\nResult: " + reply.get("result"));
         }
     }
 
     public static JSONObject QueryData(Socket s) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONObject returned = new JSONObject();
 //        JSONObject time_from = new JSONObject();  //Probably don't need...
 //        JSONObject time_to = new JSONObject();
         JSONArray group_ids = new JSONArray();
         JSONArray types = new JSONArray();
 
         /*Group ids*/
         System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
         String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
 //        String[] group_idArr = {"101"};
         for (String i : group_idArr) {
             if (isInteger(i)) {
                 group_ids.put(Integer.parseInt(i));
             } else {
                 System.out.println(i + " is not an integer, ignoring.");
             }
         }
         params.put("group_ids", group_ids);
 
         /*Time from*/
         System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
         params.put("time_from", new Scanner(System.in).nextLine());
 //        params.put("time_from", "2012-01-01 01:01:01");
 
         /*Time to*/
         System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
         params.put("time_to", new Scanner(System.in).nextLine());
 //        params.put("time_to", "2013-04-07 19:42:09.0");
 
         /*Types*/
         System.out.println("Please enter which types of readings you want to query, separated by a space (\"light\", \"temperature\", \"humidity\"):");
         String[] typesArr = ((new Scanner(System.in)).nextLine()).split(" ");
 //        String[] typesArr = {"light", "temperature"};
         for (String i : typesArr) {
             types.put(i);
         }
         params.put("types", types);
 
         query.put("method", "query_readings");
         query.put("group_id", 22);
         query.put("params", params);
 
         JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
 
         return reply;
     }
 
     public static JSONObject AggregatedData(Socket s, String aggregation, String readingType) {
         JSONObject query = new JSONObject();
         JSONObject params = new JSONObject();
         JSONObject returned = new JSONObject();
 
         /*Group id if given*/
         //System.out.println("Please enter in the group to query by their group ID. Enter 0 for all data. (Enter to confirm):");
         //Scanner scan = new Scanner(System.in);
         //groupNo = scan.nextLine();
 //        if (isInteger(groupNo)) {
 //            if (Integer.parseInt(groupNo) == 0) {
 //                //do nothing
 //            } else {
 //                params.put("group_id", Integer.parseInt(groupNo));
         //params.put("group_id", Integer.parseInt(groupNo)); //hardcoded version
 //            }
 //        } else {
 //            System.out.println("Not an integer, assuming 0");
 //        }
 
         //test
         //params.put("aggregation", "mean");
 //        System.out.println("mean put");
         //params.put("type", "temperature");
 //        System.out.println("temp put");
 
         /*Aggregation*/
         //System.out.println("Please enter which types of statistic you want to query (\"light\", \"temperature\", \"humidity\"):");
         //readingType = new Scanner(System.in).nextLine();
         params.put("type", readingType);
 
         /*Types*/
         //System.out.println("Please enter which types of reading you want to query(\"count\", \"average\", \"min\", \"max\", \"stddev\", \"mode\", \"median\"):");
         //aggrigator = new Scanner(System.in).nextLine();
         params.put("aggregation", aggregation);
 
         /*Time from*/
         //System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_from", new Scanner(System.in).nextLine());
         //params.put("time_from", "2013-01-01 01:01:01");
 //
         /*Time to*/
         //System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
 //        params.put("time_to", new Scanner(System.in).nextLine());
         //params.put("time_to", "2013-04-10 23:55:01");
 
         query.put("method", "aggregate");
         query.put("group_id", 22);
         query.put("params", params);
 
         returned = new JSONObject(GetReplyFromServer(query, s));
 
         return returned;
     }
 
     public static void GetDataSumm(Socket s) {
         JSONObject query = new JSONObject();
         query.put("method", "data_summary");
         query.put("group_id", "22");
 
         JSONObject returned = new JSONObject(GetReplyFromServer(query, s));
 
 
         JSONArray dataArr = returned.getJSONArray("result"); //array of JSONobjects
 
         for (int i = 0; i < dataArr.length(); i++) {
             if (i == 0) {
                 System.out.println("============");
             }
             JSONObject temp = new JSONObject();
             temp = dataArr.getJSONObject(i);
             System.out.println("Type: " + temp.get("type") + "\nType ID: " + temp.get("type_id") + "\nMean: " + temp.get("mean")
                     + "\nMin: " + temp.get("min") + "\nMax: " + temp.get("max") + "\nStandard dev: " + temp.get("stddev") + "\n");
             System.out.println("============");
 
         }
     }
 
     public static ArrayList<String> ReadFromFile() throws FileNotFoundException {    //Works! Don't change!
         Scanner sc1 = new Scanner(System.in);
         System.out.println("Please enter the name of the text file containing new readings (eg MoteDump.txt)");
         String filename = sc1.nextLine();
 //        sc1.close();
         ArrayList<String> temp = new ArrayList<String>();
         File file = new File(filename);
         try {
             Scanner sc2 = new Scanner(file);
 
             while (sc2.hasNext()) {
                 String reading = "";
                 reading += sc2.next() + " ";
                 sc2.next();
                 reading += sc2.next() + " ";
                 reading += sc2.next();
                 if (isReading(reading)) {
                     temp.add(reading);
                 } else {
                     System.out.println(reading + " is not a valid reading, ignoring...");
                 }
             }
 //            sc2.close();
         } catch (FileNotFoundException e) {
             System.out.println("File not found. Please try again.");
         }
         return temp;
     }
 
     public static Socket Connect() {
         Socket sock = new Socket();
         try {
             sock = new Socket(host, 3000);
         } catch (IOException ex) {
             System.out.println(ex);
         }
         return sock;
     }
 
     public static String GetReplyFromServer(JSONObject query, Socket s) {
         String reply = "";
         try {
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 
             System.out.println("\nQuerying server...\n");
             out.write(query.toString());
             out.println();
 
             reply = in.readLine();
         } catch (IOException e) {
             System.out.println(e);
         }
         return reply;
     }
 
     private static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
 
     private static boolean isDouble(String s) {
         try {
             Double.parseDouble(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
 
     private static boolean isLong(String s) {
         try {
             Long.parseLong(s);
         } catch (NumberFormatException e) {
             return false;
         }
         // only got here if we didn't return false
         return true;
     }
 
     public static boolean isTimeStamp(String inputString) {
         SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
         try {
             format.parse(inputString);
         } catch (ParseException e) {
             return false;
         }
         return true;
     }
 
     private static boolean isReading(String i) {
         System.out.println("Checking reading: " + i);
         String[] parts = i.split(" ");
         if (parts.length == 3) {
             if (parts[0].equals("Temp") || parts[0].equals("temperature") || parts[0].equals("Light") || parts[0].equals("light") || parts[0].equals("humidity") || parts[0].equals("Humidity")) {
                 if (isInteger(parts[1]) || isDouble(parts[1])) {
                     if (isTimeStamp(parts[2]) || isLong(parts[2])) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     public static void DisplayQueryResults(JSONObject j) {  //TODO format the information stored in the JSONObject returned by the server
         //System.out.println(j.toString()); //{"time":"2013-04-07 19:42:09.0","group_id":22,"value":28,"type":"Light"}
         JSONArray results = (JSONArray) j.get("result");
 
         System.out.println(results.toString());
 
         JSONArray light = new JSONArray();
         JSONArray temperature = new JSONArray();
         JSONArray humidity = new JSONArray();
 
         JSONObject temp = new JSONObject();
 
         for (int i = 0; i < results.length(); i++) {
             temp = results.getJSONObject(i);
 // System.out.println(temp.toString());
 // System.out.println(temp.get("type"));
             if (temp.get("type").equals("light")) {
                 light.put(temp);
             } else if (temp.get("type").equals("temperature")) {
                 temperature.put(temp);
             } else if (temp.get("type").equals("humidity")) {
                 humidity.put(temp);
             } else {
             }
         }
 
         System.out.println(light.toString());
         System.out.println(temperature.toString());
 
         /*Creating the light query graph*/
 
         int lightValue;
 
         XYSeriesCollection lightdataset = new XYSeriesCollection();
         XYSeries lightdata = new XYSeries("Data");
 
         for (int i = 0; i < light.length(); i++) {
             lightValue = (int) light.getJSONObject(i).get("value");
             //time = (String)light.getJSONObject(i).get("time");
             lightdata.add((i + 1), lightValue);
         }
 
         lightdataset.addSeries(lightdata);
 
         JFreeChart lightchart = ChartFactory.createScatterPlot(
                 "Query results", // chart title
                 "Time", // x axis label
                 "Value", // y axis label
                 lightdataset, // data
                 PlotOrientation.VERTICAL,
                 true, // include legend
                 true, // tooltips
                 false // urls
                 );
         XYPlot lightplot = (XYPlot) lightchart.getPlot();
         XYLineAndShapeRenderer lightrenderer = new XYLineAndShapeRenderer();
         lightrenderer.setSeriesLinesVisible(0, true);
         lightplot.setRenderer(lightrenderer);
 
         ChartPanel lightchartPanel = new ChartPanel(lightchart);
         lightchartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
         ApplicationFrame lightframe = new ApplicationFrame("Queried Data");
         lightframe.setContentPane(lightchartPanel);
         lightframe.pack();
         lightframe.setVisible(true);
 
         /*Creating the temperature query graph*/
 
         double temperatureValue;
 
         XYSeriesCollection temperaturedataset = new XYSeriesCollection();
         XYSeries temperaturedata = new XYSeries("Data");
 
         for (int i = 0; i < temperature.length(); i++) {
             temperatureValue = (double) temperature.getJSONObject(i).get("value");
             //time = (String)temperature.getJSONObject(i).get("time");
             temperaturedata.add((i + 1), temperatureValue);
 
         }
 
         temperaturedataset.addSeries(temperaturedata);
 
         JFreeChart temperaturechart = ChartFactory.createScatterPlot(
                 "Query results", // chart title
                 "Time", // x axis label
                 "Frequency", // y axis label
                 temperaturedataset, // data
                 PlotOrientation.VERTICAL,
                 true, // include legend
                 true, // tooltips
                 false // urls
                 );
         XYPlot temperatureplot = (XYPlot) temperaturechart.getPlot();
         XYLineAndShapeRenderer temperaturerenderer = new XYLineAndShapeRenderer();
         temperaturerenderer.setSeriesLinesVisible(0, true);
         temperatureplot.setRenderer(temperaturerenderer);
 
         ChartPanel temperaturechartPanel = new ChartPanel(temperaturechart);
         temperaturechartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
         ApplicationFrame temperatureframe = new ApplicationFrame("Queried Data");
         temperatureframe.setContentPane(temperaturechartPanel);
         temperatureframe.pack();
         temperatureframe.setVisible(true);
 
     }
 
     public static void GraphStatData(Socket s) {
 
         /*Chart display*/
         //System.out.println(AggregatedData(s, "mean", "light").toString());
         double lightMean = (double) AggregatedData(s, "mean", "light").get("result");
         int lightMin = (int) AggregatedData(s, "min", "light").get("result");
         int lightMax = (int) AggregatedData(s, "max", "light").get("result");
         double lightStddev = (double) AggregatedData(s, "stddev", "light").get("result");
 
         double temperatureMean = (double) AggregatedData(s, "mean", "temperature").get("result");
         int temperatureMin = (int) AggregatedData(s, "min", "temperature").get("result");
         double temperatureMax = (double) AggregatedData(s, "max", "temperature").get("result");
         double temperatureStddev = (double) AggregatedData(s, "stddev", "temperature").get("result");
 
         //DisplayQueryResults(QueryData(s));
 
         /*Light Chart*/
         DefaultCategoryDataset lightds = new DefaultCategoryDataset();
         lightds.addValue(lightMean, "Mean", "");
         lightds.addValue(lightMin, "Min", "");
         lightds.addValue(lightMax, "Max", "");
         lightds.addValue(lightStddev, "Standard Deviation", "");
 
         JFreeChart lightbc = ChartFactory.createBarChart("Light Statistics", "Key", "Value", lightds, PlotOrientation.VERTICAL, true, false, false);
 
         CategoryPlot lightmainPlot = lightbc.getCategoryPlot();
 
         NumberAxis lightmainAxis = (NumberAxis) lightmainPlot.getRangeAxis();;
         lightmainAxis.setLowerBound(0);
         lightmainAxis.setUpperBound(500);
 
         ChartFrame lightcf = new ChartFrame("Data", lightbc);
         lightcf.setSize(800, 600);
         lightcf.setVisible(true);
 
         /*temperature Chart*/
         DefaultCategoryDataset tempds = new DefaultCategoryDataset();
         tempds.addValue(temperatureMean, "Mean", "");
         tempds.addValue(temperatureMin, "Min", "");
         tempds.addValue(temperatureMax, "Max", "");
         tempds.addValue(temperatureStddev, "Standard Deviation", "");
 
         JFreeChart tempbc = ChartFactory.createBarChart("Temperature Statistics", "Key", "Value", tempds, PlotOrientation.VERTICAL, true, false, false);
 
         CategoryPlot tempmainPlot = tempbc.getCategoryPlot();
 
         NumberAxis tempmainAxis = (NumberAxis) tempmainPlot.getRangeAxis();;
         tempmainAxis.setLowerBound(0);
         tempmainAxis.setUpperBound(50);
 
         ChartFrame tempcf = new ChartFrame("Data", tempbc);
         tempcf.setSize(800, 600);
         tempcf.setVisible(true);
     }
 }
