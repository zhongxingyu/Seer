 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense. 
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.admin;
 
 import java.util.*;
 import java.net.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import org.melati.*;
 import org.melati.util.*;
 import org.melati.poem.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.servlet.*;
 import org.webmacro.engine.*;
 import org.webmacro.resource.*;
 import org.webmacro.broker.*;
 
 /**
  * FIXME getting a bit big, wants breaking up
  */
 
 public class Admin extends MelatiServlet {
 
   protected Persistent create(Table table, final WebContext context) {
     return table.create(
         new Initialiser() {
           public void init(Persistent object)
               throws AccessPoemException, ValidationPoemException {
             Melati.extractFields(context, object);
           }
         });
   }
 
   protected final Template adminTemplate(WebContext context, String name)
       throws NotFoundException, InvalidTypeException {
         return getTemplate("admin/" + name);
   }
   
   // return the 'Main' admin frame
   protected Template mainTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Main.wm");
   }
 
   // return top template
   protected Template topTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Top.wm");
   }
   
   // return the 'bottom' admin page
   protected Template bottomTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Bottom.wm");
   }
   
   // return the 'left' admin page
   protected Template leftTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Left.wm");
   }
 
   // return primary select template
   protected Template primarySelectTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(primarySelect(context,melati), "PrimarySelect.wm");
   }
 
   protected WebContext primarySelect(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     final Table table = melati.getTable();
     context.put("table", table);
 
     final Database database = table.getDatabase();
     context.put("database", database);
 
     Field primaryCriterion;
 
     Column column = table.primaryCriterionColumn();
     if (column != null) {
       String sea = context.getForm("field_" + column.getName());
       primaryCriterion = new Field(
           sea == null || sea.equals("") ? null :
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
   protected Template selectionTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(selection(context,melati), "Selection.wm");
   }
 
   // return select template (a selection of records from a table)
   protected Template selectionRightTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(selection(context,melati), "SelectionRight.wm");
   }
 
   protected WebContext selection(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
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
         whereClause.addElement(name + "=" + URLEncoder.encode(string));
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
 
     Vector orderingNames = new Vector();
     Vector orderClause = new Vector();
     for (int o = 1; o <= 2; ++o) {
       String name = "order-" + o;
       String orderColumnIDString = context.getForm("field_" + name);
       Integer orderColumnID = null;
       if (orderColumnIDString != null && !orderColumnIDString.equals("")) {
         orderColumnID =
             (Integer)searchColumnsType.rawOfString(orderColumnIDString);
         ColumnInfo info =
             (ColumnInfo)searchColumnsType.cookedOfRaw(orderColumnID);
         orderingNames.addElement(database.quotedName(info.getName()));
         orderClause.addElement(name+"="+orderColumnIDString);
       }
     }
 
    String orderBySQL = null;
    if (orderingNames.elements().hasMoreElements()) 
      orderBySQL = EnumUtils.concatenated(", ", orderingNames.elements());
     context.put("orderClause",
                 EnumUtils.concatenated("&", orderClause.elements()));
 
     int start = 0;
     String startString = context.getForm("start");
     if (startString != null) {
       try {
         start = Math.max(0, Integer.parseInt(startString));
       }
       catch (NumberFormatException e) {
         throw new HandlerException("`start' param to `List' must be a number");
       }
     }
 
     context.put("results", table.selection(table.whereClause(criteria),
                                            orderBySQL, false, start, 20));
 
     return context;
   }
 
   // return the 'navigation' admin page
   protected Template navigationTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "Navigation.wm");
   }
 
   protected Template popupTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(popup(context,melati), "PopupSelect.wm");
   }
 
   
   protected WebContext popup(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
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
 
     Vector orderingNames = new Vector();
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
         orderingNames.addElement(database.quotedName(info.getName()));
       }
 
       orderings.addElement(
           new Field(orderColumnID,
                     new BaseFieldAttributes(name, searchColumnsType)));
     }
 
     context.put("orderings", orderings);
 
     return context;
   }
 
   protected Template selectionWindowTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     context.put("database", PoemThread.database());
     context.put("table", melati.getTable());
     return adminTemplate(context, "SelectionWindow.wm");
   }
 
   // return primary select template
   protected Template selectionWindowPrimarySelectTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(primarySelect(context,melati), "SelectionWindowPrimarySelect.wm");
   }
 
   // return select template (a selection of records from a table)
   protected Template selectionWindowSelectionTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(selection(context,melati), "SelectionWindowSelection.wm");
   }
 
   protected Template columnCreateTemplate(WebContext context,
                                           Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     
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
 
     return adminTemplate(context, "CreateColumn.wm");
   }
 
   protected Template tableCreateTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
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
 
     return adminTemplate(context, "CreateTable.wm");
   }
 
   protected Template tableCreate_doitTemplate(WebContext context,
                                               Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     Database database = melati.getDatabase();
     database.addTableAndCommit(
         (TableInfo)create(database.getTableInfoTable(), context),
         context.getForm("field_troidName"));
 
     return adminTemplate(context, "CreateTable_doit.wm");
   }
 
   protected Template columnCreate_doitTemplate(final WebContext context,
                                                final Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
 
     Database db = melati.getDatabase();
 
     ColumnInfo columnInfo =
         (ColumnInfo)db.getColumnInfoTable().create(
             new Initialiser() {
               public void init(Persistent object)
                   throws AccessPoemException, ValidationPoemException {
                 Melati.extractFields(context, object);
               }
             });
 
     columnInfo.getTableinfo().actualTable().addColumnAndCommit(columnInfo);
 
     return adminTemplate(context, "CreateTable_doit.wm");
   }
 
   protected WebContext editingTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     melati.getObject().assertCanRead();
     context.put("object", melati.getObject());
     Database database = melati.getDatabase();
     context.put("database", database);
     context.put("table", melati.getTable());
     return context;
   }
 
   protected Template rightTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     return adminTemplate(editingTemplate(context,melati), "Right.wm");
   }
 
   protected Template editHeaderTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     return adminTemplate(editingTemplate(context,melati), "EditHeader.wm");
   }
 
   protected Template editTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     return adminTemplate(editingTemplate(context,melati), "Edit.wm");
   }
 
   protected Template addTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
 
     context.put("table", melati.getTable());
 
     Enumeration columns = melati.getTable().columns();
     Vector fields = new Vector();
     while (columns.hasMoreElements()) {
       Column column = (Column)columns.nextElement();
       String stringValue = context.getForm("field_" + column.getName());
       Object value = null;
       if (stringValue != null) value = column.getType().rawOfString(stringValue);
       fields.add(new Field(value, column));
     }
     context.put("fields", fields.elements());
 /*
     the lovely MappedEnumeration approach has been removed in order to allow fields
     to take default values passed on the query string :(
     Enumeration fields =
         new MappedEnumeration(melati.getTable().columns()) {
           public Object mapped(Object column) {
             return new Field((Object)null, (Column)column);
           }
         };
     context.put("fields", fields);
 */
     return adminTemplate(context, "Add.wm");
   }
 
   protected Template updateTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     Melati.extractFields(context, melati.getObject());
     return adminTemplate(context, "Update.wm");
   }
 
   protected Template addUpdateTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     create(melati.getTable(), context);
     return adminTemplate(context, "Update.wm");
   }
 
   protected Template deleteTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     try {
       melati.getObject().deleteAndCommit();
       return adminTemplate(context, "Update.wm");
     }
     catch (DeletionIntegrityPoemException e) {
       context.put("object", e.object);
       context.put("references", e.references);
       return adminTemplate(context, "DeleteFailure.wm");
     }
   }
 
    protected Template duplicateTemplate(WebContext context, Melati melati)
        throws NotFoundException, InvalidTypeException, PoemException {
      // FIXME the ORIGINAL object is the one that will get edited when the
      // update comes in from Edit.wm, because it will be identified from
      // the path info!
      
      Persistent dup = melati.getObject().duplicated();
      Melati.extractFields(context, dup);
      dup.getTable().create(dup);
      context.put("object", dup);
      return adminTemplate(context, "Update.wm");
    }
 
   protected Template modifyTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     String action = context.getRequest().getParameter("action");
     if ("Update".equals(action))
       return updateTemplate(context, melati);
     else if ("Delete".equals(action))
       return deleteTemplate(context, melati);
     else if ("Duplicate".equals(action))
       return duplicateTemplate(context, melati);
     else
       throw new HandlerException("bad action from Edit.wm: " + action);
   }
 
   protected Template handle(WebContext context, Melati melati)
       throws Exception {
     Capability admin = PoemThread.database().getCanAdminister();
     AccessToken token = PoemThread.accessToken();
     if (!token.givesCapability(admin)) 
       throw new AccessPoemException(token,admin);
 
     context.put("admin", melati.getAdminUtils());
     if (melati.getObject() != null) {
       if (melati.getMethod().equals("Right"))
         return rightTemplate(context, melati);
       if (melati.getMethod().equals("EditHeader"))
         return editHeaderTemplate(context, melati);
       if (melati.getMethod().equals("Edit"))
         return editTemplate(context, melati);
       else if (melati.getMethod().equals("Update"))
         return modifyTemplate(context, melati);
       else if (melati.getObject() instanceof AdminSpecialised) {
         Template it =
             ((AdminSpecialised)melati.getObject()).adminHandle(
                 melati, melati.getHTMLMarkupLanguage());
         if (it != null) return it;
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
 	return mainTemplate(context, melati);
       if (melati.getMethod().equals("Top"))
 	return topTemplate(context, melati);
       if (melati.getMethod().equals("Create"))
 	return tableCreateTemplate(context, melati);
       if (melati.getMethod().equals("Create_doit"))
 	return tableCreate_doitTemplate(context, melati);
       if (melati.getMethod().equals("CreateColumn"))
         return columnCreateTemplate(context, melati);
       if (melati.getMethod().equals("CreateColumn_doit"))
         return columnCreate_doitTemplate(context, melati);
     }
 
     throw new InvalidUsageException(this, melati.getContext());
   }
 }
