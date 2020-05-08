 package no.vindsiden.process;
 import no.vindsiden.configuration.Configuration;
 import no.vindsiden.parser.impl.DavisDataParser;
 import no.vindsiden.parser.impl.WeatherDisplayDataParser;
 import no.vindsiden.vindsiden.VindsidenHttpClient;
 import no.vindsiden.weatherstation.impl.DavisWeatherLinkWeatherStation;
 import no.vindsiden.weatherstation.impl.DavisWeatherStation;
 import no.vindsiden.weatherstation.impl.WeatherDisplayWeatherStation;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
import com.thoughtworks.xstream.XStream;

 
 /**
  * @author Erik Mohn - mohn.erik@gmail.com
  */
 public class WeatherFetcherTest {
 
 	private WeatherFetcher fetcher;
 	
 	@Mock
 	private VindsidenHttpClient vindsidenHttpClientMock;
 	
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);		
 		
 		Configuration config = new Configuration();
 		config.setVindSidenUrl("http://www.vindsiden.no/wrm.aspx");
 		config.setTimeToSleepBeforeErrorHandling(100L);
 
 		DavisWeatherStation ws = new DavisWeatherStation(52, DavisDataParser.class, "Moss havn");
 		ws.setUrl("http://www.moss-havn.no/weather/upload/");
 		ws.setEnabled(true);
 		
 		DavisWeatherLinkWeatherStation ws2 = new DavisWeatherLinkWeatherStation(51, "http://www.weatherlink.com/user/srfsnosk8hvasser/index.php?view=summary&amp;amp;headers=0", "SrfSnoSk8 Hvasser");
 		ws2.setEnabled(true);
 		
 		WeatherDisplayWeatherStation ws3 = new WeatherDisplayWeatherStation(99, WeatherDisplayDataParser.class, "Kystvind");
 		ws3.setUrl("http://kystvind.no/");
 		ws3.setEnabled(true);
 		
 		config.addWeatherStation(ws);
 		config.addWeatherStation(ws2);
 		config.addWeatherStation(ws3);
 		
		System.out.println(new XStream().toXML(config));
 		
 		fetcher = new WeatherFetcher(Configuration.getConfiguration());
 		fetcher.setHttpClient(vindsidenHttpClientMock);
 	}
 	
 	@Test
 	public void testExecution() throws Exception {		
 		fetcher.execute();
 		
 	}
 
 }
