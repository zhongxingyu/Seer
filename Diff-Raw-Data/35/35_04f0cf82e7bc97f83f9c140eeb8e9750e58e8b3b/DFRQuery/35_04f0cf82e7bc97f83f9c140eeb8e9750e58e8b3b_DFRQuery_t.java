 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.jstor.io;
 
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.seasr.meandre.support.generic.io.IOUtils;
 import org.seasr.meandre.support.generic.jstor.DFRNamespaceContext;
 import org.w3c.dom.Document;
 
 /**
  * Performs a query against the JSTOR 'Data For Research' data
  * API available at: http://dfr.jstor.org/api
  *
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
        description = "This component performs a query against the JSTOR 'Data For Research' data. " +
        		"The format of the query conforms to CQL (Contextual Query Language), a standard syntax " +
        		"for representing queries. A formal definition of CQL can be found on the " +
        		"<a href='http://www.loc.gov/standards/sru/specs/cql.html'>Library of Congress</a> web site.<br><br>" +
        		"<h4>Searchable fields:</h4>" +
        		"<p><ul>" +
        		"<li><b>dc.creator</b> - Article author(s)</li>" +
        		"<li><b>dc.date</b> - Searches the articles date of publication, format='YYYY-MM-DDT00:00:00Z'</li>" +
        		"<li><b>dc.description</b> - Searches article abstract, if available</li>" +
        		"<li><b>dc.identifier</b> - Article UID</li>" +
        		"<li><b>dc.language</b> - Searches language field using a <a href='http://www.loc.gov/standards/iso639-2/php/code_list.php'>ISO 639-2 3 digit language code</a></li>" +
        		"<li><b>dc.publisher</b> - Refer to <a href='http://dfr.jstor.org/?fc=pum2'>Publisher facet</a> on DfR Explore page for publisher names</li>" +
        		"<li><b>dc.subject</b> - Searches auto-extracted keywords associated with article</li>" +
        		"<li><b>dc.title</b> - Article title</li>" +
        		"<li><b>jstor.articletype</b> - JSTOR article type, recognized values are 'research-article', 'book-review', 'misc', 'news', and 'editorial'</li>" +
        		"<li><b>jstor.discipline</b> - Refer to <a href='http://dfr.jstor.org/?fc=dim1'>Discipline facet</a> on DfR Explore page for discipline names</li>" +
        		"<li><b>jstor.journaltitle</b> - Refer to <a href='http://dfr.jstor.org/?fc=snm2'>Journal facet</a> facet on DfR Explore page for journal titles</li>" +
        		"<li><b>jstor.text</b> - Searches everything</li>" +
        		"</ul></p>" +
        		"<h4>Sample queries:</h4>" +
        		"<p><ul>" +
        		"<li>Search for documents in 'Education' discipline: <code>jstor.discipline='Education'</code></li>" +
        		"<li>Search for documents with keyterm 'space': <code>dc.subject=space</code></li>" +
        		"<li>Search for documents with publisher 'Springer': <code>dc.publisher=springer</code></li>" +
        		"<li>Search for documents with term 'jefferson' anywhere: <code>jstor.text=jefferson</code></li>" +
        		"</ul></p>",
         name = "JSTOR DFR Query",
         tags = "jstor, dfr, data for research",
         rights = Licenses.UofINCSA,
         mode = Mode.compute,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class DFRQuery extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_QUERY,
             description = "The query" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
     )
     protected static final String PORT_QUERY = Names.PORT_QUERY;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_XML,
             description = "The JSTOR DFR XML response" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_RESPONSE_XML = Names.PORT_XML;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             defaultValue = "100",
             description = "Maximum number of records to retrieve (set to -1 to retrieve all records)",
             name = Names.PROP_MAX_SIZE
     )
     protected static final String PROP_MAX_RECORDS = Names.PROP_MAX_SIZE;
 
     //--------------------------------------------------------------------------------------------
 
     protected static final String JSTOR_DFR_QUERY_URL = "http://dfr.jstor.org/sru/?operation=searchRetrieve&version=1.1&query=";
 
     private int _maxRecords;
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _maxRecords = Integer.parseInt(ccp.getProperty(PROP_MAX_RECORDS));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String inQuery = DataTypeParser.parseAsString(cc.getDataComponentFromInput(PORT_QUERY))[0].trim();
         if (inQuery.length() == 0) {
             cc.pushDataComponentToOutput(OUT_ERROR,
                     String.format("The query string cannot be empty - please pass a query string to the '%s' input!", PORT_QUERY));
             return;
         }
 
         inQuery = URLEncoder.encode(inQuery, "UTF-8");
 
         String sQuery = String.format("%s%s&maximumRecords=", JSTOR_DFR_QUERY_URL, inQuery);
 
         URL queryURL = new URL(sQuery + "1");
         Document doc = DOMUtils.createDocument(queryURL.openStream());
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(new DFRNamespaceContext());
 
         XPathExpression exprNRecords = xpath.compile("/ns1:searchRetrieveResponse/ns1:numberOfRecords/text()");
         int nRecords = ((Double) exprNRecords.evaluate(doc, XPathConstants.NUMBER)).intValue();
 
         console.fine("Available records: " + nRecords);
 
         if (nRecords > 0) {
             if (_maxRecords == -1)
                 _maxRecords = nRecords;
 
             queryURL = new URL(sQuery + Math.min(nRecords, _maxRecords));
 
             console.fine("Query URL set to: " + queryURL);
 
             String xml = IOUtils.getTextFromReader(new InputStreamReader(queryURL.openStream()));
 
             console.finest(xml);
 
             cc.pushDataComponentToOutput(OUT_RESPONSE_XML, BasicDataTypesTools.stringToStrings(xml));
         } else
             cc.pushDataComponentToOutput(OUT_ERROR, "Your query did not return any results");
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
 }
