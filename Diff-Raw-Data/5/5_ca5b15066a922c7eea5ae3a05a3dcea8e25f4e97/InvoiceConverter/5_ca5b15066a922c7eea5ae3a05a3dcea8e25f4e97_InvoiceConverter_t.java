 package com.hunterpowerequipment.util;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.log4j.Logger;
 
 /**
  * Hello world!
  *
  */
 public class InvoiceConverter 
 {
 	private static final Logger logger = Logger.getLogger(InvoiceConverter.class);
 
	public static void main( final String[] args ) throws Exception
     {
     	if( args.length != 1 )
     	{
     		System.out.println( "Add an input directory." ) ;
     		return ;
     	}
     	
         // create the camel context that is the "heart" of Camel
         CamelContext camel = new DefaultCamelContext();
 
         try {
             camel.addRoutes( new RouteBuilder() {
                 
                 @Override
                 public void configure() throws Exception {
                     
                     String in = "file:" + args[0] ;
                     String out = in + "//output" ;
                     System.out.println( "In file is: " + in ) ;
                     System.out.println( "Out file is: " + out ) ;
 
                     // check the directory 'in' for new files
                     from(in)
                     
                     	.to("log:com.hunterpowerequipment?level=DEBUG")
 
                     	// split the file contents by lines.
                     	// when finished, use the LineParserStrategy to rebuild the form
                         .split(body(String.class).tokenize("\n"), new LineParserStrategy())
                         	// use this bean to parse each line, removing the header
                         	.to("bean:com.hunterpowerequipment.util.LineParserStrategy?method=handleLine")
                         .end() // always end the split
                         
                         // now, we have the file contents with the headers removed.  
                         // between each page will be the SPLIT_TOKEN
                     	.to("log:com.hunterpowerequipment?level=DEBUG")
                         
                         // re-split the file by the split token
                         .split(body(String.class).tokenize(LineParserStrategy.SPLIT_TOKEN))
                       
                         	// now we have one body for each page
                         
                         	.process( new Processor() {
                         		// this processor is just to get a unique file name for each page
                         		// for when we write the file to the file system
                         		// it's really just for debugging.  this whole piece could be removed.
                         		public void process(Exchange exchange) throws Exception {
                         			
                         			final String SALE_HEADER = "                                                     SALE    " ;
                         			
                         			String body = exchange.getIn().getBody(String.class) ;
                         			
                         			int beginIndex = body.indexOf( SALE_HEADER ) + SALE_HEADER.length() ;
                         			
                         			String orderNumber = body.substring( beginIndex, beginIndex + 6 ) ;
                         			
                         			exchange.getIn().setHeader( "CamelFileName", orderNumber + ".txt" ) ;
                         			logger.debug( "Outputting file: " + exchange.getIn().getHeader("CamelFileName"));
                         		}
                         	})
                     
                         	// output as a file
                         	.to( out )                  
                     
                     	// .to( printer ) 
                         	
                         	.end(); // end the split
                 }
             } );
         
             // start Camel
             camel.start();
         } catch (Exception e1) {
             System.out.println( "ERROR: Failed to start camel, due to" + e1.getMessage() );
             return ;
         }
         
         while( true )
          Thread.sleep(50);
         
     }
 }
