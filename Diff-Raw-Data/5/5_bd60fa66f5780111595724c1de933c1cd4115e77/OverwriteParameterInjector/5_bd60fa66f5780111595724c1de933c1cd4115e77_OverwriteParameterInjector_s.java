 package net.cheney.motown.server.dispatcher.dynamic;
 
import static net.cheney.motown.common.api.Header.DESTINATION;
import static net.cheney.motown.common.api.Header.OVERWRITE;

import java.net.URI;

 import net.cheney.motown.common.api.Header;
 import net.cheney.motown.server.api.Environment;
 
 public class OverwriteParameterInjector extends MethodParameterInjector {
 
 	@Override
 	public Boolean injectParameter(Environment env) {
 		String overwrite = env.header(Header.OVERWRITE).getOnlyElementWithDefault("T");
 		return overwrite.equals("T");
 	}
 
 }
