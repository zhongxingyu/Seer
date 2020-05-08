 package de.shop.bestellverwaltung.rest;
 
 import static com.jayway.restassured.RestAssured.given;
 import static de.shop.util.TestConstants.ACCEPT;
 import static de.shop.util.TestConstants.ARTIKEL_URI;
 import static de.shop.util.TestConstants.BESTELLUNGEN_ID_KUNDE_PATH;
 import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH;
 import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH_PARAM;
 import static de.shop.util.TestConstants.BESTELLUNGEN_PATH;
 import static de.shop.util.TestConstants.KUNDEN_URI;
 import static de.shop.util.TestConstants.LOCATION;
 import static java.net.HttpURLConnection.HTTP_CREATED;
 import static java.net.HttpURLConnection.HTTP_OK;
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 import static org.hamcrest.CoreMatchers.endsWith;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.runners.MethodSorters.NAME_ASCENDING;
 
 import java.io.StringReader;
 import java.lang.invoke.MethodHandles;
 import java.util.logging.Logger;
 
 import javax.json.JsonObject;
 import javax.json.JsonReader;
 
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.FixMethodOrder;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.jayway.restassured.response.Response;
 
 import de.shop.util.AbstractResourceTest;
 
 
 @RunWith(Arquillian.class)
 @FixMethodOrder(NAME_ASCENDING)
 public class BestellungResourceTest extends AbstractResourceTest {
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 	
 	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(601);
 	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(204);
 	private static final Long ARTIKEL_ID_VORHANDEN_1 = Long.valueOf(502);
 	private static final Long ARTIKEL_ID_VORHANDEN_2 = Long.valueOf(503);
 
 	
 	@Test
 	public void findBestellungById() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
 		
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .pathParameter(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
 				                         .get(BESTELLUNGEN_ID_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_OK));
 		
 		try (final JsonReader jsonReader = 
 				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			final JsonObject jsonObject = jsonReader.readObject();
 			assertThat(jsonObject.getJsonNumber("id").longValue(), is(bestellungId.longValue()));
 			assertThat(jsonObject.getString("kundeUri"), is(notNullValue()));
 		}
 
 		LOGGER.finer("ENDE");
 	}
 
 	//@Test
 	public void findKundeByBestellungId() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
 		
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .pathParameter(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                          .get(BESTELLUNGEN_ID_KUNDE_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_OK));
 		
 		try (final JsonReader jsonReader =
 				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			final JsonObject jsonObject = jsonReader.readObject();
 			assertThat(jsonObject.getString("bestellungenUri"),
 					   endsWith("/kunden/" + jsonObject.getInt("id") + "/bestellungen"));
 		}
	
 		LOGGER.finer("ENDE");
 	}
 
 	//@Test
 	public void createBestellung() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_VORHANDEN;
 		final Long artikelId1 = ARTIKEL_ID_VORHANDEN_1;
 		final Long artikelId2 = ARTIKEL_ID_VORHANDEN_2;
 		final String username = USERNAME;
 		final String password = PASSWORD;
 		
 		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
 		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
 				                      .add("kundeUri", KUNDEN_URI + "/" + kundeId)
 				                      .add("bestellpositionen", getJsonBuilderFactory().createArrayBuilder()
 				            		                            .add(getJsonBuilderFactory().createObjectBuilder()
 				            		                                 .add("artikelUri", ARTIKEL_URI + "/" + artikelId1)
 				            		                                 .add("anzahl", 1))
 				            		                            .add(getJsonBuilderFactory().createObjectBuilder()
 				            		                                 .add("artikelUri", ARTIKEL_URI + "/" + artikelId2)
 				            		                                 .add("anzahl", 2)))
 				                      .build();
 
 		// When
 		final Response response = given().contentType(APPLICATION_JSON)
 				                         .body(jsonObject.toString())
 				                         .auth()
 				                         .basic(username, password)
 				                         .post(BESTELLUNGEN_PATH);
 		
 		assertThat(response.getStatusCode(), is(HTTP_CREATED));
 		final String location = response.getHeader(LOCATION);
 		final int startPos = location.lastIndexOf('/');
 		final String idStr = location.substring(startPos + 1);
 		final Long id = Long.valueOf(idStr);
 		assertThat(id.longValue() > 0, is(true));
 
 		LOGGER.finer("ENDE");
 	}
 }
