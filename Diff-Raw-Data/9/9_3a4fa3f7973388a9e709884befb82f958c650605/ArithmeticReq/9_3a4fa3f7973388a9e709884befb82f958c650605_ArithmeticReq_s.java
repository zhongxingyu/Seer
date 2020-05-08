 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.arithmetic.requestor;
 
 import sorcer.arithmetic.provider.Adder;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.service.*;
 import sorcer.service.Strategy.Monitor;
 import sorcer.service.Strategy.Wait;
 
 import java.io.File;
 
 import static sorcer.eo.operator.*;
 
 public class ArithmeticReq extends ServiceRequestor {
 
 	/* (non-Javadoc)
 	 * @see sorcer.core.requestor.ServiceRequestor#getExertion(java.lang.String[])
 	 */
 	@Override
 	public Exertion getExertion(String... args) throws ExertionException {
 		try {
 			if (getProperty("exertion.filename") != null)
 				exertion = (Exertion)evaluate(new File(getProperty("exertion.filename")));
 			else
 				exertion = createExertion(args);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ExertionException(e);
 		} 
 		
 		return exertion;
 	}
 
 	public Exertion createExertion(String... args) throws ExertionException,
 			SignatureException, ContextException {
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
 						out("result/y", null)), strategy(Monitor.NO, Wait.YES));
 		return f5;
 	}
 	
	public void postprocess(String... args) throws ContextException {
 		super.postprocess();
 		logger.info("<<<<<<<<<< f5 context: \n" + ((Job)exertion).getExertion("f5").getContext());
 	}
 
 }
