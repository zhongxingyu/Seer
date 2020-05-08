 package org.xmx0632.deliciousfruit.api.v1;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.RestTemplate;
 import org.springside.modules.mapper.JsonMapper;
 import org.xmx0632.deliciousfruit.api.v1.bo.RegisterRequest;
 import org.xmx0632.deliciousfruit.api.v1.bo.RegisterResponse;
 import org.xmx0632.deliciousfruit.functional.BaseControllerTestCase;
 
 public class UserAccountApiControllerRegisterRestTemplateTest extends
 		BaseControllerTestCase {
 
 	private final RestTemplate restTemplate = new RestTemplate();
 
 	private static String url;
 
 	@BeforeClass
 	public static void initUrl() {
 		url = baseUrl + "/register";
 	}
 
 	@Test
 	public void testRegisterSuccess() throws URISyntaxException {
 		RegisterRequest registerRequest = new RegisterRequest("aaaa@bbb.com",
 				"", "password", "");
 
 		URI uri = new URI(url);
 		ResponseEntity<RegisterResponse> result = restTemplate.postForEntity(
 				uri, registerRequest, RegisterResponse.class);
 		RegisterResponse response = result.getBody();
 
 		String expected = "RegisterResponse [result=Result [msg=, value=0], err=null]";
 		assertEquals(expected, response.toString());
 		formatHttpInfoPrint(HttpMethod.POST, url, null, "用户注册成功",
 				new JsonMapper().toJson(registerRequest),
 				jsonMapper.toJson(response));
 	}
 
 	@Test
 	public void testRegisterMobileSuccess() throws URISyntaxException {
 		RegisterRequest registerRequest = new RegisterRequest("",
 				"13611128888", "password", "");
 
 		URI uri = new URI(url);
 		ResponseEntity<RegisterResponse> result = restTemplate.postForEntity(
 				uri, registerRequest, RegisterResponse.class);
 		RegisterResponse response = result.getBody();
 
 		String expected = "RegisterResponse [result=Result [msg=, value=0], err=null]";
 		assertEquals(expected, response.toString());
 		formatHttpInfoPrint(HttpMethod.POST, url, null, "用户手机号注册成功",
 				new JsonMapper().toJson(registerRequest),
 				jsonMapper.toJson(response));
 	}
 
 	@Test
 	public void testRegisterFail_username_exists() throws URISyntaxException {
		RegisterRequest registerRequest = new RegisterRequest("aaa@bbb.com",
				"aaa@bbb.com", "password", "");
 
 		URI uri = new URI(url);
 		ResponseEntity<RegisterResponse> result = restTemplate.postForEntity(
 				uri, registerRequest, RegisterResponse.class);
 		RegisterResponse response = result.getBody();
 
 		String expected = "RegisterResponse [result=Result [msg=username exist, value=2], err=null]";
 		assertEquals(expected, response.toString());
 		formatHttpInfoPrint(HttpMethod.POST, url, null, "用户名已存在",
 				new JsonMapper().toJson(registerRequest),
 				jsonMapper.toJson(response));
 	}
 
 	@Test
 	public void testRegisterFail() throws URISyntaxException {
 
 		String password = null;
 		RegisterRequest registerRequest = new RegisterRequest(null, null,
 				password, "");
 
 		URI uri = new URI(url);
 		ResponseEntity<RegisterResponse> result = restTemplate.postForEntity(
 				uri, registerRequest, RegisterResponse.class);
 		RegisterResponse response = result.getBody();
 		String expected = "RegisterResponse [result=Result [msg=invalid property, value=1], err={username=不能为null}]";
 		assertEquals(expected, response.toString());
 
 		formatHttpInfoPrint(HttpMethod.POST, url, null, "用户注册失败",
 				new JsonMapper().toJson(registerRequest),
 				jsonMapper.toJson(response));
 	}
 }
