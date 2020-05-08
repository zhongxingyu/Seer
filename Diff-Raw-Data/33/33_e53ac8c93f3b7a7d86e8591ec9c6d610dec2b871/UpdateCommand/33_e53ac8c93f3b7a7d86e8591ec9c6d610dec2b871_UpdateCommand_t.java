 package com.meltmedia.cadmium.cli;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.meltmedia.cadmium.status.Status;
 
 @Parameters(commandDescription = "Instructs a site to update its content.", separators="=")
 public class UpdateCommand extends AbstractAuthorizedOnly implements CliCommand {
 
 	private final Logger log = LoggerFactory.getLogger(getClass());
 
	@Parameter(names={"--branch", "-b"}, description="The branch that you are updating to", required=false)
 	private String branch;
 	
	@Parameter(names={"--tag", "-t"}, description="The tag that you are updating to - should not be used with a branch or with a revision", required=false)
 	private String tag;
 
 	@Parameter(names={"--revision", "-r"}, description="The revision that you are updating to", required=false)
 	private String revision;
 
 	@Parameter(description="<site>", required=true)
 	private List<String> site;
 
 
 	@Parameter(names={"--message", "-m"}, description="The comment for the history", required=true)
 	private String message;
 
 	@Parameter(names={"--force", "-f"}, description="Force the update", required=false)
 	private boolean force;
 
 	public static final String JERSEY_ENDPOINT = "/system/update";
 
 	public void execute() throws ClientProtocolException, IOException {
 
 		DefaultHttpClient client = new DefaultHttpClient();
 
 		String siteUrl = site.get(0);
 		String url = siteUrl + JERSEY_ENDPOINT;	
 
 		log.debug("site + JERSEY_ENDPOINT = {}", url);
 
 
 		System.out.println("Getting status of ["+ siteUrl +"]");
 		try {
 
 			Status siteStatus = CloneCommand.getSiteStatus(siteUrl, token);
 
 			boolean branchSame = false;
 			boolean revisionSame = false;
 			boolean forceUpdate = force;
 			boolean isTagAlone = false;
 			
 			//if tag is NOT null and either the branch or the revision are NOT null, error out, else continue
 			if(tag != null && branch == null && revision == null) {
 				
 				log.debug("Tag was inputted by itself.");
 				isTagAlone = true;
 			}
 			else if(tag != null) {	
 				
 				System.err.println("Tag was inputted with either a branch or a revision.");
 				System.err.println("Please input the tag without a branch and revision.");
 				System.exit(1);
 			}
 			
 			String currentRevision = siteStatus.getRevision();
 			String currentBranch = siteStatus.getBranch();
 			
 
 			log.debug("branch = {}, and currentBranch = {}", branch, currentBranch);
 			
 			if(branch != null && branch.trim().equals(currentBranch.trim())) {
 								
 				branchSame = true;
 			}
 
 			log.debug("revision = {}, and currentRevision = {}", revision, currentRevision);
 
 			if(revision != null && revision.trim().equals(currentRevision.trim())) {
 							
 				revisionSame = true;
 			}
 
 			log.debug("branchSame = {}, and revisionSame = {}", branchSame, revisionSame);
 
 			if(branchSame && revisionSame && !forceUpdate) {
 
 				System.out.println("The site [" + siteUrl  + "] is already on branch [" + branch + "] and revision [" + revision  + "].");
 			}
 			else {				
 
 				HttpPost post = new HttpPost(url);
 				addAuthHeader(post);
 				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
 					
 				//check to see if tag was inputted without branch and revision
 				if(isTagAlone) {
 					
					nvps.add(new BasicNameValuePair("branch", tag.trim()));	
					log.debug("revision being added = {}", revision);
 				}
 				else {
 					
 					if(branch != null) {
 						
 						nvps.add(new BasicNameValuePair("branch", branch.trim()));
						log.debug("branch being added = {}", branch);
 					}
 	
 					if(revision != null) {
 	
 						nvps.add(new BasicNameValuePair("sha", revision.trim()));
						log.debug("revision being added = {}", revision);
 					}					
 				}
 				
 				nvps.add(new BasicNameValuePair("comment", message.trim()));
 
 				post.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
 				HttpResponse response = client.execute(post);
 
 				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 					HttpEntity entity = response.getEntity();
 					String resp = EntityUtils.toString(entity);
 					if(resp.equalsIgnoreCase("ok")) {
 						System.out.println("Success!");
 					} else {
 						System.out.println(resp);
 					}
 				} else {
 					System.out.println(response.toString());
 				}
 
 			}
 
 		} 
 		catch (Exception e) {
 
 			System.err.println("Failed to updated site [" + siteUrl  + "] to branch [" + branch  + "] and revision [" + revision  + "], or tag [" + tag + "].");
 		}
 
 	}
 
 	@Override
 	public String getCommandName() {
 		return "update";
 	}
 }
