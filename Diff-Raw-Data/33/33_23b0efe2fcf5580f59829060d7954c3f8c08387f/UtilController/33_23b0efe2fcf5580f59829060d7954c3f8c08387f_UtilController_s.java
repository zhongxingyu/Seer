 package com.mayroro.controllers;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.docs.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.WorksheetEntry;
 import com.google.gdata.util.ServiceException;
 import com.mayroro.util.ConstFunc;
 import com.mayroro.util.UserInfo;
 
 @Controller
 @RequestMapping("util")
 public class UtilController {
 	@RequestMapping("new_spreadsheet")
	public ModelAndView newSpreadsheet(HttpServletRequest req, HttpServletResponse res){
 		ModelAndView mv = new ModelAndView("redirect:/home");
 		
 		HttpSession session = req.getSession();
 		UserInfo ui = (UserInfo)session.getAttribute("userInfo");
 		
 		DocsService service = new DocsService("mayRoro");
 		service.setHeader("Authorization", "OAuth "+ui.getAccess_token());
 		
		String title = null;
		title = req.getParameter("title");
 		if(title == null)
 			return new ModelAndView("forward:/error?type=parameter_missing");
 		
 		try {
 			SpreadsheetEntry spreadsheet = createNewSpreadsheet(service, ConstFunc.SPREADSHEET_PREFIX+title);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new ModelAndView("forward:/error?type=new_spreadsheet_error");
 		}
 		
 		return mv;
 	}
 	
 	private SpreadsheetEntry createNewSpreadsheet(DocsService service, String title) throws IOException, ServiceException {
 		SpreadsheetEntry newEntry = new SpreadsheetEntry();
 		newEntry.setTitle(new PlainTextConstruct(title));
 		
 		// Prevent collaborators from sharing the document with others?
 		// newEntry.setWritersCanInvite(false);
 
 		// You can also hide the document on creation
 		// newEntry.setHidden(true);
 		
 		SpreadsheetEntry spreadsheet = service.insert(new URL("https://docs.google.com/feeds/default/private/full/"), newEntry);
 		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
 		
 
 		// Worksheet - atributi
 		createNewWorksheet(service, worksheetFeedUrl, "atributi", 100, 2);
 		WorksheetEntry worksheet = spreadsheet.getWorksheets().get(0);
 		worksheet.delete();
 		
 		// Worksheet - drevo
 		createNewWorksheet(service, worksheetFeedUrl, "drevo", 100, 2);
 		
 		// Worksheet - funkcije
 		createNewWorksheet(service, worksheetFeedUrl, "funkcije", 50, 60);
 
 		// Worksheet - utei
 		createNewWorksheet(service, worksheetFeedUrl, "utei", 50, 30);
 
 		// Worksheet - alternative
 		createNewWorksheet(service, worksheetFeedUrl, "alternative", 50, 2);
 
 		// Worksheet - maut
 		createNewWorksheet(service, worksheetFeedUrl, "maut", 101, 51);
 		
 		return spreadsheet;
 	}
 	
 	private WorksheetEntry createNewWorksheet(DocsService service, URL worksheetFeedUrl, String title, int rowCount, int colCount) throws IOException, ServiceException{
 		WorksheetEntry worksheet = new WorksheetEntry();
 		worksheet.setTitle(new PlainTextConstruct(title));
 		worksheet.setRowCount(rowCount);
 		worksheet.setColCount(colCount);
 		service.insert(worksheetFeedUrl, worksheet);
 		return worksheet;
 	}
 	
 //	private void createFolder(DocsService client, String title) throws IOException, ServiceException {
 //		FolderEntry newEntry = new FolderEntry();
 //		newEntry.setTitle(new PlainTextConstruct(title));
 //		URL feedUrl = new URL("https://docs.google.com/feeds/default/private/full/");
 //		client.insert(feedUrl, newEntry);
 //	}
 }
