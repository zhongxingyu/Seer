 package org.oobium.build.clients.blazeds;
 
 import static org.oobium.build.util.ProjectUtils.getPrefsFileDate;
 import static org.oobium.utils.FileUtils.createFolder;
 import static org.oobium.utils.FileUtils.deleteContents;
 import static org.oobium.utils.FileUtils.*;
 import static org.oobium.utils.StringUtils.*;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.oobium.build.gen.model.PropertyDescriptor;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.util.MethodCreator;
 import org.oobium.build.workspace.Module;
 import org.oobium.persist.Binary;
 import org.oobium.persist.Text;
 
 public class FlexProjectGenerator {
 
 	private static final Map<String, String> flexTypes;
 	static {
 		flexTypes = new HashMap<String, String>();
 		flexTypes.put(Binary.class.getCanonicalName(),			null);
 		flexTypes.put(byte[].class.getCanonicalName(),			null);
 		flexTypes.put(String.class.getCanonicalName(),			"String");
 		flexTypes.put(Text.class.getCanonicalName(),			"String");
 		flexTypes.put(Integer.class.getCanonicalName(),			"int");
 		flexTypes.put(int.class.getCanonicalName(),				"int");
 		flexTypes.put(Float.class.getCanonicalName(),			"Number");
 		flexTypes.put(float.class.getCanonicalName(),			"Number");
 		flexTypes.put(Long.class.getCanonicalName(), 			"int");
 		flexTypes.put(long.class.getCanonicalName(), 			"int");
 		flexTypes.put(Boolean.class.getCanonicalName(), 		"Boolean");
 		flexTypes.put(boolean.class.getCanonicalName(), 		"Boolean");
 		flexTypes.put(Double.class.getCanonicalName(), 			"Number");
 		flexTypes.put(double.class.getCanonicalName(), 			"Number");
 		flexTypes.put(Date.class.getCanonicalName(), 			"Date");
 		flexTypes.put(java.sql.Date.class.getCanonicalName(),	"Date");
 		flexTypes.put(Time.class.getCanonicalName(),			"Date");
 		flexTypes.put(Timestamp.class.getCanonicalName(),		"Date");
 		flexTypes.put(BigDecimal.class.getCanonicalName(),		"Number");
 	}
 
 	
 	private final Module module;
 	private final File project;
 	
 	private boolean force;
 	private String flexSdk;
 	
 	public FlexProjectGenerator(Module module) {
 		this(module, null);
 	}
 	
 	public FlexProjectGenerator(Module module, File project) {
 		this.module = module;
 		if(project == null) {
 			project = module.file.getParentFile();
 		}
 		if(project.isDirectory()) {
 			this.project = new File(project, module.name + ".blazeds.flex");
 		} else {
 			this.project = project;
 		}
 	}
 	
 	public File create() {
 		if(force) {
 			deleteContents(project);
 		}
 		else if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		}
 		
 		createFolder(project, "bin");
 		
 		File src = createFolder(project, "src");
 		createObserversFile(src);
 		for(File file : module.findModels()) {
 			createModel(src, file);
 		}
 
 		createActionScriptPropertiesFile();
 		createFlexLibPropertiesFile();
 		createPrefsFile();
 		createProjectFile();
 		if(flexSdk != null) {
 			createExternalToolBuilderFile();
 		}
 
 		return project;
 	}
 	
 	private void createActionScriptPropertiesFile() {
 		String path = project.getName() + ".as";
 		String uuid = UUID.randomUUID().toString();
 		writeFile(project, ".actionScriptProperties", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
 				"<actionScriptProperties analytics=\"false\" mainApplicationPath=\"{path}\" projectUUID=\"{uuid}\" version=\"10\">",
 				" <compiler additionalCompilerArguments=\"-locale en_US\" autoRSLOrdering=\"true\" copyDependentFiles=\"false\" fteInMXComponents=\"false\" generateAccessible=\"false\" htmlExpressInstall=\"true\" htmlGenerate=\"false\" htmlHistoryManagement=\"false\" htmlPlayerVersionCheck=\"true\" includeNetmonSwc=\"false\" outputFolderPath=\"bin\" removeUnusedRSL=\"true\" sourceFolderPath=\"src\" strict=\"true\" targetPlayerVersion=\"0.0.0\" useApolloConfig=\"false\" useDebugRSLSwfs=\"true\" verifyDigests=\"true\" warn=\"true\">",
 				"  <compilerSourcePath/>",
 				"  <libraryPath defaultLinkType=\"0\">",
 				"   <libraryPathEntry kind=\"4\" path=\"\">",
 				"    <excludedEntries>",
 				"     <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/flex.swc\" useDefaultLinkType=\"false\"/>",
 				"     <libraryPathEntry kind=\"3\" linkType=\"1\" path=\"${PROJECT_FRAMEWORKS}/libs/core.swc\" useDefaultLinkType=\"false\"/>",
 				"    </excludedEntries>",
 				"   </libraryPathEntry>",
 				"  </libraryPath>",
 				"  <sourceAttachmentPath/>",
 				" </compiler>",
 				" <applications>",
 				"  <application path=\"{path}\"/>",
 				" </applications>",
 				" <modules/>",
 				" <buildCSSFiles/>",
 				" <flashCatalyst validateFlashCatalystCompatibility=\"false\"/>",
 				"</actionScriptProperties>"
 			).replace("{path}", path).replace("{uuid}", uuid));
 	}
 	
 	private void createExternalToolBuilderFile() {
 		writeFile(project, ".externalToolBuilders/Flex_Builder.launch", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
 				"<launchConfiguration type=\"org.eclipse.ui.externaltools.ProgramBuilderLaunchConfigurationType\">",
 				" <booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"true\"/>",
 				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"{flexSdk}/bin/compc\"/>",
 				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,\"/>",
 				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS\" value=\"-source-path=src/&#10;-include-sources=src/&#10;-output=bin/{module}.swc\"/>",
 				" <booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>",
 				" <stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY\" value=\"${workspace_loc:/{project}}\"/>",
 				"</launchConfiguration>"
 			).replace("{flexSdk}", flexSdk).replace("{module}", module.name).replace("{project}", project.getName()));
 	}
 	
 	private void createFlexLibPropertiesFile() {
 		writeFile(project, ".flexLibProperties", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
 				"<flexLibProperties includeAllClasses=\"true\" useMultiPlatformConfig=\"false\" version=\"3\">",
 				" <includeClasses/>",
 				" <includeResources/>",
 				" <namespaceManifests/>",
 				"</flexLibProperties>"
 			));
 	}
 
 	private void createModel(File srcFolder, File modelFile) {
 		ModelDefinition model = new ModelDefinition(modelFile);
 		ActionScriptFile as = new ActionScriptFile();
 		as.packageName = model.getPackageName();
 
 		as.imports.add("mx.controls.Alert");
 		as.imports.add("mx.rpc.remoting.RemoteObject");
 		as.imports.add("mx.rpc.events.ResultEvent");
 		as.imports.add("mx.rpc.events.FaultEvent");
 		as.imports.add("org.oobium.persist.Observers");
 		
 		as.classMetaTags.add("RemoteClass(alias=\"" + model.getCanonicalName() + "\")");
 		as.simpleName = model.getSimpleName();
 		
 		as.staticVariables.put("ro", "private static var ro:RemoteObject;");
 		
 		as.staticInitializers.add("ro = new RemoteObject();");
 		as.staticInitializers.add("ro.destination = \"" + model.getControllerName() + "\";");
 		as.staticInitializers.add("ro.addEventListener(\"fault\", faultHandler);");
 		as.staticInitializers.add("ro.addObserver.addEventListener(\"result\", Observers.onChannelAdded);");
 		
 		as.staticMethods.put("addObserver", source(
 				"public static function addObserver(method:String, callback:Function):void {",
 				" {type}.ro.addObserver();",
 				" Observers.addObserver(\"{fullType}\", method, callback);",
 				"}"
 			).replace("{type}", as.simpleName).replace("{fullType}", model.getCanonicalName()));
 
 		as.staticMethods.put("fault", source(
 				"private static function faultHandler (event:FaultEvent):void {",
 				" Alert.show(event.fault.faultString, 'Error');",
 				"}"
 			));
 		
 		String finders = source(
 				"public static function {name}(o:Object, callback:Function):void {",
 				" {type}.ro.{name}.addEventListener(\"result\", function(event:ResultEvent):void {",
 				"  {type}.ro.{name}.removeEventListener(\"result\", arguments.callee);",
 				"  callback(event);",
 				" });",
 				" if(typeof(o) == \"number\") {",
 				"  {type}.ro.{name}(o as int);",
 				" } else if(typeof(o) == \"string\") {",
 				"  {type}.ro.{name}(o as String);",
 				" } else if(o != null) {",
 				"  {type}.ro.{name}(o.toString());",
 				" } else {",
 				"  throw new Error(\"object cannot be null\");",
 				" }",
 				"}").replace("{type}", as.simpleName);
 
 		as.staticMethods.put("find", finders.replace("{name}", "find"));
 		as.staticMethods.put("findAll", finders.replace("{name}", "findAll"));
 
 
 		as.variables.put("", "public var id:int");
 		for(PropertyDescriptor prop : model.getProperties().values()) {
 			if(prop.isAttr()) {
 				as.variables.put(prop.variable(), "public var " + prop.variable() + ":" + flexType(prop.fullType()));
 			}
 			else if(prop.hasOne()) {
 				as.variables.put(prop.variable(), "public var " + prop.variable() + ":" + prop.castType());
 			}
 			else if(prop.hasMany()) {
 				String name = prop.variable();
 				as.methods.put(name, createMethod(as.simpleName, name, false));
 			}
 		}
 
 		as.methods.put("create", createMethod(as.simpleName, "create", true));
 		as.methods.put("update", createMethod(as.simpleName, "update", true));
 		as.methods.put("destroy", createMethod(as.simpleName, "destroy", true));
 
 		as.methods.put("save", source(
 				"public function save(callback:Function = null):void {",
 				" if(id < 1) {",
 				"  create(callback);",
 				" } else {",
 				"  update(callback);",
 				" }",
 				"}"
 		));
 		
 		writeFile(srcFolder, as.getFilePath(), as.toSource());
 	}
 
 	private String createMethod(String type, String name, boolean allowNull) {
 		if(allowNull) {
 			return source(
 					"public function {name}(callback:Function = null):void {",
 					" if(callback != null) {",
 					"  {type}.ro.{name}.addEventListener(\"result\", function(event:ResultEvent):void {",
 					"   {type}.ro.{name}.removeEventListener(\"result\", arguments.callee);",
 					"   callback(event);",
 					"  });",
 					" }",
 					" {type}.ro.{name}(this);",
 					"}"
 				).replace("{name}", name).replace("{type}", type);
 		}
 		else {
 			return source(
 					"public function {name}(callback:Function):void {",
					" {type}.ro.{name}.addEventListener(\"result\", function(event:ResultEvent):void {",
					"  {type}.ro.{name}.removeEventListener(\"result\", arguments.callee);",
					"  callback(event);",
					" });",
 					" {type}.ro.{name}(this);",
 					"}"
 				).replace("{name}", name).replace("{type}", type);
 		}
 	}
 	
 	private void createObserversFile(File src) {
 		String source = getResourceAsString(getClass(), "Observers.as");
 		source = source.replace("{serverUrl}", "http://localhost:8400"); // TODO server URL
 		writeFile(src, "org/oobium/persist/Observers.as", source);
 	}
 	
 	private void createPrefsFile() {
 		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs",
 				"#" + getPrefsFileDate() + "\n" +
 				"eclipse.preferences.version=1\n" +
 				"encoding/<project>=utf-8"
 			);
 	}
 	
 	private void createProjectFile() {
 		String builder;
 		if(flexSdk == null) {
 			builder = 
 				"   <name>com.adobe.flexbuilder.project.flexbuilder</name>\n" +
 				"   <arguments>\n" +
 				"   </arguments>\n";
 		} else {
 			builder = 
 				"   <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n" +
 				"   <triggers>auto,full,incremental,</triggers>\n" +
 				"   <arguments>\n" +
 				"    <dictionary>\n" +
 				"     <key>LaunchConfigHandle</key>\n" +
 				"     <value>&lt;project&gt;/.externalToolBuilders/Flex_Builder.launch</value>\n" +
 				"    </dictionary>\n" +
 				"   </arguments>";
 		}
 		writeFile(project, ".project", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<projectDescription>",
 				" <name>{project}</name>",
 				" <comment></comment>",
 				" <projects>",
 				" </projects>",
 				" <buildSpec>",
 				"  <buildCommand>",
 				builder +
 				"  </buildCommand>",
 				" </buildSpec>",
 				" <natures>",
 				"  <nature>com.adobe.flexbuilder.project.flexlibnature</nature>",
 				"  <nature>com.adobe.flexbuilder.project.actionscriptnature</nature>",
 				" </natures>",
 				"</projectDescription>"
 			).replace("{project}", project.getName()).replace("{builder}", builder));
 	}
 	
 	private void createUserSession(File srcFolder){
 		ActionScriptFile as = new ActionScriptFile();
 		as.packageName = module.packageName(module.models);
 		//as.classMetaTags.add("RemoteClass(alias=\"" + module.packageName(module.models) +".UserSession\")");
 		as.simpleName = "UserSession";
 		as.staticVariables.put("ro", "public static const ro:Object = createRemoteObject()");
 		
 		MethodCreator m0 = new MethodCreator("0UserSession()");
 		m0.addLine("public function UserSession():void{");
 			m0.addLine("ro = new RemoteObject();");
 			m0.addLine("ro.destination = \"UserSessionController\";");
 			m0.addLine("ro.addEventListener(\"fault\", faultHandler);");
 		m0.addLine("}");
 		as.addMethod(m0);
 		
 
 		MethodCreator m1 = new MethodCreator("1login");
 		m1.addLine("public function login(userName:String, password:String, callback:Function):void {");
 			m1.addLine("ro.login.addEventListener(\"result\", callback);");
 			m1.addLine("ro.login(userName, password);");
 		m1.addLine("}");
 		as.addMethod(m1);
 		
 		MethodCreator m2 = new MethodCreator("2logout()");
 		m2.addLine("public function logout(callback:Function):void {");
 			m2.addLine("ro.logout();");
 		m2.addLine("}");
 		as.addMethod(m2);
 		
 		MethodCreator m3 = new MethodCreator("3getUserName()");
 		m3.addLine("public function getUserName(callback:Function):void {");
 			m3.addLine("ro.getUserName.addEventListener(\"result\", callback);");
 			m3.addLine("ro.getUserName();");
 		m3.addLine("}");
 		as.addMethod(m3);
 		
 		MethodCreator m4 = new MethodCreator("4getPassword()");
 		m4.addLine("public function getPassword(callback:Function):void {");
 			m4.addLine("ro.getPassword.addEventListener(\"result\", callback);");
 			m4.addLine("ro.getPassword();");
 		m4.addLine("}");
 		as.addMethod(m4);
 		
 		as.addMethod("5",
 				"public function getSessionId(callback:Function):void {",
 				" ro.getSessionId.addEventListener(\"result\", callback);",
 				" ro.getSessionId();",
 				"}"
 			);
 		
 		//PRIVATE_FUNCTIONS used to make the public functions work
 	
 		MethodCreator m6 = new MethodCreator("6faultHandler()");
 		m6.addLine("private function faultHandler (event:FaultEvent):void {");
 			m6.addLine("Alert.show(event.fault.faultString, 'Error');");
 		m6.addLine("}");
 		as.addMethod(m6);
 		
 		writeFile(srcFolder, as.getFilePath(), as.toSource());
 	}
 	
 	private String flexType(String javaType) {
 		String flexType = flexTypes.get(javaType);
 		if(flexType == null) {
 			return "!unknown type: " + javaType + "!";
 		}
 		return flexType;
 	}
 	
 	public File getProject() {
 		return project;
 	}
 	
 	public void setFlexSdk(String flexSdk) {
 		this.flexSdk = flexSdk;
 	}
 	
 	public void setForce(boolean force) {
 		this.force = force;
 	}
 
 }
