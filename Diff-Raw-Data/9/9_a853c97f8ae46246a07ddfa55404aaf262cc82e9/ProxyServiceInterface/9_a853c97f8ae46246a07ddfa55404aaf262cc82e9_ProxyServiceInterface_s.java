 package org.vpac.grisu.client.control;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.activation.DataHandler;
 
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.model.MountPoint;
 import org.vpac.grisu.model.dto.DtoApplicationDetails;
 import org.vpac.grisu.model.dto.DtoApplicationInfo;
 import org.vpac.grisu.model.dto.DtoDataLocations;
 import org.vpac.grisu.model.dto.DtoFolder;
 import org.vpac.grisu.model.dto.DtoGridResources;
 import org.vpac.grisu.model.dto.DtoHostsInfo;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.model.dto.DtoJobs;
 import org.vpac.grisu.model.dto.DtoMountPoints;
 import org.vpac.grisu.model.dto.DtoSubmissionLocations;
 
 public class ProxyServiceInterface implements ServiceInterface {
 	
 	private final Object si;
 	
 	public ProxyServiceInterface(Object si) {
 		this.si = si;
 	}
 
 	public void addJobProperties(String jobname, DtoJob properties)
 			throws NoSuchJobException {
 		try {
 			Method m = si.getClass().getMethod("addJobProperties", jobname.getClass(), properties.getClass());
 			m.invoke(si, jobname, properties);
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 		
 	}
 
 	public void addJobProperty(String jobname, String key, String value)
 			throws NoSuchJobException {
 
 		try {
 			Method m = si.getClass().getMethod("addJobProperty", key.getClass(), value.getClass() );
 			m.invoke(si, key, value);
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String cp(String source, String target, boolean overwrite,
 			boolean waitForFileTransferToFinish)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("cp", source.getClass(), target.getClass(), boolean.class, boolean.class);
			return (String)(m.invoke(si, source, target));
 			
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String createJobUsingJsdl(String jsdl, String fqan,
 			String jobnameCreationMethod) throws JobPropertiesException {
 				try {
 			Method m = si.getClass().getMethod("createJobUsingJsdl", jsdl.getClass(), fqan.getClass(), jobnameCreationMethod.getClass());
 			return (String)(m.invoke(si, jsdl, fqan, jobnameCreationMethod));
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof JobPropertiesException ) {
 					throw (JobPropertiesException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String createJobUsingMap(DtoJob job, String fqan,
 			String jobnameCreationMethod) throws JobPropertiesException {
 				try {
 			Method m = si.getClass().getMethod("createJobUsingMap", job.getClass(), fqan.getClass(), jobnameCreationMethod.getClass());
 			return (String)(m.invoke(si, job, fqan, jobnameCreationMethod));
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof JobPropertiesException ) {
 					throw (JobPropertiesException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public void deleteFile(String file) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("deleteFile", file.getClass());
 			m.invoke(si, file);
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 		
 	}
 
 	public void deleteFiles(String[] files) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("deleteFiles", files.getClass());
 			m.invoke(si, (Object)files);
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 		
 	}
 
 	public DtoMountPoints df() {
 				try {
 			Method m = si.getClass().getMethod("df");
 			return (DtoMountPoints)(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DataHandler download(String filename)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("download", filename.getClass());
 			return (DataHandler)(m.invoke(si, filename));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public boolean fileExists(String file) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("fileExists", file.getClass());
 			return (Boolean)(m.invoke(si, file));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingJsdl(
 			String jsdl, String fqan) {
 				try {
 			Method m = si.getClass().getMethod("findMatchingSubmissionLocationsUsingJsdl", jsdl.getClass(), fqan.getClass());
 			return (DtoGridResources)(m.invoke(si, jsdl, fqan));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoGridResources findMatchingSubmissionLocationsUsingMap(
 			DtoJob jobProperties, String fqan) {
 				try {
 			Method m = si.getClass().getMethod("findMatchingSubmissionLocationsUsingMap", jobProperties.getClass(), fqan.getClass());
 			return (DtoGridResources)(m.invoke(si, jobProperties, fqan));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String[] getAllAvailableApplications(String[] sites) {
 				try {
 			Method m = si.getClass().getMethod("getAllAvailableApplications", sites.getClass());
 			return (String[])(m.invoke(si, (Object)sites));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoHostsInfo getAllHosts() {
 				try {
 			Method m = si.getClass().getMethod("getAllHosts");
 			return (DtoHostsInfo)(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoJob getAllJobProperties(String jobname) throws NoSuchJobException {
 				try {
 			Method m = si.getClass().getMethod("getAllJobProperties", jobname.getClass());
 			return (DtoJob)(m.invoke(si, jobname));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String[] getAllJobnames() {
 				try {
 			Method m = si.getClass().getMethod("getAllJobnames");
 			return (String[])(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String[] getAllSites() {
 				try {
 			Method m = si.getClass().getMethod("getAllSites");
 			return (String[])(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoSubmissionLocations getAllSubmissionLocations() {
 				try {
 			Method m = si.getClass().getMethod("getAllSubmissionLocations");
 			return (DtoSubmissionLocations)(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoSubmissionLocations getAllSubmissionLocationsForFqan(String fqan) {
 				try {
 			Method m = si.getClass().getMethod("getAllSubmissionLocationsForFqan", fqan.getClass());
 			return (DtoSubmissionLocations)(m.invoke(si, fqan));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoApplicationDetails getApplicationDetailsForSite(
 			String application, String siteOrSubmissionLocation) {
 				try {
 			Method m = si.getClass().getMethod("getApplicationDetailsForSite", application.getClass(), siteOrSubmissionLocation.getClass());
 			return (DtoApplicationDetails) m.invoke(si, application, siteOrSubmissionLocation);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoApplicationDetails getApplicationDetailsForVersionAndSite(
 			String application, String version, String site) {
 				try {
 			Method m = si.getClass().getMethod("getApplicationDetailsForVersionAndSite", application.getClass(), version.getClass(), site.getClass());
 			return (DtoApplicationDetails) m.invoke(si, application, version, site);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String[] getChildrenFileNames(String folder, boolean onlyFiles)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("getChildrenFileNames", folder.getClass(), boolean.class);
 			return (String[]) m.invoke(si, folder, onlyFiles);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public long getCredentialEndTime() {
 				try {
 			Method m = si.getClass().getMethod("getCredentialEndTime");
 			return (Long) m.invoke(si);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getCurrentStatusMessage(String handle) {
 				try {
 			Method m = si.getClass().getMethod("getCurrentStatusMessage", handle.getClass());
 			return (String) m.invoke(si, handle);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getDN() {
 				try {
 			Method m = si.getClass().getMethod("getDN");
 			return (String) m.invoke(si);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoDataLocations getDataLocationsForVO(String fqan) {
 				try {
 			Method m = si.getClass().getMethod("getDataLocationsForVO", fqan.getClass());
 			return (DtoDataLocations) m.invoke(si, fqan);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public long getFileSize(String file) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("getFileSize", file.getClass());
 			return (Long) m.invoke(si, file);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String[] getFqans() {
 		
 		try {
 			Method m = si.getClass().getMethod("getFqans");
 			return (String[])(m.invoke(si));
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getInterfaceVersion() {
 				try {
 			Method m = si.getClass().getMethod("getInterfaceVersion");
 			return (String) m.invoke(si);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getJobProperty(String jobname, String key)
 			throws NoSuchJobException {
 				try {
 			Method m = si.getClass().getMethod("getJobProperty", jobname.getClass(), key.getClass());
 			return (String) m.invoke(si, jobname, key);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public int getJobStatus(String jobname) {
 				try {
 			Method m = si.getClass().getMethod("getJobStatus", jobname.getClass());
 			return (Integer) m.invoke(si, jobname);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getJsdlDocument(String jobname) throws NoSuchJobException {
 				try {
 			Method m = si.getClass().getMethod("getJsdlDocument", jobname.getClass());
 			return (String) m.invoke(si, jobname);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public MountPoint getMountPointForUri(String uri) {
 				try {
 			Method m = si.getClass().getMethod("getMountPointForUri", uri.getClass());
 			return (MountPoint) m.invoke(si, uri);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getSite(String host) {
 				try {
 			Method m = si.getClass().getMethod("getSite", host.getClass());
 			return (String) m.invoke(si, host);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String[] getStagingFileSystemForSubmissionLocation(String subLoc) {
 				try {
 			Method m = si.getClass().getMethod("getStagingFileSystemForSubmissionLocation", subLoc.getClass());
 			return (String[]) m.invoke(si, subLoc);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplication(
 			String application) {
 				try {
 			Method m = si.getClass().getMethod("getSubmissionLocationsForApplication", application.getClass());
 			return (DtoSubmissionLocations) m.invoke(si, application);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(
 			String application, String version) {
 				try {
 			Method m = si.getClass().getMethod("getSubmissionLocationsForApplicationAndVersion", application.getClass(), version.getClass());
 			return (DtoSubmissionLocations) m.invoke(si, application, version);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(
 			String application, String version, String fqan) {
 				try {
 			Method m = si.getClass().getMethod("getSubmissionLocationsForApplicationAndVersionAndFqan", application.getClass(), version.getClass(), fqan.getClass());
 			return (DtoSubmissionLocations) m.invoke(si, application, version, fqan);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
 			String application) {
 				try {
 			Method m = si.getClass().getMethod("getSubmissionLocationsPerVersionOfApplication", application.getClass());
 			return (DtoApplicationInfo) m.invoke(si, application);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String getTemplate(String application)
 			throws NoSuchTemplateException {
 				try {
 			Method m = si.getClass().getMethod("getTemplate", application.getClass());
 			return (String) m.invoke(si, application);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof NoSuchTemplateException ) {
 					throw (NoSuchTemplateException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String getUserProperty(String key) {
 				try {
 			Method m = si.getClass().getMethod("getUserProperty", key.getClass());
 			return (String) m.invoke(si, key);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public String[] getVersionsOfApplicationOnSubmissionLocation(
 			String application, String submissionLocation) {
 				try {
 			Method m = si.getClass().getMethod("getVersionsOfApplicationOnSubmissionLocation", application.getClass(), submissionLocation.getClass());
 			return (String[]) m.invoke(si, application, submissionLocation);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public boolean isFolder(String file) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("isFolder", file.getClass());
 			return (Boolean) m.invoke(si, file);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public void kill(String jobname, boolean clean)
 			throws RemoteFileSystemException, NoSuchJobException {
 
 			Method m;
 			try {
 				m = si.getClass().getMethod("kill", boolean.class);
 				m.invoke(si, clean);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else if ( e.getCause() instanceof NoSuchJobException ) {
 					throw (NoSuchJobException) e.getCause();
 				} else {
 					throw new RuntimeException("Proxy method exception.", e.getCause());
 				}
 			}
 		
 	}
 
 	public long lastModified(String remoteFile)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("lastModified", remoteFile.getClass());
 			return (Long) m.invoke(si, remoteFile);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public String[] listHostedApplicationTemplates() {
 				try {
 			Method m = si.getClass().getMethod("listHostedApplicationTemplates");
 			return (String[]) m.invoke(si);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public void login(String username, String password) {
 				try {
 			Method m = si.getClass().getMethod("login", username.getClass(), password.getClass());
 			m.invoke(si, username, password);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 		
 	}
 
 	public String logout() {
 				try {
 			Method m = si.getClass().getMethod("logout");
 			return (String) m.invoke(si);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public DtoFolder ls(String directory, int recursionLevel)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("ls", directory.getClass(), int.class);
 			return (DtoFolder) m.invoke(si, directory, recursionLevel);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public boolean mkdir(String folder) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("mkdir", folder.getClass());
 			return (Boolean) m.invoke(si, folder);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public MountPoint mount(String url, String mountpoint, String fqan,
 			boolean useHomeDirectoryOnThisFileSystemIfPossible)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("mount", url.getClass(), mountpoint.getClass(), fqan.getClass(), boolean.class);
 			return (MountPoint) m.invoke(si, url, mountpoint, fqan, useHomeDirectoryOnThisFileSystemIfPossible);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public MountPoint mountWithoutFqan(String url, String mountpoint,
 			boolean useHomeDirectoryOnThisFileSystemIfPossible)
 			throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("mountWithoutFqan", url.getClass(), mountpoint.getClass(), boolean.class);
 			return (MountPoint) m.invoke(si, url, mountpoint, useHomeDirectoryOnThisFileSystemIfPossible);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 	public DtoJobs ps(boolean refreshJobStatus) {
 				try {
 			Method m = si.getClass().getMethod("ps", boolean.class);
 			return (DtoJobs) m.invoke(si, refreshJobStatus);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 	}
 
 	public void submitJob(String jobname) throws JobSubmissionException {
 				try {
 			Method m = si.getClass().getMethod("submitJob", jobname.getClass());
 			m.invoke(si, jobname);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof JobSubmissionException ) {
 					throw (JobSubmissionException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 		
 	}
 
 	public void submitSupportRequest(String subject, String description) {
 				try {
 			Method m = si.getClass().getMethod("submitSupportRequest", subject.getClass(), description.getClass());
 			m.invoke(si, subject, description);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 		
 	}
 
 	public void umount(String mountpoint) {
 				try {
 			Method m = si.getClass().getMethod("umount", mountpoint.getClass());
 			m.invoke(si, mountpoint);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				throw (RuntimeException) e.getCause();
 			}
 		
 	}
 
 	public String upload(DataHandler file, String filename,
 			boolean returnAbsoluteUrl) throws RemoteFileSystemException {
 				try {
 			Method m = si.getClass().getMethod("upload", file.getClass(), filename.getClass(), boolean.class);
 			return (String) m.invoke(si, file, filename, returnAbsoluteUrl);
 			} catch (SecurityException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Proxy method exception.", e);
 			} catch (InvocationTargetException e) {
 				if ( e.getCause() instanceof RemoteFileSystemException ) {
 					throw (RemoteFileSystemException) e.getCause();
 				} else {
 					throw (RuntimeException) e.getCause();
 				}
 			}
 	}
 
 }
