 package gov.usgs;
 
 import static gov.usgs.HTTPParameters.ExtParam.*;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ExportServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -7580708007639009664L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		String siteNo = SITE_NO.parse(req);
 		String agencyCd = AGENCY_CODE.parse(req);
 
 
		resp.setContentType("application/zip");
		resp.setHeader("Content-Disposition", "attachment; filename=gwdp_"
				+ agencyCd + "_" + siteNo + ".zip");
 
 		ServletOutputStream os = resp.getOutputStream();
 
 		// fetch files
 		// Get response data.
 		RequestType serviceRequest = RequestType.download;
 		String urlString = serviceRequest.makeCacheUrl(agencyCd, siteNo);
 		log("gov.usgs.ExportServlet.doGet url=" + urlString);
 		
 		// copy cache output to client
 		copyUrlContents(urlString, os);
 		os.flush();
 		os.close();
 	}
 	
 	private void copyUrlContents(String urlString, OutputStream os) 
 			throws IOException, ServletException
 	{
 
 		try {
 
 			URL url = new URL(urlString);
 			URLConnection conn = url.openConnection();
 			InputStream input = new DataInputStream(conn.getInputStream());
 			BufferedInputStream br = new BufferedInputStream(input);
 			try {
 				int ct = copy(br, os);
 				log("gov.usgs.ExportServlet.copyUrlContents done, ct=" + ct);
 			} finally {
 				br.close();
 			}
 		} 
 		catch (IOException ioe) {
 			throw ioe;
 		} 
 		catch (Exception e) {
 			log("problem in gov.usgs.ExportServlet.copyUrlContents", e);
 			throw new ServletException(e);
 		}
 	}
 
 	private int copy(InputStream is, OutputStream os) 
 			throws IOException
 	{
 		byte[] buf = new byte[1024];
 		
 		int tct = 0;
 		
 		while (true) {
 			int ct = is.read(buf);
 			if (ct <= 0) {
 				break;
 			}
 			os.write(buf, 0, ct);
 			tct += ct;
 		}
 		
 		return tct;
 	}
 
 }
