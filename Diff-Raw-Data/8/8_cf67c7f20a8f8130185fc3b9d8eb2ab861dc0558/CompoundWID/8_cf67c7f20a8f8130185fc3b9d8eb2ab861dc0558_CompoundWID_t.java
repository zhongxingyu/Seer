 package com.wealdtech.utils;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.base.Splitter;
 import com.wealdtech.DataError;
 import com.wealdtech.WID;
 
 import java.io.Serializable;
 import java.util.Iterator;
 
 import static com.wealdtech.Preconditions.checkNotNull;
 
 /**
  * Sometimes a compound ID is required, for example when an item has multiple instances.  CompoundWID extends {@link WID} to provide
  * an additional instance ID.
  *
  * Normal form for
  */
 public class CompoundWID<T> implements Comparable<CompoundWID<T>>, Serializable
 {
   private static final long serialVersionUID = 1203541025301978079L;
 
   private final WID<T> id;
   private final Optional<Long> instanceId;
 
   // Radix for WID - hex
   private static final int RADIX = 16;
 
   /**
    * Create a new compound WID given an ID and instance ID
    * @param id the ID
    * @param instanceId the instance ID
    */
   public CompoundWID(final long id, final Long instanceId)
   {
     this.id = WID.fromLong(id);
     this.instanceId = Optional.fromNullable(instanceId);
   }
 
   /**
    * Obtain the (W)ID
    * <p/>
    * @return the WID
    */
   @JsonIgnore
   public WID<T> getId()
   {
     return this.id;
   }
 
   /**
    * Obtain the instance ID
    * <p/>
    * @return the instance ID
    */
   @JsonIgnore
   public Optional<Long> getInstanceId()
   {
     return this.instanceId;
   }
 
  public static <T> CompoundWID<T> fromLongs(final Long id, final Long instanceId)
  {
    return new CompoundWID<T>(id, instanceId);
  }

   public static <T> CompoundWID<T> fromString(final String input)
   {
     checkNotNull(input, "Passed NULL WID");
     try
     {
       final Iterator<String> it = Splitter.on('.').split(input).iterator();
       final long id = Long.valueOf(it.next(), RADIX);
       Long instanceId = null;
       if (it.hasNext())
       {
         final String next = it.next();
         if (next != null && !next.equals(""))
         {
           instanceId = Long.valueOf(next, RADIX);
         }
       }
       return new CompoundWID<>(id, instanceId);
     }
     catch (Exception e)
     {
       throw new DataError.Bad("Invalid format for instance ID");
     }
   }
 
   // Standard object methods
 
   @Override
   public String toString()
   {
     final StringBuilder sb = new StringBuilder(32);
     sb.append(this.id.toString());
     if (this.instanceId.isPresent())
     {
       sb.append('.');
       sb.append(Long.toHexString(this.instanceId.get()));
     }
     return sb.toString();
   }
 
   @Override
   public int hashCode()
   {
     return Objects.hashCode(this.id, this.instanceId);
   }
 
   /**
    * Note that due to type erasure we can't confirm that
    * WID&lt;A&gt; != WID&lt;B&gt; when they hold the same
    * underlying ID.  This should not matter given the ID
    * generation mechanism, but do be aware of this if you're
    * relying on this somewhere.
    */
   @Override
   public boolean equals(final Object that)
   {
    return ((that instanceof CompoundWID) && this.compareTo((CompoundWID<T>)that) == 0);
   }
 
   @Override
   public int compareTo(final CompoundWID<T> that)
   {
     int result = this.id.compareTo(that.id);
     if (result == 0 && this.instanceId.isPresent())
     {
        result = (this.instanceId.get() < that.instanceId.get() ? -1 : (this.instanceId.get() > that.instanceId.get() ? 1 : 0));
     }
     return result;
   }
 }
