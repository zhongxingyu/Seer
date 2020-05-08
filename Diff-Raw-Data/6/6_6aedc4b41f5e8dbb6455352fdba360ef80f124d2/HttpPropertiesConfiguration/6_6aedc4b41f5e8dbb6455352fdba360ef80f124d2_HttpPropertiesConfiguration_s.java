 package hu.meza.tools.config;
 
import com.sun.org.apache.bcel.internal.util.ByteSequence;
 import hu.meza.tools.HttpCall;
 import hu.meza.tools.HttpClientWrapper;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class HttpPropertiesConfiguration implements Configuration {
 
 	private final HttpClientWrapper client;
 	private final String url;
 	private Properties props = new Properties();
 
 	public HttpPropertiesConfiguration(String url) {
 		this(url, new HttpClientWrapper());
 	}
 
 	public HttpPropertiesConfiguration(String url, HttpClientWrapper client) {
 		this.url = url;
 		this.client = client;
 	}
 
 	@Override
 	public boolean load() {
 		HttpCall result = client.getFrom(url);
 
		InputStream is = new ByteSequence(result.body().getBytes());
 
 		try {
 			props.load(is);
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public Properties properties() {
 		return props;
 	}
 }
