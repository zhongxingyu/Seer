 package org.eclipse.dltk.core.search;
 
 import org.eclipse.dltk.core.ISearchPatternProcessor;
 
 public abstract class SearchPatternProcessor implements ISearchPatternProcessor {
 
 	protected static final String TYPE_SEPARATOR_STR = String
 			.valueOf(TYPE_SEPARATOR);
 
 	private static ISearchPatternProcessor instance = null;
 
 	public static ISearchPatternProcessor getDefault() {
 		if (instance == null) {
 			instance = new SearchPatternProcessor() {
 			};
 		}
 		return instance;
 	}
 
 	protected SearchPatternProcessor() {
 		//
 	}
 
 	protected static class TypePatten implements ITypePattern {
 		private final String qualification;
 		private final String simpleName;
 
 		public TypePatten(String qualification, String simpleName) {
 			this.qualification = qualification;
 			this.simpleName = simpleName;
 		}
 
 		public char[] qualification() {
 			return qualification != null ? qualification.toCharArray() : null;
 		}
 
		public String getQualification() {
 			return qualification;
 		}
 
 		public char[] simpleName() {
 			return simpleName != null ? simpleName.toCharArray() : null;
 		}
 
 		public String getSimpleName() {
 			return simpleName;
 		}
 
 		@Override
 		public String toString() {
 			return getClass().getSimpleName() + '(' + qualification + ','
 					+ simpleName + ')';
 		}
 	}
 
 	public ITypePattern parseType(String patternString) {
 		return new TypePatten(null, patternString);
 	}
 
 	public String getDelimiterReplacementString() {
 		return ".";
 	}
 
 	public char[] extractDeclaringTypeQualification(String patternString) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public char[] extractDeclaringTypeSimpleName(String patternString) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public char[] extractSelector(String patternString) {
 		// TODO Auto-generated method stub
 		return patternString.toCharArray();
 	}
 
 	public final char[] extractTypeQualification(String patternString) {
 		return parseType(patternString).qualification();
 	}
 
 	@Deprecated
 	public final String extractTypeChars(String patternString) {
 		return parseType(patternString).getSimpleName();
 	}
 
 }
