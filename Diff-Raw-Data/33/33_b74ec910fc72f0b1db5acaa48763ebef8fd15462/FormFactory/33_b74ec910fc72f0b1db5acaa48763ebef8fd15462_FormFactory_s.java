 package com.nacre.service;
 
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.namespace.NamespaceContext;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.xml.sax.SAXException;
 
 import com.nacre.service.vo.ComplexType;
 import com.nacre.service.vo.Decoration;
 import com.nacre.service.vo.Field;
 import com.nacre.service.vo.Form;
 import com.nacre.service.vo.FormAction;
 import com.nacre.service.vo.SimpleType;
 import com.sun.xml.xsom.XSAnnotation;
 import com.sun.xml.xsom.XSAttGroupDecl;
 import com.sun.xml.xsom.XSAttributeDecl;
 import com.sun.xml.xsom.XSAttributeUse;
 import com.sun.xml.xsom.XSComplexType;
 import com.sun.xml.xsom.XSComponent;
 import com.sun.xml.xsom.XSContentType;
 import com.sun.xml.xsom.XSElementDecl;
 import com.sun.xml.xsom.XSFacet;
 import com.sun.xml.xsom.XSIdentityConstraint;
 import com.sun.xml.xsom.XSModelGroup;
 import com.sun.xml.xsom.XSModelGroupDecl;
 import com.sun.xml.xsom.XSNotation;
 import com.sun.xml.xsom.XSParticle;
 import com.sun.xml.xsom.XSRestrictionSimpleType;
 import com.sun.xml.xsom.XSSchema;
 import com.sun.xml.xsom.XSSchemaSet;
 import com.sun.xml.xsom.XSSimpleType;
 import com.sun.xml.xsom.XSWildcard;
 import com.sun.xml.xsom.XSXPath;
 import com.sun.xml.xsom.XmlString;
 import com.sun.xml.xsom.parser.XSOMParser;
 import com.sun.xml.xsom.visitor.XSFunction;
 
 public class FormFactory
 {
 	private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
 	private XSSchemaSet parsed;
 	public FormFactory(URL... xsd) throws SAXException {
 		XSOMParser parser = new XSOMParser();
 		parser.setEntityResolver(ParsingUtils.nacreXSOMEntityResolver);
 		parser.setErrorHandler(ParsingUtils.nacreXSOMErrorHandler);
 		parser.setAnnotationParser(ParsingUtils.nacreXSOMAnnotationParserFactory);
 		for (URL schema : xsd) {
 			// TODO check last modified timestamp and only reload if necessary
 			parser.parse(schema);
 		}
 		parsed = parser.getResult();
 	}
 	
 	public Field query(String query) {
 		System.out.println("Query for " + query);
 		XSComponent result = parsed.selectSingle(query, new NamespaceContext() {
 			public String getNamespaceURI(String prefix) {
 				return "http://www.nacre.com/test";
 			}
 			public String getPrefix(String namespaceURI) {
 				return "";
 			}
 			public Iterator<String> getPrefixes(String namespaceURI) {
 				return Arrays.asList(new String[]{""}).iterator();
 			}
 		});
 		return result.apply(func);
 	}
 
 	public Form getForm(String name) {
 		Form form = new Form();
 		form.setForm(findComplexType(name));
 		FormAction saveAction = new FormAction();
 		saveAction.setLabel("Save");
 		saveAction.setUri("/save");
 		form.getActions().add(saveAction);
 		return form;
 	}
 	
 	public ComplexType findComplexType(String name) {
 		System.out.println("Getting form " + name);
 		XSComplexType type = null;
 		for (XSSchema schema : parsed.getSchemas()) {
 			type = schema.getComplexType(name);
 		}
 		return (ComplexType) type.apply(func);
 	}
 
 	private ComplexType getComplexType(XSComplexType type) {
 		XSParticle particle = type.getContentType().asParticle();
 		ComplexType vo = (ComplexType) particle.apply(func);
 		for (XSAttributeUse attr : type.getAttributeUses()) {
 			vo.getFields().add(attr.apply(func));
 		}
 		vo.setName(type.getName());
 		return vo;
 	}
 
 	private Field parseParticle(XSParticle particle) {
 		Field vo = particle.getTerm().apply(func);
 		vo.setMaxOccurs(particle.getMaxOccurs());
 		vo.setMinOccurs(particle.getMinOccurs());
 		return vo;
 	}
 	
 	private ComplexType parseModelGroup(XSModelGroup group) {
 		ComplexType vo = new ComplexType();
 		System.out.println(group.getChildren().length + " children");
 		for (XSParticle child : group.getChildren()) {
 			System.out.println("child is " + child.getTerm());
 			Field field = child.apply(func);
 			vo.getFields().add(field);
 		}
 		return vo;
 	}
 	@SuppressWarnings("unchecked")
 	private Field parseElement(XSElementDecl elem) {
 		Field field;
 		System.out.println("\telem " + elem.getName() + " " + elem.getType().getName() + " ns: " + elem.getType().getTargetNamespace());
 		if (elem.getType().getName() == null) {
 			// anonymous restriction
 			field = elem.getType().asSimpleType().apply(func);
 		} else if (!elem.getType().getTargetNamespace().equals(XSD_NAMESPACE)) {
 			// user-defined type
 			if (elem.getType().isComplexType()) {
 				field = elem.getType().asComplexType().apply(func);
 			} else {
 				field = elem.getType().asSimpleType().apply(func);
 			}
 		} else {
 			// simple type, no restriction
 			field = new SimpleType();
 			((SimpleType)field).setBaseType(elem.getType().getName());
 		}
 		com.sun.xml.xsom.XSAnnotation annotation = elem.getAnnotation();
 		if (annotation != null) {
 			System.out.println("adding decoration " + annotation.getLocator().getLineNumber() + ":" + annotation.getLocator().getColumnNumber());
 			field.setDecoration((Decoration) ((Map<String,Decoration>)annotation.getAnnotation()).get(ParsingUtils.locatorKey(annotation.getLocator())));
 			System.out.println("Added annot label " + field.getDecoration().getLabel());
 		} else {
 			// beautify field name if no label is set
 			// TODO this should be configurable via schema-level annotation, options for delimiter-base parsing as well
 			Decoration dec = new Decoration();
 			dec.setLabel(WordUtils.capitalize(StringUtils.join(
 					StringUtils.splitByCharacterTypeCamelCase(elem.getName()), " ")));
 			field.setDecoration(dec);
 		}
 		field.setName(elem.getName());
 		if (field.getFieldType().equals(Field.FieldType.SimpleType)) {
 			((SimpleType)field).setDefault(ObjectUtils.defaultIfNull(elem.getDefaultValue(), "").toString());
 			((SimpleType)field).setFixed(ObjectUtils.defaultIfNull(elem.getFixedValue(), "").toString());
 		}
 		return field;
 	}
 
 	public Field findSimpleType(String name) {
 		XSSimpleType type = null;
 		for (XSSchema schema : parsed.getSchemas()) {
 			type = schema.getSimpleType(name);
 		}
 		if (type != null) {
 			return getSimpleType(type);
 		} else {
 			System.out.println("No simple type named " + name);
 			return null;
 		}
 	}
 
 	private Field getSimpleType(XSSimpleType type) {
 		System.out.println("simple time " + type.getName() + " is " + (type.isRestriction() ? "restriction" : "simple simple"));
 		if (type.isRestriction()) {
 			XSRestrictionSimpleType restriction = type.asRestriction();
 			return getRestriction(restriction);
 		} else {
 			SimpleType st = new SimpleType();
 			st.setName(type.getName());
 			st.setBaseType(type.getBaseType().getName());
 			return st;
 		}
 	}
 
 	private Field getRestriction(XSRestrictionSimpleType restriction) {
 		SimpleType field = new SimpleType();
 		System.out.println("\t\t restriction base " + restriction.getBaseType().getName() + ", facets: " + restriction.getDeclaredFacets().size());
 		field.setMaxLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_MAXLENGTH)));
 		field.setMinLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_MINLENGTH)));
 		field.setLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_LENGTH)));
 
 		field.setPattern(getFacet(restriction, XSFacet.FACET_PATTERN));
 
 		field.setMaxExclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MAXEXCLUSIVE)));
 		field.setMaxInclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MAXINCLUSIVE)));
 		field.setMinExclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MINEXCLUSIVE)));
 		field.setMinInclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MININCLUSIVE)));
 
 		field.setFractionDigits(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_FRACTIONDIGITS)));
 		field.setTotalDigits(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_TOTALDIGITS)));
 		
 		String ws = getFacet(restriction, XSFacet.FACET_WHITESPACE);
 		if (ws != null) {
 			field.setWhitespace(SimpleType.WhiteSpaceRestriction.valueOf(ws));
 		}
 
 		field.setBaseType(restriction.getBaseType().getName());
 		return field;
 	}
 
 	private String getFacet(XSRestrictionSimpleType r, String name) {
 		XSFacet facet = r.getDeclaredFacet(name);
 		if (facet != null) {
 			XmlString xs = facet.getValue();
 			return xs.toString();
 		}
 		return null;
 	}
 	
 	private XSFunction<Field> func = new XSFunction<Field>() {
 
 		public Field annotation(XSAnnotation arg0) {
 			System.out.println("Annotation?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field attGroupDecl(XSAttGroupDecl arg0) {
 			System.out.println("Attr group decl?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field attributeDecl(XSAttributeDecl arg0) {
 			System.out.println("Attr decl?");
			// TODO Auto-generated method stub
			return null;
		}

		public Field attributeUse(XSAttributeUse arg0) {
			System.out.println("Attr use?");
 			SimpleType st;
			if (arg0.getDecl().getType() == null) {
 				System.out.println("attr type is null");
				st = (SimpleType) arg0.getDecl().getType().apply(func);
 			} else {
 				st = new SimpleType();
				st.setBaseType(arg0.getDecl().getType().getName());
				st.setName(arg0.getDecl().getName());
 				System.out.println("attr type is there " + st.getName() + ", " + st.getBaseType());
 			}
 			st.setDefault(ObjectUtils.defaultIfNull(arg0.getDefaultValue(),"").toString());
 			st.setFixed(ObjectUtils.defaultIfNull(arg0.getFixedValue(),"").toString());
 			st.setAttribute(true);
 			return st;
 		}
 
 		public Field complexType(XSComplexType arg0) {
 			System.out.println("query returned complex type");
 			return getComplexType(arg0);
 		}
 
 		public Field facet(XSFacet arg0) {
 			System.out.println("Facet ?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field identityConstraint(XSIdentityConstraint arg0) {
 			System.out.println("id constraint?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field notation(XSNotation arg0) {
 			System.out.println("Note?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field schema(XSSchema arg0) {
 			System.out.println("Schema?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field xpath(XSXPath arg0) {
 			System.out.println("XPath?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field empty(XSContentType arg0) {
 			System.out.println("content type?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field particle(XSParticle arg0) {
 			System.out.println("query returned particle");
 			return parseParticle(arg0);
 		}
 
 		public Field simpleType(XSSimpleType arg0) {
 			System.out.println("query returned simple type");
 			return getSimpleType(arg0);
 		}
 
 		public Field elementDecl(XSElementDecl arg0) {
 			System.out.println("query returned element declaration");
 			return parseElement(arg0);
 		}
 
 		public Field modelGroup(XSModelGroup arg0) {
 			System.out.println("query returned term");
 			return parseModelGroup(arg0);
 		}
 
 		public Field modelGroupDecl(XSModelGroupDecl arg0) {
 			System.out.println("query returned model group decl");
 			return parseModelGroup(arg0.getModelGroup());
 		}
 		public Field wildcard(XSWildcard arg0) {
 			System.out.println("wildcard?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	};
 }
