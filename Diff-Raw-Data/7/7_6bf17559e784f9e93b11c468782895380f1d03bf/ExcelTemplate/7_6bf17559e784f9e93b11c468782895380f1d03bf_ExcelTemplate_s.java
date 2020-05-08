 /*
  * Copyright 2002-2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.gageot.excel.core;
 
 import static com.google.common.base.Preconditions.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 import org.springframework.dao.CleanupFailureDataAccessException;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataAccessResourceFailureException;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.ObjectArrays;
 
 /**
  * <b>This is the central class in the Excel core package.</b>
  * It simplifies the use of Excel and helps to avoid common errors.
  * It executes core Excel parsing workflow, leaving application code extract results.
  *
  * <p>Code using this class need only implement callback interfaces, giving
  * them a clearly defined contract. The RowCallbackHandler
  * interface extracts values from each row of a HSSFSheet.
  *
  * <p>Can be used within a service implementation via direct instantiation
  * with a Resource reference, or get prepared in an application context
  * and given to services as bean reference.
  *
  * <p>The motivation and design of this class is inspired from JdbcTemplate.
  *
  * <p>Because this class is parameterizable by the callback interfaces,
  * it isn't necessary to subclass it.
  *
  * @author David Gageot
  * @see SheetExtractor
  * @see RowCallbackHandler
  * @see RowMapper
  */
 public class ExcelTemplate implements InitializingBean {
 	/** Used to obtain Excel data throughout the lifecycle of this object */
 	private Resource resource;
 
 	/**
 	 * Construct a new ExcelTemplate for bean usage.
 	 * Note: The Resource has to be set before using the instance.
 	 * This constructor can be used to prepare a ExcelTemplate via a BeanFactory,
 	 * typically setting the Resource via setResource.
 	 * @see #setResource
 	 */
 	public ExcelTemplate() {
 		// Do nothing
 	}
 
 	/**
 	 * Construct a new ExcelTemplate, given a Resource to obtain the Excel stream from.
 	 * @param aResource Resource to obtain the Excel stream from
 	 */
 	public ExcelTemplate(Resource aResource) {
 		setResource(aResource);
 		afterPropertiesSet();
 	}
 
 	/**
 	 * Construct a new ExcelTemplate, given an Excel File.
 	 * @param aFile Excel file
 	 */
 	public ExcelTemplate(File aFile) {
 		setResource(new FileSystemResource(aFile));
 		afterPropertiesSet();
 	}
 
 	/**
 	 * Construct a new ExcelTemplate, given a path to an Excel file.
 	 * The path can be relative to the given class,
 	 * or absolute within the classpath via a leading slash.
 	 * @param aPath relative or absolute path within the class path
 	 * @param aClass the class to load resources with
 	 * @see java.lang.Class#getResourceAsStream
 	 */
 	public ExcelTemplate(String aPath, Class<?> aClass) {
 		setResource(new ClassPathResource(aPath, aClass));
 		afterPropertiesSet();
 	}
 
 	/**
 	 * Read the sheet names of an Excel file.
 	 * @return an array containing a java.lang.String for each sheet. Empty if not sheet.
 	 * @throws DataAccessException if there is any problem
 	 */
 	public String[] getSheetNames() {
 		return read(new Function<HSSFWorkbook, String[]>() {
 			@Override
 			public String[] apply(HSSFWorkbook workbook) {
 				int sheetCount = workbook.getNumberOfSheets();
 
 				String[] sheetNames = new String[sheetCount];
 				for (int i = 0; i < sheetCount; i++) {
 					sheetNames[i] = workbook.getSheetName(i);
 				}
 
 				return sheetNames;
 			}
 		});
 	}
 
 	/**
 	 * Read the content of an Excel file for a given sheet name.
 	 * The content of the sheet is extracted using SheetExtractor.
 	 * @param sheetName name of the excel sheet
 	 * @param sheetExtractor object that will extract results
 	 * @return an arbitrary result object, as returned by the ResultSetExtractor
 	 * @throws DataAccessException if there is any problem
 	 */
 	public <T> T read(final String sheetName, final SheetExtractor<T> sheetExtractor) throws DataAccessException {
 		checkNotNull(sheetExtractor, "SheetExtractor must not be null");
 		checkNotNull(sheetName, "sheetName must not be null");
 
 		return read(new Function<HSSFWorkbook, T>() {
 			@Override
 			public T apply(HSSFWorkbook workbook) {
 				HSSFSheet sheet = workbook.getSheet(sheetName);
 				try {
 					return sheetExtractor.extractData(sheet);
 				} catch (IOException e) {
 					throw new DataAccessResourceFailureException("Problem reading file", e);
 				}
 			}
 		});
 	}
 
 	private <T> T read(Function<HSSFWorkbook, T> transform) {
 		checkNotNull(getResource(), "resource must not be null");
 
 		InputStream in = null;
 		try {
			in = getResource().getInputStream();
 
			return transform.apply(new HSSFWorkbook(in));
 		} catch (IOException e) {
 			throw new DataAccessResourceFailureException("Problem reading file", e);
 		} finally {
 			if (null != in) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new CleanupFailureDataAccessException("Problem closing file", e);
 				}
 			}
 		}
 	}
 
 	public String[][] read(String sheetName) throws DataAccessException {
 		return read(sheetName, new StringCellMapper(), String.class);
 	}
 
 	public <T> T[][] read(String sheetName, CellMapper<T> cellMapper, Class<T> clazz) throws DataAccessException {
 		return read(sheetName, new ObjectArraySheetExtractor<T>(cellMapper, clazz));
 	}
 
 	public <T> List<T> readList(String sheetName, RowMapper<T> rowMapper) throws DataAccessException {
 		return read(sheetName, new RowMapperSheetExtractor<T>(rowMapper));
 	}
 
 	public List<Map<String, String>> readList(String sheetName) throws DataAccessException {
 		return readList(sheetName, new StringCellMapper());
 	}
 
 	public <T> List<Map<String, T>> readList(String sheetName, CellMapper<T> cellMapper) throws DataAccessException {
 		MapListRowCallbackHandler<T> rowHandler = new MapListRowCallbackHandler<T>(cellMapper);
 
 		read(sheetName, rowHandler);
 
 		return rowHandler.getValues();
 	}
 
 	public void read(String sheetName, RowCallbackHandler rowCallbackHandler) throws DataAccessException {
 		read(sheetName, new RowCallbackHandlerSheetExtractor(rowCallbackHandler));
 	}
 
 	public void read(String sheetName, CellCallbackHandler cellCallbackHandler) throws DataAccessException {
 		read(sheetName, new CellCallbackHandlerSheetExtractor(cellCallbackHandler));
 	}
 
 	public <T> List<T> readBeans(String sheetName, Class<T> clazz) throws DataAccessException {
 		BeanCellCallbackHandler<T> handler = new BeanCellCallbackHandler<T>(clazz);
 
 		read(sheetName, handler);
 
 		return handler.getBeans();
 	}
 
 	/**
 	 * Set the Resource to obtain the Excel stream from.
 	 */
 	public Resource getResource() {
 		return resource;
 	}
 
 	/**
 	 * Return the Resource used by this template.
 	 */
 	public void setResource(Resource aResource) {
 		resource = aResource;
 	}
 
 	/**
 	 */
 	@Override
 	public void afterPropertiesSet() {
 		checkArgument(null != getResource(), "resource is required");
 	}
 
 	/**
 	 * RowMapper implementation that creates a <code>java.lang.Object</code> array
 	 * for each row.
 	 */
 	private static class ObjectArrayRowMapper<T> implements RowMapper<T[]> {
 		private final CellMapper<T> cellMapper;
 		private final Class<T> cellClass;
 
 		public ObjectArrayRowMapper(CellMapper<T> aCellMapper, Class<T> aCellClass) {
 			cellMapper = aCellMapper;
 			cellClass = aCellClass;
 		}
 
 		@Override
 		public T[] mapRow(HSSFRow row, int rowNum) throws IOException {
 			short lastColumnNum = row.getLastCellNum();
 
 			if (lastColumnNum <= 0) {
 				lastColumnNum = 0; // TODO
 			}
 
 			T[] rowValues = ObjectArrays.newArray(cellClass, lastColumnNum);
 
 			for (short columnNum = 0; columnNum < lastColumnNum; columnNum++) {
 				rowValues[columnNum] = cellMapper.mapCell(row.getCell(columnNum, Row.RETURN_BLANK_AS_NULL), rowNum, columnNum);
 			}
 
 			return rowValues;
 		}
 	}
 
 	/**
 	 * Adapter to enable use of a ObjectArrayRowMapper inside a SheetExtractor.
 	 */
 	private static class ObjectArraySheetExtractor<T> implements SheetExtractor<T[][]> {
 		private final CellMapper<T> cellMapper;
 		private final Class<T> cellClass;
 
 		public ObjectArraySheetExtractor(CellMapper<T> aCellMapper, Class<T> aCellClass) {
 			cellMapper = aCellMapper;
 			cellClass = aCellClass;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public T[][] extractData(HSSFSheet sheet) throws IOException {
 			List<T[]> rowValues = Lists.newArrayList();
 
 			ObjectArrayRowMapper<T> rowMapper = new ObjectArrayRowMapper<T>(cellMapper, cellClass);
 
 			int maxRowSize = 0;
 			int firstRowIndex = sheet.getFirstRowNum();
 			int lastRowIndex = sheet.getLastRowNum();
 
 			for (int i = firstRowIndex; i <= lastRowIndex; i++) {
 				HSSFRow row = sheet.getRow(i);
 
 				if (null != row) {
 					T[] currentRowValues = rowMapper.mapRow(row, i);
 					maxRowSize = Math.max(maxRowSize, currentRowValues.length);
 					rowValues.add(currentRowValues);
 				}
 			}
 
 			return rowValues.toArray((T[][]) Array.newInstance(cellClass, new int[] {
 					rowValues.size(), maxRowSize
 			}));
 		}
 	}
 
 	/**
 	 * RowCallbackHandler implementation that creates a <code>java.util.Map</code>
 	 * for each row and put all maps in a list.
 	 * The first line is used as keys for the maps.
 	 */
 	private static class MapListRowCallbackHandler<T> implements RowCallbackHandler {
 		private final List<Map<String, T>> values = new ArrayList<Map<String, T>>();
 		private ColumnMapRowMapper<T> rowMapper;
 		protected final CellMapper<T> cellMapper;
 
 		public MapListRowCallbackHandler(CellMapper<T> aCellMapper) {
 			cellMapper = aCellMapper;
 		}
 
 		@Override
 		public void processRow(HSSFRow row, int rowIndex) throws IOException {
 			if (null == rowMapper) { // First line, read keys
 				RowMapper<String[]> firstRowMapper = new ObjectArrayRowMapper<String>(new StringCellMapper(), String.class);
 
 				String[] keys = firstRowMapper.mapRow(row, rowIndex);
 
 				rowMapper = new ColumnMapRowMapper<T>(keys, cellMapper);
 			} else { // Other lines, read values
 				values.add(rowMapper.mapRow(row, rowIndex));
 			}
 		}
 
 		public List<Map<String, T>> getValues() {
 			return values;
 		}
 	}
 
 	/**
 	 * Adapter to enable use of a RowCallbackHandler inside a SheetExtractor.
 	 */
 	private static class RowCallbackHandlerSheetExtractor implements SheetExtractor<Void> {
 		private final RowCallbackHandler rowCallbackHandler;
 
 		public RowCallbackHandlerSheetExtractor(RowCallbackHandler aRowCallbackHandler) {
 			rowCallbackHandler = aRowCallbackHandler;
 		}
 
 		@Override
 		public Void extractData(HSSFSheet sheet) throws IOException {
 			int firstRowIndex = sheet.getFirstRowNum();
 			int lastRowIndex = sheet.getLastRowNum();
 
 			for (int i = firstRowIndex; i <= lastRowIndex; i++) {
 				rowCallbackHandler.processRow(sheet.getRow(i), i);
 			}
 
 			return null;
 		}
 	}
 
 	/**
 	 * Adapter to enable use of a CellCallbackHandler inside a SheetExtractor.
 	 */
 	private static class CellCallbackHandlerSheetExtractor implements SheetExtractor<Void> {
 		private final CellCallbackHandler cellCallbackHandler;
 
 		public CellCallbackHandlerSheetExtractor(CellCallbackHandler aCellCallbackHandler) {
 			cellCallbackHandler = aCellCallbackHandler;
 		}
 
 		@Override
 		public Void extractData(HSSFSheet sheet) throws IOException {
 			int firstRowIndex = sheet.getFirstRowNum();
 			int lastRowIndex = sheet.getLastRowNum();
 
 			for (int i = firstRowIndex; i <= lastRowIndex; i++) {
 				HSSFRow row = sheet.getRow(i);
 				if (null != row) {
 					short firstColIndex = row.getFirstCellNum();
 					short lastColIndex = row.getLastCellNum();
 
 					for (short j = firstColIndex; j < lastColIndex; j++) {
 						cellCallbackHandler.processCell(row.getCell(j, Row.RETURN_BLANK_AS_NULL), i, j);
 					}
 				}
 			}
 
 			return null;
 		}
 	}
 }
