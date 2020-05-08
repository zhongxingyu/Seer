 /*===========================================================================
   Copyright (C) 2012 by the Okapi Framework contributors
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
 
 package org.w3c.its;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.Util;
 
 import org.junit.Test;
 
 public class ITSTest {
 
 	private String root = TestUtil.getParentDir(this.getClass(), "/input.xml") + "/ITS2/input";
 	private FileCompare fc = new FileCompare();
 	
 	@Test
 	public void testTranslateXML () {
 		String base = root+"/translate/xml";
 		removeOutput(base);
 		process(base+"/translate1xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate2xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate3xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate4xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate5xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate6xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate7xml.xml", Main.DC_TRANSLATE);
 		process(base+"/translate8xml.xml", Main.DC_TRANSLATE);
 	}
 
 	@Test
 	public void testTranslateHTML () {
 		String base = root+"/translate/html";
 		removeOutput(base);
 		process(base+"/translate1html.html", Main.DC_TRANSLATE);
		process(base+"/translate2html.html", Main.DC_TRANSLATE);
 		process(base+"/translate3html.html", Main.DC_TRANSLATE);
		process(base+"/translate4html.html", Main.DC_TRANSLATE);
 		process(base+"/translate5html.html", Main.DC_TRANSLATE);
 	}
 
 //	@Test
 //	public void testDomainXML () {
 //		String base = root+"/domain/xml";
 //		removeOutput(base);
 //		process(base+"/Domain1Xml.xml", Main.DC_DOMAIN);
 //		process(base+"/Domain2Xml.xml", Main.DC_DOMAIN);
 //		process(base+"/Domain4Xml.xml", Main.DC_DOMAIN);
 //		process(base+"/Domain5Xml.xml", Main.DC_DOMAIN);
 //	}
 
 	@Test
 	public void testIdValueXML () {
 		String base = root+"/idvalue/xml";
 		removeOutput(base);
 		process(base+"/idvalue1xml.xml", Main.DC_IDVALUE);
 		process(base+"/idvalue2xml.xml", Main.DC_IDVALUE);
 		process(base+"/idvalue3xml.xml", Main.DC_IDVALUE);
 	}
 
 	@Test
 	public void testLocaleFilterXML () {
 		String base = root+"/localeFilter/xml";
 		removeOutput(base);
 		process(base+"/Locale1Xml.xml", Main.DC_LOCALEFILTER);
 		process(base+"/Locale2Xml.xml", Main.DC_LOCALEFILTER);
 		process(base+"/Locale3Xml.xml", Main.DC_LOCALEFILTER);
 		process(base+"/Locale4Xml.xml", Main.DC_LOCALEFILTER);
 		process(base+"/Locale5Xml.xml", Main.DC_LOCALEFILTER);
 	}
 
 	@Test
 	public void testLocalizationNoteXML () {
 		String base = root+"/localizationNote/xml";
 		removeOutput(base);
 		process(base+"/EX-locNote-element-1.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/EX-locNotePointer-attribute-1.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/EX-locNoteRef-attribute-1.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/EX-locNoteRefPointer-attribute-1.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/LocNote1.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/LocNote2.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/LocNote3.xml", Main.DC_LOCALIZATIONNOTE);
 		process(base+"/LocNote4.xml", Main.DC_LOCALIZATIONNOTE);
 	}
 
 	@Test
 	public void testExternalResourceXML () {
 		String base = root+"/externalResource/xml";
 		removeOutput(base);
 		process(base+"/ExternalResource1Xml.xml", Main.DC_EXTERNALRESOURCE);
 		process(base+"/ExternalResource2Xml.xml", Main.DC_EXTERNALRESOURCE);
 		process(base+"/ExternalResource3Xml.xml", Main.DC_EXTERNALRESOURCE);
 	}
 	
 	@Test
 	public void testExternalResourceHTML () {
 		String base = root+"/externalResource/html";
 		removeOutput(base);
 		process(base+"/ExternalResource1Html.html", Main.DC_EXTERNALRESOURCE);
 	}
 	
 //	@Test
 //	public void testLocQualityIssueXML () {
 //		String base = root+"/locQualityIssue/xml";
 //		removeOutput(base);
 //		process(base+"/EX-locQualityIssue-global-1.xml", Main.DC_LOCQUALITYISSUE);
 //		process(base+"/EX-locQualityIssue-global-2.xml", Main.DC_LOCQUALITYISSUE);
 //		process(base+"/EX-locQualityIssue-local-1.xml", Main.DC_LOCQUALITYISSUE);
 //		process(base+"/EX-locQualityIssue-local-2.xml", Main.DC_LOCQUALITYISSUE);
 //		process(base+"/EX-locQualityIssue-local-3.xml", Main.DC_LOCQUALITYISSUE);
 //	}
 
 	private void removeOutput (String baseDir) {
 		String outDir = baseDir.replace("/input/", "/output/");
 		Util.deleteDirectory(outDir, true);
 	}
 
 	private void process (String baseName,
 		String dataCategory)
 	{
 		String input = baseName;
 		String output = input.replace("/input/", "/output/");
 		int n = output.lastIndexOf('.');
 		if ( n > -1 ) output = output.substring(0, n);
 		output += "output";
 		output += ".txt"; //Util.getExtension(input);
 		
 		Main.main(new String[]{input, output, "-dc", dataCategory});
 		assertTrue(new File(output).exists());
 		
 		String gold = output.replace("/output/", "/expected/");
 		assertTrue(fc.compareFilesPerLines(output, gold, "UTF-8"));
 	}
 	
 }
