 package org.vpac.grisu.control.info;
 
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.interfaces.GridResource;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.SubmissionLocationHelpers;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bestgrid.mds.SQLQueryClient;
 
 /**
  * 
  * @author Yuriy Halytskyy A replacement for CachedMdsInformationManager that
  *         uses SQL backend
  */
 public class SqlMDSInformationManager implements InformationManager {
 
 	private final Map<String, String> configuration;
 	private final SQLQueryClient client;
 
 	public SqlMDSInformationManager(Map<String, String> configuration) {
 		this.configuration = configuration;
 		String databaseUrl = configuration.get("databaseUrl");
 		String databaseUser = configuration.get("user");
 		String databasePassword = configuration.get("password");
 
 		client = new SQLQueryClient(databaseUrl, databaseUser, databasePassword);
 	}
 
 	public String[] getAllApplicationsAtSite(String site) {
 		return client.getCodesAtSite(site);
 	}
 
 	public String[] getAllApplicationsOnGrid() {
 		return client.getCodesOnGrid();
 	}
 
 	public String[] getAllApplicationsOnGridForVO(String fqan) {
 		return client.getCodesOnGridForVO(fqan);
 	}
 
 	public Map<String, GridResource> getAllGridResources() {
 		GridResource[] resources = client.getAllGridResources();
 		HashMap<String, GridResource> resourceMap = new HashMap<String, GridResource>();
 		for (GridResource resource : resources) {
 
 			String subLoc = SubmissionLocationHelpers
 			.createSubmissionLocationString(resource);
 			resourceMap.put(subLoc, resource);
 		}
 		return resourceMap;
 
 	}
 
 	public Map<String, String> getAllHosts() {
 		Map<String, String> computes = client.getAllComputeHosts();
 		Map<String, String> dataHosts = client.getAllDataHosts();
 		computes.putAll(dataHosts);
 		return computes;
 	}
 
 	public String[] getAllSites() {
 		return client.getSitesOnGrid();
 	}
 
 	public String[] getAllSubmissionLocations() {
 		String[] sites = client.getSitesOnGrid();
 		List<String> submissionLocations = new LinkedList<String>();
 		for (String site : sites) {
 			String[] contactStrings = getContactStrings(site, null);
 			for (String cs : contactStrings) {
 				submissionLocations.add(cs);
 			}
 
 		}
 		return submissionLocations.toArray(new String[] {});
 	}
 
 	public String[] getAllSubmissionLocations(String application, String version) {
 		String[] sites = client.getSitesWithAVersionOfACode(application,
 				version);
 		return getContactStringsForSitesWithApplication(sites, application,
 				version);
 	}
 
 	public String[] getAllSubmissionLocationsForApplication(String application) {
 		String[] sites = client.getSitesWithCode(application);
 		return getContactStringsForSitesWithApplication(sites, application,
 				null);
 	}
 
 	// this does not make any sense... the method asks for locations, but we
 	// return applications
 	// but I am just implementing what is in cached manager for now.
 	public String[] getAllSubmissionLocationsForSite(String site) {
 		return client.getCodesAtSite(site);
 	}
 
 	public String[] getAllSubmissionLocationsForVO(String fqan) {
 		if (Constants.NON_VO_FQAN.equals(fqan)) {
 			return getAllSubmissionLocations();
 		}
 
 		String[] sites = client.getSitesForVO(fqan);
 		Set<String> locations = new HashSet<String>();
 		for (String site: sites){
 			for (String queue: client.getQueueNamesAtSite(site, fqan)){
				for (String location: client.getContactStringOfQueueAtSite(site, queue)){
					locations.add(location);
 				}
 			}
 		}
 		return locations.toArray(new String[] {});
 	}
 
 	public String[] getAllVersionsOfApplicationOnGrid(String application) {
 		return client.getVersionsOfCodeOnGrid(application);
 	}
 
 	public String[] getAllVersionsOfApplicationOnGridForVO(String application,
 			String vo) {
 		return client.getVersionsOfCodeOnGridForVO(application, vo);
 	}
 
 	public Map<String, String> getApplicationDetails(String application,
 			String version, String subLoc) {
 		Map<String, String> codeDetails = new HashMap<String, String>();
 
 		codeDetails.put(Constants.MDS_MODULES_KEY, client
 				.getModuleNameOfCodeForSubmissionLocation(subLoc, application,
 						version));
 		codeDetails.put(Constants.MDS_SERIAL_AVAIL_KEY, Boolean.toString(client
 				.isSerialAvailForCodeForSubmissionLocation(subLoc, application,
 						version)));
 		codeDetails.put(Constants.MDS_PARALLEL_AVAIL_KEY, Boolean
 				.toString(client.isParallelAvailForCodeForSubmissionLocation(
 						subLoc, application, version)));
 		String[] executables = client.getExeNameOfCodeForSubmissionLocation(
 				subLoc, application, version);
 		StringBuffer exeStrBuff = new StringBuffer();
 		for (int i = 0; i < executables.length; i++) {
 			exeStrBuff.append(executables[i]);
 			if (i < executables.length - 1) {
 				exeStrBuff.append(",");
 			}
 		}
 
 		codeDetails.put(Constants.MDS_EXECUTABLES_KEY, exeStrBuff.toString());
 		return codeDetails;
 	}
 
 	public String[] getApplicationsThatProvideExecutable(String executable) {
 		return client.getApplicationNamesThatProvideExecutable(executable);
 	}
 
 	private String getContactString(String queue, String hostname,
 			String jobManager) {
 
 		if (jobManager != null) {
 			if (jobManager.toLowerCase().indexOf("pbs") < 0) {
 				return queue + ":" + getHostname(hostname) + "#" + jobManager;
 			} else {
 				return queue + ":" + getHostname(hostname);
 			}
 		}
 		return null;
 	}
 
 	private String[] getContactStrings(String site, String fqan) {
 		List<String> result = new LinkedList<String>();
 		String[] queues;
 		if (fqan != null) {
 			queues = client.getQueueNamesAtSite(site, fqan);
 		} else {
 			queues = client.getQueueNamesAtSite(site);
 		}
 		for (String queue : queues) {
 			String[] contacts = client.getContactStringOfQueueAtSite(site,
 					queue);
 			String jobManager = client.getJobManagerOfQueueAtSite(site, queue);
 			for (String contact : contacts) {
 
 				String cs = getContactString(queue, contact, jobManager);
 				if (cs != null) {
 					result.add(cs);
 				}
 			}
 		}
 		return result.toArray(new String[] {});
 	}
 
 	private String[] getContactStringsForSitesWithApplication(String[] sites,
 			String application, String version) {
 		List<String> result = new LinkedList<String>();
 		for (String site : sites) {
 			String[] queues;
 
 			if (version != null) {
 				queues = client.getQueueNamesForCodeAtSite(site, application,
 						version);
 			} else if (application != null) {
 				queues = client.getQueueNamesForCodeAtSite(site, application);
 			} else {
 				queues = client.getQueueNamesAtSite(site);
 			}
 
 			for (String queue : queues) {
 				String[] contacts = client.getContactStringOfQueueAtSite(site,
 						queue);
 				String jobManager = client.getJobManagerOfQueueAtSite(site,
 						queue);
 				for (String contact : contacts) {
 
 					String cs = getContactString(queue, contact, jobManager);
 					if (cs != null) {
 						result.add(cs);
 					}
 				}
 			}
 		}
 		return result.toArray(new String[] {});
 	}
 
 	public Map<String, String[]> getDataLocationsForVO(String fqan) {
 		return client.calculateDataLocationsForVO(fqan);
 	}
 
 	public GridResource getGridResource(String subLoc) {
 		Map<String, GridResource> grs = getAllGridResources();
 		// System.out.println(grs.get(subLoc));
 		return grs.get(subLoc);
 	}
 
 	private String getHostname(String contactString) {
 		String hostname = contactString.substring(
 				contactString.indexOf("https://") != 0 ? 0 : 8,
 						contactString.indexOf(":8443"));
 		return hostname;
 	}
 
 	public String getJobmanagerOfQueueAtSite(String site, String queue) {
 		return client.getJobManagerOfQueueAtSite(site, queue);
 	}
 
 	public String getSiteForHostOrUrl(String host_or_url) {
 		String temp = null;
 		try {
 
 			URL url = new URL(host_or_url.replace("gsiftp", "http"));
 			temp = url.getHost();
 		} catch (Exception e) {
 			temp = host_or_url;
 		}
 		return getAllHosts().get(temp);
 	}
 
 	public String[] getStagingFileSystemForSubmissionLocation(String subLoc) {
 		// subLoc = queuename@cluster:contactstring#JobManager
 		int queSepIndex = subLoc.indexOf(":");
 		if (queSepIndex < 1) {
 			throw new RuntimeException(
 					"Wrong submission location format. Queue missing in subLoc: "
 					+ subLoc);
 		}
 		String queueName = subLoc.substring(0, queSepIndex);
 		String contactString = "";
 		if (subLoc.indexOf("#") > 0) {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1,
 					subLoc.indexOf("#"));
 		} else {
 			contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 		}
 
 		// get site name for contact string
 		// GridInfoInterface client = new QueryClient();
 		String siteName = client.getSiteForHost(contactString);
 
 		String[] result = client.getGridFTPServersForQueueAtSite(siteName,
 				queueName);
 		return result;
 	}
 
 	public String[] getVersionsOfApplicationOnSite(String application,
 			String site) {
 		return client.getVersionsOfCodeAtSite(site, application);
 	}
 
 	public String[] getVersionsOfApplicationOnSubmissionLocation(
 			String application, String submissionLocation) {
 
 		String queue = SubmissionLocationHelpers
 		.extractQueue(submissionLocation);
 		String host = SubmissionLocationHelpers.extractHost(submissionLocation);
 
 		String[] temp = client.getVersionsOfCodeForQueueAndContactString(queue,
 				host, application);
 		return temp;
 	}
 
 	public boolean isVolatileDataLocation(String hostname, String endpoint , String fqan) {
 		return client.isVolatile(hostname, endpoint , fqan);
 	}
 
 }
