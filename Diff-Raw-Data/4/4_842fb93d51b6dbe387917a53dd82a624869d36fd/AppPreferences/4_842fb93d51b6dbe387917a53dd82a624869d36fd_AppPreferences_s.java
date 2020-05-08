 /*
  * AppPreferences.java
  * Copyright (C) 2005 by:
  *
  *----------------------------
  * cismet GmbH
  * Goebenstrasse 40
  * 66117 Saarbruecken
  * http://www.cismet.de
  *----------------------------
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *----------------------------
  * Author:
  * thorsten.hell@cismet.de
  *----------------------------
  *
  * Created on 16. August 2005, 10:59
  *
  */
 
 package de.cismet.verdis.data;
 
 
 import de.cismet.cismap.commons.preferences.CismapPreferences;
 import de.cismet.cismap.commons.wfsforms.AbstractWFSForm;
 import de.cismet.cismap.commons.wfsforms.WFSFormFactory;
 import de.cismet.ee.EJBAccessor;
 import de.cismet.lagisEE.bean.LagisServerRemote;
 import de.cismet.lagisEE.crossover.LagisCrossoverRemote;
 import de.cismet.tools.ConnectionInfo;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 /**
  *
  * @author thorsten.hell@cismet.de
  */
 public class AppPreferences {
     
     private String version;
     private String environment;
     private String mode;
     
     private String domainserver;
     private int kassenzeichenClassId;
     private int geomClassId;
     private int dmsUrlBaseClassId;
     private int dmsUrlClassId;
     private String kassenzeichenSuche;
     private Vector usergroups=new Vector();
     private Vector rwGroups=new Vector();
     private ConnectionInfo dbConnectionInfo;
     private CismapPreferences cismapPrefs;
     private String standaloneDomainname;
     private String standaloneCallServerHost;
     private String reportUrl="http://s10220:8090/verdis/vorn.pdf?KASSENZEICHEN=";    
     private String albUrl="http://www.cismet.de";
 
     public String getAlbUrl() {
         return albUrl;
     }
 
     public void setAlbUrl(String albUrl) {
         this.albUrl = albUrl;
     }
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private HashMap<String,AbstractWFSForm>  wfsForms=new HashMap<String,AbstractWFSForm>();
     //ADDED FOR RM PLUGIN FUNCTIONALTY 22.07.07 Sebastian Puhl
     private int primaryPort;
     private int secondaryPort;
     private String rmRegistryServerPath;
     private int verdisCrossoverPort;
     private int lagisCrossoverPort;
     private LagisCrossoverRemote lagisCrossoverAccessor;
     private LagisServerRemote lagisServerAccessor;
     private double flurstueckBuffer=-0.5;
 
     /** Creates a new instance of AppPreferences */
     
     
     public AppPreferences(InputStream is) {
         try {
             SAXBuilder builder = new SAXBuilder(false);
             Document doc=builder.build(is);
             
             Element prefs=doc.getRootElement();
             readFromAppPreferences(prefs);
         } catch (Exception e) {
             log.warn("Fehler beim Lesen der Einstellungen (InputStream)", e);
         }
     }
     public AppPreferences(URL url) {
         try {
             SAXBuilder builder = new SAXBuilder(false);
             Document doc=builder.build(url);
             Element prefs=doc.getRootElement();
             readFromAppPreferences(prefs);
         } catch (Exception e) {
             log.warn("Fehler beim Lesen der Einstellungen ("+url.toString()+")", e);
         }
     }
     public AppPreferences(Element appPreferences) {
         readFromAppPreferences(appPreferences);
     }
     private void readFromAppPreferences(Element root) {
         try {
             version=root.getChild("general").getAttribute("version").getValue();
             environment=root.getChild("general").getAttribute("environment").getValue();
             mode=root.getChild("general").getAttribute("mode").getValue();
             domainserver=root.getChild("general").getAttribute("domainserver").getValue();
             kassenzeichenClassId=root.getChild("general").getAttribute("kassenzeichenClassId").getIntValue();
             geomClassId=root.getChild("general").getAttribute("geomClassId").getIntValue();
             dmsUrlBaseClassId=root.getChild("general").getAttribute("dmsUrlBaseClassId").getIntValue();
             dmsUrlClassId=root.getChild("general").getAttribute("dmsUrlClassId").getIntValue();
             kassenzeichenSuche=root.getChild("general").getAttribute("kassenzeichenSuche").getValue();
             standaloneDomainname=root.getChild("standalone").getAttribute("userdomainname").getValue();
             standaloneCallServerHost=root.getChild("standalone").getAttribute("callserverhost").getValue();
             try {
             //Added for RM Plugin functionalty 22.07.2007 Sebastian Puhl
             primaryPort = Integer.parseInt(root.getChild("rmPlugin").getChild("primaryPort").getText());
             } catch (Exception e) {
                 log.debug("Fehler beim parsen des primaryPorts --> benutze default 1099");
                 primaryPort = 1099;                
             }
             
             try {
             secondaryPort = Integer.parseInt(root.getChild("rmPlugin").getChild("secondaryPort").getText());
             } catch (Exception e) {
                 log.debug("Fehler beim parsen des primaryPorts --> benutze default 9001");
                 secondaryPort = 9001;                
             }
             
             try {
             rmRegistryServerPath = root.getChild("rmPlugin").getChild("rmRegistryServer").getText();
             } catch (Exception e) {
                 log.debug("Fehler beim parsen des primaryPorts --> benutze default rmi://localhost:1099/RMRegistryServer");
                 rmRegistryServerPath = "rmi://localhost:1099/RMRegistryServer";                
             }
             
             try {
                 reportUrl=root.getChild("general").getAttribute("reportUrl").getValue();
             } catch (Exception e) {
                 //nix passiert, da mit Standardwert vorbelegt
             }
             
             try  {
                 albUrl=root.getChild("general").getChild("albUrl").getTextTrim();
             }
             catch (Exception e) {
                 log.error("Fehler beim auslesen von albUrl",e);
             }
 
             try  {
                 albUrl=root.getChild("general").getChild("albUrl").getTextTrim();
             }
             catch (Exception e) {
                 log.error("Fehler beim auslesen von albUrl",e);
             }
 
             try {
                 Element crossoverPrefs = root.getChild("CrossoverConfiguration");
                 final String crossoverServerPort = crossoverPrefs.getChildText("ServerPort");
                 log.debug("Crossover: Crossover port: " + crossoverServerPort);
                 setVerdisCrossoverPort(Integer.parseInt(crossoverServerPort));
             } catch (Exception ex) {
                 log.warn("Crossover: Error beim setzen des Server ports", ex);
             }
 
              try {
                 Element crossoverPrefs = root.getChild("CrossoverConfiguration");
                 final String lagisHost = crossoverPrefs.getChild("LagisConfiguration").getChildText("Host");
                 log.debug("Crossover: lagisHost: " + lagisHost);
                 final String lagisORBPort = crossoverPrefs.getChild("LagisConfiguration").getChildText("ORBPort");
                 log.debug("Crossover: lagisHost: " + lagisORBPort);
                 setLagisCrossoverPort(Integer.parseInt(crossoverPrefs.getChild("LagisConfiguration").getChildText("LagisCrossoverPort")));
                 log.debug("Crossover: LagisCrossoverPort: " + getLagisCrossoverPort());
                 lagisCrossoverAccessor = EJBAccessor.createEJBAccessor(lagisHost, lagisORBPort, LagisCrossoverRemote.class).getEjbInterface();
                 lagisServerAccessor = EJBAccessor.createEJBAccessor(lagisHost, lagisORBPort, LagisServerRemote.class).getEjbInterface();
             } catch (Exception ex) {
                 log.warn("Crossover: Error beim setzen des LagIS servers", ex);
             }
             try{
                 Element crossoverPrefs = root.getChild("CrossoverConfiguration");
                 flurstueckBuffer = Double.parseDouble(crossoverPrefs.getChildText("FlurstueckBuffer"));
 
             } catch(Exception ex){
                 log.error("Crossover: Fehler beim setzen den buffers für die Flurstückabfrage",ex);
             }
             
             List list=root.getChild("usergroups").getChildren("ug");
             Iterator it=list.iterator();
             while (it.hasNext()) {
                 Object o=it.next();
                 if (o instanceof Element ) {
                     Element e=(Element)o;
                     usergroups.add(e.getText().toLowerCase());
                     if (((Element)o).getAttribute("rw").getBooleanValue()) {
                         rwGroups.add(e.getText().toLowerCase());
                     }
                 }
             }
             dbConnectionInfo=new ConnectionInfo(root.getChild("dbConnectionInfo"));
             
             cismapPrefs=new CismapPreferences(root.getChild("cismapPreferences"));
             
             try {
                 WFSFormFactory wfsFormFactory=WFSFormFactory.getInstance();
                 wfsFormFactory.masterConfigure(root);
                 wfsForms=wfsFormFactory.getForms();
             } catch (Exception e) {
                 log.warn("Fehler beim Auslesen der WFSFormsProperties");
             }
             
         } catch (Exception e) {
             log.error("Einstellungen konnten nicht gelesen werden",e);
         }
         
     }
 
     public int getVerdisCrossoverPort() {
         return verdisCrossoverPort;
     }
 
     public void setVerdisCrossoverPort(int verdisCrossoverPort) {
         this.verdisCrossoverPort = verdisCrossoverPort;
     }
 
     public int getLagisCrossoverPort() {
         return lagisCrossoverPort;
     }
 
     public void setLagisCrossoverPort(int lagisCrossoverPort) {
         this.lagisCrossoverPort = lagisCrossoverPort;
     }
         
      public LagisCrossoverRemote getLagisCrossoverAccessor() {
         return lagisCrossoverAccessor;
     }
 
     public LagisServerRemote getLagisServerAccessor() {
         return lagisServerAccessor;
     }
     
     //ADDED FOR RM PLUGIN FUNCTIONALTY 22.07.07 Sebastian Puhl
     public int getPrimaryPort(){
         return primaryPort;
     }
     
     public int getSecondaryPort(){
         return secondaryPort;
     }
     
     public String getRmRegistryServerPath(){
         return rmRegistryServerPath;
     }
     
     public String getVersion() {
         return version;
     }
     
     public void setVersion(String version) {
         this.version = version;
     }
     
     public String getEnvironment() {
         return environment;
     }
     
     public void setEnvironment(String environment) {
         this.environment = environment;
     }
     
     public String getMode() {
         return mode;
     }
     
     public void setMode(String mode) {
         this.mode = mode;
     }
     
     public String getDomainserver() {
         return domainserver;
     }
     
     public void setDomainserver(String domainserver) {
         this.domainserver = domainserver;
     }
     
     public int getKassenzeichenClassId() {
         return kassenzeichenClassId;
     }
     
     public void setKassenzeichenClassId(int kassenzeichenClassId) {
         this.kassenzeichenClassId = kassenzeichenClassId;
     }
     
     public int getGeomClassId() {
         return geomClassId;
     }
     
     public void setGeomClassId(int geomClassId) {
         this.geomClassId = geomClassId;
     }
     
     public int getDmsUrlBaseClassId() {
         return dmsUrlBaseClassId;
     }
     
     public void setDmsUrlBaseClassId(int dmsUrlBaseClassId) {
         this.dmsUrlBaseClassId = dmsUrlBaseClassId;
     }
     
     public int getDmsUrlClassId() {
         return dmsUrlClassId;
     }
     
     public void setDmsUrlClassId(int dmsUrlClassId) {
         this.dmsUrlClassId = dmsUrlClassId;
     }
     
     public String getKassenzeichenSuche() {
         return kassenzeichenSuche;
     }
     
     public void setKassenzeichenSuche(String kassenzeichenSuche) {
         this.kassenzeichenSuche = kassenzeichenSuche;
     }
     
     public ConnectionInfo getDbConnectionInfo() {
         return dbConnectionInfo;
     }
     
     public void setDbConnectionInfo(ConnectionInfo dbConnectionInfo) {
         this.dbConnectionInfo = dbConnectionInfo;
     }
     
     public CismapPreferences getCismapPrefs() {
         return cismapPrefs;
     }
     
     public void setCismapPrefs(CismapPreferences cismapPrefs) {
         this.cismapPrefs = cismapPrefs;
     }
     
     public Vector getRwGroups() {
         return rwGroups;
     }
     
     public String getReportUrl() {
         return reportUrl;
     }
     
     public void setReportUrl(String reportUrl) {
         this.reportUrl = reportUrl;
     }
 
     public HashMap<String, AbstractWFSForm> getWfsForms() {
         return wfsForms;
     }
 
     public Vector getUsergroups() {
         return usergroups;
     }
 
     public void setUsergroups(Vector usergroups) {
         this.usergroups = usergroups;
     }
 
     public String getStandaloneDomainname() {
         return standaloneDomainname;
     }
 
     public void setStandaloneDomainname(String standaloneDomainname) {
         this.standaloneDomainname = standaloneDomainname;
     }
 
     public String getStandaloneCallServerHost() {
         return standaloneCallServerHost;
     }
 
     public void setStandaloneCallServerHost(String standaloneCallServerHost) {
         this.standaloneCallServerHost = standaloneCallServerHost;
     }
 
     public double getFlurstueckBuffer() {
         return flurstueckBuffer;
     }
 
     public void setFlurstueckBuffer(double flurstueckBuffer) {
         this.flurstueckBuffer = flurstueckBuffer;
     }
     
 }
