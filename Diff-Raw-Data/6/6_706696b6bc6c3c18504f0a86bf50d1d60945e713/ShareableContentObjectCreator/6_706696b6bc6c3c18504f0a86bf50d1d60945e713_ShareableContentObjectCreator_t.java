 package au.edu.labshare.schedserver.scormpackager.lila;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 import java.util.zip.Deflater;
 import java.util.Collection;
 import java.util.Iterator;
 
 //Needed for Manifest creation
 import au.edu.labshare.schedserver.scormpackager.lila.Manifest;
 import au.edu.labshare.schedserver.scormpackager.manifest.Dependency;
 import au.edu.labshare.schedserver.scormpackager.manifest.Item;
 import au.edu.labshare.schedserver.scormpackager.manifest.MetaData;
 import au.edu.labshare.schedserver.scormpackager.manifest.Organization;
 import au.edu.labshare.schedserver.scormpackager.manifest.Resource;
 import au.edu.labshare.schedserver.scormpackager.manifest.ResourceFile;
 
 public class ShareableContentObjectCreator extends au.edu.labshare.schedserver.scormpackager.ShareableContentObjectCreator 
 {
 	private ManifestXMLDecorator manifestXMLDecorator;
 	
 	//TODO - this is to be replaced by reading from Properties file. 
 	private static final String SCO_FILENAME = null;
 	private static final String GENERIC_TITLE = "Generic Title";
 	private static final String DEFAULT_INSTITUTION = "LabShare";
 	private static final String SCO_FILEPATH = "/SCOs";
 	private static final int BUFFER_SIZE = 18024;
 	
 	@Override
 	public String createSCO(String title, Collection <File> assets, String LMSName) 
 	{
 		String zipFileName = null;
 		String filePathSCO = null;
 		String filePathManifest = null;
 		FileInputStream fileInStream = null;
 		FileOutputStream fileOutStream = null;
 		ZipOutputStream zipFileOutStream = null;
 		byte[] buffer = new byte[BUFFER_SIZE];
 		Manifest imsmanifest = null;
 		
 		//If no title was provided use generic name placeholder.
 		if(title == null)
 			title = GENERIC_TITLE;
 		
 
 		//Create the manifest
 		String[] titles = new String[1];   //TODO Need to extract the title from the *.html files (assets).
 		titles[0] = title;                 //TODO WARNING: This is just temporary. Extracting Title should be done before hand!!!
 		imsmanifest = createManifest(titles, assets);
 		
 		//Decorate the manifest with relevant XML information
 		manifestXMLDecorator = new ManifestXMLDecorator();
 		filePathManifest = manifestXMLDecorator.decorateManifest(title, assets); 
 		
 		//Zip the contents into a file
 		try 
 		{
 			fileOutStream = new FileOutputStream(SCO_FILENAME);
 		} 
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace(); //TODO Replace with Sahara Logger as part of refactoring.
 		}
 
 		if(fileOutStream != null)
 		{
 			try
 			{
 				zipFileOutStream  = new ZipOutputStream(fileOutStream);
 				zipFileOutStream.setLevel(Deflater.DEFAULT_COMPRESSION);
 				
 				Iterator <File> iter = assets.iterator();
 				while(iter.hasNext())
 				{
 					fileInStream = new FileInputStream(iter.next());
 					//TODO The below string conversion does not handle the lib/ direcctory structure
 					zipFileOutStream.putNextEntry(new ZipEntry(iter.next().getName()));
 					int len;
 				    while ((len = fileInStream.read(buffer)) > 0)
 				    {
 				    	zipFileOutStream.write(buffer, 0, len);
 				    }
 				    				    	
 				    zipFileOutStream.closeEntry();
 				    fileInStream.close();
 				}
 				
 				//Add the imsmanifest.xml file to SCO
 				fileInStream = new FileInputStream(filePathManifest);
 				zipFileOutStream.putNextEntry(new ZipEntry(filePathManifest));
 				int len;
 			    while ((len = fileInStream.read(buffer)) > 0)
 			    {
 			    	zipFileOutStream.write(buffer, 0, len);
 			    }
 			    zipFileOutStream.closeEntry();
 			    fileInStream.close();
 			    
 			    
 			    filePathSCO = SCO_FILEPATH + "/" + zipFileName + ".zip";
 				zipFileOutStream.close();
 				
 				//TODO Need to associate the ZipFile object type to the zipFileOutStream/ZipEntries.
 				return filePathSCO;
 			}
 			catch(IOException e)
 			{
 				e.printStackTrace(); //TODO replace with Sahara Logger as part of refactoring.
 			}
 			catch(NullPointerException e)
 			{
 				e.printStackTrace(); //TODO replace with Sahara Logger as part of refactoring.
 			}
 			
 			return null;
 		}
 		else
 			return null;
 	}
 
 	private Manifest createManifest(String[] titles, Collection<File> assets) 
 	{
 		Manifest manifest = null;
 		Collection<Resource> resources;
 		Collection<Organization> organizations;
 		String[] institutions = new String[1];
 		
 		institutions[0] = ShareableContentObjectCreator.DEFAULT_INSTITUTION;
 
 		manifest = new Manifest();
		File[] files;
		files = (File[])assets.toArray(new File[assets.size()]);
		organizations = manifest.generateOrganisations(institutions, titles, files);
		resources = manifest.generateResources(files);
 
 		//Do a quick check to see if the resources and organizations are not empty
 		if(organizations != null && resources !=null)
 			return manifest;
 		else
 			return null;
 	}
 
 	@Override
 	public boolean validateApplicationProfile(ZipFile SCO) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean validateManifest(ZipFile SCO) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean validateMetaData(ZipFile SCO) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean validatePresentationNavigationInfo(ZipFile SCO) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean validateSequencing(ZipFile SCO) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	@Override
 	public boolean validateLMSConnection(ZipFile jsLMSFile)
 	{
 		// TODO Depending on the structure of the 
 		return false;
 	}
 
 	@Override
 	public String createPIF(String title, Collection<File> content) 
 	{
 		return createSCO(title, content, null);
 	}
 
 	@Override
 	public boolean validateContent(ZipFile PIF) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
