 /*
 
 Copyright (c) 2010, Jared Crapo All rights reserved. 
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met: 
 
 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer. 
 
 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution. 
 
 - Neither the name of jactiveresource.org nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
  */
 
 package org.jactiveresource;
 
 import static org.jactiveresource.Inflector.dasherize;
 import static org.jactiveresource.Inflector.singularize;
 import static org.jactiveresource.Inflector.underscore;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.lang.reflect.Field;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.util.EntityUtils;
 import org.jactiveresource.annotation.CollectionName;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
 import com.thoughtworks.xstream.core.DefaultConverterLookup;
 import com.thoughtworks.xstream.core.util.ClassLoaderReference;
 import com.thoughtworks.xstream.core.util.CompositeClassLoader;
 import com.thoughtworks.xstream.io.xml.XppDriver;
 
 /**
  * <h3>Finding Resources</h3>
  * 
  * @version $LastChangedRevision$ <br>
  *          $LastChangedDate$
  * @author $LastChangedBy$
  */
 public class ResourceFactory {
 
 	private ResourceConnection connection;
 	private Class<? extends ActiveResource> clazz;
 	private XStream xstream;
 	private URLBuilder collectionURL;
 
 	public ResourceFactory(ResourceConnection c,
 			Class<? extends ActiveResource> clazz) {
 		this.connection = c;
 		this.clazz = clazz;
 		collectionURL = new URLBuilder(getCollectionName());
 
 		// xstream = new XStream();
 		xstream = makeXStream();
 		xstream.registerConverter(new ISO8601DateConverter());
 
 		// "#{prefix(prefix_options)}#{collection_name}/#{id}.#{format.extension}#{
 		// query_string(query_options)}"
 
 		String xmlname = singularize(dasherize(getCollectionName()));
 		xstream.alias(xmlname, clazz);
 
 		Field[] fields = clazz.getDeclaredFields();
 		for (Field field : fields) {
 			xmlname = dasherize(underscore(field.getName()));
 			xstream.aliasField(xmlname, clazz, field.getName());
 		}
 
 		xstream.processAnnotations(clazz);
 	}
 
 	private XStream makeXStream() {
 		RailsConverterRegistry rcr = new RailsConverterRegistry();
 		XStream xstream = new XStream(null, new XppDriver(),
 				new ClassLoaderReference(new CompositeClassLoader()), null,
 				new DefaultConverterLookup(), rcr);
 		return xstream;
 	}
 
 	/**
 	 * Retrieve the resource identified by <code>id</code>, and return a new
 	 * instance of the appropriate object
 	 * 
 	 * @param <T>
 	 * @param id
 	 *            the primary identifier
 	 * @return a new instance of a subclass of @{link ActiveResource}
 	 * @throws URISyntaxException
 	 * @throws InterruptedException
 	 * @throws IOException
 	 * @throws HttpException
 	 */
 	public <T extends ActiveResource> T find(String id) throws HttpException,
 			IOException, InterruptedException, URISyntaxException {
		return fetchOne(collectionURL.add(id + getResourceFormat().extension()));
 	}
 
 	/**
 	 * Fetch all the resources. Say I have a person service at
 	 * <code>http://localhost:3000/</code>. The following would return the list
 	 * of people returned by <code>http://localhost:3000/people.xml</code>.
 	 * 
 	 * <pre>
      * {@code
      * c = new ResourceConnection("http://localhost:3000");
      * rf = new ResourceFactory(c, Person.class);
      * ArrayList<Person> people = rf.findAll();
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll()
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
 		URLBuilder url = new URLBuilder(getCollectionName()
 				+ getResourceFormat().extension());
 		return fetchMany(url);
 	}
 
 	/**
 	 * Fetch resources using query parameters. Say I have a collection of people
 	 * at <code>http://localhost:3000/people.xml</code>. I can specify a
 	 * parameter to limit the people to those who hold a position of manager by
 	 * using <code>http://localhost:3000/people.xml?position=manager</code>.
 	 * 
 	 * <pre>
      * {@code
      * ResourceConnection c = new ResourceConnection("http://localhost:3000");
      * ResourceFactory rf = new ResourceFactory(c, Person.class);
      * HashMap<String,String> params = new HashMap<String,String>();
      * params.put("position", "manager");
      * ArrayList<Person> rubydevs = rf.findAll(params);
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @param params
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll(
 			Map<Object, Object> params) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		URLBuilder url = new URLBuilder(getCollectionName()
 				+ getResourceFormat().extension());
 		return fetchMany(url.addQuery(params));
 	}
 
 	/**
 	 * Fetch resources using query parameters. In this case the query parameters
 	 * are taken from a URLBuilder object. To get resources from
 	 * <code>http://localhost:3000/people.xml?position=manager</code> do:
 	 * 
 	 * <pre>
      * {@code
      * ResourceConnection c = new ResourceConnection("http://localhost:3000");
      * ResourceFactory rf = new ResourceFactory(c, Person.class);
      * URLBuilder params = new URLBuilder();
      * params.addQuery("position", "manager");
      * ArrayList<Person> rubydevs = rf.findAll(params);
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @param params
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll(URLBuilder params)
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
 		URLBuilder url = new URLBuilder(getCollectionName()
 				+ getResourceFormat().extension());
 		return fetchMany(url.addQuery(params));
 	}
 
 	/**
 	 * Fetch a list of resources using a custom method. Say I have a collection
 	 * of people at <code>http://localhost:3000/people.xml</code>. Say there is
 	 * a custom method managers at
 	 * <code>http://localhost:3000/people/geeks.xml</code> which returns only
 	 * the people who are geeks. To get the list of geeks I would use:
 	 * 
 	 * <pre>
      * {@code
      * c = new ResourceConnection("http://localhost:3000");
      * rf = new ResourceFactory(c, Person.class);
      * ArrayList<Person> geeks = rf.findAll("geeks");
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @param from
 	 *            the name of the custom method
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll(String from)
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
 		URLBuilder url = collectionURL.add(from
 				+ getResourceFormat().extension());
 		return fetchMany(url);
 	}
 
 	/**
 	 * Fetch resources using a custom method and query parameters. Say I have a
 	 * collection of people at <code>http://localhost:3000/people.xml</code>.
 	 * Say there is a custom method <code>developers</code> at
 	 * <code>http://localhost:3000/people/developers.xml</code> which returns
 	 * only the people who are developers. Additionally, I can specify a
 	 * parameter to limit the developers to those who are skilled in a
 	 * particular language by using
 	 * <code>http://localhost:3000/people/developers.xml?language=ruby</code>
 	 * <p>
 	 * To get the ruby developers:
 	 * 
 	 * <pre>
      * {@code
      *     ResourceConnection c = new ResourceConnection("http://localhost:3000");
      *     ResourceFactory rf = new ResourceFactory(c, Person.class);
      *     HashMap<String, String> params = new HashMap<String, String>();
      *     params.put("language", "ruby");
      *     ArrayList<Person> rubydevs = rf.findAll("developers", params);
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @param from
 	 * @param params
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll(String from,
 			Map<Object, Object> params) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		URLBuilder url = collectionURL.add(from
 				+ getResourceFormat().extension());
 		return fetchMany(url.addQuery(params));
 	}
 
 	/**
 	 * Fetch resources using a custom method and query parameters, where the
 	 * query parameters are taken from a URLBuilder object. To get the resources
 	 * from
 	 * <code>http://localhost:3000/people/developers.xml?language=ruby</code>
 	 * do:
 	 * 
 	 * <pre>
      * {@code
      *     ResourceConnection c = new ResourceConnection("http://localhost:3000");
      *     ResourceFactory rf = new ResourceFactory(c, Person.class);
      *     URLBuilder params = new URLBuilder();
      *     params.addQuery("language", "ruby");
      *     ArrayList<Person> rubydevs = rf.findAll("developers", params);
      * }
      * </pre>
 	 * 
 	 * @param <T>
 	 * @param from
 	 * @param params
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> findAll(String from,
 			URLBuilder params) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		URLBuilder url = collectionURL.add(from
 				+ getResourceFormat().extension());
 		return fetchMany(url.addQuery(params));
 	}
 
 	/**
 	 * Return true if a resource exists. Say I have a person service at
 	 * <code>http://localhost:3000/</code>. If the following is a valid URL
 	 * which returns data <code>http://localhost:3000/people/5.xml</code>, then
 	 * <code>fred</code> is true.
 	 * 
 	 * <pre>
      * {@code
      * c = new ResourceConnection("http://localhost:3000");
      * rf = new ResourceFactory(c, Person.class);
      * boolean fred = rf.exists("5");
      * }
      * </pre>
 	 * 
 	 * @param id
 	 *            the id you want to check
 	 * @return true if the resource exists, false if it does not
 	 */
 	public boolean exists(String id) {
 		String url = "/" + getCollectionName() + "/" + id
 				+ getResourceFormat().extension();
 		try {
 			connection.get(url);
 			return true;
 		} catch (HttpException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		} catch (InterruptedException e) {
 			return false;
 		} catch (URISyntaxException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Create a new instance of a resource. This method calls the default
 	 * constructor of your resource class, and also attaches the factory to the
 	 * resource class. Say we had a Person class like this:
 	 * 
 	 * <pre>
      * {@code
      * public class Person extends ActiveResource {
      *   private String id;
      *   public String getId() {
      *     return id;
      *   }
      *   public void setId( String id ) {
      *     this.id = id;
      *   }
      * }
      * }
      * </pre>
 	 * 
 	 * And:
 	 * 
 	 * <pre>
      * {@code
      * ResourceFactory pf = new ResourceFactory(c, Person.class);
      * Person a = new Person();
      * a.setFactory(pf);
      * Person b = pf.instantiate();
      * }
      * </pre>
 	 * 
 	 * Person a and Person b are now equivalent.
 	 * 
 	 * @param <T>
 	 * @return a new instance of a resource
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends ActiveResource> T instantiate()
 			throws InstantiationException, IllegalAccessException {
 		T obj = (T) clazz.newInstance();
 		obj.setFactory(this);
 		return obj;
 	}
 
 	/**
 	 * create a new resource on the server from a local object
 	 * 
 	 * @param r
 	 * @throws ClientProtocolException
 	 * @throws ClientError
 	 * @throws ServerError
 	 * @throws IOException
 	 */
 	public boolean create(ActiveResource r) throws ClientProtocolException,
 			ClientError, ServerError, IOException {
 		String url = "/" + getCollectionName()
 				+ getResourceFormat().extension();
 		String xml = xstream.toXML(r);
 		HttpResponse response = connection.post(url, xml, ResourceFormat.XML
 				.contentType());
 		String entity = EntityUtils.toString(response.getEntity());
 		try {
 			connection.checkHttpStatus(response);
 			xstream.fromXML(entity, r);
 			r.setFactory(this);
 			return true;
 		} catch (ResourceInvalid e) {
 			return false;
 		}
 	}
 
 	/**
 	 * update the server resource associated with an object
 	 * 
 	 * @param r
 	 * @throws URISyntaxException
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public boolean update(ActiveResource r) throws URISyntaxException,
 			HttpException, IOException, InterruptedException {
 		URLBuilder url = collectionURL.add(r.getId()
 				+ getResourceFormat().extension());
 		String xml = xstream.toXML(r);
 		HttpResponse response = connection.put(url.toString(), xml,
 				getResourceFormat().contentType());
 		// now let's see what came back
 		// String entity = EntityUtils.toString(response.getEntity());
 		try {
 			connection.checkHttpStatus(response);
 			return true;
 		} catch (ResourceInvalid e) {
 			return false;
 		}
 	}
 
 	/**
 	 * create the resource if it is new, otherwise update it
 	 * 
 	 * @param r
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 * @throws HttpException
 	 * @throws InterruptedException
 	 */
 	public boolean save(ActiveResource r) throws ClientProtocolException,
 			IOException, URISyntaxException, HttpException,
 			InterruptedException {
 		if (r.isNew())
 			return create(r);
 		else
 			return update(r);
 	}
 
 	/**
 	 * repopulate a local object with data from the service
 	 * 
 	 * @param r
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public void reload(ActiveResource r) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		String url = "/" + getCollectionName() + "/" + r.getId()
 				+ getResourceFormat().extension();
 		fetchOne(url, r);
 	}
 
 	/**
 	 * delete a resource
 	 * 
 	 * @param r
 	 * @throws ClientError
 	 * @throws ServerError
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void delete(ActiveResource r) throws ClientError, ServerError,
 			ClientProtocolException, IOException {
 		URLBuilder url = collectionURL.add(r.getId()
 				+ getResourceFormat().extension());
 		connection.delete(url.toString());
 	}
 
 	/**
 	 * Create one object from the response of a given url. If your subclass
 	 * wants to create a bunch of cool find methods that each generate a proper
 	 * URL, they can then use this method to get the data and create the objects
 	 * from the response.
 	 * 
 	 * @param <T>
 	 * @param url
 	 * @return a new object representing the data returned by the url
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> T fetchOne(Object url)
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
		return deserializeOne(connection.get(url.toString()));
 	}
 
 	/**
 	 * Inflate (or unmarshall) an object from serialized data
 	 * 
 	 * @param <T>
 	 * @param data
 	 *            a string of serialized data
 	 * @return a new object
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends ActiveResource> T deserializeOne(String data)
 			throws IOException {
 		T obj = (T) xstream.fromXML(data);
 		obj.setFactory(this);
 		return obj;
 	}
 
 	/**
 	 * Update an existing object with data from the given url
 	 * 
 	 * @param <T>
 	 * @param url
 	 * @param resource
 	 *            the object to update
 	 * @return the updated resource object you passed in
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> T fetchOne(String url, T resource)
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
 		return deserializeAndUpdateOne(connection.get(url), resource);
 	}
 
 	/**
 	 * Weasel method to update an existing object with serialized data.
 	 * 
 	 * @param <T>
 	 * @param data
 	 *            serialized data
 	 * @param resource
 	 *            the object to update
 	 * @return the updated resource object you passed in
 	 * @throws IOException
 	 */
 	public <T extends ActiveResource> T deserializeAndUpdateOne(String data,
 			T resource) throws IOException {
 		xstream.fromXML(data, resource);
 		resource.setFactory(this);
 		return resource;
 	}
 
 	/**
 	 * Create an array of objects from the response of a given url.
 	 * 
 	 * @param <T>
 	 * @param url
 	 * @return an array of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedExceptionInflate
 	 *             (or unmarshall) a list of objects from a stream
 	 * @throws URISyntaxException
 	 */
 	public <T extends ActiveResource> ArrayList<T> fetchMany(Object url)
 			throws HttpException, IOException, InterruptedException,
 			URISyntaxException {
 		return deserializeMany(connection.getStream(url.toString()));
 	}
 
 	/**
 	 * Inflate (or unmarshall) a list of objects from a stream using XStream.
 	 * This method exhausts and closes the stream.
 	 * 
 	 * @param <T>
 	 * @param stream
 	 *            an open input stream
 	 * @return a list of objects
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends ActiveResource> ArrayList<T> deserializeMany(
 			BufferedReader stream) throws IOException {
 
 		ObjectInputStream ostream = xstream.createObjectInputStream(stream);
 
 		ArrayList<T> list = new ArrayList<T>();
 		T obj;
 		while (true) {
 			try {
 				obj = (T) ostream.readObject();
 				obj.setFactory(this);
 				list.add(obj);
 			} catch (EOFException e) {
 				break;
 			} catch (ClassNotFoundException e) {
 				// do nothing
 			}
 		}
 		ostream.close();
 		return list;
 	}
 
 	/**
 	 * by default, we use XML as the format for data exchange
 	 * 
 	 * You can override this in your subclasses to change it
 	 * 
 	 * @return a resource format
 	 */
 	protected ResourceFormat getResourceFormat() {
 		return ResourceFormat.XML;
 	}
 
 	/**
 	 * figure out the name of the collection of resources generated by this
 	 * factory.
 	 * 
 	 * This method first looks for a CollectionName annotation on the class it
 	 * knows how to create. If there is no annotation, then it guesses based on
 	 * the name of the class.
 	 * 
 	 * @return the name of the collection
 	 */
 	protected String getCollectionName() {
 		String name;
 		CollectionName cn = clazz.getAnnotation(CollectionName.class);
 		if (cn != null) {
 			name = cn.value();
 		} else {
 			name = Inflector.underscore(clazz.getSimpleName());
 			name = Inflector.pluralize(name);
 		}
 		return name;
 	}
 
 }
