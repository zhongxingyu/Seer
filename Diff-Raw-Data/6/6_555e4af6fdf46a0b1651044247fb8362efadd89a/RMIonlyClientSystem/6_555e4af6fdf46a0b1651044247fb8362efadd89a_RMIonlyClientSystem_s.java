 package ch.hsr.objectCaching.rmiOnlyClient;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import ch.hsr.objectCaching.account.AccountService;
 import ch.hsr.objectCaching.interfaces.clientSystemUnderTest.ClientSystemUnderTest;
 
 public class RMIonlyClientSystem implements ClientSystemUnderTest {
 	
 	private IStreamProvider streamProvider = new StreamProvider();
 
 	@Override
 	public AccountService getAccountService() {
 		AccountServiceStub accountServiceStub = new AccountServiceStub();
 		accountServiceStub.setStreamProvider(streamProvider);
 		return accountServiceStub;
 	}
 
 	@Override
 	public void setServerSocketAdress(InetSocketAddress socketAdress) {
 		streamProvider.setSocketAdress(socketAdress);
 	}
 
 	@Override
 	public void shutdown() {
 		try {
			streamProvider.getObjectOutputStream().close();
 			streamProvider.getObjectInputStream().close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
