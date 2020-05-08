 /**
  * Class to represent insert size calculations for a specified orientation type
  */
 
 import java.util.TreeMap;
 import net.sf.picard.sam.SamPairUtil.PairOrientation;
 import java.io.*;
 import java.util.LinkedList;
 
 /**
  * @author Nirav Shah niravs@bcm.edu
  *
  */
 public class InsertSizeStats
 {
   private PairOrientation orientation;     // Orientation of the read pair
   private int medianInsertSize;            // Median insert size value
   private int modeInsertSize;              // Mode insert size value
   private int totalPairs;                  // Total pairs with this orientation
   private double threshold = 0.01;         // To clip insert size chart
   
   private TreeMap<Integer, Integer> insertSizeList = null;
   
   /**
    * Class constructor - prepare the object
    * @param orient
    */
   public InsertSizeStats(PairOrientation orient)
   {
     this.orientation = orient;
     insertSizeList = new TreeMap<Integer, Integer>();
   }
   
   /**
    * Add the insert size for the next read
    * @param insertSize
    */
   public void addInsertSize(int insertSize)
   {
     totalPairs++;
 	    
     Integer iSize = new Integer(insertSize); 
     if(insertSizeList.containsKey(iSize))
     {
       Integer val = insertSizeList.get(iSize);
       val++;
       insertSizeList.put(iSize, val);
       val = null;
     }
     else
     {
       insertSizeList.put(iSize, 1);
     }
     iSize = null;
   }
   
   public int getTotalPairs()
   {
     return totalPairs;
   }
 
   public int getMedianInsertSize()
   {
     return medianInsertSize;
   }
 
   public int getModeInsertSize()
   {
     return modeInsertSize;
   } 
 
   public PairOrientation getPairOrientation()
   {
     return orientation;
   }
   
   public void finishedAllReads()
   {
     calculateStats();
   }
 
   /**
    * Method to log the insert size distribution in a file and plot 
    * @throws IOException
    */
   public void logDistribution() throws IOException
   {
     logInsertSizeDistribution();
     createDistributionChart();
   }
   
   /**
    * Calculate mode and insert insert size values
    */
   private void calculateStats()
   {
     int medianIndex     = totalPairs / 2;
     int numElements     = 0;
     boolean foundMedian = false;
 	    
     Integer modeInsert = insertSizeList.firstKey();
     Integer modeValue  = insertSizeList.get(modeInsert);
 	    
     for(Integer key : insertSizeList.keySet())
     {
       numElements = numElements + insertSizeList.get(key).intValue();
 	      
       if(numElements > medianIndex && foundMedian == false)
       {
         medianInsertSize = key.intValue();
         foundMedian = true;
       }
 	      
       if(modeValue < insertSizeList.get(key))
       {
         modeInsert = key;
         modeValue  = insertSizeList.get(key);
       }
       key = null;
     }
 	modeInsertSize = modeInsert.intValue();
   }
   
   /**
    * Log insert size distribution in a CSV
    */
   private void logInsertSizeDistribution() throws IOException
   {
     System.err.println("Logging time");
     String logFileName    = orientation.toString() + "_InsertSizeDist.csv";
     BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName));
     
     for(Integer key : insertSizeList.keySet())
     {
       writer.write(key.toString() + "," + insertSizeList.get(key).toString());
       writer.newLine();
     }
     writer.close();
   }
 
   /**
    * Plot the distribution of insert size using GNUPlot
    */
   private void createDistributionChart()
   {
     System.err.println("chart time");
     String outputFile = orientation.toString() + "_InsertSizeDist.png";
 
     long startTime = System.currentTimeMillis();
     trimInsertSizeDistribution2();
     long endTime = System.currentTimeMillis();
     System.out.println("Time in trimming : " + (endTime - startTime));
      
     System.err.println("CHART TRIMMED.. PLOT TIME");
  
     int length = insertSizeList.keySet().size();
     double xAxis[] = new double[length];
     double yAxis[] = new double[length];
     int idx = 0;
     
     for(Integer key : insertSizeList.keySet())
     {
       xAxis[idx] = key.doubleValue();
       yAxis[idx] = insertSizeList.get(key).doubleValue();
       idx++;
     }
     try
     {
       Plot p = new Plot(outputFile, "Insert Size Distribution", "Insert Size", 
                         "Number of Reads", orientation.toString() + "_Distribution",
                         xAxis, yAxis);
       p.plotGraph();
     }
     catch(Exception e)
     {
       System.err.println(e.getMessage());
     }
   }
   
   /**
    * Trim insert size distribution to plot a meaningful graph
    */
   private void trimInsertSizeDistribution()
   {
     int numModeElements = insertSizeList.get(modeInsertSize).intValue();
     int minValue = (int)(threshold * numModeElements);
     int val;
     LinkedList<Integer> binsToRemove = new LinkedList<Integer>();
    
     System.err.println("List size before trimming : " + insertSizeList.size());
  
     /**
      * Keep all the insert size values lower than the modal value.
      * For the insert size value beyond the modal value, keep only the
      * records that exceed the minimum threshold.
      */
     for(Integer key : insertSizeList.keySet())
     {
       val = insertSizeList.get(key).intValue();
       if((key.intValue() > modeInsertSize) && (val < minValue))
       {
         binsToRemove.add(key);
       }
     }
     
     for(int i = 0; i < binsToRemove.size(); i++)
     {
       insertSizeList.remove(binsToRemove.get(i));
     }
     System.err.println("List size after trimming : " + insertSizeList.size());
   }
 
   private void trimInsertSizeDistribution2()
   {
     int numModeElements = insertSizeList.get(modeInsertSize).intValue();
     int minValue = (int)(threshold * numModeElements);
     int val;
     int lastKey;
    
     System.out.println("Mode = " + modeInsertSize);
     System.err.println("List size before trimming : " + insertSizeList.size());
  
     while(insertSizeList.size() > 0)
     {
       lastKey = insertSizeList.lastKey().intValue();
       val = insertSizeList.get(lastKey).intValue();
     //  System.out.println("Last key = " + lastKey + " Value = " + val + " min value = " + minValue);
 
       if(lastKey > modeInsertSize && (val < minValue))
       {
      //   System.out.println("Removing : " + lastKey);
         insertSizeList.remove(lastKey);
       }
       //if(lastKey <= modeInsertSize)
       else
         break;
 			  
       insertSizeList.remove(lastKey);
     }
     System.err.println("List size after trimming : " + insertSizeList.size());
     System.out.println("Mode = " + modeInsertSize);
   }
 }
