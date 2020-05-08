 package org.realityforge.tarrabah;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.DeflaterInputStream;
 import java.util.zip.GZIPInputStream;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.enterprise.context.Dependent;
 import javax.inject.Inject;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 @Dependent
 public class GelfHandler
   extends SimpleChannelHandler
 {
   static final short[] CHUNKED_BYTE_PREFIX = new short[]{ 0x1E, 0x0F };
   private static final short[] ZLIP_BYTE_PREFIX = new short[]{ 0x78, 0x9C };
   private static final short[] GZIP_BYTE_PREFIX = new short[]{ 0x1F, 0x8B };
 
   @Inject
   private Logger _logger;
 
   private Integer _windowSize = 5000;
 
   private final Map<Long, GelfChunkedMessage> _messages = new ConcurrentHashMap<Long, GelfChunkedMessage>();
 
   @Override
   public void messageReceived( final ChannelHandlerContext context,
                                final MessageEvent e )
     throws Exception
   {
     final InetSocketAddress remoteAddress = (InetSocketAddress) e.getRemoteAddress();
     final ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
 
     final byte[] readable = new byte[ buffer.readableBytes() ];
     buffer.toByteBuffer().get( readable, buffer.readerIndex(), buffer.readableBytes() );
 
     final JsonObject object = processPayload( readable );
     if ( null != object )
     {
       Channels.fireMessageReceived( context, object, remoteAddress );
     }
   }
 
   @Nullable
   final JsonObject processPayload( @Nonnull final byte[] payload )
     throws IOException
   {
     if ( payload.length > 2 &&
          CHUNKED_BYTE_PREFIX[ 0 ] == ( payload[ 0 ] & 0xFF ) &&
          CHUNKED_BYTE_PREFIX[ 1 ] == ( payload[ 1 ] & 0xFF ) )
     {
       //Chunked
       final byte[] fullMessagePayload = insertChunk( new GelfMessageChunk( payload ) );
       if ( null != fullMessagePayload )
       {
         return processCompleteMessage( fullMessagePayload );
       }
       else
       {
         return null;
       }
     }
     else
     {
       return processCompleteMessage( payload );
     }
   }
 
   @Nonnull
   private JsonObject processCompleteMessage( @Nonnull final byte[] payload )
     throws IOException
   {
     final ByteArrayInputStream inputStream = new ByteArrayInputStream( payload );
     if ( payload.length > 2 &&
          ZLIP_BYTE_PREFIX[ 0 ] == ( payload[ 0 ] & 0xFF ) &&
          ZLIP_BYTE_PREFIX[ 1 ] == ( payload[ 1 ] & 0xFF ) )
     {
       // ZLIB'd
       return processJsonMessage( new DeflaterInputStream( inputStream ) );
     }
     else if ( payload.length > 2 &&
               GZIP_BYTE_PREFIX[ 0 ] == ( payload[ 0 ] & 0xFF ) &&
               GZIP_BYTE_PREFIX[ 1 ] == ( payload[ 1 ] & 0xFF ) )
     {
       //GZIP'd
       return processJsonMessage( new GZIPInputStream( inputStream ) );
     }
     else
     {
       //plain
       return processJsonMessage( inputStream );
     }
   }
 
   @Nonnull
   private JsonObject processJsonMessage( @Nonnull final InputStream input )
   {
     final JsonParser parser = new JsonParser();
     final JsonElement element = parser.parse( new InputStreamReader( input ) );
     //if ( !( element instanceof JsonObject ) )
     //{
     //  //Error
     //}
     final JsonObject object = (JsonObject) element;
     System.out.println( "object = " + object );
 
     return object;
   }
 
   /**
    * Insert chunk into the cache.
    * If the chunk completes a message then return the contents of the message as a stream.
    *
    * @param chunk the chunk
    * @return the stream for a message, if it is completed.
    */
   @Nullable
   final synchronized byte[] insertChunk( @Nonnull final GelfMessageChunk chunk )
   {
     final Long messageID = chunk.getMessageID();
     GelfChunkedMessage message = _messages.get( messageID );
     if ( null == message )
     {
       if ( _logger.isLoggable( Level.FINEST ) )
       {
         _logger.log( Level.FINEST, "Initiating chunk message with ID " + Long.toHexString( messageID ) );
       }
       message = new GelfChunkedMessage( chunk );
       _messages.put( messageID, message );
     }
     else
     {
       message.insert( chunk );
     }
     if ( message.isComplete() )
     {
       if ( _logger.isLoggable( Level.FINEST ) )
       {
         _logger.log( Level.FINEST, "Completing chunk message with ID " + Long.toHexString( messageID ) );
       }
 
       _messages.remove( messageID );
       return message.toPayload();
     }
     else
     {
       return null;
     }
   }
 
   public synchronized void purgeOutdated()
   {
     final long thresholdTime = System.currentTimeMillis() - _windowSize;
     for ( final Entry<Long, GelfChunkedMessage> entry : _messages.entrySet() )
     {
       if ( entry.getValue().getLastUpdateTime() < thresholdTime )
       {
         final Long messageID = entry.getKey();
         if ( _logger.isLoggable( Level.FINEST ) )
         {
           _logger.log( Level.FINEST, "Dropping outdated chunk message with ID " + Long.toHexString( messageID ) );
         }
         _messages.remove( messageID );
       }
     }
   }
 
   @Override
   public void exceptionCaught( final ChannelHandlerContext context, final ExceptionEvent e )
     throws Exception
   {
    final InetSocketAddress remoteAddress = (InetSocketAddress) e.getChannel().getRemoteAddress();
    final Throwable cause = e.getCause();
    _logger.log( Level.WARNING,
                 "Problem handling gelf packet from '" + remoteAddress + "'. Problem: " + cause.getMessage(),
                 cause );
   }
 }
