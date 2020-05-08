 package toctep.skynet.backend.test.dal;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import toctep.skynet.backend.dal.dao.BoundingBoxDao;
 import toctep.skynet.backend.dal.dao.BoundingBoxTypeDao;
 import toctep.skynet.backend.dal.dao.CountryDao;
 import toctep.skynet.backend.dal.dao.DaoFacade;
 import toctep.skynet.backend.dal.dao.GeoDao;
 import toctep.skynet.backend.dal.dao.GeoTypeDao;
 import toctep.skynet.backend.dal.dao.HashtagDao;
 import toctep.skynet.backend.dal.dao.KeywordDao;
 import toctep.skynet.backend.dal.dao.LanguageDao;
 import toctep.skynet.backend.dal.dao.PlaceDao;
 import toctep.skynet.backend.dal.dao.PlaceTypeDao;
 import toctep.skynet.backend.dal.dao.SourceTypeDao;
 import toctep.skynet.backend.dal.dao.TimeZoneDao;
 import toctep.skynet.backend.dal.dao.TweetContributorDao;
 import toctep.skynet.backend.dal.dao.TweetDao;
 import toctep.skynet.backend.dal.dao.TweetHashtagDao;
 import toctep.skynet.backend.dal.dao.TweetKeywordDao;
 import toctep.skynet.backend.dal.dao.TweetMentionDao;
 import toctep.skynet.backend.dal.dao.TweetUrlDao;
 import toctep.skynet.backend.dal.dao.UrlDao;
 import toctep.skynet.backend.dal.dao.UserDao;
 import toctep.skynet.backend.dal.dao.impl.mysql.DaoFacadeImpl;
 import toctep.skynet.backend.dal.dao.impl.mysql.MySqlUtil;
 
import com.mysql.jdbc.Connection;

 public abstract class DomainTest extends TestCase {
 
	private Connection conn;
 	private MySqlUtil mySqlUtil;
 	
 	private DaoFacade daoFacade;
 	
 	protected BoundingBoxDao boundingBoxDao;
 	protected BoundingBoxTypeDao boundingBoxTypeDao;
 	protected CountryDao countryDao;
 	protected GeoDao geoDao;
 	protected GeoTypeDao geoTypeDao;
 	protected HashtagDao hashtagDao;
 	protected LanguageDao languageDao;
 	protected PlaceDao placeDao;
 	protected PlaceTypeDao placeTypeDao;
 	protected SourceTypeDao sourceTypeDao;
 	protected TimeZoneDao timeZoneDao;
 	protected TweetContributorDao tweetContributorDao;
 	protected TweetHashtagDao tweetHashtagDao;
 	protected TweetDao tweetDao;
 	protected TweetMentionDao tweetMentionDao;
 	protected TweetUrlDao tweetUrlDao;
 	protected UrlDao urlDao;
 	protected UserDao userDao;
 	protected KeywordDao keywordDao;
 	protected TweetKeywordDao tweetKeywordDao;
 	
 	@Before
 	public void setUp() {
 		mySqlUtil = MySqlUtil.getInstance("mysql_test.properties");
		conn = mySqlUtil.getConnection();
 		
 		emptyDatabase();
 		
		daoFacade = new DaoFacadeImpl();
 		
 		boundingBoxDao = daoFacade.getBoundingBoxDao();
 		boundingBoxTypeDao = daoFacade.getBoundingBoxTypeDao();
 		countryDao = daoFacade.getCountryDao();
 		geoDao = daoFacade.getGeoDao();
 		geoTypeDao = daoFacade.getGeoTypeDao();
 		hashtagDao = daoFacade.getHashtagDao();
 		tweetUrlDao = daoFacade.getTweetUrlDao();
 		tweetMentionDao = daoFacade.getTweetMentionDao();
 		languageDao = daoFacade.getLanguageDao();
 		placeDao = daoFacade.getPlaceDao();
 		placeTypeDao = daoFacade.getPlaceTypeDao();
 		sourceTypeDao = daoFacade.getSourceTypeDao();
 		timeZoneDao = daoFacade.getTimeZoneDao();
 		tweetContributorDao = daoFacade.getTweetContributorDao();
 		tweetHashtagDao = daoFacade.getTweetHashtagDao();
 		tweetDao = daoFacade.getTweetDao();
 		tweetUrlDao = daoFacade.getTweetUrlDao();
 		urlDao = daoFacade.getUrlDao();
 		userDao = daoFacade.getUserDao();
 		keywordDao = daoFacade.getKeywordDao();
 		tweetKeywordDao = daoFacade.getTweetKeywordDao();
 	}
 
 	@After
 	public void tearDown() {
 		emptyDatabase();
 	}
 	
 	public void emptyDatabase() {
 		mySqlUtil.truncateDatabase();
 	}
 	
 	@Test
 	public abstract void testCreate();
 	
 	@Test
 	public abstract void testInsert();
 	
 	@Test
 	public abstract void testSelect();
 	
 	@Test
 	public abstract void testUpdate();
 	
 	@Test
 	public abstract void testDelete();
 	
 }
