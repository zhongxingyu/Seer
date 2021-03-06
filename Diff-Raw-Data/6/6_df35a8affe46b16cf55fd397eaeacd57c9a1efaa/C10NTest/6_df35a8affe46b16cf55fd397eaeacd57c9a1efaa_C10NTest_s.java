 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  *
  */
 
 package c10n;
 
 import c10n.annotations.DefaultC10NAnnotations;
 import c10n.annotations.En;
 import c10n.share.util.RuleUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestRule;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 /**
  * <p>Created: 7/31/12 11:33 AM</p>
  *
  * @author rodion
  */
 public class C10NTest {
     @Rule
     public static TestRule tmpLocale = RuleUtils.tmpLocale();
     @Rule
     public static TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
 
     @Test
     public void parameterisationIsDisabledWhenRawFalseIsPresent() {
         C10N.configure(new DefaultC10NAnnotations());
         Messages msg = C10N.get(Messages.class);
         assertThat(msg.text("ignored"), is("{} {0} {hello}"));
     }
 
     interface Messages {
         @En(value = "{} {0} {hello}", raw = true)
         String text(String ignored);
     }
 }
