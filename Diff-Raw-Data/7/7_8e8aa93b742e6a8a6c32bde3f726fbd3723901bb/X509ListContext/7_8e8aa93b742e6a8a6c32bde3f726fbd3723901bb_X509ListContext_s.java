 /*
  * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.codeartisans.qipki.ca.application.contexts.x509;
 
 import java.io.StringReader;
 import java.security.cert.X509Certificate;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 
 import org.codeartisans.qipki.ca.domain.ca.CA;
 import org.codeartisans.qipki.ca.domain.ca.CARepository;
 import org.codeartisans.qipki.ca.domain.x509.X509;
 import org.codeartisans.qipki.ca.domain.x509.X509Factory;
 import org.codeartisans.qipki.ca.domain.x509.X509Repository;
 import org.codeartisans.qipki.ca.domain.x509profile.X509Profile;
 import org.codeartisans.qipki.ca.domain.x509profile.X509ProfileRepository;
 import org.codeartisans.qipki.ca.presentation.rest.resources.WrongParametersBuilder;
 import org.codeartisans.qipki.crypto.io.CryptIO;
 import org.codeartisans.qipki.core.dci.Context;
 
 import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
 import org.qi4j.api.query.Query;
 import org.qi4j.api.unitofwork.NoSuchEntityException;
 
 public class X509ListContext
         extends Context
 {
 
    @This
    private X509ListContext composite;
     @Service
     private CryptIO cryptIO;
 
     public Query<X509> list( int start )
     {
         return context.role( X509Repository.class ).findAllPaginated( start, 25 );
     }
 
     public X509 createX509( String caIdentity, String x509ProfileIdentity, PKCS10CertificationRequest pkcs10 )
     {
         try {
             CA ca = context.role( CARepository.class ).findByIdentity( caIdentity );
             X509Profile x509Profile = context.role( X509ProfileRepository.class ).findByIdentity( x509ProfileIdentity );
             X509Certificate cert = ca.sign( x509Profile, pkcs10 );
             X509 x509 = context.role( X509Factory.class ).create( cert, ca, x509Profile );
             return x509;
 
         } catch ( NoSuchEntityException ex ) {
             throw new WrongParametersBuilder().title( "Invalid CA identity: " + caIdentity ).build( ex );
         }
     }
 
     public X509 createX509( String caIdentity, String x509ProfileIdentity, String pkcs10PEM )
     {
        return composite.createX509( caIdentity, x509ProfileIdentity, cryptIO.readPKCS10PEM( new StringReader( pkcs10PEM ) ) );
     }
 
     public X509 findByHexSha256( String hexSha256 )
     {
         return context.role( X509Repository.class ).findByHexSha256( hexSha256 );
     }
 
 }
