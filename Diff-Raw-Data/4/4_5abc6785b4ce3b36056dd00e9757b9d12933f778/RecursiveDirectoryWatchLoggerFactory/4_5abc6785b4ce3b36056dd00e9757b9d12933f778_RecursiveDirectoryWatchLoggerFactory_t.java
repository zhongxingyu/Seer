 /**
  * Copyright 2014 Eediom Inc.
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
 package org.araqne.log.api.nio;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.log.api.AbstractLoggerFactory;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.log.api.LoggerSpecification;
 import org.araqne.log.api.MutableStringConfigType;
 
 @Component(name = "recursive-directory-watch-logger-factory")
 @Provides
 public class RecursiveDirectoryWatchLoggerFactory extends AbstractLoggerFactory {
 	private boolean useNio;
 
 	@Override
 	public String getName() {
 		return "recursive-dirwatch";
 	}
 
 	@Validate
 	public void start() {
 		// null -> nio
 		this.useNio = false;
 		String version = System.getProperty("java.version");
 		boolean isJdk7 = (version == null) ? false : version.compareTo("1.7") >= 0;
 
 		String useNaive = System.getProperty("araqne.logapi.watcher");
 		boolean isNaive = (useNaive == null) ? false : useNaive.equals("naive");
 		useNio = isJdk7 && !isNaive;
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "리커시브 디렉터리 와처";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "再帰ディレクトリウォッチャー";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "递归目录监控";
 		return "Recursive Directory Watcher";
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "지정된 디렉터리에서 파일이름 패턴과 일치하는 모든 텍스트 로그 파일을 수집합니다.";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "指定されたディレクトリでファイル名パータンと一致するすべてのテキストログファイルを収集します。";
 		if (locale != null && locale.equals(Locale.CHINESE))
 			return "从指定目录(包括子目录)中采集所有文本文件。";
 		return "collect all text log files in specified directory";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
 		LoggerConfigOption basePath = new MutableStringConfigType("base_path", t("Directory path", "디렉터리 경로", "ディレクトリ経路", "目录"),
 				t(
 						"Base log file directory path", "로그 파일을 수집할 대상 디렉터리 경로", "ログファイルを収集する対象ディレクトリ経路", "要采集的日志文件所在目录"), true);
 
 		LoggerConfigOption dirPathPattern = new MutableStringConfigType("dirpath_pattern", t("Directory Path pattern",
				"디렉토리 이름 패턴", "ディレクトリ経路パータン", "文件名模式"), t("Regular expression to match directory path",
				"대상 로그 파일이 있는 디렉토리를 선택하는데 사용할 정규표현식", "対象ログファイルがあるディレクトリを選ぶとき使う正規表現", "表示要采集的文件所在目录的正则表达式"), false);
 
 		LoggerConfigOption fileNamePattern = new MutableStringConfigType("filename_pattern", t("Filename pattern", "파일이름 패턴",
 				"ファイル名パータン", "文件名模式"), t("Regular expression to match log file name", "대상 로그 파일을 선택하는데 사용할 정규표현식",
 				"対象ログファイルを選ぶとき使う正規表現", "用于筛选文件的正则表达式"), true);
 
 		LoggerConfigOption datePattern = new MutableStringConfigType("date_pattern", t("Date pattern", "날짜 정규표현식", "日付正規表現",
 				"日期正则表达式"), t("Regular expression to match date and time strings", "날짜 및 시각을 추출하는데 사용할 정규표현식", "日付と時刻を解析する正規表現",
 				"用于提取日期及时间的正则表达式"), false);
 
 		LoggerConfigOption dateFormat = new MutableStringConfigType("date_format", t("Date format", "날짜 포맷", "日付フォーマット", "日期格式"),
 				t("date format to parse date and time strings. e.g. yyyy-MM-dd HH:mm:ss",
 						"날짜 및 시각 문자열을 파싱하는데 사용할 포맷. 예) yyyy-MM-dd HH:mm:ss", "日付と時刻を解析するフォーマット。例) yyyy-MM-dd HH:mm:ss",
 						"用于解析日期及时间字符串的格式。 示例) yyyy-MM-dd HH:mm:ss"), false);
 
 		LoggerConfigOption dateLocale = new MutableStringConfigType("date_locale", t("Date locale", "날짜 로케일", "日付ロケール", "日期区域"),
 				t("date locale, e.g. en", "날짜 로케일, 예를 들면 ko", "日付ロケール。例えばjp", "日期区域， 例如 zh"), false);
 
 		LoggerConfigOption timezone = new MutableStringConfigType("timezone", t("Time zone", "시간대", "時間帯", "时区"), t(
 				"time zone, e.g. EST or America/New_york ", "시간대, 예를 들면 KST 또는 Asia/Seoul", "時間帯。例えばJSTまたはAsia/Tokyo",
 				"时区， 例如 Asia/Beijing"), false);
 
 		LoggerConfigOption newlogRegex = new MutableStringConfigType("newlog_designator", t("Regex for first line", "로그 시작 정규식",
 				"ログ始め正規表現", "日志起始正则表达式"), t("Regular expression to determine whether the line is start of new log."
 				+ "(if a line does not matches, the line will be merged to prev line.).",
 				"새 로그의 시작을 인식하기 위한 정규식(매칭되지 않는 경우 이전 줄에 병합됨)", "新しいログの始まりを認識する正規表現 (マッチングされない場合は前のラインに繋げる)"
 				, "用于识别日志起始位置的正则表达式(如没有匹配项，则合并到之前日志)"), false);
 
 		LoggerConfigOption newlogEndRegex = new MutableStringConfigType("newlog_end_designator", t("Regex for last line",
 				"로그 끝 정규식", "ログ終わり正規表現", "日志结束正则表达式"), t("Regular expression to determine whether the line is end of new log."
 				+ "(if a line does not matches, the line will be merged to prev line.).",
 				"로그의 끝을 인식하기 위한 정규식(매칭되지 않는 경우 이전 줄에 병합됨)", "ログの終わりを認識する正規表現 (マッチングされない場合は前のラインに繋げる)"
 				, "用于识别日志结束位置地正则表达式(如没有匹配项，则合并到之前日志)"), false);
 
 		LoggerConfigOption charset = new MutableStringConfigType("charset", t("Charset", "문자 집합", "文字セット", "字符集"), t(
 				"charset encoding", "텍스트 파일의 문자 인코딩 방식", "テキストファイルの文字エンコーディング方式", "文本文件的字符编码方式"), false);
 
 		LoggerConfigOption recursive = new MutableStringConfigType("recursive", t("Recursive", "하위 디렉터리 포함", "再帰", "包括下级目录"), t(
 				"Include sub-directories. default is false", "하위 디렉터리 포함 여부, 기본값 false", "下位ディレクトリを含む。基本値はfalse",
 				"是否包换下级目录，默认值为false。"), false);
 
 		LoggerConfigOption fileTag = new MutableStringConfigType("file_tag", t("Filename Tag", "파일네임 태그", "ファイル名タグ", "文件标记"), t(
 				"Field name for filename tagging", "파일명을 태깅할 필드 이름", "ファイル名をタギングするフィールド名", "要进行文件名标记的字段"), false);
 
 		return Arrays.asList(basePath, dirPathPattern, fileNamePattern, datePattern, dateFormat, dateLocale, timezone,
 				newlogRegex, newlogEndRegex, charset, recursive, fileTag);
 	}
 
 	private Map<Locale, String> t(String enText, String koText, String jpText, String cnText) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, enText);
 		m.put(Locale.KOREAN, koText);
 		m.put(Locale.JAPANESE, jpText);
 		m.put(Locale.CHINESE, cnText);
 		return m;
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		if (!useNio)
 			return new NaiveRecursiveDirectoryWatchLogger(spec, this);
 		else
 			return new NioRecursiveDirectoryWatchLogger(spec, this);
 	}
 }
