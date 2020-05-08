 /* Entry for sortable.com coding challenge
  * by Kellen Steffen
  */
 
 import java.util.Stack;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.Map;
import java.util.regex.Pattern;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.text.Normalizer;
 
 import org.json.*;
 
 public class Match
 {
   public static final String PRODUCT_NAME = "product_name";
   public static final String PRODUCT_MANUFACTURER = "manufacturer";
   public static final String PRODUCT_FAMILY = "family";
   public static final String PRODUCT_MODEL = "model";
   public static final String PRODUCT_DATE = "announced-date";
 
   public static final String LISTING_TITLE = "title";
   public static final String LISTING_MANUFACTURER = "manufacturer";
   public static final String LISTING_CURRENCY = "currency";
   public static final String LISTING_PRICE = "price";
 
   public static final String PRODUCT_FILE = "products.txt";
   public static final String LISTING_FILE = "listings.txt";
   public static final String RESULT_FILE = "results.txt";
 
   protected static HashMap<String,String> productsMap =
       new HashMap<String,String>();
   protected static HashMap<String,Stack<JSONObject>> resultsMap =
       new HashMap<String,Stack<JSONObject>>();
   protected static HashMap<String,Stack<JSONObject>> manufacturerMap =
       new HashMap<String,Stack<JSONObject>>();
 
 
   protected static String normalizeString( String s )
   {
     /*String t = Normalizer.normalize( s, Normalizer.Form.NFD );
     t = t.toLowerCase().trim();
     //t = t.replaceAll( "[^a-z0-9\":{}, ]", "" );
     return t;*/
     return s.toLowerCase().trim();
   }
 
 
   /*
    * processProductFile()
    * reads strings from r, expecting one JSON object per line.
    * normalizes all strings to convert non-english characters.
    * converts the strings to JSONObjects and pushes them onto s.
    */
   protected static void processProductFile( FileReader r )
   {
     try {
       // create a JSONTokener to read r
       JSONTokener toke = new JSONTokener( r );
       while ( toke.more() ) {
         // get the next line
         String str = toke.nextTo( '\n' );
         // discard the newline
         toke.next();
 
         // map normalized product name to pristine product name
         String con = normalizeString( str );
         JSONObject o = new JSONObject( con );
         JSONObject po = new JSONObject( str );
         productsMap.put( o.getString( PRODUCT_NAME ),
             po.getString( PRODUCT_NAME ) );
         resultsMap.put( po.getString( PRODUCT_NAME ), new Stack<JSONObject>() );
 
         // convert the normalized string to JSON and
         // put the product on the appropriate manufacturer stack
         String manufacturer = o.getString( PRODUCT_MANUFACTURER );
         manufacturer = manufacturer.toLowerCase();
         if ( !manufacturerMap.containsKey( manufacturer ) ) {
           Stack<JSONObject> s = new Stack<JSONObject>();
           s.push( o );
           manufacturerMap.put( manufacturer, s );
         } else {
           manufacturerMap.get( manufacturer ).push( o );
         }
       }
     } catch ( Exception e ) {
       System.err.println( "that was exceptional!" );
       System.err.println( e + ":\n" + e.getStackTrace() );
       System.exit( 2 );
     }
   }
 
 
   /*
    * 
    * 
    * 
    * 
    */
   protected static void processListingFile( FileReader r )
   {
     try {
       // create a JSONTokener to read r
       JSONTokener toke = new JSONTokener( r );
       while ( toke.more() ) {
         // get the next line
         String str = toke.nextTo( '\n' );
         // discard the newline
         toke.next();
 
         // put original, "pristine" string in the pristine stack
         JSONObject pristineO = new JSONObject( str );
 
         // convert the string to JSON and put it on the product stack
         String con = normalizeString( str );
 
         JSONObject o = new JSONObject( con );
 
         // match manufacturers
         String title = o.getString( LISTING_TITLE );
         String manufacturer = o.getString( LISTING_MANUFACTURER );
         manufacturer = manufacturer.toLowerCase().trim();
         Iterator i = manufacturerMap.keySet().iterator();
         while ( i.hasNext() ) {
           String m = (String) i.next();
           if ( manufacturer.contains( m ) ) {
             // manufacturer matches, so check family/model
             // get all products by that manufacturer
             //System.err.println( "checking for family" );
             Stack<JSONObject> s = (Stack<JSONObject>) manufacturerMap.get( m );
             Iterator mi = s.iterator();
             while ( mi.hasNext() ) {
               JSONObject mo = (JSONObject) mi.next();
               String name = productsMap.get( mo.getString( PRODUCT_NAME ) );
               String model = mo.getString( PRODUCT_MODEL );
               if ( mo.has( PRODUCT_FAMILY ) ) {
                 //System.err.println( "there's a family! checking model" );
                 String family = mo.getString( PRODUCT_FAMILY );
                 if ( manufacturer.equals( "nikon" ) ) {
                   //System.err.println( "it's a nikon" );
                   System.out.println( "\"" + title  + "\" contains \"" + family
                       + "\" and \"" +  model + "\"?" );
                 }
                if ( //title.contains( family )
                    title.contains( family )
                    && title.matches( ".*\\b" + model + "\\b.*" ) ) {
                    //&& title.contains( model ) ) {
                   if ( manufacturer.equals( "nikon" ) ) {
                     System.out.println( "yes" );
                   }
                   resultsMap.get( name ).push( pristineO );
                   //System.err.println( "pushed" );
                   break;
                 }
               } else {
                 //System.err.println( "no family, checking model" );
                   if ( manufacturer.equals( "nikon" ) ) {
                     System.out.println( title  + " contains " + model + "?");
                   }
                   if ( title.contains( model ) ) {
                     // model matches, so add it to the product's
                     // stack of listings in the results
                     if ( manufacturer.equals( "nikon" ) ) {
                       System.out.println( "yes" );
                     }
                     resultsMap.get( name ).push( pristineO );
                     break;
                   }
               }
             }
             break;
           }
         }
       }
     } catch ( Exception e ) {
       System.err.println( "that was exceptional!" );
       System.err.println( e + ":\n" + e.getStackTrace() );
       System.exit( 2 );
     }
   }
 
 
   /*
    * main()
    * reads and cross-matches the product and listing files,
    * writing the results to disk
    */
   public static void main( String[] args )
   {
     FileReader productReader;
     FileReader listingReader;
     FileWriter resultWriter;
     JSONWriter jsonWriter;
 
     try {
       // open up the product, listing, and result files
       productReader = new FileReader( PRODUCT_FILE );
       listingReader = new FileReader( LISTING_FILE );
       resultWriter = new FileWriter( RESULT_FILE );
       jsonWriter = new JSONWriter( resultWriter );
 
       // read the product file
       processProductFile( productReader );
       productReader.close();
 
       // read the listing file
       HashMap<String,Stack<JSONObject>> listingManufacturerMap =
           new HashMap<String,Stack<JSONObject>>();
       processListingFile( listingReader );
       listingReader.close();
 
       // compare products and listings
       
       // write the results to disk
       Iterator i = resultsMap.keySet().iterator();
       while ( i.hasNext() ) {
         String name = (String) i.next();
         Stack<JSONObject> listings = resultsMap.get( name );
         resultWriter.write( "{\"product_name\":\"" + name + "\",\"listings\":"
             + listings + "}\n");
       }
 
       System.out.println( "\nResults written to results.txt." );
       jsonWriter = null;
       resultWriter.close();
 
     } catch ( Exception e ) {
       System.err.println( "that was exceptional!" );
       System.err.println( e + ":\n" + e.getStackTrace() );
       System.exit( 1 );
     }
   }
 }
 
