 package bingo.lang.xml;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import bingo.lang.Converts;
 import bingo.lang.Enumerable;
 import bingo.lang.Strings;
 import bingo.lang.enumerable.IterableEnumerable;
 import bingo.lang.xml.XmlUtils.Predicates;
 
 public class XmlElement extends XmlContainer implements XmlNamed {
 
 	private final String name;
 	private final String prefix;
 	private final List<XmlAttribute> attributes = new ArrayList<XmlAttribute>();
 	
 	public XmlElement(String name, Object... content) {
 		this(null,name,content);
 	}	
 
 	public XmlElement(String prefix, String name, Object... content) {
 		this.prefix = prefix;
 		this.name   = name;
 		
 		for (Object obj : content) {
 			add(obj);
 		}
 	}
 	
 	public String name() {
 	    return name;
     }
 
 	public String prefix() {
 	    return prefix;
     }
 
 	public String text() {
 		Enumerable<XmlText> texts = childNodes().ofType(XmlText.class);
 		
 		if(texts.isEmpty()){
 			return null;
 		}else{
 			StringBuilder buf = new StringBuilder();
 			for(XmlText text : texts){
 				buf.append(text.value());
 			}
 			return buf.toString();
 		}
 	}
 	
 	public String textTrimmed(){
 		return Strings.trim(text());
 	}
 	
 	public Integer intText(){
 		String value = text();
 		return Strings.isEmpty(value) ? null : Converts.convert(Strings.trim(value), Integer.class);
 	}
 	
 	public int intText(int defaultValue){
 		Integer value = intText();
 		return null == value ? defaultValue : value;
 	}
 	
 	public boolean intText(boolean defaultValue){
 		Boolean value = boolText();
 		return null == value ? defaultValue : value;
 	}
 	
 	public Boolean boolText(){
 		String value = text();
 		return Strings.isEmpty(value) ? null : Converts.convert(Strings.trim(value), Boolean.class);
 	}
 	
 	public boolean boolText(boolean defaultValue){
 		Boolean value = boolText();
 		return null == value ? defaultValue : value;
 	}
 	
 	public Float floatText(){
 		String value = text();
 		return Strings.isEmpty(value) ? null : Converts.convert(Strings.trim(value), Float.class);
 	}
 	
 	public float floatText(float defaultValue){
 		Float value = floatText();
 		return null == value ? defaultValue : value;
 	}
 	
 	public Double doubleText(){
 		String value = text();
 		return Strings.isEmpty(value) ? null : Converts.convert(Strings.trim(value), Double.class);
 	}
 	
 	public double doubleText(double defaultValue){
 		Double value = doubleText();
 		return null == value ? defaultValue : value;
 	}
 	
 	public BigDecimal decimalText(){
 		String value = text();
 		return Strings.isEmpty(value) ? null : Converts.convert(Strings.trim(value), BigDecimal.class);
 	}
 	
 	public boolean hasAttributes(){
 		return !attributes.isEmpty();
 	}
 	
 	public IterableEnumerable<XmlAttribute> attributes() {
 		return IterableEnumerable.of(attributes);
 	}
 
 	public XmlAttribute attribute(String name) {
 		return attributes().firstOrNull(Predicates.<XmlAttribute> xnameEquals(name));
 	}
 	
 	public String attributeValue(String name) {
 		XmlAttribute attr = attribute(name);
 		return null == attr ? null : attr.value(); 
 	}
 	
 	public Boolean attributeValueForBool(String name){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? null : attr.boolValue();
 	}
 	
 	public boolean attributeValueForBool(String name,boolean defaultValue){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? defaultValue : attr.boolValue(defaultValue);
 	}
 	
 	public Integer attributeValueForInt(String name){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? null : attr.intValue();
 	}
 	
 	public int attributeValueForInt(String name,int defaultValue){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? defaultValue : attr.intValue(defaultValue);
 	}
 	
 	public Float attributeValueForFloat(String name){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? null : attr.floatValue();
 	}
 	
 	public float attributeValueForFloat(String name,float defaultValue){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? defaultValue : attr.floatValue(defaultValue);
 	}
 	
 	public Double attributeValueForDouble(String name){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? null : attr.doubleValue();
 	}
 	
 	public double attributeValueForDouble(String name,double defaultValue){
 		XmlAttribute attr = attribute(name);
 		return attr == null ? defaultValue : attr.doubleValue(defaultValue);
 	}
 	
 	public String attributeValueOrText(String attributeName){
 		XmlAttribute attr = attribute(attributeName);
 		
 		return attr == null ? text() : attr.value();
 	}
 	
 	public String childElementText(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.text();
 	}
 	
 	public Integer childElementTextForInt(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.intText();
 	}
 	
 	public int childElementTextForInt(String name,int defaultValue) {
 		XmlElement e = childElement(name);
 		return null == e ? defaultValue : e.intText(defaultValue);
 	}
 	
 	public Boolean childElementTextForBool(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.boolText();
 	}
 	
 	public boolean childElementTextForBool(String name,boolean defaultValue) {
 		XmlElement e = childElement(name);
 		return null == e ? defaultValue : e.boolText(defaultValue);
 	}
 	
 	public Float childElementTextForFloat(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.floatText();
 	}
 	
 	public float childElementTextForFloat(String name,float defaultValue) {
 		XmlElement e = childElement(name);
 		return null == e ? defaultValue : e.floatText(defaultValue);
 	}
 	
 	public Double childElementTextForDouble(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.doubleText();
 	}
 	
 	public double childElementTextForFloat(String name,double defaultValue) {
 		XmlElement e = childElement(name);
 		return null == e ? defaultValue : e.doubleText(defaultValue);
 	}
 	
 	public BigDecimal childElementTextForDecimal(String name) {
 		XmlElement e = childElement(name);
 		return null == e ? null : e.decimalText();
 	}
 	
 	public XmlElement requiredChildElement(String name) {
 		XmlElement e = childElement(name);
 		
 		if(null == e){
 			throw new XmlValidationException("child element '{0}' of parent '{1}' is required in xml : {2}",name, name(),documentUrl());
 		}
 		
 		return e;
 	}
 	
 	public String requiredChildElementText(String name) {
 		return requiredChildElement(name).requiredText();
 	}
 	
 	public String requiredText() throws XmlValidationException {
 		String string = Strings.trim(text());
 		
 		if(Strings.isEmpty(string)){
 			throw new XmlValidationException("text of element '{0}' is required in xml : {1}",name(),documentUrl());
 		}
 		
 		return string;
 	}
 	
 	public XmlAttribute requiredAttribute(String name) throws XmlValidationException {
 		XmlAttribute attr = attributes().firstOrNull(Predicates.<XmlAttribute> xnameEquals(name));
 		
 		if(null == attr){
 			throw new XmlValidationException("attribute '{0}' of element '{1}' is required in xml : {2}",name,name(),documentUrl());
 		}
 		
 		return attr.required();
 	}
 	
 	public String requiredAttributeValue(String name) throws XmlValidationException {
 		XmlAttribute attr = attribute(name);
 		
		if(null == attr || Strings.isEmpty(attr.value())){
 			throw new XmlValidationException("attribute '{0}' value of element '{1}' is required in xml : {2}",name,name(),documentUrl());
 		}
 		
		return attr.value();
 	}
 	
 	public String requiredAttributeValueOrText(String attributeName) throws XmlValidationException {
 		XmlAttribute attr = attribute(attributeName);
 
 		if(null != attr){
 			return attr.required().value();
 		}
 		
 		return requiredText();
 	}
 	
 	@Override
 	public void add(Object content) {
 		if (content instanceof XmlAttribute) {
 			attributes.add((XmlAttribute) content);
 		} else {
 			super.add(content);
 		}
 	}
 
 	@Override
 	public XmlNodeType nodeType() {
 		return XmlNodeType.ELEMENT;
 	}
 	
 	@Override
 	protected XmlElement element() {
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return toXml(XmlFormat.NOT_INDENTED);
 	}
 
 	@Override
 	public String toXml(XmlFormat format) {
 		boolean enableIndent = format.isIndentEnabled();
 		String indent = enableIndent ? getIndent(format) : "";
 		String newline = enableIndent ? "\n" : "";
 
 		String tagName = name();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(indent);
 		sb.append('<');
 		sb.append(tagName);
 
 		for (XmlAttribute att : attributes) {
 			sb.append(' ');
 			sb.append(att.name());
 			sb.append("=\"");
 			sb.append(XmlUtils.escapeAttributeValue(att.value()));
 			sb.append('"');
 		}
 
 		List<XmlNode> nodes = childNodes().toList();
 		if (nodes.size() == 0) {
 			sb.append(" />");
 		} else {
 			sb.append('>');
 			boolean onlyText = true;
 			for (XmlNode node : childNodes()) {
 				if (node.nodeType() == XmlNodeType.TEXT) {
 					sb.append(node.toXml(format));
 				} else {
 					onlyText = false;
 					sb.append(newline);
 					sb.append(node.toXml(format.incrementLevel()));
 				}
 			}
 			if (!onlyText) {
 				sb.append(newline + indent);
 			}
 			sb.append("</");
 			sb.append(tagName);
 			sb.append('>');
 		}
 		return sb.toString();
 	}
 }
