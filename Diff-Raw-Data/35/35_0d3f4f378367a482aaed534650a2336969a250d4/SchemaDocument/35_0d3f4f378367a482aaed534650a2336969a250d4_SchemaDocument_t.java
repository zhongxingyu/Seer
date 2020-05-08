 package com.xaf.db;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import java.lang.reflect.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.*;
 
 import com.xaf.xml.*;
 import com.xaf.form.field.SelectChoicesList;
 import com.xaf.form.field.SelectChoice;
 import com.xaf.form.DialogContext;
 
 /**
  * Provides the ability to fully describe an entire database
  * schema in a single or multiple XML files; complete support for data
  * dictionaries, column domains, and table inheritance is built-in.
  * These XML files can then be read in at run-time to provide complete
  * database schema information in a RDBMS-independent manner.
  * After a schema is read in, complete information about all tables,
  * columns, enumerations, foreign keys, indexes, etc are available for processing
  * or DDL generation. A reference implementation servlet is provided to allow to
  * be able to browse a SchemaDocument (providing a full online database
  * design document that could easily replace ERDs).
  *
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 public class SchemaDocument extends XmlSource
 {
 	public static final String ATTRNAME_TYPE = "type";
 
 	public static final String[] MACROSIN_COLUMNNODES = { "parentref", "lookupref", "selfref", "usetype", "cache", "sqldefn", "size" };
     public static final String[] MACROSIN_TABLENODES = { "name", "abbrev", "parent" };
     public static final String[] MACROSIN_INDEXNODES = { "name" };
     public static final String[] REFTYPE_NAMES = { "none", "parent", "lookup", "self", "usetype" };
 
 	public static final int REFTYPE_NONE    = 0;
 	public static final int REFTYPE_PARENT  = 1;
 	public static final int REFTYPE_LOOKUP  = 2;
 	public static final int REFTYPE_SELF    = 3;
 	public static final int REFTYPE_USETYPE = 4;
 
     static HashSet replaceMacrosInColumnNodes = null;
     static HashSet replaceMacrosInTableNodes = null;
     static HashSet replaceMacrosInIndexNodes = null;
 
 	private Map dataTypeNodes = new HashMap();
 	private Map tableTypeNodes = new HashMap();
 	private Map indexTypeNodes = new HashMap();
 	private Map tableNodes = new HashMap();
 	private List columnTableNodes = new ArrayList();
     private Map tableParams = new HashMap(); // key is table name, value is hash-table of key/value pairs
     private Map enumTableDataChoices = new HashMap(); // key is an enum data table name, value is SelectChoicesList
 
 	public SchemaDocument()
 	{
         if(replaceMacrosInColumnNodes == null)
         {
             replaceMacrosInColumnNodes = new HashSet();
             replaceMacrosInTableNodes = new HashSet();
             replaceMacrosInIndexNodes = new HashSet();
 
             for(int i = 0; i < MACROSIN_COLUMNNODES.length; i++)
                 replaceMacrosInColumnNodes.add(MACROSIN_COLUMNNODES[i]);
             for(int i = 0; i < MACROSIN_TABLENODES.length; i++)
                 replaceMacrosInTableNodes.add(MACROSIN_TABLENODES[i]);
             for(int i = 0; i < MACROSIN_INDEXNODES.length; i++)
                 replaceMacrosInIndexNodes.add(MACROSIN_INDEXNODES[i]);
         }
     }
 
 	public SchemaDocument(File file)
 	{
         this();
 		loadDocument(file);
  	}
 
 	public SchemaDocument(Connection conn, String catalog, String schemaPattern) throws ParserConfigurationException, SQLException
 	{
 		this();
 		loadDocument(conn, catalog, schemaPattern);
 	}
 
     public void setUrl(String url)
     {
         loadDocument(new File(url));
     }
 
 	public Map getDataTypes() { return dataTypeNodes;	}
 	public Map getTableTypes() { return tableTypeNodes; }
 	public Map getIndexTypes() { return indexTypeNodes; }
 
 	public Map getTables() { return tableNodes; }
 
 	public String[] getTableNames(boolean includeAudit)
 	{
 		ArrayList tableNames = new ArrayList();
 
 		if(includeAudit)
 		{
 			for(Iterator i = tableNodes.values().iterator(); i.hasNext(); )
 			{
 				Element table = (Element) i.next();
 				tableNames.add(table.getAttribute("name"));
 			}
 		}
 		else
 		{
             for(Iterator i = tableNodes.values().iterator(); i.hasNext(); )
 			{
                 Element table = (Element) i.next();
 				if(! table.getAttribute("is-audit").equals("yes"))
 					tableNames.add(table.getAttribute("name"));
 			}
 		}
 
 		String[] result = new String[tableNames.size()];
 		tableNames.toArray(result);
 		Arrays.sort(result);
 		return result;
 	}
 
     public SelectChoicesList getEnumTableData(String tableName)
     {
         SelectChoicesList choices = (SelectChoicesList) enumTableDataChoices.get(tableName);
         if(choices != null)
             return choices;
 
         choices = new SelectChoicesList();
 
         Element tableElem = (Element) tableNodes.get(tableName.toUpperCase());
         if(tableElem == null)
         {
             choices.add(new SelectChoice("Enumeration table '"+ tableName +"' not found in schema '"+ this.getSourceDocument().getFile().getAbsolutePath() +"'"));
             return choices;
         }
 
         NodeList tableChildren = tableElem.getChildNodes();
         int tableChildrenCount = tableChildren.getLength();
         for(int c = 0; c < tableChildrenCount; c++)
         {
             Node node = tableChildren.item(c);
             if("enum".equals(node.getNodeName()))
             {
                 Element enumElem = (Element) node;
                 String id = enumElem.getAttribute("id");
                 String caption = enumElem.getFirstChild().getNodeValue();
                 choices.add(id.length() > 0 ? new SelectChoice(caption, id) : new SelectChoice(caption));
             }
         }
 
         enumTableDataChoices.put(tableName, choices);
         return choices;
     }
 
 	public void inheritNodes(Element element, Map sourcePool)
 	{
 		inheritNodes(element, sourcePool, ATTRNAME_TYPE);
 	}
 
 	public DocumentFragment getCompositeColumns(Element column, Element composite)
 	{
 		NodeList compNodes = composite.getChildNodes();
 		String compositeName = column.getAttribute("name");
 		DocumentFragment compColumns = composite.getOwnerDocument().createDocumentFragment();
 		for(int c = 0; c < compNodes.getLength(); c++)
 		{
 			Node compNode = compNodes.item(c);
 			if(compNode.getNodeName().equals("column"))
 			{
 				Node nameAttr = ((Element) compNode).getAttributeNode("name");
 				replaceNodeValue(nameAttr, "$name$", compositeName);
		        fixupColumnElement((Element) compNode, false);
 				((Element) compNode).setAttribute("descr", column.getAttribute("descr"));
 				compColumns.appendChild(compNode);
 			}
 		}
 		return compColumns;
 	}
 
    public boolean isReferenceColumn(Element column)
     {
        return  column.getAttribute("lookupref").length() > 0 || column.getAttribute("parentref").length() > 0 ||
                column.getAttribute("selfref").length() > 0 || column.getAttribute("usetype").length() > 0;
    }

    public void fixupColumnElement(Element column, boolean inRefColumn)
    {
        // if we're not fixing up a reference column and this column happens to be a reference, we're not going
        // to deal with it right now
        if(inRefColumn == false && isReferenceColumn(column))
            return;

         inheritNodes(column, dataTypeNodes);
 
         NodeList colInfo = column.getChildNodes();
         ArrayList sqlDefnElems = new ArrayList();
         String size = null;
 
         size = column.getAttribute("size");
         if(size != null && size.length() == 0)
             size = null;
 
         for(int i = 0; i < colInfo.getLength(); i++)
         {
             Node childNode = colInfo.item(i);
             String nodeName = childNode.getNodeName();
 
             if(nodeName.equals("table"))
 			{
 				columnTableNodes.add(childNode);
                 fixupTableElement((Element) childNode);
 			}
             else if(nodeName.equals("sqldefn"))
                 sqlDefnElems.add(childNode);
             else if (size == null && nodeName.equals("size"))
                 size = childNode.getFirstChild().getNodeValue();
         }
 
         if(size != null && sqlDefnElems.size() > 0)
         {
             for(int i = 0; i < sqlDefnElems.size(); i++)
                 replaceNodeValue(((Element) sqlDefnElems.get(i)).getFirstChild(), "%size%", size);
         }
     }
 
 	public String getPrimaryKey(Element table)
 	{
         NodeList columns = table.getChildNodes();
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
 			if(node.getNodeName().equals("column") && ((Element) node).getAttribute("primarykey").equals("yes"))
 				return 	((Element) node).getAttribute("name");
         }
 
 		return null;
 	}
 
 	public Element getParentConnectorColumn(Element table)
 	{
         NodeList columns = table.getChildNodes();
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
 			if(node.getNodeName().equals("column") && ((Element) node).getAttribute("reftype").equals("parent"))
 				return (Element) node;
         }
 
 		return null;
 	}
 
 	public void resolveIndexes(Element table)
 	{
         String tableName = table.getAttribute("name");
 		List columnIndexes = new ArrayList();
 		Document tableDoc = table.getOwnerDocument();
 
         NodeList columns = table.getChildNodes();
 		Element colIndexElem = null;
 		Element colNameElem = null;
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String nodeName = node.getNodeName();
             if(nodeName.equals("column"))
 			{
 				Element column = (Element) node;
 				String indexColName = column.getAttribute("name");
 				if(column.getAttribute("unique").equals("yes"))
 				{
 					colIndexElem = tableDoc.createElement("index");
 					colIndexElem.setAttribute("name", indexColName + "_unq");
 					colIndexElem.setAttribute("type", "unique");
 					colIndexElem.setAttribute("columns", indexColName);
 					colNameElem = tableDoc.createElement("column");
 					colNameElem.setAttribute("name", indexColName);
 					colIndexElem.appendChild(colNameElem);
 					columnIndexes.add(colIndexElem);
 				}
 				else if(column.getAttribute("indexed").equals("yes"))
 				{
 					colIndexElem = tableDoc.createElement("index");
 					colIndexElem.setAttribute("name", indexColName);
 					colIndexElem.setAttribute("columns", indexColName);
 					colNameElem = tableDoc.createElement("column");
 					colNameElem.setAttribute("name", indexColName);
 					colIndexElem.appendChild(colNameElem);
 					columnIndexes.add(colIndexElem);
 				}
 
 				/* the old Physia schema attributes 'indexgrp' and 'uniquegrp' are no longer supported */
 				if(column.getAttribute("indexgrp").length() > 0)
 					errors.add("The 'indexgrp' attribute is no longer supported in table '"+ tableName +"' column '"+ indexColName +"' (use 'index' tag instead)");
 				if(column.getAttribute("uniquegrp").length() > 0)
 					errors.add("The 'uniquegrp' attribute is no longer supported in table '"+ tableName +"' column '"+ indexColName +"' (use 'index' tag instead)");
 			}
 		    else if(nodeName.equals("index"))
 			{
 				Element index = (Element) node;
 		        inheritNodes(index, indexTypeNodes);
 
 				String indexName = index.getAttribute("name");
 				NodeList columnElems = table.getElementsByTagName("column");
 				int columnsCount = columnElems.getLength();
 
 				String columnsList = index.getAttribute("columns");
 				if(columnsList.length() > 0)
 				{
 					StringTokenizer st = new StringTokenizer(index.getAttribute("columns"), ",");
 					while(st.hasMoreTokens())
 					{
 						String indexColName = st.nextToken().trim();
 						boolean found = false;
 						for(int ic = 0; ic < columnsCount; ic++)
 						{
 							Element columnElem = (Element) columnElems.item(ic);
 							if(columnElem.getAttribute("name").equals(indexColName))
 							{
 								found = true;
 								colNameElem = tableDoc.createElement("column");
 								colNameElem.setAttribute("name", indexColName);
 								index.appendChild(colNameElem);
 							}
 						}
 						if(! found)
 						{
 							errors.add("Column '"+indexColName+"' not found in index '"+ indexName +"' of table '"+ tableName +"'");
 						}
 					}
 				}
 			}
             else if(nodeName.equals("java-dal-accessor"))
 			{
 				Element accessor = (Element) node;
 		        //inheritNodes(accessor, indexTypeNodes);
 
 				String methodName = accessor.getAttribute("name");
                 String type = accessor.getAttribute("type");
                 String connector = accessor.getAttribute("connector");
 				NodeList columnElems = table.getElementsByTagName("column");
 				int columnsCount = columnElems.getLength();
 
 				String columnsList = accessor.getAttribute("columns");
 				if(columnsList.length() > 0)
 				{
 					StringTokenizer st = new StringTokenizer(accessor.getAttribute("columns"), ",");
 					while(st.hasMoreTokens())
 					{
 						String accessorColName = st.nextToken().trim();
 						boolean found = false;
 						for(int ic = 0; ic < columnsCount; ic++)
 						{
 							Element columnElem = (Element) columnElems.item(ic);
 							if(columnElem.getAttribute("name").equals(accessorColName))
 							{
 								found = true;
 								colNameElem = tableDoc.createElement("column");
 								colNameElem.setAttribute("name", accessorColName);
 								accessor.appendChild(colNameElem);
                                 System.out.println("Adding column: " + accessorColName + " " + columnElem.getAttribute("name"));
                                 break;
 							}
 						}
 						if(! found)
 						{
 							errors.add("Column '"+accessorColName+"' not found in table '"+ tableName +"'");
 						}
 					}
 				}
 			}
 
         }
 
 		if(columnIndexes.size() > 0)
 		{
 			for(Iterator i = columnIndexes.iterator(); i.hasNext(); )
 			{
 				table.appendChild((Element) i.next());
 			}
 		}
 	}
 
     public void fixupTableElement(Element table)
     {
         Hashtable params = new Hashtable();
         String tableName = table.getAttribute("name");
         tableParams.put(tableName, params);
         inheritNodes(table, tableTypeNodes);
 
         NodeList columns = table.getChildNodes();
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
             if(node.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             Element childElem = (Element) node;
             String nodeName = node.getNodeName();
 
             if(nodeName.equals("column"))
                fixupColumnElement(childElem, false);
             else if (nodeName.equals("param"))
                 params.put(childElem.getAttribute("name"), childElem.getFirstChild().getNodeValue());
         }
 
 		Node[][] replaceCols = new Node[columns.getLength()][];
 		int compIndex = 0;
 
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
             if(node.getNodeName().equals("column"))
 			{
 				NodeList composites = ((Element) node).getElementsByTagName("composite");
 				for(int cc = 0; cc < composites.getLength(); cc++)
 				{
 					DocumentFragment compCols = getCompositeColumns((Element) node, (Element) composites.item(cc));
 					replaceCols[compIndex] = new Node[] { node, compCols };
 					compIndex++;
 				}
 			}
         }
 
 		for(int j = 0; j < compIndex; j++)
 		{
 			table.insertBefore(replaceCols[j][1], replaceCols[j][0]);
 			table.removeChild(replaceCols[j][0]);
 		}
 
         params.put("tbl_name", table.getAttribute("name"));
         params.put("tbl_Name", ucfirst(table.getAttribute("name")));
         params.put("tbl_abbrev", table.getAttribute("abbrev"));
 
         Element tableParentElem = (Element) table.getParentNode();
         if(tableParentElem != null && tableParentElem.getNodeName().equals("column"))
         {
             Element parentColumn = tableParentElem;
             Element parentColTable = (Element) parentColumn.getParentNode();
 
             params.put("parenttbl_name", parentColTable.getAttribute("name"));
             params.put("parenttbl_Name", ucfirst(parentColTable.getAttribute("name")));
             params.put("parenttbl_abbrev", parentColTable.getAttribute("abbrev"));
 
             String primaryKey = getPrimaryKey(parentColTable);
             if(primaryKey != null)
                 params.put("parenttbl_prikey", primaryKey);
 
             params.put("parentcol_name", parentColumn.getAttribute("name"));
             params.put("parentcol_Name", ucfirst(parentColumn.getAttribute("name")));
             params.put("parentcol_abbrev", parentColumn.getAttribute("abbrev"));
             params.put("parentcol_short", parentColumn.getAttribute("abbrev"));
             params.put("parentcol_Short", parentColumn.getAttribute("abbrev"));
         }
 
         replaceNodeMacros((Node) table, replaceMacrosInTableNodes, params);
 
         boolean isEnum = false;
         boolean isLookup = false;
 
 		Element lastColumnSeen = null;
 		int lastEnumId = 0;
         columns = table.getChildNodes();
         for(int c = 0; c < columns.getLength(); c++)
         {
             Node node = columns.item(c);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String nodeName = node.getNodeName();
             if(nodeName.equals("column"))
 			{
                 replaceNodeMacros(node, replaceMacrosInColumnNodes, params);
 				lastColumnSeen = (Element) node;
 			}
             else if(nodeName.equals("index"))
                 replaceNodeMacros(node, replaceMacrosInTableNodes, params);
 			else if(nodeName.equals("parent"))
 			{
 				if(table.getAttribute("parent").length() == 0)
 					table.setAttribute("parent", node.getFirstChild().getNodeValue());
 			}
             else if (nodeName.equals("extends"))
             {
                 String tableType = node.getFirstChild().getNodeValue();
                 if(tableType.equals("Audit"))
                     table.setAttribute("audit", "yes");
                 else if(tableType.equals("Enumeration"))
                     isEnum = true;
                 else if(tableType.equals("Lookup"))
                     isLookup = true;
             }
 			else if (nodeName.equals("enum"))
 			{
 				Element enumElem = (Element) node;
                 String constant_name = enumElem.getAttribute("java-constant");
                 if (constant_name != null && constant_name.length() > 0)
                     enumElem.setAttribute("java-constant-name", constant_name);
                 else
                     enumElem.setAttribute("java-constant-name", xmlTextToJavaConstant(enumElem.getFirstChild().getNodeValue()));
 				String enumId = enumElem.getAttribute("id");
 				if(enumId.length() == 0)
 				{
 					enumElem.setAttribute("id", Integer.toString(lastEnumId));
 					lastEnumId++;
 				}
 				else
 				{
 					try	{ lastEnumId = Integer.parseInt(enumId) + 1; }
 					catch(NumberFormatException e)
 					{
 						addError("Enum id '"+ enumId +"' in table '"+ tableName +"' is invalid.");
 					}
 				}
 			}
         }
 
 		if(lastColumnSeen != null) lastColumnSeen.setAttribute("is-last", "yes");
 		if(table.getAttribute("abbrev").length() == 0)
 			table.setAttribute("abbrev", tableName);
 
         if(isEnum && ! isLookup)
             table.setAttribute("is-enum", "yes");
         else if(isLookup)
             table.setAttribute("is-lookup", "yes");
 
 		resolveIndexes(table);
     }
 
 	class Reference
 	{
 		String reference;
 		String tableName;
 		String columnName;
 		int type;
 
 		Reference(String ref, int refType)
 		{
 			reference = ref;
 			type = refType;
 			StringTokenizer st = new StringTokenizer(ref, ".");
 			tableName = st.nextToken();
 			if(st.hasMoreTokens())
 				columnName = st.nextToken();
 			else
 				columnName = "id";
 		}
 	}
 
 	public Reference getRefInfo(Element elem, String attrName, int refType)
 	{
 		String attrValue = elem.getAttribute(attrName);
 		if(attrValue.length() == 0)
 			return null;
 		else
 			return new Reference(attrValue, refType);
 	}
 
     public Reference getColumnRefInfo(Element tableElem, Element column)
     {
         Reference refInfo = getRefInfo(column, "lookupref", REFTYPE_LOOKUP);
         if(refInfo == null)
         {
             refInfo = getRefInfo(column, "parentref", REFTYPE_PARENT);
             if(refInfo != null)
                 tableElem.setAttribute("parent", refInfo.tableName);
             else
             {
                 refInfo = getRefInfo(column, "selfref", REFTYPE_SELF);
                 if(refInfo != null)
                     refInfo.tableName = tableElem.getAttribute("name");
                 else
                     refInfo = getRefInfo(column, "usetype", REFTYPE_USETYPE);
             }
         }
 
         return refInfo;
     }
 
     public void resolveColumnReferences(Element tableElem, Element column)
     {
         // if references are already resolved, do nothing
         if(column.getAttribute("reftype").length() > 0)
             return;
 
         Reference refInfo = getColumnRefInfo(tableElem, column);
         if(refInfo == null)
             return;
 
         Element refTable = (Element) tableNodes.get(refInfo.tableName.toUpperCase());
         if(refTable == null)
         {
             errors.add("Table '"+ refInfo.tableName +"' not found for "+ REFTYPE_NAMES[refInfo.type] +" reference '"+ refInfo.reference +"' (in table '"+ tableElem.getAttribute("name") +"' column '"+ column.getAttribute("name") +"')");
             return;
         }
 
         column.setAttribute("reftype", REFTYPE_NAMES[refInfo.type]);
         column.setAttribute("reftbl", refTable.getAttribute("name"));
 
         // try and find the column that matches our reference's name
         NodeList refTableColumns = refTable.getChildNodes();
         for(int rc = 0; rc < refTableColumns.getLength(); rc++)
         {
             Node refTableColumnNode = refTableColumns.item(rc);
             if(refTableColumnNode.getNodeName().equals("column"))
             {
                 Element refColumnElem = (Element) refTableColumnNode;
                 String refColumnName = refColumnElem.getAttribute("name");
                 if(refColumnName.equalsIgnoreCase(refInfo.columnName))
                 {
                     // if the column we're pointing is a reference too, then resolve it first
                     resolveColumnReferences(refTable, refColumnElem);
 
                     column.setAttribute("refcol", refColumnName);
 
                     String copyType = findElementOrAttrValue(refColumnElem, "copytype");
                     if(copyType != null && copyType.length() > 0)
                         column.setAttribute("type", copyType);
                     else
                         column.setAttribute("type", refColumnElem.getAttribute("type"));
 
                    String sizeAttr = findElementOrAttrValue(refColumnElem, "size");
                    if(sizeAttr != null && sizeAttr.length() > 0)
                           column.setAttribute("size", sizeAttr);

                     if(column.getAttribute("type").length() == 0)
                         errors.add("Column '" + refInfo.columnName + "' has no type in Table '"+ refInfo.tableName +"' for "+ REFTYPE_NAMES[refInfo.type] +" reference '"+ refInfo.reference +"' (in table '"+ tableElem.getAttribute("name") +"' column '"+ column.getAttribute("name") +"')");
 
                    fixupColumnElement(column, true);
 
                     Element refByElem = xmlDoc.createElement("referenced-by");
                     refColumnElem.appendChild(refByElem);
                     refByElem.setAttribute("type", REFTYPE_NAMES[refInfo.type]);
                     refByElem.setAttribute("table", tableElem.getAttribute("name"));
                     refByElem.setAttribute("column", column.getAttribute("name"));
 
                     // we found our colum in the ref table, no need to do anything else
                     return;
                 }
             }
         }
 
         // if we get here, it means our column was not found
         errors.add("Column '" + refInfo.columnName + "' not found in Table '"+ refInfo.tableName +"' for "+ REFTYPE_NAMES[refInfo.type] +" reference '"+ refInfo.reference +"' (in table '"+ tableElem.getAttribute("name") +"' column '"+ column.getAttribute("name") +"')");
     }
 
     public void resolveTableReferences(Element tableElem)
     {
         NodeList children = tableElem.getChildNodes();
 
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeName().equals("column"))
             {
                 Element column = (Element) node;
                 resolveColumnReferences(tableElem, column);
             }
         }
     }
 
 	public void resolveReferences()
 	{
 		Iterator i = tableNodes.values().iterator();
 		while(i.hasNext())
 		{
 			Element tableElem = (Element) i.next();
             resolveTableReferences(tableElem);
 		}
 	}
 
     public void createAuditTables()
     {
         Element[] auditTables = new Element[tableNodes.size()];
         int auditTablesCount = 0;
 
         Element docElem = xmlDoc.getDocumentElement();
 		Iterator i = tableNodes.values().iterator();
 		while(i.hasNext())
         {
             Element mainTable = (Element) i.next();
             if(mainTable.getAttribute("audit").equals("yes"))
             {
                 String mainTableName = mainTable.getAttribute("name");
                 Element auditTable = xmlDoc.createElement("table");
                 auditTable.setAttribute("name", mainTableName + "_AUD");
                 auditTable.setAttribute("parent", mainTableName);
 				auditTable.setAttribute("is-audit", "yes");
 
                 NodeList copyColumns = mainTable.getChildNodes();
                 for(int n = 0; n < copyColumns.getLength(); n++)
                 {
                     Node node = copyColumns.item(n);
                     if(! node.getNodeName().equals("column"))
                         continue;
 
                     Element mainColumn = (Element) node;
                     String mainColumnName = mainColumn.getAttribute("name");
 
                     Element auditColumn = xmlDoc.createElement("column");
                     auditColumn.setAttribute("name", mainColumnName);
                     auditColumn.setAttribute("usetype", mainTableName + "." + mainColumnName);
                     auditColumn.setAttribute("descr", mainColumn.getAttribute("descr"));
 
                     auditTable.appendChild(auditColumn);
                 }
 
                 docElem.appendChild(auditTable);
                 auditTables[auditTablesCount] = auditTable;
                 auditTablesCount++;
             }
         }
 
         for(int t = 0; t < auditTablesCount; t++)
         {
             Element auditTable = auditTables[t];
             resolveTableReferences(auditTable);
             docElem.appendChild(auditTable);
             tableNodes.put(auditTable.getAttribute("name").toUpperCase(), auditTable);
         }
     }
 
 	public void addTableStructure(Element structure, Element table, int level)
 	{
 		Element tableStruct = xmlDoc.createElement("table");
 		String tableName = table.getAttribute("name");
 		tableStruct.setAttribute("name", tableName);
 		tableStruct.setAttribute("level", String.valueOf(level));
 		structure.appendChild(tableStruct);
 
         Element connector = getParentConnectorColumn(table);
         if(connector != null)
         {
             tableStruct.setAttribute("parent-col", connector.getAttribute("refcol"));
             tableStruct.setAttribute("child-col", connector.getAttribute("name"));
         }
 
 		Iterator i = tableNodes.values().iterator();
 		while(i.hasNext())
 		{
 			Element childTable = (Element) i.next();
 		    if(childTable.getAttribute("parent").equals(tableName))
 			{
 				addTableStructure(tableStruct, childTable, level+1);
 
                 Element childTableElem = xmlDoc.createElement("child-table");
                 childTableElem.setAttribute("name", childTable.getAttribute("name"));
                 connector = getParentConnectorColumn(childTable);
                 if(connector != null)
                 {
                     childTableElem.setAttribute("parent-col", connector.getAttribute("refcol"));
                     childTableElem.setAttribute("child-col", connector.getAttribute("name"));
                 }
                 table.appendChild(childTableElem);
 			}
 		}
 	}
 
 	public void createStructure()
 	{
 		Element structure = xmlDoc.createElement("table-structure");
 		xmlDoc.getDocumentElement().appendChild(structure);
 
 		Iterator i = tableNodes.values().iterator();
 		while(i.hasNext())
 		{
 			Element table = (Element) i.next();
 		    if(table.getAttribute("parent").length() == 0)
 			{
 				addTableStructure(structure, table, 0);
 			}
 		}
 	}
 
     public void keepOnlyLastElement(Element parent, String childName)
     {
         NodeList elems = parent.getElementsByTagName(childName);
         if(elems.getLength() > 1)
         {
             for(int i = 0; i < elems.getLength() - 1; i++)
                 parent.removeChild(elems.item(i));
         }
     }
 
     public void doDataTypeInheritance()
     {
         for(Iterator i = dataTypeNodes.values().iterator(); i.hasNext(); )
         {
             Element elem = (Element) i.next();
             inheritNodes(elem, dataTypeNodes);
         }
 
         for(Iterator i = dataTypeNodes.values().iterator(); i.hasNext(); )
         {
             Element dataType = (Element) i.next();
             keepOnlyLastElement(dataType, "size");
             keepOnlyLastElement(dataType, "default");
             keepOnlyLastElement(dataType, "java-class");
             keepOnlyLastElement(dataType, "java-type");
         }
     }
 
 	public void catalogNodes()
 	{
 		dataTypeNodes.clear();
 		indexTypeNodes.clear();
 		tableTypeNodes.clear();
 		tableNodes.clear();
 		columnTableNodes.clear();
 		tableParams.clear();
 
         if(xmlDoc == null)
             return;
 
 		NodeList children = xmlDoc.getDocumentElement().getChildNodes();
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			Element element = (Element) node;
 			String nodeName = node.getNodeName();
 
 			if(nodeName.equals("datatype"))
             {
 				dataTypeNodes.put(element.getAttribute("name"), node);
 
                 // sqlWriteFmt was used in the perl version, but JDBC doesn't
                 // require it so we'll remove the sqlWriteFmt to help save space
                 NodeList sqlWriteFmt = ((Element) node).getElementsByTagName("sqlwritefmt");
                 if(sqlWriteFmt.getLength() > 0)
                     node.removeChild(sqlWriteFmt.item(0));
             }
 			else if(nodeName.equals("tabletype"))
 				tableTypeNodes.put(element.getAttribute("name"), node);
 			else if(nodeName.equals("indextype"))
 				indexTypeNodes.put(element.getAttribute("name"), node);
 			else if(nodeName.equals("table"))
 				tableNodes.put(element.getAttribute("name").toUpperCase(), node);
 		}
 
 		Iterator i = tableNodes.values().iterator();
 		while(i.hasNext())
 			fixupTableElement((Element) i.next());
 
 		/*
 		 * at this time, all the inheritance and macro replacements should
 		 * be complete, so we go ahead an "pull out" the tables that should
 		 * be auto-generated (from columns) and resolve all references
 		 */
 
 		Node rootNode = xmlDoc.getDocumentElement();
 		i = columnTableNodes.iterator();
 		while(i.hasNext())
 		{
 			Element columnTableElem = (Element) i.next();
 			tableNodes.put(columnTableElem.getAttribute("name").toUpperCase(), columnTableElem);
 			rootNode.appendChild(columnTableElem);
 		}
 
 		resolveReferences();
         createAuditTables();
 		createStructure();
 
         // we do this at the very last so that duplicate inheritance doesn't occur in columns -- also,
         // we want to be sure to do it so that data types that are generating Java can be "extended"
         doDataTypeInheritance();
 
 		addMetaInformation();
 	}
 
 	/*------------------------------------------------------------------------*/
 
 	public void initializeConnection(Connection conn)
 	{
 		/*
 		   ORACLE connections don't do remarks reporting automatically, they have
 		   to be turned-on separately. First we check to see if it's a pooled
 		   connection (like Resin's connection pooling) which actually keeps a
 		   handle to a "real" connection. We use reflection just in case the
 		   connection isn't an ORACLE connection (will fail gracefully).
 		*/
 
 		Connection realConn = conn;
 		try
 		{
 			Method getConnection = conn.getClass().getMethod("getConnection", null);
 			realConn = (Connection) getConnection.invoke(conn, null);
 		}
 		catch(Exception e)
 		{
 			// means that the conn object is the real connection
 		}
 
 		try
 		{
 			Method remarksReporting = realConn.getClass().getMethod("setRemarksReporting", new Class[] { boolean.class } );
 			remarksReporting.invoke(realConn, new Object[] { new Boolean(true) });
 		}
 		catch(Exception e)
 		{
 		}
 	}
 
 	public void setAttribute(Element elem, String name, String value)
 	{
 		if(value != null && value.length() > 0)
 			elem.setAttribute(name, value);
 	}
 
 	public void loadDocument(Connection conn, String catalog, String schemaPattern) throws ParserConfigurationException, SQLException
 	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder parser = factory.newDocumentBuilder();
 		xmlDoc = parser.newDocument();
 
 		Element root = xmlDoc.createElement("schema");
 		root.setAttribute("name", "generated");
 		xmlDoc.appendChild(root);
 		//initializeConnection(conn);
 
 		DatabaseMetaData dbmd = conn.getMetaData();
 		Map types = new HashMap();
 		ResultSet typesRS = dbmd.getTypeInfo();
 		while(typesRS.next())
 		{
 			types.put(typesRS.getString(2), typesRS.getString(1));
 		}
 		typesRS.close();
 
 		ResultSet tables = dbmd.getTables(catalog, schemaPattern, null, new String[] { "TABLE" });
 		while(tables.next())
 		{
 			/* make the table name title cased (cap each letter after _) */
 			String tableNameOrig = tables.getString(3);
 			StringBuffer tableNameBuf = new StringBuffer(tableNameOrig.toLowerCase());
 			boolean capNext = false;
 			for(int i = 0; i < tableNameBuf.length(); i++)
 			{
 				if(tableNameBuf.charAt(i) == '_')
 					capNext = true;
 				else
 				{
 					if(i == 0 || capNext)
 					{
 						tableNameBuf.setCharAt(i, Character.toUpperCase(tableNameBuf.charAt(i)));
 						capNext = false;
 					}
 				}
 			}
 
 			String tableName = tableNameBuf.toString();
 			Element table = xmlDoc.createElement("table");
 			table.setAttribute("name", tableName);
 			root.appendChild(table);
 
 			Map primaryKeys = new HashMap();
 			try
 			{
 				ResultSet pkRS = dbmd.getPrimaryKeys(null, null, tableNameOrig);
 				while(pkRS.next())
 				{
 					primaryKeys.put(pkRS.getString(4), pkRS.getString(5));
 				}
 				pkRS.close();
 			}
 			catch(Exception e)
 			{
 				// driver may not support this function
 			}
 
 			ResultSet columns = dbmd.getColumns(null, null, tableNameOrig, null);
 			while(columns.next())
 			{
 				String columnNameOrig = columns.getString(4);
 				String columnName = columnNameOrig.toLowerCase();
 				Element column = xmlDoc.createElement("column");
 				try
 				{
 					setAttribute(column, "name", columnName);
 					setAttribute(column, "type", columns.getString(6));
 					if(primaryKeys.containsKey(columnNameOrig))
 						setAttribute(column, "primarykey", "yes");
 
 					String sqlDefn = columns.getString(5);
 					String size = columns.getString(7);
 
 					sqlDefn = (sqlDefn == null ? columns.getString(6) : (String) types.get(sqlDefn));
 					if(sqlDefn == null) sqlDefn = columns.getString(6);
 
 					setAttribute(column, "sqldefn", sqlDefn + "(" + size + ")");
 					setAttribute(column, "descr", columns.getString(12));
 					setAttribute(column, "default", columns.getString(13));
 				}
 				catch(Exception e)
 				{
 				}
 
 				table.appendChild(column);
 			}
 		    columns.close();
 		}
 		tables.close();
 	}
 
     static public class SqlDdlGenerator
     {
         private String ddlGeneratorStyleSheet;
         private String destFile;
 
         public SqlDdlGenerator(String styleSheet, String destFile)
         {
             setDdlGeneratorStyleSheet(styleSheet);
             setDestFile(destFile);
         }
 
         public String getDdlGeneratorStyleSheet()
         {
             return ddlGeneratorStyleSheet;
         }
 
         public void setDdlGeneratorStyleSheet(String ddlGeneratorStyleSheet)
         {
             this.ddlGeneratorStyleSheet = ddlGeneratorStyleSheet;
         }
 
         public String getDestFile()
         {
             return destFile;
         }
 
         public void setDestFile(String destFile)
         {
             this.destFile = destFile;
         }
 
         public void generate(SchemaDocument schemaDoc) throws TransformerConfigurationException, TransformerException
         {
             TransformerFactory tFactory = TransformerFactory.newInstance();
 			Transformer transformer = tFactory.newTransformer(new StreamSource(ddlGeneratorStyleSheet));
 
 			transformer.transform
 				(new javax.xml.transform.dom.DOMSource(schemaDoc.getDocument()),
 				 new javax.xml.transform.stream.StreamResult(destFile));
         }
     }
 
     static public class ObjectRelationalGenerator
     {
         private String destRoot;
         private String dataTypesPkg;
         private String tableTypesPkg;
         private String tablesPkg;
         private String domainsPkg;
         private String rowsPkg;
         private String rowsListPkg;
         private String schemaPkg;
         private String schemaClassName;
 
         private String dataTypesGeneratorStyleSheet;
         private String tableTypesGeneratorStyleSheet;
         private String tablesGeneratorStyleSheet;
         private String domainsGeneratorStyleSheet;
         private String rowsGeneratorStyleSheet;
         private String rowsListGeneratorStyleSheet;
         private String schemaGeneratorStyleSheet;
 
         private int dataTypesGeneratedCount;
         private int tableTypesGeneratedCount;
         private int tablesGeneratedCount;
 
         public ObjectRelationalGenerator()
         {
         }
 
         public String getDestRoot() { return destRoot; }
         public void setDestRoot(String destRoot) { this.destRoot = destRoot; }
 
         public String getDataTypesPkg() { return dataTypesPkg; }
         public void setDataTypesPkg(String dataTypesPkg) { this.dataTypesPkg = dataTypesPkg; }
 
         public String getTableTypesPkg() { return tableTypesPkg; }
         public void setTableTypesPkg(String tableTypesPkg) { this.tableTypesPkg = tableTypesPkg; }
 
         public String getTablesPkg() { return tablesPkg; }
         public void setTablesPkg(String tablesPkg) { this.tablesPkg = tablesPkg; }
 
         public String getDomainsPkg() { return domainsPkg; }
         public void setDomainsPkg(String domainsPkg) { this.domainsPkg = domainsPkg; }
 
         public String getRowsPkg() { return rowsPkg; }
         public void setRowsPkg(String rowsPkg) { this.rowsPkg = rowsPkg; }
 
         public String getRowsListPkg() { return rowsListPkg; }
         public void setRowsListPkg(String rowsListPkg) { this.rowsListPkg = rowsListPkg; }
 
         public String getSchemaPkg() { return schemaPkg; }
         public void setSchemaPkg(String schemaPkg) { this.schemaPkg = schemaPkg; }
 
         public String getSchemaClassName() { return schemaClassName; }
         public void setSchemaClassName(String schemaClassName) { this.schemaClassName = schemaClassName; }
 
         public String getDataTypesGeneratorStyleSheet() { return dataTypesGeneratorStyleSheet; }
         public void setDataTypesGeneratorStyleSheet(String dataTypesGeneratorStyleSheet) { this.dataTypesGeneratorStyleSheet = dataTypesGeneratorStyleSheet; }
 
         public String getTableTypesGeneratorStyleSheet() { return tableTypesGeneratorStyleSheet; }
         public void setTableTypesGeneratorStyleSheet(String tableTypesGeneratorStyleSheet) { this.tableTypesGeneratorStyleSheet = tableTypesGeneratorStyleSheet; }
 
         public String getTablesGeneratorStyleSheet() { return tablesGeneratorStyleSheet; }
         public void setTablesGeneratorStyleSheet(String tablesGeneratorStyleSheet) { this.tablesGeneratorStyleSheet = tablesGeneratorStyleSheet; }
 
         public String getDomainsGeneratorStyleSheet() { return domainsGeneratorStyleSheet; }
         public void setDomainsGeneratorStyleSheet(String domainsGeneratorStyleSheet) { this.domainsGeneratorStyleSheet = domainsGeneratorStyleSheet; }
 
         public String getRowsGeneratorStyleSheet() { return rowsGeneratorStyleSheet; }
         public void setRowsGeneratorStyleSheet(String rowsGeneratorStyleSheet) { this.rowsGeneratorStyleSheet = rowsGeneratorStyleSheet; }
 
         public String getRowsListGeneratorStyleSheet() { return rowsListGeneratorStyleSheet; }
         public void setRowsListGeneratorStyleSheet(String rowsListGeneratorStyleSheet) { this.rowsListGeneratorStyleSheet = rowsListGeneratorStyleSheet; }
 
         public String getSchemaGeneratorStyleSheet() { return schemaGeneratorStyleSheet; }
         public void setSchemaGeneratorStyleSheet(String schemaGeneratorStyleSheet) { this.schemaGeneratorStyleSheet = schemaGeneratorStyleSheet; }
 
         public int getDataTypesGeneratedCount() { return dataTypesGeneratedCount; }
         public int getTableTypesGeneratedCount() { return tableTypesGeneratedCount; }
         public int getTablesGeneratedCount() { return tablesGeneratedCount; }
 
         public void createDataTypesClasses(SchemaDocument schemaDoc, Map dataTypesClassMap, Map  tableTypesClassMap) throws TransformerConfigurationException, TransformerException
         {
             dataTypesGeneratedCount = 0;
             String dataTypesPkgDirName = dataTypesPkg.replace('.', '/');
             File dataTypesDir = new File(destRoot + "/" + dataTypesPkgDirName);
             dataTypesDir.mkdirs();
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer dataTypesTransformer = tFactory.newTransformer(new StreamSource(dataTypesGeneratorStyleSheet));
             dataTypesTransformer.setParameter("package-name", dataTypesPkg);
             Map dataTypes = schemaDoc.getDataTypes();
 
             DATATYPES:
             for(Iterator i = dataTypes.values().iterator(); i.hasNext(); )
             {
                 Element dataTypeElem = (Element) i.next();
                 String dataTypeName = XmlSource.xmlTextToJavaIdentifier(dataTypeElem.getAttribute("name"), true) + "Column";
                 String dataTypeFile = dataTypesDir.getAbsolutePath() + "/" + dataTypeName + ".java";
                 String javaTypeInitCap = null;
                 dataTypesClassMap.put(dataTypeElem.getAttribute("name"), dataTypesPkg + "." + dataTypeName);
 
                 NodeList children = dataTypeElem.getChildNodes();
                 for(int c = 0; c < children.getLength(); c++)
                 {
                     Node child = children.item(c);
                     if(child.getNodeType() != Node.ELEMENT_NODE)
                         continue;
 
                     String childName = child.getNodeName();
                     if("composite".equals(childName))
                     {
                         // composites will already have been "expanded" by the SchemaDocument so we don't create any classes
                         if(dataTypeElem.getElementsByTagName("composite").getLength() > 0)
                             continue DATATYPES;
                     }
                     else if("java-type".equals(childName))
                         javaTypeInitCap = XmlSource.xmlTextToJavaIdentifier(child.getFirstChild().getNodeValue(), true);
                 }
 
                 dataTypesTransformer.setParameter("data-type-name", dataTypeName);
                 if(javaTypeInitCap != null)
                     dataTypesTransformer.setParameter("java-type-init-cap", javaTypeInitCap);
 
                 dataTypesTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(dataTypeElem),
                      new javax.xml.transform.stream.StreamResult(dataTypeFile));
 
                 dataTypesGeneratedCount++;
             }
         }
 
         public void createTableTypesClasses(SchemaDocument schemaDoc, Map dataTypesClassMap, Map  tableTypesClassMap) throws TransformerConfigurationException, TransformerException
         {
             String tableTypesPkgDirName = tableTypesPkg.replace('.', '/');
             File tableTypesDir = new File(destRoot + "/" + tableTypesPkgDirName);
             tableTypesDir.mkdirs();
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer tableTypesTransformer = tFactory.newTransformer(new StreamSource(tableTypesGeneratorStyleSheet));
 
             Map tableTypes = schemaDoc.getTableTypes();
 
             for(Iterator i = tableTypes.values().iterator(); i.hasNext(); )
             {
                 Element tableTypeElem = (Element) i.next();
                 String tableTypeClassName = XmlSource.xmlTextToJavaIdentifier(tableTypeElem.getAttribute("name"), true);
                 String tableTypeFile = tableTypesDir.getAbsolutePath() + "/" + tableTypeClassName + ".java";
                 tableTypesClassMap.put(tableTypeElem.getAttribute("name"), tableTypesPkg + "." + tableTypeClassName);
 
                 NodeList children = tableTypeElem.getChildNodes();
                 for(int c = 0; c < children.getLength(); c++)
                 {
                     Node child = children.item(c);
                     if(child.getNodeType() != Node.ELEMENT_NODE)
                         continue;
 
                     String childName = child.getNodeName();
                     if("column".equals(childName))
                     {
                         Element columnElem = (Element) child;
                         columnElem.setAttribute("_gen-member-name", XmlSource.xmlTextToJavaIdentifier(columnElem.getAttribute("name"), false));
                         columnElem.setAttribute("_gen-method-name", XmlSource.xmlTextToJavaIdentifier(columnElem.getAttribute("name"), true));
                         columnElem.setAttribute("_gen-data-type-class", (String) dataTypesClassMap.get(columnElem.getAttribute("type")));
 
                         NodeList jtnl = columnElem.getElementsByTagName("java-type");
                         if(jtnl.getLength() > 0)
                             columnElem.setAttribute("_gen-java-type-init-cap", XmlSource.xmlTextToJavaIdentifier(jtnl.item(0).getFirstChild().getNodeValue(), true));
                     }
                 }
 
                 tableTypesTransformer.setParameter("package-name", tableTypesPkg);
                 tableTypesTransformer.setParameter("table-type-name", tableTypeClassName);
                 tableTypesTransformer.setParameter("table-type-class-name", tableTypesPkg + "." + tableTypeClassName);
                 tableTypesTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(tableTypeElem), new javax.xml.transform.stream.StreamResult(tableTypeFile));
                 tableTypesGeneratedCount++;
             }
         }
 
         public void createTablesClasses(SchemaDocument schemaDoc, Map dataTypesClassMap, Map  tableTypesClassMap) throws TransformerConfigurationException, TransformerException
         {
             String tablesPkgDirName = tablesPkg.replace('.', '/');
             File tablesDir = new File(destRoot + "/" + tablesPkgDirName);
             tablesDir.mkdirs();
 
             String domainsPkgDirName = domainsPkg.replace('.', '/');
             File domainsDir = new File(destRoot + "/" + domainsPkgDirName);
             domainsDir.mkdirs();
 
             String rowsPkgDirName = rowsPkg.replace('.', '/');
             File rowsDir = new File(destRoot + "/" + rowsPkgDirName);
             rowsDir.mkdirs();
 
             String rowsListPkgDirName = rowsListPkg.replace('.', '/');
             File rowsListDir = new File(destRoot + "/" + rowsListPkgDirName);
             rowsListDir.mkdirs();
 
             String completeSchemaClassName = schemaPkg + "." + schemaClassName;
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer tablesTransformer = tFactory.newTransformer(new StreamSource(tablesGeneratorStyleSheet));
             tablesTransformer.setParameter("schema-class-name", completeSchemaClassName);
             tablesTransformer.setParameter("package-name", tablesPkg);
             Transformer domainsTransformer = tFactory.newTransformer(new StreamSource(domainsGeneratorStyleSheet));
             domainsTransformer.setParameter("schema-class-name", completeSchemaClassName);
             domainsTransformer.setParameter("package-name", domainsPkg);
             Transformer rowsTransformer = tFactory.newTransformer(new StreamSource(rowsGeneratorStyleSheet));
             rowsTransformer.setParameter("schema-class-name", completeSchemaClassName);
             rowsTransformer.setParameter("package-name", rowsPkg);
             Transformer rowsListTransformer = tFactory.newTransformer(new StreamSource(rowsListGeneratorStyleSheet));
             rowsListTransformer.setParameter("schema-class-name", completeSchemaClassName);
             rowsListTransformer.setParameter("package-name", rowsListPkg);
 
             Map tables = schemaDoc.getTables();
             for(Iterator i = tables.values().iterator(); i.hasNext(); )
             {
                 Element tableElem = (Element) i.next();
 
                 String tableClassName = XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true) + "Table";
                 String tableFile = tablesDir.getAbsolutePath() + "/" + tableClassName + ".java";
                 String domainName = XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true);
                 String domainFile = domainsDir.getAbsolutePath() + "/" + domainName + ".java";
                 String rowName = XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true) + "Row";
                 String rowFile = rowsDir.getAbsolutePath() + "/" + rowName + ".java";
                 String rowListName = XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true) + "Rows";
                 String rowListFile = rowsListDir.getAbsolutePath() + "/" + rowListName + ".java";
                 StringBuffer tableTypesList = new StringBuffer();
 
                 NodeList children = tableElem.getChildNodes();
                 for(int c = 0; c < children.getLength(); c++)
                 {
                     Node child = children.item(c);
                     if(child.getNodeType() != Node.ELEMENT_NODE)
                         continue;
 
                     String childName = child.getNodeName();
                     if("column".equals(childName))
                     {
                         Element columnElem = (Element) child;
                         String columnName = columnElem.getAttribute("name");
                         columnElem.setAttribute("_gen-member-name", xmlTextToJavaIdentifier(columnName, false));
                         columnElem.setAttribute("_gen-method-name", xmlTextToJavaIdentifier(columnName, true));
                         columnElem.setAttribute("_gen-constant-name", columnName.toUpperCase());
                         columnElem.setAttribute("_gen-node-name", xmlTextToNodeName(columnName));
                         columnElem.setAttribute("_gen-data-type-class", (String) dataTypesClassMap.get(columnElem.getAttribute("type")));
 
                         NodeList jtnl = columnElem.getElementsByTagName("java-type");
                         if(jtnl.getLength() > 0)
                             columnElem.setAttribute("_gen-java-type-init-cap", xmlTextToJavaIdentifier(jtnl.item(0).getFirstChild().getNodeValue(), true));
                     }
                     else if("extends".equals(childName))
                     {
                         if(tableTypesList.length() > 0)
                             tableTypesList.append(", ");
                         tableTypesList.append((String) tableTypesClassMap.get(child.getFirstChild().getNodeValue()));
                     }
                 }
 
                 if(tableTypesList.length() > 0)
                     tableElem.setAttribute("_implements-table-types", tableTypesList.toString());
                 tableElem.setAttribute("_gen-domain-name", domainName);
                 tableElem.setAttribute("_gen-domain-class-name", domainsPkg + "." + domainName);
                 tableElem.setAttribute("_gen-domain-member-name", XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), false));
                 tableElem.setAttribute("_gen-domain-method-name", XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true));
                 tableElem.setAttribute("_gen-table-name", tableClassName);
                 tableElem.setAttribute("_gen-table-class-name", tablesPkg + "." + tableClassName);
                 tableElem.setAttribute("_gen-table-member-name", XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), false));
                 tableElem.setAttribute("_gen-table-method-name", XmlSource.xmlTextToJavaIdentifier(tableElem.getAttribute("name"), true));
                 tableElem.setAttribute("_gen-row-name", rowName);
                 tableElem.setAttribute("_gen-row-class-name", rowsPkg + "." + rowName);
                 tableElem.setAttribute("_gen-rows-name", rowListName);
                 tableElem.setAttribute("_gen-rows-member-name", xmlTextToJavaIdentifier(rowListName, false));
                 tableElem.setAttribute("_gen-rows-class-name", rowsListPkg + "." + rowListName);
             }
 
             for(Iterator i = tables.values().iterator(); i.hasNext(); )
             {
                 Element tableElem = (Element) i.next();
 
                 NodeList children = tableElem.getChildNodes();
                 for(int c = 0; c < children.getLength(); c++)
                 {
                     Node child = children.item(c);
                     if(child.getNodeType() != Node.ELEMENT_NODE)
                         continue;
 
                     String childName = child.getNodeName();
                     if("column".equals(childName))
                     {
                         Element columnElem = (Element) child;
                         String refTableName = columnElem.getAttribute("reftbl");
                         if(refTableName.length() > 0l)
                         {
                             Element refTableElem = (Element) schemaDoc.getTables().get(refTableName.toUpperCase());
                             if(refTableElem != null)
                             {
                                 if("yes".equals(refTableElem.getAttribute("is-enum")))
                                     columnElem.setAttribute("_gen-ref-table-is-enum", "yes");
                                 columnElem.setAttribute("_gen-ref-table-name", refTableElem.getAttribute("_gen-table-name"));
                                 columnElem.setAttribute("_gen-ref-table-class-name", refTableElem.getAttribute("_gen-table-class-name"));
                             }
                         }
                     }
                     else if("child-table".equals(childName))
                     {
                         Element childTableRefElem = (Element) child;
                         Element childTableElem = (Element) schemaDoc.getTables().get(childTableRefElem.getAttribute("name").toUpperCase());
                         if(childTableElem != null)
                         {
                             NamedNodeMap attrs = childTableElem.getAttributes();
                             for(int a = 0; a < attrs.getLength(); a++)
                             {
                                 // copy all the "generated" names/values for each child table so searches don't have
                                 // to be performed later
                                 Node attr = attrs.item(a);
                                 String attrName = attr.getNodeName();
                                 if(attrName.startsWith("_gen"))
                                     childTableRefElem.setAttribute(attrName, attr.getNodeValue());
                             }
 
                             Element connector = schemaDoc.getParentConnectorColumn(childTableElem);
                             if(connector != null)
                             {
                                 attrs = connector.getAttributes();
                                 for(int a = 0; a < attrs.getLength(); a++)
                                 {
                                     // copy all the "generated" names/values for each child column so searches don't have
                                     // to be performed later
                                     Node attr = attrs.item(a);
                                     String attrName = attr.getNodeName();
                                     if(attrName.startsWith("_gen"))
                                         childTableRefElem.setAttribute("child-col-" + attrName, attr.getNodeValue());
                                 }
                             }
                         }
                     }
                 }
 
                 String tableClassName = tableElem.getAttribute("_gen-table-name");
                 String domainName = tableElem.getAttribute("_gen-domain-name");
                 String rowName = tableElem.getAttribute("_gen-row-name");
                 String rowListName = tableElem.getAttribute("_gen-rows-name");
 
                 String tableFile = tablesDir.getAbsolutePath() + "/" + tableClassName + ".java";
                 String domainFile = domainsDir.getAbsolutePath() + "/" + domainName + ".java";
                 String rowFile = rowsDir.getAbsolutePath() + "/" + rowName + ".java";
                 String rowListFile = rowsListDir.getAbsolutePath() + "/" + rowListName + ".java";
 
                 tablesTransformer.setParameter("table-name", tableClassName);
                 tablesTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(tableElem), new javax.xml.transform.stream.StreamResult(tableFile));
                 tablesGeneratedCount++;
 
                 domainsTransformer.setParameter("domain-name", domainName);
                 domainsTransformer.setParameter("domain-class-name", domainsPkg + "." + domainName);
                 domainsTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(tableElem), new javax.xml.transform.stream.StreamResult(domainFile));
 
                 rowsTransformer.setParameter("row-name", rowName);
                 rowsTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(tableElem), new javax.xml.transform.stream.StreamResult(rowFile));
 
                 rowsListTransformer.setParameter("row-name", rowName);
                 rowsListTransformer.setParameter("rows-name", rowListName);
                 rowsListTransformer.transform
                     (new javax.xml.transform.dom.DOMSource(tableElem), new javax.xml.transform.stream.StreamResult(rowListFile));
             }
         }
 
         public void createSchemaClass(SchemaDocument schemaDoc) throws TransformerConfigurationException, TransformerException
         {
             String schemaPkgDirName = schemaPkg.replace('.', '/');
             File schemaDir = new File(destRoot + "/" + schemaPkgDirName);
             schemaDir.mkdirs();
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer schemaTransformer = tFactory.newTransformer(new StreamSource(schemaGeneratorStyleSheet));
             schemaTransformer.setParameter("package-name", schemaPkg);
             schemaTransformer.setParameter("class-name", schemaClassName);
             String schemaFile = schemaDir.getAbsolutePath() + "/" + schemaClassName + ".java";
 
             schemaTransformer.transform
                 (new javax.xml.transform.dom.DOMSource(schemaDoc.getDocument()), new javax.xml.transform.stream.StreamResult(schemaFile));
         }
 
         public void generate(SchemaDocument schemaDoc) throws TransformerConfigurationException, TransformerException
         {
             Map dataTypesClassMap = new HashMap();
             Map tableTypesClassMap = new HashMap();
             new File(destRoot).mkdirs();
 
             createDataTypesClasses(schemaDoc, dataTypesClassMap, tableTypesClassMap);
             createTableTypesClasses(schemaDoc, dataTypesClassMap, tableTypesClassMap);
             createTablesClasses(schemaDoc, dataTypesClassMap, tableTypesClassMap);
             createSchemaClass(schemaDoc);
         }
     }
 
 }
 
