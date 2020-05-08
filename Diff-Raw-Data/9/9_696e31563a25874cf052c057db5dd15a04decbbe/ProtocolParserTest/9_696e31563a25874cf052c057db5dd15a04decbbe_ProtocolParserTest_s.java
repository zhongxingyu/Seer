 /*
  * Copyright 2009 Scribble.org
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
  *
  */
 package org.scribble.protocol.parser.ctk;
 
 import static org.junit.Assert.*;
 
 import java.util.Comparator;
 
 import org.scribble.core.logger.*;
 import org.scribble.core.model.*;
 import org.scribble.protocol.model.*;
 import org.scribble.protocol.parser.ctk.comparators.NamespaceComparator;
 
 public class ProtocolParserTest {
 	
 	private static java.util.Map<Class<? extends ModelObject>,Comparator<ModelObject>> m_comparators=
 			new java.util.HashMap<Class<? extends ModelObject>,Comparator<ModelObject>>();
 	
 	static {
 		m_comparators.put(Namespace.class, new NamespaceComparator());
 	}
 	
 	public Model<Protocol> getModel(String filename, ScribbleLogger logger) {
 		Model<Protocol> ret=null;
 		
 		java.io.InputStream is=
 				ClassLoader.getSystemResourceAsStream("tests/"+
 							filename+".spr");
 		
 		if (is == null) {
 			fail("Failed to load protocol '"+filename+"'");
 		}
 		
 		org.scribble.protocol.parser.ProtocolParser parser=null;
 		
 		try {
 			String clsName=System.getProperty("scribble.protocol.parser");
 			
 			if (clsName == null) {
 				clsName = "org.scribble.protocol.parser.antlr.ANTLRProtocolParser";
 			}
 			
 			Class<?> cls=Class.forName(clsName);
 			
 			parser = (org.scribble.protocol.parser.ProtocolParser)
 								cls.newInstance();
 
 		} catch(Exception e) {
 			fail("Failed to get Protocol parser: "+e);
 		}
 
 		ret = parser.parse(is, logger);
 		
 		return(ret);
 	}
 
 	/**
 	 * This method validates a model against the expected model.
 	 * 
 	 * @param model The model constructed by the parser
 	 * @param expected The expected model
 	 */
 	public void verify(Model<Protocol> model, Model<Protocol> expected) {
 		java.util.List<ModelObject> mlist=sequence(model);
 		java.util.List<ModelObject> elist=sequence(expected);
 		
 		assertNotNull(mlist);
 		assertNotNull(elist);
 		
 		int len=mlist.size();
 		
 		if (len > elist.size()) {
 			len = elist.size();
 		}
 		
 		for (int i=0; i < len; i++) {
 			if (mlist.get(i).getClass() != elist.get(i).getClass()) {
 				fail("Element ("+i+") mismatch class model="+
 						mlist.get(i).getClass()+" expected="+
 						elist.get(i).getClass());
 			} else {
 				Comparator<ModelObject> comparator=
 					m_comparators.get(mlist.get(i).getClass());
 				
 				if (comparator != null &&
 						comparator.compare(mlist.get(i), elist.get(i)) != 0) {
 					fail("Element ("+i+") did not match: "+mlist.get(i)+
 							" expected="+elist.get(i));
 				}
 			}
 		}
 		
 		assertTrue(mlist.size() == elist.size());
 	}
 	
 	/**
 	 * This method converts the model tree into a flat list of model
 	 * objects which can be compared.
 	 * 
 	 * @param model The model
 	 * @return The list of model objects
 	 */
 	protected java.util.List<ModelObject> sequence(Model<Protocol> model) {
 		final java.util.List<ModelObject> ret=new java.util.Vector<ModelObject>();
 		
 		model.visit(new Visitor() {
 
 			@Override
 			public void conclude(ModelObject obj) {
 			}
 
 			@Override
 			public void prepare(ModelObject obj) {
 			}
 
 			@Override
 			public boolean visit(ModelObject obj) {
 				ret.add(obj);
 				return true;
 			}
 			
 		});
 		
 		return(ret);
 	}
 	
 	@org.junit.Test
 	public void testSingleInteraction() {
 		TestScribbleLogger logger=new TestScribbleLogger();
 		
 		Model<Protocol> model=getModel("SingleInteraction", logger);
 		
 		assertNotNull(model);
 		
 		assertTrue(logger.getErrorCount() == 0);
 		
 		// Build expected model
 		Model<Protocol> expected=new Model<Protocol>();
 		
 		Namespace ns=new Namespace();
		ns.setName("example.helloworld1");
 		expected.setNamespace(ns);
 		
 		Protocol protocol=new Protocol();
 		expected.setDefinition(protocol);
 		
 		LocatedName ln=new LocatedName();
 		ln.setName("SingleInteraction");
 		protocol.setLocatedName(ln);
 		
 		RoleList rl=new RoleList();
 		Role buyer=new Role();
 		buyer.setName("buyer");
 		rl.getRoles().add(buyer);
 		Role seller=new Role();
 		seller.setName("seller");
 		rl.getRoles().add(seller);
 		
 		protocol.getBlock().getContents().add(rl);
 		
 		Interaction interaction=new Interaction();
 		
 		MessageSignature ms=new MessageSignature();
 		TypeReference tref=new TypeReference();
 		tref.setLocalpart("Order");
 		ms.getTypes().add(tref);
 		interaction.setMessageSignature(ms);
 		interaction.setFromRole(buyer);
 		interaction.setToRole(seller);
 		
 		protocol.getBlock().getContents().add(interaction);
 		
 		verify(model, expected);
 	}
 }
