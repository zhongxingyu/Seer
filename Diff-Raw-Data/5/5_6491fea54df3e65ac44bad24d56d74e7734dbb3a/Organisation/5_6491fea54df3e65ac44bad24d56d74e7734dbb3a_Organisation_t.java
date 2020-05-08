 package nl.nikhef.jgridstart;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 
 public class Organisation extends Properties {
     
     ///
     /// Static members
     ///
 
     /** global list of all organisations */
     protected static HashMap<String, Organisation> organisations = null;
     
     /** Get an organisation by identifier */
     public static Organisation get(String org) {
 	if (organisations==null) readAll();
 	return organisations.get(org);
     }
     /** Return the list of organisations */
     public static Organisation[] getAll() {
 	if (organisations==null) readAll();
 	return organisations.values().toArray(new Organisation[0]);
     }
     /** Load the list of organisations from the configuration file */
     protected static void readAll() {
 	// load organisations
 	Properties allProps = new Properties();
 	try {
 	    allProps.load(Organisation.class.
 		    getResourceAsStream("/resources/conf/organisations.properties"));
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	// parse into objects
 	organisations = new HashMap<String, Organisation>();
 	Enumeration<Object> e = allProps.keys();
 	while (e.hasMoreElements()) {
 	    String key = (String)e.nextElement();
 	    String[] keyParts = key.split("\\.", 2);
 	    if (!organisations.containsKey(keyParts[0]))
 		organisations.put(keyParts[0], new Organisation(keyParts[0]));
 	    organisations.get(keyParts[0]).setProperty(keyParts[1], allProps.getProperty(key));
 	}
     }
     /** Returns the Organisation belonging to a CertificatePair, or null of not found.
      *
      * TODO check property and certificate subject are not out-of-sync
      */
     public static Organisation getFromCertificate(CertificatePair cert) {
 	// from direct property if set
 	if (cert.getProperty("org")!=null)
 	    return get(cert.getProperty("org"));
 	// else parse subject: find first id in certificate list
 	String[] orgs = cert.getProperty("subject.o").split(",\\s*");
 	for (int i=0; i<orgs.length; i++) {
 	    if (organisations.keySet().contains(orgs[i]))
 		return get(orgs[i]);
 	}
 	// nothing found ...
 	return null;
     }
     
     /** Returns a list of &gt;option&lt; elements to put in an html select. The CertificatePair
      * supplied is verified to exist in the options, or else a new option is added that has
      * no existing organisation from the configuration file. This is needed to be able to
      * support organisations that are not present in the configuration file. */
     public static String getAllOptionsHTML(CertificatePair cert) {
 	// setup variables to detect if certificate organisation is present already
 	boolean hasOrg = false;
 	// add all organisations
 	if (organisations==null) readAll();
 	String r = "";
 	for (Iterator<Organisation> it = organisations.values().iterator(); it.hasNext(); ) {
 	    Organisation org = it.next();
 	    r += org.getOptionHTML()+"\n";
	    if (cert!=null && org.getProperty("id").equals(cert.getProperty("org")))
 		hasOrg = true;
 	}
 	// create option for non-existent organisation
	if (cert!=null && !hasOrg) {
 	    String id = cert.getProperty("org");
 	    Organisation org = new Organisation(id);
 	    org.setProperty("name", id);
 	    org.setProperty("desc", id+" (unrecognised organisation)");
 	    r = org.getOptionHTML()+"\n" + r;
 	}
 	return r;
     }
     public static String getAllOptionsHTML() {
 	return getAllOptionsHTML(null);
     }
 
     ///
     /// Non-static members
     ///
     
     /** Create a new origanisation */
     protected Organisation(String id) {
 	setProperty("id", id);
     }
     
     /** Returns an organisation property. If the key is not found in this organisation,
      * the organisation pointed by "ref" is tried, when available.
      * 
      * This is done instead of using a parent property to allow referencing organisations
      * that are defined later in the configuration file. */
     @Override
     public String getProperty(String key) {
 	String val = super.getProperty(key);
 	if (val==null && containsKey("ref"))
 	    val = organisations.get(getProperty("ref")).getProperty(key);
 	return val;
     }
     
     /** Returns the formatted postal address for this organisation */
     public String getAddress() {
 	String r =
 	    getProperty("ra.name") + "\n" +
 	    getProperty("ra.address").replaceAll(",\\s+", "\n") + "\n";
 	if (getProperty("ra.fax")!=null)
 	    r += "\nfax: " + getProperty("ra.fax") + "\n";
 	return r;
     }
 
     /** Returns the organisation's name in html */
     public String getNameHTML() throws UnsupportedEncodingException {
 	String r = getProperty("desc");
 	if (r==null) r = getProperty("name");;
 	if (getProperty("url")!=null)
 	    r = "<a href='"+URLEncoder.encode(getProperty("url"), "UTF-8")+"'>"+r+"</a>";
 	return r;
     }
     
     /** Returns an html &gt;option&lt; tag for embedding in a &gt;select&lt; tag */
     public String getOptionHTML() {
 	return 
 		"<option value='"+getProperty("id")+"'>" +
 		getProperty("desc")+
 		"</option>";
     }
     
     /** Copy all properties to a Properties instance. These are volatile
      * attributes since they are bound to an organisation, not a Certificate. */
     public void copyTo(Properties p, String prefix) {
 	// copy from parent first
 	if (containsKey("ref"))
 	    organisations.get(getProperty("ref")).copyTo(p, prefix);
 	// then copy this organisation's properties
 	for (Enumeration<Object> en = keys(); en.hasMoreElements(); ) {
 	    String key = (String)en.nextElement();
 	    p.setProperty(prefix+key, getProperty(key));
 	    p.setProperty(prefix+key+".volatile", "true");
 	}
     }
 }
