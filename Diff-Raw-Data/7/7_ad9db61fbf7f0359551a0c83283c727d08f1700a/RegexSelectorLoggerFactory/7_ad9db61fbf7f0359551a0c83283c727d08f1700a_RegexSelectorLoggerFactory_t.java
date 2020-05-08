 /*
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
 package org.araqne.log.api;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 
 @Component(name = "regex-selector-logger-factory")
 @Provides
 public class RegexSelectorLoggerFactory extends AbstractLoggerFactory {
 	private static final String OPT_SOURCE_LOGGER = "source_logger";
 	private static final String OPT_PATTERN = "pattern";
 	private static final String OPT_INVERT = "invert";
 
 	@Requires
 	private LoggerRegistry loggerRegistry;
 
 	@Override
 	public String getName() {
 		return "regex-selector";
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "정규표현식 로그 선택자";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "正規表現ログセレクター";
 		if(locale != null && locale.equals(Locale.CHINESE))
 			return "正则表达式筛选器";
 		return "Regex Selector";
 	}
 
 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.KOREAN))
 			return "다른 로거로부터 정규표현식 패턴이 매칭되는 특정 로그들만 수집합니다.";
 		if (locale != null && locale.equals(Locale.JAPANESE))
 			return "他のロガーから正規表現がマッチングされるログだけ収集します。";
 		if(locale != null && locale.equals(Locale.CHINESE))
			return "从源数据采集器采集的数据中提取符合正则表达式特征的数据。";
 		return "select logs from logger using regular expression pattern matching";
 	}
 
 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.KOREAN, Locale.JAPANESE, Locale.CHINESE);
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption loggerName = new StringConfigType(OPT_SOURCE_LOGGER, t("Source logger name", "원본 로거 이름", "元ロガー名","源数据采集器"), t(
				"Full name of data source logger", "네임스페이스를 포함한 원본 로거 이름", "ネームスペースを含む元ロガー名","包含命名空间的源数据采集器名称"), true);
 		LoggerConfigOption pattern = new StringConfigType(OPT_PATTERN, t("Regex pattern", "정규표현식 패턴", "正規表現パターン","正则表达式模式"), t(
 				"Regex pattern to match", "매칭할 정규표현식", "マッチングする正規表現", "要匹配的正则表达式"), true);
 		LoggerConfigOption invert = new StringConfigType(OPT_INVERT, t("Invert match", "매칭 결과 반전", "結果反転","返回匹配结果"), t(
 				"Invert pattern match result", "정규표현식 매칭 결과 반전", "正規表現マッチング結果を反転します。", "返回正则表达式匹配结果"), false);
 		return Arrays.asList(loggerName, pattern, invert);
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
 		return new RegexSelectorLogger(spec, this, loggerRegistry);
 	}
 }
