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
 * The Original Code is /oxsit-sjob_uno/src/com/yacme/ext/oxsit/comp/GlobConstantJobs.java.
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
 
 package com.yacme.ext.oxsit.comp;
 
 /** this class contains the global variables needed by all this implemenatation
  * 
  * @author beppe
  *
  */
 public class GlobConstantJobs {
 	
 /** the followings are the dispatch code 'external' to this component (that is, this jar file)
  * 	public static final String m_sSignProtocolBaseUrl = "com.yacme.ext.cnipa.comp.SignatureHandler:";
  * 
  */
 	//intercepted url
 	public static final String m_sUnoSignatureURLComplete = ".uno:Signature";
 	public static final String m_sUnoSaveURLComplete = ".uno:Save";
 	public static final String m_sUnoSaveAsURLComplete = ".uno:SaveAs";
 	public static final String	m_sUnoSignatureURLProtocol	= ".uno:";
 	public static final String	m_sUnoSignatureURLPath	= "Signature";
 
 	// these come from sfx2/inc/sfx2/signaturestate.hxx
 	public static final int		SIGNATURESTATE_UNKNOWN					= -1;
 	public static final int		SIGNATURESTATE_NOSIGNATURES				= 0;
 	public static final int		SIGNATURESTATE_SIGNATURES_OK			= 1;
 	public static final int		SIGNATURESTATE_SIGNATURES_BROKEN		= 2;
 	/** State was SIGNATURES_OK, but doc is modified now
 	 * NOTE: this doesn't seem to be implemented in OOo as of 2.4. The
 	 * behavior is different: when the document is modified, the signatures
 	 * status disappears.
 	 */
 	public static final int		SIGNATURESTATE_SIGNATURES_INVALID		= 3;
 	/**
 	 *  signature is OK, but certificate could not be validated
 	 */
 	public static final int		SIGNATURESTATE_SIGNATURES_NOTVALIDATED	= 4;	
 }
