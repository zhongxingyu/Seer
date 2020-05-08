 /*
  * Created on Feb 19, 2005
  *
  */
 package org.rubypeople.rdt.internal.ui.text;
 
 import junit.framework.TestCase;
 
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.FastPartitioner;
 
 /**
  * @author Chris
  * 
  */
 public class TC_RubyPartitionScanner extends TestCase {
 	
 	private String getContentType(String content, int offset) {
 		IDocument doc = new Document(content);
		FastPartitioner partitioner = new FastPartitioner(new RubyPartitionScanner(), RubyPartitionScanner.LEGAL_CONTENT_TYPES);
 		partitioner.connect(doc);
 		return partitioner.getContentType(offset);
 	}
 	
 	public void testUnclosedInterpolationDoesntInfinitelyLoop() {
 		String source = "%[\"#{\"]";
 	    this.getContentType(source, 0);
 	    assert(true);
 	}	
 
 	public void testPartitioningOfSingleLineComment() {
 		String source = "# This is a comment\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 1));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 18));
 	}
 	
 	public void testRecognizeSpecialCase() {
 		String source = "a,b=?#,'This is not a comment!'\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 5));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 6));
 	}	
 		
 	public void testMultilineComment() {
 		String source = "=begin\nComment\n=end";
 
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, 10));
 		
 		source = "=begin\n"+
 				 "  for multiline comments, the =begin and =end must\n" + 
 				 "  appear in the first column\n" +
 				 "=end";
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, source.length() / 2));
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, source.length() - 2));
 	}
 	
 	public void testMultilineCommentNotOnFirstColumn() {
 		String source = " =begin\nComment\n=end";
 
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 1));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 10));
 	}
 	
 	public void testRecognizeDivision() {
 		String source = "1/3 #This is a comment\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 3));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 	}	
 	
 	public void testRecognizeOddballCharacters() {
 		String source = "?\" #comment\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 		
 		source = "?' #comment\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 		
 		source = "?/ #comment\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 	}
 	
 	public void testPoundCharacterIsntAComment() {
 		String source = "?#";		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(source, 1));
 	}
 	
 	public void testSinglelineCommentJustAfterMultilineComment() {
 		String source = "=begin\nComment\n=end\n# this is a singleline comment\n";
 
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, 0));
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, 10));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, source.length() - 5));
 	}
 	
 	public void testMultipleCommentsInARow() {
 		String code = "# comment 1\n# comment 2\nclass Chris\nend\n";
 		
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 6));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 17));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 26));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 29));
 	}
 	
 	public void testCommentAfterEnd() {
 		String code = "class Chris\nend # comment\n";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 12));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 17));
 	}
 	
 	public void testCommentAfterEndWhileEditing() {
 		String code = "=begin\r\n" +
 "c\r\n" +
 "=end\r\n" +
 "#hmm\r\n" +
 "#comment here why is ths\r\n" +
 "class Chris\r\n" +
 "  def thing\r\n" +
 "  end  #ocmm \r\n" +
 "end";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 76));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 83));
 	}
 
 	public void testCommentAtEndOfLineWithStringAtBeginning() {
 		String code = "hash = {\n" +
 				"  \"string\" => { # comment\n" +
 				"    123\n" +
 				"  }\n" +
 				"}";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 0));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 4));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 6));
 		
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 8));
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 12));
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 18));
 		
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 19));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 22));
 		
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 25));
 	}
 	
 	public void testLinesWithJustSpaceBeforeComment() {
 		String code = "  \n" +
 				"  # comment\n" +
 				"  def method\n" +
 				"    \n" +
 				"  end";
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 5));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 14));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 20));
 	}
 	
 	public void testCommentsWithAlotOfPrecedingSpaces() {
 		String code = "                # We \n" +
 				"                # caller-requested until.\n" +
 				"return self\n";
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 16));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 63));
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 70));		
 	}
 	
 	public void testCodeWithinString() {
 		String code = "string = \"here's some code: #{1} there\"";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // st'r'...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 10)); // "'h'er...	
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 28)); // '#'{1...
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 30));	// '1'} t...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 31)); // '}' th...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 35)); // th'e're..
 	}
 	
 	public void testCodeWithinSingleQuoteString() {
 		String code = "string = 'here s some code: #{1} there'";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // st'r'...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 10)); // "'h'er...	
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 28)); // '#'{1...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 30));	// '1'} t...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 31)); // '}' th...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 35)); // th'e're..
 	}
 	
 	public void testVariableSubstitutionWithinString() {
 		String code = "string = \"here's some code: #$global there\"";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // st'r'...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 10)); // "'h'er...	
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 28)); // '#'$glo...
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 29));	// '$'global
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 36));// ' 'there...
 	}
 	
 	public void testStringWithinCodeWithinString() {
 		String code = "string = \"here's some code: #{var = 'string'} there\"";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // st'r'...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 10)); // "'h'er...	
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 28)); // '#'{var
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 30)); // 'v'ar = 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 36)); // '''string
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 46)); // 't'here
 	}
 	
 	public void testStringWithEndBraceWithinCodeWithinString() {
 		String code = "string = \"here's some code: #{var = '}'; 1} there\"";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // st'r'...
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 10)); // "'h'er...	
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 28)); // '#'{var
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 30)); // 'v'ar = 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 37)); // '}'; 
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 39)); // ';' 1} 
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 41)); // ; '1'} 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 42)); // 1'}' t		
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 44)); // 't'here
 	}	
 	
 	public void testRegex() {
 		String code = "regex = /hi there/";
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 2)); // re'g'ex
 		assertEquals(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, this.getContentType(code, 9)); // '/'hi the
 		assertEquals(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, this.getContentType(code, 11)); // /h'i' the
 	}	
 	
 	public void testRegexWithDynamicCode() {
 		String code = "/\\.#{Regexp.escape(extension.to_s)}$/ # comment";
 		assertEquals(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, this.getContentType(code, 3));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 38)); // '#' co
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 40)); // # 'c'ommen
 	}
 	
 	public void testWronglyColoredHeredoc() {
 		// FIXME Something is weird, this test passes, and it works in a normal editor, but is broken in ActiveRecord::Base externally
 		String code = "def destroy\n" +
 "  unless new_record?\n" +
 "    connection.delete <<-end_sql, \"#{self.class.name} Destroy\"\n" +
 "      DELETE FROM #{self.class.table_name}\n" +
 "      WHERE #{connection.quote_column_name(self.class.primary_key)} = #{quoted_id}\n" +
 "    end_sql\n" +
 "  end\n" +
 "  \n" +
 "  freeze\n" +
 "end";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 58)); // <<-'e'nd_sql
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, code.length() - 8)); // fr'e'eze
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, code.length() - 2)); // e'n'd
 	}
 	
 	public void testEscapedCharactersAndSingleQuoteInsideDoubleQuote() {
 		String code = "quoted_value = \"'#{quoted_value[1..-2].gsub(/\\'/, \"\\\\\\\\'\")}'\" if quoted_value.include?(\"\\\\\\'\") # (for ruby mode) \"\n" + 
 						"quoted_value";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 16)); // 
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 19)); // #{'q'uoted 
 		assertEquals(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, this.getContentType(code, 44)); // / 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 51)); // "\ 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 59)); // '" if 
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, 62)); // 'i'f quoted_ 
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(code, 87)); // include?('"'
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(code, 95)); //'#' (for ruby mode)
 		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, this.getContentType(code, code.length() - 3));
 	}
 }
 
