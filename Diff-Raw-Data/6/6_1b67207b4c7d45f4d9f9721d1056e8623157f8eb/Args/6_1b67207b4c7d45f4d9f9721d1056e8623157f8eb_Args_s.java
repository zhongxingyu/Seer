 package dmg.util ;
 
 import java.util.* ;
 
public class Args implements java.io.Serializable {
 
    static final long serialVersionUID = -8950082352156787965L;
    private final Map<String, String> _optHash = CollectionFactory.newHashMap();
    private final List<String>    _optv  = new Vector<String>();
    private final List<String>    _argv  = new Vector<String>();
    private String    _oneChar = null ;
    public Args( String args ) {
 
       scanLine( args ) ;
    }
    public Args( String [] args ) {
 
       StringBuilder sb = new StringBuilder() ;
       for( int i = 0 ; i < args.length ; i++ )
          sb.append(args[i]).append(" ");
 
       scanLine( sb.toString() ) ;
    }
    Args( Args in ){
      _argv.addAll(in._argv);
      _optv.addAll( in._optv );
      _optHash.putAll( in._optHash );
      _line = in._line ;
    }
    public boolean isOneCharOption( char c ){
      return _oneChar.indexOf(c) > -1 ;
    }
    public int argc(){ return _argv.size() ; }
    public int optc(){ return _optv.size() ; }
    public String getOpt( String optName ){ return _optHash.get( optName ) ; }
    public String argv( int i ){
 
 	   String value = null;
        if( i < _argv.size() ) {
          value =  _argv.get(i) ;
        }
 
        return value;
 
    }
    public String optv( int i ){
 	   String value = null;
 
       if( i < _optv.size() ){
         value =  _optv.get(i) ;
       }
 
       return value;
    }
    public void shift(){
 
       if( !_argv.isEmpty() ) {
         _argv.remove(0);
       }
 
    }
 
     public Map<String, String>  options()
     {
         return Collections.unmodifiableMap(_optHash);
     }
 
    public Object clone(){ return new Args( this ) ; }
 
     public String toString()
     {
         StringBuilder s = new StringBuilder();
 
         for (Map.Entry<String,String> e: _optHash.entrySet()) {
             String key = e.getKey();
             String value = e.getValue();
             if (value.length() > 0) {
                 s.append('-').append(key).append('=').append(value);
             } else {
                 s.append('-').append(key);
             }
             s.append(' ');
         }
 
         for (int i = 0; i < argc(); i++) {
             s.append(argv(i)).append(' ');
         }
 
         return s.toString();
     }
 
    public String getInfo(){
       StringBuilder sb = new StringBuilder() ;
 
       sb.append( "Positional :\n" );
       for( int i= 0 ; i < _argv.size() ; i++ ){
          sb.append(i).append(" -> ").append(_argv.get(i)).append("\n") ;
       }
       sb.append( "Options :\n" );
       for( int i= 0 ; i < _optv.size() ; i++ ){
          String key = _optv.get(i) ;
          String val = _optHash.get(key) ;
          sb.append(key) ;
          if( val != null )
             sb.append( " -> " ).append(val) ;
          sb.append("\n") ;
       }
 
       return sb.toString() ;
    }
    private static final int IDLE          = 0 ;
    private static final int PLAIN_STRING  = 1 ;
    private static final int QUOTED_STRING = 2 ;
    private static final int OPT_KEY       = 3 ;
    private static final int OPT_VALUE     = 4 ;
    private static final int OPT_QUOTED    = 5 ;
    private static final int OPT_PLAIN     = 6 ;
 
    private void undo( char r ){ _res = r ; _undo = true ; }
 
    private boolean _undo    = false ;
    private char    _res     = 0 ;
    private int     _current = 0 ;
    private String  _line    = null ;
    private char nextChar() {
       if( _undo ){ _undo = false  ; return _res ; }
       else
          return _current >= _line.length() ?
                 END_OF_INFO :
                 _line.charAt(_current++) ;
 
    }
 
    private final static char   END_OF_INFO = (char)-1 ;
    private void scanLine( String line ){
       _line = line ;
       int  state = IDLE ;
       char c ;
       StringBuilder key = null , value = null ;
       StringBuilder oneChar = new StringBuilder() ;
       do{
          c = nextChar() ;
          switch( state ){
             case IDLE :
                if( ( c == END_OF_INFO ) || ( c == ' ' ) || ( c == '\t' ) ){
                   // nothing to do
                }else if( c == '"' ){
                   state = QUOTED_STRING ;
                   value = new StringBuilder() ;
                }else if( c == '-' ){
                   state = OPT_KEY ;
                   key   = new StringBuilder() ;
                }else{
                   value = new StringBuilder() ;
                   value.append(c);
                   state = PLAIN_STRING ;
                }
             break ;
             case PLAIN_STRING :
                if( ( c == END_OF_INFO ) || ( c == ' ' ) || ( c == '\t' ) ){
                   _argv.add( value.toString() ) ;
                   state = IDLE ;
                }else{
                   value.append(c) ;
                }
             break ;
             case QUOTED_STRING :
                if( ( c == END_OF_INFO ) ||
                    ( c == '"'         )    ){
                   _argv.add( value.toString() ) ;
                   state = IDLE ;
                }else{
                   value.append(c) ;
                }
             break ;
             case OPT_KEY :
                if( ( c == END_OF_INFO ) || ( c == ' ' ) || ( c == '\t' ) ){
                   if( key.length() != 0 ){
                      _optv.add(key.toString()) ;
                      _optHash.put( key.toString() , "" ) ;
                      oneChar.append(key.toString());
                   }
                   state = IDLE ;
                }else if( c == '=' ){
                   value = new StringBuilder() ;
                   state = OPT_VALUE ;
                }else{
                   key.append(c) ;
                }
             break ;
             case OPT_VALUE :
                if( ( c == END_OF_INFO ) || ( c == ' ' ) || ( c == '\t' ) ){
                   if( key.length() != 0 ){
                      _optv.add(key.toString()) ;
                      _optHash.put( key.toString() , "" ) ;
                   }
                   state = IDLE ;
                }else if( c == '"' ){
                   value = new StringBuilder() ;
                   state = OPT_QUOTED ;
                }else{
                   state = OPT_PLAIN ;
                   value = new StringBuilder() ;
                   value.append(c) ;
                }
             break ;
             case OPT_QUOTED :
                if( ( c == END_OF_INFO ) || ( c == '"' ) ){
                   _optv.add( key.toString() ) ;
                   _optHash.put( key.toString() , value.toString() ) ;
                   state =IDLE ;
                }else{
                   value.append(c) ;
                }
             break ;
             case OPT_PLAIN :
                if( ( c == END_OF_INFO ) || ( c == ' ' ) || ( c == '\t' ) ){
                   _optv.add( key.toString() ) ;
                   _optHash.put( key.toString() , value.toString() ) ;
                   state =IDLE ;
                }else{
                   value.append(c) ;
                }
             break ;
 
 
          }
       }while( c != END_OF_INFO ) ;
       _oneChar = oneChar.toString() ;
 
    }
    public static void main( String [] args )throws Exception {
       if( args.length < 1 ){
          System.err.println( "Usage : ... <parseString>" ) ;
          System.exit(4);
       }
       Args lineArgs = null ;
       if( args.length == 1 )
          lineArgs = new Args( args[0] ) ;
       else
          lineArgs = new Args( args );
       System.out.print( lineArgs.getInfo() ) ;
       System.out.println( "pvr="+lineArgs.getOpt( "pvr" ) ) ;
 
    }
 }
