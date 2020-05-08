 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.log;
 
 import java.io.Serializable;
 
 import org.joe_e.Powerless;
 import org.joe_e.Selfless;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 
 /**
  * An event loop turn identifier.
  */
 public final class
 Turn implements Comparable<Turn>, Selfless, Powerless, Record, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * URI for the event loop
      */
     public final String loop;
     
     /**
      * local turn number
      */
     public final long number;
     
     /**
      * Constructs an instance.
      * @param loop      {@link #loop}
      * @param number    {@link #number}
      */
     public @deserializer
     Turn(@name("loop") final String loop,
          @name("number") final long number) {
         this.loop = loop;
         this.number = number;
     }
     
     // org.joe_e.Selfless interface
     
     public boolean
     equals(final Object o) {
        return o instanceof Turn &&
             number == ((Turn)o).number &&
             (null!=loop ? loop.equals(((Turn)o).loop) : null == ((Turn)o).loop);
     }
     
     public int
     hashCode() {
         return (null != loop ? loop.hashCode() : 0) +
                (int)(number ^ (number >>> 32)) +
                0x10097C42;
     }
     
     // java.lang.Comparable interface
 
     public int
     compareTo(final Turn o) {
         if (!(null != loop ? loop.equals(o.loop) : null == o.loop)) {
             throw new RuntimeException();
         }
         final long d = number - o.number;
         return d < 0L ? -1 : d == 0L ? 0 : 1;
     }
 }
