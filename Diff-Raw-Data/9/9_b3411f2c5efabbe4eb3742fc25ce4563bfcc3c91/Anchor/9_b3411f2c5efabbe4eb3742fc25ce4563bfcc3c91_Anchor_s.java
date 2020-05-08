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
  * A marker for a point in an event loop turn where an event originated.
  */
 public class
 Anchor implements Comparable<Anchor>, Selfless, Powerless, Record, Serializable{
     static private final long serialVersionUID = 1L;
 
     /**
      * event loop turn in which the event originated
      */
     public final Turn turn;
     
     /**
      * intra-{@linkplain #turn turn} event number
      */
     public final long number;
     
     /**
      * Constructs an instance.
      * @param turn      {@link #turn}
      * @param number    {@link #number}
      */
     public @deserializer
     Anchor(@name("turn") final Turn turn,
            @name("number") final long number) {
         this.turn = turn;
         this.number = number;
     }
     
     // org.joe_e.Selfless interface
     
     public boolean
     equals(final Object o) {
        return o instanceof Anchor &&
             number == ((Anchor)o).number &&
             (null!=turn?turn.equals(((Anchor)o).turn):null==((Anchor)o).turn);
     }
     
     public int
     hashCode() {
         return (null != turn ? turn.hashCode() : 0) +
                (int)(number ^ (number >>> 32)) +
                0x7C42A2C4;
     }
 
     // java.lang.Comparable interface
     
     public int
     compareTo(final Anchor o) {
         final int major = turn.compareTo(o.turn);
         if (0 != major) { return major; }
         final long minor = number - o.number;
         return minor < 0L ? -1 : minor == 0L ? 0 : 1;
     }
 }
