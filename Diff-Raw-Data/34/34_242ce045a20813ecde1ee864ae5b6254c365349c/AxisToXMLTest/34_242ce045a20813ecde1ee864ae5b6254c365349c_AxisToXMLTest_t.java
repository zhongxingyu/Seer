 package de.zib.gndms.typecon.util;
 
 import de.zib.gndms.kit.application.AbstractApplication;
import de.zib.gndms.model.common.types.TransientContract;
 import de.zib.gndms.model.gorfx.types.ProviderStageInORQ;
 import de.zib.gndms.model.gorfx.types.io.ContractPropertyReader;
import de.zib.gndms.model.gorfx.types.io.ProviderStageInORQPropertyReader;
 import org.kohsuke.args4j.Option;
 
 /**
  * @author Maik Jorra <jorra@zib.de>
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 19.01.2009, Time: 15:58:58
  */
 public class AxisToXMLTest extends AbstractApplication {
 
 
 
     @Option( name="-props", required=true, usage="staging.properties" )
     private String sfrPropFile;
 
     @Option( name="-cprops", required=false, usage="contract.properties" )
     private String conPropFile;
 
     public static void main( String[] args ) throws Exception {
         AxisToXMLTest ni = new AxisToXMLTest( );
         ni.run( args );
     }
 
 
     public void run() throws Exception {
 
         ProviderStageInORQ orq = ProviderStageInORQPropertyReader.readFromFile( sfrPropFile );
         ProviderStageInXMLImpl xmler = new ProviderStageInXMLImpl( );
         TransientContract con = ContractPropertyReader.readFromFile( conPropFile );
         String s = xmler.toDocument( orq, con );
         System.out.println( s );
     }
 
 
 }
