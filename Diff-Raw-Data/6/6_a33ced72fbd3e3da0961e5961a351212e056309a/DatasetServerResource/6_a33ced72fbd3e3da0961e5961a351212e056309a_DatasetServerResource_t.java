 package com.kdcloud.server.rest.resource;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.restlet.Application;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Put;
 
 import weka.core.Instances;
 
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.lib.rest.api.DatasetResource;
 import com.kdcloud.lib.rest.ext.InstancesRepresentation;
 import com.kdcloud.server.entity.DataTable;
 import com.kdcloud.server.entity.Group;
 import com.kdcloud.server.entity.TableEntry;
 
 public class DatasetServerResource extends KDServerResource implements DatasetResource {
 	
 	private Group group;
 
 	public DatasetServerResource() {
 		super();
 	}
 
 	DatasetServerResource(Application application, Group group) {
 		super(application);
 		this.group = group;
 	}
 	
 	private DataTable getTable() {
 		DataTable dataset = group.map().get(user);
 		if (dataset == null) {
 			dataset = new DataTable();
 			group.getEntries().add(new TableEntry(user, dataset));
 			groupDao.save(group);
 		}
 		return dataset;
 	}
 
 
 	@Override
 	public Representation handle() {
 		String groupName = getParameter(ServerParameter.GROUP_ID);
 		group = groupDao.findByName(groupName);
		if (group == null) {
			group = new Group(groupName);
			groupDao.save(group);
		}
 		return super.handle();
 	}
 	
 	
 	@Override
 	@Put
 	public void uploadData(Representation representation) {
 		InstancesRepresentation instancesRepresentation = new InstancesRepresentation(representation);
 		try {
 			Instances data = instancesRepresentation.getInstances();
 			uploadData(data);
 		} catch (IOException e) {
 			getLogger().log(Level.INFO, "got an invalid request", e);
 			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			//TODO return an error representation
 		}
 		
 	}
 	
 	public void uploadData(Instances newData) {
 		DataTable dataset = getTable();
 		Instances storedData = dataset.getInstances();
 		if (storedData == null) {
 			dataset.setInstances(newData);
 			getLogger().info("created new dataset with size: " + newData.size());
 		}
 		else if (storedData.equalHeaders(newData)) {
 			newData.addAll(storedData);
 			dataset.setInstances(newData);
 			getLogger().info(newData.size() + " instances merged succeffully");
 		} else {
 			String error = "provided data does not match the dataset specification";
 			getLogger().info(error);
 			setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
 		}
 		groupDao.save(group);
 	}
 
 	@Override
 	@Get
 	public Representation getData() {
 		DataTable dataset = getTable();
 		if (dataset.getInstances() == null) {
 			getLogger().info("no data");
 			return null;
 		}
 		return new InstancesRepresentation(dataset.getInstances());
 	}
 
 	@Override
 	@Delete
 	public void deleteData() {
 		group.map().remove(user);
 	}
 
 }
