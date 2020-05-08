 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.kernel.impl.nioneo.store;
 
 @SuppressWarnings( "boxing" )
 public abstract class DynamicStoreAccess<T extends AbstractDynamicStore> extends StoreAccess<T, DynamicRecord>
 {
     private static final int BLOCK_HEADER_SIZE;
     static
     {
         BLOCK_HEADER_SIZE = (Integer) GraphDatabaseStore.get( AbstractDynamicStore.class, "BLOCK_HEADER_SIZE", null );
     }
 
     public void makeHeavy( DynamicRecord record )
     {
         store.makeHeavy( record );
     }
 
     DynamicStoreAccess( T store )
     {
         super( store );
     }
 
     @Override
     public DynamicRecord forceGetRecord( int blockId )
     {
         PersistenceWindow window = store.acquireWindow( blockId, OperationType.READ );
         try
         {
             DynamicRecord record = new DynamicRecord( blockId );
             Buffer buffer = window.getOffsettedBuffer( blockId );
             boolean inUse = buffer.get() == Record.IN_USE.byteValue() ? true : false;
             record.setInUse( inUse );
             int prevBlock = buffer.getInt();
             record.setPrevBlock( prevBlock );
             int dataSize = store.getBlockSize() - BLOCK_HEADER_SIZE;
             int nrOfBytes = buffer.getInt();
             int nextBlock = buffer.getInt();
             record.setNextBlock( nextBlock );
             byte byteArrayElement[] = new byte[dataSize];
             buffer.get( byteArrayElement );
             record.setData( byteArrayElement );
             record.setLength( nrOfBytes );
             return record;
         }
         finally
         {
             store.releaseWindow( window );
         }
     }
 
     public void forceUpdate( DynamicRecord record )
     {
         int blockId = record.getId();
         PersistenceWindow window = store.acquireWindow( blockId, OperationType.WRITE );
         try
         {
             Buffer buffer = window.getOffsettedBuffer( blockId );
             buffer.put( record.inUse() ? Record.IN_USE.byteValue() : Record.NOT_IN_USE.byteValue() ).putInt(
                     record.getPrevBlock() ).putInt( record.getLength() ).putInt( record.getNextBlock() );
            if ( !record.isLight() )
             {
                 if ( !record.isCharData() )
                 {
                     buffer.put( record.getData() );
                 }
                 else
                 {
                     buffer.put( record.getDataAsChar() );
                 }
             }
         }
         finally
         {
             store.releaseWindow( window );
         }
     }
 }
