 package net.sf.javascribe.patterns.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.javascribe.api.Attribute;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.config.ComponentBase;
 import net.sf.javascribe.api.config.Property;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.LocatedJavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.java5.Java5ClassDefinition;
 
 /**
  * There are several types of domain-level patterns.  The pattern 
  * @author DCS
  */
 public class DomainLogicCommon {
 
 	/*
 	 * Constants required by domain logic patterns.
 	 */
 	public static final String DOMAIN_LOGIC_IMPLEMENTATION_PREFIX = "net.sf.javascribe.patterns.domain.impl.";
 	public static final String DOMAIN_LOGIC_DEPENDENCIES = "net.sf.javascribe.patterns.domain.dependencies";
 	public static final String DOMAIN_LOGIC_PKG = "net.sf.javascribe.patterns.domain.pkg";
 	public static final String DOMAIN_LOGIC_SERVICE_OBJ = "net.sf.javascribe.patterns.domain.serviceObj";
 	public static final String DOMAIN_LOGIC_LOCATOR_CLASS = "net.sf.javascribe.patterns.domain.locatorClass";
 
 	public static final String OBJ_DOMAIN_LOGIC_OBJECT_NAMES = "net.sf.javascribe.patterns.domain.serviceNames";
 	public static final String DOMAIN_LOGIC_FINALIZER_ENSURED = "net.sf.javascribe.patterns.domain.finalizerEnsured";
 
 	public static void ensureFinalizer(ProcessorContext ctx) throws JavascribeException {
 		String val = (String)ctx.getObject(DOMAIN_LOGIC_FINALIZER_ENSURED);
 		if (val==null) {
 			ComponentBase comp = new DomainLogicFinalComponent();
 			comp.getProperty().add(new Property(DOMAIN_LOGIC_LOCATOR_CLASS,ctx.getProperty(DOMAIN_LOGIC_LOCATOR_CLASS)));
 			ctx.addComponent(comp);
 			ctx.putObject(DOMAIN_LOGIC_FINALIZER_ENSURED, ctx.getProperty(DOMAIN_LOGIC_LOCATOR_CLASS));
 		} else if (!val.equals(ctx.getProperty(DOMAIN_LOGIC_LOCATOR_CLASS))) {
 			throw new JavascribeException("Found inconsistent values for domain logic locator class");
 		}
 	}
 
 	public static List<String> getDomainLogicObjectNames(ProcessorContext ctx) {
 		List<String> ret = (List<String>)ctx.getObject(OBJ_DOMAIN_LOGIC_OBJECT_NAMES);
 		return ret;
 	}
 	
 	public static String getServiceLocatorName(ProcessorContext ctx) throws JavascribeException {
 		return ctx.getRequiredProperty(DOMAIN_LOGIC_LOCATOR_CLASS);
 	}
 
 	public static List<Attribute> getParams(DomainLogicComponent comp,ProcessorContext ctx) throws JavascribeException {
 		List<Attribute> ret = null;
 
 		// Read rule parameters
 		String paramString = comp.getParams();
 		if (paramString.trim().length()>0) {
 			ret = JavascribeUtils.readAttributes(ctx, paramString);
 		} else {
 			ret = new ArrayList<Attribute>();
 		}
 
 		return ret;
 	}
 
 	public static String getServiceObj(DomainLogicComponent comp,ProcessorContext ctx) throws JavascribeException {
 		String ret = null;
 		if (comp.getServiceObj().trim().length()>0) {
 			ret = comp.getServiceObj();
 		} else if (ctx.getProperty(DOMAIN_LOGIC_SERVICE_OBJ)!=null) {
 			ret = ctx.getProperty(DOMAIN_LOGIC_SERVICE_OBJ);
 		} else {
			throw new JavascribeException("Attribute ServiceObj Name must be specified in the component");
 		}
 
 		return ret;
 	}
 	
 	public static LocatedJavaServiceObjectType getDomainObjectType(String serviceObj,ProcessorContext ctx) throws JavascribeException {
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DomainLogicCommon.DOMAIN_LOGIC_PKG));
 		LocatedJavaServiceObjectType serviceType = null;
 		
 		serviceType = (LocatedJavaServiceObjectType)ctx.getType(serviceObj);
 		if (serviceType!=null) {
 			return serviceType;
 		}
 
 		String locatorName = getServiceLocatorName(ctx);
 		serviceType = new LocatedJavaServiceObjectType(pkg+'.'+locatorName,serviceObj,pkg,serviceObj);
 		ctx.getTypes().addType(serviceType);
 		return serviceType;
 	}
 	
 	public static String getDomainLogicPkg(ProcessorContext ctx) throws JavascribeException {
 		return JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DomainLogicCommon.DOMAIN_LOGIC_PKG));
 	}
 
 	public static DomainLogicFile getDomainObjectFile(String serviceObj,ProcessorContext ctx) throws JavascribeException {
 		String pkg = getDomainLogicPkg(ctx);
 		DomainLogicFile serviceFile = null;
 		Java5ClassDefinition serviceClass = null;
 
 		serviceFile = (DomainLogicFile)JsomUtils.getJavaFile(pkg+'.'+serviceObj, ctx);
 		if (serviceFile!=null) return serviceFile;
 		
 		// Add service file
 		serviceFile = new DomainLogicFile(new JavascribeVariableTypeResolver(ctx));
 		serviceFile.setPackageName(pkg);
 		serviceClass = serviceFile.getPublicClass();
 		serviceClass.setClassName(serviceObj);
 		JsomUtils.addJavaFile(serviceFile, ctx);
 		serviceClass = serviceFile.getPublicClass();
 		
 		List<String> services = (List<String>)ctx.getObject(OBJ_DOMAIN_LOGIC_OBJECT_NAMES);
 		if (services==null) {
 			services = new ArrayList<String>();
 			ctx.putObject(OBJ_DOMAIN_LOGIC_OBJECT_NAMES, services);
 		}
 		services.add(serviceObj);
 
 		return serviceFile;
 	}
 /*
 	public static DomainLogicFile getServiceFile(String serviceObj,Java5SourceFile locatorFile,DomainServiceLocatorType locatorType,ProcessorContext ctx) throws JavascribeException {
 		DomainLogicFile ret = null;
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DomainLogicCommon.DOMAIN_LOGIC_PKG));
 
 		ensureService(serviceObj,locatorFile,locatorType,ctx);
 		ret = (DomainLogicFile)JsomUtils.getJavaFile(pkg+'.'+serviceObj, ctx);
 
 		return ret;
 	}
 */
 
 	/*
 	public static LocatedJavaServiceObjectType getServiceType(String serviceObj,Java5SourceFile locatorFile,DomainServiceLocatorType locatorType,ProcessorContext ctx) throws JavascribeException {
 		LocatedJavaServiceObjectType ret = null;
 
 		ensureService(serviceObj,locatorFile,locatorType,ctx);
 		ret = (LocatedJavaServiceObjectType)ctx.getType(serviceObj);
 
 		return ret;
 	}
 	*/
 	
 	/*
 	public static DomainLogicFile getDomainFile(String serviceObj,ProcessorContext ctx) throws JavascribeException {
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DomainLogicCommon.DOMAIN_LOGIC_PKG));
 		DomainLogicFile serviceFile = null;
 		LocatedJavaServiceObjectType serviceType = null;
 		Java5ClassDefinition serviceClass = null;
 
 		serviceFile = (DomainLogicFile)JsomUtils.getJavaFile(pkg+'.'+serviceObj, ctx);
 		if (serviceFile!=null) return serviceFile;
 
 		// Add service file+type, create method on service locator
 		serviceFile = new DomainLogicFile(new JavascribeVariableTypeResolver(ctx));
 		serviceFile.setPackageName(pkg);
 		serviceClass = serviceFile.getPublicClass();
 		serviceClass.setClassName(serviceObj);
 		JsomUtils.addJavaFile(serviceFile, ctx);
 		serviceType = new LocatedJavaServiceObjectType(pkg+'.'+locatorClass.getClassName(),serviceObj,pkg,serviceObj);
 		ctx.getTypes().addType(serviceType);
 		serviceClass = serviceFile.getPublicClass();
 		
 		List<String> services = (List<String>)ctx.getObject(OBJ_DOMAIN_LOGIC_OBJECT_NAMES);
 		if (services==null) {
 			services = new ArrayList<String>();
 			ctx.putObject(OBJ_DOMAIN_LOGIC_OBJECT_NAMES, services);
 		}
 		services.add(serviceObj);
 
 
 		// Add locator method for service
 		try {
 			Java5DeclaredMethod locatorMethod = null;
 			Java5CompatibleCodeSnippet locatorCode = null;
 			locatorMethod = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 			locatorMethod.setStatic(true);
 			locatorMethod.setMethodName("get"+serviceObj);
 			locatorMethod.setReturnType(serviceObj);
 			locatorClass.addMethod(locatorMethod);
 			locatorCode = new Java5CodeSnippet();
 			locatorMethod.setMethodBody(locatorCode);
 			locatorCode.merge(JsomUtils.toJsomCode(serviceType.declare("_service")));
 			locatorCode.merge(JsomUtils.toJsomCode(serviceType.instantiate("_service", null)));
 			locatorCode.append("return _service;\n");
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while building service",e);
 		}
 	}
 		*/
 
 	/*
 	private static DomainServiceLocatorType getLocator(ProcessorContext ctx) {
 		DomainServiceLocatorType ret = null;
 		
 		
 		
 		return ret;
 	}
 	*/
 
 	/*
 	public static Java5SourceFile getServiceLocatorFile(String serviceLocator,ProcessorContext ctx) throws JavascribeException {
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DOMAIN_LOGIC_PKG));
 
 		Java5SourceFile ret = null;
 
 		// Ensure service locator exists
 		ret = JsomUtils.getJavaFile(pkg+'.'+serviceLocator, ctx);
 		if (ret==null) {
 			ret = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx));
 			ret.setPackageName(pkg);
 			ret.getPublicClass().setClassName(serviceLocator);
 			JsomUtils.addJavaFile(ret, ctx);
 		}
 
 		return ret;
 	}
 	*/
 
 	public static DomainServiceLocatorType getServiceLocatorType(String serviceLocator,ProcessorContext ctx) throws JavascribeException {
 		DomainServiceLocatorType ret = null;
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DOMAIN_LOGIC_PKG));
 
 		ret = (DomainServiceLocatorType)ctx.getType(serviceLocator);
 		if (ret==null) {
 			ret = new DomainServiceLocatorType(serviceLocator,pkg,serviceLocator);
 			ctx.getTypes().addType(ret);
 		}
 
 		return ret;
 	}
 	
 }
 
