 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.admin;
 
 import java.net.URLEncoder;
 
 import java.util.Vector;
 import java.util.Enumeration;
 
 import org.melati.Melati;
 import org.melati.MelatiUtil;
 import org.melati.servlet.InvalidUsageException;
 import org.melati.servlet.TemplateServlet;
 import org.melati.template.TemplateContext;
 import org.melati.template.FormParameterException;
 
 import org.melati.poem.AccessToken;
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.BaseFieldAttributes;
 import org.melati.poem.Capability;
 import org.melati.poem.Column;
 import org.melati.poem.ColumnInfo;
 import org.melati.poem.ColumnInfoTable;
 import org.melati.poem.Database;
 import org.melati.poem.DeletionIntegrityPoemException;
 import org.melati.poem.Field;
 import org.melati.poem.FieldAttributes;
 import org.melati.poem.Initialiser;
 import org.melati.poem.Persistent;
 import org.melati.poem.PoemException;
 import org.melati.poem.PoemThread;
 import org.melati.poem.PoemType;
 import org.melati.poem.PoemTypeFactory;
 import org.melati.poem.ReferencePoemType;
 import org.melati.poem.Table;
 import org.melati.poem.TableInfo;
 import org.melati.poem.TableInfoTable;
 import org.melati.poem.ValidationPoemException;
 
 import org.melati.util.EnumUtils;
 import org.melati.util.MappedEnumeration;
 
 /**
  * FIXME getting a bit big, wants breaking up
  */
 
 public class Admin extends TemplateServlet {
 
   protected Persistent create(Table table, final TemplateContext context, 
                               final Melati melati) {
     return table.create(
         new Initialiser() {
           public void init(Persistent object)
             throws AccessPoemException, ValidationPoemException {
               MelatiUtil.extractFields(context, object);
             }
         });
   }
 
   protected final String adminTemplate(TemplateContext context, String name) {
     return ("admin/" + name);
   }
 
   // return the 'Main' admin frame
 
   protected String mainTemplate(TemplateContext context) {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Main");
   }
 
   // return top template
 
   protected String topTemplate(TemplateContext context) throws PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Top");
   }
 
   // return the 'bottom' admin page
 
   protected String bottomTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Bottom");
   }
 
   // return the 'left' admin page
 
   protected String leftTemplate(TemplateContext context, Melati melati)
   throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Left");
   }
 
   // return primary select template
 
   protected String primarySelectTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(primarySelect(context, melati), "PrimarySelect");
   }
 
   protected TemplateContext primarySelect(TemplateContext context,
                                           Melati melati)
       throws PoemException {
     final Table table = melati.getTable();
     context.put("table", table);
 
     final Database database = table.getDatabase();
     context.put("database", database);
 
     Field primaryCriterion;
 
     Column column = table.primaryCriterionColumn();
     if (column != null) {
       String sea = context.getForm("field_" + column.getName());
       primaryCriterion = new Field(
           sea == null || sea.equals("") ?
             null :
             column.getType().rawOfString(sea),
           new BaseFieldAttributes(column,
                                   column.getType().withNullable(true)));
     }
     else
       primaryCriterion = null;
 
     context.put("primaryCriterion", primaryCriterion);
     return context;
   }
 
   // return select template (a selection of records from a table)
   protected String selectionTemplate(TemplateContext context, Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), "Selection");
   }
 
   // return select template (a selection of records from a table)
   protected String selectionRightTemplate(TemplateContext context, 
                                           Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), 
     "SelectionRight");
   }
 
   protected TemplateContext selection(TemplateContext context, Melati melati)
       throws FormParameterException {
     final Table table = melati.getTable();
     context.put("table", table);
 
     final Database database = table.getDatabase();
     context.put("database", database);
 
     // sort out search criteria
 
     final Persistent criteria = table.newPersistent();
 
     Vector whereClause = new Vector();
 
     for (Enumeration c = table.columns(); c.hasMoreElements();) {
       Column column = (Column)c.nextElement();
       String name = "field_" + column.getName();
       String string = context.getForm(name);
       if (string != null && !string.equals("")) {
         column.setRaw_unsafe(criteria, column.getType().rawOfString(string));
 
         // FIXME needs to work for dates?
         whereClause.addElement(name + "=" + URLEncoder.encode(string));
         //        whereClause.addElement(column.eqClause(string));
       }
     }
 
     context.put("whereClause",
                 EnumUtils.concatenated("&", whereClause.elements()));
 
     // sort out ordering (FIXME this is a bit out of control)
 
     PoemType searchColumnsType =
         new ReferencePoemType(database.getColumnInfoTable(), true) {
           protected Enumeration _possibleRaws() {
             return
                 new MappedEnumeration(table.getSearchCriterionColumns()) {
                   public Object mapped(Object column) {
                     return ((Column)column).getColumnInfo().getTroid();
                   }
                 };
           }
         };
 
     Vector orderings = new Vector();
     Vector orderClause = new Vector();
 
     for (int o = 1; o <= 2; ++o) {
       String name = "field_order-" + o;
       String orderColumnIDString = context.getForm(name);
       Integer orderColumnID = null;
 
       if (orderColumnIDString != null && !orderColumnIDString.equals("")) {
         orderColumnID =
             (Integer)searchColumnsType.rawOfString(orderColumnIDString);
         ColumnInfo info =
             (ColumnInfo)searchColumnsType.cookedOfRaw(orderColumnID);
         String desc = Boolean.TRUE.equals(info.getSortdescending()) ?
                           " DESC" : "";
         orderings.addElement(database.quotedName(info.getName()) + desc);
         orderClause.addElement(name+"="+orderColumnIDString);
       }
     }
 
     String orderBySQL = null;
     if (orderings.elements().hasMoreElements())
       orderBySQL = EnumUtils.concatenated(", ", orderings.elements());
     context.put("orderClause",
                 EnumUtils.concatenated("&", orderClause.elements()));
 
     int start = 0;
     String startString = context.getForm("start");
     if (startString != null) {
       try {
         start = Math.max(0, Integer.parseInt(startString));
       }
       catch (NumberFormatException e) {
         //FIXME - surely not a PoemException
         throw new 
             FormParameterException("start", "param to must be an Integer");
       }
     }
 
     context.put("results", table.selection(table.whereClause(criteria),
     orderBySQL, false, start, 20));
 
     return context;
   }
 
   // return the 'navigation' admin page
   protected String navigationTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Navigation");
   }
 
   protected String popupTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(popup(context, melati), "PopupSelect");
   }
 
   protected TemplateContext popup(TemplateContext context, Melati melati)
       throws PoemException {
     final Table table = melati.getTable();
     context.put("table", table);
 
     final Database database = table.getDatabase();
     context.put("database", database);
 
     // sort out search criteria
 
     final Persistent criteria = table.newPersistent();
 
     MappedEnumeration criterias =
         new MappedEnumeration(table.getSearchCriterionColumns()) {
           public Object mapped(Object c) {
             return ((Column)c).asField(criteria).withNullable(true);
           }
         };
 
     context.put("criteria", EnumUtils.vectorOf(criterias));
 
     // sort out ordering (FIXME this is a bit out of control)
 
     PoemType searchColumnsType =
         new ReferencePoemType(database.getColumnInfoTable(), true) {
           protected Enumeration _possibleRaws() {
             return
                 new MappedEnumeration(table.getSearchCriterionColumns()) {
                   public Object mapped(Object column) {
                     return ((Column)column).getColumnInfo().getTroid();
                   }
                 };
           }
         };
 
     Vector orderings = new Vector();
 
     for (int o = 1; o <= 2; ++o) {
       String name = "order-" + o;
       String orderColumnIDString = context.getForm("field_" + name);
       Integer orderColumnID = null;
       if (orderColumnIDString != null && !orderColumnIDString.equals("")) {
         orderColumnID =
             (Integer)searchColumnsType.rawOfString(orderColumnIDString);
         ColumnInfo info =
             (ColumnInfo)searchColumnsType.cookedOfRaw(orderColumnID);
       }
 
       orderings.addElement(
           new Field(orderColumnID,
                     new BaseFieldAttributes(name, searchColumnsType)));
     }
 
     context.put("orderings", orderings);
 
     return context;
   }
 
   protected String selectionWindowTemplate(TemplateContext context, 
                                            Melati melati)
       throws PoemException {
     context.put("database", PoemThread.database());
     context.put("table", melati.getTable());
     return adminTemplate(context, "SelectionWindow");
   }
 
   // return primary select template
   protected String selectionWindowPrimarySelectTemplate(TemplateContext context,
                                                         Melati melati)
       throws PoemException {
     return adminTemplate(primarySelect(context, melati), 
                          "SelectionWindowPrimarySelect");
   }
 
   // return select template (a selection of records from a table)
   protected String selectionWindowSelectionTemplate(TemplateContext context,
                                                     Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), 
                          "SelectionWindowSelection");
   }
 
   protected String columnCreateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
 
     final ColumnInfoTable cit = melati.getDatabase().getColumnInfoTable();
     final Column tic = cit.getTableinfoColumn();
     final Column typeColumn = cit.getTypefactoryColumn();
 
     Enumeration columnInfoFields =
         new MappedEnumeration(cit.getDetailDisplayColumns()) {
           public Object mapped(Object column) {
             if (column == typeColumn)
               return new Field(PoemTypeFactory.STRING.getCode(),
                                typeColumn);
             else
               return new Field((Object)null, (FieldAttributes)column);
           }
         };
 
     context.put("columnInfoFields", columnInfoFields);
 
     return adminTemplate(context, "CreateColumn");
   }
 
   protected String tableCreateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     Database database = melati.getDatabase();
 
     // Compose field for naming the TROID column: the display name and
     // description are redundant, since they not used in the template
 
     Field troidNameField = new Field(
         "id",
         new BaseFieldAttributes(
             "troidName", "Troid column", "Name of TROID column",
             database.getColumnInfoTable().getNameColumn().getType(),
             20, 1, null, false, true, true));
 
     context.put("troidNameField", troidNameField);
 
     Table tit = database.getTableInfoTable();
     Enumeration tableInfoFields =
         new MappedEnumeration(tit.columns()) {
           public Object mapped(Object column) {
             return new Field((Object)null, (Column)column);
           }
         };
 
     context.put("tableInfoFields", tableInfoFields);
 
     return adminTemplate(context, "CreateTable");
   }
 
   protected String tableCreate_doitTemplate(TemplateContext context,
                                             Melati melati)
       throws PoemException {
     Database database = melati.getDatabase();
     database.addTableAndCommit(
         (TableInfo)create(database.getTableInfoTable(), context, melati),
         context.getForm("field_troidName"));
 
     return adminTemplate(context, "CreateTable_doit");
   }
 
   protected String columnCreate_doitTemplate(final TemplateContext context,
                                              final Melati melati)
       throws PoemException {
 
     Database db = melati.getDatabase();
 
     ColumnInfo columnInfo =
         (ColumnInfo)db.getColumnInfoTable().create(
         new Initialiser() {
           public void init(Persistent object)
               throws AccessPoemException, ValidationPoemException {
             MelatiUtil.extractFields(context, object);
           }
         });
 
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
 
     return adminTemplate(context, "CreateTable_doit");
   }
 
   protected TemplateContext editingTemplate(TemplateContext context,
                                             Melati melati)
       throws PoemException {
     melati.getObject().assertCanRead();
     context.put("object", melati.getObject());
     Database database = melati.getDatabase();
     context.put("database", database);
     context.put("table", melati.getTable());
     return context;
   }
 
   protected String rightTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(editingTemplate(context, melati), "Right");
   }
 
   protected String editHeaderTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(editingTemplate(context, melati), "EditHeader");
   }
 
   protected String editTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(editingTemplate(context, melati), "Edit");
   }
 
   protected String addTemplate(TemplateContext context, Melati melati)
       throws PoemException {
 
     context.put("table", melati.getTable());
 
     Enumeration columns = melati.getTable().columns();
     Vector fields = new Vector();
     while (columns.hasMoreElements()) {
       Column column = (Column)columns.nextElement();
       String stringValue = context.getForm("field_" + column.getName());
       Object value = null;
       if (stringValue != null)
         value = column.getType().rawOfString(stringValue);
       fields.add(new Field(value, column));
     }
     context.put("fields", fields.elements());
 
     return adminTemplate(context, "Add");
   }
 
   protected String updateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     MelatiUtil.extractFields(context, melati.getObject());
     return adminTemplate(context, "Update");
   }
 
   protected String addUpdateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     create(melati.getTable(), context, melati);
     return adminTemplate(context, "Update");
   }
 
   protected String deleteTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     try {
       melati.getObject().deleteAndCommit();
       return adminTemplate(context, "Update");
     }
     catch (DeletionIntegrityPoemException e) {
       context.put("object", e.object);
       context.put("references", e.references);
       return adminTemplate(context, "DeleteFailure");
     }
   }
 
   protected String duplicateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     // FIXME the ORIGINAL object is the one that will get edited when the
     // update comes in from Edit, because it will be identified from
     // the path info!
 
     Persistent dup = melati.getObject().duplicated();
     MelatiUtil.extractFields(context, dup);
     dup.getTable().create(dup);
     context.put("object", dup);
     return adminTemplate(context, "Update");
   }
 
   protected String modifyTemplate(TemplateContext context, Melati melati)
       throws FormParameterException {
     String action = melati.getRequest().getParameter("action");
     if ("Update".equals(action))
       return updateTemplate(context, melati);
     else if ("Delete".equals(action))
       return deleteTemplate(context, melati);
     else if ("Duplicate".equals(action))
       return duplicateTemplate(context, melati);
     else
       throw new FormParameterException("action",
                                        "bad action from Edit: " + action);
   }
 
   protected String uploadTemplate(TemplateContext context)
       throws PoemException {
     context.put("field", context.getForm("field"));
     return adminTemplate(context, "Upload");
   }
 
   /*
    * For this to work you need to set your melati-wide FormDataAdaptorFactory
    * to something that returns a valid URL, for instance,
    * UploadDirDataAdaptorFactory (remember to set your UploadDir and UploadURL
    * in the Setting table.
    */
 
   protected String uploadDoneTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     String field = context.getForm("field");
     context.put("field", field);
     String url = "";
 
     try {
       url = context.getMultipartForm("file").getDataURL();
    } catch (Exception e) {
      e.printStackTrace(System.err);
     }
     context.put("url", url);
     return adminTemplate(context, "UploadDone");
   }
 
   protected String doTemplateRequest(Melati melati, TemplateContext context)
       throws Exception {
     Capability admin = PoemThread.database().getCanAdminister();
     AccessToken token = PoemThread.accessToken();
     if (!token.givesCapability(admin))
       throw new AccessPoemException(token, admin);
 
     context.put("admin", melati.getAdminUtils());
     if (melati.getObject() != null) {
       if (melati.getMethod().equals("Right"))
         return rightTemplate(context, melati);
       if (melati.getMethod().equals("EditHeader"))
         return editHeaderTemplate(context, melati);
       if (melati.getMethod().equals("Edit"))
         return editTemplate(context, melati);
       else
         if (melati.getMethod().equals("Update"))
           return modifyTemplate(context, melati);
         else
           if (melati.getObject() instanceof AdminSpecialised) {
             String templateName =
               ((AdminSpecialised)melati.getObject()).adminHandle(
                 melati, melati.getHTMLMarkupLanguage());
             if (templateName != null)
               return templateName;
           }
     }
     else if (melati.getTable() != null) {
       if (melati.getMethod().equals("Bottom"))
         return bottomTemplate(context, melati);
       if (melati.getMethod().equals("Left"))
         return leftTemplate(context, melati);
       if (melati.getMethod().equals("PrimarySelect"))
         return primarySelectTemplate(context, melati);
       if (melati.getMethod().equals("Selection"))
         return selectionTemplate(context, melati);
       if (melati.getMethod().equals("SelectionRight"))
         return selectionRightTemplate(context, melati);
       if (melati.getMethod().equals("Navigation"))
         return navigationTemplate(context, melati);
       if (melati.getMethod().equals("PopUp"))
         return popupTemplate(context, melati);
       if (melati.getMethod().equals("SelectionWindow"))
         return selectionWindowTemplate(context, melati);
       if (melati.getMethod().equals("SelectionWindowPrimarySelect"))
         return selectionWindowPrimarySelectTemplate(context, melati);
       if (melati.getMethod().equals("SelectionWindowSelection"))
         return selectionWindowSelectionTemplate(context, melati);
       if (melati.getMethod().equals("Add"))
         return addTemplate(context, melati);
       if (melati.getMethod().equals("AddUpdate"))
         return addUpdateTemplate(context, melati);
     }
     else {
       if (melati.getMethod().equals("Main"))
         return mainTemplate(context);
       if (melati.getMethod().equals("Top"))
         return topTemplate(context);
       if (melati.getMethod().equals("Create"))
         return tableCreateTemplate(context, melati);
       if (melati.getMethod().equals("Create_doit"))
         return tableCreate_doitTemplate(context, melati);
       if (melati.getMethod().equals("CreateColumn"))
         return columnCreateTemplate(context, melati);
       if (melati.getMethod().equals("CreateColumn_doit"))
         return columnCreate_doitTemplate(context, melati);
       if (melati.getMethod().equals("Upload"))
         return uploadTemplate(context);
       if (melati.getMethod().equals("UploadDone"))
         return uploadDoneTemplate(context, melati);
     }
 
     throw new InvalidUsageException(this, melati.getContext());
   }
 }
