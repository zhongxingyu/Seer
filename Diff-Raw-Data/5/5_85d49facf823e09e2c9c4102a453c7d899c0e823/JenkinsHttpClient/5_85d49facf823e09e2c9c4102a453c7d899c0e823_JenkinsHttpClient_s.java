 package com.buschmais.tinkerforge4jenkins.client;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.buschmais.tinkerforge4jenkins.core.BuildState;
 import com.buschmais.tinkerforge4jenkins.core.JobState;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.JenkinsConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.JobsType;
 
 /**
  * HTTP client for Jenkins servers.
  */
 public class JenkinsHttpClient {
 
 	/**
 	 * The logger.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(JenkinsHttpClient.class);
 
 	/**
 	 * Holds the names of Jenkins jobs to be monitored.
 	 */
 	private Set<String> jobFilter = null;
 
 	/**
 	 * The Jenkins configuration.
 	 */
 	private JenkinsConfigurationType configuration;
 
 	/**
	 * The Jackson {@link ObjectMapper}
 	 */
 	private ObjectMapper objectMapper = new ObjectMapper();
 
 	/**
 	 * The HTTP client.
 	 */
 	private HttpClient httpClient;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param configuration
 	 *            The {@link JenkinsConfigurationType}.
 	 */
 	public JenkinsHttpClient(JenkinsConfigurationType configuration,
 			HttpClient httpClient) {
 		this.configuration = configuration;
 		this.httpClient = httpClient;
 		JobsType jobs = configuration.getJobs();
 		if (jobs != null) {
 			jobFilter = new HashSet<String>();
 			for (String job : jobs.getJob()) {
 				jobFilter.add(job);
 			}
 		}
 	}
 
 	/**
 	 * Reads a JsonNode from the given URL using a HTTP request.
 	 * 
 	 * @param baseUrl
 	 *            The url.
 	 * @return The unmarshalled {@link JsonNode} or <code>null</code>.
 	 * @throws IOException
 	 *             If reading the URL fails.
 	 */
 	private JsonNode readJsonNode(String baseUrl) throws IOException {
 		HttpGet httpget = new HttpGet(baseUrl + "/api/json");
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 		String responseBody = httpClient.execute(httpget, responseHandler);
 		return objectMapper.reader().readTree(responseBody);
 	}
 
 	/**
 	 * Reads the current state of jobs represented by {@link JobState}s from the
 	 * given URL.
 	 * 
 	 * @return The list of {@link JobState}s.
 	 * @throws IOException
 	 *             If reading the states fails.
 	 */
 	public List<JobState> getJobStates() throws IOException {
 		List<JobState> result = new ArrayList<JobState>();
 		JsonNode node = this.readJsonNode(configuration.getUrl());
 		if (node == null) {
 			LOGGER.warn("Cannot read job states from url '{}'",
 					configuration.getUrl());
 		} else {
 			JsonNode jobsNode = node.get("jobs");
 			if (jobsNode != null) {
 				for (JsonNode jobNode : jobsNode) {
 					String jobName = jobNode.get("name").getTextValue();
 					if (jobFilter == null || jobFilter.contains(jobName)) {
 						String jobUrl = jobNode.get("url").getTextValue();
 						JsonNode lastBuildNode = this.readJsonNode(jobUrl
 								+ "lastBuild");
 						String buildState = lastBuildNode.get("result")
 								.getTextValue();
 						JobState state = new JobState();
 						state.setName(jobName);
 						if (buildState != null) {
 							state.setBuildState(BuildState.valueOf(buildState));
 						} else {
 							state.setBuildState(BuildState.UNKNOWN);
 						}
 						result.add(state);
 					}
 				}
 			}
 		}
 		return result;
 	}
 }
