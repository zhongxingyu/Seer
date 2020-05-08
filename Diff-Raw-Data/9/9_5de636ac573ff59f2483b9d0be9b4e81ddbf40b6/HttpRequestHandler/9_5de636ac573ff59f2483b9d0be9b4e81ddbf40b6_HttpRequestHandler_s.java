 package net.cheney.motown.protocol.common;
 
 import javax.annotation.Nonnull;
 
import net.cheney.motown.api.Request;
 
 public interface HttpRequestHandler {
 
 	void handleRequest(@Nonnull Request request, @Nonnull HttpResponseHandler responseHandler);
 	
 }
