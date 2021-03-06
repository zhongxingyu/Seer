 /*
  * The source code contained in this file is in in the public domain.
  * It can be used in any project or product without prior permission,
  * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
  * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
  * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
  * AND DATA ACCURACY.  We do not warrant or make any representations
  * regarding the use of the software or the  results thereof, including
  * but not limited to the correctness, accuracy, reliability or
  * usefulness of the software.
  */
 package org.mobicents.media.server.impl.conference;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.media.format.AudioFormat;
 import javax.media.format.UnsupportedFormatException;
 import javax.media.protocol.PushBufferStream;
 import org.apache.log4j.Logger;
 import org.mobicents.media.server.impl.BaseConnection;
 import org.mobicents.media.server.impl.BaseEndpoint;
 import org.mobicents.media.server.impl.BaseResource;
 import org.mobicents.media.server.impl.jmf.mixer.AudioMixer;
 import org.mobicents.media.server.spi.Connection;
 import org.mobicents.media.server.spi.Endpoint;
 import org.mobicents.media.server.spi.MediaResource;
 import org.mobicents.media.server.spi.NotificationListener;
 import org.mobicents.media.server.spi.MediaSource;
 
 /**
  *
  * @author Oleg Kulikov
  */
 public class LocalMixer extends BaseResource implements MediaSource {
 
     private String id;
     private Map streams = Collections.synchronizedMap(new HashMap());
     private AudioMixer mixer;
     private ConfEndpointImpl endpoint;
     private BaseConnection connection;
     private Logger logger = Logger.getLogger(LocalMixer.class);
 
     public LocalMixer(BaseEndpoint endpoint, Connection connection) {
         this.endpoint = (ConfEndpointImpl) endpoint;
         this.connection = (BaseConnection) connection;
         this.id = connection.getId();
         this.addStateListener(this.endpoint.mixerStateListener);
     }
 
     public LocalMixer(String id, AudioFormat fmt,
             int packetizationPeriod, int jitter) throws UnsupportedFormatException {
         this.id = id;
         mixer = new AudioMixer(packetizationPeriod, jitter, fmt);
     }
 
     public void add(String id, PushBufferStream pushStream) throws UnsupportedFormatException {
         streams.put(id, pushStream);
         mixer.addInputStream(pushStream);
         if (logger.isDebugEnabled()) {
             logger.debug("id=" + this.id + ", add stream from connection id=" + id +
                     ", total streams=" + mixer.size());
         }
     }
 
     public PushBufferStream remove(String id) {
         PushBufferStream pushStream = (PushBufferStream) streams.remove(id);
         if (pushStream != null) {
             mixer.removeInputStream(pushStream);
             if (logger.isDebugEnabled()) {
                 logger.debug("id=" + this.id + ", removed stream of connection id=" + id +
                         ", total streams=" + mixer.size());
             }
         }
         return pushStream;
     }
 
     public PushBufferStream getOutputStream() {
         return mixer.getOutputStream();
     }
 
     public void start() {
         mixer.start();
         setState(MediaResource.STATE_STARTED);
     }
 
     public void stop() {
         if (logger.isDebugEnabled()) {
             logger.debug("id=" + this.id + " stop mixer");
         }
 
         mixer.stop();
 
         if (getState() == MediaResource.STATE_STARTED) {
             setState(MediaResource.STATE_PREPARED);
         }
     }
 
     @Override
     public String toString() {
         return "LocalMixer[" + id + "]";
     }
 
     public void configure(Properties config) {
         AudioFormat fmt = (AudioFormat) config.get("conf.connection.format");
         int packetization = endpoint.getPacketizationPeriod();
         int jitter = endpoint.getJitter();
 
         try {
             mixer = new AudioMixer(packetization, jitter, fmt);
             setState(MediaResource.STATE_CONFIGURED);
         } catch (UnsupportedFormatException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public void addListener(NotificationListener listener) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void removeListener(NotificationListener listener) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public PushBufferStream prepare() {
         setState(MediaResource.STATE_PREPARED);
         Collection<BaseConnection> connections = endpoint.getConnections();
         for (BaseConnection conn : connections) {
             if (!conn.getId().equals(this.id)) {
                 LocalSplitter splitter = (LocalSplitter) endpoint.getResource(Endpoint.RESOURCE_AUDIO_SINK, conn.getId());
                 if (splitter != null && splitter.getState() >= MediaResource.STATE_CONFIGURED) {
                     try {
                         add(conn.getId(), splitter.newBranch(id));
                     } catch (UnsupportedFormatException e) {
                         logger.error("Unexpected error", e);
                     }
                 }
             }
         }
         return mixer.getOutputStream();
     }
 
     public void release() {
         setState(MediaResource.STATE_NULL);
         Collection<BaseConnection> connections = endpoint.getConnections();
         for (BaseConnection conn : connections) {
             if (!conn.getId().equals(this.id)) {
                 LocalSplitter splitter = (LocalSplitter) endpoint.getResource(
                         Endpoint.RESOURCE_AUDIO_SINK,
                         conn.getId());
                if (splitter != null) splitter.remove(id);
             }
         }
     }
 }
