 package models;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 import play.Logger;
 import play.Play;
 import play.libs.IO;
 import play.vfs.VirtualFile;
 import util.Check;
 import util.Utils;
 import util.XStreamHelper;
 import bundle.BundleException;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.QuickException;
 
 /**
  * A bundle is defined as a collections of server 'service's loadeable/unloadeable at runtime
  *   
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("bundle")
 public class Bundle implements Serializable {
 
	
 	/** declares the name of this bundle */
 	@XStreamAsAttribute
 	public String name;
 	
 	/** declares the version of this bundle */
 	@XStreamAsAttribute
 	public String version;
 
 	/** The bundle description text to be used in informative context */
 	public String description;
 	
 	/** the bundle descriptive label to be used insted of name attribute in informative context */
 	public String title;
 	
 	/** Bundle author */
 	public String author;
 	
 	/** Author contact email */
 	public String email;
 	
 	/** the root path on the file system */
 	@XStreamOmitField
 	public File root;
 	
 	/** the path containing templates */
 	@XStreamOmitField
 	public VirtualFile pagesPath;
 	
 	/** the bin path to be added to PATH env variables */
 	@XStreamOmitField
 	public File binPath;
 	
 	/** the 'bundle.xml' file */
 	@XStreamOmitField
 	public File conf;
 
	public void setConf( File file ) { 
		this.conf = file;
		this.lastModified = Math.max(lastModified, file.lastModified());
	}
	
 	/** the 'bundle.env' (or 'bundle.environment') file */
 	@XStreamOmitField
 	public File envFile;
 
	public void setEnvFile( File file ) { 
		this.envFile = file;
		this.lastModified = Math.max(lastModified, file.lastModified());
	}
	
 	/** the 'bundle.properties' file */
 	@XStreamOmitField
 	public File propFile;
 	
	public void setPropFile( File file ) { 
		this.propFile = file;
		this.lastModified = Math.max(lastModified, file.lastModified());
	}
	
 	@XStreamOmitField
 	public Properties environment;
 	
 	@XStreamOmitField
 	public Properties properties = new Properties();
 
 	@XStreamOmitField
 	public File publicPath;
 
 	@XStreamOmitField
 	public File cssPath;
 	
 	@XStreamOmitField
 	public File javascriptPath;
 	
 	@XStreamOmitField
 	public File navigatorPath;
 	
 	/** bundle hashCode at load time */
 	@XStreamOmitField
 	public int contentHash = 0;
 		
 	/** bundle last modified time */
 	@XStreamOmitField
 	public long lastModified = 0;
 	
 	
 	public Bundle() { 
 		
 	}
 	
 	public Bundle( String name ) {
 		this.name = name;
 	}
 	
 	
 	/**
 	 * Verify that the bundle is complying with naming and structure convetion
 	 * 
 	 */
 	public void verify() {
 		Check.notNull(root, "Bundle root path cannot be null (%s)", name);
 		Check.notEmpty(name, "Bundle name cannot be empty (%s)", root);
 		Check.notEmpty(version, "Bundle version cannot be empty (%s)", name);
 		Check.notNull(conf, "Bundle configuration file cannot be null (%s)", name);
 		Check.notNull(properties, "Bundle properties cannot be null (%s)", name);
 		Check.notNull( services, "Bundle services property cannot be null" );
 	}
 	
 	
 	/** the common {@link Definition} instance */
 	public Definition def;
 	
 	@XStreamImplicit(itemFieldName="service")
 	public List<Service> services = new ArrayList<Service>(); 
 
 	
 //	public boolean equals( Object obj ) { 
 //		Bundle that = Utils.castIfEquals( this, obj );
 //		
 //		return Utils.isEquals( this.name, that.name )
 //			&& Utils.isEquals( this.version, that.version );
 //	}
 //
 //	public int hashCode() { 
 //		int result = Utils.hash();
 //		Utils.hash(result, name);
 //		Utils.hash(result, version);
 //		return result;
 //	}
 //	
 //	public int compareTo( Bundle that ) { 
 //		if( this.equals(that) ) { 
 //			return 0;
 //		}
 //		
 //		return (name + version).compareTo( that.name + that.version ); 
 //	}
 	public Definition getDef() {
 		if( def == null ) {
 			def = new Definition();
 		}
 		return def;
 	}
 	
 	/**
 	 * Look up the {@link Service} instance named <code>name</code>
 	 * 
 	 * @param name the service unique identifier  
 	 * @return the specified instance 
 	 * @throws QuickException if not service is found
 	 */
 	public Service getService(String name) {
 		Check.notEmpty(name,"Argument name cannot be empty");
 		
 		for( Service m : services ) {
 			if( name.equalsIgnoreCase(m.name) ) {
 				return m;
 			}
  		}
 		
 		throw new QuickException("Unable to find service named: '%s'", name);
 	}
 
 	
 	/**
 	 * @return the current configuration file
 	 */
 	public static File getFile() {
 		return Play.getFile("conf/tserver.conf.xml"); 
 	}
 	
 	/**
 	 * Descriptive label, if null fallback on the mandatory name attribute 
 	 * 
 	 */
 	public String getTitle() { 
 		return title != null ? title : name;
 	}
 
 	public List<String> getGroups() {
 		List<String> result = new ArrayList<String>();
 		
 		for( Service m : services ) {
 			String g = (m.group==null) ? "" : m.group.trim(); // <-- normalize the group name;
 			
 			if( !result.contains(g) ) {
 				result.add(g);
 			}
 		}
 		
 		return result;
 	}
 	
 	public List<Service> getServicesByGroup(String group) {
 		Check.notNull(group,"Argument 'group' cannot be null");
 		group = group.trim();
 		List<Service> result = new ArrayList<Service>();
 		
 		for( Service srv : services ) {
 			String g = (srv.group==null) ? "" : srv.group.trim(); // <-- normalize the group name;
 			
 			if( group.equals(g) ) {
 				result.add(srv);
 			}
 		}
 		
 		return result;
 		
 	}
 	
 	public long getLastModified() { 
 		return lastModified;
 	}
 	
 	public void readProperties( File conf ) { 
 		readProperties(conf, Play.id);
 	}
 	
 	void readProperties( File conf, String id ) { 
 
 		Properties result = new Properties();
 		
 		/* add by default all App properties */
 		AppProps props = AppProps.instance();
 		for( String key : props.getNames() ) { 
 			String val = props.get(key,null);
 			if( val != null ) { 
 				result.put(key,val);
 			}
 		}
 
 		/* 
 		 * predefined properties 
 		 */
 		result.put( "application.path", Utils.getCanonicalPath(Play.applicationPath));
 		result.put( "application.mode", Play.configuration.getProperty("application.mode"));
 		result.put( "workspace.path", Utils.getCanonicalPath( AppProps.WORKSPACE_FOLDER ));
 
 		if( root != null ) result.put( "bundle.path", Utils.getCanonicalPath(root) );
 		if( binPath != null ) result.put( "bundle.bin.path", Utils.getCanonicalPath(binPath) );
 		if( name != null ) result.put("bundle.name", name);
 		if( version != null ) result.put("bundle.version", version);
 		if( title != null ) result.put("bundle.title", title);
 		if( email != null ) result.put("bundle.email", email);
 		if( author != null ) result.put("bundle.author", author);
 
 		
 		/* load the properties file if exists */
 		if( conf != null && conf.exists() ) { 
 	        try {
 	            Properties local = IO.readUtf8Properties(new FileInputStream(conf));
 	            
 	            /*
 	             * first load all properties without any prefix 
 	             */
 	            Properties newConfiguration = new Properties();
 	            Pattern pattern = Pattern.compile("^%([a-zA-Z0-9_\\-]+)\\.(.*)$");
 	            for (Object key : local.keySet()) {
 	                Matcher matcher = pattern.matcher(key + "");
 	                if (!matcher.matches()) {
 	                    newConfiguration.put(key, local.get(key).toString().trim());
 	                }
 	            }
 
 	            /*
 	             * then override with all prefixed properties
 	             */
 	            for (Object key : local.keySet()) {
 	                Matcher matcher = pattern.matcher(key + "");
 
 	                if (matcher.matches()) {
 	                    String instance = matcher.group(1);
 	                    if (instance.equals(id)) {
 	                        newConfiguration.put(matcher.group(2), local.get(key).toString().trim());
 	                    }
 	                }
 	            }	            
 	            
 	            /* add the properties and set the file */
 	            result.putAll(newConfiguration);
 	            this.propFile = conf;
 	            	
 	        } 
 	        catch (IOException ex) {
 	            throw new BundleException("Cannot read bundle properties file: '%s'", conf);
 	        }
 		}
         
         
         this.properties = result;
 	}
 		
 
 	
 
 	/**
 	 * Load a bundle on the file system 
 	 * 
 	 * @param root absoulte path on the file system where the bundle is located 
 	 * @return an instance of {@link Bundle}
 	 */
 	public static Bundle read( File path ) { 
 
 	       VirtualFile root = VirtualFile.open(path);
 
 	        /* 
 	         * Each service MUST have a file named 'manifest' declaring 
 	         */
 	        VirtualFile conf = root.child("conf/bundle.xml");
 	        if( !conf.exists() ) { 
 	        	throw new BundleException("Missing 'bundle.xml' file for bundle at path: '%s'", path);
 	        }
 	        
 	        Bundle bundle = XStreamHelper.fromXML(conf.getRealFile());
 	        
 	        /* the the bundle root */
 	        bundle.root = root.getRealFile();
 	        
 	        /* the bundle configuration file */
 	        bundle.conf = conf.getRealFile();
 
 	        /* the bundle stylesheet */
 	        if( root.child("conf/bundle.css").exists() ) { 
 	        	bundle.cssPath = root.child("conf/bundle.css").getRealFile();
 	        }
 
 	        if( root.child("conf/bundle.js").exists() ) { 
 	        	bundle.javascriptPath = root.child("conf/bundle.js").getRealFile();
 	        }
 
 	        if( root.child("conf/bundle.menu.html").exists() ) { 
 	        	bundle.navigatorPath = root.child("conf/bundle.menu.html").getRealFile();
 	        }
 	        
 	        /* 
 	         * load other subdirectories 
 	         */
 	        if (root.child("pages").exists()) {
 	            bundle.pagesPath = root.child("pages");
 	        }
 
 	        /*
 	         * the bundle internal path for binaries 
 	         */
 	        if (root.child("bin").exists()) {
 	            bundle.binPath = root.child("bin").getRealFile();
 	        }
 	        
 	        if( root.child("public").exists()) {
 	        	bundle.publicPath = root.child("public").getRealFile();
 	        }
 	        
 	        /* 
 	         * load properties 
 	         */
 	        bundle.readProperties( root.child("conf/bundle.properties").getRealFile() );
 	        
 	        /* 
 	         * load environment 
 	         */
 	        VirtualFile env = root.child("conf/bundle.environment");
 	        if( !env.exists() ) { 
 	        	env = root.child("conf/bundle.env");
 	        }
 	        if( env.exists() ) { 
 	        	bundle.envFile  = env.getRealFile();
 	        	try {
 					bundle.environment = IO.readUtf8Properties( new FileInputStream(bundle.envFile));
				} 
	        	catch (IOException e) {
 					Logger.warn("Unable to read bundle environment file: '%s'", bundle.envFile);
 				}
 	        }
 	        
 	        
 	        /* garantee that the services list always exists */
 	        if( bundle.services == null ) { 
 	        	bundle.services = new ArrayList<Service>();
 	        }
 	        
 	        /* inject the bundle back reference in the services */
 	        for( Service service : bundle.services ) { 
 	        	service.bundle = bundle;
 	        }
 	        
 	        return bundle;
 		
 	}
 	
 	Element getServiceElement( String serviceName ) { 
 		try {
 			SAXReader reader = new SAXReader();
 			Document doc = reader.read( conf );
 			Element elem = (Element) doc.selectSingleNode(String.format("/bundle/service[@name='%s']", serviceName));
 			return elem;
 		} 
 		catch (DocumentException e) {
         	throw new QuickException(e, "Fail getting XML for service '%s' on bundle '%s'", serviceName, name);
 		}
 	}
 	
 	
 	public String getServiceXML( String serviceName ) { 
 		return getServiceElement(serviceName).asXML();
 	}
 	
 
 }
