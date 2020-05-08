 package dlib.tests;
 
 import dlib.graphics.RenderUpdate;
 import dlib.graphics.renderer.Graphics2DRenderer;
 
 public class Graphics2DRendererTest extends Graphics2DRenderer
 {
	static Graphics2DRendererTest r;
 	
 	public static void main( String[] args )
 	{
		r = new Graphics2DRendererTest();
 		r.begin();
 	}
 
 	public void update()
 	{
		stroke( 255.f, 0.f, 0.f );
		rect( 50, 50, 700, 500 );
		stroke( 0, 0, 255, 128 );
		text( "Test", 100, 100 );
		stroke( 0, 255, 0, 128 );
		text( "Dans", 100, 100 );
 	}
 
 	public void initialize()
 	{
		size( 800, 600 );
 	}
 }
