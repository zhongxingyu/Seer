 package dlib.tests;
 
 import dlib.graphics.RenderUpdate;
 import dlib.graphics.renderer.Graphics2DRenderer;
 
 public class Graphics2DRendererTest extends Graphics2DRenderer
 {
	static Graphics2DRenderer r;
 	
 	public static void main( String[] args )
 	{
		r = new Graphics2DRenderer();
 		r.begin();
 	}
 
 	public void update()
 	{
		r.stroke( 255.f, 0.f, 0.f );
		r.rect( 50, 50, 700, 500 );
		r.stroke( 0, 0, 255, 128 );
		r.text( "Test", 100, 100 );
		r.stroke( 0, 255, 0, 128 );
		r.text( "Dans", 100, 100 );
 	}
 
 	public void initialize()
 	{
		
 	}
 }
