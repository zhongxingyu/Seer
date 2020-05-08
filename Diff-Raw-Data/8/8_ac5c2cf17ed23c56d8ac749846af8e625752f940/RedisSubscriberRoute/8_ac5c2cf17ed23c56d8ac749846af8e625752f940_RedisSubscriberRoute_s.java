 package demo.redis; 
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.impl.JndiRegistry;
 import org.apache.camel.spi.Registry;
 import org.apache.camel.spring.spi.ApplicationContextRegistry;
 import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.data.redis.listener.RedisMessageListenerContainer;

 
 public class RedisSubscriberRoute extends RouteBuilder{
 
 
         @Override
 	public void configure() throws Exception {
         	        
        from("spring-redis://localhost:6379?command=SUBSCRIBE&channels=mychannel") 
 		.process(new Processor() {
 				@Override
 				public void process(Exchange exchange) throws Exception {
 					String res = exchange.getIn().getBody().toString();
 					System.out.println("************ " + res); 
					exchange.getOut().setBody(res);
 				}
 			})
         .to("log:foo");
 	}
 
 }
