 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 
 package org.mulgara.resolver.distributed.remote;
 
 import java.io.Serializable;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.jrdf.graph.ObjectNode;
 import org.jrdf.graph.PredicateNode;
 import org.jrdf.graph.SubjectNode;
 import org.jrdf.graph.Triple;
 import org.mulgara.query.TuplesException;
 import org.mulgara.query.rdf.TripleImpl;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.ResolverSession;
 import org.mulgara.resolver.spi.Statements;
 
 /**
  * Creates a small Set of statements that be be shipped across a network.
  *
  * @created 2007-04-23
  * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
  * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class ShortGlobalStatementSet extends AbstractSet<Triple> implements Serializable {
 
   /**
    * Allow newer compiled version of the stub to operate when changes
    * have not occurred with the class.
    * NOTE : update this serialVersionUID when a method or a public member is
    * deleted.
    */
   private static final long serialVersionUID = 896369800817370415L;
 
 
   /** Internally, use a list as this has a shorter serialization. */
   private ArrayList<Triple> data;
 
 
   /**
    * Builds the set of Triple data, containing globalized triples.
    * @param statements Contains the data for the set.  This must not contain duplicates.
    * @throws TuplesException There was an error accessing the statements.
    * @throws GlobalizeException Some of the statements could not be globalized.
    */
   ShortGlobalStatementSet(Statements statements, ResolverSession session) throws TuplesException, GlobalizeException {
     // build the array
     long rowCount = statements.getRowCount();
     assert rowCount < StatementSetFactory.WATER_MARK;
     data = new ArrayList<Triple>((int)rowCount);
     // populate the array
     statements.beforeFirst();
     while (statements.next()) {
       Triple t = new TripleImpl(
           (SubjectNode)session.globalize(statements.getSubject()),
           (PredicateNode)session.globalize(statements.getPredicate()),
           (ObjectNode)session.globalize(statements.getObject())
       );
      data.add(t);
     }
   }
 
   @Override
   public Iterator<Triple> iterator() {
     return data.iterator();
   }
 
   @Override
   public int size() {
     return data.size();
   }
 
 }
