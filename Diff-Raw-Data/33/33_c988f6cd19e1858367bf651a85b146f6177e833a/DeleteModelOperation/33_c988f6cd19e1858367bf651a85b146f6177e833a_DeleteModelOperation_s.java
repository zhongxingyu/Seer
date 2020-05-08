 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010, 2012 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *   Dzmitry [zmicer] Harachka - implementation
  * </copyright>
  *
  * $Id: DeleteModelOperation.java,v 1.8 2011/09/21 14:04:56 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.service;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.provider.IdProvider;
 import org.eclipse.emf.texo.server.model.response.ResponseModelPackage;
 import org.eclipse.emf.texo.server.model.response.ResultType;
 import org.eclipse.emf.texo.utils.ModelUtils;
 
 /**
  * The model operation which takes care of deleting data.
  * 
  * The following type of request uris are supported here:
  * <ul>
  * <li>http://www.test.com/XMLService?query=select o from Order&firstResult=10&maxResults=50 a query with paging
  * parameters</li>
  * <li>http://www.test.com/XMLService?id=http://www.test.com/XMLService/Order/123 an uri with an id parameter
  * <li>http://www.test.com/XMLService/Order/123 a direct uri with a type and id</li>
  * <li>http://www.test.com/XMLService with the objects to be deleted as posted content</li>
  * </ul>
  * 
  * The response of this web service is an instance of the {@link ResultType} with the deleted
  * {@link ResultType#getDeleted()} set.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.8 $
  */
 public class DeleteModelOperation extends ModelOperation {
 
   @Override
   @SuppressWarnings("unchecked")
   protected void internalExecute() {
     final List<Object> toDelete;
     // 1) there is a query!
    if (getServiceContext().getRequestParameters().containsKey(ServiceConstants.PARAM_QUERY)) {
      final String qryStr = (String) getServiceContext().getRequestParameters().get(ServiceConstants.PARAM_QUERY);
 
       getServiceContext().getServiceOptions().checkFalse(ServiceOptions.OPTION_ALLOW_RETRIEVE_QUERIES);
 
       // check the query
       ComponentProvider.getInstance().newInstance(QueryChecker.class).checkQuery(qryStr);
 
       toDelete = (List<Object>) getObjectStore().query(qryStr, new HashMap<String, Object>(), getFirstResult(),
           getMaxResults());
     } else if (getServiceContext().getRequestParameters().containsKey(ServiceConstants.PARAM_ID)) {
       // an id which must be a uri
       final URI uri = URI.createURI((String) getServiceContext().getRequestParameters().get(ServiceConstants.PARAM_ID));
       final Object object = getObjectStore().fromUri(uri);
       toDelete = Collections.singletonList(object);
     } else {
       final String[] segments = ServiceUtils.getSegments(getServiceContext().getServiceRequestURI());
       if (segments.length < 2) {
         // try posted content
         toDelete = getServiceContext().getRequestData();
       } else if (segments.length == 2) {
         // 3) there is specific type with an id
         final EClass eClass = ModelUtils.getEClassFromQualifiedName(segments[0]);
         final String idString = segments[1];
         final Object id = IdProvider.getInstance().convertIdStringToId(eClass, idString);
         final Object object = getObjectStore().get(eClass, id);
         toDelete = Collections.singletonList(object);
       } else {
         throw new IllegalArgumentException("Service path " + getServiceContext().getServiceRequestURI() //$NON-NLS-1$
             + " not supported, uri " + getServiceContext().getRequestURI()); //$NON-NLS-1$
       }
     }
 
     final ResultType result = ResponseModelPackage.INSTANCE.getModelFactory().createResultType();
 
     for (Object o : toDelete) {
       getObjectStore().remove(o);
       result.getDeleted().add(o);
     }
 
     getServiceContext().setResultInResponse(result);
   }
 
 }
