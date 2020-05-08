 package info.micdm.ftr.utils;
 
 import java.util.Date;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Как бы парсер HTML-страниц.
  * @author Mic, 2011
  * 
  */
 public abstract class HtmlParser {
 
 	/**
 	 * Если указанная дата находится в будущем, возвращает текущую дату.
 	 */
 	protected Date _getCorrectDate(Date probablyCorrect) {
 		Date now = new Date();
 		return probablyCorrect.getTime() > now.getTime() ? now : probablyCorrect;
 	}
 	
 	/**
 	 * Нормализует строку.
 	 */
 	protected String _normalizeString(String source) {
 		String result = source.trim();
		result = result.replaceAll("<a[^>]+>|</a>", "");
 		result = result.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&");
 		return result;
 	}
 	
 	/**
 	 * Возвращает шаблон для регулярного выражения.
 	 */
 	protected abstract String _getPattern();
 	
 	/**
 	 * Создает элемент из очередного набора сопоставленных данных.
 	 * Если возвращает false, сопоставление прекращается.
 	 */
 	protected abstract Boolean _createItem(MatchResult match);
 	
 	/**
 	 * Сопоставляет шаблон и строку.
 	 */
 	protected void _findMatches(String text) {
 		Log.debug("parsing text of length " + text.length());
 		Pattern pattern = Pattern.compile(_getPattern(), Pattern.DOTALL);
 		Matcher matcher = pattern.matcher(text);
 		while (matcher.find()) {
 			if (!_createItem(matcher)) {
 				break;
 			}
 		}
 		Log.debug("text parsed");
 	}
 }
