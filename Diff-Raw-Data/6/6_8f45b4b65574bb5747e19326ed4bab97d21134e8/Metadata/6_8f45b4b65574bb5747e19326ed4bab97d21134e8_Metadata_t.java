 package monbulk.shared.Services;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import monbulk.shared.util.XmlHelper;
 import arc.mf.client.xml.*;
 
 import com.google.gwt.core.client.GWT;
 
 public class Metadata
 {
 	public enum ElementTypes
 	{
 		// These types are unique classes.
 		Document("document", true, false, false),
 		Enumeration("enumeration", true, false, true),
 
 		// These types are generic.
 		String("string", true, false, true),
 		Integer("integer", true, true, true),
 		Date("date", true, false, true),
 		Float("float", true, true, true),
 		Double("double", true, true, true),
 		Long("long", true, true, true),
 		Boolean("boolean", true, false, false),
 		
 		// Special types that aren't visible or selectable by the user.
 		Keyword("keyword", false, false, false),
 		AssetId("asset-id", false, false, false),
 		Asset("asset", false, false, false),
 		CiteableId("citeable-id", false, false, false),
 		EmailAddress("email-address", false, false, false),
 
 		// Special types that aren't real MF types.
 		Number("number", false, true, true),
 		Attribute("attribute", false, false, false),
 		All("all", false, false, false);
 		
 		private String m_typeName;
 		private boolean m_isVisible;
 		private boolean m_isNumber;
 		private boolean m_useInAttributes;
 
 		ElementTypes(String typeName, boolean isVisible, boolean isNumber, boolean useInAttributes)
 		{
 			m_typeName = typeName;
 			m_isVisible = isVisible;
 			m_isNumber = isNumber;
 			m_useInAttributes = useInAttributes;
 		}
 		
 		/**
 		 * Returns the type name of this ElementType.  This is the name that
 		 * MediaFlux uses to identify types.
 		 * @return
 		 */
 		public String getTypeName()
 		{
 			return m_typeName;
 		}
 		
 		/**
 		 * Returns true if this ElementType is visible to a user.
 		 * @return
 		 */
 		public boolean isVisible()
 		{
 			return m_isVisible;
 		}
 		
 		/**
 		 * Returns true if this ElementType represents a number.
 		 * @return
 		 */
 		public boolean isNumber()
 		{
 			return m_isNumber;
 		}
 		
 		/**
 		 * Returns true if this ElementType can be used in attributes.
 		 * @return
 		 */
 		public boolean isUseInAttributes()
 		{
 			return m_useInAttributes;
 		}
 		
 		/**
 		 * Returns true if this type is the same as otherType, or if this type
 		 * is a number type and the other type is also a number type, and vice
 		 * versa.
 		 * @param otherType
 		 * @return
 		 */
 		public boolean isSame(ElementTypes otherType)
 		{
 			return (this == otherType) || (isNumber() && otherType.isNumber());
 		}
 		
 		/**
 		 * Returns an ElementType given a MediaFlux type name.
 		 * @param typeName
 		 * @return
 		 * @throws Exception
 		 */
 		public static ElementTypes fromTypeName(String typeName) throws Exception
 		{
 			for (ElementTypes t : ElementTypes.values())
 			{
 				if (t.getTypeName().equalsIgnoreCase(typeName))
 				{
 					return t;
 				}
 			}
 			
 			throw new Exception("Unknown metadata type '" + typeName + "'");
 		}
 	}
 	
 	/**
 	 * Creates a new Metadata Element given a MediaFlux xml definition.
 	 * @param e
 	 * @return
 	 * @throws Exception
 	 */
 	public static Metadata.Element createElement(XmlElement e) throws Exception
 	{
 		String typeName = e.value("@type");
 		String name = e.value("@name");
 		String description = e.value("description");
 		Metadata.Element element = createElement(typeName, name, description, false);
 		element.setFromXmlElement(e);
 		return element;
 	}
 	
 	/**
 	 * Creates a new Metadata Element.
 	 * @param typeName
 	 * @param name
 	 * @param description
 	 * @param isAttribute
 	 * @return
 	 * @throws Exception
 	 */
 	public static Metadata.Element createElement(String typeName, String name, String description, boolean isAttribute) throws Exception
 	{
 		ElementTypes type = ElementTypes.fromTypeName(typeName);
 
 		if (type == ElementTypes.Document)
 		{
 			return new DocumentElement(name, description);
 		}
 		else if (type == ElementTypes.Enumeration)
 		{
 			return new EnumerationElement(name, description, isAttribute);
 		}
 		else
 		{
 			return new Element(type, name, description, isAttribute);
 		}
 	}
 	
 	/**
 	 * Base element class for types that don't require anything special.
 	 */
 	public static class Element
 	{
 		protected DocumentElement m_parent = null;
 		protected ElementTypes m_type;
 		protected boolean m_isAttribute = false;
 		protected HashMap<String, String> m_settings = new HashMap<String, String>();
 		protected HashMap<String, String> m_restrictions = new HashMap<String, String>();
 
 		// Attributes on an element are stored as a list of elements,
 		// since they are almost exactly the same.
 		protected ArrayList<Element> m_attributes = new ArrayList<Element>();
 		
 		protected boolean m_modified = false;
 
 		/**
 		 * Copy constructor.  Creates a new element from an existing element.
 		 * @param element
 		 */
 		public Element(Element element)
 		{
 			m_parent = element.m_parent;
 			m_type = element.m_type;
 
 			@SuppressWarnings("unchecked")
 			HashMap<String, String> settings = (HashMap<String, String>)element.m_settings.clone();
 			m_settings = settings;
 			
 			@SuppressWarnings("unchecked")
 			HashMap<String, String> restrictions = (HashMap<String, String>)element.m_restrictions.clone();
 			m_restrictions = restrictions;
 
 			m_isAttribute = element.m_isAttribute;
 			
 			for (Element e : element.m_attributes)
 			{
 				m_attributes.add(e.clone());
 			}
 		}
 
 		public Element(ElementTypes type, String name, String description, boolean isAttribute)
 		{
 			m_settings.put("name", name);
 			m_settings.put("description", description);
 			m_type = type;
 			m_isAttribute = isAttribute;
 		}
 		
 		public Element clone()
 		{
 			return new Element(this);
 		}
 
 		/**
 		 * Returns true if this metadata has been modified.
 		 * @return
 		 */
 		public boolean getIsModified()
 		{
 			boolean modified = m_modified;
 
 			// Check all attributes on this element.
 			for (Element e : m_attributes)
 			{
 				if (e.getIsModified())
 				{
 					return true;
 				}
 			}
 			
 			return modified;
 		}
 		
 		/**
 		 * Clears the modified flag to false.
 		 */
 		public void clearModified()
 		{
 			m_modified = false;
 			
 			// Clear modified flag for all attributes on this element.
 			for (Element e : m_attributes)
 			{
 				e.clearModified();
 			}
 		}
 
 		/**
 		 * Sets the restriction 'name' to 'value'.
 		 * @param name
 		 * @param value
 		 */
 		public void setRestriction(String name, String value)
 		{
 			if (value.length() > 0)
 			{
 				m_restrictions.put(name, value);
 			}
 			else
 			{
 				m_restrictions.remove(name);
 			}
 			
 			m_modified = true;
 		}
 		
 		/**
 		 * Returns the specified restriction or the default value
 		 * if it doesn't exist.
 		 * @param name
 		 * @param defaultValue
 		 * @return
 		 */
 		public String getRestriction(String name, String defaultValue)
 		{
 			String value = m_restrictions.get(name);
 			if (value == null)
 			{
 				value = defaultValue;
 			}
 			
 			return value;
 		}
 
 		/**
 		 * Sets the setting 'name' to 'value'.  Settings end up as xml attributes
 		 * so they store various attributes of an element.  Note that the
 		 * description of an element is also stored as a setting, however, the
 		 * description is actually not saved as an xml attribute but a separate node.
 		 * @param name
 		 * @param value
 		 */
 		public void setSetting(String name, String value)
 		{
 			if (value != null && value.length() > 0)
 			{
 				m_settings.put(name, value);
 			}
 			else
 			{
 				m_settings.remove(name);
 			}
 			
 			m_modified = true;
 		}
 
 		/**
 		 * Returns the specified setting or the default value
 		 * if it doesn't exist.
 		 * @param name
 		 * @param defaultValue
 		 * @return
 		 */
 		public String getSetting(String name, String defaultValue)
 		{
 			String value = m_settings.get(name);
 			if (value == null)
 			{
 				value = defaultValue;
 			}
 
 			return value;
 		}
 
 		public void setFromXmlElement(XmlElement xmlElement)
 		{
 			if (xmlElement.hasAttributes())
 			{
 				List<XmlAttribute> attributes = xmlElement.attributes();
 				for (XmlAttribute a : attributes)
 				{
 					m_settings.put(a.name(), a.value());
 				}
 			}
 
 			if (xmlElement.hasElements())			
 			{
 				for (XmlElement e : xmlElement.elements())
 				{
 					if (e.name().equals("restriction"))
 					{
 						if (e.hasElements())
 						{
 							for (XmlElement r : e.elements())
 							{
 								// HACK: For enumeration types don't add any
 								// value restrictions.  The EnumerationElement
 								// will add them.
 								if (m_type != ElementTypes.Enumeration || !r.name().equals("value"))
 								{
 									m_restrictions.put(r.name(), r.value());
 								}
 							}
 						}
 					}
 					else if (canHaveAttributes() && e.name().equals("attribute"))
 					{
 						// Process mf attributes.
 						try
 						{
 							Element element = createElement(e);
 							element.m_isAttribute = true;
 							m_attributes.add(element);
 						}
 						catch (Exception exception)
 						{
 						}
 					}
 				}
 			}
 		}
 
 		// TODO: Should parent be the owner Element for attributes?
 		public DocumentElement getParent()
 		{
 			return m_parent;
 		}
 		
 		/**
 		 * Sets the parent of this element.  Adds itself to the parent.
 		 * @param parent
 		 */
 		public void setParent(DocumentElement parent)
 		{
 			m_parent = parent;
 			parent.m_children.add(this);
 			m_modified = true;
 		}
 		
 		/**
 		 * Sets the description of this element.
 		 * @param description
 		 */
 		public void setDescription(String description)
 		{
 			setSetting("description", description);
 		}
 		
 		/**
 		 * Returns the description of this element or empty string if none
 		 * has been set.
 		 * @return
 		 */
 		public String getDescription()
 		{
 			return getSetting("description", "");
 		}
 		
 		/**
 		 * Returns the name of this element or empty string if none
 		 * has been set.
 		 * @return
 		 */
 		public String getName()
 		{
 			return getSetting("name", "");
 		}
 
 		/**
 		 * Returns the type of this element.
 		 * @return
 		 */
 		public ElementTypes getType()
 		{
 			return m_type;
 		}
 		
 		/**
 		 * Returns the number of attributes on this element.
 		 * @return
 		 */
 		public int getNumAttributes()
 		{
 			return m_attributes.size();
 		}
 
 		/**
 		 * Returns the specified attribute on this element.
 		 * @param index
 		 * @return
 		 */
 		public Element getAttribute(int index)
 		{
 			return m_attributes.get(index);
 		}
 		
 		/**
 		 * Removes the specified attribute from this element.
 		 * @param index
 		 */
 		public void removeAttribute(int index)
 		{
 			m_attributes.remove(index);
 			m_modified = true;
 		}
 		
 		/**
 		 * Adds a new attribute to this element.
 		 * @param attribute
 		 */
 		public void addAttribute(Element attribute)
 		{
 			m_attributes.add(attribute);
 			m_modified = true;
 		}
 		
 		/**
 		 * Replaces an attribute in this element.
 		 * @param oldAttribute
 		 * @param newAttribute
 		 */
 		public void replaceAttribute(Element oldAttribute, Element newAttribute)
 		{
 			int index = m_attributes.indexOf(oldAttribute);
 			if (index >= 0)
 			{
 				m_attributes.set(index, newAttribute);
 				m_modified = true;
 			}
 		}
 		
 		public boolean canHaveAttributes()
 		{
 			return m_type != ElementTypes.Date && !m_isAttribute;
 		}
 		
 		public boolean getIsAttribute()
 		{
 			return m_isAttribute;
 		}
 		
 		public String toString()
 		{
 			String name = getSetting("name", "");
 			String desc = getSetting("description", "");
 			return m_type.toString() + ", " + name + ", " + desc;
 		}
 
 		/**
 		 * Used by enum element type so it can add its own restriction elements.
 		 * @param w
 		 */
 		protected void addRestrictionsXml(XmlStringWriter w)
 		{
 		}
 
 		/**
 		 * Append an xml description of this element to the XmlStringWriter.
 		 * @param w
 		 */
 		public void addXml(XmlStringWriter w)
 		{
 			// Make a copy of the settings.
 			@SuppressWarnings("unchecked")
 			HashMap<String, String> attributes = (HashMap<String, String>)m_settings.clone();
 			
 			// Remove the "description" entry.  All settings except the
 			// description are saved as attributes on the xml element.
 			String description = attributes.remove("description");
 
 			// Add the type.
 			attributes.put("type", m_type.getTypeName());
 
 			// Add the root element.
 			String root = getIsAttribute() ? "attribute" : "element";
 			w.push(root, XmlHelper.getAttributesArray(attributes));
 			
 			if (m_restrictions != null && m_restrictions.size() > 0)
 			{
 				// Add all restrictions.
 				w.push("restriction", new String[] { "base", m_type.getTypeName() });
 				{
 					Set<Entry<String, String>> r = m_restrictions.entrySet();
 					for (Entry<String, String> e : r) 
 					{
 						String key = e.getKey();
 						String value = e.getValue();
 
 						// Ignore the dummy value.
 						if (!key.equals("dummy") && !value.equals("dummy"))
 						{
 							w.add(key, value);
 						}
 					}
 					addRestrictionsXml(w);
 				}
 				w.pop();
 			}
 			
 			if (description != null && description.length() > 0)
 			{
 				// Add the description.
 				w.add("description", description);
 			}
 			
 			if (m_attributes != null && m_attributes.size() > 0)
 			{
 				// Add any mf attributes.
 				for (Element e : m_attributes)
 				{
 					e.addXml(w);
 					w.pop();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * A document element has a number of child elements.
 	 */
 	public static class DocumentElement extends Element
 	{
 		public enum ReferenceType
 		{
 			Document,
 			Element;
 			
 			public static ReferenceType parse(String value) throws Exception
 			{
 				if (value != null)
 				{
 					for (ReferenceType r : ReferenceType.values())
 					{
 						if (r.toString().equalsIgnoreCase(value))
 						{
 							return r;
 						}
 					}
 				}
 				
 				throw new Exception("Unknown reference type '" + value + "'");
 			}
 		};
 
 		private ArrayList<Element> m_children = new ArrayList<Element>();
 		private boolean m_isReference = false;
 		private ReferenceType m_referenceType;
 		private String m_referenceName;
 		private String m_referenceValue;
 
 		/**
 		 * Copy constructor.  Creates a new DocumentElement from an existing element.
 		 * @param element
 		 */
 		public DocumentElement(DocumentElement element)
 		{
 			super(element);
 			
 			for (Element e : element.m_children)
 			{
 				Element child = e.clone();
 				m_children.add(child);
 				child.m_parent = this;
 			}
 
 			m_isReference = element.m_isReference;
 			m_referenceType = element.m_referenceType;
 			m_referenceName = element.m_referenceName;
 			m_referenceValue = element.m_referenceValue;
 		}
 		
 		public DocumentElement(String name, String description)
 		{
 			super(ElementTypes.Document, name, description, false);
 		}
 
 		/**
 		 * Returns true if this element or any of its children have been modified.
 		 * @return
 		 */
 		public boolean getIsModified()
 		{
 			for (Element e : m_children)
 			{
 				if (e.getIsModified())
 				{
 					return true;
 				}
 			}
 			
 			return super.getIsModified();
 		}
 		
 		/**
 		 * Clears the modified flag for any child elements.
 		 */
 		public void clearModified()
 		{
 			super.clearModified();
 			
 			for (Element e : m_children)
 			{
 				e.clearModified();
 			}
 		}
 		
 		/**
 		 * Replaces an element in this DocumentElement.
 		 * @param oldElement
 		 * @param newElement
 		 */
 		public void replaceChild(Element oldElement, Element newElement)
 		{
 			int index = m_children.indexOf(oldElement);
 			if (index >= 0)
 			{
 				m_children.set(index, newElement);
 				newElement.m_parent = this;
 				m_modified = true;
 			}
 		}
 
 		/**
 		 * Returns the number of children in this DocumentElement.
 		 * @return
 		 */
 		public int getNumChildren()
 		{
 			return m_children.size();
 		}
 		
 		/**
 		 * Returns a specific child of this DocumentElement.
 		 * @param index
 		 * @return
 		 */
 		public Element getChild(int index)
 		{
 			return m_children.get(index);
 		}
 		
 		/**
 		 * Removes a specified child from this DocumentElement.
 		 * @param element
 		 */
 		public void removeChild(Element element)
 		{
 			m_children.remove(element);
 			m_modified = true;
 		}
 		
 		/**
 		 * Returns false.  DocumentElement type cannot have attributes.
 		 */
 		public boolean canHaveAttributes()
 		{
 			return false;
 		}
 
 		public void setFromXmlElement(XmlElement xmlElement)
 		{
 			super.setFromXmlElement(xmlElement);
 			
 			XmlElement reference = xmlElement.element("reference");
 			if (reference != null)
 			{
 				try
 				{
 					m_referenceType = ReferenceType.parse(reference.value("@type"));
 					m_referenceName = reference.value("@name");
 					m_referenceValue = reference.value("value");
 					m_isReference = true;
 				}
 				catch (Exception e)
 				{
 					GWT.log("Bad reference: " + e.toString());
 				}
 			}
 		}
 		
 		public boolean getIsReference()
 		{
 			return m_isReference;
 		}
 		
 		public void setIsReference(boolean isReference)
 		{
 			m_isReference = isReference;
 			m_modified = true;
 		}
 		
 		public String getReferenceName()
 		{
 			return m_referenceName;
 		}
 		
 		public void setReferenceName(String referenceName)
 		{
 			m_referenceName = referenceName;
 			m_modified = true;
 		}
 		
 		public ReferenceType getReferenceType()
 		{
 			return m_referenceType;
 		}
 		
 		public void setReferenceType(ReferenceType referenceType)
 		{
 			m_referenceType = referenceType;
 			m_modified = true;
 		}
 		
 		public String getReferenceValue()
 		{
 			return m_referenceValue;
 		}
 		
 		public void setReferenceValue(String referenceValue)
 		{
 			m_referenceValue = referenceValue;
 			m_modified = true;
 		}
 		
 		public Element clone()
 		{
 			return new DocumentElement(this);
 		}
 		
 		/**
 		 * Append an xml description of this element to the XmlStringWriter.
 		 * @param w
 		 */
 		public void addXml(XmlStringWriter w)
 		{
 			super.addXml(w);
 
 			if (getIsReference())
 			{
 				// Add reference info.
 				String[] attributes = new String[4];
 				int index = 0;
 				attributes[index++] = "name";
 				attributes[index++] = getReferenceName();
 				attributes[index++] = "type";
 				attributes[index++] = getReferenceType().toString().toLowerCase();
				w.push("reference", attributes);
				{
					w.add("value", getReferenceValue());
				}
				w.pop();
 			}
 			
 			if (m_children != null && m_children.size() > 0)
 			{
 				// Add children.
 				for (Element e : m_children)
 				{
 					e.addXml(w);
 					w.pop();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * An enumeration element is an element that can be one of a set of
 	 * values.  The values can come from a dictionary or from a specific
 	 * list of values stored in the element. 
 	 */
 	public static class EnumerationElement extends Element
 	{
 		private ArrayList<String> m_values = new ArrayList<String>();
 		
 		/**
 		 * Copy constructor.  Creates a new EnumerationElement from an existing element.
 		 * @param element
 		 */
 		public EnumerationElement(EnumerationElement element)
 		{
 			super(element);
 			
 			@SuppressWarnings("unchecked")
 			ArrayList<String> values = (ArrayList<String>)element.m_values.clone();
 			m_values = values;
 		}
 
 		public EnumerationElement(String name, String description, boolean isAttribute)
 		{
 			super(ElementTypes.Enumeration, name, description, isAttribute);
 
 			// Dummy entry so there is always a restriction for enumerations.
 			m_restrictions.put("dummy", "dummy");
 		}
 
 		/**
 		 * Adds a new value to this enumeration.
 		 * @param value
 		 */
 		public void addValue(String value)
 		{
 			m_values.add(value);
 			m_modified = true;
 		}
 		
 		/**
 		 * Removes a specific value from this enumeration.
 		 * @param value
 		 */
 		public void removeValue(String value)
 		{
 			m_values.remove(value);
 			m_modified = true;
 		}
 		
 		/**
 		 * Removes all values from this enumeration.
 		 */
 		public void clearValues()
 		{
 			m_values.clear();
 			m_modified = true;
 		}
 		
 		/**
 		 * Returns the number of values in this enumeration.
 		 * @return
 		 */
 		public int getNumValues()
 		{
 			return m_values.size();
 		}
 		
 		/**
 		 * Returns a specific value from this enumeration.
 		 * @param index
 		 * @return
 		 */
 		public String getValue(int index)
 		{
 			return m_values.get(index);
 		}
 
 		/**
 		 * Sets the name of a dictionary that this enumeration should
 		 * pull its values from.  Note: this doesn't actually access
 		 * the dictionary; it merely stores the name.
 		 */
 		public void setDictionaryName(String name)
 		{
 			setRestriction("dictionary", name);
 		}
 		
 		/**
 		 * Returns the name of the dictionary that this enumeration
 		 * references, if set, or empty string if it does not reference
 		 * a dictionary.
 		 * @return
 		 */
 		public String getDictionaryName()
 		{
 			return getRestriction("dictionary", "");
 		}
 		
 		/**
 		 * Returns true if this enumeration is referencing a dictionary.
 		 * @return
 		 */
 		public boolean isUsingDictionary()
 		{
 			String dictionaryName = getDictionaryName();
 			return dictionaryName != null && dictionaryName.length() > 0;
 		}
 		
 		public void setFromXmlElement(XmlElement element)
 		{
 			super.setFromXmlElement(element);
 			
 			XmlElement restriction = element.element("restriction");
 			if (restriction != null)
 			{
 				String dictionary = restriction.value("dictionary");
 				if (dictionary != null && dictionary.length() > 0)
 				{
 					// TODO: Not needed.  This should be parsed by the parent
 					// class when it parses all restrictions.
 					setDictionaryName(dictionary);
 				}
 				else
 				{
 					List<String> values = restriction.values("value");
 					m_values.addAll(values);
 				}
 			}
 		}
 		
 		/**
 		 * Returns false.  Enumerations cannot have attributes.
 		 */
 		public boolean canHaveAttributes()
 		{
 			return false;
 		}
 		
 		public Element clone()
 		{
 			return new EnumerationElement(this);
 		}
 		
 		public void addRestrictionsXml(XmlStringWriter w)
 		{
 			if (!isUsingDictionary() && m_values != null && m_values.size() > 0)
 			{
 				for (String v : m_values)
 				{
 					w.add("value", v);
 				}
 			}
 		}
 	}
 	
 	private DocumentElement m_rootElement = new DocumentElement("root", "root");
 	
 	public Metadata(String name, String description, String label)
 	{
 		setName(name);
 		setDescription(description);
 		setLabel(label);
 	}
 
 	/**
 	 * Returns true if the metadata has been modified.
 	 * @return
 	 */
 	public boolean getIsModified()
 	{
 		return m_rootElement.getIsModified();
 	}
 
 	/**
 	 * Sets the name of this metadata.
 	 * @param name
 	 */
 	public void setName(String name)
 	{
 		m_rootElement.setSetting("name", name);
 	}
 	
 	/**
 	 * Returns the name of this metadata.
 	 * @return
 	 */
 	public String getName()
 	{
 		return m_rootElement.getName();
 	}
 	
 	/**
 	 * Sets the description of this metadata.
 	 * @param description
 	 */
 	public void setDescription(String description)
 	{
 		m_rootElement.setDescription(description);
 	}
 	
 	/**
 	 * Returns the description of this metadata.
 	 * @return
 	 */
 	public String getDescription()
 	{
 		return m_rootElement.getDescription();
 	}
 	
 	/**
 	 * Sets the label for this metadata.
 	 * @param label
 	 */
 	public void setLabel(String label)
 	{
 		m_rootElement.setSetting("label", label);
 	}
 	
 	/**
 	 * Returns the label for this metadata.
 	 * @return
 	 */
 	public String getLabel()
 	{
 		return m_rootElement.getSetting("label", "");
 	}
 	
 	/**
 	 * Returns the root element for this metadata.  The root element is a
 	 * DocumentElement but is not itself saved; it acts as a container
 	 * for the root-level elements.
 	 * @return
 	 */
 	public DocumentElement getRootElement()
 	{
 		return m_rootElement;
 	}
 	
 	/**
 	 * Clears the modified flag for this metadata and all its elements.
 	 */
 	public void clearModified()
 	{
 		m_rootElement.clearModified();
 	}
 	
 	/**
 	 * Create a new metadata object from the specified MF xml definition.
 	 * @param element
 	 * @return
 	 * @throws Exception
 	 */
 	public static Metadata create(XmlElement element) throws Exception
 	{
 		XmlElement type = element.element("type");
 		Metadata metadata = new Metadata(type.value("@name"), type.value("description"), type.value("label"));
 		parseMetadata(metadata.getRootElement(), type.elements("definition/element"));
 		metadata.clearModified();	// Some things during creation set the modified flag.
 		return metadata;
 	}
 	
 	private static void parseMetadata(Metadata.DocumentElement parent, List<XmlElement> xmlElements) throws Exception
 	{
 		if (xmlElements == null)
 		{
 			return;
 		}
 
 		for (XmlElement e : xmlElements)
 		{
 			Metadata.Element element = Metadata.createElement(e);
 			element.setParent(parent);
 			if (element instanceof Metadata.DocumentElement)
 			{
 				Metadata.DocumentElement docElement = (Metadata.DocumentElement)element;
 				parseMetadata(docElement, e.elements("element"));
 			}
 		}
 	}
 	
 	/**
 	 * Returns an xml representation of this metadata.
 	 * @return
 	 */
 	public String getXml()
 	{
 		XmlStringWriter x = new XmlStringWriter();
 		x.add("type", getName());
 
 		String label = getLabel();
 		if (label != null && label.length() > 0)
 		{
 			x.add("label", label);
 		}
 		
 		String description = getDescription();
 		if (description != null && description.length() > 0)
 		{
 			x.add("description", description);
 		}
 		
 		x.push("definition");
 		int numChildren = m_rootElement.getNumChildren();
 		for (int i = 0; i < numChildren; i++)
 		{
 			Element e = m_rootElement.getChild(i);
 			e.addXml(x);
 
 			// This feels a bit messy.  Pop should probably be in addXml()
 			// but derived metadata elements need to call up to the parent
 			// class to add common xml/attributes, so the parent can't pop.
 			x.pop(); 
 		}
 		x.pop();
 
 		return x.document();
 	}
 }
