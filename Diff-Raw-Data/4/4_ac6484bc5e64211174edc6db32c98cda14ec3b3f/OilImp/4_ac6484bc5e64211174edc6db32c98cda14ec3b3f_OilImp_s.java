 package model;
 
 import java.io.*;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.xml.sax.InputSource;
 import javax.xml.xpath.*;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.awt.Desktop;
 
 import java.security.MessageDigest;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import javax.swing.JOptionPane;
 
 import java.text.DateFormat;
 
 import java.util.Date;
 import javax.swing.JDialog;
 
 import java.awt.event.*;
 
 import java.io.*;
 
 import view.LoginForm;
 
 public class OilImp
 {
     public static enum Ressource
     {
         KEROSIN ("Kerosin"),
         DIESEL ("Diesel"),
         BENZIN ("Benzin");
         
         private String name;
         
         Ressource(String name)
         {
             this.name = name;
         }
         
         public String getName()
         {
             return this.name;
         }
     }
 
     private HttpURLConnection conn = null;
     
     private String sessid = "";
     private String username = "";
     private String password = "";
 
     private int lastGetCode = 0;
     private int lastPostCode = 0;
     
     private int[] fields = {102340, 103601, 104359, 104929};
     private String[] fieldNames = {"Tsrif", "Dnoces", "Driht", "Htrof"};
     
     private int currOilField = 0;
     
     public OilImp()
     {
     
     }
     
     private InputStream httpGET(String in_url, String ... getParams)
     {
         try
         {
             String urlStr = in_url;
             
             for (int t = 0; t < getParams.length; t++)
             {
                 if (t == 0)
                 {
                     urlStr += "?" + getParams[t];
                 }
                 else
                 {
                     urlStr += "&" + getParams[t];
                 }
             }
             
             URL url = new URL(urlStr);
             this.conn = (HttpURLConnection) url.openConnection();
             
             /*
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
             osw.write("m=a.b");
             */            
             //System.out.println(urlStr);
             
             conn.connect();
             
             
             this.lastGetCode = conn.getResponseCode();
             InputStream cont = null;
             
             if (lastGetCode / 100 == 2)
             {
                 cont = (InputStream)conn.getContent();
             }
             else
             {
                 cont = (InputStream)conn.getErrorStream();
             }
             return cont;
             /*
             BufferedReader res = new BufferedReader(new InputStreamReader(cont));
             
             LinkedList<String> list = new LinkedList<>();
             
             String line;
             while ( (line = res.readLine()) != null)
             {
                 list.add(line);
             }
             
             String[] content = list.toArray(new String[list.size()]);
             
             return content;
             */
             
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return null;
     }
     
     private InputStream httpPOST(String in_url, String ... getParams)
     {
         try
         {
         
             URL url = new URL(in_url);
             this.conn = (HttpURLConnection) url.openConnection();
             
             
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
             
             String post = "";
             for (int t = 0; t < getParams.length; t++)
             {
                 if (t == 0)
                 {
                     post += getParams[t];
                 }
                 else
                 {
                     post += "&" + getParams[t];
                 }
             }
             
             pw.write(post);
             pw.flush();
             pw.close();
             
             conn.connect();
             
             this.lastPostCode = conn.getResponseCode();
             
             
             InputStream cont = null;
             
             if (lastPostCode / 100 == 2)
             {
                 cont = (InputStream)conn.getContent();
             }
             else
             {
                 cont = (InputStream)conn.getErrorStream();
             }
             return cont;
             /*
             BufferedReader res = new BufferedReader(new InputStreamReader(cont));
             
             LinkedList<String> list = new LinkedList<>();
             
             String line;
             while ( (line = res.readLine()) != null)
             {
                 list.add(line);
             }
             
             String[] content = list.toArray(new String[list.size()]);
             
             return content;
             */
             
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return null;
     }
 
     public void showLoginWindow()
     {
         try
         {
             final Object lock = new Object();
             LoginForm lf = new LoginForm();
             lf.setVisible(true);
             
             lf.addLoginListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent ae)
                 {
                     synchronized (lock)
                     {
                         lock.notify();
                     }
                 }
             });
 
             synchronized (lock)
             {
                 lock.wait();
                 lf.setVisible(false);
                 String[] data = lf.getData();
                 this.username = data[0];
                 this.password = data[1];
                 lf.dispose();
             }
         }
         catch (InterruptedException ie)
         {
             System.err.println(ie.getMessage());
         }
     }
 
     public synchronized void login()
     {
         boolean loggedIn = false;
         int attempts = 0;
         final int MAX_ATTEMPTS = 3;
         
         // DEBUG ---------------------------------------------------------------
         BufferedReader tmpBr = null;
         try 
         {
             String filePath = System.getProperty("user.dir") + "/debug_login.txt";
             
             tmpBr = new BufferedReader(new FileReader(filePath));
             this.username = tmpBr.readLine();
             this.password = tmpBr.readLine();
 
             tmpBr.close();
         }
         catch (IOException ioe)
         {
             System.err.println("No debug login information...asking for it!");
             try
             {
                 tmpBr.close();
             }
             catch (NullPointerException npe) {}
             catch (IOException ioe2)
             {
                 ioe2.printStackTrace();
             }
             finally
             {
                 this.username = "";
                 this.password = "";
             }
         }
         // DEBUG ---------------------------------------------------------------
 
 
 
 
         while (!loggedIn)
         {
             if (attempts == MAX_ATTEMPTS)
             {
                 this.username = "";
                 this.password = "";
                 attempts = 0;
             }
 
             if (this.username.isEmpty() || this.password.isEmpty())
             {
                 this.showLoginWindow();
             }
 
             while (!loggedIn && attempts < MAX_ATTEMPTS)
             {
                 attempts++;
 
                 System.out.println(this.formatOutput("Trying to login... (%d)", attempts));
 
                 InputStream is = this.httpGET("http://www.oilimperium.de/");
 
                 String line = "";
                 String doc = "";
 
                 BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
                 System.out.println("Parsing form...");
 
                 doc = this.responseToString(is);
 
                 Pattern p = Pattern.compile(".*<input type=\"hidden\" name=\"s\" value=\"(.*)\" /></form>.*");
                 Matcher m = p.matcher(doc);
 
 
                 m.matches();
                 String form = m.group(1);
 
 
                 System.out.println("...done!");
 
                 is = this.httpPOST("http://oilimperium.de/index.php",
                                    "name=" + this.username,
                                    "pw=" + this.password,
                                    "m=login",
                                    "B1=Login",
                                    "s=" + form);
 
 
                 doc = "";
 
                 br = new BufferedReader(new InputStreamReader(is));
 
                 try
                 {
                     while ( (line = br.readLine()) != null)
                     {
                         doc += line;
                     }
                 }
                 catch (IOException ioe)
                 {
                     ioe.printStackTrace();
                 }
 
 
                 Pattern p5 = Pattern.compile("[a-z0-9]{40}");
                 Matcher m5 = p5.matcher(doc);
 
                 if (m5.find())
                 {
                     this.sessid = m5.group();
                     System.out.println("Sessid: " + this.sessid);
                 }
 
                 if (!this.sessidExpired(doc))
                 {
                     System.out.println(this.formatOutput("...successfully logged in!"));
                     loggedIn = true;
                 }
                 else
                 {
                     System.out.println(this.formatOutput("...login failed!"));
                 }
             }
         }
     }
 
     public synchronized int[] checkPrices()
     {
         String menu = "m=0xWE01";
         
         System.out.println("Sessid: " + this.sessid);
         
         String doc = "";
         String line = "";
         
         InputStream is = this.httpGET("http://s1.oilimperium.de/index.php",
                                   "sid=" + this.sessid,
                                   menu);
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
                 
         doc = this.responseToString(is);
         
         
         Pattern p1 = Pattern.compile("\\('Roh&ouml;l'\\);\" onmouseout=\"oil.infotexte\\(0\\);\">(.*)</div><div class=\"footer_marketprices_info kerosene\"");
         Pattern p2 = Pattern.compile("\\('Kerosin'\\);\" onmouseout=\"oil.infotexte\\(0\\);\">(.*)</div><div class=\"footer_marketprices_info diesel\"");
         Pattern p3 = Pattern.compile("\\('Diesel'\\);\" onmouseout=\"oil.infotexte\\(0\\);\">(.*)</div><div class=\"footer_marketprices_info gasoline\"");
         Pattern p4 = Pattern.compile("\\('Benzin'\\);\" onmouseout=\"oil.infotexte\\(0\\);\">(.*)</div><div class=\"footer_button tutorial\"");
         
         Matcher m1 = p1.matcher(doc);
         Matcher m2 = p2.matcher(doc);
         Matcher m3 = p3.matcher(doc);
         Matcher m4 = p4.matcher(doc);
         
         int rohoel = 0, kerosin = 0, diesel = 0, benzin = 0;
         
         if (m1.find())
         {
             rohoel = new Integer(m1.group(1));
         }
         if (m2.find())
         {
             kerosin = new Integer(m2.group(1));
         }
         if (m3.find())
         {
             diesel = new Integer(m3.group(1));
         }
         if (m4.find())
         {
             benzin = new Integer(m4.group(1));
         }
         
         //System.out.printf("Rohoel: %d, Kerosin: %d, Diesel: %d, Benzin: %d\n", rohoel, kerosin, diesel, benzin);
         
         int[] prices = {rohoel, kerosin, diesel, benzin};
         if (rohoel == 0 && kerosin == 0 && diesel == 0 && benzin == 0)
         {
             prices = null;
         }
         return prices;
     }
 
 
 
 
     public synchronized String[][] getFactory()
     {
         return this.getFactory(this.currOilField);
     }
         
     public synchronized String[][] getFactory(int oilFieldNr)
     {
     
         boolean success = this.changeOilField(oilFieldNr);
         String[][] factory = new String[9][2];
 
         if (success)
         {
 
             System.out.println(this.formatOutput("Getting factory information on oil field %d", this.currOilField));
 
             boolean expired = true;
             String doc = "";
             
 
             while (expired)
             {
                 String m = "0xSA09";
 
                 InputStream is = this.httpGET("http://s1.oilimperium.de/index.php",
                                           "sid=" + this.sessid,
                                           "m=" + m,
                                           "jnk=" + System.currentTimeMillis());
 
 
                 doc = this.responseToString(is);
 
 
                 if (this.sessidExpired(doc))
                 {
                     this.login();
                 }
                 else
                 {
                     expired = false;
                     //System.err.println(doc);
                 }
             }
 
             String[] products = {"Bohrturm A",
                                  "Bohrturm B",
                                  "Bohrturm C",
                                  "Tank A",
                                  "Tank B",
                                  "Tank C",
                                  "Pipeline A",
                                  "Pipeline B",
                                  "Pipeline C"};
 
             System.out.println("Parsing document...");
 
             int gesperrt = 0;
 
             for (int t = 0; t < products.length; t++)
             {
                 String prod = products[t];
                 factory[t][0] = prod;
 
                 Pattern p = Pattern.compile(prod + ".*?</tr>");
                 Matcher m = p.matcher(doc);
 
                 if (m.find())
                 {
                     //System.out.println(m.group());
                     //System.out.println("----------------------------------------");
 
                     String part = m.group();
                     if (Pattern.compile(".*herstellen.*").matcher(part).find())
                     {
                         //System.out.printf("%10s: bereit\n", prod);
                         factory[t][1] = "bereit";
                     }
                     else if (Pattern.compile(".*h</td>.*").matcher(part).find())
                     {
                         //System.out.printf("%10s: gesperrt\n", prod);
                         factory[t][1] = "gesperrt";
                         gesperrt++;
                     }
                     else if (Pattern.compile(".*endtime=.*").matcher(part).find())
                     {
                         Matcher m2 = Pattern.compile("endtime=([0-9]*+)&").matcher(part);
                         String readyDate = "";
                         String timeleft = "";
                         if (m2.find())
                         {
                             long offset = new Long(m2.group(1));
                             readyDate = this.getTimestamp(offset * 1000);
                             long hours = offset / 3600;
                             offset -= hours * 3600;
                             long minutes = offset / 60;
                             offset -= minutes * 60;
                             long seconds = offset;
                             timeleft = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                         }
                         factory[t][1] = String.format("in Produktion (%s / %s)\n", readyDate, timeleft);
                     }
                     else
                     {
                         factory[t][1] = "Lager voll";
                     }
                 }
 
             }
 
             if (gesperrt == 9)
             {
                 for (int t = 0; t < factory.length; t++)
                 {
                     factory[t][1] = "Lager voll";
                 }
             }
 
 
             System.out.println("...done!");
             System.out.println(this.formatOutput("Successfully retrieved factory information! "));
         }
         else
         {
             System.err.println("ABORTED INFORMATION RETRIEVAL");
         }
         
         return factory;
         
     }
 
     public synchronized void produceInFactory(int prodNr)
     {
         this.produceInFactory(this.currOilField, prodNr);
     }
 
     public synchronized void produceInFactory(int oilFieldNr, int prodNr)
     {
         
         boolean success = this.changeOilField(oilFieldNr);
 
         if (success)
         {
     
             if (prodNr >= 0 && prodNr < 10)
             {
                 boolean expired = true;
                 String[] products = {"bohr_a", "bohr_b", "bohr_c",
                                      "tank_a", "tank_b", "tank_c",
                                      "pipe_a", "pipe_b", "pipe_c"};
 
                 String[] products2 = {"1", "4", "7", "2", "5", "8", "3", "6", "9"};
 
                 String doc = "";
 
                 while (expired)
                 {
                     InputStream is = this.httpGET("http://s1.oilimperium.de/index.php",
                                               "m=0xSA09",
                                               "i=" + products2[prodNr],
                                               "sid=" + this.sessid);
 
                     doc = this.responseToString(is);
 
                     if (this.sessidExpired(doc))
                     {
                         this.login();
                     }
                     else
                     {
                         expired = false;
                     }
                 }
             }
         }
     }
     
     public RefineryInformation getRefinery()
     {
         
         Pattern rohoelPattern = Pattern.compile("tankinhalt=(.*?);");
         Pattern kerosinPattern = Pattern.compile("aktuell_tank_kerosin=(.*?);");
         Pattern dieselPattern = Pattern.compile("aktuell_tank_diesel=(.*?);");
         Pattern benzinPattern = Pattern.compile("aktuell_tank_benzin=(.*?);");
         
         Pattern maxKPattern = Pattern.compile("max_tank_kerosin=(.*?);");
         Pattern maxDPattern = Pattern.compile("max_tank_diesel=(.*?);");
         Pattern maxBPattern = Pattern.compile("max_tank_benzin=(.*?);");
         
         Pattern workersPattern = Pattern.compile("arbeiter_frei=(.*?);");
         Pattern maxWPattern = Pattern.compile("arbeiter_ges=(.*?);");
         
        Pattern timeLeftKPattern = Pattern.compile(".*Kerosin:.*?endtime=(.*?)&");
        Pattern timeLeftDPattern = Pattern.compile(".*Diesel:.*?endtime=(.*?)&");
         Pattern timeLeftBPattern = Pattern.compile(".*Benzin:.*?endtime=(.*?)&");
         
         int rohoel = -1;
         int kerosin = -1;
         int diesel = -1;
         int benzin = -1;
         int workers = -1;
         
         int maxK = -1;
         int maxD = -1;
         int maxB = -1;
         int maxW = -1;
         
         int timeLeftK = -1;
         int timeLeftD = -1;
         int timeLeftB = -1;
         
         boolean expired = true;
         
         String doc = "";
 
         while (expired)
         {
             String m = "0xSA07";
 
             InputStream is = this.httpGET("http://s1.oilimperium.de/index.php",
                                       "sid=" + this.sessid,
                                       "m=" + m,
                                       "jnk=" + System.currentTimeMillis());
 
             doc = this.responseToString(is);
 
             if (this.sessidExpired(doc))
             {
                 this.login();
             }
             else
             {
                 expired = false;
             }
         }
         
         Matcher m = rohoelPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 rohoel = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read Rohoel in Refinery");
             }
         }
         
         m = kerosinPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 kerosin = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read Kerosin in Refinery");
             }
         }
         
         m = dieselPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 diesel = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read Diesel in Refinery");
             }
         }
         
         m = benzinPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 benzin = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read Benzin in Refinery");
             }
         }
         
         m = maxKPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 maxK = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read MaxKerosin in Refinery");
             }
         }
         
         m = maxDPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 maxD = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read MaxDiesel in Refinery");
             }
         }
         
         m = maxBPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 maxB = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read MaxBenzin in Refinery");
             }
         }
         
         m = workersPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 workers = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read Workers in Refinery");
             }
         }
         
         m = maxWPattern.matcher(doc);
         if (m.find())
         {
             try
             {
                 maxW = new Integer(m.group(1));
             }
             catch (NumberFormatException nfe)
             {
                 System.err.println("ERROR: Couldn't read MaxWorkers in Refinery");
             }
         }
         
         
         
         // doc fuer timeLeftPatterns kuerzen
         Pattern doc2Pattern = Pattern.compile("Aktuelle Produktion:(.*?)Aktueller Lagerinhalt");
         Matcher m2 = doc2Pattern.matcher(doc);
         String doc2 = "";
         if (m2.find())
         {
             doc2 = m2.group(1);
         }
         
         m = timeLeftKPattern.matcher(doc2);
         if (m.find())
         {
             timeLeftK = new Integer(m.group(1));
         }
         
         m = timeLeftDPattern.matcher(doc2);
         if (m.find())
         {
             timeLeftD = new Integer(m.group(1));
         }
         
         m = timeLeftBPattern.matcher(doc2);
         if (m.find())
         {
             timeLeftB = new Integer(m.group(1));
         }
         
         RefineryInformation ri = new RefineryInformation(rohoel, kerosin, diesel, benzin, workers,
                                                                  maxK,    maxD,   maxB,   maxW);
         ri.setTimeLeftK(timeLeftK);
         ri.setTimeLeftD(timeLeftD);
         ri.setTimeLeftB(timeLeftB);
         return ri;
     }
     
     public synchronized void produceInRefinery(Ressource ress,
                                                int amount, 
                                                int workerCount)
     {
         String ausbeuteLabel = "Ausbeute: " + ress.getName();
         System.out.println(ausbeuteLabel);
         int ressID = 0;
         
         switch (ress)
         {
             case KEROSIN:
                 ressID = 1;
                 break;
             case DIESEL:
                 ressID = 2;
                 break;
             case BENZIN:
                 ressID = 3;
                 break;
         }
         
         boolean expired = true;
 
 
         String doc;
 
         while (expired)
         {
             InputStream is = this.httpPOST("http://s1.oilimperium.de/index.php",
                                       "m=0xSA07",
                                       
 //                                      "hidden1=100",// + amount,
                                       "hidden2=" + ressID,
 //                                      "R1=V1",
                                       "T1=",
 //                                      "T10=100",
 //                                      "T11=100",
 //                                      "T12=Ausbeute:+Kerosin",
 //                                      "T13=75",
 //                                      "T2=49.661+bbl.",
 //                                      "T3=100+bbl.",
                                       "T4=" + workerCount,
                                       "T5=" + amount,
 //                                      "T6=110+$",
 //                                      "T8=86",
 //                                      "T9=0+bbl.",
 //                                      "x=61",
 //                                      "y=14",
                                       "sid=" + this.sessid);
 
             doc = this.responseToString(is);
             
 //            System.out.println(doc);
 
             if (this.sessidExpired(doc))
             {
                 this.login();
             }
             else
             {
                 expired = false;
                 System.out.println("Successfully producing in refinery");
             }
         }
         
     }
     
 
     public synchronized boolean changeOilField(String oilFieldName)
     {
         int oilFieldNr = -1;
         for (int t = 0; t < this.fieldNames.length; t++)
         {
             if (this.fieldNames[t].equals(oilFieldName))
             {
                 oilFieldNr = t;
                 break;
             }
         }
         boolean success = this.changeOilField(oilFieldNr);
 
         if (!success)
         {
             System.err.println(this.formatOutput(String.format("Oil field name %s does not exist.", oilFieldName)));
         }
         return success;
     }
 
     public synchronized boolean changeOilField(int oilFieldNr)
     {
 
         if (oilFieldNr >= 0 && oilFieldNr < fields.length && oilFieldNr != this.currOilField)
         {
             System.out.println(this.formatOutput("Change to oilField nr %d.", oilFieldNr));
         
             boolean expired = true;
             
             while (expired)
             {
                 InputStream is = this.httpPOST("http://s1.oilimperium.de/index.php?sid=" + this.sessid + "&m=0xZZU1",
                           "sid=" + this.sessid,
                           "m=0xZZU1",
                           "quelle=" + fields[oilFieldNr],
                           "last_side=11111111");
                           
                 String doc = this.responseToString(is);
                 //System.out.println(doc);
                 
                 if (this.sessidExpired(doc))
                 {
                     this.login();
                 }
                 else
                 {
                     expired = false;
                 }
             }
             
             this.currOilField = oilFieldNr;
 
             System.out.println(this.formatOutput("Successfully changed to oil field nr %d.", oilFieldNr));
         }
         else if(oilFieldNr < 0 || oilFieldNr >= fields.length)
         {
             System.err.println(this.formatOutput("Oil field nr %d does not exist.", oilFieldNr));
             return false;
         }
         else
         {
 //            System.out.println(this.formatOutput("Already on oil field nr %d.", oilFieldNr));
         }
         return true;
     }
 
     public String getCurrentOilField()
     {
         return this.fieldNames[this.currOilField];
     }
     
     private String responseToString(InputStream is)
     {
         StringBuilder sb = new StringBuilder();
         String line = "";
         try
         {
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
             while ( (line = br.readLine()) != null)
             {
                 sb.append(line);
             }
         }
         catch (IOException ioe)
         {
             ioe.printStackTrace();
         }
         return sb.toString();
     }
     
     private boolean sessidExpired(String doc)
     {
         Pattern p = Pattern.compile("<td style=\"height:20px; padding-left:45px;\" class=\"txt5\">Pass:</td>");
         
         Matcher m = p.matcher(doc);
         
         boolean expired = false;
         if (m.find() || doc.isEmpty())
         {
             expired = true;
         }
         
         return expired;
     }
     
     private String getTimestamp()
     {
         return this.getTimestamp(0);
     }
     
     private String getTimestamp(long offset)
     {
         String date = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis() + offset));
         return date;
     }
     
     
 //    private NodeList getXPathNodes(String ex, InputStream is)
 //    {
 //        NodeList nodes = null;
 //        try
 //        {
 //            XPath xpath = XPathFactory.newInstance().newXPath();
 //            InputSource inputSource = new InputSource(is);
 //            nodes = (NodeList) xpath.evaluate(ex, inputSource, XPathConstants.NODESET);
 //        }
 //        catch (XPathExpressionException xpee)
 //        {
 //            xpee.printStackTrace();
 //        }
 //        finally
 //        {
 //            return nodes;
 //        }
 //    }
 
     private String formatOutput(String in_s, Object ... args)
     {
         String s = String.format(in_s, args);
 
         final int WIDTH = 80;
         int len = s.length();
         int times = len / WIDTH;
         int rest = WIDTH - (len % WIDTH);
         String finalStr = "";
 
         int t;
         for (t = 0; t < times; t++)
         {
             String part = s.substring(t*WIDTH, (t+1)*WIDTH).trim();
             finalStr += part + "\n";
         }
         finalStr += s.substring(t*WIDTH, len).trim();
 
         for (t = 0; t < rest; t++)
         {
             finalStr += "-";
         }
 
         return finalStr;
     }
     
     public void run()
     {
         //this.getPrices();
         //this.changeOilField(1);
         //this.produceInFactory(0);
         
         for (int t = 0; t < 4; t++)
         {
             //this.produceInFactory(0);
             String[][] factory = this.getFactory(t);
             System.out.println("\n" + this.fieldNames[t] + ":");
             for (int i = 0; i < factory.length; i++)
             {
                 System.out.println(java.util.Arrays.toString(factory[i]));
             }
         }
     }
     
     public static void main(String[] args)
     {
         
         OilImp o = new OilImp();
 //        o.produceInRefinery(Ressource.DIESEL, (int)(140/0.6), 27);
         RefineryInformation ri = o.getRefinery();
         System.err.println(ri.toString());
         
     }
 }
