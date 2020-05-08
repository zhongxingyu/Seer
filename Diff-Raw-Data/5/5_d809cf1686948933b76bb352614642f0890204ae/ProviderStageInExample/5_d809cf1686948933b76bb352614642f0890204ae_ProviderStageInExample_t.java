 package de.zib.gndms.taskflows.staging.client;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import de.zib.gndms.common.model.FileStats;
 import de.zib.gndms.common.model.gorfx.types.Order;
 import de.zib.gndms.common.model.gorfx.types.Quote;
 import de.zib.gndms.common.model.gorfx.types.TaskResult;
 import de.zib.gndms.common.rest.GNDMSResponseHeader;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.gndmc.dspace.SliceClient;
 import de.zib.gndms.gndmc.gorfx.AbstractTaskFlowExecClient;
 import de.zib.gndms.gndmc.gorfx.ExampleTaskFlowExecClient;
 import de.zib.gndms.gndmc.gorfx.GORFXTaskFlowExample;
 import de.zib.gndms.taskflows.staging.client.model.ProviderStageInOrder;
 import de.zib.gndms.taskflows.staging.client.model.ProviderStageInResult;
 import de.zib.gndms.taskflows.staging.client.tools.ProviderStageInOrderConverter;
 import de.zib.gndms.taskflows.staging.client.tools.ProviderStageInOrderPropertyReader;
 import de.zib.gndms.taskflows.staging.client.tools.ProviderStageInOrderStdoutWriter;
 import org.jetbrains.annotations.NotNull;
 import org.kohsuke.args4j.Option;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author Maik Jorra
  * @email jorra@zib.de
  * @date 07.11.11  14:59
  * @brief
  */
 public class ProviderStageInExample extends GORFXTaskFlowExample {
 
     // args4j options:
     /**
      * @deprecated
      */
 	@Option( name="-oidprefix", required=false, usage="oidPrefix to be stripped vom Object-Ids")
 	protected String oidPrefix = "";
 
     // members
     private AbstractTaskFlowExecClient etfc;
     private Order normalOrder;
 
 
     public static void main( String[] args ) throws Exception {
 
         GORFXTaskFlowExample cnt = new ProviderStageInExample();
         cnt.run( args );
         System.exit( 0 );
     }
 
 
     public ProviderStageInExample() {
         super( true );
     }
 
 
     @Override
     protected AbstractTaskFlowExecClient provideTaskFlowClient() {
 
         etfc = new ExampleTaskFlowExecClient() {
             @Override
             protected void handleResult( TaskResult res ) {
 
                 ProviderStageInResult ftr = ProviderStageInResult.class.cast( res );
                 System.out.println( "\n\nResult slice url: "+ ftr.getResult().getUrl() );
                 ProviderStageInExample.this.showResult( ftr.getResult() );
             }
 
 
             @Override
             protected GNDMSResponseHeader setupContext( final GNDMSResponseHeader context ) {
 
                context.addMyProxyToken( "C3GRID", myProxyLogin, myProxyPasswd );
                 return context;
             }
         };
         return etfc;
     }
 
 
     private void showResult( final Specifier<Void> result ) {
         
         SliceClient sliceClient = createBean( SliceClient.class );
         sliceClient.setServiceURL( gorfxEpUrl );
         final ResponseEntity<List<FileStats>> listResponseEntity =
                 sliceClient.listFiles( result.getUriMap().get( UriFactory.SUBSPACE ),
                        result.getUriMap().get( UriFactory.SLICE_KIND ),
                         result.getUriMap().get( UriFactory.SLICE ), dn );
         
         if ( HttpStatus.OK.equals( listResponseEntity.getStatusCode() ) ) {
             final List<FileStats> fileStats = listResponseEntity.getBody();
             System.out.println( "Result slice contains " + fileStats.size() + " files" );
             for( FileStats fileStat: fileStats ) {
                 System.out.println( fileStat );
                 System.out.println( "Execute: " );
                 System.out.println( "  curl -H \"DN:" + dn + "\" " + result.getUrl() + "/"
                                     + fileStat.path.replace( '/',  '_' ) );
                 System.out.println( "to download this file" );
                 System.out.println();
             }
         }
     }
 
 
     protected void normalRun() throws Exception {
 
         Quote quote = loadAndPrintDesiredQuote();
         System.out.println( "Performing normal run!!" );
         etfc.execTF( getNormalOrder(), dn, true, quote );
         System.out.println( "DONE\n" );
     }
 
 
 	protected ProviderStageInOrder loadOrderFromProps( final String orderPropFilename )
 		  throws IOException
     {
 
 		final @NotNull Properties properties = loadOrderProps( orderPropFilename );
 
 		final ProviderStageInOrderPropertyReader reader =
 			  new ProviderStageInOrderPropertyReader( properties );
 		reader.begin();
 		reader.read();
 	//	fixOids(sfrOrder.getDataDescriptor().getObjectList());
         return  reader.getProduct();
 	}
 
 
 	protected void printOrder( final ProviderStageInOrder order ) throws IOException {
 		System.out.println("# Staging request");
         ProviderStageInOrderConverter orqConverter =
                 new ProviderStageInOrderConverter( new ProviderStageInOrderStdoutWriter(),
                         order );
         orqConverter.convert();
 	}
 
 
     /**
      * @deprecated
      * @param objs
      */
 	protected void fixOids(final String[] objs) {
 		if (oidPrefix.length() > 0) {
 			if (objs != null)
 				for (int i = 0; i < objs.length; i++) {
 					if (objs[i] != null && objs[i].startsWith(oidPrefix)) {
 						final String obj = objs[i].trim();
 						objs[i] = obj.substring(oidPrefix.length());
 					}
 				}
 		}
 	}
 
     protected void failingRun() {
 
     }
 
 
     /**
      * Delviers the order which should be used for #normalRun.
      * 
      * @return an order
      */
     public Order getNormalOrder() {
         
         if( normalOrder == null )
             try {
                 normalOrder = loadOrderFromProps( orderPropFile );
             } catch ( IOException e ) {
                 throw new RuntimeException( e );
             }
 
         return normalOrder;
     }
 }
