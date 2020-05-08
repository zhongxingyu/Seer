 /**
  * 
  */
 package org.elfattah.shell;
 
 import org.apache.camel.builder.RouteBuilder;
 
 /**
  * @author ceefour
  *
  */
 public class ShellRouteBuilder extends RouteBuilder {
 
 	@Override
 	public void configure() throws Exception {
 		//from("file://test/").to("log:THIS.IS.IMPORTANT");
		from("stream:in?delay=1000").to("seda:shellIn");
 		from("file://test/").to("seda:shellIn");
		//from("xmpp://talk.google.com/?user=testbot@elfattah.org&password=").to("seda:shellIn");
 		from("seda:shellOut").to("log:THIS.IS.IMPORTANT");
 		//camelContext.createProducerTemplate().
 		//queryShell.setProducer( camelContext.getEndpoint("seda:shellOut").createProducer()
 	}
 
 }
