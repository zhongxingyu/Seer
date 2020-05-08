 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright: (c) Washington University, School of Medicine 2004</p>
  *<p>Company: Washington University, School of Medicine, St. Louis.</p>
  *@author Aarti Sharma
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.util.global;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author aarti_sharma
  *
  */
 public class Variables
 {
 
	public static String dynamicExtensionsHome = "";
 	public static List databaseDefinitions = new ArrayList();	
	public static String databaseDriver = "";
 	public static String[] databasenames;
 	public static boolean containerFlag = true;
	public static String applicationCvsTag = "";
 	public static String hibernateCfgFileName = "hibernate.cfg.xml";
 }
