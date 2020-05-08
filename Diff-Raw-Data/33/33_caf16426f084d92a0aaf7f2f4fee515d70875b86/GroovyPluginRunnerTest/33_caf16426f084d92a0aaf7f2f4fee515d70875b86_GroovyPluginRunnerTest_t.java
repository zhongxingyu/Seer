 /*
  * Copyright (c) 2005-2010 Grameen Foundation USA
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package org.example;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mifos.accounts.api.AccountService;
 import org.mifos.accounts.api.InvalidPaymentReason;
import org.mifos.accounts.api.TransactionImport;
import org.mifos.dto.domain.AccountReferenceDto;
import org.mifos.dto.domain.UserReferenceDto;
import org.mifos.dto.domain.PaymentTypeDto;
import org.mifos.dto.domain.AccountPaymentParametersDto;
import org.mifos.dto.domain.ParseResultDto;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class GroovyPluginRunnerTest {
     TransactionImport transactionImport;
     GroovyPluginRunner concreteImporter;
     @Mock
     AccountService accountService;
     @Mock
     AccountReferenceDto accountFromGlobalAccountNum;
     @Mock
     UserReferenceDto userReferenceDto;
     @Mock
     PaymentTypeDto paymentTypeDto;
 
     List<InvalidPaymentReason> noErrors = new ArrayList<InvalidPaymentReason>();
 
     private final int idFromGlobalAccountNumber = 2;
 
     /**
      * Would rather use {@link BeforeClass}, but this causes Mockito to throw an exception insisting that
      * "MockitoRunner can only be used with Junit 4.4 or higher."
      */
     @Before
     public void setUpBeforeMethod() throws Exception {
         concreteImporter = new GroovyPluginRunner();
         transactionImport = concreteImporter;
         transactionImport.setAccountService(accountService);
         transactionImport.setUserReferenceDto(userReferenceDto);
         when(accountService.validatePayment(any(AccountPaymentParametersDto.class))).thenReturn(noErrors);
         when(accountService.lookupLoanAccountReferenceFromGlobalAccountNumber(anyString())).thenReturn(
                 accountFromGlobalAccountNum);
         when(accountFromGlobalAccountNum.getAccountId()).thenReturn(idFromGlobalAccountNumber);
         when(paymentTypeDto.getName()).thenReturn("Groovy payment type");
         List<PaymentTypeDto> paymentTypeList = new ArrayList<PaymentTypeDto>();
         paymentTypeList.add(paymentTypeDto);
         when(accountService.getLoanPaymentTypes()).thenReturn(paymentTypeList);
     }
 
     /**
      * Would rather use {@link AfterClass}, but this causes Mockito to throw an exception insisting that
      * "MockitoRunner can only be used with Junit 4.4 or higher."
      */
     @After
     public void tearDownAfterMethod() {
         transactionImport = null;
         concreteImporter = null;
     }
 
     @Test
     public void successfulImport() throws Exception {
        /* TODO - this groovy plugin is broken (examplePlugin.groovy in .mifos/ is required for this plugin to work)
         String testDataFilename = this.getClass().getResource("/test.csv").getFile();
         ParseResultDto result = transactionImport.parse(new FileInputStream(testDataFilename));
         assertThat(result.getParseErrors().toString(), result.getParseErrors().size(), is(0));
         assertThat(result.getSuccessfullyParsedPayments().toString(), result.getSuccessfullyParsedPayments().size(), is(2));
        */
     }
 }
