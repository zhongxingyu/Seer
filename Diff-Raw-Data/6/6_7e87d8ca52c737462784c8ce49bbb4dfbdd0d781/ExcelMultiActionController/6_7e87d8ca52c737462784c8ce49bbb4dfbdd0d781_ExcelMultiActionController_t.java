 package com.bigcay.exhubs.controller;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.poi.ss.usermodel.Workbook;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.bigcay.exhubs.util.ExcelTemplateNameUtil;
 
 public class ExcelMultiActionController extends MultiActionController {
 
 	private static final Logger logger = LoggerFactory.getLogger(ExcelMultiActionController.class);
 	
 	private String getTemplateExcelFolderPath() {
 		return getServletContext().getRealPath("") + File.separator + "resources" + File.separator + "excel" + File.separator;
 	}
 
 	protected String getTemplateExcelFilePath(String excelName) {
 		String excelTemplateName = ExcelTemplateNameUtil.getReportTemplateNameValue(excelName) == null ? excelName
 				: ExcelTemplateNameUtil.getReportTemplateNameValue(excelName);
 		return this.getTemplateExcelFolderPath() + excelTemplateName + ".xls";
 	}
 
 	protected void exportExcel(HttpServletRequest request, HttpServletResponse response, Workbook workbook,
 			String excelName) throws Exception {
 
		logger.debug("ExcelReportController.exportExcel is invoked.");
		
 		String finalExcelName = java.net.URLEncoder.encode(excelName, "UTF-8").replaceAll("\\+", "%20");
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		workbook.write(os);
 
 		byte[] content = os.toByteArray();
 		InputStream is = new ByteArrayInputStream(content);
 
 		response.setHeader("Content-Type", "application/vnd.ms-excel;charset=utf-8");
 		response.setHeader("Content-Length", String.valueOf(content.length));
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + finalExcelName + ".xls" + "\"");
 
 		BufferedInputStream input = null;
 		BufferedOutputStream output = null;
 
 		try {
 			input = new BufferedInputStream(is);
 			output = new BufferedOutputStream(response.getOutputStream());
 
 			byte[] buffer = new byte[8192];
 			for (int length = 0; (length = input.read(buffer)) > 0;) {
 				output.write(buffer, 0, length);
 			}
 		} finally {
 			if (output != null) {
 				try {
 					output.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (input != null) {
 				try {
 					input.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
