 package nl.kennisnet.arena.formats;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 import com.google.gson.annotations.SerializedName;
 
 import nl.kennisnet.arena.model.Image;
 import nl.kennisnet.arena.model.Information;
 import nl.kennisnet.arena.model.Participant;
 import nl.kennisnet.arena.model.ParticipantAnswer;
 import nl.kennisnet.arena.model.Picture;
 import nl.kennisnet.arena.model.Positionable;
 import nl.kennisnet.arena.model.Question;
 import nl.kennisnet.arena.services.ParticipantService;
 import nl.kennisnet.arena.utils.ArenaDataBean;
 import nl.kennisnet.arena.utils.UtilityHelper;
 
 public class Arena {
 
 	@SerializedName("stats")
 	private String stats;
 
 	@SerializedName("num_results")
 	private int numResults = 0;
 	
 	private enum STATS {
 		OK, NO_OBJECTS
 	}
 	
 	@SerializedName("results")
 	private List<Result> results = new ArrayList<Result>();
 
 	public String getStats() {
 		return stats;
 	}
 
 	public void setStats(String stats) {
 		this.stats = stats;
 	}
 
 	public void checkStats() {
 		if (results.size() > 0) {
 			stats = STATS.OK.toString();
 		} else {
 			stats = STATS.NO_OBJECTS.toString();
 		}
 	}
 
 	public List<Result> getResults() {
 		return results;
 	}
 
 	public void setResults(List<Result> results) {
 		this.results = results;
 	}
 
 	public void addResult(Result result) {
 		results.add(result);
 	}
 	
 	public int getNumResults() {
 		return results.size();
 	}
 
 	public void addPositionResults(List<Positionable> positionables,
 			String baseUrl, ArenaDataBean data) {
 
 		for (Positionable positionable : positionables) {
 			Result result = new Result();
 			result = buildResultData(result, positionable);
 			result = buildWebPage(result, positionable, baseUrl, data);
 			result = buildPositionableType(result, positionable);
 			result = buildObjectImage(result, positionable, baseUrl, data);
 			results.add(result);
 		}
 	}
 	
 	private Result buildResultData(Result result, Positionable positionable){
 		result.setId(positionable.getId());
 		result.setLat((float) positionable.getLocation().getPoint().getY());
 		result.setLng((float) positionable.getLocation().getPoint().getX());
 		result.setTitle(positionable.getName());
 		result.setHasDetailPage(true);		
 		return result;
 	}
 	
 	private Result buildWebPage(Result result, Positionable positionable, String baseUrl, ArenaDataBean data){
 		if (positionable instanceof Image) {
 			result.setWebpage(baseUrl + "item/show/" + data.getQuestId() + "/"
 					+ positionable.getId() + "/" + data.getPlayer() + ".item");
 		} else if (positionable instanceof Information){
 			result.setWebpage(baseUrl + "item/show/"+ positionable.getId() +".item");
 		}
 		else if (positionable instanceof Image){
 			result.setWebpage(((Image) positionable).getUrl());
 		}
 		return result;
 	}
 
 	private Result buildPositionableType(Result result, Positionable positionable) {
 		if (positionable instanceof Image) {
 			result.setObjectType("image");
 		} else if (positionable instanceof Information) {
 			result.setObjectType("information");
 		} else if (positionable instanceof Question) {
 			result.setObjectType("question");
 		}
 		return result;
 	}
 
 	private Result buildObjectImage(Result result, Positionable positionable,
 								  String baseUrl, ArenaDataBean data) {
 		if (positionable instanceof Question) {
 			
 			ParticipantAnswer participantAnswer = data.getParticipantService()
 									.getParticipationAnswer(data.getParticipationId(),
 									(Question) positionable);			
 			
 			result.setObjectUrl(buildQuestionImage(positionable, participantAnswer, baseUrl));
 		
 		} else if (positionable instanceof Information){			
			result.setObjectType(buildInformationImage(baseUrl));			
 		} else if (positionable instanceof Image){
			result.setObjectType(buildPhotoImage(positionable, baseUrl, data));
 		}
 		return result;
 	}
 	
 	private String buildQuestionImage(Positionable positionable, ParticipantAnswer participantAnswer, String baseUrl){
 		String objectUrl = "";
 		if (participantAnswer == null) {
 			objectUrl = baseUrl + "images/blue-question.png";
 		}
 		if (participantAnswer != null) {
 			if (participantAnswer.getAnswer().equals(
 					((Question) positionable).getCorrectAnswer())) {
 				objectUrl = baseUrl + "images/green-question.png";
 			} else {
 				objectUrl = baseUrl + "images/red-question.png";
 			}
 		}
 		return objectUrl;
 	}
 	
 	private String buildInformationImage(String baseUrl){
 		return baseUrl + "images/information.png";
 	}
 	
 	private String buildPhotoImage(Positionable positionable, String baseUrl, ArenaDataBean data){
 		String url = data.getParticipantService().getImageUrl((positionable).getId());
 		return url;
 	}
 	
 }
