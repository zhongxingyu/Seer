 package com.burda.scraper.dao;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.burda.scraper.model.persisted.MasterHotel;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 
 public class MasterHotelDAO extends AbstractDynamoDBDAO<MasterHotel>
 {
 	private Logger logger = LoggerFactory.getLogger(MasterHotelDAO.class);
 	private LoadingCache<String, MasterHotel> masterCache = CacheBuilder.newBuilder()
       .expireAfterWrite(1, TimeUnit.HOURS)
       .build(
           new CacheLoader<String, MasterHotel>() {
             public MasterHotel load(String key) {
               return loadMasterHotel(key);
             }
           });
 	
 	//@ReadThroughSingleCache(namespace = "MasterHotel", expiration = 3600)
 	public MasterHotel getByHotelName(/*@ParameterValueKeyProvider*/ String name)
 	{
 		MasterHotel mh = null;
 		try
 		{
 			mh = masterCache.get(name);
 		}
		catch (Exception ee)
 		{
 			logger.error("Unable to load master hotel", ee);
 		}
 		return mh;
 	}
 	
 	private MasterHotel loadMasterHotel(String name)
 	{
 		return getDynamoMapper().load(MasterHotel.class,  name);
 	}
 }
