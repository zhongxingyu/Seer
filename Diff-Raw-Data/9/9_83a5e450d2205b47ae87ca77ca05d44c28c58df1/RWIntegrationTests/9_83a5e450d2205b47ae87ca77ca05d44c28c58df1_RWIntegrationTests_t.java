 package org.jboss.pressgangccms.test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.pressgangccms.rest.v1.entities.RESTBlobConstantV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTProjectV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTRoleV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTStringConstantV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgangccms.utils.common.MathUtilities;
 import org.jboss.pressgangccms.utils.common.StringUtilities;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
 import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
 import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
 import com.carrotsearch.junitbenchmarks.annotation.LabelType;
 import com.jayway.restassured.RestAssured;
 import com.jayway.restassured.path.json.JsonPath;
 import com.jayway.restassured.response.Response;
 
 import static com.jayway.restassured.RestAssured.*;
 import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  * A collection of tests designed to test Create, Read, Update and Delete REST methods. These tests should only be run on a development server, as the operations are
  * run multiple times by the JUnitBenchmark library (15 times by default), and these repeated modifications will lead to a lot of redundant information in the
  * Envers audit tables.
  * 
  * @author Matthew Casperson
  */
 @AxisRange(min = 0, max = 20)
 @BenchmarkHistoryChart(filePrefix = "rwbenchmarks", labelWith = LabelType.TIMESTAMP, maxRuns = 356)
 public class RWIntegrationTests extends AbstractBenchmark implements TestBase
 {
 	/** Jackson object mapper */
 	private final ObjectMapper mapper = new ObjectMapper();
 
 	/**
 	 * This method will run through a create, update and delete cycle on a entity.
 	 * 
 	 * @param entityName
 	 *            The name of the entity, as it appears in the REST paths
 	 * @param create
 	 *            The entity to be created
 	 * @param update
 	 *            The entity to be updated
 	 * @param setProperties
 	 *            The properties that were set on the create and update entities
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("rawtypes")
 	private void createUpdateDelete(final String entityName, final RESTBaseEntityV1 create, final RESTBaseEntityV1 update, final List<String> setProperties) throws JsonGenerationException, JsonMappingException, IOException
 	{
 		final String getURL = "/1/" + entityName + "/get/json";
 		final String createURL = "/1/" + entityName + "/post/json";
 		final String updateURL = "/1/" + entityName + "/put/json";
 		final String deleteURL = "/1/" + entityName + "/delete/json";
 
 		final String createJson = mapper.writeValueAsString(create);
 		final String updateJson = mapper.writeValueAsString(update);
 
 		final Map<String, String> env = System.getenv();
 		if (env.containsKey(RESTPASS) && env.containsKey(RESTUSER))
 		{
 			final String restUser = env.get(RESTUSER);
 			final String restPass = env.get(RESTPASS);
 
 			RestAssured.reset();
 			RestAssured.baseURI = "http://devrest-pressgangccms.rhcloud.com";
 			RestAssured.authentication = basic(restUser, restPass);
 			RestAssured.urlEncodingEnabled = true;
 			RestAssured.port = 80;
 
 			// ======== Attempt to create an entity ========
 			final Response createResponse = given().body(createJson).contentType(JSON_CONTENT_TYPE).post(createURL);
 
 			assertEquals(HTTP_OK, createResponse.getStatusCode());
 			assertEquals(JSON_CONTENT_TYPE, createResponse.getContentType());
 
 			final String createJsonResponse = createResponse.asString();
 			final JsonPath createJsonPath = new JsonPath(createJsonResponse);
 
 			/* All entities should have an ID */
 			assertNotNull(createJsonPath.get("id"));
 
 			/* Create a JsonPath from the supplied Json */
 			final JsonPath inputJsonPath = new JsonPath(createJson);
 
 			/* Make sure all the defined properties match */
 			for (final String property : setProperties)
 				assertEquals("Property \"" + property + "\" should be equal between the return value of the REST create and GET methods", inputJsonPath.get(property), createJsonPath.get(property));
 
 			/* We need the ID of the created entity in update and delete operations */
 			final int id = createJsonPath.getInt("id");
 
 			// ======== Attempt to get the created entity ========
 			final Response getCreateResponse = get(getURL + "/" + id);
 
 			assertEquals(HTTP_OK, getCreateResponse.getStatusCode());
 			assertEquals(JSON_CONTENT_TYPE, getCreateResponse.getContentType());
 
 			final String getCreateJsonResponse = getCreateResponse.asString();
 			final JsonPath getCreateJsonPath = new JsonPath(getCreateJsonResponse);
 
 			assertEquals(getCreateJsonPath.get("id"), createJsonPath.get("id"));
 			for (final String property : setProperties)
 				assertEquals(getCreateJsonPath.get(property), createJsonPath.get(property));
 
 			// ======== Attempt to update the entity ========
 			final String fixedUpdateJson = updateJson.replace(UPDATE_ID.toString(), id + "");
 
 			final Response updateResponse = given().body(fixedUpdateJson).contentType(JSON_CONTENT_TYPE).put(updateURL);
 
 			assertEquals(HTTP_OK, updateResponse.getStatusCode());
 			assertEquals(JSON_CONTENT_TYPE, updateResponse.getContentType());
 
 			final String updateJsonResponse = updateResponse.asString();
 			final JsonPath updateJsonPath = new JsonPath(updateJsonResponse);
 
 			assertNotNull(updateJsonPath.get("id"));
 			assertEquals(updateJsonPath.get("id"), createJsonPath.get("id"));
 
 			// ======== Attempt to get the updated entity ========
 			final Response getUpdateResponse = get(getURL + "/" + id);
 
 			assertEquals(HTTP_OK, getUpdateResponse.getStatusCode());
 			assertEquals(JSON_CONTENT_TYPE, getUpdateResponse.getContentType());
 
 			final String getUpdateJsonResponse = getUpdateResponse.asString();
 			final JsonPath getUpdateJsonPath = new JsonPath(getUpdateJsonResponse);
 
 			assertEquals(getUpdateJsonPath.get("id"), updateJsonPath.get("id"));
 			for (final String property : setProperties)
 				assertEquals(getUpdateJsonPath.get(property), updateJsonPath.get(property));
 
 			// ======== Attempt to delete the entity ========
 			final Response deleteResponse = delete(deleteURL + "/" + id);
 
 			assertEquals(HTTP_OK, deleteResponse.getStatusCode());
 			assertEquals(JSON_CONTENT_TYPE, deleteResponse.getContentType());
 
 			// ======== Attempt to get the deleted entity. This should fail. ========
 			final Response getResponse = get(getURL + "/" + id);
 
			assertEquals(HTTP_BAD_REQUEST, getResponse.getStatusCode());
 		}
 	}
 
 	@SuppressWarnings("serial")
 	@Test
 	public void crudBlobConstant()
 	{
 		final String entityName = "blobconstant";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("name");
 				add("value");
 			}
 		};
 
 		final RESTBlobConstantV1 createEntity = new RESTBlobConstantV1();
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetValue(StringUtilities.generateRandomString(10).getBytes());
 
 		final RESTBlobConstantV1 updateEntity = new RESTBlobConstantV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetValue(StringUtilities.generateRandomString(10).getBytes());
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudCategory()
 	{
 		final String entityName = "category";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("name");
 				add("description");
 				add("mutuallyExclusive");
 			}
 		};
 
 		final RESTCategoryV1 createEntity = new RESTCategoryV1();
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetMutuallyExclusive(MathUtilities.generateRandomBoolean());
 		
 		final RESTCategoryV1 updateEntity = new RESTCategoryV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetMutuallyExclusive(MathUtilities.generateRandomBoolean());
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudImage()
 	{
 		final String entityName = "image";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("description");
 			}
 		};
 
 		final RESTImageV1 createEntity = new RESTImageV1();
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		
 		final RESTImageV1 updateEntity = new RESTImageV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudProject()
 	{
 		final String entityName = "project";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("description");
 				add("name");
 			}
 		};
 
 		final RESTProjectV1 createEntity = new RESTProjectV1();
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		
 		final RESTProjectV1 updateEntity = new RESTProjectV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudPropertyTag()
 	{
 		final String entityName = "propertytag";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("description");
 				add("name");
 				add("isUnique");
 				add("canBeNull");
 				add("regex");
 				/* Don't set the value, because that only has meaning in the context of a parent entity like a Topic or Tag */
 			}
 		};
 
 		final RESTPropertyTagV1 createEntity = new RESTPropertyTagV1();
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetIsUnique(MathUtilities.generateRandomBoolean());
 		createEntity.explicitSetRegex(StringUtilities.generateRandomString(10));
 				
 		final RESTPropertyTagV1 updateEntity = new RESTPropertyTagV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetIsUnique(MathUtilities.generateRandomBoolean());
 		updateEntity.explicitSetRegex(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudRole()
 	{
 		final String entityName = "role";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("description");
 				add("name");
 			}
 		};
 
 		final RESTRoleV1 createEntity = new RESTRoleV1();
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		
 		final RESTRoleV1 updateEntity = new RESTRoleV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudStringConstant()
 	{
 		final String entityName = "stringconstant";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("value");
 				add("name");
 			}
 		};
 
 		final RESTStringConstantV1 createEntity = new RESTStringConstantV1();
 		createEntity.explicitSetValue(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		
 		final RESTStringConstantV1 updateEntity = new RESTStringConstantV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetValue(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 
 	@SuppressWarnings("serial")
 	@Test
 	public void crudTag()
 	{
 		final String entityName = "tag";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("name");
 				add("description");
 			}
 		};
 
 		final RESTTagV1 createEntity = new RESTTagV1();
 		createEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 
 		final RESTTagV1 updateEntity = new RESTTagV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetName(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 
 	@SuppressWarnings("serial")
 	@Test
 	public void crudTopic()
 	{
 		final String entityName = "topic";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("title");
 				add("description");
 				add("html");
 				add("locale");
 				add("xml");
 			}
 		};
 
 		final RESTTopicV1 createEntity = new RESTTopicV1();
 		createEntity.explicitSetTitle(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetHtml(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetLocale(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetXml(StringUtilities.generateRandomString(10));
 
 		final RESTTopicV1 updateEntity = new RESTTopicV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetTitle(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetDescription(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetHtml(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetLocale(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetXml(StringUtilities.generateRandomString(10));
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	@Test
 	public void crudTranslatedTopic()
 	{
 		final String entityName = "translatedtopic";
 		final List<String> properties = new ArrayList<String>()
 		{
 			{
 				add("html");
 				add("locale");
 				add("xml");
 				add("topicId");
 				add("topicRevision");
 				add("translationPercentage");
 				add("xmlErrors");
 				add("htmlUpdated");
 			}
 		};
 
 		final RESTTranslatedTopicV1 createEntity = new RESTTranslatedTopicV1();
 		createEntity.explicitSetHtml(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetLocale(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetXml(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetTopicId(MathUtilities.generateRandomInt(1000));
 		createEntity.explicitSetTopicRevision(MathUtilities.generateRandomInt(10000));
 		createEntity.explicitSetTranslationPercentage(MathUtilities.generateRandomInt(100));
 		createEntity.explicitSetXmlErrors(StringUtilities.generateRandomString(10));
 		createEntity.explicitSetHtmlUpdated(MathUtilities.generateRandomDate());
 
 		final RESTTranslatedTopicV1 updateEntity = new RESTTranslatedTopicV1();
 		updateEntity.setId(UPDATE_ID);
 		updateEntity.explicitSetHtml(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetLocale(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetXml(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetTopicId(MathUtilities.generateRandomInt(1000));
 		updateEntity.explicitSetTopicRevision(MathUtilities.generateRandomInt(10000));
 		updateEntity.explicitSetTranslationPercentage(MathUtilities.generateRandomInt(100));
 		updateEntity.explicitSetXmlErrors(StringUtilities.generateRandomString(10));
 		updateEntity.explicitSetHtmlUpdated(MathUtilities.generateRandomDate());
 
 		try
 		{
 			createUpdateDelete(entityName, createEntity, updateEntity, properties);
 		}
 		catch (final Exception ex)
 		{
 			fail();
 		}
 	}
 }
