 /*
  * Copyright 2013 Future Systems
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
 package org.araqne.logdb.query.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.StringTokenizer;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandParser;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryErrorMessage;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.query.parser.QueryTokenizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-query-parser-service")
 @Provides
 public class QueryParserServiceImpl implements QueryParserService {
 	private final Logger slog = LoggerFactory.getLogger(QueryParserServiceImpl.class);
	private ConcurrentMap<String, QueryCommandParser> commandParsers = new ConcurrentHashMap<String, QueryCommandParser>(); 
	private ConcurrentMap<String, QueryErrorMessage> errorMappings = new ConcurrentHashMap<String, QueryErrorMessage>();
 
 	@Requires
 	private FunctionRegistry functionRegistry;
 
 	@Validate
 	public void start() {
		commandParsers.clear();
		errorMappings.clear();
 		registerBuiltinErrors();
 	}
 
 	// support unit test
 	public void setFunctionRegistry(FunctionRegistry functionRegistry) {
 		this.functionRegistry = functionRegistry;
 	}
 
 	@Override
 	public QueryCommandParser getCommandParser(String name) {
 		return commandParsers.get(name);
 	}
 
 	@Override
 	public List<QueryCommand> parseCommands(QueryContext context, String queryString) {
 		List<QueryCommand> commands = new ArrayList<QueryCommand>();
 		int offsetCnt = 0; //
 		try {
 			for (String q : QueryTokenizer.parseCommands(queryString)) {
 				q = q.trim();
 				StringTokenizer tok = new StringTokenizer(q, " \n\t");
 				String commandType = tok.nextToken();
 				QueryCommandParser parser = commandParsers.get(commandType);
 				if (parser == null) {
 					throw new QueryParseException("unsupported-command", -1, "command is [" + commandType + "]");
 				}
 
 				QueryCommand cmd = parser.parse(context, q);
 				commands.add(cmd);
 				offsetCnt++; //
 			}
 		} catch (QueryParseException e) {
 			closePrematureCommands(commands);
 			// XXX : 오프셋 위치가 정확하지 않을 수 있으니 테스트 해 볼 것!
 			e.addOffset(offsetCnt);
 			// FIXME : 로케일 하드 코딩 되어 있음
 			String errorMessage = formatErrorMessage(e.getType(), Locale.ENGLISH, e.getParams());
 			throw new QueryParseException(e.getType(), e.getStartOffset(), errorMessage);
 		} catch (Throwable t) {
 			closePrematureCommands(commands);
 			slog.debug("QueryParserServiceImpl", t);
 
 			throw new QueryParseException("parse failure", -1, t.toString());
 		}
 
 		if (commands.isEmpty())
 			throw new IllegalArgumentException("empty query");
 
 		for (int i = 0; i < commands.size(); i++) {
 			QueryCommand command = commands.get(i);
 			if (i < commands.size() - 1)
 				command.setOutput(new QueryCommandPipe(commands.get(i + 1)));
 		}
 
 		return commands;
 	}
 
 	private void closePrematureCommands(List<QueryCommand> commands) {
 		for (QueryCommand cmd : commands) {
 			try {
 				slog.debug("araqne logdb: parse failed, closing command [{}]", cmd.toString());
 				cmd.onClose(QueryStopReason.CommandFailure);
 			} catch (Throwable t2) {
 				slog.error("araqne logdb: cannot close command", t2);
 			}
 		}
 	}
 
 	@Override
 	public String formatErrorMessage(String errorCode, Locale locale, Map<String, String> params) {
 		QueryErrorMessage m = errorMappings.get(errorCode);
 		if (m == null)
 			return null;
 
 		return m.format(locale, params);
 	}
 
 	@Override
 	public void addCommandParser(QueryCommandParser parser) {
 		parser.setQueryParserService(this);
 		commandParsers.putIfAbsent(parser.getCommandName(), parser);
 
 		for (Entry<String, QueryErrorMessage> e : parser.getErrorMessages().entrySet()) {
 			errorMappings.put(e.getKey(), e.getValue());
 		}
 	}
 
 	@Override
 	public void removeCommandParser(QueryCommandParser parser) {
 		for (Entry<String, QueryErrorMessage> e : parser.getErrorMessages().entrySet()) {
 			errorMappings.remove(e.getKey(), parser);
 		}
 
 		commandParsers.remove(parser.getCommandName(), parser);
 	}
 
 	@Override
 	public FunctionRegistry getFunctionRegistry() {
 		return functionRegistry;
 	}
 
 	private void registerBuiltinErrors() {
 		/* QueryTokenizer */
 		add("90000", "option-space-not-allowed", "옵션과 '=' 사이에 공백은 허용되지 않습니다.");
 		add("90001", "invalid-option", "[option]은 지원하지 않는 옵션입니다.");
 		add("90002", "string-quote-mismatch", "\"의 짝이 맞지 않습니다.");
 		add("90003", "empty-command", "쿼리가 없습니다.");
 		add("90004", "need-string-token", "입력된 쿼리가 없습니다.");
 		add("90005", "string-quote-mismatch", "\"의 짝이 맞지 않습니다.");
 		/* EvalOpEmitterFactory */
 		add("90100", "broken-expression", "잘못된 표현식입니다.");
 		add("90101", "unsupported operator", "[op]은 지원하지 않는 연산자입니다.");
 		/* ExpressionParser */
 		add("90200", "unexpected-term", "쿼리가 없습니다.");
 		add("90201", "remain-terms", "잘못된 쿼리 입니다.");
 		add("90202", "parens-mismatch", "잘못된 쿼리 입니다.");
 		add("90203", "quote-mismatch", "\"가 짝이 맞지 않습니다.");
 		add("90204", "sqbracket-mismatch", "'['가 짝이 맞지 않습니다.");
 		add("90205", "invalid-escape-sequence", "잘못된 이스케이프 문자입니다.([escape])");
 		/* MetadataMatcher */
 		add("90300", "broken-expression", "쿼리가 없습니다.");
 		/* Strings */
 		add("90400", "invalid-escape-sequence", "[char] ]지원하지 않는 이스케이프 문자입니다.");
 		/* TimeSpan */
 		add("90500", "invalid-timespan", "mon 은 12의 소인수(1,2,3,4,6)만 사용가능합니다.");
 		add("90501", "year should be 1", "y 앞에는 1만 사용가능합니다.");
 		/* Abs */
 		add("90600", "invalid-abs-args", "abs의 매개변수는 하나여야만 합니다.");
 		/* ContextReference */
 		add("90610", "null-context-reference", "값이 입력되지 않았습니다.");
 		add("90611", "null-context-reference", "null 값이 입력되었습니다.");
 		/* dateAdd */
 		add("90620", "invalid-dateadd-args", "올바르지 않는 입력 형식입니다.");
 		add("90621", "invalid-dateadd-calendar-field", "[field]는 잘못된 유형입니다.");
 		add("90622", "invalid-dateadd-delta-type", "[time]는 잘못된 시간입니다.");
 		/* DateDiff */
 		add("90630", "invalid-datediff-args", "올바르지 않는 입력 형식입니다.");
 		add("90631", "invalid-datediff-unit", "[field]는 잘못된 유형입니다.");
 		/* DateTrunc */
 		add("90640", "invalid-datetrunc-args", "올바르지 않는 입력 형식입니다.");
 		/* Decrypt */
 		add("90650", "insufficient-decrypt-args", "올바르지 않는 입력 형식입니다.");
 		add("90651", "invalid-cipher-algorithm", "[algorithm]은 지원하지 않는 암호화 알고리즘 입니다.");
 		/* Encrypt */
 		add("90660", "insufficient-encrypt-args", "올바르지 않는 입력 형식입니다.");
 		add("90661", "invalid-cipher-algorithm", "[algorithm]은 지원하지 않는 암호화 알고리즘 입니다.");
 		/* Field */
 		add("90670", "missing-field-name", "필드 이름을 입력하십시오.");
 		/* FromBase64 */
 		add("90680", "frombase64-arg-missing", "변환할 문자열을 입력하십시오.");
 		/* Hash */
 		add("90690", "missing-hash-algorithm", "해시 알고리즘을 입력하십시오.");
 		add("90691", "missing-hash-data", "바이너리 표현식을 입력하십시오.");
 		add("90692", "unsupported-hash", "[algorithm]은 지원하지 않는 해시 알고리즘 입니다.");
 		/* In */
 		//add("90700", "insufficient-arguments", "올바르지 않는 입력 형식입니다."); - 99000 으로 대체
 		/* Ip2Long */
 		add("90710", "invalid-ip2long-args", "올바르지 않는 입력 형식입니다.");
 		/* Left */
 		add("90720", "left-func-negative-length", "길이는 0보다 커야 합니다.(입력값 : [length])");
 		/* Long2Ip */
 		add("90730", "invalid-long2ip-args", "올바르지 않는 입력 형식입니다.");
 		/* Network */
 		add("90740", "invalid-mask", "CIDR 값이 올바르지 않습니다.(입력값: [mask])");
 		/* Rand */
 		add("90750", "invalid-rand-argument", "경계값은 숫자여야 합니다.(입력값: [bound])");
 		add("90751", "rand-bound-should-be-positive", "경계값은 양수여야 합니다.(입력값: [bound])");
 		/* RandBytes */
 		add("90760", "invalid-rand-argument", "길이는 숫자여야 합니다.(입력값: [length])");
 		add("90761", "invalid-randbytes-len", "길이는 0보다 크거나 1000000보다 작아야 합니다.(입력값: [length])");
 		/* Split */
 		//add("90770", "missing-split-args", "올바르지 않는 입력 형식입니다."); - 99000 으로 대체
 		add("90771", "invalid-delimiters", "올바르지 않는  구분자입니다.([exception])");
 		add("90772", "empty-delimiters", "구분자가 없습니다.");
 		/* StrJoin */
 		add("90780", "nvalid-strjoin-args", "올바르지 않는  구분자입니다.([exception])");
 		add("90781", "strjoin-require-constant-separato", "올바르지 않는 입력 형식입니다.");
 		/* Substr */
 		//add("90790", "invalid-substr-range", "시작 위치는 0보다 커야합니다(입력값 : [begin])");
 		//add("90791", "invalid-substr-range", "끝위치([end])는 시작위치(begin])보다 커야합니다.");
 		/* ToBase64 */
 		//add("90800", "tobase64-arg-missing", "올바르지 않는 입력 형식입니다."); - 99000 으로 대체
 		/* ToBinary */
 		//add("90810", "missing-data", "올바르지 않는 입력 형식입니다."); - 99000 으로 대체
 		add("90811", "unsupported-charset", "[charset]은 지원하지 않는 인코딩입니다.");
 		/* ToDate */
 		add("90820", "invalid date format pattern", "날짜변환에 실패하였습니다.([exception])");
 		/* ToInt */
 		add("90830", "invalid-argument radix should be 10", "radix는 10이여야 합니다..(입력값: [radix])");
 		/* ToLong */
 		add("90840", "invalid-argument radix should be 10", "radix는 10이여야 합니다..(입력값: [radix])");
 		/* UrlDecode */
 		add("90850", "invalid-charset", "[charset]은 지원하지 않는 인코딩입니다.");
 		/* ValueOf */
 		//add("90860", "insufficient-valueof-args", "올바르지 않는 입력 형식입니다.");  - 99000 으로 대체
 		/* Values */
 		add("90870", "missing-values-arg", "올바르지 않는 입력 형식입니다.");
 		/* Count */
 		add("91010", "invalid-count-args", "올바르지 않는 입력 형식입니다.");
 		/* First */
 		add("91020", "invalid-parameter-count", "올바르지 않는 입력 형식입니다.");
 		/* Foramt */
 		add("91030", "invalid-format-string", "[exp]은 문자열 형식이 아닙니다.");
 		/* FunctionExpression */
 		add("99000", "[name]-arg-missing([args]-[min])" , "[name]의 매개변수는 [min] 개 이상이여야 합니다.( [args] 개 입력 됨)");
 		/* MetadataServiceImpl */
 		add("95000", "invalid-system-object-type, type=[type]", "[type]는 잘못된 타입입니다.");
 		/* LoggerMetadataProvider */
 		add("95010", "no-read-permission", "읽기 권한이 없습니다.");
 		add("95011", "logger-load-fail", "로거를 읽어오는데 실패했습니다.(msg:[msg])");
 		/* LogMetadataProvider */
 		add("95020", "no-read-permission", "읽기 권한이 없습니다.");
 		/* MetadataQueryStringParser */
 		add("95030", "invalid-from", "from 값이 유효하지 않습니다.");
 		add("95031", "invalid-to", "to 값이 유효하지 않습니다.");
 		add("95032", "invalid-date-rangen", "from 값이 유효하지 않습니다.(from:[from])");
 		/* ThreadMetadataProvider */
 		add("95040", "no-read-permission", "읽기 권한이 없습니다.");
 		/* TopThreadMetadataProvider */
 		add("95050", "no-read-permission", "읽기 권한이 없습니다.");
 		add("95051", "topthread-not-supported", "자바가상머신이 스레드 CPU 사용량 측정을 지원하지 않습니다.");
 		/* FunctionRegistryImpl */
 		add("90900", "unsupported-function", "[function] 은 지원하지 않는 함수입니다.");
 		add("90901", "cannot create function instance", "[function] 함수 오류: [msg].");
 		add("90902", "cannot create function instance", "[function] 함수 오류 : [msg].");
 		add("90903", "no-read-permission", "[funtion] 함수 읽기 권한이 없습니다.");
 	}
 
 	private void add(String code, String en, String ko) {
 		errorMappings.put(code, new QueryErrorMessage(en, ko));
 	}
 
 }
