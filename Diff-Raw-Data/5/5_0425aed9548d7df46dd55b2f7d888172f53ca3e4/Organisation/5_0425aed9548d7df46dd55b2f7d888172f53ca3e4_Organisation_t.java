 package nl.nikhef.jgridstart;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import nl.nikhef.jgridstart.gui.util.ErrorMessage;
 
 /** Grid organisations to which a user can be associated.
  * <p>
  * Each user on the grid is bound to an organisation, which is specified in
  * the certificate's subject. This class provides additional information
  * about these organisations.
  * <p>
  * This ad-hoc class parses the organisation configuration file, and queries
  * information from that. The configuration file contains metadata for certificate
  * DNs and addresses of registration authorities. Currently, only user
  * certificates are supported (the {@code cert.users} namespace) and registration
  * authorities ({@code ra} namespace).
  * <p>
  * An organisation is one {@code <id>} in the {@code cert.users.<id>}
  * namespace, from the point of view of the configuration file.
  * <p>
  * The configuration file is loaded using {@link Class#getResource} from
  * {@code /resources/conf/cert_signup.conf}.
  * <p>
  * When an organisation-unit is specified by RDN (which are the relevant
  * {@code O} and {@code OU} parts of the certificate subject), both the
  * organisation unit ({@code OU}) and the organisation ({@code O}) need to
  * be specified. To be able to use one identifier, {@code x-full-rdn} was 
  * introduced, which is a comma-separated list of organisations and
  * organisation units. This is used to refer to an organisation or one of
  * the sub-units, and is fully derivable from a certificate subject.
  * 
  * @author wvengen
  */
 public class Organisation extends Properties {
     
     ///
     /// Static members
     ///
 
     /** global list of all organisations, indexed by configfile index */
     protected static HashMap<String, Organisation> orgIndex = null;
     /** global list of all organisations, indexed by x-full-rdn */
     protected static HashMap<String, Organisation> orgRdn = null;
     /** global list of all registration authorities */
     protected static HashMap<String, RA> ras = null;
     
     /** Get an organisation by identifier.
      * <p>
      * This returns the organisation as read from the configuration file.
      * If the organisation is not found, however, a new {@linkplain Organisation}
      * object is returned which has id and name set to the argument {@code org}.
      * 
      * @param xfullrdn {@code x-full-rdn} identifier
      * @return Organisation, or {@code null} if {@code org} was {@code null} itself.
      */
     public static Organisation get(String xfullrdn) {
 	if (xfullrdn==null) return null;
 	if (orgRdn==null) readAll();
 	Organisation o = orgRdn.get(xfullrdn);
 	if (o==null) {
 	    o = new Organisation(xfullrdn);
 	    String[] parts = xfullrdn.split(",\\s*");
 	    o.setProperty("rdn", parts[parts.length-1].trim());
 	    o.setProperty("x-full-rdn", xfullrdn);
 	    o.setProperty("name", xfullrdn);
 	    o.setProperty("desc", xfullrdn+" (unrecognised organisation)");
 	}
 	return o;
     }
     /** Return the list of organisations */
     public static Organisation[] getAll() {
 	if (orgIndex==null) readAll();
 	return orgIndex.values().toArray(new Organisation[0]);
     }
 
     /** Load the list of organisations from the configuration file */
     protected static void readAll() {
 	// save error handling so that we can load and then show error to avoid
 	//    this being called twice
 	Exception exc = null;
 	String excMsg = null;
 	// load organisations
 	Properties allProps = new Properties();
 	try {
 	    allProps.load(getFile());
 	} catch (IOException e) {
 	    // fallback to internal copy
 	    exc = e;
 	    try {
 		allProps.clear();
 		allProps.load(Organisation.class.getResourceAsStream("/resources/conf/cert_signup.conf"));
 		excMsg = "Could not load organisations from remote location, falling back to default.\n" +
 			"You can probably just continue, but if your organisation is not listed try again later.";
 	    } catch (IOException e2) {
 		excMsg = "Could not load organisations, and fallback failed as well."; 
 	    }
 	}
 	// parse into objects
 	orgIndex = new HashMap<String, Organisation>();
 	orgRdn = new HashMap<String, Organisation>();
 	ras = new HashMap<String, RA>();
 	Enumeration<?> e = allProps.propertyNames();
 	while (e.hasMoreElements()) {
 	    String key = (String)e.nextElement();
 	    String[] keyParts = key.split("\\.");
 	    
 	    if (keyParts.length > 3 && "cert".equals(keyParts[0]) && "users".equals(keyParts[1])) {
 		// organisation
 		String orgid = StringUtils.join(ArrayUtils.subarray(keyParts, 2, keyParts.length-1), '.');
 		if (!orgIndex.containsKey(orgid))
 		    orgIndex.put(orgid, new Organisation(orgid));
 		orgIndex.get(orgid).setProperty(keyParts[keyParts.length-1], allProps.getProperty(key));
 		
 	    } else if (keyParts.length > 2 && "ra".equals(keyParts[0])) {
 		// registration authority
 		String raid = keyParts[1];
 		if (!ras.containsKey(raid))
 		    ras.put(raid, new RA(raid));
 		ras.get(raid).setProperty(keyParts[2], allProps.getProperty(key));
 		
 	    } else {
 		// skip unrecognised entries
 	    }
 	}
 	// since the enumeration above is unsorted, we need to generate
 	// x-full-rdns and the related index separately
 	String[] idxs =  orgIndex.keySet().toArray(new String[0]);
 	Arrays.sort(idxs);
 	for (int i=0; i<idxs.length; i++) {
 	    String orgid = idxs[i];
 	    Organisation org = orgIndex.get(orgid);
 	    String xfullrdn = org.getProperty("rdn");
 	    if (orgid.contains(".")) {
 		String parentid = orgid.substring(0, orgid.lastIndexOf("."));
 		if (orgIndex.containsKey(parentid))
 		    xfullrdn = orgIndex.get(parentid).getProperty("x-full-rdn") + ", " + xfullrdn;		
 	    }
 	    org.setProperty("x-full-rdn", xfullrdn);
 	    orgRdn.put(xfullrdn, org);
 	    // also generate some derived properties
 	    if (org.containsKey("ra")) {
 		String[] r = org.getProperty("ra").split(",\\s*");
 		String sras = "";
 		String srashtml = "";
 		String sranames = "";
 		String sranameshtml = "";
 		for (int j=0; j<r.length; j++) {
 		    RA curra = ras.get(r[j]);
 		    if (curra==null) continue;
 		    sras += curra.getDetails() + ", or ";
 		    srashtml += curra.getDetailsHTML() + ", or ";
 		    sranames += curra.getName() + ", or ";
 		    sranameshtml += curra.getNameHTML() + ", or ";
 		}
 		if (sras.length()>5) org.setProperty("ras", sras.substring(0, sras.length()-5));
 		if (srashtml.length()>5) org.setProperty("ras.html", srashtml.substring(0, srashtml.length()-5));
 		if (sranames.length()>5) org.setProperty("ranames", sranames.substring(0, sranames.length()-5));
 		if (sranameshtml.length()>5) org.setProperty("ranames.html", sranameshtml.substring(0, sranameshtml.length()-5));
 	    }
 	}
 	// handle error
 	if (exc!=null)
 	    ErrorMessage.error(null, "Could not load organisations", exc, excMsg);
     }
     /** Retrieves organisation file
      * <p>
      * Tries to retrieve it from the location specified in the global configuration,
      * falls back to file supplied with distribution. 
      */
     protected static InputStream getFile() throws IOException {
 	if (System.getProperty("jgridstart.org.href")!=null) {
 	    URL fileurl = null;
 	    URL baseurl = null;
 	    try {
 		// Use reflection to call jnlp services to make it work without java web start too
 		//BasicService basic = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
 		Class<?> serviceManagerCls = Class.forName("javax.jnlp.ServiceManager");
 		Method lookup = serviceManagerCls.getDeclaredMethod("lookup", new Class[]{ String.class });
 		Object basic = lookup.invoke(serviceManagerCls, new Object[]{"javax.jnlp.BasicService"});
 		//URL baseurl = basic.getCodeBase();
 		Class<?> basicCls = Class.forName("javax.jnlp.BasicService");
 		Method getCodeBase = basicCls.getDeclaredMethod("getCodeBase", new Class[]{});
 		baseurl = (URL)getCodeBase.invoke(basic, new Object[]{});
 		fileurl = new URL(baseurl, System.getProperty("jgridstart.org.href"));
 	    } catch(Exception e) {
 		// java web start codebase call failed, use full url instead
 		fileurl = new URL(System.getProperty("jgridstart.org.href"));
 	    }
 	    if (baseurl!=null && !fileurl.toExternalForm().startsWith(baseurl.toExternalForm()))
 	    	throw new IOException("Organisation file must reside on same server as application.");
 	    return fileurl.openStream();
 	}
 	// fallback to local copy
 	return Organisation.class.getResourceAsStream("/resources/conf/cert_signup.conf");
     }
     
     /** Returns the Organisation belonging to a CertificatePair, or {@code null} of not found.
      * <p>
      * When the certificate property {@code org} is present, it returns that. Otherwise it
      * parses the certificate's organisations and returns the most probable one.
      * <p>
      * TODO check property and certificate subject are not out-of-sync
      */
     public static Organisation getFromCertificate(CertificatePair cert) {
 	// from direct property if set
 	if (cert.contains("org"))
 	    return get(cert.getProperty("org"));
 	return _getFromCertificate(cert);
     }
 
     /** Returns the {@linkplain Organisation} belonging to a @{linkplain CertificatePair}.
      * <p>
      * Returns {@code null} if the organisation was not found in the list of known organisations.
      * This version always parses the certificate's organisations.
      */
     private static Organisation _getFromCertificate(CertificatePair cert) {
 	if (orgRdn==null) readAll();
 	// else parse subject: find in certificate
 	// a weight is given for finding the most meaningful organisation
 	String sorg = cert.getProperty("subject.o");
 	String[] sorgs = new String[] {};
 	if (sorg!=null) sorgs = sorg.split(",\\s*");
 	Organisation org = null;
 	int weight = -100;
 	for (int i=0; i<sorgs.length; i++) {
	    String cursorg = sorgs[i].toLowerCase(); // for TERENA certificates
	    if (orgRdn.containsKey(cursorg)) {
		Organisation curorg = get(cursorg);
 		int curweight = 0;
 		// calculate weight
 		if (curorg.getProperty("ra")==null)
 		    curweight -= 10;
 		if (curorg.getProperty("sub")!=null)
 		    curweight += 5;
 		// test if this organisation is a better match
 		if (curweight > weight) {
 		    weight = curweight;
 		    org = curorg;
 		}
 	    }
 	}
 	// find sub-organisation, if any
 	// TODO allow more than one sub-organisation
 	if (org!=null && cert.getProperty("subject.ou")!=null)
 	    return org.getSubUnit(cert.getProperty("subject.ou"));
 
 	return org;
     }
     
     /** Return html fragment with list options for all organisations
      * <p>
      * Returns a list of &lt;option&gt; elements to put in an html select. The {@linkplain  CertificatePair}
      * supplied is verified to exist in the options, or else a new option is added that has
      * no existing organisation from the configuration file. This is needed to be able to
      * support organisations that are not present in the configuration file.
      *
      * @param cert CertificatePair to include organisation from
      * @param signupOnly whether to restrict options to organisations for which one can signup
      */
     public static String getAllOptionsHTML(CertificatePair cert, boolean signupOnly) {
 	// setup variables to detect if certificate organisation is present already
 	boolean hasOrg = false;
 	// add all organisations
 	if (orgIndex==null) readAll();
 	String r = "";
 	// need to sort organisations for proper parenting 
 	String[] orgindices = orgIndex.keySet().toArray(new String[0]);
 	Arrays.sort(orgindices);
 	String curParent = null; // current parent organisation, if any
 	for (int i=0; i<orgindices.length; i++) {
 	    Organisation org = orgIndex.get(orgindices[i]);
 	    // end parent's option group if no child anymore
 	    if (curParent!=null && !orgindices[i].startsWith(curParent+".")){
 		curParent = null;
 		r += "</optgroup>\n";
 	    }
 	    // create option group for parent
 	    if (org.getProperty("sub")!=null) {
 		curParent = org.getProperty("id");
 		r += "<optgroup label=\"" + org.getProperty("desc") + "\">\n";
 	    } else {
 		if (org.getProperty("ra")==null && signupOnly) continue;
 		// add organisation to list
 		r += org.getOptionHTML()+"\n";
 	    }
 	    // detect if we've found this certificate's organisation or not
 	    if (cert!=null && org.getProperty("rdn").equals(cert.getProperty("org")))
 		hasOrg = true;
 	}
 	// final tag, if needed
 	if (curParent!=null) r+= "</optgroup>\n";
 	// create option for non-existent organisation
 	if (cert!=null && !hasOrg) {
 	    Organisation org = get(cert.getProperty("org"));
 	    r = org.getOptionHTML()+"\n" + r;
 	}
 	return r;
     }
     public static String getAllOptionsHTML(CertificatePair cert) {
 	return getAllOptionsHTML(cert, true);
     }
     public static String getAllOptionsHTML(boolean signupOnly) {
 	return getAllOptionsHTML(null, signupOnly);
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
     
     /** Generated properties.
      * <p>
      * Each property ending with {@code .full} becomes a comma-separated
      * list of the key (without {@code .full}) for its parents up to itself.
      */
     @Override
     public String getProperty(String key) {
 	if (key.endsWith(".full")) {
 	    key = key.substring(0, key.length()-5);
 	    if (getParent()!=null)
 		return getParent().getProperty(key) + ", " + super.getProperty(key);
 	}
 	if (key.equals("lookupurl"))
 	    return getLookupUrl();
 	return super.getProperty(key);
     }
     
     /** Returns the organisation's description in html */
     public String getDescriptionHTML() throws UnsupportedEncodingException {
 	String r = getProperty("desc");
 	if (r==null) r = getProperty("name");
 	if (getProperty("url")!=null)
 	    r = "<a href='"+getProperty("url")+"'>"+r+"</a>";
 	return r;
     }
     /** Returns the organisation's name in html */
     public String getNameHTML() throws UnsupportedEncodingException {
 	String r = getProperty("name");
 	if (getProperty("url")!=null) {
 	    String title="";
 	    if (getProperty("desc")!=null)
 		title = " title='"+getProperty("desc")+"'";
 	    r = "<a href='"+getProperty("url")+"'"+title+">"+r+"</a>";
 	}
 	return r;
     }    
     /** Returns an html &lt;option&gt; tag for embedding in a &lt;select&gt; tag */
     public String getOptionHTML() {
 	return 
 		"<option value='"+getProperty("x-full-rdn")+"'>" +
 		  getProperty("desc") +
 		"</option>";
     }
     
     /** Returns link to website for doing an RA lookup for this organisation.
      * <p>
      * TODO make this configurable
      */
     public String getLookupUrl() {
 	String url = "https://ca.dutchgrid.nl/request/showra?o=";
 	if (getParent()!=null)
 	    url += getParent().getProperty("rdn") + "&ou=";
 	url += getProperty("rdn");
 	return url;
     }
     
     /** Copy all properties to a {@linkplain Properties} instance.
      * <p>
      * These are volatile attributes since they are bound to an organisation,
      * not a Certificate. */
     public void copyTo(Properties p, String prefix) {
 	// then copy this organisation's properties
 	for (Enumeration<?> en = propertyNames(); en.hasMoreElements(); ) {
 	    String key = (String)en.nextElement();
 	    p.setProperty(prefix+key, getProperty(key));
 	    p.setProperty(prefix+key+".volatile", "true");
 	    // expand generated properties
 	    key += ".full";
 	    p.setProperty(prefix+key, getProperty(key));
 	    p.setProperty(prefix+key+".volatile", "true");
 	}
 	p.setProperty(prefix+"lookupurl", getLookupUrl());
 	p.setProperty(prefix+"lookupurl.volatile", "true");
     }
     
     /** Return a sub-unit for this organisation */
     public Organisation getSubUnit(String ou) {
 	return orgRdn.get(getProperty("x-full-rdn")+", "+ou);
     }
     
     /** Return the parent for this sub-unit, or {@code null} if none. */
     public Organisation getParent() {
 	String orgid = getProperty("x-full-rdn");
 	if (!orgid.contains(",")) return null;
 	String parentid = orgid.substring(0, orgid.lastIndexOf(",")).trim();
 	return get(parentid);
     }
     
     /** Registration authorities associated with grid organisations */
     static public class RA extends Properties {
 	protected RA(String id) {
 	    setProperty("id", id);
 	}
 	public String getName() {
 	    return getProperty("name");
 	}
 	public String getNameHTML() {
 	    if (getProperty("email")!=null)
 		return "<a href='mailto:" + getProperty("email") + "'>" + getName() + "</a>";
 	    return getProperty("name");
 	}
 	public String getDetails() {
 	    String details = "";
 	    details += getName();
 	    if (containsKey("org"))
 	    	details += " (" + orgIndex.get(getProperty("org")).getProperty("name") + ")";
 	    details += ", " + getProperty("address");
 	    return details;
 	}
 	public String getDetailsHTML() {
 	    String details = "";
 	    details += getNameHTML();
 	    if (containsKey("org")) {
 		try { 
 	    		details += " (" + orgIndex.get(getProperty("org")).getNameHTML() + ")";
 		} catch (Exception e) { }
 	    }
 	    details += ", " + getProperty("address");
 	    if (containsKey("address-ll")) {
 		try {
 		    details += "<a href='http://maps.google.com/maps?q=" + 
 			URLEncoder.encode(getProperty("address"), "UTF-8") + "'>" +
 			"&#x279d;</a>";
 		} catch (UnsupportedEncodingException e) { }
 	    }
 	    if (containsKey("phone"))
 		details += ", phone <span class='phone'>" + getProperty("phone") + "</span>";
 	    return details;
 	}
     };
 }
