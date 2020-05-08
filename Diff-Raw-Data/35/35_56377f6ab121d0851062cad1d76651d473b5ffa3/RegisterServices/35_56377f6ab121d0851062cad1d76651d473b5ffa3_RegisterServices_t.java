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
 * The Original Code is /oxsit-sjob_uno/src/com/yacme/ext/oxsit/RegisterServices.java.
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
 
 package com.yacme.ext.oxsit;
 
 
 import com.sun.star.lang.XSingleComponentFactory;
 import com.sun.star.lib.uno.helper.Factory;
 import com.sun.star.registry.XRegistryKey;
 import com.yacme.ext.oxsit.comp.SyncJob;
 
 public class RegisterServices {
 	/** Gives a factory for creating the service(s).
 	 * This method is called by the <code>JavaLoader</code>
 	 * <p>
 	 * @return Returns a <code>XSingleServiceFactory</code> for creating the
 	 * component.
 	 * @see com.sun.star.comp.loader.JavaLoader#
 	 * @param stringImplementationName The implementation name of the component.
 	 * @param xmultiservicefactory The service manager, who gives access to every
 	 * known service.
 	 */
 	public synchronized static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
 		XSingleComponentFactory xFactory = null;
 	
 //		System.out.println("__getComponentFactory");
 		if ( sImplementationName.equals( SyncJob.m_sImplementationName ) )
 			xFactory = Factory.createComponentFactory( SyncJob.class, SyncJob.m_sServiceNames );
 		return xFactory;
 	}
 	/** Writes the service information into the given registry key.
 	 * This method is called by the <code>JavaLoader</code>.
 	 * @return returns true if the operation succeeded
 	 * @see com.sun.star.comp.loader.JavaLoader#
 	 * @see com.sun.star.lib.uno.helper.Factory#
 	 * @param xregistrykey Makes structural information (except regarding tree
 	 * structures) of a single
 	 * registry key accessible.
 	 */
 	public synchronized static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
 //		System.out.println("__writeRegistryServiceInfo: "+SignatureHandler.m_sImplementationName+" "+SignatureHandler.m_sServiceNames[0]+" "+SignatureHandler.m_sServiceNames[1] );
 		return Factory.writeRegistryServiceInfo( SyncJob.m_sImplementationName, SyncJob.m_sServiceNames, xRegistryKey );
 	}
 }
