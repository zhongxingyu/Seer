 package org.otherobjects.cms.binding;
 
 import java.beans.PropertyEditorSupport;
import java.util.regex.Pattern;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.model.CompositeDatabaseId;
 import org.otherobjects.cms.util.IdentifierUtils;
 import org.springframework.util.StringUtils;
 
 public class EntityReferenceEditor extends PropertyEditorSupport
 {
     private Class<?> type;
     private DaoService daoService;
 
     public EntityReferenceEditor(DaoService daoService, Class<?> type)
     {
         this.daoService = daoService;
         this.type = type;
     }
 
    private static Pattern pattern = Pattern.compile("^\\d+$");

     /**
      * Lookup an Entity from an id string.
      */
     @SuppressWarnings("unchecked")
     public void setAsText(String id) throws IllegalArgumentException
     {
         if (!StringUtils.hasText(id))
         {
             // Treat empty String as null value.
             setValue(null);
         }
         else
         {
             CompositeDatabaseId compositeDatabaseId = IdentifierUtils.getCompositeDatabaseId(id);
             if (compositeDatabaseId == null)
             {
                 throw new IllegalArgumentException("Not a valid ID: " + id);
             }
             else
             {
                 Object value = daoService.getDao(type).get(compositeDatabaseId.getId());
                 setValue(value);
             }
         }
     }
 
     /**
      * Format the Entity as a String, using its id value.
      * //TODO we might need a marker interface for Entities 
      */
     public String getAsText()
     {
         boolean hasId = false;
         try
         {
             hasId = getValue().getClass().getMethod("getEditableId") != null;
         }
         catch (Exception e)
         {
             // do nothing
         }
 
         if (hasId)
         {
             String id = null;
             try
             {
                 id = (String) PropertyUtils.getSimpleProperty(getValue(), "editableId");
             }
             catch (Exception e)
             {
                 // do nothing
             }
 
             return id;
         }
         else
             return null;
     }
 }
