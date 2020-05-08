 package com.socialcomputing.wps.server.plandictionary.connectors.jdbc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import com.socialcomputing.wps.server.plandictionary.WPSDictionary;
 import com.socialcomputing.wps.server.plandictionary.connectors.WPSConnectorException;
 
 /**
  * Title: WPS Connectors Description: Copyright: Copyright (c) 2000 Company:
  * VOYEZ VOUS
  * 
  * @author Franck Valetas
  * @version 1.0
  */
 
 public class JDBCProperties implements java.io.Serializable {
 
     private static final long serialVersionUID = -5574991072534266245L;
     public static final int ENTITY_PROPS = 0;
     public static final int ATTRIBUTE_PROPS = 1;
     public static final int SUBATTRIBUTE_PROPS = 2;
 
     public class JDBCPropertyGroup implements java.io.Serializable {
         static final long serialVersionUID = 8112322826983798307L;
 
         public String m_Name = null;
         public String m_Description = null;
         public int m_Type = ENTITY_PROPS;
         public int m_AttributeRestriction = WPSDictionary.APPLY_TO_ALL;
 
         public JDBCQuery m_PropertyGroupQuery = null;
 
         public boolean m_bUseRefId1 = false;
         public boolean m_bUseRefId2 = false;
         public boolean m_bMultipleRows = false;
         public boolean m_bNameIsLabel = false;
         public int m_nbColumns = -1; // -1 = toutes, sinon les premi�res
 
         // Type des colonnes (stockage pour acc�l�ration de la lecture)
         private transient Vector<InternalColumnInfo> m_ColumnInfo = null;
 
         public JDBCPropertyGroup(int type, String name) {
             m_Type = type;
         }
 
         public void openConnections(Hashtable<String, Object> wpsparams, Connection connection)
                 throws WPSConnectorException {
             m_PropertyGroupQuery.open(wpsparams, connection);
         }
 
         public void closeConnections() throws WPSConnectorException {
             if (m_PropertyGroupQuery != null)
                 m_PropertyGroupQuery.close();
             m_PropertyGroupQuery = null;
         }
 
         private class InternalColumnInfo {
             public String m_Name = null;
             public int m_Type = java.sql.Types.OTHER;
 
             public InternalColumnInfo(String name, int type) {
                 m_Name = name.toLowerCase();
                 // System.out.println("m_Name : "+m_Name);
                 m_Type = type;
             }
         }
 
         private boolean getColumnInfo(ResultSet rs, String id) throws WPSConnectorException {
             boolean ret = false;
             try {
                 ret = rs.next();
                 if (ret) {
                     if (m_ColumnInfo == null) { // Recherche des types
                         ResultSetMetaData rsmd = rs.getMetaData();
                         int count = rsmd.getColumnCount();
                         m_ColumnInfo = new Vector<InternalColumnInfo>(count);
                         for (int i = 1; i <= count; ++i) {
                             // System.out.println("m_bNameIsLabel("+i+") :"+m_bNameIsLabel);
                            m_ColumnInfo.add(new InternalColumnInfo(m_bNameIsLabel ? rsmd.getColumnLabel(i) : rsmd
                                    .getColumnName(i), rsmd.getColumnType(i)));
                         }
                     }
                 }
                 else
                     ;// System.out.println(
                      // "JDBCPropertyGroup failed to find column information for "
                      // + type + " '" + id + "'");
             }
             catch (SQLException e) {
                 throw new WPSConnectorException("JDBCPropertyGroup failed to find column information for " + m_Type
                         + " '" + id + "'", e);
             }
             return ret;
         }
 
         private Object getProperty(ResultSet rs, int index) throws WPSConnectorException {
             InternalColumnInfo info = null;
             try {
                 // Recherche des valeurs
                 Object o = null;
                 info = m_ColumnInfo.elementAt(index);
                 // System.out.println("\n\n\nthe type : "+info.m_Type);
 
                 switch (info.m_Type) {
                     case java.sql.Types.LONGVARBINARY:
                         InputStream strm = rs.getBinaryStream(1);
                         if (strm != null) {
                             int len = 0;
                             for (; strm.read() != -1; ++len)
                                 ;
                             strm.reset();
                             byte[] b = new byte[len];
                             strm.read(b);
                             return new String(b, "UTF-8");
                         }
                         return "";
                     case java.sql.Types.BIGINT:
                     case java.sql.Types.SMALLINT:
                     case java.sql.Types.TINYINT:
                     case java.sql.Types.INTEGER:
                     case java.sql.Types.NUMERIC:
                         Integer n = new Integer(rs.getInt(index + 1));
                         // System.out.println("\n\n\ntype : "+info.m_Type+"\nvalue : "+n);
                         return n;
                     case java.sql.Types.FLOAT:
                     case java.sql.Types.REAL:
                         return new Float(rs.getFloat(index + 1));
                     case java.sql.Types.DOUBLE:
                         return new Double(rs.getDouble(index + 1));
                     case java.sql.Types.LONGVARCHAR:
                     case java.sql.Types.VARCHAR:
                     case java.sql.Types.CHAR:
                        info.m_Type = 12;
                         return rs.getString(index + 1);
                         /*
                          * String testString =
                          * "ab\u3053\u3093\u306B\u3061\u306F\u4E16cd"; String t
                          * = new String(rs.getString( index+1 )); String t2 =
                          * new String(rs.getBytes(index+1),"UTF-8");
                          * System.out.println("\n\n\ntype : "
                          * +info.m_Type+"\nvalue : "+t+" "+t2);
                          */
                         // return new String(rs.getBytes(index+1),"UTF-8");
                     case java.sql.Types.DATE:
                         o = rs.getDate(index + 1);
                         return o;
                     default:
                         throw new WPSConnectorException("JDBCPropertyGroup unknown DB type: " + info.m_Type);
                 }
             }
             catch (SQLException e) {
                 throw new WPSConnectorException("JDBCPropertyGroup unable to get column type", e);
             }
             catch (IOException e) {
                 throw new WPSConnectorException("JDBCPropertyGroup error", e);
             }
         }
 
         protected int getProperties(Hashtable<String, Object> table, String id, boolean bInBase, String idRef1,
                                     String idRef2) throws WPSConnectorException {
             if ((bInBase && (m_AttributeRestriction == WPSDictionary.APPLY_TO_NOT_BASE))
                     || (!bInBase && (m_AttributeRestriction == WPSDictionary.APPLY_TO_BASE)))
                 return 0;
 
             int added = 0;
             try {
                 switch (m_Type) {
                     case JDBCProperties.ENTITY_PROPS:
                         m_PropertyGroupQuery.setCurEntity(id);
                         break;
                     case JDBCProperties.ATTRIBUTE_PROPS:
                         m_PropertyGroupQuery.setCurAttribute(id);
                         m_PropertyGroupQuery.setCurEntity(idRef1);
                         break;
                     case JDBCProperties.SUBATTRIBUTE_PROPS:
                         m_PropertyGroupQuery.setCurSubAttribute(id);
                         m_PropertyGroupQuery.setCurAttribute(idRef1);
                         m_PropertyGroupQuery.setCurEntity(idRef2);
                         break;
                 }
 
                 ResultSet rs = m_PropertyGroupQuery.executeQuery();
                 if (getColumnInfo(rs, id) && m_ColumnInfo != null) {
                     int maxCol = (m_nbColumns == -1) ? m_ColumnInfo.size() : m_nbColumns;
                     if (m_bMultipleRows && (maxCol > 0)) {
                         ArrayList<ArrayList<Object>> mainLst = new ArrayList<ArrayList<Object>>();
                         for (int i = 0; i < maxCol; ++i)
                             mainLst.add(new ArrayList<Object>());
                         do {
                             for (int i = 0; i < maxCol; ++i) {
                                 ArrayList<Object> lst = mainLst.get(i);
                                 Object o = this.getProperty(rs, i);
                                 if (o != null)
                                     lst.add(o);
                             }
                         }
                         while (rs.next());
                         for (int i = 0; i < maxCol; ++i) {
                             ArrayList<Object> lst = mainLst.get(i);
                             if (lst.size() > 0) {
                                 InternalColumnInfo info = m_ColumnInfo.elementAt(i);
                                 switch (info.m_Type) {
                                     case java.sql.Types.BIGINT:
                                     case java.sql.Types.SMALLINT:
                                     case java.sql.Types.TINYINT:
                                     case java.sql.Types.INTEGER:
                                     case java.sql.Types.NUMERIC:
                                         table.put(info.m_Name, lst.toArray(new Integer[lst.size()]));
                                         break;
                                     case java.sql.Types.FLOAT:
                                     case java.sql.Types.REAL:
                                         table.put(info.m_Name, lst.toArray(new Float[lst.size()]));
                                         break;
                                     case java.sql.Types.DOUBLE:
                                         table.put(info.m_Name, lst.toArray(new Double[lst.size()]));
                                         break;
                                     case java.sql.Types.LONGVARBINARY:
                                     case java.sql.Types.LONGVARCHAR:
                                     case java.sql.Types.VARCHAR:
                                     case java.sql.Types.CHAR:
                                         table.put(info.m_Name, lst.toArray(new String[lst.size()]));
                                         break;
                                     case java.sql.Types.DATE:
                                         table.put(info.m_Name, lst.toArray(new java.sql.Date[lst.size()]));
                                         break;
                                 }
                                 ++added;
                             }
                         }
                     }
                     if (!m_bMultipleRows) {
                         Object o;
                         for (int i = 0; i < maxCol; ++i) {
                             o = this.getProperty(rs, i);
                             if (o != null) {
                                 InternalColumnInfo info = (InternalColumnInfo) m_ColumnInfo.elementAt(i);
                                 table.put(info.m_Name, o);
                                 ++added;
                             }
                         }
                     }
                 }
                 rs.close();
             }
             catch (SQLException e) {
                 throw new WPSConnectorException("JDBCProperties failed to read properties", e);
             }
             return added;
         }
     }
 
     // Type de la colonne de nommage des proprietes (pour la gestion de
     // l'unicode dans un LONGVARBINARY)
     // private int m_ColumnNameType = java.sql.Types.OTHER;
 
     // Liste desrequetes des proprietes
     public ArrayList<JDBCPropertyGroup> m_PropertyGroups = new ArrayList<JDBCPropertyGroup>();
 
     static JDBCProperties readObject(int type, org.jdom.Element element) {
         JDBCProperties props = new JDBCProperties();
         if (element != null) {
             List lst = element.getChildren("JDBC-property");
             int size = lst.size();
             for (int i = 0; i < size; ++i) {
                 org.jdom.Element node = (org.jdom.Element) lst.get(i);
                 JDBCPropertyGroup grp = props.new JDBCPropertyGroup(type, node.getAttributeValue("name"));
                 grp.m_Description = node.getChildText("comment");
                 grp.m_PropertyGroupQuery = JDBCQuery.readObject(node);
                 String p = node.getAttributeValue("apply");
                 if (p.equalsIgnoreCase("base"))
                     grp.m_AttributeRestriction = WPSDictionary.APPLY_TO_BASE;
                 else if (p.equalsIgnoreCase("notbase"))
                     grp.m_AttributeRestriction = WPSDictionary.APPLY_TO_NOT_BASE;
                 else
                     grp.m_AttributeRestriction = WPSDictionary.APPLY_TO_ALL;
                 if (node.getAttributeValue("column-name") != null)
                     grp.m_bNameIsLabel = node.getAttributeValue("column-name").equalsIgnoreCase("label");
                 grp.m_bMultipleRows = node.getAttributeValue("multiple-rows").equalsIgnoreCase("yes");
                 try {
                     grp.m_nbColumns = Integer.parseInt(node.getAttributeValue("columns-used"));
                 }
                 catch (Exception e) {}
                 grp.m_bUseRefId1 = node.getAttributeValue("use-id1").equalsIgnoreCase("yes");
                 grp.m_bUseRefId2 = node.getAttributeValue("use-id2").equalsIgnoreCase("yes");
                 props.m_PropertyGroups.add(grp);
             }
         }
         return props;
     }
 
     public JDBCProperties() {}
 
     public void openConnections(Hashtable<String, Object> wpsparams, Connection connection)
             throws WPSConnectorException {
         for (JDBCPropertyGroup grp : m_PropertyGroups) {
             grp.openConnections(wpsparams, connection);
         }
     }
 
     public void closeConnections() throws WPSConnectorException {
         for (JDBCPropertyGroup grp : m_PropertyGroups) {
             grp.closeConnections();
         }
     }
 
     public int getProperties(Hashtable<String, Object> table, String id, boolean bInBase, String idRef1, String idRef2)
             throws WPSConnectorException {
         int count = 0;
         for (JDBCPropertyGroup grp : m_PropertyGroups) {
             count += grp.getProperties(table, id, bInBase, idRef1, idRef2);
         }
         if (!table.containsKey("id"))
             table.put("id", id);
         return count;
     }
 }
