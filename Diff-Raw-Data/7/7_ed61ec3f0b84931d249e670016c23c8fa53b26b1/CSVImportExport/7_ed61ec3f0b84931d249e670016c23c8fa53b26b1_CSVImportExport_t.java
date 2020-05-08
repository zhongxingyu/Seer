 package org.flying.lions.uitest;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.lang.System;
 import android.os.Environment;
 import android.util.Log;
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 
 public class CSVImportExport extends Plugin {
 	
 	public static final String TAG = "CSVImport_Export_Plugin";
 	
 	/** Currently active plugin instance */
 	public static Plugin currentPluginInstance;
 
 
 
     @Override
     public PluginResult execute(String action, JSONArray data, String callbackId) {
         Log.d(TAG, "Plugin Called");
         PluginResult result = null;
         //JSONObject transactions = new JSONObject();
         currentPluginInstance = this;
         
         
         if (action.equals("importCSV")) {
             try {
             	String fileLocation = data.getString(0);
             	          	
             	importCSV(fileLocation);
             	
                 result = new PluginResult(PluginResult.Status.OK);
             } catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception "+ jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
             catch (IOException ioEx) {
                 Log.d(TAG, "Got IO Exception "+ ioEx.getMessage());
                 result = new PluginResult(PluginResult.Status.IO_EXCEPTION);
             }
         }
         else if(action.equals("exportCSV")) {
             try{
             	String fileLocation = data.getString(0);
             	String transactions = data.getString(1);
             	
             	
             	exportCSV(fileLocation, transactions);
             	
             	result = new PluginResult(PluginResult.Status.OK);
             	
             } 
             catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception "+ jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
             catch (IOException ioEx) {
                 Log.d(TAG, "Got IO Exception "+ ioEx.getMessage());
                 result = new PluginResult(PluginResult.Status.IO_EXCEPTION);
             }
         }
         else if(action.equals("importOld")) {
             try{
             	String fileLocation = data.getString(0);
             	         	
             	
             	importOld(fileLocation);
             	
             	result = new PluginResult(PluginResult.Status.OK);
             	
             } 
             catch (JSONException jsonEx) {
                 Log.d(TAG, "Got JSON Exception "+ jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
             catch (IOException ioEx) {
                 Log.d(TAG, "Got IO Exception "+ ioEx.getMessage());
                 result = new PluginResult(PluginResult.Status.IO_EXCEPTION);
             }
         }
         else {
             result = new PluginResult(PluginResult.Status.INVALID_ACTION);
             Log.d(TAG, "Invalid action : "+action+" passed");
         }
         return result;
     }
     
     private void importCSV(String fileLoc) throws IOException
     {
     	String tempString;
     	
     	File sdcard = Environment.getExternalStorageDirectory();
     	
       	File deletefile = new File("mnt/sdcard/MEM/ORI/PrevValue.txt");
       	boolean success = deletefile.delete();
       	
        	File file = new File(fileLoc);   
         
        	if(file.exists())
         {
             Scanner lineScanner = new Scanner(file);
             int smsCount = 0;
             
             while (lineScanner.hasNextLine()) {
                     lineScanner.nextLine();
                     smsCount++;
             }
             lineScanner.close();
             
             Log.d(TAG, "LINECOUNT" + Integer.toString(smsCount));
             
             int incVal = 0;
             int counter = 0;
             
             Scanner sc = new Scanner(file);
             
             while (sc.hasNextLine()) {
             	tempString = sc.nextLine();
                 int comIndex = 0;
                 
                 counter+=1;
 
                 for (int y = 0; y < 7; y++) {
                     comIndex = tempString.indexOf(",");
                     tempString = tempString.substring(comIndex + 1, tempString.length());
                 }
                 try 
                 {
                     tempString = tempString.substring(1, tempString.length() - 1);
                 } 
                 catch (Exception e) 
                 {
                 	Log.e(TAG, "CSV file syntax error");
                 }
                 
                 try
     			{
                 	Long tsLong = System.currentTimeMillis();
     				String timestamp = tsLong.toString();
     				
     				MultipleSmsHandler smsHand = new MultipleSmsHandler();
     		        smsHand.parseSMS(tempString+ ":" + timestamp);
     		        //Log.d(TAG,tempString+ ";" + timestamp);
     		        
     		        
     		        incVal = counter * 100 / smsCount;
     		        
     		        currentPluginInstance.sendJavascript("javascript:upProgress("+ Integer.toString(incVal) +")");
     			}
     			catch(Exception ex)
     			{
     				ex.printStackTrace();
     			}
                 
             }
 
             sc.close();
 
         }
        	else
        	{
        		currentPluginInstance.sendJavascript("javascript:alert(\"test.csv not found on sdcard\")");
        		Log.d(TAG, "test.csv not in root");
        	}
        	
        	
     
     
     }
     
     private void exportCSV(String fileLocation, String transactions) throws IOException
     {
     	String tempString = "Griffin Budgeter - ";
 
 		FileWriter fileWriter = new FileWriter(fileLocation + ".csv");
 		 
 			 
 		fileWriter.write(tempString);
 		fileWriter.write(transactions);
 		
 		fileWriter.flush();
 		fileWriter.close();
 		
 		currentPluginInstance.sendJavascript("javascript:alert(\"All transactions have been exported\")");
     	
     }
 
     private void importOld(String fileLoc) throws IOException
     {
     	String tempString;
     	
     	File sdcard = Environment.getExternalStorageDirectory();
     	
        	File file = new File(fileLoc);   
       	File deletefile = new File("mnt/sdcard/MEM/ORI/PrevValue.txt");
       	deletefile.delete();
       	
        	
        	//GETTING PREV
        	File prevfile = new File("mnt/sdcard/MEM/ORI/PrevValue.txt");   
        	ArrayList<String> acc_numbers = new ArrayList<String>();
         ArrayList<String> acc_balances = new ArrayList<String>();
         if (prevfile.exists()) {
         	Scanner prevScanner = new Scanner(prevfile);
         	while (prevScanner.hasNextLine()) {
         		String[] s = prevScanner.nextLine().split("=");
                 s[1].replace(";","");
                 acc_numbers.add(s[0]);
                 acc_balances.add(s[1]);
         	}
         	prevScanner.close();
         }
         //END GETTING PREV
         
        	if(file.exists())
         {
             Scanner lineScanner = new Scanner(file);
             int smsCount = 0;
             while (lineScanner.hasNextLine()) {
                     lineScanner.nextLine();
                     smsCount++;
             }
             lineScanner.close();
             
             
             int incVal = 0;
             int counter = 0;
             
             Scanner sc = new Scanner(file);
             
             //Skip first two lines
             tempString = sc.nextLine();
             tempString = sc.nextLine();
             
             
             
             
             while (sc.hasNextLine()) {
             	tempString = sc.nextLine();
                 int commaIndex1 = 0;
                 String sqlVal = "";
                 String sqlStatement = "INSERT INTO sms(Date,Time,Amount,Balance,Location,Account_Num,Category) values(";
                 
                 //transaction counter
                 counter+=1;
                 String tmpbalance="";
                 //SQL query example
                 //INSERT INTO sms(Date,Time,Amount,Balance,Location,Account_Num,Category) values('2012/05/22','10000',-206.26,13196.0,'Superspar Monument P','cheq Acc..909429','food')
                 for (int y = 0; y < 7; y++) {
                     commaIndex1 = tempString.indexOf(",");
                     
                     
                     
 	                try 
 	                {
 	                	if((commaIndex1 == -1) && (y==6))
 	                		sqlVal = tempString.substring(0, tempString.length() - 1 );
 	                	else
 	                		sqlVal = tempString.substring(0, commaIndex1);
 	                	
 	                	tempString = tempString.substring(commaIndex1 + 1, tempString.length());
 	                	
 	                	
 	                	if((y==2)||(y==3))
 	                		sqlStatement += sqlVal;
 	                	else
 	                		sqlStatement += "'" + sqlVal + "'";
 	                	
 	                	//SETTING
 	                	if (y==3)
 	                    	tmpbalance = sqlVal; 
     	                if (y==5) { //acc_number
     	                	if (acc_numbers.contains(sqlVal)) {
     	                		for (int k=0; k<acc_numbers.size(); k++)
     	                			if (acc_numbers.get(k).equals(sqlVal)) {
     	                				acc_balances.set(k,tmpbalance);
     	                				break;
     	                			}    	                				
     	                	} //update balance
     	                	else {//does not exist, must add
     	                		acc_numbers.add(sqlVal);
     	                		acc_balances.add(tmpbalance);
     	                	}     	                	
     	                } //end y=5
     	                //END SETTING
 	                	
 	                	
 	                } 
 	                catch (Exception e) 
 	                {
 	                	Log.e(TAG, "CSV file syntax error");
 	                }
 	                
                 	if(y==6)
                 		sqlStatement += ")";
                 	else
                 		sqlStatement += ",";
                 	
 	                
                 }
                 
                 Log.d(TAG,"SQL ==> " + sqlStatement);
                 
                 //WRITING PREV VALUES
                 FileWriter prevfileWriter = new FileWriter("/mnt/sdcard/MEM/ORI/PrevValue.txt", false);
         		String prevstring = "";
         		for (int k=0; k<acc_balances.size(); k++) {
         			prevstring=acc_numbers.get(k)+"="+acc_balances.get(k)+";\r\n";
         		}
        		 	prevfileWriter.write(prevstring);
        		 	prevfileWriter.flush();
        		 	prevfileWriter.close();
       
        		 	
                 
         		FileWriter fileWriter = new FileWriter("/mnt/sdcard/MEM/importsqlstatements.txt", true);
 
         		fileWriter.append(sqlStatement + "\r\n");
         		
         		fileWriter.flush();
         		fileWriter.close();
                 
    		        incVal = counter * 100 / smsCount;
     		        
    		        currentPluginInstance.sendJavascript("javascript:upProgress("+ Integer.toString(incVal) +")");
 
                 
             }
 
             sc.close();
 
         }
        	else
        	{
        		currentPluginInstance.sendJavascript("javascript:alert(\"export.csv not found on sdcard\")");
        		Log.d(TAG, "test.csv not in root");
        	}
        	
        	
     
     
     }
     
 }
