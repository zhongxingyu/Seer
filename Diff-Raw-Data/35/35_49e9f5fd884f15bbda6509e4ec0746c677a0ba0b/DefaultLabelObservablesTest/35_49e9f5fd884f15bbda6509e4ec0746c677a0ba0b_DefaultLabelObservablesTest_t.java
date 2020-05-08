 /*
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
 package uk.co.iizuka.common.binding.kozo;
 
 import org.hobsoft.entangle.Observable;
 import org.hobsoft.entangle.ObservableListener;
 import org.hobsoft.symmetry.ui.Label;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import static org.junit.Assert.assertEquals;

import static uk.co.iizuka.common.binding.kozo.test.TestObservableListeners.mockObservableListener;
import static uk.co.iizuka.common.binding.kozo.test.TestObservableListeners.mockObservableListenerWithValueChanged;

 /**
  * Tests {@code DefaultLabelObservables}.
  * 
  * @author Mark Hobson
  * @version $Id: DefaultLabelObservablesTest.java 97729 2012-01-11 16:17:41Z mark@IIZUKA.CO.UK $
  * @see DefaultLabelObservables
  */
 @RunWith(JMock.class)
 public class DefaultLabelObservablesTest
 {
 	// fields -----------------------------------------------------------------
 	
 	private Mockery mockery = new JUnit4Mockery();
 	
 	private Label label;
 	
 	private DefaultLabelObservables observables;
 	
 	// public methods ---------------------------------------------------------
 	
 	@Before
 	public void setUp()
 	{
 		label = new Label();
 		observables = new DefaultLabelObservables(label);
 	}
 
 	// tests ------------------------------------------------------------------
 	
 	@Test
 	public void textGetValue()
 	{
 		label.setText("a");
 		
 		assertEquals("a", observables.text().getValue());
 	}
 	
 	@Test
 	public void textSetValue()
 	{
 		observables.text().setValue("a");
 		
 		assertEquals("a", label.getText());
 	}
 	
 	@Test
 	public void textAddObservableListenerReceivesEventWhenPropertyChanged()
 	{
 		Observable<String> text = observables.text();
 		text.addObservableListener(mockObservableListenerWithValueChanged(mockery, text, "", "a"));
 		
 		label.setText("a");
 	}
 	
 	@Test
 	public void textRemoveObservableListenerNoLongerReceivesEventWhenPropertyChanged()
 	{
 		Observable<String> text = observables.text();
 		ObservableListener<String> listener = mockObservableListener(mockery);
 		text.addObservableListener(listener);
 		text.removeObservableListener(listener);
 		
 		label.setText("a");
 	}
 }
