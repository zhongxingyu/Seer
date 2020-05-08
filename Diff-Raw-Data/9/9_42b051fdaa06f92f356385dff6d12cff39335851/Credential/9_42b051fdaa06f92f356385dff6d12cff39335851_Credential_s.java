 /*
  * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.security.credential;
 
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.Collection;
 
 import javax.crypto.SecretKey;
 
 /**
  * A credential for an entity.  Local entity credentials will usually contain a private key while 
  * peer credentails will normally contain only a public key.
  */
 public interface Credential {
     
     /**
      * The unique ID of the entity this credential is for.
      * 
      * @return unique ID of the entity this credential is for
      */
     public String getEntityId();
     
     /**
      * Gets usage type of this credential.
      * 
      * @return usage type of this credential
      */
     public UsageType getUsageType();
     
     /**
      * Gets key names for this credential.  These names may be used to reference a key(s) exchanged 
      * through an out-of-band aggreement.  Implementations may or may not implement means to resolve 
      * these names into keys retrievable through the {@link #getPublicKeys()} or {@link #getPrivateKey()} 
      * methods.
      * 
      * @return key names for this credential
      */
     public Collection<String> getKeyNames();
 
     /**
      * Gets the public keys for the entity.
      * 
      * @return public keys for the entity
      */
     public Collection<PublicKey> getPublicKeys();
     
     /**
      * Gets the secret key for this entity.
      * 
      * @return secret key for this entity
      */
    public SecretKey getSecretyKey();
 
     /**
      * Gets the private key for the entity if there is one.
      * 
      * @return the private key for the entity
      */
     public PrivateKey getPrivateKey();
 }
