 package org.amanzi.awe.afp.engine;
 
import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.amanzi.awe.afp.Activator;
 import org.eclipse.core.runtime.Path;
 
 public class AfpEngineImpl extends org.amanzi.awe.afp.AfpEngine{
 
 	@Override
 	public String getAfpEngineExecutablePath() {
 		String path = null;
 		try {
 			URL url = org.eclipse.core.runtime.FileLocator.find(Activator.getDefault().getBundle(), 
 				new Path("exe/japa_awe.exe"), null);
			
             path = org.eclipse.core.runtime.FileLocator.toFileURL(url).getPath();
            
            //LN, 28.02.2011, for linux we should set executable permissions on file
            new File(path).setExecutable(true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return path;
 	}
 }
