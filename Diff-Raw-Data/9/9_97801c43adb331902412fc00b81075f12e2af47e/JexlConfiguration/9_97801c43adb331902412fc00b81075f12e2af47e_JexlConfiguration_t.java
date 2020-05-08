 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.verifier;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 
 import org.apache.commons.jexl2.JexlEngine;
 import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
 /**
 * Configuration for a JEXL based {@link ContractContextFactory}.
  */
@Configuration
 public class JexlConfiguration {
 
     /**
     * @return A JEXL based {@link ContractContextFactory}.
      */
     @Bean
     public ContractContextFactory contractContextFactory() {
         return new JEXLBasedContractContextFactory(engine());
     }
 
     /**
      * @return A new JexlEngine
      */
     @SuppressWarnings(CompilerWarnings.STATIC_METHOD)
     @Bean
     public JexlEngine engine() {
         return new JexlEngine();
     }
 
 }
