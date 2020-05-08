 package gr.agroknow.metadata.transformer.agris2agrif;
 
 import gr.agroknow.metadata.agrif.Agrif;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 
 
 public class Main 
 {
 	
 	public static void main(String[] args) throws IOException
 	{
 		int check = 0 ;
 		String inputFolder = null ;
 		String outputFolder = null ;
 		String badFolder = null ;
 		String set = null ;
 		String manifestation = null ;
 		String mtdLanguage = null ;
 		String potentialLanguages = null ;
 		
 		for( int i = 0; i< args.length ; i++ )
 		{
 			if ( "-input".equals( args[i] ) )
 			{
 				i++ ;
 				inputFolder = args[i] ;
 				check = check + 1 ;
 			}
 			if ( "-output".equals( args[i] ) )
 			{
 				i++ ;
 				outputFolder = args[i] ;
 				check = check + 2 ;
 			}
 			if ( "-bad".equals( args[i] ) )
 			{
 				i++ ;
 				badFolder = args[i] ;
 				check = check + 4 ;
 			}
 			if ( "-set".equals( args[i] ) )
 			{
 				i++ ;
 				set = args[i] ;
 				check = check + 8 ;
 			}
 			if ( "-manifestation".equals( args[i] ) )
 			{
 				i++ ;
 				manifestation = args[i] ;
 			}
 			if ( "-mtdLanguage".equals( args[i] ) )
 			{
 				i++ ;
 				mtdLanguage = args[i] ;
 			}
 			if ( "-potentialLanguages".equals( args[i] ) )
 			{
 				i++ ;
 				potentialLanguages = args[i] ;
 			}
 		}
 		
 		if ( ((args.length % 2) != 0) || (check != 15 )  )
 		{
 			System.err.println( "Usage : java -jar agris2agrif.jar -input <INPUT_FOLDER> -output <OUTPUT_FOLDER> -bad <BAD_FOLDER> -set <SET_NAME> [-manifestation <MANIFESTATION_NAME>] [-mtdLanguage <METADATA_LANGUAGE>] [-potentialLanguages <LANG1,LANG2,LANGn>]" ) ;
 			System.exit( -1 ) ;
 		}
 		
 		AGRIS2AGRIF transformer = null ;
 		String identifier ;
 		File inputDirectory = new File( inputFolder ) ;
 		FileReader fr = null ;
 		int wrong = 0 ;
 		for (String agris: inputDirectory.list() )
 		{
 			try
 			{
 				identifier = agris.substring( 0, agris.length()-4 ) ;
 				fr = new FileReader( inputFolder + File.separator + agris ) ;
 				transformer = new AGRIS2AGRIF( fr ) ;
 				transformer.setProviderId( set ) ;
 				transformer.setManifestationType( manifestation ) ;
 				if ( mtdLanguage != null )
 				{
 					transformer.setMtdLanguage( mtdLanguage ) ;
 				}
 				if ( potentialLanguages != null )
 				{
 					transformer.setPotentialLanguages( potentialLanguages ) ;
 				}
 				transformer.yylex() ;
 				// identifier = transformer.getId() ;
 				int iter = 0 ;
 				for( Agrif agrif: transformer.getAgrifs() )
 				{
 					FileUtils.writeStringToFile( new File( outputFolder + File.separator + identifier + iter + ".json" ) , agrif.toJSONString() ) ;
 					iter++ ;
 				}
 			}
 			catch( Exception e )
 			{
 				e.printStackTrace() ;
 				wrong++ ;
 				FileUtils.copyFile( new File(inputFolder + File.separator + agris) , new File( badFolder + File.separator + agris ) )  ;
 				System.out.println( "Wrong file : " + agris ) ;
				e.printStackTrace() ;
				System.exit( -1 ) ;
 			}
 			finally
 			{
 				try 
 				{
 					fr.close() ;
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 		System.out.println( "#wrong : " + wrong ) ;
 	}
 }
