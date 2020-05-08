 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.verifier.impl;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.inject.Inject;
 
 import ch.qos.cal10n.IMessageConveyor;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 import com.github.sebhoss.contract.annotation.Clause;
 import com.github.sebhoss.contract.verifier.ContractExceptionFactory;
 
 /**
  * TODO: Write documentation!
  */
 public final class ReflectionBasedContractExceptionFactory implements ContractExceptionFactory {
 
     private final IMessageConveyor messages;
 
     /**
      * @param messages
      *            The cal10n message conveyor to use.
      */
     @Inject
     public ReflectionBasedContractExceptionFactory(final IMessageConveyor messages) {
         this.messages = messages;
     }
 
     @SuppressWarnings(CompilerWarnings.NLS)
     @Override
     public RuntimeException breachOfContract(final Clause clause) {
        final boolean needsStringConstructor = !clause.message().isEmpty();
 
         try {
             RuntimeException contractException;
 
             if (needsStringConstructor) {
                 final Constructor<? extends RuntimeException> constructors = clause.exception().getConstructor(
                        String.class);
                 contractException = constructors.newInstance(clause.message());
             } else {
                final Constructor<? extends RuntimeException> constructors = clause.exception().getConstructor();
                 contractException = constructors.newInstance();
             }
 
             if (contractException == null) {
                 throw new NullPointerException("No suitable constructor found!");
             }
 
             return contractException;
         } catch (final InstantiationException exception) {
             throw new IllegalArgumentException(messages.getMessage(ExceptionFactoryErrors.TYPE_IS_ABSTRACT));
         } catch (final IllegalAccessException exception) {
             throw new IllegalArgumentException(messages.getMessage(ExceptionFactoryErrors.NO_ACCESSIBLE_CONSTRUCTOR));
         } catch (final NoSuchMethodException exception) {
             if (needsStringConstructor) {
                 throw new IllegalArgumentException(
                         "The specified exception type has no String-constructor but a message was provided! Either create an appropriate constructor or delete the message from the contract clause. Clause: "
                                 + clause.toString());
             }
 
             throw new IllegalArgumentException(
                     "The specified exception type has no default constructor! Either create an appropriate constructor or try adding a message. Clase: "
                             + clause.toString());
         } catch (final SecurityException exception) {
             throw exception;
         } catch (final IllegalArgumentException exception) {
             throw exception;
         } catch (final InvocationTargetException exception) {
             throw new IllegalArgumentException("Exception while creating contract-exception: "
                     + exception.getLocalizedMessage());
         }
     }
 
 }
