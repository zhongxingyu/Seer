 package ecologylab.serialization;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.StringWriter;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Stack;
 
 import org.xml.sax.Attributes;
 
 import ecologylab.generic.Debug;
 import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;
 
 /**
  * This class is the heart of the <code>ecologylab.serialization</code> translation framework.
  * 
  * <p/>
  * To use the framework, the programmer must define a tree of objects derived from this class. The
  * public fields in each of these derived objects correspond to the XML DOM. The declarations of
  * attribute fields must preceed thos for nested XML elements. Attributes are built directly from
  * Strings, using classes derived from
  * 
  * @link ecologylab.types.Type ecologylab.types.Type}.
  * 
  *       <p/>
  *       The framework proceeds automatically through the application of rules. In the standard
  *       case, the rules are based on the automatic mapping of XML element names (aka tags), to
  *       ElementState class names. An mechanism for supplying additional translations may also be
  *       provided.
  * 
  *       <p/>
  *       <code>ElementState</code> is based on 2 methods, each of which employs Java reflection and
  *       recursive descent.
  * 
  *       <li><code>translateToXML(...)</code> translates a tree of these <code>ElementState</code>
  *       objects into XML.</li>
  * 
  *       <li><code>translateFromXML(...)</code> translates an XML DOM into a tree of these
  *       <code>ElementState</code> objects</li>
  * 
  * @author Andruid Kerne
  * @author Madhur Khandelwal
  * 
  * @version 2.9
  */
 public class ElementState extends Debug implements FieldTypes, XMLTranslationExceptionTypes
 {
 
 	private boolean	isRoot	= false;
 
 	// --------//
 
 	public enum FORMAT
 	{
 		XML, JSON, TLV, YAML, BIBTEX;
 	}
 
 	/**
 	 * Link for a DOM tree. should be removed. its not a tree!!
 	 */
 	transient ElementState									parent;
 
 	/**
 	 * to handle objects with multiple parents this variable helps keep track of parents in
 	 * deserializing graph
 	 */
 	Stack<ElementState>											parents										= null;
 
 	/**
 	 * Just-in time look-up tables to make translation be efficient. Allocated on a per class basis.
 	 */
 	transient private ClassDescriptor				classDescriptor;
 
 	/**
 	 * Use for resolving getElementById()
 	 */
 	transient HashMap<String, ElementState>	elementByIdMap;
 
 	transient HashMap<String, ElementState>	nestedNameSpaces;
 
 	static protected final int							ESTIMATE_CHARS_PER_FIELD	= 80;
 
 	/**
 	 * Construct. Create a link to a root optimizations object.
 	 */
 	public ElementState()
 	{
 	}
 
 	public void serialize(OutputStream outStream, FORMAT format) throws SIMPLTranslationException
 	{
 		if (outStream == null)
 			throw new SIMPLTranslationException("outStream is null");
 
 		TranslationContext graphContext = new TranslationContext();
 
 		isRoot = true;
 
 		try
 		{
 			graphContext.resolveGraph(this);
 
 			switch (format)
 			{
 			case XML:
 				serializeToXML(classDescriptor().pseudoFieldDescriptor(), new PrintStream(outStream),
 						graphContext);
 				break;
 			case TLV:
 				serializeToTLV(classDescriptor().pseudoFieldDescriptor(), new DataOutputStream(outStream),
 						graphContext);
 				break;
 			case JSON:
 				serializeToJSON(classDescriptor().pseudoFieldDescriptor(), new PrintStream(outStream),
 						graphContext);
 				break;
 			case BIBTEX:
 				serializeToBibtex(classDescriptor().pseudoFieldDescriptor(), new PrintStream(outStream),
 						graphContext);
 			}
 
 		}
 		catch (IOException e)
 		{
 			throw new SIMPLTranslationException("IO", e);
 		}
 	}
 
 	private void serializeToBibtex(FieldDescriptor fieldDescritpor, PrintStream appendable,
 			TranslationContext graphContext) throws SIMPLTranslationException, IOException
 	{
 		appendable.append("@");
 		appendable.append(fieldDescritpor.getBibtexTagName());
 		appendable.append(" { ");
 
 		ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor()
 				.elementFieldDescriptors();
 
 		ArrayList<FieldDescriptor> attributeFieldDescriptors = classDescriptor()
 				.attributeFieldDescriptors();
 
 		int numAttributes = attributeFieldDescriptors.size();
 		int numElements = elementFieldDescriptors.size();
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				// iterate through fields to find @bibtex_key
 				for (int i = 0; i < numAttributes; i++)
 				{
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					if (childFD.getField().getAnnotation(bibtex_key.class) != null)
 					{
 						childFD.appendValueAsBibtexAttribute(appendable, this, true);
 						break;
 					}
 				}
 
 				// iterate through fields that are not @bibtex_key
 				for (int i = 0; i < numAttributes; i++)
 				{
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					if (childFD.getField().getAnnotation(bibtex_key.class) != null)
 						continue;
 
 					boolean isDefaultValue = childFD.isDefaultValue(this);
 					if (!isDefaultValue)
 					{
 						childFD.appendValueAsBibtexAttribute(appendable, this, false);
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new SIMPLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		boolean elementsSerialized = false;
 		boolean isFirstCollection = true;
 		for (int i = 0; i < numElements; i++)
 		{
 			FieldDescriptor childFD = elementFieldDescriptors.get(i);
 			final int childFdType = childFD.getType();
 			if (childFdType == SCALAR)
 			{
 				try
 				{
 					boolean isDefaultValue = childFD.isDefaultValue(this);
 					if (!isDefaultValue)
 					{
 						childFD.appendValueAsBibtexAttribute(appendable, this, !elementsSerialized);
 						if (!elementsSerialized)
 						{
 							elementsSerialized = true;
 						}
 					}
 				}
 				catch (Exception e)
 				{
 					throw new SIMPLTranslationException("TranslateToXML for leaf node " + this, e);
 				}
 			}
 			else
 			{
 				Object thatReferenceObject = null;
 				Field childField = childFD.getField();
 				try
 				{
 					thatReferenceObject = childField.get(this);
 				}
 				catch (IllegalAccessException e)
 				{
 					debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 					childField.setAccessible(true);
 					try
 					{
 						thatReferenceObject = childField.get(this);
 					}
 					catch (IllegalAccessException e1)
 					{
 						error("Can't access " + childField.getName());
 						e1.printStackTrace();
 					}
 				}
 				// ignore null reference objects
 				if (thatReferenceObject == null)
 					continue;
 
 				final boolean isScalar = (childFdType == COLLECTION_SCALAR) || (childFdType == MAP_SCALAR);
 				// gets Collection object directly or through Map.values()
 				Collection thatCollection;
 				switch (childFdType)
 				{
 				case COLLECTION_ELEMENT:
 				case COLLECTION_SCALAR:
 				case MAP_ELEMENT:
 				case MAP_SCALAR:
 					thatCollection = XMLTools.getCollection(thatReferenceObject);
 					break;
 				default:
 					thatCollection = null;
 					break;
 				}
 				if (thatCollection != null && (thatCollection.size() > 0))
 				{
 					if (elementsSerialized || isFirstCollection)
 					{
 						appendable.append(", ");
 						isFirstCollection = false;
 					}
 
 					elementsSerialized = true;
 
 					if (!childFD.isPolymorphic())
 					{
 						Object[] collecitonArray = thatCollection.toArray();
 						int collectionSize = thatCollection.size();
 
 						if (collectionSize > 0)
 						{
 							childFD.writeBibtexCollectionStart(appendable);
 							appendable.append("{");
 						}
 
 						for (int j = 0; j < collectionSize; j++)
 						{
 							Object next = collecitonArray[j];
 							if (isScalar) // leaf node!
 							{
 								try
 								{
 									String delim = "author".equals(childFD.getBibtexTagName()) ? " and "
 											: graphContext.getDelimiter() + " ";
 									childFD.appendBibtexCollectionAttribute(appendable, next, j == 0, delim);
 								}
 								catch (IllegalArgumentException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 								catch (IllegalAccessException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 							}
 							else if (next instanceof ElementState)
 							{
 								ElementState nestedES = (ElementState) next;
 								FieldDescriptor compositeAsScalarFD = nestedES.classDescriptor()
 										.getScalarValueFieldDescripotor();
 								try
 								{
 									if (compositeAsScalarFD != null)
 										compositeAsScalarFD.appendBibtexCollectionCompositeAttribute(appendable,
 												nestedES, j == 0);
 									else
 										debugA("WARNING : Serializing composite object " + nestedES
 												+ " failed. No inner annotation @simpl_composte_as_scalar present");
 								}
 								catch (IllegalArgumentException e)
 								{
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 								catch (IllegalAccessException e)
 								{
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 						}
 
 						if (collectionSize > 0)
 							appendable.append("}");
 					}
 				}
 				else if (thatReferenceObject instanceof ElementState)
 				{
 					ElementState nestedES = (ElementState) thatReferenceObject;
 					FieldDescriptor compositeAsScalarFD = nestedES.classDescriptor()
 							.getScalarValueFieldDescripotor();
 					try
 					{
 						if (compositeAsScalarFD != null)
 							compositeAsScalarFD.appendValueAsBibtexAttribute(appendable, nestedES,
 									!elementsSerialized);
 						else
 							debugA("WARNING : Serializing composite object " + nestedES
 									+ " failed. No inner annotation @simpl_composte_as_scalar present");
 					}
 					catch (IllegalArgumentException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					catch (IllegalAccessException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		appendable.append('\n');
 		appendable.append("}\n");
 
 	}
 
 	private void serializeToXML(FieldDescriptor pseudoFieldDescriptor, PrintStream printStream,
 			TranslationContext graphContext) throws SIMPLTranslationException, IOException
 	{
 		serializeToAppendable(pseudoFieldDescriptor, printStream, graphContext);
 	}
 
 	private void serializeToJSON(FieldDescriptor fieldDescriptor, PrintStream appendable,
 			TranslationContext graphContext) throws IOException, SIMPLTranslationException
 	{
 		appendable.append('{');
 		serializeToJSONRecursive(fieldDescriptor, appendable, true, graphContext);
 		appendable.append('}');
 	}
 
 	private void serializeToJSONRecursive(FieldDescriptor fieldDescriptor, PrintStream appendable,
 			boolean withTag, TranslationContext graphContext) throws IOException,
 			SIMPLTranslationException
 	{
 		// To handle cyclic pointers. map marshalled ElementState Objects.
 		graphContext.mapElementState(this);
 
 		fieldDescriptor.writeJSONElementStart(appendable, withTag);
 
 		ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor()
 				.elementFieldDescriptors();
 
 		ArrayList<FieldDescriptor> attributeFieldDescriptors = classDescriptor()
 				.attributeFieldDescriptors();
 
 		int numAttributes = attributeFieldDescriptors.size();
 		int numElements = elementFieldDescriptors.size();
 
 		boolean attributesSerialized = false;
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i = 0; i < numAttributes; i++)
 				{
 					// iterate through fields
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					boolean isDefaultValue = childFD.isDefaultValue(this);
 					if (!isDefaultValue)
 					{
 						childFD.appendValueAsJSONAttribute(appendable, this, !attributesSerialized);
 						if (!attributesSerialized)
 						{
 							attributesSerialized = true;
 						}
 					}
 
 				}
 			}
 			catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new SIMPLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		graphContext.appendSimplIdIfRequired(appendable, this, FORMAT.JSON);
 
 		boolean elementsSerialized = false;
 		for (int i = 0; i < numElements; i++)
 		{
 			FieldDescriptor childFD = elementFieldDescriptors.get(i);
 			final int childFdType = childFD.getType();
 			if (childFdType == SCALAR)
 			{
 				try
 				{
 					boolean isDefaultValue = childFD.isDefaultValue(this);
 					if (!isDefaultValue)
 					{
 						childFD.appendValueAsJSONAttribute(appendable, this, !elementsSerialized);
 						if (!elementsSerialized)
 						{
 							elementsSerialized = true;
 						}
 					}
 				}
 				catch (Exception e)
 				{
 					throw new SIMPLTranslationException("TranslateToXML for leaf node " + this, e);
 				}
 			}
 			else
 			{
 				// if (attributesSerialized || i > 0)
 				// appendable.append(", ");
 
 				Object thatReferenceObject = null;
 				Field childField = childFD.getField();
 				try
 				{
 					thatReferenceObject = childField.get(this);
 				}
 				catch (IllegalAccessException e)
 				{
 					debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 					childField.setAccessible(true);
 					try
 					{
 						thatReferenceObject = childField.get(this);
 					}
 					catch (IllegalAccessException e1)
 					{
 						error("Can't access " + childField.getName());
 						e1.printStackTrace();
 					}
 				}
 				// ignore null reference objects
 				if (thatReferenceObject == null)
 					continue;
 
 				final boolean isScalar = (childFdType == COLLECTION_SCALAR) || (childFdType == MAP_SCALAR);
 				// gets Collection object directly or through Map.values()
 				Collection thatCollection;
 				switch (childFdType)
 				{
 				case COLLECTION_ELEMENT:
 				case COLLECTION_SCALAR:
 				case MAP_ELEMENT:
 				case MAP_SCALAR:
 					thatCollection = XMLTools.getCollection(thatReferenceObject);
 					break;
 				default:
 					thatCollection = null;
 					break;
 				}
 
 				if (thatCollection != null && (thatCollection.size() > 0))
 				{
 					if (attributesSerialized || elementsSerialized)
 						appendable.append(", ");
 
 					elementsSerialized = true;
 
 					if (!childFD.isPolymorphic())
 					{
 						if (childFD.isWrapped())
 							childFD.writeJSONWrap(appendable, false);
 
 						Object[] collecitonArray = thatCollection.toArray();
 						int collectionSize = thatCollection.size();
 
 						childFD.writeJSONCollectionStart(appendable);
 
 						for (int j = 0; j < collectionSize; j++)
 						{
 							Object next = collecitonArray[j];
 							if (isScalar) // leaf node!
 							{
 								try
 								{
 									childFD.appendJSONCollectionAttribute(appendable, next, j == 0);
 								}
 								catch (IllegalArgumentException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 								catch (IllegalAccessException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 							}
 							else if (next instanceof ElementState && !childFD.isPolymorphic())
 							{
 								if (j != 0)
 									appendable.append(',');
 
 								ElementState collectionSubElementState = (ElementState) next;
 								serializeCompositeJSONElements(appendable, collectionSubElementState, childFD,
 										graphContext, false);
 								// collectionSubElementState.serializeToJSONRecursive(childFD, appendable, false,
 								// graphContext);
 							}
 						}
 
 						childFD.writeJSONCollectionClose(appendable);
 
 						if (childFD.isWrapped())
 							childFD.writeJSONWrap(appendable, true);
 					}
 					else
 					{
 						Object[] collecitonArray = thatCollection.toArray();
 						int collectionSize = thatCollection.size();
 
 						childFD.writeJSONPolymorphicCollectionStart(appendable);
 
 						for (int j = 0; j < collectionSize; j++)
 						{
 							if (j != 0)
 								appendable.append(',');
 
 							Object next = collecitonArray[j];
 							ElementState collectionSubElementState = (ElementState) next;
 
 							FieldDescriptor collectionElementFD = collectionSubElementState.classDescriptor()
 									.pseudoFieldDescriptor();
 
 							appendable.append('{');
 
 							serializeCompositeJSONElements(appendable, collectionSubElementState,
 									collectionElementFD, graphContext, true);
 
 							// collectionSubElementState.serializeToJSONRecursive(collectionElementFD, appendable,
 							// true, graphContext);
 							appendable.append('}');
 						}
 
 						childFD.writeJSONCollectionClose(appendable);
 					}
 
 				}
 				else if (thatReferenceObject instanceof ElementState)
 				{
 					if (attributesSerialized || elementsSerialized)
 						appendable.append(", ");
 
 					elementsSerialized = true;
 
 					ElementState nestedES = (ElementState) thatReferenceObject;
 					FieldDescriptor nestedFD = childFD.isPolymorphic() ? nestedES.classDescriptor()
 							.pseudoFieldDescriptor() : childFD;
 
 					serializeCompositeJSONElements(appendable, nestedES, nestedFD, graphContext, true);
 					// nestedES.serializeToJSONRecursive(nestedFD, appendable, true, graphContext);
 
 				}
 			}
 		}
 
 		fieldDescriptor.writeJSONCloseTag(appendable);
 	}
 
 	private void serializeToTLV(FieldDescriptor fieldDescriptor, DataOutputStream dataOutputStream,
 			TranslationContext graphContext) throws IOException, SIMPLTranslationException
 	{
 
 		// To handle cyclic pointers. map marshalled ElementState Objects.
 		graphContext.mapElementState(this);
 
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		DataOutputStream outputBuffer = new DataOutputStream(byteArrayOutputStream);
 
 		int tlvId = fieldDescriptor.getTLVId();
 
 		ArrayList<FieldDescriptor> attributeFieldDescriptors = classDescriptor()
 				.attributeFieldDescriptors();
 
 		int numAttributes = attributeFieldDescriptors.size();
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i = 0; i < numAttributes; i++)
 				{
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					childFD.appendTLV(outputBuffer, this);
 				}
 			}
 			catch (Exception e)
 			{
 				throw new SIMPLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		// To handle cyclic graphs append simpl id as an attribute.
 		// appendSimplIdIfRequired(buffy);
 
 		// ArrayList<Field> elementFields = optimizations.elementFields();
 		ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor()
 				.elementFieldDescriptors();
 		int numElements = elementFieldDescriptors.size();
 
 		for (int i = 0; i < numElements; i++)
 		{
 			// NodeToJavaOptimizations pte = optimizations.getPTEByFieldName(thatFieldName);
 			FieldDescriptor childFD = elementFieldDescriptors.get(i);
 			final int childFdType = childFD.getType();
 			if (childFdType == SCALAR)
 			{
 				try
 				{
 					childFD.appendTLV(outputBuffer, this);
 				}
 				catch (Exception e)
 				{
 					throw new SIMPLTranslationException("TranslateToXML for leaf node " + this, e);
 				}
 			}
 			else
 			{
 				Object thatReferenceObject = null;
 				Field childField = childFD.getField();
 				try
 				{
 					thatReferenceObject = childField.get(this);
 				}
 				catch (IllegalAccessException e)
 				{
 					debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 					childField.setAccessible(true);
 					try
 					{
 						thatReferenceObject = childField.get(this);
 					}
 					catch (IllegalAccessException e1)
 					{
 						error("Can't access " + childField.getName());
 						e1.printStackTrace();
 					}
 				}
 				// ignore null reference objects
 				if (thatReferenceObject == null)
 					continue;
 
 				final boolean isScalar = (childFdType == COLLECTION_SCALAR) || (childFdType == MAP_SCALAR);
 				// gets Collection object directly or through Map.values()
 				Collection thatCollection;
 				switch (childFdType)
 				{
 				case COLLECTION_ELEMENT:
 				case COLLECTION_SCALAR:
 				case MAP_ELEMENT:
 				case MAP_SCALAR:
 					thatCollection = XMLTools.getCollection(thatReferenceObject);
 					break;
 				default:
 					thatCollection = null;
 					break;
 				}
 
 				if (thatCollection != null && (thatCollection.size() > 0))
 				{
 
 					ByteArrayOutputStream byteArrayOutputStreamCollection = new ByteArrayOutputStream();
 					DataOutputStream collectionBuffer = new DataOutputStream(byteArrayOutputStreamCollection);
 
 					for (Object next : thatCollection)
 					{
 						if (isScalar) // leaf node!
 						{
 							try
 							{
 								childFD.appendTLVCollectionItem(collectionBuffer, next);
 							}
 							catch (IllegalArgumentException e)
 							{
 								throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this, e);
 							}
 							catch (IllegalAccessException e)
 							{
 								throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this, e);
 							}
 						}
 						else if (next instanceof ElementState)
 						{
 							ElementState collectionSubElementState = (ElementState) next;
 							final Class<? extends ElementState> collectionElementClass = collectionSubElementState
 									.getClass();
 
 							FieldDescriptor collectionElementFD = childFD.isPolymorphic() ?
 							// tag by class
 							collectionSubElementState.classDescriptor().pseudoFieldDescriptor()
 									: childFD; // tag by annotation
 
 							// inside handles cyclic pointers by translating only the simpl id if already
 							// serialized.
 							collectionSubElementState.serializeToTLV(collectionElementFD, collectionBuffer,
 									graphContext);
 
 							// collectionSubElementState.serializeToAppendable(collectionElementFD, appendable);
 						}
 						else
 							throw collectionElementTypeException(thatReferenceObject);
 					}
 
 					if (childFD.isWrapped())
 						childFD.writeTLVWrap(outputBuffer, byteArrayOutputStreamCollection);
 					else
 						byteArrayOutputStreamCollection.writeTo(outputBuffer);
 				}
 				else if (thatReferenceObject instanceof ElementState)
 				{ // one of our nested elements, so recurse
 					ElementState nestedES = (ElementState) thatReferenceObject;
 					// if the field type is the same type of the instance (that is, if no subclassing),
 					// then use the field name to determine the XML tag name.
 					// if the field object is an instance of a subclass that extends the declared type of
 					// the
 					// field, use the instance's type to determine the XML tag name.
 					FieldDescriptor nestedFD = childFD.isPolymorphic() ? nestedES.classDescriptor()
 							.pseudoFieldDescriptor() : childFD;
 
 					nestedES.serializeToTLV(nestedFD, outputBuffer, graphContext);
 				}
 			}
 		} // end if no nested elements or text node
 
 		appendTLVHeader(dataOutputStream, byteArrayOutputStream, tlvId);
 	}
 
 	private void appendTLVHeader(DataOutputStream dataOutputStream, ByteArrayOutputStream buffer,
 			int tlvId) throws IOException
 	{
 		dataOutputStream.writeInt(tlvId);
 		dataOutputStream.writeInt(buffer.size());
 		buffer.writeTo(dataOutputStream);
 	}
 
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements.
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements must be after all
 	 * attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * 
 	 * @return the generated xml string, in a Reusable SBtringBuilder
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if there is a problem with the structure. Specifically, in each ElementState object,
 	 *           fields for attributes must be declared before all fields for nested elements (those
 	 *           derived from ElementState). If there is any public field which is not derived from
 	 *           ElementState declared after the declaration for 1 or more ElementState instance
 	 *           variables, this exception will be thrown.
 	 */
 	public StringBuilder serialize() throws SIMPLTranslationException
 	{
 		return serialize((StringBuilder) null);
 	}
 
 	/**
 	 * Serializes this object in the specified format and returns as a string builder.
 	 * 
 	 * @param format
 	 *          The desired format.
 	 * @throws SIMPLTranslationException
 	 * 
 	 */
 	public StringBuilder serialize(FORMAT format) throws SIMPLTranslationException
 	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		serialize(outputStream, format);
 		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(outputStream);
 		return stringBuilder;
 	}
 
 	/**
 	 * Allocated a StringBuilder for translateToXML(), based on a rough guess of how many fields there
 	 * are to translate.
 	 * 
 	 * @return
 	 */
 	private StringBuilder allocStringBuilder()
 	{
 		return new StringBuilder(classDescriptor().numFields() * ESTIMATE_CHARS_PER_FIELD);
 	}
 
 	/**
 	 * Translates a tree of ElementState objects into equivalent XML in a StringBuilder.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements must be after all
 	 * attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * 
 	 * @param buffy
 	 *          StringBuilder to translate into, or null if you want one created for you.
 	 * 
 	 * @return the generated xml string
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if a problem arises during translation. Problems with Field access are possible, but
 	 *           very unlikely.
 	 */
 	public StringBuilder serialize(StringBuilder buffy) throws SIMPLTranslationException
 	{
 		if (buffy == null)
 			buffy = allocStringBuilder();
 
 		try
 		{
 			// first-pass of the two pass algorithm. resolves cyclic pointers by creating appropriate data
 			// structures.
 			TranslationContext graphContext = new TranslationContext();
 			graphContext.resolveGraph(this);
 
 			isRoot = true;
 
 			serializeToBuilder(classDescriptor().pseudoFieldDescriptor(), buffy, graphContext);
 
 			return buffy;
 		}
 		catch (IOException e)
 		{
 			throw new SIMPLTranslationException("IO", e);
 		}
 	}
 
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the File passed in.
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * <p/>
 	 * Makes directories if necessary.
 	 * 
 	 * @param outputFile
 	 *          File to write the XML to.
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if a problem arises during translation. Problems with Field access are possible, but
 	 *           very unlikely.
 	 * @throws IOException
 	 *           If there are problems with the file.
 	 */
 	public void serialize(File outputFile) throws SIMPLTranslationException, IOException
 	{
 		XMLTools.createParentDirs(outputFile);
 
 		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 		serialize(bufferedWriter, outputFile.getParentFile());
 		bufferedWriter.close();
 	}
 
 	/**
 	 * Translates a tree of ElementState objects, and writes the output to the Appendable passed in.
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * 
 	 * @param appendable
 	 *          Appendable to translate into. Must be non-null. Can be a Writer, OutputStream, ...
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if a problem arises during translation. The most likely cause is an IOException.
 	 * 
 	 *           <p/>
 	 *           Problems with Field access are possible, but very unlikely.
 	 */
 	public void serialize(Appendable appendable) throws SIMPLTranslationException
 	{
 		serialize(appendable, null);
 	}
 
 	/**
 	 * Serialize to System.out. Catch and report exceptions. Output a new line to System.out.
 	 */
 	public void serializeOut()
 	{
 
 	}
 
 	public void serializeOut(String msg)
 	{
 		try
 		{
 			System.out.print(msg);
 			System.out.print(": ");
 			serialize(System.out);
 			System.out.println();
 
 		}
 		catch (SIMPLTranslationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void serialize(Appendable appendable, File fileContext) throws SIMPLTranslationException
 	{
 		if (appendable == null)
 			throw new SIMPLTranslationException("Appendable is null");
 
 		try
 		{
 			// first-pass of the two pass algorithm. resolves cyclic pointers by creating appropriate data
 			// structures.
 			TranslationContext graphContext = (fileContext == null) ? new TranslationContext()
 					: new TranslationContext(fileContext);
 			graphContext.resolveGraph(this);
 			isRoot = true;
 			serializeToAppendable(classDescriptor().pseudoFieldDescriptor(), appendable, graphContext);
 
 		}
 		catch (IOException e)
 		{
 			throw new SIMPLTranslationException("IO", e);
 		}
 	}
 
 	public TranslationContext createGraphContext() throws SIMPLTranslationException
 	{
 		TranslationContext graphContext = new TranslationContext();
 		graphContext.resolveGraph(this);
 		isRoot = true;
 		return graphContext;
 	}
 
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements must be after all
 	 * attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * 
 	 * @return the generated xml string
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if a problem arises during translation. Problems with Field access are possible, but
 	 *           very unlikely.
 	 * @throws IOException
 	 */
 	private void serializeToBuilder(FieldDescriptor fieldDescriptor, StringBuilder buffy,
 			TranslationContext graphContext) throws SIMPLTranslationException, IOException
 	{
 
 		// To handle cyclic pointers. map marshalled ElementState Objects.
 		graphContext.mapElementState(this);
 
 		this.serializationPreHook();
 
 		fieldDescriptor.writeElementStart(buffy);
 
 		// TODO -- namespace support
 		// ArrayList<FieldToXMLOptimizations> xmlnsF2XOs =
 		// classDescriptor().xmlnsAttributeOptimizations();
 		// int numXmlnsAttributes = (xmlnsF2XOs == null) ? 0 : xmlnsF2XOs.size();
 		// if (numXmlnsAttributes > 0)
 		// {
 		// for (int i=0; i<numXmlnsAttributes; i++)
 		// {
 		// FieldToXMLOptimizations xmlnsF2Xo = xmlnsF2XOs.get(i);
 		// xmlnsF2Xo.xmlnsAttr(buffy);
 		// }
 		// }
 
 		ArrayList<FieldDescriptor> attributeFieldDescriptors = classDescriptor()
 				.attributeFieldDescriptors();
 		int numAttributes = attributeFieldDescriptors.size();
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i = 0; i < numAttributes; i++)
 				{
 					// iterate through fields
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					childFD.appendValueAsAttribute(buffy, this);
 				}
 			}
 			catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new SIMPLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		if (graphContext.isGraph() && isRoot)
 		{
 			graphContext.appendSimplNameSpace(buffy);
 		}
 
 		// To handle cyclic graphs append simpl id as an attribute.
 		graphContext.appendSimplIdIfRequired(buffy, this, FORMAT.XML);
 
 		ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor()
 				.elementFieldDescriptors();
 		int numElements = elementFieldDescriptors.size();
 
 		boolean hasXmlText = fieldDescriptor.hasXmlText();
 		if ((numElements == 0) && !hasXmlText)
 		{
 			buffy.append('/').append('>'); // done! completely close element behind attributes
 		}
 		else
 		{
 			if (!fieldDescriptor.isXmlNsDecl())
 				buffy.append('>'); // close open tag behind attributes
 
 			if (hasXmlText)
 			{
 				try
 				{
 					classDescriptor().getScalarTextFD().appendXMLScalarText(buffy, this);
 				}
 				catch (Exception e)
 				{
 					throw new SIMPLTranslationException("TranslateToXML for @xml_field " + this, e);
 				}
 			}
 
 			for (int i = 0; i < numElements; i++)
 			{
 				FieldDescriptor childFD = elementFieldDescriptors.get(i);
 				final int childFdType = childFD.getType();
 				if (childFD.getType() == SCALAR)
 				{
 					try
 					{
 						childFD.appendLeaf(buffy, this);
 					}
 					catch (Exception e)
 					{
 						throw new SIMPLTranslationException("TranslateToXML for leaf node " + this, e);
 					}
 				}
 				else
 				{
 					Object thatReferenceObject = null;
 					Field childField = childFD.getField();
 					try
 					{
 						thatReferenceObject = childField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 						childField.setAccessible(true);
 						try
 						{
 							thatReferenceObject = childField.get(this);
 						}
 						catch (IllegalAccessException e1)
 						{
 							error("Can't access " + childField.getName());
 							e1.printStackTrace();
 						}
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 						continue;
 
 					final boolean isScalar = (childFdType == COLLECTION_SCALAR)
 							|| (childFdType == MAP_SCALAR);
 					// gets Collection object directly or through Map.values()
 					Collection thatCollection;
 					switch (childFdType)
 					{
 					case COLLECTION_ELEMENT:
 					case COLLECTION_SCALAR:
 					case MAP_ELEMENT:
 					case MAP_SCALAR:
 						thatCollection = XMLTools.getCollection(thatReferenceObject);
 						break;
 					default:
 						thatCollection = null;
 						break;
 					}
 
 					if (thatCollection != null && (thatCollection.size() > 0))
 					{
 						// if the object is a collection,
 						// iterate thru the collection and emit XML from each element
 						if (childFD.isWrapped())
 							childFD.writeWrap(buffy, false);
 						for (Object next : thatCollection)
 						{
 							if (isScalar) // leaf node!
 							{
 								try
 								{
 									childFD.appendCollectionLeaf(buffy, next);
 								}
 								catch (IllegalArgumentException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 								catch (IllegalAccessException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 							}
 							else if (next instanceof ElementState)
 							{
 								ElementState collectionSubElementState = (ElementState) next;
 
 								// FIXME -- uses class instead of field to get F2XO
 								// does this work correctly with @xml_classes ?
 								final Class<? extends ElementState> collectionElementClass = collectionSubElementState
 										.getClass();
 								// TODO -- changed by andruid 7/21/08 -- not sure if this breaks anything else, but
 								// it seems correct,
 								// and it fixes @simpl_scalar @simpl_hints(Hint.XML_TEXT) output
 
 								FieldDescriptor collectionElementFD = childFD.isPolymorphic() ? collectionSubElementState
 										.classDescriptor().pseudoFieldDescriptor()
 										: childFD;
 
 								// inside handles cyclic pointers by translating only the simpl id if already
 								// serialized.
 								serializeCompositeElements(buffy, collectionSubElementState, collectionElementFD,
 										graphContext);
 								// collectionSubElementState.serializeToBuilder(collectionElementFD, buffy);
 							}
 							else
 								throw collectionElementTypeException(thatReferenceObject);
 						}
 						if (childFD.isWrapped())
 							childFD.writeWrap(buffy, true);
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{ // one of our nested elements, so recurse
 						ElementState nestedES = (ElementState) thatReferenceObject;
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of
 						// the
 						// field, use the instance's type to determine the XML tag name.
 						FieldDescriptor nestedFD = childFD.isPolymorphic() ? nestedES.classDescriptor()
 								.pseudoFieldDescriptor() : childFD;
 
 						if (childFD.isWrapped())
 							childFD.writeWrap(buffy, false);
 
 						// inside handles cyclic pointers by translating only the simpl id if already
 						// serialized.
 						serializeCompositeElements(buffy, nestedES, nestedFD, graphContext);
 
 						if (childFD.isWrapped())
 							childFD.writeWrap(buffy, true);
 
 						// buffy.append('\n');
 					}
 				}
 			} // end of for each element child
 
 			// TODO -- namespace support!
 			// HashMap<String, ElementState> nestedNameSpaces = this.nestedNameSpaces;
 			// if (nestedNameSpaces != null)
 			// {
 			// for (ElementState nestedNSE : nestedNameSpaces.values())
 			// {
 			// //TODO -- where do we get optimizations for nested namespace elements?
 			// FieldToXMLOptimizations nestedNsF2XO =
 			// nestedNSE.classDescriptor().pseudoFieldDescriptor();
 			// nestedNSE.translateToXMLBuilder(nestedNsF2XO, buffy);
 			// }
 			// }
 			// end the element
 			fieldDescriptor.writeCloseTag(buffy);
 		} // end if no nested elements or text node
 
 		this.serializationPostHook();
 	}
 
 	/**
 	 * Generate an exception during translateToXML() when a collection contains elements that are not
 	 * ElementState subclasses, or ScalarType leafs.
 	 * 
 	 * @param thatReferenceObject
 	 * @return
 	 */
 	private SIMPLTranslationException collectionElementTypeException(Object thatReferenceObject)
 	{
 		return new SIMPLTranslationException("Collections MUST contain "
 				+ "objects of class derived from ElementState or Scalars, but " + thatReferenceObject
 				+ " contains some that aren't.");
 	}
 
 	/**
 	 * /** Translates a tree of ElementState objects, and writes the output to the Appendable passed
 	 * in
 	 * <p/>
 	 * Uses Java reflection to iterate through the public fields of the object. When primitive types
 	 * are found, they are translated into attributes. When objects derived from ElementState are
 	 * found, they are recursively translated into nested elements.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default value for each type.
 	 * Attributes which are set to the default value (for that type), are not emitted.
 	 * 
 	 * @param fieldDescriptor
 	 * @param appendable
 	 *          Appendable to translate into. Must be non-null. Can be a Writer, OutputStream, ...
 	 * 
 	 * @throws SIMPLTranslationException
 	 *           if a problem arises during translation. Problems with Field access are possible, but
 	 *           very unlikely.
 	 * @throws IOException
 	 */
 	private void serializeToAppendable(FieldDescriptor fieldDescriptor, Appendable appendable,
 			TranslationContext serializationContext) throws SIMPLTranslationException, IOException
 	{
 
 		// To handle cyclic pointers. map marshalled ElementState Objects.
 		serializationContext.mapElementState(this);
 
 		this.serializationPreHook();
 
 		fieldDescriptor.writeElementStart(appendable);
 
 		// TODO -- namespace support
 		// ArrayList<FieldToXMLOptimizations> xmlnsF2XOs =
 		// classDescriptor().xmlnsAttributeOptimizations();
 		// int numXmlnsAttributes = (xmlnsF2XOs == null) ? 0 : xmlnsF2XOs.size();
 		// if (numXmlnsAttributes > 0)
 		// {
 		// for (int i=0; i<numXmlnsAttributes; i++)
 		// {
 		// FieldToXMLOptimizations xmlnsF2Xo = xmlnsF2XOs.get(i);
 		// xmlnsF2Xo.xmlnsAttr(appendable);
 		// }
 		// }
 		ArrayList<FieldDescriptor> attributeFieldDescriptors = classDescriptor()
 				.attributeFieldDescriptors();
 
 		int numAttributes = attributeFieldDescriptors.size();
 
 		if (numAttributes > 0)
 		{
 			try
 			{
 				for (int i = 0; i < numAttributes; i++)
 				{
 					// iterate through fields
 					FieldDescriptor childFD = attributeFieldDescriptors.get(i);
 					childFD.appendValueAsAttribute(appendable, this, serializationContext);
 				}
 			}
 			catch (Exception e)
 			{
 				// IllegalArgumentException, IllegalAccessException
 				throw new SIMPLTranslationException("TranslateToXML for attribute " + this, e);
 			}
 		}
 
 		if (serializationContext.isGraph() && isRoot)
 		{
 			serializationContext.appendSimplNameSpace(appendable);
 		}
 
 		// To handle cyclic graphs append simpl id as an attribute.
 		serializationContext.appendSimplIdIfRequired(appendable, this, FORMAT.XML);
 
 		// ArrayList<Field> elementFields = optimizations.elementFields();
 		ArrayList<FieldDescriptor> elementFieldDescriptors = classDescriptor()
 				.elementFieldDescriptors();
 		int numElements = elementFieldDescriptors.size();
 
 		boolean hasXmlText = classDescriptor().hasScalarFD();
 		if ((numElements == 0) && !hasXmlText)
 		{
 			appendable.append('/').append('>'); // done! completely close element behind attributes
 		}
 		else
 		{
 			if (!fieldDescriptor.isXmlNsDecl())
 				appendable.append('>'); // close open tag behind attributes unless in a nested namespace
 			// root
 
 			if (hasXmlText)
 			{
 				try
 				{
 					classDescriptor().getScalarTextFD().appendXMLScalarText(appendable, this);
 				}
 				catch (Exception e)
 				{
 					throw new SIMPLTranslationException("TranslateToXML for @xml_field " + this, e);
 				}
 			}
 
 			for (int i = 0; i < numElements; i++)
 			{
 				// NodeToJavaOptimizations pte = optimizations.getPTEByFieldName(thatFieldName);
 				FieldDescriptor childFD = elementFieldDescriptors.get(i);
 				final int childFdType = childFD.getType();
 				if (childFdType == SCALAR)
 				{
 					try
 					{
 						childFD.appendLeaf(appendable, this, serializationContext);
 					}
 					catch (Exception e)
 					{
 						throw new SIMPLTranslationException("TranslateToXML for leaf node " + this, e);
 					}
 				}
 				else
 				{
 					Object thatReferenceObject = null;
 					Field childField = childFD.getField();
 					try
 					{
 						thatReferenceObject = childField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						debugA("WARNING re-trying access! " + e.getStackTrace()[0]);
 						childField.setAccessible(true);
 						try
 						{
 							thatReferenceObject = childField.get(this);
 						}
 						catch (IllegalAccessException e1)
 						{
 							error("Can't access " + childField.getName());
 							e1.printStackTrace();
 						}
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 						continue;
 
 					final boolean isScalar = (childFdType == COLLECTION_SCALAR)
 							|| (childFdType == MAP_SCALAR);
 					// gets Collection object directly or through Map.values()
 					Collection thatCollection;
 					switch (childFdType)
 					{
 					case COLLECTION_ELEMENT:
 					case COLLECTION_SCALAR:
 					case MAP_ELEMENT:
 					case MAP_SCALAR:
 						thatCollection = XMLTools.getCollection(thatReferenceObject);
 						break;
 					default:
 						thatCollection = null;
 						break;
 					}
 
 					if (thatCollection != null && (thatCollection.size() > 0))
 					{ // if the object is a collection, iterate thru the collection and emit XML for each
 						// element
 						if (childFD.isWrapped())
 							childFD.writeWrap(appendable, false);
 						for (Object next : thatCollection)
 						{
 							if (isScalar) // leaf node!
 							{
 								try
 								{
 									childFD.appendCollectionLeaf(appendable, next);
 								}
 								catch (IllegalArgumentException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 								catch (IllegalAccessException e)
 								{
 									throw new SIMPLTranslationException("TranslateToXML for collection leaf " + this,
 											e);
 								}
 							}
 							else if (next instanceof ElementState)
 							{
 								ElementState collectionSubElementState = (ElementState) next;
 								final Class<? extends ElementState> collectionElementClass = collectionSubElementState
 										.getClass();
 
 								FieldDescriptor collectionElementFD = childFD.isPolymorphic() ?
 								// tag by class
 								collectionSubElementState.classDescriptor().pseudoFieldDescriptor()
 										: childFD; // tag by annotation
 
 								// inside handles cyclic pointers by translating only the simpl id if already
 								// serialized.
 								serializeCompositeElements(appendable, collectionSubElementState,
 										collectionElementFD, serializationContext);
 
 								// collectionSubElementState.serializeToAppendable(collectionElementFD, appendable);
 							}
 							else
 								throw collectionElementTypeException(thatReferenceObject);
 						}
 						if (childFD.isWrapped())
 							childFD.writeWrap(appendable, true);
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{ // one of our nested elements, so recurse
 						ElementState nestedES = (ElementState) thatReferenceObject;
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of
 						// the
 						// field, use the instance's type to determine the XML tag name.
 						FieldDescriptor nestedFD = childFD.isPolymorphic() ? nestedES.classDescriptor()
 								.pseudoFieldDescriptor() : childFD;
 
 						if (childFD.isWrapped())
 							childFD.writeWrap(appendable, false);
 						// inside handles cyclic pointers by translating only the simpl id if already
 						// serialized.
 						serializeCompositeElements(appendable, nestedES, nestedFD, serializationContext);
 
 						if (childFD.isWrapped())
 							childFD.writeWrap(appendable, true);
 					}
 				}
 			} // end of for each element child
 			// FIXME -- Add namespace support!
 			// HashMap<String, ElementState> nestedNameSpaces = this.nestedNameSpaces;
 			// if (nestedNameSpaces != null)
 			// {
 			// for (ElementState nestedNSE : nestedNameSpaces.values())
 			// {
 			// // translate nested namespace root
 			// FieldToXMLOptimizations nestedNsF2XO =
 			// nestedNSE.classDescriptor().pseudoFieldDescriptor();
 			// nestedNSE.translateToXMLAppendable(nestedNsF2XO, appendable);
 			// }
 			// }
 			// end the element
 			fieldDescriptor.writeCloseTag(appendable);
 		} // end if no nested elements or text node
 
 		this.serializationPostHook();
 	}
 
 	/**
 	 * Link new born root element to its Optimizations and create an elementByIdMap for it.
 	 */
 	void setupRoot()
 	{
 		elementByIdMap = new HashMap<String, ElementState>();
 	}
 
 	/**
 	 * Used in SAX parsing to unmarshall attributes into fields.
 	 * 
 	 * @param translationScope
 	 * @param attributes
 	 * @param scalarUnmarshallingContext
 	 *          TODO
 	 */
 	void translateAttributes(TranslationScope translationScope, Attributes attributes,
 			ScalarUnmarshallingContext scalarUnmarshallingContext, ElementState context,
 			TranslationContext graphContext)
 	{
 		int numAttributes = attributes.getLength();
 		for (int i = 0; i < numAttributes; i++)
 		{
 			// TODO -- figure out what we're doing if there's a colon and a namespace
 			final String tag = attributes.getQName(i);
 			final String value = attributes.getValue(i);
 
 			if (graphContext.handleSimplIds(tag, value, this))
 				continue;
 
 			// TODO String attrType = getType()?!
 			if (value != null)
 			{
 				ClassDescriptor classDescriptor = classDescriptor();
 				FieldDescriptor fd = classDescriptor
 						.getFieldDescriptorByTag(tag, translationScope, context);
 				if (fd == null)
 					classDescriptor.warning(" FieldDescriptor not found for tag " + tag);
 				else
 				{
 					try
 					{
 						// don't enforce hints -- if there is an attribute, use it.
 						fd.setFieldToScalar(this, value, scalarUnmarshallingContext);
 						// the value can become a unique id for looking up this
 						// TODO -- could support the ID type for the node here!
 						if ("id".equals(fd.getTagName()))
 							this.elementByIdMap.put(value, this);
 					}
 					catch (Throwable ex)
 					{
 						classDescriptor.error(" processing FieldDescriptor for tag " + tag);
 						ex.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Translate to XML, then write the result to a file.
 	 * 
 	 * @param outputFileName
 	 * @throws SIMPLTranslationException
 	 */
 	public void serialize(String outputFileName) throws SIMPLTranslationException, IOException
 	{
 		if (!outputFileName.endsWith(".xml") && !outputFileName.endsWith(".XML"))
 		{
 			outputFileName = outputFileName + ".xml";
 		}
 		serialize(new File(outputFileName));
 	}
 
 	// ///////////////////////// other methods //////////////////////////
 
 	/**
 	 * The DOM classic accessor method.
 	 * 
 	 * @return element in the tree rooted from this, whose id attrribute is as in the parameter.
 	 * 
 	 */
 	public ElementState getElementStateById(String id)
 	{
 		return this.elementByIdMap.get(id);
 	}
 
 	/**
 	 * @return the parent
 	 */
 	public ElementState parent()
 	{
 		// return (parent != null) ? parent :
 		// (parents != null && !parents.empty()) ? parents.firstElement() : null;
 		return parent;
 	}
 
 	/**
 	 * Set the parent of this, to create the tree structure.
 	 * 
 	 * @param parent
 	 */
 	public void setParent(ElementState parent)
 	{
 		this.parent = parent;
 	}
 
 	public ElementState getRoot()
 	{
 		ElementState parent = parent();
 		return parent == null ? this : parent.getRoot();
 	}
 
 	/**
 	 * Metalanguage declaration that tells simpl serialization that each Field it is applied to as an
 	 * annotation is a scalar-value.
 	 * <p/>
 	 * The attribute name will be derived from the field name, using camel case conversion, unless @xml_tag
 	 * is used.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_scalar
 	{
 	}
 
 	/**
 	 * S.IM.PL declaration for hints that precisely define the syntactic structure of serialization.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_hints
 	{
 		Hint[] value() default
 		{ Hint.XML_ATTRIBUTE };
 	}
 
 	/**
 	 * S.IM.PL declaration for scalar fields.
 	 * <p/>
 	 * Specifies filtering a scalar value on input, using a regex, before marshalling by a ScalarType.
 	 * Only activated when you call on your TranslationScope instance, setPerformFilters(), before
 	 * calling deserialize(Stream).
 	 * 
 	 * @author andruid
 	 * 
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_filter
 	{
 		String regex();
 
 		String replace() default "";
 	}
 
 	/**
 	 * Optional metalanguage declaration. Enables specificaition of one or more formatting strings.
 	 * Only affects ScalarTyped Fields (ignored otherwise). The format string will be passed to the
 	 * ScalarType for type-specific interpretation.
 	 * <p/>
 	 * An example of use is to pass DateFormat info to the DateType.
 	 * 
 	 * @author andruid
 	 * @author toupsz
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_format
 	{
 		String[] value();
 	}
 
 	/**
 	 * Metalanguage declaration that tells ecologylab.serialization translators that each Field it is
 	 * applied to as an annotation is represented in XML by a (non-leaf) nested child element. The
 	 * field must be a subclass of ElementState.
 	 * <p/>
 	 * The nested child element name will be derived from the field name, using camel case conversion,
 	 * unless @xml_tag is used.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_composite
 	{
 		String value() default NULL_TAG;
 	}
 
 	static final String	NULL_TAG	= "";
 
 	/**
 	 * Metalanguage declaration that tells ecologylab.serialization translators that each Field it is
 	 * applied to as an annotation is of type Collection. An argument may be passed to declare the tag
 	 * name of the child elements. The XML may define any number of child elements with this tag. In
 	 * this case, the class of the elements will be dervied from the instantiated generic type
 	 * declaration of the children. For example,
 	 * <code>@xml_collection("item")    ArrayList&lt;Item&gt;	items;</code>
 	 * <p/>
 	 * For that formulation, the type of the children may be a subclass of ElementState, for full
 	 * nested elements, or it may be a ScalarType, for leaf nodes.
 	 * <p/>
 	 * Without the tag name declaration, the tag name will be derived from the class name of the
 	 * children, and in translate from XML, the class name will be derived from the tag name, and then
 	 * resolved in the TranslationSpace.
 	 * <p/>
 	 * Alternatively, to achieve polymorphism, for children subclassed from ElementState only, this
 	 * declaration can be combined with @xml_classes. In such cases, items of the various classes will
 	 * be collected together in the declared Collection. Then, the tag names for these elements will
 	 * be derived from their class declarations.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_collection
 	{
 		String value() default NULL_TAG;
 	}
 
 	/**
 	 * Metalanguage declaration that tells ecologylab.serialization translators that each Field it is
 	 * applied to as an annotation is of type Map. An argument may be passed to declare the tag name
 	 * of the child elements. The XML may define any number of child elements with this tag. In this
 	 * case, the class of the elements will be dervied from the instantiated generic type declaration
 	 * of the children.
 	 * <p/>
 	 * For example, <code>@xml_map("foo")    HashMap&lt;String, FooFoo&gt;	items;</code><br/>
 	 * The values of the Map must implement the Mappable interface, to supply a key which matches the
 	 * key declaration in the Map's instantiated generic types.
 	 * <p/>
 	 * Without the tag name declaration, the tag name will be derived from the class name of the
 	 * children, and in translate from XML, the class name will be derived from the tag name, and then
 	 * resolved in the TranslationSpace.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_map
 	{
 		String value() default NULL_TAG;
 	}
 
 	/**
 	 * Metalanguage declaration that can be applied either to field or to class declarations.
 	 * 
 	 * Annotation that tells ecologylab.serialization translators that instead of generating a name
 	 * for XML elements corresponding to the field or class using camel case conversion, one is
 	 * specified explicitly. This name is specified by the value of this annotation.
 	 * <p/>
 	 * Note that programmers should be careful when specifying an xml_tag, to ensure that there are no
 	 * collisions with other names. Note that when an xml_tag is specified for a field or class, it
 	 * will ALWAYS EMIT AND TRANSLATE FROM USING THAT NAME.
 	 * 
 	 * xml_tag's should typically be something that cannot be represented using camel case name
 	 * conversion, such as utilizing characters that are not normally allowed in field names, but that
 	 * are allowed in XML names. This can be particularly useful for building ElementState objects out
 	 * of XML from the wild.
 	 * <p/>
 	 * You cannot use XML-forbidden characters or constructs in an xml_tag!
 	 * 
 	 * When using @xml_tag, you MUST create your corresponding TranslationSpace entry using a Class
 	 * object, instead of using a default package name.
 	 * 
 	 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface xml_tag
 	{
 		String value();
 	}
 
 	/**
 	 * annotation used for serializing in bibtex format. The tag value is the name of the key in
 	 * key-value pairs of a bibtex entry
 	 * 
 	 * @author nabeel
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface bibtex_tag
 	{
 		String value();
 	}
 
 	/**
 	 * annotation to define the type of a bibtex entry.
 	 * 
 	 * @author nabeel
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface bibtex_type
 	{
 		String value();
 	}
 
 	/**
 	 * annotation to define the type of a bibtex key.
 	 * 
 	 * @author nabeel
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface bibtex_key
 	{
 
 	}
 
 	/**
 	 * annotation to define the type of a bibtex key.
 	 * 
 	 * @author nabeel
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface simpl_composite_as_scalar
 	{
 
 	}
 
 	/**
 	 * annotation to define the type of a bibtex key.
 	 * 
 	 * @author nabeel
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface simpl_composite_as_scalar_value
 	{
 
 	}
 
 	/**
 	 * This optional metalanguage declaration is used to add extra tags to a field or class, in order
 	 * to enable backwards compatability with a previous dialect of XML. It affects only translate
 	 * from XML; translateToXML() never uses these entries.
 	 * 
 	 * @author andruid
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface xml_other_tags
 	{
 		String[] value();
 	}
 
 	/**
 	 * Supplementary metalanguage declaration that can be applied only to a field. The argument is the
 	 * name of a TranslationScope.
 	 * <p/>
 	 * Annotation uses the argument to lookup a TranslationScope. If there is none, a warning is
 	 * provided. Otherwise, mappings are created for tag names associated with each class in the
 	 * TranslationScope. It then creates a mapping from the tag and class names to the field it is
 	 * applied to, so that translateFromXML(...) will set a value based on an element with the tags,
 	 * if field is also declared with @xml_nested, or collect values when elements have the tags, if
 	 * the field is declared with @xml_collection.
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Inherited
 	public @interface simpl_scope
 	{
 		String value();
 	}
 
 	/**
 	 * Supplementary metalanguage declaration that can be applied only to a field. The argument is an
 	 * array of Class objects.
 	 * <p/>
 	 * Annotation forms tag names from each of the class names, using camel case conversion. It then
 	 * creates a mapping from the tag and class names to the field it is applied to, so that
 	 * translateFromXML(...) will set a value based on an element with the tags, if field is also
 	 * declared with @xml_nested, or collect values when elements have the tags, if the field is
 	 * declared with @xml_collection.
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	public @interface simpl_classes
 	{
 		Class<? extends ElementState>[] value();
 	}
 
 	/**
 	 * Used to specify that the elements of a collection or map should not be wrapped by an outer tag
 	 * corresponding to their field name.
 	 * 
 	 * @author andruid
 	 * 
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Inherited
 	public @interface simpl_nowrap
 	{
 	}
 
 	/**
 	 * Used to specify that the elements of a collection or map should be wrapped by an outer tag
 	 * corresponding to their field name.
 	 * 
 	 * @author andruid
 	 * 
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Inherited
 	public @interface simpl_wrap
 	{
 	}
 
 	/**
 	 * Source of bindings that will be mapped to an xml_bind_from declaration inside an inheriting
 	 * object or one referenced through a field.
 	 * 
 	 * @author andruid
 	 * 
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Inherited
 	public @interface simpl_bind_to
 	{
 		/**
 		 * @return This is the name of this binding site. It must match the name of the bind_from site.
 		 *         Common static final constants can be used across @xml_bind_to and @xml_bind_from,
 		 *         ensuring robust consistency through re-factoring.
 		 */
 		String name();
 
 		String scopeName();
 
 		Class<? extends ElementState>[] classes();
 	}
 
 	@Retention(RetentionPolicy.RUNTIME)
 	@Inherited
 	public @interface simpl_bind_from
 	{
 		/**
 		 * @return This is the name of this binding site. It must match the name of the bind_to site.
 		 *         Common static final constants can be used across @xml_bind_to and @xml_bind_from,
 		 *         ensuring robust consistency through re-factoring.
 		 */
 		String value();
 	}
 
 	public enum DbHint
 	{
 		PRIMARY_KEY, NOT_NULL, NULL, UNIQUE
 	}
 
 	/**
 	 * Supplementary metalanguage declaration that can be applied only to a field. This is used for
 	 * assigning database constraints to a field, which are referenced in creating correponding sql
 	 * table schema. Database constraints are defined in 'DbHint'
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	@Inherited
 	public @interface simpl_db
 	{
 		/**
 		 * @return database constraints defined in 'DbHint' and name of reference table
 		 */
 		DbHint[] value();
 
 		String references() default "null";
 	}
 
 	/**
 	 * @return Returns the optimizations.
 	 */
 
 	public ClassDescriptor classDescriptor()
 	{
 		ClassDescriptor result = classDescriptor;
 		if (result == null)
 		{
 			result = ClassDescriptor.getClassDescriptor(this);
 			this.classDescriptor = result;
 		}
 		return result;
 	}
 
 	/**
 	 * Perform custom processing on the newly created child node, just before it is added to this.
 	 * <p/>
 	 * This is part of depth-first traversal during translateFromXML().
 	 * <p/>
 	 * This, the default implementation, does nothing. Sub-classes may wish to override.
 	 * 
 	 * @param foo
 	 */
 	protected void createChildHook(ElementState foo)
 	{
 
 	}
 
 	/**
 	 * Perform custom processing immediately before translating this to XML.
 	 * <p/>
 	 * This, the default implementation, does nothing. Sub-classes may wish to override.
 	 * 
 	 */
 	protected void serializationPreHook()
 	{
 
 	}
 
 	/**
 	 * Perform processing immediately after serializing this.
 	 * <p/>
 	 * The default implementation does nothing. Sub-classes may wish to override.
 	 */
 	protected void serializationPostHook()
 	{
 
 	}
 
 	/**
 	 * Perform custom processing immediately after all translation from XML is completed. This allows
 	 * a newly-created ElementState object to perform any post processing with all the data it will
 	 * have from XML.
 	 * <p/>
 	 * This method is called by NodeToJavaOptimizations.createChildElement() or translateToXML
 	 * depending on whether the element in question is a child or the top-level parent.
 	 * <p/>
 	 * This, the default implementation, does nothing. Sub-classes may wish to override. dd
 	 */
 	protected void deserializationPostHook()
 	{
 
 	}
 
 	protected void deserializationPreHook()
 	{
 	}
 
 	/**
 	 * Clear data structures and references to enable garbage collecting of resources associated with
 	 * this.
 	 */
 	public void recycle()
 	{
 		if (parent == null)
 		{ // root state!
 			if (elementByIdMap != null)
 			{
 				elementByIdMap.clear();
 				elementByIdMap = null;
 			}
 		}
 		else
 			parent = null;
 
 		elementByIdMap = null;
 		classDescriptor = null;
 		if (nestedNameSpaces != null)
 		{
 			for (ElementState nns : nestedNameSpaces.values())
 			{
 				if (nns != null)
 					nns.recycle();
 			}
 			nestedNameSpaces.clear();
 			nestedNameSpaces = null;
 		}
 	}
 
 	/**
 	 * Set-up referential chains for a newly born child of this.
 	 * 
 	 * @param newParent
 	 * @param ourClassDescriptor
 	 *          TODO
 	 */
 	void setupInParent(ElementState newParent, ClassDescriptor ourClassDescriptor)
 	{
 		this.elementByIdMap = newParent.elementByIdMap;
 		this.manageParents(newParent);
 		this.classDescriptor = ourClassDescriptor;
 	}
 
 	private void manageParents(ElementState newParent)
 	{
 		if (this.parent == null)
 		{
 			this.parent = newParent;
 		}
 		else
 		{
 			if (this.parents == null)
 			{
 				this.parents = new Stack<ElementState>();
 				this.parents.push(this.parent);
 				this.parents.push(newParent);
 			}
 			else
 			{
 				this.parents.push(newParent);
 			}
 		}
 	}
 
 	/**
 	 * Either lookup an existing Nested Namespace object, or form a new one, map it, and return it.
 	 * This lazy evaluation type call is invoked either in translateFromXML(), or, when procedurally
 	 * building an element with Namespace children.
 	 * 
 	 * @param id
 	 * @param esClass
 	 * @return Namespace ElementState object associated with urn.
 	 */
 	public ElementState getNestedNameSpace(String id)
 	{
 		/*
 		 * ElementState result = (nestedNameSpaces == null) ? null : nestedNameSpaces.get(id); if
 		 * (result == null) { Class<? extends ElementState> esClass =
 		 * classDescriptor.lookupNameSpaceClassById(id); if (esClass != null) { try { result =
 		 * XMLTools.getInstance(esClass); this.setupChildElementState(result);
 		 * result.classDescriptor.setNameSpaceID(id); // result.parent = this; nestNameSpace(id,
 		 * result); // debug("WOW! Created nested Namespace xmlns:"+id+'\n'); } catch
 		 * (XMLTranslationException e) { e.printStackTrace(); } } } return result;
 		 */
 		return null;
 	}
 
 	/**
 	 * Add a NestedNameSpace object to this.
 	 * 
 	 * @param urn
 	 * @param nns
 	 */
 	private void nestNameSpace(String urn, ElementState nns)
 	{
 		if (nestedNameSpaces == null)
 			nestedNameSpaces = new HashMap<String, ElementState>(2);
 
 		nestedNameSpaces.put(urn, nns);
 	}
 
 	/**
 	 * Lookup an ElementState subclass representing the scope of the nested XML Namespace in this.
 	 * 
 	 * @param id
 	 * @return The ElementState subclass associated with xmlns:id, if there is one. Otherwise, null.
 	 */
 	public ElementState lookupNestedNameSpace(String id)
 	{
 		return (nestedNameSpaces == null) ? null : nestedNameSpaces.get(id);
 	}
 
 	/*
 	 * Cyclic graph related functions
 	 */
 
 	private void serializeCompositeElements(Appendable appendable, ElementState nestedES,
 			FieldDescriptor nestedF2XO, TranslationContext graphContext) throws IOException,
 			SIMPLTranslationException
 	{
 		if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON && graphContext.alreadyMarshalled(nestedES))
 		{
 			graphContext.appendSimplRefId(appendable, nestedES, nestedF2XO, FORMAT.XML, false);
 		}
 		else
 		{
 			nestedES.serializeToAppendable(nestedF2XO, appendable, graphContext);
 		}
 	}
 
 	private void serializeCompositeJSONElements(PrintStream appendable, ElementState nestedES,
 			FieldDescriptor nestedF2XO, TranslationContext graphContext, boolean withTag)
 			throws IOException, SIMPLTranslationException
 	{
 		if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON && graphContext.alreadyMarshalled(nestedES))
 		{
 			graphContext.appendSimplRefId(appendable, nestedES, nestedF2XO, FORMAT.JSON, withTag);
 		}
 		else
 		{
 			nestedES.serializeToJSONRecursive(nestedF2XO, appendable, withTag, graphContext);
 		}
 	}
 
 	private void serializeCompositeTLVElements(DataOutputStream appendable, ElementState nestedES,
 			FieldDescriptor nestedF2XO) throws IOException, SIMPLTranslationException
 	{
 		// if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON && alreadyMarshalled(nestedES))
 		// {
 		// appendSimplRefId(appendable, nestedES, nestedF2XO);
 		// }
 		// else
 		{
 
 		}
 	}
 
 	// public static void recycleSerializationMappings()
 	// {
 	// if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON)
 	// {
 	// marshalledObjects.clear();
 	// visitedElements.clear();
 	// needsAttributeHashCode.clear();
 	// }
 	// }
 	//
 	// public static void recycleDeserializationMappings()
 	// {
 	// if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON)
 	// {
 	// marshalledObjects.clear();
 	// visitedElements.clear();
 	// needsAttributeHashCode.clear();
 	// unmarshalledObjects.clear();
 	// }
 	// }
 
 	/**
 	 * method returns whether a strict pbject graph is required
 	 * 
 	 * @return
 	 */
 	public boolean getStrictObjectGraphRequired()
 	{
 		return classDescriptor().getStrictObjectGraphRequired();
 	}
 }
