 package com.bgg.farmstoryback.service;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.bgg.farmstoryback.common.ConstantsForDb;
 import com.bgg.farmstoryback.common.ConstantsForParam;
 import com.bgg.farmstoryback.common.ConstantsForResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:servlet-contextForTest.xml"})
 public class ContentsServiceTest {
 	
 	Logger logger = LoggerFactory.getLogger(getClass());
 	
 	@Autowired
 	ContentsService contentsService;
 	
 	@Autowired
 	BrandService brandService;
 	
 	@Autowired
 	SeriesService seriesService;
 	
 	String testJoinStartDate = "20130926";
 	String testJoinEndDate = "20130926";
 	
 	@SuppressWarnings({ "rawtypes"})
 	private Map requestParamMap=null;
 	
 
 	@Before
 	@SuppressWarnings({ "rawtypes" })
 	public void setUp() throws Exception {
 		requestParamMap = new HashMap();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testTopList() {
 		
 		// given 
 		int limitCount = 6;
 
 		// when
 		List<Map> topList = contentsService.top(limitCount); 
 
 		// then
 		assertThat(topList, is(notNullValue()));
 		assertTrue(topList.size() <= limitCount);
 
 	}
 	
 	@Test
 	@SuppressWarnings("rawtypes")
 	public void testList() {
 		
 		// given 
 		setDefaultPageParam();
 
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 
 		assertContentList(contentsList);
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testUnDisplayList() {
 		
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.DISPLAY_YN, "N");
 
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 
 		// then
 		for(Map contentsInfo : contentsList){
 			assertThat((String)contentsInfo.get(ConstantsForDb.DISPLAY_YN), is("N"));
 		}
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListByContentsName() {
 		
 		// given 
 		String testContentsName = "What";
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.SEARCH, testContentsName);
 		requestParamMap.put(ConstantsForParam.SEARCH_TYPE, "name");
 		
 
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 		
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 			String contentsName = (String)contentsInfo.get(ConstantsForDb.CONTENTS_NM);
 			assertTrue(contentsName.contains(testContentsName));
 			
 		}
 	}
 	
 //	@Test
 //	public void testSearchListByContentsId() {
 //		
 //		// given 
 //		int testContentsId = 699;
 //		setDefaultPageParam();
 //		requestParamMap.put(ConstantsForParam.SEARCH, testContentsId);
 //		requestParamMap.put(ConstantsForParam.SEARCH_TYPE, "id");
 //		
 //		
 //		// when
 //		List<Map> contentsList = contentsService.list(requestParamMap);
 //		
 //		assertContentList(contentsList);
 //		for(Map contentsInfo : contentsList){
 //			int contentsId= (Integer)contentsInfo.get(ConstantsForDb.CONTENTS_ID);
 //			assertTrue(contentsId == testContentsId);
 //			
 //		}
 //	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListByCategory() {
 		// given 
 		long testCategoryId = 32;
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.CATEGORY_ID, testCategoryId);
 		
 		
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 		
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 			long contentsId= (Long)contentsInfo.get(ConstantsForDb.CATEGORY_ID);
 			assertTrue(contentsId == testCategoryId);
 		}
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListByBrand() {
 		// given 
 		long testBrandID = 143;
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.BRAND_ID, testBrandID);
 		
 		
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 		
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 			long brandId= (Long)contentsInfo.get(ConstantsForDb.BRAND_ID);
 			assertTrue(brandId == testBrandID);
 		}
 		
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListBySeries() {
 		// given 
 		long testSeries = 33;
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.SERIES_ID, testSeries);
 		
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 		
 		// then
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 			long seriesId= (Long)contentsInfo.get(ConstantsForDb.SERIES_ID);
 			assertTrue(seriesId == testSeries);
 		}
 		
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListByRegDt() {
 		// given 
 		setDefaultPageParam();
 		requestParamMap.put(ConstantsForParam.SEARCH_START_DT, testJoinStartDate);
 		requestParamMap.put(ConstantsForParam.SEARCH_END_DT, testJoinEndDate);
 		
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 		
 		// then
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 			String memberRegDt =(String)contentsInfo.get(ConstantsForDb.REG_DT);
 			if(memberRegDt.replaceAll("-", "").contains(testJoinStartDate)
 					|| memberRegDt.replaceAll("-", "").contains(testJoinEndDate)){
 				assertTrue(true);
 			}else{
 				assertTrue(false);
 			}
 		}
 		
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testSearchListByDisplayYn() {
 		// given 
 		setDefaultPageParam();
 		String testDisplayYn = "Y";
 		requestParamMap.put(ConstantsForParam.DISPLAY_YN, testDisplayYn);
 
 		// when
 		List<Map> contentsList = contentsService.list(requestParamMap);
 
 		// then
 		assertContentList(contentsList);
 		for(Map contentsInfo : contentsList){
 				assertTrue(contentsInfo.get(ConstantsForDb.DISPLAY_YN).equals(testDisplayYn));
 		}
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testAddContentsJustContentsTable() {
 		
 		
 		// given
 		// root map
 		Map contentsInfo  = new HashMap();
 		
 		// set root info
 		List<Map> publisherList = brandService.listAll();
 		Map publisherInfo = publisherList.get(0);
 		contentsInfo.put(ConstantsForParam.BRAND_ID, publisherInfo.get(ConstantsForDb.BRAND_ID));
 		
 		List<Map> seriesList =  seriesService.list(requestParamMap);
 		Map seriesInfo = seriesList.get(0);
 		contentsInfo.put(ConstantsForParam.SERIES_ID, seriesInfo.get(ConstantsForDb.SERIES_ID));
 		
 		String moviePath = "temp/temp.mp4";
 		String imgPath = "temp/temp.gif";
 		int publisherId = 137;
 		int seriesId = 48;
 		contentsInfo.put(ConstantsForParam.MOVIE_PATH, moviePath);
 		contentsInfo.put(ConstantsForParam.IMG_PATH, imgPath);
 		contentsInfo.put(ConstantsForParam.BRAND_ID, publisherId);
 		contentsInfo.put(ConstantsForParam.SERIES_ID, seriesId);
 		
 		// root > contentsDetail
 		List<Map> contenstDetailList = new ArrayList<Map>();
 		
 		
 			// set root > contentsDetail info(test-data 1)
 			Map contentsDetail01 = new HashMap();
 			String contentsName01 = "addContentsName01";
 			String contentsDesc01 = "addContentsDesc01";
 			String locationCode01 = "kr";
 			contentsDetail01.put(ConstantsForParam.DISPLAY_YN, "N");
 			contentsDetail01.put(ConstantsForParam.CONTENTS_NAME, contentsName01);
 			contentsDetail01.put(ConstantsForParam.CONTENTS_DESC, contentsDesc01);
 			contentsDetail01.put(ConstantsForParam.LOCATION_CODE, locationCode01);
 			
 				// set root > contentsDetail > Caegory info(test-data 1)
 				List<Map> contentsCategory01 = new ArrayList<Map>();
 				Map category01 = new HashMap();
 				category01.put(ConstantsForParam.CATEGORY_ID, 32);
 				Map category02 = new HashMap();
 				category02.put(ConstantsForParam.CATEGORY_ID, 38);
 				contentsCategory01.add(category01);
 				contentsCategory01.add(category02);
 				contentsDetail01.put(ConstantsForParam.CATEGORY_LIST, contentsCategory01);
 		
 		
 			// set root > contentsDetail info(test-data 2)
 			Map contentsDetail02 = new HashMap();
 			String contentsName02 = "addContentsName02";
 			String contentsDesc02 = "addContentsDesc02";
 			String locationCode02 = "us";
 			contentsDetail02.put(ConstantsForParam.DISPLAY_YN, "N");
 			contentsDetail02.put(ConstantsForParam.CONTENTS_NAME, contentsName02);
 			contentsDetail02.put(ConstantsForParam.CONTENTS_DESC, contentsDesc02);
 			contentsDetail02.put(ConstantsForParam.LOCATION_CODE, locationCode02);
 			
 				// set root > contentsDetail > Caegory info(test-data 2)
 				List<Map> contentsCategory02 = new ArrayList<Map>();
 				Map category03 = new HashMap();
 				category03.put(ConstantsForParam.CATEGORY_ID, 54);
 				Map category04 = new HashMap();
 				category04.put(ConstantsForParam.CATEGORY_ID, 68);
 				contentsCategory02.add(category03);
 				contentsCategory02.add(category04);
 				contentsDetail02.put(ConstantsForParam.CATEGORY_LIST, contentsCategory02);
 		
 		contenstDetailList.add(contentsDetail01);
 		contenstDetailList.add(contentsDetail02);
 		
 		contentsInfo.put(ConstantsForParam.CONTENTS_DETAIL_LIST, contenstDetailList);
 		
 		// when
 		contentsService.addContents(contentsInfo);
 
 		// then
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testModifyContents() {
 		
 		// given
 		// root map
 		Map contentsInfo  = new HashMap();
 		
 		// set root info
 		List<Map> publisherList = brandService.listAll();
 		Map publisherInfo = publisherList.get(0);
 		contentsInfo.put(ConstantsForParam.BRAND_ID, publisherInfo.get(ConstantsForDb.BRAND_ID));
 		
 		List<Map> seriesList =  seriesService.list(requestParamMap);
 		Map seriesInfo = seriesList.get(0);
 		contentsInfo.put(ConstantsForParam.SERIES_ID, seriesInfo.get(ConstantsForDb.SERIES_ID));
 		
 		String moviePath = "modifyTemp/temp.mp4";
 		String imgPath = "modifyTemp/temp.gif";
 		int publisherId = 139;
 		int seriesId = 51;
 		int contentsId = 1338;
 		contentsInfo.put(ConstantsForParam.MOVIE_PATH, moviePath);
 		contentsInfo.put(ConstantsForParam.IMG_PATH, imgPath);
 		contentsInfo.put(ConstantsForParam.BRAND_ID, publisherId);
 		contentsInfo.put(ConstantsForParam.SERIES_ID, seriesId);
 		contentsInfo.put(ConstantsForParam.CONTENTS_ID, contentsId);
 		
 		// root > contentsDetail
 		List<Map> contenstDetailList = new ArrayList<Map>();
 		
 		
 			// set root > contentsDetail info(test-data 1)
 			Map contentsDetail01 = new HashMap();
 			String contentsName01 = "modifyContentsName01";
 			String contentsDesc01 = "modifyContentsDesc01";
 			String locationCode01 = "us";
 			int contentsDetailIdx01 = 344;
 			contentsDetail01.put(ConstantsForParam.DISPLAY_YN, "N");
 			contentsDetail01.put(ConstantsForParam.CONTENTS_NAME, contentsName01);
 			contentsDetail01.put(ConstantsForParam.CONTENTS_DESC, contentsDesc01);
 			contentsDetail01.put(ConstantsForParam.LOCATION_CODE, locationCode01);
 			contentsDetail01.put(ConstantsForParam.CONTENTS_DETAIL_IDX, contentsDetailIdx01);
 			
 				// set root > contentsDetail > Caegory info(test-data 1)
 				List<Map> contentsCategory01 = new ArrayList<Map>();
 				Map category01 = new HashMap();
 				category01.put(ConstantsForParam.CATEGORY_ID, 54);
 				Map category02 = new HashMap();
 				category02.put(ConstantsForParam.CATEGORY_ID, 68);
 				contentsCategory01.add(category01);
 				contentsCategory01.add(category02);
 				contentsDetail01.put(ConstantsForParam.CATEGORY_LIST, contentsCategory01);
 		
 		
 			// set root > contentsDetail info(test-data 2)
 			Map contentsDetail02 = new HashMap();
 			String contentsName02 = "modifyContentsName02";
 			String contentsDesc02 = "modifyContentsDesc02";
 			String locationCode02 = "kr";
 			int contentsDetailIdx02 = 343;
 			contentsDetail02.put(ConstantsForParam.DISPLAY_YN, "N");
 			contentsDetail02.put(ConstantsForParam.CONTENTS_NAME, contentsName02);
 			contentsDetail02.put(ConstantsForParam.CONTENTS_DESC, contentsDesc02);
 			contentsDetail02.put(ConstantsForParam.LOCATION_CODE, locationCode02);
 			contentsDetail02.put(ConstantsForParam.CONTENTS_DETAIL_IDX, contentsDetailIdx02);
 			
 				// set root > contentsDetail > Caegory info(test-data 2)
 				List<Map> contentsCategory02 = new ArrayList<Map>();
 				Map category03 = new HashMap();
 				category03.put(ConstantsForParam.CATEGORY_ID, 32);
 				Map category04 = new HashMap();
 				category04.put(ConstantsForParam.CATEGORY_ID, 38);
 				contentsCategory02.add(category03);
 				contentsCategory02.add(category04);
 				contentsDetail02.put(ConstantsForParam.CATEGORY_LIST, contentsCategory02);
 		
 		contenstDetailList.add(contentsDetail01);
 		contenstDetailList.add(contentsDetail02);
 		
 		contentsInfo.put(ConstantsForParam.CONTENTS_DETAIL_LIST, contenstDetailList);
 		
 		// when
 		contentsService.modifyContents(contentsInfo);
 		
 		// then
 
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked" })
 	public void testDeleteContents() {
 		
 		// given 
 		int contentsId = 1338;
 		requestParamMap.put(ConstantsForParam.CONTENTS_ID, contentsId);
 
 		// when
 		contentsService.delete(requestParamMap);
 
 		// then
 
 	}
 	
 	@Test
 	public void testContentsDetail() {
 		
 		// given 
 		int contentsId = 1339;
 		requestParamMap.put(ConstantsForParam.CONTENTS_ID, contentsId);
 
 		// when
 		Map contentsDetailInfo = contentsService.detail(requestParamMap);
 		
 		
 		Map contentsInfo  = (Map)contentsDetailInfo.get(ConstantsForResponse.CONTENTS_INFO);
 		List<Map> contentsDetailList = (List<Map>)contentsDetailInfo.get(ConstantsForResponse.CONTENTS_DETAIL_LIST);
 		List<Map> contentsCateList = (List<Map>)contentsDetailList.get(0).get(ConstantsForResponse.CONTENTS_CATE_LIST);
 
 		// then
 		assertThat(contentsDetailInfo, is(notNullValue()));
 		assertThat(contentsInfo, is(notNullValue()));
 		assertThat(contentsDetailList, is(notNullValue()));
 		assertThat(contentsCateList, is(notNullValue()));
 		assertThat(contentsDetailList.size(), is(not(0)));
 		assertThat(contentsCateList.size(), is(not(0)));
 		
 		logger.info("{}", contentsInfo);
 		logger.info("{}", contentsDetailList);
 
 	}
 	
 	@Test
 	public void testTotalCoutn() {
 		
 		// given 
 		String contenstName = "안녕";
 		requestParamMap.put(ConstantsForParam.CONTENTS_NAME, contenstName);
 				
 		
 		// when
		int contentsTotalCountByName = contentsService.totalCount(requestParamMap);
		
		requestParamMap.put(ConstantsForParam.CONTENTS_NAME, null);
		int contentsTotalCountAll = contentsService.totalCount(requestParamMap);
 
 		// then
		assertThat(contentsTotalCountByName, is(not(0)));
		assertThat(contentsTotalCountAll, is(not(0)));
 
 	}
 
 	@SuppressWarnings({ "rawtypes", })
 	private void assertContentList(List<Map> contentsList) {
 		assertThat(contentsList, is(notNullValue()));
 		assertThat(contentsList.size(), is(not(0)));
 	}
 	
 	
 	@SuppressWarnings({ "unchecked" })
 	private void setDefaultPageParam() {
 		requestParamMap.put(ConstantsForParam.PAGE_PER_PAGE, 10);
 		requestParamMap.put(ConstantsForParam.PAGE_START_NO, 1);
 	}
 	
 	
 }
