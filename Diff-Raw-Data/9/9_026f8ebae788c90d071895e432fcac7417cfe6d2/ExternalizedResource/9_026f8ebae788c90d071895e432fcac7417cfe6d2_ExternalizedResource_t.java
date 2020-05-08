 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.externalizer;
 
 import java.util.Collection;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class ExternalizedResource {
     /*
      * the following two members are the key elements,
      * used by hashCode() and equals();
      */
     private final Object extObject;
     private final Externalizer externalizer;
 
     private final String mimeType;
     private final int flags;
     private final long lastModified;
     private final Collection headers;
     private String id;
 
     public ExternalizedResource(Object obj, Externalizer ext,
                                 String mimeType, Collection headers, int flags) {
         extObject = obj;
         externalizer = ext;
         this.mimeType = mimeType;
         this.flags = flags;
 
         if (externalizer == null || extObject == null) {
             throw new IllegalArgumentException("no externalizer or null object");
         }
 
         lastModified = System.currentTimeMillis();
         this.headers = headers;
     }
 
 
     public final String getMimeType() {
         return mimeType == null ? externalizer.getMimeType(extObject) : mimeType;
     }
 
 
     public final Object getObject() {
         return extObject;
     }
 
 
     public final Externalizer getExternalizer() {
         return externalizer;
     }
 
 
     public final Collection getHeaders() {
         return headers == null ?  externalizer.getHeaders(extObject) : headers;
     }
 
 
     public final boolean deliverOnce() {
         return (flags & AbstractExternalizeManager.REQUEST) > 0;
     }
 
 
     public final boolean isFinal() {
         return (((flags & AbstractExternalizeManager.FINAL) > 0
                 || externalizer.isFinal(extObject))
                 // if flags is request only, then object is not final!!
                 && (flags & AbstractExternalizeManager.REQUEST) == 0);
     }
 
 
     public final String getExtension() {
         return externalizer.getExtension(extObject);
     }
 
 
     public final int getFlags() {
         return flags;
     }
 
 
     public final long getLastModified() {
         if (isFinal())
             return lastModified;
         else
            return System.currentTimeMillis();
     }
 
 
     void setId(String s) {
         id = s;
     }
 
 
     public String getId() {
         return id;
     }
 
 
     public String toString() {
         return "Externalized Info";
     }
 
     /**
      * The HashCode of the externalized resource is
      * the hash code of the to be externalized object
      * (the externalized resource). Together with the implementation
      * of the {@link #equals(Object)}-Method, this makes sure, that
      * the same resource gets the same ID.
      *
      * @return the has code of the externalized object.
      */
     public final int hashCode() {
         return extObject.hashCode();
     }
 
     /**
      * If the managed externalized resource and the
      * externalized manager are the same, then both
      * ExternalizedResource objects are regarded equal.
      *
      * @return true, if the other externalized resource equals
      *         this resource regarding the key members.
      */
     public final boolean equals(ExternalizedResource e) {
         return (extObject.equals(e.extObject)
                 && externalizer.equals(e.externalizer));
     }
 
     /**
      * @return true, if the other object is an ExternalizedResource
      *         and {@link #equals(ExternalizedResource)} returns true.
      */
     public final boolean equals(Object o) {
         if (o instanceof ExternalizedResource) {
             return equals((ExternalizedResource) o);
         }
         return false;
     }
 
 }
 
 
 
 
 
 
