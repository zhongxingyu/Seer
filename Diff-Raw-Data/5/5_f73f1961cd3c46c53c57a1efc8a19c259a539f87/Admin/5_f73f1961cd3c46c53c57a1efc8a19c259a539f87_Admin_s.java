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
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.admin;
 
 import java.util.*;
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
   
   // return the 'Main' admin page
   protected Template mainTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Main.wm");
   }
   
   // return the 'LowerFrame' admin page
   protected Template lowerFrameTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     final Table table = melati.getTable();
     context.put("table", table);
     return adminTemplate(context, "LowerFrame.wm");
   }
 
   protected Template tablesViewTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     context.put("database", PoemThread.database());
     return adminTemplate(context, "Tables.wm");
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
             database.getColumnInfoTable().getNameColumn().getType(), null));
 
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
         context.getForm("field-troidName"));
 
     return adminTemplate(context, "CreateTable_doit.wm");
   }
 
   protected Template tableListTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(tableList(context,melati), "Select.wm");
   }
 
   protected Template popupTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     return adminTemplate(tableList(context,melati), "PopupSelect.wm");
   }
   
   protected WebContext tableList(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException,
              HandlerException {
     final Table table = melati.getTable();
     context.put("table", table);
 
     final Database database = table.getDatabase();
     context.put("database", database);
 
     // sort out search criteria
 
     final Persistent criteria = table.newPersistent();
 
     for (Enumeration c = table.columns(); c.hasMoreElements();) {
       Column column = (Column)c.nextElement();
       String string = context.getForm("field-" + column.getName());
       if (string != null && !string.equals(""))
         column.setRaw_unsafe(criteria, column.getType().rawOfString(string));
     }
 
    MappedEnumeration criteria =
         new MappedEnumeration(table.getSearchCriterionColumns()) {
 	  public Object mapped(Object c) {
 	    Column column = (Column)c;
 	    final PoemType nullable = column.getType().withNullable(true);
 	    return
 		new Field(column.getRaw(criteria), column) {
 		  public PoemType getType() {
 		    return nullable;
 		  }
 		};
 	  }
 	};
 
    context.put("criteria", EnumUtils.vectorOf(criteria));
 
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
       String orderColumnIDString = context.getForm("field-" + name);
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
 
     String orderByClause = EnumUtils.concatenated(", ",
                                                   orderingNames.elements());
 
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
 
     context.put("objects", table.selection(table.whereClause(criteria),
                                            orderByClause, false, start, 20));
 
     return context;
   }
 
   protected Template columnCreateTemplate(WebContext context,
                                           Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     
     final ColumnInfoTable cit = melati.getDatabase().getColumnInfoTable();
     final Column tic = cit.getTableinfoColumn();
     final Column typeColumn = cit.getTypeColumn();
 
     Enumeration columnInfoFields =
         new MappedEnumeration(cit.columns()) {
           public Object mapped(Object column) {
 /*            if (column == tic)
               // What does this do??!
               return new Field(
                   (Object)null,
                   new BaseFieldAttributes(
                       tic.getName(), tic.getDisplayName(), tic.getDescription(),
                       tic.getType(), tic.getRenderInfo()));
             else 
 */
 			if (column == typeColumn)
               return new Field(PoemTypeFactory.STRING.getCode(), typeColumn);
             else
               return new Field((Object)null, (FieldAttributes)column);
           }
         };
 
     context.put("columnInfoFields", columnInfoFields);
 
     return adminTemplate(context, "CreateColumn.wm");
   }
 
   protected Template columnCreate_doitTemplate(final WebContext context,
                                                final Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
 
     ColumnInfo columnInfo =
         (ColumnInfo)melati.getDatabase().getColumnInfoTable().create(
             new Initialiser() {
               public void init(Persistent object)
                   throws AccessPoemException, ValidationPoemException {
                 ((ColumnInfo)object).setTableinfoTroid(
                     melati.getTable().tableInfoID());
                 Melati.extractFields(context, object);
               }
             });
 
     melati.getTable().addColumnAndCommit(columnInfo);
 
     return adminTemplate(context, "CreateTable_doit.wm");
   }
 
   protected Template editTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
     melati.getObject().assertCanRead();
     context.put("object", melati.getObject());
     Database database = melati.getDatabase();
     context.put("database", database);
     return adminTemplate(context, "Edit.wm");
   }
 
   protected Template addTemplate(WebContext context, Melati melati)
       throws NotFoundException, InvalidTypeException, PoemException {
 
     context.put("table", melati.getTable());
 
     Enumeration fields =
         new MappedEnumeration(melati.getTable().columns()) {
           public Object mapped(Object column) {
             return new Field((Object)null, (Column)column);
           }
         };
     context.put("fields", fields);
 
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
      
      melati.getObject().duplicated();
      context.put("object", melati.getObject());
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
       throws PoemException, HandlerException {
     try {
       context.put("admin",
                   new AdminUtils(context.getRequest().getServletPath(),
                                  melati.getLogicalDatabaseName()));
 
       if (melati.getObject() != null) {
         if (melati.getMethod().equals("Edit"))
           return editTemplate(context, melati);
         else if (melati.getMethod().equals("Update"))
           return modifyTemplate(context, melati);
       }
       else if (melati.getTable() != null) {
         if (melati.getMethod().equals("View"))
           return tableListTemplate(context, melati);
         if (melati.getMethod().equals("LowerFrame"))
           return lowerFrameTemplate(context, melati);
         if (melati.getMethod().equals("PopUp"))
           return popupTemplate(context, melati);
         else if (melati.getMethod().equals("Add"))
           return addTemplate(context, melati);
         else if (melati.getMethod().equals("AddUpdate"))
           return addUpdateTemplate(context, melati);
       }
       else {
         if (melati.getMethod().equals("Main"))
           return mainTemplate(context, melati);
         if (melati.getMethod().equals("View"))
           return tablesViewTemplate(context, melati);
         else if (melati.getMethod().equals("Create"))
           return tableCreateTemplate(context, melati);
         else if (melati.getMethod().equals("Create_doit"))
           return tableCreate_doitTemplate(context, melati);
         else if (melati.getMethod().equals("CreateColumn"))
           return columnCreateTemplate(context, melati);
         else if (melati.getMethod().equals("CreateColumn_doit"))
           return columnCreate_doitTemplate(context, melati);
       }
 
       throw new InvalidUsageException(this, melati.getContext());
     }
     catch (PoemException e) {
       // we want to let these through untouched, since MelatiServlet handles
       // AccessPoemException specially ...
       throw e;
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new HandlerException("Bollocks: " + e);
     }
   }
 }
