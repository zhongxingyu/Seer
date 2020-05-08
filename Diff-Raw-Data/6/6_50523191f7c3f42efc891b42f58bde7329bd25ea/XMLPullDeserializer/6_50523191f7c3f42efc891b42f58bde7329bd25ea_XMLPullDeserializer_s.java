 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.Map;
 
 import ecologylab.platformspecifics.FundamentalPlatformSpecifics;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * Pull API implementation to transform XML documents to corresponding object models. Utilizes
  * XMLStreamReader to get sequential access to tags in XML.
  * 
  * @author nabeel
  */
 public class XMLPullDeserializer extends StringPullDeserializer
 {
 
 	// private CharSequence test;
 
 	XMLParser xmlParser;
 	
 	/**
 	 * 
 	 * @param translationScope
 	 * @param translationContext
 	 * @param deserializationHookStrategy
 	 */
 	public XMLPullDeserializer(
 			SimplTypesScope translationScope,
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
 	public XMLPullDeserializer(SimplTypesScope translationScope, TranslationContext translationContext)
 	{
 		super(translationScope, translationContext);
 	}
 
 	@Override
 	public Object parse(InputStream inputStream, Charset charSet) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream, charSet);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
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
 	@Override
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
 	 * Configures the input stream. Creates an instance of XMLStreamReader on the input stream.
 	 * 
 	 * @param inputStream
 	 * @param charSet
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 */
 	private void configure(InputStream inputStream, Charset charSet) throws SIMPLTranslationException
 	{
 		xmlParser = FundamentalPlatformSpecifics.get().getXMLParser(inputStream, charSet);
 	}
 	
 	/**
 	 * 
 	 * @param inputStream
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 */
 	private void configure(InputStream inputStream) throws SIMPLTranslationException
 	{
 		xmlParser = FundamentalPlatformSpecifics.get().getXMLParser(inputStream);
 	}
 
 	/**
 	 * 
 	 * @param charSequence
 	 * @throws XMLStreamException
 	 * @throws FactoryConfigurationError
 	 */
 	private void configure(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		xmlParser = FundamentalPlatformSpecifics.get().getXMLParser(charSequence);
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws XMLStreamException
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	private Object parse() throws SIMPLTranslationException, IOException
 	{
 		Object root = null;
 
 		nextEvent();
 
 		if (xmlParser.getEventType() != XMLParser.START_ELEMENT)
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
 	 * Recursive method that moves forward in the CharSequence through XMLStreamReader to create a
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
 			throws IOException, SIMPLTranslationException
 	{
 		try
 		{
 			int event = 0;
 			event = nextEvent();
 
 			FieldDescriptor currentFieldDescriptor = null; // new FieldDescriptor();
 
 			String xmlText = "";
 
 			while ( event!= XMLParser.END_DOCUMENT
 					&& (event != XMLParser.END_ELEMENT || !rootTag.equals(getTagName())))
 			{
 				if (event != XMLParser.START_ELEMENT)
 				{
 					if (event == XMLParser.CHARACTERS)
 						xmlText += xmlParser.getText();
 					else if (event == XMLParser.END_ELEMENT && currentFieldDescriptor != null && currentFieldDescriptor.getType() == WRAPPER)
 						currentFieldDescriptor = currentFieldDescriptor.getWrappedFD();
 					event = nextEvent();
 					continue;
 				}
 
 				String tag = getTagName();
 
 				currentFieldDescriptor = currentFieldDescriptor != null &&currentFieldDescriptor.getType() == WRAPPER
 						? currentFieldDescriptor.getWrappedFD()
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
 
 				if (event == XMLParser.END_DOCUMENT)
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
			if (deserializationHookStrategy != null && (currentFieldDescriptor == null || currentFieldDescriptor.getType() != IGNORED_ELEMENT))
				deserializationHookStrategy.deserializationPostHook(root, currentFieldDescriptor);
 //				deserializationHookStrategy.deserializationPostHook(root, null);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			printParse();
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 * 
 	 * @param root
 	 * @param fd
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws XMLStreamException
 	 */
 	private int deserializeScalarCollection(Object root, FieldDescriptor fd)
 			throws SIMPLTranslationException
 	{
 		int event = xmlParser.getEventType();
 
 		String tagName = getTagName();
 		if (!fd.isCollectionTag(tagName))
 		{
 			event = ignoreTag(tagName);
 		}
 		else
 		{
 			while (fd.isCollectionTag(tagName))
 			{
 				if (event != XMLParser.START_ELEMENT)
 				{
 					// end of collection
 					break;
 				}
 
 				event = xmlParser.next();
 
 				if (event == XMLParser.CHARACTERS && event != XMLParser.END_ELEMENT)
 				{
 					StringBuilder text = new StringBuilder();
 					text.append(xmlParser.getText());
 					while (xmlParser.next() != XMLParser.END_ELEMENT)
 					{
 						if (xmlParser.getEventType() == XMLParser.CHARACTERS)
 							text.append(xmlParser.getText());
 					}
 
 					String value = text.toString();
 					fd.addLeafNodeToCollection(root, value, translationContext);
 				}
 
 				event 	= xmlParser.nextTag();
 				tagName = getTagName();
 			}
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
 			throws SIMPLTranslationException, IOException
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
 			throws SIMPLTranslationException, IOException
 	{
 		Object subRoot;
 		int event = xmlParser.getEventType();
 
 		while (fd.isCollectionTag(getTagName()))
 		{
 			if (event != XMLParser.START_ELEMENT)
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
 
 			event = xmlParser.nextTag();
 
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
 			throws SIMPLTranslationException, IOException
 	{
 		Object subRoot;
 		int event = xmlParser.getEventType();
 		String tagName = getTagName();
 		if (!fd.isCollectionTag(tagName))
 		{
 			event = ignoreTag(tagName);
 		}
 		else
 		{
 			while (fd.isCollectionTag(tagName))
 			{
 				if (event != XMLParser.START_ELEMENT)
 				{
 					// end of collection
 					break;
 				}
 
 				subRoot = getSubRoot(fd, tagName, root);
 				Collection collection = (Collection) fd.automaticLazyGetCollectionOrMap(root);
 				collection.add(subRoot);
 
 				event 	= xmlParser.nextTag();
 				tagName = getTagName();
 			}
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
 			throws SIMPLTranslationException
 	{
 		// nextEvent();
 
 		StringBuilder text = new StringBuilder();
 		// text.append(xmlStreamReader.getText());
 
 		do
 		{
 			if (xmlParser.getEventType() == XMLParser.CHARACTERS)
 				text.append(xmlParser.getText());
 		}
 		while (nextEvent() != XMLParser.END_ELEMENT);
 
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
 	private int ignoreTag(String tag) throws SIMPLTranslationException
 	{
 		int event = -1;
 		println("ignoring tag: " + tag);
 
 		while (event != XMLParser.END_ELEMENT || !getTagName().equals(tag))
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
 			throws SIMPLTranslationException, IOException
 	{
 		Object subRoot = null;
 		ClassDescriptor<? extends FieldDescriptor> subRootClassDescriptor = currentFieldDescriptor
 				.getChildClassDescriptor(tagName);
 
 		String simplReference = null;
 
 		if ((simplReference = getSimpleReference()) != null)
 		{
 			subRoot = translationContext.getFromMap(simplReference);
 			xmlParser.next();
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
 		
 		if (deserializationHookStrategy != null && subRoot != null)
 		{
 			Object newSubRoot= deserializationHookStrategy.changeObjectIfNecessary(subRoot, currentFieldDescriptor);
 			if (newSubRoot != null)
 				subRoot = newSubRoot;
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
 
 		for (int i = 0; i < xmlParser.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlParser.getAttributePrefix(i);
 			String tag = xmlParser.getAttributeLocalName(i);
 			String value = xmlParser.getAttributeValue(i);
 
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
 		for (int i = 0; i < xmlParser.getAttributeCount(); i++)
 		{
 			String attributePrefix = xmlParser.getAttributePrefix(i);
 			String tag = xmlParser.getAttributeLocalName(i);
 			String value = xmlParser.getAttributeValue(i);
 
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
 	private int nextEvent() throws SIMPLTranslationException
 	{
 		int eventType = XMLParser.END_DOCUMENT;
 
 		// skip events that we don't handle.
 		while ((eventType = xmlParser.next()) != xmlParser.END_DOCUMENT)
 		{
 			if (xmlParser.getEventType() == XMLParser.START_DOCUMENT
 					|| xmlParser.getEventType() == XMLParser.START_ELEMENT
 					|| xmlParser.getEventType() == XMLParser.END_ELEMENT
 					|| xmlParser.getEventType() == XMLParser.END_DOCUMENT
 					|| xmlParser.getEventType() == XMLParser.CHARACTERS)
 			{
 				break;
 			}
 		}
 
 		return eventType;
 	}
 
 	/**
 	 * @throws SIMPLTranslationException 
 	 * 
 	 */
 	protected void debug()
 	{
 		try
 		{
 			int event = xmlParser.getEventType();
 			switch (event)
 			{
 			case XMLParser.START_ELEMENT:
 				System.out.println(getTagName());
 				break;
 			case XMLParser.END_ELEMENT:
 				System.out.println(getTagName());
 				break;
 			case XMLParser.CHARACTERS:
 				System.out.println(xmlParser.getText());
 				break;
 			case XMLParser.CDATA:
 				System.out.println("cdata " + xmlParser.getText());
 				break;
 			} // end switch
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private String getTagName()
 	{
 		if (xmlParser.getPrefix() != null && xmlParser.getPrefix().length() != 0)
 			return xmlParser.getPrefix() + ":" + xmlParser.getLocalName();
 		else
 			return xmlParser.getLocalName();
 	}
 
 	/**
 	 * 
 	 * @throws XMLStreamException
 	 */
 	protected void printParse() throws SIMPLTranslationException
 	{
 		int event;
 		do
 		{
 			event = xmlParser.getEventType();
 			switch (event)
 			{
 				case XMLParser.START_ELEMENT:
 					System.out.print("start element: ");
 					System.out.print(xmlParser.getEventType());
 					System.out.print(" : ");
 					System.out.print(xmlParser.getName());
 					System.out.println();
 					break;
 				case XMLParser.END_ELEMENT:
 					System.out.print("end element: ");
 					System.out.print(xmlParser.getEventType());
 					System.out.print(" : ");
 					System.out.print(xmlParser.getName());
 					System.out.println();
 					break;
 				case XMLParser.CHARACTERS:
 					System.out.print("characters: ");
 					System.out.print(xmlParser.getEventType());
 					System.out.print(" : ");
 					System.out.print(xmlParser.getText());
 					System.out.println();
 					break;
 				case XMLParser.CDATA:
 					System.out.println("cdata " + xmlParser.getText());
 					break;
 				default:
 					System.out.println(xmlParser.getEventType());
 			} // end switch
 		}
 		while (xmlParser.next() != XMLParser.END_DOCUMENT);
 			
 	}
 }
