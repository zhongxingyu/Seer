 /* Copyright (c) 2008-2010 Stanford University
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
 
 package org.fiz.section;
 
 import org.fiz.*;
 import java.util.*;
 
 import ys.wikiparser.WikiParser;
 
 /**
  * The MarkupLayout class is a layout where the format string is wiki markup,
  * based on the Creole wiki standard. See <a href="link http://wikicreole.org/">
  * the official creole website</a> for a full specification. Template
  * substitution is used to substitute values into the layout, using the same
 * &#064; convention. See {@link Template} for more information.
  *
  * This class supports the same properties as the Layout class, though the
  * {@code format} is interpreted as markup rather than HTML.
  */
 public class MarkupLayout extends Layout{
     /**
      * Constructs a MarkupLayout object with the given set of properties.
      * @param properties      A collection of values describing the
      *                        configuration of the section; see above and the
      *                        Layout class for supported values.
      */
     public MarkupLayout(Dataset properties) {
         super(properties);
     }
 
     /**
      * Generates HTML for the the MarkupLayout, using properties passed to the
      * constructor.
      * @param cr             Overall information about the client
      *                       request being serviced.
      */
     public void render(ClientRequest cr) {
         String format = findFormat(cr);
         String rendered = WikiParser.renderXHTML(format);
         Template.appendToClientRequest(cr, rendered, getData(cr));
     }
 }
