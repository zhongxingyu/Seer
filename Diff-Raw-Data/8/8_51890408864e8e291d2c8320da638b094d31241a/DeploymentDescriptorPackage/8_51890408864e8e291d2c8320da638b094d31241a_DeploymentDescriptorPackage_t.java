 package org.zend.php.zendserver.deployment.core.descriptor;
 
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 
 public class DeploymentDescriptorPackage {
 
 public static final int PACKAGE_ID = 0;
 	
 	public static final Feature PACKAGE = new Feature("package", null, IModelObject.class, PACKAGE_ID); //$NON-NLS-1$
 	
 	public static final int PKG_TYPE_ID = 38;
 
 	public static final Feature PKG_TYPE = new Feature("type", null, String.class, PKG_TYPE_ID); //$NON-NLS-1$
 	
 	public static final int PKG_NAME_ID = 1;
 	
 	public static final Feature PKG_NAME = new Feature("name", null, String.class, PKG_NAME_ID); //$NON-NLS-1$
 	
 	public static final int SUMMARY_ID = 2;
 	
 	public static final Feature SUMMARY = new Feature("summary", null, String.class, SUMMARY_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int PKG_DESCRIPTION_ID = 3;
 	
 	public static final Feature PKG_DESCRIPTION = new Feature("description", null, String.class, PKG_DESCRIPTION_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int VERSION_RELEASE_ID = 4;
 	
 	public static final Feature VERSION_RELEASE = new Feature("version/release", null, String.class, VERSION_RELEASE_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int VERSION_API_ID = 5;
 	
 	public static final Feature VERSION_API = new Feature("version/api", null, String.class, VERSION_API_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int EULA_ID = 6;
 	
 	public static final Feature EULA = new Feature("eula", null, String.class, EULA_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int ICON_ID = 7;
 	
 	public static final Feature ICON = new Feature("icon", null, String.class, ICON_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DOCROOT_ID = 8;
 	
 	public static final Feature DOCROOT = new Feature("docroot", null, String.class, DOCROOT_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int SCRIPTSDIR_ID = 9;
 	
 	public static final Feature SCRIPTSDIR = new Feature("scriptsdir", null, String.class, SCRIPTSDIR_ID); //$NON-NLS-1$
 	
 	public static final int HEALTHCHECK_ID = 10;
 	
 	public static final Feature HEALTHCHECK = new Feature("healthcheck", null, String.class, HEALTHCHECK_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int APPDIR_ID = 11;
 	
	public static final Feature APPDIR = new Feature("appdir", null, String.class, APPDIR_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_PHP_ID = 12;
 	
 	public static final Feature DEPENDENCIES_PHP = new Feature("dependencies/required/php", null, IModelObject.class, DEPENDENCIES_PHP_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_EXTENSION_ID = 13;
 	
 	public static final Feature DEPENDENCIES_EXTENSION = new Feature("dependencies/required/extension", null, IModelObject.class, DEPENDENCIES_EXTENSION_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_DIRECTIVE_ID = 14;
 	
 	public static final Feature DEPENDENCIES_DIRECTIVE = new Feature("dependencies/required/directive", null, IModelObject.class, DEPENDENCIES_DIRECTIVE_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_ZENDSERVER_ID = 15;
 	
 	public static final Feature DEPENDENCIES_ZENDSERVER = new Feature("dependencies/required/zendserver", null, IModelObject.class, DEPENDENCIES_ZENDSERVER_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_ZENDFRAMEWORK_ID = 16;
 	
 	public static final Feature DEPENDENCIES_ZENDFRAMEWORK = new Feature("dependencies/required/zendframework", null, IModelObject.class, DEPENDENCIES_ZENDFRAMEWORK_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_ZENDFRAMEWORK2_ID = 161;
 	
 	public static final Feature DEPENDENCIES_ZENDFRAMEWORK2 = new Feature("dependencies/required/zendframework2", null, IModelObject.class, DEPENDENCIES_ZENDFRAMEWORK2_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_ZSCOMPONENT_ID = 17;
 	
 	public static final Feature DEPENDENCIES_ZSCOMPONENT = new Feature("dependencies/required/zendservercomponent", null, IModelObject.class, DEPENDENCIES_ZSCOMPONENT_ID); //$NON-NLS-1$
 	
 	public static final int DEPENDENCIES_LIBRARY_ID = 121;
 
 	public static final Feature DEPENDENCIES_LIBRARY = new Feature(
 			"dependencies/required/library", null, IModelObject.class, DEPENDENCIES_LIBRARY_ID); //$NON-NLS-1$
 	
 	public static final int PARAMETERS_ID = 18;
 	
 	public static final Feature PARAMETERS = new Feature("parameters/parameter", null, IModelObject.class, PARAMETERS_ID); //$NON-NLS-1$
 	
 	public static final int VARIABLES_ID = 19;
 	
 	public static final Feature VARIABLES = new Feature("variables/variable", null, IModelObject.class, VARIABLES_ID); //$NON-NLS-1$
 	
 	public static final int PERSISTENT_RESOURCES_ID = 20;
 	
 	public static final Feature PERSISTENT_RESOURCES = new Feature("persistentresources/resource", null, String.class, PERSISTENT_RESOURCES_ID); //$NON-NLS-1$
 	
 	
 
 	public static final int DEPENDENCY_NAME_ID = 21;
 	
 	public static final Feature DEPENDENCY_NAME = new Feature("name", null, String.class, DEPENDENCY_NAME_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCY_EQUALS_ID = 22;	
 	
 	public static final Feature DEPENDENCY_EQUALS = new Feature("equals", null, String.class, DEPENDENCY_EQUALS_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCY_MIN_ID = 23;
 	
 	public static final Feature DEPENDENCY_MIN = new Feature("min", null, String.class, DEPENDENCY_MIN_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCY_MAX_ID = 24;
 	
 	public static final Feature DEPENDENCY_MAX = new Feature("max", null, String.class, DEPENDENCY_MAX_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCY_EXCLUDE_ID = 25;
 	
 	public static final Feature DEPENDENCY_EXCLUDE = new Feature("exclude", null, String.class, DEPENDENCY_EXCLUDE_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	public static final int DEPENDENCY_CONFLICTS_ID = 26;
 	
 	public static final Feature DEPENDENCY_CONFLICTS = new Feature("conflicts", null, String.class, DEPENDENCY_CONFLICTS_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	
 	
 	
 	
 
 	public static final int DISPLAY_ID = 27;
 	public static final Feature DISPLAY = new Feature(null, "display", String.class, DISPLAY_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int REQUIRED_ID = 28;
 	public static final Feature REQUIRED = new Feature(null, "required", Boolean.class, REQUIRED_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int READONLY_ID = 29;
 	public static final Feature READONLY = new Feature(null, "readonly", Boolean.class, READONLY_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int TYPE_ID = 30;
 	public static final Feature TYPE = new Feature(null, "type", String.class, TYPE_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int IDENTICAL_ID = 31;
 	public static final Feature IDENTICAL = new Feature(null, "identical", String.class, IDENTICAL_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int ID_ID = 32;
 	public static final Feature ID = new Feature(null, "id", String.class, ID_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int DEFAULTVALUE_ID = 33;
 	public static final Feature DEFAULTVALUE = new Feature("defaultvalue", null, String.class, DEFAULTVALUE_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int PARAM_DESCRIPTION_ID = 34;
 	public static final Feature PARAM_DESCRIPTION = new Feature("description", null, String.class, PARAM_DESCRIPTION_ID, Feature.SET_EMPTY_TO_NULL); //$NON-NLS-1$
 	public static final int VALIDATION_ID = 35;
 	public static final Feature VALIDATION = new Feature("validation/enums/enum", null, String.class, VALIDATION_ID); //$NON-NLS-1$
 	
 	
 	
 	
 	public static final int VAR_NAME_ID = 36;
 	public static final Feature VAR_NAME = new Feature(null, "name", String.class, VAR_NAME_ID); //$NON-NLS-1$
 	public static final int VALUE_ID = 37;
 	public static final Feature VALUE = new Feature(null, "value", String.class, VALUE_ID); //$NON-NLS-1$
 	
 	
 }
