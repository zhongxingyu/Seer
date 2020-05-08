 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Map;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.codehaus.jackson.JsonParseException;
 
 import ecologylab.generic.StringInputStream;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * Pull API implementation to transform XML documets to corresponding object models. Utilizes
  * XMLStreamReader to get sequential access to tags in XML.
  * 
  * @author nabeel
  */
 public class XMLPullDeserializer extends StringPullDeserializer
 {
 
 	private CharSequence	test;
 
 	XMLStreamReader				xmlStreamReader	= null;
 
 	/**
 	 * 
 	 * @param translationScope
 	 * @param translationContext
 	 * @param deserializationHookStrategy
 	 */
 	public XMLPullDeserializer(
 			TranslationScope translationScope,
 			TranslationContext translationContext,
 			DeserializationHookStrategy<? extends Object, ? extends FieldDescriptor> deserializationHookStrategy)
 	{
 		super(translationScope, translationContext, deserializationHookStrategy);
 	}
 
 	/**
 	 * 
 	 * @param translationScope
 	 * @param translationContext
 	 */
 	public XMLPullDeserializer(TranslationScope translationScope,
 			TranslationContext translationContext)
 	{
 		super(translationScope, translationContext);
 	}
 
 	@Override
 	public Object parse(InputStream inputStream) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	/**
 	 * Parses a charsequence of the XML document and returns the corresponding object model.
 	 * 
 	 * @param charSequence
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	public Object parse(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(charSequence);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	/**
 	 * 
 	 * @param inputStream
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 */
 	private void configure(InputStream inputStream) throws XMLStreamException,
 			FactoryConfigurationError
 	{
 		xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
 	}
 
 	/**
 	 * 
 	 * @param charSequence
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 */
 	private void configure(CharSequence charSequence) throws XMLStreamException,
 			FactoryConfigurationError
 	{
 		test = charSequence;
 		InputStream xmlStream = new StringInputStream(charSequence, StringInputStream.UTF8);
 		xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(xmlStream, "UTF-8");
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws XMLStreamException
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	private Object parse() throws XMLStreamException, SIMPLTranslationException, IOException
 	{
 		Object root = null;
 
 		nextEvent();
 
 		if (xmlStreamReader.getEventType() != XMLStreamConstants.START_ELEMENT)
 		{
 			throw new SIMPLTranslationException("start of an element expected");
 		}
 
 		String rootTag = getTagName();
 
 		ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor = translationScope
 				.getClassDescriptorByTag(rootTag);
 
 		if (rootClassDescriptor == null)
 		{
 			throw new SIMPLTranslationException("cannot find the class descriptor for root element <"
 					+ rootTag + ">; make sure if translation scope is correct.");
 		}
 
 		root = rootClassDescriptor.getInstance();
 
 		deserializationPreHook(root, translationContext);
 		if (deserializationHookStrategy != null)
 			deserializationHookStrategy.deserializationPreHook(root, null);
 
 		deserializeAttributes(root, rootClassDescriptor);
 
 		createObjectModel(root, rootClassDescriptor, rootTag);
 
 		return root;
 	}
 
 	/**
 	 * Recursive method that moves forward in the CharSequence through JsonParser to create a
 	 * corresponding object model
 	 * 
 	 * @param root
 	 *          instance of the root element created by the calling method
 	 * @param rootClassDescriptor
 	 *          instance of the classdescriptor of the root element created by the calling method
 	 * @param tagName
 	 *          TODO
 	 * @throws JsonParseException
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 * @throws XMLStreamException
 	 */
 	private void createObjectModel(Object root,
 			ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor, String rootTag)
 			throws IOException, SIMPLTranslationException, XMLStreamException
 	{
 
 		try
 		{
 
 			int event = 0;
 			event = nextEvent();
 
 			FieldDescriptor currentFieldDescriptor = new FieldDescriptor();
 
 			String xmlText = "";
 
 			while (xmlStreamReader.hasNext()
 					&& (event != XMLStreamConstants.END_ELEMENT || !rootTag.equals(getTagName())))
 			{
 				if (event != XMLStreamConstants.START_ELEMENT)
 				{
 					if (event == XMLStreamConstants.CHARACTERS)
 						xmlText += xmlStreamReader.getText();
 					event = nextEvent();
 					continue;
 				}
 
 				String tag = getTagName();
 
 				currentFieldDescriptor = currentFieldDescriptor.getType() == WRAPPER ? currentFieldDescriptor
 						.getWrappedFD()
 						: rootClassDescriptor.getFieldDescriptorByTag(tag, translationScope, null);
 
 				if (currentFieldDescriptor == null)
 				{
 					currentFieldDescriptor = FieldDescriptor.makeIgnoredFieldDescriptor(tag);
 				}
 
 				int fieldType = currentFieldDescriptor.getType();
 
 				switch (fieldType)
 				{
 				case SCALAR:
 					event = deserializeScalar(root, currentFieldDescriptor);
 					break;
 				case COLLECTION_SCALAR:
 					event = deserializeScalarCollection(root, currentFieldDescriptor);
 					break;
 				case COMPOSITE_ELEMENT:
 					event = deserializeComposite(root, currentFieldDescriptor);
 					break;
 				case COLLECTION_ELEMENT:
 					event = deserializeCompositeCollection(root, currentFieldDescriptor);
 					break;
 				case MAP_ELEMENT:
 					event = deserializeCompositeMap(root, currentFieldDescriptor);
 					break;
 				case WRAPPER:
 					event = nextEvent();
 					break;
 				case IGNORED_ELEMENT:
 					event = ignoreTag(tag);
 					break;
 				default:
 					event = nextEvent();
 				}
 
 				if (event == XMLStreamConstants.END_DOCUMENT || !xmlStreamReader.hasNext())
 				{
 					// no more data? but we are expecting so its not correct
 					throw new SIMPLTranslationException(
 							"premature end of file: check XML file for consistency");
 				}
 			}
 
 			if (rootClassDescriptor.hasScalarFD())
 			{
 				rootClassDescriptor.getScalarTextFD().setFieldToScalar(root, xmlText, translationContext);
 			}
 			deserializationPostHook(root, translationContext);
 			if (deserializationHookStrategy != null)
 				deserializationHookStrategy.deserializationPostHook(root, null);
 		}
 
 		catch (Exception ex)
 		{
 			printParse();
 			System.out.println(ex);
 		}
 	}
 
 	private int deserializeScalarCollection(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, XMLStreamException
 	{
 		int event = xmlStreamReader.getEventType();
 
 		while (fd.isCollectionTag(getTagName()))
 		{
 			String tag = getTagName();
 			if (event != XMLStreamConstants.START_ELEMENT)
 			{
 				// end of collection
 				break;
 			}
 
 			event = xmlStreamReader.next();
 
 			if (event == XMLStreamConstants.CHARACTERS && event != XMLStreamConstants.END_ELEMENT)
 			{
 				StringBuilder text = new StringBuilder();
 				text.append(xmlStreamReader.getText());
 				while (xmlStreamReader.next() != XMLStreamConstants.END_ELEMENT)
 				{
 					if (xmlStreamReader.getEventType() == XMLStreamConstants.CHARACTERS)
 						text.append(xmlStreamReader.getText());
 				}
 
 				String value = text.toString();
 				fd.addLeafNodeToCollection(root, value, translationContext);
 			}
 
 			event = xmlStreamReader.nextTag();
 		}
 
 		return event;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param currentFieldDescriptor
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 * @throws XMLStreamException
 	 */
 	private int deserializeComposite(Object root, FieldDescriptor currentFieldDescriptor)
 			throws SIMPLTranslationException, IOException, XMLStreamException
 	{
 
 		String tagName = getTagName();
 		Object subRoot = getSubRoot(currentFieldDescriptor, tagName, root);
 		currentFieldDescriptor.setFieldToComposite(root, subRoot);
 
 		return nextEvent();
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param fd
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 * @throws XMLStreamException
 	 */
 	private int deserializeCompositeMap(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, IOException, XMLStreamException
 	{
 		Object subRoot;
 		int event = xmlStreamReader.getEventType();
 
 		while (fd.isCollectionTag(getTagName()))
 		{
 			if (event != XMLStreamConstants.START_ELEMENT)
 			{
 				// end of collection
 				break;
 			}
 
 			String compositeTagName = getTagName();
 			subRoot = getSubRoot(fd, compositeTagName, root);
 			if (subRoot instanceof IMappable<?>)
 			{
 				final Object key = ((IMappable<?>) subRoot).key();
 				Map map = (Map) fd.automaticLazyGetCollectionOrMap(root);
 				map.put(key, subRoot);
 			}
 
 			event = xmlStreamReader.nextTag();
 
 		}
 		return event;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param fd
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 * @throws XMLStreamException
 	 */
 	private int deserializeCompositeCollection(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException, IOException, XMLStreamException
 	{
 		Object subRoot;
 		int event = xmlStreamReader.getEventType();
 		while (fd.isCollectionTag(getTagName()))
 		{
 			if (event != XMLStreamConstants.START_ELEMENT)
 			{
 				// end of collection
 				break;
 			}
 
 			String compositeTagName = getTagName();
 			subRoot = getSubRoot(fd, compositeTagName, root);
 			Collection collection = (Collection) fd.automaticLazyGetCollectionOrMap(root);
 			collection.add(subRoot);
 
 			event = xmlStreamReader.nextTag();
 		}
 
 		return event;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param currentFieldDescriptor
 	 * @return
 	 * @throws XMLStreamException
 	 */
 	private int deserializeScalar(Object root, FieldDescriptor currentFieldDescriptor)
 			throws XMLStreamException
 	{
 		nextEvent();
 
 		StringBuilder text = new StringBuilder();
 		text.append(xmlStreamReader.getText());
 
 		while (nextEvent() != XMLStreamConstants.END_ELEMENT)
 		{
 			if (xmlStreamReader.getEventType() == XMLStreamConstants.CHARACTERS)
 				text.append(xmlStreamReader.getText());
 		}
 
 		String value = text.toString();
 		currentFieldDescriptor.setFieldToScalar(root, value, translationContext);
 
 		return nextEvent();
 
 	}
 
 	/**
 	 * 
 	 * @param tag
 	 * @return
 	 * @throws XMLStreamException
 	 */
 	private int ignoreTag(String tag) throws XMLStreamException
 	{
 		int event = -1;
 		println("ignoring tag: " + tag);
 
 		while (event != XMLStreamConstants.END_ELEMENT || !getTagName().equals(tag))
 			event = nextEvent();
 
 		return nextEvent();
 	}
 
 	/**
 	 * Gets the sub root of the object model if its a composite object. Does graph handling/ Handles
 	 * simpl:ref tag to assign an already created instance of the composite object instead of creating
 	 * a new one
 	 * 
 	 * @param currentFieldDescriptor
 	 * @param root
 	 *          TODO
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws JsonParseException
 	 * @throws IOException
 	 * @throws XMLStreamException
 	 */
 	private Object getSubRoot(FieldDescriptor currentFieldDescriptor, String tagName, Object root)
 			throws SIMPLTranslationException, IOException, XMLStreamException
 	{
 		Object subRoot = null;
 		ClassDescriptor<? extends FieldDescriptor> subRootClassDescriptor = currentFieldDescriptor
 				.getChildClassDescriptor(tagName);
 
 		String simplReference = null;
 
 		if ((simplReference = getSimpleReference()) != null)
 		{
 			subRoot = translationContext.getFromMap(simplReference);
 			xmlStreamReader.next();
 		}
 		else
 		{
 			subRoot = subRootClassDescriptor.getInstance();
 
 			deserializationPreHook(subRoot, translationContext);
 			if (deserializationHookStrategy != null)
 				deserializationHookStrategy.deserializationPreHook(subRoot, currentFieldDescriptor);
 
 			if (subRoot != null)
 			{
 				if (subRoot instanceof ElementState && root instanceof ElementState)
 				{
 					((ElementState) subRoot).setupInParent((ElementState) root);
 				}
 			}
 
 			deserializeAttributes(subRoot, subRootClassDescriptor);
 			createObjectModel(subRoot, subRootClassDescriptor, tagName);
 		}
 
 		return subRoot;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private String getSimpleReference()
 	{
 		String simplReference = null;
 
 		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlStreamReader.getAttributePrefix(i);
 			String tag = xmlStreamReader.getAttributeLocalName(i);
 			String value = xmlStreamReader.getAttributeValue(i);
 
 			if (TranslationContext.SIMPL.equals(attributePrefix))
 			{
 				if (tag.equals(TranslationContext.REF))
 				{
 					simplReference = value;
 				}
 			}
 		}
 
 		return simplReference;
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param rootClassDescriptor
 	 * @return
 	 */
 	private boolean deserializeAttributes(Object root,
 			ClassDescriptor<? extends FieldDescriptor> rootClassDescriptor)
 	{
 		for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlStreamReader.getAttributePrefix(i);
 			String tag = xmlStreamReader.getAttributeLocalName(i);
 			String value = xmlStreamReader.getAttributeValue(i);
 
 			if (TranslationContext.SIMPL.equals(attributePrefix))
 			{
 				if (tag.equals(TranslationContext.ID))
 				{
 					translationContext.markAsUnmarshalled(value, root);
 				}
 			}
 			else
 			{
 				FieldDescriptor attributeFieldDescriptor = rootClassDescriptor.getFieldDescriptorByTag(tag,
 						translationScope);
 
 				if (attributeFieldDescriptor != null)
 				{
 					attributeFieldDescriptor.setFieldToScalar(root, value, translationContext);
 				}
 				else
 				{
 					debug("ignoring attribute: " + tag);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws XMLStreamException
 	 */
 	private int nextEvent() throws XMLStreamException
 	{
 		int eventType = 0;
 
 		// skip events that we don't handle.
 		while (xmlStreamReader.hasNext())
 		{
 			eventType = xmlStreamReader.next();
 			if (xmlStreamReader.getEventType() == XMLStreamConstants.START_DOCUMENT
 					|| xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT
 					|| xmlStreamReader.getEventType() == XMLStreamConstants.END_ELEMENT
 					|| xmlStreamReader.getEventType() == XMLStreamConstants.END_DOCUMENT
 					|| xmlStreamReader.getEventType() == XMLStreamConstants.CHARACTERS)
 			{
 				break;
 			}
 		}
 
 		return eventType;
 	}
 
 	protected void debug()
 	{
 		int event = xmlStreamReader.getEventType();
 		switch (event)
 		{
 		case XMLStreamConstants.START_ELEMENT:
 			System.out.println(getTagName());
 			break;
 		case XMLStreamConstants.END_ELEMENT:
 			System.out.println(getTagName());
 			break;
 		case XMLStreamConstants.CHARACTERS:
 			System.out.println(xmlStreamReader.getText());
 			break;
 		case XMLStreamConstants.CDATA:
 			System.out.println("cdata " + xmlStreamReader.getText());
 			break;
 		} // end switch
 	}
 
 	private String getTagName()
 	{
		if (!(xmlStreamReader.getPrefix().length() == 0))
			return xmlStreamReader.getPrefix() + ":" + xmlStreamReader.getLocalName();
 		else
 			return xmlStreamReader.getLocalName();
 	}
 
 	/**
 	 * 
 	 * @throws XMLStreamException
 	 */
 	protected void printParse() throws XMLStreamException
 	{
 		int event;
 		while (xmlStreamReader.hasNext())
 		{
 			event = xmlStreamReader.getEventType();
 			switch (event)
 			{
 			case XMLStreamConstants.START_ELEMENT:
 				System.out.print("start element: ");
 				System.out.print(xmlStreamReader.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlStreamReader.getName().toString());
 				System.out.println();
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				System.out.print("end element: ");
 				System.out.print(xmlStreamReader.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlStreamReader.getName().toString());
 				System.out.println();
 				break;
 			case XMLStreamConstants.CHARACTERS:
 				System.out.print("characters: ");
 				System.out.print(xmlStreamReader.getEventType());
 				System.out.print(" : ");
 				System.out.print(xmlStreamReader.getText());
 				System.out.println();
 				break;
 			case XMLStreamConstants.CDATA:
 				System.out.println("cdata " + xmlStreamReader.getText());
 				break;
 			default:
 				System.out.println(xmlStreamReader.getEventType());
 			} // end switch
 			xmlStreamReader.next();
 		} // end while
 	}
 }
