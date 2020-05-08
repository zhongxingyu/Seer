 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
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
 
 package de.escidoc.core.om.service.interfaces;
 
 import de.escidoc.core.common.exceptions.EscidocException;
 
 /**
  * Interface of an resource ingest handler.
  *
  * @author Steffen Wagner
  * @author Kai Strnad
  */
 public interface IngestHandlerInterface {
 
     /**
      * Ingests a resource<br/>
      * <p/>
     * The resource to be ingested can be either one of Item, Container, Organizational Unit or Context.<br/>
      * <p/>
      * If the XML document is an eScicDoc Item, it can be directly released by setting its public-status to
      * "released".<br/>
      * <p/>
      * <b>Prerequisites:</b><br/> <ol> <li>The XML document to be ingested must exist and validate against its respective
      * XML schema. </li> <li> If the generation of PIDs is turned on, the resource to be ingested is an Item and it has
      * public-status "released", then the resource must contain a PID. </li> </ol> <br/> <b>Tasks:</b><br/> See the
      * documentation of the respective resource.
      *
      * @param xmlData The XML representation of the resource to be ingested. The resource can be either one of Item,
     *                Container, Organizational Unit or Context.
      * @return Returns an XML document with the generated object id (objid).
      * @throws EscidocException Thrown if the ingest fails for any reason.
      */
     String ingest(String xmlData) throws EscidocException;
 
 }
