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
 package org.ow2.sirocco.apis.rest.cimi.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
import javax.xml.bind.DatatypeConverter;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 public class CimiDateAdapter extends XmlAdapter<String, Date> {
 
     private SimpleDateFormat dateFormat;
 
     /**
      * Constructor by default.
      */
     public CimiDateAdapter() {
         super();
         this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         this.dateFormat.setTimeZone(TimeZone.getTimeZone("Zulu"));
     }
 
     @Override
     public String marshal(final Date v) throws Exception {
         synchronized (this.dateFormat) {
             return this.dateFormat.format(v);
         }
     }
 
     @Override
     public Date unmarshal(final String v) throws Exception {
        return DatatypeConverter.parseDateTime(v).getTime();
     }
 }
