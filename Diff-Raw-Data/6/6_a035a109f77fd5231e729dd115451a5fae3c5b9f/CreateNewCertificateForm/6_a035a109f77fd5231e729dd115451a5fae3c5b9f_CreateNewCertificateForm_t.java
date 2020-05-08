 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.install.forms;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Locale;
 import java.util.prefs.Preferences;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 import com.adito.boot.ContextHolder;
 import com.adito.boot.KeyStoreManager;
 import com.adito.wizard.AbstractWizardSequence;
 import com.adito.wizard.forms.DefaultWizardForm;
 
 public class CreateNewCertificateForm extends DefaultWizardForm {
     // Statics for sequence attributes
     public final static String ATTR_HOSTNAME = "hostname";
     public final static String ATTR_ORGANISATIONAL_UNIT = "organisationalUnit";
     public final static String ATTR_COMPANY = "company";
     public final static String ATTR_COUNTRY_CODE = "countryCode";
     public final static String ATTR_CITY = "city";
     public final static String ATTR_STATE = "state";
     public static final String ATTR_KEY_STORE_TYPE = "keyStoreType";
 
     // Private instance variables
     
     private String hostname;
     private String organisationalUnit;
     private String company;
     private String countryCode;    
     private String city;    
     private String state;
     private String keyStoreType;
     
     public final static Preferences PREF_NODE = ContextHolder.getContext().getPreferences().node("installation").node("certificate");
         
     public CreateNewCertificateForm() {
         super(true, true, "/WEB-INF/jsp/content/install/createNewCertificate.jspf", 
             "hostname", true, false, "createNewCertificate", 
             "install", "installation.createNewCertificate", 1);
     }
 
     /* (non-Javadoc)
      * @see com.adito.wizard.forms.AbstractWizardForm#init(com.adito.wizard.AbstractWizardSequence)
      */
     public void init(AbstractWizardSequence sequence, HttpServletRequest request) throws Exception {
         String localhostAddress = null;
         hostname = (String)sequence.getAttribute(ATTR_HOSTNAME, PREF_NODE.get("hostname", ""));
         if (hostname.equals("")) {
             try {
                 Enumeration e = NetworkInterface.getNetworkInterfaces();
 
                 while (e.hasMoreElements() && hostname == null) {
                     NetworkInterface netface = (NetworkInterface) e.nextElement();
                     Enumeration e2 = netface.getInetAddresses();
                     while (e2.hasMoreElements() && hostname == null) {
                         InetAddress ip = (InetAddress) e2.nextElement();
                         if (!ip.getCanonicalHostName().equals("localhost")
                                         && !ip.getCanonicalHostName().equals("localhost.localdomain")) {
                             hostname = ip.getCanonicalHostName();
                         } else {
                             localhostAddress = ip.getCanonicalHostName();
                         }
                     }
                 }
             } catch (Exception e) {
             }
             if (hostname.equals("")) {
                 hostname = localhostAddress == null ? "localhost" : localhostAddress;
             }
         }
         countryCode =(String) sequence.getAttribute(ATTR_COUNTRY_CODE, PREF_NODE.get("countryCode", ""));
         if (countryCode.equals("")) {
             countryCode = Locale.getDefault().getCountry();
         }
        organisationalUnit = (String)sequence.getAttribute(ATTR_ORGANISATIONAL_UNIT, PREF_NODE.get("organisationalUnit", ""));
         company = (String)sequence.getAttribute(ATTR_COMPANY, PREF_NODE.get("company", ""));
         city =  (String)sequence.getAttribute(ATTR_CITY, PREF_NODE.get("city", ""));
         state =  (String)sequence.getAttribute(ATTR_STATE, PREF_NODE.get("state", ""));
         keyStoreType = (String)sequence.getAttribute(ATTR_KEY_STORE_TYPE, getAvailableKeyStoreTypes().get(0).toString());
         
     }
 
     public List getAvailableKeyStoreTypes() {
         return KeyStoreManager.getInstance(KeyStoreManager.DEFAULT_KEY_STORE).getSupportedKeyStoreTypes();
     }
     
     public String getKeyStoreType() {
         return keyStoreType;
     }
     
     public void setKeyStoreType(String keyStoreType) {
         this.keyStoreType = keyStoreType;
     }
 
     /* (non-Javadoc)
      * @see com.adito.wizard.forms.AbstractWizardForm#apply(com.adito.wizard.AbstractWizardSequence)
      */
     public void apply(AbstractWizardSequence sequence) throws Exception {  
         sequence.putAttribute(ATTR_CITY, city);       
         sequence.putAttribute(ATTR_COMPANY, company);       
         sequence.putAttribute(ATTR_COUNTRY_CODE, countryCode);       
         sequence.putAttribute(ATTR_HOSTNAME, hostname);       
         sequence.putAttribute(ATTR_ORGANISATIONAL_UNIT, organisationalUnit);       
         sequence.putAttribute(ATTR_STATE, state);       
         sequence.putAttribute(ATTR_KEY_STORE_TYPE, keyStoreType);
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public void setState(String state) {
         this.state = state;
     }
 
     public String getCompany() {
         return company;
     }
 
     public void setCompany(String company) {
         this.company = company;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public String getCity() {
         return city;
     }
 
     public String getState() {
         return state;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public void setHostname(String hostname) {
         this.hostname = hostname;
     }
 
     public String getOrganisationalUnit() {
         return organisationalUnit;
     }
 
     public void setOrganisationalUnit(String organisationalUnit) {
         this.organisationalUnit = organisationalUnit;
     }
     
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errs = new ActionErrors();
         if(isCommiting()) {
             if ("".equals(hostname)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noHost"));
             } 
             // LDP - This is a problem because it stops wildcard certificates from being generated with
             // hostnames such as *.3sp.co.uk
             /*else if (!isValidIpAddress(hostname)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.invalidHost"));
             }*/
 
             if ("".equals(organisationalUnit)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noOrganisationalUnit"));
             }
 
             if ("".equals(company)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noCompany"));
             }
             
             if ("".equals(city)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noCity"));
             }
             
             if ("".equals(state)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noState"));
             }
 
             if ("".equals(countryCode)) {
                 errs.add(Globals.ERROR_KEY, new ActionMessage("installation.createNewCertificate.error.noCountryCode"));
             }
         }
         return errs;
     }
     
     /*private static boolean isValidIpAddress(String ipAddress) {
         try {
             InetAddress.getByName(ipAddress);
             return true;
         } catch (UnknownHostException e) {
             return false;
         }
     }*/
}
