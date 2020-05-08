 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import org.fiz.ClientRequest.Type;
 import org.fiz.test.*;
 
 /**
  * Junit tests for the RatingSection class.
  */
 public class RatingSectionTest extends junit.framework.TestCase {
     protected ClientRequestFixture cr;
     protected Dataset sampleData = new Dataset("record", new Dataset(
             "sampleRating", "1.3",
             "testing", "Quite Certainly&"
     ));
 
     public void setUp() {
         cr = new ClientRequestFixture();
     }
     
     public void test_setRating() {
         cr.setClientRequestType(Type.AJAX);
         RatingSection rs = new RatingSection(new Dataset(
                 "id", "testRS",
                 "numImages", "3"
         ));
         
         // Ensure that nothing happens if the RatingSection hasn't been 
         // rendered.
         rs.setRating(cr, 2);
         assertEquals("Js code evaluated when RatingSection not rendered", 
                 "", cr.getJsCode(false));
         
         // Ensure that the correct Javascript code is executed when the 
         // RatingSection has been rendered.
         rs.render(new ClientRequestFixture());
         rs.setRating(cr, 2);
         assertEquals("Js code evaluated when RatingSection rendered", 
         		"Fiz.ids.testRS.setOfficialRating(2.0);", cr.getJsCode(false));
     }
     
     public void test_setReadOnly() {
         cr.setClientRequestType(Type.AJAX);
         RatingSection rs = new RatingSection(new Dataset(
                 "id", "testRS",
                 "numImages", "3"
         ));
         
         // Ensure that nothing happens if the RatingSection hasn't been 
         // rendered.
         rs.setReadOnly(cr, true);
         assertEquals("Js code evaluated when RatingSection not rendered", 
                 "", cr.getJsCode(false));
         
         // Ensure that the correct Javascript code is executed when the 
         // RatingSection has been rendered.
         rs.render(new ClientRequestFixture());
         rs.setReadOnly(cr, true);
         assertEquals("Js code evaluated when RatingSection rendered", 
                 "Fiz.ids.testRS.readOnly = true;", cr.getJsCode(false));
     }
     
     public void test_render_overview_allDefaults() {
         RatingSection rs = new RatingSection(new Dataset(
                 "numImages", "2"
         ));
         rs.render(cr);
         
         // Ensure that the correct files and tokens were included.
         assertEquals("Included Javascript", 
                 "static/fiz/Ajax.js, static/fiz/Fiz.js, " +
                 "static/fiz/FizCommon.js, static/fiz/RatingSection.js",
                 cr.getHtml().getJsFiles());
         assertEquals("Included CSS", "RatingSection.css", 
                 cr.getHtml().getCssFiles());
         assertTrue("Auth Token Set", cr.isAuthTokenSet());
         
         // Ensure that the generated HTML is correct.
         String generatedHtml = cr.getHtml().getBody().toString();
         assertEquals("Generated HTML",
                 "\n<!-- Start RatingSection rating0 -->\n" +
                 "<div id=\"rating0\" class=\"RatingSection\" " +
                   "onmousemove=\"Fiz.ids.rating0.mouseMoveHandler(event);\" " +
                   "onmouseout=\"Fiz.ids.rating0.mouseOutHandler(event);\" " +
                   "onclick=\"Fiz.ids.rating0.clickHandler(event);\">\n" +
                 "  <div id=\"rating0_offRating\" class=\"offRating\">\n" +
                 "    <img id=\"rating0_offImg1\" src=\"/static/fiz/images/" +
                       "goldstar24-off.png\" alt=\"Off1\"></img>" +
                     "<img id=\"rating0_offImg2\" src=\"/static/fiz/images/" +
                       "goldstar24-off.png\" alt=\"Off2\"></img>\n" +
                 "  </div>\n" +
                 "  <div id=\"rating0_onRating\" class=\"onRating\">\n" +
                 "    <img id=\"rating0_onImg1\" src=\"/static/fiz/images/" +
                       "goldstar24-on.png\" alt=\"On1\"></img>" +
                     "<img id=\"rating0_onImg2\" src=\"/static/fiz/images/" +
                       "goldstar24-on.png\" alt=\"On2\"></img>\n" +
                 "  </div>\n" +
                 "  <div id=\"rating0_unratedMsg\" class=\"unratedMsg\">" +
                     "Not Yet Rated</div>\n" +
                 "</div>\n" +
                 "<!-- End RatingSection rating0 -->\n",
                 generatedHtml);
         
         // Ensure that the generated HTML is valid XHTML.
         TestUtil.assertXHTML(generatedHtml);
         
        // Ensure that the evaluated Javascript is correct.
        assertEquals("Evaluated Javascript", 
                "Fiz.auth = \"JHB9AM69@$6=TAF*J \";\n" +
                "Fiz.ids.rating0 = new Fiz.RatingSection(\"rating0\", 2, " +
                    "1, -1, false, true, \"\");\n", cr.getJsCode(true));
     }
     
     public void test_render_userId() {
         RatingSection rs = new RatingSection(new Dataset(
                 "numImages", "2",
                 "id", "testRS"
         ));
         rs.render(cr);
         
         // Ensure the specified id is correctly substituted throughout the HTML
         // and Javascript.
         String generatedHtml = cr.getHtml().getBody().toString();
         TestUtil.assertSubstring("id on main RatingSection <div>", 
                 "id=\"testRS\"", generatedHtml);
         TestUtil.assertSubstring("id on offRating <div>", 
                 "id=\"testRS_offRating\"", generatedHtml);
         TestUtil.assertSubstring("id on onRating <div>", 
                 "id=\"testRS_onRating\"", generatedHtml);
         TestUtil.assertSubstring("id on unratedMsg <div>", 
                 "id=\"testRS_unratedMsg\"", generatedHtml);
         TestUtil.assertSubstring("id on an 'off' <img>", 
                 "id=\"testRS_offImg", generatedHtml);
         TestUtil.assertSubstring("id on an 'on' <img>", 
                 "id=\"testRS_onImg", generatedHtml);
         
         TestUtil.assertSubstring("id in evaluated Js", "Fiz.ids.testRS = " +
         		"new Fiz.RatingSection(\"testRS\"", cr.getJsCode(true));
     }
     
     public void test_render_dataRequest_noRecord() {
         cr.addDataRequest("dataRequest", RawDataManager.newRequest(
                 new Dataset("message", "hello!")
         ));
         RatingSection rs = new RatingSection(new Dataset(
                 "numImages", "2",
                 "request", "dataRequest",
                 "unratedMessage", "Message is: @message"
         ));
         rs.render(cr);
         
         // Ensure data from a specified request was correctly retrieved and 
         // substituted.
         TestUtil.assertSubstring("Data from request found in HTML", 
                 "Message is: hello!", cr.getHtml().getBody().toString());
     }
     
     public void test_render_dataRequest_withRecord() {
         cr.addDataRequest("dataRequest", RawDataManager.newRequest(
                 new Dataset("record", new Dataset("message", "hello!"))
         ));
         RatingSection rs = new RatingSection(new Dataset(
                 "numImages", "2",
                 "request", "dataRequest",
                 "unratedMessage", "Message is: @message"
         ));
         rs.render(cr);
         
         // Ensure data from a specified request was correctly retrieved and 
         // substituted.
         TestUtil.assertSubstring("Data from request found in HTML", 
                 "Message is: hello!", cr.getHtml().getBody().toString());
     }
     
     public void test_render_userClass() {
         RatingSection rs = new RatingSection(new Dataset(
                 "numImages", "2",
                 "class", "MyClass"
         ));
         rs.render(cr);
         
         // Ensure that the specified class is used in the generated HTML.
         TestUtil.assertSubstring("class on main RatingSection <div>", 
                 "class=\"MyClass\"", cr.getHtml().getBody().toString());
         
         // Ensure that specifying a class caused no CSS to be included.
         assertEquals("Included CSS", "", cr.getHtml().getCssFiles());
     }
     
     public void test_render_generatedHtml_noDefaults() {
         cr.getMainDataset().set("message", "howdy!");
         RatingSection rs = new RatingSection(new Dataset(
                 "class", "MyClass",
                 "imageFamily", "/x/y/z.jpg",
                 "numImages", "2",
                 "unratedMessage", "Message is: @message"
         ));
         rs.render(cr);
         
         // Ensure the specified parameters occur throughout the generated HTML.
         String generatedHtml = cr.getHtml().getBody().toString();
         TestUtil.assertSubstring("imageFamily -off on <img> tags", 
                 "src=\"/x/y/z-off.jpg\"", generatedHtml);
         TestUtil.assertSubstring("imageFamily -on on <img> tags", 
                 "src=\"/x/y/z-on.jpg\"", generatedHtml);
         TestUtil.assertSubstring("unratedMessage in unratedMsg <div>", 
                 "Message is: howdy!", generatedHtml);
     }
     
     public void test_render_evaluatedJs_noDefaults() {
         cr.addDataRequest("dataRequest", RawDataManager.newRequest(
                 new Dataset("requestRating", "1.1")
         ));
         cr.getMainDataset().set("user", "tester");
         RatingSection rs = new RatingSection(new Dataset(
                 "ajaxUrl", "/x/ajaxY?user=@user",
                 "autoSizeText", "false",
                 "granularity", "0.4",
                 "initRating", "0.9",
                 "initRatingKey", "requestRating",
                 "numImages", "2",
                 "readOnly", "true",
                 "request", "dataRequest"
         ));
         rs.render(cr);
         
         // Ensure the specified parameters occur in the evaluated Javascript.
         assertEquals("Evaluated Javascript", 
                 "Fiz.auth = \"JHB9AM69@$6=TAF*J \";\n" +
                 "Fiz.ids.rating0 = new Fiz.RatingSection(\"rating0\", 2, " +
                     "0.4, 0.9, true, false, \"/x/ajaxY?user=tester\");\n", 
                 cr.getJsCode(true));
     }
     
     public void test_render_evaluatedJs_initRatingKey() {
         cr.addDataRequest("dataRequest", RawDataManager.newRequest(
                 new Dataset("requestRating", "1.1")
         ));
         RatingSection rs = new RatingSection(new Dataset(
                 "initRatingKey", "requestRating",
                 "numImages", "2",
                 "request", "dataRequest"
         ));
         rs.render(cr);
         
         // Ensure that initRatingKey is used to retrieve the initial rating.
         TestUtil.assertSubstring("initRatingKey used to retrieve rating", 
                 ", 1.1,", cr.getJsCode(true));
     }
 }
