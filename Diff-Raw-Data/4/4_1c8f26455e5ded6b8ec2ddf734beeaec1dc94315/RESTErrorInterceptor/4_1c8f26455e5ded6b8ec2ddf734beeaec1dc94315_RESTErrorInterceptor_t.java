 package org.jboss.pressgang.ccms.rest;
 
 import javax.ws.rs.ext.Provider;
 
 import java.net.HttpURLConnection;
 
 import org.jboss.pressgang.ccms.provider.exception.BadRequestException;
 import org.jboss.pressgang.ccms.provider.exception.InternalServerErrorException;
 import org.jboss.pressgang.ccms.provider.exception.NotFoundException;
 import org.jboss.pressgang.ccms.provider.exception.UnauthorisedException;
 import org.jboss.pressgang.ccms.provider.exception.UpgradeException;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.client.core.ClientErrorInterceptor;
 
 @Provider
 public class RESTErrorInterceptor implements ClientErrorInterceptor {
     @Override
     public void handle(ClientResponse<?> response) throws RuntimeException {
         final int status = response.getStatus();
         String message = null;
         try {
            message = response.getEntity(String.class);
         } catch (Exception e) {
         } finally {
             response.releaseConnection();
         }
         switch (status) {
             case HttpURLConnection.HTTP_BAD_REQUEST: throw new BadRequestException(message);
             case HttpURLConnection.HTTP_INTERNAL_ERROR: throw new InternalServerErrorException(message);
             case HttpURLConnection.HTTP_UNAUTHORIZED: throw new UnauthorisedException(message);
             case 426: throw new UpgradeException(message);
             case HttpURLConnection.HTTP_NOT_FOUND: throw new NotFoundException(message);
         }
     }
 }
