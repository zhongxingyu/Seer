 package ecologylab.concurrent;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import ecologylab.generic.Generic;
 import ecologylab.io.BasicSite;
 import ecologylab.io.Downloadable;
 import ecologylab.net.ParsedURL;
 
 /**
  * @author Blake Dworaczyk 
  * Represents an HTML page that can be downloaded. This Downloadable acquires the full 
  * text of an HTML page.
  */
 public class DownloadableHtmlPage implements Downloadable
 {
     /**
      * An identifier used to keep track of this Downloadable. This
      * is not actually used internally, so this can be anything.
      */
     String identifier;
     
     /**
      * The ParsedURL to download and retrieve.
      */
     ParsedURL purl;
     
     /**
      * The initial size of the byte buffer.
      */
     int initialBufferSize  = 320000;
     
     /**
      * The number of bytes to read at a time from the server.
      */
     int charChunkSize      = 1000; 
     
     boolean isDone = false;
     
     /**
      * The HTML content.
      */
     String finalContent;
     
     /**
      * The number of timeouts that have occured trying to download this page.
      */
     int numTimeouts = 0;
     
     /**
      * Maximum number of timeouts after which the download is aborted.
      */
     final static int MAX_TIMEOUTS = 3;
     
     /**
      * The number of times we have retried downloading this page (not counting timeouts).
      */
     int numRetries = 0;
     
     /**
      * The maximum number of times to try to redownload this page after an IO error.
      */
     final static int MAX_RETRIES = 3;
     
     /**
      * Create a new Downloadble object, one that downloads HTML pages.
      * 
      * @param identifier An arbitrary string identifier
      * @param purl  The URL to download
      * @param initialBuferSize  The initial size of the byte buffer
      * @param byteChunkSize     The number of bytes downloaded per chunk.
      */
     public DownloadableHtmlPage(String identifier, ParsedURL purl, int initialBuferSize, int byteChunkSize)
     {
         this(identifier, purl);
         
         this.initialBufferSize  = initialBuferSize;
         this.charChunkSize      = byteChunkSize;
     }
     
     public DownloadableHtmlPage(String identifier, ParsedURL purl)
     {
         this.identifier         = identifier;
         this.purl               = purl;
     }
     
     public void performDownload() throws IOException
     {
         System.out.println("Downloading: " + purl);
         
         InputStream inputStream = purl.url().openConnection().getInputStream();
         InputStreamReader isReader = new InputStreamReader(inputStream);
         
         char[] charBuffer = new char[initialBufferSize];
         
         int beginIndex  = 0;
         int endIndex    = charChunkSize - 1;
         while (isReader.read(charBuffer, beginIndex, endIndex-beginIndex) != -1)
         {
             beginIndex  += charChunkSize;
             endIndex    += charChunkSize;
         }
         
         finalContent    = String.valueOf(charBuffer);
         isDone          = true;
     }
 
     public boolean isDownloadDone()
     {
         return isDone;
     }
 
     public boolean handleTimeout()
     {
         numTimeouts++;
         
         return (numTimeouts >= MAX_TIMEOUTS);
     }
 
     public void handleIoError()
     {
         System.err.println("DownloadableHtmlPage: IO Error, retrying a max of " + MAX_RETRIES + " times.");
         
         Generic.sleep(1000); //sleep one second
         
         while (numRetries <= MAX_RETRIES)
         {
             numRetries++;
             try
             {
                 performDownload();
             } catch (Exception e)
             {
                 System.err.println("IO Error number: " + numRetries);
                 e.printStackTrace();
             }
         }
     }
 
     
     public void downloadAndParseDone() {}
     
     /**
      * Get the final HTML content of the downloaded page.
      * @return
      */
     public String getContent()
     {
         return finalContent;
     }
     
     public ParsedURL getDownloadablePurl()
     {
     		return purl;
     }
 
 	public boolean isRecycled()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean cancel()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public BasicSite getSite()
 	{
 		return null;
 	}
 
   /**
    * 
    * @return	What to tell the user about what is being downloaded.
    */
   public String message()
   {
   	return purl.toString();
   }
  
  public ParsedURL purl()
  {
	  return purl;
  }
 
 }
