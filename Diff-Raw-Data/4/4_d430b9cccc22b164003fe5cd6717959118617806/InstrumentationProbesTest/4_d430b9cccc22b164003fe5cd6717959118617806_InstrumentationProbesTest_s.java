 /*
  * Copyright 2013 Charles University in Prague
  * Copyright 2013 Vojtech Horky
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cz.cuni.mff.d3s.spl.tests.probes.instrument;
 
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import cz.cuni.mff.d3s.spl.core.Data;
 import cz.cuni.mff.d3s.spl.core.impl.PlainBufferDataSource;
 import cz.cuni.mff.d3s.spl.instrumentation.CommonExtraArgument;
 import cz.cuni.mff.d3s.spl.instrumentation.ExtraArgumentsBuilder;
 import cz.cuni.mff.d3s.spl.probe.InstrumentationProbeControllerBuilder;
 import cz.cuni.mff.d3s.spl.probe.ProbeController;
 import cz.cuni.mff.d3s.spl.tests.TestUtils;
 import cz.cuni.mff.d3s.spl.tests.probes.AcceptOnlyOddSizes;
 import static cz.cuni.mff.d3s.spl.tests.TestUtils.assertSampleCount;
 
 public class InstrumentationProbesTest {
 	
 	private void activateAndWait(ProbeController ctl) {
 		ctl.activate();
 		
 		TestUtils.tryToSleep(21);
 	}
 	
 	private InstrumentationProbeControllerBuilder probeBuilder;
 	private Data data;
 	private Data dataOdd;
 	private Data dataEven;
 	private MyClassLoader myLoader;
 	
 	@Before
 	public void setupBuilder() {
 		probeBuilder = new InstrumentationProbeControllerBuilder("cz.cuni.mff.d3s.spl.tests.probes.instrument.Action#action");
 	}
 	
 	@Before
 	public void setupData() {
 		data = new PlainBufferDataSource(500);
 		dataOdd = new PlainBufferDataSource(500);
 		dataEven = new PlainBufferDataSource(500);
 	}
 	
 	@Before
 	public void setupClassLoader() {
 		myLoader = new MyClassLoader(new URL[0], this.getClass().getClassLoader());
 	}
 	
 	@After
 	public void removeInstrumentation() {
 		probeBuilder.get().deactivate();
 		
 		try {
 			Thread.sleep(21);
 		} catch (InterruptedException e) {
 			/* Silently ignore. */
 		}
 	}
 	
 	@Test
 	public void deactivationWorks() {
 		probeBuilder.forwardSamplesToDataSource(data);
 		
 		ProbeController probeCtl = probeBuilder.get();
 		
 		activateAndWait(probeCtl);
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(456);
 			a.action(i);
 		}
 		
 		assertSampleCount(100, data);
 		
 		probeCtl.deactivate();
 		TestUtils.tryToSleep(21);
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(456);
 			a.action(i);
 		}
 		
 		assertSampleCount(100, data);
 	}
 	
 	@Test
 	public void allRunsCollected() {
 		probeBuilder.forwardSamplesToDataSource(data);
 		
 		ProbeController probeCtl = probeBuilder.get();
 		
 		activateAndWait(probeCtl);
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(456);
 			a.action(i);
 		}
 		
 		assertSampleCount(100, data);
 	}
 	
 	@Test
 	public void invokeOnlyForOddIterations() {
 		probeBuilder.forwardSamplesToDataSource(data);
 		
 		probeBuilder.setInvocationFilter(new AcceptOnlyOddSizes(), CommonExtraArgument.METHOD_PARAM_1);
 		
 		ProbeController probeCtl = probeBuilder.get();
 		
 		activateAndWait(probeCtl);
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(120 + i);
 			a.action(i);
 		}
 		
 		assertSampleCount(50, data);
 	}
 	
 	@Test
 	public void splittingConsumer() {
 		ExtraArgumentsBuilder consumerArgs = new ExtraArgumentsBuilder();
 		consumerArgs.addField("list");
 		
 		probeBuilder.setConsumer(new SplitOddEven(dataOdd, dataEven), consumerArgs.get());
 		
 		ProbeController probeCtl = probeBuilder.get();
 		
 		activateAndWait(probeCtl);
 		
 		for (int i = 0; i < 101; i++) {
 			Action a = new Action(120 + i);
 			a.action(i);
 		}
 		
		assertSampleCount(51, dataOdd);
		assertSampleCount(50, dataEven);
 	}
 	
 	@Test
 	public void filteringByClassLoader() throws ReflectiveOperationException {
 		probeBuilder.forwardSamplesToDataSource(data);
 		
 		probeBuilder.setClassLoaderFilter(new AcceptOnlyMyClassLoader());
 		
 		ProbeController probeCtl = probeBuilder.get();
 		
 		activateAndWait(probeCtl);
 		
 		Class<?> actionClassDynamic = myLoader.loadClass("cz.cuni.mff.d3s.spl.tests.probes.instrument.Action");
 		
 		for (int i = 0; i < 100; i++) {
 			Runnable a = new Action(120 + i);
 			a.run();
 			
 			a = (Runnable) actionClassDynamic.newInstance();
 			a.run();
 		}
 		
 		assertSampleCount(100, data);
 	}
 }
