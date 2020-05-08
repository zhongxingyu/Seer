 package info.plagiatsjaeger;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.mozilla.intl.chardet.nsDetector;
 import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
 
 
 /**
  * Klasse zum Laden von Daten.
  * 
  * @author Andreas
  */
 public class SourceLoader
 {
 	public static final Logger	_logger				= Logger.getLogger(SourceLoader.class.getName());
 
 	private static final String	DEFAULT_CONTENTTYPE	= ConfigReader.getPropertyString("DEFAULTCONTENTTYPE");
 	private static final String	CONTENTTYPE_PATTERN	= ConfigReader.getPropertyString("CONTENTTYPEPATTERN");
 
	private String		_detectedCharset;
 
 	/**
 	 * Laed eine Website. (Prueft das verwendete Charset und bereinigt die URL)
 	 * 
 	 * @param strUrl
 	 * @return
 	 */
 	public String loadURL(String strUrl)
 	{
 		return loadURL(strUrl, true);
 	}
 
 	public String loadURL(String strUrl, boolean detectCharset)
 	{
 		return loadURL(strUrl, true, true);
 	}
 
 	/**
 	 * Laed den Text einer Webseite.
 	 * 
 	 * @param strUrl
 	 * @return
 	 */
 	public String loadURL(String strUrl, boolean detectCharset, boolean cleanUrl)
 	{
 		String result = "";
 		try
 		{
 			if (cleanUrl)
 			{
 				strUrl = cleanUrl(strUrl);
 			}
 			URL url = new URL(strUrl);
 			URLConnection urlConnection = url.openConnection();
 			// Pattern zum auffinden des contenttypes
 			String charset = DEFAULT_CONTENTTYPE;
 			String contentType = urlConnection.getContentType();
 			if (contentType != null)
 			{
 				Pattern pattern = Pattern.compile(CONTENTTYPE_PATTERN);
 				Matcher matcher = pattern.matcher(urlConnection.getContentType());
 				// Wenn ein Contenttype gefunden wird, wird dieser verwendet,
 				// sonst
 				// der defaul wert
 				if (matcher.matches())
 				{
 					charset = matcher.group(1);
 					_logger.info("Charset detected: " + charset + "(URL: " + strUrl + ")");
 					result = Jsoup.parse(url.openStream(), charset, strUrl).text();
 				}
 				else
 				{
 					_logger.info("No match found " + strUrl);
 					if (detectCharset)
 					{
 						detectCharset(url.openStream());
 						result = Jsoup.parse(url.openStream(), _detectedCharset, strUrl).text();
 					}
 				}
 			}
 			else
 			{
 				_logger.info("CONTENT_TYPE IS null " + strUrl);
 				if (detectCharset)
 				{
 					detectCharset(url.openStream());
 					result = Jsoup.parse(url.openStream(), _detectedCharset, strUrl).text();
 				}
 			}
 		}
 		catch (MalformedURLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL MalformedURLException";
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL UnsupportedEncodingException";
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL IOException";
 		}
 		return result;
 	}
 
	private void detectCharset(InputStream stream)
 	{
 		nsDetector detector = new nsDetector();
 		detector.Init(new nsICharsetDetectionObserver()
 		{
 			@Override
 			public void Notify(String charset)
 			{
 				_logger.info("Charset detected: " + charset);
 				_detectedCharset = charset;
 			}
 		});
 		BufferedInputStream bufferedInputStream;
 		try
 		{
 			bufferedInputStream = new BufferedInputStream(stream);
 			byte[] buffer = new byte[1024];
 			int length;
 			boolean done = false;
 			boolean isAscii = true;
 
 			while ((length = bufferedInputStream.read(buffer, 0, buffer.length)) != -1)
 			{
 				// Kontrollieren ob der Stream nur Ascii zeichen enthaelt
 				if (isAscii) isAscii = detector.isAscii(buffer, length);
 				// DoIt Wenn keine Ascii vorhanden sind und die detection noch
 				// nicht fertig ist
 				if (!isAscii && !done) done = detector.DoIt(buffer, length, false);
 			}
 			detector.DataEnd();
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 	}
 
 	public static String loadFile(String filePath)
 	{
 		return loadFile(filePath, true);
 	}
 
 	/**
 	 * Laed eine Datei.
 	 * 
 	 * @param filePath
 	 * @return
 	 */
 	public static String loadFile(String filePath, boolean convertToUTF8)
 	{
 		String result = "";
 		FileInputStream inputStream = null;
 		DataInputStream dataInputStream = null;
 
 		StringBuilder stringBuilder = new StringBuilder();
 		String charset = "ISO-8859-1";
 		try
 		{
 			inputStream = new FileInputStream(filePath);
 			String line = "";
 			byte[] array = IOUtils.toByteArray(inputStream);
 			// Detect charset
 			if (array != null && array.length >= 2)
 			{
 				if (array[0] == -1 && array[1] == -2)
 				{
 					// UTF-16 big Endian
 					charset = "UTF-16";
 				}
 				else if (array[0] == -2 && array[1] == -1)
 				{
 					// UTF-16 little Endian
 					charset = "UTF-16";
 				}
 				else if (array.length >= 3 && array[0] == -17 && array[1] == -69 && array[2] == -65)
 				{
 					// UTF-8
 					charset = "UTF-8";
 				}
 			}
 
 			System.out.println(charset);
 			inputStream = new FileInputStream(filePath);
 			dataInputStream = new DataInputStream(inputStream);
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream, charset));
 			while ((line = bufferedReader.readLine()) != null)
 			{
 				if (stringBuilder.length() > 0)
 				{
 					stringBuilder.append("\n");
 				}
 				stringBuilder.append(line);
 			}
 
 		}
 		catch (FileNotFoundException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = "FAIL FileNotFoundException";
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			result = "FAIL IOException";
 		}
 		finally
 		{
 			if (dataInputStream != null)
 			{
 				try
 				{
 					dataInputStream.close();
 				}
 				catch (IOException e)
 				{
 					_logger.fatal(e.getMessage(), e);
 					e.printStackTrace();
 				}
 				if (charset == "UTF-8") stringBuilder.deleteCharAt(0);
 				result = stringBuilder.toString();
 				if(charset == "ISO-8859-1" && (result.contains("ü") || result.contains("ä") || result.contains("ö")))
 				{
 					try
 					{
 						byte[] bytes = result.getBytes("ISO-8859-1");
 						result = new String(bytes, "UTF-8");
 					}
 					catch (UnsupportedEncodingException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 
 				if (convertToUTF8)
 				{
 					try
 					{
 						_logger.info("Before encodeing: " + result);
 						result = new String(Charset.forName("UTF-8").encode(result).array(), charset);
 						_logger.info("After encodeing: " + result);
 					}
 					catch (UnsupportedEncodingException e)
 					{
 						_logger.fatal(e.getMessage(), e);
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Bereinigt eine Url, sodass sie immer vollstaendig ist
 	 * 
 	 * @param dirtyUrl
 	 * @return result
 	 */
 	public static String cleanUrl(String dirtyUrl)
 	{
 		String result = "";
 		dirtyUrl = dirtyUrl.replaceAll("www.", "");
 		dirtyUrl = dirtyUrl.replaceAll("http://", "");
 		dirtyUrl = dirtyUrl.replaceAll("https://", "");
 		result = "http://www." + dirtyUrl;
 		_logger.info("Dirty-URL: " + dirtyUrl);
 		_logger.info("Clean-URL: " + result);
 		return result;
 	}
 }
