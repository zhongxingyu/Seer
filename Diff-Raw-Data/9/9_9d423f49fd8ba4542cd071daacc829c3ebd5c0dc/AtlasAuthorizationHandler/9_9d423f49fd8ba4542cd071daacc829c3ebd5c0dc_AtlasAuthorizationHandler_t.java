 package edu.uchicago.xrootd4j;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 
 import javax.security.auth.Subject;
 
 import nl.uva.vlet.glite.lfc.LFCConfig;
 import nl.uva.vlet.glite.lfc.LFCServer;
 import nl.uva.vlet.glite.lfc.internal.FileDesc;
 import nl.uva.vlet.glite.lfc.internal.ReplicaDesc;
 
 import org.dcache.xrootd.core.XrootdException;
 import org.dcache.xrootd.plugins.AuthorizationHandler;
 import org.dcache.xrootd.protocol.XrootdProtocol;
 import org.dcache.xrootd.protocol.XrootdProtocol.FilePerm;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 public class AtlasAuthorizationHandler implements AuthorizationHandler {
 
 	final static Logger log = LoggerFactory.getLogger(AtlasAuthorizationHandler.class);
 
	
 	private LFCConfig config = null;
 	private String LFC_HOST = "";
 	private String SRM_HOST = "";
 
 	private static RucioN2N rucio = null;
 
 	public AtlasAuthorizationHandler(RucioN2N rc, Properties properties) throws IllegalArgumentException, MissingResourceException {
 
 		rucio = rc;
 
 		LFC_HOST = properties.getProperty("lfc_host");
 		SRM_HOST = properties.getProperty("srm_host");
 
 		if (LFC_HOST == null) {
 			log.error("*** Error: LFC_HOST parameter not defined. Please set it (in etc/dcache.conf) and restart the server.");
 			throw new IllegalArgumentException("LFC_HOST parameter not defined. Please set it (in etc/dcache.conf) and restart the server.");
 		} else {
 			log.info("Setting LFC_HOST to: " + LFC_HOST);
 		}
 
 		if (SRM_HOST == null) {
 			log.error("*** Error: SRM_HOST parameter not defined. Please set it (in etc/dcache.conf)  and restart the server.");
 			throw new IllegalArgumentException("SRM_HOST parameter not defined. Please set it (in etc/dcache.conf) and restart the server.");
 		} else {
 			log.info("Setting SRM_HOST to: " + SRM_HOST);
 		}
 		try {
 			config = new LFCConfig();
 			log.info("trying to get proxy...");
 			config.globusCredential = getValidProxy();
 		} catch (Exception e) {
 			log.info("*** Can't get valid Proxy. We hope that your LFC_HOST allows for non-authenticated read-only access and you have gLite.");
 			log.debug(e.getMessage());
 			config = null;
 		}
 
 	}
 
 	public GlobusCredential getValidProxy() {
 		GlobusCredential cred = null;
 
 		log.info("Using default proxy file.");
 		try {
 			cred = GlobusCredential.getDefaultCredential();
 		} catch (GlobusCredentialException e) {
 			log.warn("*** Can't get default proxy. " + e.getMessage());
 			throw new MissingResourceException("*** Can't get default proxy.", "GlobusCredential", "");
 		}
 
 		if (cred == null) {
 			log.error("Couldn't find valid proxy");
 			throw new MissingResourceException("*** Error: no valid default proxy.", "GlobusCredential", "");
 		}
 
 		if (cred.getTimeLeft() <= 0) {
 			log.error("Expired Credential detected.");
 			throw new MissingResourceException("*** Error: Expired Credential detected.", "GlobusCredential", "");
 		}
 
 		log.info("proxy timeleft=" + cred.getTimeLeft());
 
 		return cred;
 	}
 
 	@Override
 	public String authorize(Subject subject, InetSocketAddress localAddress, InetSocketAddress remoteAddress, String path, Map<String, String> opaque,
 			int request, FilePerm mode) throws SecurityException, GeneralSecurityException, XrootdException {
 
 		log.info("GOT to translate: " + path);
 
 		if (path.startsWith("pnfs/") || path.startsWith("/pnfs/") ) {
 			return path;
 		}
 
 		String LFN = path;
 		if (!LFN.startsWith("/atlas/")) {
 			log.error("*** Error: LFN must start with /atlas/. ");
 			throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: LFN must start with /atlas/. ");
 		}
 
 		if (LFN.startsWith("/atlas/rucio/")) {
 			String pfn = rucio.translate(LFN);
 			if (pfn == null) {
 				log.info("rucio name not found.");
 				pfn = "";
 				throw new XrootdException(XrootdProtocol.kXR_NotFound, "rucio name not found.");
 			} else {
 				log.info("rucio translated name: " + pfn);
 				return pfn;
 			}
 		}
 
 		String sLFN = "lfn://grid" + LFN;
 		LFN = "lfn://" + LFC_HOST + "//grid" + LFN;
 
 		log.info("FINALY translating: " + LFN);
		
		URI lfcUri = null;
 
 		if (config != null) { // access through API
 
 			// if (config.globusCredential.getTimeLeft() <= 60)
 			// getValidProxy();
 
 			try {
 				lfcUri = new URI(LFN);
 				log.debug("Is a proper url.");
 			} catch (URISyntaxException e) {
 				log.error("*** Error: Invalid URI:" + LFN);
 				log.error(e.getMessage());
 				throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: Invalid URI:" + LFN + "\n" + e.getMessage());
 			}
 
 			LFCServer lfcServer;
 			try {
 				lfcServer = new LFCServer(config, lfcUri);
 				log.debug("LFC server created.");
 			} catch (Exception e) {
 				log.error("*** Could not connect to LFC. Giving up.");
 				log.error(e.getMessage());
 				throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Could not connect to LFC. Giving up." + e.getMessage());
 			}
 
 			String guid = "";
 			int gu = LFN.lastIndexOf("GUID=");
 
 			if (gu > 0) {
 				guid = LFN.substring(gu + 5);
 				if (guid.length() != 36) {
 					log.error("*** Error: GUID has to have 36 characters. 32 hex numbers and 4 minuses");
 					throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: GUID has to have 36 characters. 32 hex numbers and 4 minuses");
 				}
 			} else {
 				FileDesc entry = new FileDesc();
 				try {
 					entry = lfcServer.fetchFileDesc(lfcUri.getPath());
 				} catch (Exception e) {
 					log.error("*** Can't get file description for entry: " + lfcUri.getPath());
 					log.error("*** Can't get file description for entry: " + e.getMessage());
 				}
 
 				if (entry.getGuid() == null && LFN.contains("/user/")) {
 					log.info("maybe this is pathena registered file. Trying that...");
 					entry = ifInputIsPathenaRegistered(lfcUri.getPath(), lfcServer);
 				}
 
 				if (entry.getGuid() == null) {
 					log.info("maybe got container and not dataset. Trying that...");
 					entry = ifInputIsContainerDS(lfcUri.getPath(), lfcServer);
 				}
 
 				if (!entry.isFile()) {
 					log.error("*** Error: No such file or not a file.");
 					throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: No such file or not a file.");
 				}
 				guid = entry.getGuid();
 			}
 
 			log.debug("guid:" + guid);
 
 			try {
 				ArrayList<ReplicaDesc> replicas = lfcServer.getReplicas(guid);
 				if (replicas.isEmpty()) {
 					log.info("*** Error: No replica exists in this LFC.");
 					throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: No replica exists in this LFC.");
 				} else {
 					String PFN = "";
 					log.debug("found " + replicas.size() + " replicas.");
 					for (ReplicaDesc replica : replicas) {
 						String line = replica.getSfn();
 						if (replica.getHost().equals(SRM_HOST)) {
 							log.debug("replica found \n " + replica.toString());
 							int li = line.lastIndexOf("=") + 1;
 							if (li < 1) {
 								log.debug("could not find = sign. looking for /pnfs");
 								li = line.indexOf("/pnfs/");
 							}
 							PFN = line.substring(li);
 							log.info("PFN: " + PFN);
 							return PFN;
 						}
 					}
 					log.error("*** Error: No replica coresponding to this SRM_HOST exists in this LFC.");
 					throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: No replica coresponding to this SRM_HOST exists in this LFC.");
 				}
 			} catch (Exception e) {
 				log.error("*** Error: Can't get list of Replicas.");
 				log.error(e.getMessage());
 				throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: Can't get list of Replicas.\n" + e.getMessage());
 			}
 
 		}
 
 		else { // access through lcg-lr
 
 			log.info("Trying to use lcg-lr " + sLFN);
 			ProcessBuilder pb = new ProcessBuilder("lcg-lr", sLFN, "--connect-timeout", "10");
 			Map<String, String> env = pb.environment();
 			env.put("LFC_HOST", LFC_HOST);
 			try {
 				Process p = pb.start();
 				InputStream is = p.getInputStream();
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				String line;
 				String PFN;
 				while ((line = br.readLine()) != null) {
 					// log.error(line);
 					if (line.indexOf(SRM_HOST) == -1)
 						continue;
 					Integer ind = line.indexOf("=") + 1;
 					if (ind > 0) { // long form
 						PFN = line.substring(ind);
 						log.error("PFN: " + PFN);
 						return PFN;
 					}
 					ind = line.indexOf("/", 6);
 					if (ind > 0) { // short form
 						PFN = line.substring(ind);
 						log.error("PFN: " + PFN);
 						return PFN;
 					}
 					log.error("Could not interpret LFC name: " + line);
 					return line;
 				}
 				// log.error("lcg-lr returned.");
 
 			} catch (IOException e) {
 				log.error("IO Exception: " + e.getMessage());
 			}
 
 		}
 
 		throw new XrootdException(XrootdProtocol.kXR_NotFound, "*** Error: File not Found.");
 	}
 
 	public FileDesc ifInputIsContainerDS(String path, LFCServer lfcServer) {
 		FileDesc entry = new FileDesc();
 		log.debug("path -> " + path);
 		path = path.replaceAll("\\b//\\b", "/");
 		log.debug("stripped path -> " + path);
 
 		int li = path.lastIndexOf("/");
 		String fn = path.substring(li + 1);
 		log.debug("filename -> " + fn);
 
 		path = path.substring(0, li); // no filename
 		li = path.lastIndexOf("/");
 		String DSname = path.substring(li + 1);
 		log.debug("dsname -> " + DSname);
 		String dirname = path.substring(0, li);
 		log.debug("dir -> " + dirname);
 
 		try {
 			ArrayList<FileDesc> dirsdescs = lfcServer.listDirectory(dirname);
 			log.debug("got list of " + dirsdescs.size() + " data sets in this container.");
 			for (FileDesc dirdesc : dirsdescs) {
 				log.debug(dirdesc.getFileName());
 				if (dirdesc.getFileName().indexOf(DSname) != -1) {
 					String newFN = dirname + "/" + dirdesc.getFileName() + "/" + fn;
 					log.debug("found matching DS! trying: " + newFN);
 					try {
 						return lfcServer.fetchFileDesc(newFN);
 					} catch (Exception e) {
 						log.error("*** Not this one.");
 					}
 				}
 			}
 		} catch (Exception e1) {
 			log.info("***  There is no dataset container named: " + dirname);
 			return entry;
 		}
 		return entry;
 	}
 
 	public FileDesc ifInputIsPathenaRegistered(String path, LFCServer lfcServer) {
 		FileDesc entry = new FileDesc();
 		path = path.replaceAll("\\b//\\b", "/");
 		log.debug("stripped path -> " + path);
 
 		path = path.replace("user", "users/pathena");
 		log.debug("filename changed to pathena one -> " + path);
 
 		try {
			entry = lfcServer.fetchFileDesc(path);
 		} catch (Exception e1) {
 			log.info("*** It did not work. ");
 			return entry;
 		}
 		return entry;
 	}
 
 }
