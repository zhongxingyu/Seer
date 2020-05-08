 /* IdentifierType.java - created on Mar 18, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.qualifier;
 
 /**
  * Qualifier for resource identifiers.
  * 
  * @author Nuno Freire <nfreire@gmail.com>
  * @since Mar 18, 2011
  */
 public enum IdentifierType {
     /**
      * ISBN
      */
     ISBN,
     /**
      * ISSN
      */
     ISSN,
     /**
      * an URN (if the URN represents an ISBN or ISSN, than the ISBN/ISBN should be extracted from
      * the URN
      */
     URN,
     /**
      * DOI - Digital Object Identifier
      */
     DOI,
     /**
      * Legal deposit number
      */
     LEGAL_DEPOSIT,
     /**
      * a general URI (should be used only if the specific type of URI is not known (for example
      * URL))
      */
     URI,
     /**
      * URL
      */
     URL,
     /**
      * Serial Item and Contribution Identifier
      */
     SICI,
     /**
      * An URL linking to an OpenURL service
      */
     OPEN_URL,
     /**
      * A persistent URL (see purl.org)
      */
     PURL,
     /**
      * NBN - National bibliography number
      */
     NBN,
     /**
      * A identifier unique in the local system at the data provider (e.g. Control number)
      */
     LOCAL_IDENTIFIER,
     /**
      * The identifier is unknown or was not specified
      */
     OTHER,
     /**
      * The identifier used in the oaipmh frame
      */
     OAIPMH,
     /**
      * This identifier should be used to display the EOD button, and be sent to the EOD service link
      * This identifier should not be displayed. If it is to be displayed, the identifier is
      * duplicated in another identifier object in the record.
      */
    EOD,
    /**
     * identifier representing cluster group (detailed types are insided identifier)
     */
    CLUSTER
 }
