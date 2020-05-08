 package com.burda.scraper.dao;
 
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.burda.scraper.model.persisted.HotelDetail;
 import com.burda.scraper.model.persisted.RoomTypeDetail;
 import com.google.code.ssm.api.ParameterValueKeyProvider;
 import com.google.code.ssm.api.ReadThroughSingleCache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 
 public class HotelDetailDAO extends AbstractDynamoDBDAO<HotelDetail>
 {
 	private Logger logger = LoggerFactory.getLogger(HotelDetailDAO.class);
 	private RoomTypeDetailDAO roomTypeDetailDAO;
 	
 	private LoadingCache<HotelDetailCacheKey, HotelDetail> hotelDetailCache = CacheBuilder.newBuilder()
       .expireAfterWrite(1, TimeUnit.HOURS)
       .build(
           new CacheLoader<HotelDetailCacheKey, HotelDetail>() {
             public HotelDetail load(HotelDetailCacheKey key) {
               return loadHotelDetailFromDB(key);
             }
             @Override
             public Map<HotelDetailCacheKey, HotelDetail> loadAll(Iterable<? extends HotelDetailCacheKey> keys)
             {
             	return null;
             }
           });
 	
 	//@ReadThroughSingleCache(namespace = "HotelDetail", expiration = 3600)
 	public HotelDetail getHotelDetail(/*@ParameterValueKeyProvider(order=1)*/ HotelDetailCacheKey ck)
 	{
 		HotelDetail hd = null;
 		try
 		{
 			hd = hotelDetailCache.get(ck);
 		}
		catch (ExecutionException ee)
 		{
 			logger.error("Unable to load hotelDetail", ee);
 		}
 		return hd;
 	}
 	
 	private HotelDetail loadHotelDetailFromDB(HotelDetailCacheKey ck)
 	{
 		HotelDetail hd = getDynamoMapper().load(HotelDetail.class,  ck.getHotelName());
 		if (hd != null)
 		{
 			List<RoomTypeDetail> roomTypes = roomTypeDetailDAO.getRoomTypeDetails(ck);
 			if (roomTypes != null)
 			{
 				for (RoomTypeDetail rtd: roomTypes)
 					hd.addRoomTypeDetail(rtd);
 			}
 		}
 		return hd;		
 	}
 	
 	@Override
 	public void save(HotelDetail hd)
 	{
 		super.save(hd);
 		if (hd.getRoomTypeDetails() != null && !hd.getRoomTypeDetails().isEmpty())
 			roomTypeDetailDAO.save(hd.getRoomTypeDetails());
 	}
 	
 	public void setRoomTypeDetailDAO(RoomTypeDetailDAO rtdDAO)
 	{
 		this.roomTypeDetailDAO = rtdDAO;
 	}
 }
