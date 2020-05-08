 package com.burda.scraper.dao;
 
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
 import com.amazonaws.services.dynamodb.model.AttributeValue;
 import com.burda.scraper.model.persisted.HotelDetail;
 import com.burda.scraper.model.persisted.RoomTypeDetail;
 import com.google.code.ssm.api.ParameterValueKeyProvider;
 import com.google.code.ssm.api.ReadThroughSingleCache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.Lists;
 
 public class RoomTypeDetailDAO extends AbstractDynamoDBDAO<RoomTypeDetail>
 {
 	private static Logger logger = LoggerFactory.getLogger(RoomTypeDetailDAO.class);
 	private LoadingCache<HotelDetailCacheKey, List<RoomTypeDetail>> roomTypeDetailCache = CacheBuilder.newBuilder()
       .expireAfterWrite(1, TimeUnit.HOURS)
       .build(
           new CacheLoader<HotelDetailCacheKey, List<RoomTypeDetail>>() {
             public List<RoomTypeDetail> load(HotelDetailCacheKey key) {
               return loadRoomTypeDetailsFromDB(key);
             }
           });
 	
 	//@ReadThroughSingleCache(namespace = "RoomTypeDetail", expiration = 3600)
 	public List<RoomTypeDetail> getRoomTypeDetails(/*@ParameterValueKeyProvider(order=1)*/ HotelDetailCacheKey ck)
 	{
 		List<RoomTypeDetail> details = null;
 		try
 		{
 			details = roomTypeDetailCache.get(ck);	
 		}
		catch (Exception ee)
 		{
 			logger.error("error loading rtds", ee);
 		}
 		return details;	
 	}	
 	
 	private List<RoomTypeDetail> loadRoomTypeDetailsFromDB(HotelDetailCacheKey ck)
 	{
 		DynamoDBQueryExpression queryExpression = 
 				new DynamoDBQueryExpression(new AttributeValue().withS(ck.getHotelName()));
 
 		List<RoomTypeDetail> roomTypeDetails = Lists.newArrayList();
 		roomTypeDetails.addAll(getDynamoMapper().query(RoomTypeDetail.class, queryExpression));
 		
 		return roomTypeDetails;		
 	}
 }
