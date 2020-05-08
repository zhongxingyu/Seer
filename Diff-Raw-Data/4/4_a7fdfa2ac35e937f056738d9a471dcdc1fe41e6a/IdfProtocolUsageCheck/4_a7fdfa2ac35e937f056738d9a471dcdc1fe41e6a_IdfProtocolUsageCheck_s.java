 /*
  * Copyright 2009-2013 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.checks.refs;
 
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.Check;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.MageTabCheck;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checker.annotation.Visit;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.Protocol;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.SdrfProtocolNode;
 
 import java.util.Set;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckModality.WARNING;
 
 /**
  * @author Olga Melnichuk
  */
 @MageTabCheck(
         ref = "REF01",
         value = "IDF should not contain protocol definitions that are not used in SDRF",
         modality = WARNING
 )
 public class IdfProtocolUsageCheck {
 
     private final Set<String> definedProtocols = newHashSet();
     private final Set<String> usedProtocols = newHashSet();
 
     @Visit
     public void visit(Protocol protocol) {
         definedProtocols.add(protocol.getName().getValue());
     }
 
     @Visit
     public void visit(SdrfProtocolNode protocolNode) {
         Protocol protocol = protocolNode.getProtocol();
        usedProtocols.add(protocol.getName().getValue());
     }
 
     @Check
     public void check() {
         assertThat(usedProtocols, is(definedProtocols));
     }
 }
