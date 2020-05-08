 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.backend.core;
 
 import java.net.URI;
 
 import org.eclipse.b3.backend.evaluator.B3ContextAccess;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.core.runtime.AssertionFailedException;
 
 import com.google.inject.Singleton;
 
 /**
  * A provider of the root of the b3 output.
  * Translates the URI scheme b3output:/absolutePath/ to a file path
  * In order of priority:
  * ${b3.output.rootURI}
  * ${b3.output.dir}
  * ${user.home}/tmp/b3output/
  * ${java.io.tmpdir}/b3output/
  */
 @Singleton
 public class B3OutputLocationProvider {
 
 	public URI getFileURI(URI b3OutputURI) {
 		if(!b3OutputURI.getScheme().equals("b3output"))
 			return b3OutputURI; // S.A.P
 
 		String rootURI = safeGetStringProperty("b3.output.rootURI", null);
 		ROOTDIRSELECTION: if(rootURI == null) {
 			String tmpDir = safeGetStringProperty("b3.output.dir", null);
 			if(tmpDir != null)
 				break ROOTDIRSELECTION;
 
 			if((tmpDir = safeGetStringProperty("user.home", null)) != null) {
				rootURI = "file:" + tmpDir + "/tmp/b3output";
 				break ROOTDIRSELECTION;
 			}
 
 			if((tmpDir = safeGetStringProperty("java.io.tmpdir", null)) != null) {
				rootURI = "file:" + tmpDir + "/b3output";
 				break ROOTDIRSELECTION;
 			}
 			throw new AssertionFailedException(
 				"No temp directory found - b3.output.rootURI, java.io.tmpdir, nor user.home");
 		}
 		URI root = URI.create(rootURI);
 		String path = b3OutputURI.getPath();
 		if(path.startsWith("/"))
 			path = path.substring(1);
 		root = root.resolve(path);
 		return root;
 	}
 
 	private String safeGetStringProperty(final String propertyName, String defaultValue) {
 		final BExecutionContext ctx = B3ContextAccess.get();
 		if(ctx != null) {
 			Object result;
 			try {
 				result = ctx.getValue("${" + propertyName + "}");
 			}
 			catch(B3EngineException e) {
 				return null;
 			}
 			if(result == null || !(result instanceof String))
 				return defaultValue;
 			return (String) result;
 		}
 		return System.getProperty(propertyName, defaultValue);
 	}
 }
