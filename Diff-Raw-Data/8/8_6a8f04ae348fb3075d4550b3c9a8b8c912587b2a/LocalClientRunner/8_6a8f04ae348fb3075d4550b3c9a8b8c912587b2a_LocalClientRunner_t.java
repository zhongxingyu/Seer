 package fi.haju.haju3d.client;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import fi.haju.haju3d.client.connection.ServerConnector;
 import fi.haju.haju3d.client.ui.ChunkRenderer;
 import fi.haju.haju3d.protocol.Client;
import fi.haju.haju3d.server.ServerImpl;
 import fi.haju.haju3d.server.ServerModule;
 import fi.haju.haju3d.server.ServerSettings;
 import fi.haju.haju3d.server.WorldSaver;
 
 import java.rmi.RemoteException;
 
 public class LocalClientRunner {
   public static void main(String[] args) throws RemoteException {
     Injector clientInjector = Guice.createInjector(new ClientModule());
     Injector serverInjector = Guice.createInjector(new ServerModule());
 
    ServerImpl server = serverInjector.getInstance(ServerImpl.class);
     clientInjector.getInstance(ServerConnector.class).setRemoteServer(server);
 
     ServerSettings settings = serverInjector.getInstance(ServerSettings.class);
     settings.init();
 
     final WorldSaver saver = serverInjector.getInstance(WorldSaver.class);
     saver.start();
 
    server.init();

     ChunkRenderer app = clientInjector.getInstance(ChunkRenderer.class);
     Client client = new ClientImpl(app);
     server.login(client);
 
     app.setCloseEventHandler(new CloseEventHandler() {
       @Override
       public void onClose() {
         saver.stop();
       }
     });
 
     app.start();
   }
 }
