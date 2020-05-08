 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.workspace;
 
 import static org.oobium.client.Client.client;
 import static org.oobium.utils.CharStreamUtils.find;
 import static org.oobium.utils.CharStreamUtils.findAll;
 import static org.oobium.utils.FileUtils.readFile;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.join;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.literal.Map;
 import static org.oobium.utils.literal.e;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.oobium.client.Client;
 import org.oobium.client.ClientResponse;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.FileUtils;
 import org.oobium.utils.literal;
 import org.oobium.utils.json.IConverter;
 import org.oobium.utils.json.JsonBuilder;
 import org.oobium.utils.json.JsonUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Bundle extends Project {
 
 	public static final String OOBIUM_SERVICE = "Oobium-Service";
 	public static final String OOBIUM_MIGRATION_SERVICE = "Oobium-MigrationService";
 	
 
 	private static String createPath(String name, Version version) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("/bundles/").append(name);
 		if(version != null) {
 			sb.append('_').append(version);
 		}
 		return sb.toString();
 	}
 
 	@Deprecated
 	public static List<Bundle> deploy(String domain, int port, Bundle... bundles) {
 		List<Bundle> deployed = new ArrayList<Bundle>();
 		Client client = new Client(domain, port);
 		for(Bundle bundle : bundles) {
 			ClientResponse response = client.get("/bundles/" + bundle.name);
 			if(response.isSuccess()) {
 				String location = ("file:" + bundle.file.getAbsolutePath());
 				List<Object> list = JsonUtils.toList(response.getBody());
 				if(list.isEmpty()) {
 					response = client.post("/bundles", literal.Map("location", location));
 					if(response.isSuccess()) {
 						deployed.add(bundle);
 						slogger.debug("posted " + location + ": " + response.getStatus());
 					} else {
 						slogger.debug("posted " + location + ": " + response.getStatus() + "\n" + response.getBody());
 						if(response.exceptionThrown()) {
 							slogger.debug(response.getException());
 						}
 					}
 				} else {
 					Map<?, ?> map = (Map<?, ?>) list.get(0);
 					int id = coerce(map.get("id"), int.class);
 					if(!bundle.version.equals(map.get("version"))) {
 						response = client.put("/bundles/" + id, literal.Map("location", location));
 						if(response.isSuccess()) {
 							deployed.add(bundle);
 							slogger.debug("put /bundles/" + id + ": " + response.getStatus());
 						} else {
 							slogger.debug("put /bundles/" + id + ": " + response.getStatus() + "\n" + response.getBody());
 							if(response.exceptionThrown()) {
 								slogger.debug(response.getException());
 							}
 						}
 					} else {
 						slogger.debug("skipping /bundles/" + id);
 					}
 				}
 			}
 		}
 		return deployed;
 	}
 
 	public static boolean install(String domain, int port, Bundle... bundles) {
 		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
 			@Override
 			public Object convert(Object object) {
 				if(object instanceof Bundle) {
 					return "file:" + ((Bundle) object).file.getAbsolutePath();
 				}
 				return object;
 			}
 		});
 		String location = builder.toJson(bundles);
 		ClientResponse response = client(domain, port).post("/bundles", literal.Map("location", location));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("posted " + location + ": " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("posted " + location + ": " + response.getStatus());
 				if(response.hasBody()) {
 					slogger.debug(response.getBody());
 				}
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	public static boolean isDeployed(String domain, int port, Bundle bundle) {
 		return isDeployed(domain, port, bundle.name, bundle.version);
 	}
 
 	public static boolean isDeployed(String domain, int port, String name) {
 		return isDeployed(domain, port, name, null);
 	}
 
 	public static boolean isDeployed(String domain, int port, String name, Version version) {
 		Client client = new Client(domain, port);
 		ClientResponse response = client.get("/bundles/" + name);
 		if(response.isSuccess()) {
 			List<Object> list = JsonUtils.toList(response.getBody());
 			if(!list.isEmpty()) {
 				for(Object o : list) {
 					if(version == null || version.equals(((Map<?, ?>) o).get("version"))) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public static void refresh(String domain, int port) {
 		client(domain, port).post("refresh");
 	}
 
 	public static boolean start(String domain, int port, Bundle bundle) {
 		return start(domain, port, bundle.name, bundle.version);
 	}
 
 	public static boolean start(String domain, int port, List<Bundle> bundles) {
 		return updateState(domain, port, bundles, "32");
 	}
 
 	public static boolean start(String domain, int port, String name) {
 		return start(domain, port, name, null);
 	}
 
 	public static boolean start(String domain, int port, String name, Version version) {
 		return updateState(domain, port, createPath(name, version), "32");
 	}
 
 	public static boolean stop(String domain, int port, Bundle bundle) {
 		return stop(domain, port, bundle.name, bundle.version);
 	}
 
 	public static boolean stop(String domain, int port, List<Bundle> bundles) {
 		return updateState(domain, port, bundles, "4");
 	}
 
 	public static boolean stop(String domain, int port, String name) {
 		return stop(domain, port, name, null);
 	}
 
 	public static boolean stop(String domain, int port, String name, Version version) {
 		return updateState(domain, port, createPath(name, version), "4");
 	}
 
 	public static boolean uninstall(String domain, int port, Bundle... bundles) {
 		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
 			@Override
 			public Object convert(Object object) {
 				if(object instanceof Bundle) {
 					Bundle bundle = (Bundle) object;
 					return bundle.name + "_" + bundle.version;
 				}
 				return object;
 			}
 		});
 		String names = builder.toJson(bundles);
 		ClientResponse response = client(domain, port).delete("/bundles", literal.Map("bundles", names));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete /bundles: " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete /bundles: " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	public static boolean uninstall(String domain, int port, String name) {
 		return uninstall(domain, port, name, null);
 	}
 
 	public static boolean uninstall(String domain, int port, String name, Version version) {
 		String path = createPath(name, version);
 		ClientResponse response = client(domain, port).delete(path);
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete " + path + ": " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete " + path + ": " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	public static boolean uninstall(String domain, int port, String[] names) {
 		ClientResponse response = client(domain, port).delete("/bundles", literal.Map("bundles", JsonUtils.toJson(names)));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete /bundles: " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("delete /bundles: " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	public static boolean update(String domain, int port, Bundle bundle, String location) {
 		String path = "/bundles/" + bundle.name;
 		ClientResponse response = client(domain, port).put(path, literal.Map("location", location));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put " + path + ": " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put " + path + ": " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	private static boolean updateState(String domain, int port, List<Bundle> bundles, String state) {
 		JsonBuilder builder = JsonBuilder.jsonBuilder(new IConverter() {
 			@Override
 			public Object convert(Object object) {
 				if(object instanceof Bundle) {
 					Bundle bundle = (Bundle) object;
 					return bundle.name + "_" + bundle.version;
 				}
 				return object;
 			}
 		});
 		String names = builder.toJson(bundles);
 		ClientResponse response = client(domain, port).put("/bundles", Map(e("state", state), e("bundles", names)));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put /bundles: " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put /bundles: " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	private static boolean updateState(String domain, int port, String path, String state) {
 		ClientResponse response = client(domain, port).put(path, literal.Map("state", state));
 		if(response.isSuccess()) {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put " + path + ": " + response.getStatus());
 			}
 			return true;
 		} else {
 			if(slogger.isLoggingDebug()) {
 				slogger.debug("put " + path + ": " + response.getStatus() + "\n" + response.getBody());
 				if(response.exceptionThrown()) {
 					slogger.debug(response.getException());
 				}
 			}
 			return false;
 		}
 	}
 
 	
 	/**
 	 * This bundle's version, as specified by the manifest header
 	 * <code>Bundle-Version</code>
 	 */
 	public final Version version;
 
 	/**
 	 * A list of bundles that are required by this bundle, as specified by the
 	 * manifest header <code>Require-Bundle</code>.
 	 */
 	public final List<RequiredBundle> requiredBundles;
 
 	/**
 	 * A list of packages that are imported by this bundle, as specified by the
 	 * manifest header <code>Import-Package</code>.
 	 */
 	public final Set<ImportedPackage> importedPackages;
 
 	/**
 	 * A list of packages that are exported by this bundle, as specified by the
 	 * manifest header <code>Export-Package</code>.
 	 */
 	public final Set<ExportedPackage> exportedPackages;
 
 	/**
 	 * True is this bundle is an OSGi framework bundle (exports
 	 * org.osgi.framework)
 	 */
 	private boolean isFramework;
 
 	/**
 	 * this project's activator file, as specified by the manifest header
 	 * <code>Bundle-Activator</code>. Not valid for Fragments.
 	 */
 	public final File activator;
 
 	/**
 	 * a list of services that this bundle registers with the OSGi framework
 	 */
 	private final String[] services;
 
 	Bundle(Type type, File file, Manifest manifest) {
 		super(type, file, manifest);
 		this.version = new Version((String) manifest.getMainAttributes().getValue("Bundle-Version"));
 		this.requiredBundles = parseRequiredBundles(manifest);
 		this.importedPackages = parseImportedPackages(manifest);
 		this.exportedPackages = parseExportedPackages(manifest);
 		this.services = parseServices(manifest);
 		if(isJar) {
 			this.activator = null;
 		} else {
 			this.activator = parseActivator(manifest);
 		}
 	}
 
 	private void addClasspathEntries(Set<String> cpes) {
 		if(classpath != null && classpath.isFile()) {
 			try {
 				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 				Document doc = docBuilder.parse(classpath);
 				NodeList list = doc.getElementsByTagName("classpathentry");
 				for(int i = 0; i < list.getLength(); i++) {
 					Node node = list.item(i);
 					if(node.getNodeType() == Node.ELEMENT_NODE) {
 						Element cpe = (Element) node;
 						String kind = cpe.getAttribute("kind");
 						if("src".equals(kind)) {
 							String path = file.getAbsolutePath() + File.separator + cpe.getAttribute("path");
 							cpes.add(path);
 						}
 					}
 				}
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if(file.isDirectory()) {
 			cpes.add(file.getAbsolutePath() + File.separator + "bin");
 		} else {
 			cpes.add(file.getAbsolutePath());
 		}
 	}
 	
 	/**
 	 * @see #getDependencies()
 	 */
 	protected final void addDependencies(Workspace workspace, Map<Bundle, List<Bundle>> dependencies) {
 		if(requiredBundles != null) {
 			for(RequiredBundle requiredBundle : requiredBundles) {
 				Bundle bundle = workspace.getBundle(requiredBundle);
 				if(bundle != null) {
 					addDependency(dependencies, bundle);
 					bundle.addDependencies(workspace, dependencies);
 				}
 			}
 		}
 		if(importedPackages != null) {
 			for(ImportedPackage importedPackage : importedPackages) {
 				Bundle bundle = workspace.getBundle(importedPackage);
 				if(bundle != null && bundle != this && !dependencies.containsKey(bundle)) {
 					addDependency(dependencies, bundle);
 					bundle.addDependencies(workspace, dependencies);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see #getDependencies(Mode)
 	 */
 	protected void addDependencies(Workspace workspace, Mode mode, Map<Bundle, List<Bundle>> dependencies) {
 		if(requiredBundles != null) {
 			for(RequiredBundle requiredBundle : requiredBundles) {
 				Bundle bundle = workspace.getBundle(requiredBundle);
 				if(bundle != null) {
 					if(bundle.name.startsWith("org.oobium.build")) {
 						System.out.println("   !!!   requiredBundle: " + this);
 					}
 					addDependency(dependencies, bundle);
 					bundle.addDependencies(workspace, mode, dependencies);
 				}
 			}
 		}
 		if(importedPackages != null) {
 			for(ImportedPackage importedPackage : importedPackages) {
 				Bundle bundle = workspace.getBundle(importedPackage);
 				if(bundle != null && bundle != this && !dependencies.containsKey(bundle)) {
 					if(bundle.name.startsWith("org.oobium.build")) {
 						System.out.println("   !!!   Import-Package: " + importedPackage + " in: " + this);
 					}
 					addDependency(dependencies, bundle);
 					bundle.addDependencies(workspace, mode, dependencies);
 				}
 			}
 		}
 	}
 
 	protected void addDependency(Map<Bundle, List<Bundle>> dependencies, Bundle bundle) {
 		List<Bundle> list = dependencies.get(bundle);
 		if(list == null) {
 			list = new ArrayList<Bundle>();
 			dependencies.put(bundle, list);
 		}
 		list.add(this);
 	}
 
 	private void addExportedPackage(String str, Set<ExportedPackage> packages) {
 		ExportedPackage exportedPackage = new ExportedPackage(str);
 		packages.add(exportedPackage);
 		if(exportedPackage.isFramework()) {
 			isFramework = true;
 		}
 	}
 
 	public boolean addExportPackage(String packageName) {
 		String exportStr = "Export-Package: ";
 		StringBuilder sb = readFile(manifest);
 		char[] ca = new char[sb.length()];
 		sb.getChars(0, sb.length(), ca, 0);
 		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
 		if(ix == -1) {
 			ix = findAll(ca, 0, ca.length - 1, "Import-Package".toCharArray());
 			sb.insert(ix, exportStr);
 			sb.insert(ix + exportStr.length(), packageName);
 			sb.insert(ix + exportStr.length() + packageName.length(), "\n");
 		} else {
 			int s1 = ix + exportStr.length();
 			int s2 = s1;
 			while(s2 != -1) {
 				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
 				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
 					break;
 				}
 			}
 			String[] exports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
 			for(String export : exports) {
 				if(packageName.equals(export.trim())) {
 					// already exported so exit without writing anything
 					return false;
 				}
 			}
 			exports = Arrays.copyOf(exports, exports.length + 1);
 			exports[exports.length - 1] = packageName;
 			Arrays.sort(exports);
 			sb.replace(s1, s2, join(exports, ",\n "));
 		}
 
 		ExportedPackage exportedPackage = new ExportedPackage(packageName + ";" + version.toString(true));
 		exportedPackages.add(exportedPackage);
 
 		writeFile(manifest, sb.toString());
 		return true;
 	}
 
 	public boolean addImportPackage(String packageName) {
 		String importStr = "Import-Package: ";
 		StringBuilder sb = readFile(manifest);
 		char[] ca = new char[sb.length()];
 		sb.getChars(0, sb.length(), ca, 0);
 		int ix = findAll(ca, 0, ca.length - 1, importStr.toCharArray());
 		if(ix == -1) {
 			ix = findAll(ca, 0, ca.length - 1, "Export-Package".toCharArray());
 			sb.insert(ix, importStr);
 			sb.insert(ix + importStr.length(), packageName);
 			sb.insert(ix + importStr.length() + packageName.length(), "\n");
 		} else {
 			int s1 = ix + importStr.length();
 			int s2 = s1;
 			while(s2 != -1) {
 				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
 				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
 					break;
 				}
 			}
 			String[] imports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
 			for(String impPkg : imports) {
 				if(packageName.equals(impPkg.trim())) {
 					// already imported so exit without writing anything
 					return false;
 				}
 			}
 			imports = Arrays.copyOf(imports, imports.length + 1);
 			imports[imports.length - 1] = packageName;
 			Arrays.sort(imports);
 			sb.replace(s1, s2, join(imports, ",\n "));
 		}
 
 		ImportedPackage importedPackage = new ImportedPackage(packageName);
 		importedPackages.add(importedPackage);
 
 		writeFile(manifest, sb.toString());
 		return true;
 	}
 
 	@Override
 	public int compareTo(Project o) {
 		int i = name.compareTo(o.name);
 		if(i != 0) {
 			return i;
 		}
 		if(o instanceof Bundle) {
 			return version.compareTo(((Bundle) o).version);
 		} else {
 			return 1;
 		}
 	}
 
 	/**
 	 * Create a Jar file for this bundle.
 	 * @param jar a File object pointing to the Jar file. If the file does not exist, it will be created.
 	 * If the file is a directory, then a new file will be created in the directory with the name: this.name + "_" + version + ".jar"
 	 * @param version
 	 * @throws IOException
 	 */
 	public void createJar(File jar, Version version) throws IOException {
 		if(isJar) {
 			FileUtils.copy(file, jar);
 		} else {
 			Map<String, File> files = getBuildFiles();
 	
 			Manifest manifest = manifest(file);
 			manifest.getMainAttributes().putValue("Bundle-Version", version.toString());
 	
 			if(jar.isDirectory()) {
 				jar = new File(jar, name + "_" + version + ".jar");
 			}
 			FileUtils.createJar(jar, files, manifest);
 		}
 	}
 
 	/**
 	 * Create a Jar file for this bundle containing only the source files of the bundle.
 	 * @param jar a File object pointing to the Jar file. If the file does not exist, it will be created.
 	 * If the file is a directory, then a new file will be created in the directory with the name: this.name + ".source_" + version + ".jar"
 	 * @param version
 	 * @throws IOException
 	 */
 	public void createSourceJar(File jar, Version version) throws IOException {
 		Map<String, File> files = getSourceBuildFiles();
 
 		String sourceName = name + ".source";
 		
 		Manifest manifest = manifest(file);
 		Attributes attrs = manifest.getMainAttributes();
 		
 		String vendor = attrs.getValue("Bundle-Vendor");
		String bname = attrs.getValue("Bundle-Name");
 		
 		attrs.clear();
 		
 		attrs.putValue("Manifest-Version", "1.0");
 		attrs.putValue("Bundle-Vendor", vendor);
 		attrs.putValue("Eclipse-SourceBundle", name + ";version=\"" + version.toString() + "\"");
		attrs.putValue("Bundle-Name", bname);
 		attrs.putValue("Bundle-SymbolicName", sourceName);
 		attrs.putValue("Bundle-Version", version.toString());
 		attrs.putValue("Bundle-ManifestVersion", "2");
 
 		if(jar.isDirectory()) {
 			jar = new File(jar, sourceName + "_" + version + ".jar");
 		}
 		FileUtils.createJar(jar, files, manifest);
 	}
 
 	public boolean hasSource() throws IOException {
 		if(!isJar) {
 			File buildFile = new File(file, "build.properties");
 			if(buildFile.isFile()) {
 				Properties props = new Properties();
 				props.load(new FileReader(buildFile));
 				String prop = props.getProperty("source..");
 				if(prop != null && prop.length() > 0) {
 					return true;
 				} else {
 					prop = props.getProperty("src.includes");
 					if(prop != null && prop.length() > 0) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean exports(String regex) {
 		if(exportedPackages != null) {
 			for(ExportedPackage ep : exportedPackages) {
 				if(ep.name.matches(regex)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean exportsModels() {
 		if(this instanceof Module) {
 			return exportedPackages.contains(new ExportedPackage(name + ".models"));
 		}
 		return false;
 	}
 	
 	public Set<String> getClasspathEntries(Workspace workspace) {
 		Set<String> cpes = new LinkedHashSet<String>();
 		addClasspathEntries(cpes);
 		
 		for(Bundle bundle : getDependencies(workspace).keySet()) {
 			bundle.addClasspathEntries(cpes);
 		}
 
 		return cpes;
 	}
 
 	public Set<String> getClasspathEntries(Workspace workspace, Mode mode) {
 		Set<String> cpes = new LinkedHashSet<String>();
 		addClasspathEntries(cpes);
 		
 		for(Bundle bundle : getDependencies(workspace, mode).keySet()) {
 			bundle.addClasspathEntries(cpes);
 		}
 
 		return cpes;
 	}
 
 	/**
 	 * Get all classpath dependencies (those required to build this particular
 	 * bundle)
 	 * 
 	 * @return a set of bundles that are required to build this bundle
 	 */
 	public final Map<Bundle, List<Bundle>> getDependencies(Workspace workspace) {
 		Map<Bundle, List<Bundle>> dependencies = new TreeMap<Bundle, List<Bundle>>();
 		addDependencies(workspace, dependencies);
 		return dependencies;
 	}
 
 	/**
 	 * Get all dependencies (those required to build and deploy this particular
 	 * bundle - includes configured services)
 	 * 
 	 * @return a set of bundles that are required to build and deploy this
 	 *         bundle
 	 */
 	public Map<Bundle, List<Bundle>> getDependencies(Workspace workspace, Mode mode) {
 		Map<Bundle, List<Bundle>> dependencies = new TreeMap<Bundle, List<Bundle>>();
 		// long start = System.currentTimeMillis();
 		addDependencies(workspace, mode, dependencies);
 		// logger.debug("getDependencies(" + mode + "): " +
 		// (System.currentTimeMillis() - start));
 		return dependencies;
 	}
 
 	public ExportedPackage getExportedPackage(String packageName) {
 		if(exportedPackages != null && packageName != null) {
 			for(ExportedPackage exportedPackage : exportedPackages) {
 				if(exportedPackage.name.equals(packageName)) {
 					return exportedPackage;
 				}
 			}
 		}
 		return null;
 	}
 
 	public Set<ExportedPackage> getExportedPackages() {
 		return (exportedPackages == null) ? new HashSet<ExportedPackage>(0) : new TreeSet<ExportedPackage>(exportedPackages);
 	}
 
 	/**
 	 * Get the full name of this bundle, including the qualifier: "com.test.blog_1.0.0.123"
 	 * @return the full name of this bundle, including the qualifier
 	 */
 	public String getFullName() {
 		return name + "_" + version;
 	}
 
 	public Set<ImportedPackage> getImportedPackages() {
 		return (importedPackages == null) ? new HashSet<ImportedPackage>(0) : new TreeSet<ImportedPackage>(importedPackages);
 	}
 
 	/**
 	 * Get the full name of this bundle without the qualifier: "com.test.blog_1.0.0"
 	 * @return the full name of this bundle
 	 */
 	public String getName() {
 		return name + "_" + version.toString(true);
 	}
 
 	public List<RequiredBundle> getRequiredBundles() {
 		return (requiredBundles == null) ? new ArrayList<RequiredBundle>(0) : new ArrayList<RequiredBundle>(requiredBundles);
 	}
 
 	/**
 	 * Get the full name of the source bundle, including the qualifier, for this bundle: "com.test.blog.source_1.0.0.123.zip"
 	 * @return the full name of the source bundle, including the qualifier
 	 */
 	public String getSourceName() {
 		return name + ".source_" + version;
 	}
 
 	public boolean imports(String regex) {
 		if(importedPackages != null) {
 			for(ImportedPackage ip : importedPackages) {
 				if(ip.name.matches(regex)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether or not this bundle is an OSGi framework bundle, meaning
 	 * that it exports the org.osgi.framework package.
 	 * 
 	 * @return true if this bundle is an OSGi framework bundle; false otherwise.
 	 */
 	public boolean isFramework() {
 		return isFramework;
 	}
 
 	public boolean isService() {
 		return services.length > 0;
 	}
 
 	private File parseActivator(Manifest manifest) {
 		String name = (String) manifest.getMainAttributes().getValue("Bundle-Activator");
 		if(name != null) {
 			int ix = name.lastIndexOf('.');
 			if(ix != -1) {
 				String path = name.substring(0, ix + 1).replace('.', File.separatorChar);
 				name = name.substring(ix + 1) + ".java";
 				return new File(src, path + name);
 			}
 		}
 		return null;
 	}
 
 	private Set<ExportedPackage> parseExportedPackages(Manifest manifest) {
 		Set<ExportedPackage> packages = new TreeSet<ExportedPackage>();
 		String str = (String) manifest.getMainAttributes().getValue("Export-Package");
 		if(str != null && str.trim().length() > 0) {
 
 			int s = 0;
 			for(int i = 0; i < str.length(); i++) {
 				char c = str.charAt(i);
 				if(c == '"') {
 					i++;
 					while(i < str.length()) {
 						if(str.charAt(i) == '"') {
 							break;
 						}
 						i++;
 					}
 					if(i == str.length() - 1) {
 						addExportedPackage(str.substring(s, str.length()).trim(), packages);
 					}
 				} else if(str.charAt(i) == ',') {
 					addExportedPackage(str.substring(s, i).trim(), packages);
 					s = i + 1;
 				} else if(i == str.length() - 1) {
 					addExportedPackage(str.substring(s, str.length()).trim(), packages);
 					s = i + 1;
 				}
 			}
 		}
 		return packages;
 	}
 
 	private Set<ImportedPackage> parseImportedPackages(Manifest manifest) {
 		Set<ImportedPackage> packages = new TreeSet<ImportedPackage>();
 		String str = (String) manifest.getMainAttributes().getValue("Import-Package");
 		if(str != null && str.trim().length() > 0) {
 			int s = 0;
 			for(int i = 0; i < str.length(); i++) {
 				char c = str.charAt(i);
 				if(c == '"') {
 					i++;
 					while(i < str.length()) {
 						if(str.charAt(i) == '"') {
 							break;
 						}
 						i++;
 					}
 					if(i == str.length() - 1) {
 						packages.add(new ImportedPackage(str.substring(s, str.length()).trim()));
 					}
 				} else if(str.charAt(i) == ',') {
 					packages.add(new ImportedPackage(str.substring(s, i).trim()));
 					s = i + 1;
 				} else if(i == str.length() - 1) {
 					packages.add(new ImportedPackage(str.substring(s, str.length()).trim()));
 					s = i + 1;
 				}
 			}
 		}
 		return packages;
 	}
 
 	private List<RequiredBundle> parseRequiredBundles(Manifest manifest) {
 		String str = (String) manifest.getMainAttributes().getValue("Require-Bundle");
 		if(str != null && str.trim().length() > 0) {
 			List<RequiredBundle> bundles = new ArrayList<RequiredBundle>();
 
 			int s = 0;
 			for(int i = 0; i < str.length(); i++) {
 				char c = str.charAt(i);
 				if(c == '"') {
 					i++;
 					while(i < str.length()) {
 						if(str.charAt(i) == '"') {
 							break;
 						}
 						i++;
 					}
 					if(i == str.length() - 1) {
 						bundles.add(new RequiredBundle(str.substring(s, str.length()).trim()));
 					}
 				} else if(str.charAt(i) == ',') {
 					bundles.add(new RequiredBundle(str.substring(s, i).trim()));
 					s = i + 1;
 				} else if(i == str.length() - 1) {
 					bundles.add(new RequiredBundle(str.substring(s, str.length()).trim()));
 				}
 			}
 
 			return Collections.unmodifiableList(bundles);
 		}
 		return null;
 	}
 
 	/**
 	 * @param manifest
 	 * @return an array of service names that this module registers
 	 */
 	private String[] parseServices(Manifest manifest) {
 		String serviceHeader = (String) manifest.getMainAttributes().getValue(OOBIUM_SERVICE);
 		if(serviceHeader != null) {
 			return serviceHeader.split(",");
 		} else {
 			return new String[0];
 		}
 	}
 
 	public boolean removeExportPackage(String packageName) {
 		String exportStr = "Export-Package: ";
 		StringBuilder sb = readFile(manifest);
 		char[] ca = new char[sb.length()];
 		sb.getChars(0, sb.length(), ca, 0);
 		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
 		if(ix == -1) {
 			return false;
 		} else {
 			int s1 = ix + exportStr.length();
 			int s2 = s1;
 			while(s2 != -1) {
 				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
 				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
 					break;
 				}
 			}
 			String[] exports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
 			int foundAt = -1;
 			for(int i = 0; i < exports.length; i++) {
 				if(packageName.equals(exports[i])) {
 					foundAt = i;
 					break;
 				}
 			}
 			if(foundAt == -1) {
 				return false;
 			}
 			String[] tmp = new String[exports.length - 1];
 			System.arraycopy(exports, 0, tmp, 0, foundAt);
 			System.arraycopy(exports, foundAt + 1, tmp, foundAt, tmp.length - foundAt);
 			exports = tmp;
 			Arrays.sort(exports);
 			sb.replace(s1, s2, join(exports, ",\n "));
 		}
 
 		ExportedPackage exportedPackage = new ExportedPackage(packageName + ";" + version.toString(true));
 		exportedPackages.remove(exportedPackage);
 
 		writeFile(manifest, sb.toString());
 		return true;
 	}
 
 	public boolean removeImportPackage(String packageName) {
 		String exportStr = "Import-Package: ";
 		StringBuilder sb = readFile(manifest);
 		char[] ca = new char[sb.length()];
 		sb.getChars(0, sb.length(), ca, 0);
 		int ix = findAll(ca, 0, ca.length - 1, exportStr.toCharArray());
 		if(ix == -1) {
 			return false;
 		} else {
 			int s1 = ix + exportStr.length();
 			int s2 = s1;
 			while(s2 != -1) {
 				s2 = find(ca, '\n', s2 + 1, ca.length - 1);
 				if(s2 != -1 && (s2 >= ca.length - 1 || ca[s2 + 1] != ' ')) {
 					break;
 				}
 			}
 			String[] imports = new String(ca, s1, s2 - s1 + 1).trim().split("\\s*,\\s*");
 			int foundAt = -1;
 			for(int i = 0; i < imports.length; i++) {
 				if(packageName.equals(imports[i])) {
 					foundAt = i;
 					break;
 				}
 			}
 			if(foundAt == -1) {
 				return false;
 			}
 			String[] tmp = new String[imports.length - 1];
 			System.arraycopy(imports, 0, tmp, 0, foundAt);
 			System.arraycopy(imports, foundAt + 1, tmp, foundAt, tmp.length - foundAt);
 			imports = tmp;
 			Arrays.sort(imports);
 			sb.replace(s1, s2, join(imports, ",\n "));
 		}
 
 		ImportedPackage importedPackage = new ImportedPackage(packageName + ";" + version.toString(true));
 		importedPackages.remove(importedPackage);
 
 		writeFile(manifest, sb.toString());
 		return true;
 	}
 
 	public boolean requires(String regex) {
 		if(requiredBundles != null) {
 			for(RequiredBundle rb : requiredBundles) {
 				if(rb.name.matches(regex)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean resolves(ImportedPackage importedPackage) {
 		if(exportedPackages != null) {
 			for(ExportedPackage exportedPackage : exportedPackages) {
 				if(exportedPackage.resolves(importedPackage)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean resolves(RequiredBundle requiredBundle) {
 		return name.equals(requiredBundle.name) && version.resolves(requiredBundle.versionRange);
 	}
 
 	@Override
 	public String toString() {
 		return toString(null);
 	}
 
 	public String toString(Date date) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(type).append(':').append(' ').append(name).append('_');
 		if(date == null) {
 			sb.append(version);
 		} else {
 			sb.append(version.resolve(date));
 		}
 		if(isJar) {
 			sb.append(" (jarred)");
 		}
 		return sb.toString();
 	}
 
 }
