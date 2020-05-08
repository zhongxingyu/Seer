 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api;
 
import org.apache.commons.lang.Validate;

public final class JiveUris
 {
    private JiveUris()
    {
    }
    
     /**
      * Generates the complete uri for the get or delete service.
      * 
      * @param id A {@link String} containing the path parameters to add
      * @return The resouce uri with the path parameters added
      */
     public static String getOperationUri(String baseOperationUri, final String id)
     {
        Validate.notNull(baseOperationUri);
        return id == null ? baseOperationUri : baseOperationUri + "/" + JiveIds.toPathVariable(id);
     }
 
 }
