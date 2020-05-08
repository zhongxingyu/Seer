 /**
  * Copyright (C) 2012 Vincenzo Pirrone
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package com.kdcloud.server.rest.resource;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import javax.xml.XMLConstants;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.restlet.data.Method;
 import org.restlet.ext.xml.XmlRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 import org.xml.sax.SAXException;
 
 import com.kdcloud.server.entity.User;
 import com.kdcloud.server.persistence.DataMapperFactory;
 import com.kdcloud.server.persistence.EntityMapper;
 import com.kdcloud.server.persistence.InstancesMapper;
 import com.kdcloud.server.rest.application.ConvertHelper;
 import com.kdcloud.server.rest.application.ResourcesFinder;
 import com.kdcloud.server.rest.application.UserProvider;
 
 public abstract class KDServerResource extends ServerResource {
 
 	private EntityMapper entityMapper;
 	private InstancesMapper instancesMapper;
 	private UserProvider userProvider;
 	private ResourcesFinder resourcesFinder;
 
 	User user;
 
 	@Override
 	protected void doInit() throws ResourceException {
 		super.doInit();
 		userProvider = inject(UserProvider.class);
 		resourcesFinder = inject(ResourcesFinder.class);
 
 		DataMapperFactory factory = inject(DataMapperFactory.class);
 		entityMapper = factory.getEntityMapper();
 		instancesMapper = factory.getInstancesMapper();
 	}
 
 	protected String getResourceIdentifier() {
 		return (String) getRequestAttributes().get("id");
 	}
 	
 	protected String getResourceUri() {
 		return getResourceReference().replace(getHostRef().toString(), "");
 	}
 	
 	protected String getResourceReference() {
 		return getReference().toString().replaceAll("\\?.*", "");
 	}
 	
 	public Representation doGet() {
 		ClientResource cr = new ClientResource(getRequest().getResourceRef());
 		cr.setChallengeResponse(getChallengeResponse());
 		return cr.get();
 	}
 	
 	public void beforeHandle() {
 		user = userProvider.getUser(getRequest(), entityMapper);
 		if (user == null || user.getName() == null) {
 			getLogger().severe("invalid user");
 			throw new ResourceException(500);
 		}
 	}
 	
 	@Override
 	public Representation handle() {
 		try {
 			beforeHandle();
 		} catch (Throwable t) {
 			doCatch(t);
 		}
 		if (resourcesFinder != null && getMethod().equals(Method.GET))
 			try {
 				Representation local = resourcesFinder.find(getResourceUri());
 				getResponse().setEntity(local);
 				return local;
 			} catch (ResourceException e) {}
 		return super.handle();
 	}
 
 
 	@SuppressWarnings("unchecked")
 	protected <T> T inject(Class<T> baseClass) {
 		return (T) getApplication().getContext().getAttributes()
 				.get(baseClass.getName());
 	}
 	
 	public <T> T unmarshal(Class<T> clazz, Representation rep) {
 		Representation schemaRep;
 		if (resourcesFinder != null) {
			schemaRep = resourcesFinder.find(getHostRef() + ResourcesFinder.XML_SCHEMA_URI);
 		} else {
 			String url = getHostRef() + ResourcesFinder.XML_SCHEMA_URI;
 			schemaRep = new ClientResource(url).get();
 		}
 		try {
 			SAXSource source = XmlRepresentation.getSaxSource(schemaRep);
 			Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(source);
 			return ConvertHelper.toObject(clazz, rep, schema);
 		} catch (SAXException e) {
 			getLogger().log(Level.SEVERE, "error during schema generation", e);
 		} catch (IOException e) {
 			getLogger().log(Level.SEVERE, "error during schema generation", e);
 		}
 		throw new ResourceException(500);
 	}
 
 	public EntityMapper getEntityMapper() {
 		return entityMapper;
 	}
 	
 	public InstancesMapper getInstancesMapper() {
 		return instancesMapper;
 	}
 
 	public User getUser() {
 		return user;
 	}
 	
 }
