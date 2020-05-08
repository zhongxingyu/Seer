 /*
  * Copyright (c) 2004 UNINETT FAS A/S
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package no.feide.moria.directory.index;
 
 import java.util.Hashtable;
 import no.feide.moria.log.MessageLogger;
 
 /**
  * @author Cato Olsen
  */
 public class Register {
 
     /* The internal register. */
     private Hashtable register;
 
 
     /**
      * 
      */
     public Register() {
         register = new Hashtable();
     }
 
 
     /**
      * @param element
      */
     protected void put(Element element) {
 
         // Validity check.
         if (element == null)
             throw new IllegalArgumentException("Element cannot be NULL");
 
         // Did we just update an existing element?
         if (register.put(new String(element.getName()), element) != null)
            MessageLogger.logInfo("Element " + element.getName() + " updated");
     }
 
 
     /**
      * @param name
      * @return
      */
     public Element get(String name) {
 
         // Validity check.
         if (name == null)
             throw new IllegalArgumentException("Name cannot be NULL");
 
         return (Element)register.get(name);
     }
 
 }
