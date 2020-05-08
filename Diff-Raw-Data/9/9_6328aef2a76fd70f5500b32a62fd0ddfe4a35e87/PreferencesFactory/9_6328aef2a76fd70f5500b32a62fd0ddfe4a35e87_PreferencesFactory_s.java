 package org.eclipse.imp.prefspecs.compiler.codegen;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.preferences.IPreferencesService;
 import org.eclipse.imp.preferences.PreferencesService;
 import org.eclipse.imp.preferences.PreferencesTab;
 import org.eclipse.imp.preferences.PreferencesUtilities;
 import org.eclipse.imp.preferences.TabbedPreferencesPage;
 import org.eclipse.imp.preferences.fields.BooleanFieldEditor;
 import org.eclipse.imp.preferences.fields.DirectoryListFieldEditor;
 import org.eclipse.imp.preferences.fields.FieldEditor;
 import org.eclipse.imp.preferences.fields.FileFieldEditor;
 import org.eclipse.imp.preferences.fields.IntegerFieldEditor;
 import org.eclipse.imp.preferences.fields.StringFieldEditor;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteBooleanFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteDirListFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteFileFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteIntFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteStringFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.IPreferencesGeneratorData;
 import org.eclipse.imp.prefspecs.pageinfo.PreferencesPageInfo;
 import org.eclipse.imp.prefspecs.pageinfo.PreferencesTabInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualBooleanFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualIntFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualStringFieldInfo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Link;
 
 public class PreferencesFactory implements IPreferencesFactory
 {
 	protected TabbedPreferencesPage prefsPage;	
 	protected PreferencesTab prefsTab;
 	protected IPreferencesService prefsService;
 	protected String tabLevel;
 	protected Composite parent;
 	protected PreferencesUtilities prefUtils;
 	protected IPreferencesGeneratorData generatorData;
 	
 	private final String createFieldsErrorPrefix =
 		"PreferencesFactory.createFields:  IllegalArgumentException:  ";
 
 	
 	public PreferencesFactory(
 		TabbedPreferencesPage page,
 		PreferencesTab tab,
 		IPreferencesService service,
 		IPreferencesGeneratorData generatorData)
 	{
 		if (service == null) {	
 			throw new IllegalArgumentException("PreferencesFactory(): preferences service is null; not allowed");
 		}
 		// TODO:  Add checks for other inputs
 		this.prefsService = service;
 		this.prefsTab = tab;	
 		this.prefsPage = page;
 		this.generatorData = generatorData;
 		
 		prefUtils = new PreferencesUtilities(service);
 	}
 	
 
 
 	public static IFile generatePreferencesConstants(
 		PreferencesPageInfo pageInfo,
 		ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 	{
 		// Generate file text
 		String fileText = generateConstantsPartBeforeFields(packageName, className);
 		fileText = generateConstantsFields(pageInfo, fileText);
 		fileText = generateConstantsAfterFields(fileText);
 
 		IFile constantsFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 		return constantsFile;
 	}
 	
 	
 	public static IFile generatePreferencesInitializers(
 		PreferencesPageInfo pageInfo,
 		String pluginProjectName, String pluginClassName, String constantsClassName,
 		ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 	{
 		System.out.println("PreferencesFactory.generatePreferencesInitializers():  generating (insofar as implemented)");
 		
 		// Generate file text
 		String fileText = generateInitializersPartBeforeFields(pluginProjectName, pluginClassName, packageName, className);
 		fileText = generateInitializersFields(pageInfo, constantsClassName, fileText);
 		fileText = generateInitializersAfterFields(fileText);
 		
 		IFile initializersFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 		return initializersFile;
 		
 	}
 
 	
 	
 	public static IFile generateDefaultTab(
 		PreferencesPageInfo pageInfo,
 		String pluginProjectName, String pluginClassName, String constantsClassName, String initializerClassName,
 		ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 	{
 		System.out.println("PreferencesFactory.generateDefaultTab():  generating (insofar as implemented)");
 		
 		// Generate file text
 		String fileText = generateTabBeforeFields(pluginProjectName, pluginClassName, packageName, className, initializerClassName, IPreferencesService.DEFAULT_LEVEL);
 		fileText = generateTabFields(pageInfo, constantsClassName, fileText, IPreferencesService.DEFAULT_LEVEL);
 		fileText = generateTabAfterFields(fileText);
 		
 		IFile initializersFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 		return initializersFile;
 	}
 	
 	
 	public static IFile generateConfigurationTab(
 			PreferencesPageInfo pageInfo,
 			String pluginProjectName, String pluginClassName, String constantsClassName,
 			ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 	{
 		System.out.println("PreferencesFactory.generateConfigurationTab():  generating (insofar as implemented)");
 		
 		// Generate file text
 		String fileText = generateTabBeforeFields(pluginProjectName, pluginClassName, packageName, className, null, IPreferencesService.CONFIGURATION_LEVEL);
 		fileText = generateTabFields(pageInfo, constantsClassName, fileText, IPreferencesService.CONFIGURATION_LEVEL);
 		fileText = generateTabAfterFields(fileText);
 		
 		IFile initializersFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 		return initializersFile;
 	}
 	
 	
 	
 	public static IFile generateInstanceTab(
 			PreferencesPageInfo pageInfo,
 			String pluginProjectName, String pluginClassName, String constantsClassName,
 			ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 		{
 			System.out.println("PreferencesFactory.generateInstanceTab():  generating (insofar as implemented)");
 			
 			// Generate file text
 			String fileText = generateTabBeforeFields(pluginProjectName, pluginClassName, packageName, className, null, IPreferencesService.INSTANCE_LEVEL);
 			fileText = generateTabFields(pageInfo, constantsClassName, fileText, IPreferencesService.INSTANCE_LEVEL);
 			fileText = generateTabAfterFields(fileText);
 			
 			IFile initializersFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 			return initializersFile;
 		}
 		
 		
 		public static IFile generateProjectTab(
 				PreferencesPageInfo pageInfo,
 				String pluginProjectName, String pluginClassName, String constantsClassName,
 				ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 		{
 			System.out.println("PreferencesFactory.generateProjectTab():  generating (insofar as implemented)");
 			
 			// Generate file text
 			String fileText = generateTabBeforeFields(pluginProjectName, pluginClassName, packageName, className, null, IPreferencesService.PROJECT_LEVEL);
 			fileText = generateTabFields(pageInfo, constantsClassName, fileText, IPreferencesService.PROJECT_LEVEL);
 			fileText = generateTabAfterFields(fileText);
 			fileText = regenerateEndOfProjectTab(pageInfo, fileText);
 			
 			IFile initializersFile = createFileWithText(fileText, project, projectSourceLocation, packageName, className, mon);
 			return initializersFile;
 		}
 		
 	
 	
 	
 	public FieldEditor[] createFields(				//Composite parent, String tabName)
 			TabbedPreferencesPage page,
 			PreferencesTab tab,
 			String level,
 			Composite parent,
 			IPreferencesService prefsService)
 	{
 		// Check parameters
 		if (parent == null) {
 			throw new IllegalArgumentException(createFieldsErrorPrefix + "Composite 'parent' is null; not allowed");
 		}
 		if (level == null) {
 			throw new IllegalArgumentException(createFieldsErrorPrefix + "Tab name is null; not allowed");
 		}
 		if (!prefsService.isaPreferencesLevel(level)) {
 			throw new IllegalArgumentException(createFieldsErrorPrefix + "tab name is not valid");
 		}
 		
 		tabLevel = level;
 		this.parent = parent;	
 		
 		List<FieldEditor> result = new ArrayList();
 		FieldEditor[] resultArray = null;
 		BooleanFieldEditor boolField = null;
 //		if (level.equals(IPreferencesService.DEFAULT_LEVEL)) {
 //			resultArray = createFields(page, tab, level, parent, prefsService);
 //		} else if (level.equals(IPreferencesService.CONFIGURATION_LEVEL)) {
 //			resultArray = createFields(IPreferencesService.CONFIGURATION_LEVEL);
 //		} else if (level.equals(IPreferencesService.INSTANCE_LEVEL)) {
 //			resultArray = createFields(IPreferencesService.INSTANCE_LEVEL);
 //		} else if (level.equals(IPreferencesService.PROJECT_LEVEL)) {
 //			resultArray = createFields(IPreferencesService.PROJECT_LEVEL);
 //		}
 //		return resultArray;
 		
 		// For the final return
 		//FieldEditor[] resultArray = null;
 		// To accumulate incremental results
 		List<FieldEditor> resultList = new ArrayList();
 		
 		// Get info on the fields to construct
 		PreferencesPageInfo pageInfo = generatorData.getPageInfo();
 		//tabLevel = tab;
 		PreferencesTabInfo tabInfo = pageInfo.getTabInfo(level);
 		Iterator fieldsIter = tabInfo.getConcreteFields();
 		
 		while (fieldsIter.hasNext()) {
 			FieldEditor field = null;
 			ConcreteFieldInfo fieldInfo = (ConcreteFieldInfo) fieldsIter.next();
 			if (fieldInfo instanceof ConcreteBooleanFieldInfo) {
 				field = createFieldEditor((ConcreteBooleanFieldInfo)fieldInfo); 
 			} else if (fieldInfo instanceof ConcreteDirListFieldInfo) {
 				field = createFieldEditor((ConcreteDirListFieldInfo)fieldInfo);
 			} else if (fieldInfo instanceof ConcreteFileFieldInfo) {
 				field = createFieldEditor((ConcreteFileFieldInfo)fieldInfo); 
 			} else if (fieldInfo instanceof ConcreteIntFieldInfo) {
 				field = createFieldEditor((ConcreteIntFieldInfo)fieldInfo); 
 			} else if (fieldInfo instanceof ConcreteStringFieldInfo) {
 				field = createFieldEditor((ConcreteStringFieldInfo)fieldInfo); 
 			} else {
 				System.err.println("PreferencesFieldFactory.createFields(" + tab + "):  got unrecognized field-info kind");
 			} 
 			
 			if (field != null) {
 				resultList.add(field);
 			}
 		}
 		
 		resultArray = new FieldEditor[resultList.size()];
 		for (int i = 0; i < resultList.size(); i++) {
 			resultArray[i] = resultList.get(i);
 		}
 		return resultArray;
 		
 	}
 
 	
 //	protected FieldEditor[] createFields(String tab)
 //	{
 //		// For the final return
 //		FieldEditor[] resultArray = null;
 //		// To accumulate incremental results
 //		List<FieldEditor> resultList = new ArrayList();
 //		
 //		// Get info on the fields to construct
 //		PreferencesPageInfo pageInfo = generatorData.getPageInfo();
 //		tabLevel = tab;
 //		PreferencesTabInfo tabInfo = pageInfo.getTabInfo(tabLevel);
 //		Iterator fieldsIter = tabInfo.getConcreteFields();
 //		
 //		while (fieldsIter.hasNext()) {
 //			FieldEditor field = null;
 //			ConcreteFieldInfo fieldInfo = (ConcreteFieldInfo) fieldsIter.next();
 //			if (fieldInfo instanceof ConcreteBooleanFieldInfo) {
 //				field = createFieldEditor((ConcreteBooleanFieldInfo)fieldInfo); 
 //			} else {
 //				System.err.println("PreferencesFieldFactory.createFields(" + tab + "):  got unrecognized field-info kind");
 //			}
 //			
 //			if (field != null) {
 //				resultList.add(field);
 //			}
 //		}
 //		
 //		resultArray = new FieldEditor[resultList.size()];
 //		for (int i = 0; i < resultList.size(); i++) {
 //			resultArray[i] = resultList.get(i);
 //		}
 //		return resultArray;
 //	}
 //	
 	
 	// SMS 22 Jul 2007:  These forms are not really used at the moment ...
 		
 	protected FieldEditor createFieldEditor(ConcreteBooleanFieldInfo fieldInfo)				
 	{
 		BooleanFieldEditor boolField =
 			prefUtils.makeNewBooleanField(
 			   		prefsPage, prefsTab, prefsService,
 					tabLevel, fieldInfo.getName(), fieldInfo.getName(),			// tab level, key, text
 					parent,
 					fieldInfo.getIsEditable(), fieldInfo.getIsEditable(),		// enabled, editable (treat as same)
 					fieldInfo.getHasSpecialValue(), fieldInfo.getSpecialValue(),
 					false, false,												// empty allowed (always false for boolean), empty (irrelevant)
 					fieldInfo.getIsRemovable());								// false for default tab but not necessarily any others
 		Link fieldDetailsLink = prefUtils.createDetailsLink(parent, boolField, boolField.getChangeControl().getParent(), "Details ...");
 		
 		return boolField;
 	}
 
 
 	
 	protected FieldEditor createFieldEditor(ConcreteDirListFieldInfo fieldInfo)				
 	{
 		DirectoryListFieldEditor dirlistField =
 			prefUtils.makeNewDirectoryListField(
 			   		prefsPage, prefsTab, prefsService,
 					tabLevel, fieldInfo.getName(), fieldInfo.getName(),			// tab level, key, text
 					parent,
 					fieldInfo.getIsEditable(), fieldInfo.getIsEditable(),		// enabled, editable (treat as same)
 					fieldInfo.getHasSpecialValue(), fieldInfo.getSpecialValue(),
 					fieldInfo.getEmptyValueAllowed(), fieldInfo.getEmptyValue(),													// empty allowed (always false for boolean), empty (irrelevant)
 					fieldInfo.getIsRemovable());								// false for default tab but not necessarily any others
 		Link fieldDetailsLink = prefUtils.createDetailsLink(parent, dirlistField, dirlistField.getTextControl().getParent(), "Details ...");
 		
 		return dirlistField;
 	}
 	
 	
 
 	protected FieldEditor createFieldEditor(ConcreteFileFieldInfo fieldInfo)				
 	{
 		FileFieldEditor fileField =
 			prefUtils.makeNewFileField(
 			   		prefsPage, prefsTab, prefsService,
 					tabLevel, fieldInfo.getName(), fieldInfo.getName(),			// tab level, key, text
 					parent,
 					fieldInfo.getIsEditable(), fieldInfo.getIsEditable(),		// enabled, editable (treat as same)
 					fieldInfo.getHasSpecialValue(), fieldInfo.getSpecialValue(),
 					fieldInfo.getEmptyValueAllowed(), fieldInfo.getEmptyValue(),	// empty allowed (always false for boolean), empty (irrelevant)
 					fieldInfo.getIsRemovable());								// false for default tab but not necessarily any others
 		Link fieldDetailsLink = prefUtils.createDetailsLink(parent, fileField, fileField.getTextControl().getParent(), "Details ...");
 		
 		return fileField;
 	}
 
 
 	protected FieldEditor createFieldEditor(ConcreteIntFieldInfo fieldInfo)				
 	{
 		IntegerFieldEditor intField =
 			prefUtils.makeNewIntegerField(
 			   		prefsPage, prefsTab, prefsService,
 					tabLevel, fieldInfo.getName(), fieldInfo.getName(),			// tab level, key, text
 					parent,
 					fieldInfo.getIsEditable(), fieldInfo.getIsEditable(),		// enabled, editable (treat as same)
 					fieldInfo.getHasSpecialValue(), String.valueOf(fieldInfo.getSpecialValue()),
 					false, "0",								// empty allowed (always false for boolean, int, with empty irrelevant)
 					fieldInfo.getIsRemovable());								// false for default tab but not necessarily any others
 		Link fieldDetailsLink = prefUtils.createDetailsLink(parent, intField, intField.getTextControl().getParent(), "Details ...");
 		
 		return intField;
 	}
 	
 	protected FieldEditor createFieldEditor(ConcreteStringFieldInfo fieldInfo)				
 	{
 		StringFieldEditor stringField =
 			prefUtils.makeNewStringField(
 			   		prefsPage, prefsTab, prefsService,
 					tabLevel, fieldInfo.getName(), fieldInfo.getName(),			// tab level, key, text
 					parent,
 					fieldInfo.getIsEditable(), fieldInfo.getIsEditable(),		// enabled, editable (treat as same)
 					fieldInfo.getHasSpecialValue(), fieldInfo.getSpecialValue(),
 					fieldInfo.getEmptyValueAllowed(), fieldInfo.getEmptyValue(),													// empty allowed (always false for boolean), empty (irrelevant)
 					fieldInfo.getIsRemovable());								// false for default tab but not necessarily any others
 		Link fieldDetailsLink = prefUtils.createDetailsLink(parent, stringField, stringField.getTextControl().getParent(), "Details ...");
 		
 		return stringField;
 	}
 	// etc. for other field types
 	
 		
 	// ???
 //	protected void addFieldToPage(FieldEditor field) {
 //		
 //	}
 	
 	
 	/*
 	 * Subroutines to generate parts of the preferences constants class
 	 */
 	
 	
 	protected static String generateConstantsPartBeforeFields(String packageName, String className)
 	{
 		if (className.endsWith(".java")) {
 			className = className.substring(0, className.length()-5);
 		}
 
 		String fileText = "package " + packageName + ";\n\n";
 		fileText = fileText + "/**\n";
 		fileText = fileText + " * Constant definitions for preferences.\n";
 		fileText = fileText + " *\n";
 		fileText = fileText + " * The preferences service uses Strings as keys for preference values,\n";
 		fileText = fileText + " * so Strings defined here are used here to designate preference fields.\n";
 		fileText = fileText + " * These strings are generated automatically from a preferences specification.\n";
 		fileText = fileText + " * Other constants may be defined here manually.\n";
 		fileText = fileText + " *\n";
 		fileText = fileText + " */\n";
 		fileText = fileText + "\n\n";
 		fileText = fileText + "public class " + className + " {\n";
 		
 		return fileText;
 	}
 	
 	
 	protected static String generateConstantsFields(PreferencesPageInfo pageInfo, String fileText)
 	{
 		Iterator vFields = pageInfo.getVirtualFieldInfos();
 		while (vFields.hasNext()) {
 			VirtualFieldInfo vField = (VirtualFieldInfo) vFields.next();
 			fileText = fileText + "\n\tpublic static final String P_" +
 				vField.getName().toUpperCase() + " = \"" + vField.getName() + "\""+ ";\n";
 		}			
 		return fileText;		
 	}
 
 	
 	protected static String generateConstantsAfterFields(String fileText) {
 		return fileText + "\n}\n";
 	}
 	
 	
 	/*
 	 * Subroutines to generate parts of the preferences initializations class
 	 */
 	
 	
 	protected static String generateInitializersPartBeforeFields(
 			String pluginPackageName, String pluginClassName, String packageName, String className)
 	{
 		if (className.endsWith(".java")) {
 			className = className.substring(0, className.length()-5);
 		}
 
 		String fileText = "package " + packageName + ";\n\n";
 		fileText = fileText + "import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;\n";
 		fileText = fileText + "import org.eclipse.imp.preferences.IPreferencesService;\n";
 		fileText = fileText + "import " + pluginPackageName + "." + pluginClassName + ";\n\n";
 		fileText = fileText + "/**\n";
 		fileText = fileText + " * Initializations of default values for preferences.\n";
 		fileText = fileText + " */\n";
 		fileText = fileText + "\n\n";
 		fileText = fileText + "public class " + className + " extends AbstractPreferenceInitializer {\n";
 		fileText = fileText + "\t/*\n";
 		fileText = fileText + "\t * (non-Javadoc)\n";
 		fileText = fileText + "\t * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()\n";
 		fileText = fileText + "\t */\n";
 		fileText = fileText + "\tpublic void initializeDefaultPreferences() {\n";
 		fileText = fileText + "\t\tIPreferencesService service = " + pluginClassName + ".getPreferencesService();\n\n";
 		
 		return fileText;
 	}
 	
 	
 	// Examples:
 	//service.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, PreferenceConstants.P_EMIT_MESSAGES, getDefaultEmitMessages());
 
 	
 	protected static String generateInitializersFields(PreferencesPageInfo pageInfo, String constantsClassName, String fileText)
 	{
 		Iterator vFields = pageInfo.getVirtualFieldInfos();
 		while (vFields.hasNext()) {
 			VirtualFieldInfo vField = (VirtualFieldInfo) vFields.next();
 			if (vField instanceof VirtualBooleanFieldInfo) {
 				VirtualBooleanFieldInfo vBool = (VirtualBooleanFieldInfo) vField;
 				fileText = fileText + "\t\tservice.setBooleanPreference(IPreferencesService.DEFAULT_LEVEL, " +
 										constantsClassName + "." + preferenceConstantForName(vBool.getName()) + ", " +
 										vBool.getDefaultValue() + ");\n";
 			} else if (vField instanceof VirtualIntFieldInfo) {
 				// Int fields are a subtype of String fields, but int values are stored
 				// separately in the preferences service
 				VirtualIntFieldInfo vInt = (VirtualIntFieldInfo) vField;
 				fileText= fileText + "\t\tservice.setIntPreference(IPreferencesService.DEFAULT_LEVEL, " +
 									constantsClassName + "." + preferenceConstantForName(vInt.getName()) + ", " +
 									vInt.getDefaultValue() + ");\n";
 			} else if (vField instanceof VirtualStringFieldInfo) {
 				// Subsumes subtypes of VirtualStringFieldInfo
 				VirtualStringFieldInfo vString = (VirtualStringFieldInfo) vField;
 				fileText= fileText + "\t\tservice.setStringPreference(IPreferencesService.DEFAULT_LEVEL, " +
 									constantsClassName + "." + preferenceConstantForName(vString.getName()) + ", " +
 									vString.getDefaultValue() + ");\n";
 			} else {
 				fileText = fileText + "\t\t//Encountered unimplemented initialization for field = " + vField.getName() + "\n";
 			}
 		}			
 		return fileText;		
 	}
 
 	
 	protected static String generateInitializersAfterFields(String fileText) {
 		// Note:  first closing brace in text is for the initializeDefaultPreferences method
 		return fileText + "\t}\n}\n";
 	}
 	
 	
 	
 	
 	/*
 	 * Subroutines to generate parts of the default tab class
 	 */
 	
 	
 	protected static String generateTabBeforeFields(
 			String pluginPackageName, String pluginClassName, String packageName, String className, String initializerClassName, String levelName)
 	{
 		if (className.endsWith(".java")) {
 			className = className.substring(0, className.length()-5);
 		}
 		levelName = levelName.toLowerCase();
 		String levelNameUpperInitial = levelName.substring(0, 1).toUpperCase() + levelName.substring(1, levelName.length());
 
 		String fileText = "package " + packageName + ";\n\n";
 		fileText = fileText + "import java.util.List;\n";
 		fileText = fileText + "import java.util.ArrayList;\n";
 		fileText = fileText + "import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;\n";
 		fileText = fileText + "import org.eclipse.core.runtime.preferences.IEclipsePreferences;\n";
 		fileText = fileText + "import org.eclipse.swt.widgets.Composite;\n";
 		fileText = fileText + "import org.eclipse.swt.widgets.Link;\n";
 		fileText = fileText + "import org.eclipse.imp.preferences.*;\n";
 		fileText = fileText + "import org.eclipse.imp.preferences.fields.*;\n";
 		fileText = fileText + "import org.osgi.service.prefs.Preferences;\n";
 		fileText = fileText + "\n//		 TODO:  Import additional classes for specific field types from\n";
 		fileText = fileText + "//		 org.eclipse.uide.preferences.fields";
 		fileText = fileText + "\n\n/**\n";
 		fileText = fileText + " * The " + levelName + " level preferences tab.\n";
 		fileText = fileText + " */\n";
 		fileText = fileText + "\n\n";
 		fileText = fileText + "public class " + className + " extends " + levelNameUpperInitial + "PreferencesTab {\n\n";
 		
 		fileText = fileText + "\tpublic " + className + "(IPreferencesService prefService) {\n";
 		fileText = fileText + "\t\tsuper(prefService);\n\t}\n\n";
 		
 		if (initializerClassName != null) {
 			fileText = fileText + "\t/**\n";
 			fileText = fileText + "\t * Creates a language-specific preferences initializer.\n";
 			fileText = fileText + "\t *\n";
 			fileText = fileText + "\t * @return    The preference initializer to be used to initialize\n";
 			fileText = fileText + "\t *            preferences in this tab\n";
 			fileText = fileText + "\t */\n";
 			fileText = fileText + "\tpublic AbstractPreferenceInitializer getPreferenceInitializer() {\n";
 			fileText = fileText + "\t\t" + initializerClassName +	" preferencesInitializer = new " + initializerClassName + "();\n";
 			fileText = fileText + "\t\treturn preferencesInitializer;\n";
 			fileText = fileText + "\t}\n\n";
 		}
 		
 		fileText = fileText + "\t/**\n";
 		fileText = fileText + "\t * Creates specific preference fields with settings appropriate to\n";
 		fileText = fileText + "\t * the " + levelName + " preferences level.\n";
 		fileText = fileText + "\t *\n";
 		fileText = fileText + "\t * Overrides an unimplemented method in PreferencesTab.\n";
 		fileText = fileText + "\t *\n";
 		fileText = fileText + "\t * @return    An array that contains the created preference fields\n";
 		fileText = fileText + "\t *\n";
 		fileText = fileText + "\t */\n";
 		fileText = fileText + "\tprotected FieldEditor[] createFields(\n";
 		fileText = fileText + "\t\tTabbedPreferencesPage page, PreferencesTab tab, String tabLevel,\n";
 		fileText = fileText + "\t\tComposite parent, IPreferencesService prefsService)\n\t{\n";
 		fileText = fileText + "\t\tList fields = new ArrayList();\n";
 
 		return fileText;
 	}
 	
 
 	
 	protected static String generateTabFields(PreferencesPageInfo pageInfo, String constantsClassName, String fileText, String tabLevel)
 	{
 		PreferencesTabInfo tabInfo = pageInfo.getTabInfo(tabLevel);
 		Iterator cFields = tabInfo.getConcreteFields();
 
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 
 			if (cFieldInfo instanceof ConcreteBooleanFieldInfo) {	
 				ConcreteBooleanFieldInfo cBoolFieldInfo = (ConcreteBooleanFieldInfo) cFieldInfo;
 				fileText = fileText + getTextToCreateBooleanField(pageInfo, cBoolFieldInfo, tabLevel);
 			} else if (cFieldInfo instanceof ConcreteIntFieldInfo) {
 				ConcreteIntFieldInfo cIntFieldInfo = (ConcreteIntFieldInfo) cFieldInfo;
 				fileText = fileText + getTextToCreateIntegerField(pageInfo, cIntFieldInfo, tabLevel);	
 			} else if (cFieldInfo instanceof ConcreteStringFieldInfo) {
 				// Subsumes subtypes of ConcreteStringFieldInfo
 				ConcreteStringFieldInfo cStringFieldInfo = (ConcreteStringFieldInfo) cFieldInfo;
 				fileText = fileText + getTextToCreateStringField(pageInfo, cStringFieldInfo, tabLevel);
 			} else {
 				fileText = fileText + "\t\t//Encountered unimplemented initialization for field = " + cFieldInfo.getName() + "\n\n";
 			}
 			// SMS 16 Aug 2007
 			if (cFieldInfo.getIsConditional()) {
 				fileText = fileText + generateFieldToggleText(cFieldInfo, fileText);
 			}
 		}
 		return fileText;		
 	}	
 
 	
 	
 	protected static String generateFieldToggleText(ConcreteFieldInfo cFieldInfo, String fileText)
 	{
 		boolean onProjectLevel = cFieldInfo.getParentTab().getName().equals(PreferencesService.PROJECT_LEVEL);
 		
 		String condFieldName = cFieldInfo.getConditionField().getName();
 		String result = "\n";
 		// Initialize the sense of the toggle-field listener
 		result = result + "\t\tprefUtils.createToggleFieldListener(" +
 			condFieldName + ", " +  cFieldInfo.getName() + ", " +
 			(cFieldInfo.getConditionalWith() ? "true" : "false") + ");\n";
 		
 		// Initialize the string that represents the value to which the enabled state
 		// of the field is set (given that it is enabled conditionally)
 		String enabledValueString = null;
 		if (onProjectLevel) {
 			enabledValueString = "false;\n";
 		} else if (cFieldInfo.getConditionalWith()) {
 			enabledValueString = condFieldName + ".getBooleanValue();\n";
 		} else {
 			enabledValueString = "!" + condFieldName + ".getBooleanValue();\n";
 		}
 		
 		String enabledFieldName = "isEnabled" + cFieldInfo.getName();
 		
 		result = result + "\t\tboolean " + enabledFieldName + " = " + enabledValueString;
 		if (cFieldInfo instanceof ConcreteStringFieldInfo) {
 			result = result + "\t\t" + cFieldInfo.getName() + ".getTextControl().setEditable(" + enabledFieldName +  ");\n";
 			result = result + "\t\t" + cFieldInfo.getName() + ".getTextControl().setEnabled(" + enabledFieldName +  ");\n";
 			result = result + "\t\t" + cFieldInfo.getName() + ".setEnabled(" + enabledFieldName + ", " + cFieldInfo.getName() + ".getParent());\n\n";
 		} else if (cFieldInfo instanceof ConcreteBooleanFieldInfo) {
 			result = result + "\t\t" + cFieldInfo.getName() + ".getChangeControl().setEnabled(" + enabledFieldName +  ");\n";
 			result = result + "\t\t" + cFieldInfo.getName() + ".setEnabled(" + enabledFieldName + ", " + cFieldInfo.getName() + ".getParent());\n\n";
 		}
 		// TODO:  May need to address other filed types if and when
 		// they're added
 
 		/*
 		 * Example target code:	
 		prefUtils.createToggleFieldListener(useDefaultGenIncludePathField, includeDirectoriesField, false);
 		value = !useDefaultGenIncludePathField.getBooleanValue();
 		includeDirectoriesField.getTextControl().setEditable(value);
 		includeDirectoriesField.getTextControl().setEnabled(value);
 		includeDirectoriesField.setEnabled(value, includeDirectoriesField.getParent());
 		*/
 		
 		return result;
 	}
 	
 	
 	protected static String getTextToCreateBooleanField(
 		PreferencesPageInfo pageInfo, ConcreteBooleanFieldInfo fieldInfo, String tabLevel	)
 	{
 		boolean editable = tabLevel.equals(PreferencesService.PROJECT_LEVEL) ? false : fieldInfo.getIsEditable();
 		
 		String result = "\n";
 		result = result + "\t\tBooleanFieldEditor " + fieldInfo.getName() + " = prefUtils.makeNewBooleanField(\n";
 		result = result + "\t\t\tpage, tab, prefsService,\n";
 		result = result + "\t\t\t\"" + tabLevel + "\", \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getName() + "\",\n";	// tab level, key, text\n";
 		result = result + "\t\t\tparent,\n";
 		result = result + "\t\t\t" + editable + ", " + editable + ",\n";		// enabled, editable (treat as same)\n";
 		result = result + "\t\t\t" + fieldInfo.getHasSpecialValue() + ", " + fieldInfo.getSpecialValue() + ",\n";
 		result = result + "\t\t\tfalse, false,\n";										// empty allowed (always false for boolean), empty (irrelevant)
 		result = result + "\t\t\t" + fieldInfo.getIsRemovable() + ");\n";	// false for default tab but not necessarily any others\n";
 		result = result + "\t\t\tLink " + fieldInfo.getName() + "DetailsLink = prefUtils.createDetailsLink(parent, " +
 							fieldInfo.getName() + ", " + fieldInfo.getName() + ".getChangeControl().getParent()" + ", \"Details ...\");\n\n";	
 		result = result + "\t\tfields.add(" + fieldInfo.getName() + ");\n\n";
 		return result;
 	}
 	
 	
 	protected static String getTextToCreateIntegerField(
 			PreferencesPageInfo pageInfo, ConcreteIntFieldInfo fieldInfo, String tabLevel	)
 		{
 		    boolean editable = tabLevel.equals(PreferencesService.PROJECT_LEVEL) ? false : fieldInfo.getIsEditable();
 		
 			String result = "\n";
 			result = result + "\t\tIntegerFieldEditor " + fieldInfo.getName() + " = prefUtils.makeNewIntegerField(\n";
 			
 			result = result + "\t\t\tpage, tab, prefsService,\n";
 			result = result + "\t\t\t\"" + tabLevel + "\", \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getName() + "\",\n";	// tab level, key, text\n";
 			result = result + "\t\t\tparent,\n";
 			result = result + "\t\t\t" + editable + ", " + editable + ",\n";		// enabled, editable (treat as same)\n";
 			result = result + "\t\t\t" + fieldInfo.getHasSpecialValue() + ", String.valueOf(" + fieldInfo.getSpecialValue() + "),\n";
 			result = result + "\t\t\tfalse, \"0\",\n";										// empty allowed, empty value
 			result = result + "\t\t\t" + fieldInfo.getIsRemovable() + ");\n";	// false for default tab but not necessarily any others\n";
 			result = result + "\t\t\tLink " + fieldInfo.getName() + "DetailsLink = prefUtils.createDetailsLink(parent, " +
 								fieldInfo.getName() + ", " + fieldInfo.getName() + ".getTextControl().getParent()" + ", \"Details ...\");\n\n";	
 			result = result + "\t\tfields.add(" + fieldInfo.getName() + ");\n\n";
 			return result;
 		}
 		
 	
 	/**
 	 * Returns the text needed to create a field of type String or one of the
 	 * supported subtypes of type String
 	 * 
 	 * @param pageInfo
 	 * @param fieldInfo
 	 * @param tabLevel
 	 * @return
 	 */
 	protected static String getTextToCreateStringField(
 		PreferencesPageInfo pageInfo, ConcreteStringFieldInfo fieldInfo, String tabLevel)
 	{
 	    boolean editable = tabLevel.equals(PreferencesService.PROJECT_LEVEL) ? false : fieldInfo.getIsEditable();
 		
 		String result = "\n";
 		if (fieldInfo instanceof ConcreteDirListFieldInfo) {
 			result = result + "\t\tDirectoryListFieldEditor " + fieldInfo.getName() + " = prefUtils.makeNewDirectoryListField(\n";
 		} else if (fieldInfo instanceof ConcreteFileFieldInfo) {
 			result = result + "\t\tFileFieldEditor " + fieldInfo.getName() + " = prefUtils.makeNewFileField(\n";
 		} else if (fieldInfo instanceof ConcreteStringFieldInfo) {
 			result = result + "\t\tStringFieldEditor " + fieldInfo.getName() + " = prefUtils.makeNewStringField(\n";
 		}
 		result = result + "\t\t\tpage, tab, prefsService,\n";
 		result = result + "\t\t\t\"" + tabLevel + "\", \"" + fieldInfo.getName() + "\", \"" + fieldInfo.getName() + "\",\n";	// tab level, key, text\n";
 		result = result + "\t\t\tparent,\n";
 		result = result + "\t\t\t" + editable + ", " + editable + ",\n";		// enabled, editable (treat as same)\n";
 		result = result + "\t\t\t" + fieldInfo.getHasSpecialValue() + ", \"" + stripQuotes(fieldInfo.getSpecialValue()) + "\",\n";
 		result = result + "\t\t\t" + fieldInfo.getEmptyValueAllowed() + ", \"" + stripQuotes(fieldInfo.getEmptyValue()) + "\",\n";										// empty allowed, empty value
 		result = result + "\t\t\t" + fieldInfo.getIsRemovable() + ");\n";	// false for default tab but not necessarily any others\n";
 		result = result + "\t\t\tLink " + fieldInfo.getName() + "DetailsLink = prefUtils.createDetailsLink(parent, " +
 							fieldInfo.getName() + ", " + fieldInfo.getName() + ".getTextControl().getParent()" + ", \"Details ...\");\n\n";	
 		result = result + "\t\tfields.add(" + fieldInfo.getName() + ");\n\n";
 		return result;
 	}
 	
 
 
 	
 
 	protected static String generateTabAfterFields(String fileText)
 	{
 		fileText = fileText + "\t\tFieldEditor[] fieldsArray = new FieldEditor[fields.size()];\n";
 		fileText = fileText + "\t\tfor (int i = 0; i < fields.size(); i++) {\n";
 		fileText = fileText + "\t\t\tfieldsArray[i] = (FieldEditor) fields.get(i);\n";
 		fileText = fileText + "\t\t}\n";
 		fileText = fileText + "\t\treturn fieldsArray;\n";
 		
 		// Note:  first closing brace in text is for the createFields method
 		return fileText + "\t}\n}\n";
 	}
 
 	
 	protected static String regenerateEndOfProjectTab(PreferencesPageInfo pageInfo, String fileText)
 	{
 		// Assuming that the given text represents a complete class,
 		// "erase" the closing brace
 		fileText = fileText.substring(0, fileText.lastIndexOf("}")) + "\n\n";
 
 		// Generate first field-independent part of the addressProjectSelection method
 		fileText = fileText + "\tprotected void addressProjectSelection(IPreferencesService.ProjectSelectionEvent event, Composite composite)\n";
 		fileText = fileText + "\t{\n";
 		fileText = fileText + "\t\tboolean haveCurrentListeners = false;\n\n";
 		fileText = fileText + "\t\tPreferences oldeNode = event.getPrevious();\n";
 		fileText = fileText + "\t\tPreferences newNode = event.getNew();\n\n";
 		
 		fileText = fileText + "\t\tif (oldeNode == null && newNode == null) {\n";		
 		fileText = fileText + "\t\t\t// Happens sometimes when you clear the project selection.\n";
 		fileText = fileText + "\t\t\t// Nothing, really, to do in this case ...\n";
 		fileText = fileText + "\t\t\treturn;\n";
 		fileText = fileText + "\t\t}\n\n";
 		
 		fileText = fileText + "\t\t// If oldeNode is not null, we want to remove any preference-change listeners from it\n";
 		fileText = fileText + "\t\tif (oldeNode != null && oldeNode instanceof IEclipsePreferences && haveCurrentListeners) {\n";
 		fileText = fileText + "\t\t\tremoveProjectPreferenceChangeListeners();\n";
 		fileText = fileText + "\t\t\thaveCurrentListeners = false;\n";
 		fileText = fileText + "\t\t} else {\n";
 		fileText = fileText + "\t\t\t// Print an advisory message if you want to\n";
 		fileText = fileText + "\t\t}\n\n";
 		
 		// Generate code to declare a local variable for each field
 		// (to simplify subsequent references)
 		
 		fileText = fileText + "\t\t// Declare local references to the fields\n";
 		PreferencesTabInfo tabInfo = pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL);
 		Iterator cFields = tabInfo.getConcreteFields();
 		int i = 0;
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 			String fieldTypeName = null;
 			if (cFieldInfo instanceof ConcreteBooleanFieldInfo) {
 				fieldTypeName = "BooleanFieldEditor";
 			} else if (cFieldInfo instanceof ConcreteDirListFieldInfo) {
 				fieldTypeName = "DirectoryListFieldEditor";
 			} else if (cFieldInfo instanceof ConcreteFileFieldInfo) {
 				fieldTypeName = "FileFieldEditor";
 			} else if (cFieldInfo instanceof ConcreteIntFieldInfo) {
 				fieldTypeName = "IntegerFieldEditor";
 			} else if (cFieldInfo instanceof ConcreteStringFieldInfo) {
 				fieldTypeName = "StringFieldEditor";
 			} else {
 				fieldTypeName = "UnrecognizedFieldType";
 			}
 			fileText = fileText + "\t\t" + fieldTypeName + " " + cFieldInfo.getName() + " = (" + fieldTypeName + ") fields[" + i++ + "];\n";
 		}	
 		fileText += "\n";
 		
 //		fileText = fileText + "\t\tBooleanFieldEditor useDefaultExecutable = (BooleanFieldEditor) fields[0];
 //		fileText = fileText + "\t\tBooleanFieldEditor useDefaultClasspath  = (BooleanFieldEditor) fields[1];
 //		BooleanFieldEditor emitDiagnostics      = (BooleanFieldEditor) fields[2];
 //		BooleanFieldEditor generateLog          = (BooleanFieldEditor) fields[3];
 
 		fileText = fileText + "\t\t// Declare a 'holder' for each preference field; not strictly necessary\n";
 		fileText = fileText + "\t\t// but helpful in various manipulations of fields and controls to follow\n";
 		
 		
 		// Generate a 'holder' for each field (for ease of expression in later uses of fields)
 		
 		tabInfo = pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL);
 		cFields = tabInfo.getConcreteFields();
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 			fileText = fileText + "\t\tComposite " + cFieldInfo.getName() + "Holder = null;\n";
 		}			
 
 		
 		// Generate next block of field-independent text
 
 		fileText = fileText + "\t\t// If we have a new project preferences node, then do various things\n";
 		fileText = fileText + "\t\t// to set up the project's preferences\n";
 		fileText = fileText + "\t\tif (newNode != null && newNode instanceof IEclipsePreferences) {\n";
 		fileText = fileText + "\t\t\t// Set project name in the selected-project field\n";
 		fileText = fileText + "\t\t\tselectedProjectName.setStringValue(newNode.name());\n\n";
 	
 		fileText = fileText + "\t\t\t// If the containing composite is not disposed, then set field values\n";
 		fileText = fileText + "\t\t\t// and make them enabled and editable (as appropriate to the type of field)\n\n";
 	
 		fileText = fileText + "\t\t\tif (!composite.isDisposed()) {\n";		
 		fileText = fileText + "\t\t\t\t// Note:  Where there are toggles between fields, it is a good idea to set the\n";
 		fileText = fileText + "\t\t\t\t// properties of the dependent field here according to the values they should have\n";
 		fileText = fileText + "\t\t\t\t// based on the independent field.  There should be listeners to take care of \n";
 		fileText = fileText + "\t\t\t\t// that sort of adjustment once the tab is established, but when properties are\n";
 		fileText = fileText + "\t\t\t\t// first initialized here, the properties may not always be set correctly through\n";
 		fileText = fileText + "\t\t\t\t// the toggle.  I'm not entirely sure why that happens, except that there may be\n";
 		fileText = fileText + "\t\t\t\t// a race condition between the setting of the dependent values by the listener\n";
 		fileText = fileText + "\t\t\t\t// and the setting of those values here.  If the values are set by the listener\n";
 		fileText = fileText + "\t\t\t\t// first (which might be surprising, but may be possible) then they will be\n";
 		fileText = fileText + "\t\t\t\t// overwritten by values set here--so the values set here should be consistent\n";
 		fileText = fileText + "\t\t\t\t// with what the listener would set.\n\n";
 		
		fileText = fileText + "\t\t\t\t// Used in setting enabled and editable status\n";
		fileText = fileText + "\t\t\t\tboolean enabledState = false;\n\n";

 			
 		// Generate code for the (field-specific) initialization and enabling of each field
 		// For conditionally enabled fields, attempts to account for the conditionally enabling
 		// field
 		tabInfo = pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL);
 		cFields = tabInfo.getConcreteFields();
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 			String fieldName = cFieldInfo.getName();
 			String holderName = fieldName + "Holder";
 			String enabledRepresentation = null;
 			if (!cFieldInfo.getIsConditional()) {
 				// simple case--enabled state is what it is
 				enabledRepresentation = Boolean.toString(cFieldInfo.getIsEditable());
 			} else {
 				// have to represent the setting with or against the condition field
				enabledRepresentation = cFieldInfo.getConditionField().getName() + ".getChangeControl().getEnabled()";
 				if (!cFieldInfo.getConditionalWith())
 					enabledRepresentation = "!" + enabledRepresentation;
 			}
 
 			if (cFieldInfo instanceof ConcreteBooleanFieldInfo) {
 				fileText = fileText + "\t\t\t\t" + holderName + " = " + fieldName + ".getChangeControl().getParent();\n";
 				fileText = fileText + "\t\t\t\tprefUtils.setField(" + fieldName	 + ", " + holderName + ");\n";
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getChangeControl().setEnabled(" + enabledRepresentation + ");\n\n";
 			} else if (cFieldInfo instanceof ConcreteIntFieldInfo ||
 					   cFieldInfo instanceof ConcreteStringFieldInfo)
 			{
 				fileText = fileText + "\t\t\t\t" + holderName + " = " + fieldName + ".getTextControl().getParent();\n";	
 				fileText = fileText + "\t\t\t\tprefUtils.setField(" + fieldName	 + ", " + holderName + ");\n";
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getTextControl().setEditable(" + enabledRepresentation + ");\n";
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getTextControl().setEnabled(" + enabledRepresentation + ");\n";
 				fileText = fileText + "\t\t\t\t" + fieldName + ".setEnabled(" + enabledRepresentation + ", " + fieldName + ".getParent());\n\n";
 			} // etc.
 		}	
 		fileText = fileText + "\t\t\t\tclearModifiedMarksOnLabels();\n"; 
 		fileText = fileText + "\t\t\t}\n\n";	// closes if not disposed ...
 		
 		
 		// Generate code to create a property-change listener for each field
 
 		fileText = fileText + "\t\t\t// Add property change listeners\n";
 		tabInfo = pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL);
 		cFields = tabInfo.getConcreteFields();
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 			String fieldName = cFieldInfo.getName();
 			String holderName = fieldName + "Holder";
 			fileText = fileText + 
 				"\t\t\tif (" + holderName + " != null) addProjectPreferenceChangeListeners(" + 
 								fieldName + ", \"" + fieldName + "\", " + holderName + ");\n";
 		}
 		
 		fileText = fileText + "\n\t\t\thaveCurrentListeners = true;\n";
 		fileText = fileText + "\t\t}\n\n";
 
 		
 
 		// Generate field-independent code for disabling fields
 
 		fileText = fileText + "\t\t// Or if we don't have a new project preferences node ...\n";
 		fileText = fileText + "\t\tif (newNode == null || !(newNode instanceof IEclipsePreferences)) {\n";
 		fileText = fileText + "\t\t\t// May happen when the preferences page is first brought up, or\n";
 		fileText = fileText + "\t\t\t// if we allow the project to be deselected\\nn";
 
 		fileText = fileText + "\t\t\t// Unset project name in the tab\n";
 		fileText = fileText + "\t\t\tselectedProjectName.setStringValue(\"none selected\");\n\n";
 
 		fileText = fileText + "\t\t\t// Clear the preferences from the store\n";
 		fileText = fileText + "\t\t\tprefService.clearPreferencesAtLevel(IPreferencesService.PROJECT_LEVEL);\n\n";
 
 		fileText = fileText + "\t\t\t// Disable fields and make them non-editable\n";
 		fileText = fileText + "\t\t\tif (!composite.isDisposed()) {\n";
 					
 		// Generate field-dependent code for disabling fields
 		tabInfo = pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL);
 		cFields = tabInfo.getConcreteFields();
 		while (cFields.hasNext()) {
 			ConcreteFieldInfo cFieldInfo = (ConcreteFieldInfo) cFields.next();
 			String fieldName = cFieldInfo.getName();
 			if (cFieldInfo instanceof ConcreteBooleanFieldInfo) {
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getChangeControl().setEnabled(false);\n\n";
 			} else if (cFieldInfo instanceof ConcreteIntFieldInfo ||
 					   cFieldInfo instanceof ConcreteStringFieldInfo)
 			{
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getTextControl().setEditable(false);\n";
 				fileText = fileText + "\t\t\t\t" + fieldName + ".getTextControl().setEnabled(false);\n";
 				// I think we want the following also
 				fileText = fileText + "\t\t\t\t" + fieldName + ".setEnabled(false, " + fieldName + ".getParent());\n\n";
 			} // etc.
 		}
 		fileText = fileText + "\t\t\t}\n\n";
 		
 		
 		// Generate code to remove listeners
 		fileText = fileText + "\t\t\t// Remove listeners\n";
 		fileText = fileText + "\t\t\tremoveProjectPreferenceChangeListeners();\n";
 		fileText = fileText + "\t\t\thaveCurrentListeners = false;\n";
 		
 		// To help assure that field properties are established properly
 		fileText = fileText + "\t\t\t// To help assure that field properties are established properly\n";
 		fileText = fileText + "\t\t\tperformApply();\n";
 		fileText = fileText + "\t\t}\n";	// close for if newnode ==  null ...
 		fileText = fileText + "\t}\n\n";		// close for method
 
 		// Close class
 		fileText = fileText + "\n}\n";
 		return fileText;
 	}
 	
 	
 	/*
 	 * Utility subroutines
 	 */
 	
 	
 	protected static IFile createFileWithText(
 			String fileText, ISourceProject project, String projectSourceLocation, String packageName, String className, IProgressMonitor mon)
 	{
 		// Find or create the folder to contain the file		
 		IFolder packageFolder = null;
 		String packageFolderName = packageName.replace(".", "/");
 		String createdPath = null;
 		String[] pathSegs = (projectSourceLocation + packageFolderName).split("/");
 		for (int i = 0; i < pathSegs.length; i++) {
 			if (createdPath == null)
 				createdPath = pathSegs[i];
 			else
 				createdPath = createdPath + "/" + pathSegs[i];
 			packageFolder = project.getRawProject().getFolder(createdPath);
 			try {
 				if (!packageFolder.exists()) {
 					packageFolder.create(true, true, mon);
 					if (!packageFolder.exists()) {	
 						System.err.println("PreferencesFactory.createFileWithText(): cannot find or create package folder; returning null" +
 								"\tpackage folder = " + packageFolder.getLocation().toString());
 						return null;
 					}
 				}
 			} catch (CoreException e) {
 				System.err.println("PreferencesFactory.createFileWithText(): CoreException finding or creating package folder; returning null" +
 						"\tpackage folder = " + packageFolder.getLocation().toString());
 				return null;
 			}
 			
 		}
 		
 //		IFolder packageFolder = project.getRawProject().getFolder(projectSourceLocation + packageFolderName);
 //		try {
 //			if (!packageFolder.exists()) {
 //				packageFolder.create(true, true, mon);
 //				if (!packageFolder.exists()) {	
 //					System.err.println("PreferencesFactory.createFileWithText(): cannot find or create package folder; returning null" +
 //							"\tpackage folder = " + packageFolder.getLocation().toString());
 //					return null;
 //				}
 //			}
 //		} catch (CoreException e) {
 //			System.err.println("PreferencesFactory.createFileWithText(): CoreException finding or creating package folder; returning null" +
 //					"\tpackage folder = " + packageFolder.getLocation().toString());
 //			return null;
 //		}
 		
 		// Find or create the file to contain the text,
 		// and put the text into it
 		String fileName = className;
 		if (!fileName.endsWith(".java"))
 			fileName += ".java";
 		IFile file = packageFolder.getFile(fileName);
 		try {
 			if (file.exists()) {
 				file.setContents(new ByteArrayInputStream(fileText.getBytes()), true, true, mon);
 			} else {
 			    file.create(new ByteArrayInputStream(fileText.getBytes()), true, mon);
 			}
 		} catch (CoreException e) {
 			System.err.println("PreferencesFactory.createFileWithText(): CoreException creating file; returning null");
 			return null;
 		}
 		
 		return file;
 	}
 	
 	
 	protected static String preferenceConstantForName(String  name) {
 		return "P_" + name.toUpperCase();
 	}
 	
 	
 	public static String stripQuotes(String s)
 	{
 		if (s == null) 
 			return null;
 		if (s.length() == 0)
 			return s;
 		if (s.length() == 1) {
 			if (s.charAt(0) == '"')
 				return "";
 			else
 				return s;
 		}
 		
 		int newStart, newEnd;
 		if (s.charAt(0) == '"')
 			newStart = 1;
 		else
 			newStart = 0;
 		
 		if (s.charAt(s.length()-1) == '"')
 			newEnd = s.length()-1;
 		else
 			newEnd = s.length();
 		
 		return s.substring(newStart, newEnd);
 	}
 	
 }
