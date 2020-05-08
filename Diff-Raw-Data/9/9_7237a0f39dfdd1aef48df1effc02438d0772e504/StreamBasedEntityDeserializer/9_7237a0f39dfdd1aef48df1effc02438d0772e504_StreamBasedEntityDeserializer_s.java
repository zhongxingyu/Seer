 /*
  * This is a utility project for wide range of applications
  *
 * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.atom;
 
 import java.io.InputStream;
 import java.net.URI;
 
 /**
  * A de-serializer that can de-serialze an entity from its stream
  * @author imyousuf
  */
 public interface StreamBasedEntityDeserializer<T> {
 
   /**
    * Deserialize a input stream content to the entity specified.
    * @param inputStream The stream to deserialize from after/while reading
    * @param src The source URI of the content
    * @param mimeType The MIME Type of the inputStream content
    * @return Entity after deserialization
    */
   public T deserialze(InputStream inputStream,
                       URI src,
                       String mimeType);
 }
