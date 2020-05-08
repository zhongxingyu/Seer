 package cm.generic;
 
 import java.net.*;
 import java.util.*;
 
 
 /**
  * @author andruid
  *
  * Methods for manipulating </ode>String</code> and <code>StringBuffer</code>s.
  */
 public class StringTools
 extends Debug
 {
    static final String[]	oneDotDomainStrings = 
    {
       "com", "edu", "gov", "org", "net", "tv",
    };
    static final HashMap	oneDotDomains	= 
       Generic.buildHashMapFromStrings(oneDotDomainStrings);
 
    public static final String	EMPTY_STRING	= "";
    
 /**
  * Changes the StringBuffer to lower case, in place, without any new storage
  * allocation.
  */
    public static final void toLowerCase(StringBuffer buffer)
    {
       int length	= buffer.length();
       for (int i=0; i<length; i++)
       {
 	 char c		= buffer.charAt(i);
 //	 if (Character.isUpperCase(c))
 	 // A = 0x41, Z = 0x5A; a = 0x61, z = 0x7A
 	 if ((c >='A') && (c <= 'Z'))
 	 {
 	    c	       += 0x20;
 	    buffer.setCharAt(i, c);
 	 }
       }
    }
    public static final boolean sameDomain(URL url1, URL url2)
    {
       return domain(url1).equals(domain(url2));
    }
    public static final String domain(URL url)
    {
       return domain(url.getHost());
    }
 /**
  * Useful for finding common domains.
  */
    public static final String domain(String urlString)
    {
       if ((urlString == null) || (urlString.length() == 0))
 	 return null;
       int end	= urlString.length() - 1;
       int domainStartingDot	= 0;
       boolean foundFirstDot	= false;
       boolean foundSecondDot	= false;
       boolean international	= false;
       String result	= urlString;
       for (int i=end; i>0; i--)
       {
 	 if (urlString.charAt(i) == '.')
 	 {
 	    if (foundFirstDot)
 	    {
 	       if (international && !foundSecondDot)
 	       {
 		  foundSecondDot	= true;
 	       }
 	       else
 	       {
 		  domainStartingDot	= i + 1;
 		  break;
 	       }
 	    }
 	    else
 	    {
 	       foundFirstDot	= true;
 	       String suffix	= urlString.substring(i+1);
 	       international	= !oneDotDomains.containsKey(suffix);
 	    }
 	 }
       }
       return urlString.substring(domainStartingDot, end + 1);
    }
    
 /**
  * Use this method to efficiently get a <code>String</code> from a
  * <code>StringBuffer</code> on those occassions when you plan to keep
  * using the <code>StringBuffer</code>, and want an efficiently made copy.
  * In those cases, <i>much</i> better than 
  * <code>new String(StringBuffer)</code>
  */
    public static final String toString(StringBuffer buffer)
    {
       return buffer.substring(0);
    }
    public static final boolean contains(String in, String toMatch)
    {
       return (in == null) ? false : in.indexOf(toMatch) != -1;
    }
    public static final boolean contains(String in, char toMatch)
    {
       return (in == null) ? false : in.indexOf(toMatch) != -1;
    }
 
 /**
  * Very efficiently forms String representation of url (better than 
  * <code>URL.toExternalForm(), URL.toString()</code>). Doesn't include query or anchor.
  */
    public static final String noAnchorNoQueryPageString(URL u)
    {
       String protocol	= u.getProtocol();
       String authority	= u.getAuthority(); // authority is host:port
       String path	= u.getPath();	    // doesn't include query
 
       int pathLength	= (path == null) ? 0 : path.length();
       // pre-compute length of StringBuffer
       int length =0;
       
       try
       {
     if(protocol != null && authority != null)
	 {
     	length	=
     		protocol.length() + 3 /* :// */ + authority.length() + pathLength;
	 }
    } catch (Exception e)
    {
       Debug.println("protocol="+protocol+" authority="+authority+
 		     u.toExternalForm());
       e.printStackTrace();
    }
       
       StringBuffer result = new StringBuffer(length);
       result.append(protocol).append("://").append(authority).append(path);
 
       return new String(result);
    }
    
    
    public static final String noAnchorPageString(URL u)
    {
       String protocol	= u.getProtocol();
       String authority	= u.getAuthority(); // authority is host:port
       String path	= u.getPath();	    // doesn't include query
       String query  = u.getQuery();
 
       int pathLength	= (path == null) ? 0 : path.length();
       int queryLength	= (query == null) ? 0: query.length();
       
       // pre-compute length of StringBuffer
       int length =0;
       
       try
       {
 	 length	=
 	 protocol.length() + 3 /* :// */ + authority.length() + pathLength + 1/* ? */ + queryLength;
    } catch (Exception e)
    {
       Debug.println("protocol="+protocol+" authority="+authority+
 		     u.toExternalForm());
       e.printStackTrace();
    }
       
       StringBuffer result = new StringBuffer(length);
       result.append(protocol).append("://").append(authority).append(path);
       if(query != null)
       result.append("?").append(query);
 
       return new String(result);
    }
 
    public static final String pageString(URL u)
    {
       String protocol	= u.getProtocol();
       String authority	= u.getAuthority(); // authority is host:port
       String path	= u.getPath();	    // doesn't include query
       String query  = u.getQuery();
       String anchor = u.getRef();
 
       int pathLength	= (path == null) ? 0 : path.length();
       int queryLength	= (query == null) ? 0: query.length();
       int anchorLength	= (anchor == null) ? 0: anchor.length();
       int authorityLength	= (authority == null) ? 0: authority.length();
 
       // pre-compute length of StringBuffer
       int length =0;
       
       try
       {
 	 length	=
 	 protocol.length() + 3 /* :// */ + authorityLength + pathLength
 	    + 1 /* ? */ + queryLength + 1 /* # */ + anchorLength;
    } catch (Exception e)
    {
       Debug.println("protocol="+protocol+" authority="+authority+
 		     u.toExternalForm());
       e.printStackTrace();
    }
       
       StringBuffer result = new StringBuffer(length);
       result.append(protocol).append("://");
       if (authority != null)
 	 result.append(authority);
       if (path != null)
 	 result.append(path);
       if(query != null)
 	 result.append("?").append(query);
       if(anchor != null)
 	 result.append("#").append(anchor);
 
       return new String(result);
    }
    
    public static final URL urlRemoveAnchorIfNecessary(URL source)
    {
       String anchor			= source.getRef();
       return (anchor == null) ? source : urlNoAnchor(source);
    	
    }
    
    public static final URL urlNoAnchor(URL source)
    {
       URL result = null;
       
       if(source==null)
       return result;
       
       try
       {
 	 result= new URL(source.getProtocol(), source.getHost(),
 			 source.getPort(), source.getFile());
       } catch (MalformedURLException e)
       {
 	 e.printStackTrace();
 	 throw new RuntimeException("Cant form noHashUrl from " +
 				    source.toString());
       }
       return result;
    }
 /*      
    public static void main(String[] args)
    {
       for (int i=0; i<args.length; i++)
 	 println(pageString(Generic.getURL(args[i], "oops " + i)));
 	 
    }
  */
    
 /**
  * For example, input "isFileName", output "is file name"
  * 
  * @return 
  */   
    public static String[] seperateLowerUpperCase(String in)
    {
       int n = in.length();
       // pass 1 -- just find out how many transitions there are?
       int numWords = 1;
       for (int i=0; i<n; i++)
       {
 	 char thisChar = in.charAt(i);
 	 if (Character.isUpperCase(thisChar) && (i != 0))
 	    numWords++;
       }
       // pass 2 -- create the result set and fill it in
       String result[]	= new String[numWords];
       int bufferIndex = 0;
       int resultIndex = 0;
       int transition  = 0;
       char[] buffer = new char[n];
       for (int i=0; i<n; i++)
       {
 	 char thisChar = in.charAt(i);
 	 if (Character.isUpperCase(thisChar))
 	 {
 	    thisChar = Character.toLowerCase(thisChar);
 	    if (i > 0)
 	    {
 	       result[resultIndex++]	= 
 		  new String(buffer, transition, (i - transition));
 //	       result[resultIndex++]	= in.substring(transition, i);
 	       transition		= i;
 	    }
 	 }
 	 buffer[i]	= thisChar;
       }
       result[resultIndex]= new String(buffer, transition, (n - transition));
       return result;
    }
 /**
  * Remove all instances of @param c from @arg string
  */   
    public static String remove(String string, char c)
    {
       int index;
       
       while ((index = string.indexOf(c)) > -1)
       {
 	 int length	= string.length();
 	 if (index == 0)
 	    string	= string.substring(1);
 	 else if (index == (length - 1))
 	    string	= string.substring(0, length-1);
 	 else
 	    string	= string.substring(0,index) +string.substring(index+1);
       }
       return string;
    }
    public static final String FIND_PUNCTUATION_REGEX = 
       "(:)|(\\d)|(\\.)|(/++)|(=)|(\\?)|(\\-)|(\\+)|(_)|(%)|(\\,)";        
 /**
  * Use RegEx to turn punctuation into space delimiters.
  */
    public static String removePunctuation(String s)
    {
       int length		= s.length();
       StringBuffer buffy	= new StringBuffer(length);
       
       boolean	wasSpace	= true;
       
       for (int i=0; i<length; i++)
       {
 	 char c			= s.charAt(i);
 	 if (Character.isLetter(c))
 	 {
 	    buffy.append(c);
 	    wasSpace		= false;
 	 }
 	 else
 	 {
 	    if (!wasSpace)
 //	       buffy.append('-');
 	       buffy.append(' ');
 	    wasSpace		= true;
 	 }
       }
       return new String(buffy);
    }
    
    public static String removePunctuation2(String s)
    {
       return s.replaceAll(FIND_PUNCTUATION_REGEX, " ");
    }
 
    public static String removePunctuation(URL u)
    {
       // we don't use the protocol and host:port part in url
       return removePunctuation(u.getPath());
    }
    public static String[] wordsFromURL(URL u)
    {
       String withoutPunctuation = StringTools.removePunctuation(u);
       return StringTools.seperateLowerUpperCase(withoutPunctuation);
    }
    
 
    public static void main(String[] s)
    {
       URL u = Generic.getURL("http://www.bbc.co.uk/eastenders/images/navigation/icon_bbc_one.gif", "foo");
 //      println(removePunctuation("http://www.bbc.co.uk/eastenders/images/navigation/icon_bbc_one.gif"));
       println(removePunctuation(u));
    }
    public static void main2(String[] s)
    {
    		for (int i=0; i<s.length; i++)
    		{
 		   String[] result = seperateLowerUpperCase(s[i]);
 		   System.out.print(s[i] + " -> " );
 		   for (int j=0; j<result.length; j++)
 		      System.out.print(result[j] + " ");
 		   System.out.println();
 		}   	
    }
 }
 
