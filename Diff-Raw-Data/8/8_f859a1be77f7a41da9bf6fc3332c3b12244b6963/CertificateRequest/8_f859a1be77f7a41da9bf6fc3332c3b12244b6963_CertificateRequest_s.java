 package nl.nikhef.jgridstart;
 
 import java.security.InvalidKeyException;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import nl.nikhef.xhtmlrenderer.swing.TemplateDocument;
 
 /** Helper class for requesting a new certificate. */
 public class CertificateRequest {
 
     /** System properties prefix for prefill defaults */
     public static final String defaultsPrefix = "jgridstart.defaults.";
     
     /** Set default properties based on an amount of guessing to aid
      * the user in filling in the form.
      * <p>
      * First properties are copied from its parent, if any. If the parent
      * has no such property, it looks at system properties
      * {@literal jgridstart.defaults.*} and sets defaults from these.
      * If a property already exists, it is not overwritten.
      * <p>
      * The parent is meant for renewing certificates, where most properties
      * need to be copied from the parent certificate, but not all.
      * 
      * @param p Properties to set
      * @param parent Parent Properties to copy from 
      */
     static public void preFillData(Properties p, Properties parent) {
 	// parse parent properties
 	if (parent!=null) {
 	    // just copy most properties
 	    for (Enumeration<?> it = parent.propertyNames(); it.hasMoreElements(); ) {
 		String name = (String)it.nextElement();
 		String value = parent.getProperty(name);
 		// filter out state properties that shouldn't be copied
 		if (name.equals("request.submitted")) continue;
 		if (name.equals("request.processed")) continue;
 		if (name.equals("install.done")) continue;
 		if (name.equals("request.serial")) continue;
 		// copy if unset
 		if (!p.containsKey(name))
 		    p.setProperty(name, value);
 	    }
 	    // this is a renewal
 	    p.setProperty("renewal", Boolean.toString(true));
 	}
 	// read defaults from system properties
 	for (Enumeration<?> it = System.getProperties().propertyNames(); it.hasMoreElements(); ) {
 	    String name = (String)it.nextElement();
 	    String value = System.getProperty(name);
 	    if (name.startsWith(defaultsPrefix) ) {
 		String localName = name.substring(defaultsPrefix.length()+1);
 		if (!p.containsKey(localName))
 		    p.setProperty(name.substring(defaultsPrefix.length()), value);
 	    }
 	}
     }
     /** Set default properties to aid user in filling in the form.
      * <p>
      * This is equal to {@link #preFillData(Properties, Properties) preFillData(Properties, null)}.
      * 
      * @see #preFillData(Properties, Properties)
      * @param p Properties to set
      */
     static public void preFillData(Properties p) {
 	preFillData(p);
     }
     
     /** Complete data entered before creating a certificate signing
      * request.
      * <p>
      * This should be called before CertificateStore.generateRequest()
      * or CertificatePair.generateRequest() is called.
      * <p>
      * Currently assumes that the {@code org} property is a comma-separated
      * list of organisation, organisation-units (if any OUs).
      * 
      * @param p Properties to update
      */
     static public void postFillData(Properties p) {
 	// construct subject
 	String subject = "";
 	if (p.getProperty("level", "").equals("tutorial"))
 	    subject += "O=edgtutorial";
 	else
 	    subject += "O=dutchgrid";
 	if (p.getProperty("level", "").equals("demo"))
 	    subject += ", O=dutch-demo";
 	subject += ", O=users";
 	
 	String[] orgs = p.getProperty("org").split(",\\s*");
 	subject += ", O=" + orgs[0];
 	for (int i=1; i<orgs.length; i++)
 	    subject += ", OU=" + orgs[i];
 	
 	p.setProperty("fullname", p.getProperty("givenname").trim() +
 		" " + p.getProperty("surname").trim());
 	p.setProperty("fullname.lock", "true");
 	subject += ", CN=" + p.getProperty("fullname");;
 	// simulate x-full propery from certificate to generate request
 	p.setProperty("subject", subject);
 	p.setProperty("subject.volatile", "true");
     }
     
     /** Lock fields on which the request is dependent.
      * <p>
      * When the CSR is generated, some fields should not be changed 
      * anymore since these have become part of the request. This
      * method should set all {@code foo.locked} variables so that
      * the fields cannot be edited in {@linkplain TemplateDocument}s. 
      */
     static public void postFillDataLock(Properties p) {
 	p.setProperty("givenname.lock", Boolean.toString(true));
 	p.setProperty("surname.lock", Boolean.toString(true));
 	p.setProperty("subject.lock", Boolean.toString(true));
 	p.setProperty("level.lock", Boolean.toString(true));
 	p.setProperty("org.lock", Boolean.toString(true));
     }
     
     /** Completes fields from certificate.
      * <p>
      * When an external certificate is imported, its DN has to be parsed
      * to get names, level, etc. This is kinda reverse of {#postFillData}.
      */
     static public void completeData(Properties p) {
 	// by default medium level, overriden if O=dutch-demo is present
 	if (p.getProperty("subject")!=null &&
 		p.getProperty("subject").toUpperCase().contains("O=DUTCH-DEMO"))
 	    p.setProperty("level", "demo");
 	else
 	    p.setProperty("level", "medium");
 
 	// name
 	if (p.getProperty("subject.cn")!=null) {
 	    p.setProperty("fullname", p.getProperty("subject.cn"));
 	    p.setProperty("fullname.lock", "true");
 	}
	
	// make sure we have an organisation defined
	// since properties can be copied later, it needs to
	// be explicitely defined to renewals
	p.setProperty("org", p.getProperty("org"));
     }
     
     /** Verifies that password is according to policy.
      * <p>
      * Throws an {@linkplain InvalidKeyException} if the policy is violated.
      * Requirements are configured through the system properties;
      * please see {@literal global.properties}.
      * Properties used:
      * <ul>
      *   <li><tt>jgridstart.password.mode</tt> -
      *       how to apply the password policy. {@literal enforceminlength} is always
      *       checked in strict mode. The value of this property affects all the other
      *       conditions: {@literal enforce} to check them in strict mode,
      *       {@literal complain} to check them in non-strict mode, and 
      *       and {@literal ignore} to ignore them completely. 
      *   <li><tt>jgridstart.password.minlength</tt> -
      *       minimum password length</li>
      *   <li><tt>jgridstart.password.regexp</tt> -
      *       regular expression that should return {@code true}</li>
      *   <li><tt>jgridstart.enforceminlength</tt> -
      *       minimum password length that is always enforced in strict mode</li>
      * </ul>
      * If any property is not specified, it is not checked for.
      * 
      * @param strict whether to check for strict requirements (strict mode),
      *        or suggestions only (non-strict mode)  
      */
     static public void validatePassword(String pw, boolean strict) throws InvalidKeyException {
 	if (strict) {
 	    // strict minimum length
 	    String minlen = System.getProperty("jgridstart.password.enforceminlength");
 	    if (minlen==null) minlen="4";
 	    if (pw.length() < Integer.valueOf(minlen))
 		throw new InvalidKeyException("Password must be at least "+minlen+" characters long.");
 	}
 	// all the other checks depend on the mode
 	String mode = System.getProperty("jgridstart.password.mode");
 	if (mode==null || "ignore".equals(mode))
 	    return;
 	if ( (strict && "enforce".equals(mode)) || (!strict && "complain".equals(mode)) ) {
 	    String verb = strict ? "must" : "should";
 	    // minimum length
 	    String minlen = System.getProperty("jgridstart.password.minlength");
 	    if (minlen!=null && pw.length() < Integer.valueOf(minlen))
 		throw new InvalidKeyException("Password "+verb+" be at least "+minlen+" characters long.");
 	    // regular expression
 	    String regex = System.getProperty("jgridstart.password.regexp");
 	    if (regex!=null && !Pattern.matches(regex, pw)) {
 		String msg = System.getProperty("jgridstart.password.explanation");
 		if (msg==null) msg = "Password "+verb+" validate regular expression: "+regex;
 		throw new InvalidKeyException(msg);
 	    }
 	}
 	// TODO throw error/exception if password enforcement policy is none of the three
     }
     /** Verifies that password is according to policy.
      * <p>
      * Accepts {@code char} array as parameter for future compatibility.
      * 
      * @see #validatePassword(String, boolean) */
     static public void validatePassword(char[] pw, boolean strict) throws InvalidKeyException {
 	validatePassword(new String(pw), strict);
     }
     
     /** Verifies that DN (complete or component) is valid.
      * <p>
      * The CP/CPS specified that PRINTABLESTRING should be used, and that
      * quotes should not be used. What characters are allowed explicitly
      * is determined by the property <tt>jgridstart.dnpolicy</tt> which is
      * a regular expression.
      */
     static public void validateDN(String dn) throws Exception {
 	String regex = System.getProperty("jgridstart.dnpolicy.regexp");
 	if (regex!=null && !Pattern.matches(regex, dn)) {
 	    String msg = System.getProperty("jgridstart.dnpolicy.explanation");
 	    if (msg==null) msg = "\""+dn+"\" does not validate regular expression: "+regex;
 	    throw new Exception(msg);
 	}
     }
     
 }
