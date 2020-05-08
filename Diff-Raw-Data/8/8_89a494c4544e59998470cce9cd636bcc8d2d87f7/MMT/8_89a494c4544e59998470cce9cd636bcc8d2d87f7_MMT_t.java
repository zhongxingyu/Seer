 package org.ndaguan.micromanager.mmtracker;
 
 public class MMT {
 	public static final String menuName = "MultZIndexMeasure";
 	public static final String tooltipDescription = "MultZIndexMeasure";
	public static String DEFAULT_TITLE = "Magnetic Tweezers Images Analyzer(SM4.IOP.CAS.CN)";
 
 	public static  String magnetXYstage_ = "MP285 XY Stage";
 	public static  String magnetZStage_ = "MP285 Z Stage";
 
 	public static  String xyStage_ = null; 
 	public static  String zStage_ = null;
 
 	public static String[] VARNAME = new String[]{
 		"beanRadius",
 		"contourLen",
 		"calRange",
 		"calStepSize",
 		"rInterStep",
 		"magnetStepSize",
 		"frameToCalcForce",
 		"beanRadiuPixel",
 		"persistance",
 		"kT",
 		"pixelToPhysX",
 		"pixelToPhysY",
 		"precision",};
 	public static String[] UNIT = new String[]{"/uM","/uM","/uM","/uM","/pixel","/uM","","/pixel","nM","pN/nM","(Um/pixel)","(Um/pixel)","/uM"};
 	public static int[] PRECISION = new int[]{3,3,3,3,2,0,0,0,0,1,3,3,3};
 	public static String[] CHARTLIST = new String[]{"Chart-Z","Chart-X","Chart-Y","Chart-FX","Chart-FY","Chart-STDXDY","Chart-SKREWNESS","Chart-Debug"};
 
 	public static void logError(String string) 
 	{
 		System.out.print(String.format("Err!!!\t%s\r\n",string));
 	}
 	public static void debugError(String string) 
 	{
 		System.out.print(String.format("Err!!!\t%s\r\n",string));
 	}
 	public static void logMessage(String string) 
 	{
 		System.out.print(String.format("Msg>>\t%s\r\n",string));
 	}
 	public static void debugMessage(String string) 
 	{
 		System.out.print(String.format("Msg>>%s\t\r\n",string));
 	}
 }
