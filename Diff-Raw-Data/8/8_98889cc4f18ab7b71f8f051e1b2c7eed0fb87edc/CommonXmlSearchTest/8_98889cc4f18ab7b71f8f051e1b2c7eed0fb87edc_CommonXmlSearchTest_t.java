 package ru.yandex.qatools.xmlsearch;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
import java.io.File;
import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.junit.Test;
 
 import ru.yandex.qatools.xmlsearch.beans.Yandexsearch;
 
 public class CommonXmlSearchTest {
	URL searchResultUrl = CommonXmlSearchTest.class.getResource("/searchresults.xml");
 
 	@Test
 	public void testSimpleRequest() throws JAXBException, MalformedURLException {
 		JAXBContext jaxbContext = JAXBContext.newInstance(Yandexsearch.class.getPackage().getName());
 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Yandexsearch yaSearch = (Yandexsearch) unmarshaller.unmarshal(searchResultUrl);
 		int resultSize = yaSearch.getResponse().getResults().getGrouping().getGroup().size();
 		assertThat(resultSize, is(10));
 	}
 
 }
