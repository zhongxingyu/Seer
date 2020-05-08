 /*******************************************************************************
  * Copyright (c) Apr 5, 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.logic.reporting;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IQueryService;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.Function;
 import com.netxforge.netxstudio.library.LevelKind;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.operators.Marker;
 import com.netxforge.netxstudio.operators.ToleranceMarker;
 
 /**
  * Outputs resources reports.
  * 
  * @author Christophe
  * 
  */
 public class ResourceReportingEngine {
 
 	private ModelUtils modelUtils;
 	
 	private DateTimeRange period;
 	
 	private Workbook workBook;
 	
 	@SuppressWarnings("unused")
 	private IQueryService queryService;
 
 	private static final int NODE_COLUMN = 2;
 
 	public ResourceReportingEngine(ModelUtils modelUtils,
 			DateTimeRange period, Workbook workBook, IQueryService queryService) {
 		this.modelUtils = modelUtils;
 		this.period = period;
 		this.workBook = workBook;
 		this.queryService = queryService;
 	}
 
 	public void writeComponentLine(int newRow, Sheet sheet, Component component) {
 
 		Row componentRow = sheet.createRow(newRow);
 		Cell componentCell = componentRow.createCell(NODE_COLUMN + 1);
 
 		// Build the presentation of a component.
 		StringBuilder builder = new StringBuilder();
 
 		if (component instanceof Function) {
 			builder.append(component.getName());
 
 			// DEBUG CODE.
 			// if(component.getName().equals("amm02in0assoc0")){
 			// System.out.println("DEBUG this comp" + component.getName());
 			// }
 		} else if (component instanceof Equipment) {
 			builder.append(((Equipment) component).getEquipmentCode());
 
 			builder.append(component
 					.eIsSet(LibraryPackage.Literals.COMPONENT__NAME) ? " name:"
 					+ component.getName() : "");
 		}
 		builder.append(component
 				.eIsSet(LibraryPackage.Literals.COMPONENT__DESCRIPTION) ? " description:"
 				+ component.getDescription()
 				: "");
 
 		componentCell.setCellValue(builder.toString());
 	}
 
 	public void writeComponentLine(Sheet sheet, Row row, Component component) {
 
 		Cell componentCell = row.createCell(NODE_COLUMN + 1);
 
 		// Build the presentation of a component.
 		StringBuilder builder = new StringBuilder();
 
 		if (component instanceof Function) {
 			builder.append(component.getName());
 
 			// DEBUG CODE.
 			// if(component.getName().equals("amm02in0assoc0")){
 			// System.out.println("DEBUG this comp" + component.getName());
 			// }
 		} else if (component instanceof Equipment) {
 			builder.append(((Equipment) component).getEquipmentCode());
 
 			builder.append(component
 					.eIsSet(LibraryPackage.Literals.COMPONENT__NAME) ? " name:"
 					+ component.getName() : "");
 		}
 		builder.append(component
 				.eIsSet(LibraryPackage.Literals.COMPONENT__DESCRIPTION) ? " description:"
 				+ component.getDescription()
 				: "");
 
 		componentCell.setCellValue(builder.toString());
 	}
 
 	/**
 	 * Write the resources for a component in the engine's set period.
 	 * 
 	 * @param newRow
 	 * @param sheet
 	 * @param component
 	 * @param markersForNode
 	 */
 	// public void writeHiarchy(int newRow, HSSFSheet sheet, Component
 	// component,
 	// Map<NetXResource, List<Marker>> markersForNode) {
 	// int resourceIndex = newRow + 1;
 	//
 	// for (NetXResource resource : component.getResourceRefs()) {
 	//
 	// List<Marker> markersForResource = null;
 	// if (markersForNode != null && markersForNode.containsKey(resource)) {
 	// markersForResource = markersForNode.get(resource);
 	// }
 	//
 	// HSSFRow resourceRow = sheet.createRow(resourceIndex++);
 	//
 	// HSSFCell resourceCell = resourceRow.createCell(NODE_COLUMN + 2);
 	// resourceCell.setCellValue(resource.getLongName());
 	//
 	// for (MetricValueRange mvr : resource.getMetricValueRanges()) {
 	// resourceIndex = writeRange(sheet, resourceIndex, resource,
 	// markersForResource, mvr);
 	// }
 	//
 	// { // Write the capacity.
 	// // !Potentially long operation, as we sort of the whole rang.e
 	// resourceIndex = writeCapacity(sheet, resourceIndex, resource);
 	// }
 	//
 	// { // Write the utilization.
 	// // !Potentially long operation, as we sort of the whole rang.e
 	// resourceIndex = writeUtilization(sheet, resourceIndex, resource);
 	// }
 	//
 	// }
 	// }
 
 	/**
 	 * 
 	 * @param newRow
 	 * @param sheet
 	 * @param component
 	 * @param markersForNode
 	 */
 	public void writeFlat(int newRow, Sheet sheet, Component component,
 			Map<NetXResource, List<Marker>> markersForNode) {
 		int rowIndex = newRow;
 
 		// writeTS(sheet, rowIndex);
 
 		// Write one line for each resource MVR.
 		for (NetXResource resource : component.getResourceRefs()) {
 
 			List<Marker> markersForResource = null;
 			if (markersForNode != null && markersForNode.containsKey(resource)) {
 				markersForResource = markersForNode.get(resource);
 				if(markersForResource.size() > 0 ){
 					System.out.println("Markers found for this resource " + resource.getLongName() + " size=" +markersForResource.size());
 				}
 				
 			}
 			
 			for (MetricValueRange mvr : resource.getMetricValueRanges()) {
 
 				if (mvr != null) {
 
 					Row nextRow = this.rowForIndex(sheet, ++rowIndex);
 
 					// The component name.
 					this.writeComponentLine(sheet, nextRow, component);
 
 					// The resource (metric) name.
 					Cell resourceCell = nextRow.createCell(NODE_COLUMN + 2);
 					resourceCell.setCellValue(resource.getLongName());
 
 					// The range name.
 					Cell mvrCell = nextRow.createCell(NODE_COLUMN + 3);
 					mvrCell.setCellValue(nameForValueRange(mvr));
 
 					writeRange(markersForResource, sheet, mvr, nextRow);
 				}
 			}
 			{ // Write the capacity.
 				
 				// increment the index before creating a new row. 
 				Row nextRow = this.rowForIndex(sheet, ++rowIndex);
 
 				// The component name.
 				this.writeComponentLine(sheet, nextRow, component);
 
 				// The resource (metric) name.
 				Cell resourceCell = nextRow.createCell(NODE_COLUMN + 2);
 				resourceCell.setCellValue(resource.getLongName());
 
 				// The range name.
 				Cell mvrCell = nextRow.createCell(NODE_COLUMN + 3);
 				mvrCell.setCellValue("Capacity");
 
 				// !Potentially long operation, as we sort of the whole rang.e
 				writeCapacity(sheet, nextRow, resource);
 
 			}
 
 			{ // Write the utilization.
 				
 				// increment the index before creating a new row. 
 				Row nextRow = this.rowForIndex(sheet, ++rowIndex);
 
 				// The component name.
 				this.writeComponentLine(sheet, nextRow, component);
 
 				// The resource (metric) name.
 				Cell resourceCell = nextRow.createCell(NODE_COLUMN + 2);
 				resourceCell.setCellValue(resource.getLongName());
 
 				// The range name.
 				Cell mvrCell = nextRow.createCell(NODE_COLUMN + 3);
 				mvrCell.setCellValue("Utilization");
 
 				// !Potentially long operation, as we sort of the whole rang.e
 				writeUtilization(sheet, nextRow, resource);
 			}
 		}
 	}
 
 	/*
 	 * A map of column index and TimesStamp.
 	 */
 	Map<Integer, Date> columnTS = Maps.newHashMap();
 
 	/**
 	 * @param sheet
 	 * @param rowIndex
 	 */
 	public void writeTS(Sheet sheet, int rowIndex) {
 		// Get the timestamps by week numbers.
 		Multimap<Integer, XMLGregorianCalendar> timeStampsByWeek = modelUtils
 				.hourlyTimeStampsByWeekFor(this.getPeriod());
 
 		Row tsRow = this.rowForIndex(sheet, rowIndex);
 		System.out
 				.println("Analyzed weeks " + timeStampsByWeek.keySet().size());
 
 		int column = NODE_COLUMN + 4;
 
 		// TODO, Should reverse the weeks.
 
 		// build an index of colums and timestamps.
 		for (int i : timeStampsByWeek.keySet()) {
 			Collection<XMLGregorianCalendar> collection = timeStampsByWeek
 					.get(i);
 
 			List<Date> weekTS = modelUtils.transformXMLDateToDate(collection);
 			Collections.sort(weekTS);
 
 			// CB Apply a check, if our usemodel for POI is based on HSSF which
 			// is < '07 excel.
 			// if( weekTS.size() + column >= 256 ){
 			// // With HSSF POI model, we can't do more than 256 columns
 			// break;
 			// }
 
 			// Write the timestamps from the specified column.
 			column = this.writeTS(columnTS, sheet, tsRow, weekTS, i, column);
 
 			System.out.println(weekTS);
 
 		}
 	}
 
 	// public int writeRange(HSSFSheet sheet, int resourceIndex,
 	// NetXResource resource, List<Marker> markersForResource,
 	// MetricValueRange mvr) {
 	// if (mvr != null) {
 	// resourceIndex = writeRange(markersForResource, sheet, mvr,
 	// resourceIndex);
 	// }
 	// return resourceIndex;
 	// }
 
 	public void writeCapacity(Sheet sheet, Row capRow, NetXResource resource) {
 		List<Value> capRange = getModelUtils().sortValuesByTimeStamp(
 				resource.getCapacityValues());
 		capRange = getModelUtils().valuesInsideRange(capRange, this.getPeriod());
 		writeRange(null, sheet, capRow, capRange);
 	}
 
 	public void writeUtilization(Sheet sheet, Row capRow, NetXResource resource) {
 		List<Value> utilRange = getModelUtils().sortValuesByTimeStamp(
 				resource.getUtilizationValues());
 		utilRange = getModelUtils().valuesInsideRange(utilRange, this.getPeriod());
 
 		writeRange(null, sheet, capRow, utilRange);
 	}
 
 	public void writeRange(List<Marker> markers, Sheet sheet,
 			MetricValueRange mvr, Row valueRow) {
 
 		// use a query, experimental.
 //		List<Value> range = queryService.getSortedValues(mvr);
 		
 		// !Potentially long operation, as we sort of the whole rang.e
 		List<Value> range = getModelUtils().sortValuesByTimeStamp(
 				mvr.getMetricValues());
 		range = getModelUtils().valuesInsideRange(range, this.getPeriod());
 		writeRange(markers, sheet, valueRow, range);
 	}
 
 	/**
 	 * @param mvr
 	 */
 	private String nameForValueRange(MetricValueRange mvr) {
 		String fromMinutes = this.getModelUtils().fromMinutes(
 				mvr.getIntervalHint());
 
 		String rangeKind = mvr.getKindHint().getName();
 		return fromMinutes + " (" + rangeKind + ")";
 	}
 
 	public void writeRange(List<Marker> markers, Sheet sheet, Row valueRow,
 			List<Value> range) {
 
 		CreationHelper createHelper = this.getWorkBook().getCreationHelper();
 		CellStyle cellStyle = this.getWorkBook().createCellStyle();
 		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
 				"m-d-yy h:mm"));
 
 		// Styles for markers.
 //		CellStyle markerStyleRed = this.getWorkBook().createCellStyle();
 //		markerStyleRed.setFillForegroundColor(IndexedColors.RED.getIndex());
 //		markerStyleRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		
 		CellStyle markerStyleRed  = createRedBorderStyle();
 		CellStyle markerStyleAmber  = createAmberBorderStyle();
 		
 //		CellStyle markerStyleAmber = this.getWorkBook().createCellStyle();
 //		markerStyleAmber.setFillPattern(CellStyle.SOLID_FOREGROUND);
 //		markerStyleAmber
 //				.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
 
 		// Write the values.
 		for (Value v : range) {
 
 			// lookup the value.
 			int valueIndex = tsColumnForValue(v);
 			if (valueIndex == -1) {
 				continue;
 			}
 
 			Cell valueCell = valueRow.createCell(valueIndex);
 			valueCell.setCellValue(v.getValue());
 
 			// Adapt the width of the column for this value.
 			sheet.setColumnWidth(valueIndex, 14 * 256);
 
 			// Set the markers.
 			if (markers != null) {
 				Marker m;
 				if ((m = this.getModelUtils().markerForValue(markers, v)) != null) {
 					if (m instanceof ToleranceMarker) {
 						switch (((ToleranceMarker) m).getLevel().getValue()) {
 						case LevelKind.RED_VALUE: {
 							valueCell.setCellStyle(markerStyleRed);
 						}
 							break;
 						case LevelKind.AMBER_VALUE: {
 							valueCell.setCellStyle(markerStyleAmber);
 						}
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private CellStyle createAmberBorderStyle() {
 		CellStyle style = this.getWorkBook().createCellStyle();
 	    style.setBorderBottom(CellStyle.BORDER_THIN);
 	    style.setBottomBorderColor(IndexedColors.ORANGE.getIndex());
 	    style.setBorderLeft(CellStyle.BORDER_THIN);
 	    style.setLeftBorderColor(IndexedColors.ORANGE.getIndex());
 	    style.setBorderRight(CellStyle.BORDER_THIN);
 	    style.setRightBorderColor(IndexedColors.ORANGE.getIndex());
 	    style.setBorderTop(CellStyle.BORDER_THIN);
 	    style.setTopBorderColor(IndexedColors.ORANGE.getIndex());
 	    return style;
 	}
 
 	private CellStyle createRedBorderStyle() {
 		CellStyle style = this.getWorkBook().createCellStyle();
 	    style.setBorderBottom(CellStyle.BORDER_THIN);
 	    style.setBottomBorderColor(IndexedColors.RED.getIndex());
 	    style.setBorderLeft(CellStyle.BORDER_THIN);
 	    style.setLeftBorderColor(IndexedColors.RED.getIndex());
 	    style.setBorderRight(CellStyle.BORDER_THIN);
 	    style.setRightBorderColor(IndexedColors.RED.getIndex());
 	    style.setBorderTop(CellStyle.BORDER_THIN);
 	    style.setTopBorderColor(IndexedColors.RED.getIndex());
 	    return style;
 	}
 
 	private int tsColumnForValue(Value v) {
 		final Date toLookup = modelUtils.fromXMLDate(v.getTimeStamp());
 		Map<Integer, Date> filterEntries = Maps.filterEntries(columnTS,
 				new Predicate<Entry<Integer, Date>>() {
 
 					public boolean apply(Entry<Integer, Date> input) {
 						Date value = input.getValue();
 						return value.compareTo(toLookup) == 0;
 					}
 
 				});
 
 		// there should only be one entry, ugly hack.
		// http://work.netxforge.com/issues/292
 		if (filterEntries.size() == 1) {
			return filterEntries.keySet().iterator().next() -1 ;
 		}
 		return -1;
 	}
 
 	/**
 	 * Has a side effect of populating the columnTSMap with the index and date.
 	 * 
 	 */
 	public int writeTS(Map<Integer, Date> columnTSMap, Sheet sheet, Row tsRow,
 			List<Date> range, int weekNumber, int columnIndex) {
 
 		CreationHelper createHelper = this.getWorkBook().getCreationHelper();
 		CellStyle dateStyle = this.getWorkBook().createCellStyle();
 		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(
 				"m-d-yy h:mm"));
 
 		// Write the values.
 		int valueIndex = columnIndex;
 		for (Date d : range) {
 			Cell tsCell = tsRow.createCell(valueIndex);
 			// sheet.setColumnWidth(valueIndex, 14 * 256);
 			tsCell.setCellValue(d);
 			tsCell.setCellStyle(dateStyle);
 			valueIndex++;
 			columnTS.put(valueIndex, d);
 		}
 		return valueIndex;
 	}
 
 	// Getters and setters.
 
 	public ModelUtils getModelUtils() {
 		return modelUtils;
 	}
 
 	public void setModelUtils(ModelUtils modelUtils) {
 		this.modelUtils = modelUtils;
 	}
 
 	public DateTimeRange getPeriod() {
 		return period;
 	}
 
 	public void setPeriod(DateTimeRange period) {
 		this.period = period;
 	}
 
 	public Workbook getWorkBook() {
 		return workBook;
 	}
 
 	public void setWorkBook(Workbook workBook) {
 		this.workBook = workBook;
 	}
 
 	public Row rowForIndex(Sheet sheet, int rowIndex) {
 		Row tsRow = sheet.getRow(rowIndex);
 		if (tsRow == null) {
 			tsRow = sheet.createRow(rowIndex);
 		}
 		return tsRow;
 	}
 
 }
