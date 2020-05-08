 package com.xuechong.utils.exl.process.writer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.struts2.ServletActionContext;
 
 public class WorkBookWriter {
 	///wether the exl file will save in the local file system
 	private static final Boolean LOCAL_FILE = Boolean.TRUE;
 	/**
 	 * 输出
 	 * @param book
 	 * @author xuechong
 	 */
 	public static void writeBook(Workbook book,String fileName) {
 		if(fileName==null){
 			fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
 		}
 		OutputStream out = getOutStream(fileName);
 		try {
 			book.write(out);
 			out.close();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}finally{
 			out = null;
 		}
 	}
 	
 	/**
 	 * get the outputstream to write the exl file
 	 * @return
 	 * @author xuechong
 	 */
 	public static OutputStream getOutStream(String fileName){
 		OutputStream out = null;
 		if (LOCAL_FILE) {
 			File parent = new File(File.listRoots()[0].getPath() + "exportedExls");
 			parent.mkdir();
 			File file = new File(parent.getPath() + File.separator + fileName
 					+ ".xls");
 			try {
 				out = new FileOutputStream(file);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				out = ServletActionContext.getResponse().getOutputStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return out;
 	}
 }
