 /* TextType.java - created on Mar 18, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package org.theeuropeanlibrary.model.common.qualifier;
 
 /**
  * Qualifier for text values.
  * 
  * @author Nuno Freire <nfreire@gmail.com>
  * @since Mar 18, 2011
  */
 public enum TextRelation {
     /**
      * The abstract of the resource
      */
     ABSTRACT,
     /**
      * Any kind of description of the resource
      */
     DESCRIPTION,
     /**
      * The table of contents of the resource
      */
     TABLE_OF_CONTENTS,
     /**
      * Rights information. If encoded according to Europeana guidelines, the Link object (with LinkTarget.RIGHTS qualifier) should be
      * used to encode the data, instead of this one
      */
     RIGHTS_UNCONTROLED,
     /**
      * resource type. if encoded according to TEL normalized value
      */
     TYPE_UNCONTROLED,
     /**
      * resource format. if encoded according to TEL normalized value
      */
     FORMAT_UNCONTROLED,
     /**
      * a medium value without encoding scheme
      */
     MEDIUM_UNCONTROLED,
     /**
      * A language value that was not recognized
      */
     LANGUAGE_UNCONTROLED,
     /**
      * see dc:extent
      */
     EXTENT,
 
     /**
      * see dc:source
      */
     SOURCE,
     /**
      * see dc:bibliographicCitation
      */
     BIBLIOGRAPHIC_CITATION,
     /**
      * see dc:provenance
      */
     PROVENANCE,
     /**
      * see dc:audience
      */
     AUDIENCE,
     /**
      * A subcollection within the Collection 8as used by TEL in UIM)
      */
     SUBCOLLECTION,
     /**
      * The statement of responsibility of an object (the name of the authors as written in the cover of a book, for example) 
      */
     STATEMENT_OF_RESPONSIBILITY,
 
     /** 
      * see dc:relation
      */
     RELATION, 
     /** 
      * see mods location
      */
     LOCATION,
     /** 
      * A note made by the cataloguer. See NoteType enum for the type of note.
      */
     NOTE, 
     /** 
      * Information about a thesis
      *  */
     DISSERTATION_STATEMENT,
     /** 
      * Information about a thesis
      *  */
     DISSERTATION_DEGREE,
     /** 
      * Information about a thesis
      *  */
     DISSERTATION_DISCIPLINE, 
     /** 
      * Scale of a map or visual material
      *  */
     SCALE, 
     /** 
      * Information known about the item that may be used to determine copyright status. 
      * */
     COPYRIGHT_STATUS, 
     /** Cartographic MATHEMATICAL DATA, scale, projection, coordinates  */
     CARTOGRAPHIC_DATA,
     /** Cartographic projection  */
     PROJECTION, 
     /** Cartographic coordinates  */
     COORDINATES, 
     /** Cartographic zone for celestial charts*/
     CELESTIAL_ZONE, 
     /** Cartographic equiox  */
     EQUINOX, 
     /**  The numbering and/or dates of coverage of the first and last issues of a serial when the numbers and dates are known */
     NUMBERING_SERIALS,
     /**
      * Series that the resource is part of
      */
    SERIES,
 }
