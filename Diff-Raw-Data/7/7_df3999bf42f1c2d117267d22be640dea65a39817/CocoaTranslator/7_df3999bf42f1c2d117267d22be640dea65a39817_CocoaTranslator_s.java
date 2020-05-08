 package ecologylab.translators.cocoa;
 
 import japa.parser.ParseException;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.formatenums.Format;
 import ecologylab.serialization.library.rss.Channel;
 import ecologylab.serialization.library.rss.Item;
 import ecologylab.serialization.library.rss.RssState;
 import ecologylab.standalone.xmlpolymorph.BItem;
 import ecologylab.standalone.xmlpolymorph.SchmItem;
 import ecologylab.standalone.xmlpolymorph.Schmannel;
 import ecologylab.translators.java.JavaTranslationConstants;
 import ecologylab.translators.parser.JavaDocParser;
 
 /**
  * This class is the main class which provides the functionality of translation of Java classes into
  * the objective class header files.
  * 
  * <p>
  * It uses the same syntactical annotations used the by {@code ecologylab.serialization} to
  * translate Java objects into xml files. Since it uses the same annotations the data types
  * supported for translation are also the same. The entry point functions into the class are.
  * <ul>
  * <li>{@code translateToObjC(Class<?>, Appendable)}</li>
  * </ul>
  * </p>
  * 
  * @author Nabeel Shahzad
  * @version 1.0
  */
 public class CocoaTranslator
 {
 /**
     * Using this internal class for the calling the hook method after
     * translation of a Java class to C This basically used for {@code
     * @xml_nested} attribute from the {@code ecologylab.serialization} When ever we find
     * an {@code @xml_nested} attribute we want to generate the Objective-C
     * header file for the nested class as well
     * 
     * @author Nabeel Shahzad
     */
 	private class NestedTranslationHook
 	{
 		/**
 		 * Class on which this class will fire a hook method to generate Objective-C class.
 		 */
 		private ClassDescriptor		inputClass;
 
 		/**
 		 * The appendable object on which the hook method will write the generated code.
 		 */
 		private Appendable	appendable;
 
 		/**
 		 * The directory location to generate the nested file
 		 */
 		private File				directoryLocation;
 
 		/**
 		 * Constructor method. Takes the {@code Class} for which it will generate the Objective-C header
 		 * file and also the {@code Appendable} object on which it will append the generated code.
 		 * 
 		 * @param inputClass
 		 * @param appendable
 		 */
 		public NestedTranslationHook(ClassDescriptor inputClass, Appendable appendable)
 		{
 			this.inputClass = inputClass;
 			this.appendable = appendable;
 			this.directoryLocation = null;
 		}
 
 		/**
 		 * Constructor for taking in the directory location
 		 */
 		public NestedTranslationHook(ClassDescriptor inputClass, File directoryLocation)
 		{
 			this.inputClass = inputClass;
 			this.directoryLocation = directoryLocation;
 			this.appendable = null;
 		}
 
 		/**
 		 * The main hook method. It simple instantiates and object of the {@code CocoaTranslator }and
 		 * calls the entry point method of {@code translateToObjC(Class<?extends ElementState>,
 		 * Appendable)} on the already populated member fields.
 		 * 
 		 * @throws IOException
 		 * @throws CocoaTranslationException
 		 */
 		public void execute() throws IOException, CocoaTranslationException
 		{
 			CocoaTranslator ct = new CocoaTranslator();
 			ClassDescriptor elementStateClassDescriptor = ClassDescriptor.getClassDescriptor(inputClass.getClass().asSubclass(ElementState.class));
 
 			if (directoryLocation == null)
 			{
 				ct.translateToObjCRecursive(elementStateClassDescriptor, appendable);
 			}
 			else
 			{
 				ct.translateToObjCRecursive(elementStateClassDescriptor, directoryLocation);
 			}
 		}
 	}
 
 /**
     * Member variable to hold the list of the hooks. For each {@code
     * @xml_nested} attribute encountered during the translation to Objective-C
     * file a {@code NestedTranslationHook} is registered which takes care of
     * translating the nested object.
     */
 	private ArrayList<NestedTranslationHook>	nestedTranslationHooks;
 
 	/**
 	 * Flag which does parent objects recursively
 	 */
 	private boolean														isRecursive;
 
 	/**
 	 * Location of the directory where the output header and implementation will be put
 	 */
 	private File															directoryLocation;
 
 	/**
 	 * Constructor method
 	 * <p>
 	 * Initializes the {@code nestedTranslationHooks} member of the class
 	 * </p>
 	 */
 	public CocoaTranslator()
 	{
 		nestedTranslationHooks = new ArrayList<NestedTranslationHook>();
 		isRecursive = false;
 		directoryLocation = null;
 	}
 
 /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.serialization}.
     * <p>
     * This function will <b>not</b> try to generate the header file for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.serialization} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
 	public void translateToObjC(Appendable appendable, ClassDescriptor... classes) throws IOException,
 			CocoaTranslationException
 	{
 		int length = classes.length;
 		for (int i = 0; i < length; i++)
 		{
 			translateToObjC(classes[i], appendable);
 		}
 
 	}
 
 	public void translateToObjC(ClassDescriptor thatClass, Appendable appendable) throws IOException,
 			CocoaTranslationException
 	{
 		translateToObjCHeader(thatClass, appendable);
 		translateToObjCImplementation(thatClass, appendable);
 	}
 
 /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.serialization}.
     * <p>
     * This function will <b>not</b> try to generate the header file only for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.serialization} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
 	private void translateToObjCHeader(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 
 		if (inputClass.getSuperClassName().equals(JavaTranslationConstants.JAVA_OBJECT))
 			CocoaTranslationConstants.INHERITENCE_OBJECT = CocoaTranslationConstants.OBJC_OBJECT;
 		else
 			CocoaTranslationConstants.INHERITENCE_OBJECT = inputClass.getSuperClassName();
 
 		HashMapArrayList<String, ? extends FieldDescriptor> fieldDescriptors = inputClass
 				.getFieldDescriptorsByFieldName();
 
 		appendHeaderComments(inputClass, appendable);
 		openHeaderFile(inputClass, appendable);
 
 		addHookForParentClassIfNotElementState(inputClass, appendable);
 
 		if (fieldDescriptors.size() > 0)
 		{
 			inputClass.resolvePolymorphicAnnotations();
 
 			openFieldDeclartion(appendable);
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.belongsTo(inputClass))
 					appendFieldAsObjectiveCAttribute(fieldDescriptor, appendable);
 			}
 
 			closeFieldDeclartion(appendable);
 
 			for (FieldDescriptor fieldAccessor : fieldDescriptors)
 			{
 				if (fieldAccessor.belongsTo(inputClass))
 					appendPropertyOfField(fieldAccessor, appendable);
 			}
 
 			for (FieldDescriptor fieldAccessor : fieldDescriptors)
 			{
 				if (fieldAccessor.belongsTo(inputClass) && fieldAccessor.isScalar()
 						&& fieldAccessor.getScalarType().isPrimitive()
 						&& fieldAccessor.getField().getType() != String.class)
 					appendFieldSetterFunctionDefinition(appendable, fieldAccessor);
 			}
 		}
 
 		closeHeaderFile(appendable);
 
 		if (isRecursive)
 		{
 			for (NestedTranslationHook nestedTranslationHook : nestedTranslationHooks)
 			{
 				nestedTranslationHook.execute();
 			}
 		}
 	}
 
 	private void addHookForParentClassIfNotElementState(ClassDescriptor inputClass, Appendable appendable)
 	{
 		if (inputClass.getSuperClass() == null || (!"ElementState".equals(inputClass.getSuperClass().getClassSimpleName())))
 		{
 			if (directoryLocation == null)
 			{
 				nestedTranslationHooks
 						.add(new NestedTranslationHook(inputClass.getSuperClass(), appendable));
 			}
 			else
 			{
 				nestedTranslationHooks.add(new NestedTranslationHook(inputClass.getSuperClass(),
 						directoryLocation));
 			}
 		}
 	}
 
 /**
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.serialization}.
     * <p>
     * This function will <b>not</b> try to generate the implementation file only for the
     * Class whose objects are present in the current Java file and annotated by
     * {@code ecologylab.serialization} attributes.
     * </p>
     * <p>
     * See {@code translateToObjCRecursive()} if you want to generate nested
     * objects
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
 	private void translateToObjCImplementation(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 
 		CocoaTranslationConstants.INHERITENCE_OBJECT = inputClass.getSuperClassName();
 
 		HashMapArrayList<String, ? extends FieldDescriptor> attributes = inputClass
 				.getFieldDescriptorsByFieldName();
 
 		appendImplementationComments(inputClass, appendable);
 
		startImport(XMLTools.getClassSimpleName(inputClass), appendable);
 		openImplementationFile(inputClass, appendable);
 
 		if (attributes.size() > 0)
 		{
 			for (FieldDescriptor fieldAccessor : attributes)
 			{
 				if (fieldAccessor.belongsTo(inputClass))
 				{
 					appendSynthesizedField(fieldAccessor, appendable);
 				}
 			}
 		}
 
 		// generateInitializationFunction(inputClass, appendable);
 		generateDeallocFunction(inputClass, attributes, appendable);
 
 		for (FieldDescriptor fieldAccessor : attributes)
 		{
 			if (fieldAccessor.belongsTo(inputClass) && fieldAccessor.isScalar()
 					&& fieldAccessor.getScalarType().isPrimitive()
 					&& fieldAccessor.getField().getType() != String.class)
 				appendFieldSetterFunctionImplementation(appendable, fieldAccessor);
 		}
 
 		closeImplementationFile(appendable);
 	}
 
 /**
     * Recursive version of the main function. Will also be generating
     * Objective-C header outputs for {@code @xml_nested} objects
     * <p>
     * The main entry function into the class. Goes through a sequence of steps
     * to convert the Java class file into Objective-C header file. It mainly
     * looks for {@code @xml_attribute} , {@code @xml_collection} and {@code
     * @xml_nested} attributes of the {@code ecologylab.serialization}.
     * </P>
     * <p>
     * This function will also try to generate the header file for the Class
     * whose objects are present in the current Java file and annotated by
     * {@code ecologylab.serialization} attributes.
     * </p>
     * <p>
     * Currently this function is implemented in such a way as to maintain the
     * directory structure of the Java classes mentioned by the package
     * specifiers.
     * </p>
     * 
     * @param inputClass
     * @param appendable
     * @throws IOException
     * @throws CocoaTranslationException 
     */
 	public void translateToObjCRecursive(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		isRecursive = true;
 		translateToObjC(inputClass, appendable);
 	}
 
 	/**
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	public void translateToObjC(ClassDescriptor inputClass, File directoryLocation) throws IOException,
 			CocoaTranslationException
 	{
 		translateToObjCHeader(inputClass, directoryLocation);
 		translateToObjCImplementation(inputClass, directoryLocation);
 	}
 
 	/**
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	public void translateToObjC(File directoryLocation, ClassDescriptor... classes) throws IOException,
 			CocoaTranslationException
 	{
 		int length = classes.length;
 		for (int i = 0; i < length; i++)
 		{
 			translateToObjCHeader(classes[i], directoryLocation);
 			translateToObjCImplementation(classes[i], directoryLocation);
 		}
 	}
 
 	/**
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 * @throws SIMPLTranslationException
 	 */
 	public void translateToObjC(File directoryLocation, SimplTypesScope tScope) throws IOException,
 			CocoaTranslationException, SIMPLTranslationException
 	{
 		// Generate header and implementation files
 		ArrayList<ClassDescriptor<? extends FieldDescriptor>> classDescriptors = tScope.getClassDescriptors();
 
 		int length = classDescriptors.size();
 		for (int i = 0; i < length; i++)
 		{
 			translateToObjC(classDescriptors.get(i), directoryLocation);
 		}
 
 		// Serialize translation scope
 
 		SimplTypesScope.enableGraphSerialization();
 		SimplTypesScope.serialize(tScope, new File(directoryLocation
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR + tScope.getName()
 				+ CocoaTranslationConstants.XML_FILE_EXTENSION), Format.XML);
 		SimplTypesScope.enableGraphSerialization();
 
 		// create a folder to put the translation scope getter class
 		File tScopeDirectory = createGetTranslationScopeFolder(directoryLocation);
 
 		// generate translation scope getter class
 		generateTranslationScopeGetterClass(tScopeDirectory, tScope);
 	}
 
 	private File createGetTranslationScopeFolder(File directoryLocation)
 	{
 		String tScopeDirectoryPath = directoryLocation.toString()
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 
 		tScopeDirectoryPath += CocoaTranslationConstants.TRANSATIONSCOPE_FOLDER
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 		File tScopeDirectory = new File(tScopeDirectoryPath);
 		tScopeDirectory.mkdir();
 		return tScopeDirectory;
 	}
 
 	/**
 	 * 
 	 * @param directoryLocation
 	 * @param tScope
 	 * @param workSpaceLocation
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 * @throws SIMPLTranslationException
 	 * @throws ParseException
 	 */
 	public void translateToObjC(File directoryLocation, SimplTypesScope tScope, File workSpaceLocation)
 			throws IOException, CocoaTranslationException, SIMPLTranslationException, ParseException
 	{
 		// Parse source files for javadocs
 		JavaDocParser.parseSourceFileIfExists(tScope, workSpaceLocation);
 
 		translateToObjC(directoryLocation, tScope);
 	}
 
 	/**
 	 * 
 	 * @param directoryLocation
 	 * @param tScope
 	 * @throws IOException
 	 */
 	private void generateTranslationScopeGetterClass(File directoryLocation, SimplTypesScope tScope)
 			throws IOException
 	{
 		createTranslationScopeGetterClassHeader(directoryLocation, tScope);
 		createTranslationScopeGetterClassImplementation(directoryLocation, tScope);
 	}
 
 	/**
 	 * 
 	 * @param directoryLocation
 	 * @param tScope
 	 * @throws IOException
 	 */
 	private void createTranslationScopeGetterClassImplementation(File directoryLocation,
 			SimplTypesScope tScope) throws IOException
 	{
 		File implementationFile = new File(directoryLocation
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR + tScope.getName()
 				+ CocoaTranslationConstants.IMPLEMENTATION_FILE_EXTENSION);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(implementationFile));
 
 		startImport(tScope.getName(), null, bufferedWriter);
 
 		declareStaticTranslationScope(bufferedWriter);
 
 		openImplementationFile(tScope.getName(), bufferedWriter);
 
 		generateGetterFunction(tScope, bufferedWriter);
 
 		closeImplementationFile(bufferedWriter);
 
 		bufferedWriter.close();
 	}
 
 	/**
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void declareStaticTranslationScope(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.STATIC);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.TRANSLATIONSCOPE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.TRANSATIONSCOPE_VAR);
 		appendable.append(CocoaTranslationConstants.END_LINE);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param tScope
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void generateGetterFunction(SimplTypesScope tScope, Appendable appendable)
 			throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.PLUS);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.OPENING_BRACE);
 		appendable.append(CocoaTranslationConstants.TRANSLATIONSCOPE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.CLOSING_BRACE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.GET_TRANSLATIONSCOPE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		// Generate header and implementation files
 		ArrayList<ClassDescriptor<? extends FieldDescriptor>> classDescriptors = tScope.getClassDescriptors();
 		int length = classDescriptors.size();
 		for (int i = 0; i < length; i++)
 		{
 			generateInitializeStatement(classDescriptors.get(i), appendable);
 		}
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.IF_TRANSLATIONTION_SCOPE_NULL);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append(CocoaTranslationConstants.FILE_PATH_INITIALIZER_STATEMENT.replace(
 				CocoaTranslationConstants.FILE_NAME_PLACEHOLDER, tScope.getName()));
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append(CocoaTranslationConstants.TRANSLATION_INIT_WITH_XML_FILE_PATH);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.RETURN_TRANSLATION_SCOPE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 	}
 
 	/**
 	 * 
 	 * @param directoryLocation
 	 * @param tScope
 	 * @throws IOException
 	 */
 	private void createTranslationScopeGetterClassHeader(File directoryLocation,
 			SimplTypesScope tScope) throws IOException
 	{
 		File headerFile = new File(directoryLocation + CocoaTranslationConstants.FILE_PATH_SEPARATOR
 				+ tScope.getName() + CocoaTranslationConstants.HEADER_FILE_EXTENSION);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(headerFile));
 
 		CocoaTranslationConstants.INHERITENCE_OBJECT = CocoaTranslationConstants.OBJC_OBJECT;
 		// importClass(CocoaTranslationConstants.TRANSLATIONSCOPE, bufferedWriter);
 		startImport(CocoaTranslationConstants.TRANSLATIONSCOPE, tScope, bufferedWriter);
 		openHeaderFile(tScope.getName(), bufferedWriter, null);
 		openFieldDeclartion(bufferedWriter);
 		closeFieldDeclartion(bufferedWriter);
 
 		generateGetterFunctionSignature(bufferedWriter);
 
 		closeHeaderFile(bufferedWriter);
 
 		bufferedWriter.close();
 	}
 
 	private void generateGetterFunctionSignature(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.PLUS);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.OPENING_BRACE);
 		appendable.append(CocoaTranslationConstants.TRANSLATIONSCOPE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.CLOSING_BRACE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.GET_TRANSLATIONSCOPE);
 		appendable.append(CocoaTranslationConstants.END_LINE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void translateToObjCHeader(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException, CocoaTranslationException
 	{
 		File outputFile = createHeaderFileWithDirectoryStructure(inputClass, directoryLocation);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
 		translateToObjCHeader(inputClass, bufferedWriter);
 		bufferedWriter.close();
 	}
 
 	/**
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void translateToObjCImplementation(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException, CocoaTranslationException
 	{
 		File outputFile = createImplementationFileWithDirectoryStructure(inputClass, directoryLocation);
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
 		translateToObjCImplementation(inputClass, bufferedWriter);
 		bufferedWriter.close();
 	}
 
 	/**
 	 * Recursive function to generate output files of the {@code @xml_nested} objects
 	 * <p>
 	 * Takes an input class to generate an Objective-C version of the file. Takes the {@code
 	 * directoryLocation} of the files where the file needs to be generated.
 	 * </p>
 	 * <p>
 	 * This function internally calls the {@code translateToObjC} main entry function to generate the
 	 * required files
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 * @throws Exception
 	 */
 	public void translateToObjCRecursive(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException, CocoaTranslationException
 	{
 		this.isRecursive = true;
 		this.directoryLocation = directoryLocation;
 
 		translateToObjCHeader(inputClass, directoryLocation);
 		translateToObjCImplementation(inputClass, directoryLocation);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C header file. Uses
 	 * constants and appends them to the appendable object for output.
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openHeaderFile(ClassDescriptor thatClass, Appendable appendable) throws IOException
 	{
 		openHeaderFile(thatClass.getDescribedClassSimpleName(), appendable, thatClass);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C header file. Uses
 	 * constants and appends them to the appendable object for output.
 	 * 
 	 * @param className
 	 * @param appendable
 	 * @param classDescriptor
 	 * @throws IOException
 	 */
 	private void openHeaderFile(String className, Appendable appendable, ClassDescriptor classDescriptor)
 			throws IOException
 	{
 
 		appendable.append(CocoaTranslationConstants.FOUNDATION_HEADER);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		if (!CocoaTranslationConstants.INHERITENCE_OBJECT.equals(CocoaTranslationConstants.OBJC_OBJECT))
 		{
 			appendable.append(CocoaTranslationConstants.INCLUDE_OBJECT.replace(
 					CocoaTranslationConstants.AT, CocoaTranslationConstants.INHERITENCE_OBJECT));
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		}
 
 		// includes for nested types
 		if (classDescriptor != null)
 		{
 			HashMapArrayList<String, ? extends FieldDescriptor> fieldDescriptors = classDescriptor
 					.getFieldDescriptorsByFieldName();
 
 			HashMap<ClassDescriptor, Boolean> importedFiles = new HashMap<ClassDescriptor, Boolean>();
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 
 				Boolean alreadyImported = importedFiles.get(fieldDescriptor.getType()) == null ? false : true;
 
 				if (fieldDescriptor.getDeclaringClassDescriptor().getClassSimpleName() == classDescriptor.getClassSimpleName() && !alreadyImported)
 				{
 					if (fieldDescriptor.isNested())
 					{
 						appendable.append(CocoaTranslationConstants.INCLUDE_OBJECT.replace(
 								CocoaTranslationConstants.AT, fieldDescriptor.getObjectiveCTypeName()));
 						appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 					}
 					else if (fieldDescriptor.getFieldType() == ParsedURL.class)
 					{
 						appendable.append(CocoaTranslationConstants.INCLUDE_OBJECT.replace(
 								CocoaTranslationConstants.AT, fieldDescriptor.getObjectiveCTypeName()));
 						appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 					}
 					if (!fieldDescriptor.getFieldType().isPrimitive())
 						importedFiles.put(ClassDescriptor.getClassDescriptor(fieldDescriptor.getFieldType()), true);
 				}
 			}
 		}
 
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 
 		appendClassHeaderComments(className, appendable, classDescriptor);
 
 		appendable.append(CocoaTranslationConstants.INTERFACE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(className);
 		
 		if(!CocoaTranslationConstants.INHERITENCE_OBJECT.equals(CocoaTranslationConstants.OBJC_OBJECT))
 		{
 			appendable.append(CocoaTranslationConstants.SPACE);
 			appendable.append(CocoaTranslationConstants.INHERITENCE_OPERATOR);
 			appendable.append(CocoaTranslationConstants.SPACE);
 			appendable.append(CocoaTranslationConstants.INHERITENCE_OBJECT);
 		}
 		
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	private void appendClassHeaderComments(String className, Appendable appendable,
 			ClassDescriptor inputClass) throws IOException
 	{
 		appendable.append("/*!");
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		appendable.append("@class");
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append(className);
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append("@abstract");
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		appendable.append("This class is generated by CocoaTranslator. ");
 
 		if (inputClass != null)
 		{
 			Annotation[] annotations = inputClass.getClass().getAnnotations();
 			if (annotations != null && annotations.length > 0)
 				appendable.append("Annotated as: ");
 
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 			for (Annotation annotation : annotations)
 			{
 				appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 				appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 				appendable.append(CocoaTranslationConstants.AT);
 				appendable.append(annotation.annotationType().getSimpleName());
 				appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 			}
 		}
 		else
 		{
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		}
 
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		appendable.append("@discussion");
 
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		String javaDocComment = JavaDocParser.getClassJavaDocs(className);
 		if (javaDocComment != null)
 			appendable.append(javaDocComment);
 		else
 			appendable.append("missing java doc comments or could not find the source file.");
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append("*/");
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 	}
 
 	/**
 	 * Simple private function implements the syntax for closing an Objective-C header file. Uses
 	 * constants and appends them to the appendable object for output.
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void closeHeaderFile(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.END);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening the field declaration in Objective-C.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openFieldDeclartion(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * Simple private function implements the syntax for closing the field declaration in Objective-C.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void closeFieldDeclartion(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C implementation file.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openImplementationFile(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
		openImplementationFile(inputClass.getClassSimpleName(), appendable);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C implementation file.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param className
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void openImplementationFile(String className, Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.IMPLEMENTATION);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(className);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param className
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void startImport(String className, Appendable appendable) throws IOException
 	{
 		startImport(className, null, appendable);
 	}
 
 	private void startImport(String className, SimplTypesScope tScope, Appendable appendable)
 			throws IOException
 	{
 		importClass(className, appendable);
 
 		if (tScope != null)
 		{
 			ArrayList<ClassDescriptor<? extends FieldDescriptor>> classes = tScope.getClassDescriptors();
 			int length = classes.size();
 			for (int i = 0; i < length; i++)
 			{
 				importClass(classes.get(i).getDescribedClassSimpleName(), appendable);
 			}
 		}
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param className
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void importClass(String className, Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.INCLUDE_OBJECT.replace(
 				CocoaTranslationConstants.AT, className));
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C implementation file.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendFieldSetterFunctionDefinition(Appendable appendable,
 			FieldDescriptor fieldDescriptor) throws IOException, CocoaTranslationException
 	{
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		checkForKeywords(fieldDescriptor, appendable);
 		appendable.append(CocoaTranslationConstants.RETURN_VOID);
 		appendable.append(CocoaTranslationConstants.SET
 				+ fieldDescriptor.getName().substring(0, 1).toUpperCase());
 		appendable.append(fieldDescriptor.getName().substring(1, fieldDescriptor.getName().length()));
 		appendable.append(CocoaTranslationConstants.WITH_REFERENCE);
 		appendable.append(CocoaTranslationConstants.OPENING_BRACE);
 		appendable.append(fieldDescriptor.getObjectiveCType());
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.CLOSING_BRACE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.PARAMETER);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(CocoaTranslationConstants.END_LINE);
 	}
 
 	/**
 	 * Simple private function implements the syntax for opening an Objective-C implementation file.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendFieldSetterFunctionImplementation(Appendable appendable,
 			FieldDescriptor fieldDescriptor) throws IOException, CocoaTranslationException
 	{
 
 		boolean isKeywordField = false;
 		if (CocoaTranslationUtilities.isKeyword(fieldDescriptor.getName()))
 		{
 			isKeywordField = true;
 		}
 
 		if (isKeywordField)
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_COMMENT);
 
 		appendable.append(CocoaTranslationConstants.RETURN_VOID);
 		appendable.append(CocoaTranslationConstants.SET
 				+ fieldDescriptor.getName().substring(0, 1).toUpperCase());
 		appendable.append(fieldDescriptor.getName().substring(1, fieldDescriptor.getName().length()));
 		appendable.append(CocoaTranslationConstants.WITH_REFERENCE);
 		appendable.append(CocoaTranslationConstants.OPENING_BRACE);
 		appendable.append(fieldDescriptor.getObjectiveCType());
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.CLOSING_BRACE);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.PARAMETER + fieldDescriptor.getName());
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		if (isKeywordField)
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_COMMENT);
 
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(CocoaTranslationConstants.EQUALTO);
 		appendable.append(CocoaTranslationConstants.REFERENCE);
 		appendable.append(CocoaTranslationConstants.PARAMETER);
 		appendable.append(fieldDescriptor.getName());
 
 		appendable.append(CocoaTranslationConstants.END_LINE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		if (isKeywordField)
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_COMMENT);
 
 		appendable.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * Simple private function implements the syntax for closing an Objective-C implemenatation file.
 	 * Uses constants and appends them to the appendable object for output.
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void closeImplementationFile(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.END);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * Appends an attribute in the Objective-C header file for the corresponding attribute in the Java
 	 * class file. The attribute can be a primitive type or reference type. Reference type can be a
 	 * single object, a collection or a nested class object.
 	 * 
 	 * @param classDescriptor
 	 *          TODO
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * 
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendFieldAsObjectiveCAttribute(FieldDescriptor fieldDescriptor,
 			Appendable appendable) throws IOException, CocoaTranslationException
 	{
 		checkForKeywords(fieldDescriptor, appendable);
 
 		appendFieldHeaderDocComment(fieldDescriptor, appendable);
 
 		if (fieldDescriptor.isCollection())
 		{
 			appendFieldAsReference(fieldDescriptor, appendable);
 
 			if (fieldDescriptor.isPolymorphic())
 			{
 				Collection<ClassDescriptor> tagClasses = fieldDescriptor.getPolymorphicClassDescriptors();
 
 				if (tagClasses != null)
 					for (ClassDescriptor classObj : tagClasses)
 					{
 						if (isRecursive)
 						{
 							NestedTranslationHook nestedTranslationHook;
 
 							if (directoryLocation == null)
 							{
 								nestedTranslationHook = new NestedTranslationHook(classObj, appendable);
 								nestedTranslationHooks.add(nestedTranslationHook);
 							}
 							else
 							{
 								nestedTranslationHook = new NestedTranslationHook(classObj, directoryLocation);
 								nestedTranslationHooks.add(nestedTranslationHook);
 							}
 						}
 					}
 			}
 		}
 		else if (fieldDescriptor.isScalar())
 		{
 			if (fieldDescriptor.getScalarType().isPrimitive()
 					&& fieldDescriptor.getField().getType() != String.class)
 			{
 				appendFieldAsPrimitive(fieldDescriptor, appendable);
 			}
 			else if (fieldDescriptor.getScalarType().isReference()
 					|| fieldDescriptor.getField().getType() == String.class)
 			{
 				appendFieldAsReference(fieldDescriptor, appendable);
 			}
 		}
 		else if (fieldDescriptor.isNested())
 		{
 			appendFieldAsNestedAttribute(fieldDescriptor, appendable);
 
 			if (isRecursive)
 			{
 				NestedTranslationHook nestedTranslationHook;
 
 				if (directoryLocation == null)
 				{
 					nestedTranslationHook = new NestedTranslationHook(ClassDescriptor.getClassDescriptor(fieldDescriptor.getFieldType()),
 							appendable);
 					nestedTranslationHooks.add(nestedTranslationHook);
 				}
 				else
 				{
 					nestedTranslationHook = new NestedTranslationHook(ClassDescriptor.getClassDescriptor(fieldDescriptor.getFieldType()),
 							directoryLocation);
 					nestedTranslationHooks.add(nestedTranslationHook);
 				}
 			}
 		}
 	}
 
 	private void appendFieldHeaderDocComment(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append("/*!");
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append("@var");
 
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append("@abstract");
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		appendable.append("Annotated as : ");
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		Annotation[] annotations = fieldDescriptor.getField().getAnnotations();
 		for (Annotation annotation : annotations)
 		{
 			appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 			appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 			appendable.append(CocoaTranslationConstants.TAB);
 			appendable.append(CocoaTranslationConstants.AT);
 			appendable.append(annotation.annotationType().getSimpleName());
 			appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		}
 
 		appendable.append(CocoaTranslationConstants.DOUBLE_TAB);
 		appendable.append("@discussion");
 		appendable.append(CocoaTranslationConstants.TAB);
 
 		String javaDocComment = JavaDocParser.getFieldJavaDocs(fieldDescriptor.getField());
 		if (javaDocComment != null)
 			appendable.append(javaDocComment);
 		else
 			appendable.append("missing java doc comments or could not find the source file.");
 
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append("*/");
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	private void checkForKeywords(FieldDescriptor fieldAccessor, Appendable appendable)
 			throws IOException
 	{
 		if (CocoaTranslationUtilities.isKeyword(fieldAccessor.getName()))
 		{
 			Debug.warning(fieldAccessor, " Field Name: [" + fieldAccessor.getName()
 					+ "]. This is a keyword in objective-c. Cannot translate");
 			appendComments(appendable);
 		}
 	}
 
 	private void appendComments(Appendable appendable) throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_COMMENT);
 
 	}
 
 	/**
 	 * Appends the property of each field using the Objective-C property directive. The object can be
 	 * a primitive or reference type. Reference type can be a single object, a collection or a nested
 	 * class object
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendPropertyOfField(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		checkForKeywords(fieldDescriptor, appendable);
 
 		if (fieldDescriptor.isCollection())
 		{
 			appendPropertyAsReference(fieldDescriptor, appendable);
 		}
 		else if (fieldDescriptor.isScalar())
 		{
 			if (fieldDescriptor.getScalarType().isPrimitive()
 					&& fieldDescriptor.getField().getType() != String.class)
 			{
 				appendPropertyAsPrimitive(fieldDescriptor, appendable);
 			}
 			else if (fieldDescriptor.getScalarType().isReference()
 					|| fieldDescriptor.getField().getType() == String.class)
 			{
 				appendPropertyAsReference(fieldDescriptor, appendable);
 			}
 		}
 		else if (fieldDescriptor.isNested())
 		{
 			appendPropertyAsNestedAttribute(fieldDescriptor, appendable);
 		}
 	}
 
 	/**
 	 * Appends an attribute in the Objective-C header file for the corresponding attribute in the Java
 	 * class file. The attribute can be a primitive type or reference type. Reference type can be a
 	 * single object, a collection or a nested class object.
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendSynthesizedField(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		checkForKeywords(fieldDescriptor, appendable);
 
 		StringBuilder synthesizeDeclaration = new StringBuilder();
 
 		synthesizeDeclaration.append(CocoaTranslationConstants.SYNTHESIZE);
 		synthesizeDeclaration.append(CocoaTranslationConstants.SPACE);
 		synthesizeDeclaration.append(fieldDescriptor.getName());
 		synthesizeDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		synthesizeDeclaration.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(synthesizeDeclaration);
 
 	}
 
 	/**
 	 * Appends a reference type field in the output Objective-C header file
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendFieldAsReference(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		StringBuilder fieldDeclaration = new StringBuilder();
 
 		fieldDeclaration.append(CocoaTranslationConstants.TAB);
 		// fieldDeclaration.append(CocoaTranslationUtilities.getObjectiveCType(fieldAccessor.getField()
 		// .getType()));
 
 		fieldDeclaration.append(fieldDescriptor.getObjectiveCType());
 		fieldDeclaration.append(CocoaTranslationConstants.SPACE);
 		fieldDeclaration.append(CocoaTranslationConstants.REFERENCE);
 		fieldDeclaration.append(fieldDescriptor.getName());
 		fieldDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		fieldDeclaration.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 
 		appendable.append(fieldDeclaration);
 	}
 
 	/**
 	 * Appends a primitive type field in the output Objective-C header file
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendFieldAsPrimitive(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		StringBuilder fieldDeclaration = new StringBuilder();
 
 		fieldDeclaration.append(CocoaTranslationConstants.TAB);
 		// fieldDeclaration.append(CocoaTranslationUtilities.getObjectiveCType(fieldAccessor.getField()
 		// .getType()));
 		fieldDeclaration.append(fieldDescriptor.getScalarType().deriveObjectiveCTypeName());
 		fieldDeclaration.append(CocoaTranslationConstants.SPACE);
 		fieldDeclaration.append(fieldDescriptor.getName());
 		fieldDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		fieldDeclaration.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 
 		appendable.append(fieldDeclaration);
 	}
 
 	/**
 	 * Appends a reference type nested field in the output Objective-C header file
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendFieldAsNestedAttribute(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		StringBuilder fieldDeclaration = new StringBuilder();
 
 		fieldDeclaration.append(CocoaTranslationConstants.TAB);
 		fieldDeclaration.append(CocoaTranslationUtilities.classSimpleName(fieldDescriptor
 				.getFieldType()));
 		fieldDeclaration.append(CocoaTranslationConstants.SPACE);
 		fieldDeclaration.append(CocoaTranslationConstants.REFERENCE);
 		fieldDeclaration.append(fieldDescriptor.getName());
 		fieldDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		fieldDeclaration.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 
 		appendable.append(fieldDeclaration);
 	}
 
 	/**
 	 * Appends a reference type attributes property in the output Objective-C header file
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendPropertyAsReference(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		StringBuilder propertyDeclaration = new StringBuilder();
 
 		propertyDeclaration.append(CocoaTranslationConstants.PROPERTY_REFERENCE);
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(fieldDescriptor.getObjectiveCType());
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(CocoaTranslationConstants.REFERENCE);
 		propertyDeclaration.append(fieldDescriptor.getName());
 		propertyDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		propertyDeclaration.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(propertyDeclaration);
 	}
 
 	/**
 	 * Appends a reference type attributes property in the output Objective-C header file
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendPropertyAsNestedAttribute(FieldDescriptor fieldDescriptor,
 			Appendable appendable) throws IOException, CocoaTranslationException
 	{
 		StringBuilder propertyDeclaration = new StringBuilder();
 
 		propertyDeclaration.append(CocoaTranslationConstants.PROPERTY_REFERENCE);
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(CocoaTranslationUtilities.classSimpleName(fieldDescriptor
 				.getFieldType()));
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(CocoaTranslationConstants.REFERENCE);
 		propertyDeclaration.append(fieldDescriptor.getName());
 		propertyDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		propertyDeclaration.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(propertyDeclaration);
 	}
 
 	/**
 	 * Appends a primitive type attributes property in the output Objective-C header file
 	 * 
 	 * @param fieldAccessor
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void appendPropertyAsPrimitive(FieldDescriptor fieldAccessor, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 		StringBuilder propertyDeclaration = new StringBuilder();
 
 		propertyDeclaration.append(CocoaTranslationConstants.PROPERTY_PRIMITIVE);
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(fieldAccessor.getObjectiveCType());
 		propertyDeclaration.append(CocoaTranslationConstants.SPACE);
 		propertyDeclaration.append(fieldAccessor.getName());
 		propertyDeclaration.append(CocoaTranslationConstants.TERMINATOR);
 		propertyDeclaration.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(propertyDeclaration);
 	}
 
 	/**
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void generateInitializationFunction(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 
 		StringBuilder initializationFunction = new StringBuilder();
 
 		initializationFunction.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		initializationFunction.append(CocoaTranslationConstants.PLUS);
 		initializationFunction.append(CocoaTranslationConstants.SPACE);
 		initializationFunction.append(CocoaTranslationConstants.OPENING_BRACE);
 		initializationFunction.append(CocoaTranslationConstants.VOID);
 		initializationFunction.append(CocoaTranslationConstants.CLOSING_BRACE);
 		initializationFunction.append(CocoaTranslationConstants.SPACE);
 		initializationFunction.append(CocoaTranslationConstants.INITIALIZE);
 		initializationFunction.append(CocoaTranslationConstants.SPACE);
 		initializationFunction.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		initializationFunction.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 		generateInitializeStatement(inputClass, initializationFunction);
 		initializationFunction.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 		initializationFunction.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 
 		appendable.append(initializationFunction);
 	}
 
 	/**
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void generateInitializeStatement(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.OPENING_SQUARE_BRACE);
 		appendable.append(inputClass.getDescribedClassSimpleName());
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.CLASS);
 		appendable.append(CocoaTranslationConstants.CLOSING_SQUARE_BRACE);
 		appendable.append(CocoaTranslationConstants.TERMINATOR);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param inputClass
 	 * @param attributes
 	 * @param appendable
 	 * @throws IOException
 	 * @throws CocoaTranslationException
 	 */
 	private void generateDeallocFunction(ClassDescriptor inputClass,
 			HashMapArrayList<String, ? extends FieldDescriptor> attributes, Appendable appendable)
 			throws IOException, CocoaTranslationException
 	{
 
 
 		appendable.append(CocoaTranslationConstants.RETURN_VOID);
 		appendable.append(CocoaTranslationConstants.DEALLOC);
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.OPENING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		if (attributes.size() > 0)
 		{
 			for (FieldDescriptor fieldDescriptor : attributes)
 			{
 				if (fieldDescriptor.belongsTo(inputClass)
 						&& ((fieldDescriptor.getScalarType() != null && fieldDescriptor.getScalarType()
 								.isReference())
 								|| fieldDescriptor.isCollection() || fieldDescriptor.isNested()))
 				{
 					appendDeallocStatement(fieldDescriptor, appendable);
 				}
 			}
 		}
 
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.SUPER_DEALLOC);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 
 		appendable.append(CocoaTranslationConstants.CLOSING_CURLY_BRACE);
 		appendable.append(CocoaTranslationConstants.DOUBLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendDeallocStatement(FieldDescriptor fieldDescriptor, Appendable appendable)
 			throws IOException
 	{
 		appendable.append(CocoaTranslationConstants.TAB);
 		appendable.append(CocoaTranslationConstants.OPENING_SQUARE_BRACE);
 		appendable.append(fieldDescriptor.getName());
 		appendable.append(CocoaTranslationConstants.SPACE);
 		appendable.append(CocoaTranslationConstants.RELEASE);
 		appendable.append(CocoaTranslationConstants.CLOSING_SQUARE_BRACE);
 		appendable.append(CocoaTranslationConstants.END_LINE);
 		appendable.append(CocoaTranslationConstants.SINGLE_LINE_BREAK);
 	}
 
 	/**
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendHeaderComments(ClassDescriptor inputClass, Appendable appendable) throws IOException
 	{
 		try
 		{
 			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
 			DateFormat yearFormat = new SimpleDateFormat("yyyy");
 
 			Date date = new Date();
 			appendable.append("//\n//  " + inputClass.getClassSimpleName()
 					+ ".h\n//  ecologylabXML\n//\n//  Generated by CocoaTranslator on "
 					+ dateFormat.format(date) + ".\n//  Copyright " + yearFormat.format(date)
 					+ " Interface Ecology Lab. \n//\n\n");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param inputClass
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void appendImplementationComments(ClassDescriptor inputClass, Appendable appendable)
 			throws IOException
 	{
 		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
 		DateFormat yearFormat = new SimpleDateFormat("yyyy");
 
 		Date date = new Date();
 		appendable.append("//\n//  " + inputClass.getClassSimpleName()
 				+ ".m\n//  ecologylabXML\n//\n//  Generated by CocoaTranslator on "
 				+ dateFormat.format(date) + ".\n//  Copyright " + yearFormat.format(date)
 				+ " Interface Ecology Lab. \n//\n\n");
 	}
 
 	/**
 	 * Creates a directory structure from the path of the given by the {@code directoryLocation}
 	 * parameter Uses the class and package names from the parameter {@code inputClass}
 	 * <p>
 	 * This function deletes the files if the files with same class existed inside the directory
 	 * structure and creates a new file for that class
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param directoryLocation
 	 * @return
 	 * @throws IOException
 	 */
 	private File createHeaderFileWithDirectoryStructure(ClassDescriptor inputClass, File directoryLocation)
 			throws IOException
 	{
 		String packageName = inputClass.getDescribedClassPackageName();
 		String className = inputClass.getDescribedClassSimpleName();
 		String currentDirectory = directoryLocation.toString()
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 
 		String[] arrayPackageNames = packageName
 				.split(CocoaTranslationConstants.PACKAGE_NAME_SEPARATOR);
 
 		for (String directoryName : arrayPackageNames)
 		{
 			currentDirectory += directoryName + CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 		}
 
 		File directory = new File(currentDirectory);
 		directory.mkdirs();
 
 		File currentFile = new File(currentDirectory + className
 				+ CocoaTranslationConstants.HEADER_FILE_EXTENSION);
 
 		if (currentFile.exists())
 		{
 			currentFile.delete();
 		}
 
 		currentFile.createNewFile();
 
 		return currentFile;
 	}
 
 	/**
 	 * Creates a directory structure from the path of the given by the {@code directoryLocation}
 	 * parameter Uses the class and package names from the parameter {@code inputClass}
 	 * <p>
 	 * This function deletes the files if the files with same class existed inside the directory
 	 * structure and creates a new file for that class
 	 * </p>
 	 * 
 	 * @param inputClass
 	 * @param directoryLocation
 	 * @return
 	 * @throws IOException
 	 */
 	private File createImplementationFileWithDirectoryStructure(ClassDescriptor inputClass,
 			File directoryLocation) throws IOException
 	{
 		String packageName = inputClass.getDescribedClassPackageName();
 		String className = inputClass.getDescribedClassSimpleName();
 		String currentDirectory = directoryLocation.toString()
 				+ CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 
 		String[] arrayPackageNames = packageName
 				.split(CocoaTranslationConstants.PACKAGE_NAME_SEPARATOR);
 
 		for (String directoryName : arrayPackageNames)
 		{
 			currentDirectory += directoryName + CocoaTranslationConstants.FILE_PATH_SEPARATOR;
 		}
 
 		File directory = new File(currentDirectory);
 		directory.mkdirs();
 
 		File currentFile = new File(currentDirectory + className
 				+ CocoaTranslationConstants.IMPLEMENTATION_FILE_EXTENSION);
 
 		if (currentFile.exists())
 		{
 			currentFile.delete();
 		}
 
 		currentFile.createNewFile();
 
 		return currentFile;
 	}
 
 	/**
 	 * Main method to test the working of the library.
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String args[]) throws Exception
 	{
 		CocoaTranslator c = new CocoaTranslator();
 		// c.translateToObjC(Item.class, new ParsedURL(new File("/")));
 		c
 				.translateToObjC(
 						new File("/output"),
 						SimplTypesScope.get("RSSTranslations", Schmannel.class, BItem.class, SchmItem.class,
 								RssState.class, Item.class, Channel.class),
 						new File(
 								"/Users/nabeelshahzad/Documents/workspace/ecologylabFundamental/ecologylab/xml/library/rss"));
 
 		// c.translateToObjCRecursive(RssState.class, System.out);
 
 	}
 
 }
