 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 
 @Component(name = "directory-watch-logger-factory")
 @Provides
 public class DirectoryWatchLoggerFactory extends AbstractLoggerFactory {
 
 	@Override
 	public String getName() {
 		return "dirwatch";
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale.equals(Locale.KOREAN))
 			return "디렉터리 와처";
 		return "Directory Watcher";
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale.equals(Locale.KOREAN))
 			return "지정된 디렉터리에서 파일이름 패턴과 일치하는 모든 텍스트 로그 파일을 수집합니다.";
 		return "collect all text log files in specified directory";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN);
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
 		LoggerConfigOption basePath = new StringConfigType("base_path", t("Directory path", "디렉터리 경로"), t(
 				"Base log file directory path", "로그 파일을 수집할 대상 디렉터리 경로"), true);
 
 		LoggerConfigOption fileNamePattern = new StringConfigType("filename_pattern", t("Filename pattern", "파일이름 패턴"), t(
 				"Regular expression to match log file name", "대상 로그 파일을 선택하는데 사용할 정규표현식"), true);
 
 		LoggerConfigOption datePattern = new StringConfigType("date_pattern", t("Date pattern", "날짜 정규표현식"), t(
 				"Regular expression to match date and time strings", "날짜 및 시각을 추출하는데 사용할 정규표현식"), false);
 
 		LoggerConfigOption dateFormat = new StringConfigType("date_format", t("Date format", "날짜 포맷"), t(
 				"date format to parse date and time strings. e.g. yyyy-MM-dd HH:mm:ss",
 				"날짜 및 시각 문자열을 파싱하는데 사용할 포맷. 예) yyyy-MM-dd HH:mm:ss"), false);
 
 		LoggerConfigOption dateLocale = new StringConfigType("date_locale", t("Date locale", "날짜 로케일"), t("date locale, e.g. en",
 				"날짜 로케일, 예를 들면 ko"), false);
 
 		LoggerConfigOption newlogRegex = new StringConfigType("newlog_designator",
				t("Regex for first line", "로그 시작 정규식"),
 				t("Regular expression to determine whether the line is start of new log."
 						+ "(if a line does not matches, the line will be merged to prev line.).",
 						"새 로그의 시작을 인식하기 위한 정규식(매칭되지 않는 경우 이전 줄에 병합됨)"), false);
 
 		LoggerConfigOption newlogEndRegex = new StringConfigType("newlog_end_designator",
				t("Regex for last line", "로그 끝 정규식"),
 				t("Regular expression to determine whether the line is end of new log."
 						+ "(if a line does not matches, the line will be merged to prev line.).",
 						"로그의 끝을 인식하기 위한 정규식(매칭되지 않는 경우 이전 줄에 병합됨)"), false);
 
 		LoggerConfigOption charset = new StringConfigType("charset", t("Charset", "문자 집합"), t("charset encoding",
 				"텍스트 파일의 문자 인코딩 방식"), false);
 
 		return Arrays.asList(basePath, fileNamePattern, datePattern, dateFormat, dateLocale, newlogRegex, newlogEndRegex, charset);
 	}
 
 	private Map<Locale, String> t(String enText, String koText) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, enText);
 		m.put(Locale.KOREAN, koText);
 		return m;
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		return new DirectoryWatchLogger(spec, this);
 	}
 
 	@Override
 	public void deleteLogger(String namespace, String name) {
 		super.deleteLogger(namespace, name);
 
 		// delete lastpos file
 		File dataDir = new File(System.getProperty("araqne.data.dir"), "araqne-log-api");
 		File f = new File(dataDir, "dirwatch-" + name + ".lastlog");
 		f.delete();
 	}
 
 }
