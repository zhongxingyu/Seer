 package org.xmx0632.deliciousfruit.api.v1;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.web.client.RestTemplate;
 import org.xmx0632.deliciousfruit.api.v1.bo.AllFruitProductRequest;
 import org.xmx0632.deliciousfruit.api.v1.bo.AllFruitProductResponse;
 import org.xmx0632.deliciousfruit.api.v1.bo.Result;
 import org.xmx0632.deliciousfruit.api.v1.bo.TerminalType;
 import org.xmx0632.deliciousfruit.functional.BaseControllerTestCase;
 
 public class FruitProductApiGetAllTest extends BaseControllerTestCase {
 
 	private final RestTemplate restTemplate = new RestTemplate();
 
 	private static String url;
 
 	@BeforeClass
 	public static void initUrl() {
 		url = baseUrl + "/fruitproduct/getAll";
 	}
 
 	@Test
 	public void testGetAllSuccess() throws Exception {
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password1");
 
 		AllFruitProductRequest allFruitProductRequest = new AllFruitProductRequest();
 		allFruitProductRequest.setTerminalType(TerminalType.IOS_RETINA.name());
 
 		HttpEntity<AllFruitProductRequest> entity = new HttpEntity<AllFruitProductRequest>(
 				allFruitProductRequest, requestHeaders);
 
 		AllFruitProductResponse response = restTemplate.postForObject(url,
 				entity, AllFruitProductResponse.class);
 
 		assertEquals(Result.SUCCESS, response.getResult().getValue());
		String expected = "["
				+ "FruitProductBo [productId=110101, productName=红苹果, spec=好红苹果, place=新疆, min=1, max=10, unit=个, keyword=红, expirationDate=2013-06-30 00:00:00+0800, e6Price=10.00, marketPrice=20.00, picUrl=http://localhost/ios_retina/fruit_product/product_1.jpg, introduction=好吃, description=很红, descriptionPicUrl=http://localhost/ios_retina/fruit_product/product_desc_1.jpg, fruitCategoryId=C000001, promotion=, seconddiscount=0.00, quantity=0],"
				+ " FruitProductBo [productId=110103, productName=烟台苹果, spec=最好的苹果, place=烟台, min=1, max=5, unit=个, keyword=烟台, expirationDate=2015-07-30 00:00:00+0800, e6Price=666.00, marketPrice=888.00, picUrl=http://localhost/ios_retina/fruit_product/product_3.jpg, introduction=无敌好吃, description=最好, descriptionPicUrl=http://localhost/ios_retina/fruit_product/product_desc_3.jpg, fruitCategoryId=C000001, promotion=, seconddiscount=0.00, quantity=0],"
				+ " FruitProductBo [productId=120101, productName=新疆葡萄, spec=好葡萄, place=新疆, min=1, max=5, unit=串, keyword=甜, expirationDate=2014-07-30 00:00:00+0800, e6Price=20.00, marketPrice=30.00, picUrl=http://localhost/ios_retina/fruit_product/product_4.jpg, introduction=很甜, description=不错, descriptionPicUrl=http://localhost/ios_retina/fruit_product/product_desc_4.jpg, fruitCategoryId=S001201, promotion=, seconddiscount=0.00, quantity=0],"
				+ " FruitProductBo [productId=110601, productName=红苹果, spec=箱, place=新疆, min=1, max=10, unit=个, keyword=红, expirationDate=2013-06-30 00:00:00+0800, e6Price=10.00, marketPrice=20.00, picUrl=http://localhost/ios_retina/fruit_product/product_1.jpg, introduction=好吃, description=很红, descriptionPicUrl=http://localhost/ios_retina/fruit_product/product_desc_1.jpg, fruitCategoryId=, promotion=买30个送烟台苹果1个, seconddiscount=0.00, quantity=0]]";
 		assertEquals(expected.toString(), response.getFruitProducts()
 				.toString());
 
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得所有水果产品, 成功", jsonMapper.toJson(allFruitProductRequest),
 				jsonMapper.toJson(response));
 
 	}
 
 	@Test
 	public void testFailNotValidTerminalType() throws Exception {
 		HttpHeaders requestHeaders = createHttpHeader("user2", "password");
 
 		AllFruitProductRequest allFruitProductRequest = new AllFruitProductRequest();
 		allFruitProductRequest.setTerminalType("ipad");
 
 		HttpEntity<AllFruitProductRequest> entity = new HttpEntity<AllFruitProductRequest>(
 				allFruitProductRequest, requestHeaders);
 
 		AllFruitProductResponse response = restTemplate.postForObject(url,
 				entity, AllFruitProductResponse.class);
 
 		assertEquals(Result.FAIL, response.getResult().getValue());
 		assertEquals(Result.MSG_ERR_NOT_VALID_TERMINAL_TYPE, response
 				.getResult().getMsg());
 
 		formatHttpInfoPrint(HttpMethod.POST, url, requestHeaders,
 				"获得所有水果产品, 失败. 原因: 无效的终端类型",
 				jsonMapper.toJson(allFruitProductRequest),
 				jsonMapper.toJson(response));
 	}
 
 }
