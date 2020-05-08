 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * QuestionExcelView.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * Nov 21, 2012			chalam						Created
  * *************************************************************
  */
 package org.sourcetree.interview.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.sourcetree.interview.AppConstants;
 import org.sourcetree.interview.dto.CategoryDTO;
 import org.sourcetree.interview.dto.QuestionDTO;
 import org.sourcetree.interview.support.CoreUtil;
 import org.springframework.web.servlet.view.document.AbstractExcelView;
 
 /**
  * @author chalam
  * 
  */
 public class QuestionExcelView extends AbstractExcelView
 {
 
 	private static final int QUESTIONID_CELLNUM = 0;
 	private static final int QUESTION_CELLNUM = 1;
 	private static final int ANSWER_CELLNUM = 2;
 	private static final int CATEGORIES_CELLNUM = 3;
 
 	private static final String COMMA = ",";
 
 	/**
 	 * to build the response as excel file.
 	 * 
 	 * @see org.springframework.web.servlet.view.document.AbstractExcelView#
 	 *      buildExcelDocument(java.util.Map,
 	 *      org.apache.poi.hssf.usermodel.HSSFWorkbook,
 	 *      javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	@SuppressWarnings(AppConstants.SUPPRESS_WARNINGS_UNCHECKED)
 	@Override
 	protected void buildExcelDocument(Map<String, Object> model,
 			HSSFWorkbook myWorkBook, HttpServletRequest arg2,
 			HttpServletResponse arg3) throws Exception
 	{
 		List<QuestionDTO> questionDTOs = (List<QuestionDTO>) model
 				.get("questions");
 
 		HSSFSheet questionsSheet = myWorkBook
 				.createSheet(AppConstants.EXCEL_SHEET_NAME);
 
 		HSSFRow headerRow = questionsSheet.createRow(0);
 
 		HSSFCellStyle wrapCellStyle = myWorkBook.createCellStyle();
 		wrapCellStyle.setWrapText(true);
 		wrapCellStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
 
 		createNewCell(headerRow, QUESTIONID_CELLNUM, "Question ID",
 				wrapCellStyle);
		createNewCell(headerRow, QUESTIONID_CELLNUM, "Question", wrapCellStyle);
		createNewCell(headerRow, QUESTIONID_CELLNUM, "Answer", wrapCellStyle);
		createNewCell(headerRow, QUESTIONID_CELLNUM,
 				"Categories (Category seperated by comma)", wrapCellStyle);
 
 		HSSFRow dataRow = null;
 
 		if (!CoreUtil.isEmpty(questionDTOs))
 		{
 			int i = 0;
 			for (QuestionDTO questionDTO : questionDTOs)
 			{
 				dataRow = questionsSheet.createRow(i + 1);
 
 				createNewCell(dataRow, QUESTIONID_CELLNUM,
 						Long.toString(questionDTO.getId()), null);
 
 				createNewCell(dataRow, QUESTION_CELLNUM,
 						questionDTO.getQuestion(), wrapCellStyle);
 
 				createNewCell(dataRow, ANSWER_CELLNUM, questionDTO.getAnswer(),
 						wrapCellStyle);
 
 				createNewCell(dataRow, CATEGORIES_CELLNUM,
 						parseCategoryDTOs(questionDTO.getCategoryDTOs()),
 						wrapCellStyle);
 
 				// Increment row
 				i = i + 1;
 			}
 		}
 
 		questionsSheet.autoSizeColumn(QUESTIONID_CELLNUM);
 		questionsSheet.autoSizeColumn(QUESTION_CELLNUM);
 		questionsSheet.autoSizeColumn(ANSWER_CELLNUM);
 		questionsSheet.autoSizeColumn(CATEGORIES_CELLNUM);
 
 		questionsSheet.setDefaultColumnStyle(QUESTION_CELLNUM, wrapCellStyle);
 		questionsSheet.setDefaultColumnStyle(ANSWER_CELLNUM, wrapCellStyle);
 		questionsSheet.setDefaultColumnStyle(CATEGORIES_CELLNUM, wrapCellStyle);
 	}
 
 	private HSSFCell createNewCell(HSSFRow row, int cellNum, String value,
 			HSSFCellStyle cellStyle)
 	{
 		HSSFCell hssfCell = row.createCell(cellNum);
 		hssfCell.setCellValue(value);
 
 		if (cellStyle != null)
 		{
 			hssfCell.setCellStyle(cellStyle);
 		}
 
 		return hssfCell;
 	}
 
 	/**
 	 * parses category DTO and returns comma separated categories.
 	 * 
 	 * @param categoryDTOs
 	 * @return
 	 */
 	private String parseCategoryDTOs(List<CategoryDTO> categoryDTOs)
 	{
 		String categoryColumnData = "";
 		if (!CoreUtil.isEmpty(categoryDTOs))
 		{
 			StringBuilder sb = new StringBuilder();
 			for (CategoryDTO questionCategoryDTO : categoryDTOs)
 			{
 				sb.append(questionCategoryDTO.getCategoryName()).append(COMMA);
 			}
 			categoryColumnData = sb.substring(0, sb.length() - 1).toString();
 		}
 
 		return categoryColumnData;
 	}
 }
