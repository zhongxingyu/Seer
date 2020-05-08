 /**
  * @(#)ResourceTypeField.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.app.program.resource.db;
 
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 
 /**
  *  ResourceTypeField - .
  */
 public class ResourceTypeField extends StringPopupField
 {
     public static final String LIST_RESOURCE_BUNDLE = "ListResourceBundle";
     public static final String PROPERTIES = "Properties";
     /**
      * Default constructor.
      */
     public ResourceTypeField()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public ResourceTypeField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         super.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
     * Set this field to its initial value.
     */
    public int initField(boolean bDisplayOption)
    {
        return this.setString(ResourceTypeField.PROPERTIES, bDisplayOption, DBConstants.INIT_MOVE);
    }
    /**
      * Get the conversion Map.
      */
     public String[][] getPopupMap()
     {
         String[][] string = {
             {LIST_RESOURCE_BUNDLE, "ListResourceBundle"},
             {PROPERTIES, "Properties"}
         };
         return string;
     }
 
 }
