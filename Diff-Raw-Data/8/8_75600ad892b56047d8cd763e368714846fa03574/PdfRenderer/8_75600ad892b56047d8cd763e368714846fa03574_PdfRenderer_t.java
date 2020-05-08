 /*
  * Copyright 2013- Yan Bonnel
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.ybonnel.breizhcamppdf;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.itextpdf.text.*;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.html.simpleparser.HTMLWorker;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.petebevin.markdown.MarkdownProcessor;
 import fr.ybonnel.breizhcamppdf.model.Speaker;
 import fr.ybonnel.breizhcamppdf.model.Talk;
 import fr.ybonnel.breizhcamppdf.model.TalkDetail;
 import fr.ybonnel.breizhcamppdf.model.Track;
 
 import java.awt.*;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 
 public class PdfRenderer {
 
     private static final Font talkFont =
             FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL, BaseColor.BLACK);
     private static final Font speakerFont =
             FontFactory.getFont(FontFactory.HELVETICA, 9,
                     Font.NORMAL, BaseColor.BLACK);
 
     private static final Font themeFont =
             FontFactory.getFont(FontFactory.HELVETICA, 8,
                     Font.ITALIC, BaseColor.GRAY);
 
     private static final Font presentFont =
             FontFactory.getFont(FontFactory.HELVETICA, 13,
                     Font.BOLD, BaseColor.GRAY);
 
     private static final Font talkFontTitle =
             FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, Font.UNDERLINE, BaseColor.DARK_GRAY);
 
     private Document document;
     private PdfWriter pdfWriter;
     private DataService service = new DataService();
 
     public PdfRenderer(Document document, PdfWriter pdfWriter) {
         this.document = document;
         this.pdfWriter = pdfWriter;
     }
 
     public void render() throws DocumentException, IOException {
 
         // Footer
         pdfWriter.setPageEvent(new PdfPageEventHelper() {
             @Override
             public void onEndPage(PdfWriter writer, Document document) {
                System.out.println(document.getPageSize().getLeft());
                System.out.println(document.getPageSize().getRight());
                System.out.println(document.getPageSize().getBottom());
                Rectangle rect = document.getPageSize();
                 ColumnText.showTextAligned(
                         writer.getDirectContent(),
                         Element.ALIGN_CENTER,
                         new Phrase("BreizhCamp 2013"),
                        (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() + 18, 0);
             }
         });
 
         createFirstPage();
         List<Talk> talksToExplain = createProgrammePages();
         createTalksPages(talksToExplain);
     }
 
 
     private void createFirstPage() throws DocumentException, IOException {
         Rectangle savePagesize = document.getPageSize();
         document.setPageSize(PageSize.A4);
         document.newPage();
         Image imageLogo = Image.getInstance(PdfRenderer.class.getResource("/logo.png"));
         imageLogo.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin(), imageLogo.getHeight());
         document.add(imageLogo);
 
         Paragraph paragraph = new Paragraph("13 et 14 Juin 2013");
         paragraph.setSpacingAfter(25);
         paragraph.getFont().setSize(25);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         document.add(paragraph);
 
         document.setPageSize(savePagesize);
     }
 
 
     private PdfPCell createHeaderCell(String content) {
         Paragraph paragraph = new Paragraph();
         Font font = new Font();
         font.setColor(BaseColor.WHITE);
         paragraph.setFont(font);
         paragraph.add(new Phrase(content));
         PdfPCell cell = new PdfPCell(paragraph);
         cell.setPaddingBottom(10);
         cell.setHorizontalAlignment(Element.ALIGN_CENTER);
         cell.setBackgroundColor(BaseColor.GRAY);
         return cell;
     }
 
     private List<Talk> createProgrammePages() throws DocumentException, IOException {
         List<Talk> talksToExplain = new ArrayList<>();
         document.setPageSize(PageSize.A4.rotate());
         Font font = new Font();
         font.setStyle(Font.BOLD);
         font.setSize(14);
         Paragraph titre = null;
 
 
         for (String date : service.getDates()) {
             PdfPTable table = createBeginningOfPage(font, date);
             for (String creneau : service.getCreneaux().get(date)) {
                 // Nouvelle page à 14h
                 if (creneau.startsWith("14")) {
                     document.add(table);
                     table = createBeginningOfPage(font, date);
                 }
 
                 table.addCell(createCellCentree(creneau));
                 for (String room : service.getRooms()) {
                     PdfPCell cell = new PdfPCell();
                     cell.setPaddingBottom(10);
                     cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 
                     Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
                     if (talk != null) {
                         talksToExplain.add(talk);
                         remplirCellWithTalk(cell, talk);
                     }
                     table.addCell(cell);
                 }
             }
             document.add(table);
         }
         return talksToExplain;
     }
 
     private PdfPTable createBeginningOfPage(Font font, String date) throws DocumentException {
         Paragraph titre;
         document.newPage();
         titre = new Paragraph();
         titre.setFont(font);
         titre.setAlignment(Paragraph.ALIGN_CENTER);
         titre.add(new Phrase("Programme du " + date));
         document.add(titre);
 
         float[] relativeWidth = new float[service.getRooms().size() + 1];
         Arrays.fill(relativeWidth, 1f);
         relativeWidth[0] = 0.5f;
 
         PdfPTable table = new PdfPTable(relativeWidth);
 
         table.setWidthPercentage(100);
         table.setSpacingBefore(10);
         table.setSpacingAfter(20);
 
         table.addCell(createHeaderCell("Heure"));
         for (String room : service.getRooms()) {
             table.addCell(createHeaderCell(room));
         }
         return table;
     }
 
     private PdfPCell createCellCentree(String content) {
         return createCellCentree(content, null);
     }
 
     private PdfPCell createCellCentree(String content, Font font) {
         Paragraph creneau = null;
         if (font == null) {
             creneau = new Paragraph(content);
         } else {
             creneau = new Paragraph(content, font);
         }
         creneau.setAlignment(Paragraph.ALIGN_CENTER);
 
         PdfPCell cell = new PdfPCell();
         cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
         cell.setPaddingBottom(10);
         cell.addElement(creneau);
         return cell;
     }
 
     private static final Map<String, BaseColor> mapTrack = new HashMap<String, BaseColor>() {{
         put("web", BaseColor.BLUE);
         put("cloud et bigdata", BaseColor.CYAN);
         put("agilité", BaseColor.GREEN);
         put("devops", BaseColor.MAGENTA);
         put("eXtreme", BaseColor.ORANGE);
         put("keynote", BaseColor.PINK);
         put("cloud et architecture", BaseColor.RED);
         put("langages", BaseColor.YELLOW);
         put("découverte", new BaseColor(Color.decode("#B0C4DE")));
         put("web et mobile", new BaseColor(Color.decode("#FF8C00")));
         put("tooling", new BaseColor(Color.decode("#D8BFD8")));
     }};
 
 
     private void remplirCellWithTalk(PdfPCell cell, Talk talk) throws DocumentException, IOException {
         Image image = Image.getInstance(PdfRenderer.class.getResource("/formats/" + talk.getFormat().replaceAll(" ", "") + ".png"));
         image.scaleAbsoluteWidth(15);
         image.setAlignment(Image.TEXTWRAP);
         image.setAbsolutePosition(0, 0);
 
 
         float[] widths = {0.15f, 0.85f};
         PdfPTable table = new PdfPTable(widths);
         table.setWidthPercentage(100f);
         table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
         table.addCell(image);
         PdfPCell subCell = new PdfPCell();
         Chunk chunk = new Chunk(talk.getTitle(), talkFont);
         chunk.setLocalGoto("talk" + talk.getId());
         Paragraph titleTalk = new Paragraph();
         titleTalk.add(chunk);
         titleTalk.setAlignment(Paragraph.ALIGN_CENTER);
         subCell.addElement(titleTalk);
         Paragraph track = new Paragraph(new Phrase(talk.getTrack(), themeFont));
         track.setAlignment(Paragraph.ALIGN_CENTER);
 
         subCell.addElement(track);
         for (Speaker speaker : TalkService.INSTANCE.getTalkDetail(talk.getId()).getSpeakers()) {
             Paragraph speakerText = new Paragraph(speaker.getFullname(), speakerFont);
             speakerText.setAlignment(Paragraph.ALIGN_CENTER);
             subCell.addElement(speakerText);
         }
         subCell.setBorder(Rectangle.NO_BORDER);
         table.addCell(subCell);
         cell.setBackgroundColor(mapTrack.get(talk.getTrack()));
         cell.addElement(table);
     }
 
 
     private MarkdownProcessor markdownProcessor = new MarkdownProcessor();
 
 
     private void createTalksPages(List<Talk> talksToExplain) throws DocumentException, IOException {
         document.setPageSize(PageSize.A4);
         document.newPage();
 
         Paragraph paragraph = new Paragraph("Liste des talks");
         paragraph.setSpacingAfter(25);
         paragraph.getFont().setSize(25);
         paragraph.setAlignment(Element.ALIGN_CENTER);
         document.add(paragraph);
 
         for (TalkDetail talk : Lists.transform(talksToExplain, new Function<Talk, TalkDetail>() {
             @Override
             public TalkDetail apply(Talk input) {
                 return TalkService.INSTANCE.getTalkDetail(input.getId());
             }
         })) {
 
             Paragraph empty = new Paragraph(" ");
             PdfPTable table = new PdfPTable(1);
             table.setWidthPercentage(100);
             table.setKeepTogether(true);
             PdfPCell cell = null;
             Chunk titleTalk = new Chunk(talk.getTitle(), talkFontTitle);
             titleTalk.setLocalDestination("talk" + talk.getId());
 
             cell = new PdfPCell(new Paragraph(titleTalk));
             cell.setBorder(0);
             table.addCell(cell);
             cell = new PdfPCell(empty);
             cell.setBorder(0);
             table.addCell(cell);
 
             cell = new PdfPCell();
             cell.setBorder(0);
             for (Element element : HTMLWorker.parseToList(new StringReader(markdownProcessor.markdown(talk.getDescription())), null)) {
                 cell.addElement(element);
             }
             table.addCell(cell);
             cell = new PdfPCell(empty);
             cell.setBorder(0);
             table.addCell(cell);
             StringBuilder textSpeakers = new StringBuilder("Présenté par ");
             List<Speaker> speakers = new ArrayList<>(talk.getSpeakers());
             for (int countSpeakers = 0; countSpeakers < speakers.size(); countSpeakers++) {
                 if (countSpeakers != 0 && countSpeakers == speakers.size() - 1) {
                     textSpeakers.append(" et ");
                 } else if (countSpeakers != 0) {
                     textSpeakers.append(", ");
                 }
                 textSpeakers.append(speakers.get(countSpeakers).getFullname());
             }
             Paragraph presentBy = new Paragraph(textSpeakers.toString(), presentFont);
             cell = new PdfPCell(presentBy);
             cell.setBorder(0);
             table.addCell(cell);
             cell = new PdfPCell(empty);
             cell.setBorder(0);
             table.addCell(cell);
             document.add(table);
         }
     }
 }
