 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.tools.text.transform;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.datatype.table.TableFactory;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 
 
 @Component(
         creator = "Lily Dong",
         description = "Converts token count to table.",
         name = "Token Count To Table",
         tags = "token, count, table, convert",
         firingPolicy = FiringPolicy.any,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/tools/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 
 public class TokenCountToTable extends AbstractExecutableComponent {
 	@ComponentInput(
 			description = "The token counts",
 			name = Names.PORT_TOKEN_COUNTS)
 	public final static String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	@ComponentInput(
 			description = "The TableFactory object",
 			name = Names.PROP_TABLE_FACTORY)
 	public final static String IN_TABLE_FACTORY = Names.PROP_TABLE_FACTORY;
 
 	@ComponentOutput(
 			description = "Output Table object.",
 			name = Names.PROP_TABLE)
 	public final static String OUT_TABLE = Names.PROP_TABLE;
 
 	private boolean _gotInitiator;
 	private TableFactory _fact;
 	private ExampleTable _termTable;
 	private HashMap<String, Integer> m_gtm = new HashMap<String, Integer>();
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		_gotInitiator = false;
 		_fact = null;
 		_termTable = null;
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 
 		if (cc.isInputAvailable(IN_TABLE_FACTORY)) {
 
 			_fact = (TableFactory)cc.getDataComponentFromInput(
 					IN_TABLE_FACTORY);
 			_termTable = _fact.createTable().toExampleTable();
 
 			_termTable.addRows(2); //for table viewer using jQuery
 		}
 
 		if (cc.isInputAvailable(IN_TOKEN_COUNTS)) {
 
 			Map<String, Integer> map = DataTypeParser.parseAsStringIntegerMap(
 					cc.getDataComponentFromInput(IN_TOKEN_COUNTS));
 			if(map.size() > 0) {
 				int row = _termTable.getNumRows();
 				_termTable.addRows(1);
 				for(String key: map.keySet()) {
 					Integer count = map.get(key);
 
 					Integer colobj = m_gtm.get(key);
 					int col = 0; //find the column
 					boolean flag = false;
 					if (colobj == null) {
 						col = _termTable.getNumColumns();
 						m_gtm.put(key, new Integer(col));
 						flag = true;
 					} else {
 						col = colobj.intValue();
 					}
 
 					// set the value in the table
 					_termTable.setInt(count.intValue(), row, col);
 
 					if (flag) {
 						_termTable.setColumnLabel(key, col);
 					}
 				}
 			}
 
 			if (!_gotInitiator) {
 			    cc.pushDataComponentToOutput(OUT_TABLE, _termTable);
 			    m_gtm.clear();
 			}
 		}
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
 	protected void handleStreamInitiators() throws Exception {
 
 		if (_gotInitiator)
 			throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");
 
 		m_gtm = new HashMap<String, Integer>();
 	    _gotInitiator = true;
 	}
 
 	@Override
     protected void handleStreamTerminators() throws Exception {
 
 		if (!_gotInitiator)
 			throw new Exception("Received StreamTerminator without receiving StreamInitiator");
 
 		componentContext.pushDataComponentToOutput(OUT_TABLE, new StreamInitiator());
 	    componentContext.pushDataComponentToOutput(OUT_TABLE, _termTable);
 	    componentContext.pushDataComponentToOutput(OUT_TABLE, new StreamTerminator());
 
 		m_gtm.clear();
 		_gotInitiator = false;
 	}
 }
