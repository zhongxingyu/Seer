 package net.sf.javascribe.patterns.domain;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.sf.javascribe.api.Attribute;
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.VariableType;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.api.config.ComponentBase;
 import net.sf.javascribe.langsupport.java.Injectable;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.LocatedJavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.ServiceLocator;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.javascribe.patterns.CorePatternConstants;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5ClassDefinition;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5CompatibleCodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 import org.apache.log4j.Logger;
 
 @Scannable
 @Processor
 public class RetrieveDataRuleProcessor {
 
 	private static final Logger log = Logger.getLogger(RetrieveDataRuleProcessor.class);
 	
 	@ProcessorMethod(componentClass=RetrieveDataRule.class)
 	public void process(RetrieveDataRule comp,ProcessorContext ctx) throws JavascribeException {
 
 		ctx.setLanguageSupport("Java");
 
 		try {
 			// Read service locator name
 			String serviceLocatorName = null;
 			if (comp.getServiceLocator().trim().length()>0) {
 				serviceLocatorName = comp.getServiceLocator();
 			} else if (ctx.getProperty(DomainLogicCommon.DOMAIN_LOGIC_LOCATOR_CLASS)!=null) {
 				serviceLocatorName = ctx.getProperty(DomainLogicCommon.DOMAIN_LOGIC_LOCATOR_CLASS);
 			} else {
 				throw new JavascribeException("Service Locator Name must be specified in the component or in property '"+DomainLogicCommon.DOMAIN_LOGIC_LOCATOR_CLASS+"'");
 			}
 
 			// Read business object name
 			String serviceObjName = null;
 			if (comp.getServiceObj().trim().length()>0) {
 				serviceObjName = comp.getServiceObj();
 			} else if (ctx.getProperty(DomainLogicCommon.DOMAIN_LOGIC_SERVICE_OBJ)!=null) {
 				serviceObjName = ctx.getProperty(DomainLogicCommon.DOMAIN_LOGIC_SERVICE_OBJ);
 			} else {
 				throw new JavascribeException("Service Object Name must be specified in the component or in property '"+DomainLogicCommon.DOMAIN_LOGIC_SERVICE_OBJ+"'");
 			}
 
 			// Read rule name
 			String ruleName = null;
 			ruleName = comp.getRule();
 			if (ruleName.trim().length()==0) {
 				throw new JavascribeException("Attribute 'ruleName' is required on component Retrieve Data Rule");
 			}
 
 			log.info("Processing retrieve data rule '"+serviceObjName+"."+ruleName+"'");
 
 			// Read rule return type
 			String returnType = comp.getReturnType();
 			if (returnType.trim().length()==0) {
 				throw new JavascribeException("Attribute 'returnType' is required on component Retrieve Data Rule");
 			}
 
 			// Read rule parameters
 			String paramString = comp.getParams();
 			List<Attribute> params = null;
 			if (paramString.trim().length()>0) {
 				params = JavascribeUtils.readAttributes(ctx, paramString);
 			} else {
 				params = new ArrayList<Attribute>();
 			}
 
 			// Read rule dependencies
 			List<Attribute> deps = null;
 			String depNames = null;
 			if (comp.getDependencies().trim().length()>0) {
 				depNames = comp.getDependencies();
 				deps = JavascribeUtils.readAttributes(ctx, depNames);
 			} else if (ctx.getProperty(RetrieveDataRule.DOMAIN_LOGIC_DEPENDENCIES)!=null) {
 				depNames = ctx.getProperty(RetrieveDataRule.DOMAIN_LOGIC_DEPENDENCIES);
 				deps = JavascribeUtils.readAttributes(ctx, depNames);
 			} else {
 				deps = new ArrayList<Attribute>();
 			}
 
			log.debug("Found dependencies as '"+depNames+"'");
 			// Get package for domain logic
 			String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(DomainLogicCommon.DOMAIN_LOGIC_PKG));
 
 			Java5SourceFile locatorFile = null;
 			Java5SourceFile serviceFile = null;
 			Java5ClassDefinition locatorClass = null;
 			Java5ClassDefinition serviceClass = null;
 			DomainServiceLocatorType locatorType = null;
 			LocatedJavaServiceObjectType serviceType = null;
 			Java5CompatibleCodeSnippet locatorCode = null;
 
 			// Ensure service locator exists
 			locatorFile = JsomUtils.getJavaFile(pkg+'.'+serviceLocatorName, ctx);
 			if (locatorFile==null) {
 				locatorFile = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx));
 				locatorFile.setPackageName(pkg);
 				locatorFile.getPublicClass().setClassName(serviceLocatorName);
 				JsomUtils.addJavaFile(locatorFile, ctx);
 				locatorType = new DomainServiceLocatorType(serviceLocatorName,pkg,serviceLocatorName);
 				ctx.getTypes().addType(locatorType);
 				locatorClass = locatorFile.getPublicClass();
 			} else {
 				locatorClass = locatorFile.getPublicClass();
 				locatorType = (DomainServiceLocatorType)ctx.getType(serviceLocatorName);
 			}
 
 			// Ensure service exists and get locator method in service locator
 			Java5DeclaredMethod locatorMethod = null;
 			serviceFile = JsomUtils.getJavaFile(pkg+'.'+serviceObjName, ctx);
 			if (serviceFile==null) {
 				// Add service file and create method on service locator
 				serviceFile = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx));
 				serviceFile.setPackageName(pkg);
 				serviceFile.getPublicClass().setClassName(serviceObjName);
 				JsomUtils.addJavaFile(serviceFile, ctx);
 				serviceType = new LocatedJavaServiceObjectType(pkg+'.'+locatorClass.getClassName(),serviceObjName,pkg,serviceObjName);
 				ctx.getTypes().addType(serviceType);
 				serviceClass = serviceFile.getPublicClass();
 
 				// Add locator method for service
 				locatorMethod = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 				locatorMethod.setStatic(true);
 				locatorMethod.setMethodName("get"+serviceObjName);
 				locatorMethod.setReturnType(serviceObjName);
 				locatorClass.addMethod(locatorMethod);
 				locatorCode = new Java5CodeSnippet();
 				locatorMethod.setMethodBody(locatorCode);
 				locatorCode.merge(JsomUtils.toJsomCode(serviceType.declare("_service")));
 				locatorCode.merge(JsomUtils.toJsomCode(serviceType.instantiate("_service", null)));
 
 				DomainObjectFinalizer obj = new DomainObjectFinalizer(serviceObjName);
 				ctx.addComponent(obj);
 			} else {
 				serviceType = (LocatedJavaServiceObjectType)ctx.getType(serviceObjName);
 				serviceClass = serviceFile.getPublicClass();
 				locatorMethod = (Java5DeclaredMethod)locatorClass.getDeclaredMethod("get"+serviceObjName);
 				locatorCode = locatorMethod.getMethodBody();
 			}
 
 			//Java5DeclaredMethod locatorMethod = (Java5DeclaredMethod)locatorClass.getDeclaredMethod("get"+serviceObjName);
 
 			for(Attribute d : deps) {
 				if (!serviceType.getDependancyNames().contains(d.getName())) {
 					String depBigEndian = JavascribeUtils.getUpperCamelName(d.getName());
 					serviceType.getDependancyNames().add(d.getName());
 					Java5DeclaredMethod setter = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					setter.setMethodName("set"+depBigEndian);
 					setter.addArg(d.getType(), d.getName());
 					serviceClass.addMethod(setter);
 					serviceClass.addMemberVariable(d.getName(), d.getType(), null);
 					Java5CodeSnippet code = new Java5CodeSnippet();
 					code.append("this."+d.getName()+" = "+d.getName()+';');
 					setter.setMethodBody(code);
 					VariableType injType = ctx.getType(d.getType());
 					if ((injType==null) || (!(injType instanceof Injectable))) {
 						throw new JavascribeException("Dependency '"+d.getName()+"' is not an injectable type");
 					}
 					Injectable i = (Injectable)injType;
 					JsomUtils.merge(locatorCode, i.getInstance(d.getName(), null));
 //					locatorCode.merge(JsomUtils.declareAndInstantiateObject((JavaServiceObjectType)injType, d.getName(), null));
 					locatorCode.append("_service.set"+depBigEndian+"("+d.getName()+");\n");
 				}
 			}
 			
 			Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 			method.setMethodName(comp.getRule());
 			method.setReturnType(comp.getReturnType());
 			Java5CodeSnippet methodCode = new Java5CodeSnippet();
 			method.setMethodBody(methodCode);
 			serviceClass.addMethod(method);
 			CodeExecutionContext execCtx = new CodeExecutionContext(null,ctx.getTypes());
 
 			for(Attribute param : params) {
 				method.addArg(param.getType(), param.getName());
 				execCtx.addVariable(param.getName(), param.getType());
 			}
 			
 			serviceType.addMethod(JsomUtils.createJavaOperation(method));
 			
 			HashMap<String,String> availableObjects = new HashMap<String,String>();
 			for(Attribute d : deps) {
 				availableObjects.put(d.getType(), d.getName());
 				VariableType type = ctx.getType(d.getType());
 				if ((type instanceof ServiceLocator)) {
 					ServiceLocator loc = (ServiceLocator)type;
 					List<String> getServices = loc.getAvailableServices();
 					for(String g : getServices) {
 						String code = loc.getService(d.getName(), g, execCtx);
 						availableObjects.put(g, code);
 					}
 				}
 			}
 			RuleResolver ruleResolver = RuleResolver.getRuleResolver(returnType, execCtx, availableObjects, ctx);
 			Java5CodeSnippet code = ruleResolver.resolve();
 			if (code==null) {
 				throw new JavascribeException("Couldn't resolve rule - couldn't resolve for return type '"+returnType+"'");
 			}
 			methodCode.merge(code);
 			methodCode.append("return "+JavascribeUtils.getLowerCamelName(returnType)+";\n");
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while processing domain rule",e);
 		}
 
 	}
 }
 
 class DomainObjectFinalizer extends ComponentBase {
 	String serviceObjectName = null;
 
 	public DomainObjectFinalizer(String name) {
 		this.serviceObjectName = name;
 	}
 
 	public String getServiceObjectName() {
 		return serviceObjectName;
 	}
 
 	public int getPriority() { return CorePatternConstants.PRIORITY_RETRIEVE_DATA_RULE+1; }
 
 }
 
 
