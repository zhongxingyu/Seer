 package net.intensicode.runme.util;
 
 import java.util.ArrayList;
 
 public final class Log
     {
     public static StringBuffer format( final String aMessage, final Object[] aObjects )
         {
         if ( aMessage == null ) return new StringBuffer();
 
        final ArrayList<Integer> insertPositions = new ArrayList<Integer>();
         findInsertPosition( aMessage, 0, insertPositions );
 
         final StringBuffer buffer = new StringBuffer( aMessage );
         final int valid = Math.min( aObjects.length, insertPositions.size() );
         for ( int idx = valid - 1; idx >= 0; idx-- )
             {
             final int insertPos = insertPositions.get( idx );
             buffer.delete( insertPos, insertPos + 2 );
             buffer.insert( insertPos, aObjects[ idx ] );
             }
 
         return buffer;
         }
 
     public static Log create()
         {
         return new Log();
         }
 
     //#ifdef DEBUG
 
     public static void trace()
         {
         LOG.doTrace();
         }
 
     public static void debug( final String aMessage )
         {
         debug( aMessage, NO_PARAMETERS );
         }
 
     public static void debug( final String aMessage, final long aValue1 )
         {
         debug( aMessage, new Object[]{ aValue1 } );
         }
 
     public static void debug( final String aMessage, final Object aValue1 )
         {
         debug( aMessage, new Object[]{ aValue1 } );
         }
 
     public static void debug( final String aMessage, final Object aValue1, final Object aValue2 )
         {
         debug( aMessage, new Object[]{ aValue1, aValue2 } );
         }
 
     public static void debug( final String aMessage, final int aValue1, final int aValue2 )
         {
         debug( aMessage, new Object[]{ aValue1, aValue2 } );
         }
 
     public static void debug( final String aMessage, final long aValue1, final long aValue2 )
         {
         debug( aMessage, new Object[]{ aValue1, aValue2 } );
         }
 
     public static void debug( final String aMessage, final double aValue1, final double aValue2 )
         {
         debug( aMessage, new Object[]{ aValue1, aValue2 } );
         }
 
     public static void debug( final String aMessage, final Object[] aObjects )
         {
         final StringBuffer buffer = format( aMessage, aObjects );
         LOG.doDebug( buffer );
         }
 
     //#endif
 
     public static void error( final Throwable aThrowable )
         {
         error( null, null, aThrowable );
         }
 
     public static void error( final String aMessage, final Throwable aThrowable )
         {
         error( aMessage, new Object[]{ }, aThrowable );
         }
 
     public static void error( final String aMessage, final Object aObject, final Throwable aThrowable )
         {
         error( aMessage, new Object[]{ aObject }, aThrowable );
         }
 
     public static void error( final String aMessage, final Object[] aObjects, final Throwable aThrowable )
         {
         final StringBuffer buffer = format( aMessage, aObjects );
         LOG.doError( buffer, aThrowable );
         }
 
     // Protected API
 
     protected Log()
         {
         }
 
     //#ifdef DEBUG
 
     protected final void doTrace()
         {
         doLog( "INFO", "TRACE", null );
         }
 
     protected final void doDebug( final StringBuffer aBufferWithMessage )
         {
         doLog( "DEBUG", aBufferWithMessage.toString(), null );
         }
 
     //#endif
 
     protected final void doError( final StringBuffer aBufferWithMessage, final Throwable aThrowable )
         {
         doLog( "ERROR", aBufferWithMessage.toString(), aThrowable );
         }
 
     // Implementation
 
    private static void findInsertPosition( final String aMessage, final int aStartIndex, final ArrayList<Integer> aOutputArray )
         {
         final int foundIndex = aMessage.indexOf( "{}", aStartIndex );
         if ( foundIndex == -1 ) return;
 
         aOutputArray.add( foundIndex );
         findInsertPosition( aMessage, foundIndex + 1, aOutputArray );
         }
 
     private static String makeCodeHintString()
         {
         final RuntimeException notThrownException = new RuntimeException();
         final StackTraceElement[] stackTrace = notThrownException.getStackTrace();
         for ( int i = 0; i < stackTrace.length; i++ )
             {
             final StackTraceElement element = stackTrace[ i ];
             if ( element.getClassName().endsWith( "Log" ) ) continue;
             final StringBuffer buffer = new StringBuffer();
             buffer.append( element.getClassName() );
             buffer.append( "#" );
             buffer.append( element.getMethodName() );
             buffer.append( "[" );
             buffer.append( element.getLineNumber() );
             buffer.append( "]" );
             return buffer.toString();
             }
         return EMPTY_STRING;
         }
 
     private void doLog( final String aLevel, final String aMessage, final Throwable aThrowableOrNull )
         {
         System.out.print( aLevel );
         System.out.print( " " );
         System.out.print( aMessage );
         System.out.print( " :: " );
         System.out.print( makeCodeHintString() );
         System.out.println();
         if ( aThrowableOrNull != null ) aThrowableOrNull.printStackTrace( System.out );
         }
 
 
     private static String EMPTY_STRING = "";
 
     private static Object[] NO_PARAMETERS = new Object[0];
 
     private static final Log LOG = new Log();
     }
