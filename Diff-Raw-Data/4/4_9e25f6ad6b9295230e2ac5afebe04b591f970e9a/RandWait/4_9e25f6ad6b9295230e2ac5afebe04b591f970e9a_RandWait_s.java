 package kembe.sim;
 
 import fj.F;
 import kembe.Time;
 import kembe.sim.rand.DoubleFromZeroIncToOne;
 import kembe.sim.rand.Rand;
 import org.joda.time.*;
 
 import java.util.Random;
 
 public abstract class RandWait {
 
 
     public static RandWait waitUntil(final Instant instant) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant i) {
                 return new Rand<Instant>() {
                     @Override public Instant next(Random t) {
                         return instant;
                     }
                 };
             }
         };
     }
 
     public static RandWait waitUntil(final LocalTime localTime) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant instant) {
                 return new Rand<Instant>() {
                     @Override public Instant next(Random t) {
                         return Time.next( localTime, instant ).toInstant();
                     }
                 };
             }
         };
     }
 
     public static RandWait waitUntilBetween(final LocalTime from, final LocalTime to) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant instant) {
                Duration duration = Time.from(from.toDateTime( instant )).until( to.toDateTime( instant ) ).toDuration();
 
                return within( Time.from( Time.next( from, instant ) ).lasting( duration ) );
             }
         };
     }
 
     public static RandWait waitFor(final Duration dur) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant i) {
                 return new Rand<Instant>() {
                     @Override public Instant next(Random t) {
                         return i.plus( dur );
                     }
                 };
             }
         };
     }
 
     public static RandWait waitFor(final ReadablePeriod p){
         return waitFor( p.toPeriod().toStandardDuration() );
     }
 
     public static RandWait waitForBetween(final Duration min, final Duration max) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant i) {
                 return within( Time.from( i.plus( min ) ).until( i.plus( max ) ) );
             }
         };
     }
 
     public static RandWait waitForAtLeast(final Duration dur) {
         return new RandWait() {
             @Override public Rand<Instant> after(final Instant i) {
                 return within( Time.from( i ).lasting( dur ) );
             }
         };
     }
     public static RandWait waitForAtLeast(final ReadablePeriod p){
         return waitForAtLeast( p.toPeriod().toStandardDuration() );
     }
 
     private static Rand<Instant> within(final Interval interval) {
         Rand<Instant> rg = new Rand<Instant>() {
 
             @Override public Instant next(Random random) {
                 return Rand.randomDouble().map( new F<DoubleFromZeroIncToOne, Instant>() {
                     @Override
                     public Instant f(DoubleFromZeroIncToOne d) {
                         long fraction = (long) (interval.toDurationMillis() * d.value);
                         return new Instant( interval.getStartMillis() + fraction );
                     }
                 } ).next( random );
             }
         };
 
         return rg;
     }
 
     public abstract Rand<Instant> after(Instant instant);
 }
