 package org.nohope.test;
 
 import akka.actor.ActorSystem;
 import com.typesafe.config.ConfigFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.toFile;

 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 8/16/12 10:15 PM
  */
 public final class AkkaUtils {
 
     private AkkaUtils() {
     }
 
     public static URI generateActorUri(final String actorSystemName,
                                        final String hostName,
                                        final String actorName) {
         final InetSocketAddress randomAvailableAddress =
                 SocketUtils.getAvailableAddress(hostName);
         try {
             return new URI(String.format("akka://%s@%s:%d/user/%s",
                     actorSystemName,
                     randomAvailableAddress.getHostString(),
                     randomAvailableAddress.getPort(),
                     actorName));
         } catch (final URISyntaxException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     public static ActorSystem createLocalSystem(final String systemName) {
         return buildSystem(systemName).build();
     }
 
     public static ActorSystemBuilder buildSystem(final String systemName) {
         return new ActorSystemBuilder(systemName);
     }
 
     public static ActorSystem createRemoteSystem(final String systemName) {
         return new ActorSystemBuilder(systemName)
                 .put("actor.provider", "akka.remote.RemoteActorRefProvider")
                 .put("remote.transport", "akka.remote.netty.NettyRemoteTransport")
                 .put("remote.log-sent-messages", "on")
                 .put("remote.log-received-messages", "on")
                 .put("remote.log-received-messages", "on")
                 .put("remote.netty.hostname", "localhost")
                 .put("remote.netty.port", SocketUtils.getAvailablePort())
                 .put("remote.netty.port", SocketUtils.getAvailablePort())
                 .build();
     }
 
     public static URI generateLocalHostActorUri(final String actorSystemName,
                                                 final String actorName) {
         return generateActorUri(actorSystemName,
                 SocketUtils.getLocalHostAddress(),
                 actorName);
     }
 
     public static class ActorSystemBuilder {
         private final StringBuilder config = new StringBuilder();
         private final String name;
 
         public ActorSystemBuilder(final String name) {
             this.name = name;
         }
 
         public ActorSystemBuilder put(final String key, final Object value) {
             config.append(key)
                   .append(" = ")
                   .append(value)
                   .append('\n');
             return this;
         }
 
         public ActorSystem build() {
             final String conf;
             try {
                conf = readFileToString(toFile(
                        getClass().getResource("/test/akka.conf")
                ));
             } catch (final IOException e) {
                 throw new IllegalStateException(e);
             }
 
             final String config = String.format(conf, this.config.toString());
             return ActorSystem.create(name, ConfigFactory.parseString(config));
         }
     }
 }
