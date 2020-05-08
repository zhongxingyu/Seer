 package org.tdom;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 import static org.junit.Assert.*;
 
 import static org.tdom.TDom.*;
 
 public class BasicTest
 {
     private final void check(TNode base, String expected)
     {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         base.dump(pw);
         pw.close();
         assertEquals(expected, sw.toString());
     }
 
     @Test public void testConstruction()
     {
         TNode html =
             n("html",
               n("head",
                 n("link",
                   a("type", "stylesheet"),
                   a("href", "http://example.com/a.css"))),
               n("body",
                 n("p", t("first paragraph")),
                 n("p", t("second paragraph"))));
 
         check(html,
               "<html><head>"+
               "<link type=\"stylesheet\" href=\"http://example.com/a.css\" />"+
               "</head>"+
               "<body><p>first paragraph</p><p>second paragraph</p></body>"+
               "</html>");
     }
 
     @Test public void testInserts()
     {
         TNode content =
             n("div",
               n("p", t("first paragraph")),
               n("p", t("second paragraph")));
 
         content.append(n("p", t("third paragraph")));
         check(content,
               "<div>"+
               "<p>first paragraph</p>"+
               "<p>second paragraph</p>"+
               "<p>third paragraph</p>"+
               "</div>");
         content.prepend(n("p", t("zeroth paragraph")));
         check(content,
               "<div>"+
               "<p>zeroth paragraph</p>"+
               "<p>first paragraph</p>"+
               "<p>second paragraph</p>"+
               "<p>third paragraph</p>"+
               "</div>");
         content.select("p").nth(1).before
             (n("p", t("halfth paragraph")));
         check(content,
               "<div>"+
               "<p>zeroth paragraph</p>"+
               "<p>halfth paragraph</p>"+
               "<p>first paragraph</p>"+
               "<p>second paragraph</p>"+
               "<p>third paragraph</p>"+
               "</div>");
         content.select("p").last().after
             (n("p", t("last paragraph")));
         check(content,
               "<div>"+
               "<p>zeroth paragraph</p>"+
               "<p>halfth paragraph</p>"+
               "<p>first paragraph</p>"+
               "<p>second paragraph</p>"+
               "<p>third paragraph</p>"+
               "<p>last paragraph</p>"+
               "</div>");
     }
 
     @Test public void testSelection()
     {
         TNode content =
             n("div",
               n("p", a("class", "first"), t("first paragraph")),
               n("p", a("class", "second"), t("second paragraph")),
               n("p", a("id", "third_id"), t("third paragraph")));
 
         check(content.select("p"),
               "<p class=\"first\">first paragraph</p>"+
               "<p class=\"second\">second paragraph</p>"+
               "<p id=\"third_id\">third paragraph</p>");
 
         check(content.select("p.first"),
               "<p class=\"first\">first paragraph</p>");
         check(content.select(".first"),
               "<p class=\"first\">first paragraph</p>");
         check(content.select("#third_id"),
               "<p id=\"third_id\">third paragraph</p>");
         check(content.select("#nothing"), "");
         check(content.select("div"),
               "<div>"+
               "<p class=\"first\">first paragraph</p>"+
               "<p class=\"second\">second paragraph</p>"+
               "<p id=\"third_id\">third paragraph</p>"+
               "</div>");
     }
 
     @Test public void testDeletes()
     {
         TNode content =
             n("div",
               n("p", a("class", "first"), t("first paragraph")),
               n("p", a("class", "second"), t("second paragraph")),
               n("p", a("id", "third_id"), t("third paragraph")));
 
         content.remove("#third_id");
         check(content,
               "<div>"+
               "<p class=\"first\">first paragraph</p>"+
               "<p class=\"second\">second paragraph</p>"+
               "</div>");
 
         content.remove("#nothing");
         check(content,
               "<div>"+
               "<p class=\"first\">first paragraph</p>"+
               "<p class=\"second\">second paragraph</p>"+
               "</div>");
         content.select(".second").remove();
         check(content,
               "<div>"+
               "<p class=\"first\">first paragraph</p>"+
               "</div>");
     }
 
     @Test public void adHocTests()
     {
         TNode html =
             n("html",
               n("head",
                 n("link", a("type", "stylesheet"),
                   a("href", "http://example.com/a.css"))));
 
         TNode body =
               n("body",
                 n("h1",
                   t("Hello, world")),
                 n("div", a("class", "content")));
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "</div></body>");
 
         body.append
             (".content",
              n("div", a("class", "tile"), t("This is tile 0")));
 
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<div class=\"tile\">This is tile 0</div></div></body>");
 
         html.after("head", body);
         check(html,
               "<html><head>"+
               "<link type=\"stylesheet\" href=\"http://example.com/a.css\" />"+
               "</head>"+
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<div class=\"tile\">This is tile 0</div></div></body></html>");
 
         // Should not be able to reparent body.
         try {
             html.append(body);
             fail("Did not catch reparenting attempt");
         }
         catch (IllegalStateException ise) {
             // ok
         }
 
         html.before
             (".tile", n("h2", t("Tile header")));
 
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<h2>Tile header</h2><div class=\"tile\">"+
               "This is tile 0</div></div></body>");
 
         html.after
             (".tile", n("h2", a("class", "footer"), t("a footer")));
 
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<h2>Tile header</h2><div class=\"tile\">"+
               "This is tile 0</div><h2 class=\"footer\">a footer</h2>"+
               "</div></body>");
 
         html.before
             ("h2", n("p", a("class", "prefooter"), t("a pre-footer")));
 
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<p class=\"prefooter\">a pre-footer</p>"+
               "<h2>Tile header</h2><div class=\"tile\">This is tile 0</div>"+
               "<p class=\"prefooter\">a pre-footer</p>"+
               "<h2 class=\"footer\">a footer</h2></div></body>");
 
         check(html.select("p.prefooter"),
               "<p class=\"prefooter\">a pre-footer</p>"+
               "<p class=\"prefooter\">a pre-footer</p>");
 
         html.remove("p.prefooter");
 
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<h2>Tile header</h2><div class=\"tile\">This is tile 0</div>"+
               "<h2 class=\"footer\">a footer</h2></div></body>");
 
         html.remove("h2");
         check(body,
               "<body><h1>Hello, world</h1><div class=\"content\">"+
               "<div class=\"tile\">This is tile 0</div></div></body>");
 
         html.remove(".content");
         check(body,
               "<body><h1>Hello, world</h1></body>");
     }
 }
