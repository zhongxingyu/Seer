 package org.xmx0632.deliciousfruit.api.v1;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.web.client.RestTemplate;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryBo;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryMaterialBo;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryMenuBo;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryProcedureBo;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryRequest;
 import org.xmx0632.deliciousfruit.api.v1.bo.FruitStoryResponse;
 import org.xmx0632.deliciousfruit.api.v1.bo.Result;
 import org.xmx0632.deliciousfruit.api.v1.bo.TerminalType;
 import org.xmx0632.deliciousfruit.functional.BaseControllerTestCase;
 
 public class FruitSroryApiControllerGetOneByIdTest extends
 		BaseControllerTestCase {
 
 	private final RestTemplate restTemplate = new RestTemplate();
 
 	private static String url;
 
 	@BeforeClass
 	public static void initUrl() {
 		url = baseUrl + "/fruitstory/getbyid";
 	}
 
 	@Test
 	public void testGetOneByIdSuccess() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("1");
 		fruitStoryRequest.setTerminalType(TerminalType.IOS_NORMAL.name());
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("0", response.getResult().getValue());
 		assertEquals(genSucessResFruitStory().toString(), response
 				.getFruitStory().toString());
 
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,成功", jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNoId() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("");
 		fruitStoryRequest.setTerminalType(TerminalType.IOS_NORMAL.name());
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("1", response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_NO_ID, response.getResult().getMsg());
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,失败，原因:没有水果故事ID",
 				jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNotValidId() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("aaa");
 		fruitStoryRequest.setTerminalType(TerminalType.IOS_NORMAL.name());
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("1", response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_NOT_VALID_ID, response.getResult().getMsg());
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,失败，原因:无效的水果故事ID",
 				jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNotValidTerminalType() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("1");
 		fruitStoryRequest.setTerminalType("ios");
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("1", response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_NOT_VALID_TERMINAL_TYPE, response
 				.getResult().getMsg());
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,失败，原因:无效的终端类型", jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNotExist() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("6");
 		fruitStoryRequest.setTerminalType(TerminalType.IOS_RETINA.name());
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("1", response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_NOT_EXIST, response.getResult().getMsg());
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,失败，原因:水果故事不存在", jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNotOnline() throws Exception {
 
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 		FruitStoryRequest fruitStoryRequest = new FruitStoryRequest();
 		fruitStoryRequest.setId("2");
 		fruitStoryRequest.setTerminalType(TerminalType.IOS_RETINA.name());
 
 		HttpEntity<FruitStoryRequest> entity = new HttpEntity<FruitStoryRequest>(
 				fruitStoryRequest, requestHeaders);
 
 		FruitStoryResponse response = restTemplate.postForObject(url, entity,
 				FruitStoryResponse.class);
 
 		assertEquals("1", response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_STORY_NOT_ONLINE, response.getResult()
 				.getMsg());
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得单个水果故事,失败，原因:水果故事不是上线状态",
 				jsonMapper.toJson(fruitStoryRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	private FruitStoryBo genSucessResFruitStory() {
 		FruitStoryBo fruitStoryBo = new FruitStoryBo(Long.valueOf("1"),
 				"水果故事1", "这是一个上线水果故事", pictureServerRootUrl
						+ "/ios_normal/fruit_story/1.jpg");
 
 		FruitStoryMenuBo fruitStoryMenu = new FruitStoryMenuBo("10001",
 				"第一个果谱,味道鲜美,清爽可口,实在是居家旅行杀人越货的必备良品客官快来尝尝.", pictureServerRootUrl
						+ "/ios_normal/fruit_menu/1.jpg");
 
 		fruitStoryBo.setFruitStoryMenu(fruitStoryMenu);
 		fruitStoryMenu.getMaterials().add(new FruitStoryMaterialBo("苹果", "2个"));
 		fruitStoryMenu.getMaterials()
 				.add(new FruitStoryMaterialBo("猕猴桃", "3个"));
 		fruitStoryMenu.getProcedures().add(
 				new FruitStoryProcedureBo("苹果去皮，切块", pictureServerRootUrl
 						+ "/ios_normal/fruit_procedure/1.jpg"));
 		fruitStoryMenu.getProcedures().add(
 				new FruitStoryProcedureBo("猕猴桃去皮，榨汁", pictureServerRootUrl
 						+ "/ios_normal/fruit_procedure/2.jpg"));
 		return fruitStoryBo;
 	}
 }
