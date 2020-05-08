 package fr.gbloquel.codestory;
 
 import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
 import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
 import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.IOException;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class FunctionalTest {
 
 	@Before
 	public void setup() {
 		setBaseUrl("http://localhost:6543/");
 	}
 	
 	@Test
 	public void should_getEmail() {
 		beginAt("/?q=Quelle+est+ton+adresse+email");
 		assertTextPresent("gregory.bloquel@gmail.com");
 	}
 
 	@Test
 	public void should_subscribe_mailing_list() {
 		beginAt("/?q=Es+tu+abonne+a+la+mailing+list(OUI/NON)");
 		assertTextPresent("OUI");
 
 	}
 
 	@Test
 	public void should_be_happy() {
 		beginAt("/?q=Es+tu+heureux+de+participer(OUI/NON)");
 		assertTextPresent("OUI");
 
 	}
 
 	@Test
 	public void should_receive_markdown_format() {
 		beginAt("/?q=Es+tu+pret+a+recevoir+une+enonce+au+format+markdown+par+http+post(OUI/NON)");
 		assertTextPresent("OUI");
 
 	}
 
 	@Test
 	public void should_always_yes() {
 		beginAt("?q=Est+ce+que+tu+reponds+toujours+oui(OUI/NON)");
 		assertTextPresent("NON");
 	}
 
 	@Test
 	public void shouldPostEnonce() {
 		Client client = Client.create();
 
 		WebResource webResource = client
 				.resource("http://localhost:6543/enonce/1");
 		
 		String input = "blabla";
 		 
 		ClientResponse response = webResource.type("text/plain")
 		   .post(ClientResponse.class, input);
 
 		assertThat(response).isNotNull();
 		assertThat(response.getClientResponseStatus()).isEqualTo(ClientResponse.Status.CREATED);
 		
 	}
 	@Test
 	public void should_receive_statement() {
 		beginAt("/?q=As+tu+bien+recu+le+premier+enonce(OUI/NON)");
 		assertTextPresent("OUI");
 	}
 	
 	
 	@Test
 	public void should_scalaskel_change_1() {
 		beginAt("/scalaskel/change/1");
 		assertTextPresent("{\"foo\":1}");
 	}
 	
 	@Test
 	public void should_scalaskel_change_7() {
 		beginAt("/scalaskel/change/7");
 		assertTextPresent("[{\"foo\":7},{\"bar\":1}]"); 
 	}
 	
 	@Test
 	public void should_scalaskel_change_19() {
 		beginAt("/scalaskel/change/19");
 		assertTextPresent("[{\"foo\":19},{\"foo\":12,\"bar\":1},{\"foo\":5,\"bar\":2},{\"foo\":8,\"qix\":1},{\"foo\":1,\"bar\":1,\"qix\":1}]"); 
 	}
 	
 	@Test
 	public void should_add_1_plus_1() {
 		beginAt("/?q=1+1");
 		assertTextPresent("2");
 	}
 	
 	@Test
 	public void should_multiple_1_plus_1() {
 		beginAt("/?q=1*1");
 		assertTextPresent("1");
 	}
 	
 	@Test
 	public void should_add_1_2_multiplication_2() {
 		beginAt("/?q=(1+2)*2");
 		assertTextPresent("6");
 	}
 	
 	@Test
 	public void should_add_1_2_divise_2() {
 		beginAt("/?q=(1+2)/2");
 		assertTextPresent("1,5");
 	}
 	
 	@Test
 	public void should_add_1_5_multiple_4() {
 		beginAt("/?q=1,5*4");
 		assertTextPresent("6");
 	}
 	
 	@Test
 	public void should_big_operation() {
 		beginAt("?q=((1,1+2)+3,14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000");
 		assertTextPresent("31878018903828899277492024491376690701584023926880");
 	}
 	
 	@Test
 	public void shoud_good_night() {
 		beginAt("/?q=As+tu+passe+une+bonne+nuit+malgre+les+bugs+de+l+etape+precedente(PAS_TOP/BOF/QUELS_BUGS)");
 		assertTextPresent("QUELS_BUGS");
 	}
 	
 	@Test
 	public void should_received_2nd_text() {
 		beginAt("?q=As+tu+bien+recu+le+second+enonce(OUI/NON)");
 		assertTextPresent("OUI");
 	}
 	
     @Test
     public void should_jajascript_optimize_one_flight() throws IOException {
         Client client = Client.create();
 
         WebResource webResource = client.resource("http://localhost:6543/jajascript/optimize");
 
         // JSon
         String input = "[{ \"VOL\": \"MONAD42\", \"DEPART\": 0, \"DUREE\": 5, \"PRIX\": 10 }]";
 
         ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                 input);
 
         assertThat(response).isNotNull();
         assertThat(response.getClientResponseStatus()).isEqualTo(ClientResponse.Status.CREATED);
         String responseAsString = IOUtils.toString(response.getEntityInputStream());
        assertThat(responseAsString).isEqualTo("{\"gain\":10,\"path\":[\"MONAD42\"]}");
 
     }
 	
 
 	@Test
     public void should_jajascript_optimize() throws IOException {
 		Client client = Client.create();
 
 		WebResource webResource = client
 				.resource("http://localhost:6543/jajascript/optimize");
 		
 		//JSon
 		String input = "[{ \"VOL\": \"MONAD42\", \"DEPART\": 0, \"DUREE\": 5, \"PRIX\": 10 },{ \"VOL\": \"META18\", \"DEPART\": 3, \"DUREE\": 7, \"PRIX\": 14 },{ \"VOL\": \"LEGACY01\", \"DEPART\": 5, \"DUREE\": 9, \"PRIX\": 8 },{ \"VOL\": \"YAGNI17\", \"DEPART\": 5, \"DUREE\": 9, \"PRIX\": 7 }]";
 		 
         ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                 input);
 
         assertThat(response).isNotNull();
         assertThat(response.getClientResponseStatus()).isEqualTo(ClientResponse.Status.CREATED);
         String responseAsString = IOUtils.toString(response.getEntityInputStream());
        assertThat(responseAsString).isEqualTo("{\"gain\":18,\"path\":[\"MONAD42\",\"LEGACY01\"]}");
 		
 	}
 
 }
