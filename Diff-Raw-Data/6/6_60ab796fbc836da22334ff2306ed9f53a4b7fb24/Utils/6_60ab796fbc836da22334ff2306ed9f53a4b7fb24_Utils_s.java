 /**
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.EnumOption;
 import org.chai.kevin.data.EnumService;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.value.Value;
 
 /**
  * @author Jean Kahigiso M.
  * 
  */
 public class Utils {
 	private static EnumService enumService;
 	private static LanguageService languageService;
 	
 	private final static String DATE_FORMAT = "dd-MM-yyyy";
 	private final static String DATE_FORMAT_TIME = "dd-MM-yyyy hh:mm:ss";
 	private final static String CSV_FILE_EXTENSION = ".csv";
 	private final static String ZIP_FILE_EXTENSION = ".zip";
 
 	public static Set<String> split(String string) {
 		Set<String> result = new HashSet<String>();
 		if (string != null) result.addAll(Arrays.asList(StringUtils.split(string, ',')));
 		return result;
 	}
 
 	public static String unsplit(Object list) {
 		List<String> result = new ArrayList<String>();
 		
 		if (list instanceof String) result.add((String) list);
 		if (list instanceof Collection) result.addAll((Collection<String>)list);
 		else result.addAll(Arrays.asList((String[]) list));
 		
 		for (String string : new ArrayList<String>(result)) {
 			if (string.isEmpty()) result.remove(string);
 		}
 		
 		return StringUtils.join(result, ',');
 	}
 		
 	@SuppressWarnings("unused")
 	private static boolean matches(String text, String value) {
 		if (value == null) return false;
 		return value.matches("(?i).*"+text+".*");
 	}
 
 	public static Set<String> getUuids(List<DataLocationType> types) {
 		Set<String> result = new HashSet<String>();
 		for (DataLocationType type : types) {
 			result.add(type.getCode());
 		}
 		return result;
 	}
 	
 	public static String formatDate(Date date) {
 		if (date == null) return null;
 		return new SimpleDateFormat(DATE_FORMAT).format(date);
 	}
 	public static String formatDateWithTime(Date date) {
 		if (date == null) return null;
 		return new SimpleDateFormat(DATE_FORMAT_TIME).format(date);
 	}
 	
 	public static Date parseDate(String string) throws ParseException {
 		return new SimpleDateFormat(DATE_FORMAT).parse(string);
 	}
 	
 	public static boolean containsId(String string, Long id) {
 		return string.matches(".*\\$"+id+"(\\z|\\D|$).*");
 	}
 	
 	public static String stripHtml(String htmlString, Integer num) {
 		String noHtmlString;
 		Integer length = num;
 	
 		if (htmlString != null){
 			noHtmlString = htmlString.replace("&nbsp;", " ");
 			noHtmlString = noHtmlString.replaceAll("<.*?>", " ");
 			noHtmlString = StringEscapeUtils.unescapeHtml(noHtmlString);
 			noHtmlString = noHtmlString.trim();
 		}
 		else noHtmlString = htmlString;
 	
 		if (num == null || noHtmlString.length() <= num) return noHtmlString;
 		return noHtmlString.substring(0, length);
 	}
 	
 	public static String getValueString(Type type, Value value){
 		if(value != null && !value.isNull()){
 			switch (type.getType()) {
 			case NUMBER:
 				return value.getNumberValue().toString();
 			case BOOL:
 				return value.getBooleanValue().toString();
 			case STRING:
 				return value.getStringValue();
 			case TEXT:
 				return value.getStringValue();
 			case DATE:
 				if(value.getDateValue() != null) return Utils.formatDate(value.getDateValue());	
 				else return value.getStringValue();
 			case ENUM:
				String enumCode = type.getEnumCode();
				Enum enume = enumService.getEnumByCode(enumCode);
				if(enume != null){
					EnumOption enumOption = enume.getOptionForValue(value.getEnumValue());
					if (enumOption != null) return languageService.getText(enumOption.getNames());
				}
 				return value.getStringValue();
 			default:
 				throw new IllegalArgumentException("get value string can only be called on simple type");
 			}			
 		}
 		return "";
 	}
 	
 	public static File getZipFile(File file, String filename) throws IOException {		
 		
 		File zipFile = File.createTempFile(filename, ZIP_FILE_EXTENSION);
 
 		ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
 		FileInputStream fileInputStream = new FileInputStream(file);
 		try {						
 		    ZipEntry zipEntry = new ZipEntry(file.getName());
 		    zipOutputStream.putNextEntry(zipEntry);
 		    
 		    IOUtils.copy(fileInputStream, zipOutputStream);
 		    zipOutputStream.closeEntry();
 		} catch (IOException e) {
 			throw e;
 		} finally {
 		    IOUtils.closeQuietly(zipOutputStream);
 		    IOUtils.closeQuietly(zipOutputStream);
 		}
 			
 		return zipFile;
 	}
 	
 	
 }
