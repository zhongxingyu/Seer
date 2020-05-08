 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.serialization.registry;
 
 import com.cedarsoft.StillContainedException;
 import com.cedarsoft.registry.DefaultRegistry;
 import com.cedarsoft.registry.Registry;
 import com.cedarsoft.registry.RegistryFactory;
 import com.cedarsoft.serialization.NotFoundException;
 import com.cedarsoft.serialization.Serializer;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Serializer for registries.
  *
  * @param <T> the type
  * @param <R> the registry for the given type
  */
 public class RegistrySerializer<T, R extends Registry<T>> {
   @NotNull
   private final IdResolver<T> idResolver;
   @Nullable
   private final Comparator<T> comparator;
 
   private final RegistrySerializingStrategy<T> serializingStrategy;
 
   /**
    * Creates a new registry serializer
    *
    * @param objectsAccess the objects access
    * @param serializer    the serializer
    * @param idResolver    the id resolver
    */
   @Deprecated
   public RegistrySerializer( StreamBasedObjectsAccess objectsAccess, @NotNull Serializer<T> serializer, @NotNull IdResolver<T> idResolver ) {
     this( objectsAccess, serializer, idResolver, null );
   }
 
   /**
    * Creates a new registry serializer
    *
    * @param objectsAccess the objects access
    * @param serializer    the serializer
    * @param idResolver    the id resolver
    * @param comparator    the (optional) comparator
    */
   @Deprecated
   public RegistrySerializer( @NotNull StreamBasedObjectsAccess objectsAccess, @NotNull Serializer<T> serializer, @NotNull IdResolver<T> idResolver, @Nullable Comparator<T> comparator ) {
     this( new SerializerBasedRegistrySerializingStrategy( objectsAccess, serializer ), idResolver, comparator );
   }
 
   /**
    * Creates a new registry serializer
    *
    * @param serializingStrategy the serializing strategy
    * @param idResolver          the id resolver
    */
   public RegistrySerializer( @NotNull RegistrySerializingStrategy<T> serializingStrategy, @NotNull IdResolver<T> idResolver ) {
     this( serializingStrategy, idResolver, null );
   }
 
   /**
    * Creates a new registry serializer
    *
    * @param serializingStrategy the serializer strategy
    * @param idResolver          the id resolver
    * @param comparator          the (optional) comparator
    */
   public RegistrySerializer( @NotNull RegistrySerializingStrategy<T> serializingStrategy, @NotNull IdResolver<T> idResolver, @Nullable Comparator<T> comparator ) {
     this.serializingStrategy = serializingStrategy;
     this.idResolver = idResolver;
     this.comparator = comparator;
   }
 
   @NotNull
   public List<? extends T> deserialize() throws IOException {
     List<T> objects = new ArrayList<T>( serializingStrategy.deserialize() );
 
     //Sort the objects - if a comparator has been set
     if ( comparator != null ) {
       Collections.sort( objects, comparator );
     }
 
     return objects;
   }
 
   /**
    * Serializes an object
    *
    * @param object the object
    * @throws IOException
    * @throws StillContainedException
    */
   public void serialize( @NotNull T object ) throws StillContainedException, IOException {
     serializingStrategy.serialize( object, getId( object ) );
   }
 
   public void update( @NotNull T object ) throws NotFoundException, IOException {
     serializingStrategy.update( object, getId( object ) );
   }
 
   public void remove( @NotNull T object ) throws NotFoundException, IOException {
     serializingStrategy.remove( object, getId( object ) );
   }
 
   /**
    * Creates a connected registry
    *
    * @param factory the factory used to create the registry
    * @return the connected registry
    *
    * @throws IOException
    */
   @NotNull
  public R createConnectedRegistry( @NotNull RegistryFactory<T, ? extends R> factory ) throws IOException {
     List<? extends T> objects = deserialize();
     R registry = factory.createRegistry( objects, new Comparator<T>() {
       @Override
       public int compare( T o1, T o2 ) {
         return getId( o1 ).compareTo( getId( o2 ) );
       }
     } );
 
     registry.addListener( new DefaultRegistry.Listener<T>() {
       @Override
       public void objectStored( @NotNull T object ) {
         try {
           serialize( object );
         } catch ( IOException e ) {
           throw new RuntimeException( e );
         }
       }
 
       @Override
       public void objectRemoved( @NotNull T object ) {
         try {
           remove( object );
         } catch ( IOException e ) {
           throw new RuntimeException( e );
         }
       }
 
       @Override
       public void objectUpdated( @NotNull T object ) {
         try {
           update( object );
         } catch ( IOException e ) {
           throw new RuntimeException( e );
         }
       }
     } );
     return registry;
   }
 
   @NotNull
   @NonNls
   protected String getId( @NotNull T object ) {
     return idResolver.getId( object );
   }
 
   @Deprecated
   @NotNull
   public Serializer<T> getSerializer() {
     if ( serializingStrategy instanceof SerializerBasedRegistrySerializingStrategy ) {
       return ( ( SerializerBasedRegistrySerializingStrategy ) serializingStrategy ).getSerializer();
     }
 
     throw new UnsupportedOperationException( "Invalid call for this strategy <" + serializingStrategy + ">" );
   }
 
  @NotNull
   public RegistrySerializingStrategy<T> getSerializingStrategy() {
     return serializingStrategy;
   }
 
   @NotNull
   public IdResolver<T> getIdResolver() {
     return idResolver;
   }
 
   @Nullable
   public Comparator<T> getComparator() {
     return comparator;
   }
 
   public interface IdResolver<T> {
     /**
      * Returns the id for the given object
      *
      * @param object the object
      * @return the id
      */
     @NotNull
     @NonNls
     String getId( @NotNull T object );
   }
 
 }
