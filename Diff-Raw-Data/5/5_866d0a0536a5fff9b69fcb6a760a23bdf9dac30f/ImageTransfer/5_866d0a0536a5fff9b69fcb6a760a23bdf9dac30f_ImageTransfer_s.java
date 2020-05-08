 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.ScreenShot;
 import play.db.jpa.JPABase;
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 
 public class ImageTransfer extends Controller {
 	
 	public static void addScreenShot(ScreenShot sshot ) {
 		   sshot.save();
 		   shots();
 		}
 	
 	/*public static void addViewScreen(  String title, File attachment ) {
 		String mimeType = MimeTypes.getContentType(attachment.getName());
 		
 		System.err.println("title = " + title + " and mime type = " + mimeType);
 		renderJSON("title = " + title + " and mime type = " + mimeType);
 	}*/
 	
 	
 	public static void addViewScreen(  ScreenShot sshot, String aname ) {
 		//System.err.println("received " + aname);
 		String addr =  request.remoteAddress ;
 		sshot.curl = addr;
 		sshot.title=aname;
 		sshot.name = sshot.scrimage.getFile().getName();
 		sshot.save();
 		renderJSON("Enviado: "+sshot.title);
 	}
 
 	public static void getImageByTitle( String title )
 	{
 		final ScreenShot screen = ScreenShot.findByTitle(title); 
 		   response.setContentTypeIfNotSet(screen.scrimage.type());
 		   java.io.InputStream binaryData = screen.scrimage.get();
 		   renderBinary(binaryData);
 	}
 	
 	public static void shotframes()
 	{
 		List<ScreenShot> allss = ScreenShot.getAll(); 
 		ArrayList<String>urls = new ArrayList<String>();
 		for ( ScreenShot s : allss )
 		{
 			if ( !urls.contains( s.curl ) )
 				urls.add( s.curl );
 		}
 		
 		if ( urls.size() > 0 )
 		{
 			render( urls );
 		}
 		else
 		{
 			render();
 		}
 	}
 	
 	public static void getShotsForURL( String theurl )
 	{
 		List<ScreenShot> shots = ScreenShot.getShotsForURL( theurl );
 		render( shots );
 	}
 	
 	
 	public static void shots()
 	{
 			List<ScreenShot> allss = ScreenShot.getAll(); 
 			
 			if ( allss.size() > 0 )
 			{
 				ArrayList<String> titles = new ArrayList<String>();
 				for ( ScreenShot s : allss)
 				{
 					titles.add( s.title );
 				}
 				render( titles );
 			}
 			else
 			{
 				render();
 			}
 	}
 	
 	
 	public static void activity()
 	{
 		render();
 	}
 	
 }
