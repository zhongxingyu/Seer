 /* ----------------------------------------------------------------------------
  * Copyright (C) 2013      European Space Agency
  *                         European Space Operations Centre
  *                         Darmstadt
  *                         Germany
  * ----------------------------------------------------------------------------
  * System                : CCSDS MO Service Stub Generator
  * ----------------------------------------------------------------------------
  * Licensed under the European Space Agency Public License, Version 2.0
  * You may not use this file except in compliance with the License.
  *
  * Except as expressly set forth in this License, the Software is provided to
  * You on an "as is" basis and without warranties of any kind, including without
  * limitation merchantability, fitness for a particular purpose, absence of
  * defects or errors, accuracy or non-infringement of intellectual property rights.
  * 
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  * ----------------------------------------------------------------------------
  */
 package esa.mo.tools.stubgen;
 
 import static esa.mo.tools.stubgen.GeneratorDocument.splitString;
 import esa.mo.tools.stubgen.specification.CompositeField;
 import esa.mo.tools.stubgen.specification.StdStrings;
 import esa.mo.tools.stubgen.specification.TypeUtils;
 import esa.mo.tools.stubgen.writers.AbstractWriter;
 import esa.mo.tools.stubgen.xsd.*;
 import esa.mo.tools.stubgen.xsd.EnumerationType.Item;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 import javax.xml.bind.JAXBException;
 
 /**
  * Generates a MS Word compliant docx file of the service specification.
  */
 public class GeneratorDocx extends GeneratorDocument
 {
   private static final String STD_COLOUR = null;
   private static final String HEADER_COLOUR = "00CCFF";
   private static final String FIXED_COLOUR = "E0E0E0";
   private static final int[] SERVICE_OVERVIEW_TABLE_WIDTHS = new int[]
   {
     2250, 2801, 1382, 1185, 1382
   };
   private static final int[] SERVICE_COM_TYPES_TABLE_WIDTHS = new int[]
   {
     2250, 1685, 2801, 2370
   };
   private static final int[] SERVICE_EVENT_TABLE_WIDTHS = new int[]
   {
     2250, 1685, 2801, 1185, 1185
   };
   private static final int[] OPERATION_OVERVIEW_TABLE_WIDTHS = new int[]
   {
     2395, 3538, 3067
   };
   private static final int[] OPERATION_ERROR_TABLE_WIDTHS = new int[]
   {
     2250, 2250, 4500
   };
   private static final int[] ERROR_TABLE_WIDTHS = new int[]
   {
     2302, 1430, 5268
   };
   private static final int[] ENUM_TABLE_WIDTHS = new int[]
   {
     2302, 2430, 4268
   };
   private static final int[] LIST_TABLE_WIDTHS = new int[]
   {
     2302, 6698
   };
   private static final int[] COMPOSITE_TABLE_WIDTHS = new int[]
   {
     2302, 1830, 1100, 3768
   };
 
   /**
    * Constructor.
    *
    * @param logger The logger to use.
    */
   public GeneratorDocx(org.apache.maven.plugin.logging.Log logger)
   {
     super(logger, new GeneratorConfiguration("", "", "", "", "", "", "", "", "", "", "", ""));
   }
 
   @Override
   public String getShortName()
   {
     return "DOCX";
   }
 
   @Override
   public String getDescription()
   {
     return "Generates a document of the service specification.";
   }
 
   @Override
   public void compile(String destFolderName, SpecificationType spec) throws IOException, JAXBException
   {
     for (AreaType area : spec.getArea())
     {
       String destinationFolderName = destFolderName + "/" + area.getName();
       DocxNumberingWriter docxNumberingFile = new DocxNumberingWriter(destinationFolderName + "/word", "numbering", "xml");
       DocxWriter docxServiceFile = new DocxWriter(destinationFolderName + "/word", "document", "xml", docxNumberingFile);
       DocxBaseWriter docxDataFile = new DocxBaseWriter();
 
       // create DOCX target area
       StubUtils.createResource(destinationFolderName, "[Content_Types]", "xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/><Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/><Override PartName=\"/word/numbering.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml\"/></Types>");
       StubUtils.createResource(destinationFolderName + "/_rels", "", "rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
       StubUtils.createResource(destinationFolderName + "/word/_rels", "document.xml", "rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering\" Target=\"numbering.xml\"/></Relationships>");
       StubUtils.createResource(destinationFolderName + "/word", "styles", "xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:styles xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii=\"Calibri\" w:eastAsia=\"Times New Roman\" w:hAnsi=\"Calibri\" w:cs=\"Times New Roman\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/><w:lang w:val=\"en-GB\" w:eastAsia=\"en-GB\" w:bidi=\"ar-SA\"/></w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults><w:latentStyles w:defLockedState=\"0\" w:defUIPriority=\"99\" w:defSemiHidden=\"1\" w:defUnhideWhenUsed=\"1\" w:defQFormat=\"0\" w:count=\"267\"><w:lsdException w:name=\"Normal\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 1\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 2\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 3\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 4\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 5\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 6\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 7\" w:locked=\"1\" w:uiPriority=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 8\" w:locked=\"1\" w:uiPriority=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"heading 9\" w:locked=\"1\" w:uiPriority=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"toc 1\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 2\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 3\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 4\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 5\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 6\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 7\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 8\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"toc 9\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"caption\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Title\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Default Paragraph Font\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Subtitle\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Strong\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Emphasis\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Table Grid\" w:locked=\"1\" w:semiHidden=\"0\" w:uiPriority=\"0\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Placeholder Text\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"No Spacing\" w:semiHidden=\"0\" w:uiPriority=\"1\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Light Shading\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Revision\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"List Paragraph\" w:semiHidden=\"0\" w:uiPriority=\"34\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Quote\" w:semiHidden=\"0\" w:uiPriority=\"29\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Intense Quote\" w:semiHidden=\"0\" w:uiPriority=\"30\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Medium List 2 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 1\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 2\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 3\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 4\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 5\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Shading Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"60\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light List Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"61\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Light Grid Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"62\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 1 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"63\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Shading 2 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"64\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 1 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"65\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium List 2 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"66\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 1 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"67\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 2 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"68\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Medium Grid 3 Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"69\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Dark List Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"70\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Shading Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"71\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful List Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"72\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Colorful Grid Accent 6\" w:semiHidden=\"0\" w:uiPriority=\"73\" w:unhideWhenUsed=\"0\"/><w:lsdException w:name=\"Subtle Emphasis\" w:semiHidden=\"0\" w:uiPriority=\"19\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Intense Emphasis\" w:semiHidden=\"0\" w:uiPriority=\"21\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Subtle Reference\" w:semiHidden=\"0\" w:uiPriority=\"31\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Intense Reference\" w:semiHidden=\"0\" w:uiPriority=\"32\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Book Title\" w:semiHidden=\"0\" w:uiPriority=\"33\" w:unhideWhenUsed=\"0\" w:qFormat=\"1\"/><w:lsdException w:name=\"Bibliography\" w:uiPriority=\"37\"/><w:lsdException w:name=\"TOC Heading\" w:uiPriority=\"39\" w:qFormat=\"1\"/></w:latentStyles><w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/><w:qFormat/><w:rsid w:val=\"008E3E32\"/></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading1\"><w:name w:val=\"heading 1\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading1Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:pPr><w:keepNext/><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"0\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:bCs/><w:kern w:val=\"32\"/><w:sz w:val=\"32\"/><w:szCs w:val=\"32\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading2\"><w:name w:val=\"heading 2\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading2Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:pPr><w:keepNext/><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"1\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:bCs/><w:i/><w:iCs/><w:sz w:val=\"28\"/><w:szCs w:val=\"28\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading3\"><w:name w:val=\"heading 3\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading3Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:pPr><w:keepNext/><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"2\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:b/><w:bCs/><w:sz w:val=\"26\"/><w:szCs w:val=\"26\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading4\"><w:name w:val=\"heading 4\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading4Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:pPr><w:keepNext/><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"3\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:bCs/><w:sz w:val=\"28\"/><w:szCs w:val=\"28\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading5\"><w:name w:val=\"heading 5\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading5Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:pPr><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"4\"/></w:pPr><w:rPr><w:b/><w:bCs/><w:i/><w:iCs/><w:sz w:val=\"26\"/><w:szCs w:val=\"26\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading6\"><w:name w:val=\"heading 6\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:link w:val=\"Heading6Char\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:pPr><w:spacing w:before=\"240\" w:after=\"60\"/><w:outlineLvl w:val=\"5\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:bCs/></w:rPr></w:style><w:style w:type=\"character\" w:default=\"1\" w:styleId=\"DefaultParagraphFont\"><w:name w:val=\"Default Paragraph Font\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/></w:style><w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:qFormat/><w:tblPr><w:tblInd w:w=\"0\" w:type=\"dxa\"/><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"108\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr></w:style><w:style w:type=\"numbering\" w:default=\"1\" w:styleId=\"NoList\"><w:name w:val=\"No List\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading1Char\"><w:name w:val=\"Heading 1 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading1\"/><w:uiPriority w:val=\"99\"/><w:locked/><w:rPr><w:rFonts w:ascii=\"Cambria\" w:hAnsi=\"Cambria\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/><w:kern w:val=\"32\"/><w:sz w:val=\"32\"/><w:szCs w:val=\"32\"/></w:rPr></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading2Char\"><w:name w:val=\"Heading 2 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading2\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:locked/><w:rPr><w:rFonts w:ascii=\"Cambria\" w:hAnsi=\"Cambria\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/><w:i/><w:iCs/><w:sz w:val=\"28\"/><w:szCs w:val=\"28\"/></w:rPr></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading3Char\"><w:name w:val=\"Heading 3 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading3\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:locked/><w:rPr><w:rFonts w:ascii=\"Cambria\" w:hAnsi=\"Cambria\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/><w:sz w:val=\"26\"/><w:szCs w:val=\"26\"/></w:rPr></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading4Char\"><w:name w:val=\"Heading 4 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading4\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/><w:sz w:val=\"28\"/><w:szCs w:val=\"28\"/></w:rPr></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading5Char\"><w:name w:val=\"Heading 5 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading5\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/><w:i/><w:iCs/><w:sz w:val=\"26\"/><w:szCs w:val=\"26\"/></w:rPr></w:style><w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Heading6Char\"><w:name w:val=\"Heading 6 Char\"/><w:basedOn w:val=\"DefaultParagraphFont\"/><w:link w:val=\"Heading6\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:locked/><w:rsid w:val=\"008E3E32\"/><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\" w:cs=\"Times New Roman\"/><w:b/><w:bCs/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"Caption\"><w:name w:val=\"caption\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:uiPriority w:val=\"99\"/><w:qFormat/><w:locked/><w:rPr><w:b/><w:bCs/><w:sz w:val=\"20\"/><w:szCs w:val=\"20\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:customStyle=\"1\" w:styleId=\"TableTitle\"><w:name w:val=\"_Table_Title\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:uiPriority w:val=\"99\"/><w:rsid w:val=\"006673F3\"/><w:pPr><w:keepNext/><w:keepLines/><w:suppressAutoHyphens/><w:spacing w:before=\"480\" w:after=\"240\"/><w:jc w:val=\"center\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/><w:lang w:val=\"en-US\" w:eastAsia=\"en-US\"/></w:rPr></w:style><w:style w:type=\"paragraph\" w:styleId=\"ListParagraph\"><w:name w:val=\"List Paragraph\"/><w:basedOn w:val=\"Normal\"/><w:uiPriority w:val=\"34\"/><w:qFormat/><w:rsid w:val=\"0052240D\"/><w:pPr><w:ind w:left=\"720\"/><w:contextualSpacing/></w:pPr></w:style></w:styles>");
 
       if ((!area.getName().equalsIgnoreCase(StdStrings.COM)) || (generateCOM()))
       {
         getLog().info("Processing area: " + area.getName());
         docxServiceFile.addTitle(1, "Specification: " + area.getName());
 
         docxServiceFile.addTitle(2, "General");
         docxServiceFile.addComment(area.getComment());
 
         if ((null != area.getRequirements()) && (0 < area.getRequirements().trim().length()))
         {
           docxServiceFile.addTitle(2, "Requirements");
           docxServiceFile.addNumberedComment(splitString(null, area.getRequirements()));
         }
 
         // create services
         for (ServiceType service : area.getService())
         {
           docxServiceFile.addTitle(2, "Service: " + service.getName());
           docxServiceFile.addTitle(3, "General");
           docxServiceFile.addComment(service.getComment());
           drawServiceTable(docxServiceFile, area, service);
 
           if ((null != service.getRequirements()) && (0 < service.getRequirements().trim().length()))
           {
             docxServiceFile.addTitle(3, "Requirements");
             docxServiceFile.addNumberedComment(splitString(null, service.getRequirements()));
           }
 
           if (!StdStrings.COM.equalsIgnoreCase(service.getName()))
           {
             if (service instanceof ExtendedServiceType)
             {
               drawCOMUsageTables(docxServiceFile, area, ((ExtendedServiceType) service));
             }
           }
           else
           {
             List<String> comments = new ArrayList<String>();
 
             for (CapabilitySetType cSet : service.getCapabilitySet())
             {
               String str = cSet.getComment();
 
               if (null != str)
               {
                 comments.addAll(splitString(null, str));
               }
             }
 
             docxServiceFile.addNumberedComment(comments);
           }
 
           for (CapabilitySetType cSet : service.getCapabilitySet())
           {
             for (OperationType op : cSet.getSendIPOrSubmitIPOrRequestIP())
             {
               docxServiceFile.addTitle(3, "OPERATION: " + op.getName());
               docxServiceFile.addTitle(4, "General");
               docxServiceFile.addComment(op.getComment());
               drawOperationTable(docxServiceFile, area, op);
               addOperationStructureDetails(docxServiceFile, op);
               addOperationErrorDetails(docxServiceFile, area, op);
             }
           }
         }
 
         // process data types
         docxDataFile.addTitle(1, "Data types");
         boolean hasDataTypes = false;
         // if area level types exist
         if ((null != area.getDataTypes()) && !area.getDataTypes().getFundamentalOrAttributeOrComposite().isEmpty())
         {
           hasDataTypes = true;
           docxDataFile.addTitle(2, "Area data types: " + area.getName());
           // create area level data types
           for (Object oType : area.getDataTypes().getFundamentalOrAttributeOrComposite())
           {
             if (oType instanceof FundamentalType)
             {
               createFundamentalClass(docxDataFile, (FundamentalType) oType);
             }
             else if (oType instanceof AttributeType)
             {
               createAttributeClass(docxDataFile, (AttributeType) oType);
             }
             else if (oType instanceof CompositeType)
             {
               createCompositeClass(docxDataFile, area, (CompositeType) oType);
             }
             else if (oType instanceof EnumerationType)
             {
               createEnumerationClass(docxDataFile, (EnumerationType) oType);
             }
             else
             {
               throw new IllegalArgumentException("Unexpected area (" + area.getName() + ") level datatype of " + oType.getClass().getName());
             }
           }
         }
 
         // process service level types
         for (ServiceType service : area.getService())
         {
           // if service level types exist
           if ((null != service.getDataTypes()) && !service.getDataTypes().getCompositeOrEnumeration().isEmpty())
           {
             hasDataTypes = true;
             docxDataFile.addTitle(2, "Service data types: " + service.getName());
             for (Object oType : service.getDataTypes().getCompositeOrEnumeration())
             {
               if (oType instanceof EnumerationType)
               {
                 createEnumerationClass(docxDataFile, (EnumerationType) oType);
               }
               else if (oType instanceof CompositeType)
               {
                 createCompositeClass(docxDataFile, area, (CompositeType) oType);
               }
               else
               {
                 throw new IllegalArgumentException("Unexpected service (" + area.getName() + ":" + service.getName() + ") level datatype of " + oType.getClass().getName());
               }
             }
           }
         }
 
         if (!hasDataTypes)
         {
           docxDataFile.addComment("No data types are defined in this specification.");
         }
 
         // process errors
         docxDataFile.addTitle(1, "Error codes");
 
         List<ErrorDefinitionType> errors = new LinkedList<ErrorDefinitionType>();
 
         // if area level types exist
         if ((null != area.getErrors()) && !area.getErrors().getError().isEmpty())
         {
           errors.addAll(area.getErrors().getError());
         }
 
         // process service level types
         for (ServiceType service : area.getService())
         {
           // if service level types exist
           if ((null != service.getErrors()) && !service.getErrors().getError().isEmpty())
           {
             errors.addAll(service.getErrors().getError());
 
             // check for operation level error definitions
             for (CapabilitySetType cap : service.getCapabilitySet())
             {
               for (OperationType op : cap.getSendIPOrSubmitIPOrRequestIP())
               {
                 if (op instanceof SubmitOperationType)
                 {
                   SubmitOperationType lop = (SubmitOperationType) op;
                   addErrorDefinitions(errors, lop.getErrors());
                 }
                 else if (op instanceof RequestOperationType)
                 {
                   RequestOperationType lop = (RequestOperationType) op;
                   addErrorDefinitions(errors, lop.getErrors());
                 }
                 else if (op instanceof InvokeOperationType)
                 {
                   InvokeOperationType lop = (InvokeOperationType) op;
                   addErrorDefinitions(errors, lop.getErrors());
                 }
                 else if (op instanceof ProgressOperationType)
                 {
                   ProgressOperationType lop = (ProgressOperationType) op;
                   addErrorDefinitions(errors, lop.getErrors());
                 }
                 else if (op instanceof PubSubOperationType)
                 {
                   PubSubOperationType lop = (PubSubOperationType) op;
                   addErrorDefinitions(errors, lop.getErrors());
                 }
               }
             }
           }
         }
 
         if (0 < errors.size())
         {
           docxDataFile.addComment("The following table lists the errors defined in this specification:");
 
           docxDataFile.startTable(ERROR_TABLE_WIDTHS, area.getName() + " Error Codes");
           docxDataFile.startRow();
           docxDataFile.addCell(0, ERROR_TABLE_WIDTHS, "Error", HEADER_COLOUR);
           docxDataFile.addCell(1, ERROR_TABLE_WIDTHS, "Error #", HEADER_COLOUR);
           docxDataFile.addCell(2, ERROR_TABLE_WIDTHS, "Comment", HEADER_COLOUR);
           docxDataFile.endRow();
 
           for (ErrorDefinitionType err : errors)
           {
             docxDataFile.startRow();
             docxDataFile.addCell(0, ERROR_TABLE_WIDTHS, err.getName());
             docxDataFile.addCell(1, ERROR_TABLE_WIDTHS, String.valueOf(err.getNumber()));
             docxDataFile.addCell(2, ERROR_TABLE_WIDTHS, err.getComment(), false);
             docxDataFile.endRow();
           }
 
           docxDataFile.endTable();
         }
         else
         {
           docxDataFile.addComment("No errors are defined in this specification.");
         }
       }
 
       docxServiceFile.appendBuffer(docxDataFile.getBuffer());
       docxServiceFile.flush();
       docxNumberingFile.flush();
 
       String[] filenames = new String[]
       {
         "[Content_Types].xml", "_rels/.rels", "word/_rels/document.xml.rels", "word/document.xml", "word/styles.xml", "word/numbering.xml"
       };
       StubUtils.createZipfile(destinationFolderName, filenames, "/ServiceSpec" + area.getName() + ".docx");
     }
   }
 
   private void drawServiceTable(DocxBaseWriter docxFile, AreaType area, ServiceType service) throws IOException
   {
     docxFile.startTable(SERVICE_OVERVIEW_TABLE_WIDTHS, service.getName() + " Service Operations");
 
     docxFile.startRow();
     docxFile.addCell(0, SERVICE_OVERVIEW_TABLE_WIDTHS, "Area Identifier", HEADER_COLOUR);
     docxFile.addCell(1, SERVICE_OVERVIEW_TABLE_WIDTHS, "Service Identifier", HEADER_COLOUR);
     docxFile.addCell(2, SERVICE_OVERVIEW_TABLE_WIDTHS, "Area Number", HEADER_COLOUR);
     docxFile.addCell(3, SERVICE_OVERVIEW_TABLE_WIDTHS, "Service Number", HEADER_COLOUR);
     docxFile.addCell(4, SERVICE_OVERVIEW_TABLE_WIDTHS, "Area Version", HEADER_COLOUR);
     docxFile.endRow();
     docxFile.startRow();
     docxFile.addCell(0, SERVICE_OVERVIEW_TABLE_WIDTHS, area.getName());
     docxFile.addCell(1, SERVICE_OVERVIEW_TABLE_WIDTHS, service.getName());
     docxFile.addCell(2, SERVICE_OVERVIEW_TABLE_WIDTHS, String.valueOf(area.getNumber()));
     docxFile.addCell(3, SERVICE_OVERVIEW_TABLE_WIDTHS, String.valueOf(service.getNumber()));
     docxFile.addCell(4, SERVICE_OVERVIEW_TABLE_WIDTHS, String.valueOf(area.getVersion()));
     docxFile.endRow();
 
     docxFile.startRow();
     docxFile.addCell(0, SERVICE_OVERVIEW_TABLE_WIDTHS, "Interaction Pattern", HEADER_COLOUR);
     docxFile.addCell(1, SERVICE_OVERVIEW_TABLE_WIDTHS, "Operation Identifier", HEADER_COLOUR);
     docxFile.addCell(2, SERVICE_OVERVIEW_TABLE_WIDTHS, "Operation Number", HEADER_COLOUR);
     docxFile.addCell(3, SERVICE_OVERVIEW_TABLE_WIDTHS, "Support in Replay", HEADER_COLOUR);
     docxFile.addCell(4, SERVICE_OVERVIEW_TABLE_WIDTHS, "Capability Set", HEADER_COLOUR);
     docxFile.endRow();
 
     for (CapabilitySetType cSet : service.getCapabilitySet())
     {
       drawServiceCapabilitySet(docxFile, cSet, STD_COLOUR);
     }
 
     docxFile.endTable();
   }
 
   private static void drawServiceCapabilitySet(DocxBaseWriter docxFile, CapabilitySetType cSet, String colour) throws IOException
   {
     if (null != cSet)
     {
       boolean firstRow = true;
       for (OperationType op : cSet.getSendIPOrSubmitIPOrRequestIP())
       {
         docxFile.startRow();
         docxFile.addCell(0, SERVICE_OVERVIEW_TABLE_WIDTHS, operationType(op), colour);
         docxFile.addCell(1, SERVICE_OVERVIEW_TABLE_WIDTHS, op.getName(), colour);
         docxFile.addCell(2, SERVICE_OVERVIEW_TABLE_WIDTHS, String.valueOf(op.getNumber()), colour);
         docxFile.addCell(3, SERVICE_OVERVIEW_TABLE_WIDTHS, yesNoType(op.isSupportInReplay()), colour);
         docxFile.addCell(4, SERVICE_OVERVIEW_TABLE_WIDTHS, String.valueOf(cSet.getNumber()), colour, true, firstRow);
         docxFile.endRow();
 
         firstRow = false;
       }
     }
   }
 
   private static void drawCOMUsageTables(DocxBaseWriter docxFile, AreaType area, ExtendedServiceType service) throws IOException
   {
     SupportedFeatures features = service.getFeatures();
 
     if (null != features)
     {
       if (null != features.getObjects())
       {
         docxFile.addTitle(3, "COM usage");
         docxFile.addNumberedComment(splitString(null, features.getObjects().getComment()));
 
         if (!features.getObjects().getObject().isEmpty())
         {
           docxFile.startTable(SERVICE_COM_TYPES_TABLE_WIDTHS, service.getName() + " Service Object Types");
 
           docxFile.startRow();
           docxFile.addCell(0, SERVICE_COM_TYPES_TABLE_WIDTHS, "Object Name", HEADER_COLOUR);
           docxFile.addCell(1, SERVICE_COM_TYPES_TABLE_WIDTHS, "Object Number", HEADER_COLOUR);
           docxFile.addCell(2, SERVICE_COM_TYPES_TABLE_WIDTHS, "Object Body Type", HEADER_COLOUR);
           docxFile.addCell(3, SERVICE_COM_TYPES_TABLE_WIDTHS, "Related points to", HEADER_COLOUR);
           docxFile.endRow();
 
           List<String> cmts = new LinkedList<String>();
 
           for (ModelObjectType obj : features.getObjects().getObject())
           {
             docxFile.startRow();
             docxFile.addCell(0, SERVICE_COM_TYPES_TABLE_WIDTHS, obj.getName());
             docxFile.addCell(1, SERVICE_COM_TYPES_TABLE_WIDTHS, String.valueOf(obj.getNumber()));
            if (null != obj.getObjectType() && (null != obj.getObjectType().getAny()))
            {
              docxFile.addCell(2, SERVICE_COM_TYPES_TABLE_WIDTHS, createFQTypeName(area, TypeUtils.getTypeListViaXSDAny(obj.getObjectType().getAny(), null)), null);
            }
            else
            {
              docxFile.addCell(2, SERVICE_COM_TYPES_TABLE_WIDTHS, "No body");
            }
             if (null != obj.getRelatedObject() && (null != obj.getRelatedObject().getObjectType()))
             {
               docxFile.addCell(3, SERVICE_COM_TYPES_TABLE_WIDTHS, createFQTypeName(area, obj.getRelatedObject().getObjectType()), null);
             }
             else
             {
               docxFile.addCell(3, SERVICE_COM_TYPES_TABLE_WIDTHS, "Not used");
             }
             docxFile.endRow();
           }
 
           docxFile.endTable();
 
           docxFile.addNumberedComment(cmts);
         }
       }
 
       if (null != features.getEvents())
       {
         DocxBaseWriter evntTable = new DocxBaseWriter(docxFile.numberWriter);
         evntTable.addTitle(3, "COM Event Service usage");
         evntTable.addNumberedComment(splitString(null, features.getEvents().getComment()));
 
         evntTable.startTable(SERVICE_EVENT_TABLE_WIDTHS, service.getName() + " Service Events");
 
         evntTable.startRow();
         evntTable.addCell(0, SERVICE_EVENT_TABLE_WIDTHS, "Event Name", HEADER_COLOUR);
         evntTable.addCell(1, SERVICE_EVENT_TABLE_WIDTHS, "Object Number", HEADER_COLOUR);
         evntTable.addCell(2, SERVICE_EVENT_TABLE_WIDTHS, "Object Body Type", HEADER_COLOUR);
         evntTable.addCell(3, SERVICE_EVENT_TABLE_WIDTHS, "Related points to", HEADER_COLOUR);
         evntTable.addCell(4, SERVICE_EVENT_TABLE_WIDTHS, "Source points to", HEADER_COLOUR);
         evntTable.endRow();
 
         for (ModelObjectType evnt : features.getEvents().getEvent())
         {
           evntTable.startRow();
           evntTable.addCell(0, SERVICE_EVENT_TABLE_WIDTHS, evnt.getName(), STD_COLOUR);
           evntTable.addCell(1, SERVICE_EVENT_TABLE_WIDTHS, String.valueOf(evnt.getNumber()), STD_COLOUR);
           if (null != evnt.getObjectType())
           {
             evntTable.addCell(2, SERVICE_EVENT_TABLE_WIDTHS, createFQTypeName(area, TypeUtils.getTypeListViaXSDAny(evnt.getObjectType().getAny(), null)), null);
           }
           else
           {
             evntTable.addCell(2, SERVICE_EVENT_TABLE_WIDTHS, "Not used", STD_COLOUR);
           }
           if (null != evnt.getRelatedObject())
           {
             if (null != evnt.getRelatedObject().getObjectType())
             {
               evntTable.addCell(3, SERVICE_EVENT_TABLE_WIDTHS, createFQTypeName(area, evnt.getRelatedObject().getObjectType()), null);
             }
             else
             {
               if (null != evnt.getRelatedObject().getComment())
               {
                 evntTable.addCell(3, SERVICE_EVENT_TABLE_WIDTHS, evnt.getRelatedObject().getComment(), null);
               }
               else
               {
                 evntTable.addCell(3, SERVICE_EVENT_TABLE_WIDTHS, "Not specified");
               }
             }
           }
           else
           {
             evntTable.addCell(3, SERVICE_EVENT_TABLE_WIDTHS, "Not specified", STD_COLOUR);
           }
           if (null != evnt.getSourceObject())
           {
             if (null != evnt.getSourceObject().getObjectType())
             {
               evntTable.addCell(4, SERVICE_EVENT_TABLE_WIDTHS, createFQTypeName(area, evnt.getSourceObject().getObjectType()), null);
             }
             else
             {
               if (null != evnt.getSourceObject().getComment())
               {
                 evntTable.addCell(4, SERVICE_EVENT_TABLE_WIDTHS, evnt.getSourceObject().getComment(), null);
               }
               else
               {
                 evntTable.addCell(4, SERVICE_EVENT_TABLE_WIDTHS, "Not specified");
               }
             }
           }
           else
           {
             evntTable.addCell(4, SERVICE_EVENT_TABLE_WIDTHS, "Not specified", STD_COLOUR, 2);
           }
 
           evntTable.endRow();
         }
 
         evntTable.endTable();
 
         docxFile.appendBuffer(evntTable.getBuffer());
       }
 
       if (null != features.getArchiveUsage())
       {
         DocxBaseWriter archiveUsage = new DocxBaseWriter(docxFile.numberWriter);
         archiveUsage.addTitle(3, "COM Archive Service usage");
         archiveUsage.addNumberedComment(splitString(null, features.getArchiveUsage().getComment()));
 
         docxFile.appendBuffer(archiveUsage.getBuffer());
       }
 
       if (null != features.getActivityUsage())
       {
         DocxBaseWriter activityUsage = new DocxBaseWriter(docxFile.numberWriter);
         activityUsage.addTitle(3, "COM Activity Service usage");
         activityUsage.addNumberedComment(splitString(null, features.getActivityUsage().getComment()));
 
         docxFile.appendBuffer(activityUsage.getBuffer());
       }
     }
   }
 
   private static void drawOperationTable(DocxBaseWriter docxFile, AreaType area, OperationType op) throws IOException
   {
     docxFile.startTable(OPERATION_OVERVIEW_TABLE_WIDTHS);
 
     docxFile.startRow();
     docxFile.addCell(0, OPERATION_OVERVIEW_TABLE_WIDTHS, "Operation Identifier", HEADER_COLOUR);
     docxFile.addCell(1, OPERATION_OVERVIEW_TABLE_WIDTHS, op.getName(), STD_COLOUR, 2);
     docxFile.endRow();
 
     if (op instanceof SendOperationType)
     {
       SendOperationType lop = (SendOperationType) op;
       drawOperationPattern(docxFile, "SEND");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, true, "SEND", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getSend().getAny(), null));
     }
     else if (op instanceof SubmitOperationType)
     {
       SubmitOperationType lop = (SubmitOperationType) op;
       drawOperationPattern(docxFile, "SUBMIT");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, true, "SUBMIT", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getSubmit().getAny(), null));
     }
     else if (op instanceof RequestOperationType)
     {
       RequestOperationType lop = (RequestOperationType) op;
       drawOperationPattern(docxFile, "REQUEST");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, true, "REQUEST", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getRequest().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "RESPONSE", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getResponse().getAny(), null));
     }
     else if (op instanceof InvokeOperationType)
     {
       InvokeOperationType lop = (InvokeOperationType) op;
       drawOperationPattern(docxFile, "INVOKE");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, true, "INVOKE", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getInvoke().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "ACK", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getAcknowledgement().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "RESPONSE", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getResponse().getAny(), null));
     }
     else if (op instanceof ProgressOperationType)
     {
       ProgressOperationType lop = (ProgressOperationType) op;
       drawOperationPattern(docxFile, "PROGRESS");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, true, "PROGRESS", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getProgress().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "ACK", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getAcknowledgement().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "UPDATE", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getUpdate().getAny(), null));
       drawOperationMessageDetails(docxFile, area, false, "RESPONSE", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getResponse().getAny(), null));
     }
     else if (op instanceof PubSubOperationType)
     {
       PubSubOperationType lop = (PubSubOperationType) op;
       drawOperationPattern(docxFile, "PUBLISH-SUBSCRIBE");
       drawOperationMessageHeader(docxFile);
       drawOperationMessageDetails(docxFile, area, false, "PUBLISH/NOTIFY", TypeUtils.getTypeListViaXSDAny(lop.getMessages().getPublishNotify().getAny(), null));
     }
 
     docxFile.endTable();
   }
 
   private static void drawOperationPattern(DocxBaseWriter docxFile, String patternType) throws IOException
   {
     docxFile.startRow();
     docxFile.addCell(0, OPERATION_OVERVIEW_TABLE_WIDTHS, "Interaction Pattern", HEADER_COLOUR);
     docxFile.addCell(1, OPERATION_OVERVIEW_TABLE_WIDTHS, patternType, FIXED_COLOUR, 2);
     docxFile.endRow();
   }
 
   private static void drawOperationMessageHeader(DocxBaseWriter docxFile) throws IOException
   {
     docxFile.startRow();
     docxFile.addCell(0, OPERATION_OVERVIEW_TABLE_WIDTHS, "Pattern Sequence", HEADER_COLOUR);
     docxFile.addCell(1, OPERATION_OVERVIEW_TABLE_WIDTHS, "Message", HEADER_COLOUR);
     docxFile.addCell(2, OPERATION_OVERVIEW_TABLE_WIDTHS, "Body Type", HEADER_COLOUR);
     docxFile.endRow();
   }
 
   private static void drawOperationMessageDetails(DocxBaseWriter docxFile, AreaType area, boolean isIn, String message, List<TypeReference> types) throws IOException
   {
     docxFile.startRow();
     if (isIn)
     {
       docxFile.addCell(0, OPERATION_OVERVIEW_TABLE_WIDTHS, "IN", FIXED_COLOUR);
     }
     else
     {
       docxFile.addCell(0, OPERATION_OVERVIEW_TABLE_WIDTHS, "OUT", FIXED_COLOUR);
     }
     docxFile.addCell(1, OPERATION_OVERVIEW_TABLE_WIDTHS, message, FIXED_COLOUR);
 
     if (null == types)
     {
       docxFile.addCell(2, OPERATION_OVERVIEW_TABLE_WIDTHS, "Empty", FIXED_COLOUR);
     }
     else
     {
       docxFile.addCell(2, OPERATION_OVERVIEW_TABLE_WIDTHS, createFQTypeName(area, types), null);
     }
     docxFile.endRow();
   }
 
   private static void addOperationStructureDetails(DocxBaseWriter docxFile, OperationType op) throws IOException
   {
     docxFile.addTitle(4, "Structures");
 
     List<AnyTypeReference> msgs = new LinkedList<AnyTypeReference>();
 
     if (op instanceof SendOperationType)
     {
       SendOperationType lop = (SendOperationType) op;
       msgs.add(lop.getMessages().getSend());
     }
     else if (op instanceof SubmitOperationType)
     {
       SubmitOperationType lop = (SubmitOperationType) op;
       msgs.add(lop.getMessages().getSubmit());
     }
     else if (op instanceof RequestOperationType)
     {
       RequestOperationType lop = (RequestOperationType) op;
       msgs.add(lop.getMessages().getRequest());
       msgs.add(lop.getMessages().getResponse());
     }
     else if (op instanceof InvokeOperationType)
     {
       InvokeOperationType lop = (InvokeOperationType) op;
       msgs.add(lop.getMessages().getInvoke());
       msgs.add(lop.getMessages().getAcknowledgement());
       msgs.add(lop.getMessages().getResponse());
     }
     else if (op instanceof ProgressOperationType)
     {
       ProgressOperationType lop = (ProgressOperationType) op;
       msgs.add(lop.getMessages().getProgress());
       msgs.add(lop.getMessages().getAcknowledgement());
       msgs.add(lop.getMessages().getUpdate());
       msgs.add(lop.getMessages().getResponse());
     }
     else if (op instanceof PubSubOperationType)
     {
       PubSubOperationType lop = (PubSubOperationType) op;
       msgs.add(lop.getMessages().getPublishNotify());
     }
 
     if (0 < msgs.size())
     {
       addMessageStructureDetails(docxFile, msgs);
     }
   }
 
   private static void addMessageStructureDetails(DocxBaseWriter docxFile, List<AnyTypeReference> msgs) throws IOException
   {
     List<String> strings = null;
     for (AnyTypeReference msg : msgs)
     {
       strings = splitString(strings, msg.getComment());
     }
 
     docxFile.addNumberedComment(strings);
   }
 
   private void addOperationErrorDetails(DocxBaseWriter docxFile, AreaType area, OperationType op) throws IOException
   {
     docxFile.addTitle(4, "Errors");
 
     if (op instanceof SendOperationType)
     {
       docxFile.addComment("The operation cannot return any errors.");
     }
     else if (op instanceof SubmitOperationType)
     {
       SubmitOperationType lop = (SubmitOperationType) op;
       addErrorStructureDetails(docxFile, area, lop.getErrors());
     }
     else if (op instanceof RequestOperationType)
     {
       RequestOperationType lop = (RequestOperationType) op;
       addErrorStructureDetails(docxFile, area, lop.getErrors());
     }
     else if (op instanceof InvokeOperationType)
     {
       InvokeOperationType lop = (InvokeOperationType) op;
       addErrorStructureDetails(docxFile, area, lop.getErrors());
     }
     else if (op instanceof ProgressOperationType)
     {
       ProgressOperationType lop = (ProgressOperationType) op;
       addErrorStructureDetails(docxFile, area, lop.getErrors());
     }
     else if (op instanceof PubSubOperationType)
     {
       PubSubOperationType lop = (PubSubOperationType) op;
       addErrorStructureDetails(docxFile, area, lop.getErrors());
     }
   }
 
   private void addErrorStructureDetails(DocxBaseWriter docxFile, AreaType area, OperationErrorList errs) throws IOException
   {
     if ((null != errs) && (null != errs.getErrorOrErrorRef()) && (0 < errs.getErrorOrErrorRef().size()))
     {
       if (1 == errs.getErrorOrErrorRef().size())
       {
         docxFile.addComment("The operation may return the following error:");
       }
       else
       {
         docxFile.addComment("The operation may return the following errors:");
       }
 
       TreeMap<String, List<Object[]>> m = new TreeMap();
 
       for (Object object : errs.getErrorOrErrorRef())
       {
         if (object instanceof ErrorDefinitionType)
         {
           ErrorDefinitionType err = (ErrorDefinitionType) object;
 
           List<String> pcmts = splitString(null, err.getComment());
           if (null != err.getExtraInformation())
           {
             pcmts = splitString(pcmts, err.getExtraInformation().getComment());
           }
 
           String ev = "Not Used";
           if (null != err.getExtraInformation())
           {
             ev = createFQTypeName(area, err.getExtraInformation().getType());
           }
 
           List<Object[]> v;
           if (m.containsKey(String.valueOf(err.getNumber())))
           {
             v = m.get(String.valueOf(err.getNumber()));
           }
           else
           {
             v = new ArrayList();
             m.put(String.valueOf(err.getNumber()), v);
           }
 
           v.add(new Object[]
           {
             err.getName(), pcmts, err.getNumber(), ev
           });
         }
         else if (object instanceof ErrorReferenceType)
         {
           ErrorReferenceType err = (ErrorReferenceType) object;
 
           List<String> pcmts = splitString(null, err.getComment());
           if (null != err.getExtraInformation())
           {
             pcmts = splitString(pcmts, err.getExtraInformation().getComment());
           }
 
           String en;
           String es;
           if ((null == err.getType().getArea()) || (err.getType().getArea().equals(area.getName())))
           {
             ErrorDefinitionType edt = getErrorDefinition(err.getType().getName());
             if (null != edt)
             {
               en = String.valueOf(edt.getNumber());
             }
             else
             {
               en = "UNKNOWN ERROR NUMBER!";
             }
             es = en;
           }
           else
           {
             en = "Defined in " + err.getType().getArea();
             es = "0";
           }
 
           String ev = "Not Used";
           if (null != err.getExtraInformation())
           {
             ev = createFQTypeName(area, err.getExtraInformation().getType());
           }
 
           List<Object[]> v;
           if (m.containsKey(es))
           {
             v = m.get(es);
           }
           else
           {
             v = new ArrayList();
             m.put(es, v);
           }
 
           v.add(new Object[]
           {
             err.getType().getName(), pcmts, en, ev
           });
         }
       }
 
       for (String en : m.navigableKeySet())
       {
         for (Object[] err : m.get(en))
         {
           docxFile.addTitle(5, "ERROR: " + (String) err[0]);
 
           docxFile.addNumberedComment((List<String>) err[1]);
 
           docxFile.startTable(OPERATION_ERROR_TABLE_WIDTHS);
           docxFile.startRow();
           docxFile.addCell(0, OPERATION_ERROR_TABLE_WIDTHS, "Error", HEADER_COLOUR);
           docxFile.addCell(1, OPERATION_ERROR_TABLE_WIDTHS, "Error #", HEADER_COLOUR);
           docxFile.addCell(2, OPERATION_ERROR_TABLE_WIDTHS, "ExtraInfo Type", HEADER_COLOUR);
           docxFile.endRow();
 
           docxFile.startRow();
           docxFile.addCell(0, OPERATION_ERROR_TABLE_WIDTHS, (String) err[0]);
           docxFile.addCell(1, OPERATION_ERROR_TABLE_WIDTHS, String.valueOf(err[2]));
           docxFile.addCell(2, OPERATION_ERROR_TABLE_WIDTHS, (String) err[3]);
           docxFile.endRow();
           docxFile.endTable();
         }
       }
     }
     else
     {
       docxFile.addComment("The operation does not return any errors.");
     }
   }
 
   private void addErrorDefinitions(List<ErrorDefinitionType> errors, OperationErrorList errs) throws IOException
   {
     if ((null != errs) && (null != errs.getErrorOrErrorRef()) && (0 < errs.getErrorOrErrorRef().size()))
     {
       for (Object object : errs.getErrorOrErrorRef())
       {
         if (object instanceof ErrorDefinitionType)
         {
           errors.add((ErrorDefinitionType) object);
         }
       }
     }
   }
 
   private void createFundamentalClass(DocxBaseWriter docxFile, FundamentalType fundamental) throws IOException
   {
     String fundName = fundamental.getName();
 
     getLog().info("Creating fundamental class " + fundName);
 
     docxFile.addTitle(3, "Fundamental: " + fundName);
 
     if ((null != fundamental.getComment()) && (0 < fundamental.getComment().length()))
     {
       docxFile.addComment(fundamental.getComment());
     }
   }
 
   private void createAttributeClass(DocxBaseWriter docxFile, AttributeType attribute) throws IOException
   {
     String attrName = attribute.getName();
 
     getLog().info("Creating attribute class " + attrName);
 
     docxFile.addTitle(3, "Attribute: " + attrName);
 
     if ((null != attribute.getComment()) && (0 < attribute.getComment().length()))
     {
       docxFile.addComment(attribute.getComment());
     }
 
     docxFile.startTable(LIST_TABLE_WIDTHS);
 
     docxFile.startRow();
     docxFile.addCell(0, LIST_TABLE_WIDTHS, "Name", HEADER_COLOUR);
     docxFile.addCell(1, LIST_TABLE_WIDTHS, attrName, STD_COLOUR);
     docxFile.endRow();
 
     docxFile.startRow();
     docxFile.addCell(0, LIST_TABLE_WIDTHS, "Extends", HEADER_COLOUR);
     docxFile.addCell(1, LIST_TABLE_WIDTHS, StdStrings.ATTRIBUTE, STD_COLOUR);
     docxFile.endRow();
 
     docxFile.startRow();
     docxFile.addCell(0, LIST_TABLE_WIDTHS, "Short Form Part", HEADER_COLOUR);
     docxFile.addCell(1, LIST_TABLE_WIDTHS, String.valueOf(attribute.getShortFormPart()), STD_COLOUR);
     docxFile.endRow();
 
     docxFile.endTable();
   }
 
   private void createEnumerationClass(DocxBaseWriter docxFile, EnumerationType enumeration) throws IOException
   {
     String enumName = enumeration.getName();
 
     getLog().info("Creating enumeration class " + enumName);
 
     docxFile.addTitle(3, "ENUMERATION: " + enumName);
 
     if ((null != enumeration.getComment()) && (0 < enumeration.getComment().length()))
     {
       docxFile.addComment(enumeration.getComment());
     }
 
     docxFile.startTable(ENUM_TABLE_WIDTHS);
 
     docxFile.startRow();
     docxFile.addCell(0, ENUM_TABLE_WIDTHS, "Name", HEADER_COLOUR);
     docxFile.addCell(1, ENUM_TABLE_WIDTHS, enumeration.getName(), STD_COLOUR, 2);
     docxFile.endRow();
 
     docxFile.startRow();
     docxFile.addCell(0, ENUM_TABLE_WIDTHS, "Short Form Part", HEADER_COLOUR);
     docxFile.addCell(1, ENUM_TABLE_WIDTHS, String.valueOf(enumeration.getShortFormPart()), STD_COLOUR, 2);
     docxFile.endRow();
 
     // create attributes
     docxFile.startRow();
     docxFile.addCell(0, ENUM_TABLE_WIDTHS, "Enumeration Value", HEADER_COLOUR);
     docxFile.addCell(1, ENUM_TABLE_WIDTHS, "Numerical Value", HEADER_COLOUR);
     docxFile.addCell(2, ENUM_TABLE_WIDTHS, "Comment", HEADER_COLOUR);
     docxFile.endRow();
 
     for (Item item : enumeration.getItem())
     {
       docxFile.startRow();
       docxFile.addCell(0, ENUM_TABLE_WIDTHS, item.getValue());
       docxFile.addCell(1, ENUM_TABLE_WIDTHS, String.valueOf(item.getNvalue()));
       docxFile.addCell(2, ENUM_TABLE_WIDTHS, item.getComment(), false);
       docxFile.endRow();
     }
 
     docxFile.endTable();
   }
 
   private void createCompositeClass(DocxBaseWriter docxFile, AreaType area, CompositeType composite) throws IOException
   {
     String compName = composite.getName();
 
     getLog().info("Creating composite class " + compName);
 
     docxFile.addTitle(3, "Composite: " + compName);
 
     if ((null != composite.getComment()) && (0 < composite.getComment().length()))
     {
       docxFile.addComment(composite.getComment());
     }
 
     docxFile.startTable(COMPOSITE_TABLE_WIDTHS);
 
     docxFile.startRow();
     docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, "Name", HEADER_COLOUR);
     docxFile.addCell(1, COMPOSITE_TABLE_WIDTHS, composite.getName(), STD_COLOUR, 3);
     docxFile.endRow();
 
     String extendsClass = StdStrings.COMPOSITE;
     if ((null != composite.getExtends()) && (null != composite.getExtends().getType()))
     {
       extendsClass = createFQTypeName(area, composite.getExtends().getType());
     }
     docxFile.startRow();
     docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, "Extends", HEADER_COLOUR);
     docxFile.addCell(1, COMPOSITE_TABLE_WIDTHS, extendsClass, STD_COLOUR, 3);
     docxFile.endRow();
 
     if (null == composite.getShortFormPart())
     {
       docxFile.startRow();
       docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, "Abstract", HEADER_COLOUR, 4);
       docxFile.endRow();
     }
     else
     {
       docxFile.startRow();
       docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, "Short Form Part", HEADER_COLOUR);
       docxFile.addCell(1, COMPOSITE_TABLE_WIDTHS, String.valueOf(composite.getShortFormPart()), STD_COLOUR, 3);
       docxFile.endRow();
     }
 
     // create attributes
     List<CompositeField> compElements = createCompositeElementsList(docxFile, composite);
     if (!compElements.isEmpty())
     {
       docxFile.startRow();
       docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, "Field", HEADER_COLOUR);
       docxFile.addCell(1, COMPOSITE_TABLE_WIDTHS, "Type", HEADER_COLOUR);
       docxFile.addCell(2, COMPOSITE_TABLE_WIDTHS, "Nullable", HEADER_COLOUR);
       docxFile.addCell(3, COMPOSITE_TABLE_WIDTHS, "Comment", HEADER_COLOUR);
       docxFile.endRow();
 
       for (CompositeField element : compElements)
       {
         docxFile.startRow();
         docxFile.addCell(0, COMPOSITE_TABLE_WIDTHS, element.getFieldName());
         docxFile.addCell(1, COMPOSITE_TABLE_WIDTHS, createFQTypeName(area.getName(), element.getEncodeCall(), element.getTypeName(), element.isList()));
         docxFile.addCell(2, COMPOSITE_TABLE_WIDTHS, element.isCanBeNull() ? "Yes" : "No");
         docxFile.addCell(3, COMPOSITE_TABLE_WIDTHS, element.getComment(), false);
         docxFile.endRow();
       }
     }
 
     docxFile.endTable();
   }
 
   private static String operationType(OperationType op) throws IOException
   {
     if (op instanceof SendOperationType)
     {
       return "SEND";
     }
     else if (op instanceof SubmitOperationType)
     {
       return "SUBMIT";
     }
     else if (op instanceof RequestOperationType)
     {
       return "REQUEST";
     }
     else if (op instanceof InvokeOperationType)
     {
       return "INVOKE";
     }
     else if (op instanceof ProgressOperationType)
     {
       return "PROGRESS";
     }
     else if (op instanceof PubSubOperationType)
     {
       return "PUBLISH-SUBSCRIBE";
     }
 
     return "Unknown";
   }
 
   private static String yesNoType(boolean bool) throws IOException
   {
     if (bool)
     {
       return "Yes";
     }
 
     return "No";
   }
 
   private static String[] createFQTypeName(AreaType area, List<TypeReference> types)
   {
     String[] buf = new String[types.size()];
 
     for (int i = 0; i < types.size(); i++)
     {
       TypeReference type = types.get(i);
       buf[i] = createFQTypeName(area.getName(), type.getArea(), type.getName(), type.isList());
     }
 
     return buf;
   }
 
   private static String createFQTypeName(AreaType area, TypeReference type)
   {
     return createFQTypeName(area.getName(), type.getArea(), type.getName(), type.isList());
   }
 
   private static String createFQTypeName(AreaType area, ObjectReference type)
   {
     return createFQTypeName(area.getName(), type.getArea(), String.valueOf(type.getNumber()), false);
   }
 
   private static String createFQTypeName(String myArea, String typeArea, String typeName, boolean isList)
   {
     if (!myArea.equalsIgnoreCase(typeArea))
     {
       typeName = typeArea + "::" + typeName;
     }
 
     if (isList)
     {
       typeName = "List<" + typeName + ">";
     }
 
     return typeName;
   }
 
   private class DocxNumberingWriter extends AbstractWriter
   {
     private int numberingInstance = 1;
     private final Writer file;
     private final StringBuffer buffer = new StringBuffer();
 
     protected DocxNumberingWriter(String folder, String className, String ext) throws IOException
     {
       file = StubUtils.createLowLevelWriter(folder, className, ext);
       file.append(addFileStatement(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:abstractNum w:abstractNumId=\"0\"><w:nsid w:val=\"0010107A\"/><w:multiLevelType w:val=\"hybridMultilevel\"/><w:tmpl w:val=\"2FAEA97C\"/><w:lvl w:ilvl=\"0\"><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"-\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"1080\"/></w:tabs><w:ind w:left=\"1080\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"1\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"o\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"1800\"/></w:tabs><w:ind w:left=\"1800\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"2\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"?\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"2520\"/></w:tabs><w:ind w:left=\"2520\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum><w:abstractNum w:abstractNumId=\"1\"><w:nsid w:val=\"6805624A\"/><w:multiLevelType w:val=\"hybridMultilevel\"/><w:tmpl w:val=\"E1A8AF06\"/><w:lvl w:ilvl=\"0\" w:tplc=\"08090001\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"720\"/></w:tabs><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/></w:rPr></w:lvl><w:lvl w:ilvl=\"1\" w:tplc=\"607A8D92\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%2.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"1440\"/></w:tabs><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"2\" w:tplc=\"87184C80\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%3.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"2160\"/></w:tabs><w:ind w:left=\"2160\" w:hanging=\"180\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"3\" w:tplc=\"277E9776\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%4.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"2880\"/></w:tabs><w:ind w:left=\"2880\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"4\" w:tplc=\"3934DEFA\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%5.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"3600\"/></w:tabs><w:ind w:left=\"3600\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"5\" w:tplc=\"B838E67C\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%6.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"4320\"/></w:tabs><w:ind w:left=\"4320\" w:hanging=\"180\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"6\" w:tplc=\"AF3C1126\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%7.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"5040\"/></w:tabs><w:ind w:left=\"5040\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"7\" w:tplc=\"2DE61552\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%8.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"5760\"/></w:tabs><w:ind w:left=\"5760\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"8\" w:tplc=\"43546844\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%9.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"6480\"/></w:tabs><w:ind w:left=\"6480\" w:hanging=\"180\"/></w:pPr></w:lvl></w:abstractNum>", false));
       buffer.append(addFileStatement(2, "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num>", false));
 
       getLog().info("Creating file " + folder + " " + className + "." + ext);
     }
 
     protected int getNextNumberingInstance() throws IOException
     {
       int instance = ++numberingInstance;
       file.append(addFileStatement(0, "<w:abstractNum w:abstractNumId=\"" + instance + "\"><w:nsid w:val=\"" + instance + "\"/><w:multiLevelType w:val=\"hybridMultilevel\"/><w:tmpl w:val=\"FFFFFFFF\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%1)\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"720\"/></w:tabs><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"1\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%2.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"1440\"/></w:tabs><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"2\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%3.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"2160\"/></w:tabs><w:ind w:left=\"2160\" w:hanging=\"180\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"3\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%4.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"2880\"/></w:tabs><w:ind w:left=\"2880\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"4\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%5.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"3600\"/></w:tabs><w:ind w:left=\"3600\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"5\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%6.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"4320\"/></w:tabs><w:ind w:left=\"4320\" w:hanging=\"180\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"6\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%7.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"5040\"/></w:tabs><w:ind w:left=\"5040\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"7\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerLetter\"/><w:lvlText w:val=\"%8.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"5760\"/></w:tabs><w:ind w:left=\"5760\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"8\" w:tentative=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"lowerRoman\"/><w:lvlText w:val=\"%9.\"/><w:lvlJc w:val=\"right\"/><w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"6480\"/></w:tabs><w:ind w:left=\"6480\" w:hanging=\"180\"/></w:pPr></w:lvl></w:abstractNum>", false));
       buffer.append(addFileStatement(2, "<w:num w:numId=\"" + instance + "\"><w:abstractNumId w:val=\"" + instance + "\"/></w:num>", false));
       return instance;
     }
 
     @Override
     public void flush() throws IOException
     {
       file.append(buffer.toString());
       file.append(addFileStatement(0, "</w:numbering>", false));
       file.flush();
     }
   }
 
   private static class DocxBaseWriter extends AbstractWriter
   {
     private final DocxNumberingWriter numberWriter;
     private final StringBuffer buffer = new StringBuffer();
 
     protected DocxBaseWriter() throws IOException
     {
       super("\r\n");
       numberWriter = null;
     }
 
     protected DocxBaseWriter(DocxNumberingWriter numberWriter) throws IOException
     {
       super("\r\n");
       this.numberWriter = numberWriter;
     }
 
     protected void startTable(int[] widths) throws IOException
     {
       startTable(widths, null);
     }
 
     protected void startTable(int[] widths, String caption) throws IOException
     {
       if (null != caption)
       {
         buffer.append(addFileStatement(2, "<w:p>", false));
         buffer.append(addFileStatement(3, "<w:pPr><w:pStyle w:val=\"TableTitle\"/></w:pPr><w:r><w:t xml:space=\"preserve\">Table </w:t></w:r>", false));
         buffer.append(addFileStatement(3, "<w:bookmarkStart w:id=\"0\" w:name=\"T_" + caption + "\"/>", false));
         buffer.append(addFileStatement(3, "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> STYLEREF \"Heading 1\"\\l \\n \\t  \\* MERGEFORMAT </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>1</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r><w:r><w:noBreakHyphen/></w:r><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> SEQ Table \\s 1 </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>1</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r><w:bookmarkEnd w:id=\"0\"/><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText>tc  \\f T \"</w:instrText></w:r><w:fldSimple w:instr=\" STYLEREF &quot;Heading 1&quot;\\l \\n \\t  \\* MERGEFORMAT \">", false));
         buffer.append(addFileStatement(3, "<w:bookmarkStart w:id=\"1\" w:name=\"_" + caption + "\"/><w:r><w:instrText>1</w:instrText></w:r></w:fldSimple>", false));
         buffer.append(addFileStatement(3, "<w:r><w:instrText>-</w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> SEQ Table_TOC \\s 1 </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:instrText>1</w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>", false));
         buffer.append(addFileStatement(3, "<w:r><w:instrText>" + caption + "</w:instrText></w:r>", false));
         buffer.append(addFileStatement(3, "<w:bookmarkEnd w:id=\"1\"/><w:r><w:instrText>\"</w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r>", false));
         buffer.append(addFileStatement(3, "<w:r><w:t>:  " + caption + "</w:t></w:r>", false));
         buffer.append(addFileStatement(2, "</w:p>", false));
 
       }
       buffer.append(addFileStatement(2, "<w:tbl>", false));
       buffer.append(addFileStatement(3, "<w:tblPr>", false));
       buffer.append(addFileStatement(4, "<w:tblW w:w=\"00\" w:type=\"auto\"/>", false));
       buffer.append(addFileStatement(4, "<w:tblBorders>", false));
       buffer.append(addFileStatement(5, "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(5, "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(5, "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(5, "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(5, "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(5, "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>", false));
       buffer.append(addFileStatement(4, "</w:tblBorders>", false));
       buffer.append(addFileStatement(3, "</w:tblPr>", false));
 
       if (null != widths)
       {
         buffer.append(addFileStatement(3, "<w:tblGrid>", false));
         for (int i : widths)
         {
           buffer.append(addFileStatement(4, "<w:gridCol w:w=\"" + i + "\"/>", false));
         }
         buffer.append(addFileStatement(3, "</w:tblGrid>", false));
       }
     }
 
     protected void startRow() throws IOException
     {
       buffer.append(addFileStatement(3, "<w:tr>", false));
     }
 
     protected void addCell(int index, int[] widths, String text) throws IOException
     {
       addCell(index, widths, text, null, true, 0, false, false);
     }
 
     protected void addCell(int index, int[] widths, String text, boolean vMerge, boolean vRestart) throws IOException
     {
       addCell(index, widths, text, null, true, 0, vMerge, vRestart);
     }
 
     protected void addCell(int index, int[] widths, String text, String shade, boolean vMerge, boolean vRestart) throws IOException
     {
       addCell(index, widths, text, shade, true, 0, vMerge, vRestart);
     }
 
     protected void addCell(int index, int[] widths, String text, boolean centered) throws IOException
     {
       addCell(index, widths, text, null, centered, 0, false, false);
     }
 
     protected void addCell(int index, int[] widths, String text, String shade) throws IOException
     {
       addCell(index, widths, text, shade, true, 0, false, false);
     }
 
     protected void addCell(int index, int[] widths, String text, String shade, boolean centered) throws IOException
     {
       addCell(index, widths, text, shade, centered, 0, false, false);
     }
 
     protected void addCell(int index, int[] widths, String text, String shade, int span) throws IOException
     {
       addCell(index, widths, text, shade, true, span, false, false);
     }
 
     protected void addCell(int index, int[] widths, String[] text, String shade) throws IOException
     {
       addCell(index, widths, text, shade, 1);
     }
 
     protected void addCell(int index, int[] widths, String[] text, String shade, int span) throws IOException
     {
       StringBuilder buf = new StringBuilder();
       for (int i = 0; i < text.length; i++)
       {
         String string = text[i];
         buf.append("<w:r><w:t>");
         buf.append(escape(string));
         buf.append("</w:t></w:r>");
         if ((i + 1) != text.length)
         {
           buf.append("</w:p><w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr>");
         }
       }
       actualAddCell(index, widths, buf.toString(), shade, true, span, false, false);
     }
 
     protected void addCell(int index, int[] widths, String text, String shade, boolean centered, int span, boolean vMerge, boolean vRestart) throws IOException
     {
       actualAddCell(index, widths, "<w:r><w:t>" + escape(text) + "</w:t></w:r>", shade, centered, span, vMerge, vRestart);
     }
 
     protected void actualAddCell(int index, int[] widths, String text, String shade, boolean centered, int span, boolean vMerge, boolean vRestart) throws IOException
     {
       StringBuilder buf = new StringBuilder();
       buf.append("<w:tc><w:tcPr>");
       if (null != widths)
       {
         int width = 0;
         if (1 < span)
         {
           for (int i = index; i < (index + span); i++)
           {
             width += widths[i];
           }
         }
         else
         {
           width = widths[index];
         }
 
         buf.append("<w:tcW w:w=\"").append(width).append("\" w:type=\"dxa\"/>");
       }
       if (vMerge)
       {
         if (vRestart)
         {
           buf.append("<w:vMerge w:val=\"restart\"/><w:vAlign w:val=\"center\"/>");
         }
         else
         {
           buf.append("<w:vMerge/>");
         }
       }
       if (1 < span)
       {
         buf.append("<w:gridSpan w:val=\"");
         buf.append(span);
         buf.append("\"/>");
       }
       if (null != shade)
       {
         buf.append("<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"");
         buf.append(shade);
         buf.append("\"/></w:tcPr>");
       }
       else
       {
         buf.append("</w:tcPr>");
       }
       if ((!vMerge) || (vRestart))
       {
         buf.append("<w:p>");
         if (centered)
         {
           buf.append("<w:pPr><w:jc w:val=\"center\"/></w:pPr>");
         }
         buf.append(text);
         buf.append("</w:p>");
       }
       else
       {
         buf.append("<w:p/>");
       }
       buf.append("</w:tc>");
 
       buffer.append(addFileStatement(4, buf.toString(), false));
     }
 
     protected void endRow() throws IOException
     {
       buffer.append(addFileStatement(3, "</w:tr>", false));
     }
 
     protected void endTable() throws IOException
     {
       buffer.append(addFileStatement(2, "</w:tbl>", false));
     }
 
     protected void addTitle(int level, String section) throws IOException
     {
       buffer.append(addFileStatement(2, "<w:p><w:pPr><w:pStyle w:val=\"Heading" + level + "\"/></w:pPr><w:r><w:t>" + section + "</w:t></w:r></w:p>", false));
     }
 
     protected void addNumberedComment(List<String> strings) throws IOException
     {
       if ((null != strings) && (0 < strings.size()))
       {
         if (1 == strings.size())
         {
           addComment(strings.get(0));
         }
         else
         {
           int instance = 0;
           if (null != this.numberWriter)
           {
             instance = this.numberWriter.getNextNumberingInstance();
           }
 
           for (String text : strings)
           {
             if (null != text)
             {
               addNumberedComment(instance, text);
             }
           }
         }
       }
     }
 
     protected void addComment(String text) throws IOException
     {
       List<String> strings = splitString(null, text);
 
       if (0 < strings.size())
       {
 
         for (int i = 0; i < strings.size(); i++)
         {
           String string = strings.get(i);
 
           if (null != string)
           {
             if (string.contentEquals("<ul>") || string.contentEquals("</ul>"))
             {
               string = null;
             }
             else
             {
               if (string.contains("<li>"))
               {
                 string = string.substring(string.indexOf("<li>") + 4, string.indexOf("</li>"));
                 string = "<w:p><w:pPr><w:pStyle w:val=\"ListParagraph\"/><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr><w:r><w:t>" + escape(string) + "</w:t></w:r></w:p>";
               }
               else
               {
                 string = "<w:p><w:r><w:t>" + escape(string) + "</w:t></w:r></w:p>";
               }
             }
 
             strings.set(i, string);
           }
         }
 
         for (String str : strings)
         {
           if (null != str)
           {
             buffer.append(addFileStatement(2, str, false));
           }
         }
       }
     }
 
     @Override
     public void flush() throws IOException
     {
     }
 
     protected StringBuffer getBuffer()
     {
       return buffer;
     }
 
     protected void appendBuffer(StringBuffer buf)
     {
       buffer.append(buf);
     }
 
     private void addNumberedComment(int instance, String text) throws IOException
     {
       List<String> strings = splitString(null, text);
       if (0 < strings.size())
       {
         if (1 < strings.size())
         {
           for (String str : strings)
           {
             addNumberedComment(instance, str);
           }
         }
         else
         {
           String str = strings.get(0);
           if ((null != str) && (0 < str.length()))
           {
             buffer.append(addFileStatement(2, "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"" + instance + "\"/></w:numPr></w:pPr><w:r><w:t>" + escape(str) + "</w:t></w:r></w:p>", false));
           }
         }
       }
     }
 
     private String escape(String t)
     {
       if (null != t)
       {
         t = t.replaceAll("&", "&amp;");
         t = t.replaceAll("<", "&lt;");
         t = t.replaceAll(">", "&gt;");
 
         return t;
       }
 
       return "";
     }
   }
 
   private class DocxWriter extends DocxBaseWriter
   {
     private final Writer file;
 
     protected DocxWriter(String folder, String className, String ext, DocxNumberingWriter numberWriter) throws IOException
     {
       super(numberWriter);
 
       file = StubUtils.createLowLevelWriter(folder, className, ext);
 
       getLog().info("Creating file " + folder + " " + className + "." + ext);
 
       file.append(addFileStatement(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", false));
       file.append(addFileStatement(0, "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">", false));
       file.append(addFileStatement(1, "<w:body>", false));
     }
 
     @Override
     public void flush() throws IOException
     {
       file.append(getBuffer());
       file.append(addFileStatement(1, "</w:body>", false));
       file.append(addFileStatement(0, "</w:document>", false));
       file.flush();
     }
   }
 }
