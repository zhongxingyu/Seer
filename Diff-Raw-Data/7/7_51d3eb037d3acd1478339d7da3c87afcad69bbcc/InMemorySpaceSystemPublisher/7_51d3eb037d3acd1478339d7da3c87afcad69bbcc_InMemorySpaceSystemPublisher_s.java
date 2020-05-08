 package org.hbird.core.spacesystempublisher.publishing;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.hbird.core.commons.tmtc.CommandGroup;
 import org.hbird.core.commons.tmtc.ParameterGroup;
 import org.hbird.core.spacesystemmodel.SpaceSystemModel;
 import org.hbird.core.spacesystemmodel.encoding.Encoding;
 import org.hbird.core.spacesystempublisher.interfaces.SpaceSystemModelUpdate;
 import org.hbird.core.spacesystempublisher.interfaces.SpaceSystemPublisher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A {@link SpaceSystemPublisher} that holds a {@link SpaceSystemModel} in memory at runtime.
  *
  * @author Mark Doyle
  *
  */
 public class InMemorySpaceSystemPublisher implements SpaceSystemPublisher {
 	private static final Logger LOG = LoggerFactory.getLogger(InMemorySpaceSystemPublisher.class);
 
 	private SpaceSystemModel model;
 
 	public InMemorySpaceSystemPublisher() {
 	}
 
 	/**
 	 * Creates the space system publisher with the provided {@link SpaceSystemModel}
 	 * This model will be used to provide responses to all service requests regarding the space system.
 	 * @param model
 	 */
 	public InMemorySpaceSystemPublisher(final SpaceSystemModel model) {
 		if(LOG.isDebugEnabled()) {
 			LOG.debug("Instantiated InMemorySpaceSystemPublisher using space system model " + model.getName());
 		}
 		this.model = model;
 	}
 
 	@Override
 	public Map<String, ParameterGroup> getParameterGroups() {
 		System.out.println("Request received!!!!");
 		return model.getParameterGroups();
 	}
 
 	@Override
 	public List<ParameterGroup> getParameterGroupList() {
 		System.out.println("Request received for parameter group list");
 		return new ArrayList<ParameterGroup>(model.getParameterGroupsCollection());
 	}
 
 	@Override
 	public Map<String, CommandGroup> getCommands() {
 		return model.getCommands();
 	}
 
 	@Override
 	public List<CommandGroup> getCommandList() {
 		return new ArrayList<CommandGroup>(model.getCommands().values());
 	}
 
 	@Override
 	public Map<String, Encoding> getEncodings() {
 		return model.getEncodings();
 	}
 
 	/**
 	 * @return the model
 	 */
 	public SpaceSystemModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @param model the model to set
 	 */
 	public void setModel(final SpaceSystemModel model) {
 		this.model = model;
 	}
 
 	@Override
 	public Map<String, List<String>> getRestrictions() {
 		return this.model.getAllPayloadRestrictions();
 	}
 
 	@Override
 	public void fireUpdate(final SpaceSystemModelUpdate update) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public CommandGroup getCommand(final String qualifiedName) {
		// TODO Auto-generated method stub
		//return null;
 		throw new UnsupportedOperationException();
 	}
 
 
 }
