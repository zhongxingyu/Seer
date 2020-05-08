 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.application;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.zip.GZIPOutputStream;
 
 import com.nginious.http.HttpException;
 import com.nginious.http.HttpRequest;
 import com.nginious.http.HttpResponse;
 import com.nginious.http.HttpStatus;
 import com.nginious.http.common.StringUtils;
 import com.nginious.http.server.ByteRange;
 import com.nginious.http.server.Header;
 import com.nginious.http.server.HeaderException;
 import com.nginious.http.server.HeaderParameter;
 import com.nginious.http.server.HeaderParameters;
 import com.nginious.http.server.MimeTypes;
 
 /**
  * Negotiates, caches and serves static content to HTTP clients. Support for the following content negotiation
  * is implemented as specified in <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC</a>.
  * 	
  * <ul>
  * <li><code>Accept</code> content type matching as specified in section 14.1.</li>
  * <li><code>If-Modified-Since</code> conditional serving as specified in section 14.25.</li>
  * <li><code>If-None-Match</code> conditional serving as specified in section 14.26.</li>
  * <li><code>If-Unmodified-Since</code> conditional serving as specified in section 14.28.</li>
  * <li><code>If-Match</code> conditional serving as specified in section 14.24.</li>
  * <li><code>If-Range</code> conditional serving as specified in section 14.27.</li>
  * <li><code>Range</code> support as specified in section 14.35. Multiple ranges are not supported.</li>
  * </ul>
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 class StaticContent {
 	
	private static final String DEFAULT_FILE = "index.html";
	
 	private static HashSet<String> compressedContentTypes = new HashSet<String>();
 	
 	static {
 		compressedContentTypes.add("application/gzip");
 		compressedContentTypes.add("application/zip");
 		compressedContentTypes.add("application/x-gtar");
 		compressedContentTypes.add("application/compress");
 	}
 	
 	private File baseDir;
 	
 	private String path;
 	
 	private String contentType;
 	
 	private String entityTag;
 	
 	private long length;
 	
 	private long lastModified;
 	
 	private String lastModifiedFormatted;
 	
 	private File contentFile;
 	
 	private byte[] content;
 	
 	private long gzipLength;
 	
 	private File gzipContentFile;
 	
 	private byte[] gzipContent;
 	
 	/**
 	 * Constructs a new static content for the file located at the specified
 	 * base directory and relative path.
 	 * 
 	 * @param baseDir the web application base directory
 	 * @param path file path relative to the web application base directory
 	 */
 	StaticContent(File baseDir, String path) {
 		this.baseDir = baseDir;
		initContentFile(path);
	}
	
	private void initContentFile(String path) {
 		this.path = path;
 		this.contentFile = new File(this.baseDir, path);
 		this.lastModified = contentFile.lastModified();
 		this.lastModifiedFormatted = Header.formatDate(new Date(this.lastModified));
 		this.length = contentFile.length();
 		this.contentType = MimeTypes.getMimeTypeByExtenstion(path);
 		
 		if(this.contentType == null) {
 			this.contentType = "application/octet-stream";
 		}		
 	}
 	
 	/**
 	 * Serves static content using the headers in the specified HTTP request for content
 	 * negotiation. The response is sent in the specified HTTP response.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @throws IOException if unable to read static content file
 	 * @throws HttpException if a HTTP error occurs
 	 */
 	void execute(HttpRequest request, HttpResponse response) throws IOException, HttpException {
		if(contentFile.isDirectory()) {
			initContentFile(this.path + DEFAULT_FILE);
			
			if(!contentFile.exists()) {
				throw new HttpException(HttpStatus.NOT_FOUND, this.path);
			}
		}
		
 		checkAcceptable(request, this.path, this.contentType);
 		
		if(executeIfModifiedSince(request, response, this.contentFile)) {
 			return;
 		}
 		
		if(executeIfNoneMatch(request, response, path, this.contentFile)) {
 			return;
 		}
 		
 		executeIfUnmodifiedSince(request, path, contentFile);
 		boolean httpVersion11 = request.getVersion().equals("HTTP/1.1");
 		
 		if(httpVersion11) {
			executeIfMatch(request, response, path, this.contentFile);
 		}
 		
 		if(executeRangeIfRange(request)) {
 			if(writeRangeContent(request, response, this.path)) {
 				return;
 			}
 		}
 		
 		response.setStatus(HttpStatus.OK);
 		
 		if(httpVersion11) {
 			String date = Header.formatDate(new Date());
 			response.addHeader("Date", date);
 		}
 		
 		response.addHeader("Last-Modified", this.lastModifiedFormatted);
 		response.addHeader("Content-Type", this.contentType);
 		
 		if(httpVersion11) {
 			response.addHeader("ETag", getOrReadEntityTag());
 		}
 		
 		if(checkGzip(request, this.contentType)) {
 			writeGzipContent(response);
 		} else {
 			if(httpVersion11) {
 				response.addHeader("Accept-Ranges", "bytes");
 			}
 			
 			writeContent(response, 0, (int)this.length - 1);			
 		}
 	}
 	
 	/**
 	 * Checks whether or not the client that sent the specified HTTP request accepts response with gzip compressed
 	 * content. A client accepts gzip compressed response if the HTTP request contains a <code>Accept-Encoding</code>
 	 * header with value <code>gzip</code> included in the list. The content type of the file must also not be
 	 * compressed.
 	 * 
 	 * @param request the HTTP request to check for <code>Accept-Encoding</code> header
 	 * @param contentType the file content type
 	 * @return <code>true</code> if the file content can be gzip compressed prior to sending response, <code>false</code>
 	 * 	otherwise
 	 */
 	private boolean checkGzip(HttpRequest request, String contentType) {
 		String acceptEncoding = request.getHeader("Accept-Encoding");
 		return acceptEncoding != null && acceptEncoding.indexOf("gzip") > -1 && !compressedContentTypes.contains(contentType);
 	}
 	
 	/**
 	 * Checks whether or not the specified content type is matched by a <code>Accept</code> header found in the 
 	 * specified HTTP request. See section 14.1 in <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP /1.1 RFC</a> 
 	 * for  detailed information about <code>Accept</code> header matching rules.
 	 * 
 	 * @param request the HTTP request
 	 * @param path the static content file path
 	 * @param contentType the file content type to match against <code>Accept</code> header
 	 * @throws HttpException an exception with status 412 Not Acceptable is thrown if no match
 	 */
 	private void checkAcceptable(HttpRequest request, String path, String contentType) throws HttpException {
 		String accept = request.getHeader("Accept");
 		
 		if(accept == null) {
 			return;
 		}
 		
 		try {
 			Header header = new Header("Accept", accept);
 			
 			if(!header.isAcceptable(contentType)) {
 				throw new HttpException(HttpStatus.NOT_ACCEPTABLE, "content type " + contentType + " not acceptable for path " + path);
 			}
 		} catch(HeaderException e) {
 			throw new HttpException(HttpStatus.NOT_ACCEPTABLE, "content type " + contentType + " not acceptable for path " + path);			
 		}
 	}
 	
 	/**
 	 * Checks whether or not this static content is not modified since the datetime found in the <code>If-Range</code>
 	 * header in the specified HTTP request. See section 14.27 in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC</a> for detailed information.
 	 * 
 	 * @param request the HTTP request
 	 * @return <code>true</code> if partial range of this static content should be returned, <code>false</code> otherwise.
 	 */
 	// Return true = return partial content
 	// Return false = return all content
 	private boolean executeRangeIfRange(HttpRequest request) {
 		String ifRange = request.getHeader("If-Range");
 		String range = request.getHeader("Range");
 		
 		if(ifRange == null) {
 			return range != null;
 		}
 		
 		if(range == null) {
 			return false;
 		}
 		
 		Date ifModifiedSince = Header.parseDate(ifRange);
 		
 		if(ifModifiedSince != null) {
 			return ifModifiedSince.getTime() < this.lastModified;
 		} else {
 			String entityTag = getOrReadEntityTag();
 			
 			if(entityTag == null) {
 				return false;
 			}
 			
 			return entityTag.equals(ifRange);
 		}
 	}
 	
 	/**
 	 * Checks whether or not the specified content file has been modified since the datetime found in any
 	 * <code>If-Modified-Since</code> header of the specified HTTP request. See section 14.25 in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC</a> for detailed information about
 	 * <code>If-Modified-Since</code> header rules.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param contentFile the static content file
 	 * @return <code>true</code if the HTTP request contains a <code>If-Modified-Since</code> header
 	 * with a datetime that is less than the content files modified datetime, <code>false</code>
 	 * otherwise
 	 */
 	private boolean executeIfModifiedSince(HttpRequest request, HttpResponse response, File contentFile) {
 		String ifModifiedValue = request.getHeader("If-Modified-Since");
 		
 		if(ifModifiedValue != null && !ifModifiedValue.equals("")) {			
 			Date ifModifiedSince = Header.parseDate(ifModifiedValue);
 			
 			if(ifModifiedSince != null && ifModifiedSince.getTime() >= contentFile.lastModified()) {
 				response.setStatus(HttpStatus.NOT_MODIFIED);
 				return true;
 			}
 		}
 		
 		return false;
 	}	
 	
 	/**
 	 * Checks that none of the entity tags found in the <code>If-None-Match</code> of the specified HTTP request matches
 	 * the entity tag of this static content. See section 14.26 If-None-Match in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a> for detailed information.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param path path to the static content file
 	 * @param contentFile static content file
 	 * @return <code>true</code> if a <code>If-None-Match</code> exists in the HTTP request and none of the entity tags match,
 	 * 	<code>false</code> otherwise
 	 * @throws HttpException if a HTTP error occurs
 	 */
 	private boolean executeIfNoneMatch(HttpRequest request, HttpResponse response, String path, File contentFile) throws HttpException {
 		String entityTag = getOrReadEntityTag();
 		
 		if(entityTag == null) {
 			return false;
 		}
 		
 		try {
 			String ifNoneMatch = request.getHeader("If-None-Match");
 			
 			if(ifNoneMatch != null) {
 				boolean found = false;
 				Header header = new Header("If-None-Match", ifNoneMatch);
 				HeaderParameters parameters = header.getParameters();
 				HeaderParameter[] allParameters = parameters.getSorted();
 				
 				for(HeaderParameter parameter : allParameters) {
 					String headerTag = parameter.getName();
 					found = headerTag.equals("*") || headerTag.equals(entityTag);
 				}
 				
 				if(found) {
 					response.setStatus(HttpStatus.NOT_MODIFIED);
 					return true;
 				}
 			}
 			
 			return false;
 		} catch(HeaderException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks whether or not this static content is unmodified since the date found in the <code>If-Unmodified-Since</code>
 	 * header found in the specified HTTP request. See section 14.28 If-Match in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a> for detailed information.
 	 * 
 	 * @param request the HTTP request
 	 * @param path path to the static content file
 	 * @param contentFile static content file
 	 * @throws HttpException if static content is modified a <code>HttpException</code> with status 412 Precondition Failed</code>
 	 * 	is thrown
 	 */
 	private void executeIfUnmodifiedSince(HttpRequest request, String path, File contentFile) throws HttpException {
 		String ifUnmodifiedValue = request.getHeader("If-Unmodified-Since");
 		
 		if(ifUnmodifiedValue != null && !ifUnmodifiedValue.equals("")) {			
 			Date ifUnmodifiedSince = Header.parseDate(ifUnmodifiedValue);
 			
 			// If resource has been modified return a 412 Precondition Failed, RFC 2616 - 14.28
 			if(ifUnmodifiedSince != null && ifUnmodifiedSince.getTime() < this.lastModified) {
 				throw new HttpException(HttpStatus.PRECONDITION_FAILED, "resource modified " + path);
 			}
 		}		
 	}
 	
 	/**
 	 * Checks whether or not any of the entity tags defined in the <code>If-Match></code> header found in the
 	 * specified HTTP request matches this static contents entity tag. See section 14.24 If-Match in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a> for detailed information.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param path path to static content file
 	 * @param contentFile static content file
 	 * @throws HttpException if no entity tag matches a <code>HttpException</code> with status 412 Precondition Failed</code>
 	 * 	is thrown
 	 */
 	private void executeIfMatch(HttpRequest request, HttpResponse response, String path, File contentFile) throws HttpException {
 		String entityTag = getOrReadEntityTag();
 		
 		if(entityTag == null) {
 			return;
 		}
 		
 		try {
 			String ifMatch = request.getHeader("If-Match");
 			
 			if(ifMatch != null) {
 				boolean found = false;
 				Header header = new Header("If-Match", ifMatch);
 				HeaderParameters parameters = header.getParameters();
 				HeaderParameter[] allParameters = parameters.getSorted();
 				
 				for(int i = 0; i < allParameters.length && !found; i++) {
 					String headerTag = allParameters[i].getName();
 					found = headerTag.equals("*") || headerTag.equals(entityTag);
 				}
 				
 				if(!found) {
 					throw new HttpException(HttpStatus.PRECONDITION_FAILED, "resource modified " + path);
 				}
 			}
 		} catch(HeaderException e) {
 			return;
 		}
 	}
 	
 	/**
 	 * Writes a range of this static content to the specified HTTP response. The range is retrieved from the
 	 * <code>Range</code> header in the specified HTTP request. See section 14.35 Range in the 
 	 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 RFC 2616</a> for detailed information.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param path path to content for this static content
 	 * @return <code>true</code> if a valid <code>Range</code> header was found and content was written, <code>false</code> otherwise
 	 * @throws IOException if unable to write content
 	 * @throws HttpException if a HTTP error occurs while writing
 	 */
 	private boolean writeRangeContent(HttpRequest request, HttpResponse response, String path) throws IOException, HttpException {
 		String range = request.getHeader("Range");
 		
 		if(range == null) {
 			return false;
 		}
 		
 		try {
 			Header header = new Header("Range", range);
 			ByteRange[] byteRanges = header.createByteRanges((int)this.length);
 			
 			// Multiple ranges not supported for now
 			if(byteRanges.length > 1) {
 				return false;
 			}
 			
 			if(byteRanges[0].getStart() >= this.length) {
 				response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "range " + byteRanges[0].toString() + " can't be satisfied for " + path);
 				int startRange = 0;
 				int endRange = (int)this.length - 1;
 				response.addHeader("Content-Range", startRange + "-" + endRange);
 				return true;
 			}
 			
 			response.setStatus(HttpStatus.PARTIAL_CONTENT);
 			String date = Header.formatDate(new Date());
 			response.addHeader("Content-Type", this.contentType);
 			response.addHeader("ETag", getOrReadEntityTag());
 			
 			response.addHeader("Date", date);
 			response.addHeader("Last-Modified", this.lastModifiedFormatted);
 			response.addHeader("Accept-Ranges", "bytes");
 			response.addHeader("Content-Range", byteRanges[0].toString());
 			writeContent(response, byteRanges[0].getStart(), byteRanges[0].getEnd());
 			return true;
 		} catch(HeaderException e) {
 			// Can't parse byte range, send the whole entity
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Writes this static content GZIP compresses to the specified HTTP response. Data is read from file or from
 	 * local cache. The first request always reads from file. Subsequent requests for this static content are read
 	 * from cache. If content is more than 2097152 bytes uncompressed it is never cached and always read from file.
 	 * 
 	 * @param response the HTTP response
 	 * @throws IOException if unable to write content
 	 * @throws HttpException if a HTTP error occurs while writing
 	 */
 	private void writeGzipContent(HttpResponse response) throws IOException, HttpException {
 		response.addHeader("Content-Encoding", "gzip");
 		
 		if(this.length > 2097152) {
 			writeFileGzipContent(response);
 		} else {
 			writeCachedGzipContent(response);
 		}
 	}
 	
 	/**
 	 * Writes this static content to the specified HTTP response. Data is written from the specified start position
 	 * inclusive and to the specified end position inclusive. Data is read from file or from local cache. The first
 	 * request is always reads from file. Subsequent requests for this static content are read from cache. If content
 	 * is more than 2097152 bytes it is never cached and always read from file.
 	 * 
 	 * @param response the HTTP response
 	 * @param startInclusive the start position inclusive
 	 * @param endInclusive the end position inclusive
 	 * @throws IOException if unable to write content
 	 * @throws HttpException if a HTTP error occurs while writing
 	 */
 	private void writeContent(HttpResponse response, int startInclusive, int endInclusive) throws IOException, HttpException {
 		response.setContentLength((int)endInclusive - startInclusive + 1);
 		
 		if(this.length > 2097152) {
 			writeFileContent(response, startInclusive, endInclusive);
 		} else {
 			writeCachedContent(response, startInclusive, endInclusive);
 		}
 	}
 	
 	/**
 	 * Writes this static content from file to the specified HTTP response. Data is written from the specified start position 
 	 * inclusive to the specified end position inclusive.
 	 * 
 	 * @param response the HTTP response
 	 * @param startInclusive the start position inclusive in the content file
 	 * @param endInclusive the end position inclusive in the content file
 	 * @throws HttpException if unable to read content file
 	 */
 	private void writeFileContent(HttpResponse response, int startInclusive, int endInclusive) throws HttpException {
 		RandomAccessFile in = null;
 		
 		try {
 			in = new RandomAccessFile(this.contentFile, "r");
 			in.seek(startInclusive);
 			int len = endInclusive - startInclusive + 1;
 			
 			OutputStream out = response.getOutputStream();
 			byte[] b = new byte[4096];
 			
 			while(len > 0) {
 				int readLen = in.read(b);
 				out.write(b, 0, readLen > len ? len : readLen);
 				len -= readLen;
 			}
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 		}		
 	}
 	
 	/**
 	 * Writes GZIP compressed version of this static content to the specified HTTP response. If GZIP compressed
 	 * content of this static content does not exists it is created and stored in a separate file next to the
 	 * original static content file.
 	 *  
 	 * @param response the HTTP response
 	 * @throws HttpException if unable to read content file 
 	 */
 	private void writeFileGzipContent(HttpResponse response) throws HttpException {
 		FileInputStream in = null;
 		
 		try {
 			if(this.gzipContentFile == null) {
 				gzipContentFile();
 			}
 			
 			response.setContentLength((int)this.gzipLength);
 			in = new FileInputStream(this.gzipContentFile);
 			OutputStream out = response.getOutputStream();
 			byte[] b = new byte[4096];
 			int len = 0;
 			
 			while((len = in.read(b)) > 0) {
 				out.write(b, 0, len);
 			}
 			
 			out.flush();
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 		}		
 	}
 	
 	/**
 	 * Writes static content from internal cache to the specified HTTP response. Data is written from the specified
 	 * start inclusive position to the specified end inclusive position in the content. If static content is not cached
 	 * it is first stored in cache before written to the HTTP response.
 	 * 
 	 * @param response the HTTP respone
 	 * @param startInclusive the start position in content inclusive
 	 * @param endInclusive the end position in content inclusive
 	 * @throws IOException if unable to write static content
 	 * @throws HttpException if a HTTP error occurs while writing
 	 */
 	private void writeCachedContent(HttpResponse response, int startInclusive, int endInclusive) throws IOException, HttpException {
 		if(this.content == null) {
 			this.content = readContent();
 		}
 		
 		int len = endInclusive - startInclusive + 1;
 		OutputStream out = response.getOutputStream();
 		out.write(this.content, startInclusive, len);
 	}
 	
 	/**
 	 * Writes GZIP compressed static content from internal cache to the specified HTTP response. If compressed
 	 * static content is not cached it is first stored in cache before written to the HTTP response.
 	 * 
 	 * @param response the HTTP response
 	 * @throws IOException if unable to write static content
 	 * @throws HttpException if a HTTP error occurs while writing
 	 */
 	private void writeCachedGzipContent(HttpResponse response) throws IOException, HttpException {
 		if(this.gzipContent == null) {
 			this.gzipContent = readGzipContent();
 		}
 		
 		response.setContentLength((int)this.gzipLength);
 		OutputStream out = response.getOutputStream();
 		out.write(this.gzipContent);
 		out.flush();
 	}
 	
 	/**
 	 * Reads this static content from file.
 	 * 
 	 * @return the static content
 	 * @throws HttpException if unable to read static content
 	 */
 	private byte[] readContent() throws HttpException {
 		FileInputStream in = null;
 		
 		try {
			in = new FileInputStream(this.contentFile);
 			byte[] b = new byte[(int)this.length];
 			
			if(in.read(b) != this.length) {
 				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 			}
 			
 			return b;
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 		}		
 	}
 	
 	/**
 	 * Reads GZIP compressed static content.
 	 * 
 	 * @return the GZIP compressed static content
 	 * @throws HttpException if unable to read content
 	 */
 	private byte[] readGzipContent() throws HttpException {
 		FileInputStream in = null;
 		
 		try {
			in = new FileInputStream(this.contentFile);
 			ByteArrayOutputStream bOut = new ByteArrayOutputStream((int)this.length);
 			GZIPOutputStream out = new GZIPOutputStream(bOut);
 			byte[] b = new byte[(int)this.length];
 			
 			if(in.read(b) != this.length) {
 				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 			}
 			
 			out.write(b);
 			out.finish();
 			out.flush();
 			b = bOut.toByteArray();
 			this.gzipLength = b.length;
 			return b;
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed reading content " + this.path);
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 		}		
 	}
 	
 	/**
 	 * Compresses this static content with GZIP and places it in a temporary file.
 	 * 
 	 * @throws HttpException if unable to compress this static content
 	 */
 	private void gzipContentFile() throws HttpException {
 		FileInputStream in = null;
 		FileOutputStream out = null;
 		File tmpGzipContentFile = new File(this.baseDir, createTempGzipFilename());
 		boolean done = false;
 		
 		try {
 			in = new FileInputStream(this.contentFile);
 			out = new FileOutputStream(tmpGzipContentFile);
 			GZIPOutputStream zOut = new GZIPOutputStream(out);
 			byte[] b = new byte[4096];
 			int len = 0;
 			
 			while((len = in.read(b)) > 0) {
 				zOut.write(b, 0, len);
 			}
 			
 			zOut.finish();
 			zOut.flush();
 			out.close();
 			out = null;
 			
 			File gzipContentFile = new File(this.baseDir, this.path + ".gz");
 			tmpGzipContentFile.renameTo(gzipContentFile);
 			done = true;
 			this.gzipContentFile = gzipContentFile;
 			this.gzipLength = this.gzipContentFile.length();
 		} catch(IOException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "failed compressing content " + this.path);
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 			
 			if(out != null) {
 				try { out.close(); } catch(IOException e) {}
 			}
 			
 			if(!done && tmpGzipContentFile.exists()) {
 				tmpGzipContentFile.delete();
 			}
 		}
 	}
 	
 	/**
 	 * Creates temporary file name for compressed content of this static content. The compresses temporary
 	 * file name has the same path and name as the original static content filename with the addition
 	 * of ".gz" and a timestamp.
 	 *  
 	 * @return the temporary filename path
 	 */
 	private String createTempGzipFilename() {
 		StringBuffer name = new StringBuffer(this.path);
 		name.append(".gz-");
 		name.append(System.currentTimeMillis());
 		name.append(Thread.currentThread().getName());
 		return name.toString();
 	}
 	
 	/**
 	 * Gets or creates entity tag as a hex encoded MD5 sum of this static content. If the entity tag
 	 * has note been calculated before it is calculated and stored.
 	 * 
 	 * @return the entity tag as a hex encoded MD5 sum
 	 */
 	private String getOrReadEntityTag() {
 		if(this.entityTag != null) {
 			return this.entityTag;
 		}
 		
 		if(this.length > 2097152) {
 			this.entityTag = readFileEntityTag();
 		} else {
 			this.entityTag = readCachedEntityTag();
 		}
 		
 		return this.entityTag;
 	}
 	
 	/**
 	 * Creates an entity tag for this static content by reading cached content and calculating a MD5
 	 * sum of the content.
 	 * 
 	 * @return the entity tag as a hex encoded MD5 sum
 	 */
 	private String readCachedEntityTag() {
 		try {
 			if(this.content == null) {
 				this.content = readContent();
 			}
 			
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(this.content, 0, (int)this.length);
 			return StringUtils.asHexString(md.digest());
 		} catch(IOException e) {
 			return null;
 		} catch(NoSuchAlgorithmException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates an entity tag for this static content by reading the static content file and calculating a MD5 
 	 * sum of the content.
 	 * 
 	 * @return the entity tag as a hex encoded MD5 sum
 	 */
 	private String readFileEntityTag() {
 		FileInputStream in = null;
 		
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			
 			in = new FileInputStream(this.contentFile);
 			byte[] b = new byte[4096];
 			int len = 0;
 			
 			while((len = in.read(b)) > 0) {
 				md.update(b, 0, len);
 			}
 			
 			return StringUtils.asHexString(md.digest());
 		} catch(NoSuchAlgorithmException e) {
 			return null;
 		} catch(IOException e) {
 			return null;
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 		}
 	}
 }
