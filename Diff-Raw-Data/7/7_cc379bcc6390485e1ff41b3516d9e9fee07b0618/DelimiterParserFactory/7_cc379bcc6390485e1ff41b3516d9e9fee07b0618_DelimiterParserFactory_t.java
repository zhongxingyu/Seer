 /*
  * Copyright 2010 NCHOVY
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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 
 @Component(name = "delimiter-parser-factory")
 @Provides
 public class DelimiterParserFactory extends AbstractLogParserFactory {
 
 	private static final String DELIMITER_TARGET = "delimiter_target";
 	private static final String DELIMITER = "delimiter";
 	private static final String COLUMN_HEADERS = "column_headers";
 	private static final String INCLUDE_DELIMITER_TARGET = "include_delimiter_target";
 
 	@Override
 	public String getName() {
 		return DELIMITER;
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "구분자";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "区切り文字";
 		if(locale != null && locale.equals(Locale.CHINESE))
 			return "分隔符";
 		return "Delimiter";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "구분자로 구분된 각 토큰에 대하여 설정된 필드 이름들을 순서대로 적용하여 파싱합니다.";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "区切り文字で分けている各トークンに設定されたフィールド名を順番に適用し解析します。";
 		if(locale != null && locale.equals(Locale.CHINESE))
 			return "对以分隔符分隔的各关键字以设置的字段名称顺序进行解析。";
 		return "devide a string into tokens based on the given delimiter and column names";
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
 		List<LoggerConfigOption> options = new ArrayList<LoggerConfigOption>();
 		options.add(new StringConfigType(DELIMITER, t(DELIMITER, "구분자", "区切り文字", "分隔符"), t(
 				"one delimiter character or 4-digit unicode escape sequence (e.g. \u0007)",
 				"하나의 아스키 구분자 혹은 4자리 유니코드 이스케이프 시퀀스 (예: \u0007)", 
 				"一つのアスキー区切り文字もしくは４桁のユニコードエスケープシーケンス(例: \u0007)",
 				"一个ASCII码字符或4位unicode字符(示例: \u0007)"), true));
 		options.add(new StringConfigType(COLUMN_HEADERS, t("column headers", "필드 이름 목록", "フィールド名リスト", "字段名称列表"),
 				t("separated by comma",
 				"쉼표로 구분된 필드 이름들", "コンマで分けているフィールドな", "以逗号分隔的字段名称"), false));
 		options.add(new StringConfigType(DELIMITER_TARGET, t("delimiter target field", "대상 필드", "対象フィールド", "目标字段"), t(
 				"delimiter target field name", "구분자로 파싱할 대상 필드 이름", "区切り文字で解析するフィールドな", "需要以分隔符解析的目标字段名称"), false));
 		options.add(new StringConfigType(INCLUDE_DELIMITER_TARGET, t("include delimiter target", "원본 값 포함 여부", "原本含み可否", "是否包含原始字段"), t(
 				"return also delimiter target field (true or false)", "구분자로 파싱된 결과 외에 원본 필드 값도 포함할지 설정합니다. true 혹은 false",
 				"区切り文字で解析した結果に原本フィールドを含むか設定します。trueまたはfalse", "以逗号分隔的原始字段=变更字段名称列表"), false));
 
 		return options;
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
 	public LogParser createParser(Map<String, String> config) {
 		String delimiter = config.get(DELIMITER);
 		if (delimiter == null)
 			delimiter = " ";
 
 		String[] columnHeaders = null;
 		String h = config.get(COLUMN_HEADERS);
		if (h != null) {
 			columnHeaders = h.split(",");
		
			for (int i = 0; i < columnHeaders.length; i++)
				columnHeaders[i] = columnHeaders[i].trim();
		}
 
 		boolean includeDelimiterTarget = false;
 		if (config.containsKey(INCLUDE_DELIMITER_TARGET)) {
 			String s = config.get(INCLUDE_DELIMITER_TARGET);
 			includeDelimiterTarget = (s != null && Boolean.parseBoolean(s));
 		}
 
 		String delimiterTarget = config.get(DELIMITER_TARGET);
 		if (delimiterTarget == null)
 			delimiterTarget = "line";
 
 		return new DelimiterParser(delimiter, columnHeaders, delimiterTarget, includeDelimiterTarget);
 	}
 }
