/* ***** BEGIN LICENSE BLOCK ********************************************
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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/ConstantCustomIT.java.
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
 
 import com.sun.star.beans.NamedValue;
 import com.sun.star.document.XStorageBasedDocument;
 import com.sun.star.drawing.XDrawPagesSupplier;
 import com.sun.star.embed.XStorage;
 import com.sun.star.frame.XController;
 import com.sun.star.frame.XFrame;
 import com.sun.star.frame.XModel;
 import com.sun.star.io.IOException;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.NoSuchMethodException;
 import com.sun.star.lang.WrappedTargetException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.lang.XServiceInfo;
 import com.sun.star.lib.uno.helper.ComponentBase;
 import com.sun.star.presentation.XPresentation;
 import com.sun.star.sheet.XSpreadsheetDocument;
 import com.sun.star.task.XJob;
 import com.sun.star.text.XTextDocument;
 import com.sun.star.ucb.ServiceNotFoundException;
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.RuntimeException;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.util.CloseVetoException;
 import com.sun.star.util.XChangesBatch;
 import com.sun.star.util.XCloseListener;
 import com.sun.star.util.XCloseable;
 import com.yacme.ext.oxsit.Helpers;
 import com.yacme.ext.oxsit.XOX_DispatchInterceptor;
 import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
 import com.yacme.ext.oxsit.logging.DynamicLogger;
 import com.yacme.ext.oxsit.ooo.GlobConstant;
 import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
 import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
 import com.yacme.ext.oxsit.security.XOX_SignatureState;
 
 /**
  * this class is the class to be registered when installing the extension
  * 
  * @author beppe
  * 
  */
 public class SyncJob extends ComponentBase
 	implements XServiceInfo, // general
 		XJob, // synchronous Job interface (activates a Java thread for
 		// the XDispatcherInterceptor operations)
 		XCloseable {
 
 	// needed for registration
 	public static final String			m_sImplementationName	= SyncJob.class.getName();
 	public static final String[]		m_sServiceNames			= { "com.sun.star.task.Job" };
 
 	private XModel 						m_axModel;
 	private XFrame						m_axFrame;												// use
 	private XOX_DocumentSignaturesState 		m_aDocSign;
 	// when frame is needed as reference
 	private XComponentContext			m_xComponentContext;
 
 	protected XMultiComponentFactory	m_xServiceManager		= null;
 	
 	private XOX_SingletonDataAccess		m_axoxSingletonDataAccess;
 	
 	private Object m_oSingleLogObj;	
 	private	DynamicLogger				m_aLogger;
 	
 	/**
 	 * The toolkit, that we can create UNO dialogs.
 	 */
 	/*
 	 * private static XToolkit m_xToolkit = null;
 	 */
 	// next 3 static vars are for debug only
 	// public static int m_nOnLoadCount = 0;
 	// public static int m_nOnSaveCount = 0;
 	// public static int m_nOnSaveAsCount = 0;
 	/**
 	 * Constructs a new instance
 	 * 
 	 * @param context
 	 *            the XComponentContext
 	 */
 	public SyncJob(XComponentContext context) {
 
 		m_oSingleLogObj = context.getValueByName(GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE);
 		if(m_oSingleLogObj == null) {
 			System.out.println("cannot build first singleton logger!");
 		}
 
 		m_aLogger = new DynamicLogger(this,context);
 //DEBUG  comment this if no logging needed
 		m_aLogger.enableLogging();
 		m_aLogger.ctor();
 		m_xComponentContext = context;
 		m_axFrame = null;
 		m_axModel = null;
 		m_aDocSign = null;
 
 		try {
 			m_axoxSingletonDataAccess = Helpers.getSingletonDataAccess(m_xComponentContext);
 //			m_aLoggerDialog.info(" singleton service data "+Helpers.getHashHex(m_axoxSingletonDataAccess));
 		} catch (ClassCastException e) {
 			m_aLogger.severe("", "",e);
 		} catch (ServiceNotFoundException e) {
 			m_aLogger.severe("", "",e);
 		} catch (NoSuchMethodException e) {
 			m_aLogger.severe("", "",e);
 		}
 
 		/*
 		 * if(m_xComponentContext != null) System.out.println(" got a context!");
 		 */
 		try {
 			// get the service manager from the component context
 			m_xServiceManager = m_xComponentContext.getServiceManager();
 		} catch (java.lang.Exception ex) {
 			m_aLogger.severe("ctor", "No service manager!", ex);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
 	 */
 	public String getImplementationName() {
 		return m_sImplementationName;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
 	 */
 	public String[] getSupportedServiceNames() {
 		return m_sServiceNames;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
 	 */
 	public boolean supportsService(String _sService) {
 		int len = m_sServiceNames.length;
 
 		for (int i = 0; i < len; i++) {
 			if (_sService.equals( m_sServiceNames[i] ))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Updates the Desktop current component in case of opening, creating or
 	 * swapping to other document
 	 * 
 	 * @return XComponent Returns the current component of Desktop object
 	 * 
 	 */
 	/*
 	 * public void updateCurrentComponent(){
 	 * 
 	 * XComponent ret = null; Object desktop; try { desktop =
 	 * m_xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop",
 	 * m_xComponentContext); XDesktop xDesktop =
 	 * (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop); ret =
 	 * xDesktop.getCurrentComponent();
 	 * 
 	 * this.m_xMultiComponentFactory = this.m_xContext.getServiceManager();
 	 * this.m_xFactory = (XMultiServiceFactory)
 	 * UnoRuntime.queryInterface(XMultiServiceFactory.class,
 	 * this.m_xCurrentComponent); } catch (com.sun.star.uno.Exception ex) {
 	 * ex.printStackTrace(); } this.m_xCurrentComponent = ret; }
 	 */
 
 	// /////////////////////////////////////////////////////////////////////////////////////
 	/**
 	 * implements the execute method of synch Job handler single method for XJob
 	 * 
 	 * @return the result of the job. The concrete semantics is
 	 *         service-dependent. But it should be possible to - deregister the
 	 *         job - let him registered although execution was successfully(!) -
 	 *         make some job specific data persistent inside the job
 	 *         configuration which is provided by the executor.
 	 * 
 	 * 
 	 * as a test, implement once, when the OOo starts at the very beginning to
 	 * initialize the stuff (change the elements)
 	 */
 	public Object execute(NamedValue[] lArgs) throws IllegalArgumentException, Exception {
 		// detect the reason of calling this job
 		com.sun.star.beans.NamedValue[] lGenericConfig = null;
 		com.sun.star.beans.NamedValue[] lJobConfig = null;
 		com.sun.star.beans.NamedValue[] lEnvironment = null;
 		com.sun.star.beans.NamedValue[] lDynamicData = null;
 
 		int c = lArgs.length;
 		for (int i = 0; i < c; ++i) {
 			if (lArgs[i].Name.equals( "Config" )) {
 				lGenericConfig = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
 						.toArray( lArgs[i].Value );
 			}
 			else if (lArgs[i].Name.equals( "JobConfig" )) {
 				lJobConfig = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
 						.toArray( lArgs[i].Value );
 			}
 			else if (lArgs[i].Name.equals( "Environment" )) {
 				lEnvironment = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
 						.toArray( lArgs[i].Value );
 			}
 			else if (lArgs[i].Name.equals( "DynamicData" )) {
 				lDynamicData = (com.sun.star.beans.NamedValue[]) com.sun.star.uno.AnyConverter
 						.toArray( lArgs[i].Value );
 			}
 		}
 
 		/*
 		 * if (lGenericConfig == null) System.out.println("lGenericConfig is
 		 * empty"); if (lJobConfig == null) System.out.println("lJobConfig is
 		 * empty"); if (lDynamicData == null) System.out.println("lDynamicData
 		 * is empty");
 		 */
 		// Analyze the environment info. This sub list is the only guaranteed
 		// one!
 		if (lEnvironment == null)
 			m_aLogger.log( "lEnvironment is empty" );
 		else {
 			java.lang.String sEnvType = null;
 			java.lang.String sEventName = null;
 			c = lEnvironment.length;
 
 			for (int i = 0; i < c; ++i) {
 				if (lEnvironment[i].Name.equals( "EnvType" )) {
 					sEnvType = com.sun.star.uno.AnyConverter.toString( lEnvironment[i].Value );
 				} else if (lEnvironment[i].Name.equals( "EventName" )) {
 					sEventName = com.sun.star.uno.AnyConverter.toString( lEnvironment[i].Value );
 				} else if (lEnvironment[i].Name.equals( "Frame" )) {
 
 					m_axFrame = (com.sun.star.frame.XFrame) com.sun.star.uno.AnyConverter
 							.toObject( new com.sun.star.uno.Type(
 									com.sun.star.frame.XFrame.class ),lEnvironment[i].Value );
 				} else if (lEnvironment[i].Name.equals( "Model" )) {
 					m_axModel = (com.sun.star.frame.XModel) com.sun.star.uno.AnyConverter
 							.toObject( new com.sun.star.uno.Type(
 									com.sun.star.frame.XModel.class ),lEnvironment[i].Value );
 				}
 			}
 			if (m_axModel != null) {
 				XController xController = m_axModel.getCurrentController();
 				if (xController != null) {
 					m_axFrame = xController.getFrame();
 				}
 			}
 			/**
 			 * environment analized, so, do as requested
 			 * OOo code dealing with event firing is in
 			 * http://svn.services.openoffice.org/opengrok/xref/Current%20(trunk)/sfx2/source/appl/appinit.cxx#306
 			 * http://svn.services.openoffice.org/opengrok/xref/Current%20(trunk)/sfx2/source/doc/objxtor.cxx#929
 			 * 
 			 */
 			if (sEventName != null) {
 				m_aLogger.debug("execute", "event received: " + sEventName );
 				if (sEventName.equalsIgnoreCase( "OnStartApp" ) ) {
 					executeOnStartApp();
 				} else if (sEventName.equalsIgnoreCase( "OnViewCreated" )) {
 					executeOnViewCreated();
 				} else if (sEventName.equalsIgnoreCase( "OnLoad" )) {
 					executeOnLoad();
 				} else if (sEventName.equalsIgnoreCase( "OnUnload" )) {
 					executeOnUnload();
 				} else if (sEventName.equalsIgnoreCase( "OnSaveDone" )) {
 					executeOnSaveDone();
 				} else if (sEventName.equalsIgnoreCase( "OnSaveAsDone" )) {
 					executeOnSaveAsDone();
 				}
 				else if (sEventName.equalsIgnoreCase( "OnCloseApp" ))
 					executeOnCloseApp();
 			}
 			else
 				m_aLogger.log("execute", "called with no event ???");
 		}
 		return null;
 	}
 
 	protected void logSystemProperties() {
 		final String propList[] =  { "java.version", 
 		"java.vendor",
 		"java.vendor.url",
 		"java.home",
 		"java.vm.specification.version",
 		"java.vm.specification.vendor",
 		"java.vm.specification.name",
 		"java.vm.version",
 		"java.vm.vendor",
 		"java.vm.name",
 		"java.specification.version",
 		"java.specification.vendor",
 		"java.specification.name",
 		"java.class.version",
 		"java.class.path",
 		"java.library.path",
 		"java.io.tmpdir",
 		"java.compiler",
 		"java.ext.dirs",
 		"os.name",
 		"os.arch",
 		"os.version",
 		"file.separator",
 		"path.separator",
 		"line.separator",
 		"user.name",
 		"user.home",
 		"user.dir" };
 		
 		m_aLogger.log("====================== START of System Property Values ======================");
 		for(int i = 0; i < propList.length; i++) {
 			String aPropValue = System.getProperty(propList[i]);
 			m_aLogger.log(propList[i]+": "+aPropValue);
 		}
 		m_aLogger.log("====================== END of System Property Values ======================");
 	}
 
 	protected void executeOnStartApp() {
 		
 		try {
 		com.yacme.ext.oxsit.uno_types.LogJarVersion uno_TypeStart =
 			new com.yacme.ext.oxsit.uno_types.LogJarVersion();
 		
 		m_aLogger.log(uno_TypeStart.getVersion());
 		
 		com.yacme.ext.oxsit.sjob_uno.LogJarVersion sjob_unoStart = 
 			new com.yacme.ext.oxsit.sjob_uno.LogJarVersion(m_aLogger);
 
 		m_aLogger.log(sjob_unoStart.getVersion());
 		
 		com.yacme.ext.oxsit.sig_opt_uno.LogJarVersion sig_opt_unoStart = 
 			new com.yacme.ext.oxsit.sig_opt_uno.LogJarVersion(m_aLogger);
 		
 		m_aLogger.log(sig_opt_unoStart.getVersion());
 		
 		com.yacme.ext.oxsit.sing_var_uno.LogJarVersion sing_var_unoStart =
 			new com.yacme.ext.oxsit.sing_var_uno.LogJarVersion(m_aLogger);
 		
 		m_aLogger.log(sing_var_unoStart.getVersion());
 		
 		com.yacme.ext.oxsit.sig_uno.LogJarVersion sig_unoStart = 
 			new com.yacme.ext.oxsit.sig_uno.LogJarVersion(m_aLogger);
 		
 		m_aLogger.log(sig_unoStart.getVersion());
 		
 		//log the java system properties
 		logSystemProperties();
 
 		//we'll need to initialize the security stuff, done once on init.
 		
 /*		m_aLogger.log("os.name: \""+System.getProperty("os.name")+"\"");
 		m_aLogger.log("os.arch: \""+System.getProperty("os.arch")+"\"");
 		m_aLogger.log("os.version: \""+System.getProperty("os.version")+"\"");
 
 		m_aLogger.log("java.class.path: \""+System.getProperty("java.class.path")+"\"");
 		m_aLogger.log("java.library.path: \""+
 				System.getProperty("java.library.path")+"\"");*/
 		
 		//try to change the java.library.path
 /*		try {
 			String libPath = System.getProperty("java.library.path");
 			//form the current extension path
 			String m_sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(m_xComponentContext);
 			m_aLogger.log("extension installed in: " + m_sExtensionSystemPath);
 			libPath = libPath+System.getProperty("path.separator")+m_sExtensionSystemPath;
 			System.setProperty("java.library.path", libPath);
 
 		} catch (URISyntaxException e) {
 			m_aLogger.severe( e );*/
 		} catch (java.lang.Exception e) {
 			m_aLogger.severe(e);
 		}
 	}
 
 	protected void executeOnUnload() {
 		// delete the single frame data
 		if (m_axModel != null) {
 			if(m_axoxSingletonDataAccess != null)
 				m_axoxSingletonDataAccess.removeDocumentSignatures(Helpers.getHashHex(m_axModel));
 			else
 				m_aLogger.debug("OnUnload: m_axoxSingletonDataAccess is null");
 		}
 		else
 			m_aLogger.debug("OnUnload: m_axModel is null");		
 	}
 
 	protected void executeOnSaveDone() {
 		// only clear the status of the corresponding frame
 		//save done, update status of the model
 		if (m_axFrame != null) {
 			m_aLogger.info(" model hash: "+Helpers.getHashHex(m_axModel) + " frame hash: " + Helpers.getHashHex(m_axFrame));
 			String aUrl = m_axModel.getURL();
 			if (aUrl.length() > 0) {
 				m_aLogger.info(" model hash: "+Helpers.getHashHex(m_axModel) + " frame hash: " + Helpers.getHashHex(m_axFrame));
 				if(m_axoxSingletonDataAccess != null) {
 					m_aDocSign  = m_axoxSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(m_axModel), null);
 					if(m_aDocSign != null) {
 						//determine the storage, it may be different, set it to the document
 						Helpers.updateAggregateSignaturesState(m_aDocSign, GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES);
 					}
 					else
 						m_aLogger.severe("execute","Missing XOX_DocumentSignaturesState interface");						
 				}
 				else
 					m_aLogger.severe("execute","Missing XOX_SingletonDataAccess interface"); 
 			}
 		}
 	}
 
 	protected void executeOnSaveAsDone() {
 		//a new document, see if already there, init signature states if necessary
 		if (m_axFrame != null) {
 			String aUrl = m_axModel.getURL();
 			if (aUrl.length() > 0) {
 				m_aLogger.debug(" model hash: "+Helpers.getHashHex(m_axModel) + " frame hash: " + Helpers.getHashHex(m_axFrame));
 				if(m_axoxSingletonDataAccess != null) {
 					m_aDocSign  = m_axoxSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(m_axModel), null);
 					if(m_aDocSign != null) {
 						//determine the storage, it may be different, set it to the document
 						Helpers.updateAggregateSignaturesState(m_aDocSign, GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES);
 					}
 					else
 						m_aLogger.severe("execute, OnSaveAsDone","Missing XOX_DocumentSignaturesState interface");						
 				}
 				else
 					m_aLogger.severe("execute, OnSaveAsDone","Missing XOX_SingletonDataAccess interface"); 
 			}
 		}
 	}
 
 	protected void executeOnLoad() throws IOException, Exception {
 		final String __FUNCTION__ = "executeOnLoad: ";
 		/**
 		 * this event is fired up when the document loading is finished
 		 * 
 		 * seems in sfx2/source/doc/objstor.cxx#577 (dev300-m10)
 		 * though I'm not sure
 		 * 
 		 */
 
 		if (m_axFrame != null) {
 			m_aLogger.debug("execute", "document loaded URL: " + m_axModel.getURL() );
 
 //grab the XStorage interface of this document
 /*
 * unfortunately, the storage we get this way doesn't give us access to the
 * whole stuff
 * some part of the storage throw an IOError exception, something that doesn't happen
 * if the package is opened anew using the URL
 */
 			XStorage xStorage = null;
 			XStorageBasedDocument xDocStorage =
 						(XStorageBasedDocument)UnoRuntime.queryInterface( XStorageBasedDocument.class, m_axModel );
 
 			xStorage = xDocStorage.getDocumentStorage();
 			/**
 			 * we can read the file and check if a CNIPA signature
 			 * TODO start a thread to do the job? may be creating a
 			 * sort of user feedback if the file is big or composed
 			 * of a lot of images
 			 * 
 			 */
 			//FIXME: the signature should be checked by using the correct exported service
 			//in IT implementation the service is exported 
 			// check the signature status and init the proper value
 			// in the configuration, check the signature and set the
 			// status properly
 			// after the creation the frame data are manipulated by the
 			// toolbar dispatcher.
 			// if the dispatcher is dead, then no longer needs them,
 			// then the data will be cleared at the next app start
 			m_aLogger.debug("executeOnLoad"," model hash: "+Helpers.getHashHex(m_axModel) + " frame hash: " + Helpers.getHashHex(m_axFrame));						
 			if(m_axoxSingletonDataAccess != null) {
 				m_aDocSign  = m_axoxSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(m_axModel), null);
 				if(m_aDocSign != null) {
 					m_aDocSign.setDocumentStorage(xStorage);
 					Helpers.updateAggregateSignaturesState(m_aDocSign, GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES);
 //try to load see if the file has signatures
 					Object aDocVerService = m_xServiceManager.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xComponentContext);
 					if(aDocVerService != null) {				
 						XOX_DocumentSignaturesVerifier m_axoxDocumentVerifier =
 							(XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
 						if(m_axoxDocumentVerifier != null) {
 //try to load the signatures and set the local data appropriately							
 							XOX_SignatureState[] aSigStates = 
 								m_axoxDocumentVerifier.loadAndGetSignatures(m_axFrame,m_axModel);
 							if(aSigStates != null) {
 								//signatures present, set status
 								Helpers.updateAggregateSignaturesState(m_aDocSign, GlobConstant.m_nSIGNATURESTATE_UNKNOWN);
 								//FIXME: if signatures are present, or not: not preset = no_SIGNATURES, present = STATE UNKNOWN
 								//verify signatures, if the case (check if this is true, or if we need to start a thread and wai for it's completion
 								for(int idx = 0; idx < aSigStates.length; idx++) {
 									//load them internally
 									//else use this and add to the local storage
 									m_aDocSign.addSignatureState(aSigStates[idx]);
 									m_aLogger.debug(__FUNCTION__+"signature state added");									
 								}
 							}
 //							else status alread set to m_nSIGNATURESTATE_NOSIGNATURES
 					        // now clean up the verifier
 					        ((XComponent) UnoRuntime.queryInterface(XComponent.class, aDocVerService)).dispose();
 						}
 					}
 				}
 				else
 					m_aLogger.severe(__FUNCTION__, "cannot create the document data!" );
 			}
 			else
 				m_aLogger.severe("executeOnLoad","Missing XOX_SingletonDataAccess interface"); 
 		}		
 	}
 
 	///////////////////// OnViewCreated //////////////////////////7
 	protected void executeOnViewCreated() throws Exception {
 		/**
 		 * this event is fired at the end of the method sal_Bool
 		 * SfxTopFrame::InsertDocument( SfxObjectShell* pDoc ) in
 		 * file sfx2/source/view/topfrm.cxx When the view has been
 		 * created and the model, frame, controller are all
 		 * connected
 		 * 
 		 * try to get the frame, then activate at the frame our
 		 * interceptor
 		 * 
 		 */
 		if (m_axFrame != null) {
 			if (canBeSigned(m_axFrame)) {
 				try {
 					Object aObj = m_xServiceManager.createInstanceWithContext(GlobConstant.m_sDISPATCH_INTERCEPTOR_SERVICE, m_xComponentContext);
 					XOX_DispatchInterceptor xD = (XOX_DispatchInterceptor)UnoRuntime.queryInterface(XOX_DispatchInterceptor.class, aObj);
 					xD.startListening(m_axFrame);
 				}
 				catch (RuntimeException ex) {
 					m_aLogger.severe("executeOnViewCreated", "cannot create DispatchInterceptor", ex);
 				}
 			}
 		}
 	}
 
 	/**
 	 * executed when application is closed, stops the logger
 	 * close all the log files
 	 */
 	protected void executeOnCloseApp() {
 		//at this stage, remove cache used in running
 		//in Italian implementation this contains the CA list and the CRL used to control the 
 		// certificates
 //grab the singleton, the dispose will stop the logging function as well
 		XComponent xSingle = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_oSingleLogObj);
 		if(xSingle != null)
 			xSingle.dispose();
 		else
 			System.out.println("Singleton doesn't exists !");
 	}
 
 	/*
 	 * this section contains all the method needed to access the registry. It
 	 * seems that the Sync job MUST have the registry manipulation inside
 	 * itself. There isn't the possibility to do it in an another java class
 	 * even inside the same package
 	 * 
 	 */
 
 	/**
 	 * 
 	 * 
 	 * @param _TheViewRoot
 	 */
 	private void commitChanges(Object _TheViewRoot) {
 		XChangesBatch xUpdateControl = (XChangesBatch) UnoRuntime.queryInterface(
 				XChangesBatch.class, _TheViewRoot );
 		try {
 			xUpdateControl.commitChanges();
 		} catch (WrappedTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private XMultiServiceFactory getConfigurationProvider() {
 		XMultiServiceFactory aConfProvider = null;
 		// get provider
 		final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
 
 		try {
 			aConfProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(
 					XMultiServiceFactory.class, m_xServiceManager
 							.createInstanceWithContext( sProviderService, m_xComponentContext ) );
 		} catch (Exception e) {
 			e.printStackTrace();
 			m_aLogger.info( "error !" );
 		}
 		return aConfProvider;
 	}
 
 	/**
 	 * return an opened read write view on a configuration node
 	 * 
 	 * @param aConfProvider
 	 * @param aPath
 	 * @return
 	 */
 	private Object getReadWriteView(XMultiServiceFactory aConfProvider, String aPath) {
 		// The service name: Need update access:
 		final String cUpdatableView = "com.sun.star.configuration.ConfigurationUpdateAccess";
 
 		// creation arguments: nodepath
 		com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
 		aPathArgument.Name = "nodepath";
 		aPathArgument.Value = GlobConstant.m_sEXTENSION_CONF_FRAME_KEY;
 		// creation arguments: commit mode - write-through or write-back
 		com.sun.star.beans.PropertyValue aModeArgument = new com.sun.star.beans.PropertyValue();
 		aModeArgument.Name = "enableasync";
 		aModeArgument.Value = new Boolean( false );
 
 		Object[] aArguments = new Object[2];
 		aArguments[0] = aPathArgument;
 		aArguments[1] = aModeArgument;
 
 		// create the view
 		Object xViewRoot = null;
 		try {
 			xViewRoot = aConfProvider.createInstanceWithArguments( cUpdatableView,
 					aArguments );
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return xViewRoot;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.util.XCloseable#close(boolean)
 	 */
 	public void close(boolean arg0) throws CloseVetoException {
 		// TODO Auto-generated method stub
 		m_aLogger.entering( "close called" );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.util.XCloseBroadcaster#addCloseListener(com.sun.star.util.XCloseListener)
 	 */
 	public void addCloseListener(XCloseListener arg0) {
 		// TODO Auto-generated method stub
 		m_aLogger.entering( "addCloseListener called" );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sun.star.util.XCloseBroadcaster#removeCloseListener(com.sun.star.util.XCloseListener)
 	 */
 	public void removeCloseListener(XCloseListener arg0) {
 		// TODO Auto-generated method stub
 		m_aLogger.entering( "removeCloseListener called" );
 	}
 
 	/** check if the document type can be signed
 	 * 
 	 * @param _theModel
 	 * @return
 	 */
 	protected boolean canBeSigned(XModel _theModel) {
 		// detect if the document we are linked to is a Writer,
 		// an Impress or a Draw one
 		// if false the XAdES CNIPA signature is not supported
 		// get the component, we then query the interface XSpreadsheetDocument
 		// from the model
 		XSpreadsheetDocument xSpreadsheetDocument = 
 			(XSpreadsheetDocument)UnoRuntime.queryInterface( XSpreadsheetDocument.class, _theModel );
 		// we query the interface XTextDocument from the model
 		XTextDocument xTextDocument =
 			(XTextDocument)UnoRuntime.queryInterface( XTextDocument.class, _theModel );
 		// we query the interface XDrawPagesSupplier from the
 		// model
 		XDrawPagesSupplier xDrawDocument =
 			(XDrawPagesSupplier) UnoRuntime.queryInterface( XDrawPagesSupplier.class, _theModel );
 		// and last, the XPresentation interface, again from the model.
 		XPresentation xPresentation =
 			(XPresentation) UnoRuntime.queryInterface( XPresentation.class, _theModel );
 
 		// test if it's a XAdES (CNIPA) signable type of document, then start the
 		// interceptor
 		//DOCUMENTTYPE    <<--= don't remove that text key,
 		//i's a developer bookmark, t's needed if document type is changed
 		if (xSpreadsheetDocument != null || xTextDocument != null
 				|| xDrawDocument != null || xPresentation != null)
 			return true;
 		return false;
 	}
 
 	//overloaded version of same method above
 	protected boolean canBeSigned(XFrame _theFrame) {
 		return canBeSigned(_theFrame.getController().getModel());
 	}
 }
