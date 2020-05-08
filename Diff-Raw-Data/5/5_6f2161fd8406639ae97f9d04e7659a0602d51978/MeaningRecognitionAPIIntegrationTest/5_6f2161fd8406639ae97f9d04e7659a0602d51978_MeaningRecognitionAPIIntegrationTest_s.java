 package com.springsense.disambig;
 
 import com.google.gson.Gson;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class MeaningRecognitionAPIIntegrationTest {
 
 	private MeaningRecognitionAPI api;
 
 	@Before
 	public void setUp() {
		api = new MeaningRecognitionAPI("http://prod.springsense.com:8080/disambiguate", "customer id", "api key", null, true);
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testConstructor() {
		assertEquals("http://prod.springsense.com:8080/disambiguate", api.getUrl());
 		assertEquals("customer id", api.getCustomerId());
 		assertEquals("api key", api.getApiKey());
 	}
 
 	@Test
 	public void testRecognize() throws Exception {
 		String textToRecognize = "black box";
 
 		final String expectedResponseJson = "[{\"terms\":[{\"term\":\"black box\",\"lemma\":\"black_box\",\"word\":\"black_box\",\"POS\":\"NN\",\"offset\":0,\"meanings\":[{\"definition\":\"equipment that records information about the performance of an aircraft during flight\",\"meaning\":\"black_box_n_01\"}]}],\"scores\":[1.0]}]";
 		DisambiguationResult expectedResult = DisambiguationResult.fromJson(expectedResponseJson);
 		final String expectedResultNormalized = new Gson().toJson(expectedResult);
 
 		DisambiguationResult result = api.recognize(textToRecognize);
 		final String resultNormalized = new Gson().toJson(result);
 
 		assertEquals(expectedResultNormalized, resultNormalized);
 	}
 
 }
