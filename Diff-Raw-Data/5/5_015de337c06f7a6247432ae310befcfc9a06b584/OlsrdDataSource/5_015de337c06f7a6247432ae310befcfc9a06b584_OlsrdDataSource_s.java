 /* net.relet.freimap.OlsrdDataSource.java
 
   This file is part of the freimap software available at freimap.berlios.de
 
   This software is copyright (c)2007 Thomas Hirsch <thomas hirsch gmail com>
 
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License with
   the Debian GNU/Linux distribution in file /doc/gpl.txt
   if not, write to the Free Software Foundation, Inc., 59 Temple Place,
   Suite 330, Boston, MA 02111-1307 USA
 */
 
 package net.relet.freimap;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.regex.*;
 
 public class OlsrdDataSource implements DataSource {
   DotPluginListener dot;
   DataSourceListener listener;
   TreeMap<Long, Vector<FreiLink>> data = new TreeMap<Long, Vector<FreiLink>>();
   Hashtable<String, FreiNode> knownNodes=new Hashtable<String, FreiNode>();
   Hashtable<String, FreiNode> generatedNodes=new Hashtable<String, FreiNode>();
   
   long lastUpdateTime = 1, 
        firstUpdateTime = 1;
   String nodefile;
   
   MysqlDataSource mysqlSource;
   FreifunkMapDataSource ffmdSource;
 
   DataSource nodeSource;
   String sNodeSource;
        
   public OlsrdDataSource() {
   }
   public void init(HashMap<String, Object> conf) {
     //accept either
     String url = Configurator.getS("url", conf);
     //or
     String host = Configurator.getS("host", conf);
     int port = Configurator.getI("port", conf);
 
     nodefile = Configurator.getS("nodefile", conf);
     //System.out.println("nodefile = "+nodefile);
 
     sNodeSource = Configurator.getS("nodesource", conf);
     int interval = Configurator.getI("interval", conf);
     
     if ((url == null) && (port==-1)) { 
       System.err.println("invalid port parameter "+port);
       System.exit(1);
     }
     if (url == null) {
       dot = new DotPluginListener(host, port, this);
     } else {
       dot = new DotPluginListener(url, this);
     }
     if (interval>0) {
       dot.setInterval(interval);
     }
   }
   
   @SuppressWarnings("unchecked")
   public Vector<FreiNode> getNodeList() {
     if ((nodeSource == null) && (sNodeSource != null)) {
       nodeSource=Visor.sources.get(sNodeSource);
       sNodeSource = null;
     }
 
     if (nodeSource!=null) {
       Vector<FreiNode> nodes = nodeSource.getNodeList();
       for (Enumeration<String> enu = generatedNodes.keys(); enu.hasMoreElements();) {
         nodes.add(generatedNodes.get(enu.nextElement()));
       }
       for (int i=0;i<nodes.size();i++) { 
         knownNodes.put(nodes.elementAt(i).id, nodes.elementAt(i));
       }
       return nodes;
     } else {
       try {
         ObjectInputStream ois=new ObjectInputStream(ClassLoader.getSystemResourceAsStream(nodefile));
         Vector<FreiNode> nodes = (Vector<FreiNode>)ois.readObject();
         ois.close();
         for (int i=0;i<nodes.size();i++) { 
           knownNodes.put(nodes.elementAt(i).id, nodes.elementAt(i));
         }
         return nodes;
       } catch (Exception ex) {
         ex.printStackTrace();
       }
     }
     return null;
   }
 
   public FreiNode getNodeByName(String id) {
     if (nodeSource!=null) {
       FreiNode x = nodeSource.getNodeByName(id);
       if (x!=null) return x;
     }
     FreiNode node= knownNodes.get(id);
     if (node!=null) return node;
     else return generatedNodes.get(id);
   }
   
   public Hashtable<String, Float> getNodeAvailability(long time) {
     if (nodeSource!=null) {
       return nodeSource.getNodeAvailability(time);
     } else {
       return new Hashtable<String, Float>(); //empty
     }
   }
   public long getLastUpdateTime() {
     return lastUpdateTime;
   }
   public long getFirstUpdateTime() {
     return firstUpdateTime;
   }
   public long getLastAvailableTime() {
     return lastUpdateTime;
   }
   public long getFirstAvailableTime() {
     return firstUpdateTime;
   }
 
 
   public long getClosestUpdateTime(long time) {
     long cur=-1, closest = Long.MAX_VALUE;
     Set<Long> keys = data.keySet();
     Iterator<Long> ki = keys.iterator();
     while (ki.hasNext()) {
       cur = ki.next().longValue();
       long d=Math.abs(time-cur);
       if (d<closest) closest=d;
       else break;
     }
     return cur;
   }
   
   public Vector<FreiLink> getLinks(long time) {
     Vector<FreiLink> linkdata = data.get(new Long(time));
     return linkdata;
   }
   //threaded information fetching
   public void addDataSourceListener(DataSourceListener dsl) {
     listener = dsl;
     if (!dot.isAlive()) dot.start();
   }
   //some optional methods
   public void getLinkCountProfile(FreiNode node, NodeInfo info) {
     LinkedList<LinkCount> lcp=new LinkedList<LinkCount>();
     //select HIGH_PRIORITY unix_timestamp(clock) as time, count(*) as num_links from "+TABLE_LINKS+" where dest='"+node.id+"' group by clock"
     Iterator<Long> times=data.keySet().iterator(); 
     while(times.hasNext()) {
       Long time=times.next();
       Vector<FreiLink> links=data.get(time);
       int lc=0;
       for (int i=0; i<links.size(); i++) {
         FreiLink link=links.elementAt(i);
         if (link.to.equals(node)) {
           lc++;
         }
       }
       lcp.add(new LinkCount(time.longValue(), lc));
     }
     info.setLinkCountProfile(lcp);
   }
   public void getLinkProfile(FreiLink mylink, LinkInfo info) {
     LinkedList<LinkData> lp=new LinkedList<LinkData>();
     //select HIGH_PRIORITY unix_timestamp(clock) as time, quality from "+TABLE_LINKS+" where src='"+link.from.id+"' and dest='"+link.to.id+"'");
     Iterator <Long> times=data.keySet().iterator();
     while (times.hasNext()) {
       Long time=times.next();
       Vector<FreiLink> links=data.get(time);
       float quality=0;
       for (int i=0; i<links.size(); i++) {
         FreiLink link=links.elementAt(i);
         if (link.equals(mylink)) {
           quality=link.etx;
         }
       }
       lp.add(new LinkData(time.longValue(), quality));
     }
     info.setLinkProfile(lp);
   }
 
   //private methods
   private void addLinkData(long time, Vector<FreiLink> linkData) {
     data.put(new Long(time), linkData);
     if (firstUpdateTime==1) firstUpdateTime=time;
     lastUpdateTime=time;
     if (listener != null) listener.timeRangeAvailable(firstUpdateTime, lastUpdateTime);
   }
   
   class DotPluginListener extends Thread {
     BufferedReader in;
     String host;
     int port;
     String url = null;
     int interval=10000;
     OlsrdDataSource parent;
 
     Pattern reNode = Pattern.compile("^(\\d+) \\[label=\"(.*?)\",");
     Pattern reLink = Pattern.compile("^(\\d+) -> (\\d+) \\[");
     Pattern reETX  = Pattern.compile("^label=\"(.*?)\"");
     
     public DotPluginListener(String host, int port, OlsrdDataSource parent) {
       this.parent=parent;
       this.host=host;
       this.port=port;
       System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
     }
     public DotPluginListener(String url, OlsrdDataSource parent) {
       this.parent=parent;
       this.url=url;
       System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
     }
     public void setInterval(int i) {
       this.interval = i;
     }
     public int getInterval() {
       return this.interval;
     }
 
     public void run() {
       InetSocketAddress destination = null;
       Vector<FreiLink> linkData = null;
       HashMap<String, FreiNode> nodeIds = null;
       boolean usingWebServiceFormat = false;
       try {
         if (host != null) {
           destination = new InetSocketAddress(host, port);
         }
         while (true) { //reconnect upon disconnection
           if (url != null) {
             System.out.println("Fetching topology from url "+url);
             usingWebServiceFormat = true;
             in =  new BufferedReader(new InputStreamReader(new URL(url).openStream()));
           } else {
             Socket s = new Socket();
 	          //s.setSoTimeout(10000);
             s.connect(destination, 25000);
             in = new BufferedReader(new InputStreamReader(s.getInputStream()));
           }
           String from=null,to=null;
           while (in!=null) {
             String line=in.readLine();
             { //this used to be a try-catch statement
               if (line==null) break;
               line = line.trim();
               if (line.equals("digraph topology")) {
                 if (linkData!=null) parent.addLinkData(System.currentTimeMillis()/1000, linkData);
                 linkData = new Vector<FreiLink>();
                 nodeIds = new HashMap<String, FreiNode>();
               } else if ((usingWebServiceFormat) && (linkData != null)) {  //yeah, some regexp parsing might be more adequate here
                 Matcher m = reNode.matcher(line);
                 if (m.matches()) {
                   String id = m.group(1);
                   String snode = m.group(2);
                   FreiNode node = getNodeByName(snode);
                   if (node == null) {
                     node = new FreiNode(snode);
                     generatedNodes.put(snode, node);
                     if (listener!=null) listener.nodeListUpdate(node);
                   } 
                   nodeIds.put(id, node);
                 } else {
                   m = reLink.matcher(line);
                   if (m.matches()) {
                     from = m.group(1);
                     to   = m.group(2);
                   } else {
                     m=reETX.matcher(line);
                     if (m.matches()) {
                       String setx = m.group(1);
                       FreiNode nfrom = nodeIds.get(from);
                       FreiNode nto   = nodeIds.get(to);
                      float etx = Float.parseFloat(setx);
                       if (nfrom == null) {
                         System.err.println("ERROR: MISSING "+from);
                       }
                       if (nto == null) {
                         System.err.println("ERROR: MISSING TO "+to);
                       }
                       linkData.add(new FreiLink(nfrom, nto, etx, false));
                     }
                   }
                 }
               } else if ((linkData != null) && (line.length()>0) && (line.charAt(0)=='"')) { //using local OLSR graph format
                 StringTokenizer st=new StringTokenizer(line, "\"", false);
                 from = st.nextToken();
 		//if (from.indexOf("/")>-1) { from = from.substring(0, from.indexOf("/")); }
                 st.nextToken();
                 if (st.hasMoreTokens()) { //otherwise it's a gateway node!
                   to = st.nextToken();
    		  //if (to.indexOf("/")>-1) { to = to.substring(0, to.indexOf("/")); }
                   st.nextToken();
                   String setx = st.nextToken();
                   boolean hna = setx.equals("HNA"); 
                  float etx = hna?0:(setx=="INFINITE"?0.0:Float.parseFloat(setx));
                   FreiNode nfrom = getNodeByName(from),
                            nto   = getNodeByName(to);
                   if (nfrom == null) {
                             nfrom = new FreiNode(from);
                             generatedNodes.put(from, nfrom);
                             if (listener!=null) listener.nodeListUpdate(nfrom);
                   }
                   if (nto   == null) {
                             nto = new FreiNode(to);
                             generatedNodes.put(to, nto);
                             if (listener!=null) listener.nodeListUpdate(nto);
                   }
                   System.out.println("adding one.");
                   linkData.add(new FreiLink(nfrom, nto, etx, hna));
                 }
               } 
             }
           }
           Thread.sleep(this.getInterval());
         }
       } catch (SocketTimeoutException ex) {
         System.err.println("[OlsrdDataSource] timeout while trying to connect. "+ex.getMessage());
         return;
       } catch (ConnectException ex) {
         System.err.println("connection to " + host + ":" + port + " failed. Detailed node data won't be available.");
         return;
       } catch (Exception ex) {
         ex.printStackTrace();
       }
     }
   }
 }
