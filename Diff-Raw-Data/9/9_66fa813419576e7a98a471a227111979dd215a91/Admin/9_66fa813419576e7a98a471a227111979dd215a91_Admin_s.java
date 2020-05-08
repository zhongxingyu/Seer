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

 import java.io.File;
 
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
 import org.melati.poem.ValidationPoemException;
 
 import org.melati.util.EnumUtils;
 import org.melati.util.MappedEnumeration;
 import org.melati.util.StringUtils;
 import org.melati.util.MelatiRuntimeException;
 
 
 /**
  * Melati template servlet for administration.
  * <p>
  * This class defines
  * {@link #doTemplateRequest(Melati, TemplateContext)}
  * and methods it calls to
  * interpret request methods, perhaps depending on the current
  * table and object, if any.
  * <p>
  * Java methods with names ending "<code>Template</code>"
  * and taking a {@link TemplateContext} and {@link Melati}
  * as arguments are generally called by
  * {@link #doTemplateRequest(Melati, TemplateContext)}) to
  * implement corresponding request methods. 
  * {@link #modifyTemplate(TemplateContext, Melati)}
  * and associated methods are slight variations.
  * <p>
  * {@link #adminTemplate(TemplateContext, String)} is called
  * in all cases to return the template path. The name of the
  * template is usually the same as the request method but not
  * if the same template is used for more than one method or
  * the template served depends on how request processing
  * proceeds.
  * <p>
  * These methods are sometimes called to modify the context:
  * <ul>
  * <li>{@link #editingTemplate(TemplateContext, Melati)}</li>
  * <li>{@link #popup(TemplateContext, Melati)}</li>
  * <li>{@link #primarySelect(TemplateContext, Melati)}</li>
  * <li>{@link #selection(TemplateContext, Melati)}</li>
  * </ul>
  * <p>
  * At the time of writing this covers everything except
  * {@link #create(Table, TemplateContext, Melati)}.
  * <p>
  * (Please review this description and delete this line. JimW)
  *
  * @todo Getting a bit big, wants breaking up
  * @todo Ensure that the new, duplicated record is editted, not the original, 
  *       in Duplicate (see FIXME)
  * @todo Review working of where clause for dates (see FIXME)
  */
 
 public class Admin extends TemplateServlet {
 
   /**
    * Creates a row for a table using field data in a template context.
    */
   protected Persistent create(Table table, final TemplateContext context, 
                               final Melati melati) {
     Persistent result =
       table.create(
         new Initialiser() {
           public void init(Persistent object)
             throws AccessPoemException, ValidationPoemException {
               MelatiUtil.extractFields(context, object);
             }
         });
     result.postEdit(true);
     return result;
   }
 
   /**
    * Return the resource path for an admin template.
    *
    * @param context Ignored. Allows modification in the same expression. Yuk.
    */
   protected final String adminTemplate(TemplateContext context, String name) {
     return "org" + File.separatorChar + 
            "melati" + File.separatorChar + 
            "admin" + File.separatorChar + 
            name;
   }
 
   /**
    *  @return the 'Main' admin frame
    */
 
   protected String mainTemplate(TemplateContext context) {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Main");
   }
 
   /**
    *  @return a DSD for the database
    */
   protected String dsdTemplate(TemplateContext context) {
     context.put("database", PoemThread.database());
     // Webmacro security prevents access from within template
 
     // Note: getPackage() can return null dependant upon 
     // the classloader so we have to chomp the class name
 
     String  c = PoemThread.database().getClass().getName();
     int dot = c.lastIndexOf('.');
     String p = c.substring(0,dot);
 
     context.put("package", p);
     context.put("stringutils", new StringUtils());
     return adminTemplate(context, "DSD");
   }
 
   /**
    *  @return the top template
    */
   protected String topTemplate(TemplateContext context) throws PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Top");
   }
 
   /**
    *  @return the 'bottom' admin page
    */
   protected String bottomTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Bottom");
   }
 
   /**
    *  @return the 'left' admin page
    */
   protected String leftTemplate(TemplateContext context, Melati melati)
   throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Left");
   }
 
   /**
    *  @return primary select template
    */
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
 
   /**
    * Return template for a selection of records from a table.
    */
   protected String selectionTemplate(TemplateContext context, Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), "Selection");
   }
 
   /**
    * Implements request to display a selection of records from a table.
    *
    * @return SelectionRight template. 
    */
   protected String selectionRightTemplate(TemplateContext context, 
                                           Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), 
     "SelectionRight");
   }
 
   /**
    * Modifies the context in preparation for serving a template to view a
    * selection of rows.
    * <p>
    * The table and database are added to the context.
    * <p>
    * Any form fields in the context with names starting "field_"
    * are assumed to hold values that must be matched in selected rows
    * (if not null - or does that mean there is no such field? FIXME).
    * These contribute to the where clause for SQL SELECT.
    * <p>
    * An encoding of the resulting where clause is added to the context.
    * "AND" is replaced by an & separator.
    * <p>
    * There's some stuff that needs sorting out (FIXME) regarding ordering,
    * presumably of selected rows. The resulting orderClause is added to
    * the context.
    * <p>
    * A form field with name "start" is assumed to hold the number
    * of the start row in the result set. The default is zero.
    * The next 20 rows are selected and added as to the context as
    * "results".
    *
    * @return The modified context.
    * @see #adminTemplate(TemplateContext, String)
    */
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
       String string = MelatiUtil.getFormNulled(context,name);
       if (string != null) {
         column.setRaw_unsafe(criteria, column.getType().rawOfString(string));
 
         // FIXME needs to work for dates?
        whereClause.addElement(name + "=" + URLEncoder.encode(string));
       }
     }
 
     context.put("whereClause",
                 EnumUtils.concatenated("&", whereClause.elements()));
 
     // sort out ordering (FIXME this is a bit out of control and is mostly
     // duplicated in popup())
 
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
       String orderColumnIDString = MelatiUtil.getFormNulled(context,name);
       Integer orderColumnID = null;
 
       if (orderColumnIDString != null) {
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
     String startString = MelatiUtil.getFormNulled(context,"start");
     if (startString != null) {
       try {
         start = Math.max(0, Integer.parseInt(startString));
       }
       catch (NumberFormatException e) {
         throw new 
             FormParameterException("start", "param to must be an Integer");
       }
     }
 
     context.put("results", table.selection(table.whereClause(criteria),
                 orderBySQL, false, start, 20));
 
     return context;
   }
 
   /**
    *  @return the 'navigation' admin page
    */
   protected String navigationTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Navigation");
   }
 
   /**
    * Implements the "PopUp" request method.
    * <p>
    * The name should really be <code>popUpTemplate()</code> (FIXME?).
    * The default template name is "PopupSelect". (FIXME?)
    */
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
 
     // sort out ordering (FIXME this is a bit out of control and is mostly
     // duplicated in selection())
 
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
       String orderColumnIDString =
           MelatiUtil.getFormNulled(context,"field_" + name);
       Integer orderColumnID = null;
       if (orderColumnIDString != null) {
         orderColumnID =
             (Integer)searchColumnsType.rawOfString(orderColumnIDString);
         // This is not used but 
         //ColumnInfo info =
         //    (ColumnInfo)searchColumnsType.cookedOfRaw(orderColumnID);
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
 
   /**
    *  @return primary select template
    */
   protected String selectionWindowPrimarySelectTemplate(TemplateContext context,
                                                         Melati melati)
       throws PoemException {
     return adminTemplate(primarySelect(context, melati), 
                          "SelectionWindowPrimarySelect");
   }
 
   /**
    *  @return select template (a selection of records from a table)
    */
   protected String selectionWindowSelectionTemplate(TemplateContext context,
                                                     Melati melati)
       throws FormParameterException {
     return adminTemplate(selection(context, melati), 
                          "SelectionWindowSelection");
   }
 
   /**
    * Implements the "ColumnCreate" request method.
    * <p>
    * FIXME Why is this not called
    * <code>createColumnTemplate()</code>? Could deprecate.
    */
   protected String columnCreateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
 
     final ColumnInfoTable cit = melati.getDatabase().getColumnInfoTable();
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
 
   /**
    * Implements the "Create" request method.
    * <p>
    * The request method, java method and template name do
    * not follow the naming conventions (FIXME?).
    */
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
 
   /**
    * Implements the "Create_doit" request method.
    * <p>
    * The request method, java method and template name do
    * not follow the naming conventions (FIXME?).
    */
   protected String tableCreate_doitTemplate(TemplateContext context,
                                             Melati melati)
       throws PoemException {
     Database database = melati.getDatabase();
     database.addTableAndCommit(
         (TableInfo)create(database.getTableInfoTable(), context, melati),
         context.getForm("field_troidName"));
 
     return adminTemplate(context, "CreateTable_doit");
   }
 
   /**
    * Implements the "ColumnCreate_doit" request method.
    * <p>
    * FIXME Why is this not called
    * <code>createColumn_doitTemplate()</code>? Could deprecate.
    * <p>
    * The template served is "CreateTable_doit".
    */
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
 
   /**
    * Prepare to use an editing template.
    * <p>
    * Throw an exception if the access token does not allow the object
    * to be read.
    * <p>
    * Put objects required by editing templates in the context.
    */
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
 
   protected String treeTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(editingTemplate(context, melati), "Tree");
   }
 
   protected String treeControlTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     return adminTemplate(editingTemplate(context, melati), "TreeControl");
   }
 
   /**
    * Returns the Add template after placing the table and fields for
    * the new row in the context using any field values already in
    * the context.
    */
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
 
   /**
    * Returns the Update template after modifying the current
    * row according to field values in the context.
    * <p>
    * If successful the template will say so while reloading according
    * to the returnTarget and returnURL values from the Form in context.
    */
   protected String updateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     Persistent object = melati.getObject();
     object.preEdit();
     MelatiUtil.extractFields(context, object);
     object.postEdit(false);
     return adminTemplate(context, "Update");
   }
 
   /**
    * Returns the Update template after creating a new row using field
    * data in the context.
    * <p>
    * If successful the template will say so while reloading according
    * to the returnTarget and returnURL values from the Form in context.
    */
   protected String addUpdateTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     Persistent object = create(melati.getTable(), context, melati);
     context.put("object", object);
     return adminTemplate(context, "Update");
   }
 
   protected String deleteTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     try {
       melati.getObject().delete();
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
 
   /**
    * Implements request method "Update".
    * <p>
    * Calls another method depending on the requested action.
    *
    * @see #updateTemplate(TemplateContext, Melati)
    * @see #deleteTemplate(TemplateContext, Melati)
    * @see #duplicateTemplate(TemplateContext, Melati)
    */
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
 
   /** 
    * Finished uploading.
    *
    * If you want the system to display the file 
    * you need to set your melati-wide FormDataAdaptorFactory,
    * in org.melati.MelatiServlet.properties,
    * to something that returns a valid URL, for instance,
    * PoemFileDataAdaptorFactory;
    * (remember to set your UploadDir and UploadURL
    * in the Setting table).
    *
    * @param context the {@link TemplateContext} in use
    * @param melati the current {@link Melati}
    * @return a template name
    */
 
   protected String uploadDoneTemplate(TemplateContext context, Melati melati)
       throws PoemException {
     String field = context.getForm("field");
     context.put("field", field);
     String url = "";
     url = context.getMultipartForm("file").getDataURL();
     if (url == null) throw new NullUrlDataAdaptorException();
     context.put("url", url);
     return adminTemplate(context, "UploadDone");
   }
 
   static class NullUrlDataAdaptorException extends MelatiRuntimeException {
     /** @return the message */
     public String getMessage() {
       return  "The configured FormDataAdaptor returns a null URL.";
     }
   }
 
   public static final String
       METHOD_CREATE_TABLE = "Create",
       METHOD_CREATE_COLUMN = "CreateColumn",
       METHOD_ADD_RECORD = "Add";
 
   protected String doTemplateRequest(Melati melati, TemplateContext context)
       throws Exception {
     Capability admin = PoemThread.database().getCanAdminister();
     AccessToken token = PoemThread.accessToken();
     if (!token.givesCapability(admin))
       throw new AccessPoemException(token, admin);
 
     context.put("admin", melati.getAdminUtils());
     
     /* upload can take place without an object
      */
     if (melati.getMethod().equals("Upload"))
       return uploadTemplate(context);
     if (melati.getMethod().equals("UploadDone"))
       return uploadDoneTemplate(context, melati);
     
     if (melati.getObject() != null) {
       if (melati.getMethod().equals("Right"))
         return rightTemplate(context, melati);
       if (melati.getMethod().equals("EditHeader"))
         return editHeaderTemplate(context, melati);
       if (melati.getMethod().equals("Tree"))
         return treeTemplate(context, melati);
       if (melati.getMethod().equals("TreeControl"))
         return treeControlTemplate(context, melati);
       if (melati.getMethod().equals("Edit"))
         return editTemplate(context, melati);
       if (melati.getMethod().equals("Update"))
         return modifyTemplate(context, melati);
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
       if (melati.getMethod().equals(METHOD_ADD_RECORD))
         return addTemplate(context, melati);
       if (melati.getMethod().equals("AddUpdate"))
         return addUpdateTemplate(context, melati);
     }
     else {
       if (melati.getMethod().equals("Main"))
         return mainTemplate(context);
       if (melati.getMethod().equals("DSD"))
         return dsdTemplate(context);
       if (melati.getMethod().equals("Top"))
         return topTemplate(context);
       if (melati.getMethod().equals(METHOD_CREATE_TABLE))
         return tableCreateTemplate(context, melati);
       if (melati.getMethod().equals("Create_doit"))
         return tableCreate_doitTemplate(context, melati);
       if (melati.getMethod().equals(METHOD_CREATE_COLUMN))
         return columnCreateTemplate(context, melati);
       if (melati.getMethod().equals("CreateColumn_doit"))
         return columnCreate_doitTemplate(context, melati);
     }
 
     throw new InvalidUsageException(this, melati.getContext());
   }
 }
