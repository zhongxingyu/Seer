 package pl.pks.memgen.resources;
 
 import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import java.io.InputStream;
 import javax.ws.rs.core.MediaType;
 import org.junit.Test;
 import org.mockito.BDDMockito;
 import pl.pks.memgen.api.Figure;
 import pl.pks.memgen.db.StorageService;
 import pl.pks.memgen.io.FigureUploader;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import com.yammer.dropwizard.testing.ResourceTest;
 import com.yammer.dropwizard.views.ViewMessageBodyWriter;
 
 public class FigureResourceIntegrationTest extends ResourceTest {
 
     private StorageService storage = mock(StorageService.class);
     private FigureUploader uploader = mock(FigureUploader.class);
 
     @Override
     protected void setUpResources() {
         addResource(new FigureResource(storage, uploader));
         addProvider(ViewMessageBodyWriter.class);
     }
 
     @Test
     public void shouldPersistFigureFromLink() throws Exception {
         // given
         WebResource service = client().resource("/figure");
         final String url = "https://dl.dropbox.com/u/1114182/memgen/philosoraptor.jpg";
         Form form = getFormFixutre(url);
         given(uploader.fromLink(anyString())).willReturn(getFigureFixed());
         // when
         ClientResponse post = service.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, form);
         // then
         String locationHeader = post.getHeaders().getFirst("location");
        assertThat(locationHeader).contains("/meme/new/foo.jpg");
     }
 
     @Test
     public void shouldPersistFigureFromDisk() {
         // given
         WebResource service = client().resource("/figure/fromDisk");
         InputStream stream = getClass().getClassLoader().getResourceAsStream("philosoraptor.jpg");
         given(uploader.fromDisk(any(InputStream.class), anyString())).willReturn(getFigureFixed());
         @SuppressWarnings("resource")
         FormDataMultiPart part = new FormDataMultiPart().field("file", stream,
             MULTIPART_FORM_DATA_TYPE).field("name", "philosoraptor.jpg");
 
         // when
         ClientResponse post = service.type(MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, part);
 
         // then
         String locationHeader = post.getHeaders().getFirst("location");
        assertThat(locationHeader).contains("/meme/new/foo.jpg");
     }
 
     @Test
     public void shouldShowAllFigures() {
         // given
         WebResource service = client().resource("/figure");
         // when
         service.get(String.class);
         // then
         BDDMockito.verify(storage).findAllFigures();
     }
 
     private Figure getFigureFixed() {
         return new Figure("foo.jpg", "http://bar/");
     }
 
     private Form getFormFixutre(String url) {
         Form form = new Form();
         form.add("url", url);
         return form;
     }
 
 }
