 package xingu.url;
 
 import xingu.url.impl.SimpleUrl;
 import br.com.ibnetwork.xingu.lang.BadParameter;
 import br.com.ibnetwork.xingu.utils.StringUtils;
 
 /**
  * @see http://en.wikipedia.org/wiki/URI_scheme
  * @see http://en.wikipedia.org/wiki/URL
  * 
  *      scheme://username:password@domain:port/path?query_string#fragment_id
  */
 public class UrlParser
 {
 	public enum State {
 		START, SCHEME, AUTH, HOST, PORT, PATH, QUERY, FRAGMENT, END
 	};
 
 	private String input;
 
 	private int schemeEnd = -1, authEnd = -1, hostEnd = -1, portEnd = -1, pathEnd = -1, queryEnd = -1, fragmentEnd = -1;
 
 	public static final int NO_PORT = -1, MISSING_PORT = -2, BAD_PORT = -3;
 
 	public static Url parse(String spec)
 	{
 		if (StringUtils.isEmpty(spec))
 		{
 			throw new IllegalArgumentException("Can't parse '" + spec + "'");
 		}
 
 		// HACK
 		if (!spec.startsWith("http://") && !spec.startsWith("https://") && !spec.startsWith("about:"))
 		{
 			spec = "http://" + spec;
 		}
 
 		UrlParser parser = new UrlParser(spec);
 		String scheme = parser.getScheme();
 		String host = parser.getHost();
 		int port = parser.getPort();
 		String path = parser.getPath();
 		String query = parser.getQuery();
 		String fragment = parser.getFragment();
 		return new SimpleUrl(spec, scheme, host, port, path, query, fragment);
 	}
 
 	private String getScheme()
 	{
 		if (schemeEnd == -1)
 		{
 			return null;
 		}
 		return input.substring(0, schemeEnd);
 	}
 
 	private String getHost()
 	{
 		if (authEnd == hostEnd)
 		{
 			return null;
 		}
 		String host = input.substring(authEnd + 1, hostEnd);
 		char[] array = host.toCharArray();
 		for (char c : array)
 		{
 			if (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != '_')
 			{
				throw new BadParameter("Invalid character in host/domain name: " + host);
 			}
 		}
 		return host;
 	}
 
 	private int getPort()
 	{
 		if (portEnd == -1)
 		{
 			return NO_PORT;
 		}
 		if (portEnd == hostEnd)
 		{
 			if (portEnd < input.length())
 			{
 				char c = input.charAt(portEnd);
 				return c == ':' ? MISSING_PORT : NO_PORT;
 			}
 			return NO_PORT;
 		}
 		if (hostEnd + 1 == portEnd)
 		{
 			return MISSING_PORT;
 		}
 		String value = input.substring(hostEnd + 1, portEnd);
 		if (StringUtils.isEmpty(value))
 		{
 			return MISSING_PORT;
 		}
 		try
 		{
 			return Integer.parseInt(value);
 		}
 		catch (NumberFormatException e)
 		{
 			return BAD_PORT;
 		}
 	}
 
 	private String getPath()
 	{
 		if (pathEnd == -1)
 		{
 			return null;
 		}
 		if (portEnd == pathEnd)
 		{
 			if (portEnd < input.length())
 			{
 				char c = input.charAt(portEnd);
 				return c == '/' ? "/" : null;
 			}
 			return null;
 		}
 		if (portEnd + 1 == pathEnd || pathEnd + 1 == queryEnd)
 		{
 			return null;
 		}
 		if (portEnd < pathEnd)
 		{
 			return input.substring(portEnd, pathEnd);
 		}
 		return null;
 	}
 
 	private String getQuery()
 	{
 		if (queryEnd == pathEnd || queryEnd == pathEnd + 1)
 		{
 			return null;
 		}
 		return input.substring(pathEnd + 1, queryEnd);
 	}
 
 	private String getFragment()
 	{
 		if (fragmentEnd == queryEnd)
 		{
 			return null;
 		}
 		return input.substring(queryEnd + 1, fragmentEnd);
 	}
 
 	public UrlParser(String raw)
 	{
 		input = raw;
 		parse0(raw);
 	}
 
 	private void parse0(String input)
 	{
 		char[] array = input.toCharArray();
 		State state = State.START;
 		for (int i = 0; i < array.length; i++)
 		{
 			switch (state)
 			{
 				case START:
 					state = onStart(array, i);
 					break;
 				case SCHEME:
 					state = onScheme(array, i);
 					if (State.AUTH == state)
 					{
 						i += 2; /* fast forward :// */
 					}
 					break;
 				case AUTH:
 					state = onAuth(array, i);
 					break;
 				case HOST:
 					state = onHost(array, i);
 					break;
 				case PORT:
 					state = onPort(array, i);
 					break;
 				case PATH:
 					state = onPath(array, i);
 					break;
 				case QUERY:
 					state = onQuery(array, i);
 					break;
 				case FRAGMENT:
 					state = onFragment(array, i);
 					break;
 				case END:
 					i = array.length;
 					break;
 			}
 		}
 	}
 
 	private State onStart(char[] array, int i)
 	{
 		char c = array[i];
 		if (Character.isLetterOrDigit(c))
 		{
 			return State.SCHEME;
 		}
 		return State.START;
 	}
 
 	private State onScheme(char[] array, int i)
 	{
 		char c = array[i];
 		switch (c)
 		{
 			case ':':
 				if ((i == 4 || i == 5) && array[i + 1] == '/' && array[i + 2] == '/')
 				{
 					schemeEnd = i;
 					authEnd = i + 2; /* fast forward :// */
 					hostEnd = i + 2; /* fast forward :// */
 					portEnd = i + 2; /* fast forward :// */
 					pathEnd = i + 2; /* fast forward :// */
 					queryEnd = i + 2; /* fast forward :// */
 					fragmentEnd = i + 2; /* fast forward :// */
 					return State.AUTH;
 				}
 				else
 				{
 					return State.END;
 				}
 		}
 		return State.SCHEME;
 	}
 
 	private State onAuth(char[] array, int i)
 	{
 		char c = array[i];
 		boolean finish = false;
 		State result = State.AUTH;
 
 		switch (c)
 		{
 			case '@':
 				authEnd = i;
 				finish = true;
 				result = State.HOST;
 				break;
 
 			case ':':
 				// not sure if we are reading username:passwor or host:port
 				boolean isPort = searchNextCharsForPort(array, i);
 				if (isPort /* maybe a port */)
 				{
 					finish = true;
 					result = State.PORT;
 				}
 				break;
 
 			case '/':
 				finish = true;
 				// endsWithPath = isLastElement(array, i);
 				result = State.PATH;
 				break;
 
 			case '?':
 				finish = true;
 				result = State.QUERY;
 				break;
 
 			case '#':
 				finish = true;
 				result = State.FRAGMENT;
 				break;
 		}
 
 		boolean isLast = isLastElement(array, i);
 		if (isLast)
 		{
 			hostEnd = i + 1;
 			portEnd = i + 1;
 			pathEnd = i + 1;
 			queryEnd = i + 1;
 			fragmentEnd = i + 1;
 		}
 
 		if (finish)
 		{
 			hostEnd = i;
 			portEnd = i;
 			pathEnd = i;
 			queryEnd = i;
 			fragmentEnd = i;
 		}
 		return result;
 	}
 
 	private boolean searchNextCharsForPort(char[] array, int i)
 	{
 		/*
 		 * We only have 65000 or so ports available. Test the next 6 chars
 		 */
 		for (int j = 1; j <= 6; j++)
 		{
 			int idx = i + j;
 			if (idx < array.length)
 			{
 				char c = array[idx];
 				switch (c)
 				{
 					case '/':
 					case '?':
 					case '#':
 						return true;
 
 					case '@':
 						return false;
 				}
 			}
 			else
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private State onHost(char[] array, int i)
 	{
 		char c = array[i];
 		boolean finish = false;
 		State result = State.HOST;
 
 		switch (c)
 		{
 			case ':':
 				finish = true;
 				result = State.PORT;
 				break;
 
 			case '/':
 				finish = true;
 				// endsWithPath = isLastElement(array, i);
 				result = State.PATH;
 				break;
 		}
 
 		if (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != '_' && c != ':' && c != '/')
 		{
			throw new BadParameter("Invalid character in host/domain name: " + array.toString());
 		}
 
 		boolean isLast = isLastElement(array, i);
 		if (isLast)
 		{
 			hostEnd = i + 1;
 			portEnd = i + 1;
 			pathEnd = i + 1;
 			queryEnd = i + 1;
 			fragmentEnd = i + 1;
 		}
 
 		if (finish)
 		{
 			hostEnd = i;
 			portEnd = i;
 			pathEnd = i;
 			queryEnd = i;
 			fragmentEnd = i;
 
 		}
 		return result;
 	}
 
 	private State onPort(char[] array, int i)
 	{
 		char c = array[i];
 		State result = State.PORT;
 		boolean finish = false, addOne = false;
 		boolean isLast = isLastElement(array, i);
 
 		switch (c)
 		{
 			case '/':
 				finish = true;
 				result = State.PATH;
 				// endsWithPath = isLast;
 				break;
 
 			case '?':
 				finish = true;
 				result = State.QUERY;
 				break;
 
 			case '#':
 				finish = true;
 				result = State.FRAGMENT;
 				break;
 
 			default:
 				if (isLast)
 				{
 					addOne = true;
 				}
 		}
 
 		if (isLast || finish)
 		{
 			int position = isLast ? i + 1 : i;
 			portEnd = addOne ? i + 1 : i;
 			pathEnd = addOne ? i + 1 : i;
 			queryEnd = position;
 			fragmentEnd = position;
 		}
 		return result;
 	}
 
 	private State onPath(char[] array, int i)
 	{
 		char c = array[i];
 		boolean isLast = isLastElement(array, i);
 		switch (c)
 		{
 			case '?':
 				pathEnd = i;
 				queryEnd = i;
 				fragmentEnd = i;
 				return State.QUERY;
 
 			case '#':
 				if (pathEnd == queryEnd)
 				{
 					pathEnd = i;
 					queryEnd = i;
 					fragmentEnd = i;
 					// no path, no query
 					return State.FRAGMENT;
 				}
 
 			default:
 				if (isLast)
 				{
 					pathEnd = i + 1;
 					queryEnd = i + 1;
 					fragmentEnd = i + 1;
 				}
 		}
 		return State.PATH;
 	}
 
 	private State onQuery(char[] array, int i)
 	{
 		char c = array[i];
 		switch (c)
 		{
 			case '#':
 				queryEnd = i;
 				fragmentEnd = i;
 				return State.FRAGMENT;
 
 			default:
 				if (isLastElement(array, i))
 				{
 					queryEnd = i + 1;
 					fragmentEnd = i + 1;
 				}
 		}
 		return State.QUERY;
 	}
 
 	private State onFragment(char[] array, int i)
 	{
 		if (isLastElement(array, i))
 		{
 			fragmentEnd = i + 1;
 		}
 		return State.FRAGMENT;
 	}
 
 	private boolean isLastElement(char[] array, int i)
 	{
 		return i + 1 == array.length;
 	}
 }
