 package org.fcrepo.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.fcrepo.jaxb.responses.access.ObjectDatastreams;
 import org.fcrepo.jaxb.responses.access.ObjectProfile;
 import org.fcrepo.jaxb.responses.management.DatastreamProfile;
 
 public class FedoraClient {
 
 	private static final String PATH_OBJECT_PROFILE = "/objects/";
	private static final String PATH_DATASTREAMS = "/datstreams/";
 	private static final String PATH_DATASTREAM_CONTENT = "/content/";
 
 	private final HttpClient client = new DefaultHttpClient();
 	private URI fedoraUri;
 
 	private Unmarshaller unmarshaller;
 
 	public FedoraClient(String fedoraUri) {
 		this.fedoraUri = URI.create(fedoraUri);
 	}
 
 	public FedoraClient() {
 		super();
 	}
 
 	public void setFedoraUri(String fedoraUri) {
 		this.fedoraUri = URI.create(fedoraUri);
 	}
 
 	private Unmarshaller getUnmarshaller() throws JAXBException {
 		if (unmarshaller == null) {
 			unmarshaller = JAXBContext.newInstance(ObjectProfile.class, ObjectDatastreams.class, DatastreamProfile.class)
 					.createUnmarshaller();
 		}
 		return unmarshaller;
 	}
 
 	public ObjectProfile getObjectProfile(final String id) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + id);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			ObjectProfile profile = (ObjectProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return profile;
 		} catch (JAXBException e) {
 			throw new IOException("Unabel to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 	}
 
 	public ObjectDatastreams getObjectDatastreams(final String objectId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			ObjectDatastreams datastreams = (ObjectDatastreams) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return datastreams;
 		} catch (JAXBException e) {
 			throw new IOException("Unabel to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 	}
 
 	public DatastreamProfile getDatastreamProfile(final String objectId, final String dsId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS + dsId);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			DatastreamProfile ds = (DatastreamProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return ds;
 		} catch (JAXBException e) {
 			throw new IOException("Unabel to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 
 	}
 
 	public InputStream getDatastreamContent(final String objectId, final String dsId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS + dsId
 				+ PATH_DATASTREAM_CONTENT);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		return resp.getEntity().getContent();
 	}
 }
