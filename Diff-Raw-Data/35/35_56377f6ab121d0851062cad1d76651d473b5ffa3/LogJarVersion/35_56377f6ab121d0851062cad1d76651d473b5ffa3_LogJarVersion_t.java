/****** BEGIN LICENSE BLOCK ********************************************
  * Version: EUPL 1.1/GPL 3.0
  * 
  * The contents of this file are subject to the EUPL, Version 1.1 or 
  * - as soon they will be approved by the European Commission - 
  * subsequent versions of the EUPL (the "Licence");
  * you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
 * The Original Code is /oxsit-sjob_uno/src/com/yacme/ext/oxsit/comp/SyncJob.java.
  *
  * The Initial Developer of the Original Code is
  * Giuseppe Castagno giuseppe.castagno@acca-esse.it
  * 
  * Portions created by the Initial Developer are Copyright (C) 2009-2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 3 or later (the "GPL")
  * in which case the provisions of the GPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of the GPL, and not to allow others to
  * use your version of this file under the terms of the EUPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the EUPL, or the GPL.
  *
  * ***** END LICENSE BLOCK ******************************************** */
 
 package com.yacme.ext.oxsit.sjob_uno;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.CodeSource;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import com.yacme.ext.oxsit.logging.DynamicLogger;
 
 /**
  * @author beppe
  * This class simply log this jar version
  *
  */
 public class LogJarVersion {
 	
 	final String m_aJarFileName = "oxsit-sjob_uno.uno.jar";
 	
 	URI executivePath ;
 	private DynamicLogger m_aLogger;
 
 	private String m_aJarVersion;
 	public LogJarVersion(DynamicLogger _aLogger) {
 		
 		m_aLogger = _aLogger;
 		
         CodeSource aCs = LogJarVersion.class.getProtectionDomain().getCodeSource();
         if(aCs != null) {
             try {
                 URL aURL = aCs.getLocation(); // where this class is 'seen' by the java runtime
                 //System.out.println(aURL.toString()+" "+aURL.getPath());
                 int pos = aURL.toString().indexOf(m_aJarFileName); //FIXME: _00 modificare in modo che il nome del jar sia quello giusto
                 if(pos == -1) {
                     //non esiste, l'URL è il path
                     executivePath = new URI(aURL.toString());
                 }
                 else {
                     //esiste, elimina
                     executivePath = new URI(aURL.toString().substring(0, pos-1));
                 }
             } catch (URISyntaxException e) {
                 m_aLogger.log(e, false);
             }
         }
 
       //legge la versione dal manifest
         try {
             URI aNew = new URI(executivePath.getScheme(),
                     executivePath.getUserInfo(), executivePath.getHost(), executivePath.getPort(),
                     executivePath.getPath()+"/"+m_aJarFileName,
                     executivePath.getQuery(),
                     executivePath.getFragment());
 //            info(aNew.getPath());
             JarFile jarFile = new JarFile(aNew.getPath());
             Manifest mf = jarFile.getManifest();
             Attributes mfAttr = mf.getMainAttributes();
             //qui estrae la versione e la stampa.
             String aVers = mfAttr.getValue("Implementation-Version");
             if(aVers != null) {
                 String jarVersion = "version: ";
                 jarVersion += aVers;
                 m_aJarVersion = jarVersion; 
             }
             //else
                 //jarVersion += "not found in jar file !";
         } catch (IOException e3) {
             m_aLogger.log(e3, false);
         } catch (URISyntaxException e) {
             m_aLogger.log(e, false);
         }		
 	}
 	
 	public String getVersion() {
 		return m_aJarFileName+":     "+m_aJarVersion;
 	}
 }
