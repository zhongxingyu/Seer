 package net.sf.javascribe.patterns.service;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import net.sf.javascribe.api.Attribute;
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaBeanType;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomJavaBeanType;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5DataObjectSourceFile;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 @Scannable
 @Processor
 public class ServiceProcessor {
 
 	private static final Logger log = Logger.getLogger(ServiceProcessor.class);
 
 	public static final String SERVICE_PKG = "net.sf.javascribe.patterns.service.Service.pkg";
 
 	@ProcessorMethod(componentClass=Service.class)
 	public void process(Service service,ProcessorContext ctx) throws JavascribeException {
 		String servicePkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(SERVICE_PKG));
 		String serviceName = service.getModule()+"Service";
 		
 		ctx.setLanguageSupport("Java");
 
 		try {
 			JavaServiceObjectType serviceType = null;
 			Java5SourceFile file = null;
 
 			log.info("Processing service "+service.getModule()+"."+service.getName());
 
 			String serviceResult = handleResult(ctx,service,servicePkg);
 
 			file = JsomUtils.getJavaFile(servicePkg+'.'+serviceName, ctx);
 			if (file==null) {
 				file = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx.getTypes()));
 				file.setPackageName(servicePkg);
 				file.getPublicClass().setClassName(serviceName);
 				JsomUtils.addJavaFile(file, ctx);
 			}
 			serviceType = (JavaServiceObjectType)ctx.getTypes().getType(serviceName);
 			if (serviceType==null) {
 				serviceType = new JavaServiceObjectType(serviceName,servicePkg,serviceName);
 				serviceType.setClassName(serviceName);
 				serviceType.setPkg(servicePkg);
 				ctx.getTypes().addType(serviceType);
 			}
 
 			handleService(ctx,service,serviceType,file,serviceResult);
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while processing service",e);
 		}
 	}
 
 	private void handleService(ProcessorContext ctx,Service service,JavaServiceObjectType type,Java5SourceFile src,String resultName) throws CodeGenerationException,JavascribeException {
 		List<Attribute> params = null;
 		Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 		CodeExecutionContext execCtx = new CodeExecutionContext(null,ctx.getTypes());
 
 		params = JavascribeUtils.readAttributes(ctx, service.getParams());
 
 		method.setMethodName(service.getName());
 		method.setReturnType(resultName);
 		for(Attribute att : params) {
 			method.addArg(att.getType(), att.getName());
 			execCtx.addVariable(att.getName(), att.getType());
 		}
 		src.getPublicClass().addMethod(method);
 		Java5CodeSnippet code = new Java5CodeSnippet();
 		code.append(resultName+" returnValue = new "+resultName+"();\n");
 		code.append("returnValue.setStatus(0);\n");
 		execCtx.addVariable("returnValue", resultName);
 
 		// Read service operations
 		appendServiceCode(ctx,service,code,execCtx);
 
 		code.append("return returnValue;");
 		method.setMethodBody(code);
 		type.addMethod(JsomUtils.createJavaOperation(method));
 	}
 
 	private void appendServiceCode(ProcessorContext ctx,Service service,Java5CodeSnippet code,CodeExecutionContext execCtx) throws CodeGenerationException,JavascribeException {
 		List<ServiceOperation> ops = service.getServiceOperation();
 		Java5CodeSnippet addition = null;
 
 		addition = processOperations(ctx,ops,execCtx);
 		code.merge(addition);
 	}
 
 	private Java5CodeSnippet processOperations(ProcessorContext ctx,List<ServiceOperation> ops,CodeExecutionContext execCtx) throws CodeGenerationException,JavascribeException {
 		Java5CodeSnippet ret = new Java5CodeSnippet();
 		ServiceOperationRenderer renderer = null;
 		NestingServiceOperationRenderer nestingRenderer = null;
 		Java5CodeSnippet addition = null;
 		NestingOperation nestingOp = null;
 
 		for(ServiceOperation op : ops) {
 			renderer = op.getRenderer();
 			if (renderer==null) {
 				throw new CodeGenerationException("Service Operation "+op+" does not have a code renderer");
 			}
 			renderer.setGeneratorContext(ctx);
 			addition = renderer.getCode(execCtx);
 			ret.merge(addition);
 			if (op instanceof NestingOperation) {
 				if (!(renderer instanceof NestingServiceOperationRenderer)) {
 					throw new CodeGenerationException("Found a nesting service operation without a nesting service operation renderer: "+op);
 				}
 				nestingOp = (NestingOperation)op;
 				nestingRenderer = (NestingServiceOperationRenderer)renderer;
 				CodeExecutionContext subExecCtx = new CodeExecutionContext(execCtx, execCtx.getTypes());
 				addition = processOperations(ctx,nestingOp.getOperation(), subExecCtx);
 				ret.merge(addition);
 				addition = nestingRenderer.endingCode(execCtx);
 				ret.merge(addition);
 			}
 		}
 
 		return ret;
 	}
 
 	private String handleResult(ProcessorContext ctx,Service service,String servicePkg) throws JavascribeException {
 		JsomJavaBeanType obj = null;
 		Java5DataObjectSourceFile src = new Java5DataObjectSourceFile(new JavascribeVariableTypeResolver(ctx.getTypes()));
 		String resultName = null;
 
 		try {
 			resultName = service.getName();
 			resultName = Character.toUpperCase(service.getName().charAt(0))+service.getName().substring(1);
 			resultName = resultName + "ServiceResult";
 			log.debug("Creating service result type '"+resultName+"'");
 			obj = new JsomJavaBeanType(resultName,servicePkg,resultName);
 			ctx.getTypes().addType(obj);
 
 			src.setPackageName(servicePkg);
 			src.getPublicClass().setClassName(resultName);
 			JsomUtils.addJavaFile(src, ctx);
 
 			// Add status, validationMessages, message
 			obj.addAttribute("validationMessages", "list/string");
 			obj.addAttribute("status", "integer");
 			obj.addAttribute("message", "string");
 			src.addJavaBeanProperty("message", "string");
 			src.addJavaBeanProperty("status", "integer");
 			src.addJavaBeanProperty("validationMessages", "list/string");
 
 			// Add status, validationMessages, and message attributes
 			ctx.addAttribute("status", "integer");
 			ctx.addAttribute("validationMessages", "list/string");
 			ctx.addAttribute("message", "string");
 
 			List<ServiceOperation> ops = service.getServiceOperation();
 			readOperationsForResult(ctx,obj, src, ops);
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while building service result type",e);
 		}
 		return resultName;
 	}
 
 	private void readOperationsForResult(ProcessorContext ctx,JavaBeanType obj, Java5DataObjectSourceFile src, 
 			List<ServiceOperation> ops) throws JavascribeException,CodeGenerationException {
 		for(ServiceOperation op : ops) {
 			if (op instanceof ResultServiceOperation) {
 				String resultType,resultName;
 				ResultServiceOperation res = (ResultServiceOperation)op;
 				resultType = res.getResultType(ctx);
 				if (resultType!=null) {
 					resultName = res.getResultName(ctx);
 					if ((resultName!=null) && (resultName.startsWith("returnValue."))) {
 						resultName = resultName.substring(12);
 						if (obj.getAttributeType(resultName)==null) {
 							if ((ctx.getAttributeType(resultName)!=null) && 
 									(!ctx.getAttributeType(resultName).equals(resultType))) {
								throw new JavascribeException("Found inconsistent types for attribute '"+resultName+"'");
 							}
 							ctx.addAttribute(resultName, resultType);
 							obj.addAttribute(resultName, resultType);
 							src.addJavaBeanProperty(resultName, resultType);
 						}
 					}
 				}
 			}
 			if (op instanceof NestingOperation) {
 				NestingOperation nes = (NestingOperation)op;
 				readOperationsForResult(ctx,obj,src,nes.getOperation());
 			}
 		}
 
 	}
 
 }
 
