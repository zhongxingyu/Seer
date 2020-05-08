 package org.openrdf.alibaba.decor;
 
 import java.io.BufferedReader;
 import java.io.CharArrayWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import junit.framework.TestCase;
 
 import org.openrdf.alibaba.decor.helpers.Context;
 import org.openrdf.alibaba.exceptions.AlibabaException;
 import org.openrdf.alibaba.factories.DisplayFactory;
 import org.openrdf.alibaba.factories.PerspectiveFactory;
 import org.openrdf.alibaba.formats.Layout;
 import org.openrdf.alibaba.pov.Display;
 import org.openrdf.alibaba.pov.Expression;
 import org.openrdf.alibaba.pov.Intent;
 import org.openrdf.alibaba.pov.SearchPattern;
 import org.openrdf.alibaba.vocabulary.ALI;
 import org.openrdf.concepts.foaf.Person;
 import org.openrdf.concepts.owl.DatatypeProperty;
 import org.openrdf.concepts.rdf.Seq;
 import org.openrdf.concepts.rdfs.Class;
 import org.openrdf.elmo.ElmoManager;
 import org.openrdf.elmo.ElmoManagerFactory;
 import org.openrdf.elmo.sesame.SesameManagerFactory;
 import org.openrdf.model.vocabulary.FOAF;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.realiser.StatementRealiserRepository;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.sail.memory.MemoryStore;
 
 public class JsonTest extends TestCase {
 	private static final String POVS_PROPERTIES = "META-INF/org.openrdf.alibaba.povs";
 
 	private static final String DECORS_PROPERTIES = "META-INF/org.openrdf.alibaba.decors";
 
 	private static final String SELECT_PERSON = "PREFIX rdf:<"
 			+ RDF.NAMESPACE
 			+ ">\n"
 			+ "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
 			+ "SELECT DISTINCT ?person "
 			+ "WHERE {?person rdf:type foaf:Person ; foaf:name ?name ; foaf:surname ?surname}";
 
 	private static final String NS = "http://www.example.com/rdf/2007/";
 
 	private Repository repository;
 
 	private ElmoManager manager;
 
 	public void testImport() throws Exception {
 		DatatypeProperty namep = manager.designate(DatatypeProperty.class,
 				new QName(FOAF.NAMESPACE, "name"));
 		DatatypeProperty surnamep = manager.designate(DatatypeProperty.class,
 				new QName(FOAF.NAMESPACE, "surname"));
 		Display name = createPropertyDisplay(namep);
 		Display surname = createPropertyDisplay(surnamep);
 
 		Seq list = manager.designate(Seq.class);
 		list.add(name);
 		list.add(surname);
 		Expression expression = manager.designate(Expression.class);
 		expression.setPovInSparql(SELECT_PERSON);
 		SearchPattern query = manager.designate(SearchPattern.class, new QName(NS,
 				"test-import"));
 		query.setPovSelectExpression(expression);
 		query.setPovLayout((Layout) manager.find(new QName(ALI.NS, "table")));
 		query.setPovDisplays(list);
 		// create data
 		Person megan = manager.designate(Person.class, new QName(NS, "megan"));
 		megan.getFoafNames().add("Megan");
 		megan.getFoafSurnames().add("Smith");
 		Person kelly = manager.designate(Person.class, new QName(NS, "kelly"));
 		kelly.getFoafNames().add("Kelly");
 		kelly.getFoafSurnames().add("Smith");
 		// test
 		String string = "[{'name':['Megan'],'surname':['Smith','Leigh']},{'name':['Kelly'],'surname':['Smith']}]";
 		save(string, query, Collections.EMPTY_MAP, null);
 		megan = (Person) manager.find(new QName(NS, "megan"));
 		assertEquals(new HashSet(Arrays.asList("Smith", "Leigh")), megan
 				.getFoafSurnames());
 		// test
 		string = "[{'name':['Megan'],'surname':['Leigh']},{'name':['Kelly'],'surname':['Smith']}]";
 		save(string, query, Collections.EMPTY_MAP, null);
 		megan = (Person) manager.find(new QName(NS, "megan"));
 		assertEquals("Leigh", megan.getFoafSurnames().toArray()[0]);
 	}
 
 	public void testExport() throws Exception {
 		DatatypeProperty namep = manager.designate(DatatypeProperty.class,
 				new QName(FOAF.NAMESPACE, "name"));
 		DatatypeProperty surnamep = manager.designate(DatatypeProperty.class,
 				new QName(FOAF.NAMESPACE, "surname"));
 		Display name = createPropertyDisplay(namep);
 		Display surname = createPropertyDisplay(surnamep);
 
 		Seq list = manager.designate(Seq.class);
 		list.add(name);
 		list.add(surname);
 		Expression expression = manager.designate(Expression.class);
 		expression.setPovInSparql(SELECT_PERSON);
 		SearchPattern query = manager.designate(SearchPattern.class);
 		query.setPovSelectExpression(expression);
 		query.setPovLayout((Layout) manager.find(new QName(ALI.NS, "table")));
 		query.setPovDisplays(list);
 		Person megan = manager.designate(Person.class, new QName(NS, "megan"));
 		megan.getFoafNames().add("Megan");
 		megan.getFoafSurnames().add("Smith");
 		Person kelly = manager.designate(Person.class, new QName(NS, "kelly"));
 		kelly.getFoafNames().add("Kelly");
 		kelly.getFoafSurnames().add("Smith");
 		String string = "[{'name':['Megan'],'surname':['Smith']},{'name':['Kelly'],'surname':['Smith']}]";
 		assertEquals(string, load(query, Collections.EMPTY_MAP, null));
 	}
 
 	private Display createPropertyDisplay(DatatypeProperty property) {
 		PerspectiveFactory pf = (PerspectiveFactory) manager
 				.find(ALI.PERSPECTIVE_FACTORY);
 		DisplayFactory df = pf.getPovDisplayFactory();
 		Display display = df.createDisplay(property);
 		return display;
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		repository = new SailRepository(new MemoryStore());
 		repository = new StatementRealiserRepository(repository);
 		repository.initialize();
 		RepositoryConnection conn = repository.getConnection();
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		loadPropertyKeysAsResource(conn, cl, POVS_PROPERTIES);
 		loadPropertyKeysAsResource(conn, cl, DECORS_PROPERTIES);
 		conn.close();
 		ElmoManagerFactory factory = new SesameManagerFactory(repository);
 		manager = factory.createElmoManager(Locale.US);
		manager.getTransaction().begin();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
		if (manager.getTransaction().isActive()) {
			manager.getTransaction().rollback();
		}
 		manager.close();
 		repository.shutDown();
 	}
 
 	private void loadPropertyKeysAsResource(RepositoryConnection conn,
 			ClassLoader cl, String listing) throws IOException,
 			RDFParseException, RepositoryException {
 		Enumeration<URL> list = cl.getResources(listing);
 		while (list.hasMoreElements()) {
 			Properties prop = new Properties();
 			prop.load(list.nextElement().openStream());
 			for (Object res : prop.keySet()) {
 				URL url = cl.getResource(res.toString());
 				RDFFormat format = RDFFormat.forFileName(url.getFile());
 				conn.add(url, "", format);
 			}
 		}
 	}
 
 	private String load(SearchPattern spec, Map<String, String> parameters,
 			String orderBy) throws AlibabaException, IOException {
 		CharArrayWriter writer = new CharArrayWriter();
 		Intent intention = (Intent) manager.find(ALI.GENERAL);
 		TextPresentation present = (TextPresentation) manager
 				.find(ALI.JSON_PRESENTATION);
 		spec.setPovPurpose(intention);
 		Class type = manager.designate(Class.class);
 		spec.getPovRepresents().add(type);
		manager.getTransaction().commit();
 		Context ctx = new Context(parameters, orderBy);
 		ctx.setElmoManager(manager);
 		ctx.setIntent(intention);
 		ctx.setWriter(new PrintWriter(writer));
 		ctx.setLocale(manager.getLocale());
 		present.exportPresentation(spec, type, ctx);
 		return writer.toString().trim();
 	}
 
 	private void save(String text, SearchPattern spec,
 			Map<String, String> parameters, String orderBy)
 			throws AlibabaException, IOException {
 		StringReader reader = new StringReader(text);
 		Intent intention = (Intent) manager.find(ALI.GENERAL);
 		TextPresentation present = (TextPresentation) manager
 				.find(ALI.JSON_PRESENTATION);
 		spec.setPovPurpose(intention);
 		Class type = manager.designate(Class.class);
 		spec.getPovRepresents().add(type);
		if (manager.getTransaction().isActive()) {
			manager.getTransaction().commit();
		}
 		Context ctx = new Context(parameters, orderBy);
 		ctx.setElmoManager(manager);
 		ctx.setIntent(intention);
 		ctx.setReader(new BufferedReader(reader));
 		ctx.setLocale(manager.getLocale());
 		present.importPresentation(spec, type, ctx);
 	}
 
 }
