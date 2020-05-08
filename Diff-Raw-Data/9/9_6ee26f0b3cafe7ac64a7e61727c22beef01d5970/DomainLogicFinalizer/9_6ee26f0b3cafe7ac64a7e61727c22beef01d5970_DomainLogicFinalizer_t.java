 package net.sf.javascribe.patterns.domain;
 
 import java.util.List;
 
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.Injectable;
 import net.sf.javascribe.langsupport.java.LocatedJavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5ClassDefinition;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 import org.apache.log4j.Logger;
 
 @Scannable
 @Processor
 public class DomainLogicFinalizer {
 
 	Logger log = Logger.getLogger(DomainLogicFinalizer.class);
 
 	/**
 	 * This processor is tasked with building the Service Locator class and service 
 	 * locator methods.
 	 * @param comp
 	 * @param ctx
 	 * @throws JavascribeException
 	 */
 	@ProcessorMethod(componentClass=DomainLogicFinalComponent.class)
 	public void finalizeDomainLogic(DomainLogicFinalComponent comp,ProcessorContext ctx) throws JavascribeException {
 		ctx.setLanguageSupport("Java");
 		
 		log.info("Finalizing domain logic layer");
 		List<String> objs = DomainLogicCommon.getDomainLogicObjectNames(ctx);
 
 		if ((objs==null) || (objs.size()==0)) {
 			log.warn("Found a Domain Logic Finalizer but no Domain Logic classes.");
 			return;
 		}
 
 		String pkg = DomainLogicCommon.getDomainLogicPkg(ctx);
 
 		try {
 			// Create a service locator
 			String serviceLocatorName = DomainLogicCommon.getServiceLocatorName(ctx);
 			Java5SourceFile locatorFile = JsomUtils.createJavaSourceFile(ctx);
 			locatorFile.setPackageName(pkg);
 			Java5ClassDefinition cl = locatorFile.getPublicClass();
 			cl.setClassName(serviceLocatorName);
 			JsomUtils.addJavaFile(locatorFile, ctx);
 
 			DomainServiceLocatorType locatorType = new DomainServiceLocatorType(serviceLocatorName,pkg,serviceLocatorName);
 			ctx.getTypes().addType(locatorType);
 
 			for(String obj : objs) {
 				locatorType.getAvailableServices().add(obj);
 				LocatedJavaServiceObjectType type = (LocatedJavaServiceObjectType)ctx.getType(obj);
 				DomainLogicFile file = DomainLogicCommon.getDomainObjectFile(obj, ctx);
 				
 				String impl = ctx.getProperty(DomainLogicCommon.DOMAIN_LOGIC_IMPLEMENTATION_PREFIX+obj);
 				boolean useImpl = false;
 				if ((file.getPublicClass().isAbstract()) || (file.getPublicClass().isInterface())) {
 					if (impl==null) {
						throw new JavascribeException("Domain Logic implementation class not found for "+obj+" which has abstract methods.  The implementation class must be specified in property '"+DomainLogicCommon.DOMAIN_LOGIC_IMPLEMENTATION_PREFIX+obj+"'");
 					} else {
 						useImpl = true;
 					}
 				}
 				
 				Java5DeclaredMethod locatorMethod = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 				locatorMethod.setMethodName("get"+obj);
 				Java5CodeSnippet locatorCode = new Java5CodeSnippet();
 				locatorMethod.setMethodBody(locatorCode);
 				JsomUtils.merge(locatorCode, type.declare("_ret"));
 				cl.addMethod(locatorMethod);
 				locatorMethod.setReturnType(obj);
 				if (useImpl) {
 					locatorCode.append("_ret = new "+impl+"();\n");
 				} else {
 					JsomUtils.merge(locatorCode, type.instantiate("_ret", null));
 				}
 
 				List<String> deps = file.getDependencies();
 				for(String dep : deps) {
 					String typeName = ctx.getAttributeType(dep);
 					Injectable inj = (Injectable)ctx.getType(typeName);
 					Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 					method.setMethodName("set"+JavascribeUtils.getUpperCamelName(dep));
 					Java5CodeSnippet code = new Java5CodeSnippet();
 					method.setMethodBody(code);
 					file.getPublicClass().addMemberVariable(dep, typeName, null);
 					file.addImport(inj.getImport());
 					code.append("this."+dep+" = "+dep+";\n");
 					file.getPublicClass().addMethod(method);
 					method.addArg(typeName, dep);
 					locatorFile.addImport(inj.getImport());
 					JsomUtils.merge(locatorCode,inj.getInstance(dep, null));
 					locatorCode.append("_ret.set"+(JavascribeUtils.getUpperCamelName(dep))+"("+dep+");\n");
 				}
 				locatorCode.append("return _ret;\n");
 			}
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("Caught a JSOM exception",e);
 		}
 	}
 
 }
 
