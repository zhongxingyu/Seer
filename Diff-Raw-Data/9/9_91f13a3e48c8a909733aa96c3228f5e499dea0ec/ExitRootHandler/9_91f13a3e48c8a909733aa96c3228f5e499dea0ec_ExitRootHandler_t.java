 package org.collage.dom.evaluator.java.javassist;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import org.collage.dom.creationhandler.DomNodeCreationHandlerCV;
 import org.collage.dom.evaluator.common.StringHandlerCV;
 import org.collage.dom.evaluator.java.independent.JavaTemplateCmdCV;
 import org.collage.template.TemplateSource;
 import org.collage.template.TextTemplateCompiler;
 import org.xcommand.core.IXCommand;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
 class ExitRootHandler implements IXCommand
 {
 	public void execute(Map aCtx)
 	{
		CtClass cc = null;
 		try
 		{
 			ClassPool pool = ClassPool.getDefault();
 			String className = getClassName();
 
 			CtClass ccXcommand = pool.get("org.xcommand.core.IXCommand");
			cc = pool.makeClass(className);
 			cc.addInterface(ccXcommand);
 
 			// Add method 'appendVar()':
 			addMethod(cc, "org/collage/dom/evaluator/java/javassist/appendvar.txt", new HashMap());
 
 			// Add method 'execute()':
 			StringBuffer sb = (StringBuffer) aCtx.get("methodbody");
 			HashMap ctx = new HashMap(); ctx.put("execute_method_body", sb.toString());
 			addMethod(cc, "org/collage/dom/evaluator/java/javassist/execute_method.txt", ctx);
 
 			Class clazz = cc.toClass();
 			aCtx.put("clazz", clazz);
 
 			// Create instance and put it on context:
 			Object obj = clazz.newInstance();
 			JavaTemplateCmdCV.setTemplateComand(aCtx, (IXCommand) obj);
 		}
 		catch (Exception e)
 		{
			if (cc != null)
			{
				System.err.println("class source:\n" + cc.toString());
			}
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void addMethod(CtClass aCtClass, String aFilename, Map aCtx) throws Exception
 	{
 		DomNodeCreationHandlerCV.setProduceJavaSource(aCtx, Boolean.FALSE);
 		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(aFilename);
 
 		IXCommand cmd = new TextTemplateCompiler().newTemplateCommand(new TemplateSource(is, aCtx));
 		cmd.execute(aCtx);
 		String s = StringHandlerCV.getString(aCtx);
 		is.close();
 //		System.out.println("methodstring: " + s);
 		CtMethod ctm = CtNewMethod.make(s, aCtClass);
 		aCtClass.addMethod(ctm);
 	}
 
 	private String getClassName()
 	{
 		Calendar cal = new GregorianCalendar();
 		String s = "";
 		s += "_" + cal.get(Calendar.YEAR);
 		s += "-" + cal.get(Calendar.MONTH);
 		s += "-" + cal.get(Calendar.DAY_OF_MONTH);
 		s += "_" + cal.get(Calendar.HOUR_OF_DAY);
 		s += "-" + cal.get(Calendar.MINUTE);
 		s += "-" + cal.get(Calendar.SECOND);
 		s += "-" + cal.get(Calendar.MILLISECOND);
 		String className = "JavassistTemplate" + s;
 //		System.out.println(className);
 		return className;
 	}
 }
