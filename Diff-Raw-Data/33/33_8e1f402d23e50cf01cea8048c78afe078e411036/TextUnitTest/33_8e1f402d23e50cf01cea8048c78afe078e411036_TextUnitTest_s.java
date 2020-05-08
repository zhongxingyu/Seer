 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
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
 ============================================================================*/
 
 package net.sf.okapi.common.resource;
 
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class TextUnitTest {
 
 
     private static final LocaleId locFR = LocaleId.FRENCH;
     private static final LocaleId locES = LocaleId.SPANISH;
     // private static final LocaleId locDE = LocaleId.GERMAN;
     private static final String TU1 = "tu1";
     private TextContainer tc1;
     private ITextUnit tu1;
 
 
     @Before
     public void setUp(){
         tu1 = new TextUnit(TU1);
         tc1 = new TextContainer("fr text");
     }
 
 
 
 
     ////TODO verify that these tests adequately cover the ITextUnit interface
 
 
     @Test
     public void isEmptyTrue(){
         assertTrue("The TextUnit should be empty", tu1.isEmpty());
     }
 
     @Test
     public void isEmptyFalse(){
         tu1.setSource(tc1);
         assertFalse("The TextUnit should not be empty", tu1.isEmpty());
     }
 
 
     @Test
     public void getSetSource () {
         tu1.setSource(tc1);
         assertSame(tu1.getSource(), tc1);
     }
 
 
         @Test
     public void hasVariantSourceFalse() {
         //before adding
         assertFalse("hasVariantSource() should return false before any variant "
                   + "sources have been added",
                     tu1.hasVariantSources());
     }
 
     @Test
     public void hasVariantSourceFalseAfterRemoving() {
         //add then remove variant source
         tu1.getVariantSources().create(locFR, true, IResource.CREATE_EMPTY);
         tu1.getVariantSources().remove(locFR);
         assertFalse("hasVariantSource() should return false after the last "
                   + "variant source has been removed",
                     tu1.hasVariantSources());
     }
 
     @Test
     public void hasVaraintSourceTrue() {
         tu1.getVariantSources().create(locFR, true, IResource.CREATE_EMPTY);
         assertTrue("hasVariantSource() should return true if this text unit has "
                  + "one or more varaint sources",
                    tu1.hasVariantSources());
     }
 
 
     @Test
     public void createTargetReturnsNewEmptyOnNoMatch(){
         assertNotNull("When there is no match a empty should be returned", tu1.createTarget(locFR, false, IResource.COPY_SEGMENTATION));
         assertEquals("", tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void getSetTarget () {
         tu1.setTarget(locFR, tc1);
         assertSame("The target should be TextContainer we just set", tc1, tu1.getTarget(locFR));
     }
 
 
     @Test
     public void removeTarget() {
         tu1.setTarget(locFR, tc1);
         tu1.removeTarget(locFR);
         assertFalse("TextUnit should no longer have a target", tu1.hasTarget(locFR));
     }
 
     @Test
     public void hasTargetNo(){
         assertFalse("No target should exist", tu1.hasTarget(locFR));
     }
 
     @Test
     public void hasTargetYes () {
         tu1.setTarget(locFR, tc1);
         assertTrue("TextUnit should now have a target", tu1.hasTarget(locFR));
     }
 
     @Test
     public void hasTargetCaseSensitive () {
         tu1.setTarget(locFR, tc1);
         // Language is now *not* case sensitive
         assertTrue(tu1.hasTarget(LocaleId.fromString("FR")));
         // Still: "fr" different from "fr-fr"
         assertTrue( ! tu1.hasTarget(LocaleId.fromString("fr-fr")));
     }
 
     @Test
     public void createTargetCase1 () {
     	tu1 = createSegmentedTU();
         tu1.createTarget(locFR, false, IResource.COPY_ALL);
         assertEquals(tu1.getSource().toString(), tu1.getTarget(locFR).toString());
         assertEquals(tu1.getSource().getSegments().count(), tu1.getTarget(locFR).getSegments().count());
     }
 
     @Test
     public void createTargetCase2 () {
     	tu1 = createSegmentedTU();
         tu1.createTarget(locFR, false, IResource.COPY_SEGMENTATION);
         assertEquals(tu1.getSource().getSegments().count(), tu1.getTarget(locFR).getSegments().count());
         assertEquals("[] a []", fmt.printSegmentedContent(tu1.getTarget(locFR), true));
         assertEquals(" a ", tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void createTargetCase3 () {
     	tu1 = createSegmentedTU();
         tu1.createTarget(locFR, false, IResource.COPY_CONTENT);
         assertEquals(1, tu1.getTarget(locFR).getSegments().count());
         assertEquals("Part 1. a Part 2.", tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void createTargetSourceContentAndTargetContentSame(){
         tu1.setSource(tc1);
         tu1.createTarget(locFR, false, IResource.COPY_ALL);
         assertEquals("Target text vs Source Text", tu1.getSource().toString(), tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void createTargetDoesntAlreadyExist () {
         tu1.setSource(tc1);
         TextContainer tc2 = tu1.createTarget(locFR, false, IResource.COPY_ALL);
         assertSame("Target should be the same as returned from createTarget", tc2, tu1.getTarget(locFR));
         assertNotSame("Target should have been cloned", tu1.getTarget(locFR), tu1.getSource());
     }
 
     @Test
     public void createTargetAlreadyExistsDontOverwriteExisting () {
         // Do not override existing target
         tu1.setSource(tc1);
         TextContainer tc2 = new TextContainer("unique fr text");
         tu1.setTarget(locFR, tc2);
         tu1.createTarget(locFR, false, IResource.COPY_ALL);
         assertSame("Target should not have been modified", tc2, tu1.getTarget(locFR));
     }
 
     @Test
     public void createTargetAlreadyExistsOverwriteExisting () {
         tu1.setSource(tc1);
         TextContainer tc2 = new TextContainer("unique fr text");
         tu1.setTarget(locFR, tc2);
         tu1.createTarget(locFR, true, IResource.COPY_ALL);
         assertNotSame("Target should not have been modified", tc2, tu1.getTarget(locFR));
     }
 
     @Test
     public void createTargetEmptyOption () {
         tu1.setSource(tc1);
         tu1.createTarget(locFR, false, IResource.CREATE_EMPTY);
         assertTrue(tu1.hasTarget(locFR));
         assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void createTargetEmptyOptionOverwriteExisting () {
         tu1.setSource(tc1);
         TextContainer tc2 = new TextContainer("unique fr text");
         tu1.setTarget(locFR, tc2);
         tu1.createTarget(locFR, true, IResource.CREATE_EMPTY);
         assertTrue(tu1.hasTarget(locFR));
         assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
     }
 
     @Test
     public void createTargetPropertiesOption () {
         tu1.setSource(tc1);
         tu1.getSource().setProperty(new Property("test", "value"));
         tu1.createTarget(locFR, false, IResource.COPY_PROPERTIES);
         assertTrue(tu1.hasTarget(locFR));
         assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
         assertTrue(tu1.getTarget(locFR).getProperty("test") != null);
     }
 
     @Test
     public void createTargetPropertiesOptionOverwriteExisting () {
         tu1.setSource(tc1);
         tu1.getSource().setProperty(new Property("test", "value"));
         TextContainer tc2 = new TextContainer("unique fr text");
         tu1.setTarget(locFR, tc2);
         tu1.createTarget(locFR, true, IResource.COPY_PROPERTIES);
         assertTrue(tu1.hasTarget(locFR));
         assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
         assertTrue(tu1.getTarget(locFR).getProperty("test") != null);
     }
 
     @Test
     public void createTargetPropertiesOptionNotOverwriteExisting () {
         tu1.setSource(tc1);
         tu1.getSource().setProperty(new Property("test", "value"));
         TextContainer tc2 = new TextContainer("unique fr text");
         tu1.setTarget(locFR, tc2);
         tu1.createTarget(locFR, false, IResource.COPY_PROPERTIES);
         assertTrue(tu1.hasTarget(locFR));
         assertEquals("unique fr text", tu1.getTarget(locFR).toString());
         assertTrue(tu1.getTarget(locFR).getProperty("test") == null);
     }
 
     //TODO test setContent(TextFragment)
     //TODO test setTargetContent(LocaleId, TextFragment)
     //TODO test getAlignedSegments()
 
     @Test
     public void getSegments() {
         ITextUnit tu = createSegmentedTUAndTarget();
         IAlignedSegments as = tu.getAlignedSegments();
         assertNotNull("getSegments() should return a non-null IAlignedSegments instance",
                       as);
 
         
     }
 
     @Test
     public void loopThroughSegments () {
     	tu1 = createSegmentedTUAndTarget();
     	Segment trgSeg;
     	for ( Segment srcSeg : tu1.getSourceSegments() ) {
     		if ( srcSeg.id.equals("0") ) {
     			assertEquals("Part 1.", srcSeg.text.toString());
     			trgSeg = tu1.getTargetSegment(locFR, srcSeg.id, false);
     			assertEquals("Trg 1.", trgSeg.text.toString());
     		}
     		else {
     			assertEquals("Part 2.", srcSeg.text.toString());
     			trgSeg = tu1.getTargetSegment(locFR, srcSeg.id, false);
     			assertEquals("Trg 2.", trgSeg.text.toString());
     		}
     	}
     }
 
     @Test
     public void loopThroughSegmentsWithoutTargets () {
     	ITextUnit tu = createSegmentedTU();
     	tu.createTarget(locES, true, IResource.CREATE_EMPTY); // Not a copy of the source
     	Segment trgSeg;
     	for ( Segment srcSeg : tu.getSourceSegments() ) {
     		if ( srcSeg.id.equals("0") ) {
     			assertEquals("Part 1.", srcSeg.text.toString());
     			trgSeg = tu.getTargetSegment(locFR, srcSeg.id, true); // FR
     			assertEquals("", trgSeg.text.toString());
     		}
     		else {
     			assertEquals("Part 2.", srcSeg.text.toString());
     			trgSeg = tu.getTargetSegment(locES, srcSeg.id, false); // ES
     			assertNull(trgSeg);
     		}
     	}
         assertEquals("[Part 1.] a [Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
         assertTrue(tu.hasTarget(locFR));
         assertEquals("[] a []", fmt.printSegmentedContent(tu.getTarget(locFR), true));
         assertTrue(tu.hasTarget(locES));
         assertEquals("[]", fmt.printSegmentedContent(tu.getTarget(locES), true));
     }
 
     @Test
     public void getSourceSegments () {
     	ITextUnit tu = createSegmentedTU();
     	ISegments segs = tu.getSourceSegments();
     	assertNotNull(segs);
     	assertEquals(2, segs.count());
     }
 
 
     @Test
     public void getExistingTargetSegments () {
     	ITextUnit tu = createSegmentedTUAndTarget();
     	ISegments segs = tu.getTargetSegments(locFR);
     	assertNotNull(segs);
     	assertEquals(2, segs.count());
     	assertEquals("Trg 1.", segs.get(0).toString());
     }
 
     @Test
     public void getNonExistingTargetSegments () {
     	ITextUnit tu = createSegmentedTU();
     	ISegments segs = tu.getTargetSegments(locES);
     	assertNotNull(segs);
     	assertEquals(2, segs.count());
     	assertEquals("", segs.get(0).toString());
     }
 
 
     //TODO test getSegment(String, boolean)
 
 
 
 
     private GenericContent fmt;
 
     public TextUnitTest () {
     	fmt = new GenericContent();
     }
 
 
     @Test
     public void toStringFromSource(){
         tu1.setSource(tc1);
         assertEquals("TextUnit.toString()",  "fr text", tu1.toString());
     }
 
     @Test
     public void getSetId () {
         assertEquals(tu1.getId(), TU1);
         tu1.setId("id2");
         assertEquals(tu1.getId(), "id2");
     }
 
     @Test
     public void getSetMimeType () {
         assertNull(tu1.getMimeType());
         tu1.setMimeType("test");
         assertEquals(tu1.getMimeType(), "test");
     }
 
     @Test
     public void propertiesInitialization() {
         assertEquals("Should be empty", 0, tu1.getPropertyNames().size());
     }
 
     @Test
     public void getPropertyReturnsDoesntExist() {
         assertNull("returns null when no property exists", tu1.getProperty("NAME"));
     }
 
     @Test
     public void getSetProperty() {
         Property p1 = new Property("name", "value", true);
         tu1.setProperty(p1);
         assertSame("should return the same property", p1, tu1.getProperty("name"));
     }
 
     @Test
     public void sourcePropertiesInitialization () {
         assertEquals("Should be empty", 0, tu1.getSourcePropertyNames().size());
     }
 
     @Test
     public void getSourcePropertyDoesntExist() {
         assertNull("returns null when no property exists", tu1.getSourceProperty("NAME"));
     }
 
     @Test
     public void getSetSourcePropertyFound() {
         Property p1 = new Property("name", "value", true);
         tu1.setSourceProperty(p1);
         assertSame("Should be the same object", p1, tu1.getSourceProperty("name"));
     }
 
 	@Test
 	public void targetPropertiesInitialization() {
 		assertEquals(tu1.getTargetPropertyNames(locFR).size(), 0);
     }
 
     @Test
     public void getTargetPropertyNotFound() {
 		tu1.setTarget(locFR, tc1);
         assertNull("Target shoudln't be found", tu1.getTargetProperty(locFR, "NAME"));
     }
 
     @Test
     public void getSetTargetProperty() {
         tu1.setTarget(locFR, tc1);
         Property p1 = new Property("name", "value", true);
         tu1.setTargetProperty(locFR, p1);
         assertSame("Properties should be the same", p1, tu1.getTargetProperty(locFR, "name"));
     }
 

 
     //utility methods
 
     private ITextUnit createSegmentedTU () {
     	ITextUnit tu = new TextUnit("id", "Part 1.");
     	tu.getSource().getSegments().append(new Segment("s2", new TextFragment("Part 2.")), " a ");
     	return tu;
     }
 
     private ITextUnit createSegmentedTUAndTarget () {
     	ITextUnit tu = createSegmentedTU();
     	// Add the target segments
     	ISegments segs = tu.getTargetSegments(locFR);
     	segs.get(0).text.append("Trg 1.");
     	segs.get(1).text.append("Trg 2.");
     	segs = tu.getTargetSegments(locES);
     	segs.get(0).text.append("Objetivo 1.");
     	segs.get(1).text.append("Objetivo 2.");
     	return tu;
     }
 
 }
