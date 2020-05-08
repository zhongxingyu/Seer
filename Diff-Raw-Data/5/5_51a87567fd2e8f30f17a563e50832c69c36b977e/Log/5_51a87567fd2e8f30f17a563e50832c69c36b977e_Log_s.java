 package com.gris.ege.other;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 public class Log
 {
 	private static final String  TAG            = "Log";
 
 	private static final String  APP_NAME       = "EGE";
 	private static final String  FILE_PATH      = GlobalData.PATH_ON_SD_CARD+"Logs";
 
 	private static final boolean DEBUG          = true;
 	private static final boolean OUTPUT_TO_FILE = true;
 	private static final boolean ONLY_APP_TAG   = true;
 
 	private static final int     MAX_COUNT      = 50;
 
 
 
 	private static String mFileName="";
 
 
 
 	public static void reset()
 	{
 		mFileName="";
 	}
 
 	private static void writeToFile(String aLevel, String aTag, String aMessage, Throwable aException)
 	{
 		if (OUTPUT_TO_FILE)
 		{
 			try
             {
 				if (mFileName.equals(""))
 				{
 					File aLogDir=new File(FILE_PATH);
 
 					if (aLogDir.exists())
 					{
 						String[] aFiles=aLogDir.list();
 
 						int aMaxIndex=0;
 
 						for (int i=0; i<aFiles.length; ++i)
 						{
 							String aNumber=aFiles[i].substring(0, aFiles[i].lastIndexOf('.'));
 							int aIndex=Integer.parseInt(aNumber);
 
 							if (aIndex>aMaxIndex)
 							{
 								aMaxIndex=aIndex;
 							}
 						}
 
 						for (int i=0; i<aFiles.length; ++i)
 						{
 							String aNumber=aFiles[i].substring(0, aFiles[i].lastIndexOf('.'));
 							int aIndex=Integer.parseInt(aNumber);
 
 							if (aIndex<=aMaxIndex-MAX_COUNT+1)
 							{
 								new File(FILE_PATH+"/"+String.valueOf(aIndex)+".dlv").delete();
 							}
 						}
 
 						++aMaxIndex;
 
 						mFileName=FILE_PATH+"/"+String.valueOf(aMaxIndex)+".dlv";
 					}
 					else
 					{
 						aLogDir.mkdirs();
 						mFileName=FILE_PATH+"/1.dlv";
 					}
 				}
 
 				String aCurTimeStr=new SimpleDateFormat("MM-DD kk:mm:ss:SSS", new Locale("en")).format(new Date());
 				String aPrefixText=aCurTimeStr+": "+aLevel+"/"+APP_NAME+"(9999)"+": ";
 
 	            FileOutputStream aFileStream = new FileOutputStream(mFileName, true);
 	            PrintWriter aPrinter         = new PrintWriter(aFileStream, true);
 
 	            aPrinter.println(aPrefixText+aMessage);
 
 
 
 	            if (aException!=null)
 	            {
	            	String aExceptionMsg=aException.getLocalizedMessage();
 
 	            	if (aExceptionMsg!=null)
 	            	{
	            		aPrinter.println(aPrefixText+aExceptionMsg);
 	            	}
 
 
 
 	            	StackTraceElement[] aStack=aException.getStackTrace();
 
 	            	for (int i=0; i<aStack.length; ++i)
 	            	{
 	            		String aStackStr="\tat "+aStack[i].getClassName()+"."+aStack[i].getMethodName()+"(";
 	            		int aLineNumber=aStack[i].getLineNumber();
 
 	            		if (aLineNumber==-2)
 	            		{
 	            			aStackStr=aStackStr+"Native Method";
 	            		}
 	            		else
 	            		if (aLineNumber<1)
 	            		{
 	            			aStackStr=aStackStr+"Unknown";
 	            		}
 	            		else
 	            		{
 	            			aStackStr=aStackStr+aStack[i].getFileName()+":"+String.valueOf(aLineNumber);
 	            		}
 
 	            		aStackStr=aStackStr+")";
 
 	            		aPrinter.println(aPrefixText+aStackStr);
 	            	}
 	            }
 
 
 
 	            aPrinter.close();
             }
             catch (Exception e)
             {
             	android.util.Log.e(TAG, "Impossible write log to SD card", e);
             }
 		}
 	}
 
 	public static void v(String aTag, String aMsg)
 	{
 		v(aTag, aMsg, null);
 	}
 
 	public static void v(String aTag, String aMsg, Throwable tr)
 	{
 		if (ONLY_APP_TAG)
 		{
 			aMsg = aTag + ": " + aMsg;
 			aTag = APP_NAME;
 		}
 
 		if (DEBUG)
 		{
 			android.util.Log.v(aTag, aMsg, tr);
 		}
 
 		writeToFile("VERBOSE", aTag, aMsg, tr);
 	}
 
 	public static void d(String aTag, String aMsg)
 	{
 		d(aTag, aMsg, null);
 	}
 
 	public static void d(String aTag, String aMsg, Throwable tr)
 	{
 		if (ONLY_APP_TAG)
 		{
 			aMsg = aTag + ": " + aMsg;
 			aTag = APP_NAME;
 		}
 
 		if (DEBUG)
 		{
 			android.util.Log.d(aTag, aMsg, tr);
 		}
 
 		writeToFile("DEBUG", aTag, aMsg, tr);
 	}
 
 	public static void i(String aTag, String aMsg)
 	{
 		i(aTag, aMsg, null);
 	}
 
 	public static void i(String aTag, String aMsg, Throwable tr)
 	{
 		if (ONLY_APP_TAG)
 		{
 			aMsg = aTag + ": " + aMsg;
 			aTag = APP_NAME;
 		}
 
 		if (DEBUG)
 		{
 			android.util.Log.i(aTag, aMsg, tr);
 		}
 
 		writeToFile("INFO", aTag, aMsg, tr);
 	}
 
 	public static void w(String aTag, String aMsg)
 	{
 		w(aTag, aMsg, null);
 	}
 
 	public static void w(String aTag, String aMsg, Throwable tr)
 	{
 		if (ONLY_APP_TAG)
 		{
 			aMsg = aTag + ": " + aMsg;
 			aTag = APP_NAME;
 		}
 
 		if (DEBUG)
 		{
 			android.util.Log.w(aTag, aMsg, tr);
 		}
 
 		writeToFile("WARN", aTag, aMsg, tr);
 	}
 
 	public static void e(String aTag, String aMsg)
 	{
 		e(aTag, aMsg, null);
 	}
 
 	public static void e(String aTag, String aMsg, Throwable tr)
 	{
 		if (ONLY_APP_TAG)
 		{
 			aMsg = aTag + ": " + aMsg;
 			aTag = APP_NAME;
 		}
 
 		if (DEBUG)
 		{
 			android.util.Log.e(aTag, aMsg, tr);
 		}
 
 		writeToFile("ERROR", aTag, aMsg, tr);
 	}
 
 	public static String getCurrentFile()
     {
 	    return mFileName;
     }
 
 	public static String getPreviousFile()
     {
 		if (mFileName.equals(""))
 		{
 			return null;
 		}
 
 		String aNumber=mFileName.substring(mFileName.lastIndexOf('/')+1, mFileName.lastIndexOf('.'));
 		int aIndex=Integer.parseInt(aNumber)-1;
 
 		if (new File(FILE_PATH+"/"+String.valueOf(aIndex)+".dlv").exists())
 		{
 			return FILE_PATH+"/"+String.valueOf(aIndex)+".dlv";
 		}
 
 	    return null;
     }
 }
