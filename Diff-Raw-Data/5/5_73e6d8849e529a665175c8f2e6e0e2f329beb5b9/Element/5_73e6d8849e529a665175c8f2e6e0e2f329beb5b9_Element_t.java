 package fulcrum.xml;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Element extends Node {
 	private static final long serialVersionUID = -6621995432233569022L;
 	
 	private Set<Attribute> attributes = new HashSet<Attribute>();
 	private List<Element> elements = new ArrayList<Element>();
 	private Set<Namespace> namespaces = new HashSet<Namespace>();
 	
 	public Element() {}
 	public Element(String localName) {
 		super(localName);
 	}
 	public Element(String prefix, String uri, String localName) {
 		super(prefix, uri, localName);
 	}
 	public Element(String qName, String uri) {
 		super(qName, uri);
 	}
 	public void addAttribute(Attribute attribute) {
 		this.attributes.add(attribute);
 	}
 	public void addElement(Element element) {
 		if (element.getParent()!=null) {
 			Element n = element.detach();
 			this.elements.add(n);
 		} else {
 			element.setParent(this);
 			this.elements.add(element);
 		}
 	}
 	public void appendChild(Element element) {
 		addElement(element);
 	}
 	public void appendChild(String text) {
 		setText(text);
 	}
 	public Set<Attribute> getAttributes() {
 		return attributes;
 	}
 	public List<Element> getChildren() {
 		return elements;
 	}
 	public void addNamespace(Namespace ns) {
 		if (!namespaces.contains(ns)) {
 			namespaces.add(ns);
 		}
 	}
 	public void addNamespace(String prefix, String uri) {
 		Namespace ns = new Namespace(prefix, uri);
 		addNamespace(ns);
 	}
 	public List<Element> getChildren(String localName) {
 		List<Element> out = new ArrayList<Element>();
 		for (Element el : elements) {
 			if (el.getLocalName().equals(localName)) {
 				out.add(el);
 			}
 		}
 		return out;
 	}
 	public Element getFirstChildElement(String localName, String uri) {
 		Element out = null;
 		for (Element el : elements) {
 			if (el.getLocalName().equals(localName)) {
				if (uri != null && uri.trim().length()>0 && el.getUri()!=null && el.getUri().equals(uri)) {
					out = el;
					break;
 				} else {
 					out = el;
 					break;
 				}
 			}
 		}
 		return out;
 	}
 	public String toXML() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(LT).append(getQName());
 
 		if (getNamespace()!=null) {
 			builder.append(SPACE).append(getNamespace().toXML());
 		}
 		for (Namespace ns : namespaces) {
 			builder.append(SPACE).append(ns.toXML(getPrefix()));
 		}
 		for (Attribute attribute : attributes) {
 			builder.append(SPACE).append(attribute.toXML());
 		}
 		builder.append(GT);
 		for (Element el : elements) {
 			builder.append(el.toXML());
 		}
 		if (getText()!=null&&getText().trim().length()>0) {
 			builder.append(getText());
 		}
 		builder.append(LT).append(FWDS).append(getQName()).append(GT);
 		return builder.toString();
 	}
 	public Element detach() {
 		Element el = new Element(getPrefix(), getUri(), getLocalName());
 		for (Attribute attr : attributes) {
 			el.addAttribute(attr.detach());
 		}
 		for (Element child : elements) {
 			el.appendChild(child.detach());
 		}
 		return el;
 	}
 }
