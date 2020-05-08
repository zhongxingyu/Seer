 /**
  * This program takes a website URL and Directory, prints all the image URLs from the web page, and 
  * saves all the images to the specified Directory.
  * @author Andrew Knapp
  * @version 1.0
  */
 public class collectImages 
 {
 
 	public static void main(String[] args) 
 	{
		if (args.length != 2) 
		{
             System.err.println(" Incorrect number of arguments");
             System.err.println(" Usage: ");
             System.err.
             println("\tjava collectImages <String URL> <output directory>");
             System.exit(1);
        }
 		
 		String downloadSite = args[0];
 		String fileDirectory = args[1];
 		imageCollector page = new imageCollector(downloadSite, fileDirectory);
 		page.fetchWebsite(downloadSite);
 		page.downloadImages();
 		page.printImageURLs();
 	}
 
 }
