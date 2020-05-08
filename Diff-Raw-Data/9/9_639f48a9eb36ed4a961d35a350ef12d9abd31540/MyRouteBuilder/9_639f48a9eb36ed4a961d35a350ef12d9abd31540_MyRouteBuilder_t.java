 package de.gzockoll.osgi.raumklima;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
 import org.apache.camel.dataformat.xstream.XStreamDataFormat;
 import org.apache.camel.spi.DataFormat;
 import org.apache.commons.lang3.Validate;
 
 /**
  * A Camel Java DSL Router
  */
 public class MyRouteBuilder extends RouteBuilder {
 	DataFormat bindy = new BindyCsvDataFormat("de.gzockoll.osgi.raumklima");
 
 	/**
 	 * Let's configure the Camel routing rules using Java code...
 	 */
 	public void configure() {
 		XStreamDataFormat xstreamDefinition = new XStreamDataFormat();
 		Map<String, String> aliases = new HashMap<String, String>();
 		aliases.put("klima", Sht21Data.class.getName());
 		xstreamDefinition.setAliases(aliases);
 		// here is a sample which processes the input files
 		// (leaving them in place - see the 'noop' flag)
 		// then performs content based routing on the message using XPath
 		from("jetty:http://0.0.0.0:11145/klima/humidity").to(
 				"direct:sht21").to("log:http").setBody(xpath("klima/humidity/text()")).convertBodyTo(String.class).to("log:data");
 		from("jetty:http://0.0.0.0:11145/klima/temperature").to(
 				"direct:sht21").setBody(xpath("klima/temperature/text()")).to("log:data");
 
		from("direct:sht21").to("exec:/home/pi/wrk/Raspi-SHT21-V3_0_0/sht21?args=S").to("direct:data");
 
 		from("direct:simulator")
 				.setBody()
 				.constant("21.4      43").to("direct:data");
 
 		from("direct:data")
 				.process(new Processor() {
 
 					@Override
 					public void process(Exchange ex) throws Exception {
 						String line = ex.getIn().getBody(String.class);
 						String[] parts = line.split("\\s+");
 						Validate.isTrue(parts.length == 2);
 						Sht21Data data = new Sht21Data(Double
 								.parseDouble(parts[0]), Double
 								.parseDouble(parts[1]));
 						ex.getIn().setBody(data);
 					}
				}).marshal(xstreamDefinition)
				.to("log:read");
 	}
 
 }
