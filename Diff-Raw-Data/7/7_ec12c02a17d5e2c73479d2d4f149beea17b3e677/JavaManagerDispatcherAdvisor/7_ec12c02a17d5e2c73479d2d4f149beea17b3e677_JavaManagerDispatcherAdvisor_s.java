 /* 
  * Copyright (c) 2010  Jonathan Alvarsson <jonalv@users.sourceforge.net>
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package net.bioclipse.bslrunner.mocks;
 
 import org.aopalliance.aop.Advice;
 
 import net.bioclipse.core.api.managers.IJavaManagerDispatcherAdvisor;
 
 
 /**
  * @author jonalv
  *
  */
 public class JavaManagerDispatcherAdvisor 
        implements IJavaManagerDispatcherAdvisor {
 
     @Override
     public Advice getAdvice() {
        throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean isPerInstance() {
        throw new UnsupportedOperationException();
     }
 }
