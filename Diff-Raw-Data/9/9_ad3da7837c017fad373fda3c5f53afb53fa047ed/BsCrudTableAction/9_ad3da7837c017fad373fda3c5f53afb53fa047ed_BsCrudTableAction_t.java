 package org.codelibs.crud.base.action;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.codelibs.crud.base.CommonConstants;
 import org.codelibs.crud.base.CrudMessageException;
 import org.codelibs.crud.base.util.SAStrutsUtil;
 import org.codelibs.crud.db.exentity.CrudTable;
 import org.codelibs.crud.form.CrudTableForm;
 import org.codelibs.crud.pager.CrudTablePager;
 import org.codelibs.crud.service.CrudTableService;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.seasar.framework.beans.util.Beans;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.struts.annotation.ActionForm;
 import org.seasar.struts.annotation.Execute;
 import org.seasar.struts.exception.ActionMessagesException;
 
 public class BsCrudTableAction implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log log = LogFactory.getLog(BsCrudTableAction.class);
 
     // for list
 
     public List<CrudTable> crudTableItems;
 
     // for edit/confirm/delete
 
     @ActionForm
     @Resource
     protected CrudTableForm crudTableForm;
 
     @Resource
     protected CrudTableService crudTableService;
 
     @Resource
     protected CrudTablePager crudTablePager;
 
     protected String displayList(boolean redirect) {
         // page navi
         try {
             crudTableItems = crudTableService.getCrudTableList(crudTablePager);
         } catch (Exception e) {
             crudTablePager.clear();
             log.warn("Could not retrieve the data", e);
             throw new ActionMessagesException(
                     "errors.crud_failed_to_retrieve_crud_table");
         }
 
         // restore from pager
         Beans.copy(crudTablePager, crudTableForm.searchParams)
             .excludes(CommonConstants.PAGER_CONVERSION_RULE)
     /* CRUD: BEGIN
             #if(${table.converterToSearchParams})${table.converterToSearchParams}#end##
        CRUD: END */
             .execute();
 
         if (redirect) {
             return "index?redirect=true";
         } else {
             return "index.jsp";
         }
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String index() {
         return displayList(false);
     }
 
     @Execute(validator = true, input = "error.jsp", urlPattern = "list/{pageNumber}")
     public String list() {
         // page navi
         if (StringUtil.isNotBlank(crudTableForm.pageNumber)) {
             try {
                 crudTablePager.setCurrentPageNumber(Integer
                         .parseInt(crudTableForm.pageNumber));
             } catch (NumberFormatException e) {
                 if (log.isDebugEnabled()) {
                     log.debug("Invalid value: " + crudTableForm.pageNumber, e);
                 }
             }
         }
 
         return displayList(false);
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String search() {
         Beans.copy(crudTableForm.searchParams, crudTablePager).excludes(
                 CommonConstants.PAGER_CONVERSION_RULE).excludesWhitespace()
     /* CRUD: BEGIN
             #if(${table.converterToPager})${table.converterToPager}#end##
        CRUD: END */
                 .execute();
 
         return displayList(false);
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String reset() {
         crudTablePager.clear();
 
         return displayList(false);
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String back() {
         return displayList(false);
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String editagain() {
         return "edit.jsp";
     }
 
     /* CRUD COMMENT: BEGIN */
     @Execute(validator = true, input = "error.jsp", urlPattern = "confirmpage/{crudMode}/{id}")
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
     @Execute(validator = true, input = "error.jsp", urlPattern = "confirmpage/{crudMode}/{${table.primaryKeyPath}}")
        CRUD: END */
     public String confirmpage() {
         if (crudTableForm.crudMode != CommonConstants.CONFIRM_MODE) {
             throw new ActionMessagesException("errors.crud_invalid_mode",
                     new Object[] { CommonConstants.CONFIRM_MODE,
                             crudTableForm.crudMode });
         }
 
         loadCrudTable();
 
         return "confirm.jsp";
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String createpage() {
         // page navi
         crudTableForm.initialize();
         crudTableForm.crudMode = CommonConstants.CREATE_MODE;
 
         return "edit.jsp";
     }
 
     /* CRUD COMMENT: BEGIN */
     @Execute(validator = true, input = "error.jsp", urlPattern = "editpage/{crudMode}/{id}")
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
     @Execute(validator = true, input = "error.jsp", urlPattern = "editpage/{crudMode}/{${table.primaryKeyPath}}")
        CRUD: END */
     public String editpage() {
         if (crudTableForm.crudMode != CommonConstants.EDIT_MODE) {
             throw new ActionMessagesException("errors.crud_invalid_mode",
                     new Object[] { CommonConstants.EDIT_MODE,
                             crudTableForm.crudMode });
         }
 
         loadCrudTable();
 
         return "edit.jsp";
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String editfromconfirm() {
         crudTableForm.crudMode = CommonConstants.EDIT_MODE;
 
         loadCrudTable();
 
         return "edit.jsp";
     }
 
     @Execute(validator = true, input = "edit.jsp")
     public String confirmfromcreate() {
         return "confirm.jsp";
     }
 
     @Execute(validator = true, input = "edit.jsp")
     public String confirmfromupdate() {
         return "confirm.jsp";
     }
 
     /* CRUD COMMENT: BEGIN */
     @Execute(validator = true, input = "error.jsp", urlPattern = "deletepage/{crudMode}/{id}")
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
     @Execute(validator = true, input = "error.jsp", urlPattern = "deletepage/{crudMode}/{${table.primaryKeyPath}}")
        CRUD: END */
     public String deletepage() {
         if (crudTableForm.crudMode != CommonConstants.DELETE_MODE) {
             throw new ActionMessagesException("errors.crud_invalid_mode",
                     new Object[] { CommonConstants.DELETE_MODE,
                             crudTableForm.crudMode });
         }
 
         loadCrudTable();
 
         return "confirm.jsp";
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String deletefromconfirm() {
         crudTableForm.crudMode = CommonConstants.DELETE_MODE;
 
         loadCrudTable();
 
         return "confirm.jsp";
     }
 
     @Execute(validator = true, input = "edit.jsp")
     public String create() {
         try {
             CrudTable crudTable = createCrudTable();
             crudTableService.store(crudTable);
             SAStrutsUtil.addSessionMessage("success.crud_create_crud_table");
 
             return displayList(true);
         } catch (ActionMessagesException e) {
             log.error(e.getMessage(), e);
             throw e;
         } catch (CrudMessageException e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(e.getMessageId(), e.getArgs());
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(
                     "errors.crud_failed_to_create_crud_table");
         }
     }
 
     @Execute(validator = true, input = "edit.jsp")
     public String update() {
         try {
             CrudTable crudTable = createCrudTable();
             crudTableService.store(crudTable);
             SAStrutsUtil.addSessionMessage("success.crud_update_crud_table");
 
             return displayList(true);
         } catch (ActionMessagesException e) {
             log.error(e.getMessage(), e);
             throw e;
         } catch (CrudMessageException e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(e.getMessageId(), e.getArgs());
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(
                     "errors.crud_failed_to_update_crud_table");
         }
     }
 
     @Execute(validator = true, input = "error.jsp")
     public String delete() {
         if (crudTableForm.crudMode != CommonConstants.DELETE_MODE) {
             throw new ActionMessagesException("errors.crud_invalid_mode",
                     new Object[] { CommonConstants.DELETE_MODE,
                             crudTableForm.crudMode });
         }
 
         try {
             CrudTable crudTable = crudTableService.getCrudTable(createKeyMap());
             if (crudTable == null) {
                 // throw an exception
                 throw new ActionMessagesException(
                         "errors.crud_could_not_find_crud_table",
     /* CRUD COMMENT: BEGIN */
                         new Object[] { crudTableForm.id });
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
                         new Object[] {##
         #set( $separatorFlag = "false" )##
         #foreach( $pKey in ${table.getPrimaryKeyList()} )##
           #if(${separatorFlag} == "true")##
                         + ", " +
           #else##
             #set( $separatorFlag = "true" )##
           #end##
                         crudTableForm.${pKey}
         #end##
                         });
        CRUD: END */
             }
 
             crudTableService.delete(crudTable);
             SAStrutsUtil.addSessionMessage("success.crud_delete_crud_table");
 
             return displayList(true);
         } catch (ActionMessagesException e) {
             log.error(e.getMessage(), e);
             throw e;
         } catch (CrudMessageException e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(e.getMessageId(), e.getArgs());
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new ActionMessagesException(
                     "errors.crud_failed_to_delete_crud_table");
         }
     }
 
     protected void loadCrudTable() {
 
         CrudTable crudTable = crudTableService.getCrudTable(createKeyMap());
         if (crudTable == null) {
             // throw an exception
             throw new ActionMessagesException(
                     "errors.crud_could_not_find_crud_table",
     /* CRUD COMMENT: BEGIN */
                     new Object[] { crudTableForm.id });
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
                     new Object[] {##
         #set( $separatorFlag = "false" )##
         #foreach( $pKey in ${table.getPrimaryKeyList()} )##
           #if(${separatorFlag} == "true")##
                         + ", " +
           #else##
             #set( $separatorFlag = "true" )##
           #end##
                         crudTableForm.${pKey}
         #end##
                         });
        CRUD: END */
         }
 
         Beans.copy(crudTable, crudTableForm).excludes("searchParams", "mode")
     /* CRUD: BEGIN
             #if(${table.converterToActionForm})${table.converterToActionForm}#end##
        CRUD: END */
                 .execute();
     }
 
     protected CrudTable createCrudTable() {
         CrudTable crudTable;
         if (crudTableForm.crudMode == CommonConstants.EDIT_MODE) {
             crudTable = crudTableService.getCrudTable(createKeyMap());
             if (crudTable == null) {
                 // throw an exception
                 throw new ActionMessagesException(
                         "errors.crud_could_not_find_crud_table",
     /* CRUD COMMENT: BEGIN */
                         new Object[] { crudTableForm.id });
     /* CRUD COMMENT: END */
     /* CRUD: BEGIN
                         new Object[] {##
         #set( $separatorFlag = "false" )##
         #foreach( $pKey in ${table.getPrimaryKeyList()} )##
           #if(${separatorFlag} == "true")##
                         + ", " +
           #else##
             #set( $separatorFlag = "true" )##
           #end##
                         crudTableForm.${pKey}
         #end##
                         });
        CRUD: END */
             }
         } else {
             crudTable = new CrudTable();
         }
        updateCrudTable(crudTable);

        return crudTable;
    }

    protected void updateCrudTable(CrudTable crudTable) {
         Beans.copy(crudTableForm, crudTable).excludes("searchParams", "mode")
     /* CRUD: BEGIN
             #if(${table.converterToEntity})${table.converterToEntity}#end##
        CRUD: END */
                 .execute();
     }
 
     protected Map<String, String> createKeyMap() {
         Map<String, String> keys = new HashMap<String, String>();
 
         /* CRUD COMMENT: BEGIN */
         keys.put("id", crudTableForm.id);
         /* CRUD COMMENT: END */
 
         /* CRUD: BEGIN
         #foreach( $column in ${table.getColumnList()} )
         #if(${column.primaryKey})
         keys.put("${column.propertyName}", crudTableForm.${column.propertyName});
         #end
         #end
            CRUD: END */
 
         return keys;
     }
 }
