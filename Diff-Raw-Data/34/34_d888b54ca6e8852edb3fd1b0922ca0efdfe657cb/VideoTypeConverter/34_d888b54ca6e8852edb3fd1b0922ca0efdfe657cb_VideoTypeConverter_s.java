 package com.photon.phresco.service.converters;
 
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.VideoType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.VideoTypeDAO;
 import com.photon.phresco.util.ServiceConstants;
 
 public class VideoTypeConverter implements Converter<VideoTypeDAO, VideoType>, ServiceConstants {
 	
 	@Override
 	public VideoType convertDAOToObject(VideoTypeDAO dao,
 			MongoOperations mongoOperation) throws PhrescoException {
 		VideoType videoType = new VideoType();
 		videoType.setVideoInfoId(dao.getVideoInfoId());
 		videoType.setUrl(dao.getUrl());
 		videoType.setName(dao.getName());
 		videoType.setArtifactGroup(createArtifactGroup(dao.getArtifactGroupId(),mongoOperation));
 		return videoType;
 	}
 
 	@Override
 	public VideoTypeDAO convertObjectToDAO(VideoType videoType)
 			throws PhrescoException {
 		VideoTypeDAO videoDAO = new VideoTypeDAO();
 		videoDAO.setVideoInfoId(videoType.getVideoInfoId());
 		videoDAO.setUrl(videoType.getUrl());
 		videoDAO.setName(videoType.getName());
 		videoDAO.setArtifactGroupId(videoType.getArtifactGroup().getId());
 		return videoDAO;
 	}
 	
 	private ArtifactGroup createArtifactGroup(String artifactGroupId,MongoOperations mongoOperation) throws PhrescoException {
 		ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(artifactGroupId)), ArtifactGroupDAO.class);
 		Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
 			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGroupDAO, mongoOperation);
 		return artifactGroup;
 	}
 
 
 }
