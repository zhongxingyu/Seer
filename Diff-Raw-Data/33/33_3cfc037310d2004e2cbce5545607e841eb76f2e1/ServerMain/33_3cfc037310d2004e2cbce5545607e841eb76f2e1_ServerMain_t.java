 /**
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.box.server;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.name.Names;
 
 import org.apache.commons.cli.ParseException;
 import org.waveprotocol.box.server.authentication.AccountStoreHolder;
 import org.waveprotocol.box.server.authentication.SessionManager;
 import org.waveprotocol.box.server.persistence.AccountStore;
 import org.waveprotocol.box.server.persistence.PersistenceModule;
 import org.waveprotocol.box.server.robots.RobotApiModule;
 import org.waveprotocol.box.server.robots.RobotRegistrationServlet;
 import org.waveprotocol.box.server.robots.active.ActiveApiServlet;
 import org.waveprotocol.box.server.robots.dataapi.DataApiOAuthServlet;
 import org.waveprotocol.box.server.robots.dataapi.DataApiServlet;
 import org.waveprotocol.box.server.robots.passive.RobotsGateway;
 import org.waveprotocol.box.server.rpc.AttachmentServlet;
 import org.waveprotocol.box.server.rpc.AuthenticationServlet;
 import org.waveprotocol.box.server.rpc.FetchServlet;
 import org.waveprotocol.box.server.rpc.ServerRpcProvider;
 import org.waveprotocol.box.server.rpc.SignOutServlet;
 import org.waveprotocol.box.server.rpc.UserRegistrationServlet;
 import org.waveprotocol.box.server.rpc.WaveClientServlet;
 import org.waveprotocol.box.server.util.Log;
 import org.waveprotocol.box.server.waveserver.WaveBus;
 import org.waveprotocol.box.server.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.waveprotocol.wave.federation.xmpp.ComponentPacketTransport;
 import org.xmpp.component.ComponentException;
 
 import java.io.IOException;
 
 
 /**
  * Wave Server entrypoint.
  */
 public class ServerMain {
 
   private static final Log LOG = Log.get(ServerMain.class);
 
   public static void main(String... args) throws ParseException {
     Module flags = FlagBinder.parseFlags(args, FlagSettings.class);
 
     try {
       run(flags);
       return;
     } catch (IOException e) {
       LOG.severe("IOException when running server: " + e.getMessage());
     }
   }
 
   public static void run(Module flags) throws IOException {
     Injector flagInjector = Guice.createInjector(flags);
     PersistenceModule persistenceModule = flagInjector.getInstance(PersistenceModule.class);
     Injector injector = flagInjector.createChildInjector(
         new ServerModule(), new RobotApiModule(), persistenceModule);
     ComponentPacketTransport xmppComponent = injector.getInstance(ComponentPacketTransport.class);
     ServerRpcProvider server = injector.getInstance(ServerRpcProvider.class);
 
     AccountStoreHolder.init(injector.getInstance(AccountStore.class),
         injector.getInstance(Key.get(String.class, Names.named("wave_server_domain"))));
 
     server.addServlet("/attachment/*", injector.getInstance(AttachmentServlet.class));
 
     server.addServlet(
         SessionManager.SIGN_IN_URL, injector.getInstance(AuthenticationServlet.class));
     server.addServlet("/auth/signout", injector.getInstance(SignOutServlet.class));
     server.addServlet("/auth/register", injector.getInstance(UserRegistrationServlet.class));
 
     server.addServlet("/fetch/*", injector.getInstance(FetchServlet.class));
 
     server.addServlet("/robot/dataapi", injector.getInstance(DataApiServlet.class));
    server.addServlet(DataApiOAuthServlet.DATA_API_OAUTH_PATH + "/*",
         injector.getInstance(DataApiOAuthServlet.class));
     server.addServlet("/robot/dataapi/rpc", injector.getInstance(DataApiServlet.class));
     server.addServlet("/robot/register/*", injector.getInstance(RobotRegistrationServlet.class));
     server.addServlet("/robot/rpc", injector.getInstance(ActiveApiServlet.class));
 
     server.addServlet("/", injector.getInstance(WaveClientServlet.class));
 
     RobotsGateway robotsGateway = injector.getInstance(RobotsGateway.class);
     WaveBus waveBus = injector.getInstance(WaveBus.class);
     waveBus.subscribe(robotsGateway);
 
     ProtocolWaveClientRpc.Interface rpcImpl =
         injector.getInstance(ProtocolWaveClientRpc.Interface.class);
     server.registerService(ProtocolWaveClientRpc.newReflectiveService(rpcImpl));
     try {
       xmppComponent.run();
     } catch (ComponentException e) {
       System.err.println("couldn't connect to XMPP server:" + e);
     }
     LOG.info("Starting server");
     server.startRpcServer();
     server.startWebSocketServer();
   }
 }
