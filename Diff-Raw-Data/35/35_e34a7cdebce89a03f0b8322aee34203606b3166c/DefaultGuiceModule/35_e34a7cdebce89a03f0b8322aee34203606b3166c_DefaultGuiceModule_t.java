 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.module;
 
 import com.github.sebhoss.contract.verifier.ContractModule;
 import com.github.sebhoss.contract.verifier.JuelModule;
 import com.github.sebhoss.contract.verifier.ParanamerModule;
import com.github.sebhoss.contract.verifier.impl.EnglishErrorMessagesModule;
 import com.github.sebhoss.contract.verifier.impl.SyntaxCheckModule;
 import com.google.inject.AbstractModule;
 
 /**
  * Guice module for the default behavior for annotated contracts. Uses JUEL and Paranamer for contract checking.
  */
 public class DefaultGuiceModule extends AbstractModule {
 
     @Override
     protected void configure() {
         install(new JuelModule());
         install(new ParanamerModule());
         install(new SyntaxCheckModule());
         install(new ContractModule());
        install(new EnglishErrorMessagesModule());
     }
 
 }
