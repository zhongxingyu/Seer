 package com.photon.phresco.service.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.VideoInfo;
 import com.photon.phresco.commons.model.VideoType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.VideoInfoDAO;
 import com.photon.phresco.service.dao.VideoTypeDAO;
 import com.photon.phresco.util.ServiceConstants;
 
 public class VideoConverter implements Converter<VideoInfoDAO, VideoInfo>, ServiceConstants {
 
 	@Override
 	public VideoInfo convertDAOToObject(VideoInfoDAO dao,
 			MongoOperations mongoOperation) throws PhrescoException {
 		VideoInfo videoInfo = new VideoInfo();
 		videoInfo.setId(dao.getId());
 		videoInfo.setImageurl(dao.getImageurl());
 		videoInfo.setName(dao.getName());
 		videoInfo.setVideoList(createVideoType(dao.getVideoListId(), mongoOperation));
 		return videoInfo;
 	}
 
 	@Override
 	public VideoInfoDAO convertObjectToDAO(VideoInfo videoInfo)
 			throws PhrescoException {
 		VideoInfoDAO videoDAO = new VideoInfoDAO();
 		videoDAO.setId(videoInfo.getId());
 		videoDAO.setName(videoInfo.getName());
 		videoDAO.setImageurl(videoInfo.getImageurl());
 		videoDAO.setVideoListId(videoInfo.getVideoList().get(0).getVideoInfoId());
 		return videoDAO;
 	}
 
 	private List<VideoType> createVideoType(String videoListId,MongoOperations mongoOperation) throws PhrescoException {
 		List<VideoType> videoTypes = new ArrayList<VideoType>();
 		List<VideoTypeDAO> videoTypeDAO = mongoOperation.find(VIDEOTYPES_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(videoListId)), VideoTypeDAO.class);
 		for(VideoTypeDAO vType : videoTypeDAO){
 		Converter<VideoTypeDAO, VideoType> videoConverter = 
 			(Converter<VideoTypeDAO, VideoType>) ConvertersFactory.getConverter(VideoTypeDAO.class);
 		VideoType videoType = videoConverter.convertDAOToObject(vType, mongoOperation);
 		videoTypes.add(videoType);
 		}
 		return videoTypes;
 	}
 
 }
