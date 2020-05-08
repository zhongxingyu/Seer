 package net.metadata.openannotation.lorestore.util;
 import net.metadata.openannotation.lorestore.servlet.rdf2go.AbstractRDF2GoUpdateHandler;
 
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.RDF2Go;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.node.Node;
 import org.ontoware.rdf2go.model.node.Resource;
 import org.ontoware.rdf2go.model.node.URI;
 /* 
  * Based on 
  * https://github.com/ismriv/jsonld-rdf2go/blob/master/src/main/java/ie/deri/smile/jsonld/rdf2go/RDF2GoTripleCallback.java
  * 
  * Modified for lorestore to work with new JSONLD API
  * 
  * Copyright (c) 2012 Ismael Rivera
 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 import de.dfki.km.json.jsonld.JSONLDTripleCallback;
 
 public class OATripleCallback extends JSONLDTripleCallback {
     private Model model = RDF2Go.getModelFactory().createModel();
     protected final Logger LOG = Logger.getLogger(OATripleCallback.class);
     public void setModel(Model model){
         this.model = model;
     }
     public Model getModel() {
         return this.model;
     }
     
     @Override
     public void triple(String s, String p, String o, String graph) {
         if (s == null || p == null || o == null) {
             return;
         }
         model.addStatement(createNode(s), model.createURI(p), createNode(o));
     }
     
     private Resource createNode(String thing) {
         if (thing.startsWith("http") || thing.startsWith("urn")){
             return model.createURI(thing);
         } else if (thing.startsWith("_")){
             return model.createBlankNode(thing);
         }
         return null;
     }
     
     @Override
     public void triple(String s, String p, String value, String datatype,
             String language, String graph) {
         if (s == null || p == null || value == null) {
             return;
         }
         URI subject = model.createURI(s);
         URI predicate = model.createURI(p);
     
         Node object = null;
         if (language != null) {
                 object = model.createLanguageTagLiteral(value, language);
         } else if (datatype != null) {
                 object = model.createDatatypeLiteral(value, model.createURI(datatype));
         } else {
                 object = model.createPlainLiteral(value);
         }
     
         model.addStatement(subject, predicate, object);
     }
 
 }
