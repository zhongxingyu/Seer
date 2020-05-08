 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.converter.collection;
 
 import java.util.List;
 
 import org.ow2.sirocco.apis.rest.cimi.converter.CimiConverter;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiArray;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiOperation;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiResource;
 import org.ow2.sirocco.apis.rest.cimi.domain.Operation;
 import org.ow2.sirocco.apis.rest.cimi.domain.collection.CimiCollection;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.model.cimi.Resource;
 
 /**
  * Abstract class to convert the collection of the CIMI model and the service
  * model in both directions.
  * <p>
  * Converted classes:
  * <ul>
  * <li>CIMI model: {@link CimiCollection<E>}</li>
  * <li>Service model: {@link Resource}</li>
  * </ul>
  * </p>
  */
 public abstract class CollectionConverterAbstract implements CimiConverter {
 
     /**
      * Fill the common data from a service collection to a CIMI collection.
      * 
      * @param <E> CimiResource
      * @param context The current context
      * @param dataService Source service collection
      * @param dataCimi Destination CIMI collection
      */
     protected <E extends CimiResource> void fill(final CimiContext context, final Object dataService,
         final CimiCollection<E> dataCimi) {
         if (true == context.mustBeExpanded(dataCimi)) {
             dataCimi.setResourceURI(dataCimi.getExchangeType().getResourceURI());
             dataCimi.setId(context.makeHrefBase(dataCimi));
             this.addOperations(context, dataService, dataCimi);
         }
         if (true == context.mustBeReferenced(dataCimi)) {
             dataCimi.setHref(context.makeHrefBase(dataCimi));
         }
     }
 
     /**
      * Add default CIMI operations : ADD.
      * 
      * @param <E> CimiResource
      * @param context The current context
      * @param dataService Source service collection
      * @param dataCimi Destination CIMI collection
      */
     protected <E extends CimiResource> void addOperations(final CimiContext context, final Object dataService,
         final CimiCollection<E> dataCimi) {
         String href = context.makeHrefBase(dataCimi);
         dataCimi.add(new CimiOperation(Operation.ADD.getRel(), href));
     }
 
     /**
      * Copy data from a service collection to a CIMI collection.
      * 
      * @param context The current context
      * @param dataService Source service collection
      * @param dataCimi Destination CIMI collection
      */
     @SuppressWarnings("unchecked")
     protected <E extends CimiResource> void doCopyToCimi(final CimiContext context, final List<Object> collectionService,
         final CimiCollection<E> collectionCimi) {
         this.fill(context, collectionService, collectionCimi);
         if (true == context.mustBeExpanded(collectionCimi)) {
             if ((null != collectionService) && (collectionService.size() > 0)) {
                CimiArray<E> cimiList = collectionCimi.newCollection();
                 for (Object serviceItem : collectionService) {
                     cimiList.add((E) context.convertNextCimi(serviceItem, collectionCimi.getItemClass()));
                 }
                collectionCimi.setCollection(cimiList);
             }
         }
     }
 
     /**
      * Copy data from a CIMI collection to a service collection.
      * 
      * @param context The current context
      * @param dataCimi Source CIMI collection
      * @param dataService Destination Service collection
      */
     protected <E extends CimiResource> void doCopyToService(final CimiContext context, final CimiCollection<E> collectionCimi,
         final List<Object> collectionService) {
         CimiArray<E> cimiList = collectionCimi.getCollection();
         if ((null != cimiList) && (cimiList.size() > 0)) {
             for (E cimiItem : cimiList) {
                 collectionService.add(context.convertNextService(cimiItem));
             }
         }
     }
 }
