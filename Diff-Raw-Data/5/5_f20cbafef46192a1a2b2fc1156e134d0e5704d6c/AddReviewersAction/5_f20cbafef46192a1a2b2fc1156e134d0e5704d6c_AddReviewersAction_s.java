 package com.loquatic.crucible.cli.actions;
 
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 
 import com.loquatic.crucible.cli.CommandLineOption;
 import com.loquatic.crucible.json.IProtocolHandler;
 import com.loquatic.crucible.json.ResponseData;
 import com.loquatic.crucible.util.TargetUrlUtil;
 
 public class AddReviewersAction extends AbstractAction {
 
 	public AddReviewersAction( IProtocolHandler myHandler ) {
 		super( myHandler ) ;
 	}
 	
 	@Override
 	public boolean perform(CommandLine commandLine, Properties props) {
 		
		String reviewers = getReviewId( commandLine ) ; 
 
		String reviewId = getReviewers( commandLine ) ; 
 		
 		boolean success = addReviewersToReview(props, reviewId, reviewers ) ;
 				
 		return success ;
 	}
 
 	@Override
 	public boolean addOptions(Options options) {
 		
 		options.addOption( CommandLineOption.REVIEW_ID.getName(), true, "The ID of the review you wish to close." ) ;
 
 		options.addOption( CommandLineOption.REVIEWERS.getName(), true, "Comma " +
                 "separated list of Crucible usernames to which " +
                 "to assign this review." ) ;
 
 		return true ;
 	}
 
 	@Override
 	public void printHelp() {
 		System.out.println( "--action addReviewers --reviewers user1 --reviewId PROJ-ID" ) ;
 		System.out.println( "--action addReviewers --reviewers user1,user2,user3 --reviewId PROJ-ID" ) ;
 	}
 	
 	/**
 	 * Per the Atlassian Crucible REST docs, the reviewers is a literal comma 
 	 * separated list of usernames. Not JSON formatting, just a the names
 	 * separated by commas, no spaces. Also, this doesn't return any info
 	 * it actually returns a 204 status code.
 	 * 
 	 * @param props
 	 * @param reviewId
 	 * @param reviewers
 	 * @return
 	 */
 	private boolean addReviewersToReview( Properties props, String reviewId, String reviewers ) {
 		
 		boolean success = false ;
 		
 		StringBuilder url = TargetUrlUtil.createReviewUrl(  props ) ;
 		
 		url.append( reviewId ).append( "/reviewers" ).append( "?" ).append("FEAUTH=").append(getToken() ) ;
 		
 		try {
 			ResponseData response = getHandler().doPost( reviewers, url.toString() ) ;
 			if( response.getHttpStatusCode() >= 200 && response.getHttpStatusCode() > 300 ) {
 				System.out.println( "successfully add the reviewers " + reviewers + " to review " + reviewId ) ;
 			}
 			success = true ;
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 
 		return success ;
 	}
 
 
 }
