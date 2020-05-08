 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.MethodInvocation;
 
 import callgraphanalyzer.CallGraphResources;
 
 public class Clazz
 {
 	private File					file;
 	private String					name;
 	private boolean					isInterface;
 	private List<Method>			methods;
 	
 	private int						startChar;
 	private int						endChar;
 
 	private List<Clazz>				interfaces;
 	private List<Clazz>				implementedBy;
 	private List<String>			unresolvedInterfaces;
 
 	private Clazz					superClazz;
 	private String					unresolvedSuperClazz;
 
 	private List<String>			genericTypes;
 
 	private List<Mapping>			variables;
 
 	private List<MethodInvocation>	invocations;
 	public Clazz()
 	{
 		methods = new ArrayList<Method>();
 		interfaces = new ArrayList<Clazz>();
 		implementedBy = new ArrayList<Clazz>();
 		unresolvedInterfaces = new ArrayList<String>();
 		unresolvedSuperClazz = "";
 		genericTypes = new ArrayList<String>();
 		variables = new ArrayList<Mapping>();
 
 		invocations = new ArrayList<MethodInvocation>();
 
 	}
 
 	public Clazz(String name, boolean isInterface, List<Method> methods,
 			ArrayList<Clazz> interfaces, Clazz superClazz,
 			List<Clazz> subClazzes)
 	{
 		this.name = name;
 		this.isInterface = isInterface;
 		this.methods = methods;
 		this.interfaces = interfaces;
 		this.superClazz = superClazz;
 
 		unresolvedInterfaces = new ArrayList<String>();
 		unresolvedSuperClazz = "";
 
 		implementedBy = new ArrayList<Clazz>();
 
 		genericTypes = new ArrayList<String>();
 		variables = new ArrayList<Mapping>();
 
 		invocations = new ArrayList<MethodInvocation>();
 	}
 
 	/**
 	 * This function returns the fully qualified type of the given field.
 	 * 
 	 * @param callGraph
 	 * @param variable
 	 * @return
 	 */
 	public String lookupField(CallGraph callGraph, String variable)
 	{
 		for (Clazz clazz = this; clazz != null; clazz = clazz.getSuperClazz())
 		{
 			for (Mapping map : clazz.variables)
 			{
 				if (map.getVarName().equals(variable))
 				{
 					String type = map.getType();
 					if (!CallGraphResources.isLiteral(type))
 					{
 						Clazz typeClazz = callGraph.lookupUnqualifiedClassName(
 								clazz, type);
 						if (typeClazz != null)
 							type = typeClazz.getName();
 					}
 					return type;
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean hasUnqualifiedName(String unqualifiedName)
 	{
 		String shortC = unqualifiedName;
 		if (shortC.contains("."))
 			shortC = shortC.substring(shortC.lastIndexOf(".") + 1);
 		// Check for generic specification
 		if (shortC.contains("<") && shortC.contains(">"))
 			shortC = shortC.substring(0, shortC.lastIndexOf("<"));
 		if (this.name.substring(this.name.lastIndexOf(".") + 1).equals(shortC))
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * This function will return a list of all possible
 	 * method calls from the given method.
 	 * @param methodToResolve
 	 * @return
 	 */
 	public List<Method> hasMethod(String methodToResolve)
 	{
 		List<Method> returnMethods = new ArrayList<Method>();
 
 		String unType = methodToResolve.substring(0,
 				findTypeDivider(methodToResolve));
 		String unMethodName = methodToResolve.substring(
 				findTypeDivider(methodToResolve) + 1,
 				methodToResolve.lastIndexOf("("));
 		String[] unArguments = methodToResolve.substring(
 				methodToResolve.lastIndexOf("(") + 1,
 				methodToResolve.lastIndexOf(")")).split(",(?![^<>]*>)");
 
 		for (Clazz clazz = this; clazz != null; clazz = clazz.getSuperClazz())
 		{
 			for (Method method : clazz.getMethods())
 			{
 				String type = method.getName().substring(0,
 						findTypeDivider(method.getName()));
 				String methodName = method.getName().substring(
 						findTypeDivider(method.getName()) + 1,
 						method.getName().lastIndexOf("("));
 				String[] arguments = method
 						.getName()
 						.substring(method.getName().lastIndexOf("(") + 1,
 								method.getName().lastIndexOf(")"))
 						.split(",(?![^<>]*>)");
 
 				// Strip any generics in method name
 				if (unMethodName.contains("<") && unMethodName.contains(">"))
 				{
 					unMethodName = unMethodName.substring(0,
 							unMethodName.indexOf("<"));
 				}
 
 				// Check if method names match
 				if (!unMethodName.equals(methodName))
 					continue;
 
 				// Check if arguments are the same size
 				if (unArguments.length != arguments.length)
 					continue;
 
 				// Strip any generics parameters
 				unArguments = stripGenericParameters(unArguments);
 				arguments = stripGenericParameters(arguments);
 
 				// Compare parameters
 				if (compareArguments(unArguments, arguments))
 				{
 					returnMethods.add(method);
 					continue;
 				}
 				// Check for generic method
 				if (unType.contains("<") && unType.contains(">"))
 					if (hasGenericMethod(unType, unArguments, arguments))
 						returnMethods.add(method);
 			}
 			// Move the type to the super class
 			unType = clazz.getUnresolvedSuperClazz();
 		}
 		return returnMethods;
 	}
 
 	/**
 	 * This function is used to check all possible implementations
 	 * of a method from an interface.
 	 * @param methodToResolve
 	 * @return
 	 */
 	public List<Method> hasImplementedMethod(String methodToResolve)
 	{
 		List<Method> returnMethods = new ArrayList<Method>();
 
 		for (Clazz clazz : implementedBy)
 		{
 			String methodName = clazz.getName()
 					+ methodToResolve
 							.substring(findTypeDivider(methodToResolve));
 			returnMethods.addAll(clazz.hasMethod(methodToResolve));
 		}
 
 		return returnMethods;
 	}
 
 	/**
 	 * This function will return the position of a string in which
 	 * contains the last delimiter "." before a method name.
 	 * @param methodName
 	 * @return
 	 */
 	private int findTypeDivider(String methodName)
 	{
 		int index = -1;
 		for (int i = 0; i < methodName.length(); i++)
 		{
 			if (methodName.charAt(i) == '.')
 				index = i;
 			if (methodName.charAt(i) == '(')
 				break;
 		}
 		return index;
 	}
 
 	/**
 	 * This function will strip out any of the generic parameter types from a
 	 * list of arguments leaving just the generic class type.
 	 * 
 	 * @param parameters
 	 * @return
 	 */
 	private String[] stripGenericParameters(String[] parameters)
 	{
 		for (int i = 0; i < parameters.length; i++)
 		{
 			if (parameters[i].contains("<") && parameters[i].contains(">"))
 			{
 				parameters[i] = parameters[i].substring(0,
 						parameters[i].indexOf("<"));
 			}
 		}
 
 		return parameters;
 	}
 
 	/**
 	 * This function will tell you if two set of type arguments are matching or
 	 * not. If one argument is null then the check is ignored.
 	 * 
 	 * @param unArguments
 	 * @param arguments
 	 * @return
 	 */
 	private boolean compareArguments(String[] unArguments, String[] arguments)
 	{
 		for (int i = 0; i < unArguments.length; i++)
 		{
 			if (!unArguments[i].trim().equals(arguments[i].trim())
 					&& !unArguments[i].trim().equals("null")
 					&& !this.genericTypes.contains(arguments[i]))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This function will convert generic parameters back to their generic types
 	 * and check if the function's arguments match.
 	 * 
 	 * @param unType
 	 * @param unArguments
 	 * @param arguments
 	 * @return
 	 */
 	private boolean hasGenericMethod(String unType, String[] unArguments,
 			String[] arguments)
 	{
 		String[] generics = unType.substring(unType.indexOf("<") + 1,
 				unType.lastIndexOf(">")).split(",");
 
 		for (int i = 0; i < arguments.length; i++)
 		{
 			if (this.genericTypes.contains(arguments[i]))
 			{
 				int index = this.genericTypes.indexOf(arguments[i]);
 				try
 				{
 					if (!unArguments[index].equals(generics[i]))
 						return false;
 				}
 				catch (Exception e)
 				{
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	public void print()
 	{
 		System.out.println("  CLASS: " + name);
 		System.out.println("    Interface: " + isInterface);
 		System.out.println("    Implements: ");
 		for (Clazz interf : interfaces)
 			System.out.println("      " + interf.getName());
 		System.out.println("    Unresolved Interfaces: ");
 		for (String i : unresolvedInterfaces)
 			System.out.println("      " + i);
 		System.out.print("    Super Class: ");
 		if (superClazz != null)
 			System.out.println(superClazz.getName());
 		else
 			System.out.println("");
 		System.out.println("    Unresolved Super Class: "
 				+ unresolvedSuperClazz);
 		System.out.println("    Sub Classes: ");
 		//for (Clazz clazz : subClazzes)
 			//System.out.println("      " + clazz.getName());
 		System.out.println("    Generic Parameters: ");
 		for (String s : genericTypes)
 			System.out.println("      " + s);
 		System.out.println("    Fields: ");
 		for (Mapping map : variables)
 			System.out.println("      " + map.getType() + ": "
 					+ map.getVarName());
 		for (Method m : methods)
 			m.print();
 	}
 
 	public Method getMethod(String methodName)
 	{
 		for (Method method : methods)
 		{
 			if (method.getName().equals(methodName))
 				return method;
 		}
 		return null;
 	}
 
 	public void addMethod(Method m)
 	{
 		this.methods.add(m);
 	}
 
 	public void addGenericType(String t)
 	{
 		this.genericTypes.add(t);
 	}
 
 	public void addUnresolvedInterface(String i)
 	{
 		this.unresolvedInterfaces.add(i);
 	}
 
 	public void removeUnresolvedInterface(String i)
 	{
 		this.unresolvedInterfaces.remove(i);
 	}
 
 	public void addUnresolvedSuperClazz(String s)
 	{
 		this.unresolvedSuperClazz = s;
 	}
 
 	public void removeUnresolvedSuperClazz()
 	{
 		this.unresolvedSuperClazz = "";
 	}
 
 	public void addInterface(Clazz clazz)
 	{
 		this.interfaces.add(clazz);
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public boolean isInterface()
 	{
 		return isInterface;
 	}
 
 	public void setInterface(boolean isInterface)
 	{
 		this.isInterface = isInterface;
 	}
 
 	public List<Method> getMethods()
 	{
 		return methods;
 	}
 
 	public void setMethods(List<Method> methods)
 	{
 		this.methods = methods;
 	}
 
 	public List<Clazz> getInterfaces()
 	{
 		return interfaces;
 	}
 
 	public void setInterfaces(ArrayList<Clazz> interfaces)
 	{
 		this.interfaces = interfaces;
 	}
 
 	public Clazz getSuperClazz()
 	{
 		return superClazz;
 	}
 
 	public void setSuperClazz(Clazz superClazz)
 	{
		if(superClazz == null) {
			this.superClazz = null;
			return;
		}
 		if(!superClazz.getName().equals(this.name))
 			this.superClazz = superClazz;
 	}
 
 	public File getFile()
 	{
 		return file;
 	}
 
 	public void setFile(File file)
 	{
 		this.file = file;
 	}
 
 	public String getUnresolvedSuperClazz()
 	{
 		return unresolvedSuperClazz;
 	}
 
 	public void setUnresolvedSuperClazz(String unresolvedSuperClazz)
 	{
 		this.unresolvedSuperClazz = unresolvedSuperClazz;
 	}
 
 	public List<String> getUnresolvedInterfaces()
 	{
 		return unresolvedInterfaces;
 	}
 
 	public void setUnresolvedInterfaces(List<String> unresolvedInterfaces)
 	{
 		this.unresolvedInterfaces = unresolvedInterfaces;
 	}
 
 	public List<Mapping> getVariables()
 	{
 		return variables;
 	}
 
 	public void setVariables(List<Mapping> variables)
 	{
 		this.variables = variables;
 	}
 
 	public List<MethodInvocation> getInvocations()
 	{
 		return invocations;
 	}
 
 	public void setInvocations(List<MethodInvocation> invocations)
 	{
 		this.invocations = invocations;
 	}
 
 	public List<String> getGenericTypes()
 	{
 		return genericTypes;
 	}
 
 	public void setGenericTypes(List<String> genericTypes)
 	{
 		this.genericTypes = genericTypes;
 	}
 
 	public List<Clazz> getImplementedBy()
 	{
 		return implementedBy;
 	}
 
 	public void setImplementedBy(List<Clazz> implementedBy)
 	{
 		this.implementedBy = implementedBy;
 	}
 
 	public int getStartChar()
 	{
 		return startChar;
 	}
 
 	public void setStartChar(int startChar)
 	{
 		this.startChar = startChar;
 	}
 
 	public int getEndChar()
 	{
 		return endChar;
 	}
 
 	public void setEndChar(int endChar)
 	{
 		this.endChar = endChar;
 	}
 }
