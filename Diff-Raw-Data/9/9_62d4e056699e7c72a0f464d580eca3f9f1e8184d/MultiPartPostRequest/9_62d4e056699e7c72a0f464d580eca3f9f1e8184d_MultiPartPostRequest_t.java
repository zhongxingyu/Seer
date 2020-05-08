 package com.fieldexpert.fbapi4j.http;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.fieldexpert.fbapi4j.FogBugz;
 import com.fieldexpert.fbapi4j.common.Attachment;
 
 class MultiPartPostRequest extends HttpRequest {
 
 	private static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";
 
 	/**
 	 * See: http://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
 	 */
	private String createBoundary() {
 		return "-----------------------------" + Long.toString(System.currentTimeMillis());
 	}
 
 	private String boundary;
 	private List<Attachment> attachments;
 
 	public MultiPartPostRequest(URL url, Map<String, String> parameters, List<Attachment> attachments) {
 		super(Http.POST, url, parameters);
 		this.attachments = attachments;
 		this.boundary = createBoundary();
 	}
 
 	@Override
 	String getContentType() {
 		return MULTIPART_FORM_DATA + boundary;
 	}
 
 	@Override
 	void write(OutputStream out) throws IOException {
 		MultiPartOutputStream mout = new MultiPartOutputStream(out, boundary);
 
 		for (Entry<String, String> param : parameters.entrySet()) {
 			mout.write(param.getKey(), param.getValue());
 		}
 
		int files = 0;
 		for (Attachment attachment : attachments) {
			mout.write(FogBugz.FILE + (++files), attachment.getFilename(), attachment.getType(), attachment.getContent());	
 		}
		mout.write(FogBugz.N_FILE_COUNT, files);
 
 		mout.writeEnd();
 	}
 }
