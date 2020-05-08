 package plan;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import models.Cacho;
 import models.RetrievalPlan;
 import models.User;
 import models.UserCacho;
 import models.UserChunks;
 import models.Video;
 import play.Play;
 
 public class RetrievalPlanCreator {
 
 	private final Video video;
 	private final User planRequester;
 	private final CachoPropioMaker cachoPropioMaker;
 	private final CachoAjenoMaker cachoAjenoMaker;
 
 	/*
 	 * TODO usar el maxCachoSize para limitar el tamaño de los cachos -> ¿va esto?
 	 */
 	private int maxCachoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")); 
 	private long chunkSize = Long.valueOf(Play.configuration.getProperty("chunk.size")) * 1024 * 1024;
 
 	public RetrievalPlanCreator(Video video, User user) {
 		super();
 		this.video = video;
 		this.planRequester = user;
 		cachoPropioMaker = new CachoPropioMaker(user, video);
 		cachoAjenoMaker = new CachoAjenoMaker(video);
 	}
 
 	public RetrievalPlan generateRetrievalPlan(){
 		
 		play.Logger.info("armando plan para "+planRequester.email+" - video: "+video.videoId+" * "+video.lenght);
 		
 		boolean firstCacho = true;
 
 		List<UserChunks> userChunks = new ArrayList<UserChunks>(video.userChunks);
 	
 		UserChunks planRequesterChunks = video.getChunksFrom(planRequester);
 		
 		play.Logger.info("plan requester chunks: %s", planRequesterChunks.chunks);
 
 		SortedSet<UserChunks> result = new TreeSet<UserChunks>(new ChunkPositionComparator());
 
 		
 		List<UserChunks> noPlanRequesterChunks = noPlanRequesterChunks(userChunks);
 		
 		List<UserChunks> removedNoRequesterChunks = new ArrayList<UserChunks>();
 		
 
 		for(int i = 0; i<video.chunks.size(); i++) {
 			/*
 			 * mientras el requester tenga este chunk, voy inflando un userChunks para el
 			 */
 			UserChunks nextCacho = null;
 			if (planRequesterChunks.hasChunk(i)){
 				nextCacho = cachoPropioMaker.makeCacho(i, planRequesterChunks);
 				
 			} else {
 				/*
 				 * en cuento deja de tener, inflo uno para el que menos tenga a partir del current chunk 
 				 * o hasta que el requester vuelva a tener el chunk
 				 * 
 				 */
 				UserChunks noRequesterShortestCacho = cachoAjenoMaker.makeCacho(i, planRequesterChunks, noPlanRequesterChunks, firstCacho);
 				/*
 				 * round robin
 				 */
 				
 				removeFromNoRequesterUserChunks(noRequesterShortestCacho, noPlanRequesterChunks, removedNoRequesterChunks, i);
 				
 				if(noRequesterShortestCacho == null) {
 					/*
 					 * try again with excluded users
 					 */
 					noRequesterShortestCacho = cachoAjenoMaker.makeCacho(i, planRequesterChunks, removedNoRequesterChunks, firstCacho);
 					if(noRequesterShortestCacho == null) {
 						return null;
 					}
 				}
 				nextCacho = noRequesterShortestCacho;
 				play.Logger.info("next cacho -> chunks: %s - user: %s - lowerChunkPos: %s - higherChunkPos: %s", nextCacho.chunks.size(), nextCacho.user.email, nextCacho.lowerChunkPosition(), nextCacho.higherChunkPosition());
 			}
 			firstCacho = false;
 			result.add(nextCacho);
 			if(nextCacho.hasChunk(video.chunks.size()-1)){
 				play.Logger.info("debug");
 				play.Logger.info("result %s",result);
 				
 				Set<UserChunks> mergedChunks = new SameUserChunksMerger().mergeChunks(result);
 				play.Logger.info("mergedChunks: %s", mergedChunks);
 				List<UserCacho> cachos = cachosFrom(mergedChunks, video);
 				play.Logger.info("cachos %s", cachos);
 				return retrievalPlanFor(video, cachos);
 			} 
 			i = nextCacho.higherChunkPosition();
 		}
 		play.Logger.error("Unable to ellaborate retrieving plan for video %s for user %s - not enough sources available", video.videoId, planRequester.email);
 		return null;
 	}
 
 	private List<UserChunks> removeFromNoRequesterUserChunks(UserChunks noRequesterShortestCacho,
 			List<UserChunks> noPlanRequesterChunks, List<UserChunks> removedNoRequesterChunks, int from) {
 		
 		if(noRequesterShortestCacho == null){
 			return noPlanRequesterChunks;
 		}
 		
 
 //		List<UserChunks> toRemove = new ArrayList<UserChunks>();
 		for(UserChunks uc : noPlanRequesterChunks) {
 			if(uc.user.email.equals(noRequesterShortestCacho.user.email) && uc.hasChunk(from)) {
 				removedNoRequesterChunks.add(uc);
 			}
 		}
 		noPlanRequesterChunks.removeAll(removedNoRequesterChunks);
 		
 		return removedNoRequesterChunks;
 	}
 
 	private List<UserChunks> noPlanRequesterChunks(List<UserChunks> userChunks) {
 
 		List<UserChunks> result = new ArrayList<UserChunks>();
 
 		for(UserChunks uc :  userChunks) {
 			if(!uc.user.email.equals(planRequester.email)) {
 				result.add(uc);
 			}
 		}
 
 		return result;
 	}
 
 	private RetrievalPlan retrievalPlanFor(Video video,
 			List<UserCacho> userCachos) {
 		play.Logger.info("cachos para plan: %s", userCachos);
		play.Logger.info("cachos para plan: %s", userCachos.size());
		play.Logger.info("cachos para plan: %s", userCachos.get(0).getCacho().from);
		play.Logger.info("cachos para plan: %s", userCachos.get(0).getCacho().lenght);
 		
 		RetrievalPlan rp = new RetrievalPlan(video, userCachos); 
		play.Logger.info("plan: %s", rp.getUserCachos().get(0).getCacho().lenght);
 		return rp;
 	}
 
 	private List<UserCacho> cachosFrom(Set<UserChunks> chunks, Video video) {
 
 		List<UserCacho> result = new ArrayList<UserCacho>();
 		for(UserChunks uc : chunks) {
 			result.add(new UserCacho(uc.user, cachoFromUserChunks(uc, video)));
 		}
 		return result;
 	}
 
 	private Cacho cachoFromUserChunks(UserChunks uc, Video video) {
 
 		long from =  uc.chunks.get(0).position * chunkSize;
 		long lenght = uc.chunks.size() * chunkSize;
 
 		
 		//*TODO*/
 		boolean lastCacho = (uc.chunks.get(uc.chunks.size()-1).position) == video.chunks.size()-1;
 
 		if(lastCacho){
 			long diff  = video.lenght % chunkSize;
 			lenght-= diff;
 		}
 		Cacho cacho = new Cacho(from, lenght);
 
 		return cacho;
 	}
 }
