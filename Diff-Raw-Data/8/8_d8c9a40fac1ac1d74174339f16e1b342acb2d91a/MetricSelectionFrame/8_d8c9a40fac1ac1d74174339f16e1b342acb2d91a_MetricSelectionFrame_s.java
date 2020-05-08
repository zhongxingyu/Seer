 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.analysistoolsinterface;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JScrollPane;
 import javax.swing.JList;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.Rectangle;
 import java.awt.Dimension;
 
 /**
  * @author cfuller
  *
  */
 public class MetricSelectionFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	
 	static List<String> metrics;
 	static Map<String, String> metricLookupByName;
 	
 	ParameterSetupController psc;
 	JList parameterList;
 	
 	static final String METRICS_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/metrics.xml";
 	static final String METRIC_TAG_NAME = "metric";
 	static final String NAME_ATTR = "displayname";
 	static final String CLASS_ATTR = "class";
 	
 	static {
 		
 		metrics = new ArrayList<String>();
 		metricLookupByName = new HashMap<String, String>();
 		
 		populateFilterList();
 		
 	}
 	
 	public MetricSelectionFrame(ParameterSetupController pscIn) {
 		setPreferredSize(new Dimension(400, 300));
 		setBounds(new Rectangle(0, 22, 400, 300));
 				
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		this.psc = pscIn;
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GroupLayout groupLayout = new GroupLayout(getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
 		);
 		
 		parameterList = new JList();
 		parameterList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				String selected = (String) parameterList.getSelectedValue();
 				psc.addSelectedMetric(selected, metricLookupByName.get(selected));
 				setVisible(false);
 				dispose();
 			}
 		});
 		scrollPane.setViewportView(parameterList);
 		getContentPane().setLayout(groupLayout);
 		parameterList.setListData(metrics.toArray());
 	}
 	
 	protected static void populateFilterList() {
 		
         Document taskDoc = null;
 
        String taskURLString = ClassLoader.getSystemClassLoader().getResource(METRICS_XML_FILENAME).toString();
         
         try {
             taskDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskURLString);
         } catch (SAXException e) {
             LoggingUtilities.severe("Encountered exception while parsing filters xml file.");
             e.printStackTrace();
             return;
         } catch (ParserConfigurationException e) {
             LoggingUtilities.severe("Incorrectly configured xml parser.");
             e.printStackTrace();
             return;
         } catch (IOException e) {
             LoggingUtilities.severe("Exception while reading xml file.");
             e.printStackTrace();
             return;
         }
 
         NodeList tasks = taskDoc.getElementsByTagName(METRIC_TAG_NAME);
 
         for (int i = 0; i < tasks.getLength(); i++) {
             
             Node n = tasks.item(i);
 
             String filterName = n.getAttributes().getNamedItem(NAME_ATTR).getNodeValue();
 
             String filterClass = n.getAttributes().getNamedItem(CLASS_ATTR).getNodeValue();
 
             metricLookupByName.put(filterName, filterClass);
             
             metrics.add(filterName);
 
         }
     }
 	
 }
