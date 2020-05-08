 package com.hifiremote.jp1;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class FixedData.
  */
 public class FixedData
 {
 
   /**
    * Instantiates a new fixed data.
    * 
    * @param addr
    *          the addr
    * @param bytes
    *          the bytes
    */
   public FixedData( int addr, short[] data )
   {
     address = addr;
     this.data = data;
   }
 
   /**
    * Gets the address.
    * 
    * @return the address
    */
   public int getAddress()
   {
     return address;
   }
 
   /**
    * Gets the data.
    * 
    * @return the data
    */
   public short[] getData()
   {
     return data;
   }
 
   /**
    * Parses the fixed data.
    * 
    * @param rdr
    *          the rdr
    * @return the string
    * @throws Exception
    *           the exception
    */
   public static FixedData[] parse( RDFReader rdr ) throws Exception
   {
     java.util.List< FixedData > work = new ArrayList< FixedData >();
     java.util.List< Short > temp = new ArrayList< Short >();
     String line;
     int address = -1;
     int value = -1;
 
     while ( true )
     {
       line = rdr.readLine();
 
       if ( ( line == null ) || ( line.length() == 0 ) )
         break;
 
       StringTokenizer st = new StringTokenizer( line, ",; \t" );
       String token = st.nextToken();
       while ( true )
       {
         if ( token.charAt( 0 ) == '=' ) // the last token was an address
         {
           token = token.substring( 1 );
           if ( address != -1 ) // we've seen some bytes
           {
             short[] b = new short[ temp.size() ];
             int i = 0;
             for ( Short val : temp )
             {
               b[ i++ ] = val.shortValue();
             }
             work.add( new FixedData( address, b ) );
             temp.clear();
           }
           address = value;
           value = -1;
           if ( token.length() != 0 )
             continue;
         }
         else
         {
           int equal = token.indexOf( '=' );
           String saved = token;
           if ( equal != -1 )
           {
             token = token.substring( 0, equal );
           }
           if ( value != -1 )
           {
             temp.add( new Short( ( short )value ) );
           }
           value = RDFReader.parseNumber( token );
           if ( equal != -1 )
           {
             token = saved.substring( equal );
             continue;
           }
         }
         if ( !st.hasMoreTokens() )
           break;
         token = st.nextToken();
       }
     }
     temp.add( new Short( ( short )value ) );
     short[] b = new short[ temp.size() ];
     int j = 0;
     for ( Short by : temp )
     {
       b[ j++ ] = by.shortValue();
     }
    if ( address != -1 )
    {
      work.add( new FixedData( address, b ) );
    }
     return work.toArray( new FixedData[ work.size() ] );
   }
 
   public boolean check( short[] buffer )
   {
     for ( int i = 0; i < data.length; ++i )
     {
       if ( data[ i ] != buffer[ address + i ] )
       {
         return false;
       }
     }
     return true;
   }
   
   public static Remote[] filter( List< Remote > remotes, short[] buffer )
   {
     List< Remote > passed = new ArrayList< Remote >();
     for ( Remote remote : remotes )
     {
       boolean pass = true;
       for ( FixedData fixedData : remote.getFixedData() )
       {
         if ( ! fixedData.check( buffer ) )
         {
           pass = false;
           break;
         }
       }
       if ( pass )
       {
         passed.add( remote );
       }   
     }    
     return passed.toArray( new Remote[ 0 ] );
   }
 
   public void store( short[] buffer )
   {
     System.arraycopy( data, 0, buffer, address, data.length );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
     StringBuilder temp = new StringBuilder( 200 );
     temp.append( '$' ).append( Integer.toHexString( address ) ).append( " =" );
     for ( int i = 0; i < data.length; i++ )
     {
       temp.append( " $" );
       String str = Integer.toHexString( data[ i ] );
       int len = str.length();
       if ( len > 2 )
         str = str.substring( len - 2 );
       if ( len < 2 )
         temp.append( '0' );
       temp.append( str );
     }
     return temp.toString();
   }
 
   /** The address. */
   private int address;
 
   /** The data. */
   private short[] data;
 }
