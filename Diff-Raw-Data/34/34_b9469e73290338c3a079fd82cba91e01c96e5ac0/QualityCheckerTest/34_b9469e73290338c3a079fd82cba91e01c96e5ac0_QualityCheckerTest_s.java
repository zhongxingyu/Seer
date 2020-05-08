 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.verification;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class QualityCheckerTest {
 	
 	private QualityCheckSession session;
 	private LocaleId locEN = LocaleId.ENGLISH;
 	private LocaleId locFR = LocaleId.FRENCH;
 	private String root;
 
 	public QualityCheckerTest () throws URISyntaxException {
 		URL url = QualityCheckerTest.class.getResource("/test01.tsv");
 		root = Util.getDirectoryName(url.toURI().getPath()) + File.separator;
 	}
 	
 	@Before
 	public void setUp() {
 		session = new QualityCheckSession();
 		session.startProcess(locEN, locFR);
 	}
 
 	@Test
 	public void testMISSING_TARGETTU () {
 		// Create source with non-empty content
 		// but no target
 		ITextUnit tu = new TextUnit("id", "source");
 
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.MISSING_TARGETTU, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testEMPTY_TARGETSEG () {
 		// Create TU with source of non-empty segment
 		// and target of empty segment
 		ITextUnit tu = new TextUnit("id", "source");
 		tu.setTarget(locFR, new TextContainer());
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testEMPTY_SOURCESEG () {
 		// Create TU with source of non-empty segment
 		// and target of empty segment
 		ITextUnit tu = new TextUnit("id", "");
 		tu.setTarget(locFR, new TextContainer("target"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.EMPTY_SOURCESEG, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testMISSING_TARGETSEG () {
 		// Create TU with source of two segments
 		// and target of one segment
 		TextContainer tc = new TextContainer("srctext1");
 		tc.getSegments().append(new Segment("s2", new TextFragment("srctext2")));
 		ITextUnit tu = new TextUnit("id");
 		tu.setSource(tc);
 		tu.setTarget(locFR, new TextContainer("trgext1"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.MISSING_TARGETSEG, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testEMPTY_TARGETSEG2 () {
 		// Create TU with source of two segments
 		// and target of two segments but one empty
 		TextContainer tc = new TextContainer("srctext1");
 		tc.getSegments().append(new Segment("s2", new TextFragment("srctext2")));
 		ITextUnit tu = new TextUnit("id");
 		tu.setSource(tc);
 		tc = new TextContainer("trgtext1");
 		tc.getSegments().append(new Segment("s2", new TextFragment()));
 		tu.setTarget(locFR, tc);
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testMISSINGORDIFF_LEADINGWS () {
 		ITextUnit tu = new TextUnit("id", "  srctext");
 		tu.setTarget(locFR, new TextContainer("trgext"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.MISSINGORDIFF_LEADINGWS, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testMISSINGORDIFF_TRAILINGWS () {
 		ITextUnit tu = new TextUnit("id", " srctext ");
 		tu.setTarget(locFR, new TextContainer(" trgext"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.MISSINGORDIFF_TRAILINGWS, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testEXTRAORDIFF_LEADINGWS () {
 		ITextUnit tu = new TextUnit("id", "  srctext");
 		tu.setTarget(locFR, new TextContainer("   trgext"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.EXTRAORDIFF_LEADINGWS, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testEXTRAORDIFF_TRAILINGWS () {
 		ITextUnit tu = new TextUnit("id", "srctext  ");
 		tu.setTarget(locFR, new TextContainer("trgtext   "));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.EXTRAORDIFF_TRAILINGWS, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testTARGET_SAME_AS_SOURCE () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).issueType);
 	}
 	
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_withoutWords () {
 		ITextUnit tu = new TextUnit("id", ":?%$#@#_~`()[]{}=+-");
 		tu.setTarget(locFR, new TextContainer(":?%$#@#_~`()[]{}=+-"));
 
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(0, issues.size());
 	}
 	
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_WithSameCodes () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).issueType);
 	}
 	
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_WithDiffCodes () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<etc/>");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		// We have code difference warnings but no target==source warning
 		assertEquals(2, issues.size());
 		assertEquals(IssueType.MISSING_CODE, issues.get(0).issueType);
 		assertEquals(IssueType.EXTRA_CODE, issues.get(1).issueType);
 	}
 	
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_WithDiffCodesTurnedOff () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<etc/>");
 		
 		session.getParameters().setTargetSameAsSourceWithCodes(false);
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		// We have code difference and target==source warnings
 		assertEquals(3, issues.size());
 		assertEquals(IssueType.MISSING_CODE, issues.get(0).issueType);
 		assertEquals(IssueType.EXTRA_CODE, issues.get(1).issueType);
 		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(2).issueType);
 	}
 	
 	@Test
 	public void testCODE_DIFFERENCE () {
 		ITextUnit tu = new TextUnit("id", "src ");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("trg "));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE />");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(2, issues.size());
 		assertEquals(IssueType.MISSING_CODE, issues.get(0).issueType);
 		assertEquals(IssueType.EXTRA_CODE, issues.get(1).issueType);
 	}
 
 	@Test
 	public void testCODE_OCSEQUENCE () {
 		ITextUnit tu = new TextUnit("id", "src ");
 		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
 		tu.getSource().getSegments().get(0).text.append("text");
 		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
 		tu.setTarget(locFR, new TextContainer("trg "));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
 		tu.getTarget(locFR).getSegments().get(0).text.append("text");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.SUSPECT_CODE, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testCODE_OCSequenceNoError () {
 		ITextUnit tu = new TextUnit("id", "src ");
 		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "i", "<i>");
 		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "i", "</i>");
 		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
 		tu.getSource().getSegments().get(0).text.append("text");
 		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "br", "<br/>");
 		// target with moved codes (no parent changes)
 		tu.setTarget(locFR, new TextContainer("trg "));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "br", "<br/>");
 		tu.getTarget(locFR).getSegments().get(0).text.append("text");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "i", "<i>");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "i", "</i>");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(0, issues.size());
 	}
 
 	@Test
 	public void testCODE_DIFFERENCE_OrderDiffIsOK () {
 		ITextUnit tu = new TextUnit("id", "src ");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");
 		tu.getSource().getSegments().get(0).text.append(" and ");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
 		tu.setTarget(locFR, new TextContainer("trg "));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
 		tu.getTarget(locFR).getSegments().get(0).text.append(" et ");
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(0, issues.size());
 	}
 
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_WithDifferentCodes () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE/>");
 		
 		session.getParameters().setCodeDifference(false);
 		session.startProcess(locEN, locFR);
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		// Codes are different, since the option is code-sensitive: no issue (target not the same as source)
 		assertEquals(0, issues.size());
 	}
 
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_WithDifferentCodes_CodeInsensitive () {
 		ITextUnit tu = new TextUnit("id", "src text");
 		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
 		tu.setTarget(locFR, new TextContainer("src text"));
 		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE/>");
 		
 		session.getParameters().setCodeDifference(false);
 		session.getParameters().setTargetSameAsSourceWithCodes(false);
 		session.startProcess(locEN, locFR);
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		// Codes are different, since the option is NOT code-sensitive: issue raised (target = source)
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).issueType);
 	}
 	
 	@Test
 	public void testTARGET_SAME_AS_SOURCE_NoIssue () {
 		ITextUnit tu = new TextUnit("id", "  \t\n ");
 		tu.setTarget(locFR, new TextContainer("  \t\n "));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(0, issues.size());
 	}
 
 	@Test
 	public void testMISSING_PATTERN () {
 		ITextUnit tu = new TextUnit("id", "src text !? %s");
 		tu.setTarget(locFR, new TextContainer("trg text"));
 		ArrayList<PatternItem> list = new ArrayList<PatternItem>();
 		list.add(new PatternItem("[!\\?]", PatternItem.SAME, true, Issue.SEVERITY_LOW));
 		list.add(new PatternItem("%s", PatternItem.SAME, true, Issue.SEVERITY_HIGH));
 
 		session.getParameters().setPatterns(list);
 		session.startProcess(locEN, locFR); // Make sure we re-initialize
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(3, issues.size());
 		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).issueType);
 		assertEquals(9, issues.get(0).srcStart); 
 		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(1).issueType);
 		assertEquals(10, issues.get(1).srcStart); 
 		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(2).issueType);
 		assertEquals(12, issues.get(2).srcStart); 
 	}
 
 	@Test
 	public void testMISSING_PATTERN_ForURL () {
 		ITextUnit tu = new TextUnit("id", "test: http://thisisatest.com.");
 		tu.setTarget(locFR, new TextContainer("test: http://thisBADtest.com"));
 		session.startProcess(locEN, locFR); // Make sure we re-initialize
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).issueType);
 		assertEquals(6, issues.get(0).srcStart); 
 		assertEquals(28, issues.get(0).srcEnd); 
 	}
 
 	@Test
 	public void testNoIssues () {
 		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
 		tu.setTarget(locFR, new TextContainer("  Texte {avec} (123). "));
 		
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(0, issues.size());
 	}
 
 	@Test
 	public void testMaxLength () {
 		session.getParameters().setMaxCharLengthBreak(9);
 		session.getParameters().setMaxCharLengthAbove(149);
 		session.getParameters().setMaxCharLengthBelow(200);
 
 		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
 		tu.setTarget(locFR, new TextContainer("123456789012345")); // 15 chars
 		session.startProcess(locEN, locFR);
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).issueType);
 
 		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
 		tu.setTarget(locFR, new TextContainer("123456789012345678")); // 18 chars (==200% of src)
 		session.getIssues().clear();
 		session.startProcess(locEN, locFR);
 		session.processTextUnit(tu);
 		issues = session.getIssues();
 		assertEquals(0, issues.size());
 		
 		tu.setTarget(locFR, new TextContainer("1234567890123456789")); // 19 chars (>200% of src)
 		session.processTextUnit(tu);
 		issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testMinLength () {
 		session.getParameters().setMinCharLengthBreak(9);
 		session.getParameters().setMinCharLengthAbove(100);
 		session.getParameters().setMinCharLengthBelow(50);
 
 		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
 		tu.setTarget(locFR, new TextContainer("123456789")); // 10 chars (<100% of src)
 		session.startProcess(locEN, locFR);
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).issueType);
 
 		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
 		tu.setTarget(locFR, new TextContainer("12345")); // 5 chars (==50% of src)
 		session.getIssues().clear();
 		session.startProcess(locEN, locFR);
 		session.processTextUnit(tu);
 		issues = session.getIssues();
 		assertEquals(0, issues.size());
 		
 		tu.setTarget(locFR, new TextContainer("123")); // 4 chars (<50% of src)
 		session.processTextUnit(tu);
 		issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).issueType);
 	}
 
 	@Test
 	public void testTERMINOLOGY () {
 		ITextUnit tu = new TextUnit("id", "summer and WINTER");
 		tu.setTarget(locFR, new TextContainer("\u00e9T\u00e9 et printemps"));
 		
 		session.getParameters().setCheckTerms(true);
 		session.getParameters().setTermsPath(root+"test01.tsv");
 		session.startProcess(locEN, locFR); // Make sure we re-initialize
 
 		session.processTextUnit(tu);
 		List<Issue> issues = session.getIssues();
 		assertEquals(1, issues.size());
 		assertEquals(IssueType.TERMINOLOGY, issues.get(0).issueType);
 	}
 
 }
