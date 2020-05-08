 //---------------------------------------------------------------------------
 // Copyright 2012 Ray Group International
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //---------------------------------------------------------------------------
 
 package com.raygroupintl.m.parsetree.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.raygroupintl.m.parsetree.Local;
 import com.raygroupintl.m.parsetree.visitor.APIRecorder;
 import com.raygroupintl.struct.Filter;
 import com.raygroupintl.struct.Indexed;
 
 public class Block {
 	private final static Logger LOGGER = Logger.getLogger(APIRecorder.class.getName());
 	private static Set<EntryId> reported = new HashSet<EntryId>();
 
 	private static class FaninList {
 		private Block block;
 		private List<IndexedBlock> faninBlocks = new ArrayList<IndexedBlock>();
 		private Set<Integer> existing = new HashSet<Integer>();
 		
 		public FaninList(Block block) {
 			this.block = block;
 		}
 				
 		public void addFanin(Block faninBlock, int index, List<Indexed<String>> byRefs) {
 			int faninId = System.identityHashCode(faninBlock);
 			if (faninId != System.identityHashCode(this.block)) {
 				if (! this.existing.contains(faninId)) {
 					IndexedBlock e = new IndexedBlock(index, faninBlock, byRefs);
 					this.faninBlocks.add(e);
 					this.existing.add(faninId);
 				}
 			}
 		}
 		
 		public List<IndexedBlock> getFaninBlocks() {
 			return this.faninBlocks;
 		}
 	}
 	
 	private static class FanoutBlocks {
 		private Map<Integer, FaninList> map = new HashMap<Integer, FaninList>();
 		private List<Block> list = new ArrayList<Block>();
 		private int rootId;
 		
 		public FanoutBlocks(Block root) {
 			this.list.add(root);
 			this.rootId = System.identityHashCode(root);
 		}
 		
 		public void add(Block fanin, Block fanout, int fanoutIndex, List<Indexed<String>> byRefs) {
 			Integer fanoutId = System.identityHashCode(fanout);
 			if (fanoutId != this.rootId) {
 				FaninList faninList = this.map.get(fanoutId);
 				if (faninList == null) {
 					this.list.add(fanout);
 					faninList = new FaninList(fanout);
 					this.map.put(fanoutId, faninList);
 				}
 				faninList.addFanin(fanin, fanoutIndex, byRefs);
 			}
 		}
 		
 		public Block getBlock(int index) {
 			if (index < this.list.size()) {
 				return this.list.get(index);
 			} else {
 				return null;
 			}
 		}
 		
 		public int getSize() {
 			return this.list.size();
 		}
 		
 		public APIData getAPI() {
 			Map<Integer, APIData> datas = new HashMap<Integer, APIData>();
 			for (Block b : this.list) {
 				int id = System.identityHashCode(b);
 				APIData data = b.toAPIData();
 				datas.put(id, data);
 			}
 			
 			int totalChange = Integer.MAX_VALUE;
 
 			while (totalChange > 0) {
 				totalChange = 0;
 				for (int i=this.list.size()-1; i>0; --i) {
 					Block b = this.list.get(i);
 					int id = System.identityHashCode(b);
 					APIData data = datas.get(id);
 					FaninList faninList = this.map.get(id);
 					List<IndexedBlock> faninBlocks = faninList.getFaninBlocks();
 					for (IndexedBlock ib : faninBlocks) {
 						Block faninBlock = ib.getBlock();
 						int faninId = System.identityHashCode(faninBlock);
 						APIData faninData = datas.get(faninId);
 						totalChange += faninData.mergeOutputs(data, ib.getIndex(), ib.getByRefs());
 					}
 				}
 				LOGGER.info("Total Change: " + String.valueOf(totalChange));
 			}
 			
 			totalChange = Integer.MAX_VALUE;
 			while (totalChange > 0) {
 				totalChange = 0;
 				for (int i=this.list.size()-1; i>0; --i) {
 					Block b = this.list.get(i);
 					int id = System.identityHashCode(b);
 					APIData data = datas.get(id);
 					FaninList faninList = this.map.get(id);
 					List<IndexedBlock> faninBlocks = faninList.getFaninBlocks();
 					for (IndexedBlock ib : faninBlocks) {
 						Block faninBlock = ib.getBlock();
 						int faninId = System.identityHashCode(faninBlock);
 						APIData faninData = datas.get(faninId);
 						totalChange += faninData.mergeInputs(data, ib.getIndex(), ib.getByRefs());
 					}
 				}
 				LOGGER.info("Total Change: " + String.valueOf(totalChange));
 			}			
 			
 			Block b = this.list.get(0);
 			int id = System.identityHashCode(b);
 			APIData result = datas.get(id);
 		
 			for (int i=this.list.size()-1; i>0; --i) {
 				Block bi = this.list.get(i);
 				int idi = System.identityHashCode(bi);
 				APIData data = datas.get(idi);
 				result.mergeGlobals(data);
 			}
 
 			return result;
 		}		
 	}
 		
 	private int index;
 	private EntryId entryId;
 	private Blocks siblings;
 	
 	private String[] formals;
 	private Map<String, Integer> formalsSet;
 	private Map<String, Integer> newedLocals = new HashMap<String, Integer>();
 	private Map<String, Integer> inputLocals = new HashMap<String, Integer>();
 	private Map<String, Integer> outputLocals = new HashMap<String, Integer>();
 	private Set<String> globals = new HashSet<String>();
 
 	private List<IndexedFanout> fanouts = new ArrayList<IndexedFanout>();
 	private boolean closed;
 	
 	public Block(int index, EntryId entryId, Blocks siblings) {
 		this.index = index;
 		this.entryId = entryId;
 		this.siblings = siblings;
 	}
 	
 	public void close() {
 		this.closed = true;
 	}
 	
 	public int getIndex() {
 		return this.index;
 	}
 	
 	public EntryId getEntryId() {
 		return this.entryId;
 	}
 	
 	public void setFormals(String[] formals) {
		this.formals = formals;
 		if (formals != null) {
			this.formalsSet = new HashMap<String, Integer>(formals.length*2);
			int index = 0;
 			for (String formal : formals) {
				formalsSet.put(formal, index);
				++index;
 			}
 		} else {
 			this.formalsSet=null;
 		}
 	}
 	
 	public String[] getFormals() {
 		return this.formals;
 	}
 	
 	public void addNewed(int index, Local local) {
 		if (! this.closed) {
 			String label = local.getName().toString();
 			if (! this.newedLocals.containsKey(label)) {
 				this.newedLocals.put(label, index);
 			}
 		}
 	}		
 	
 	public void addInput(int index, Local local) {
 		if (! this.closed) {
 			String label = local.getName().toString();
 			if ((! this.newedLocals.containsKey(label)) && (! this.outputLocals.containsKey(label))) {
 				if (! this.inputLocals.containsKey(label)) {
 					this.inputLocals.put(label, index);
 				}
 			}
 		}
 	}
 
 	public void addOutput(int index, Local local) {
 		if (! this.closed) {
 			String label = local.getName().toString();
 			if (! this.newedLocals.containsKey(label)) {
 				if (! this.outputLocals.containsKey(label)) {
 					this.outputLocals.put(label, index);
 				}
 			}
 		}
 	}
 
 	public void addGlobal(String value) {
 		if (! this.closed) {
 			this.globals.add(value);
 		}
 	}
 
 	public void addFanout(int index, EntryId fanout, boolean shouldClose, CallArgument[] arguments) {
 		if (! this.closed) {
			IndexedFanout ifo = new IndexedFanout(index, fanout);
 			ifo.setByRefs(arguments);
 			this.fanouts.add(ifo);
 			if (shouldClose) {
 				this.close();
 			}
 		}
 	}	
 	
 	public Integer getAsFormal(String name) {
 		if (this.formalsSet != null) {
 			return this.formalsSet.get(name);			
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean isNewed(String name, int sourceIndex) {
 		Integer index = this.newedLocals.get(name);
 		if (index == null) {
 			return false;
 		} else if (index.intValue() > sourceIndex) {
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean isAssigned(String name, int sourceIndex) {
 		Integer index = this.outputLocals.get(name);
 		if (index == null) {
 			return false;
 		} else if (index.intValue() > sourceIndex) {
 			return false;
 		}
 		return true;
 	}
 	
 	public Map<String, Integer> getNewedLocals() {
 		return this.newedLocals;
 	}
 	
 	public APIData toAPIData() {
 		APIData result = new APIData(this);
 		result.set(this.inputLocals, this.outputLocals, this.globals);
 		return result;
 	}
 	
 	public void update(FanoutBlocks fanoutBlocks, BlocksSupply overallMap, Filter<EntryId> filter, Map<String, String> replacedRoutines) {
 		for (IndexedFanout ifout : this.fanouts) {
 			EntryId fout = ifout.getFanout();
 			if ((filter != null) && (! filter.isValid(fout))) continue;
 			String routineName = fout.getRoutineName();
 			if (routineName != null && routineName.equals("DICF2")) {
 				routineName = "DICF2";
 			}
 			String tagName = fout.getTag();					
 			if (tagName == null) {
 				tagName = routineName;
 			}
 			if (routineName == null) {
 				Block tagBlock = this.siblings.get(tagName);
 				if (tagBlock == null) {
 					String inRoutineName = this.entryId.getRoutineName();
 					if (inRoutineName != null) {
 						String replacement = replacedRoutines.get(tagName);
 						if ((replacement != null) && (replacement.equals(inRoutineName))) {
 							tagBlock = this.siblings.get(replacement);
 						}
 					}
 					if (tagBlock == null) {
 						if (! reported.contains(fout)) {						
 							LOGGER.log(Level.SEVERE, "Unable to find information about tag " + tagName + " in " + this.entryId.getRoutineName());
 							reported.add(fout);
 						}
 						continue;
 					}
 				}
 				fanoutBlocks.add(this, tagBlock, ifout.getIndex(), ifout.getByRefs());
 			} else {
 				Blocks routineBlocks = overallMap.getBlocks(routineName);
 				String originalTagName = tagName;
 				if (routineBlocks == null) {
 					if (replacedRoutines != null) {
 						String replacement = replacedRoutines.get(routineName);
 						if (replacement != null) {
 							routineBlocks = overallMap.getBlocks(replacement);
 							if (tagName.equals(routineName)) {
 								tagName = replacement;
 							}
 						}
 					}
 					if (routineBlocks == null) {					
 						if (! reported.contains(fout)) {
 							LOGGER.log(Level.SEVERE, "Unable to find information about routine " + routineName);
 							reported.add(fout);
 						}
 						continue;
 					}
 				}
 				Block tagBlock = routineBlocks.get(tagName);
 				if (tagBlock == null) {
 					if (! originalTagName.equals(tagName)) {
 						tagBlock = routineBlocks.get(originalTagName);
 					}
 					if (tagBlock == null) {
 						if (! reported.contains(fout)) {
 							LOGGER.log(Level.SEVERE, "Unable to find information about tag " + fout.toString() + " in " + routineName);
 							reported.add(fout);
 						}
 						continue;
 					}
 				}
 				fanoutBlocks.add(this, tagBlock, ifout.getIndex(), ifout.getByRefs());
 			}				 
 		}
 	}
 	
 	public APIData getAPIData(BlocksSupply blocksSupply, Filter<EntryId> filter, Map<String, String> replacedRoutines) {
 		FanoutBlocks fanoutBlocks = new FanoutBlocks(this);
 		int index = 0;	
 		while (index < fanoutBlocks.getSize()) {
 			Block b = fanoutBlocks.getBlock(index);
 			b.update(fanoutBlocks, blocksSupply, filter, replacedRoutines);
 			++index;
 		}
 		return fanoutBlocks.getAPI();
 	}
 }
