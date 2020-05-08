 package com.reckonlabs.reckoner.contentservices.repo;
 
 import java.util.Date;
 import java.util.List;
 import java.util.LinkedList;
 import javax.annotation.Resource;
 
 import com.mongodb.WriteResult;
 import org.bson.types.ObjectId;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.CollectionOptions;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.BasicQuery;
 
 import com.reckonlabs.reckoner.contentservices.factory.MongoDbQueryFactory;
 import com.reckonlabs.reckoner.contentservices.utility.ServiceProps;
 import com.reckonlabs.reckoner.domain.reckoning.Vote;
 import com.reckonlabs.reckoner.domain.utility.DBUpdateException;
 
 public class VoteRepoImpl implements VoteRepoCustom {
 	
 	@Autowired
 	MongoTemplate mongoTemplate;
 	
 	@Resource
 	ServiceProps serviceProps;
 	
 	public static final String VOTE_COLLECTION = "vote";
 	
 	private static final Logger log = LoggerFactory
 			.getLogger(VoteRepoImpl.class);
 
 	@Override
 	public void insertVote(Vote vote) {
 		if (!mongoTemplate.collectionExists(VOTE_COLLECTION)) {
 			mongoTemplate.createCollection(VOTE_COLLECTION, 
					new CollectionOptions(null, serviceProps.getVotePersistenceSize(), true));
 		}
 		
 		mongoTemplate.insert(vote, VOTE_COLLECTION);
 	}	
 	
 }
