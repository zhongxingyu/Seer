 package de.fuberlin.wiwiss.ng4j.swp.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.Signature;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Iterator;
 import java.util.Map;
 
 import sun.misc.BASE64Decoder;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.NiceIterator;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.ng4j.swp.SWPAuthority;
 import de.fuberlin.wiwiss.ng4j.swp.SWPWarrant;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPCertificateException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPNoSuchAlgorithmException;
 import de.fuberlin.wiwiss.ng4j.swp.exceptions.SWPSignatureException;
 import de.fuberlin.wiwiss.ng4j.swp.util.SWPSignatureUtilities;
 import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;
 
 public class SWPWarrantImpl implements SWPWarrant
 {
 
 	private NamedGraph warrant;
 	private NamedGraphSet local = new NamedGraphSetImpl();
 	
 	public SWPWarrantImpl( NamedGraph graph )
 	{
 		warrant = graph;
 		local.addGraph( warrant );
 	}
 	
 	public ExtendedIterator getGraphs() 
 	{
 		String warrantQuery = "SELECT * WHERE ?warrant (?graph ?p ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator itr = TriQLQuery.exec( local, warrantQuery );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return itr.hasNext();
 			}
 
 			public Object next() 
 			{
 				Map results =  ( Map ) itr.next();
 				Node graphURI = ( Node ) results.get( "graph" );	
 				return graphURI.getURI();
 			}
         };
 	}
 
 	public ExtendedIterator getAssertedGraphs() 
 	{
 		String warrantQuery = "SELECT * WHERE ?warrant (?graph swp:assertedBy ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator itr = TriQLQuery.exec( local, warrantQuery );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return itr.hasNext();
 			}
 
 			public Object next() 
 			{
 				Map results =  ( Map ) itr.next();
 				Node graphURI = ( Node ) results.get( "graph" );
 				return graphURI.getURI();
 			}
         };
 	}
 
 	public ExtendedIterator getQuotedGraphs() 
 	{
 		String warrantQuery = "SELECT * WHERE ?warrant (?graph swp:quotedBy ?warrant) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
         final Iterator itr = TriQLQuery.exec( local, warrantQuery );
 		
         return new NiceIterator()
         {
 			
 			public boolean hasNext() 
 			{
 				return itr.hasNext();
 			}
 
 			public Object next() 
 			{
 				Map results =  ( Map ) itr.next();
 				Node graphURI = ( Node ) results.get( "graph" );
 				return graphURI.getURI();
 			}
         };
 	}
 
 	public SWPAuthority getAuthority() throws SWPCertificateException 
 	{
 		SWPAuthority authority = new SWPAuthorityImpl();
		String query = "SELECT * WHERE <"+warrant.getGraphName().getURI()+"> (<"+warrant.getGraphName().getURI()+"> swp:authority ?authority . ?authority swp:X509Certificate ?certificate) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		Iterator itr = TriQLQuery.exec( local, query );
 		if ( itr.hasNext() )
 		{
 			
 			X509Certificate certificate = null;
 			Map results =  ( Map ) itr.next();
			Node auth = ( Node ) results.get( "authority" );
 			Node cert = ( Node ) results.get( "certificate" );
 			String certs = "-----BEGIN CERTIFICATE-----\n" +
 							cert.getLiteral().getLexicalForm() + 
 							"\n-----END CERTIFICATE-----";
 			authority.setID( auth );
 				
 			try 
 			{
 				CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
 				certificate = ( X509Certificate ) cf.generateCertificate( new ByteArrayInputStream( certs.getBytes() ) );
 			} 
 			catch ( CertificateException e ) 
 			{
 				throw new SWPCertificateException( "Error reading X509 Certificate PEM from Warrant graph." );
 			}
 	    		
 			authority.setCertificate( certificate );
 			
 		}
 		//else throw new SWPAuthorityNotFoundException();
 		
 		return authority;
 	}
 
 	public Signature getSignature() throws SWPSignatureException 
 	{
 		Signature sig = null;
 		byte[] signature = null;
 		String query = "SELECT * WHERE (<"+warrant.getGraphName().getURI()+"> swp:signature ?signature) (<"+warrant.getGraphName().getURI()+"> swp:signatureMethod ?smethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		Iterator itr = TriQLQuery.exec( local, query );
 		if ( itr.hasNext() )
 		{
 			Map results =  ( Map ) itr.next();
 			Node sigValue = ( Node ) results.get( "signature" );
 			Node sigMethod = ( Node ) results.get( "smethod" );
 			
         	try 
 			{
 				BASE64Decoder decoder = new BASE64Decoder();
 				signature = decoder.decodeBuffer( sigValue.getLiteral().getLexicalForm() );
 				sig = SWPSignatureUtilities.getSignatureAlgorithm( sigMethod );
 			} 
 			catch ( IOException e ) 
 			{
 				throw new SWPSignatureException( "Error occured decoding signature value from Warrant graph." );
 			} 
 			catch ( SWPNoSuchAlgorithmException e ) 
 			{
 				
 				throw new SWPSignatureException( e.getMessage() );
 			}
 		}
 		else return null;
 		
 		return sig;
 	}
 
 	public boolean isSigned() 
 	{
 		boolean result = false;
 		String query = "SELECT * WHERE (<"+warrant.getGraphName().getURI()+"> swp:signature ?signature) (<"+warrant.getGraphName().getURI()+"> swp:authority ?authority) (<"+warrant.getGraphName().getURI()+"> swp:signatureMethod ?smethod) USING swp FOR <http://www.w3.org/2004/03/trix/swp-2/>";
 		Iterator itr = TriQLQuery.exec( local, query );
 		if ( itr.hasNext() )
 		{
 			Map results =  ( Map ) itr.next();
 			Node sigValue = ( Node ) results.get( "signature" );
 			Node sigMethod = ( Node ) results.get( "smethod" );
 			Node authority = ( Node ) results.get( "authority" ); 
 			if ( ( sigValue != null ) & ( sigMethod != null ) & ( authority != null ) )
 				result = true;
 		}
 		return result;
 	}
 }
 
 /*
  *  (c)   Copyright 2004, 2005 Rowland Watkins (rowland@grid.cx) & Chris Bizer (chris@bizer.de)
  *   	  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
