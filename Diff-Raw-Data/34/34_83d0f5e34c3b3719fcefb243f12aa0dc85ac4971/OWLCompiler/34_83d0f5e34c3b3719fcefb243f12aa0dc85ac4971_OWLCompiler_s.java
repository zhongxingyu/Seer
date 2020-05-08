 /*
  * Copyright (c) 2007-2009, James Leigh All rights reserved.
  * Copyright (c) 2011 Talis Inc., Some rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */
 package org.openrdf.repository.object.compiler;
 
 import info.aduna.io.FileUtil;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.ConnectException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.openrdf.annotations.Iri;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.compiler.model.RDFClass;
 import org.openrdf.repository.object.compiler.model.RDFOntology;
 import org.openrdf.repository.object.compiler.model.RDFProperty;
 import org.openrdf.repository.object.compiler.source.JavaCompiler;
 import org.openrdf.repository.object.exceptions.ObjectStoreConfigException;
 import org.openrdf.repository.object.managers.LiteralManager;
 import org.openrdf.repository.object.managers.RoleMapper;
 import org.openrdf.repository.object.vocabulary.MSG;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Converts OWL ontologies into Java source code.
  * 
  * @author James Leigh
  * 
  */
 public class OWLCompiler {
 	private static final String META_INF_ANNOTATIONS = "META-INF/org.openrdf.annotations";
 	private static final String META_INF_BEHAVIOURS = "META-INF/org.openrdf.behaviours";
 	private static final String META_INF_CONCEPTS = "META-INF/org.openrdf.concepts";
 	private static final String META_INF_DATATYPES = "META-INF/org.openrdf.datatypes";
 	private static final String META_INF_ONTOLOGIES = "META-INF/org.openrdf.ontologies";
 
 	private class AnnotationBuilder implements Runnable {
 		private final RDFProperty bean;
 		private final List<String> content;
 		private final File target;
 
 		private AnnotationBuilder(File target, List<String> content,
 				RDFProperty bean) {
 			this.target = target;
 			this.content = content;
 			this.bean = bean;
 		}
 
 		public void run() {
 			try {
 				bean.generateAnnotationCode(target, resolver);
 				URI uri = bean.getURI();
 				String pkg = resolver.getPackageName(uri);
 				String className = resolver.getSimpleName(uri);
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					annotations.add(className);
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private class ConceptBuilder implements Runnable {
 		private final RDFClass bean;
 		private final List<String> content;
 		private final File target;
 
 		private ConceptBuilder(File target, List<String> content, RDFClass bean) {
 			this.target = target;
 			this.content = content;
 			this.bean = bean;
 		}
 
 		public void run() {
 			try {
 				bean.generateSourceCode(target, resolver);
 				URI uri = bean.getURI();
 				String pkg = resolver.getPackageName(uri);
 				String className = resolver.getSimpleName(uri);
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				boolean anon = resolver.isAnonymous(uri)
 						&& bean.isEmpty(resolver);
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					if (!anon) {
 						concepts.add(className);
 					}
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private final class DatatypeBuilder implements Runnable {
 		private final RDFClass bean;
 		private final List<String> content;
 		private final File target;
 
 		private DatatypeBuilder(List<String> content, RDFClass bean, File target) {
 			this.content = content;
 			this.bean = bean;
 			this.target = target;
 		}
 
 		public void run() {
 			try {
 				for (RDFClass equivalentRdfClass : bean.getRDFClasses(OWL.EQUIVALENTCLASS)) {
 					Class<?> equivalentJavaClass = literals.findClass(equivalentRdfClass.getURI());
 					if (equivalentJavaClass != null) {
 						String equivalentJavaClassname = equivalentJavaClass.getName();
 						List<URI> uris = datatypes.get(equivalentJavaClassname);
 						if (uris == null) {
 							uris = new ArrayList<URI>();
 							uris.add(equivalentRdfClass.getURI());
 							datatypes.put(equivalentJavaClassname, uris);
 						}
 						uris.add(bean.getURI());
 						literals.addDatatype(equivalentJavaClass, bean.getURI());
 						return;
 					}
 				}
 				bean.generateSourceCode(target, resolver);
 				String pkg = resolver.getPackageName(bean.getURI());
 				String className = resolver.getSimpleName(bean.getURI());
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					datatypes.put(className, null);
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private final class BehaviourBuilder implements Runnable {
 		private final RDFClass method;
 		private final Set<String> methods;
 		private final File dir;
 
 		private BehaviourBuilder(File dir, Set<String> methods, RDFClass method) {
 			this.methods = methods;
 			this.method = method;
 			this.dir = dir;
 		}
 
 		public void run() {
 			try {
 				Map<String, String> map = new HashMap<String, String>();
 				Resource subj = method.getResource();
 				map.putAll(model.getNamespaces());
 				for (Resource ctx : model.filter(subj, null, null).contexts()) {
 					if (ns.containsKey(ctx)) {
 						map.putAll(ns.get(ctx));
 					}
 				}
 				Set<String> set = method.msgWriteSource(resolver, map, dir);
 				synchronized (methods) {
 					methods.addAll(set);
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", method);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private static final String JAVA_NS = "java:";
 
 	Runnable helper = new Runnable() {
 		public void run() {
 			try {
 				for (Runnable r = queue.take(); r != helper; r = queue.take()) {
 					r.run();
 				}
 			} catch (InterruptedException e) {
 				logger.error(e.toString(), e);
 			}
 		}
 	};
 	final Logger logger = LoggerFactory.getLogger(OWLCompiler.class);
 	BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
 	private String[] baseClasses = new String[0];
 	private Set<String> annotations = new TreeSet<String>();
 	private Set<String> concepts = new TreeSet<String>();
 	private Map<String, List<URI>> datatypes = new HashMap<String, List<URI>>();
 	private Exception exception;
 	private LiteralManager literals;
 	private RoleMapper mapper;
 	private String memPrefix;
 	private Model model;
 	/** context -&gt; prefix -&gt; namespace */
 	private Map<URI, Map<String, String>> ns = new HashMap<URI, Map<String, String>>();
 	private String pkgPrefix = "";
 	private JavaNameResolver resolver;
 	private Collection<URL> ontologies;
 	private JavaCompiler compiler = new JavaCompiler();
 	private ClassLoader cl = Thread.currentThread().getContextClassLoader();
 	private OwlNormalizer normalizer;
 	private boolean pluralForms = false;
 
 	/**
 	 * Constructs a new compiler instance using the
 	 * existing Java classes referenced in the {@link RoleMapper} and
 	 * {@link LiteralManager}.
 	 * 
 	 */
 	public OWLCompiler(RoleMapper mapper, LiteralManager literals) {
 		assert mapper != null && literals != null;
 		this.mapper = mapper;
 		this.literals = literals;
 	}
 
 	/**
 	 * If an attempt is made to convert Set property names to their plural form.
 	 */
 	public boolean isPluralForms() {
 		return pluralForms;
 	}
 
 	public void setPluralForms(boolean enabled) {
 		this.pluralForms = enabled;
 	}
 
 	/**
 	 * Assigns the schema that will be compiled.
 	 * 
 	 * @param model contains all relevant triples
 	 */
 	public void setModel(Model model) {
 		assert model != null;
 		this.model = model;
 		normalizer = new OwlNormalizer(new RDFDataSource(model));
 		normalizer.normalize();
 	}
 
 	/**
 	 * All Java classes created will use prepend this to their package name.
 	 * Must be called before {@link #init()}.
 	 */
 	public void setPackagePrefix(String prefix) {
 		if (prefix == null) {
 			this.pkgPrefix = "";
 		} else {
 			this.pkgPrefix = prefix;
 		}
 	}
 
 	/**
 	 * Override all the prefixes used in the model namespaces to this one. Must be
 	 * called before {@link #init()}.
 	 */
 	public void setMemberPrefix(String prefix) {
 		this.memPrefix = prefix;
 	}
 
 	/**
 	 * Sets the prefixes for namespaces used in each graph of the model
 	 */
 	public void setPrefixNamespaces(Map<URI, Map<String, String>> namespaces) {
 		this.ns = namespaces;
 	}
 
 	/**
 	 * Set the classpath used when compiling.
 	 */
 	public void setClassLoader(ClassLoader cl) {
 		this.cl = cl;
 	}
 
 	/**
 	 * All concepts created will extend the give baseClasses.
 	 */
 	public void setBaseClasses(String[] baseClasses) {
 		assert baseClasses != null;
 		this.baseClasses = baseClasses;
 	}
 
 	/**
 	 * The given ontologies will be downloaded and included in the concept jar
 	 * as resources.
 	 */
 	public void setOntologies(Collection<URL> ontologies) {
 		this.ontologies = ontologies;
 	}
 
 	/**
 	 * Build concepts and behaviours, compile them and save them to this jar file
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if no concepts found
 	 * @return a ClassLoader with in jar included
 	 */
 	public ClassLoader createJar(File jar) throws RepositoryException,
 			ObjectStoreConfigException {
 		try {
 			File target = createTempDir(getClass().getSimpleName());
 			compile(target);
 			JarPacker packer = new JarPacker(target);
 			packer.packageJar(jar);
 			FileUtil.deleteDir(target);
 			return new URLClassLoader(new URL[] { jar.toURI().toURL() }, cl);
 		} catch (ObjectStoreConfigException e) {
 			throw e;
 		} catch (RepositoryException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new RepositoryException(e);
 		}
 	}
 
 	/**
 	 * Build and compile concepts and behaivours to this directory.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if no concepts found
 	 * @return list of compiled classes
 	 */
 	public List<String> compile(File dir) throws Exception {
 		if (resolver == null) {
 			resolver = buildJavaNameResolver(pkgPrefix, memPrefix, ns, model,
 					normalizer, cl);
 		}
 		List<String> classes = buildJavaFiles(dir);
 		saveConceptResources(dir);
 		List<File> classpath = getClassPath(cl);
 		compiler.compile(classes, dir, classpath);
 		return classes;
 	}
 
 	/**
 	 * Build concepts in this directory
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if no concepts found
 	 * @return list of concept classes created
 	 */
 	public List<String> buildJavaFiles(File dir) throws Exception {
 		if (resolver == null) {
 			resolver = buildJavaNameResolver(pkgPrefix, memPrefix, ns, model,
 					normalizer, cl);
 		}
 		if (baseClasses.length > 0) {
 			Set<Resource> classes = model.filter(null, RDF.TYPE, OWL.CLASS)
 					.subjects();
 			for (Resource o : new ArrayList<Resource>(classes)) {
 				RDFClass bean = new RDFClass(model, o);
 				if (bean.getURI() == null)
 					continue;
 				if (bean.isDatatype())
 					continue;
 				if (mapper.isRecordedConcept(bean.getURI(), cl))
 					continue;
 				addBaseClass(bean);
 			}
 		}
 		List<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < 3; i++) {
 			threads.add(new Thread(helper));
 		}
 		for (Thread thread : threads) {
 			thread.start();
 		}
 		Set<String> usedNamespaces = new HashSet<String>();
 		List<String> content = new ArrayList<String>();
         for (Resource o : model.filter(null, RDF.TYPE, RDFS.DATATYPE)
 				.subjects()) {
 			RDFClass bean = new RDFClass(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (literals.isRecordedeType(bean.getURI()))
 				continue;
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			new DatatypeBuilder(content, bean, dir).run();
 		}
 		for (Resource o : model.filter(null, RDF.TYPE, OWL.ANNOTATIONPROPERTY)
 				.subjects()) {
 			RDFProperty bean = new RDFProperty(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (mapper.isRecordedAnnotation(bean.getURI()))
 				continue;
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			queue.add(new AnnotationBuilder(dir, content, bean));
 		}
 		for (Resource o : model.filter(null, RDF.TYPE, OWL.CLASS).subjects()) {
 			RDFClass bean = new RDFClass(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (bean.isDatatype())
 				continue;
 			if (mapper.isRecordedConcept(bean.getURI(), cl)) {
 				if ("java:".equals(bean.getURI().getNamespace()))
 					continue;
 				if (isComplete(bean, mapper.findRoles(bean.getURI()), resolver))
 					continue;
 			}
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			queue.add(new ConceptBuilder(dir, content, bean));
 		}
 		Set<String> methods = new HashSet<String>();
 		for (RDFClass method : getMethods()) {
 			queue.add(new BehaviourBuilder(dir, methods, method));
 		}
 		for (int i = 0, n = threads.size(); i < n; i++) {
 			queue.add(helper);
 		}
 		for (String namespace : usedNamespaces) {
 			if (JAVA_NS.equals(namespace))
 				continue;
 			RDFOntology ont = findOntology(namespace);
 			ont.generatePackageInfo(dir, namespace, resolver);
 			String pkg = resolver.getBoundPackageName(namespace);
 			if (pkg != null) {
 				String className = pkg + ".package-info";
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 				}
 			}
 		}
 		for (Thread thread1 : threads) {
 			thread1.join();
 		}
 		if (exception != null)
 			throw exception;
 		if (content.isEmpty())
 			throw new IllegalArgumentException(
 					"No classes found - Try a different namespace.");
 		if (!methods.isEmpty()) {
 			printClasses(methods, dir, META_INF_BEHAVIOURS);
 			content.addAll(methods);
 		}
 		return content;
 	}
 
 	/**
 	 * Save META-INF resource for concepts in this parent directory. This method
 	 * must be called after {@link #buildConcepts(File)}.
 	 * 
 	 * @return <code>true</code> if any resources were created
 	 */
 	public void saveConceptResources(File dir) throws IOException {
 		if (!annotations.isEmpty()) {
 			printClasses(annotations, dir, META_INF_ANNOTATIONS);
 		}
 		if (!concepts.isEmpty()) {
 			printClasses(concepts, dir, META_INF_CONCEPTS);
 		}
 		if (!datatypes.isEmpty()) {
             printDatatypes(datatypes, dir, META_INF_DATATYPES);
         }
 		if (ontologies != null) {
 			packOntologies(ontologies, dir, META_INF_ONTOLOGIES);
 		}
 	}
 
 	private void addBaseClass(RDFClass klass) {
 		if (klass.getRDFClasses(RDFS.SUBCLASSOF).isEmpty()) {
 			for (String b : baseClasses) {
 				URI name = new URIImpl(JAVA_NS + b);
 				model.add(klass.getURI(), RDFS.SUBCLASSOF, name);
 			}
 		}
 	}
 
 	private File asLocalFile(URL rdf) throws UnsupportedEncodingException {
 		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
 	}
 
 	private boolean isComplete(RDFClass bean, Collection<Class<?>> roles,
 			JavaNameResolver resolver) {
 		loop: for (RDFProperty prop : bean.getDeclaredProperties()) {
 			if (prop.getURI() == null)
 				continue;
 			String iri = prop.getURI().stringValue();
 			for (Class<?> role : roles) {
 				for (Method m : role.getMethods()) {
 					if (m.isAnnotationPresent(Iri.class)
 							&& iri.equals(m.getAnnotation(Iri.class).value()))
 						continue loop;
 				}
 			}
 			return false;
 		}
 		loop: for (RDFClass type : bean.getDeclaredMessages(resolver)) {
 			if (type.getURI() == null)
 				continue;
 			String iri = type.getURI().stringValue();
 			for (Class<?> role : roles) {
 				for (Method m : role.getMethods()) {
 					if (m.isAnnotationPresent(Iri.class)
 							&& iri.equals(m.getAnnotation(Iri.class).value()))
 						continue loop;
 				}
 			}
 			return false;
 		}
 		loop: for (RDFClass sups : bean.getRDFClasses(RDFS.SUBCLASSOF)) {
 			if (sups.getURI() == null)
 				continue;
 			String iri = sups.getURI().stringValue();
 			for (Class<?> role : roles) {
 				for (Class<?> face : role.getInterfaces()) {
 					if (face.isAnnotationPresent(Iri.class)) {
 						if (iri.equals(face.getAnnotation(Iri.class).value()))
 							continue loop;
 					}
 				}
 				Class<?> parent = role.getSuperclass();
 				if (parent != null && parent.isAnnotationPresent(Iri.class)) {
 					if (iri.equals(parent.getAnnotation(Iri.class).value()))
 						continue loop;
 				}
 			}
 			return false;
 		}
 		// TODO check annotations
 		return true;
 	}
 
 	private File createTempDir(String name) throws IOException {
 		String tmpDirStr = System.getProperty("java.io.tmpdir");
 		if (tmpDirStr != null) {
 			File tmpDir = new File(tmpDirStr);
 			if (!tmpDir.exists()) {
 				tmpDir.mkdirs();
 			}
 		}
 		File tmp = File.createTempFile(name, "");
 		tmp.delete();
 		tmp.mkdir();
 		return tmp;
 	}
 
 	private List<RDFClass> getMethods() throws Exception {
 		List<RDFClass> methods = new ArrayList<RDFClass>();
 		for (URI body : MSG.MESSAGE_IMPLS) {
 			for (Resource subj : model.filter(null, body, null).subjects()) {
 				RDFClass rc = new RDFClass(model, subj);
 				if (rc.isA(OWL.CLASS)) {
 					methods.add(rc);
 				}
 			}
 		}
 		return methods;
 	}
 
 	private JavaNameResolver buildJavaNameResolver(String pkgPrefix,
 			String memberPrefix, Map<URI, Map<String, String>> namespaces,
 			Model model, OwlNormalizer normalizer, ClassLoader cl) {
 		if (model == null)
 			throw new IllegalStateException("setModel not called");
 		/** namespace -&gt; package */
 		Map<String, String> packages = new HashMap<String, String>();
 		for (String ns : findUndefinedNamespaces(model, cl)) {
 			String prefix = findPrefix(ns, model);
 			String pkgName = pkgPrefix + prefix;
 			pkgName = packageName(pkgName);
 			packages.put(ns, pkgName);
 		}
 		JavaNameResolver resolver = createJavaNameResolver(packages,
 				memberPrefix, namespaces, cl);
 		for (URI uri : normalizer.getAnonymousClasses()) {
 			resolver.assignAnonymous(uri);
 		}
 		for (Map.Entry<URI, URI> e : normalizer.getAliases().entrySet()) {
 			resolver.assignAlias(e.getKey(), e.getValue());
 		}
 		resolver.setImplNames(normalizer.getImplNames());
 		for (Resource o : model.filter(null, RDF.TYPE, OWL.CLASS).subjects()) {
 			RDFClass bean = new RDFClass(model, o);
 			URI uri = bean.getURI();
 			if (uri == null || bean.isDatatype())
 				continue;
 			if (!"java:".equals(uri.getNamespace())
 					&& mapper.isRecordedConcept(uri, cl)
 					&& !isComplete(bean, mapper.findRoles(uri), resolver)) {
 				resolver.ignoreExistingClass(uri);
 				String ns = uri.getNamespace();
 				if (!packages.containsKey(ns)) {
 					String prefix = findPrefix(ns, model);
 					String pkgName = pkgPrefix + prefix;
 					pkgName = packageName(pkgName);
 					packages.put(ns, pkgName);
 				}
 			}
 		}
 		for (Map.Entry<String, String> e : packages.entrySet()) {
 			resolver.bindPackageToNamespace(e.getValue(), e.getKey());
 		}
 		return resolver;
 	}
 
 	private String packageName(String pkgName) {
 		if (!Character.isLetter(pkgName.charAt(0))) {
 			pkgName = "_" + pkgName;
 		}
 		return pkgName.replaceAll("\\.([^a-zA-Z])", "._$1");
 	}
 
 	private JavaNameResolver createJavaNameResolver(
 			Map<String, String> packages, String memberPrefix,
 			Map<URI, Map<String, String>> namespaces, ClassLoader cl) {
 		JavaNameResolver resolver = new JavaNameResolver(cl);
 		resolver.setPluralForms(pluralForms);
 		resolver.setModel(model);
 		for (Map.Entry<String, String> e : packages.entrySet()) {
 			resolver.bindPackageToNamespace(e.getValue(), e.getKey());
 		}
 		for (Map.Entry<String, String> e : model.getNamespaces().entrySet()) {
 			resolver.bindPrefixToNamespace(e.getKey(), e.getValue());
 		}
 		if (memberPrefix == null) {
 			for (Map<String, String> p : namespaces.values()) {
 				for (Map.Entry<String, String> e : p.entrySet()) {
 					resolver.bindPrefixToNamespace(e.getKey(), e.getValue());
 				}
 			}
 		} else {
 			for (Map.Entry<String, String> e : packages.entrySet()) {
 				resolver.bindPrefixToNamespace(memberPrefix, e.getKey());
 			}
 		}
 		resolver.setRoleMapper(mapper);
 		resolver.setLiteralManager(literals);
 		return resolver;
 	}
 
 	private RDFOntology findOntology(String namespace) {
 		if (namespace.endsWith("#"))
 			return new RDFOntology(model, new URIImpl(namespace.substring(0,
 					namespace.length() - 1)));
 		return new RDFOntology(model, new URIImpl(namespace));
 	}
 
 	private String findPrefix(String ns, Model model) {
 		for (Map.Entry<String, String> e : model.getNamespaces().entrySet()) {
 			if (ns.equals(e.getValue()) && e.getKey().length() > 0) {
 				return e.getKey();
 			}
 		}
		if (ns.startsWith("http://") && !ns.endsWith(".ttl#")
				&& !ns.startsWith("http://localhost")
				&& !ns.startsWith("http://127.")
				&& !ns.startsWith("http://example.")) {
 			String prefix = NamespacePrefixService.getInstance().prefix(ns);
 			if (prefix != null && model.getNamespace(prefix) == null) {
 				model.setNamespace(prefix, ns);
 				return prefix;
 			}
 		}
 		return "ns" + Integer.toHexString(ns.hashCode());
 	}
 
 	private Set<String> findUndefinedNamespaces(Model model, ClassLoader cl) {
 		Set<String> unknown = new HashSet<String>();
 		for (Resource subj : model.filter(null, RDF.TYPE, null).subjects()) {
 			if (subj instanceof URI) {
 				URI uri = (URI) subj;
 				String ns = uri.getNamespace();
 				if (!mapper.isRecordedConcept(uri, cl)
 						&& !literals.isRecordedeType(uri)
 						&& !mapper.isRecordedAnnotation(uri)) {
 					unknown.add(ns);
 				}
 			}
 		}
 		return unknown;
 	}
 
 	private List<File> getClassPath(ClassLoader cl)
 			throws UnsupportedEncodingException {
 		List<File> classpath = new ArrayList<File>();
 		String classPath = System.getProperty("java.class.path");
 		for (String path : classPath.split(File.pathSeparator)) {
 			classpath.add(new File(path));
 		}
 		getClassPath(classpath, cl);
 		return getClassPath(classpath, getClass().getClassLoader());
 	}
 
 	private List<File> getClassPath(List<File> classpath, ClassLoader cl)
 			throws UnsupportedEncodingException {
 		if (cl == null) {
 			return classpath;
 		} else if (cl instanceof URLClassLoader) {
 			for (URL jar : ((URLClassLoader) cl).getURLs()) {
 				File file = asLocalFile(jar);
 				if (!classpath.contains(file)) {
 					classpath.add(file);
 				}
 			}
 		}
 		return getClassPath(classpath, cl.getParent());
 	}
 
 	private void printClasses(Collection<String> roles, File dir, String entry)
 			throws IOException {
 		File f = new File(dir, entry);
 		f.getParentFile().mkdirs();
 		PrintStream out = new PrintStream(new FileOutputStream(f));
 		try {
 			for (String name : roles) {
 				out.println(name);
 			}
 		} finally {
 			out.close();
 		}
 	}
 
 	private void printDatatypes(Map<String, List<URI>> datatypes, File dir, String META_INF_DATATYPES) throws FileNotFoundException {
 		File f = new File(dir, META_INF_DATATYPES);
 		f.getParentFile().mkdirs();
 		PrintStream out = new PrintStream(new FileOutputStream(f));
 		try {
 		    for (Map.Entry<String, List<URI>> entry : datatypes.entrySet()) {
 		        StringBuilder sb = new StringBuilder(entry.getKey());
 		        if (entry.getValue() != null) {
 		            StringBuilder temp = new StringBuilder();
 		            for (URI uri : entry.getValue()) {
 		                if (temp.length() > 0) {
 		                    temp.append(' ');
 		                }
 		                temp.append(uri.stringValue());
 		            }
 		            if (temp.length() > 0) {
 		                sb.append('=').append(temp);
 		            }
 		        }
 		        out.println(sb);
 		    }
 		} finally {
 		    out.close();
 		}
 	}
 
 	private void packOntologies(Collection<URL> rdfSources, File dir, String META_INF_ONTOLOGIES)
 			throws IOException {
 		File list = new File(dir, META_INF_ONTOLOGIES);
 		list.getParentFile().mkdirs();
 		PrintStream inf = new PrintStream(new FileOutputStream(list));
 		try {
 			for (URL rdf : rdfSources) {
 				String path = "META-INF/ontologies/";
 				path += asLocalFile(rdf).getName();
 				try {
 					InputStream in = rdf.openStream();
 					try {
 						File file = new File(dir, path);
 						file.getParentFile().mkdirs();
 						OutputStream out = new FileOutputStream(file);
 						try {
 							int read;
 							byte[] buf = new byte[1024];
 							while ((read = in.read(buf)) >= 0) {
 								out.write(buf, 0, read);
 							}
 						} finally {
 							out.close();
 						}
 					} finally {
 						in.close();
 					}
 					inf.println(path);
 				} catch (ConnectException exc) {
 					throw new IOException("Cannot connect to " + rdf, exc);
 				}
 			}
 		} finally {
 			inf.close();
 		}
 	}
 
 }
