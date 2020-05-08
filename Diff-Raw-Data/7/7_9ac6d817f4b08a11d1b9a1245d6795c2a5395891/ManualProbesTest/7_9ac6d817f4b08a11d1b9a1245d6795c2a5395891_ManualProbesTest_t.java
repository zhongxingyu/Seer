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
 package cz.cuni.mff.d3s.spl.tests.probes.manual;
 
 import org.junit.Test;
 
 import cz.cuni.mff.d3s.spl.core.Data;
 import cz.cuni.mff.d3s.spl.core.impl.PlainBufferDataSource;
 import cz.cuni.mff.d3s.spl.probe.ManualProbeControllerBuilder;
 import cz.cuni.mff.d3s.spl.probe.ProbeController;
 import cz.cuni.mff.d3s.spl.tests.probes.AcceptOnlyOddSizes;
 import static cz.cuni.mff.d3s.spl.tests.TestUtils.assertSampleCount;
 
 public class ManualProbesTest {
 	
 	@Test
 	public void allRunsCollected() {
 		Data data = new PlainBufferDataSource(500);
 		
 		ManualProbeControllerBuilder probeConfig = new ManualProbeControllerBuilder("allRunsCollected");
 		probeConfig.forwardSamplesToDataSource(data);
 		
 		ProbeController probeCtl = probeConfig.get();
 		
 		Action.init(probeCtl.getProbe());
 		
 		// FIXME: probeCtl.activate(); ??
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(456);
 			a.action();
 		}
 		
 		assertSampleCount(100, data);
 	}
 	
 	@Test
 	public void invokeOnlyForOddSizes() {
 		Data data = new PlainBufferDataSource(500);
 		
 		ManualProbeControllerBuilder probeConfig = new ManualProbeControllerBuilder("invokeOnlyForOddSizes");
 		probeConfig.setInvocationFilter(new AcceptOnlyOddSizes());
 		probeConfig.forwardSamplesToDataSource(data);
 		
 		ProbeController probeCtl = probeConfig.get();
 		
 		Action.init(probeCtl.getProbe());
 		
 		// FIXME: probeCtl.activate(); ??
 		
 		for (int i = 0; i < 100; i++) {
 			Action a = new Action(120 + i);
 			a.action();
 		}
 		
		assertSampleCount(50, data);
 	}
 	
 	@Test
 	public void splittingConsumer() {
 		Data dataOdd = new PlainBufferDataSource(500);
 		Data dataEven = new PlainBufferDataSource(500);
 		
 		ManualProbeControllerBuilder probeConfig = new ManualProbeControllerBuilder("invokeOnlyForOddSizes");
 		probeConfig.setConsumer(new SplitOddEven(dataOdd, dataEven));
 		
 		ProbeController probeCtl = probeConfig.get();
 		
 		Action.init(probeCtl.getProbe());
 		
 		// FIXME: probeCtl.activate(); ??
 		
 		for (int i = 0; i < 101; i++) {
 			Action a = new Action(120 + i);
 			a.action();
 		}
 		
		assertSampleCount(50, dataOdd);
		assertSampleCount(51, dataEven);
 	}
 }
