 package implementor;
 
 import util.TypeUtils;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.TypeVariable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Olga Reva
  * Date: 4/1/13
  * Time: 6:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ClassImplementor implements IImplementor {
 	private Class source;
 	private String name;
 	private Package pack;
 
 	public ClassImplementor(Class source, String implementationName, Package implementationPackage) {
 		this.source = source;
 		this.name = implementationName;
 		this.pack = implementationPackage;
 	}
 
 	/**
 	 * Creates new realization of the source with the given name
 	 */
 	public String implement() {
 		//Package
		String result = (pack != null) ? "package " + pack.getName() + ";\n" : "";
 		//Class
 		result += "public class " + name;
 		if (source.isInterface()) {
 			result += " implements " + source.getName();
 		} else {
 			result += " extends " + source.getName();
 		}
 		result += " {\n";
 		//Constructors
 		result += implementConstructors();
 		//Methods
 		result += implementMethods();
 		//End of the class
 		result += "}";
 		return result;
 	}
 
 	private String implementConstructors() {
 		String result = "";
 		Constructor[] constructors = source.getDeclaredConstructors();
 		for (Constructor c: constructors) {
 			result += new ConstructorImplementor(c, name).implement() + "\n";
 		}
 		result += "\n";
 		return result;
 	}
 
 	private String implementMethods() {
 		String result = "";
 		Method[] methods = source.getDeclaredMethods();
 		for (Method m: methods) {
 			result += new MethodImplementor(m).implement() + "\n";
 		}
 		result += "\n";
 		return result;
 	}
 }
