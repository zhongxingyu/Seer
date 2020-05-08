 package org.iucn.sis.server.api.fields;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.NamingException;
 
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldFactory;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.DBSessionFactory;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.RowProcessor;
 import com.solertium.db.SystemExecutionContext;
 import com.solertium.db.query.SelectQuery;
import com.solertium.db.vendor.PostgreSQLDBSession;
 import com.solertium.util.AlphanumericComparator;
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.ElementCollection;
 import com.solertium.util.TrivialExceptionHandler;
 
 public class FieldSchemaGenerator {
 	
 	private final ExecutionContext ec;
 	
 	public FieldSchemaGenerator() throws NamingException {
 		this("sis_lookups");
 	}
 	
 	public FieldSchemaGenerator(String session) throws NamingException {
 		this(DBSessionFactory.getDBSession(session));
 	}
 	
 	public FieldSchemaGenerator(DBSession session) {
 		ec = new SystemExecutionContext(session);
 		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
 		ec.setExecutionLevel(ExecutionContext.ADMIN);
		if (session instanceof PostgreSQLDBSession)
			ec.getDBSession().setIdentifierCase(DBSession.CASE_UPPER);
 	}
 	
 	public ExecutionContext getExecutionContext() {
 		return ec;
 	}
 	
 	/**
 	 * Generates an XSD for the given field, if found. 
 	 * @param fieldName
 	 * @return
 	 * @throws Exception
 	 */
 	public Document getSchema(String fieldName) throws Exception {
 		final Document document = createBasicSchemaDocument();
 		
 		//Defines a single field for this XML
 		final Element root = document.createElement("xs:element");
 		root.setAttribute("name", fieldName);
 		root.setAttribute("type", fieldName);
 		
 		document.getDocumentElement().appendChild(root);
 		
 		final List<Element> definitions = new ArrayList<Element>();
 		
 		getSchemaForField(document, fieldName, definitions);
 		
 		for (Element el : definitions)
 			document.getDocumentElement().appendChild(el);
 		
 		return document;
 	}
 	
 	/**
 	 * Re-writes the classification scheme XML on-the-fly to change the 
 	 * display ID of each row to be the actual information store in the 
 	 * database.
 	 * 
 	 * FIXME: this will be slow and painful, especially for big requests.  
 	 * We should probably generate this once, burn it out to a file, and 
 	 * keep that file around, updating it iff there are database changes 
 	 * made, or some function is manually called to update these files.
 	 * 
 	 * @param document
 	 * @param fieldName
 	 * @throws Exception
 	 */
 	public void appendCodesForClassificationScheme(Document document, String fieldName) throws Exception {
 		final SelectQuery query = new SelectQuery();
 		query.select(fieldName+"Lookup", "*");
 		
 		final Map<String, String> codeToID = new HashMap<String, String>();
 		
 		synchronized (this) {
 			ec.doQuery(query, new RowProcessor() {
 				public void process(Row row) {
 					codeToID.put(row.get("code").toString(), row.get("id").toString());
 				}
 			});
 		}
 		
 		for (Element el : new ElementCollection(document.getElementsByTagName("root"))) {
 			String id = el.getAttribute("code");
 			if (id != null && codeToID.containsKey(id))
 				el.setAttribute("code", codeToID.get(id));
 		}
 		
 		for (Element el : new ElementCollection(document.getElementsByTagName("child"))) {
 			String id = el.getAttribute("code");
 			if (id != null && codeToID.containsKey(id))
 				el.setAttribute("code", codeToID.get(id));
 		}
 	}
 	
 	/**
 	 * Given a field, this scans the database for lookup tables for this field and 
 	 * any of its subfields.  It returns a mapping of string (the field name) to 
 	 * a map of integer (lookup ID) to string (lookup value).
 	 *   
 	 * @param fieldName
 	 * @return
 	 * @throws Exception
 	 */
 	public Map<String, Map<Integer, String>> scanForLookups(String fieldName) throws Exception {
 		final Map<String, Map<Integer, String>> lookups = new HashMap<String, Map<Integer,String>>();
 		
 		scanForLookups(fieldName, lookups);
 		
 		return lookups;
 	}
 	
 	private void scanForLookups(String fieldName, Map<String, Map<Integer, String>> map) throws Exception {
 		final SelectQuery query = new SelectQuery();
 		query.select(fieldName, "*");
 		
 		final Row.Set rs = new Row.Set();
 		
		System.out.println(query.getSQL(ec.getDBSession()));
 		ec.doQuery(query, rs);
 		
 		for (Row row : rs.getSet()) {
 			String name = row.get("name").toString();
 			String data_type = row.get("data_type").toString();
 			
 			PrimitiveFieldType dataType = PrimitiveFieldType.get(data_type);
 			
 			if (PrimitiveFieldType.FOREIGN_KEY_PRIMITIVE.equals(dataType) || 
 					PrimitiveFieldType.FOREIGN_KEY_LIST_PRIMITIVE.equals(dataType)) {
 				String tableName = fieldName;
 				if (name.toLowerCase().endsWith("lookup")) {
 					tableName = name;
 				}
 				else {
 					if (tableName.endsWith("Subfield"))
 						tableName = tableName.substring(0, tableName.lastIndexOf("Subfield"));
 					//FIXME: this one's obvious...
 					if ("UseTrade".equals(tableName))
 						tableName += "Details_" + name + "Lookup";
 					else
 						tableName += "_" + name + "Lookup";
 				}
 				
 				final Map<Integer, String> mapping = loadLookup(tableName);
 				
 				if (!mapping.isEmpty())
 					map.put(tableName, mapping);
 			}
 			else if ("field".equals(data_type)) {
 				scanForLookups(name, map);
 			}
 		}
 	}
 	
 	public Map<Integer, String> loadLookup(final String tableName) {
 		final SelectQuery lookups = new SelectQuery();
 		lookups.select(tableName, "*");
 		
 		final Map<Integer, String> mapping = new LinkedHashMap<Integer, String>();
 		
 		synchronized(this) {
 			try {
 				ec.doQuery(lookups, new RowProcessor() {
 					public void process(Row row) {
 						try {
 							if (!"-1".equals(row.get("name").toString()))
 								mapping.put(row.get("id").getInteger(), row.get("label").toString());
 						} catch (NullPointerException e) {
 							TrivialExceptionHandler.ignore(this, e);
 							//TODO: can we stop processing early?
 						}
 					}
 				});
 			} catch (DBException e) {
 				Debug.println("Looking listing failed for {0}", tableName);
 				TrivialExceptionHandler.ignore(this, e);
 			}
 		}
 		
 		return mapping;
 	}
 	
 	private void getSchemaForField(Document document, String fieldName, List<Element> definitions) throws Exception {
 		final SelectQuery query = new SelectQuery();
 		query.select(fieldName, "name", "ASC");
 		query.select(fieldName, "data_type");
 		query.select(fieldName, "number_allowed");
 		
 		final Row.Set rs = new Row.Set();
 		
 		try {
 			ec.doQuery(query, rs);
 		} catch (DBException e) {
 			throw new Exception("No table for field " + fieldName, e);
 		}
 		
 		/*
 		 * Since the XML has to be in order, we must first add to 
 		 * these storage objects, then at the end, specify them 
 		 * in xs:sequence in order...
 		 */
 		Map<String, Row> subfields = new LinkedHashMap<String, Row>();
 		Map<String, Row> primitiveFields = new LinkedHashMap<String, Row>();
 		
 		for (Row row : rs.getSet()) {
 			String name = row.get("name").toString();
 			String dataType = row.get("data_type").toString();
 			
 			if (dataType.endsWith("_primitive_field")) {
 				primitiveFields.put(name, row);
 			}
 			else if ("field".equals(dataType)) {
 				subfields.put(name, row);
 				getSchemaForField(document, name, definitions);
 			}
 		}
 		
 		/*
 		 * Now, add the definitions to the sequence...
 		 */
 		final Element sequence = document.createElement("xs:sequence"); 
 		
 		if (!subfields.isEmpty()) {
 			final Element rootEl = document.createElement("xs:element");
 			rootEl.setAttribute("name", "subfields");
 			rootEl.setAttribute("minOccurs", "1");
 			rootEl.setAttribute("maxOccurs", "1");
 			
 			final List<String> sortedKeys = new ArrayList<String>(subfields.keySet());
 			Collections.sort(sortedKeys, new AlphanumericComparator());
 			
 			for (Map.Entry<String, Row> entry : subfields.entrySet()) {
 				String numberAllowed = entry.getValue().get("number_allowed").toString();
 				
 				Occurrence occurs = new Occurrence(numberAllowed);
 				
 				Element subfield = document.createElement("xs:element");
 				subfield.setAttribute("name", entry.getKey());
 				subfield.setAttribute("type", entry.getKey());
 				subfield.setAttribute("minOccurs", occurs.getMinOccurs());
 				subfield.setAttribute("maxOccurs", occurs.getMaxOccurs());
 			
 				Element wrapper = document.createElement("xs:complexType");
 				Element wrapperSequence = document.createElement("xs:sequence");
 				wrapperSequence.appendChild(subfield);
 				wrapper.appendChild(wrapperSequence);
 				
 				rootEl.appendChild(wrapper);
 			}
 			sequence.appendChild(rootEl);
 		}
 		
 		for (Map.Entry<String, Row> entry : primitiveFields.entrySet()) {
 			String name = entry.getValue().get("name").toString();
 			String dataType = entry.getValue().get("data_type").toString();
 			String numberAllowed = entry.getValue().get("number_allowed").toString();
 			
 			Occurrence occurs = new Occurrence(numberAllowed);
 			
 			Element primitive = document.createElement("xs:element");
 			primitive.setAttribute("name", name);
 			primitive.setAttribute("type", PrimitiveFieldType.get(dataType).getName());
 			primitive.setAttribute("minOccurs", occurs.getMinOccurs());
 			primitive.setAttribute("maxOccurs", occurs.getMaxOccurs());
 			
 			sequence.appendChild(primitive);
 		}
 		
 		//Define the field object
 		final Element field = document.createElement("xs:complexType"); {
 			field.setAttribute("name", fieldName);
 		
 			field.appendChild(sequence);
 			field.appendChild(createAttribute(document, "id", "xs:integer"));
 			field.appendChild(createAttribute(document, "version", "xs:string", "optional"));
 			
 			document.getDocumentElement().appendChild(field);
 		}
 		
 		definitions.add(field);
 	}
 	
 	private Element createAttribute(Document document, String name, String type) {
 		return createAttribute(document, name, type, "required");
 	}
 	
 	private Element createAttribute(Document document, String name, String type, String use) {
 		return createAttribute(document, name, type, use, null);
 	}
 	
 	private Element createAttribute(Document document, String name, String type, String use, String value) {
 		final Element attribute = document.createElement("xs:attribute");
 		attribute.setAttribute("name", name);
 		attribute.setAttribute("type", type);
 		attribute.setAttribute("use", use);
 		if (value != null)
 			attribute.setAttribute("fixed", value);
 		
 		return attribute;
 	}
 	
 	public Field getField(String fieldName) throws Exception {
 		final Field field = new Field();
 		field.setName(fieldName);
 		
 		findField(field, fieldName);
 		
 		return field;
 	}
 	
 	private void findField(Field field, String fieldName) throws Exception {
 		final SelectQuery query = new SelectQuery();
 		query.select(fieldName, "*");
 		
 		final Row.Set rs = new Row.Set();
 		
 		try {
 			ec.doQuery(query, rs);
 		} catch (DBException e) {
 			throw new Exception("No table for field " + fieldName, e);
 		}
 		
 		for (Row row : rs.getSet()) {
 			String name = row.get("name").toString();
 			String dataType = row.get("data_type").toString();
 			if (dataType == null)
 				continue;
 			
 			if (dataType.endsWith("_primitive_field")) {
 				PrimitiveField<?> primitiveField =  
 					PrimitiveFieldFactory.generatePrimitiveField(dataType);
 				if (primitiveField != null) {
 					primitiveField.setField(field);
 					primitiveField.setName(name);
 					
 					if (primitiveField instanceof ForeignKeyPrimitiveField || 
 							primitiveField instanceof ForeignKeyListPrimitiveField) {
 						String tableName = fieldName;
 						if (primitiveField.getName().toLowerCase().endsWith("lookup")) {
 							tableName = name;
 						}
 						else {
 							if (tableName.endsWith("Subfield"))
 								tableName = tableName.substring(0, tableName.lastIndexOf("Subfield"));
 							
 							tableName += "_" + primitiveField.getName() + "Lookup";
 						}
 						
 						if (primitiveField instanceof ForeignKeyPrimitiveField)
 							((ForeignKeyPrimitiveField)primitiveField).setTableID(tableName);
 						else if (primitiveField instanceof ForeignKeyListPrimitiveField)
 							((ForeignKeyListPrimitiveField)primitiveField).setTableID(tableName);
 						
 						/*
 						 * TODO: Do we want to pass the lookup values 
 						 * along, or query for them live, or what?  I 
 						 * know they are not being passed currently...
 						 */
 					}
 					
 					field.addPrimitiveField(primitiveField);
 				}
 			}
 			else if ("field".equals(dataType)) {
 				Field child = new Field();
 				child.setParent(field);
 				child.setName(name);
 				
 				findField(child, name);
 				
 				field.addField(child);
 			}
 		}
 	}
 	
 	private Document createBasicSchemaDocument() {
 		return BaseDocumentUtils.impl.getInputStreamFile(FieldSchemaGenerator.class.getResourceAsStream("fieldschema.xsd"));
 	}
 	
 	private static class Occurrence {
 		
 		private final String minOccurs, maxOccurs;
 		
 		public Occurrence(String numberAllowed) {
 			/*
 			 * TODO: this only parses for ?, * and #.  
 			 * There may be other possibilities, and 
 			 * we should account for them here, I just 
 			 * don't know them all.
 			 */
 			if ("?".equals(numberAllowed)) {
 				minOccurs = "0";
 				maxOccurs = "1";
 			}
 			else if ("*".equals(numberAllowed)) {
 				minOccurs = "0";
 				maxOccurs = "unbounded";
 			}
 			else {
 				Integer number = null;
 				try {
 					number = Integer.parseInt(numberAllowed);
 				} catch (NumberFormatException e) {
 					TrivialExceptionHandler.ignore(numberAllowed, e);
 				} catch (NullPointerException e) {
 					TrivialExceptionHandler.ignore(numberAllowed, e);
 				}
 				
 				if (number != null) {
 					minOccurs = numberAllowed;
 					maxOccurs = numberAllowed;
 				}
 				else {
 					minOccurs = "0";
 					maxOccurs = "unbounded";
 				}
 			}
 		}
 		
 		public String getMaxOccurs() {
 			return maxOccurs;
 		}
 		
 		public String getMinOccurs() {
 			return minOccurs;
 		}
 		
 	}
 
 }
