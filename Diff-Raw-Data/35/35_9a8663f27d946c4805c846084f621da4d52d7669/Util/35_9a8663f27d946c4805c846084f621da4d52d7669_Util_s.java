 package webClient;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.Random;
 
 import org.apache.axis.AxisFault;
 import org.apache.axis.client.Service;
 import org.apache.log4j.Logger;
 
 import webServer.WebServerSoap11BindingStub;
 import webServer.messages.DropOfferRequest;
 import webServer.messages.ErrorResponse;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.GetProfileResponse;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 import webServer.messages.LoadOffersRequest;
 import webServer.messages.LoadOffersResponse;
 import webServer.messages.LoginRequest;
 import webServer.messages.LoginResponse;
 import webServer.messages.LogoutRequest;
 import webServer.messages.OkResponse;
 import webServer.messages.RegisterProfileRequest;
 import webServer.messages.RemoveOfferRequest;
 import webServer.messages.SetProfileRequest;
 
 import config.WebServiceClientConfig;
 
 public class Util {
 	private static Logger						logger	= Logger.getLogger(Util.class);
 	private static WebServerSoap11BindingStub	client;
 
 	static {
 		try {
 			client = new WebServerSoap11BindingStub(new URL(WebServiceClientConfig.ENDPOINT_URL), new Service());
 		} catch (AxisFault e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void printErrorMessage(Object requestObj) {
 		System.err.println("Unknow type of message : " + requestObj.getClass());
 	}
 
 	public static Object askWebServer(Object requestObj) {
 		logger.debug("Begin");
 
 		Object responseObj = null;
 		byte[] responseByteArray = null;
 		byte[] requestByteArray = WebMessage.serialize(requestObj);
 
 		try {
 			if (requestObj instanceof DropOfferRequest) {
 				responseByteArray = client.dropOffer(requestByteArray);
 			} else if (requestObj instanceof ErrorResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof GetProfileRequest) {
 				responseByteArray = client.getProfile(requestByteArray);
 			} else if (requestObj instanceof GetProfileResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof LaunchOfferRequest) {
 				responseByteArray = client.launchOffer(requestByteArray);
 			} else if (requestObj instanceof LaunchOfferResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof LoginRequest) {
 				responseByteArray = client.login(requestByteArray);
 			} else if (requestObj instanceof LoginResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof LogoutRequest) {
 				responseByteArray = client.logout(requestByteArray);
 			} else if (requestObj instanceof OkResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof RegisterProfileRequest) {
 				responseByteArray = client.registerProfile(requestByteArray);
 			} else if (requestObj instanceof SetProfileRequest) {
 				responseByteArray = client.setProfile(requestByteArray);
 			} else if (requestObj instanceof LoadOffersRequest) {
 				responseByteArray = client.loadOffers(requestByteArray);
 			} else if (requestObj instanceof LoadOffersResponse) {
 				printErrorMessage(requestObj);
 			} else if (requestObj instanceof RemoveOfferRequest) {
				responseByteArray = client.removeService(requestByteArray);
 			} else {
 				printErrorMessage(requestObj);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		if (responseByteArray != null) {
 			responseObj = WebMessage.deserialize(responseByteArray);
 		}
 
 		logger.debug("End");
 		return responseObj;
 	}
 }
