 package org.muis.core;
 
 import java.net.URL;
 
 import org.muis.core.style.sheet.StyleSheet;
 
 import prisms.util.ArrayUtils;
 import prisms.util.Sealable.SealedException;
 
 /** Represents a toolkit that contains resources for use in a MUIS document */
 public class MuisToolkit extends java.net.URLClassLoader {
 	/** A toolkit style sheet contains no values itself, but serves as a container to hold all style sheets referred to by the toolkit */
 	public class ToolkitStyleSheet extends org.muis.core.style.sheet.AbstractStyleSheet implements prisms.util.Sealable {
 		@Override
 		public void addDependency(StyleSheet depend) {
 			if(isSealed())
 				throw new SealedException("Cannot modify the styles of a sealed toolkit");
 			if(!theURI.equals(MuisEnvironment.CORE_TOOLKIT) && !(depend instanceof ToolkitStyleSheet)) {
 				for(org.muis.core.style.StyleAttribute<?> attr : depend.allAttrs()) {
 					if(attr.getDomain().getClass().getClassLoader() == MuisToolkit.this)
 						continue;
 					for(org.muis.core.style.StyleExpressionValue<org.muis.core.style.sheet.StateGroupTypeExpression<?>, ?> sev : depend
 						.getExpressions(attr)) {
 						if(sev.getExpression().getType() == null || sev.getExpression().getType().getClassLoader() != MuisToolkit.this) {
 							String msg = "Toolkit " + theURI + ": Style sheet";
 							if(depend instanceof org.muis.core.style.sheet.ParsedStyleSheet)
 								msg += " defined in " + ((org.muis.core.style.sheet.ParsedStyleSheet) depend).getLocation();
 							msg += " assigns styles for non-toolkit attributes to non-toolkit element types";
 							if(!(depend instanceof org.muis.core.style.sheet.MutableStyleSheet))
 								throw new IllegalStateException(msg);
 							else {
 								theEnvironment.msg().error(msg);
 								((org.muis.core.style.sheet.MutableStyleSheet) depend).clear(attr, sev.getExpression());
 							}
 						}
 					}
 				}
 			}
 			super.addDependency(depend);
 		}
 
 		@Override
 		public boolean isSealed() {
 			return MuisToolkit.this.isSealed();
 		}
 
 		@Override
 		public void seal() {
 			throw new IllegalStateException("Toolkit style sheets cannot be sealed--the toolkit seals it");
 		}
 	}
 
 	private boolean isSealed;
 
 	private final MuisEnvironment theEnvironment;
 
 	private final URL theURI;
 
 	private String theName;
 
 	private String theDescription;
 
 	private String theVersion;
 
 	private ClassMapping [] theClassMappings;
 
 	private ResourceMapping [] theResourceMappings;
 
 	private MuisToolkit [] theDependencies;
 
 	private MuisPermission [] thePermissions;
 
 	private ToolkitStyleSheet theStyle;
 
 	/**
 	 * Creates a MUIS toolkit
 	 *
 	 * @param env The environment that the toolkit is for
 	 * @param uri The URI location of the toolkit
 	 */
 	public MuisToolkit(MuisEnvironment env, URL uri) {
 		super(new URL[0]);
 		theEnvironment = env;
 		theURI = uri;
 		theClassMappings = new ClassMapping[0];
 		theDependencies = new MuisToolkit[0];
 		thePermissions = new MuisPermission[0];
 		theStyle = new ToolkitStyleSheet();
 	}
 
 	/** @return The environment that this toolkit is for */
 	public MuisEnvironment getEnvironment() {
 		return theEnvironment;
 	}
 
 	/** @return The URI location of this toolkit */
 	public URL getURI() {
 		return theURI;
 	}
 
 	/** @return This toolkit's name */
 	public String getName() {
 		return theName;
 	}
 
 	/** @param name The name for this toolkit */
 	public void setName(String name) {
 		assertUnsealed();
 		theName = name;
 	}
 
 	/** @return This toolkit's description */
 	public String getDescription() {
 		return theDescription;
 	}
 
 	/** @param descrip The description for this toolkit */
 	public void setDescription(String descrip) {
 		assertUnsealed();
 		theDescription = descrip;
 	}
 
 	/** @return This toolkit's version */
 	public String getVersion() {
 		return theVersion;
 	}
 
 	/** @param version The version for this toolkit */
 	public void setVersion(String version){
 		assertUnsealed();
 		theVersion=version;
 	}
 
 	/** @return The styles for this toolkit */
 	public ToolkitStyleSheet getStyle() {
 		return theStyle;
 	}
 
 	/** @param classPath The class path to add to this toolkit */
 	@Override
 	public void addURL(URL classPath) {
 		assertUnsealed();
 		super.addURL(classPath);
 	}
 
 	/**
 	 * Adds a toolkit dependency for this toolkit
 	 *
 	 * @param toolkit The toolkit that this toolkit requires to perform its functionality
 	 */
 	public void addDependency(MuisToolkit toolkit) {
 		assertUnsealed();
 		theDependencies = ArrayUtils.add(theDependencies, toolkit);
 	}
 
 	/**
 	 * Maps a class to a tag name for this toolkit
 	 *
 	 * @param tagName The tag name to map the class to
 	 * @param className The fully-qualified java class name to map the tag to
 	 */
 	public void map(String tagName, String className) {
 		assertUnsealed();
 		theClassMappings = ArrayUtils.add(theClassMappings, new ClassMapping(tagName, className));
 	}
 
 	/**
 	 * Maps a resource to a tag name for this toolkit
 	 *
 	 * @param tagName The tag name to map the resource to
 	 * @param resourceLocation The location of the resource to map the tag to
 	 */
 	public void mapResource(String tagName, String resourceLocation) {
 		assertUnsealed();
 		theResourceMappings = ArrayUtils.add(theResourceMappings, new ResourceMapping(this, tagName, resourceLocation));
 	}
 
 	/**
 	 * Adds a permission requirement to this toolkit
 	 *
 	 * @param perm The permission that this toolkit requires or requests
 	 * @throws MuisException If this toolkit has been sealed
 	 */
 	public void addPermission(MuisPermission perm) throws MuisException {
 		assertUnsealed();
 		thePermissions = ArrayUtils.add(thePermissions, perm);
 	}
 
 	/** @return Whether this toolkit has been sealed or not */
 	public final boolean isSealed() {
 		return isSealed;
 	}
 
 	/** Seals this toolkit such that it cannot be modified */
 	public final void seal() {
 		for(MuisToolkit dep : theDependencies)
 			theStyle.addDependency(dep.getStyle());
 		isSealed = true;
 	}
 
 	/** @return All tag names mapped to classes in this toolkit (not including dependencies) */
 	public String [] getTagNames() {
 		String [] ret = new String[theClassMappings.length];
 		for(int i = 0; i < theClassMappings.length; i++)
 			ret[i] = theClassMappings[i].getName();
 		return ret;
 	}
 
 	/**
 	 * @param tagName The tag name mapped to the class to get
 	 * @return The class name mapped to the tag name, or null if the tag name has not been mapped in this toolkit
 	 */
 	public String getMappedClass(String tagName) {
 		int sep = tagName.indexOf(':');
 		if(sep >= 0)
 			tagName = tagName.substring(sep + 1);
 		for(ClassMapping cm : theClassMappings)
 			if(cm.getName().equals(tagName))
 				return cm.getClassName();
 		for(MuisToolkit dependency : theDependencies) {
 			String ret = dependency.getMappedClass(tagName);
 			if(ret != null)
 				return ret;
 		}
 		return null;
 	}
 
 	/**
 	 * @param tagName The tag name mapped to the resource to get
 	 * @return The location of the resource mapped to the tag name, or null if the tag name has not been mapped in this toolkit
 	 */
 	public ResourceMapping getMappedResource(String tagName) {
 		int sep = tagName.indexOf(':');
 		if(sep >= 0)
 			tagName = tagName.substring(sep + 1);
 		for(ResourceMapping rm : theResourceMappings)
 			if(rm.getName().equals(tagName))
 				return rm;
 		for(MuisToolkit dependency : theDependencies) {
 			ResourceMapping ret = dependency.getMappedResource(tagName);
 			if(ret != null)
 				return ret;
 		}
 		return null;
 	}
 
 	/**
 	 * Loads a class from its fully-qualified java name and returns it as a implementation of an interface or a subclass of a super class
 	 *
 	 * @param <T> The type of interface or superclass to return the class as
 	 * @param name The fully-qualified java name of the class, not the MUIS-tag name (e.g. "org.muis.core.BlockElement", not "block")
 	 * @param superClass The superclass or interface class to cast the class as an subclass of
 	 * @return The loaded class, as an implementation or subclass of the interface or super class
 	 * @throws MuisException If the class cannot be found, cannot be loaded, or is not an subclass/implementation of the given class or
 	 *             interface
 	 */
 	public <T> Class<? extends T> loadClass(String name, Class<T> superClass) throws MuisException {
 		Class<?> loaded;
 		try {
 			loaded = loadClass(name);
 		} catch(Throwable e) {
 			throw new MuisException("Could not load class " + name, e);
 		}
 		if(superClass == null) // This is acceptable--assume the null superClass arg is cast to
 			return (Class<? extends T>) loaded; // Class<?>
 		try {
 			return loaded.asSubclass(superClass);
 		} catch(ClassCastException e) {
 			throw new MuisException("Class " + loaded.getName() + " is not an instance of " + superClass.getName());
 		}
 	}
 
 	@Override
 	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
 		ClassNotFoundException cnfe;
 		try {
 			return super.loadClass(name, resolve);
 		} catch(ClassNotFoundException e) {
 			cnfe = e;
 		}
 		for(MuisToolkit depend : theDependencies)
 			try {
 				return depend.loadClass(name, resolve);
 			} catch(ClassNotFoundException e) {
 			}
 		throw cnfe;
 	}
 
 	/**
 	 * Attempts to find the given class, returning null the class resource cannot be found
 	 *
 	 * @param name The name of the class to try to find the definition for
 	 * @return The class defined for the given name, if it could be found
 	 */
 	protected Class<?> tryFindClass(String name) {
 		String path = name.replace('.', '/').concat(".class");
 		for(URL url : getURLs()) {
 			String file = url.getFile();
 			if(file.endsWith(".jar"))
 				continue;
 			if(!file.endsWith("/"))
 				file += "/";
 			file += path;
 			URL res;
 			try {
 				res = new URL(url.getProtocol(), url.getHost(), file);
 			} catch(java.net.MalformedURLException e) {
 				System.err.println("Made a malformed URL during findClass!");
 				e.printStackTrace();
 				continue;
 			}
 			if(!checkURL(res))
 				continue;
 			java.io.ByteArrayOutputStream content = new java.io.ByteArrayOutputStream();
 			java.io.InputStream input;
 			try {
 				input = res.openStream();
 				int read = input.read();
 				while(read >= 0) {
 					content.write(read);
 					read = input.read();
 				}
 			} catch(java.io.IOException e) {
 				continue;
 			}
 			return defineClass(name, content.toByteArray(), 0, content.size());
 		}
 		return null;
 	}
 
 	@Override
 	protected Class<?> findClass(String name) throws ClassNotFoundException {
 		Class<?> ret = tryFindClass(name);
 		if(ret != null)
 			return ret;
 		return super.findClass(name);
 	}
 
 	@Override
 	public URL findResource(String name) {
		String path = name.replace('.', '/').concat(".class");
 		for(URL url : getURLs()) {
 			String file = url.getFile();
 			if(file.endsWith(".jar"))
 				continue;
 			if(!file.endsWith("/"))
 				file += "/";
			file += path;
 			URL res;
 			try {
 				res = new URL(url.getProtocol(), url.getHost(), file);
 			} catch(java.net.MalformedURLException e) {
 				System.err.println("Made a malformed URL during findClass!");
 				e.printStackTrace();
 				continue;
 			}
 			if(!checkURL(res))
 				continue;
 			return res;
 		}
 		return super.findResource(name);
 	}
 
 	private boolean checkURL(URL url) {
 		java.net.URLConnection conn;
 		try {
 			conn = url.openConnection();
 			conn.connect();
 		} catch(java.io.IOException e) {
 			return false;
 		}
 		return conn.getLastModified() > 0;
 	}
 
 	/** @return All toolkits that this toolkit depends on */
 	public MuisToolkit [] getDependencies() {
 		return theDependencies.clone();
 	}
 
 	/** @return All permissions that this toolkit requires or requests */
 	public MuisPermission [] getPermissions() {
 		return thePermissions.clone();
 	}
 
 	void assertUnsealed() {
 		if(isSealed)
 			throw new SealedException("Cannot modify a sealed toolkit");
 	}
 }
