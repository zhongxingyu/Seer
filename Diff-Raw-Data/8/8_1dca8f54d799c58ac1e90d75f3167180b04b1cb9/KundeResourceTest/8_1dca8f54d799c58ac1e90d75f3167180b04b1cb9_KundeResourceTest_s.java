 package de.shop.kundenverwaltung.rest;
 
 import static com.jayway.restassured.RestAssured.given;
 import static de.shop.util.TestConstants.ACCEPT;
 import static de.shop.util.TestConstants.BASEPATH;
 import static de.shop.util.TestConstants.BASEURI;
 import static de.shop.util.TestConstants.KUNDEN_ID_FILE_PATH;
 import static de.shop.util.TestConstants.KUNDEN_ID_PATH_PARAM;
 import static de.shop.util.TestConstants.KUNDEN_ID_PATH;
 import static de.shop.util.TestConstants.KUNDEN_NACHNAME_QUERY_PARAM;
 import static de.shop.util.TestConstants.KUNDEN_PATH;
 import static de.shop.util.TestConstants.LOCATION;
 import static de.shop.util.TestConstants.PORT;
 import static java.net.HttpURLConnection.HTTP_CONFLICT;
 import static java.net.HttpURLConnection.HTTP_CREATED;
 import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
 import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
 import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
 import static java.net.HttpURLConnection.HTTP_OK;
 import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
 import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 import static org.hamcrest.CoreMatchers.anyOf;
 import static org.hamcrest.CoreMatchers.endsWith;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.startsWith;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.junit.runners.MethodSorters.NAME_ASCENDING;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.lang.invoke.MethodHandles;
 import java.nio.file.CopyOption;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.json.JsonArray;
 import javax.json.JsonObject;
 import javax.json.JsonObjectBuilder;
 import javax.json.JsonReader;
 import javax.xml.bind.DatatypeConverter;
 
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.FixMethodOrder;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.jayway.restassured.response.Response;
 
 import de.shop.kundenverwaltung.domain.Adresse;
 import de.shop.kundenverwaltung.domain.KundeGeschlechtType;
 import de.shop.util.AbstractResourceTest;
 
 
 @RunWith(Arquillian.class)
 @FixMethodOrder(NAME_ASCENDING)
 public class KundeResourceTest extends AbstractResourceTest {
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 	
 	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(600);
 	private static final Long KUNDE_ID_NICHT_VORHANDEN = Long.valueOf(599);
 	private static final Long KUNDE_ID_UPDATE = Long.valueOf(1000);
 	private static final Long KUNDE_ID_DELETE = Long.valueOf(600);
 	private static final Long KUNDE_ID_DELETE_MIT_BESTELLUNGEN = Long.valueOf(601);
 	private static final Long KUNDE_ID_DELETE_FORBIDDEN = Long.valueOf(602);
 	private static final String NACHNAME_VORHANDEN = "Pfeiffer";
 	private static final String NACHNAME_NICHT_VORHANDEN = "Falschername";
 	private static final String NEUER_NACHNAME = "Feurer";
 	private static final String NEUER_NACHNAME_INVALID = "!";
 	private static final String NEUER_VORNAME = "Christian";
 	private static final String NEUE_EMAIL = NEUER_NACHNAME + "@test.de";
 	private static final String NEUE_EMAIL_INVALID = "falsch@falsch";
 	private static final String NEU_GEBURT ="1999-02-02";
 	private static final String NEU_TELEFON = "0721 123456";
 	private static final String NEU_GESCHLECHT ="M";
 	
 	private static final String USERNAME ="600";
	private static final String PASSWORD ="Test012X3";
 	
 	private static final Long ADRESSE_ID_VORHANDEN = Long.valueOf(103);
 	private static final String STRASSE_NEU = "Karlsstrae 30";
 	private static final String PLZ_NEU = "76133";
 	private static final String ORT_NEU = "Karlsruhe";
 	private static final String LAND_NEU = "DE";
 	private static final String STRASSE_UPDATE = "Moltkestrae 333";
 	
 	private static final Long ZAHLUNGSINFORMATION_ID_VORHANDEN = Long.valueOf(700);
 	private static final Long ZAHLUNGSINFORMATION_ID_NICHT_VORHANDEN = Long.valueOf(2333);
 	private static final String KONTOINHABER_NEU = "Morgan Freeman";
 	private static final Long KONTONUMMER_NEU = Long.valueOf(9191221);
 	private static final Long BLZ_NEU = Long.valueOf(66650081);
 	private static final String KREDITINSTITUT_NEU = "Sparkasse Chemnitz";
 	private static final String IBAN_NEU ="DE54666500859995054284";
 	private static final String SWIFT_NEU ="PZHSDE66YYY";
 	private static final String KONTOINHABER_UPDATE = "Dennis Santos";
 	
 	private static final String FILENAME = "image.gif";
 	//private static final String FILENAME = "video.mp4";
 	private static final String FILENAME_UPLOAD = "src/test/resources/rest/" + FILENAME;
 	private static final String FILENAME_DOWNLOAD = "target/" + FILENAME;
 	private static final CopyOption[] COPY_OPTIONS = { REPLACE_EXISTING };
 	private static final Long KUNDE_ID_UPLOAD = Long.valueOf(102);
 
 	private static final String FILENAME_INVALID_MIMETYPE = "image.bmp";
 	private static final String FILENAME_UPLOAD_INVALID_MIMETYPE = "src/test/resources/rest/" + FILENAME_INVALID_MIMETYPE;
 	
 	
 	@Test
 	public void validate() {
 		assertThat(true, is(true));
 	}
 	
 	@Ignore
 	@Test
 	public void notYetImplemented() {
 		fail();
 	}
 	
 	@Test
 	public void findKundeById() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_VORHANDEN;
 		
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                          .get(KUNDEN_ID_PATH);
 
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_OK));
 		
 		try (final JsonReader jsonReader =
 				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			final JsonObject jsonObject = jsonReader.readObject();
 			assertThat(jsonObject.getJsonNumber("kundeId").longValue(), is(kundeId.longValue()));
 		}
 		
 		LOGGER.finer("ENDE");
 	}
 	
 	@Test
 	public void findKundeByIdNichtVorhanden() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_NICHT_VORHANDEN;
 		
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                          .get(KUNDEN_ID_PATH);
 
     	// Then
     	assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
 		LOGGER.finer("ENDE");
 	}
 	
 	@Test
 	public void findKundenByNachnameVorhanden() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String nachname = NACHNAME_VORHANDEN;
 
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .queryParam(KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                          .get(KUNDEN_PATH);
 		
 		// Then
 		try (final JsonReader jsonReader =
 				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			final JsonArray jsonArray = jsonReader.readArray();
 	    	assertThat(jsonArray.size() > 0, is(true));
 	    	
 	    	final List<JsonObject> jsonObjectList = jsonArray.getValuesAs(JsonObject.class);
 	    	for (JsonObject jsonObject : jsonObjectList) {
 	    		assertThat(jsonObject.getString("nachname"), is(nachname));
 	    	}
 		}
 
 		LOGGER.finer("ENDE");
 	}
 	
 	@Test
 	public void findKundenByNachnameNichtVorhanden() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String nachname = NACHNAME_NICHT_VORHANDEN;
 		
 		// When
 		final Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                         .queryParam(KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                          .get(KUNDEN_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
 
 		LOGGER.finer("ENDE");
 	}
 	
 
 	@Test
 	public void createKunde() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String nachname = NEUER_NACHNAME;
 		final String vorname = NEUER_VORNAME;
 		final String email = NEUE_EMAIL;
 		final String geschlecht = NEU_GESCHLECHT;
 		final String username = USERNAME;
		final String password = PASSWORD;
 		final String telefon = NEU_TELEFON;
 		final String geburtsdatum = NEU_GEBURT;
 		
 		final String kontoinhaber = KONTOINHABER_NEU;
 		final Long kontonummer = KONTONUMMER_NEU;
 		final Long blz = BLZ_NEU;
 		final String kreditinstitut = KREDITINSTITUT_NEU;
 		final String iban = IBAN_NEU;
 		final String swift = SWIFT_NEU;
 		
 		final String strasse = STRASSE_NEU;
 		final String plz = PLZ_NEU;
 		final String ort = ORT_NEU;
 		final String land = LAND_NEU;
 		
 		
 		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
 				 					  .add("vorname", vorname)
 		             		          .add("nachname", nachname)
 		             		          .add("email", email)
 		             		          .add("geschlecht", geschlecht)
		             		          .add("password", password)
 		             		          .add("telefonnummer",telefon)
 		             		          .add("lieferadresse",getJsonBuilderFactory().createObjectBuilder()
 		             				          .add("strasse", strasse)
 		             				          .add("plz", plz)
 		             				          .add("ort", ort)
 		             				          .add("land", land)
 		             				          .build())
 		             				  .add("rechnungsadresse", getJsonBuilderFactory().createObjectBuilder()
 									          .add("strasse", strasse)
 									          .add("plz", plz)
 									          .add("ort", ort)
 									          .add("land", land)
 									          .build())
 									  .add("zahlungsinformation", getJsonBuilderFactory().createObjectBuilder()
 				             		          .add("kontoinhaber", kontoinhaber)
 				             		          .add("kontonummer", kontonummer)
 				             		          .add("blz", blz)
 				             		          .add("kreditinstitut", kreditinstitut)
 				             		          .add("iban", iban)
 				             		          .add("swift", swift)
 				             		          .build())
 				             		  .add("geburtsdatum", geburtsdatum)
 		                              .build();
 
 		// When
 		final Response response = given().contentType(APPLICATION_JSON)
 				                         .body(jsonObject.toString())
                                          //.auth()
                                          //.basic(username, password)
                                          .post(KUNDEN_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_CREATED));
 		final String location = response.getHeader(LOCATION);
 		final int startPos = location.lastIndexOf('/');
 		final String idStr = location.substring(startPos + 1);
 		final Long id = Long.valueOf(idStr);
 		assertThat(id.longValue() > 0, is(true));
 
 		LOGGER.finer("ENDE");
 	}
 	
 	@Ignore
 	@Test
 	public void createKundeFalschesPassword() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String username = USERNAME;
 		final String password = PASSWORD_FALSCH;
 		final String nachname = NEUER_NACHNAME;
 		
 		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
             		                  .add("nachname", nachname)
             		                  .build();
 		
 		// When
 		final Response response = given().contentType(APPLICATION_JSON)
 				                         .body(jsonObject.toString())
                                          .auth()
                                          .basic(username, password)
                                          .post(KUNDEN_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_UNAUTHORIZED));
 		
 		LOGGER.finer("ENDE");
 	}
 	@Ignore
 	@Test
 	public void createKundeInvalid() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String nachname = NEUER_NACHNAME_INVALID;
 		final String vorname = NEUER_VORNAME;
 		final String email = NEUE_EMAIL_INVALID;
 		final boolean agbAkzeptiert = false;
 		final String username = USERNAME;
 		final String password = PASSWORD;
 
 		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
    		                              .add("nachname", nachname)
    		                              .add("vorname", vorname)
    		                              .add("email", email)
    		                             // .add("seit", seit)
    		                              .add("agbAkzeptiert", agbAkzeptiert)
    		                              .addNull("adresse")
    		                              .build();
 
 		// When
 		final Response response = given().contentType(APPLICATION_JSON)
 				                         .body(jsonObject.toString())
                                          //.auth()
                                          //.basic(username, password)
                                          .post(KUNDEN_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
 		// TODO einzelne Meldungen durch Bean Validation ueberpruefen
 		assertThat(response.asString().isEmpty(), is(false));
 		
 		LOGGER.finer("ENDE");
 	}
 	@Ignore
 	@Test
 	public void updateKunde() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_UPDATE;
 		final String neuerNachname = NEUER_NACHNAME;
 		final String username = USERNAME;
 		final String password = PASSWORD;
 		
 		// When
 		Response response = given().header(ACCEPT, APPLICATION_JSON)
 				                   .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                    .get(KUNDEN_ID_PATH);
 		
 		JsonObject jsonObject;
 		try (final JsonReader jsonReader =
 				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			jsonObject = jsonReader.readObject();
 		}
     	assertThat(jsonObject.getJsonNumber("id").longValue(), is(kundeId.longValue()));
     	
     	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
     	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
     	final Set<String> keys = jsonObject.keySet();
     	for (String k : keys) {
     		if ("nachname".equals(k)) {
     			job.add("nachname", neuerNachname);
     		}
     		else {
     			job.add(k, jsonObject.get(k));
     		}
     	}
     	jsonObject = job.build();
     	
 		response = given().contentType(APPLICATION_JSON)
 				          .body(jsonObject.toString())
                           .auth()
                           .basic(username, password)
                           .put(KUNDEN_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
    	}
 	@Ignore
 	@Test
 	public void deleteKunde() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_DELETE;
 		final String username = USERNAME_ADMIN;
 		final String password = PASSWORD_ADMIN;
 		
 		// When
 		final Response response = given().auth()
                                          .basic(username, password)
                                          .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                          .delete(KUNDEN_ID_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
 		LOGGER.finer("ENDE");
 	}
 	@Ignore
 	@Test
 	public void deleteKundeMitBestellung() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_DELETE_MIT_BESTELLUNGEN;
 		final String username = USERNAME_ADMIN;
 		final String password = PASSWORD_ADMIN;
 		
 		// When
 		final Response response = given().auth()
                                          .basic(username, password)
                                          .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                          .delete(KUNDEN_ID_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
 		final String errorMsg = response.asString();
 		assertThat(errorMsg, startsWith("Kunde mit ID=" + kundeId + " kann nicht geloescht werden:"));
 		assertThat(errorMsg, endsWith("Bestellung(en)"));
 
 		LOGGER.finer("ENDE");
 	}
 	
 	@Ignore
 	@Test
 	public void deleteKundeFehlendeBerechtigung() {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final String username = USERNAME;
 		final String password = PASSWORD;
 		final Long kundeId = KUNDE_ID_DELETE_FORBIDDEN;
 		
 		// When
 		final Response response = given().auth()
                                          .basic(username, password)
                                          .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                          .delete(KUNDEN_ID_PATH);
 		
 		// Then
 		assertThat(response.getStatusCode(), anyOf(is(HTTP_FORBIDDEN), is(HTTP_NOT_FOUND)));
 		
 		LOGGER.finer("ENDE");
 	}
 	@Ignore
 	@Test
 	public void uploadDownload() throws IOException {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_UPLOAD;
 		final String fileName = FILENAME_UPLOAD;
 		final String username = USERNAME;
 		final String password = PASSWORD;
 		
 		// Datei als byte[] einlesen
 		byte[] bytes;
 		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
 			Files.copy(Paths.get(fileName), stream);
 			bytes = stream.toByteArray();
 		}
 		
 		// byte[] als Inhalt eines JSON-Datensatzes mit Base64-Codierung
 		JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
 	                            .add("bytes", DatatypeConverter.printBase64Binary(bytes))
 	                            .build();
 		
 		// When
 		Response response = given().contentType(APPLICATION_JSON)
 				                   .body(jsonObject.toString())
                                    .auth()
                                    .basic(username, password)
                                    .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                                    .post(KUNDEN_ID_FILE_PATH);
 
 		// Then
 		assertThat(response.getStatusCode(), is(HTTP_CREATED));
 		// id extrahieren aus http://localhost:8080/shop2/rest/kunden/<id>/file
 		final String idStr = response.getHeader(LOCATION)
 				                     .replace(BASEURI + ":" + PORT + BASEPATH + KUNDEN_PATH + '/', "")
 				                     .replace("/file", "");
 		assertThat(idStr, is(kundeId.toString()));
 		
 		// When (2)
 		// Download der zuvor hochgeladenen Datei
 		response = given().header(ACCEPT, APPLICATION_JSON)
 				          .auth()
                           .basic(username, password)
                           .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
                           .get(KUNDEN_ID_FILE_PATH);
 		
 		try (final JsonReader jsonReader =
 				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
 			jsonObject = jsonReader.readObject();
 		}
 		final String base64String = jsonObject.getString("bytes");
 		final byte[] downloaded = DatatypeConverter.parseBase64Binary(base64String);
 		
 		// Then (2)
 		// Dateigroesse vergleichen: hochgeladene Datei als byte[] einlesen
 		byte[] uploaded;
 		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
 			Files.copy(Paths.get(fileName), stream);
 			uploaded = stream.toByteArray();
 		}
 		assertThat(uploaded.length, is(downloaded.length));
 		
 		// Abspeichern der heruntergeladenen Datei im Unterverzeichnis target zur manuellen Inspektion
 		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(downloaded)) {
 			Files.copy(inputStream, Paths.get(FILENAME_DOWNLOAD), COPY_OPTIONS);
 		}
 
 		LOGGER.info("Heruntergeladene Datei abgespeichert: " + FILENAME_DOWNLOAD);
 		LOGGER.finer("ENDE");
 	}
 	@Ignore
 	@Test
 	public void uploadInvalidMimeType() throws IOException {
 		LOGGER.finer("BEGINN");
 		
 		// Given
 		final Long kundeId = KUNDE_ID_UPLOAD;
 		final String fileName = FILENAME_UPLOAD_INVALID_MIMETYPE;
 		final String username = USERNAME;
 		final String password = PASSWORD;
 		
 		// Datei als byte[] einlesen
 		byte[] bytes;
 		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
 			Files.copy(Paths.get(fileName), stream);
 			bytes = stream.toByteArray();
 		}
 		
 		// byte[] als Inhalt eines JSON-Datensatzes mit Base64-Codierung
 		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
 	                                  .add("bytes", DatatypeConverter.printBase64Binary(bytes))
 	                                  .build();
 		
 		// When
 		final Response response = given().contentType(APPLICATION_JSON)
 				                         .body(jsonObject.toString())
 				                         .auth()
 				                         .basic(username, password)
 				                         .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
 				                         .post(KUNDEN_ID_FILE_PATH);
 		
 		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
 	}
 }
