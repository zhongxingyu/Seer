 package net.azulite.Amanatsu;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.Color;
 import android.opengl.GLUtils;
 
 /**
  * Amanatsuの描画を行うクラス。
  * @author Hiroki
  */
 
 
 // Library.
 // TODO:
 // * screen size
 // * auto change 1.1, 2.0
 // * box texture
 // * set funciton glEnable() GL_POINT_SMOOTH,GL_LINE_SMOOTH,GL_POLYGON_SMOOTH
// * float buffer reuse
// * float buffer size
 public class AmanatsuDraw
 {
   private Amanatsu ama;
   private GL10 gl = null;
   private int width, height;
   private float basex, basey, screenwidth, screenheight;
   private Resources resource;
   private AssetManager assets;
   private static Map<Integer, Texture> textures = new Hashtable< Integer, Texture >( 50 );
   private static Map<Integer, Paint> paints = new Hashtable< Integer, Paint >( 50 );
   private static float[] circlepoint;
   private static int circlepointnum = 32;
   private String gltype, glver;
 
   // Box
   private Texture boxtex;
 
   // String.
   private Bitmap stringbmp;
   private Texture stringtex;
   private int stringnum;
 
   // tmp.
   private float[] farr, farr4, mat;
   private Texture ttex;
   private Paint tpaint;
 
   public AmanatsuDraw( Amanatsu ama, String gltype, String glver )
   {
     this.ama = ama;
     resource = ama.getContext().getResources();
     assets = ama.getContext().getResources().getAssets();
     farr = new float[ 10 ];
     farr4 = new float[ 4 ];
     mat = new float[ 16 ];
     this.gltype = gltype;
     this.glver = glver;
     circlepoint = new float[ ( circlepointnum + 2 ) * 2 ];
   }
 
   public String getGLVersion_(){ return gl.glGetString( GL10.GL_VERSION ); }
   public String getGLVersion(){ return glver; }
   public String getGLType(){ return gltype; }
 
   public void init( GL10 gl )
   {
     setGL( gl );
 
     Bitmap white = Bitmap.createBitmap( 1, 1, Config.ALPHA_8 );
     Canvas canvas = new Canvas( white );
     canvas.drawARGB( 255, 255, 255, 255 );
     createTextureFromBitmap( 0, white, false );
     boxtex = ttex;
 
     boxtex.col  = createFloatBuffer( new float[]{ 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f } );
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     boxtex.uv   = createFloatBuffer( farr );
 
     stringbmp = Bitmap.createBitmap( 1024, 1024, Config.ALPHA_8 );//Bitmap.Config.ARGB_8888);
     createTextureFromBitmap( 0, stringbmp, false );
     stringtex = ttex;
     stringnum = ttex.texid[ 0 ];
     stringtex.col  = createFloatBuffer( new float[]{ 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f } );
 
     createFont( 0, 30 );
 
     setRender( Amanatsu.DRAW_TRC );
   }
 
   public void change( GL10 gl, int width, int height )
   {
     setGL( gl );
 
     gl.glMatrixMode( GL10.GL_PROJECTION );
     gl.glLoadIdentity();
     gl.glOrthof( 0.0f, width, height, 0.0f, 50.0f, -50.0f );
 
     if ( getWidth() <= 0 )
     {
       setWindowSize( width, height );
       setScreenSize( 0.0f, 0.0f, width, height );
       Amanatsu.input.setWindowSize( width, height );
       Amanatsu.input.setInputArea( 0.0f, 0.0f, width, height );
     } else
     {
       setWindowSize( width, height );
       Amanatsu.input.setWindowSize( width, height );
     }
 
     gl.glEnable( GL10.GL_BLEND );
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
     gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );//TODO
 
     gl.glFrontFace( GL10.GL_CW );
     gl.glEnable( GL10.GL_CULL_FACE );
     gl.glCullFace( GL10.GL_BACK );
 
     gl.glDisable( GL10.GL_DEPTH_TEST );
     gl.glDisable( GL10.GL_DITHER );
 
     gl.glShadeModel( GL10.GL_FLAT );
     gl.glHint( GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST );
   }
 
   /**
    * スクリーンサイズの設定。
    * スクリーンサイズとは画面全体をウィンドウとした時、その一部分を切り取ったものを指します。
    * 開始座標と領域で指定することが可能です。
    * @param x 開始X座標。
    * @param y 開始Y座標。
    * @param width 横幅。
    * @param height 高さ。
    */
   public boolean setScreenSize( float x, float y, float width, float height )
   {
     if ( Amanatsu.input != null )
     {
       Amanatsu.input.setInputArea( x, y, width, height );
     }
     basex = x * this.width / width;
     basey = y * this.height / height;
     screenwidth = width;
     screenheight = height;
     width = (this.width * this.width) / width;
     height = (this.height * this.height) / height;
 
     gl.glViewport( (int)-basex, (this.height - (int)height) + (int)basey, (int)width, (int)height );
 
     return true;
   }
 
   /**
    * スクリーンの移動。
    * @param x 移動先X座標。
    * @param y 移動先Y座標。
    */
   public boolean moveScreen( float x, float y )
   {
     basex = x;
     basey = y;
     return setScreenSize( basex, basey, screenwidth, screenheight );
   }
 
   public void release()
   {
     stringbmp.recycle();
   }
 
   public boolean setGL( GL10 gl )
   {
     this.gl = gl;
     return true;
   }
 
   /**
    * @return FPS。
    */
   public float getFps()
   {
     return ama.render.getFps();
   }
 
   /**
    * FPSの設定、
    * @param fps FPSの設定(Amanatsuのデフォルトは30.0f)。
    */
   public float setFps( float fps )
   {
     return ama.render.setFps( fps );
   }
 
   /**
    * 描画モードの設定。
    * 描画モードを切り替えます。
    * Amanatsu.DRAW_ADD=加算合成。
    * Amanatsu.DRAW_MUL=乗算合成。
    * Amanatsu.DRAW_TRC=透過色有効通常合成。
    * @param type Amanatsu.DRAW_* で設定されている描画モード。
    */
   public boolean setRender( int type )
   {
     switch ( type )
     {
     case Amanatsu.DRAW_ADD:
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
       break;
 /*    case Amanatsu.DRAW_SUB:TODO
       gl.glBlendEquationEXT( GL11ExtensionPack.GL_FUNC_REVERSE_SUBTRACT_EXT );
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
       break;*/
     case Amanatsu.DRAW_MUL:
       gl.glBlendFunc( GL10.GL_ZERO, GL10.GL_SRC_COLOR );
       // memo:
       //dst = dst * src * alpha;
       //gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_COLOR);
       //gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_SRC_ALPHA);
       break;
     default:
       gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
     }
     return true;
   }
 
   /**
    * 画面のクリア。
    * 画面を黒で塗りつぶします。
    * MainLoop開始時に一度実行してください。
    * また、画面クリア前に描画していた命令は全て消されてしまうので気をつけて下さい。
    */
   public boolean clearScreen()
   {
     return clearScreen( 0.0f, 0.0f, 0.0f );
   }
 
   /**
    * 画面のクリア。
    * 任意の色で画面を塗りつぶします(アルファ値は無視)。
    * @param color GameColorのインスタンス。
    */
   public boolean clearScreen( GameColor color )
   {
     return clearScreen( color.color[ 0 ], color.color[ 1 ], color.color[ 2 ] );
   }
 
   /**
    * 画面のクリア。
    * 任意の色で画面を塗りつぶします(アルファ値は無視)。
    * @param color 色配列( 赤, 緑, 青 ).各色の強さは0-255。
    */
   public boolean clearScreen( byte[] color )
   {
     return clearScreen( (float)color[ 0 ] / 255.0f, (float)color[ 1 ] / 255.0f, (float)color[ 2 ] / 255.0f );
   }
 
   /**
    * 画面のクリア。
    * 任意の色で画面を塗りつぶします(アルファ値は無視)。
    * @param color 色配列( 赤, 緑, 青 ).各色の強さは0.0f-1.0f。
    */
   public boolean clearScreen( float[] color )
   {
     return clearScreen( color[ 0 ], color[ 1 ], color[ 2 ] );
   }
 
   /**
    * 画面のクリア。
    * @param red 赤(0-255).
    * @param green 緑(0-255).
    * @param blue 青(0-255).
    */
   public boolean clearScreen( byte red, byte green, byte blue )
   {
     return clearScreen( (float)red / 255.0f, (float)green / 255.0f, (float)blue / 255.0f );
   }
 
   /**
    * 画面のクリア。
    * @param red 赤(0.0f-1.0f).
    * @param green 緑(0.0f-1.0f).
    * @param blue 青(0.0f-1.0f).
    */
   public boolean clearScreen( float red, float green, float blue )
   {
     gl.glClearColor( red, green, blue, 1.0f );
     gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
 
     return true;
   }
 
   /**
    * テクスチャの生成(リソースから)。
    * テクスチャをリソースから生成する。テクスチャ番号はリソース番号となる。
    * @param rnum リソース番号。
    */
   public int createTexture( int rnum )
   {
     return createTextureFromBitmap( rnum, BitmapFactory.decodeResource( resource, rnum ), true );
   }
 
   /**
    * テクスチャの生成(リソースから)。
    * テクスチャ番号は自分で設定する。
    * @param tnum テクスチャ番号。
    * @param rnum リソース番号。
    */
   public int createTexture( int tnum, int rnum )
   {
     return createTextureFromBitmap( tnum, BitmapFactory.decodeResource( resource, rnum ), true );
   }
 
   /**
    * テクスチャの設定(assets)。
    * assets内に格納したファイルからテクスチャを生成する。
    * ファイルパスはassetsをrootにした場合。
    * @param tnum テクスチャ番号。
    * @param path ファイルパス。
    */
   public int createTexture( int tnum, String path )
   {
     try
     {
       InputStream is = assets.open( path );
       return createTextureFromBitmap( tnum, BitmapFactory.decodeStream( is ), true );
     } catch ( IOException e )
     {
     }
     return -1;
   }
 
   /**
    * テクスチャの生成(Bitmap)。
    * Bitmapからテクスチャを生成する。
    * @param tnum テクスチャ番号。
    * @param bmp Bitmapのインスタンス。
    */
   public int createTexture( int tnum, Bitmap bmp )
   {
     return createTextureFromBitmap( tnum, bmp, true );
   }
 
   private int createTextureFromBitmap( int tnum, Bitmap bmp, boolean regist )
   {
     if ( bmp == null )
     {
       return -1;
     }
 
     if ( regist )
     {
       if ( textures.containsKey( tnum ) )
       {
         releaseTexture( tnum );
         ttex = textures.get( tnum );
       } else
       {
         ttex = new Texture();
         ttex.texid = new int [ 1 ];
         textures.put( tnum, ttex );
       }
     } else
     {
       // Unregist.
       ttex = new Texture();
       ttex.texid = new int [ 1 ];
     }
 
     gl.glGenTextures( 1, ttex.texid, 0 );
     gl.glBindTexture( GL10.GL_TEXTURE_2D, ttex.texid[ 0 ] );
 
     ttex.bwidth = bmp.getWidth();
     ttex.bheight = bmp.getHeight();
     if ( AmanatsuDraw.isPowerOf2( ttex.bwidth ) == false ||
          AmanatsuDraw.isPowerOf2( ttex.bheight ) == false ||
          ttex.bwidth != ttex.bheight )
     {
       int length = AmanatsuDraw.convertPowerOf2( ttex.bwidth >= ttex.bheight ? ttex.bwidth : ttex.bheight );
       bmp = AmanatsuDraw.resizeBitmap( bmp, length );
     }
     GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bmp, 0 );
 
     gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );
     //gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
     gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );
 
     ttex.width = bmp.getWidth();
     ttex.height = bmp.getHeight();
 
     if ( regist )
     {
       setColor( tnum );
       bmp.recycle();
     }
 
     return ttex.texid[ 0 ];
   }
 
   /**
    * テクスチャの存在チェック。
    * @param tnum テクスチャ番号。
    */
   public boolean existTexture( int tnum ){ return (textures.containsKey( tnum ) && textures.get( tnum ).texid != null );}
 
   /**
    * テクスチャの破棄。
    * @param tnum テクスチャ番号。
    */
   public void destroyTexture( int tnum )
   {
     if ( releaseTexture( tnum ) )
     {
       textures.remove( tnum );
     }
   }
 
   // TODO
   /**
    * テクスチャの解放。
    * @param tnum テクスチャ番号。
    */
   public boolean releaseTexture( int tnum )
   {
     if ( existTexture( tnum ) )
     {
       gl.glDeleteTextures( 1, textures.get( tnum ).texid, 0 );
       return true;
     }
     return false;
   }
 
   /**
    * テクスチャを全て解放。
    */
   public void releaseTexture()
   {
     List<Integer> keys = new ArrayList<Integer>( textures.keySet() );
     for ( Integer key : keys )
     {
       releaseTexture( key );
     }
   }
 
  private final void setFloatArray4( float f0, float f1, float f2, float f3 )
  {
    farr4[ 0 ] = f0; farr4[ 1 ] = f1;
    farr4[ 2 ] = f2; farr4[ 3 ] = f3;
  }

   private final void setFloatArray( float f0, float f1, float f2, float f3 )
   {
     farr[ 0 ] = f0; farr[ 1 ] = f1;
     farr[ 2 ] = f2; farr[ 3 ] = f3;
   }
 
   private final void setFloatArray( float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7 )
   {
     farr[ 0 ] = f0; farr[ 1 ] = f1;
     farr[ 2 ] = f2; farr[ 3 ] = f3;
     farr[ 4 ] = f4; farr[ 5 ] = f5;
     farr[ 6 ] = f6; farr[ 7 ] = f7;
   }
 
   private final void setFloatArray( float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9 )
   {
     farr[ 0 ] = f0; farr[ 1 ] = f1;
     farr[ 2 ] = f2; farr[ 3 ] = f3;
     farr[ 4 ] = f4; farr[ 5 ] = f5;
     farr[ 6 ] = f6; farr[ 7 ] = f7;
     farr[ 8 ] = f8; farr[ 9 ] = f9;
   }
 
   private static final FloatBuffer createColor( float[] color )
   {
     if ( color.length >= 16 )
     {
       float[] col =
       {
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 4 ], color[ 5 ], color[ 6 ], color[ 7 ],
         color[ 8 ], color[ 9 ], color[ 10 ], color[ 11 ],
         color[ 12 ], color[ 13 ], color[ 14 ], color[ 15 ],
       };
       return createFloatBuffer( col );
     } else if ( color.length >= 4 )
     {
       float[] col =
       {
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
         color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ],
       };
       return createFloatBuffer( col );
     } else if ( color.length > 0 )
     {
       float[] col =
       {
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
         color[ 0 ], color[ 0 ], color[ 0 ], color[ 0 ],
       };
       return createFloatBuffer( col );
     }
     float[] col =
     {
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
     };
     return createFloatBuffer( col );
   }
 
   /**
    * テクスチャの色設定(白)。
    * @param tnum テクスチャ番号。
    */
   public final void setColor( int tnum )
   {
     float[] color =
     {
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
       1.0f, 1.0f, 1.0f, 1.0f,
     };
     setColor( tnum, color );
   }
 
   /**
    * テクスチャ色の設定。
    * @param tnum テクスチャ番号。
    * @param col 色の強さ。アルファ値以外の値が全て同じ。0.0f-1.0f。
    */
   public final void setColor( int tnum, float col )
   {
     float[] color =
     {
       col, col, col, 1.0f,
       col, col, col, 1.0f,
       col, col, col, 1.0f,
       col, col, col, 1.0f,
     };
     setColor( tnum, color );
   }
 
   /**
    * テクスチャ色の設定。
    * @param tnum テクスチャ番号。
    * @param color GameColorのインスタンス。
    */
   public final void setColor( int tnum, GameColor color )
   {
     setColor( tnum, color.color );
   }
 
   /**
    * テクスチャ色の設定。
    * @param tnum テクスチャ番号。
    * @param color 色の配列( red, green, blue, alpha )。各色の強さは0.0f-1.0f。
    */
   public final void setColor( int tnum, float[] color )
   {
     Texture tex;
 
     if ( existTexture( tnum ) == false )
     {
       return;
     }
 
     tex = textures.get( tnum );
     tex.col  = createFloatBuffer( color );
   }
 
   /**
    * テクスチャのアルファ値の設定。
    * @param tnum テクスチャ番号。
    * @param alpha テクスチャのアルファ値。アルファ値の強さは0-255。
    */
   public final void setAlpha( int tnum, byte alpha )
   {
     setAlpha( tnum, (float)alpha / 255.0f );
   }
   
   /**
    * テクスチャのアルファ値の設定。
    * @param tnum テクスチャ番号。
    * @param alpha テクスチャのアルファ値。アルファ値の強さは0.0f-1.0f。
    */
   public final void setAlpha( int tnum, float alpha )
   {
     Texture tex;
 
     if ( existTexture( tnum ) == false )
     {
       return;
     }
     tex = textures.get( tnum );
     tex.col.get( mat, 0, 16 );
     mat[ 3 ] = mat[ 7 ] = mat[ 11 ] = mat[ 15 ] = alpha;
     tex.col  = createFloatBuffer( mat );
   }
 
   /**
    * テクスチャのUV設定。
    * @param tex テクスチャ番号。
    * @param uv UV展開の各座標( x0, y0, x1, y1, x2, y2, x3, y3 )。
    */
   public final boolean setUV( Texture tex, float[] uv )
   {
     tex.uv  = createFloatBuffer( uv );
     return true;
   }
 
   /**
    * テクスチャの描画座標設定。
    * @param tex テクスチャ番号。
    * @param vert 描画する各座標( x0, y0, x1, y1, x2 y2, x3, y3 )。
    */
   public final boolean setVertex( Texture tex, float[] vert )
   {
     tex.ver = createFloatBuffer( vert );
     return true;
   }
 
   /**
    * 画面の横幅取得。
    */
   public final int getWidth(){ return width; }
 
   /**
    * 画面の高さ取得。
    */
   public final int getHeight(){ return height; }
 
   /**
    * 画面の大きさ設定。
    * 基本的に画面方向が変わると呼び出される。
    * またこの値は描画領域として扱われるので、setScreenSizeと合わせて使うことで、スクロールなどが可能になる。
    */
   public boolean setWindowSize( int width, int height )
   {
     this.width = width;
     this.height = height;
     return true;
   }
 
   /**
    * テクスチャデータの取得。
    * テクスチャの色、描画座標、drawTextureで使うUV展開のデータが入っている。
    * @param tnum テクスチャ番号。
    */
   public final Texture getTexture( int tnum ){ return textures.get( tnum ); }
 
   // Support OpenGL.
 
   public static final Bitmap resizeBitmap( Bitmap bmp, int length )
   {
     Bitmap nbmp = Bitmap.createBitmap( length, length, Bitmap.Config.ARGB_8888);
 
     Canvas cv = new Canvas( nbmp );
 
     cv.drawBitmap( bmp, 0, 0, null );
 
     return nbmp;
   }
 
   public static final FloatBuffer createFloatBuffer( float[] arr )
   {
     return createFloatBuffer( arr, arr.length );
   }
   public static final FloatBuffer createFloatBuffer( float[] arr, int length )
   {
     ByteBuffer bb = ByteBuffer.allocateDirect( length * 4 );
     bb.order( ByteOrder.nativeOrder() );
     FloatBuffer fb = bb.asFloatBuffer();
     fb.put( arr );
     fb.position( 0 );
     return fb;
   }
 
   public static final boolean isPowerOf2( int val ){ return val > 0 && ( val & (val - 1) ) == 0; }
 
   public static final int convertPowerOf2( int val )
   {
     if ( val < 0 ){ return 0; }
     int ret = 1;
     for( ; val > ret ; ret <<= 1 );
     return ret;
   }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * デフォルトで0番には大きさ30.0f、色は白のフォントが作成されている。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    */
   public boolean createFont( int fnum, int size ){ return createFont( fnum, size, false, GameColor.WHITE ); }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    */
   public boolean createFont( int fnum, int size, boolean antialias ){ return createFont( fnum, size, antialias, GameColor.WHITE ); }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    * @param color GameColorのインスタンス。
    */
   public boolean createFont( int fnum, int size, boolean antialias, GameColor color ){ return createFont( fnum, size, antialias, color.color ); }
 
   // TODO load font file? set font name?
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    * @param color 色配列( red, green, blue, alpha )。各色の強さは0-255。
    */
   public boolean createFont( int fnum, int size, boolean antialias, byte[] color )
   {
     return createFont( fnum, size, antialias, color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
   }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    * @param color 色配列( red, green, blue, alpha )。各色の強さは0.0f-1.0f。
    */
   public boolean createFont( int fnum, int size, boolean antialias, float[] color )
   {
     return createFont( fnum, size, antialias, (byte)(0xff * color[ 0 ]), (byte)(0xff * color[ 1 ]), (byte)(0xff * color[ 2 ]), (byte)(0xff * color[ 3 ]) );
   }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    * @param red 赤の強さ(0.0f-1.0f)。
    * @param green 緑の強さ(0.0f-1.0f)。
    * @param blue 青の強さ(0.0f-1.0f)。
    * @param alpha 不透明の強さ(0.0f-1.0f)。
    */
   public boolean createFont( int fnum, int size, boolean antialias, float red, float green, float blue, float alpha )
   {
     return createFont( fnum, size, antialias, (byte)(0xff * red), (byte)(0xff * green), (byte)(0xff * blue), (byte)(0xff * alpha) );
   }
 
   /**
    * フォントの作成。
    * printfで文字列を描画する時に使うフォントの作成。
    * @param fnum フォント番号。
    * @param size フォントサイズ。　
    * @param antialias アンチエイリアスの設定。
    * @param red 赤の強さ(0-255)。
    * @param green 緑の強さ(0-255)。
    * @param blue 青の強さ(0-255)。
    * @param alpha 不透明の強さ(0255)。
    */
   public boolean createFont( int fnum, int size, boolean antialias, byte red, byte green, byte blue, byte alpha )
   {
     if ( paints.containsKey( fnum ) == false )
     {
       tpaint = new Paint();
       paints.put( fnum, tpaint );
     } else
     {
       tpaint = paints.get( fnum );
     }
 
     tpaint.setTextSize( size );
     tpaint.setARGB( alpha, red, green, blue );
     tpaint.setAntiAlias( antialias );
 
     return true;
   }
 
   /**
    * 文字列の描画。
    * 不正なフォント番号を使うと0番のフォントが使用される。
    * @param fnum フォント番号。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    * @param str 描画する文字列。
    */
   public boolean printf( int fnum, float dx, float dy, String str )
   {
     Canvas canvas = new Canvas( stringbmp );
 
     canvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
 
     if ( paints.containsKey( fnum ) )
     {
       tpaint = paints.get( fnum );
     } else
     {
       tpaint = paints.get( 0 );
     }
 
     canvas.drawText( str, 0, tpaint.getTextSize(), tpaint );
 
     gl.glBindTexture(GL10.GL_TEXTURE_2D, stringnum );
 
     GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, stringbmp, 0 );
 
     setFloatArray( dx, dy, dx + stringtex.width, dy, dx, dy + stringtex.height, dx + stringtex.width, dy + stringtex.height );
     stringtex.ver = createFloatBuffer( farr );
 
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     stringtex.uv   = createFloatBuffer( farr );
 
     return drawTexture( stringtex );
   }
 
   // Base draw.
 
   /**
    * 線の描画。
    * @param sx 描画開始X座標。
    * @param sy 描画開始Y座標。
    * @param ex 描画終了X座標。
    * @param ey 描画終了Y座標。
    * @param color GameColorインスタンス。
    */
   public boolean drawLine( float sx, float sy, float ex, float ey, GameColor color ){ return drawLine( sx, sy, ex, ey, color.color, 1.0f, false ); }
 
   /**
    * 線の描画。
    * @param sx 描画開始X座標。
    * @param sy 描画開始Y座標。
    * @param ex 描画終了X座標。
    * @param ey 描画終了Y座標。
    * @param color 色配列( red, green, blue, alpha )。各色の強さは0.0f-1.0f。
    */
   public boolean drawLine( float sx, float sy, float ex, float ey, float[] color ){ return drawLine( sx, sy, ex, ey, color, 1.0f, false ); }
 
   /**
    * 線の描画。
    * @param sx 描画開始X座標。
    * @param sy 描画開始Y座標。
    * @param ex 描画終了X座標。
    * @param ey 描画終了Y座標。
    * @param color GameColorインスタンス。
    * @param width 線の太さ。
    */
   public boolean drawLine( float sx, float sy, float ex, float ey, GameColor color, float width ){ return drawLine( sx, sy, ex, ey, color.color, width, false ); }
 
   /**
    * 線の描画。
    * @param sx 描画開始X座標。
    * @param sy 描画開始Y座標。
    * @param ex 描画終了X座標。
    * @param ey 描画終了Y座標。
    * @param color 色配列( red, green, blue, alpha ).各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    */
   public boolean drawLine( float sx, float sy, float ex, float ey, float[] color, float width, boolean antialias )
   {
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     // TODO antialias
 
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
     gl.glLineWidth( width );
 
     setFloatArray( sx, sy, ex, ey );
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( farr ) );
 
     gl.glDrawArrays( GL10.GL_LINE_STRIP, 0, 2 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 箱の線の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    */
   public boolean drawBoxLine( float x, float y, float w, float h, GameColor color ){ return drawBoxLine( x, y, w, h, color.color, 1.0f ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列( red, green, blue, alpha ).各色の強さは0.0f-1.0f。
    */
   public boolean drawBoxLine( float x, float y, float w, float h, float[] color ){ return drawBoxLine( x, y, w, h, color, 1.0f ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    * @param width 線の太さ。
    */
   public boolean drawBoxLine( float x, float y, float w, float h, GameColor color, float width ){ return drawBoxLine( x, y, w, h, color.color, width ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列( red, green, blue, alpha ).各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    */
   public boolean drawBoxLine( float x, float y, float w, float h, float[] color, float width )
   {
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glLineWidth( width );
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     setFloatArray( x, y, x + w, y, x + w, y + h, x, y + h, x, y );
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( farr ) );
 
     // TODO: GL_LINE_LOOP
     gl.glDrawArrays( GL10.GL_LINE_STRIP, 0, 4 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 箱の線の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    */
   public boolean drawBoxLineC( float x, float y, float w, float h, GameColor color ){ return drawBoxLine( x - w / 2.0f, y - h / 2.0f, w, h, color.color, 1.0f ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawBoxLineC( float x, float y, float w, float h, float[] color ){ return drawBoxLine( x - w / 2.0f, y - h / 2.0f, w, h, color, 1.0f ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    */
   public boolean drawBoxLineC( float x, float y, float w, float h, GameColor color, float width ){ return drawBoxLine( x - w / 2.0f, y - h / 2.0f, w, h, color.color, width ); }
 
   /**
    * 箱の線の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawBoxLineC( float x, float y, float w, float h, float[] color, float width ){ return drawBoxLine( x - w / 2.0f, y - h / 2.0f, w, h, color, width ); }
 
   /**
    * 箱の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    */
   public boolean drawBox( float x, float y, float w, float h, GameColor color ){ return drawBox( x, y, w, h, color.color ); }
 
   /**
    * 箱の描画。
    * @param x 描画X座標。
    * @param y 描画Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawBox( float x, float y, float w, float h, float[] color )
   {
     setFloatArray( x, y, x + w, y, x, y + h, x + w, y + h );
     boxtex.ver = createFloatBuffer( farr );
     boxtex.col = createColor( color );
     return drawTexture( boxtex );
   }
 
   /**
    * 箱の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color GameColorインスタンス。
    */
   public boolean drawBoxC( float x, float y, float w, float h, GameColor color ){ return drawBoxC( x, y, w, h, color.color ); }
 
   /**
    * 箱の描画。
    * @param x 描画中心X座標。
    * @param y 描画中心Y座標。
    * @param w 横幅。
    * @param h 高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawBoxC( float x, float y, float w, float h, float[] color )
   {
     setFloatArray( x - w / 2.0f, y - h / 2.0f, x + w / 2.0f, y - h / 2.0f, x - w / 2.0f, y + h / 2.0f, x + w / 2.0f, y + h / 2.0f );
     boxtex.ver = createFloatBuffer( farr );
     boxtex.col = createColor( color );
     return drawTexture( boxtex );
   }
   
   private void prepareCircle( float x, float y, float w, float h, boolean fan )
   {
     int i, count = 0;
     float rad;
 
     if ( fan )
     {
       circlepoint[ 0 ] = x;
       circlepoint[ 1 ] = y;
       count = 2;
     }
     circlepoint[ count++ ] = w + x;
     circlepoint[ count ] = y;
 
     for ( i = 1 ; i <= circlepointnum ; ++i )
     {
       rad = 2.0f * (float)i / (float) circlepointnum * (float)Math.PI;
       circlepoint[ ++count ] = (float)Math.cos( rad ) * w + x;
       circlepoint[ ++count ] = (float)Math.sin( rad ) * h + y;
     }
   }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, GameColor color ){ return drawCircleLine( x, y, w, h, color.color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, float[] color ){ return drawCircleLine( x, y, w, h, color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, GameColor color, float width ){ return drawCircleLine( x, y, w, h, color.color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, float[] color, float width ){ return drawCircleLine( x, y, w, h, color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, GameColor color, float width, boolean antialias ){ return drawCircleLine( x, y, w, h, color.color, width, antialias ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の開始X座標。
    * @param y 円を収める箱の開始Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLine( float x, float y, float w, float h, float[] color, float width, boolean antialias )
   {
     // TODO antialias
     w /= 2.0f;
     h /= 2.0f;
     prepareCircle( x + w, y + h, w, h, false );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glLineWidth( width );
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( circlepoint ) );
     /// TODO: GL_LINE_LOOP
     gl.glDrawArrays( GL10.GL_LINE_STRIP, 0, circlepointnum + 1 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color GameColorのインスタンス。
    */
   public boolean drawCircleLineC( float x, float y, float r, GameColor color ){ return drawCircleLineC( x, y, r, color.color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleLineC( float x, float y, float r, float[] color ){ return drawCircleLineC( x, y, r, color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    */
   public boolean drawCircleLineC( float x, float y, float r, GameColor color, float width ){ return drawCircleLineC( x, y, r, color.color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    */
   public boolean drawCircleLineC( float x, float y, float r, float[] color, float width ){ return drawCircleLineC( x, y, r, color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLineC( float x, float y, float r, GameColor color, float width, boolean antialias ){ return drawCircleLineC( x, y, r, color.color, width, antialias ); }
 
   /**
    * 円の線の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLineC( float x, float y, float r, float[] color, float width, boolean antialias )
   {
     // TODO antialias
     prepareCircle( x, y, r, r, false );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glLineWidth( width );
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( circlepoint ) );
     // TODO: GL_LINE_LOOP
     gl.glDrawArrays( GL10.GL_LINE_STRIP, 0, circlepointnum + 1 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, GameColor color ){ return drawCircleLineC( x, y, w, h, color.color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, float[] color ){ return drawCircleLineC( x, y, w, h, color, 1.0f, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, GameColor color, float width ){ return drawCircleLineC( x, y, w, h, color.color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, float[] color, float width ){ return drawCircleLineC( x, y, w, h, color, width, false ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, GameColor color, float width, boolean antialias ){ return drawCircleLineC( x, y, w, h, color.color, width, antialias ); }
 
   /**
    * 円の線の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    * @param width 線の太さ。
    * @param antialias アンチエイリアス(まだ無効)。
    */
   public boolean drawCircleLineC( float x, float y, float w, float h, float[] color, float width, boolean antialias )
   {
     // TODO antialias
     prepareCircle( x, y, w / 2.0f, h / 2.0f, false );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glLineWidth( width );
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( circlepoint ) );
     // TODO: GL_LINE_LOOP
     gl.glDrawArrays( GL10.GL_LINE_STRIP, 0, circlepointnum + 1 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 円の描画。
    * @param x 円を収める箱のX座標。
    * @param y 円を収める箱のY座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    */
   public boolean drawCircle( float x, float y, float w, float h, GameColor color ){ return drawCircleC( x + w / 2.0f, y + h / 2.0f, w, h, color.color ); }
 
   /**
    * 円の描画。
    * @param x 円を収める箱のX座標。
    * @param y 円を収める箱のY座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircle( float x, float y, float w, float h, float[] color ){ return drawCircleC( x + w / 2.0f, y + h / 2.0f, w, h, color ); }
 
   /**
    * 円の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color GameColorのインスタンス。
    */
   public boolean drawCircleC( float x, float y, float w, float h, GameColor color )
   {
     return drawCircleC( x, y, w, h, color.color );
   }
 
   /**
    * 円の描画。
    * @param x 円を収める箱の中心X座標。
    * @param y 円を収める箱の中心Y座標。
    * @param w 円を収める箱の横幅。
    * @param h 円を収める箱の高さ。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleC( float x, float y, float w, float h, float[] color )
   {
     prepareCircle( x, y, w / 2.0f, h / 2.0f, true );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( circlepoint ) );
     gl.glDrawArrays( GL10.GL_TRIANGLE_FAN, 0, circlepointnum + 2 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * 円の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleC( float x, float y, float r, GameColor color )
   {
     return drawCircleC( x, y, r, color.color );
   }
 
   /**
    * 円の描画。
    * @param x 円の中心X座標。
    * @param y 円の中心Y座標。
    * @param r 円の半径。
    * @param color 色配列(red, green, blue, alpha)。各色の強さは0.0f-1.0f。
    */
   public boolean drawCircleC( float x, float y, float r, float[] color )
   {
     prepareCircle( x, y, r, r, true );
 
     gl.glDisable( GL10.GL_TEXTURE_2D );
     gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
 
     gl.glColor4f( color[ 0 ], color[ 1 ], color[ 2 ], color[ 3 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, createFloatBuffer( circlepoint ) );
     gl.glDrawArrays( GL10.GL_TRIANGLE_FAN, 0, circlepointnum + 2 );
 
     gl.glEnable( GL10.GL_TEXTURE_2D );
     gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
 
     return true;
   }
 
   /**
    * テクスチャの描画。
    * すべてのテクスチャ描画はこのメソッドを利用する。
    * @param tex Textureインスタンス。
    */
   public boolean drawTexture( Texture tex )
   {
     gl.glBindTexture( GL10.GL_TEXTURE_2D, tex.texid[ 0 ] );
 
     gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, tex.ver );
     gl.glColorPointer( 4, GL10.GL_FLOAT, 0, tex.col );
     gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, tex.uv );
 
     gl.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );
 
     return true;
   }
 
   /**
    * テクスチャの描画。
    * テクスチャの全体を描画する。
    * @param tnum テクスチャ番号。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    */
   public boolean drawTexture( int tnum, float dx, float dy )
   {
 
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( dx, dy, dx + ttex.width, dy, dx, dy + ttex.height, dx + ttex.width, dy + ttex.height );
     ttex.ver = createFloatBuffer( farr );
 
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの描画。
    * テクスチャ全体を描画する。
    * @param tnum
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    */
   public boolean drawTextureC( int tnum, float dx, float dy )
   {
 
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray(
       dx - ttex.width / 2.0f, dy - ttex.height / 2.0f,
       dx + ttex.width / 2.0f, dy - ttex.height / 2.0f,
       dx - ttex.width / 2.0f, dy + ttex.height / 2.0f,
       dx + ttex.width / 2.0f, dy + ttex.height );
     ttex.ver = createFloatBuffer( farr );
 
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    */
   public boolean drawTexture( int tnum,
       float rx, float ry, float w, float h,
       float dx, float dy )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( dx, dy, dx + w, dy, dx, dy + h, dx + w, dy + h );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    */
   public boolean drawTextureC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray(
       dx - w / 2.0f, dy - h / 2.0f,
       dx + w / 2.0f, dy - h / 2.0f,
       dx - w / 2.0f, dy + h / 2.0f,
       dx + w / 2.0f, dy + h / 2.0f );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    * @param dw 描画横幅。
    * @param dh 描画高さ。
    */
   public boolean drawTextureScaling( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float dw, float dh )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( dx, dy, dx + dw, dy, dx, dy + dh, dx + dw, dy + dh );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    * @param dw 描画横幅。
    * @param dh 描画高さ。
    */
   public boolean drawTextureScalingC( int tnum, float dx, float dy, float dw, float dh )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     farr[ 0 ] = dw / 2.0f;
     farr[ 1 ] = dh / 2.0f;
 
     setFloatArray(
         dx - farr[ 0 ], dy - farr[ 1 ],
         dx + farr[ 0 ], dy - farr[ 1 ],
         dx - farr[ 0 ], dy + farr[ 1 ],
         dx + farr[ 0 ], dy + farr[ 1 ] );
     setVertex( ttex, farr );
 
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr );
 
     return drawTexture( ttex );
   }
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    * @param scale 拡大率。
    */
   public boolean drawTextureScalingC( int tnum, float dx, float dy, float scale )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     farr[ 0 ] = ttex.width * scale / 2.0f;
     farr[ 1 ] = ttex.height * scale / 2.0f;
 
     setFloatArray(
         dx - farr[ 0 ], dy - farr[ 1 ],
         dx + farr[ 0 ], dy - farr[ 1 ],
         dx - farr[ 0 ], dy + farr[ 1 ],
         dx + farr[ 0 ], dy + farr[ 1 ] );
     setVertex( ttex, farr );
 
     setFloatArray( 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
     ttex.uv   = createFloatBuffer( farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param dw 描画横幅。
    * @param dh 描画高さ。
    */
   public boolean drawTextureScalingC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float dw, float dh )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
     ttex = getTexture( tnum );
 
     setFloatArray(
       dx - dw / 2.0f, dy - dh / 2.0f,
       dx + dw / 2.0f, dy - dh / 2.0f,
       dx - dw / 2.0f, dy + dh / 2.0f,
       dx + dw / 2.0f, dy + dh / 2.0f );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画X座標。
    * @param dy 描画Y座標。
    * @param scale 拡大率。
    */
   public boolean drawTextureScaling( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( dx, dy, dx + w * scale, dy, dx, dy + h * scale, dx + w * scale, dy + h * scale );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの拡大縮小描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param scale 拡大率。
    */
   public boolean drawTextureScalingC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     scale /= 2.0f;
     setFloatArray(
       dx - w * scale, dy - h * scale,
       dx + w * scale, dy - h * scale,
       dx - w * scale, dy + h * scale,
       dx + w * scale, dy + h * scale );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの回転描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param rad ラジアン角。
    */
   public boolean drawTextureRotationC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float rad )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.rotateM( mat, 0, rad * 180.0f / (float)Math.PI, 0.0f, 0.0f, 1.0f );
 
     setFloatArray( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
     setFloatArray( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     return drawTexture( ttex );
   }
 
   /**
    * テクスチャの回転描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param angle 角度。
    */
   public boolean drawTextureRotationAngleC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float angle )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.rotateM( mat, 0, angle, 0.0f, 0.0f, 1.0f );
 
     setFloatArray( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
     setFloatArray( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx    / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * テクスチャの拡大縮小回転描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param scale 拡大率。
    * @param rad ラジアン角。
    */
   public boolean drawTextureScaleRotateC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale, float rad )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.scaleM( mat, 0, scale, scale, 1.0f );
     AMatrix.rotateM( mat, 0, rad * 180.0f / (float)Math.PI, 0.0f, 0.0f, 1.0f );
 
    setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
    setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
    setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
    setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }  
 
   /**
    * テクスチャの拡大縮小回転描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx 描画中心X座標。
    * @param dy 描画中心Y座標。
    * @param scale 拡大率。
    * @param angle 角度。
    */
   public boolean drawTextureScaleRotateAngleC( int tnum,
     float rx, float ry, float w, float h,
     float dx, float dy, float scale, float angle )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     AMatrix.identityMatrix( mat );
     AMatrix.translateM( mat, 0, dx, dy, 0.0f );
     AMatrix.scaleM( mat, 0, scale, scale, 1.0f );
     AMatrix.rotateM( mat, 0, angle, 0.0f, 0.0f, 1.0f );
 
    setFloatArray4( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
    setFloatArray4( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
    setFloatArray4( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
    setFloatArray4( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, mat, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * テクスチャの行列変形描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param matrix44 4*4の変形行列。
    */
   public boolean drawTextureMatrix( int tnum,
     float rx, float ry, float w, float h,
     float[] matrix44 )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( 0.0f, 0.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
     setFloatArray( w, 0.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
     setFloatArray( 0.0f, h, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
     setFloatArray( w, h, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * テクスチャの行列変形中心描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param matrix44 4*4の変形行列。
    */
   public boolean drawTextureMatrixC( int tnum,
     float rx, float ry, float w, float h,
     float[] matrix44 )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( - w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 0 ] = farr4[ 0 ]; farr[ 1 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, - h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 2 ] = farr4[ 0 ]; farr[ 3 ] = farr4[ 1 ];
 
     setFloatArray( - w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 4 ] = farr4[ 0 ]; farr[ 5 ] = farr4[ 1 ];
 
     setFloatArray( w / 2.0f, h / 2.0f, 0.0f, 1.0f );
     AMatrix.multiplyMV( farr4, 0, matrix44, 0, farr4, 0 );
     farr[ 6 ] = farr4[ 0 ]; farr[ 7 ] = farr4[ 1 ];
 
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
   /**
    * テクスチャの4点指定描画。
    * @param tnum テクスチャ番号。
    * @param rx テクスチャの読み込み開始X座標。
    * @param ry テクスチャの読み込み開始Y座標。
    * @param w テクスチャの切り取り横幅。
    * @param h テクスチャの切り取り高さ。
    * @param dx0 描画X座標(左上)。
    * @param dy0 描画Y座標座標(左上)。
    * @param dx1 描画X座標(右上)。
    * @param dy1 描画Y座標(右上)。
    * @param dx2 描画X座標(左下)。
    * @param dy2 描画Y座標(左下)。
    * @param dx3 描画X座標(右下)。
    * @param dy3 描画Y座標(右下)。
    */
   public boolean drawTextureVertex( int tnum,
     float rx, float ry, float w, float h,
     float dx0, float dy0,
     float dx1, float dy1,
     float dx2, float dy2,
     float dx3, float dy3 )
   {
     if ( existTexture( tnum ) == false )
     {
       return false;
     }
 
     ttex = getTexture( tnum );
 
     setFloatArray( dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3 );
     setVertex( ttex, farr );
 
     setFloatArray(
       rx     / ttex.width, ry     / ttex.height,
       rx + w / ttex.width, ry     / ttex.height,
       rx     / ttex.width, ry + h / ttex.height,
       rx + w / ttex.width, ry + h / ttex.height );
     setUV( ttex, farr );
 
     boolean ret = drawTexture( ttex );
 
     return ret;
   }
 
 }
 
