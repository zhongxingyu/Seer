 /*
  * Copyright (c) 2002-2009 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
 package org.neo4j.index.lucene;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 
 import org.neo4j.kernel.impl.transaction.xaframework.LogBuffer;
 import org.neo4j.kernel.impl.transaction.xaframework.XaCommand;
 
 abstract class LuceneCommand extends XaCommand
 {
     private final Long nodeId;
     private final String key;
     private final String value;
     
     private static final byte ADD_COMMAND = (byte) 1;
     private static final byte REMOVE_COMMAND = (byte) 2;
     
     LuceneCommand( Long nodeId, String key, String value )
     {
         this.nodeId = nodeId;
         this.key = key;
         this.value = value;
     }
     
     LuceneCommand( CommandData data )
     {
         this.nodeId = data.nodeId;
         this.key = data.key;
         this.value = data.value;
     }
     
     public Long getNodeId()
     {
         return nodeId;
     }
     
     public String getKey()
     {
         return key;
     }
     
     public String getValue()
     {
         return value;
     }
     
     @Override
     public void execute()
     {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void writeToFile( LogBuffer buffer ) throws IOException
     {
         buffer.put( getCommandValue() );
         buffer.putLong( getNodeId() != null ? getNodeId() : -1L );
         char[] keyChars = getKey().toCharArray();
         buffer.putInt( keyChars.length );
         char[] valueChars = getValue() != null ?
             getValue().toCharArray() : null;
         buffer.putInt( valueChars != null ? valueChars.length : -1 );
         buffer.put( keyChars );
         if ( valueChars != null )
         {
             buffer.put( valueChars );
         }
     }
     
     protected abstract byte getCommandValue();
     
     static class AddCommand extends LuceneCommand
     {
         AddCommand( Long nodeId, String key, String value )
         {
             super( nodeId, key, value );
         }
         
         AddCommand( CommandData data )
         {
             super( data );
         }
 
         @Override
         protected byte getCommandValue()
         {
             return ADD_COMMAND;
         }
     }
     
     static class RemoveCommand extends LuceneCommand
     {
         RemoveCommand( Long nodeId, String key, String value )
         {
             super( nodeId, key, value );
         }
         
         RemoveCommand( CommandData data )
         {
             super( data );
         }
 
         @Override
         protected byte getCommandValue()
         {
             return REMOVE_COMMAND;
         }
     }
 
     private static class CommandData
     {
         private final Long nodeId;
         private final String key;
         private final String value;
         
         CommandData( Long nodeId, String key, String value )
         {
             this.nodeId = nodeId;
             this.key = key;
             this.value = value;
         }
     }
     
     static CommandData readCommandData( ReadableByteChannel channel, 
         ByteBuffer buffer ) throws IOException
     {
         buffer.clear(); buffer.limit( 16 );
         if ( channel.read( buffer ) != buffer.limit() )
         {
             return null;
         }
         buffer.flip();
         long nodeId = buffer.getLong();
         int keyCharLength = buffer.getInt();
         int valueCharLength = buffer.getInt();
 
         char[] keyChars = new char[keyCharLength];
         keyChars = readCharArray( channel, buffer, keyChars );
         if ( keyChars == null )
         {
             return null;
         }
         String key = new String( keyChars );
 
         String value = null;
         if ( valueCharLength != -1 )
         {
             char[] valueChars = new char[valueCharLength];
             valueChars = readCharArray( channel, buffer, valueChars );
             value = new String( valueChars );
         }
         return new CommandData( nodeId != -1 ? nodeId : null, key, value );
     }
     
     private static char[] readCharArray( ReadableByteChannel channel, 
         ByteBuffer buffer, char[] charArray ) throws IOException
     {
         buffer.clear();
         int charsLeft = charArray.length;
         int maxSize = buffer.capacity() / 2;
         int offset = 0; // offset in chars
         while ( charsLeft > 0 )
         {
             if ( charsLeft > maxSize )
             {
                 buffer.limit( maxSize * 2 );
                 charsLeft -= maxSize;
             }
             else
             {
                 buffer.limit( charsLeft * 2 );
                 charsLeft = 0;
             }
             if ( channel.read( buffer ) != buffer.limit() )
             {
                 return null;
             }
             buffer.flip();
             int length = buffer.limit() / 2;
             buffer.asCharBuffer().get( charArray, offset, length ); 
             offset += length;
             buffer.clear();
         }
         return charArray;
     }
     
     static XaCommand readCommand( ReadableByteChannel channel, 
         ByteBuffer buffer )
         throws IOException
     {
         buffer.clear(); buffer.limit( 1 );
         if ( channel.read( buffer ) != buffer.limit() )
         {
             return null;
         }
         buffer.flip();
         byte commandType = buffer.get();
         CommandData data = readCommandData( channel, buffer );
         if ( data == null )
         {
             return null;
         }
         switch ( commandType )
         {
             case ADD_COMMAND: return new AddCommand( data ); 
             case REMOVE_COMMAND: return new RemoveCommand( data );
            default: return null;
         }
     }
 }
