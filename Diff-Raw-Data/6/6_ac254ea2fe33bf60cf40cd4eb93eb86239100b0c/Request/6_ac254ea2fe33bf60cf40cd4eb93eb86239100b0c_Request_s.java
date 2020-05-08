 package org.ukiuni.lighthttpserver.request;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.ukiuni.lighthttpserver.util.ByteReader;
 import org.ukiuni.lighthttpserver.util.StreamUtil;
 
 public class Request {
 	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
 	public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
 	public static final String HEADER_USER_AGENT = "User-Agent";
 	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
 	public static final String HEADER_ORIGIN = "Origin";
 	public static final String HEADER_HOST = "Host";
 	public static final String HEADER_COOKIE = "Cookie";
 	public static final Object HEADER_CONTENT_LENGTH = "Content-Length";
 	public static final Object HEADER_CONTENT_TYPE = "Content-Type";
 	private Map<String, String> headers = new HashMap<String, String>();
 	private Map<String, String> parameters = new HashMap<String, String>();
 	private Map<String, String> cookies = new HashMap<String, String>();
 	private Map<String, ParameterFile> parametersFile = new HashMap<String, ParameterFile>();
 	private Map<String, List<ParameterFile>> parametersFiles = new HashMap<String, List<ParameterFile>>();
 	private String protocol;
 	private String method;
 	private String path;
 	private String value;
 	private ByteReader byteReader;
 	private String charset;
 
 	public void dumpHeader(PrintStream out) {
 		Set<String> keySet = headers.keySet();
 		for (String key : keySet) {
 			out.println(key + ":" + headers.get(key));
 		}
 	}
 
 	public String getAbsoluteUrl() {
 		String origin = headers.get(HEADER_ORIGIN);
 		if (null == origin) {
 			String protocol = this.protocol.toLowerCase().startsWith("http") ? "http://" : "https://";
 			origin = protocol + headers.get(HEADER_HOST);
 		}
 		return origin + getPath();
 	}
 
 	public String getAbsoluteParentUrl() {
 		String absolutePath = getAbsoluteUrl();
 		return absolutePath.substring(0, absolutePath.lastIndexOf("/") + 1);
 	}
 
 	public Map<String, String> getHeaders() {
 		return headers;
 	}
 
 	public String getHeader(String key) {
 		return headers.get(key);
 	}
 
 	public Map<String, String> getParameters() {
 		return parameters;
 	}
 
 	public String getParameter(String key) {
 		return parameters.get(key);
 	}
 
 	public Map<String, ParameterFile> getParameterFile() {
 		return parametersFile;
 	}
 
 	public ParameterFile getParameterFile(String key) {
 		return parametersFile.get(key);
 	}
 
 	public Map<String, List<ParameterFile>> getParametersFiles() {
 		return parametersFiles;
 	}
 
 	public int getParameterFileLength(String key) {
 		return parametersFiles.get(key).size();
 	}
 
 	public ParameterFile getParameterFile(String key, int index) {
 		List<ParameterFile> parametersFile = parametersFiles.get(key);
 		if (null == parametersFile) {
 			return null;
 		}
 		if (index >= parametersFile.size()) {
 			return null;
 		}
 		return parametersFile.get(index);
 	}
 
 	public void saveParameterFile(String key, String filePath) throws IOException {
 		byte[] content = getParameterFile(key).content;
 		save(filePath, content);
 	}
 
 	public void saveParameterFile(String key, int index, String filePath) throws IOException {
 		byte[] content = getParameterFile(key, index).content;
 		save(filePath, content);
 	}
 
 	private void save(String filePath, byte[] content) throws FileNotFoundException, IOException {
 		FileOutputStream fout = null;
 		try {
 			File file = new File(filePath);
 			file.getParentFile().mkdirs();
 			fout = new FileOutputStream(file);
 			fout.write(content);
 		} finally {
 			StreamUtil.closeQuietry(fout);
 		}
 	}
 
 	public String getMethod() {
 		return method;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public List<String> parseAcceptCharset() {
 		String headerCharset = headers.get(HEADER_ACCEPT_CHARSET);
 		List<String> returnList = parseHeader(headerCharset);
 		return returnList;
 	}
 
 	public List<String> parseAcceptLanguage() {
 		String headerCharset = headers.get(HEADER_ACCEPT_LANGUAGE);
 		List<String> returnList = parseHeader(headerCharset);
 		return returnList;
 	}
 
 	private List<String> parseHeader(String headerCharset) {
 		if (null == headerCharset) {
 			return Collections.emptyList();
 		}
 		StringTokenizer token = new StringTokenizer(headerCharset, ",");
 		List<String> returnList = new ArrayList<String>();
 		while (token.hasMoreElements()) {
 			returnList.add(token.nextToken());
 		}
 		return returnList;
 	}
 
 	public static Request parseInput(InputStream inputStream, String charset) throws IOException {
 		Request request = new Request();
 		request.byteReader = new ByteReader(inputStream, charset, "\r\n".getBytes());
 		request.charset = charset;
 		return request.parseInput();
 	}
 
 	public void close() {
 		StreamUtil.closeQuietry(byteReader);
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		this.clone();
 	}
 
 	public Request parseInput() throws IOException {
 		String methodLine = this.byteReader.readLine();
 		if (null == methodLine) {
 			throw new IOException("no request input");
 		}
 		StringTokenizer urlParamToken = new StringTokenizer(methodLine, " ");
 		this.method = urlParamToken.nextToken();
 		String pathAndParam = urlParamToken.nextToken();
 		if (pathAndParam.contains("?")) {
 			int questIndex = pathAndParam.indexOf("?");
 			this.path = pathAndParam.substring(0, questIndex);
 			parseParameters(pathAndParam.substring(questIndex + 1), charset);
 		} else {
 			this.path = pathAndParam;
 		}
 		this.protocol = urlParamToken.nextToken();
 
 		String headerLine = this.byteReader.readLine();
 		int length = -1;
 		String contentType = null;
 		String boundary = null;
 		while (null != headerLine && !"".equals(headerLine)) {
 			int coronIndex = headerLine.indexOf(":");
 			if (coronIndex > 0) {
 				String key = headerLine.substring(0, coronIndex).trim();
 				String value = headerLine.substring(coronIndex + 1).trim();
 				this.headers.put(key, value);
 				if (HEADER_CONTENT_LENGTH.equals(key)) {
 					length = Integer.valueOf(value);
 				} else if (HEADER_CONTENT_TYPE.equals(key)) {
 					if (value.contains(";")) {
 						contentType = value.substring(0, value.indexOf(";"));
 						if (value.contains("-")) {
 							boundary = value.substring(value.lastIndexOf("=") + 1);
 						}
 					} else {
 						contentType = value;
 					}
 				} else if (HEADER_COOKIE.equals(key)) {
 					StringTokenizer token = new StringTokenizer(headerLine, ";,");
 					while (token.hasMoreTokens()) {
 						String keyValue = token.nextToken().trim();
 						int equalIndex = keyValue.indexOf("=");
 						if (0 < equalIndex) {
 							String cookieKey = keyValue.substring(0, equalIndex);
 							String cookieValue = keyValue.substring(equalIndex + 1);
 							this.cookies.put(cookieKey.trim(), cookieValue);
 						} else {
 							this.cookies.put(keyValue, null);
 						}
 					}
 				}
 			}
 			headerLine = this.byteReader.readLine();
 		}
 		if (-1 != length) {
 			if (null == contentType || "application/x-www-form-urlencoded".equals(contentType.trim())) {
				char[] valueChars = new char[length];
				for (int i = 0; i < valueChars.length; i++) {
					valueChars[i] = (char) this.byteReader.read();
				}
 				String content = new String(valueChars);
 				this.parseParameters(content, charset);
 				this.value = content;
 			} else if ("multipart/form-data".equals(contentType.trim()) || "multipart/mixed".equals(contentType.trim())) {
 				String line = this.byteReader.readLine();
 				while (null != line && !line.equals("--" + boundary + "--")) {
 					if (line.equals("--" + boundary)) {
 						line = this.byteReader.readLine();
 						ParamHeader header = new ParamHeader();
 						while (!"".equals(line.trim())) {
 							decodeParamHeader(line, header);
 							line = this.byteReader.readLine();
 						}
 						if (null == header.paramContentType) {
 							line = this.byteReader.readLine();
 							this.parameters.put(header.contentDescriptionMap.get("name"), line);
 							line = this.byteReader.readLine();
 						} else if (header.paramContentType.toLowerCase().equals("multipart/mixed")) {
 							line = this.byteReader.readLine();
 							ParamHeader additionalHeader = new ParamHeader();
 							while (!"".equals(line.trim())) {
 								decodeParamHeader(line, additionalHeader);
 								line = this.byteReader.readLine();
 							}
 							byte[] data = this.byteReader.readTo(("\r\n--").getBytes());
 							String multiDataEndSeparator = "--" + header.paramAdditionalBoundary + "--";
 							List<ParameterFile> files = new ArrayList<ParameterFile>();
 							this.parametersFiles.put(header.contentDescriptionMap.get("name"), files);
 							line = "--" + this.byteReader.readLine();
 							while (!line.equals(multiDataEndSeparator)) {
 								ParameterFile parameterFile = new ParameterFile();
 								parameterFile.fileName = additionalHeader.contentDescriptionMap.get("filename");
 								parameterFile.contentType = additionalHeader.paramContentType;
 								parameterFile.content = data;
 								line = "--" + this.byteReader.readLine();
 							}
 						} else {
 							byte[] data = this.byteReader.readTo("\r\n--".getBytes());
 							ParameterFile parameterFile = new ParameterFile();
 							parameterFile.contentType = header.paramContentType;
 							parameterFile.fileName = header.contentDescriptionMap.get("filename");
 							parameterFile.content = data;
 							ParameterFile existFile = this.parametersFile.get(header.contentDescriptionMap.get("name"));
 							if (null != existFile) {
 								List<ParameterFile> parameterFiles = this.parametersFiles.get(header.contentDescriptionMap.get("name"));
 								if (null == parameterFiles) {
 									parameterFiles = new ArrayList<ParameterFile>();
 									this.parametersFiles.put(header.contentDescriptionMap.get("name"), parameterFiles);
 									parameterFiles.add(existFile);
 								}
 								parameterFiles.add(parameterFile);
 							} else {
 								this.parametersFile.put(header.contentDescriptionMap.get("name"), parameterFile);
 							}
 							// FileOutputStream fout = new FileOutputStream(new
 							// File("data/" +
 							// contentDescriptionMap.get("filename")));
 							// fout.write(data);
 							// fout.close();
 							line = "--" + this.byteReader.readLine();
 						}
 					}
 				}
 			}
 		}
 		return this;
 	}
 
 	private static void decodeParamHeader(String line, ParamHeader header) {
 		int colonIndex = line.indexOf(":");
 		String key = line.substring(0, colonIndex);
 		String value = line.substring(colonIndex + 1);
 		if (key.trim().toLowerCase().equals("content-disposition")) {
 			StringTokenizer tokenizer = new StringTokenizer(value, ";");
 			header.paramContentDescription = tokenizer.nextToken().trim();
 			while (tokenizer.hasMoreTokens()) {
 				String additionalParam = tokenizer.nextToken();
 				int equalIndex = additionalParam.indexOf("=");
 				if (equalIndex > 1) {
 					String contentDescriptionKey = additionalParam.substring(0, equalIndex).trim();
 					String valueWithDust = additionalParam.substring(equalIndex + 1).trim();
 					header.contentDescriptionMap.put(contentDescriptionKey, valueWithDust.substring(1, valueWithDust.length() - 1));
 				}
 			}
 		} else if (key.trim().toLowerCase().equals("content-type")) {
 			if (value.contains(",")) {
 				header.paramContentType = value.substring(0, value.indexOf(",")).trim().toLowerCase();
 				String boundarySrc = value.substring(value.indexOf(",") + 1);
 				header.paramAdditionalBoundary = boundarySrc.substring(boundarySrc.indexOf("=") + 1).trim();
 			} else {
 				header.paramContentType = value.trim();
 			}
 		}
 	}
 
 	private void parseParameters(String parameters, String charset) throws UnsupportedEncodingException {
 		StringTokenizer paramToken = new StringTokenizer(parameters, "&");
 		while (paramToken.hasMoreTokens()) {
 			String paramKeyAndValue = paramToken.nextToken();
 			int equalIndex = paramKeyAndValue.indexOf("=");
 			if (0 < equalIndex) {
 				String key = paramKeyAndValue.substring(0, equalIndex);
 				String value = paramKeyAndValue.substring(equalIndex + 1);
 				this.parameters.put(key, URLDecoder.decode(value, charset));
 			} else {
 				this.parameters.put(paramKeyAndValue, null);
 			}
 
 		}
 	}
 
 	public String getCookie(String key) {
 		return cookies.get(key);
 	}
 
 	public String getFileName() {
 		if (null == path) {
 			return null;
 		}
 		int lastSlash = path.lastIndexOf("/");
 		if (lastSlash < 0 || lastSlash + 1 >= path.length()) {
 			return null;
 		}
 		return path.substring(lastSlash + 1);
 	}
 
 	private static class ParamHeader {
 		String paramContentDescription;
 		String paramContentType;
 		String paramAdditionalBoundary;
 		Map<String, String> contentDescriptionMap = new HashMap<String, String>();
 	}
 }
