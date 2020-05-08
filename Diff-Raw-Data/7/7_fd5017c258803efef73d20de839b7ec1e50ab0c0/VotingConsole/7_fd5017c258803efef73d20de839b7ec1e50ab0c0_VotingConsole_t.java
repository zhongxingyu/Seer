 package net.buhacoff.netvote.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import net.buhacoff.netvote.model.*;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  *
  * @author jbuhacoff
  */
 public class VotingConsole {
     
     private IssueList issueList;
     private String netvoteServerAddress;
     
     private ClientConnectionManager connectionManager;
     private HttpClient httpClient;
     public final ObjectMapper mapper = new ObjectMapper();
     private Input input = new Input();
     
     public VotingConsole() {
         SchemeRegistry sr = new SchemeRegistry();
         sr.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
         sr.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
         sr.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
         sr.register(new Scheme("https", 8443, SSLSocketFactory.getSocketFactory()));
         connectionManager = new PoolingClientConnectionManager(sr);
         HttpParams httpParams = new BasicHttpParams();
         httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
         httpClient = new DefaultHttpClient(connectionManager, httpParams);        
     }
     
     public void setIssueList(IssueList issueList) {
         this.issueList = issueList;
     }
 
     public void setServerAddress(String netvoteServerAddress) {
         this.netvoteServerAddress = netvoteServerAddress;
     }
     
     public void start() {
         try {
             while(true) {
                 clearScreen();
                 displayInstructions();
                 VoterSecretPassphrase secret = getVoterPassphrase();
                 for(Issue issue : issueList.issues) {
                     boolean ok = false;
                     while(!ok) {
                         try {
                             String issueChoice = getVoterInput(issue);
                             Ballot ballot = createBallot(secret.voterId, issue, issueChoice);
                             BallotRequest ballotRequest = BallotRequest.createBallotRequest(ballot, secret.passphrase);
                             submitBallot(ballotRequest);
                             ok = true;
                         }
                         catch(Exception e) {
                             System.err.println("Error: "+e.toString());
                             e.printStackTrace(System.err);
                         }
                     }
                 }
                 printReceipt();
                 displayEndInstructions();
             }        
         }
         finally {
             close();
         }
     }
 
     
     public void close() {
         connectionManager.shutdown();        
     }
 
     public void clearScreen() {
         //System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
     }
     
     public void displayInstructions() {
         System.out.println("NetVote Instructions\n----------------------------------------\n1. Enter your Voter ID\n2. Select your vote on each issue\n\n");
     }
     
     public void printReceipt() {
         // XXX TODO need to print all the signed tally documents  (we will save it to a file,  and assume a printer is available to print this or email it to the user)
         // ut fo rthe demo maybe just display them to the screen. 
     }
     
     public void displayEndInstructions() {
         System.out.println("\n\n----------------------------------------\n\nThank you for voting.\n\nPlease take your receipt.\n\nPress <ENTER> to exit...\n");
         try {
             System.in.read();
             System.in.read();
         }
         catch(IOException e) {
             System.err.println("Error: "+e.toString());
         }
     }
 
     public VoterSecretPassphrase getVoterPassphrase() {
         VoterSecretPassphrase voter = new VoterSecretPassphrase();
         voter.registrationAuthorityId = "default";
         while(voter.voterId == null || voter.registrationAuthorityId == null || voter.passphrase == null ) {
             try {
                 if( voter.voterId == null ) {
                     voter.voterId = input.readInputStringWithPrompt("Voter ID (SSN)> ");
                     if( voter.voterId.trim().isEmpty() ) { voter.voterId = null; }
                 }
                 if( voter.passphrase == null ) {
                     voter.passphrase = input.readInputStringWithPrompt("Secret Passphrase> ");
                     if( voter.passphrase.trim().isEmpty() ) { voter.passphrase = null; }
                 }
             }
             catch(IOException e) {
                 System.err.println("Error: "+e.toString());
             }
         }
         return voter;
     }
     
     public String getVoterInput(Issue issue) throws IOException {
         String issueHeader = String.format("%s [%s]\n--------------------\n%s\n--------------------\n", issue.title, issue.issueId, issue.description);
         String issueMenu = "";
         for(int i=0; i<issue.choices.size(); i++) {
             issueMenu += String.format("[%d] %s\n", i+1, issue.choices.get(i));
         }
         issueMenu += String.format("[%d] %s\n", 0, "Write-in (will not be counted on YES/NO votes)");
         String issuePrompt = issueHeader+issueMenu+"Vote> ";
         String issueChoice;
         int menuChoice = input.readInputIntegerInRangeWithPrompt(issuePrompt, issue.choices.size());
         if( menuChoice == 0 ) {
             issueChoice = input.readInputStringWithPrompt("Write-in> ");
         }
         else {
             issueChoice = issue.choices.get(menuChoice-1);
         }
         return issueChoice;
     }
     
     public Ballot createBallot(String voterId, Issue issue, String voterChoice) {
         Ballot tally = new Ballot();
         tally.netvoteClientId = "netvote-client-1"; // XXX TODO need to get client id from system properties
         tally.registrationAuthorityId = "registration-authority"; // XXX TODO need to get registrationa uthority from system prpoerties
         tally.voterId = voterId;
         tally.issueId = issue.issueId;
         tally.choice = voterChoice;
        tally.anonymous = issue.anonymous;
         //tally.signature = "signature-block"; // XXX TODO create the signature document, sign it, and put the signed document here  (maybe use S/MIME format?)
         return tally;
     }
     
     public void submitBallot(BallotRequest ballotRequest) {
         try {
             HttpPost request = new HttpPost(netvoteServerAddress+"/Netvote/ballot");
             request.setEntity(new StringEntity(mapper.writeValueAsString(ballotRequest), ContentType.APPLICATION_JSON));
 //            String message = String.format("vote=1&ssn=%s&issue=%s&choice=%s", ballot.voterId, ballot.issueId, ballot.choice);
 //            request.setEntity(new StringEntity(message, ContentType.create("application/x-www-form-urlencoded", "UTF-8")));
             HttpResponse httpResponse = httpClient.execute(request);
             byte[] content = null;
             HttpEntity entity = httpResponse.getEntity();
             if( entity != null ) {
                 InputStream contentStream = entity.getContent();
                 if( contentStream != null ) {
                     content = IOUtils.toByteArray(contentStream);
                     contentStream.close();
                 }
                 System.out.println(new String(content));
             }
             request.releaseConnection();
         }
         catch(Exception e) {
             System.err.println("Cannot submit vote: "+e.toString());            
         }
     }
     
     public void registerVoter(VoterPrivateRecord voterPrivateRecord) {
         try {
             HttpPost request = new HttpPost(netvoteServerAddress+"/Netvote/register");
 //            String message = String.format("register=1&ssn=%s&firstName=%s&lastName=%s&address=%s", voterPrivateRecord.voterId, voterPrivateRecord.firstName, voterPrivateRecord.lastName, voterPrivateRecord.address);
 //            request.setEntity(new StringEntity(message, ContentType.create("application/x-www-form-urlencoded", "UTF-8")));
             request.setEntity(new StringEntity(mapper.writeValueAsString(voterPrivateRecord), ContentType.APPLICATION_JSON));
             HttpResponse httpResponse = httpClient.execute(request);
             byte[] content = null;
             HttpEntity entity = httpResponse.getEntity();
             if( entity != null ) {
                 InputStream contentStream = entity.getContent();
                 if( contentStream != null ) {
                     content = IOUtils.toByteArray(contentStream);
                     contentStream.close();
                 }
                 System.out.println(new String(content));
             }
             request.releaseConnection();
             
         }
         catch(Exception e) {
             System.err.println("Cannot register voter: "+e.toString());            
         }
     }
 }
