 package com.burda.scraper.dao;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.burda.scraper.model.persisted.HotelDetail;
 import com.burda.scraper.model.persisted.InventorySource;
 import com.burda.scraper.model.persisted.SourceHotel;
 import com.google.code.ssm.api.ParameterValueKeyProvider;
 import com.google.code.ssm.api.ReadThroughSingleCache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 
 public class SourceHotelDAO extends AbstractDynamoDBDAO<SourceHotel>
 {	
 	private static final Logger logger = LoggerFactory.getLogger(SourceHotelDAO.class);
 	private static class CacheKey 
 	{
 		private final InventorySource is;
 		private final String hotelId;
 		
 		private CacheKey(String hotelId, InventorySource is)
 		{
 			this.is = is;
 			this.hotelId = hotelId;
 		}
 		
 		public boolean equals(Object o)
 		{
 			if (o == null)
 				return false;
 			
 			if (!(o instanceof CacheKey))
 				return false;
 			
 			CacheKey other = (CacheKey)o;
 			if (is.equals(other.is))
 				if (hotelId.equals(other.hotelId))
 					return true;
 			return false;
 		}
 	}
 	
 	private LoadingCache<CacheKey, SourceHotel> sourceCache = CacheBuilder.newBuilder()
       .expireAfterWrite(1, TimeUnit.HOURS)
       .build(
           new CacheLoader<CacheKey, SourceHotel>() {
             public SourceHotel load(CacheKey key) {
               return loadSourceHotel(key);
             }
           });
 	
 	//@ReadThroughSingleCache(namespace = "SourceHotel", expiration = 3600)
 	public SourceHotel getByHotelId(
 		/*@ParameterValueKeyProvider(order=1)*/ String hotelId, /*@ParameterValueKeyProvider(order=2)*/ InventorySource is)
 	{
 		SourceHotel s = null;
 		try
 		{
 			s = sourceCache.get(new CacheKey(hotelId, is));
 		}
		catch (Exception ee)
 		{
 			logger.error("unable to load source hotel", ee);
 		}
 		return s;
 	}
 	
 	private SourceHotel loadSourceHotel(CacheKey key)
 	{
 		return getDynamoMapper().load(SourceHotel.class,  key.hotelId, key.is.name());
 		/*
 		DynamoDBQueryExpression queryExpression = 
 				new DynamoDBQueryExpression(
 						new AttributeValue().withS(hotelId)).withRangeKeyCondition(
 								new Condition().withAttributeValueList(
 										new AttributeValue().withS(is.name())));
 		*/
 	}
 }
