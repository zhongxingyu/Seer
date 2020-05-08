 package de.unikassel.ann.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import de.unikassel.ann.config.NetConfig;
 import de.unikassel.ann.io.beans.SynapseBean;
 import de.unikassel.ann.io.beans.TopologyBean;
 import de.unikassel.ann.model.func.ActivationFunction;
 
 /**
  * The network is a container for the layers.<br>
  * The first layer is the inputLayer and the last the outputLayer.<br>
  * When creating a network with the factory, you need to call {@link #finalizeStructure()} for linking the neurons (set synapses).<br>
  * 
  */
 public class Network extends BasicNetwork {
 
 	private Boolean finalyzed;
 	private NetConfig config;
 	private List<Neuron> flatNet;
 	private Set<Synapse> synapseSet;
 	private SynapseMatrix synapseMatrix;
 
 	public Network() {
 		super();
 		synapseSet = new HashSet<Synapse>();
 		flatNet = new ArrayList<Neuron>();
 		synapseMatrix = new SynapseMatrix(this, null, null);
 		finalyzed = false;
 	}
 
 	public void finalizeFromFlatNet(final List<TopologyBean> topoBeanList, final List<SynapseBean> synapsesBanList) {
 		if (finalyzed) {
 			return;
 		}
 
 		int maxHiddenIndex = 0;
 		for (TopologyBean b : topoBeanList) {
 			maxHiddenIndex = Math.max(maxHiddenIndex, b.getLayer());
 		}
 		Layer[] hiddenLayer = new Layer[maxHiddenIndex + 1];
 		// add layers to net
 		Layer inputLayer = new Layer();
 		addLayer(inputLayer);
 		for (int i = 0; i < maxHiddenIndex + 1; i++) {
 			hiddenLayer[i] = new Layer();
 			addLayer(hiddenLayer[i]);
 		}
 		Layer outputLayer = new Layer();
 		addLayer(outputLayer);
 
 		// creating neurons and adding it to flatNet and layers
 		for (TopologyBean b : topoBeanList) {
 			Neuron n = new Neuron(b.getFunction(), b.getBias());
 			n.setId(b.getId());
 			flatNet.add(n);
 			if (b.getLayer() == -1) {
 				inputLayer.addNeuron(n);
 			} else if (b.getLayer() == -2) {
 				outputLayer.addNeuron(n);
 			} else {
 				hiddenLayer[b.getLayer()].addNeuron(n);
 			}
 		}
 
 		synapseMatrix.setSize(flatNet.size(), flatNet.size());
 
 		if (synapsesBanList != null) {
 			setFlatSynapses(synapsesBanList);
 		} else {
 
 			// TODO: extract
 			Layer previousLayer = null;
 			// set synapses (strict forward feedback)
 			for (Layer l : layers) {
 				if (previousLayer != null) {
 					for (Neuron fromNeuron : previousLayer.getNeurons()) {
 						for (Neuron toNeuron : l.getNeurons()) {
 							if (toNeuron.isBias() == false) {
 								Synapse s = new Synapse(fromNeuron, toNeuron);
 								synapseSet.add(s);
 								// in this type of network use global ids for synapse matrix
 								synapseMatrix.addOrUpdateSynapse(s, fromNeuron.getId(), toNeuron.getId());
 							}
 						}
 					}
 				}
 				previousLayer = l;
 			}
 		}
 
 		finalyzed = true;
 	}
 
 	/**
 	 * Adds a layer to the end of the layers<br>
 	 * First time it becomes the input layer<br>
 	 * 2nd time it becomes the output layer<br>
 	 * 3rd time and more the new layer becomes output, and the previous becomes hidden layer
 	 * 
 	 * @param neuronCount
 	 * @param bias
 	 * @param function
 	 */
 	public void addLayer(final int neuronCount, final boolean bias, final ActivationFunction function) {
 		Layer l = new Layer();
 		if (bias) {
 			l.addNeuron(new Neuron(function, true));
 		}
 		for (int i = 0; i < neuronCount; i++) {
 			Neuron n = new Neuron(function, false);
 			l.addNeuron(n);
 		}
 		addLayer(l);
 	}
 
 	public void setInputLayerSize(final int inputSize) {
 		ActivationFunction function = getStandardFunction();
 		if (layers.isEmpty()) {
 			// add input layer
 			layers.add(new Layer());
 		}
 		Layer inputLayer = layers.get(0);
 		setLayerSize(inputSize, function, inputLayer);
 	}
 
 	public void setOuputLayerSize(final int outputSize) {
 		ActivationFunction function = getStandardFunction();
 		if (layers.isEmpty()) {
 			// add input layer
 			setInputLayerSize(1);
 		}
 		if (layers.size() == 1) {
 			// add output layer
 			layers.add(new Layer());
 		}
 		Layer outputLayer = layers.get(layers.size() - 1);
 		setLayerSize(outputSize, function, outputLayer);
 	}
 
 	public void setSizeOfHiddenLayers(final int hiddenLayerCount) {
 		if (layers.isEmpty()) {
 			// add input layer
 			setInputLayerSize(1);
 		}
 		if (layers.size() == 1) {
 			// add output layer
 			setOuputLayerSize(1);
 		}
 
 		int diff = hiddenLayerCount - (layers.size() - 2);
 		if (diff == 0) {
 			return;
 		} else if (diff > 0) {
 			// add new layers
 			for (int i = 0; i < diff; i++) {
 				int index = layers.size() - 1; // old output index = new hidden index
 				Layer hiddenLayer = new Layer();
 				layers.add(index, hiddenLayer); // shift the output layer to right
 				setHiddenLayerSize(index, 1); // initial size
 			}
 		} else {
 			// remove layers
			for (int i = 0; i < -diff; i++) {
 				layers.remove(layers.size() - 2); // -1 = output, -2 last hidden
 			}
 		}
 	}
 
 	// TODO: can also used for input layer, is it good?
 	public void setHiddenLayerSize(final int layerIndex, final int layerSize) {
 		ActivationFunction function = getStandardFunction();
 		// add only, if the layer already exist
 		// -1 because the the 2nd operand is index, not size
 		if (layers.size() - 1 >= layerIndex) {
 			// for example layerIndex
 			setLayerSize(layerSize, function, layers.get(layerIndex)); // initial size
 		}
 
 	}
 
 	private void setLayerSize(final int inputSize, final ActivationFunction function, final Layer inputLayer) {
 		int currentLayerSize = inputLayer.getNeurons().size();
 		// positive -> add
 		// negative -> remove
 		int diff = inputSize - currentLayerSize;
 		if (diff == 0) {
 			return;
 		} else if (diff > 0) {
 			// add neurons
 			for (int i = 0; i < diff; i++) {
 				inputLayer.addNeuron(new Neuron(function, false));
 			}
 		} else {
 			// delete neurons
 			List<Neuron> inputNeurons = inputLayer.getNeurons();
 			for (int i = 0; i < -diff; i++) {
 				inputNeurons.remove(inputNeurons.size() - 1);
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private ActivationFunction getStandardFunction() {
 		// TODO: get frmo sidebar
 		return null;
 	}
 
 	public void setFlatSynapses(final List<SynapseBean> synapsesBanList) {
 		// connect neurons / create synapses
 		for (SynapseBean b : synapsesBanList) {
 			synapseRangeCheck(b);
 			Neuron fromNeuron = flatNet.get(b.getFrom());
 			Neuron toNeuron = flatNet.get(b.getTo());
 			Synapse s = new Synapse(fromNeuron, toNeuron);
 			if (b.getRandom() == false) {
 				s.setWeight(b.getValue());
 			}
 			neuronRangeCheck(fromNeuron, toNeuron);
 			synapseSet.add(s);
 			// in this type of network use global ids for synapse matrix
 			synapseMatrix.addOrUpdateSynapse(s, fromNeuron.getId(), toNeuron.getId());
 		}
 
 	}
 
 	private void neuronRangeCheck(final Neuron fromNeuron, final Neuron toNeuron) {
 		if (fromNeuron.getId() < 0) {
 			throw new IllegalArgumentException("invalid neuron id, negative value not permitted\n" + fromNeuron);
 		} else if (toNeuron.getId() >= synapseMatrix.getSynapses().length) {
 			throw new IllegalArgumentException("invalid neuron id, don't fit into flat synapse array \n" + toNeuron);
 		}
 
 	}
 
 	private void synapseRangeCheck(final SynapseBean b) {
 		if (b.getFrom() < flatNet.get(0).getId()) {
 			throw new IllegalArgumentException("synapse connections does not match with neuron ids, 1st id is too small\n" + b);
 		} else if (b.getTo() > flatNet.get(flatNet.size() - 1).getId()) {
 			throw new IllegalArgumentException("synapse connections does not match with neuron ids, 2nd id is too high\n" + b);
 		}
 
 	}
 
 	/**
 	 * Creates the synapses between all neurons. After this call you can train the network.
 	 */
 	public void finalizeStructure() {
 		if (finalyzed) {
 			return;
 		}
 
 		Layer previousLayer = null;
 		for (Layer l : layers) {
 
 			// set flat net
 			for (Neuron n : l.getNeurons()) {
 				n.setId(flatNet.size());
 				flatNet.add(n);
 			}
 		}
 		synapseMatrix.setSize(flatNet.size(), flatNet.size());
 
 		// TODO: extract
 
 		// set synapses (strict forward feedback)
 		for (Layer l : layers) {
 			if (previousLayer != null) {
 				for (Neuron fromNeuron : previousLayer.getNeurons()) {
 					for (Neuron toNeuron : l.getNeurons()) {
 						if (toNeuron.isBias() == false) {
 							Synapse s = new Synapse(fromNeuron, toNeuron);
 							synapseSet.add(s);
 							// in this type of network use global ids for synapse matrix
 							synapseMatrix.addOrUpdateSynapse(s, fromNeuron.getId(), toNeuron.getId());
 						}
 					}
 				}
 			}
 			previousLayer = l;
 		}
 
 		finalyzed = true;
 	}
 
 	/**
 	 * Sets values for inputLayer.<br>
 	 * Lenght of array must match to input layer neuron count!
 	 * 
 	 * @param input
 	 *            for whole input layer
 	 */
 	public void setInputLayerValues(final Double[] input) {
 		Layer layer = getInputLayer();
 		List<Neuron> neuronList = layer.getNeurons();
 		int biasOffset = layer.hasBias() ? 1 : 0;
 		if (input.length != neuronList.size() - biasOffset) {
 			throw new IllegalArgumentException("input layer count != input.lenght");
 		}
 		for (int i = biasOffset; i < neuronList.size(); i++) {
 			neuronList.get(i).setOutputValue(input[i - biasOffset]);
 		}
 	}
 
 	public void setOutputToPair(final Double[] output) {
 		Layer layer = getOutputLayer();
 		List<Neuron> neuronList = layer.getNeurons();
 		if (output.length != neuronList.size()) {
 			throw new IllegalArgumentException("output layer count != ouput.length");
 		}
 		for (int i = 0; i < neuronList.size(); i++) {
 			output[i] = neuronList.get(i).getValue();
 		}
 	}
 
 	/**
 	 * @return true if {@link #finalizeStructure()} was called; otherwise false
 	 */
 	public boolean isFinalized() {
 		return finalyzed;
 	}
 
 	/**
 	 * @return the network in reverse order for backpropagation
 	 */
 	public List<Layer> reverse() {
 		ArrayList<Layer> reversedLayers = new ArrayList<Layer>(layers);
 		Collections.reverse(reversedLayers);
 		return reversedLayers;
 	}
 
 	public void setConfig(final NetConfig config) {
 		this.config = config;
 	}
 
 	public NetConfig getConfig() {
 		return config;
 	}
 
 	public List<Neuron> getFlatNet() {
 		return flatNet;
 	}
 
 	@Override
 	public SynapseMatrix getSynapseMatrix() {
 		return synapseMatrix;
 	}
 
 	public Set<Synapse> getSynapseSet() {
 		return synapseSet;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("finalized: ");
 		sb.append(finalyzed);
 		sb.append(", ");
 		sb.append(layers.size());
 		sb.append(" layers");
 		return sb.toString();
 	}
 
 }
