 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package eu.cassandra.training.consumption;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import eu.cassandra.training.utils.Constants;
 
 /**
  * This class is used for implementing the notion of a consumption event
  * repository. The consumption events are collected from the imported data set
  * and then an overall analysis can be done in this repository, in order to
  * create the necessary files and histograms for the training procedure at hand.
  * 
  * @author Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 public class ConsumptionEventRepo
 {
   /**
    * This variable represents the appliance that the consumption events belong
    * to.
    */
   String appliance;
 
   /**
    * This is a list of the consumption events collected for a single appliance
    * from the measurements data set.
    */
   ArrayList<ConsumptionEvent> events = new ArrayList<ConsumptionEvent>();
 
   /**
    * This variable is a map of the of the consumption events that are collected
    * for each date available in the data set.
    */
   Map<DateTime, ArrayList<ConsumptionEvent>> eventsPerDate =
     new HashMap<DateTime, ArrayList<ConsumptionEvent>>();
 
   /**
    * This is a map of the number of consumption events that are collected for
    * each date available in the data set.
    */
   Map<DateTime, Integer> numberEventsPerDate = new TreeMap<DateTime, Integer>();
 
   /**
    * This variable presents a map histogram of the different values found as
    * duration (in minutes) in the consumption events to their frequency
    * probability in the data set..
    */
   Map<Integer, Double> eventsDurationHistogram = new TreeMap<Integer, Double>();
 
   /**
    * This variable presents a map histogram of the different values found as
    * daily times an event was present for each available date to their frequency
    * probability in the data set.
    */
   Map<Integer, Double> eventsDailyTimesHistogram =
     new TreeMap<Integer, Double>();
 
   /**
    * This variable presents a map histogram of the different values found as
    * start minute of the day for an event to their frequency
    * probability in the data set.
    */
   Map<Integer, Double> eventsStartTimeHistogram =
     new TreeMap<Integer, Double>();
 
   /**
    * This variable presents a map histogram of the different values found as
    * start minute of the day in binned intervals (e.g. 10-minute, quarter
    * intervals) for an event to their frequency probability in the data set.
    */
   Map<Integer, Double> eventsStartTimeBinnedHistogram =
     new TreeMap<Integer, Double>();
 
   /**
    * This variable represents the number of bins that are appropriate for that
    * size of sample for the activity..
    */
   int bins;
 
   /**
    * This variable represents the number of minutes each bin contains.
    */
   int binSize;
 
   // =================CREATION FUNCTIONS==============================//
 
   /**
    * This is the constructor of an appliance's consumption event repo.
    * 
    * @param appliance
    *          The appliance the consumption events under investigation belong
    *          to.
    */
   public ConsumptionEventRepo (String appliance)
   {
     this.appliance = appliance;
   }
 
   /**
    * This function adds an consumption event to the repository.
    * 
    * @param e
    *          The detected consumption event.
    */
   public void addEvent (ConsumptionEvent e)
   {
     events.add(e);
   }
 
   /**
    * This function is used to fill the event per date map of the repository.
    * Each available consumption event is parsed and added to the appropriate
    * date.
    */
   public void createEventPerDateHashmap ()
   {
     // Initialize the auxiliary variables
     Map<DateTime, ArrayList<ConsumptionEvent>> tempMap =
       new HashMap<DateTime, ArrayList<ConsumptionEvent>>();
     ArrayList<ConsumptionEvent> events = getEvents();
 
     // Find the starting dates of all the events.
     DateTime temp = events.get(0).getStartDate();
     DateTime temp2 = events.get(events.size() - 1).getStartDate();
 
     // Fill the map with all the dates
     while (!temp.isAfter(temp2)) {
 
       tempMap.put(temp, new ArrayList<ConsumptionEvent>());
       temp = temp.plusDays(1);
 
     }
 
     // Add each and every event to the appropriate date.
     for (int i = 0; i < events.size(); i++) {
       DateTime loop = events.get(i).getStartDate();
       ArrayList<ConsumptionEvent> tempList = tempMap.get(loop);
       tempList.add(events.get(i));
       tempMap.put(loop, tempList);
     }
 
     // Sort the map based on dates
     eventsPerDate = new TreeMap<DateTime, ArrayList<ConsumptionEvent>>(tempMap);
 
     // Fill the number of events per date map also.
     for (DateTime date: eventsPerDate.keySet())
       numberEventsPerDate.put(date, eventsPerDate.get(date).size());
     //
     // System.out.println(eventsPerDate.toString());
     // System.out.println(numberEventsPerDate.toString());
   }
 
   /**
    * This function clears the maps of the repository in order to refill them
    * with new analysis data.
    */
   public void clear ()
   {
     numberEventsPerDate.clear();
     eventsPerDate.clear();
     eventsDurationHistogram.clear();
     eventsDailyTimesHistogram.clear();
     eventsStartTimeHistogram.clear();
     eventsStartTimeBinnedHistogram.clear();
   }
 
   /**
    * This function clears the event list of the repository in order to refill it
    * with new consumption events.
    */
   public void cleanEvents ()
   {
     events.clear();
   }
 
   /**
    * This function analyses the collected consumption events and create the
    * resulting frequency histograms.
    * 
    * @throws FileNotFoundException
    */
   public void analyze () throws FileNotFoundException
   {
 
     clear();
 
     createEventPerDateHashmap();
 
     System.out.println("Overall Days:" + eventsPerDate.keySet().size());
     setBins();
     createDurationHistogram();
     createDailyTimesHistogram();
     createStartTimeHistogram();
     createStartTimeBinnedHistogram(binSize, bins);
 
     // ChartUtils.createHistogram("Duration", "Minutes", "Possibility",
     // eventsDurationHistogram);
     // DurationHistogramToFile();
     // ChartUtils.createHistogram("DailyTimes", "Daily Times",
     // "Possibility",
     // eventsDailyTimesHistogram);
     // DailyTimesHistogramToFile();
     // ChartUtils.createHistogram("StartTime", "Minute Of Day",
     // "Possibility",
     // eventsStartTimeHistogram);
     // StartTimeHistogramToFile();
     // ChartUtils.createHistogram("StartTimeBinned", "Ten-Minute Of Day",
     // "Possibility", eventsStartTimeHistogram);
     // StartTimeBinnedHistogramToFile();
 
   }
 
   /**
    * This function sets the appropriate size and number of the bins for the
    * histograms.
    */
   private void setBins ()
   {
 
     if (events.size() <= Constants.HOUR_SAMPLE_LIMIT) {
       binSize = Constants.MINUTES_PER_HOUR;
       bins = Constants.HOURS_PER_DAY;
     }
     else if (events.size() <= Constants.QUARTER_SAMPLE_LIMIT) {
       binSize = Constants.QUARTER;
       bins = Constants.QUARTERS_PER_DAY;
     }
     else if (events.size() <= Constants.TEN_MINUTE_SAMPLE_LIMIT) {
       binSize = Constants.TEN_MINUTES;
       bins = Constants.TEN_MINUTES_PER_DAY;
     }
     else if (events.size() <= Constants.FIVE_MINUTE_SAMPLE_LIMIT) {
       binSize = Constants.FIVE_MINUTES;
       bins = Constants.FIVE_MINUTES_PER_DAY;
     }
     else {
       binSize = Constants.ONE_MINUTE;
       bins = Constants.MINUTES_PER_DAY;
     }
 
     System.out.println("Events: " + events.size() + " Bins: " + bins
                        + " Minutes per bin: " + binSize);
 
   }
 
   /**
    * This is a getter function for the bin number variable.
    * 
    * @return the number of bins for the histograms.
    */
   public int getBins ()
   {
     return bins;
   }
 
   /**
    * This is a getter function for the bin size variable.
    * 
    * @return the size of the bins for the histograms.
    */
   public int getBinSize ()
   {
     return binSize;
   }
 
   /**
    * This is a getter function for the available consumption events.
    * 
    * @return the list of consumption events of the repository
    */
   public ArrayList<ConsumptionEvent> getEvents ()
   {
     return events;
   }
 
   /**
    * This is a getter function for the map of number of events per date.
    * 
    * @return the map of number of events per date.
    */
   public Map<DateTime, Integer> getNumberEventsPerDate ()
   {
     return numberEventsPerDate;
   }
 
   /**
    * This is a getter function for the map of events per date.
    * 
    * @return the map of events per date.
    */
   public Map<DateTime, ArrayList<ConsumptionEvent>> getEventsPerDate ()
   {
     return eventsPerDate;
   }
 
   /**
    * This is a getter function for the duration histogram.
    * 
    * @return the duration histogram.
    */
   public Map<Integer, Double> getDurationHistogram ()
   {
     return eventsDurationHistogram;
   }
 
   /**
    * This is a getter function for the daily times histogram.
    * 
    * @return the daily times histogram.
    */
   public Map<Integer, Double> getDailyTimesHistogram ()
   {
     return eventsDailyTimesHistogram;
   }
 
   /**
    * This is a getter function for the start time histogram.
    * 
    * @return the start time histogram.
    */
   public Map<Integer, Double> getStartTimeHistogram ()
   {
     return eventsStartTimeHistogram;
   }
 
   /**
    * This is a getter function for the start time binned histogram.
    * 
    * @return the start time binned histogram.
    */
   public Map<Integer, Double> getStartTimeBinnedHistogram ()
   {
     return eventsStartTimeBinnedHistogram;
   }
 
   /**
    * This is the function that creates the duration histogram by parsing through
    * all the available consumption events and checking on their duration.
    */
   public void createDurationHistogram ()
   {
 
     Map<Integer, Double> tempDurationHistogram = new HashMap<Integer, Double>();
     ArrayList<ConsumptionEvent> events = getEvents();
 
     for (int i = 0; i < events.size(); i++) {
 
       Integer temp = (int) events.get(i).getDuration().getStandardMinutes();
 
       if (tempDurationHistogram.containsKey(temp))
         tempDurationHistogram.put(temp, tempDurationHistogram.get(temp) + 1);
       else
         tempDurationHistogram.put(temp, Double.valueOf(1));
     }
 
     // System.out.println(tempDurationHistogram.toString());
     // double sum = 0;
 
     for (Integer duration: tempDurationHistogram.keySet()) {
 
       tempDurationHistogram.put(duration,
                                 Double.valueOf(tempDurationHistogram
                                         .get(duration) / events.size()));
 
       // sum += tempDurationHistogram.get(duration);
     }
 
     eventsDurationHistogram =
       new TreeMap<Integer, Double>(tempDurationHistogram);
     /*
      * System.out.print("Number of Events for type " + type + ":" +
      * events.size() + " "); System.out.println(sortedMap.toString());
      * System.out.println(sum);
      */
 
   }
 
   /**
    * This is the function that creates the daily histogram by parsing through
    * all the available dates and checking on the number of consumption events
    * present for each date.
    */
   public void createDailyTimesHistogram ()
   {
     Map<Integer, Double> tempDailyTimesHistogram =
       new HashMap<Integer, Double>();
 
     Map<DateTime, Integer> eventsPerDate = getNumberEventsPerDate();
 
     for (DateTime date: eventsPerDate.keySet()) {
 
       Integer temp = eventsPerDate.get(date);
 
       if (tempDailyTimesHistogram.containsKey(temp))
         tempDailyTimesHistogram
                 .put(temp, tempDailyTimesHistogram.get(temp) + 1);
       else
         tempDailyTimesHistogram.put(temp, Double.valueOf(1));
     }
 
     // System.out.println(tempDailyTimesHistogram.toString());
     double total = 0;
 
     for (Integer times: tempDailyTimesHistogram.keySet()) {
 
       total += tempDailyTimesHistogram.get(times);
 
     }
 
     // double sum = 0;
     for (Integer dailyTimes: tempDailyTimesHistogram.keySet()) {
 
       tempDailyTimesHistogram.put(dailyTimes,
                                   Double.valueOf(tempDailyTimesHistogram
                                           .get(dailyTimes) / total));
 
       // sum += tempDailyTimesHistogram.get(dailyTimes);
     }
 
     eventsDailyTimesHistogram =
       new TreeMap<Integer, Double>(tempDailyTimesHistogram);
 
     // System.out.println(eventsDailyTimesHistogram.toString());
     /*
      * System.out.print("Number of Events for type " + type + ":" +
      * events.size() + " ");
      * System.out.println(tempDailyTimesHistogram.toString());
      * System.out.println(sum);
      */
 
   }
 
   /**
    * This is the function that creates the start time histogram by parsing
    * through all the available consumption events and checking on their
    * start minute of the day.
    */
   public void createStartTimeHistogram ()
   {
 
     Map<Integer, Double> tempStartTimeHistogram =
       new HashMap<Integer, Double>();
 
     ArrayList<ConsumptionEvent> events = getEvents();
 
     for (int i = 0; i < events.size(); i++) {
 
       Integer temp = events.get(i).getStartMinuteOfDay();
 
       if (tempStartTimeHistogram.containsKey(temp))
         tempStartTimeHistogram.put(temp, tempStartTimeHistogram.get(temp) + 1);
       else
         tempStartTimeHistogram.put(temp, Double.valueOf(1));
     }
 
     // System.out.println(tempStartTimeHistogram.toString());
     // double sum = 0;
 
     for (Integer startTime: tempStartTimeHistogram.keySet()) {
 
       tempStartTimeHistogram.put(startTime,
                                  Double.valueOf(tempStartTimeHistogram
                                          .get(startTime) / events.size()));
 
       // sum += tempStartTimeHistogram.get(startTime);
     }
 
     eventsStartTimeHistogram =
       new TreeMap<Integer, Double>(tempStartTimeHistogram);
     /*
      * System.out.print("Number of Events for type " + type + ":" +
      * events.size() + " ");
      * System.out.println(tempStartTimeHistogram.toString());
      * System.out.println(sum);
      */
 
   }
 
   /**
    * This is the function that creates the start time histogram by parsing
    * through all the available consumption events and checking on their
    * start minute of the day.
    */
   public void createStartTimeHistogram2 ()
   {
 
     Map<Integer, Double> tempStartTimeHistogram =
       new HashMap<Integer, Double>();
 
     Map<Integer, Double> temp = new HashMap<Integer, Double>();
 
     ArrayList<ConsumptionEvent> events = getEvents();
     double percentage = 0;
     Integer minute;
 
     for (int i = 0; i < events.size(); i++) {
 
       minute = events.get(i).getStartMinuteOfDay() / binSize;
 
       if (tempStartTimeHistogram.containsKey(minute))
         tempStartTimeHistogram.put(minute,
                                    tempStartTimeHistogram.get(minute) + 1);
       else
         tempStartTimeHistogram.put(minute, Double.valueOf(1));
     }
 
     // System.out.println(tempStartTimeHistogram.toString());
 
     for (Integer startTime: tempStartTimeHistogram.keySet()) {
 
       tempStartTimeHistogram.put(startTime,
                                  Double.valueOf(tempStartTimeHistogram
                                          .get(startTime) / events.size()));
 
     }
 
     for (int i = 0; i < bins; i++) {
 
       if (tempStartTimeHistogram.containsKey(i)) {
 
         percentage = tempStartTimeHistogram.get(i) / binSize;
 
         for (int j = 0; j < binSize; j++) {
           minute = i * binSize + j;
           temp.put(minute, percentage);
         }
 
       }
 
     }
 
     // double sum = 0;
     //
     // for (Integer startTime: temp.keySet())
     // sum += temp.get(startTime);
 
     eventsStartTimeHistogram = new TreeMap<Integer, Double>(temp);
 
     // System.out.print("Number of Events:" + events.size() + " ");
     // System.out.println(temp.toString());
     // System.out.println(sum);
 
   }
 
   /**
    * This is the function that creates the start time binned histogram by
    * aggregating the data available from the start time histogram to certain
    * time intervals.
    * 
    * @param minuteInterval
    *          The number of minutes per bin.
    * @param intervals
    *          The number of bins
    */
   public void
     createStartTimeBinnedHistogram (int minuteInterval, int intervals)
   {
 
     Map<Integer, Double> tempStartTimeBinnedHistogram =
       new HashMap<Integer, Double>();
 
     Map<Integer, Double> eventsStartTimeHistogram = getStartTimeHistogram();
 
     // System.out.println(eventsStartTimeHistogram.toString());
 
     for (int i = 0; i < intervals; i++) {
 
       double tempSum = 0;
 
       for (int j = 0; j < minuteInterval; j++) {
 
         int tick = minuteInterval * i + j;
         if (eventsStartTimeHistogram.containsKey(tick))
           tempSum += eventsStartTimeHistogram.get(tick);
       }
 
       tempStartTimeBinnedHistogram.put(i, tempSum);
 
     }
 
     // double sum = 0;
 
     // for (Integer startTime: tempStartTimeBinnedHistogram.keySet()) {
     //
     // sum += tempStartTimeBinnedHistogram.get(startTime);
     // }
 
     eventsStartTimeBinnedHistogram =
       new TreeMap<Integer, Double>(tempStartTimeBinnedHistogram);
 
     // System.out.println(tempStartTimeBinnedHistogram.toString());
     // System.out.println(sum);
 
   }
 
   /**
    * Function for exporting the consumption event start and end times
    * to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    */
   public void eventsToFile (String filename)
   {
     try {
 
       DateTime startBase = events.get(0).getStartDate();
       DateTime endBase = events.get(events.size() - 1).getEndDate().plusDays(1);
 
       long endTick =
         new Interval(startBase, endBase).toDuration().getStandardMinutes();
 
       PrintStream realSystemOut = System.out;
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
 
       System.out.println("End:" + endTick);
 
       for (int i = 0; i < events.size(); i++) {
 
         long startDistance =
           new Interval(startBase, events.get(i).getStartDateTime())
                   .toDuration().getStandardMinutes();
 
         long endDistance =
           new Interval(startBase, events.get(i).getEndDateTime()).toDuration()
                   .getStandardMinutes();
 
         System.out.println(startDistance + "-" + endDistance);
 
       }
 
       System.setOut(realSystemOut);
       output.close();
 
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Function for exporting the values that are found in the consumption events
    * for a certain attribute to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    * @param atribute
    *          The name of the attribute ((Daily Times, Duration, Start Time,
    *          Start Time Binned)
    */
   public void attributeToFile (String filename, String attribute)
   {
     try {
 
       ArrayList<ConsumptionEvent> events = getEvents();
       Map<DateTime, Integer> numberEvents = getNumberEventsPerDate();
       int temp = 0;
 
       PrintStream realSystemOut = System.out;
 
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
 
       switch (attribute) {
 
       case "DailyTimes":
         for (DateTime date: numberEvents.keySet()) {
           temp = numberEvents.get(date);
           System.out.println(temp);
         }
         break;
 
       case "Duration":
         for (int i = 0; i < events.size(); i++) {
           temp = (int) (events.get(i).getDuration().getStandardMinutes());
           System.out.println(temp);
         }
         break;
 
       case "StartTime":
         for (int i = 0; i < events.size(); i++) {
           temp = (events.get(i).getStartMinuteOfDay());
           System.out.println(temp);
         }
         break;
 
       case "StartTimeBinned":
         for (int i = 0; i < events.size(); i++) {
           temp = events.get(i).getStartMinuteOfDay() / Constants.TEN_MINUTES;
           System.out.println(temp);
         }
         break;
 
       default:
         System.out.println("ERROR");
 
       }
 
       System.setOut(realSystemOut);
       output.close();
 
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Function for exporting the Duration histogram of the consumption event
    * repository to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    */
   public void DurationHistogramToFile (String filename)
   {
     try {
 
       DecimalFormat df = new DecimalFormat("#.#####");
       Map<Integer, Double> temp = getDurationHistogram();
 
       PrintStream realSystemOut = System.out;
 
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
       System.out.println("Histogram");
       System.out.println("0-0");
 
       for (Integer duration: temp.keySet()) {
 
         System.out.println(duration + "-" + df.format(temp.get(duration)));
 
       }
 
       System.setOut(realSystemOut);
       output.close();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Function for exporting the Daily Times histogram of the consumption event
    * repository to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    */
   public void DailyTimesHistogramToFile (String filename)
   {
     try {
 
       DecimalFormat df = new DecimalFormat("#.#####");
       Map<Integer, Double> temp = getDailyTimesHistogram();
 
       PrintStream realSystemOut = System.out;
 
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
 
       System.out.println("Histogram");
 
       for (Integer duration: temp.keySet()) {
 
         System.out.println(duration + "-" + df.format(temp.get(duration)));
 
       }
 
       System.setOut(realSystemOut);
       output.close();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Function for exporting the Start Time histogram of the consumption event
    * repository to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    */
   public void StartTimeHistogramToFile (String filename)
   {
     try {
 
       DecimalFormat df = new DecimalFormat("#.#####");
       Map<Integer, Double> temp = getStartTimeHistogram();
 
       PrintStream realSystemOut = System.out;
 
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
 
       System.out.println("Histogram");
 
       for (int i = 0; i < Constants.MINUTES_PER_DAY; i++) {
 
         if (temp.containsKey(i))
           System.out.println(i + "-" + df.format(temp.get(i)));
         else
           System.out.println(i + "-0");
       }
 
       System.setOut(realSystemOut);
       output.close();
 
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Function for exporting the Start Binned histogram of the consumption event
    * repository to a file for the training procedure.
    * 
    * @param filename
    *          The name of the file that will be exported.
    */
   public void StartTimeBinnedHistogramToFile (String filename)
   {
     try {
 
       DecimalFormat df = new DecimalFormat("#.#####");
       Map<Integer, Double> temp = getStartTimeBinnedHistogram();
 
       PrintStream realSystemOut = System.out;
 
       OutputStream output = new FileOutputStream(filename);
       PrintStream printOut = new PrintStream(output);
       System.setOut(printOut);
 
       System.out.println("Histogram");
 
       for (Integer duration: temp.keySet()) {
 
         System.out.println(duration + "-" + df.format(temp.get(duration)));
 
       }
 
       System.setOut(realSystemOut);
       output.close();
 
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * 
    * Function for importing consumption events from an file.
    * 
    * @param filename
    *          The name of the file that will be exported.
    * @throws FileNotFoundException
    */
   public void readEventsFile (String filename) throws FileNotFoundException
   {
 
     int startMinute = 0;
     int endMinute = 0;
     int counter = 0;
     DateTime startDateTime = new DateTime();
     DateTime endDateTime = new DateTime();
     DateTime startDate = new DateTime();
     DateTime endDate = new DateTime();
     DateTime date = new DateTime(2010, 1, 1, 0, 0);
 
     System.out.println(filename);
 
     File file = new File(filename);
 
     Scanner scanner = new Scanner(file);
 
     String line = scanner.nextLine();
     String[] temp = new String[2];
 
     while (scanner.hasNext()) {
 
       line = scanner.nextLine();
       temp = line.split("-");
 
       startMinute = Integer.parseInt(temp[0]);
       endMinute = Integer.parseInt(temp[1]);
 
       startDateTime = date.plusMinutes(startMinute);
       endDateTime = date.plusMinutes(endMinute);
 
       startDate =
         new DateTime(startDateTime.getYear(), startDateTime.getMonthOfYear(),
                      startDateTime.getDayOfMonth(), 0, 0);
       endDate =
         new DateTime(endDateTime.getYear(), endDateTime.getMonthOfYear(),
                      endDateTime.getDayOfMonth(), 0, 0);
 
       if (startDateTime.isAfter(endDateTime) == false)
         events.add(new ConsumptionEvent(counter++, startDateTime, startDate,
                                         endDateTime, endDate));
      else
        System.out.println("Start: " + startDateTime + " End: " + endDateTime);
 
     }
 
     scanner.close();
 
     analyze();
 
   }
 
 }
