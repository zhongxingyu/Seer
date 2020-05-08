 package in.di.ce.service;
 
 import in.di.ce.Tracking;
 import in.di.ce.Video;
 import in.di.ce.prevalence.BaseModel;
 import in.di.ce.prevalence.transaction.RegisterChunks;
 import in.di.ce.prevalence.transaction.RegisterVideo;
 import in.di.ce.prevalence.transaction.UnregisterChunks;
 import in.di.ce.service.rta.Ok;
 import in.di.ce.service.rta.Respuesta;
 import in.di.ce.service.rta.TodoMal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 
 @Controller
 @RequestMapping("/video")
 public class VideoService {
 
 	private static final Log log = LogFactory.getLog(VideoService.class); 
 
 	@Autowired @Setter @Getter private BaseModel baseModel;
 
 	/**
 	 * 
 	 * @param videoId
 	 * @return el video para este video id
 	 */
 	@RequestMapping(value="check/{videoId}", method = RequestMethod.GET)
 	public @ResponseBody Respuesta<Video> checkVideo(@PathVariable String videoId){
 		Video video = null;
 		if(baseModel.getModel().videoExist(videoId)){
 			video = baseModel.getModel().getVideo(videoId);
 			return new Ok(video);
 		} else {
 			return new TodoMal(video);
 		}
 		
 	}	
 
 	/**
 	 * 
 	 * @param videoId
 	 * @param fileName
 	 * @param lenght
 	 * @param userId
 	 * @param chunks
 	 * @return true si se registro el video
 	 */
 	@RequestMapping(value="register/{videoId}/{fileName}/{lenght}/{userId}", method = RequestMethod.POST)
 	public @ResponseBody Respuesta<Video> registerVideo(@PathVariable String videoId, @PathVariable String fileName, @PathVariable Long lenght, @PathVariable String userId, @RequestParam(value="chunks", required=true) String chunks){
 		
 		if (!baseModel.getModel().userExist(userId)) {
 			return new TodoMal("El usuario "+userId+" no existe");
 		}
 		
 		if(baseModel.getModel().videoExist(videoId)){
 			log.info("Unable to add video ["+videoId+"]in videos central repository. Video already exists");
 			Video video = new Video(videoId, "video id: " + videoId+" is already registered in central repository", 0, null, null);
 			return new TodoMal("El video ya existe en el indice. Se registraron los chunks para este usuario");
 		}
 
 		Video registered = null;
 		try {
 			registered = (Video)this.getBaseModel().getPrevayler().execute(new RegisterVideo(videoId, fileName, lenght, this.chunkIds(chunks), userId));
 			return new Ok(registered);
 		} catch (Exception e) {
 			log.error("unable to register video "+ videoId, e);
 		}
 
 		return new TodoMal(registered);
 	}
 
 	/**
 	 * 
 	 * @param fileName
 	 * @param userId
 	 * @param chunks
 	 * @return map con los chunks registrados para este usuario
 	 */
 	/*
 	 * chunk map format expected: 
 	 * ordinal-chunkId|ordinal-chunkId
 	 */
	@RequestMapping(value="registerChunks/{fileName}/{userId}/{chunks}", method = RequestMethod.POST)
 	public @ResponseBody Respuesta registerChunks(@PathVariable String fileName, @PathVariable String userId, @PathVariable String chunks){
 		
 		if(StringUtils.isEmpty(fileName)){
 			log.error("File name can not be null nor empty");
 			return new TodoMal("File name can not be null nor empty");
 		}
 		String videoId = baseModel.getModel().getVideoIdFromFilename(fileName);
 		if(StringUtils.isEmpty(videoId)){
 			log.error("No video id for file "+fileName+" in central repository");
 			return new TodoMal("No video id for file "+fileName+" in central repository");
 		}
 		Map<Integer, String> chunkOrdinals = chunkOrdinalsForExistentVideo(baseModel.getModel(), videoId, chunks);
 		if(MapUtils.isEmpty(chunkOrdinals)){
 			log.error("No chunks passed for register for file: "+fileName+" - id: "+videoId);
 			return new TodoMal("No chunks passed for register for file: "+fileName+" - id: "+videoId);
 		}
 		log.info("registering chunks "+chunkOrdinals+"for video "+videoId+" by user " + userId);
 		
 		try {
 			this.getBaseModel().getPrevayler().execute(new RegisterChunks(videoId, userId, new ArrayList(chunkOrdinals.keySet())));
 		} catch (Exception e) {
 			log.error("unable to register chunks "+ userId +" - "+videoId+" - "+chunks, e);
 		}
 		
 		return new Ok(chunkOrdinals);
 	}
 	
 	/**
 	 * 
 	 * @param fileName
 	 * @param userId
 	 * @param chunks
 	 * @return map con los chunks borrados del registro para este usuario
 	 */
 	/*
 	 * chunk map format expected: 
 	 * ordinal-chunkId|ordinal-chunkId
 	 */
 	@RequestMapping(value="unregisterChunks/{fileName}/{userId}/{from}/{lenght}", method = RequestMethod.GET)
 	public Map<Integer, String> unregisterChunks(@PathVariable String fileName, @PathVariable String userId, @PathVariable String chunks){
 		
 		if(StringUtils.isEmpty(fileName)){
 			throw new IllegalArgumentException("File name can not be null nor empty");
 		}
 		String videoId = baseModel.getModel().getVideoIdFromFilename(fileName);
 		Map<Integer, String> chunkOrdinals = chunkOrdinalsForExistentVideo(baseModel.getModel(), videoId, chunks);
 		log.info("unregistering chunks "+chunkOrdinals+"for video " +  videoId + " by user " + userId);
 		
 		try {
 			this.getBaseModel().getPrevayler().execute(new UnregisterChunks(videoId, userId, new ArrayList(chunkOrdinals.keySet())));
 		} catch (Exception e) {
 			log.error("unable to unregister chunks "+ userId +" - "+videoId+" - "+chunks, e);
 		}
 		
 		return chunkOrdinals;
 	}
 
 	@RequestMapping(value="getChunks/{videoId}/{userId}", method = RequestMethod.GET)
 	public @ResponseBody List<String> getChunksFrom(@PathVariable String videoId, @PathVariable String userId){
 		if(!baseModel.getModel().videoExist(videoId)){
 			throw new IllegalArgumentException("video id: " + videoId+" does not exist");
 		}
 		log.info("about to retrieve chunks from video: " +  videoId + " for user " + userId);
 		return baseModel.getModel().getVideo(videoId).getChunks(); 
 	}
 
 	public Map<Integer, String> chunkOrdinalsForExistentVideo(Tracking tracking, String videoId, String chunks) {
 
 		if(!tracking.videoExist(videoId)){
 			throw new IllegalArgumentException("video id: "+videoId+" does not exist");
 		}
 		Video video = tracking.getVideo(videoId);
 
 		Map<Integer, String> result = new HashMap<Integer, String>();
 		
 		for(String chunk : chunks.split("\\&")){
 			String[] splittedChunk = chunk.split("!");
 			int chunkOrdinal = Integer.parseInt(splittedChunk[0]);
 			String chunkId = splittedChunk[1];
 			
 			if(chunkOrdinal > video.getChunks().size()) {
 				throw new IllegalArgumentException("Trying to register chunk that is beyond video's size");
 			}
 			
 			if(video.getChunks().get(chunkOrdinal) != null && video.getChunks().get(chunkOrdinal).equals(chunkId)){
 				result.put(chunkOrdinal, chunkId);
 			} 
 		}
 		if(MapUtils.isEmpty(result)){
 			throw new IllegalArgumentException("no valid chunks passed for video: " + videoId);
 		}
 		
 		return result;
 	}
 
 
 	private List<String> chunkIds(String chunks) {
 		List<String> result = Arrays.asList(chunks.split("!"));
 		return result;
 	}
 
 }
 
