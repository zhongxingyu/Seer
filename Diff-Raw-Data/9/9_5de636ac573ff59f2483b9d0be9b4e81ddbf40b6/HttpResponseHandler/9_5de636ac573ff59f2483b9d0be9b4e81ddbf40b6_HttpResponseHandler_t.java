 package net.cheney.motown.protocol.common;
 
 import javax.annotation.Nonnull;
 
import net.cheney.motown.common.api.Response;
 
 public interface HttpResponseHandler {
 
 	void sendResponse(@Nonnull Response response, boolean close);
 }
