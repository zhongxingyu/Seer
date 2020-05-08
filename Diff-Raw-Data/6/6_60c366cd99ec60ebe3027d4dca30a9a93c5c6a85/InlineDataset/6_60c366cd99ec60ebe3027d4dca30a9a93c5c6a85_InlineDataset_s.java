 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.comp;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.swows.producer.Producer;
 import org.swows.producer.ProducerFactory;
 import org.swows.producer.ProducerMap;
 import org.swows.source.DatasetChanges;
 import org.swows.source.DatasetChangesFactory;
 import org.swows.source.DatasetSource;
 import org.swows.source.DatasetSourceBase;
 import org.swows.source.DatasetSourceListener;
 import org.swows.util.GraphUtils;
 import org.swows.vocabulary.DF;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.sparql.core.DatasetGraph;
 import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
 
 public class InlineDataset extends DatasetSourceBase {
 	
 	DatasetSource defaultGraphSource;
 	Map<Node,DatasetSource> namedGraphSources;
 	boolean listeningFromSnapshots = false;
 	boolean listeningFromChanges = false;
 	
 	private static DatasetGraph createDataset(
 			DatasetSource defaultGraphSource,
 			Map<Node,DatasetSource> namedGraphSources) {
 		Graph defaultGraph = defaultGraphSource.lastDataset().getDefaultGraph();
 		DatasetGraph newDataset = DatasetGraphFactory.create(defaultGraph);
 		for (Node graphName : namedGraphSources.keySet()) {
 			newDataset.addGraph(
 					graphName,
 					namedGraphSources.get(graphName).lastDataset().getDefaultGraph());
 		}
 		return newDataset;
 	}
 	
 	private DatasetChanges createChanges() {
 		DatasetChanges defaultGraphChanges =
 				defaultGraphSource.changesFromPrevDataset();
 		DatasetGraph addedDataset =
 				DatasetGraphFactory.create(
 						defaultGraphChanges
 						.getAddedAsDataset().getDefaultGraph());
 		DatasetGraph deletedDataset =
 				DatasetGraphFactory.create(
 						defaultGraphChanges
 						.getDeletedAsDataset().getDefaultGraph());
 		for (Node graphName : namedGraphSources.keySet()) {
 			DatasetChanges namedGraphChanges =
 					namedGraphSources.get(graphName).changesFromPrevDataset();
 			addedDataset.addGraph(
 					graphName,
 					namedGraphChanges.getAddedAsDataset().getDefaultGraph());
 			deletedDataset.addGraph(
 					graphName,
 					namedGraphChanges.getDeletedAsDataset().getDefaultGraph());
 		}
 		return DatasetChangesFactory.fromDatasets(addedDataset, deletedDataset);
 	}
 	
 	public InlineDataset(
 			DatasetSource defaultGraphSource,
 			Map<Node,DatasetSource> namedGraphSources) {
 		super(createDataset(defaultGraphSource, namedGraphSources));
 		this.defaultGraphSource = defaultGraphSource;
 		this.namedGraphSources = namedGraphSources;
 	}
 	
 	@Override
 	public void registerSnapshotListener(DatasetSourceListener l) {
 		super.registerSnapshotListener(l);
 		if (!listeningFromSnapshots) {
 			registerAsSnapshotListenerTo(defaultGraphSource);
 			for (Node graphName : namedGraphSources.keySet()) {
 				registerAsSnapshotListenerTo(namedGraphSources.get(graphName));
 			}
 			listeningFromSnapshots = true;
 		}
 	}
 
 	@Override
 	public void registerChangesListener(DatasetSourceListener l) {
 		super.registerSnapshotListener(l);
 		if (!listeningFromChanges) {
 			registerAsChangesListenerTo(defaultGraphSource);
 			for (Node graphName : namedGraphSources.keySet()) {
 				registerAsChangesListenerTo(namedGraphSources.get(graphName));
 			}
 			listeningFromChanges = true;
 		}
 	}
 	
 	@Override
 	public synchronized DatasetGraph lastDataset() {
 		return lastDataset;
 	}
 
 	@Override
 	public synchronized DatasetChanges changesFromPrevDataset() {
 		return changesFromPrev;
 	}
 	
 	@Override
 	protected void allSnapshotListenersReady() {
 		lastDataset = null;
 	}
 
 	@Override
 	protected void allChangesListenersReady() {
 		changesFromPrev = null;
 	}
 
 	@Override
	protected void allListenersReady() {
		prevDataset = lastDataset;
		lastDataset = null;
		super.allListenersReady();	
	}
 
 	@Override
 	protected void readyForExecution() {
 		if (listeningFromSnapshots)
 			lastDataset = createDataset(defaultGraphSource, namedGraphSources);
 		if (listeningFromChanges)
 			changesFromPrev = createChanges();
 		hasChanged = true;
 		notifyAdvanced();
 	}
 
 	private static ProducerFactory<DatasetSource> factory =
 			new ProducerFactory<DatasetSource>() {
 
 		@Override
 		public Producer<DatasetSource> createProducer(
 				final Graph conf,
 				final Node confRoot,
 				final ProducerMap map) {
 			Node defaultInputNode = GraphUtils.getSingleValueOptProperty(conf, confRoot, DF.defaultInput.asNode());
 			final Producer<DatasetSource> defaultInputProducer =
 					(defaultInputNode == null)
 							? null
 							: map.getProducer(defaultInputNode);
 			final Map<Node, Producer<DatasetSource>> namedInputProducers =
 					new HashMap<Node, Producer<DatasetSource>>();
 			Iterator<Node> namedInputNodes = GraphUtils.getPropertyValues(conf, confRoot, DF.namedInput.asNode());
 			while (namedInputNodes.hasNext()) {
 				Node namedInputNode = namedInputNodes.next();
 				Node nameNode = GraphUtils.getSingleValueProperty(conf, namedInputNode, DF.name.asNode());
 				Node graphNode = GraphUtils.getSingleValueProperty(conf, namedInputNode, DF.input.asNode());
 				Producer<DatasetSource> producer = map.getProducer(graphNode);
 				namedInputProducers.put(nameNode, producer);
 			}
 			return new Producer<DatasetSource>() {
 
 				@Override
 				public DatasetSource create(DatasetSource inputDatasetSource) {
 					DatasetSource defaultInputGraphSource = defaultInputProducer.create(inputDatasetSource);
 					Map<Node, DatasetSource> namedInputGraphSources =
 							new HashMap<Node, DatasetSource>();
 					for (Node graphName : namedInputProducers.keySet()) {
 						namedInputGraphSources.put(graphName, namedInputProducers.get(graphName).create(inputDatasetSource));
 					}
 					return new InlineDataset(defaultInputGraphSource, namedInputGraphSources);
 				}
 			};
 			
 		}
 		
 	};
 	
 	public static ProducerFactory<DatasetSource> getFactory() {
 		return factory;
 	}
 
 }
