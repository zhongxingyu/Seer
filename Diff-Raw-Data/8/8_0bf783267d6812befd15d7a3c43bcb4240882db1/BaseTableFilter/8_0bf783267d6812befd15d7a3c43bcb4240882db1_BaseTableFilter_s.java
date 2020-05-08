 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.table.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
 import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
 import net.sf.okapi.lib.extra.filters.TextProcessingResult;
 
 /**
  * 
  * 
  * @version 0.1, 09.06.2009
  */
 
 public class BaseTableFilter extends BasePlainTextFilter {
 	
 	public static final String FILTER_NAME		= "okf_csv";
 	public static final String FILTER_MIME		= MimeTypeMapper.CSV_MIME_TYPE;	
 	public static final String FILTER_CONFIG	= "okf_csv";
 	
 	public static String ROW_NUMBER		= "row_number";
 	public static String COLUMN_NUMBER	= "column_number";			
 	
 	private Parameters params; // Base Table Filter parameters
 	
 	protected List<Integer> sourceColumns;
 	protected List<Integer> targetColumns;
 	protected List<Integer> targetSourceRefs;
 	protected List<Integer> commentColumns;
 	protected List<Integer> commentSourceRefs;	
 	protected List<Integer> sourceIdColumns;
 	protected List<Integer> sourceIdSourceRefs;
 	
 	protected List<String> columnNames;
 	protected List<String> sourceIdSuffixes;
 	protected List<LocaleId> targetLanguages;
 
 //	protected ArrayList<TextUnit> tuCache;
 	
 	private int rowNumber = 0;
 	private boolean inHeaderArea = true;
 	private boolean sendListedMode = false;
 	private boolean inMultilineColumnNames = false;
 	private boolean isHeaderLine;
 	private boolean isColumnNames;
 	private boolean isFixedNumColumns;
 	
 //	public void component_create() {
 //		
 //		super.component_create();
 	public BaseTableFilter() {	
 	
 		setName(FILTER_NAME);
 		setMimeType(FILTER_MIME);
 		
 //		addConfiguration(true, // Do not inherit configurations from Base Plain Text
 //				FILTER_CONFIG,
 //				"Table Filter",
 //				"Table-like files such as tab-delimited, CSV, fixed-width columns, etc.", 
 //				null);
 		
 		columnNames = new ArrayList<String>();
 //		tuCache = new ArrayList<TextUnit>();
 				
 		
 		setParameters(new Parameters());	// Base Table Filter parameters
 		
 	}
 
 	@Override
 	protected void component_init() {
 		
 		// Commons, should be included in all descendants introducing own params
 		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException
 		super.component_init();
 		
 		// Initialization
 		sourceIdColumns = ListUtil.stringAsIntList(params.sourceIdColumns);
 		sourceColumns = ListUtil.stringAsIntList(params.sourceColumns);
 		targetColumns = ListUtil.stringAsIntList(params.targetColumns);
 		targetLanguages = ListUtil.stringAsLanguageList(params.targetLanguages);
 		commentColumns = ListUtil.stringAsIntList(params.commentColumns);
 		targetSourceRefs = ListUtil.stringAsIntList(params.targetSourceRefs);
 		commentSourceRefs = ListUtil.stringAsIntList(params.commentSourceRefs);
 		sourceIdSourceRefs = ListUtil.stringAsIntList(params.sourceIdSourceRefs);
 		sourceIdSuffixes = ListUtil.stringAsList(params.sourceIdSuffixes);
 										
 		sendListedMode = params.sendColumnsMode == Parameters.SEND_COLUMNS_LISTED;
 		setMultilingual(sendListedMode && targetColumns.size() > 0);
 		
 //		if (params.trimMode == Parameters.TRIM_NONE) {
 //			
 //			params.trimLeading = false;
 //			params.trimTrailing = false;
 //		}
 		
 //		if (params.trimMode != Parameters.TRIM_NONE) {
 //			
 //			params.trimLeading = true;
 //			params.trimTrailing = true;
 //		}
 		
 		rowNumber = 0;
 		
 		if (columnNames != null) 
 			columnNames.clear();
 		else
 			columnNames = new ArrayList<String>();
 		
 		inMultilineColumnNames = false;
 	}
 
 	@Override
 	protected TextProcessingResult component_exec(TextContainer lineContainer) {		
 		
 		if (lineContainer == null) return TextProcessingResult.REJECTED;
 				
 		Property lineNumProp = lineContainer.getProperty(AbstractLineFilter.LINE_NUMBER);
 		long lineNum = new Long(lineNumProp.getValue());
 
 		updateLineInfo(lineNum);
 		
 		if (inHeaderArea && params.sendHeaderMode == Parameters.SEND_HEADER_NONE)  
 			return TextProcessingResult.REJECTED;
 		
 		if (inHeaderArea && !isColumnNames && params.sendHeaderMode == Parameters.SEND_HEADER_COLUMN_NAMES_ONLY) 
 			return TextProcessingResult.REJECTED;
 		
 		if (inHeaderArea)			
 			rowNumber = 0;
 		else {			
 			if (rowNumber <= 0)
 				rowNumber = 1;
 			else
 				rowNumber++;
 		}
 
 		// Send regular header lines (not column names) as a whole
 		if (isHeaderLine) {
 			
 			lineContainer.setProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
 			return super.sendAsSource(lineContainer);
 		}
 		
 		List<TextUnit> cells = new ArrayList<TextUnit>(); 
 		TextProcessingResult res = extractCells(cells, lineContainer, lineNum);
 		
 		switch (res) {
 			case REJECTED:
 				return res;
 				
 			case DELAYED_DECISION:
 				if (isColumnNames) inMultilineColumnNames = true;
 				return res;
 		}
 		
 		// res = ACCEPTED
 			
 		if (isColumnNames) inMultilineColumnNames = false;
 		if (Util.isEmpty(cells)) return super.sendAsSource(lineContainer); // No chunks, process the whole line
 			
 		if (processCells(cells, lineNum))
 			return TextProcessingResult.ACCEPTED;
 		else 
 			return TextProcessingResult.REJECTED;
 	}
 	
 	/**
 	 * Splits line into table cells. 
 	 * @param line string containing separated cells
 	 * @return string array of cells
 	 */
 	protected TextProcessingResult extractCells(List<TextUnit> cells, TextContainer lineContainer, long lineNum) {		
 		// To be overridden in descendant classes
 		
 		if (cells != null) cells.add(TextUnitUtil.buildTU(lineContainer));
 		
 		return TextProcessingResult.ACCEPTED; 
 	}
 
 //	protected String preProcessCell(String cell) {
 //		
 //		return cell;
 //	}
 	
 	protected String getFieldDelimiter() {
 		
 		return null;
 	}
 	
 	protected boolean processCells(List<TextUnit> cells, long lineNum) {
 		// Processes cells of one line
 		// To be called from descendants, least likely overridden
 
 		if (params.sendColumnsMode == Parameters.SEND_COLUMNS_NONE) return false;
 		if (cells == null) return false;		
 		
 		updateLineInfo(lineNum);
 		
 		// If a fixed number of columns is expected, truncate extra chunks, or pad with empty chunks for missing
 		if (isFixedNumColumns) {
 						
 			if (cells.size() < params.numColumns)
 				for (int i = cells.size(); i < params.numColumns; i++)
 					cells.add(TextUnitUtil.buildTU(""));
 
 			if (cells.size() > params.numColumns)
 				cells.subList(params.numColumns, cells.size()).clear();
 		}
 								
 		if (isColumnNames) {
 			
 			columnNames.clear();
 			
 			for (TextUnit tu : cells) {
 				
 				String st = TextUnitUtil.getSourceText(tu).trim();
 				columnNames.add(st);
 			}
 						
 			if (params.detectColumnsMode == Parameters.DETECT_COLUMNS_COL_NAMES)
 				params.numColumns = cells.size();
 		}
 				
 		boolean tuSent = false;
 		int startGroupIndex = getQueueSize();
 		
 		// Send all cells
 		if (params.sendColumnsMode == Parameters.SEND_COLUMNS_ALL || inHeaderArea) {
 			
 			for (int i = 0; i < cells.size(); i++)	{
 				
 				if (i > 0) sendAsSkeleton(getFieldDelimiter());
 				
 				TextUnit cell = cells.get(i);
 				int colNumber = i + 1;
 				
 				if (TextUnitUtil.isEmpty(cell, true)) {  // only spaces, no translatable text
 					
 					//sendSkeletonCell(cell, getActiveSkeleton(), colNumber, cells.size());
 					sendAsSkeleton(cell);
 					continue;
 				}					
 								
 				cell.setSourceProperty(new Property(AbstractLineFilter.LINE_NUMBER, String.valueOf(lineNum), true));				
 				cell.setSourceProperty(new Property(COLUMN_NUMBER, String.valueOf(colNumber), true));
 				cell.setSourceProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
 				
 				if (sendAsSource(cell) != TextProcessingResult.ACCEPTED) continue;
 				tuSent = true;
 			}					
 		}
 		
 		// Send only listed cells (id, source, target, comment)
 		else if (sendListedMode) {
 							
 			// Add content of other columns to the created sources
 			for (int i = 0; i < cells.size(); i++)	{
 				
 				TextUnit cell = cells.get(i); // Can be empty				
 				//String trimmedCell = preProcessCell(Util.trim(TextUnitUtil.getSourceText(cell)));
 				
 				TextUnit temp = new TextUnit("temp", TextUnitUtil.getSourceText(cell)); 
 				TextUnitUtil.trimTU(temp, true, true);
 				String trimmedCell = TextUnitUtil.getSourceText(temp);
 				
 				int colNumber = i + 1;
 				
 				if (isSourceId(colNumber)) {
 					
 					TextUnit tu = getSourceFromIdRef(cells, colNumber);
 					if (tu == null) continue;										
 					
 					if (TextUnitUtil.isEmpty(cell, true)) {
 
 						String recordID = ""; 
 						int index = params.recordIdColumn - 1;
 						
 						if (Util.checkIndex(index, cells))
 							recordID = TextUnitUtil.getSourceText(cells.get(index));
 						
 						if (recordID != null) recordID = recordID.trim();
 						
 						String colSuffix = getSuffixFromSourceRef(colNumber);
 						
 						if (!Util.isEmpty(recordID) && !Util.isEmpty(colSuffix))
 							tu.setName(recordID + colSuffix);
 					}
 					else
 						tu.setName(trimmedCell);
 															
 					continue;
 				}
 				
 				if (isComment(colNumber)) {
 					
 					TextUnit tu = getSourceFromCommentRef(cells, colNumber);
 					if (tu == null) continue;
 					if (Util.isEmpty(trimmedCell)) continue;
 					
 					tu.setProperty(new Property(Property.NOTE, trimmedCell));
 					
 					continue;
 				}
 				
 				if (isSource(colNumber)) {
 					
 					if (cell == null) continue;
 
 					cell.setSourceProperty(new Property(AbstractLineFilter.LINE_NUMBER, String.valueOf(lineNum), true));				
 					cell.setSourceProperty(new Property(COLUMN_NUMBER, String.valueOf(colNumber), true));
 					cell.setSourceProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
 					
 					continue;
 				}				
 			}
 			
 			// Send cells (OKAPI-A 7*)
 			for (int i = 0; i < cells.size(); i++)	{
 
 				if (i > 0) sendAsSkeleton(getFieldDelimiter());
 					
 				
 				TextUnit cell = cells.get(i); // Can be empty
 				
 				int colNumber = i + 1;
 				
 				if (isSource(colNumber)) {
 					
 					if (cell == null) continue;
 
 					if (sendAsSource(cell) != TextProcessingResult.ACCEPTED) {
 						
 						sendAsSkeleton(cell);
 						continue; 
 					}
 					tuSent = true;
 					
 					continue;
 				}
 				
 				if (isTarget(colNumber)) {
 					TextUnit tu = getSourceFromTargetRef(cells, colNumber);
 					if ( tu == null ) {
 						sendAsSkeleton(cell);
 						continue;
 					}
 					// Else:
 					LocaleId language = getLanguageFromTargetRef(colNumber);
 					if ( Util.isNullOrEmpty(language) ) {
 						sendAsSkeleton(cell);
 					}
					else {
						sendAsTarget(cell, tu, language);
					}
 				}
 				
 				// All other kinds of cells go to skeleton
 				sendAsSkeleton(cell);
 			}									
 		}
 		
 		if (tuSent) {
 			
 			StartGroup startGroup = new StartGroup("");
 			
 			if (startGroup != null)
 				startGroup.setType("row"); // restype = "row"
 				
 			sendEvent(startGroupIndex, EventType.START_GROUP, startGroup);
 			
 			sendEvent(EventType.END_GROUP, new Ending(""));
 		}
 		
 		return true;		
 	}
 	
 	public List<String> getColumnNames() {
 		
 		if (columnNames == null)
 			columnNames = new ArrayList<String>();
 				
 		return columnNames;
 	}
 	
 	private boolean isSource(int colNumber) {return (sourceColumns == null) ? null : sourceColumns.contains(colNumber);}
 	private boolean isSourceId(int colNumber) {return (sourceIdColumns == null) ? null : sourceIdColumns.contains(colNumber);}	
 	private boolean isTarget(int colNumber) {return (targetColumns == null) ? null : targetColumns.contains(colNumber);}
 	private boolean isComment(int colNumber) {return (commentColumns == null) ? null : commentColumns.contains(colNumber);}	
 
 	private TextUnit getSource(List<TextUnit> cells, int colNum, List<Integer> columnsList, List<Integer> refList) {
 		
 		if (columnsList == null) return null;		
 		int index = columnsList.indexOf(colNum); 
 		
 		if (!Util.checkIndex(index, refList)) return null;
 		int ref = refList.get(index) - 1; // refList items are 1-based
 		
 		//return getFromTuCache(--ref); // ref is 1-based
 		if (!Util.checkIndex(ref, cells)) return null;
 		return cells.get(ref);
 	}
 	
 	private TextUnit getSourceFromTargetRef(List<TextUnit> cells, int colNum) {
 
 		return getSource(cells, colNum, targetColumns, targetSourceRefs);
 	}
 			
 	private TextUnit getSourceFromIdRef(List<TextUnit> cells, int colNum) {
 		
 		return getSource(cells, colNum, sourceIdColumns, sourceIdSourceRefs);
 	}
 	
 	private TextUnit getSourceFromCommentRef(List<TextUnit> cells, int colNum) {
 		
 		return getSource(cells, colNum, commentColumns, commentSourceRefs);
 	}
 	
 //	private TextUnit getFromTuCache(int cacheIndex) {
 //		
 //		if (!Util.checkIndex(cacheIndex, tuCache)) return null;
 //		
 //		return tuCache.get(cacheIndex);
 //	}
 	
 	private LocaleId getLanguageFromTargetRef(int colNum) {
 		if ( targetColumns == null ) return LocaleId.EMPTY;		
 		int index = targetColumns.indexOf(colNum); 
 		if ( !Util.checkIndex(index, targetLanguages) ) return LocaleId.EMPTY;
 		return targetLanguages.get(index);
 	}
 	
 	private String getSuffixFromSourceRef(int colNum) {
 		
 		if (sourceIdColumns == null) return "";		
 		int index = sourceIdColumns.indexOf(colNum);
 		
 		if (!Util.checkIndex(index, sourceIdSuffixes)) return "";
 		return sourceIdSuffixes.get(index);
 	}
 
 //	protected boolean sendSourceCell(TextUnit tu, int column, int numColumns) {
 //		// Can be overridden in descendant classes
 //		
 //		return sendAsSource(tu) == TextProcessingResult.ACCEPTED;
 //	}
 
 //	protected void preProcessTarget(TextUnit target) {
 //		
 //	}
 	
 //	protected boolean sendTargetCell(TextUnit target, TextUnit source,
 //			GenericSkeleton skel, String language, int column, int numColumns) {
 	
 //	@Override
 //	protected TextProcessingResult sendAsTarget(TextUnit target, TextUnit source, String language, GenericSkeleton skel) {
 //		
 //		preProcessTarget(target);
 //		return super.sendAsTarget(target, source, language, skel);
 //	}
 
 	
 //	protected boolean sendSkeletonCell(TextUnit cell, GenericSkeleton skel, int column, int numColumns) {
 //		
 ////		if (skel == null) return;
 ////		
 //////		String st = TextUnitUtil.getSourceText(cell);
 ////		
 ////		// If the cell contained a skeleton, before we drop it, we need to transfer it to the skel
 ////				
 //////		GenericSkeleton skel2 = TextUnitUtil.getSkeleton(cell, true);
 //////		skel.add(skel2);
 //////		
 //////		if (Util.isEmpty(st)) return; // strict empty, even no spaces allowed
 //////		
 //////		skel.add(st);
 ////		
 ////		skel.add(TextUnitUtil.convertToSkeleton(cell));
 //		
 //		return sendAsSkeleton(cell, skel) == TextProcessingResult.ACCEPTED;
 //	}
 	
 	private void updateLineInfo(long lineNum) {
 		inHeaderArea = lineNum < params.valuesStartLineNum;
 		
 		isColumnNames = 
 			inHeaderArea && 
 					(lineNum == params.columnNamesLineNum || 
 					(lineNum > params.columnNamesLineNum && inMultilineColumnNames));
 		
 		isHeaderLine = inHeaderArea && !isColumnNames;
 		
 		isFixedNumColumns = 		
 			(params.detectColumnsMode == Parameters.DETECT_COLUMNS_FIXED_NUMBER && params.numColumns > 0) ||
 			(params.detectColumnsMode == Parameters.DETECT_COLUMNS_COL_NAMES && !inHeaderArea);
 	}
 }
