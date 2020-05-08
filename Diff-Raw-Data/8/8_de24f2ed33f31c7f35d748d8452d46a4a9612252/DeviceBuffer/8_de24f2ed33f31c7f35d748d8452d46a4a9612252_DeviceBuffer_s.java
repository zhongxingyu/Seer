 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.io;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public final class DeviceBuffer
     implements Device
 {
     //  private static final byte[] NULL_STRING = "null".getBytes();
 
     private byte[] buffer;
 
     private int position = 0;
 
     private int capacityIncrement;
 
     public DeviceBuffer (int initialCapacity, int capacityIncrement) {
         buffer = new byte[initialCapacity];
         this.capacityIncrement = capacityIncrement;
     }
 
     /**
      * TODO: documentation
      *
      * @param initialCapacity
      * @return
      */
     public DeviceBuffer (int initialCapacity) {
         this(initialCapacity, -1);
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public DeviceBuffer () {
         this(2000);
     }
 
     /**
      * Flush this Stream.
      */
     public void flush () throws IOException {
     }
 
     /**
      * Print a String.
      */
     public Device print (String s)  throws IOException {
 
         if (s == null)
             return print("null");
 
         int len = s.length();
         for (int i = 0; i < len; i++) {
             //
             // XXX NOTE:  This is clearly incorrect for many strings,
             // but is the only consistent approach within the current
             // servlet framework.  It must suffice until servlet output
             // streams properly encode their output.
             //
             write ((byte)s.charAt(i));
         }
         return this;
 
         /*
          Eigentlich muesste hier das Encoding der HTMLSeite bekannt sein!!!
          Dann koennte man es auch korrekt implementieren !
 
          byte[] bytes = NULL_STRING;
          if ( s!=null )
          bytes = s.getBytes();
 
          return write(bytes);
          */
     }
 
     /**
      * Print an integer.
      */
     public Device print (int i)    throws IOException {
         return print(String.valueOf(i));
     }
 
     /**
      * Print any Object
      */
     public Device print (Object o) throws IOException {
         if ( o!=null )
             return print(o.toString());
         else
             return print("null");
     }
 
     /**
      * Print a String. For compatibility.
      * @*deprecated use print() instead
      */
     public Device append (String s) {
         try {
             print (s);
         }
         catch (IOException ignore) {}
         return this;
     }
 
     /**
      * Print an Integer. For compatibility.
      * @*deprecated use print() instead
      */
     public Device append (int i) {
         try {
             print (i);
         }
         catch (IOException ignore) {}
         return this;
     }
 
     /**
      * Print any Object. For compatibility.
      * @*deprecated use print() instead
      */
     public Device append (Object o) {
         try {
             print (o);
         }
         catch (IOException ignore) {}
         return this;
     }
 
     /**
      * Print a character.
      */
     public Device print (char c)    throws IOException {
         return print(String.valueOf(c));
     }
 
     /**
      * Writes the specified byte to this data output stream.
      */
     public Device write (int c) throws IOException {
         return print(String.valueOf(c));
     }
 
     /**
      * Writes b.length bytes from the specified byte array to this
      * output stream.
      *
      * @param b
      * @return
      * @throws IOException
      */
     public Device write(byte b) throws IOException {
        if ( position+1>buffer.length )
             incrementCapacity();
         buffer[position++] = b;
         return this;
     }
 
     /**
      * Writes b.length bytes from the specified byte array to this
      * output stream.
      */
     public Device write(byte b[]) throws IOException {
         return write(b, 0, b.length);
     }
 
     /**
      * TODO: documentation
      *
      */
     public void clear() {
         position = 0;
     }
 
     private void incrementCapacity() {
         byte[] oldBuffer = buffer;
 
         int newCapacity = (capacityIncrement > 0) ?
             (buffer.length + capacityIncrement) : (buffer.length * 2);
 
        System.out.println("increment Capacity to " + newCapacity);

         buffer = new byte[newCapacity];
         System.arraycopy(oldBuffer, 0, buffer, 0, position);
     }
 
     /**
      * Writes len bytes from the specified byte array starting at offset
      * off to this output stream.
      */
     public Device write(byte b[], int off, int len) throws IOException {
        if ( position+len>buffer.length )
             incrementCapacity();
 
         System.arraycopy(b, off, buffer, position, len);
         position+=len;
 
         return this;
     }
 
     /**
      * TODO: documentation
      *
      * @param d
      */
     public void writeTo(Device d) {
         try {
             d.write(buffer, 0, position);
         }
         catch (IOException ignore) {}
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
