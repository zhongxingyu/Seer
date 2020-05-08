 package jenkins.plugins.maveninfo.util;
 
 import hudson.maven.ModuleName;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Pattern to match Maven Module Names.
  * 
  * 
  * 
  * @author emenaceb
  * 
  */
 public class ModuleNamePattern {
 
 	private static final String WILCARD_REGEXP = ".*";
 
 	private static final char WILDCARD_CHAR = '*';
 
 	private Pattern PATTERN_PARTS = Pattern.compile("^([^:]*):([^:]*)$");
 
 	private Matcher groupMatcher;
 
 	private Matcher artifactMatcher;
 
 	public ModuleNamePattern(String pattern) throws InvalidPatternException {
 		Matcher splitter = PATTERN_PARTS.matcher(pattern);
 		if (!splitter.matches()) {
 			throw new InvalidPatternException(
					"Patterns are of the form groupIdFilter:artifactIdFilter");
 		}
 
 		String groupPattern = splitter.group(1);
 		String artifactPattern = splitter.group(2);
 
 		if (StringUtils.isNotBlank(groupPattern) && !"*".equals(groupPattern)) {
 			groupPattern = preparePattern(groupPattern);
 			groupMatcher = Pattern.compile(groupPattern).matcher("");
 		}
 
 		if (StringUtils.isNotBlank(artifactPattern)
 				&& !"*".equals(artifactPattern)) {
 			artifactPattern = preparePattern(artifactPattern);
 			artifactMatcher = Pattern.compile(artifactPattern).matcher("");
 		}
 
 	}
 
 	public static String preparePattern(String pattern) {
 
 		StringBuilder sb = new StringBuilder();
 
 		int pos = pattern.indexOf(WILDCARD_CHAR);
 		int begin = 0;
 
 		while (pos != -1) {
 			if (begin < pos) {
 				String prefix = pattern.substring(begin, pos);
 				sb.append(Pattern.quote(prefix));
 			}
 			sb.append(WILCARD_REGEXP);
 			begin = pos + 1;
 			pos = pattern.indexOf(WILDCARD_CHAR, begin);
 		}
 
 		if (begin < pattern.length()) {
 			String suffix = pattern.substring(begin, pattern.length());
 			sb.append(Pattern.quote(suffix));
 		}
 		return sb.toString();
 	}
 
 	public boolean matches(ModuleName name) {
 
 		if (groupMatcher != null) {
 			groupMatcher.reset(name.groupId);
 			if (!groupMatcher.matches()) {
 				return false;
 			}
 		}
 
 		if (artifactMatcher != null) {
 			artifactMatcher.reset(name.artifactId);
 			if (!artifactMatcher.matches()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
