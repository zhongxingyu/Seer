 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.networkplaces.actions;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileType;
 import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 import com.adito.boot.Util;
 import com.adito.core.BundleActionMessage;
 import com.adito.core.CoreEvent;
 import com.adito.core.CoreException;
 import com.adito.core.CoreServlet;
 import com.adito.core.CoreUtil;
 import com.adito.core.FileDownloadPageInterceptListener;
 import com.adito.core.PopupException;
 import com.adito.core.RedirectWithMessages;
 import com.adito.core.actions.AbstractPopupAuthenticatedDispatchAction;
 import com.adito.networkplaces.NetworkPlace;
 import com.adito.networkplaces.NetworkPlacePlugin;
 import com.adito.networkplaces.NetworkPlaceResourceType;
 import com.adito.networkplaces.NetworkPlaceUploadHandler;
 import com.adito.networkplaces.NetworkPlacesException;
 import com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent;
 import com.adito.networkplaces.forms.FileSystemForm;
 import com.adito.networkplaces.model.FileItem;
 import com.adito.networkplaces.model.FileSystemItem;
 import com.adito.networkplaces.model.FolderItem;
 import com.adito.policyframework.LaunchSession;
 import com.adito.policyframework.LaunchSessionFactory;
 import com.adito.policyframework.NoPermissionException;
 import com.adito.security.AbstractHTTPAuthenticationModule;
 import com.adito.security.Constants;
 import com.adito.security.LogonControllerFactory;
 import com.adito.security.SessionInfo;
 import com.adito.vfs.FileObjectVFSResource;
 import com.adito.vfs.UploadDetails;
 import com.adito.vfs.VFSRepository;
 import com.adito.vfs.VFSResource;
 import com.adito.vfs.VfsUtils;
 import com.adito.vfs.ZipDownload;
 import com.adito.vfs.clipboard.Clipboard;
 import com.adito.vfs.clipboard.ClipboardContent;
 import com.adito.vfs.webdav.DAVAuthenticationRequiredException;
 import com.adito.vfs.webdav.DAVException;
 import com.adito.vfs.webdav.DAVTransaction;
 import com.adito.vfs.webdav.DAVUtilities;
 
 /**
  * <p>
  * This class performs the operations on the file system accessed through a
  * {@link com.adito.networkplaces.NetworkPlace}.
  * <p>
  * This action <b>must</b> use request scope as there may be many instances in
  * one users session.
  */
 public class FileSystemViewDispatchAction extends AbstractPopupAuthenticatedDispatchAction {
 
 	final static Log log = LogFactory.getLog(FileSystemViewDispatchAction.class);
 
 	final static List<String> NO_DELETE = new ArrayList<String>();
 	final static List<String> READ_ONLY = new ArrayList<String>();
 
 	static {
 		// This is a holder for the actions which are not allowed for a given
 		// parameter.
 		NO_DELETE.add("delete");
 		NO_DELETE.add("deleteFile");
 
 		READ_ONLY.add("delete");
 		READ_ONLY.add("deleteFile");
 		READ_ONLY.add("cut");
 		READ_ONLY.add("cutFile");
 		READ_ONLY.add("paste");
 		READ_ONLY.add("pasteFile");
 		READ_ONLY.add("showMkDir");
 		READ_ONLY.add("upload");
 	}
 
 	/**
 	 * Constructor
 	 */
 	public FileSystemViewDispatchAction() {
 		super(null, null, NetworkPlacePlugin.NETWORK_PLACE_RESOURCE_TYPE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
 	 *      org.apache.struts.action.ActionForm,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		try {
 			ActionForward fwd = super.execute(mapping, form, request, response);
             // JB This has been removed for defect #461 pages expired.
 			// Util.noCache(response);
 			return fwd;
 		} catch (PopupException pe) {
             Throwable cause = pe.getCause();
             if (cause instanceof CoreException) {
                 ActionMessages errs = getErrors(request);
                 errs.add(Globals.ERROR_KEY, ((CoreException) cause).getBundleActionMessage());
                 saveErrors(request, errs);
                 return mapping.getInputForward();
             } else if (cause instanceof DAVException) {
             	log.error("File system operation failed.", cause);
                 String maskedException = VfsUtils.maskSensitiveArguments(cause.getMessage());
                 throw new IOException(maskedException);
             } else if (cause instanceof FileSystemException) {
             	log.error("File system operation failed.", cause);
                 String maskedException = VfsUtils.maskSensitiveArguments(cause.getMessage());
                 throw new IOException(maskedException);
             }
             throw pe;
         }
 	}
     
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.struts.actions.DispatchAction#unspecified(org.apache.struts.action.ActionMapping,
 	 *      org.apache.struts.action.ActionForm,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 										HttpServletResponse response) throws Exception {
 		return super.unspecified(mapping, form, request, response);
 	}
 
 	/**
 	 * <p>
 	 * Return the file system to the home of the <@link
 	 * com.adito.vfs.NetworkPlace>.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if an exception is thrown.
 	 */
 	public ActionForward home(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("List files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			return new ActionForward(mapping.getPath() + ".do?actionTarget=list&path=" + res.getMount().getMountString() + "&" + LaunchSession.LAUNCH_ID + "=" + fileSystemForm.getLaunchId(), true);
 
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 
 	}
 
 	/**
 	 * <p>
 	 * Return the file system to the location specified.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward gotoPath(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("goto path location.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 		fileSystemForm.clearPathsTo(request.getParameter("id"));
 		return this.list(mapping, fileSystemForm, request, response);
 	}
 
 	/**
 	 * <p>
 	 * Rename the selected file.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward renameFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("rename the file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		checkLaunchSession(request, response, fileSystemForm);
 		ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 		if (fwd != null) {
 			return fwd;
 		}
 
 		VFSResource sourceResource = null;
 		VFSResource destResource = null;
 		try {
 			sourceResource = getResourceForPath(fileSystemForm.getLaunchSession(),
 				request,
 				response,
 				fileSystemForm.getPath() + "/" + fileSystemForm.getFileName());
 			destResource = getResourceForPath(fileSystemForm.getLaunchSession(), request, response, fileSystemForm.getPath() + "/"
 				+ fileSystemForm.getNewName());
 
 			if (sourceResource == null) {
 				throw new Exception("Could not locate source resource '" + fileSystemForm.getPath()
 					+ "/"
 					+ fileSystemForm.getFileName()
 					+ "'");
 			}
 			if (destResource == null) {
 				throw new Exception("Could not locate destination resource '" + fileSystemForm.getPath()
 					+ "/"
 					+ fileSystemForm.getNewName()
 					+ "'");
 			}
             
             if(sourceResource.getFullPath().equals(destResource.getFullPath())) {
                 return mapping.findForward("list");
             }
             
 			fwd = checkMount(destResource, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			sourceResource.move(destResource, true);
 
 			if (sourceResource.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessRenameEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					sourceResource.getDisplayName(),
 					destResource.getDisplayName(),
 					null));
 			}
 
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		} catch (Exception e) {
 			if (sourceResource != null && destResource != null) {
 				if (sourceResource.getMount().getStore().getProvider().isFireEvents()) {
 					CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessRenameEvent(this,
 						fileSystemForm.getLaunchSession(),
 						request,
 						fileSystemForm.getPath(),
 						fileSystemForm.getFullURI(),
 						sourceResource.getDisplayName(),
 						destResource.getDisplayName(),
 						e));
 				}
 			}
 			if (log.isErrorEnabled()) {
 				log.error("Rename Error.", e);
 			}
 			saveError(request, "vfs.rename.error", e.getMessage());
 		}
 
 		return mapping.findForward("list");
 	}
 
 	/**
 	 * <p>
 	 * Upload a file to the file system.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward upload(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("List files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 		checkLaunchSession(request, response, fileSystemForm);
 		ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 		if (fwd != null) {
 			return fwd;
 		}
 		ActionForward returnTo = new ActionForward("/fileSystem.do?" + LaunchSession.LAUNCH_ID + "=" + fileSystemForm.getLaunchSession().getId()
 			+ "&actionTarget=list&path="
 			+ DAVUtilities.encodePath(fileSystemForm.getPath()), true);
 		UploadDetails details = new UploadDetails(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 						NetworkPlaceUploadHandler.TYPE_VFS,
 						fileSystemForm.getPath(),
 						mapping.findForward("upload"),
 						returnTo,
 						null);
 		details.setExtraAttribute1(fileSystemForm.getPath());
 		details.setExtraAttribute2(fileSystemForm.getLaunchSession().getId());
 		request.setAttribute(Constants.REQ_ATTR_UPLOAD_DETAILS, new Integer(CoreUtil.addUpload(request.getSession(), details)));
 		return mapping.findForward("upload");
 	}
 
 	/**
 	 * Refilter.
 	 * 
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @return forward
 	 * @throws Exception on any error
 	 */
 	public ActionForward filter(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		return list(mapping, form, request, response);
 	}
 
 	/**
 	 * List.
 	 * 
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @return forward
 	 * @throws Exception on any error
 	 */
 	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("List files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 
 			/**
 			 * LDP - This extra getChildren exception was being ignored. This is not
 			 * acceptable behaviour, instead we now catch the exception and display as
 			 * an error on screen, and log to file. 
 			 * 
 			 * This extra getChildren also meant that each directory is listed TWICE!
 			 */
 //            
 //            try {
 //                res.getChildren(); 
 //                // if we can't get the children we are assuming access was denied
 //            } catch (FileSystemException e) {
 //
 //            }
             
 			buildModel(res, fileSystemForm, request);
 			if (res.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessListEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					null));
 			}
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		} catch (UnknownHostException e) {
             ActionMessages errs = getErrors(request);
             errs.add(Globals.ERROR_KEY, new ActionMessage("error.networkPlaces.unknown.host", e.getMessage()));
             saveErrors(request, errs);
             return mapping.findForward("display");
 		} catch (Exception e) {
 			CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessListEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					e));
 			
             ActionMessages errs = getErrors(request);
             errs.add(Globals.ERROR_KEY, new ActionMessage("error.networkPlaces.generic", VfsUtils.maskSensitiveArguments(e.getMessage())));
             saveErrors(request, errs);
             String stacktraceAsString = VfsUtils.maskSensitiveArguments(CoreUtil.toString(e));
             log.error("File system error:" + stacktraceAsString);
             return mapping.findForward("display");
         }
 
 		return mapping.findForward("display");
 	}
 
 	/**
 	 * Launch.
 	 * 
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @return forward
 	 * @throws Exception on any error
 	 */
 	public ActionForward launch(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Launch network place.");
 		return list(mapping, form, request, response);
 	}
 
 	void buildModel(VFSResource res, FileSystemForm fileSystemForm, HttpServletRequest request) throws Exception {
 		List<FileSystemItem> allFileSystemItems = new ArrayList<FileSystemItem>();
 		ActionMessages warnings = new ActionMessages();
 		if (res != null) {
 			Iterator itr = res.getChildren();
 
 			for (int i = 0; itr != null && itr.hasNext(); i++) {
 				// We overide to string so filtering works
 				Calendar gc = new GregorianCalendar() {
 					public String toString() {
 						return SimpleDateFormat.getInstance().format(this.getTime());
 					}
 				};
                 gc.setTimeInMillis(0);
 				FileObjectVFSResource element = (FileObjectVFSResource) itr.next();
 				// this is an extra defense against imaginary files.
 				FileType ft = null;
 				try {
 					ft = element.getFile().getType();
 				} catch (FileSystemException e) {
 				} catch (IOException e) {
 				}
 				FileSystemItem item = null;
 				if (ft != null && element.getFile().getType().equals(FileType.FOLDER)
 					&& fileSystemForm.getNetworkPlace().isAllowRecursive()) {
 					// if it is a folder
                     if(element.getLastModified() != null) 
                         gc.setTime(element.getLastModified());
 					item = new FolderItem(fileSystemForm.getLaunchSession(), element.getDisplayName(), res.getMount()
 									.getStore()
 									.getName(), fileSystemForm.getPath(), gc, element.getFile().getType().getName(), false, i);
 				} else if (ft != null && element.getFile().getType().equals(FileType.FILE)) {
 					// if it is a file
                     if(element.getLastModified() != null) 
                         gc.setTime(element.getLastModified());
 					item = new FileItem(fileSystemForm.getLaunchSession(), element.getDisplayName(), element.getContentLength()
 									.longValue(), gc, element.getFile().getType().getName(), false, i);
 				} else {
 					if (log.isInfoEnabled())
 						log.info("Unable to display file " + element.getDisplayName() + " as it is an imaginary file.");
 					warnings.add(Constants.REQ_ATTR_WARNINGS, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY,
 									"vfs.imaginary.file",
 									element.getDisplayName()));
 
 					// decrement the counter as there is no file added.
 					i--;
 				}
 				if (item != null) {
 					allFileSystemItems.add(item);
 					if (request.getParameter("select" + Util.urlEncode(item.getFileName())) != null) {
 						item.setChecked(true);
 					}
 				}
 			}
 			if (fileSystemForm.getPaths() == null || fileSystemForm.getPaths().isEmpty()) {
 				fileSystemForm.setHome(fileSystemForm.getPath());
 			} else {
 				fileSystemForm.addPath(fileSystemForm.getPath());
 			}
 		}
 		if (warnings.size() > 0) {
 			addWarnings(request, warnings);
 		}
 		fileSystemForm.initialize(request, allFileSystemItems, this.getSessionInfo(request));
 	}
 
 	VFSResource checkLaunchSession(HttpServletRequest request, HttpServletResponse response, FileSystemForm fileSystemForm)
 					throws Exception {
 		LaunchSession launchSession = fileSystemForm.getLaunchSession();
 		if (launchSession == null) {
 			if (fileSystemForm.getLaunchId() == null || fileSystemForm.getLaunchId() == "") {
 				// For a path that is not a network place 
 				launchSession = new LaunchSession(getSessionInfo(request));
 			}
 			else {
 				launchSession = LaunchSessionFactory.getInstance().getLaunchSession(getSessionInfo(request), fileSystemForm.getLaunchId());
 			}
 			if(launchSession == null) {
 				throw new Exception("No launch session.");
 			}
 			fileSystemForm.setLaunchSession(launchSession);
 		}
 		DAVTransaction transaction = new DAVTransaction(request, response);
 		VFSRepository repository = VFSRepository.getRepository(launchSession.getSession().getHttpSession());
 		VFSResource res = repository.getResource(launchSession, fileSystemForm.getPath(), transaction.getCredentials());
 		if (res == null) {
 			throw new Exception("Could not find network place resource for path " + fileSystemForm.getPath() + ".");
 		}
 		fileSystemForm.setVFSResource(res);
 		return res;
 	}
 
 	VFSResource getResourceForPath(LaunchSession launchSession, HttpServletRequest request, HttpServletResponse response,
 									String path) throws Exception {
 
 		DAVTransaction transaction = new DAVTransaction(request, response);
 
 		VFSRepository repository = VFSRepository.getRepository(request.getSession());
 		VFSResource res = repository.getResource(launchSession, path, transaction.getCredentials());
 
 		if (res == null) {
 			throw new Exception("Could not find network place resource for path " + path + ".");
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adito.core.actions.CoreAction#getNavigationContext(org.apache.struts.action.ActionMapping,
 	 *      org.apache.struts.action.ActionForm,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public int getNavigationContext(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
 		return SessionInfo.MANAGEMENT_CONSOLE_CONTEXT | SessionInfo.USER_CONSOLE_CONTEXT;
 	}
 
 	/**
 	 * <p>
 	 * Zip a collection of files from the file system to your choice.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward zip(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Zip files.");
 
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			String[] uris = fileSystemForm.getSelectedFileNames();
 			if (uris == null || uris.length < 1) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new ActionMessage("vfs.zip.select.error"));
 				saveErrors(request, msgs);
 				return mapping.getInputForward();
 			}
 			return zipSelection(mapping, request, fileSystemForm, uris);
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Zip a selection of files.
 	 * 
 	 * @param mapping action mapping
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param fileSystemForm The <code>FileSystemForm<code> for the action.
 	 * @param uris An array of selected files.
 	 * @return fwd forward to direct
 	 */
 	private ActionForward zipSelection(ActionMapping mapping, HttpServletRequest request, FileSystemForm fileSystemForm,
 										String[] uris) {
 		ActionForward fwd = mapping.findForward("showFileDownload");
 		/*
 		 * TODO now the vfs displays in its own window using page intercept
 		 * listeners is going to be a bad idea.
 		 * 
 		 * In this case we forward straight to the download page to get it out
 		 * of the way as quick as possible.
 		 * 
 		 * If the user tries to use the main interface whilsts a download is
 		 * waiting, weird stuff is going to happen.
 		 * 
 		 * In fact, now there is the possibility of multiple windows for every
 		 * use page intercept listeners are a bad idea full stop.
 		 */
 		FileDownloadPageInterceptListener l = (FileDownloadPageInterceptListener) CoreUtil.getPageInterceptListenerById(request.getSession(),
 			FileDownloadPageInterceptListener.INTERCEPT_ID);
 		if (l == null) {
 			l = new FileDownloadPageInterceptListener();
 			CoreUtil.addPageInterceptListener(request.getSession(), l);
 		}
 		int id = l.addDownload(new ZipDownload(fileSystemForm.getLaunchSession(),
 						fwd,
 						fileSystemForm.getPath(),
 						uris,
 						new ActionForward("/fileSystem.do?actionTarget=list&path=" + fileSystemForm.getPath() + "&" + LaunchSession.LAUNCH_ID + "=" + fileSystemForm.getLaunchId()),
 						"downloadZip.message",
 						NetworkPlacePlugin.MESSAGE_RESOURCES_KEY));
 
 		CoreEvent evt = NetworkPlaceResourceType.getResourceAccessZipEvent(this,
 			fileSystemForm.getLaunchSession(),
 			request,
 			fileSystemForm.getPath(),
 			fileSystemForm.getFullURI(),
 			null);
 		int fileCounter = 1;
 		int directorieCounter = 1;
 		for (int i = 0; i < uris.length; i++) {
 			if (uris[i].endsWith("/")) {
 				NetworkPlaceResourceType.addDirectoryAttribute(evt, uris[i], directorieCounter);
 				directorieCounter++;
 			} else {
 				NetworkPlaceResourceType.addFileAttribute(evt, uris[i], fileCounter);
 				fileCounter++;
 			}
 		}
 		CoreServlet.getServlet().fireCoreEvent(evt);
 		return CoreUtil.addParameterToForward(fwd, "id", String.valueOf(id));
 	}
 
 	/**
 	 * <p>
 	 * Delete the selected files.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Delete files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			String[] uris = fileSystemForm.getSelectedFileNames();
 			for (int i = 0; i < uris.length; i++) {
 				String delPath = fileSystemForm.getPath() + "/" + uris[i];
 				deleteSingleFile(request, response, delPath, fileSystemForm);
 			}
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * redirect ot confirm the deletion.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward deleteSelected(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 										HttpServletResponse response) throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Delete selected files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			Object[] uris = fileSystemForm.getSelectedFileNames();
 			if (uris == null || uris.length < 1) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new ActionMessage("vfs.delete.select.error"));
 				saveErrors(request, msgs);
 				return mapping.getInputForward();
 			}
 			return mapping.findForward("deleteFiles");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Delete the selected file.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward deleteFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Delete file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			String delPath = fileSystemForm.getPath() + "/" + fileSystemForm.getFileName();
 			deleteSingleFile(request, response, delPath, fileSystemForm);
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Confirm deletion of the selected single file.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward confirmDeleteFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 											HttpServletResponse response) throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Delete file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			return mapping.findForward("deleteFile");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Cut the selected file into the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward cutFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Cut file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			addToClipboard(request, fileSystemForm, true);
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param fileSystemForm The <code>FileSystemForm<code> for the action.
 	 * @param delOrig Delete the source files.
 	 */
 	private void addToClipboard(HttpServletRequest request, FileSystemForm fileSystemForm, boolean delOrig) {
 		String delPath = fileSystemForm.getPath() + "/" + fileSystemForm.getFileName();
 		ActionMessages msgs = new ActionMessages();
 		Clipboard cb = new Clipboard();
 		NetworkPlaceClipboardContent fcc = new NetworkPlaceClipboardContent(delPath, delOrig);
 		cb.addContent(fcc);
 		request.getSession().setAttribute(Constants.CLIPBOARD, cb);
 		msgs.add(Globals.MESSAGE_KEY, new ActionMessage("vfs.copy.to.clipboard", fileSystemForm.getFileName()));
 		this.addMessages(request, msgs);
 	}
 
 	/**
 	 * <p>
 	 * Copy the selected file into the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward copyFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Copy file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			addToClipboard(request, fileSystemForm, false);
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Zip the selected file.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward zipFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Zip file.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			String[] uris = { fileSystemForm.getFileName() };
 			return zipSelection(mapping, request, fileSystemForm, uris);
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Paste the selected files from the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if an exception is thrown.
 	 */
 	public ActionForward confirmPasteFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 	    return pasteFile(mapping, form, request, response, true);
 	}
     
     /**
      * <p>
      * Paste the contents of the
      * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent} to the
      * specified location.
      * 
      * @param mapping The <code>ActionMapping<code> associated with this dispatch action.
      * @param form The <code>FileSystemForm<code> for the action.
      * @param request The <code>HttpServletRequest<code> for the action.
      * @param response The <code>HttpServletResponse<code> for the action.
      * @return <code>ActionForward<code> The result of the action.
      * @throws Exception if aan exception is thrown.
      */
     public ActionForward pasteOverwriteFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         return pasteFile(mapping, form, request, response, false);
     }
 
     private ActionForward pasteFile(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, boolean requiresOverwriteCheck)
 	            throws Exception {
 	    if (log.isDebugEnabled()) {
 	        log.debug("Paste file.");
         }
 	    try {
             FileSystemForm fileSystemForm = (FileSystemForm) form;
             checkLaunchSession(request, response, fileSystemForm);
 	        ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 	        if (fwd != null) {
 	            return fwd;
 	        }
 	        
 	        String path = DAVUtilities.concatenatePaths(fileSystemForm.getPath(), fileSystemForm.getFileName());
 	        if (requiresOverwriteCheck) {
 	            boolean isOverwritingFiles = isOverwritingFiles(request, response, path, fileSystemForm);
 	            if (isOverwritingFiles) {
 	                return mapping.findForward("pasteOverwriteFiles");
 	            }
 	        }
 	        
 	        pasteFromClipboard(request, response, path, fileSystemForm);
 	        return mapping.findForward("list");
 	    } catch (DAVAuthenticationRequiredException e) {
 	        AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 	        return null;
 	    }
 	}
 
 	/**
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @param delPath The path to the resource to be deleted.
 	 * @param fileSystemForm the form
 	 * @throws Exception if an exception is thrown.
 	 */
 	private void deleteSingleFile(HttpServletRequest request, HttpServletResponse response, String delPath,
 									FileSystemForm fileSystemForm) throws Exception {
 		VFSResource res = getResourceForPath(fileSystemForm.getLaunchSession(), request, response, delPath);
 		if (log.isDebugEnabled())
 			log.debug("Deleting " + res.getRelativePath());
 		String name = null;
 		try {
 			name = res.getDisplayName();
 			res.delete();
 			if (res.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessDeleteEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					name,
 					null));
 			}
 		} catch (DAVException dave) {
 			if (log.isErrorEnabled()) {
 				log.error("Delete Error.", dave);
 			}
 			saveError(request, "vfs.delete.error", name);
 		} catch (Exception e) {
 			if (res.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessDeleteEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					name,
 					e));
 			}
 			throw e;
 		}
 
 	}
 
 	/**
 	 * <p>
 	 * Cut the selected files into the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward cut(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Cut files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			Object[] uris = fileSystemForm.getSelectedFileNames();
 			if (uris == null || uris.length < 1) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new ActionMessage("vfs.cut.select.error"));
 				saveErrors(request, msgs);
 				return mapping.getInputForward();
 			}
 			copyFilesToClipboard(form, request, response, true, fileSystemForm.getSelectedFileNames());
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Copy the selected files into the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		if (log.isDebugEnabled())
 			log.debug("Copy files.");
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			VFSResource res = checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fwd = checkMount(res, mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			buildModel(res, fileSystemForm, request);
 			String[] uris = fileSystemForm.getSelectedFileNames();
 			if (uris == null || uris.length < 1) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new ActionMessage("vfs.copy.select.error"));
 				saveErrors(request, msgs);
 				return mapping.getInputForward();
 			}
 			copyFilesToClipboard(form, request, response, false, uris);
 			return mapping.findForward("list");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Copy files into the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent}.
 	 * 
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @param deleteOnPaste weather the source resource is deleted on th epaste action.
 	 * @param uris uris to copy
 	 */
 	private void copyFilesToClipboard(ActionForm form, HttpServletRequest request, HttpServletResponse response,
 										boolean deleteOnPaste, String[] uris) {
 		ActionMessages msgs = new ActionMessages();
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 		String allFiles = "";
 		Clipboard cb = new Clipboard();
 		if (uris != null) {
 			for (int i = 0; i < uris.length; i++) {
 				String divider = allFiles.equals("") ? "" : ", ";
 				allFiles = allFiles + divider + uris[i];
 				NetworkPlaceClipboardContent fcc = new NetworkPlaceClipboardContent(fileSystemForm.getPath() + "/" + uris[i],
 								deleteOnPaste);
 				cb.addContent(fcc);
 			}
 			msgs.add(Globals.MESSAGE_KEY, new ActionMessage("vfs.copy.to.clipboard", allFiles));
 		}
 		this.addMessages(request, msgs);
 		request.getSession().setAttribute(Constants.CLIPBOARD, cb);
 	}
 
 	/**
 	 * <p>
 	 * Paste the contents of the
 	 * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent} to the
 	 * specified location.
 	 * 
 	 * @param mapping The <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward confirmPaste(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
         return pasteFiles(mapping, form, request, response, true);
     }
     
     /**
      * <p>
      * Paste the contents of the
      * {@link com.adito.networkplaces.clipboard.NetworkPlaceClipboardContent} to the
      * specified location.
      * 
      * @param mapping The <code>ActionMapping<code> associated with this dispatch action.
      * @param form The <code>FileSystemForm<code> for the action.
      * @param request The <code>HttpServletRequest<code> for the action.
      * @param response The <code>HttpServletResponse<code> for the action.
      * @return <code>ActionForward<code> The result of the action.
      * @throws Exception if aan exception is thrown.
      */
     public ActionForward pasteOverwriteFiles(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         return pasteFiles(mapping, form, request, response, false);
     }
 
     private ActionForward pasteFiles(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, boolean requiresOverwriteCheck)
                     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Paste files.");
         }
         try {
             FileSystemForm fileSystemForm = (FileSystemForm) form;
             ActionForward validatePasteForward = validatePasteForward(mapping, fileSystemForm, request, response);
             if(validatePasteForward != null) {
                 return validatePasteForward;
             }
             
             String path = DAVUtilities.concatenatePaths(fileSystemForm.getPath(), fileSystemForm.getFileName());
             if (requiresOverwriteCheck) {
                 boolean isOverwritingFiles = isOverwritingFiles(request, response, path, fileSystemForm);
                 if (isOverwritingFiles) {
                     return mapping.findForward("pasteOverwriteFiles");
                 }
             }
             
             pasteFromClipboard(request, response, path, fileSystemForm);
             return mapping.findForward("list");
         } catch (DAVAuthenticationRequiredException e) {
             AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
             return null;
         }
     }
     
     private ActionForward validatePasteForward(ActionMapping mapping, FileSystemForm fileSystemForm, HttpServletRequest request, HttpServletResponse response) 
                     throws Exception {
         VFSResource vfsResource = checkLaunchSession(request, response, fileSystemForm);
         ActionForward forward = checkAccess(mapping, fileSystemForm, request);
         if (forward != null) {
             return forward;
         }
         forward = checkMount(vfsResource, mapping, fileSystemForm, request);
         if (forward != null) {
             return forward;
         }
         buildModel(vfsResource, fileSystemForm, request);
         Clipboard clipboard = (Clipboard) request.getSession().getAttribute("clipboard");
         if (clipboard != null) {
             if (!clipboard.getContent().iterator().hasNext()) {
                 ActionMessages msgs = getErrors(request);
                 msgs.add(Globals.ERROR_KEY, new ActionMessage("vfs.paste.error"));
                 saveErrors(request, msgs);
                 return mapping.getInputForward();
             }
         }
         return null;
     }
     
     private boolean isOverwritingFiles(HttpServletRequest request, HttpServletResponse response, String destinationPath, FileSystemForm fileSystemForm) 
                     throws Exception {
         Map<VFSResource, VFSResource> pasteMap = buildPasteMap(request, response, destinationPath, fileSystemForm);
         for (VFSResource destinationResource : pasteMap.values()) {
             if(!destinationResource.isNull()) {
                 return true;
             }
         }
         return false;
     }
     
     // really the method below should encorporate this as it duplicates lots of that behaviour.  I don't want to touch it however!
     private Map<VFSResource, VFSResource> buildPasteMap(HttpServletRequest request, HttpServletResponse response, String destinationPath, FileSystemForm fileSystemForm) 
                     throws Exception {
         Clipboard clipboard = (Clipboard) request.getSession().getAttribute(Constants.CLIPBOARD);
         Map<VFSResource, VFSResource> pasteDetails = new HashMap<VFSResource, VFSResource>();
         if (clipboard != null) {
             for (ClipboardContent content : clipboard.getContent()) {
                 NetworkPlaceClipboardContent element = (NetworkPlaceClipboardContent) content;
                 VFSResource sourceResource = getResourceForPath(fileSystemForm.getLaunchSession(), request, response, element.getPath());
                 if (log.isDebugEnabled()) {
                     log.debug("  Source. = " + sourceResource + " (display name = " + sourceResource.getDisplayName() + ", mount = " + sourceResource.isMount() + ")");
                 }
                 String concatenatePaths = DAVUtilities.concatenatePaths(destinationPath, sourceResource.getDisplayName());
                 VFSResource destinationResource = getResourceForPath(fileSystemForm.getLaunchSession(), request, response, concatenatePaths);
 
                 if (log.isDebugEnabled()) {
                     log.debug("  Dest. = " + destinationResource);
                 }
                 
                 if (sourceResource.getFullPath().equals(destinationResource.getFullPath())) {
                     throw new NetworkPlacesException(NetworkPlacesException.ERR_VFS_CANNOT_PASTE_TO_SOURCE);
                 }            
                 
                 pasteDetails.put(sourceResource, destinationResource);
             }
         }
         return pasteDetails;
     }
 
 	private void pasteFromClipboard(HttpServletRequest request, HttpServletResponse response, String destinationPath,
 									FileSystemForm fileSystemForm) throws Exception {
 	    Clipboard cb = (Clipboard) request.getSession().getAttribute(Constants.CLIPBOARD);
 	    ActionMessages msgs = getMessages(request);
 	    ActionMessages errs = getErrors(request);
         if (cb != null){
             try {
                 int fileCounter = 1;
                 int directorieCounter = 1;
                 String allFiles = "";
                 if (log.isDebugEnabled())
                     log.debug("Pasting from clipboard to " + destinationPath);
                 Iterator clipboardIterator = cb.getContent().iterator();
 
                 CoreEvent evt = NetworkPlaceResourceType.getResourceAccessPasteEvent(this,
                     fileSystemForm.getLaunchSession(),
                     request,
                     fileSystemForm.getPath(),
                     fileSystemForm.getFullURI(),
                     null);
 
                 while (clipboardIterator.hasNext()) {
                     String divider = allFiles.equals("") ? "" : ", ";
                     NetworkPlaceClipboardContent element = (NetworkPlaceClipboardContent) clipboardIterator.next();
                     try {
                         VFSResource sourceResource = getResourceForPath(fileSystemForm.getLaunchSession(),
                             request,
                             response,
                             element.getPath());
                         if (log.isDebugEnabled())
                             log.debug("  Source. = " + sourceResource
                                 + " (display name = "
                                 + sourceResource.getDisplayName()
                                 + ", mount = "
                                 + sourceResource.isMount()
                                 + ")");
                         VFSResource destinationResource = null;
                         if (log.isDebugEnabled())
                             log.debug("Paste");
                         destinationResource = getResourceForPath(fileSystemForm.getLaunchSession(),
                             request,
                             response,
                             DAVUtilities.concatenatePaths(destinationPath, sourceResource.getDisplayName()));
 
                         if (log.isDebugEnabled())
                             log.debug("  Dest. = " + destinationResource);                  
                         
                         if (sourceResource.getFullPath().equals(destinationResource.getFullPath())) {
                             throw new NetworkPlacesException(NetworkPlacesException.ERR_VFS_CANNOT_PASTE_TO_SOURCE);
                         }
                         
                         allFiles = allFiles + divider + sourceResource.getDisplayName();
                         sourceResource.copy(destinationResource, true, true);
                         
                         if (sourceResource.isCollection()) {
                             NetworkPlaceResourceType.addDirectoryAttribute(evt, sourceResource.getFullPath(), directorieCounter);
                             directorieCounter++;
                         } else {
                             NetworkPlaceResourceType.addDirectoryAttribute(evt, sourceResource.getFullPath(), fileCounter);
                             fileCounter++;
                         }
                         NetworkPlaceResourceType.addOperationType(evt, element.deleteOnPaste());
                         CoreServlet.getServlet().fireCoreEvent(evt);
 
                         if (element.deleteOnPaste()) {
                             if (log.isDebugEnabled())
                                 log.debug("      Deleting source");
                             sourceResource.delete();
                         }
                         msgs.add(Globals.MESSAGE_KEY, new ActionMessage("vfs.paste.from.clipboard", allFiles));
                     } catch (CoreException e) {
                         errs.add(Globals.ERROR_KEY, e.getBundleActionMessage());
                     }
                 }
                 // The void entries cannot be removed untill the iterator is
                 // finished with.
                 cb.clearClipboard();
 
                 this.saveErrors(request, errs);
                 this.saveMessages(request, msgs);
             } catch (Exception e) {
                 CoreEvent evt = NetworkPlaceResourceType.getResourceAccessPasteEvent(this,
                     fileSystemForm.getLaunchSession(),
                     request,
                     fileSystemForm.getPath(),
                     fileSystemForm.getFullURI(),
                     e);
                 CoreServlet.getServlet().fireCoreEvent(evt);
                 throw e;
             }
         }
         else{
            errs.add(Globals.ERROR_KEY, new ActionError("vfs.paste.from.clipboard.no.copy"));
             this.saveErrors(request, errs);
         }
 	}
 
 	/**
 	 * <p>
 	 * Make a new directory in the current location.
 	 * 
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward mkdir(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 		checkLaunchSession(request, response, fileSystemForm);
 		ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 		if (fwd != null) {
 			return fwd;
 		}
 		String path = DAVUtilities.stripTrailingSlash(fileSystemForm.getPath() + "/" + fileSystemForm.getNewFolder()) + "/";
 		VFSResource res = null;
 		res = getResourceForPath(fileSystemForm.getLaunchSession(), request, response, path);
 		fwd = checkMount(res, mapping, fileSystemForm, request);
 		if (fwd != null) {
 			return fwd;
 		}
 		try {
 			res.makeCollection();
 			fileSystemForm.setPath(path);
 			if (res.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessMkDirEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					null));
 			}
 
 		} catch (DAVException e) {
 			if (res.getMount().getStore().getProvider().isFireEvents()) {
 				CoreServlet.getServlet().fireCoreEvent(NetworkPlaceResourceType.getResourceAccessMkDirEvent(this,
 					fileSystemForm.getLaunchSession(),
 					request,
 					fileSystemForm.getPath(),
 					fileSystemForm.getFullURI(),
 					e));
 			}
 			if (e.getStatus() == 405) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.folder.exists", fileSystemForm.getNewFolder()));
 				saveErrors(request, msgs);
 				return mapping.findForward("list");
 			} else if (e.getStatus() == 507) {
 			        ActionMessages msgs = getErrors(request);
 			        msgs.add(Globals.ERROR_KEY, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.folder.not.allowed", fileSystemForm.getNewFolder()));
 			        saveErrors(request, msgs);
 			        return mapping.findForward("list");
 			    } else {
 				throw e;
 			}
 		}
 		return mapping.findForward("list");
 	}
 
 	/**
 	 * <p>
 	 * Action to forward to the make directory page.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward showMkDir(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 					throws Exception {
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 		checkLaunchSession(request, response, fileSystemForm);
 		ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 		if (fwd != null) {
 			return fwd;
 		}
 		return mapping.findForward("showMkDir");
 	}
 
 	/**
 	 * <p>
 	 * Action to forward to the rename file page.
 	 * 
 	 * @param mapping The
 	 *        <code>ActionMapping<code> associated with this dispatch action.
 	 * @param form The <code>FileSystemForm<code> for the action.
 	 * @param request The <code>HttpServletRequest<code> for the action.
 	 * @param response The <code>HttpServletResponse<code> for the action.
 	 * @return <code>ActionForward<code> The result of the action.
 	 * @throws Exception if aan exception is thrown.
 	 */
 	public ActionForward showRenameFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 										HttpServletResponse response) throws Exception {
 		FileSystemForm fileSystemForm = (FileSystemForm) form;
 
 		try {
 			checkLaunchSession(request, response, fileSystemForm);
 			ActionForward fwd = checkAccess(mapping, fileSystemForm, request);
 			if (fwd != null) {
 				return fwd;
 			}
 			fileSystemForm.setNewName(DAVUtilities.stripTrailingSlash(fileSystemForm.getFileName()));
 			return mapping.findForward("showRenameFile");
 		} catch (DAVAuthenticationRequiredException e) {
 			AbstractHTTPAuthenticationModule.sendAuthorizationError(request, response, e.getHttpRealm());
 			return null;
 		}
 	}
 
 	private ActionForward checkMount(VFSResource vfsResource, ActionMapping mapping, FileSystemForm fileSystemForm, HttpServletRequest request)
 					throws NoPermissionException {
 		if (vfsResource.getMount().isReadOnly()) {
 			if(READ_ONLY.contains(fileSystemForm.getActionTarget())) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.readOnly.error"));
 				saveMessages(request, msgs);
 				return mapping.getInputForward();
 			}
 		}
 		return null;		
 	}
 
 	private ActionForward checkAccess(ActionMapping mapping, FileSystemForm fileSystemForm, HttpServletRequest request)
 					throws NoPermissionException {
 		SessionInfo actualSession = LogonControllerFactory.getInstance().getSessionInfo(request);
 		SessionInfo session = fileSystemForm.getLaunchSession().getSession();
 
 		if (fileSystemForm.getNetworkPlace() != null) {
 			NetworkPlace resource = fileSystemForm.getNetworkPlace();
 
 			// check access for the attributes on the NetworkPlace.
 			if (resource.isNoDelete() && NO_DELETE.contains(fileSystemForm.getActionTarget())) {
 				ActionMessages msgs = getErrors(request);
 				msgs.add(Globals.ERROR_KEY, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.noDelete.error"));
 				saveMessages(request, msgs);
 				return mapping.getInputForward();
 			}
 		}
 
 		try {
 			if(fileSystemForm.getLaunchSession().isTracked()) {
 				LaunchSession.AccessRight accessRight = fileSystemForm.getLaunchSession().checkAccessRights(null, actualSession);
 				if (accessRight == LaunchSession.USER_ACCESS || isSuperUser(request)) {
 					fileSystemForm.setReadWrite();
 				} else if (accessRight == LaunchSession.MANAGEMENT_ACCESS) {
 					ActionMessages warnings = getWarnings(request);
 					warnings.add(Constants.REQ_ATTR_WARNINGS, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.manageOnly.warning"));
 					saveWarnings(request, warnings);
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			log.error("Failed to test if user has access to resource. Denying", e);
 			throw new NoPermissionException("Permission denied.", session.getUser(), NetworkPlacePlugin.NETWORK_PLACE_RESOURCE_TYPE);
 		}
 	}
 
     private boolean isSuperUser(HttpServletRequest request) {
         SessionInfo sessionInfo = getSessionInfo(request);
         return LogonControllerFactory.getInstance().isAdministrator(sessionInfo.getUser());
     }
     
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adito.core.actions.AuthenticatedDispatchAction#gotoLogon(org.apache.struts.action.ActionMapping,
 	 *      org.apache.struts.action.ActionForm,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	protected ActionForward gotoLogon(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 										HttpServletResponse response) throws Exception {
         ActionMessages warnings = getWarnings(request);
         warnings.add(Constants.REQ_ATTR_WARNINGS, new BundleActionMessage(NetworkPlacePlugin.MESSAGE_RESOURCES_KEY, "vfs.session.timeout"));
         saveWarnings(request, warnings);
 		return new RedirectWithMessages(mapping.findForward("logon"), request) ;
 	}
 }
