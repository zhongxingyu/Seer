 /*
     Weave (Web-based Analysis and Visualization Environment)
     Copyright (C) 2008-2011 University of Massachusetts Lowell
 
     This file is a part of Weave.
 
     Weave is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License, Version 3,
     as published by the Free Software Foundation.
 
     Weave is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Weave.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletInputStream;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import sun.misc.BASE64Decoder;
 
 /**
  * @author kmonico
  */
 public class Base64ImageService extends HttpServlet 
 {
 	private static final long serialVersionUID = 1L;
 	
 	public void init(ServletConfig config) throws ServletException
 	{
 		super.init(config);
 		
 		ServletContext context = config.getServletContext();
 		tempImagePath = context.getRealPath(context.getInitParameter("tempImagePath")).replace('\\', '/');
 	}
 
 	private String tempImagePath = "";
 	
 	/**
 	 * @param request A request which has the name of a file provided in the URL parameters.
 	 * @param response The binary of the image.
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		response.setContentType("image/png");
 		try
 		{
 			String fileName = request.getParameter("file");
 			File tempFile = new File(tempImagePath + "/" + new File(fileName).getName());
 			FileInputStream fileInStream = new FileInputStream(tempFile);
 			ServletOutputStream outStream = response.getOutputStream();
 			
 			int length = (int)tempFile.length();
 			byte[] bytes = new byte[length];
 			fileInStream.read(bytes);
 			outStream.write(bytes);
 			fileInStream.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @param request A request containing a Base64 string of the image as its input.
 	 * @param response A response which will be the name of the file on disk.
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		try
 		{
 			UUID rand = UUID.randomUUID();
 			String fileName = rand.toString() + ".png";
 			
 			// get the file name and write it to the response
 			byte[] bytes = fileName.getBytes();
 			OutputStream fileOutStream = new FileOutputStream(tempImagePath + "/" + fileName);
 			response.getOutputStream().write(bytes);
 
 			// decode the input stream and write to file
 			BASE64Decoder decoder = new BASE64Decoder();
 			ServletInputStream inStream = request.getInputStream();
 			bytes = decoder.decodeBuffer(inStream);
 			fileOutStream.write(bytes);
 			fileOutStream.flush();
 
 			// close the file stream
 			fileOutStream.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
