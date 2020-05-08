 package rtay;
 
 import java.util.Vector;
 
 /**
  * @author Raymond Tay
  * @date 05 July 2012
  * @version 1.0
  * Class to manage functions and represent an actual xml tag
  * 		   contains a cyclic reference to itself as link to child tags
  * 		   manages conversion of object to string toString()
  */
 public class XmlTag {
	private static final char LEVEL_DELIMITER = '\n'; // may require change depending on platform
 	private String name; // compulsory attribute 
 	private Vector <XmlTag> childTags;
 	private Vector <XmlAttribute> attributes;
 	private String value = ""; // default to empty if not explicitly set
 	private int level = 0; // default to 0 if not explicitly set
 	
 	// ctor
 	/**
 	 * XmlTag constructor with compulsory name
 	 * @param name Name of tag
 	 */
 	public XmlTag(String name) {
 		this.childTags = new Vector <XmlTag>();
 		this.attributes = new Vector <XmlAttribute>();
 		this.setName(name);
 	}
 	
 	/**
 	 * XmlTag constructor with compulsory name, optional value
 	 * @param name Name of tag
 	 * @param value	 Value of specified tag
 	 */
 	public XmlTag(String name, String value) {		
 		this.childTags = new Vector <XmlTag>();
 		this.attributes = new Vector <XmlAttribute>();
 		this.setName(name);
 		this.setValue(value);
 	}
 	
 	// setters
 	/**
 	 * Sets name of XmlTag
 	 * @param name	Name of tag
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Sets value of XmlTag
 	 * @param value Value of tag
 	 */
 	public void setValue(String value) {
 		this.value = value;
 	}
 	
 	/**
 	 * Sets value of Level
 	 * 		   Defines the number of tabs before the tag
 	 * @param level		level of the tag
 	 */
 	public void setLevel(int level) {
 		this.level = level;		
 	}
 	
 	/**
 	 * Adds an attribute to tag
 	 * @param attribute XmlAttribute containing attribute name and value
 	 */
 	public void addAttribute(XmlAttribute attribute) {
 		this.attributes.add(attribute);
 	}
 	
 	/**
 	 * Adds a child tag to the current tag with lower level
 	 * @param childTag
 	 */
 	public void addChildTag(XmlTag childTag) {
 		// Reset tag of child to current add one
 		childTag.setLevel(this.getLevel() + 1);
 		this.childTags.add(childTag);		
 	}
 	
 	// getters
 	/**
 	 * Retrieves name of tag
 	 * @return name
 	 */
 	public String getName() {
 		return this.name;
 	}
 	
 	/**
 	 * Retrieves value of tag
 	 * @return value
 	 */
 	public String getValue() {
 		return this.value;
 	}
 	
 	/**
 	 * Retrieves level of tag in the tag hierarchy
 	 * @return level of tag
 	 */
 	public int getLevel() {
 		return this.level;
 	}
 	
 	// methods
 	/**
 	 * Generates the open tag (including attributes if set) 
 	 * @param tabs Adds tabs based on level before open tag if true
 	 * @return	string representing open tag, eg. <FirstName>
 	 */
 	public String getOpenTagString(boolean tabs) {
 		String xmlString = "";
 		
 		// Generate tabs for opening tag
 		if(tabs) {
 			for(int i=0; i< level; i++) {
 				xmlString += '\t';
 			}
 		}
 		
 		// Generate Xml opening tag string
 		xmlString += "<"; // Start opening tag
 		xmlString += this.name;
 	
 		// Concatenate all attributes
 		for(XmlAttribute xa : this.attributes) {
 			xmlString += " ";
 			xmlString += xa.toString();
 		}
 		
 		if(this.childTags.size()>0 && this.value.length() > 0) {
 			// show value as attribute if set and contains child tags
 			xmlString += " ";
 			xmlString += "value=";
 			xmlString += '\"';
 			xmlString += this.value;
 			xmlString += '\"';
 		}
 		xmlString += ">"; // End of opening tag
 		return xmlString;
 	}
 	
 	/**
 	 * Generates the body tag (including child tags if set) 
 	 * @return	string representing body within tag, eg. Raymond
 	 */
 	public String getBodyString() {
 		String xmlString = "";		
 		
 		// Generate Xml body string 
 		if(this.childTags.size() == 0) {
 			// show value in innerXML without delimiter (end of recursion)
 			xmlString += this.value;
 		}
 		else
 		{
 			for(XmlTag childTag: this.childTags) {
 				// there is another level below current tag
 				xmlString += LEVEL_DELIMITER; 
 				// show lower level tag (recursively)
 				xmlString += childTag.toString();
 			}
 			xmlString += LEVEL_DELIMITER;
 		}
 		return xmlString;
 	}
 	
 	/**
 	 * Generates the close tag 
 	 * @param tabs Adds tabs based on level before close tag if true
 	 * @return	string representing close tag, eg. </FirstName>
 	 */	
 	public String getCloseTagString(boolean tabs) {
 		String xmlString = "";	
 		// Generate Xml closing tags
 		
 		// Generate tab for closing tags (only if newline)
 		if(this.childTags.size() != 0 && tabs) {
 			for(int i=0; i< level; i++) {
 					xmlString += '\t';
 			}
 		}
 		
 		xmlString += "</"; // start of closing tag
 		xmlString += this.name;
 		xmlString += ">"; // end of closing tag
 		
 		return xmlString;
 	}
 	
 	/**
 	 * @brief	Display Xmltag data as actual Xml string 
 	 * @return	Xml formatted tabbed string with open tag, body, and close tag
 	 */
 	public String toString() {
 		String xmlString = "";
 		xmlString += this.getOpenTagString(true);
 		xmlString += this.getBodyString();
 		xmlString += this.getCloseTagString(true);
 		return xmlString;
 	}
 	
 	// static methods
 	/**
 	 * GEDCOM specific convenience method to create an XmlTag 
 	 * @param tagOrId String containing GEDCOM tagOrID field 
 	 * @param data String containing GEDCOM data field
 	 * @param tagOrIdRegex Regex string to distinguish Id in tagOrId
 	 * @return
 	 */
 	public static XmlTag createGEDCOMTag(String tagOrId, String data, String tagOrIdRegex) {
 		XmlTag newObj;
 		if(tagOrId.matches(tagOrIdRegex)) {
 			// tagOrID is an ID, Data is the tagName
 			newObj = new XmlTag(data);
 			newObj.addAttribute(new XmlAttribute("id", tagOrId));
 		}
 		else {
 			// tagORID is a tag, Data is value
 			newObj = new XmlTag(tagOrId, data);
 		}
 		return newObj;
 	}	
 }
