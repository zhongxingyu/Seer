 package org.cloudsmith.jenkins.stackhammer.deployment;
 
 import hudson.Functions;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.cloudsmith.jenkins.stackhammer.common.StackOpResult;
 import org.cloudsmith.stackhammer.api.model.CatalogGraph;
 import org.cloudsmith.stackhammer.api.model.LogEntry;
 import org.cloudsmith.stackhammer.api.model.MessageWithSeverity;
 import org.cloudsmith.stackhammer.api.model.ResultWithDiagnostic;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.ExportedBean;
 
 @ExportedBean(defaultVisibility = 999)
 public class DeploymentResult extends StackOpResult<List<CatalogGraph>> {
 	public static class HostEntry {
 		private final CatalogGraph catalogGraph;
 
 		private final List<LogEntry> logEntries;
 
 		private final String name;
 
 		private final String machineName;
 
 		public HostEntry(String name, String machineName, CatalogGraph catalogGraph, List<LogEntry> logEntries) {
 			this.name = name;
 			this.machineName = machineName;
 			this.catalogGraph = catalogGraph;
 			this.logEntries = logEntries;
 		}
 
 		public CatalogGraph getCatalogGraph() {
 			return catalogGraph;
 		}
 
 		public String getCatalogGraphURL() {
 			return catalogGraph == null
 					? null
 					: "catalogGraph/" + getName();
 		}
 
 		public String getDisplayName() {
 			return "Host \"" + getName() + '"';
 		}
 
 		public List<LogEntry> getLogEntries() {
 			return logEntries == null
 					? Collections.<LogEntry> emptyList()
 					: logEntries;
 		}
 
 		public int getLogEntryCount() {
 			return logEntries == null
 					? 0
 					: logEntries.size();
 		}
 
 		public String getMachineName() {
 			return machineName;
 		}
 
 		public String getName() {
 			if(machineName == null)
 				return name;
 
 			return name + " [" + machineName + ']';
 		}
 	}
 
 	private static final long serialVersionUID = 6226198097084585053L;
 
 	private List<LogEntry> logEntries;
 
 	private transient Map<String, List<LogEntry>> logEntriesPerHost;
 
 	private transient Map<String, List<LogEntry>> logEntriesPerMachine;
 
 	private transient List<HostEntry> hostEntries;
 
 	public synchronized void addLogEntries(List<LogEntry> newLogEntries) {
 		if(logEntries == null)
 			logEntries = new ArrayList<LogEntry>(newLogEntries);
 		else
 			logEntries.addAll(newLogEntries);
 
 		// Invalidate cached entries if any
 		hostEntries = null;
 		logEntriesPerHost = null;
 		logEntriesPerMachine = null;
 	}
 
 	@Override
 	public DeploymentResult clone() {
 		DeploymentResult clone;
 		try {
 			clone = (DeploymentResult) super.clone();
 		}
 		catch(CloneNotSupportedException e) {
 			throw new RuntimeException("Error cloning BuildData", e);
 		}
 		return clone;
 	}
 
 	/**
 	 * Method called by the Stapler dispatcher. It is automatically detected
 	 * when the dispatcher looks for methods that starts with &quot;do&quot;
 	 * The method doValidation corresponds to the path <build>/stackhammerValidation/dependencyGraph
 	 * 
 	 * @param req
 	 * @param rsp
 	 * @throws IOException
 	 */
 	public void doCatalogGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
 		String name = req.getRestOfPath();
 		if(!(name == null || name.isEmpty())) {
 			name = name.substring(1);
 			for(HostEntry he : getHostEntries()) {
 				if(name.equals(he.getName())) {
 					CatalogGraph graph = he.getCatalogGraph();
 					if(graph != null) {
 						rsp.setContentType("image/svg+xml");
 						OutputStream out = rsp.getOutputStream();
 						try {
 							byte[] svgData = Base64.decodeBase64(graph.getCatalogGraph());
 							// svgData = GraphTrimmer.stripFixedSize(svgData);
 							rsp.setContentLength(svgData.length);
 							out.write(svgData);
 							return;
 						}
 						finally {
 							out.close();
 						}
 					}
 				}
 			}
 		}
 		rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
 	}
 
 	@Override
 	public String getDisplayName() {
 		return "Deployment Report";
 	}
 
 	public synchronized List<HostEntry> getHostEntries() {
 		if(hostEntries != null)
 			return hostEntries;
 
 		List<HostEntry> result = new ArrayList<HostEntry>();
 		List<CatalogGraph> graphs = getResult();
 		for(Map.Entry<String, List<LogEntry>> logEntries : getLogEntriesPerHost().entrySet()) {
 			String hostName = logEntries.getKey();
 			String machineName = null;
 			CatalogGraph catalogGraph = null;
 			if(graphs != null) {
 				for(CatalogGraph cg : graphs) {
 					if(hostName.equals(cg.getNodeName())) {
 						catalogGraph = cg;
 						machineName = cg.getInstanceID();
 						break;
 					}
 				}
 			}
 
 			List<LogEntry> les = logEntries.getValue();
 			if(machineName == null) {
 				for(LogEntry le : les) {
 					machineName = le.getPhysicalOrigin();
 					if(machineName != null)
 						break;
 				}
 			}
 			result.add(new HostEntry(hostName, machineName, catalogGraph, les));
 		}
 
 		// In the unlikely event that we have a catalog graph for which no log
 		// has been produced
 		if(graphs != null) {
 			for(CatalogGraph cg : graphs) {
 				if(getLogEntriesPerHost().containsKey(cg.getNodeName()))
 					continue;
 				result.add(new HostEntry(cg.getNodeName(), cg.getInstanceID(), cg, Collections.<LogEntry> emptyList()));
 			}
 		}
 
 		for(Map.Entry<String, List<LogEntry>> logEntries : getLogEntriesPerMachine().entrySet())
 			result.add(new HostEntry("unknown", logEntries.getKey(), null, logEntries.getValue()));
 
 		hostEntries = result;
 		return result;
 	}
 
 	public int getHostEntryCount() {
 		return getHostEntries().size();
 	}
 
 	@Override
 	public String getIconFileName() {
 		return Functions.getResourcePath() + "/plugin/stackhammer/icons/hammer-32x32.png";
 	}
 
 	@Override
 	public String getLargeIconFileName() {
 		return "/plugin/stackhammer/icons/hammer-48x48.png";
 	}
 
 	public List<LogEntry> getLogEntries() {
 		return logEntries == null
 				? Collections.<LogEntry> emptyList()
 				: logEntries;
 	}
 
 	private synchronized Map<String, List<LogEntry>> getLogEntriesPerHost() {
 		if(logEntriesPerHost != null)
 			return logEntriesPerHost;
 
 		for(LogEntry le : getLogEntries()) {
 			String hostName = le.getLogicalOrigin();
 			if(hostName == null)
 				continue;
 
 			if(logEntriesPerHost == null)
 				logEntriesPerHost = new HashMap<String, List<LogEntry>>();
 			List<LogEntry> hostEntries = logEntriesPerHost.get(hostName);
 			if(hostEntries == null) {
 				hostEntries = new ArrayList<LogEntry>();
 				logEntriesPerHost.put(hostName, hostEntries);
 			}
 			hostEntries.add(le);
 		}
 		if(logEntriesPerHost == null)
 			logEntriesPerHost = Collections.emptyMap();
 		return logEntriesPerHost;
 	}
 
 	/**
 	 * Returns the log entries that origins from machines that are no longer
 	 * attached to a logical host.
 	 */
 	private synchronized Map<String, List<LogEntry>> getLogEntriesPerMachine() {
 		if(logEntriesPerMachine != null)
 			return logEntriesPerMachine;
 
 		for(LogEntry le : getLogEntries()) {
 			if(le.getLogicalOrigin() != null)
 				continue;
 
 			String machineName = le.getPhysicalOrigin();
 			if(machineName == null)
 				continue;
 
 			if(logEntriesPerMachine == null)
 				logEntriesPerMachine = new HashMap<String, List<LogEntry>>();
 
 			List<LogEntry> machineEntries = logEntriesPerMachine.get(machineName);
 			if(machineEntries == null) {
 				machineEntries = new ArrayList<LogEntry>();
 				logEntriesPerMachine.put(machineName, machineEntries);
 			}
 			machineEntries.add(le);
 		}
 		if(logEntriesPerMachine == null)
 			logEntriesPerMachine = Collections.emptyMap();
 		return logEntriesPerMachine;
 	}
 
 	public String getSummary() {
 		int hostsWithErrors = 0;
 		int hostsWithWarnings = 0;
 		List<HostEntry> hosts = getHostEntries();
 		int hostCount = hosts.size();
 		if(hostCount == 0)
 			return "No hosts deployed";
 
 		for(int idx = 0; idx < hostCount; ++idx) {
 			boolean hasErrors = false;
 			boolean hasWarnings = false;
 			for(LogEntry le : hosts.get(idx).getLogEntries()) {
 				switch(le.getSeverity()) {
 					case MessageWithSeverity.FATAL:
 					case MessageWithSeverity.ERROR:
 						hasErrors = true;
 						break;
 					case MessageWithSeverity.WARNING:
 						hasWarnings = true;
 				}
 				if(hasErrors && hasWarnings)
 					break;
 			}
 			if(hasErrors)
 				hostsWithErrors++;
 			if(hasWarnings)
 				hostsWithWarnings++;
 		}
 
 		StringBuilder bld = new StringBuilder();
 		bld.append(hostCount);
		bld.append("host");
 		if(hostCount > 1)
 			bld.append('s');
 		bld.append(" deployed");
 		if(hostsWithErrors > 0 || hostsWithWarnings > 0) {
			bld.append('(');
 			if(hostsWithErrors > 0) {
 				bld.append(hostsWithErrors);
 				bld.append(" with errors");
 				if(hostsWithWarnings > 0)
 					bld.append(", ");
 			}
 			if(hostsWithWarnings > 0) {
 				bld.append(hostsWithWarnings);
 				bld.append(" with warnings");
 			}
 			bld.append(')');
 		}
 		return bld.toString();
 	}
 
 	@Override
 	public String getUrlName() {
 		return "stackhammerDeployment";
 	}
 
 	/**
 	 * @param validationDiagnostic the validationDiagnostic to set
 	 */
 	@Override
 	public void setResult(ResultWithDiagnostic<List<CatalogGraph>> resultDiagnostic) {
 		super.setResult(resultDiagnostic);
 		hostEntries = null;
 	}
 }
