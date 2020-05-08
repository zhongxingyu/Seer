 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
package jaxrpc.no.feide.mellon;
 
 /**
  * Used to signal an exception from the Moria interface.
  * @author Cato Olsen
  */
 public class MoriaException
 extends java.lang.Exception {
     
     /**
      * Basic constructor.
      */
     public MoriaException() {
     }
     
     
     /**
      * Message constructor.
      * @param message The exception message.
      */
     public MoriaException(String message) {
         super(message);
     }
     
     
     /**
      * Throwable constructor.
      * @param cause The original cause.
      */
     public MoriaException(Throwable cause) {
         super(cause);
     }
     
     
     /**
      * Message and throwable constructor.
      * @param message The exception message.
      * @param cause The original cause.
      */
     public MoriaException(String message, Throwable cause) {
         super(message, cause);
     }
 }
