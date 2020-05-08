 package by.bsu.rfe.nosova.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import by.bsu.rfe.nosova.utils.ExcelSheetWriter;
 
 public class Excel extends HttpServlet {
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("application/vnd.ms-excel");
 		resp.setHeader("Content-Disposition", "attachment; filename=data.xls");
 		List<String> headers = new ArrayList<String>();
 		List<Double[]> data = new ArrayList<Double[]>();
 
 		headers.add("x");
		headers.add(req.getParameter("lable"));
 		
 		String dataString = req.getParameter("data");
 		String[] dataArrayString = dataString.split(",");
 		Double[] dataArrayDouble = new Double[dataArrayString.length];
 		for(int i=0;i<dataArrayString.length;i++){
 			dataArrayDouble[i] = Double.parseDouble(dataArrayString[i]);
 		}
 		
 		
 		int index = 0;
 		for(int i=2; i<dataArrayDouble.length; i+=2){
			data.add(Arrays.copyOfRange(dataArrayDouble, index, i));
 			index = i;
 		}
 		
 		
 		ExcelSheetWriter.writeExcelFile(headers, data, resp.getOutputStream());
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.getWriter().write("It is works!");
 	}
 }
