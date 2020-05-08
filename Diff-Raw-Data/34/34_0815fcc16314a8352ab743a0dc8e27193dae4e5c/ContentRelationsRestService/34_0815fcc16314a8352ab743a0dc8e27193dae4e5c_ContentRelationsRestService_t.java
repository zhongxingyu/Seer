 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
  * for the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 package org.escidoc.core.content.relation;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBElement;
 
import org.escidoc.core.domain.predicate.list.PredicatesTypeTO;
 import org.escidoc.core.domain.sru.ResponseTypeTO;
 import org.escidoc.core.domain.sru.parameters.SruSearchRequestParametersBean;
 
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 /**
  * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
  */
 @Path("/ir/content-relations")
 public interface ContentRelationsRestService {
 
     /**
      * The list of all content-relations matching the given filter criteria will be created.
      * <p/>
      * <br/> See chapter "Filters" for detailed information about filter definitions.
      *
      * @param parameters       The Standard SRU Get-Parameters as Object
      * @param userId           The custom SRU Get Parameter x-info5-userId
      * @param roleId           The custom SRU Get Parameter x-info5-roleId
      * @param omitHighlighting The custom SRU Get Parameter x-info5-omitHighlighting
      * @return The XML representation of the the filtered list of content-relations corresponding to SRW schema as
      *         JAXBElement.
      * @throws InvalidSearchQueryException thrown if the given search query could not be translated into a CQL query
      * @throws SystemException             Thrown if a framework internal error occurs.
      */
     @GET
     @Produces(MediaType.TEXT_XML)
     JAXBElement<? extends ResponseTypeTO> retrieveContentRelations(
         @QueryParam("") SruSearchRequestParametersBean parameters,
         @QueryParam("x-info5-roleId") String roleId,
         @QueryParam("x-info5-userId") String userId, 
         @QueryParam("x-info5-omitHighlighting") String omitHighlighting) throws InvalidSearchQueryException,
             SystemException;
 
     @GET
     @Path("/retrieve-registered-predicates")
     @Produces(MediaType.TEXT_XML)
     JAXBElement<PredicatesTypeTO> retrieveRegisteredPredicates() throws InvalidContentException, InvalidXmlException, SystemException;
 
 }
