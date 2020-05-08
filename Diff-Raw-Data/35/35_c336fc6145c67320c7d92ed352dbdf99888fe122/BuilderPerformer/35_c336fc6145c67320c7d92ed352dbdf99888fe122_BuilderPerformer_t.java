 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.builder.impl;
 
 import java.util.Iterator;
 import java.util.List;
 
 import fr.liglab.adele.cilia.Binding;
 import fr.liglab.adele.cilia.Chain;
 import fr.liglab.adele.cilia.CiliaContext;
 import fr.liglab.adele.cilia.MediatorComponent;
 import fr.liglab.adele.cilia.builder.Architecture;
 import fr.liglab.adele.cilia.exceptions.BuilderException;
 import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
 import fr.liglab.adele.cilia.model.AdapterImpl;
 import fr.liglab.adele.cilia.model.BindingImpl;
 import fr.liglab.adele.cilia.model.ChainImpl;
 import fr.liglab.adele.cilia.model.MediatorImpl;
 import fr.liglab.adele.cilia.model.PatternType;
 
 /**
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 public class BuilderPerformer {
 
 	private CiliaContext ccontext;
 	private ArchitectureImpl architecture;
 	private Chain chain;
 
 	/**
 	 * @param architectureImpl
 	 */
 	public BuilderPerformer(ArchitectureImpl arch, CiliaContext context) {
 		ccontext = context;
 		architecture = arch;
 	}
 
 	/**
 	 * @throws BuilderException
 	 * 
 	 */
 	public void perform() throws BuilderPerformerException {
 		chain = getChain();
 		verifyOperations();
 		doCreate();
 		doRemove();
 		doModify();
 		doBind();
 		doUnbind();
 	}
 
 	private Chain getChain() throws BuilderPerformerException {
 		Chain chain = null;
 		if (architecture.isCreatingChain()) {
 			if (ccontext.getChain(architecture.getChainId()) != null) {
 				throw new BuilderPerformerException(
 						"Chain with the same ID already exist: "
 								+ architecture.getChainId());
 			}
 			chain = new ChainImpl(architecture.getChainId(), null, null, null);
 		} else {
 			if (ccontext.getChain(architecture.getChainId()) == null) {
 				throw new BuilderPerformerException("Chain does not exist: "
 						+ architecture.getChainId());
 			}
 			chain = ccontext.getChain(architecture.getChainId());
 		}
 		return chain;
 	}
 
 	private void doCreate() throws BuilderPerformerException {
 		List created = architecture.getCreated();
 		Iterator it = created.iterator();
 		while (it.hasNext()) {
 			CreatorImpl creat = (CreatorImpl) it.next();
 			switch (creat.getInstanceType()) {
 			case Architecture.ADAPTER:
 				createAdapter(creat);
 				break;
 			case Architecture.MEDIATOR:
 				createMediator(creat);
 				break;
 			}
 		}
 	}
 
 	private void createMediator(CreatorImpl creator) {
 		new MediatorImpl(creator.getId(), creator.getType(),
 				creator.getNamespace(), creator.getCategory(),
 				creator.getVersion(), creator.getProperties(), chain);
 	}
 
 	private void createAdapter(CreatorImpl creator) {
 		new AdapterImpl(creator.getId(), creator.getType(),
 				creator.getNamespace(), creator.getVersion(),
 				creator.getProperties(), chain, PatternType.UNASSIGNED);
 	}
 
 	private void doRemove() throws BuilderPerformerException {
 		Iterator it = architecture.getRemoved().iterator();
 		while (it.hasNext()) {
 			RemoverImpl toRemove = (RemoverImpl) it.next();
 			String id = toRemove.getId();
 			switch (toRemove.getType()) {
 			case Architecture.ADAPTER:
 				chain.removeAdapter(id);
 				break;
 			case Architecture.MEDIATOR:
 				chain.removeMediator(id);
 				break;
 			}
 		}
 	}
 
 	private void doModify() throws BuilderPerformerException {
 		Iterator it = architecture.getModified().iterator();
 		while (it.hasNext()) {
 			InstanceModifierImpl toModify = (InstanceModifierImpl) it.next();
 			String id = toModify.getId();
 			MediatorComponent comp = null;
 			switch (toModify.getType()) {
 			case Architecture.ADAPTER:
 				comp = chain.getAdapter(id);
 				break;
 			case Architecture.MEDIATOR:
 				comp = chain.getMediator(id);
 				break;
 			}
 			if (comp == null) {
 				throw new BuilderPerformerException("Unable to modify inexistent component:" + id);
 			}
 			comp.setProperties(toModify.getConfiguration());
 		}
 	}
 
 	private void doBind() throws BuilderPerformerException {
 		Iterator it = architecture.getBindings().iterator();
 		while (it.hasNext()) {
 			BinderImpl bi = (BinderImpl) it.next();
 			MediatorComponent from = getMediatorComponent(bi.getFromMediator());
 			MediatorComponent to = getMediatorComponent(bi.getToMediator());
 			String using = bi.getUsing();
			Binding binding = new BindingImpl(bi.getUsing(), bi.getConfiguration());
 			
 			
 			//chain.bind(outPort, inPort, bindingModel)
 		}
 	}
 
 	private MediatorComponent getMediatorComponent(String id) throws BuilderPerformerException {
 		MediatorComponent medComponent = chain.getMediator(id);
 		if (medComponent == null) {
 			medComponent = chain.getAdapter(id);
 		}
 		if(medComponent == null) {
 			throw new BuilderPerformerException("Unable to retrieve to perform bin; ID: " + id);
 		}
 		return medComponent;
 	}
 	
 	private void doUnbind() throws BuilderPerformerException {
 
 	}
 
 	private void verifyOperations() throws BuilderPerformerException {
 		verifyNewInstances();
 		verifyRemoveInstances();
 		verifyConfiguration();
 		verifyBindings();
 		verifyUnbindings();
 	}
 
 	private void verifyNewInstances() throws BuilderPerformerException {
 		List created = architecture.getCreated();
 		Iterator it = created.iterator();
 		while (it.hasNext()) {
 			CreatorImpl creat = (CreatorImpl) it.next();
 			String id = creat.getId();
 			if ((chain.getMediator(id) != null) || chain.getAdapter(id) != null) { //
 				switch (creat.getInstanceType()) {
 				case Architecture.ADAPTER:
 					throw new BuilderPerformerException(
 							"Impossible to create an adapter with ID"
 									+ id
 									+ "; Another instance with the same ID exists in chain "
 									+ chain.getId());
 				case Architecture.MEDIATOR:
 					throw new BuilderPerformerException(
 							"Impossible to create a mediator with ID"
 									+ id
 									+ "; Another instance with the same ID exists in chain "
 									+ chain.getId());
 				}
 			}
 		}
 	}
 
 	private void verifyRemoveInstances() throws BuilderPerformerException {
 		List removed = architecture.getRemoved();
 		Iterator it = removed.iterator();
 		while (it.hasNext()) {
 			RemoverImpl toRemove = (RemoverImpl) it.next();
 			String id = toRemove.getId();
 			if ((chain.getMediator(id) == null)
 					&& (chain.getAdapter(id) == null)) { //
 				switch (toRemove.getType()) {
 				case Architecture.ADAPTER:
 					throw new BuilderPerformerException(
 							"Impossible to remove an adapter with ID" + id
 									+ "; Unexistant adapterin chain "
 									+ chain.getId());
 				case Architecture.MEDIATOR:
 					throw new BuilderPerformerException(
 							"Impossible to remove a mediator with ID" + id
 									+ "; Unexistant mediator in chain "
 									+ chain.getId());
 				}
 			}
 		}
 	}
 
 	private void verifyConfiguration() throws BuilderPerformerException {
 		Iterator it =  architecture.getModified().iterator();
 		while (it.hasNext()) {
 			InstanceModifierImpl toModify = (InstanceModifierImpl) it.next();
 			String id = toModify.getId();
 			if ((chain.getMediator(id) == null) && chain.getAdapter(id) == null) { //
 				switch (toModify.getType()) {
 				case Architecture.ADAPTER:
 					throw new BuilderPerformerException(
 							"Impossible to remove an adapter with ID" + id
 									+ "; Unexistant adapterin chain "
 									+ chain.getId());
 				case Architecture.MEDIATOR:
 					throw new BuilderPerformerException(
 							"Impossible to remove a mediator with ID" + id
 									+ "; Unexistant mediator in chain "
 									+ chain.getId());
 				}
 			}
 		}
 	}
 
 	private void verifyBindings() throws BuilderPerformerException {
 		Iterator it = architecture.getBindings().iterator();
 		while (it.hasNext()) {
 			BinderImpl bi = (BinderImpl) it.next();
 			String id = bi.getFromMediator();
 			if (!isRealComponent(id)) {
 				throw new BuilderPerformerException(
 						"Unable to bind from an inexistent component:" + id);
 			}
 			id = bi.getToMediator();
 			if (!isRealComponent(id)) {
 				throw new BuilderPerformerException(
 						"Unable to bind to an inexistant component:" + id);
 			}
 		}
 	}
 
 	private void verifyUnbindings() throws BuilderPerformerException {
 		Iterator it = architecture.getBindings().iterator();
 		while (it.hasNext()) {
 			BinderImpl bi = (BinderImpl) it.next();
 			String id = bi.getFromMediator();
 			if (!isRealComponent(id)) {
 				throw new BuilderPerformerException(
 						"Unable to unbind from an inexistent component:" + id);
 			}
 			id = bi.getToMediator();
 			if (!isRealComponent(id)) {
 				throw new BuilderPerformerException(
 						"Unable to bind to an inexistant component:" + id);
 			}
 		}
 	}
 
 	private boolean isRealComponent(String id) {
 		if (chain.getAdapter(id) != null || chain.getMediator(id) != null) {
 			return true;
 		}
 		List newerComponents = architecture.getCreated();
 		Iterator it = newerComponents.iterator();
 		while (it.hasNext()) {
 			CreatorImpl toCreate = (CreatorImpl) it.next();
 			String mid = toCreate.getId();
 			if (id.equalsIgnoreCase(mid)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
