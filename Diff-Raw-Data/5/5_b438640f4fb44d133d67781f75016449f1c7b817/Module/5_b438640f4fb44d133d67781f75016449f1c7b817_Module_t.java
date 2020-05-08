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
 
 import static org.oobium.build.util.ProjectUtils.getGenAnnotations;
 import static org.oobium.build.util.ProjectUtils.getSrcAnnotations;
 import static org.oobium.utils.Config.CACHE;
 import static org.oobium.utils.Config.MODULES;
 import static org.oobium.utils.Config.PERSIST;
 import static org.oobium.utils.Config.SESSION;
 import static org.oobium.utils.DateUtils.httpDate;
 import static org.oobium.utils.FileUtils.findFiles;
 import static org.oobium.utils.FileUtils.readFile;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.camelCase;
 import static org.oobium.utils.StringUtils.tableName;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.jar.Manifest;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.internet.InternetAddress;
 
 import org.oobium.app.MutableAppConfig;
 import org.oobium.app.controllers.HttpController;
 import org.oobium.app.http.Action;
 import org.oobium.app.persist.ModelNotifier;
 import org.oobium.app.server.Websocket;
 import org.oobium.app.views.View;
 import org.oobium.build.esp.ESourceFile;
 import org.oobium.build.gen.ControllerGenerator;
 import org.oobium.build.gen.EFileGenerator;
 import org.oobium.build.gen.Generator;
 import org.oobium.build.gen.MailerGenerator;
 import org.oobium.build.gen.ModelGenerator;
 import org.oobium.build.gen.ProjectGenerator;
 import org.oobium.build.gen.ViewGenerator;
 import org.oobium.build.util.ProjectUtils;
 import org.oobium.build.util.SourceFile;
 import org.oobium.mailer.Mailer;
 import org.oobium.persist.Model;
 import org.oobium.persist.ModelDescription;
 import org.oobium.persist.PersistService;
 import org.oobium.utils.Config;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.DateUtils;
 import org.oobium.utils.FileUtils;
 
 public class Module extends Bundle {
 
 	public static String NATURE_WEBSERVICE = "org.oobium.webservice"; 
 	
 	public static final int
 		MODEL 	 	= 1 << 0,
 		VIEW 		= 1 << 1,
 		CONTROLLER	= 1 << 2,
 		MAILER		= 1 << 3,
 		MANIFEST	= 1 << 4;
 
 
 	private static final FileFilter mailersFilter = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return ProjectUtils.isMailer(file);
 		}
 	};
 
 	private static final FileFilter modelsFilter = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return ProjectUtils.isModel(file);
 		}
 	};
 
 	/**
 	 * this module's main generated source directory
 	 */
 	public final File genMain;
 
 	/**
 	 * this module's configuration file
 	 */
 	public final File config;
 
 	/**
 	 * this module's base application directory (parent of the models, views, and controllers directories)
 	 */
 	public final File app;
 
 	/**
 	 * this module's mailers directory
 	 */
 	public final File mailers;
 
 	/**
 	 * this projecmodule's  directory
 	 */
 	public final File models;
 
 	/**
 	 * this module's observers directory
 	 */
 	public final File observers;
 
 	/**
 	 * this module's notifiers directory
 	 */
 	public final File notifiers;
 
 	/**
 	 * this module's views directory
 	 */
 	public final File views;
 
 	/**
 	 * this module's controllers directory
 	 */
 	public final File controllers;
 
 	/**
 	 * this module's caches directory
 	 */
 	public final File caches;
 	
 	/**
 	 * this module's assets directory
 	 */
 	public final File assets;
 
 	/**
 	 * this module's assetList file
 	 */
 	public final File assetList;
 
 	/**
 	 * this module's generated directory
 	 */
 	public final File generated;
 
 	/**
 	 * this module's migrator bundle (may not actually exist)
 	 */
 	public final File migrator;
 
 	/**
 	 * the name of this module's migrator bundle (may not actually exist)
 	 */
 	public final String migratorName;
 
 	/**
 	 * this module's test suite bundle (may not actually exist)
 	 */
 	public final File testSuite;
 
 	/**
 	 * the name of this module's test suite bundle (may not actually exist)
 	 */
 	public final String testSuiteName;
 	
 	Module(Type type, File file, Manifest manifest) {
 		super(type, file, manifest);
 		
 		this.config = new File(main, "configuration.js");
 
 		Config config = loadConfiguration();
 
 		this.assets = new File(file, "assets");
 		this.generated = new File(file, "generated");
 
 		String base = name.replace('.', File.separatorChar);
 		
 		this.app = file(src, config.getPathToApp(base));
 		
 		this.models = file(src, config.getPathToModels(base));
 		this.notifiers = file(src, config.getPathToNotifiers(base));
 		this.observers = file(src, config.getPathToObservers(base));
 
 		this.views = file(src, config.getPathToViews(base));
 
 		this.controllers = file(src, config.getPathToControllers(base));
 		this.caches = file(src, config.getPathToCaches(base));
 
 		this.mailers = file(src, config.getPathToMailers(base));
 		
 		this.genMain = new File(generated, name.replace('.', File.separatorChar));
 		this.assetList = new File(genMain, "assets.js");
 		
 		this.migratorName = name + ".migrator";
 		this.migrator = new File(file.getParent(), migratorName);
 		
 		this.testSuiteName = name + ".tests";
 		this.testSuite = new File(file.getParent(), testSuiteName);
 	}
 	
 	@Override
 	protected void addDependencies(Workspace workspace, Mode mode, Map<Bundle, List<Bundle>> dependencies) {
 		super.addDependencies(workspace, mode, dependencies);
 		
 		Config configuration = loadConfiguration();
 		
 		addDependency(workspace, mode, configuration.getString(CACHE, mode), dependencies);
 		addDependency(workspace, mode, configuration.getString(SESSION, mode), dependencies);
 		addPersistDependency(workspace, mode, configuration.get(PERSIST, mode), dependencies);
 		addModulesDependency(workspace, mode, configuration.get(MODULES, mode), dependencies);
 	}
 
 	protected void addDependency(Workspace workspace, Mode mode, String fullName, Map<Bundle, List<Bundle>> dependencies) {
 		if(!blank(fullName)) {
 			Bundle bundle = workspace.getBundle(fullName);
 			if(bundle != null) {
 				addDependency(dependencies, bundle);
 				bundle.addDependencies(workspace, mode, dependencies);
 			} else {
 				throw new IllegalStateException(this + " has an unresolved requirement: " + fullName);
 			}
 		}
 	}
 	
 	public boolean addDiscoveryRoute(String path, boolean home) {
 		if(activator != null && activator.isFile()) {
 			String src = FileUtils.readFile(activator).toString();
 			
 			if(!Pattern.compile("router.setDiscovery\\s*\\([^\\)]*\\)\\s*;").matcher(src).find()) {
 				src = src.replaceFirst("public\\s+void\\s+addRoutes\\s*\\(\\s*Config\\s+config\\s*,\\s*(App)?Router\\s+router\\s*\\)\\s*\\{\\s*",
 												"public void addRoutes(Config config, $1Router router) {\n" +
 												"\t\t// auto-generated\n" +
 												"\t\trouter.setDiscovery(\"" + path + "\"" + (home ? ", true" : "" ) + ");\n\n\t\t");
 				FileUtils.writeFile(activator, src);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * @param modelName
 	 * @return true if there were changes made to Application.java; false otherwise
 	 */
 	public boolean addModelRoute(String modelName, Action action) {
 		modelName = adjust(modelName);
 		
 		String oldsrc = FileUtils.readFile(activator).toString();
 		String newsrc = oldsrc;
 
 		Pattern p;
 
 		String regex2 = "router.addResource\\s*\\(\\s*" + modelName + ".class\\s*,\\s*(Action\\.)?(\\w+)\\)\\s*;";
 		p = Pattern.compile(regex2);
 		Matcher m2 = p.matcher(newsrc);
 		boolean singular = m2.find();
 		if(singular) {
 			// check the singular to see if our action already exists - exit if it does (nothing to do)
 			try {
 				if(Action.valueOf(m2.group(2)) == action) {
 					return false; // already routed
 				}
 			} catch(IllegalArgumentException e) {
 				// skip it
 			}
 			// don't add it here - prefer the plural, so check for that first
 		}
 
 		String regex1 = "router.addResources\\s*\\(\\s*" + modelName + ".class\\s*([^\\)]*)\\s*\\)\\s*;";
 		p = Pattern.compile(regex1);
 		Matcher m1 = p.matcher(newsrc);
 		boolean plural = m1.find();
 		if(plural) {
 			if(m1.group(1).length() == 0) {
 				return false; // already routed - no actions means they are all routed
 			}
 			List<Action> actions = getActions(m1.group(1));
 			for(Action a : actions) {
 				if(a == action) {
 					return false; // already routed
 				}
 			}
 			actions.add(action);
 			newsrc = updateRoute(newsrc, modelName, actions, regex1);
 		}
 		else if(singular) {
 			// there's no plural, but there is a singular so add the action to that
 			List<Action> actions = new ArrayList<Action>();
 			try {
 				actions.add(Action.valueOf(m2.group(2)));
 				actions.add(action);
 				newsrc = updateRoute(newsrc, modelName, actions, regex2);
 			} catch(IllegalArgumentException e) {
 				newsrc = newsrc.replaceFirst(regex2, "router.addResources(" + modelName + ".class, $1$2, " + action.name() + ");");
 				newsrc = SourceFile.ensureImport(newsrc, "static " + Action.class.getCanonicalName() + "." + action.name());
 			}
 		}
 		else {
 			// lastly, neither plural nor singular were found so add it fresh
 			newsrc = oldsrc.replaceFirst("public\\s+void\\s+addRoutes\\s*\\(\\s*Config\\s+config\\s*,\\s*(App)?Router\\s+router\\s*\\)\\s*\\{\\s*",
 											"public void addRoutes(Config config, $1Router router) {\n" +
 											"\t\trouter.addResource(" + modelName + ".class, " + action.name() + ");\n\t\t");
 			newsrc = SourceFile.ensureImport(newsrc, packageName(getModel(modelName)) + "." + modelName);
 			newsrc = SourceFile.ensureImport(newsrc, "static " + Action.class.getCanonicalName() + "." + action.name());
 		}
 		
 		if(!newsrc.equals(oldsrc)) {
 			FileUtils.writeFile(activator, newsrc);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param modelName
 	 * @return true if there were changes made to Application.java; false otherwise
 	 */
 	public boolean addModelRoutes(String modelName) {
 		modelName = adjust(modelName);
 		
 		String oldsrc = FileUtils.readFile(activator).toString();
 		String newsrc = oldsrc;
 		
 		if(!Pattern.compile("router.addResources\\s*\\(\\s*" + modelName + ".class\\s*\\)\\s*;").matcher(newsrc).find()) {
 			newsrc = oldsrc.replaceFirst("public\\s+void\\s+addRoutes\\s*\\(\\s*Config\\s+config\\s*,\\s*(App)?Router\\s+router\\s*\\)\\s*\\{\\s*",
 											"public void addRoutes(Config config, $1Router router) {\n" +
 											"\t\t// auto-generated\n" +
 											"\t\trouter.addResources(" + modelName + ".class);\n\n\t\t");
 		}
 		if(!Pattern.compile("import\\s+"+packageName(models)+"."+modelName).matcher(newsrc).find()) {
 			newsrc = newsrc.replaceFirst("(package\\s+[\\w\\.]+;)", "$1\n\nimport "+packageName(models)+"."+modelName+";");
 		}
 
 		if(!newsrc.equals(oldsrc)) {
 			FileUtils.writeFile(activator, newsrc);
 			return true;
 		}
 		return false;
 	}
 	
 	public void addModule(Module module) {
 		addModule(module, null);
 	}
 	
 	public void addModule(Module module, Mode mode) {
 		MutableAppConfig config = MutableAppConfig.loadMutableConfiguration(this.config);
 		config.add(mode, "modules", module.getName());
 		config.save();
 		
 		for(ExportedPackage exportedPackage : module.getExportedPackages()) {
 			addImportPackage(exportedPackage.toDeclaration());
 		}
 	}
 
 	protected void addModulesDependency(Workspace workspace, Mode mode, Object obj, Map<Bundle, List<Bundle>> dependencies) {
 		if(!blank(obj)) {
 			if(obj instanceof Map) {
 				obj = ((Map<?,?>) obj).keySet().iterator().next();
 			}
 			if(obj instanceof String) {
 				addDependency(workspace, mode, (String) obj, dependencies);
 			} else if(obj instanceof Iterable<?>) {
 				for(Object o : (Iterable<?>) obj) {
 					String module = null;
 					if(o instanceof Map<?,?>) {
 						o = ((Map<?,?>) o).keySet().iterator().next();
 					}
 					if(o instanceof String) {
 						module = (String) o;
 					}
 					if(module == null) {
 						throw new IllegalArgumentException("invalid module type: " + o);
 					}
 					addDependency(workspace, mode, (String) module, dependencies);
 				}
 			} else {
 				throw new IllegalArgumentException(this + " has an invalid " + mode + " modules setting: " + obj);
 			}
 		}
 	}
 	
 	protected void addPersistDependency(Workspace workspace, Mode mode, Object obj, Map<Bundle, List<Bundle>> dependencies) {
 		if(!blank(obj)) {
 			if(obj instanceof String) {
 				addDependency(workspace, mode, (String) obj, dependencies);
 			} else if(obj instanceof List<?>) {
 				for(Object o : (List<?>) obj) {
 					addPersistDependency(workspace, mode, o, dependencies);
 				}
 			} else if(obj instanceof Map<?,?>) {
 				Map<?, ?> map = (Map<?, ?>) obj;
 				String service = (String) map.get(PersistService.SERVICE);
 				if(!blank(service)) {
 					addDependency(workspace, mode, service, dependencies);
 				} else {
 					throw new IllegalArgumentException(this + " " + mode + " persist setting must specify a service");
 				}
 			} else {
 				throw new IllegalArgumentException(this + " has an invalid " + mode + " persist setting: " + obj);
 			}
 		}
 	}
 
 	private String adjust(String rawName) {
 		return adjust(rawName, null);
 	}
 
 	private String adjust(String rawName, String nameEnding) {
 		String name = rawName;
 		
 		// strip extension
 		int ix = name.indexOf('.');
 		if(ix != -1) {
 			name = name.substring(0, ix);
 		}
 		
 		// adjust file path separator
 		if('/' != File.separatorChar) {
 			name = name.replace('/', File.separatorChar);
 		}
 		
 		// just CamelCase the last segment (leave and path segments alone)
 		ix = name.lastIndexOf(File.separatorChar);
 		if(ix != -1) {
 			name = name.substring(0, ix+1) + camelCase(name.substring(ix+1));
 		} else {
 			name = camelCase(name);
 		}
 
 		// add name ending, if necessary
 		if(nameEnding != null) {
 			if(!name.endsWith(nameEnding)) {
 				if(name.toLowerCase().endsWith(nameEnding.toLowerCase())) {
 					name = name.substring(0, name.length() - nameEnding.length()) + nameEnding;
 				} else {
 					name = name + nameEnding;
 				}
 			}
 		}
 		
 		return name;
 	}
 
 	public void clean() {
 		FileUtils.deleteContents(bin, generated);
 	}
 	
 	public File createActionCache(String name, String modelName, Action...actions) {
 		return ProjectGenerator.createActionCache(this, adjust(name), adjust(modelName), actions);
 	}
 	
 	public File createController(File controller) {
 		return createController(getControllerName(controller));
 	}
 	
 	public File createController(String name) {
 		File controller = ControllerGenerator.createController(this, adjust(name, "Controller"));
 		addImportPackage(HttpController.class.getPackage().getName());
 		addExportPackage(packageName(controller));
 		return controller;
 	}
 	
 	/**
 	 * Create complimentary elements for the model with the given name.<br>
 	 * Model must exist already.
 	 * @param modelName
 	 * @param flags
 	 * @return
 	 */
 	public File[] createForModel(Workspace workspace, File model, int flags) {
 		Set<File> changed = new LinkedHashSet<File>();
 		if(model.isFile()) {
 			int f = 0;
 			if((flags & CONTROLLER) != 0) {
 				f |= ModelGenerator.GEN_CONTROLLERS;
 			}
 			if((flags & VIEW) != 0) {
 				f |= ModelGenerator.GEN_VIEWS;
 			}
 			
 			changed.addAll(Arrays.asList(ModelGenerator.generate(workspace, this, model, f)));
 
 			if((flags & CONTROLLER) != 0) {
 				if(addImportPackage(HttpController.class.getPackage().getName())) {
 					changed.add(manifest);
 				}
 				if(addExportPackage(packageName(controllers))) {
 					changed.add(manifest);
 				}
 			}
 			if((flags & VIEW) != 0) {
 				if(addImportPackage(View.class.getPackage().getName())) {
 					changed.add(manifest);
 				}
 				if(addExportPackage(packageName(getViewsFolder(model)))) {
 					changed.add(manifest);
 				}
 			}
 			return changed.toArray(new File[changed.size()]);
 		}
 		return new File[0];
 	}
 
 	/**
 	 * Creates the mailer, if it does not exist, and updates the Manifest's
 	 * Import-Package and Export-Package sections accordingly.
 	 * @param name
 	 * @param methods
 	 * @throws IllegalArgumentException if an attribute cannot be parsed
 	 * @return a list of files, for the mailer and each template (corresponding to each method)
 	 */
 	public File createMailer(String name, String...methods) {
 		File mailer = ProjectGenerator.createMailer(this, adjust(name, "Mailer"), methods);
 		addExportPackage(packageName(mailers));
 		addImportPackage(Mailer.class.getPackage().getName());
 		addImportPackage(InternetAddress.class.getPackage().getName());
 		return mailer;
 	}
 	
 	public File createMailerLayout() {
 		return MailerGenerator.createLayout(this);
 	}
 	
 	public File createMailerLayout(String mailerName) {
 		return MailerGenerator.createLayout(this, mailerName);
 	}
 	
 	public File createMailerTemplate(String mailerName, String name) {
 		MailerGenerator.addMethod(this, mailerName, name);
 		return MailerGenerator.createTemplate(this, mailerName, name);
 	}
 	
 	public List<File> createMailerTemplates(String Name) {
 		return MailerGenerator.createTemplates(this, Name);
 	}
 
 	/**
 	 * Creates the model, if it does not exist, and updates the Manifest's
 	 * Import-Package and Export-Package sections accordingly.
 	 * @param name
 	 * @param attrs
 	 * @throws IllegalArgumentException if an attribute cannot be parsed
 	 * @return the model file
 	 */
 	public File createModel(String name, Map<String, String> attrs) {
 		File model = ProjectGenerator.createModel(this, adjust(name), attrs);
 		addExportPackage(packageName(models));
 		addImportPackage(Model.class.getPackage().getName());
 		addImportPackage(ModelDescription.class.getPackage().getName());
 		return model;
 	}
 
 	public File createNotifier(File model) {
 		String pkg = packageName(model);
 		String modelName = getModelName(model);
 		return createNotifier(pkg, modelName);
 	}
 	
 	public File createNotifier(String modelPackage, String modelName) {
 		addImportPackage(Action.class.getPackage().getName());
 		addImportPackage(ModelNotifier.class.getPackage().getName());
 		addImportPackage(Websocket.class.getPackage().getName());
 		return ProjectGenerator.createNotifier(this, modelPackage, adjust(modelName));
 	}
 	
 	public File createObserver(String modelPackage, String modelName) {
 		return ProjectGenerator.createObserver(this, modelPackage, adjust(modelName));
 	}
 	
 	public File createView(String name, String content) {
 		File view = ViewGenerator.createView(views, adjust(name), content);
 		addImportPackage(View.class.getPackage().getName());
 		addExportPackage(packageName(view));
 		return view;
 	}
 	
 	/**
 	 * Destroy a UI file and its generated file.  Also removes exported packages if
 	 * necessary.  Only intended to work with .esp and .emt files
 	 * @param file a .esp or .emt file
 	 * @return a list of files that were changed and/or deleted
 	 */
 	public File[] destroy(File file) {
 		Set<File> changed = new LinkedHashSet<File>();
 		
 		File folder = file.getParentFile();
 		if(folder.isDirectory() && ((file.exists() && folder.list().length == 1)
 									|| (!file.exists() && folder.list().length == 0))) {
 			if(removeExportPackage(packageName(file))) {
 				changed.add(manifest);
 			}
 			if(file.delete()) {
 				changed.add(file);
 			}
 			folder.delete();
 			changed.add(folder);
 		} else if(file.isFile()) {
 			file.delete();
 		}
 		
 		File genFile = null;
 		String name = file.getName();
 		if(name.endsWith(".emt")) {
 			genFile = getGenMailerTemplate(file);
 		} else if(name.endsWith(".esp")) {
 			genFile = getGenView(file);
 		}
 		
 		if(genFile != null) {
 			folder = genFile.getParentFile();
 			if(folder.isDirectory() && ((genFile.exists() && folder.list().length == 1)
 										|| (!genFile.exists() && folder.list().length == 0))) {
 				if(genFile.delete()) {
 					changed.add(file);
 				}
 				folder.delete();
 				changed.add(folder);
 			} else if(genFile.isFile()) {
 				if(genFile.delete()) changed.add(file);
 			}
 		}
 
 		if(!changed.isEmpty()) {
 			if(!hasViews() && !hasMailerTemplates()) {
 				if(removeImportPackage(View.class.getPackage().getName())) {
 					changed.add(manifest);
 				}
 			}
 			if(name.endsWith(".emt") && !hasMailerTemplates() && 
 					(removeImportPackage(Mailer.class.getPackage().getName()) ||
 							removeImportPackage(InternetAddress.class.getPackage().getName()))) {
 				changed.add(manifest);
 			}
 		}
 		
 		return changed.toArray(new File[changed.size()]);
 	}
 
 	/**
 	 * Destroy complimentary elements for the model with the given name.<br>
 	 * Model does not need to exist.
 	 * @param modelName
 	 * @param flags
 	 * @return
 	 */
 	public File[] destroy(String modelName, int flags) {
 		Set<File> files = new LinkedHashSet<File>();
 		
 		if((flags & CONTROLLER) != 0) {
 			File file = getController(modelName);
 			if(file.delete()) {
 				files.add(file);
 			}
 			if(!hasControllers()) {
 				if(removeImportPackage(HttpController.class.getPackage().getName())) {
 					files.add(manifest);
 				}
 			}
 		}
 
 		if((flags & VIEW) != 0) {
 			File folder = getViewsFolder(modelName);
 			files.addAll(FileUtils.delete(folder, gen(folder)));
 			if(removeExportPackage(packageName(folder))) {
 				files.add(manifest);
 			}
 			if(!hasViews()) {
 				if(removeImportPackage(View.class.getPackage().getName())) {
 					files.add(manifest);
 				}
 			}
 		}
 
 		return files.toArray(new File[files.size()]);
 	}
 	
 	public File[] destroyMailerTemplate(String name) {
 		return destroy(getMailerTemplate(name));
 	}
 	
 	public File[] destroyModel(String name) {
 		List<File> files = new ArrayList<File>();
 		File model = getModel(name);
 		if(removeModelRoutes(model)) {
 			files.add(activator);
 		}
 		if(model.delete()) {
 			files.add(model);
 			if(!hasModels()) {
 				if(removeExportPackage(packageName(models))
 						|| removeImportPackage(Model.class.getPackage().getName())
 						|| removeImportPackage(ModelDescription.class.getPackage().getName())) {
 					files.add(manifest);
 				}
 			}
 		}
 		File genModel = getGenModel(model);
 		if(genModel.delete()) {
 			files.add(genModel);
 		}
 		return files.toArray(new File[files.size()]);
 	}
 	
 	public File[] destroyView(String name) {
 		return destroy(getView(name));
 	}
 
 	private File file(File file, String configPath) {
 		if('/' != File.separatorChar) {
 			return new File(file, configPath.replace('/', File.separatorChar));
 		}
 		return new File(file, configPath);
 	}
 
 	public List<File> findControllers() {
 		List<File> files = Arrays.asList(findFiles(controllers, "Controller.java"));
 		Collections.sort(files);
 		return new ArrayList<File>(files);
 	}
 
 	public List<File> findGenMailers() {
 		File folder = gen(mailers);
 		if(folder.isDirectory()) {
 			return new ArrayList<File>(Arrays.asList(folder.listFiles()));
 		} else {
 			return new ArrayList<File>(0);
 		}
 	}
 
 	public List<File> findGenMailerTemplates() {
 		File folder = gen(mailers);
 		if(folder.isDirectory()) {
 			return new ArrayList<File>(Arrays.asList(findFiles(folder, ".emt")));
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	public List<File> findGenModels() {
 		File folder = gen(models);
 		if(folder.isDirectory()) {
 			return new ArrayList<File>(Arrays.asList(folder.listFiles()));
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	/**
 	 * Get a list of all the Java source files in the generated views folder of this module.
 	 * These will consist of views (.esp), style sheets (.ess), and script files (.ejs).
 	 * @return a List of File objects; never null
 	 */
 	public List<File> findGenViews() {
 		File folder = gen(views);
 		if(folder.isDirectory()) {
 			return new ArrayList<File>(Arrays.asList(findFiles(folder, ".java")));
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	/**
 	 * Get a list of all the Java source files in the generated views folder of the given model.
 	 * These will consist of views (.esp), style sheets (.ess), and script files (.ejs).
 	 * @return a List of File objects; never null
 	 */
 	public List<File> findGenViews(String modelName) {
 		File folder = getGenViewsFolder(modelName);
 		if(folder.isDirectory()) {
 			return new ArrayList<File>(Arrays.asList(findFiles(folder)));
 		}
 		return new ArrayList<File>(0);
 	}
 
 	public List<File> findMailers() {
 		if(mailers.isDirectory()) {
 			List<File> files = Arrays.asList(mailers.listFiles(mailersFilter));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 
 	public List<File> findMailerTemplates() {
 		if(mailers != null && mailers.isDirectory()) {
 			List<File> files = Arrays.asList(findFiles(mailers, ".emt"));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 
 	/**
 	 * Get all models that exist in this module.
 	 * @return a List of the models in this module; never null.
 	 */
 	public List<File> findModels() {
 		if(models.isDirectory()) {
 			List<File> files = Arrays.asList(models.listFiles(modelsFilter));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 
 	public List<File> findScriptFiles() {
 		if(views.isDirectory()) {
 			List<File> files = Arrays.asList(findFiles(views, ".ejs"));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	public List<File> findStyleSheets() {
 		if(views.isDirectory()) {
 			List<File> files = Arrays.asList(findFiles(views, ".ess"));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	public List<File> findViews() {
 		if(views.isDirectory()) {
 			List<File> files = Arrays.asList(findFiles(views, ".esp"));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 
 	public List<File> findViews(String modelName) {
 		File folder = getViewsFolder(modelName);
 		if(folder.isDirectory()) {
 			List<File> files = Arrays.asList(findFiles(views, ".esp"));
 			Collections.sort(files);
 			return new ArrayList<File>(files);
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	private File gen(File srcFile) {
 		String relativePath = srcFile.getAbsolutePath().substring(src.getAbsolutePath().length());
 		return new File(generated, relativePath);
 	}
 	
 	/**
 	 * Generate the Java source for an EFile (.esp, .emt, .ess, or .ejs).<br>
 	 * This method also creates the generated Java source file on the file system.
 	 * @param efile the EFile; null will simply return null
 	 * @return the Java source file, if it could be generated; null otherwise
 	 */
 	public File generate(File efile) {
 		return EFileGenerator.generate(this, efile);
 	}
 	
 	/**
 	 * Generate the Java source for an EFile (.esp, .emt, .ess, or .ejs), using the given source.<br>
 	 * This method returns the EspSourceFile used to generate the Java source file, but also creates
 	 * the generated Java source file on the file system.
 	 * @param efile the EFile; null will simply return null
 	 * @param source the source of the EFile; used to generate the Java source.
 	 * @return the EspSourceFile, if it could be generated; null otherwise
 	 */
 	public ESourceFile generate(File efile, String source) {
 		return EFileGenerator.generate(this, efile, source);
 	}
 	
 	/**
 	 * Generate the Java sources for the given EFiles (.esp, .emt, .ess, or .ejs).<br>
 	 * This method also creates the generated Java source files on the file system.
 	 * @param efiles a List of EFiles; null will simply return null
 	 * @return the Java source file, if it could be generated; null otherwise
 	 */
 	public List<File> generate(List<File> efiles) {
 		return EFileGenerator.generate(this, efiles);
 	}
 
 	public List<File> generate(Workspace workspace) {
 		Generator generator = new Generator(workspace, this);
 		List<File> list = generator.generate().get(this);
 		if(list != null) {
 			return list;
 		}
 		return new ArrayList<File>(0);
 	}
 	
 	public Map<Bundle, List<File>> generate(Workspace workspace, Mode mode) {
 		Generator generator = new Generator(workspace, this);
 		generator.setMode(mode);
 		return generator.generate();
 	}
 	
 	/**
 	 * Generate the asset list file (assets.js) from the assets currently contained by this module.<br/>
 	 * Paths in the assets.js file have the following format: <pre>path|size|lastModified[:realm]</pre>
 	 * lastModified is in the format of an http-date (RFC 1123, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1)
 	 * @return the generated File if it could be created; null otherwise
 	 * @see DateUtils#httpDate(java.util.Date)
 	 */
 	public File generateAssetList() {
 		if(assets != null && assets.exists()) {
 			ArrayList<String> realms = new ArrayList<String>();
 			ArrayList<String> paths = new ArrayList<String>();
 
 			int len = assets.getPath().length();
 
 			// get all files in the assets folder and its subfolders
 			// note that each folder may contain a special "authentication.realm" file
 			//   this file contains the name of the realm that is allowed access to the files in the folder
 			for(File file : findFiles(assets)) {
 				if(file.getName().equals("authentication.realm")) {
					realms.add(file.getParent().substring(len).replace('\\', '/') + File.separator + "|" + readFile(file));
 				} else {
					paths.add(file.getPath().substring(len).replace('\\', '/') + "|" + file.length() + "|" + httpDate(file.lastModified()));
 				}
 			}
 
 			if(!realms.isEmpty()) {
 				for(String s : realms) {
 					String[] sa = s.split("|");
 					for(int i = 0; i < paths.size(); i++) {
 						String path = paths.get(i);
 						if(path.startsWith(sa[0])) {
 							paths.set(i, path + "|" + sa[1]);
 						}
 					}
 				}
 			}
 			
 			Collections.sort(paths);
 
 			len = 10;
 			for(String path : paths) {
 				len += path.length();
 				len += 5;
 			}
 			
 			StringBuilder sb = new StringBuilder(len);
 			sb.append("([");
 			for(int i = 0; i < paths.size(); i++) {
 				if(i != 0) sb.append(',');
 				sb.append('\n').append('\t').append('"').append(paths.get(i)).append('"');
 			}
 			sb.append("\n]);\n");
 			
 			writeFile(assetList, sb.toString());
 			return assetList;
 		}
 		return null;
 	}
 
 	public List<File> generateMailer(File mailer) {
 		return MailerGenerator.generate(this, mailer);
 	}
 	
 	public File generateMailerTemplate(File template) {
 		if(template.isFile()) {
 			return EFileGenerator.generate(this, template);
 		}
 		return null;
 	}
 	
 	/**
 	 * Generate the super class for the given model file.  This is a convenience method that
 	 * simply calls <code>generateModel(model, true)</code>.
 	 * @param model the model file whose super class is to generated
 	 * @return a File object for the generated Java file, if it was generated; an empty list otherwise - never null.
 	 * @see Module#generateModel(File, boolean)
 	 */
 	public List<File> generateModel(Workspace workspace, File model) {
 		return generateModel(workspace, model, true);
 	}
 
 	/**
 	 * Generate the super class for the given model file.
 	 * @param model the model file whose super class is to generated
 	 * @param force if true then the super class will be generated even if it is already up to date
 	 * @return a File object for the generated Java file, if it was generated; an empty list otherwise - never null.
 	 */
 	public List<File> generateModel(Workspace workspace, File model, boolean force) {
 		if(model.exists()) {
 			if(!force) {
 				File genModel = getGenModel(model);
 				if(genModel.exists()) {
 					if(model.lastModified() <= genModel.lastModified()) {
 						return new ArrayList<File>(0);
 					} else {
 						try {
 							String srcAnnotations = getSrcAnnotations(model).replaceAll("\\s", "");
 							String genAnnotations = getGenAnnotations(genModel).replaceAll("\\s", "");
 							if(srcAnnotations.equals(genAnnotations)) {
 								return new ArrayList<File>(0);
 							}
 						} catch(IOException e) {
 							logger.warn("failed to compare annotations (re-run with force flag):\n  " + model + "\n  " + genModel, e);
 							return new ArrayList<File>(0);
 						}
 					}
 				}
 			}
 			return Arrays.asList(ModelGenerator.generate(workspace, this, model));
 		}
 		return new ArrayList<File>(0);
 	}
 
 	public File generateScriptFile(File script) {
 		if(script.exists()) {
 			return EFileGenerator.generate(this, script);
 		}
 		return null;
 	}
 	
 	public File generateStyleSheet(File style) {
 		if(style.exists()) {
 			return EFileGenerator.generate(this, style);
 		}
 		return null;
 	}
 
 	public File generateView(File view) {
 		if(view.exists()) {
 			return EFileGenerator.generate(this, view);
 		}
 		return null;
 	}
 
 	public File getActionCache(String name) {
 		return new File(caches, adjust(name) + ".java");
 	}
 
 	private List<Action> getActions(String str) {
 		List<Action> actions = new ArrayList<Action>();
 		for(String s : str.split("\\s*,\\s*")) {
 			if(s.length() > 0) {
 				if(s.startsWith("Action.")) {
 					s = s.substring(7);
 				}
 				try {
 					actions.add(Action.valueOf(s));
 				} catch(IllegalArgumentException e) {
 					// skip it
 				}
 			}
 		}
 		return actions;
 	}
 	
 	public File[] getBinEFiles(File...efiles) {
 		File[] binViews = new File[efiles.length];
 		String mailersPath = this.mailers.getAbsolutePath();
 		String viewsPath = this.views.getAbsolutePath();
 		String srcPath = this.src.getAbsolutePath();
 		for(int i = 0; i < efiles.length; i++) {
 			String name = efiles[i].getAbsolutePath();
 			String path = name.endsWith(".emt") ? mailersPath : viewsPath;
 			name = name.substring(path.length() + 1, name.length() - 4);
 			String relativePath = path.substring(srcPath.length());
 			binViews[i] = new File(bin, relativePath + File.separator + name + ".class");
 		}
 		return binViews;
 	}
 	
 	public File[] getBinFiles(File srcFile) {
 		String path = srcFile.getAbsolutePath();
 		String srcPath = src.getAbsolutePath();
 		int len;
 		if(path.startsWith(srcPath)) {
 			len = srcPath.length();
 		} else {
 			len = generated.getAbsolutePath().length();
 		}
 		return getBinFiles(path, len);
 	}
 	
 	public Set<File> getBinFiles(List<File> srcFiles) {
 		Set<File> binFiles = new HashSet<File>();
 
 		String metaPath = file + File.separator + "META-INF";
 		
 		// TODO source paths should actually pull from the build file (then this method could be used in Bundle)
 		List<String> srcPaths = new ArrayList<String>();
 		srcPaths.add(src.getAbsolutePath() + File.separator);
 		srcPaths.add(generated.getAbsolutePath() + File.separator);
 		srcPaths.add(assets.getAbsolutePath() + File.separator);
 		srcPaths.add(file + File.separator + "META-INF");
 		
 		for(File file : srcFiles) {
 			File binFile = null;
 			String path = file.getAbsolutePath();
 			if(!path.endsWith(".esp")) {
 				if(path.endsWith(".java")) {
 					path = path.substring(0, path.length() - 5) + ".class";
 				}
 				if(path.startsWith(metaPath)) {
 					binFile = new File(bin, path.substring(this.file.getAbsolutePath().length()));
 				} else {
 					for(String srcPath : srcPaths) {
 						if(path.startsWith(srcPath)) {
 							binFile = new File(bin, path.substring(srcPath.length()));
 							break;
 						}
 					}
 				}
 				if(binFile == null) {
 					throw new IllegalStateException("could not find root of " + file);
 				}
 				binFiles.add(binFile);
 			}
 		}
 		
 		return binFiles;
 	}
 	
 	/**
 	 * Get a File for a controller in this module with the given name (the controller name, or the model name).<br>
 	 * Note that this controller may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the controller or model
 	 * @return a File object for the controller (whether it exists or not); never null
 	 */
 	public File getController(String name) {
 		return new File(controllers, adjust(name, "Controller") + ".java");
 	}
 	
 	/**
 	 * Get a File for a controller in this module with the given model file.<br>
 	 * Note that this controller may not actually exist - check using {@link File#exists()}.
 	 * @param modelFile the file for the model
 	 * @return a File object for the controller (whether it exists or not); never null
 	 */
 	public File getControllerFor(File modelFile) {
 		return getController(getModelName(modelFile));
 	}
 
 	/**
 	 * Get a File for a controller in this module with the given model name.<br>
 	 * Note that this controller may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the model
 	 * @return a File object for the controller (whether it exists or not); never null
 	 */
 	public File getControllerFor(String modelName) {
 		return getController(modelName);
 	}
 	
 	public String getControllerName(File controller) {
 		String name = controller.getAbsolutePath();
 		name = name.substring(controllers.getAbsolutePath().length() + 1, name.length() - 5);
 		return name;
 	}
 	
 	public String getControllerType(String name) {
 		return getControllerType(getController(name));
 	}
 	
 	public String getControllerType(File controller) {
 		return packageName(controller, true) + "." + getControllerName(controller);
 	}
 	
 	/**
 	 * Get the generated file for the given efile (.esp, .emt, .ess, or .ejs).<br>
 	 * Note that the file may not actually exist on the file system - 
 	 * check using {@link File#exists()}.
 	 * @param name
 	 * @return a {@link File} for the generated efile whether it exists or not; returns null 
 	 * if the given efile is not actually an efile (typically happens when opening javascript or css files)
 	 */
 	public File getGenFile(File efile) {
 		String name = efile.getName();
 		if(name.endsWith(".esp")) {
 			return getGenView(efile);
 		} else if(name.endsWith(".emt")) {
 			return getGenMailerTemplate(efile);
 		} else if(name.endsWith(".ess") || name.endsWith(".ejs")) {
 			String s = gen(efile).getAbsolutePath();
 			return new File(s.substring(0, s.length()-4) + ".java");
 		}
 		return null;
 	}
 	
 	public File getGenMailer(File mailer) {
 		String relativePath = mailer.getParentFile().getAbsolutePath().substring(src.getAbsolutePath().length()) + 
 								File.separator + "Abstract" + mailer.getName();
 		return new File(generated, relativePath);
 	}
 	
 	public File getGenMailer(String name) {
 		return getGenMailer(getMailer(name));
 	}
 	
 	/**
 	 * Get the generated file for the given mailer template.<br>
 	 * Note that the file may not actually exist on the file system - 
 	 * check using {@link File#exists()}.
 	 * @param name
 	 * @return a {@link File} for the generated mailer template whether it exists or not; never null
 	 */
 	public File getGenMailerTemplate(File template) {
 		return getGenMailerTemplate(getMailerTemplateName(template));
 	}
 	
 	/**
 	 * Get the generated file for the mailer template with the given name.<br>
 	 * Note that the file may not actually exist on the file system - 
 	 * check using {@link File#exists()}.
 	 * @param name
 	 * @return a {@link File} for the generated mailer template whether it exists or not; never null
 	 */
 	public File getGenMailerTemplate(String name) {
 		return new File(gen(mailers), name + ".java");
 	}
 	
 	public File getGenModel(File model) {
 		String path = model.getAbsolutePath();
 		path = path.substring(0, path.length() - 5) + "Model.java";
 		return gen(new File(path));
 	}
 	
 	/**
 	 * Get the generated file for the given view.<br>
 	 * Note that the file may not actually exist on the file system - 
 	 * check using {@link File#exists()}.
 	 * @param name
 	 * @return a {@link File} for the generated view whether it exists or not; never null
 	 */
 	public File getGenView(File view) {
 		return getGenView(getViewName(view));
 	}
 	
 	/**
 	 * Get the generated file for the view with the given name.<br>
 	 * Note that the file may not actually exist on the file system - 
 	 * check using {@link File#exists()}.
 	 * @param name
 	 * @return a {@link File} for the generated view whether it exists or not; never null
 	 */
 	public File getGenView(String name) {
 		return new File(gen(views), name + ".java");
 	}
 	
 	public File getGenViewsFolder(String modelName) {
 		return gen(getViewsFolder(modelName));
 	}
 
 	public File getLayout() {
 		return new File(views, "_layouts" + File.separator + "_Layout.esp");
 	}
 	
 	public File getLayoutFor(String modelName) {
 		return new File(views, "_layouts" + File.separator + adjust(modelName) + "Layout.esp");
 	}
 
 	public int getLine(String name, Action action) {
 		if(!isJar) {
 			File controller = getController(name);
 			if(controller.isFile()) {
 				String src = readFile(controller).toString();
 				String regex = "public\\s+void\\s+" + action.name() + "\\s*()";
 				Pattern p = Pattern.compile(regex);
 				Matcher m = p.matcher(src);
 				if(m.find()) {
 					int line = 1;
 					for(int i = m.start(); i > 0; i--) {
 						if(src.charAt(i) == '\n') {
 							line++;
 						}
 					}
 					return line;
 				}
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Get a File for a mailer in this module with the given name.
 	 * Note that this mailer may not actually exist - check using
 	 * {@link File#exists()}.
 	 * @param name either the name or simple name of the mailer
 	 * @return a File object for the mailer (whether it exists or not); never null
 	 */
 	public File getMailer(String name) {
 		return new File(mailers, adjust(name, "Mailer") + ".java");
 	}
 	
 	public File getMailerLayout() {
 		return new File(mailers, "_layouts" + File.separator + "_Layout.emt");
 	}
 
 	public File getMailerLayout(String mailerName) {
 		return new File(mailers, "_layouts" + File.separator + adjust(mailerName, "Layout") + ".emt");
 	}
 
 	/**
 	 * Get a File for a mailer template in this module with the given name.<br>
 	 * Note that this view may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the mailer template
 	 * @return a File object for the mailer template (whether it exists or not); never null
 	 * @see #mailerTemplates
 	 */
 	public File getMailerTemplate(String name) {
 		return new File(mailers, adjust(name) + ".emt");
 	}
 	
 	/**
 	 * Get the name of the mailer template from either the source file (.emt) or its generated file (.java).
 	 * @param template
 	 * @return
 	 */
 	public String getMailerTemplateName(File template) {
 		String name = template.getAbsolutePath();
 		if(name.startsWith(mailers.getAbsolutePath())) {
 			name = name.substring(mailers.getAbsolutePath().length() + 1, name.length() - 4);
 		} else {
 			name = name.substring(gen(mailers).getAbsolutePath().length() + 1, name.length() - 5);
 		}
 		return name;
 	}
 	
 	/**
 	 * Get a File for a model in this module with the given name.<br>
 	 * Note that this model may not actually exist - check using
 	 * {@link File#exists()}.
 	 * @param name the name of the model
 	 * @return a File object for the model (whether it exists or not); never null
 	 */
 	public File getModel(String name) {
 		return new File(models, adjust(name) + ".java");
 	}
 	
 	public String getModelName(File model) {
 		String name = model.getAbsolutePath();
 		name = name.substring(models.getAbsolutePath().length() + 1, name.length() - 5);
 		return name;
 	}
 	
 	public String getModelType(File model) {
 		return packageName(model, true) + "." + getModelName(model);
 	}
 	
 	public String[] getModules() {
 		Config config = loadConfiguration();
 		List<String> modules = config.getModules();
 		return modules.toArray(new String[modules.size()]);
 	}
 	
 	public File getNotifier(File model) {
 		return getNotifier(getModelName(model));
 	}
 	
 	public File getNotifier(String name) {
 		return new File(notifiers, adjust(name, "Notifier") + ".java");
 	}
 	
 	public File getObserver(String name) {
 		return new File(observers, adjust(name, "Observer") + ".java");
 	}
 	
 	/**
 	 * Get a File for a script in this module with the given name.<br>
 	 * Note that this script may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the script (relative to the views directory - may need to include folder name: 'pages/Home')
 	 * @return a File object for the script (whether it exists or not); never null
 	 * @see #views
 	 */
 	public File getScriptFile(String name) {
 		return new File(views, adjust(name) + ".ejs");
 	}
 	
 	public File getSrcMailer(File genMailer) {
 		return src(genMailer);
 	}
 
 	public File getSrcMailerTemplate(File genView) {
 		return getMailerTemplate(getMailerTemplateName(genView));
 	}
 	
 	public File getSrcModel(File genModel) {
 		String path = genModel.getAbsolutePath();
 		path = path.substring(0, path.length() - 10) + ".java";
 		return src(new File(path));
 	}
 
 	/**
 	 * Get a File for the view in this module that is used to generate the given Java file.<br>
 	 * The returned File may be for an actual view (.esp), or a style sheet (.ess), or script file (.ejs).
 	 * Note that the returned File may not actually exist on the file system - check using {@link File#exists()}.
 	 * @param genView the generated Java file of the view
 	 * @return a File object for the view (whether it exists or not); never null
 	 * @see #views
 	 */
 	public File getSrcView(File genView) {
 		String name = getViewName(genView);
 		File file = new File(views, name + ".esp");
 		if(!file.exists()) {
 			file = new File(views, name + ".ess");
 			if(!file.exists()) {
 				file = new File(views, name + ".ejs");
 			}
 		}
 		return file;
 	}
 
 	/**
 	 * Get a File for a style sheet in this module with the given name.<br>
 	 * Note that this style sheet may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the style sheet (relative to the views directory - may need to include folder name: 'pages/Home')
 	 * @return a File object for the style sheet (whether it exists or not); never null
 	 * @see #views
 	 */
 	public File getStyleSheet(String name) {
 		return new File(views, adjust(name) + ".ess");
 	}
 
 	public int getType(File file) {
 		if(ProjectUtils.isModel(file)) {
 			return MODEL;
 		}
 		if(ProjectUtils.isController(file)) {
 			return CONTROLLER;
 		}
 		if(ProjectUtils.isView(file)) {
 			return VIEW;
 		}
 		if(ProjectUtils.isMailer(file)) {
 			return MAILER;
 		}
 		if(ProjectUtils.isManifest(file)) {
 			return MANIFEST;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Get a File for a view in this module with the given name.<br>
 	 * Note that this view may not actually exist - check using {@link File#exists()}.
 	 * @param name the name of the view (relative to the views directory - may need to include folder name: 'pages/Home')
 	 * @return a File object for the view (whether it exists or not); never null
 	 * @see #views
 	 */
 	public File getView(String name) {
 		return new File(views, adjust(name) + ".esp");
 	}
 	
 	public File getView(String modelName, String name) {
 		return new File(getViewsFolder(modelName), adjust(name) + ".esp");
 	}
 	
 	/**
 	 * Get the name of the view from either the source file (.esp) or its generated file (.java).<br>
 	 * Works also for style sheets (.ess) and script files (.ejs).
 	 * @param view
 	 * @return
 	 */
 	public String getViewName(File view) {
 		String name = view.getAbsolutePath();
 		if(name.startsWith(views.getAbsolutePath())) {
 			name = name.substring(views.getAbsolutePath().length() + 1, name.length() - 4);
 		} else {
 			name = name.substring(gen(views).getAbsolutePath().length() + 1, name.length() - 5);
 		}
 		return name;
 	}
 
 	public File getViewsFolder(File model) {
 		return getViewsFolder(getModelName(model));
 	}
 
 	public File getViewsFolder(String modelName) {
 		return new File(views, tableName(adjust(modelName)));
 	}
 
 	public boolean hasControllers() {
 		File[] files = FileUtils.findFiles(controllers, ".java");
 		for(File file : files) {
 			if(ProjectUtils.isController(file)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Does this module have any mailer template files (.emt).
 	 * @return true if there are .emt files, false otherwise
 	 */
 	public boolean hasMailerTemplates() {
 		return !findMailerTemplates().isEmpty();
 	}
 
 	public boolean hasModels() {
 		File[] files = FileUtils.findFiles(models, ".java");
 		for(File file : files) {
 			if(ProjectUtils.isModel(file)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Does this module have any view files (.esp).
 	 * @return true if there are .esp files, false otherwise
 	 */
 	public boolean hasViews() {
 		return !findViews().isEmpty();
 	}
 
 	public boolean isRouted(String modelName, Action action) {
 		if(!isJar) {
 			String model = adjust(modelName);
 			String src = readFile(activator).toString();
 			String regex = "router.addResources\\s*\\(\\s*" + modelName + ".class\\s*([^\\)]*)\\s*\\)\\s*;";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(src);
 			if(m.find()) {
 				String actions = m.group(1);
 				if(actions.length() == 0) {
 					return true;
 				}
 				for(String s : actions.split("\\s*,\\s*")) {
 					if(s.length() > 0) {
 						if(s.startsWith("Action.")) {
 							s = s.substring(7);
 						}
 						try {
 							if(action == Action.valueOf(s)) {
 								return true;
 							}
 						} catch(IllegalArgumentException e) {
 							// skip it
 						}
 					}
 				}
 			}
 			p = Pattern.compile("router.addResource\\s*\\(\\s*" + model + "\\.class\\s*,\\s*(Action\\.)?" + action.name() + "\\)");
 			m = p.matcher(src);
 			if(m.find()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isWebservice() {
 		return hasNature(NATURE_WEBSERVICE);
 	}
 
 	public Config loadConfiguration() {
 		return Config.loadConfiguration(config);
 	}
 
 	public boolean removeModelRoute(String modelName, Action action) {
 		modelName = adjust(modelName);
 
 		String oldsrc = FileUtils.readFile(activator).toString();
 		String newsrc = oldsrc;
 		
 		String regex = "router.addResources\\s*\\(\\s*" + modelName + ".class\\s*([^\\)]*)\\s*\\)\\s*;";
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(newsrc);
 		if(m.find()) {
 			List<Action> actions = (m.group(1).length() == 0) ? Arrays.asList(Action.values()) : getActions(m.group(1));
 			for(Action a1 : actions) {
 				if(a1 == action) {
 					Set<String> imports = new TreeSet<String>();
 					StringBuilder sb = new StringBuilder();
 					actions.remove(action);
 					for(Action a2 : sortActions(actions)) {
 						sb.append(", ").append(a2.name());
 						imports.add("static " + Action.class.getCanonicalName() + "." + a2.name());
 					}
 					if(actions.size() == 1) {
 						newsrc = newsrc.replaceFirst(regex, "router.addResource(" + modelName + ".class" + sb.toString() + ");");
 					} else {
 						newsrc = newsrc.replaceFirst(regex, "router.addResources(" + modelName + ".class" + sb.toString() + ");");
 					}
 					newsrc = SourceFile.ensureImports(newsrc, imports);
 					break;
 				}
 			}
 		}
 
 		regex = "router.addResource\\s*\\(\\s*" + modelName + ".class\\s*,\\s*(Action\\.)?" + action.name() + "\\s*\\)\\s*;";
 		newsrc = newsrc.replaceFirst(regex + "\\s*", "\n\t\t");
 
 		if(!newsrc.equals(oldsrc)) {
 			newsrc = SourceFile.removeUnusedImport(newsrc, packageName(getModel(modelName)) + "." + modelName);
 			newsrc = SourceFile.removeUnusedImport(newsrc, "static " + Action.class.getCanonicalName() + "." + action.name());
 			FileUtils.writeFile(activator, newsrc);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeModelRoutes(File model) {
 		return removeModelRoutes(model, getModelName(model));
 	}
 
 	private boolean removeModelRoutes(File model, String modelName) {
 		String className = packageName(model) + "." + modelName;
 		String srcOld = readFile(activator).toString();
 		String srcNew = srcOld.replaceFirst("import\\s+" + className + "\\s*;\\s*", "");
 		if(!srcOld.equals(srcNew)) {
 			srcNew = srcNew.replaceAll("(// auto-generated)?\\s*router\\.addResources\\(" + modelName + ".class\\);\\s*", "");
 			writeFile(activator, srcNew);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean removeModelRoutes(String modelName) {
 		return removeModelRoutes(getModel(modelName), modelName);
 	}
 	
 	public void removeModule(Module module) {
 		removeModule(module, null);
 	}
 	
 	public void removeModule(Module module, Mode mode) {
 		MutableAppConfig config = MutableAppConfig.loadMutableConfiguration(this.config);
 		config.remove(mode, "modules", module.getName());
 		config.save();
 
 		for(ExportedPackage exportedPackage : module.getExportedPackages()) {
 			removeImportPackage(exportedPackage.toDeclaration());
 		}
 	}
 	
 	private List<Action> sortActions(List<Action> actions) {
 		Collections.sort(actions, new Comparator<Action>() {
 			@Override
 			public int compare(Action a1, Action a2) {
 				return a1.ordinal() - a2.ordinal();
 			}
 		});
 		return actions;
 	}
 	
 	private File src(File genFile) {
 		String relativePath = genFile.getAbsolutePath().substring(generated.getAbsolutePath().length());
 		return new File(src, relativePath);
 	}
 	
 	private String updateRoute(String newsrc, String modelName, List<Action> actions, String regex) {
 		Set<String> imports = new TreeSet<String>();
 		StringBuilder sb = new StringBuilder();
 		for(Action a : sortActions(actions)) {
 			sb.append(", ").append(a.name());
 			imports.add("static " + Action.class.getCanonicalName() + "." + a.name());
 		}
 		newsrc = newsrc.replaceFirst(regex, "router.addResources(" + modelName + ".class" + sb.toString() + ");");
 		return SourceFile.ensureImports(newsrc, imports);
 	}
 	
 }
