 package au.edu.labshare.schedserver.scormpackager.sahara;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Set;
 
 import biz.source_code.miniTemplator.MiniTemplator;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigTypeMedia;
 import au.edu.labshare.schedserver.scormpackager.sahara.RigMedia;
 
 import au.edu.labshare.schedserver.scormpackager.sahara.BlurbDecorator;
 import au.edu.labshare.schedserver.scormpackager.sahara.PictureDecorator;
 import au.edu.labshare.schedserver.scormpackager.sahara.LaunchButtonDecorator;
 import au.edu.labshare.schedserver.scormpackager.sahara.TitleDecorator;
 
 
 public class RigLaunchPageCreator 
 {
 	private MiniTemplator templator;
 	
 	ILaunchPage decoratedPage;
 	
 	public static final String TEMPLATE_DOCUMENT_TYPE = "scormpackager_template_1";
 	
 	private LaunchPage launchPage;
 	private TitleDecorator titleDecorator;
 	private PictureDecorator picDecorator;
 	private BlurbDecorator blurbDecorator;
 	private LaunchButtonDecorator launchButtonDecorator;
 	
 	/*
 	 * This is the output path we are going to use using the templating engine
 	 */
 	private String path = null;
 	
 	public RigLaunchPageCreator()
 	{
 		try
 		{
 			Properties defaultProps = new Properties();
 		    FileInputStream in;
 
 		    //in = new FileInputStream("resources/scormpackager.properties"); //TODO: Should place this as a static string
 		    String cwd = new java.io.File( "." ).getCanonicalPath();
 		    if(!cwd.contains("ScormPackager"))
 				in = new FileInputStream(cwd + "/ScormPackager/" + "resources/scormpackager.properties"); //TODO: Should place this as a static string
 			else
 				in = new FileInputStream("resources/scormpackager.properties"); //TODO: Should place this as a static string
 		    
 	        defaultProps.load(in);
 	        in.close();
 			templator = new MiniTemplator(defaultProps.getProperty(TEMPLATE_DOCUMENT_TYPE));
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace(); //TODO: Need to replace with Sahara Logger
 		}
 	}
 	
 	public void setOutputPath(String path)
 	{
 		this.path = path;
 	}
 	
 	public String getOutputPath()
 	{
 		return this.path;
 	}
 	
 	// We want to create the LaunchPage by tapping into DataAccess Layer
 	public String createLaunchPage(String experimentName, org.hibernate.Session session)
 	{		
 		launchPage = new LaunchPage();
 		RigMedia rigMedia = new RigMedia(session);
 		RigType rigType = rigMedia.getRigType(experimentName);
 		
 		Set<RigTypeMedia> rigTypeMediaSet;
 		rigTypeMediaSet = rigType.getMedia();
 		
 		for(RigTypeMedia itemRigTypeMedia : rigTypeMediaSet)
 		{
 			// Should decorate based on the type of the media to update
 			if(itemRigTypeMedia.getMime().toString().equals(RigMedia.MIME_JPEG) || 
 			   itemRigTypeMedia.getMime().toString().equals(RigMedia.MIME_PNG)  ||
 			   itemRigTypeMedia.getMime().toString().equals(RigMedia.MIME_GIF))
 			{
 				picDecorator = new PictureDecorator(launchPage);
				picDecorator.setPicLocation(itemRigTypeMedia.getFileName());
 				decoratePage(picDecorator);
 			}
 			else if(itemRigTypeMedia.getMime().toString().equals(RigMedia.MIME_TXT)) //If it is just plain txt we are assuming blurb
 			{
 				blurbDecorator = new BlurbDecorator(launchPage);
 				
 				File file = new File(itemRigTypeMedia.getFileName());
 				StringBuffer contents = new StringBuffer();
 			    BufferedReader reader = null;
 			    try
 			    {
 			    	reader = new BufferedReader(new FileReader(file));
 			    	String text = null;
 
 			            // repeat until all lines is read
 			            while ((text = reader.readLine()) != null)
 			            {
 			                contents.append(text).append(System.getProperty("line.separator"));
 			            }
 			    } 
 			    catch(FileNotFoundException e)
 			    {
 			    	e.printStackTrace();  //TODO: Need to replace with Sahara Logger
 			    } 
 			    catch(IOException e)
 			    {
 			    	e.printStackTrace();
 			    } 
 			    finally
 			    {
 			    	try
 			    	{
 			    		if(reader != null)
 			    		{
 			    			reader.close();
 			    		}
 			    	} 
 			    	catch (IOException e)
 			    	{
 			    		e.printStackTrace();
 			    	}
 			    }
 
 				blurbDecorator.setBlurb(contents.toString());
 				decoratePage(blurbDecorator);
 			}
 		}
 		
 		/*
 		 *  Decorate the rest of the fields on template:
 		 */
 
 		// 1. Decorate Title
 		titleDecorator = new TitleDecorator(launchPage);
 		titleDecorator.setTitle(experimentName);
 		decoratePage(titleDecorator);
 		
 		// 2. Decorate with a LaunchButton
 		launchButtonDecorator = new LaunchButtonDecorator(launchPage);
 		decoratePage(launchButtonDecorator);
 		
 		// 3. Render the page. Need to set the path first though
 		if(this.path != null) // Should never be the case if it is invoked
 		{
 			launchPage.setLaunchPagePath(this.path);
 			launchPage.render(templator);
 		}
 		
 		return launchPage.getLaunchPagePath(); 
 	}
 	
 	public String decoratePage(ILaunchPage launchPageDecorator)
 	{
 		// Appears to be issue with reflections in OSGi - limitation of 15 times - http://dev.eclipse.org/mhonarc/lists/eclipselink-dev/msg04431.html
 		decoratedPage = launchPageDecorator;
 		decoratedPage.render(templator);
 		return decoratedPage.getLaunchPageName();
 		
 		/*if(launchPageDecorator.getClass().toString().equals(PictureDecorator.class.toString()))
 		{
 
 		}
 		else if(launchPageDecorator.getClass().toString().equals(TitleDecorator.class.toString()))
 		{
 			
 		}*/
 
 	}
 }
