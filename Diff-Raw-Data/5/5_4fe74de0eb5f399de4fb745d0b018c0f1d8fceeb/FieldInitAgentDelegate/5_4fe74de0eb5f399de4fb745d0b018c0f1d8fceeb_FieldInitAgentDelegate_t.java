 package com.hapiware.asm.fieldinit;
 
 import java.lang.instrument.Instrumentation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 
 /**
  * {@code FieldInitAgentDelegate} can be used to set initial values for member variables without
  * touching the source code of the target class. This is useful for testing purpouses where some
  * specific initial values may be required instead of the default values.
  * <p> 
  * {@code FieldInitAgentDelegate} is specified in the agent configuration XML file using
  * {@code /agent/delegate} element. For example:
  * <xmp>
  * 	<?xml version="1.0" encoding="UTF-8" ?>
  * 	<agent>
  * 		<delegate>com.hapiware.asm.fieldinit.FieldInitAgentDelegate</delegate>
  * 		...
  * 	</agent>
  * </xmp>
  * 
  * 
  * 
  * <h3>Requirements</h3>
  * {@code FieldInitAgentDelegate} requires:
  * <ul>
  * 		<li>
  * 			{@code com.hapiware.agent.Agent}
  * 			(see <a href="http://www.hapiware.com/java-agent-general" target="_blank">http://www.hapiware.com/java-agent-general</a>)
  * 		</li>
  * 		<li>
  * 			ASM 3.0 or later (see <a href="http://asm.ow2.org/" target="_blank">http://asm.ow2.org/</a>)
  * 		</li>
  * </ul>
  * 
  * 
  * 
  * <h3>Configuring the field initialisation agent</h3>
  * The field initialisation agent is configured using {@code /agent/configuration/custom} element
  * with several elements defining the initialised fields. The configuration XML file looks like this:
  * <xmp>
  * 	<?xml version="1.0" encoding="UTF-8" ?>
  *	<agent>
  *		<!--
  *			variable, delegate, classpath and filter elements as described in the documentation
  *			of the general Java agent.
  *		-->
  *		...
  *		<configuration>
  *			<custom>
  *				<target-class type="class.name.to.be.Initialised">
  *					<target-field name="_fieldName">
  *						<initialiser type="class.to.be.Instantiated"> <!-- Can be primitive type also -->
  *							<argument type="primitive-type">value</argument>
  *							<argument type="class.with.string.Constructor">2</argument>
  *							...
  *						</initialiser>
  *					</target-field>
  *		
  *					<target-field name="_date2">
  *						<initialiser type="java.util.Date">
  *							<argument type="java.lang.String">Sat, 12 Aug 1995 13:30:00 GMT</argument>
  *						</initialiser>
  *					</target-field>
  *		
  *					<target-field name="_fieldName2">
  *						<initialiser type="primitive-type">
  *							<!-- For primitive fields the argument type cannot be defined. -->
  *							<argument>314</argument>
  *						</initialiser>
  *					</target-field>
  *		
  *					<target-field name="_fieldName3">
  *						<initialiser
  *							type="class.to.be.Instantiated"
  *							factory-class="package.name.FactoryClass"
  *							method="staticFactoryMethod"
  *						/>
  *					</target-field>
  *		
  *					...
  *				</target-class>
  *		
  *				<target-class...> ... </target-class>
  *		
  *				<target-class...> ... </target-class>
  *				...
  *			</custom>
  *		</configuration>
  * 	</agent>
  * </xmp>
  * 
  * In general the configuration XML file defines which classes and which fields of those classes
  * are to be set to configured values or instantiated objects. There are three ways to instantiate
  * an object for setting a field:
  * 	<ol>
  * 		<li>Setting a primitive value</li>
  * 		<li>Defining the constructor of the initialiser class</li>
  * 		<li>Using the factory method</li>
  * 	</ol>
  * 
  * The general structure is that a single {@code target-class} element can have several
  * {@code target-field} elements which always have a single {@code initialiser} element.
  * {@code initialiser} elements can have zero to unlimited number of {@code argument} elements
  * depending on the case.
  * 
  * 
  * 
  * <h4><a name="field-init-setting-primitive-value">Setting a primitive value</a></h4>
  * Setting a primitive value to a field is simple. Just define a {@code target-class} element
  * and {@code target-field} element for each of the primitive type fields you want to initialise.
  * In the case of the primitive type {@code target-field} can (and must) have only a single
  * {@code argument} element which cannot have any attributes (i.e. when {@code initialiser[@type]}
  * attribute is a primitive type). Here is an example:
  * 
  * <xmp>
  * 	<?xml version="1.0" encoding="UTF-8" ?>
  * 	<agent>
  * 		<delegate>com.hapiware.asm.fieldinit.FieldInitAgentDelegate</delegate>
  *		<classpath>
  * 			<entry>${project.build.directory}/field-init-agent-delegate-1.0.0.jar</entry>
  * 			<entry>${user.home}/.m2/repository/asm/asm-all/3.3/asm-all-3.3.jar</entry>
  * 		</classpath>
  * 		<filter>
  *			<include>^com/hapiware/asm/fieldinit/.+</include>
  * 		</filter>
  *		<configuration>
  *			<custom>
  *				<target-class type="com.hapiware.asm.fieldinit.Container">
  *					<target-field name="_i">
  *						<initialiser type="int">
  *							<argument>314</argument>
  *						</initialiser>
  *					</target-field>
  *				</target-class>
  *			</custom>
  *		</configuration>
  * 	</agent>
  * </xmp>
  * 
  * 
  * 
  * <h4><a name="field-init-defining-constructor">Defining the constructor</a></h4>
  * Defining an object using {@code initialiser} element does not differ much from defining a primitive
  * value. The major difference is that {@code initialiser} can take multiple {@code argument} elements.
  * {@code argument} elements are used to define a correct constructor for the {@code initialiser}
  * class. In this case {@code argument} elements must have a {@code type} attribute to describe
  * the type of the {@code argument}. {@code type} attribute can be:
  * 	<ol>
  * 		<li>a primitive type (i.e. int, double, boolean, etc.)</li>
  * 		<li>
  * 			any class that accepts a single {@code java.lang.String} as a constructor argument
  * 			like {@code java.lang.String} (well, that's obvious), {@code java.lang.Integer} and
  * 			all the other wrapper classes
  * 		</li>
  * 	</ol>
  * 
  * There are cases when this is not quite enough. For example {@code org.joda.time.DateTime} class
  * accecpts a string as constructor parameter but in reality {@code org.joda.time.DateTime} does
  * not have a defined constructor for {@code java.lang.String}. Instead Joda time uses a generic
  * constructor which accepts a single {@code java.lang.Object} as a parameter. To handle this
  * situation {@code argument} element accepts {@code cast-to} attribute to define a correct
  * constructor. So, if {@code cast-to} attribute is defined then it is used to determine the correct
  * constructor call, otherwise {@code type} attribute is used.
  * <p>
  * Here is an example of defining different kind of constructors:
  * <xmp>
  * 	<?xml version="1.0" encoding="UTF-8" ?>
  * 	<agent>
  * 		<delegate>com.hapiware.asm.fieldinit.FieldInitAgentDelegate</delegate>
  *		<classpath>
  * 			<entry>${project.build.directory}/field-init-agent-delegate-1.0.0.jar</entry>
  * 			<entry>${user.home}/.m2/repository/asm/asm-all/3.3/asm-all-3.3.jar</entry>
  * 		</classpath>
  * 		<filter>
  *			<include>^com/hapiware/asm/fieldinit/.+</include>
  * 		</filter>
  *		<configuration>
  *			<custom>
  *				<target-class type="com.hapiware.asm.fieldinit.Container">
  *					<target-field name="_date">
  *						<initialiser type="java.util.Date">
  *							<argument type="int">111</argument>
  *							<argument type="int">2</argument>
  *							<argument type="int">16</argument>
  *						</initialiser>
  *					</target-field>
  *					<target-field name="_date2">
  *						<initialiser type="java.util.Date">
  *							<argument type="java.lang.String">Sat, 12 Aug 1995 13:30:00 GMT</argument>
  *						</initialiser>
  *					</target-field>
  *					<target-field name="_address">
  *						<initialiser type="com.hapiware.asm.fieldinit.Address">
  *							<argument type="java.lang.String" cast-to="java.lang.Object">Street,12345,City</argument>
  *						</initialiser>
  *					</target-field>
  *				</target-class>
  *			</custom>
  *		</configuration>
  * 	</agent>
  * </xmp>
  * 
  * 
  * 
  * <h4><a name="field-init-using-factory-method">Using the factory method</a></h4>
  * Factory method is to be used when the creation of the initialisation object cannot be done with
  * just using the {@code initialiser} element. Creating an object with a factory method is very
  * simple. This is done by defining a {@code target-class} element and {@code target-field} element
  * for each of the fields you want to initialise. Each {@code target-field} accepts a single
  * {@code initialser} element which needs the following attributes:
  * 	<ul>
  * 		<li><b>{@code type}</b> which is the type of the field to be initialised</li>
  * 		<li><b>{@code factory-class}</b> a class which contains the <u>static</u> factory method</li>
  * 		<li>
  * 			<b>{@code method}</b> a <u>static</u> method which creates the object to be used for
  * 			the field initialisation. Notice also that the factory method must not take any
  * 			arguments.
  * 		</li>
  * 	</ul>
  * 
  * {@code initialiser} element does not accept any {@code argument} elements when
  * factories are used.
  * 
  * <p>
  * <b>NOTICE!</b></br>
  * Remember to add your factory class to the classpath of the target JVM (i.e. to that JVM where
  * you define {@code -javaagent} switch).
  * <p>
  * Here is an example of using a factory method:
  * <xmp>
  * 	<?xml version="1.0" encoding="UTF-8" ?>
  * 	<agent>
  * 		<delegate>com.hapiware.asm.fieldinit.FieldInitAgentDelegate</delegate>
  *		<classpath>
  * 			<entry>${project.build.directory}/field-init-agent-delegate-1.0.0.jar</entry>
  * 			<entry>${user.home}/.m2/repository/asm/asm-all/3.3/asm-all-3.3.jar</entry>
  * 		</classpath>
  * 		<filter>
  *			<include>^com/hapiware/asm/fieldinit/.+</include>
  * 		</filter>
  *		<configuration>
  *			<custom>
  *				<target-class type="com.hapiware.asm.fieldinit.Container">
  *					<target-field name="_address">
  *						<initialiser
  *							type="com.hapiware.asm.fieldinit.Address"
  *							factory-class="com.hapiware.asm.fieldinit.Factory"
  *							method="createAddress"
  *						/>
  *					</target-field>
  *				</target-class>
  *			</custom>
  *		</configuration>
  * 	</agent>
  * </xmp>
  * 
  * 
  * 
  * <h4><a name="field-init-configuration-elements">Configuration elements</a></h4>
  * Here is a list of configuration elements and their attributes:
  * 	<ol>
  * 		<li>
  * 			<b>{@code target-class}</b> is a main level element which defines the class to
  * 			be initialised
  * 			<ul>
 * 				<li><b>{@code type}</b> the class name (e.g. com.hapiware.asm.fieldinit.Container)</li>
  * 			</ul>
  * 		</li>
  * 		<li>
  * 			<b>{@code target-field}</b> defines the field to be initialised
  * 			<ul>
  * 				<li><b>{@code name}</b> the name of the field (e.g. _address)</li>
  * 			</ul>
  * 		</li>
  * 		<li>
  * 			<b>{@code initialiser}</b> defines an initialiser for the defined {@code target-field}
  * 			(see <a href="#field-init-defining-constructor">Defining the constructor</a>)
  * 			<ul>
  * 				<li><b>{@code type}</b> the type of object to be instantiated (e.g. java.lang.Date)</li>
  * 				<li><b>{@code factory-class}</b> the factory class (e.g. om.hapiware.asm.fieldinit.Factory)</li>
  * 				<li><b>{@code method}</b> a static factory method name (e.g. createDate)</li>
  * 			</ul>
  * 		</li>
  * 		<li>
  * 			<b>{@code argument}</b> defines the arguments and possibly types for the constructor
  * 			defined in {@code initialiser}
  * 			(see <a href="#field-init-defining-constructor">Defining the constructor</a>)
  * 			<ul>
  * 				<li>
  * 					<b>{@code type}</b> a primitive type or a class which is instantiated using
 * 					a constructor which takes a single string argument (e.g. int or java.lang.String)
  * 				</li>
  * 				<li><b>{@code cast-to}</b> optinal class to define a proper constructor</li>
  * 			</ul>
  * 		</li>
  * 	</ol>
  * 
  * 
  * @author <a href="http://www.hapiware.com" target="_blank">hapi</a>
  * @see com.hapiware.asm.agent.Agent
  */
 public class FieldInitAgentDelegate
 {
 	private final static String CONFIGURATION_ELEMENT = "configuration/custom"; 
 	private final static String TARGET_CLASS_ELEMENT = CONFIGURATION_ELEMENT + "/target-class"; 
 	private final static String TARGET_FIELD_ELEMENT = TARGET_CLASS_ELEMENT + "/target-field"; 
 	private final static String INITIALISER_ELEMENT = TARGET_FIELD_ELEMENT + "/initialiser"; 
 	private final static String ARGUMENT_ELEMENT = INITIALISER_ELEMENT + "/argument";
 	private final static Map<String, String> PRIMITIVE_TYPES;
 	private final static Map<String, String> WRAPPER_TYPES;
 	
 	static {
 		@SuppressWarnings("serial")
 		Map<String, String> primitiveTypes =
 			new HashMap<String, String>()
 			{{
 				put("short", "S");
 				put("int", "I");
 				put("long", "J");
 				put("boolean", "Z");
 				put("char", "C");
 				put("byte", "B");
 				put("float", "F");
 				put("double", "D");
 			}};
 		PRIMITIVE_TYPES = Collections.unmodifiableMap(primitiveTypes);
 		@SuppressWarnings("serial")
 		Map<String, String> wrapperTypes =
 			new HashMap<String, String>()
 			{{
 				put("short", "java.lang.Short");
 				put("int", "java.lang.Integer");
 				put("long", "java.lang.Long");
 				put("boolean", "java.lang.Boolean");
 				put("char", "java.lang.Character");
 				put("byte", "java.lang.Byte");
 				put("float", "java.lang.Float");
 				put("double", "java.lang.Double");
 			}};
 		WRAPPER_TYPES = Collections.unmodifiableMap(wrapperTypes);
 	}
 	
 	
 	/**
 	 * This method is called by the general agent {@code com.hapiware.agent.Agent} and
 	 * is done before the main method call right after the JVM initialisation. 
 	 * <p>
 	 * <b>Notice</b> the difference between this method and 
 	 * the {@code public static void premain(String, Instrumentation} method described in
 	 * {@code java.lang.instrument} package. 
 	 *
 	 * @param includePatterns
 	 * 		A list of patterns to include classes for instrumentation.
 	 * 
 	 * @param excludePatterns
 	 * 		A list patterns to set classes not to be instrumented.
 	 * 
 	 * @param config
 	 * 		Custom configuration elements to initialise specified fields. See the class
 	 * 		description for further details.
 	 * 
 	 * @param instrumentation
 	 * 		See {@link java.lang.instrument.Instrumentation}
 	 * 
 	 * @throws IllegalArgumentException
 	 * 		If there is something wrong with the configuration file.
 	 *
 	 * @see java.lang.instrument
 	 */
 	public static void premain(
 		Pattern[] includePatterns,
 		Pattern[] excludePatterns,
 		Object config,
 		Instrumentation instrumentation
 	)
 	{
 		try {
 			instrumentation.addTransformer(
 				new FieldInitTransformer(includePatterns, excludePatterns, (TargetClass[])config)
 			);
 		}
 		catch(Exception e) 
 		{
 			System.err.println(
 				"Couldn't start the field initialisation agent delegate due to an exception. "
 					+ e.getMessage()
 			);
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public static Object unmarshall(Element configElement)
 	{
 		try {
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			
 			NodeList targetClassEntries =
 				(NodeList)xpath.evaluate(
 					"./target-class",
 					configElement,
 					XPathConstants.NODESET
 				);
 			List<TargetClass> targetClasses = new ArrayList<TargetClass>();
 			for(int i = 0; i < targetClassEntries.getLength(); i++) {
 				Element targetClassElement = (Element)targetClassEntries.item(i);
 				int numOfAllAttributes = targetClassElement.getAttributes().getLength();
 				int numOfTypeAttributes = 
 					((NodeList)xpath.evaluate(
 						"@type",
 						targetClassElement,
 						XPathConstants.NODESET
 					)).getLength();
 				if(numOfTypeAttributes < 1)
 					throw
 						new ConfigurationError(
 							"A required 'type' attribute is missing from " + TARGET_CLASS_ELEMENT + " element."
 						);
 				String targetClassName = targetClassElement.getAttribute("type");
 				if(targetClassName == null)
 					throw
 					new ConfigurationError(
 						"Only 'type' attribute is allowed for " + TARGET_CLASS_ELEMENT + " element."
 					);
 				if(numOfAllAttributes > 1)
 					throw
 						new ConfigurationError(
 							"Only one 'type' attribute is allowed for " + TARGET_CLASS_ELEMENT + " element."
 						);
 				targetClasses.add(
 					new TargetClass(targetClassName, unmarshallTargetFields(xpath, targetClassElement))
 				);
 			}
 			
 			return targetClasses.toArray(new TargetClass[0]);
 		}
 		catch(XPathExpressionException e) {
 			throw
 				new ConfigurationError(
 					"A config node was not found from the field initialisation agent delegate configuration file.",
 					e
 				);
 		}
 	}
 
 	private static List<TargetField> unmarshallTargetFields(XPath xpath, Element targetClassElement)
 		throws
 			XPathExpressionException
 	{
 		NodeList targetFieldEntries =
 			(NodeList)xpath.evaluate(
 				"./target-field",
 				targetClassElement,
 				XPathConstants.NODESET
 			);
 		List<TargetField> targetFields = new ArrayList<TargetField>();
 		for(int i = 0; i < targetFieldEntries.getLength(); i++) {
 			Element targetFieldElement = (Element)targetFieldEntries.item(i);
 			int numOfNameAttributes = targetFieldElement.getAttributes().getLength();
 			if(numOfNameAttributes < 1)
 				throw
 					new ConfigurationError(
 						"A required 'name' attribute is missing from " + TARGET_FIELD_ELEMENT + " element."
 					);
 			String targetFieldName = targetFieldElement.getAttribute("name");
 			if(targetFieldName == null)
 				throw
 					new ConfigurationError(
 						"Only 'name' attribute is allowed for " + TARGET_FIELD_ELEMENT + " element."
 					);
 			
 			targetFields.add(
 				new TargetField(targetFieldName, unmarshallInitialiser(xpath, targetFieldElement))
 			);
 		}
 		return targetFields;
 	}
 	
 	
 	private static Initialiser unmarshallInitialiser(XPath xpath, Element targetFieldElement)
 		throws
 			XPathExpressionException
 	{
 		NodeList initialiserEntries =
 			(NodeList)xpath.evaluate(
 				"./initialiser",
 				targetFieldElement,
 				XPathConstants.NODESET
 			);
 		if(initialiserEntries.getLength() < 1)
 			throw new ConfigurationError("A required " + INITIALISER_ELEMENT + " is missing.");
 		if(initialiserEntries.getLength() > 1)
 			throw new ConfigurationError("Only one " + INITIALISER_ELEMENT + " is allowed.");
 		Element initialiserElement = (Element)initialiserEntries.item(0);
 		NamedNodeMap attributes = initialiserElement.getAttributes();
 		Node typeNode = attributes.getNamedItem("type");
 		Node factoryClassNode = attributes.getNamedItem("factory-class");
 		Node methodNode = attributes.getNamedItem("method");
 		if(typeNode == null)
 			throw
 				new ConfigurationError(
 					"'type' attribute for " + INITIALISER_ELEMENT + " element is required."
 				);
 		if((factoryClassNode == null && methodNode != null) || (factoryClassNode != null && methodNode == null))
 			throw
 				new ConfigurationError(
 					"Both, 'factory-class' and 'method' attributes for " + INITIALISER_ELEMENT + " element are required."
 				);
 		if(factoryClassNode == null && methodNode == null)
 		{
 			if(attributes.getLength() != 1)
 				throw
 					new ConfigurationError(
 						"'type' attribute for " + INITIALISER_ELEMENT + " element can exists only alone."
 					);
 			return
 				new Initialiser(
 					typeNode.getTextContent(),
 					unmarshallConstructorArguments(xpath, initialiserElement)
 				);
 		}
 		else {
 			if(attributes.getLength() > 3)
 				throw
 					new ConfigurationError(
 						"Too many attributes for " + INITIALISER_ELEMENT + " element."
 					);
 			return new Initialiser(typeNode.getTextContent(), factoryClassNode.getTextContent(), methodNode.getTextContent());
 		}
 	}
 	
 	
 	private static List<ConstructorArgument> unmarshallConstructorArguments(XPath xpath, Element initialiserElement)
 		throws
 			XPathExpressionException
 	{
 		NodeList argumentEntries =
 			(NodeList)xpath.evaluate(
 				"./argument",
 				initialiserElement,
 				XPathConstants.NODESET
 			);
 		String initialiserType = initialiserElement.getAttribute("type");
 		final boolean primitiveInitialiser = PRIMITIVE_TYPES.containsKey(initialiserType);
 		if(primitiveInitialiser && argumentEntries.getLength() > 1)
 			throw
 				new ConfigurationError(
 					"For the primitive type '" + initialiserType 
 						+ "' only one "	+ ARGUMENT_ELEMENT + " element is allowed."
 				);
 		List<ConstructorArgument> arguments = new ArrayList<ConstructorArgument>();
 		for(int i = 0; i < argumentEntries.getLength(); i++) {
 			Element argumentElement = (Element)argumentEntries.item(i);
 			int numOfTypeAttributes = 
 				((NodeList)xpath.evaluate(
 					"@type",
 					argumentElement,
 					XPathConstants.NODESET
 				)).getLength();
 			int numOfCastTypeAttributes = 
 				((NodeList)xpath.evaluate(
 					"@cast-to",
 					argumentElement,
 					XPathConstants.NODESET
 				)).getLength();
 			int numOfAllAttributes = argumentElement.getAttributes().getLength();
 			if(numOfAllAttributes != numOfTypeAttributes + numOfCastTypeAttributes)
 				throw
 					new ConfigurationError(
 						"Only 'type' and 'cast-to' arguments are allowed for "
 							+ ARGUMENT_ELEMENT + " elements."
 					);
 			
 			if(!primitiveInitialiser && numOfTypeAttributes < 1)
 				throw
 					new ConfigurationError(
 						"A required 'type' attribute is missing from " + ARGUMENT_ELEMENT + " element."
 					);
 			if(numOfTypeAttributes > 1)
 				throw
 					new ConfigurationError(
 						"Only one 'type' attribute is allowed for " + ARGUMENT_ELEMENT + " element."
 					);
 			Node castTypeNode = argumentElement.getAttributeNode("cast-to");
 			if(castTypeNode != null  && primitiveInitialiser)
 				throw
 					new ConfigurationError(
 						"'cast-to' attribute is not allowed for primitive types in "
 							+ ARGUMENT_ELEMENT + " element."
 					);
 			if(!primitiveInitialiser) {
 				String typeName = argumentElement.getAttribute("type");
 				arguments.add(
 					new ConstructorArgument(
 						typeName,
 						castTypeNode == null ? null : castTypeNode.getTextContent(),
 						argumentElement.getTextContent())
 				);
 			}
 			else
 				arguments.add(
 					new ConstructorArgument(initialiserType, null, argumentElement.getTextContent())
 				);
 		}
 		return arguments;
 	}
 	
 	
 	public static class TargetClass
 	{
 		private final String _name;
 		private final Pattern _namePattern;
 		private final TargetField[] _targetFields;
 		
 		
 		public TargetClass(String name, List<TargetField> targetFields)
 		{
 			_name = name.replace('.', '/');
 			_namePattern = Pattern.compile("^" + _name + "$");
 			_targetFields = targetFields.toArray(new TargetField[0]);
 		}
 
 
 		public String getName()
 		{
 			return _name;
 		}
 
 
 		public Pattern getNamePattern()
 		{
 			return _namePattern;
 		}
 
 
 		public TargetField[] getTargetFields()
 		{
 			return _targetFields;
 		}
 	}
 
 	public static class TargetField
 	{
 		private final String _name;
 		private boolean _isStatic;
 		private final Initialiser _initialiser;
 		private String _targetTypeDescriptor;
 
 		public TargetField(String name, Initialiser initialiser)
 		{
 			_name = name;
 			_initialiser = initialiser;
 		}
 
 		public String getName()
 		{
 			return _name;
 		}
 
 		public boolean isStatic()
 		{
 			return _isStatic;
 		}
 		
 		public void setStatic(boolean isStatic)
 		{
 			_isStatic = isStatic;
 		}
 
 		public Initialiser getInitialiser()
 		{
 			return _initialiser;
 		}
 
 		public void setTargetTypeDescriptor(String targetTypeDescriptor)
 		{
 			_targetTypeDescriptor = targetTypeDescriptor;
 		}
 
 		public String getTargetTypeDescriptor()
 		{
 			return _targetTypeDescriptor;
 		}
 	}
 	
 	public static class Initialiser
 	{
 		private final String _typeName;
 		private final Pattern _typeNamePattern;
 		private final ConstructorArgument[] _constructorArguments;
 		private final String _factoryClassName;
 		private final Pattern _factoryClassNamePattern;
 		private final String _methodName;
 		private final String _descriptor;
 		private final boolean _isPrimitive;
 		
 		public Initialiser(
 			String typeName,
 			List<ConstructorArgument> constructorArguments
 		)
 		{
 			_typeName = typeName.replace('.', '/');
 			_typeNamePattern = Pattern.compile("^" + _typeName + "$");
 			_constructorArguments = constructorArguments.toArray(new ConstructorArgument[0]);
 			_factoryClassName = null;
 			_factoryClassNamePattern = null;
 			_methodName = null;
 			String descriptor = "(";
 			boolean isPrimitive = false;
 			for(ConstructorArgument ca : constructorArguments) {
 				// If ca.getCastType() == null then we know that the target field is
 				// expected to be a primitive type.
 				String argumentTypeName = ca.getCastType() == null ? _typeName : ca.getCastType();
 				String type = PRIMITIVE_TYPES.get(argumentTypeName);
 				if(type == null)
 					type ="L" + argumentTypeName + ";";
 				else
 					isPrimitive = constructorArguments.size() == 1;
 				descriptor += type;
 			}
 			descriptor += ")V";
 			_descriptor = descriptor;
 			_isPrimitive = isPrimitive;
 		}
 
 		public Initialiser(String typeName, String className, String methodName)
 		{
 			_typeName = typeName.replace('.', '/');
 			_typeNamePattern = Pattern.compile("^" + _typeName + "$");
 			_constructorArguments = null;
 			_factoryClassName = className.replace('.', '/');
 			_factoryClassNamePattern = Pattern.compile("^" + _factoryClassName + "$");
 			_methodName = methodName;
 			String type = PRIMITIVE_TYPES.get(_typeName);
 			if(type == null)
 				type ="L" + _typeName + ";";
 			_descriptor = "()" + type;
 			_isPrimitive = false;
 		}
 
 		public String getTypeName()
 		{
 			return _typeName;
 		}
 
 		public Pattern getTypeNamePattern()
 		{
 			return _typeNamePattern;
 		}
 
 		public ConstructorArgument[] getConstructorArguments()
 		{
 			return _constructorArguments;
 		}
 
 		public String getFactoryClassName()
 		{
 			return _factoryClassName;
 		}
 
 		public Pattern getFactoryClassNamePattern()
 		{
 			return _factoryClassNamePattern;
 		}
 
 		public String getMethodName()
 		{
 			return _methodName;
 		}
 
 		public String getDescriptor()
 		{
 			return _descriptor;
 		}
 		
 		public boolean isPrimitive()
 		{
 			return _isPrimitive;
 		}
 	}
 	
 	public static class ConstructorArgument
 	{
 		private final String _type;
 		private final Pattern _typePattern;
 		private final String _castType;
 		private final String _value;
 		private final Object _argument;
 		
 		public ConstructorArgument(String type, String castType, String value)
 		{
 			_type = type.replace('.', '/');
 			_typePattern = Pattern.compile("^" + _type + "$");
 			_castType = castType == null ? _type : castType.replace('.', '/');
 			_value = value;
 			Object argument = null;
 			try {
 				argument = create(type, value);
 			}
 			catch(Exception e) {
 				e.printStackTrace();
 			}
 			_argument = argument;
 		}
 
 		public String getType()
 		{
 			return _type;
 		}
 
 		public Pattern getTypePattern()
 		{
 			return _typePattern;
 		}
 
 		public String getCastType()
 		{
 			return _castType;
 		}
 
 		public String getValue()
 		{
 			return _value;
 		}
 		
 		public Object getArgument()
 		{
 			return _argument;
 		}
 
 		private static Object create(String type, String value)
 			throws
 				ClassNotFoundException,
 				SecurityException,
 				NoSuchMethodException,
 				IllegalArgumentException,
 				InstantiationException,
 				IllegalAccessException,
 				InvocationTargetException
 		{
 			String wrapper = WRAPPER_TYPES.get(type);
 			Class<?> c = wrapper == null ? Class.forName(type) : Class.forName(wrapper);
 			Constructor<?> constructor = c.getConstructor(String.class);
 			return constructor.newInstance(value);
 		}
 	}
 	
 	/**
 	 * A runtime error to indicate that there is something wrong with the configuration of
 	 * the field initialisation agent delegate. 
 	 * 
 	 * @author hapi
 	 *
 	 */
 	static class ConfigurationError extends Error
 	{
 		private static final long serialVersionUID = -236321167558175285L;
 
 		public ConfigurationError()
 		{
 			super();
 		}
 
 		public ConfigurationError(String message, Throwable cause)
 		{
 			super(message, cause);
 		}
 
 		public ConfigurationError(String message)
 		{
 			super(message);
 		}
 
 		public ConfigurationError(Throwable cause)
 		{
 			super(cause);
 		}
 	}
 	
 }
 
