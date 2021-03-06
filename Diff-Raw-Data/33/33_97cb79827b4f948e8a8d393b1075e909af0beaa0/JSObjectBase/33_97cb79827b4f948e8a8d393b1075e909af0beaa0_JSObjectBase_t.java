 // JSObjectBase.java
 
 package ed.js;
 
 import java.io.*;
 import java.util.*;
 
 import ed.db.*;
 import ed.js.func.*;
 import ed.js.engine.*;
 import ed.util.*;
 
 public class JSObjectBase implements JSObject {
     
     public static final String GETSET_PREFIX = "_____gs____";
     public static final String SCOPE_FAILOVER_PREFIX = "_____scope_failover____";
     
     static final Set<String> BAD_KEY_NAMES = new HashSet<String>();
     static {
         BAD_KEY_NAMES.add( "__proto__" );
         BAD_KEY_NAMES.add( "__constructor__" );
         BAD_KEY_NAMES.add( "constructor" );
         BAD_KEY_NAMES.add( "__parent____" );
         
         JS._debugSIStart( "JSObjectBase" );
     }
 
     static final String OBJECT_STRING = "Object";
 
     public JSObjectBase(){
     }
 
     public JSObjectBase( JSFunction constructor ){
         setConstructor( constructor );
     }
 
     public void prefunc(){}
 
 
     public Object set( Object n , Object v ){
         _readOnlyCheck();
         prefunc();
         
         if ( n == null )
             n = "null";
         
         if ( v != null && "_id".equals( n ) &&
 	     ( ( v instanceof String ) || ( v instanceof JSString ) )
 	     ){
             v = new ObjectId( v.toString() );
         }
 
         if ( v != null && v instanceof String )
             v = new JSString( v.toString() );
 
         if ( n instanceof Number ){
             setInt( ((Number)n).intValue() , v );
             return v;
         }
 
         String name = n.toString();
 
         if ( name.startsWith( GETSET_PREFIX ) ){
             name = name.substring( GETSET_PREFIX.length() );
             String type = name.substring( 0 , 3 );
             name = name.substring( 3 );
             if ( type.equals( "GET" ) )
                 setGetter( name , (JSFunction)v );
             else if ( type.equals( "SET" ) )
                 setSetter( name , (JSFunction)v );
             else
                 throw new RuntimeException( "broken" );
             return v;
         }
         
         JSFunction func = getSetter( name );
         if ( func != null )
             return _call( func , v );
         
         _checkMap();
         
         if ( ! BAD_KEY_NAMES.contains( name ) )
             if ( ! _map.containsKey( name ) )
                 _keys.add( name );
         
         _dirtyMyself();
         _map.put( name , v );
         if ( v instanceof JSObjectBase )
             ((JSObjectBase)v)._name = name;
             return v;
     }
 
     private void _checkMap(){
         if ( _map == null )
             _map = new TreeMap<String,Object>();
         
         if ( _keys == null )
             _keys = new ArrayList<String>();
 
         _dirtyMyself();
     }
 
     public String getAsString( Object n ){
         final Object v = get( n );
         if ( v == null )
             return null;
         
         return v.toString();
     }
 
     public Object get( Object n ){
         
         prefunc();
         
         if ( n == null )
             n = "null";
         
         if ( ! "__preGet".equals( n ) ){
             Object foo = _simpleGet( "__preGet" );
             if ( foo != null && foo instanceof JSFunction )
                 _call( (JSFunction)foo , n );
         }
 
         if ( n instanceof Number )
             return getInt( ((Number)n).intValue() );
         
         return _simpleGet( n.toString() );
     }
 
     public Object _simpleGet( String s ){
         return _simpleGet( s , 0 );
     }
     
     Object _simpleGet( String s , int depth ){
 
         final boolean scopeFailover = s.startsWith( SCOPE_FAILOVER_PREFIX );
         if ( scopeFailover )
             s = s.substring( SCOPE_FAILOVER_PREFIX.length() );
         
         Object res = null;
         
         if ( depth == 0 && ! BAD_KEY_NAMES.contains( s ) ){
             JSFunction f = getGetter( s );
             if ( f != null )
                 return _call( f );
         }
 
         if ( _map != null ){
             res = _mapGet( s );
             if ( res != null || _map.containsKey( s ) ) return res;
         }
         
         res = _getFromParent( s , depth );
         if ( res != null ) return res;
 
         if ( _objectLowFunctions != null 
              && _constructor == null ){
             res = _objectLowFunctions.get( s );
             if ( res != null ) return res;
         }
 
         if ( depth == 0 && 
              ! "__notFoundHandler".equals( s ) &&
              ! "__preGet".equals( s ) && 
              ! scopeFailover && 
              ! BAD_KEY_NAMES.contains( s )
              ){
             
             JSFunction f = _getNotFoundHandler();
             if ( f != null ){
                 Scope scope = f.getScope();
                 if ( scope == null )
                     scope = Scope.getAScope( false , true );
                 
                 scope = scope.child();
                 scope.setThis( this );
                 if ( ! _inNotFoundHandler.get() ){
                     try {
                         _inNotFoundHandler.set( true );
                         return f.call( scope , s );
                     }
                     finally {
                         _inNotFoundHandler.set( false );
                     }
                 }
             }
         }
         
         if ( scopeFailover ){
             Scope scope = Scope.getAScope( false , true );
             if ( scope != null )
                 return scope.get( s );
         }
 
         return null;
     }
     
     private JSFunction _getNotFoundHandler(){
         Object blah = _simpleGet( "__notFoundHandler" );
         if ( blah instanceof JSFunction )
             return (JSFunction)blah;
         
         return null;
     }
 
 
     // ----
     // inheritnace jit START
     // ----
     
     private Object _getFromParent( String s , int depth ){
         _getFromParentCalls++;
         
         if ( s.equals( "__proto__" ) || s.equals( "prototype" ) )
             return null;
 
         boolean jit = false;
         
         if ( ( depth > 0 && _getFromParentCalls > 50 ) ||
              _getFromParentCalls > 1000 ){
             if ( _dependenciesOk() ){
 
                 jit = true;
                 
                 if ( _jitCache == null )
                     _jitCache = new HashMap<String,Object>();
                 
                 if ( _jitCache.containsKey( s ) ){
                     return _jitCache.get( s );
                 }
             }
             else {
                 _dependencies();
             }
         }
 
         Object res = _getFromParentHelper( s , depth );
         if ( jit )
             _jitCache.put( s , res );
 
         return res;
     }
     
     private Object _getFromParentHelper( String s , int depth ){
 
         JSObject proto = null;
         JSObject prototype = null;
 
         _updatePlacesToLook();
         
         final int max = _placesToLook.length;
 
         for ( int i=0; i<max; i++ ){
             JSObject o = _placesToLook[i];
             if ( o == null )
                 continue;
             
             Object res = o.get( s );
             if ( res != null )
                 return res;
         }
         
         return null;
     }
     
     private void _updatePlacesToLook(){
 
         if ( _placesToLookUpdated )
             return;
         
         if ( _map != null ){
             _placesToLook[0] = (JSObject)_mapGet( "__proto__" );
             _placesToLook[1] = (JSObject)_mapGet( "prototype" );
         }
 
         if ( _constructor != null ){
             _placesToLook[2] = _constructor._prototype;
             _placesToLook[3] = _constructor;
         }
 
         for ( int i=1; i<_placesToLook.length; i++ )
             for ( int j=0; j<i; j++ )
                 if ( _placesToLook[i] == _placesToLook[j] )
                     _placesToLook[i] = null;
         
         _placesToLookUpdated = true;
     }
     
     private boolean _dependenciesOk(){
         if ( _badDepencies )
             return false;
         
         if ( _dependencies == null )
             return false;
 
         List<JSObjectBase> lst = _dependencies;
         for ( int i=0; i<lst.size(); i++ ){
             if ( lst.get(i)._lastModified >= _dependencyBuildTime )
                 return false;
         }
         
         return true;
     }
     
     private List<JSObjectBase> _dependencies(){
 
         if ( _badDepencies )
             return null;
 
         if ( _dependenciesOk() )
             return _dependencies;
 
         List<JSObjectBase> lst = new ArrayList<JSObjectBase>();
         lst = _addDependencies( lst );
 
         if ( lst == null )
             _badDepencies = true;
         _dependencies = lst;
         _dependencyBuildTime = System.currentTimeMillis();
 
         return lst;
     }
 
     protected List<JSObjectBase> _addDependencies( List<JSObjectBase> lst ){
         _updatePlacesToLook();
         for ( int i=0; i<_placesToLook.length; i++ ){
             if ( _placesToLook[i] == null )
                 continue;
             
             // uh-oh
             if ( ! ( _placesToLook[i] instanceof JSObjectBase ) )
                 return null;
 
             JSObjectBase job = (JSObjectBase)_placesToLook[i];
             lst.add( job );
             job._addDependencies( lst  );
         }
         return lst;
     }
 
     // ----
     // inheritnace jit END
     // ----
 
     public Object removeField( Object n ){
         if ( n == null )
             return null;
         
         if ( n instanceof JSString )
             n = n.toString();
         
         Object val = null;
 
         if ( n instanceof String ){
             if ( _map != null )
                 val = _map.remove( (String)n );
             if ( _keys != null )
                 _keys.remove( n );
         }
         
         return val;
     }
 
 
     public Object setInt( int n , Object v ){
         _readOnlyCheck();
         prefunc();
         return set( String.valueOf( n ) , v );
     }
 
     public Object getInt( int n ){
         prefunc();
         return get( String.valueOf( n ) );
     }
 
 
     public boolean containsKey( String s ){
         prefunc();
         if ( _map != null && _map.containsKey( s ) )
             return true;
         
         if ( _constructor != null && _constructor._prototype.containsKey( s ) )
             return true;
 
         return false;
     }
 
     public Collection<String> keySet(){
         prefunc();
         if ( _keys == null )
             return EMPTY_SET;
         return _keys;
     }
 
     // ----
     // [gs]etter
     // ---
 
     void setSetter( String name , JSFunction func ){
         _dirtyMyself();
         _getSetterAndGetter( name , true ).second = func;
     }
     
     void setGetter( String name , JSFunction func ){
         _dirtyMyself();
         _getSetterAndGetter( name , true ).first = func;
     }
 
     JSFunction getSetter( String name ){
         Pair<JSFunction,JSFunction> p = _getSetterAndGetter( name, false );
         if ( p != null )
             return p.second;
         
         JSObject s = getSuper();
         if ( s != null )
             return ((JSObjectBase)s).getSetter( name );
 
         return null;
     }
 
     JSFunction getGetter( String name ){
         Pair<JSFunction,JSFunction> p = _getSetterAndGetter( name, false );
         if ( p != null )
             return p.first;
         
         JSObject s = getSuper();
         if ( s  instanceof JSObjectBase )
             return ((JSObjectBase)s).getGetter( name );
 
         return null;
     }
 
     public static String setterName( String name ){
         return GETSET_PREFIX + "SET" + name;
     }
 
     public static String getterName( String name ){
         return GETSET_PREFIX + "GET" + name;
     }
 
     // ---
 
     public String toString(){
         Object temp = get( "toString" );
         
         if ( ! ( temp instanceof JSFunction ) )
             return OBJECT_STRING;
         
         JSFunction f = (JSFunction)temp;
 
         Scope s;
         try {
             s= f.getScope().child();
             s.setThis( this );
         } catch(RuntimeException t) {
             throw t;
         }
         
         Object res = f.call( s );
         if ( res == null )
             return "Object(toString was null)";
         return res.toString();
     }
 
     protected void addAll( JSObject other ){
         for ( String s : other.keySet() )
             set( s , other.get( s ) );
     }
 
     private Object _call( JSFunction func , Object ... params ){
         Scope sc = Scope.getAScope();
         sc.setThis( this );
         try {
             return func.call( sc , params );
         }
         finally {
             sc.clearThisNormal( null );
         }
     }
 
     public String getJavaString( Object name ){
         Object foo = get( name );
         if ( foo == null )
             return null;
         return foo.toString();
     }
 
     public void setConstructor( JSFunction cons ){
         setConstructor( cons , false , null );
     }
 
     public void setConstructor( JSFunction cons , boolean exec ){
         setConstructor( cons , exec , null );
     }
     
     public void setConstructor( JSFunction cons , boolean exec , Object args[] ){
         _readOnlyCheck();
         _dirtyMyself();
         
         _constructor = cons;
         _mapSet( "__constructor__" , _constructor );
         _mapSet( "constructor" , _constructor );
 
         Object __proto__ = _constructor == null ? null : _constructor._prototype;
         _mapSet( "__proto__" , __proto__ );
 
         if ( _constructor != null && exec ){
             
             Scope s = _constructor.getScope();
             
             if ( s == null )
                 s = Scope.getThreadLocal();
             
             s = s.child();
             
             s.setThis( this );
             _constructor.call( s , args );
         }
     }
 
     public JSFunction getConstructor(){
         return _constructor;
     }
 
     public JSObject getSuper(){
 
         if ( _map != null ){
             JSObject p = (JSObject)_mapGet( "__proto__" );
             if ( p != null )
                 return p;
         }
 
         if ( _constructor != null && _constructor._prototype != null )
             return _constructor._prototype;
 
 
         return null;
     }
 
     public void lock(){
         setReadOnly( true );
     }
 
     public void setReadOnly( boolean readOnly ){
         _readOnly = readOnly;
     }
 
     private final void _readOnlyCheck(){
         if ( _readOnly )
             throw new RuntimeException( "can't modify JSObject - read only" );
     }
 
     public void extend( JSObject other ){
         if ( other == null )
             return;
 
         for ( String key : other.keySet() ){
             set( key , other.get( key ) );
         }
 
     }
 
     public void debug(){
         try {
             debug( 0 , System.out );
         }
         catch ( IOException ioe ){
             ioe.printStackTrace();
         }
     }
 
     Appendable _space( int level , Appendable a )
         throws IOException {
         for ( int i=0; i<level; i++ )
             a.append( "  " );
         return a;
     }
 
     public void debug( int level , Appendable a )
         throws IOException {
         _space( level , a );
         
         a.append( "me :" );
         if ( _name != null )
             a.append( " name : [" ).append( _name ).append( "] " );
         if( _keys != null )
             a.append( "keys : " ).append( _keys.toString() );
         a.append( "\n" );
         
         if ( _map != null ){
             JSObjectBase p = (JSObjectBase)_simpleGet( "prototype" );
             if ( p != null ){
                 _space( level + 1 , a ).append( "prototype ||\n" );
                 p.debug( level + 2 , a );
             }
             
         }
 
         if ( _constructor != null ){
             _space( level + 1 , a ).append( "__constructor__ ||\n" );
             _constructor.debug( level + 2 , a );
         }
     }
 
     public int hashCode(){
         int hash = 81623;
         
         if ( _constructor != null )
             hash += _constructor.hashCode();
         
         if ( _map != null ){
             for ( Map.Entry<String,Object> e : _map.entrySet() ){
                 hash += ( 3 * e.getKey().hashCode() );
                 if ( e.getValue() != null )
                     hash += ( 7 * e.getValue().hashCode() );
             }
         }
 
         return hash;
     }
 
     // -----
     // name is very weird. it probably doesn't work the way you think or want
     // ----
 
     public String _getName(){
         return _name;
     }
         
     public void _setName( String n ){
         _name = n;
     }
 
     private synchronized Pair<JSFunction,JSFunction> _getSetterAndGetter( String name , boolean add ){
         if ( _setterAndGetters == null ){
             if ( ! add )
                 return null;
             _setterAndGetters = new TreeMap<String,Pair<JSFunction,JSFunction>>();                
         }
         
         Pair<JSFunction,JSFunction> p = _setterAndGetters.get( name );
         if ( ! add || p != null )
             return p;
 
         p = new Pair<JSFunction,JSFunction>();
         _setterAndGetters.put( name , p );
         return p;
     }
 
     private Object _mapGet( final String s ){
         if ( _map == null )
             return null;
         final Object o = _map.get( s );
         if ( o == UNDEF )
             return null;
         return o;
     }
 
     private void _mapSet( final String s , final Object o ){
         _checkMap();
         _map.put( s , o );
     }
 
     private void _dirtyMyself(){
         _lastModified = System.currentTimeMillis();
         _placesToLookUpdated = false;
         _dependencies = null;
         if ( _jitCache != null )
             _jitCache.clear();
     }
 
     public long approxSize(){
         long size = JSObjectSize.OBJ_OVERHEAD + 128;
 
         if ( _name != null )
             size += JSObjectSize.OBJ_OVERHEAD + ( _name.length() * 2 );
         
         if ( _keys != null ){
             size += 32 + ( _keys.size() * 4 ) ; // overhead for Collection
             for ( String s : _keys )
                 size += ( s.length() * 2 );
         }
         
         if ( _map != null ){
             size += 32 + ( _map.size() * 8 );
             for ( Map.Entry<String,Object> e : _map.entrySet() ){
                 size += e.getKey().length() * 2;
                 size += JSObjectSize.size( e.getValue() );
             }
         }
         
         return size;
     }
 
    public boolean isPartialObject(){
        return _isPartialObject;
    }
    
    public void markAsPartiableObject(){
        _isPartialObject = true;
    }

     protected Map<String,Object> _map = null;
     protected Map<String,Pair<JSFunction,JSFunction>> _setterAndGetters = null;
     private Collection<String> _keys = null;
     private JSFunction _constructor;
     private boolean _readOnly = false;
     private String _name;
    
    private boolean _isPartialObject = false;
    
 
     // jit stuff
     
     private long _lastModified = System.currentTimeMillis();
 
     private List<JSObjectBase> _dependencies = null;
     private boolean _badDepencies = false;
     private long _dependencyBuildTime = 0;
 
     private boolean _placesToLookUpdated = false;
     private JSObject _placesToLook[] = new JSObject[4];
 
     private int _getFromParentCalls = 0;
     private Map<String,Object> _jitCache;
 
     static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );
     static final Object UNDEF = new Object(){
             public String toString(){
                 return "undefined";
             }
         };
 
     public static class BaseThings extends JSObjectLame {
         
         public BaseThings(){
             init();
         }
 
         public Object get( Object o ){
             String name = o.toString();
             return _things.get( name );
         }
 
         public Object set( Object name , Object val ){
             _things.put( name.toString() , val );
             return val;
         }
         
         protected void init(){
             
             set( "__extend" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object other , Object args[] ){
                         
                         if ( other == null )
                             return null;
                         
                         Object blah = s.getThis();
                         if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                             throw new RuntimeException( "extendt not passed real thing" );
                     
                         if ( ! ( other instanceof JSObject ) )
                             throw new RuntimeException( "can't extend with a non-object" );
                     
                         ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                         return null;
                     }
                 } );
 
 
             set( "merge" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object other , Object args[] ){
                         
                         if ( other == null )
                             return null;
                         
                         Object blah = s.getThis();
                         if ( ! ( blah != null && blah instanceof JSObject ) )
                             throw new RuntimeException( "extend not passed real thing" );
                         
                         if ( ! ( other instanceof JSObject ) )
                             throw new RuntimeException( "can't extend with a non-object" );
                         
                         JSObjectBase n = new JSObjectBase();
                         n.extend( (JSObject)s.getThis() );
                         n.extend( (JSObject)other );
 
                         return n;
                     }
                 } );
             
 
 
             set( "__include" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object other , Object args[] ){
 
                         if ( other == null )
                             return null;
 
                         if ( ! ( other instanceof JSObject ) )
                             throw new RuntimeException( "can't include with a non-object" );
                     
                         Object blah = s.getThis();
                         if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                             throw new RuntimeException( "extend not passed real thing" );
                     
                         ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                         return null;
                     }
                 } );
         
 
             set( "__send" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
 
                         JSObject obj = ((JSObject)s.getThis());
                         if ( obj == null )
                             throw new NullPointerException( "send called on a null thing" );
                     
                         JSFunction func = ((JSFunction)obj.get( name ) );
                     
                         if ( func == null ){
                             // this is a dirty dirty hack for namespace collisions
                             // i hate myself for even writing it in the first place
                             func = ((JSFunction)obj.get( "__" + name ) );
                         }
                         
                         if ( func == null )
                             func = (JSFunction)s.get( name );
 
                         if ( func == null )
                             throw new NullPointerException( "can't find method [" + name + "] to send" );
 
                         return func.call( s , args );
                     }
                 
                 } );
 
             set( "valueOf" , new JSFunctionCalls0(){
                     public Object call( Scope s , Object args[] ){
                         return s.getThis();
                     }
                 } );
 
             // TODO: fix.  this is totally wrong
             set( "class" , new JSFunctionCalls0(){
                     public Object call( Scope s , Object args[] ){
                         return s.getThis();
                     }
                 } );
 
             set( "__keySet" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         JSObjectBase obj = ((JSObjectBase)s.getThis());
                         return new JSArray( obj.keySet() );
                     }
                 } );
 
             set( "instance_methods" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         JSObjectBase obj = ((JSObjectBase)s.getThis());
                         return new JSArray( obj.keySet() );
                     }
                 } );
 
             set( "__debug" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         JSObjectBase obj = ((JSObjectBase)s.getThis());
                         obj.debug();
                         return null;
                     }
                 } );
 
             set( "__hashCode" , new JSFunctionCalls0(){
                     public Object call( Scope s , Object args[] ){
                         JSObjectBase obj = ((JSObjectBase)s.getThis());
                         return obj.hashCode();
                     }
                 } );
 
             set( "is_a_q_" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object type , Object args[] ){
                         return JSInternalFunctions.JS_instanceof( s.getThis() , type );
                     }
                 } );
             
 
             set( "eql_q_" , new JSFunctionCalls1() {
                     public Object call( Scope s , Object o , Object crap[] ){
                         return s.getThis().equals( o );
                     }
                 } );
 
             set( "_lb__rb_" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         return ((JSObjectBase)s.getThis()).get( name );
                     }
                 } );
 
             set( "key_q_" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         if ( name == null )
                             return null;
                         return ((JSObjectBase)s.getThis()).containsKey( name.toString() );
                     }
                 } );
 
             set( "has_key_q_" , get( "key_q_" ) );
 
             set( "__delete" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object name , Object args[] ){
                         return ((JSObjectBase)s.getThis()).removeField( name );
                     }
                 } );
             
             set( "const_defined_q_" , new JSFunctionCalls1(){
                     public Object call( Scope s , Object type , Object args[] ){
                         return s.get( type ) != null;
                     }
                 } );
 
             set( "__defineGetter__" , new JSFunctionCalls2(){
                     public Object call( Scope s , Object name , Object func , Object args[] ){
                         if ( ! ( s.getThis() instanceof JSObjectBase ) )
                             throw new RuntimeException( "not a JSObjectBase" );
                         
                         JSObjectBase o = (JSObjectBase)s.getThis();
                         o.setGetter( name.toString() , (JSFunction)func );
                         return null;
                     }
                 } );
 
             set( "__defineSetter__" , new JSFunctionCalls2(){
                     public Object call( Scope s , Object name , Object func , Object args[] ){
                         if ( ! ( s.getThis() instanceof JSObjectBase ) )
                             throw new RuntimeException( "not a JSObjectBase" );
                         
                         JSObjectBase o = (JSObjectBase)s.getThis();
                         o.setSetter( name.toString() , (JSFunction)func );
                         return null;
                     }
                 } );
 
             set( "to_i" , new JSFunctionCalls0(){
                     public Object call( Scope s , Object args[] ){                    
                         return JSInternalFunctions.parseNumber( s.getThis() , null );
                     }
                 } );
 
         }
         
         public Collection<String> keySet(){
             return _things.keySet();
         }
 
         private Map<String,Object> _things = new HashMap<String,Object>();
     }
 
     public static final JSObject _objectLowFunctions = new BaseThings();
     
     private static final ThreadLocal<Boolean> _inNotFoundHandler = new ThreadLocal<Boolean>(){
         protected Boolean initialValue(){
             return false;
         }
     };
 }
