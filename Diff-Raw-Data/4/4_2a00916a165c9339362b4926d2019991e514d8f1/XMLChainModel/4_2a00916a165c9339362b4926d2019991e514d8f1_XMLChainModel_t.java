 /**
  * Copyright Universite Joseph Fourier (www.ujf-grenoble.fr)
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
 package fr.liglab.adele.cilia.workbench.designer.parser.chain.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
import fr.liglab.adele.cilia.workbench.common.cilia.CiliaConstants;
 import fr.liglab.adele.cilia.workbench.common.cilia.CiliaException;
 import fr.liglab.adele.cilia.workbench.common.identifiable.NameNamespaceID;
 import fr.liglab.adele.cilia.workbench.common.misc.Strings;
 import fr.liglab.adele.cilia.workbench.common.parser.AbstractModel;
 import fr.liglab.adele.cilia.workbench.common.parser.PhysicalResource;
 import fr.liglab.adele.cilia.workbench.common.parser.chain.AdapterRef;
 import fr.liglab.adele.cilia.workbench.common.parser.chain.Cardinality;
 import fr.liglab.adele.cilia.workbench.common.parser.chain.MediatorRef;
 import fr.liglab.adele.cilia.workbench.common.service.Changeset;
 import fr.liglab.adele.cilia.workbench.common.service.MergeUtil;
 import fr.liglab.adele.cilia.workbench.common.service.Mergeable;
 import fr.liglab.adele.cilia.workbench.common.xml.XMLHelpers;
 import fr.liglab.adele.cilia.workbench.common.xml.XMLStringUtil;
 import fr.liglab.adele.cilia.workbench.designer.parser.chain.abstractcomposition.AbstractBinding;
 import fr.liglab.adele.cilia.workbench.designer.parser.chain.abstractcomposition.AbstractChain;
 import fr.liglab.adele.cilia.workbench.designer.parser.chain.abstractcomposition.MediatorSpecRef;
 import fr.liglab.adele.cilia.workbench.designer.parser.chain.dscilia.AdapterImplemRef;
 import fr.liglab.adele.cilia.workbench.designer.parser.chain.dscilia.MediatorImplemRef;
 import fr.liglab.adele.cilia.workbench.designer.service.chain.common.ChainRepoService;
 
 /**
  * 
  * @author Etienne Gandrille
  */
 public abstract class XMLChainModel<ChainType extends XMLChain> extends AbstractModel implements Mergeable {
 
 	protected List<ChainType> model = new ArrayList<ChainType>();
 	protected final ChainRepoService<?, ?, ChainType> repository;
 
 	public XMLChainModel(PhysicalResource file, String rootNodeName, ChainRepoService<?, ?, ChainType> repository) {
 		super(file, rootNodeName);
 		this.repository = repository;
 	}
 
 	public List<ChainType> getChains() {
 		return model;
 	}
 
 	@Override
 	public List<Changeset> merge(Object other) throws CiliaException {
 		ArrayList<Changeset> retval = new ArrayList<Changeset>();
 		@SuppressWarnings("unchecked")
 		XMLChainModel<ChainType> newInstance = (XMLChainModel<ChainType>) other;
 
 		retval.addAll(MergeUtil.mergeLists(newInstance.getChains(), model));
 
 		for (Changeset c : retval)
 			c.pushPathElement(this);
 
 		return retval;
 	}
 
 	public void createChain(NameNamespaceID id) throws CiliaException {
 
 		// Document creation
 		Document document = getDocument();
 		Node root = getRootNode(document);
 		Element child = document.createElement(XMLChain.XML_NODE_NAME);
 		child.setAttribute(XMLChain.XML_ATTR_ID, id.getName());
 		child.setAttribute(XMLChain.XML_ATTR_NAMESPACE, id.getNamespace());
 		root.appendChild(child);
 
 		writeToFile(document);
 		notifyRepository();
 	}
 
 	public void deleteChain(NameNamespaceID id) throws CiliaException {
 
 		// Finding target node
 		Document document = getDocument();
 		Node target = findXMLChainNode(document, id);
 
 		if (target != null) {
 			getRootNode(document).removeChild(target);
 			writeToFile(document);
 			notifyRepository();
 		}
 	}
 
 	protected void createComponentInstanceInternal(XMLChain chain, String id, NameNamespaceID type, String rootNode, String elementNode) throws CiliaException {
 		Document document = getDocument();
 		Node chainNode = findXMLChainNode(document, chain.getId());
 		Node componentNode = XMLHelpers.getOrCreateChild(document, chainNode, rootNode);
 
 		Element child = document.createElement(elementNode);
 		child.setAttribute(XMLComponentRefHelper.XML_ATTR_ID, id);
 		child.setAttribute(XMLComponentRefHelper.XML_ATTR_TYPE, type.getName());
 		if (!Strings.isNullOrEmpty(type.getNamespace()))
 			child.setAttribute(XMLComponentRefHelper.XML_ATTR_NAMESPACE, type.getNamespace());
 		componentNode.appendChild(child);
 
 		writeToFile(document);
 		notifyRepository();
 	}
 
 	/**
 	 * srcCard and dstCard can be null. If they are, cardinalities are not
 	 * written in the XML file
 	 */
 	public void createBinding(XMLChain chain, String srcElem, String srcPort, String dstElem, String dstPort, Cardinality srcCard, Cardinality dstCard)
 			throws CiliaException {
 		if (chain.isNewBindingAllowed(srcElem, srcPort, dstElem, dstPort) == null) {
 
 			String from;
 			if (Strings.isNullOrEmpty(srcPort))
 				from = srcElem;
 			else
 				from = srcElem + ":" + srcPort;
 
 			String to;
 			if (Strings.isNullOrEmpty(dstPort))
 				to = dstElem;
 			else
 				to = dstElem + ":" + dstPort;
 
 			Document document = getDocument();
 			Node chainNode = findXMLChainNode(document, chain.getId());
 			Node componentNode = XMLHelpers.getOrCreateChild(document, chainNode, AbstractChain.XML_ROOT_BINDINGS_NAME);
 
 			Element child = document.createElement(XMLBinding.XML_NODE_NAME);
 			child.setAttribute(XMLBinding.XML_FROM_ATTR, from);
 			child.setAttribute(XMLBinding.XML_TO_ATTR, to);
 			if (srcCard != null)
 				child.setAttribute(AbstractBinding.XML_FROM_CARD_ATTR, srcCard.stringId());
 			if (dstCard != null)
 				child.setAttribute(AbstractBinding.XML_TO_CARD_ATTR, dstCard.stringId());
 			componentNode.appendChild(child);
 
 			writeToFile(document);
 			notifyRepository();
 		}
 	}
 
 	public void deleteBinding(XMLChain chain, XMLBinding binding) throws CiliaException {
 		Document document = getDocument();
 		Node chainNode = findXMLChainNode(document, chain.getId());
 		Node subNode = XMLHelpers.findChild(chainNode, AbstractChain.XML_ROOT_BINDINGS_NAME);
 		if (subNode == null)
 			return;
 
 		Node[] nodes = XMLHelpers.findChildren(subNode, XMLBinding.XML_NODE_NAME, XMLBinding.XML_FROM_ATTR, binding.getSource(), XMLBinding.XML_TO_ATTR,
 				binding.getDestination());
 		if (nodes.length == 0)
 			throw new CiliaException("Can't find binding " + binding);
 		subNode.removeChild(nodes[0]);
 
 		writeToFile(document);
 		notifyRepository();
 	}
 
 	protected Node findXMLChainNode(Document document, NameNamespaceID id) throws CiliaException {
 		Node root = getRootNode(document);
 		Node[] results;
 
		if (Strings.isNullOrEmpty(id.getNamespace()) || id.getNamespace().equals(CiliaConstants.CILIA_DEFAULT_NAMESPACE))
 			results = XMLHelpers.findChildren(root, AbstractChain.XML_NODE_NAME, AbstractChain.XML_ATTR_ID, id.getName());
 		else
 			results = XMLHelpers.findChildren(root, AbstractChain.XML_NODE_NAME, AbstractChain.XML_ATTR_ID, id.getName(), AbstractChain.XML_ATTR_NAMESPACE,
 					id.getNamespace());
 
 		if (results.length == 0)
 			return null;
 		else
 			return results[0];
 	}
 
 	protected void notifyRepository() {
 		repository.updateModel();
 	}
 
 	protected void deleteMediator(XMLChain chain, MediatorRef mediator, String XMLNodeName) throws CiliaException {
 		Document document = getDocument();
 		Node chainNode = findXMLChainNode(document, chain.getId());
 		Node subNode = XMLHelpers.findChild(chainNode, AbstractChain.XML_ROOT_MEDIATORS_NAME);
 
 		Node leafs[] = null;
 		if (mediator instanceof MediatorImplemRef)
 			leafs = XMLHelpers.findChildren(subNode, XMLNodeName, XMLComponentRefHelper.XML_ATTR_ID, mediator.getId());
 		if (mediator instanceof MediatorSpecRef)
 			leafs = XMLHelpers.findChildren(subNode, MediatorSpecRef.XML_NODE_NAME, XMLComponentRefHelper.XML_ATTR_ID, mediator.getId());
 
 		if (leafs == null || leafs.length == 0)
 			throw new CiliaException("Can't find mediator with id " + mediator.getId() + " in XML file");
 
 		for (Node leaf : leafs)
 			subNode.removeChild(leaf);
 
 		deleteBindingsWithReferenceToComponent(chainNode, mediator.getId());
 		writeToFile(document);
 		notifyRepository();
 	}
 
 	protected void deleteAdapter(XMLChain chain, AdapterRef adapter, String XMLNodeName) throws CiliaException {
 		Document document = getDocument();
 		Node chainNode = findXMLChainNode(document, chain.getId());
 		Node subNode = XMLHelpers.findChild(chainNode, AbstractChain.XML_ROOT_ADAPTERS_NAME);
 
 		Node leafs[] = null;
 		if (adapter instanceof AdapterImplemRef)
 			leafs = XMLHelpers.findChildren(subNode, XMLNodeName, XMLComponentRefHelper.XML_ATTR_ID, adapter.getId());
 		// Adapter spec...
 
 		if (leafs == null || leafs.length == 0)
 			throw new CiliaException("Can't find adapter with id " + adapter.getId() + " in XML file");
 
 		for (Node leaf : leafs)
 			subNode.removeChild(leaf);
 
 		deleteBindingsWithReferenceToComponent(chainNode, adapter.getId());
 		writeToFile(document);
 		notifyRepository();
 	}
 
 	private void deleteBindingsWithReferenceToComponent(Node chainNode, String componentID) throws CiliaException {
 		Node subNode = XMLHelpers.findChild(chainNode, AbstractChain.XML_ROOT_BINDINGS_NAME);
 		if (subNode == null)
 			return;
 
 		// finds nodes
 		List<Node> nodes = new ArrayList<Node>();
 		Node bindings[] = XMLHelpers.findChildren(subNode, XMLBinding.XML_NODE_NAME);
 		for (Node binding : bindings) {
 			String from = XMLHelpers.findAttributeValueOrEmpty(binding, XMLBinding.XML_FROM_ATTR);
 			String to = XMLHelpers.findAttributeValueOrEmpty(binding, XMLBinding.XML_TO_ATTR);
 
 			String fromID = XMLStringUtil.getBeforeSeparatorOrAll(from);
 			String toID = XMLStringUtil.getBeforeSeparatorOrAll(to);
 
 			if (fromID.equalsIgnoreCase(componentID) || toID.equalsIgnoreCase(componentID))
 				nodes.add(binding);
 		}
 
 		// remove nodes
 		for (Node binding : nodes) {
 			subNode.removeChild(binding);
 		}
 	}
 }
