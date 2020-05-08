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
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 5));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 6));
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
 		assertEquals(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, this.getContentType(source, source.length() - 1));
 	}
 	
 	public void testMultilineCommentNotOnFirstColumn() {
 		String source = " =begin\nComment\n=end";
 
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 0));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 1));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 2));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 10));
 	}
 
 	public void testHereDocWithSpacesOK() {
 		String source = "puts <<-TEST\nMyName\n\tTEST\nputs 'ab'";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 		
 		source = "puts <<-TEST\nMyName\n  TEST\nputs 'ab'";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 
 		source = "puts <<-'ax%&'\nMyName\n   ax%&";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 	}
 	
 	public void testHereDocOK() {
 		String source = "puts <<TEST\nMyName\nTEST";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 
 		source = "puts <<-TEST\nMyName\nTEST\nputs 'ab'";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 
 		source = "puts <<\"TEST\"\nMyName\nTEST";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 
 		source = "puts <<'TEST'\nMyName\nTEST";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 
 		source = "puts <<-'ax%&'\nMyName\nax%&";
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 5));
 		
 	}
 	
 	public void testNoHereDoc() {
 		
 		// space after <<
 		String source = "puts << 'abc'";
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 5));
 		// normaler String
 		assertEquals(RubyPartitionScanner.RUBY_STRING, this.getContentType(source, 9));
 		
 		// end not on first column
 		source = "puts <<HERE\n HERE" ;
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 5));
 
 		source = "puts <<-"; // whatever that means in ruby
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 5));
 		
 		// recognize keyword end although there is no matching HERE
 		// this assures that there are no tokens eaten
 		source = "puts <<HERE\nend\n" ;
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 14));
 		
 		source = "puts <<'abc'\ntest" ;
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 6));  
 		
 		source = "puts <<'abc'\ntest\nabd" ;
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 6)); 		
 	}		
 	
 	public void testRecognizeDivision() {
 		String source = "1/3 #This is a comment\n";
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 0));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 3));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 	}	
 	
 	public void testRecognizeOddballCharacters() {
 		String source = "?\" #comment\n";
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 0));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 		
 		source = "?' #comment\n";
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 0));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 		
 		source = "?/ #comment\n";
 		
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 0));
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, this.getContentType(source, 2));
 		assertEquals(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, this.getContentType(source, 5));
 	}
 	
 }
