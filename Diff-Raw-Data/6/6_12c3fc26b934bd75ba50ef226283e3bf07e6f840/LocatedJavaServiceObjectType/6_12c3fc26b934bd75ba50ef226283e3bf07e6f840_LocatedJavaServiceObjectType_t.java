 package net.sf.javascribe.langsupport.java;
 
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.JavascribeException;
 
 /**
  * A service represents a Java business logic service that is found via a service locator.
  * @author Dave
  */
 public class LocatedJavaServiceObjectType extends JavaServiceObjectType implements Injectable {
 	protected String locatorClass = null;
 	
 	public LocatedJavaServiceObjectType(String locatorClass,String serviceName,String pkg,String className) {
 		super(serviceName,pkg,className);
 		this.locatorClass = locatorClass;
 	}
 
 	public String getLocatorClass() {
 		return locatorClass;
 	}
 	
 	@Override
 	public JavaCode getInstance(String varName,CodeExecutionContext execCtx) throws JavascribeException {
 		JavaCodeImpl ret = new JavaCodeImpl();
 
 		ret.addImport(getImport());
 		ret.appendCodeText(varName+" = new "+locatorClass+"().get"+getName()+"();\n");
 
 		return ret;
 	}
 	
 	
 }
