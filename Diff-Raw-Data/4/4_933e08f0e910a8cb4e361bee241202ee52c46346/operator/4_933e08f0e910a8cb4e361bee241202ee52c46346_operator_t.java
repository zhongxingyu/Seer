 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
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
 package sorcer.eo;
 
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.lookup.ServiceTemplate;
 import net.jini.core.transaction.Transaction;
 import sorcer.co.tuple.Entry;
 import sorcer.co.tuple.IndexedTriplet;
 import sorcer.co.tuple.Path;
 import sorcer.co.tuple.Tuple2;
 import sorcer.core.Provider;
 import sorcer.core.SorcerConstants;
 import sorcer.core.context.*;
 import sorcer.core.context.ControlContext.ThrowableTrace;
 import sorcer.core.exertion.*;
 import sorcer.core.signature.EvaluationSignature;
 import sorcer.core.signature.NetSignature;
 import sorcer.core.signature.ObjectSignature;
 import sorcer.core.signature.ServiceSignature;
 import sorcer.service.*;
 import sorcer.service.Signature.Direction;
 import sorcer.service.Signature.ReturnPath;
 import sorcer.service.Signature.Type;
 import sorcer.service.Strategy.*;
 import sorcer.util.ExertProcessor;
 import sorcer.util.ServiceAccessor;
 import sorcer.util.Sorcer;
 import sorcer.util.bdb.SosURL;
 import sorcer.util.bdb.objects.SorcerDatabaseViews.Store;
 import sorcer.util.bdb.sdb.SdbUtil;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class operator {
 
 	private static int count = 0;
 
 	private static final Logger logger = Logger.getLogger(operator.class
 			.getName());
 
 	public static String path(List<String> attributes) {
 		if (attributes.size() == 0)
 			return null;
 		if (attributes.size() > 1) {
 			StringBuilder spr = new StringBuilder();
 			for (int i = 0; i < attributes.size() - 1; i++) {
 				spr.append(attributes.get(i)).append(SorcerConstants.CPS);
 			}
 			spr.append(attributes.get(attributes.size() - 1));
 			return spr.toString();
 		}
 		return attributes.get(0);
 	}
 
 	public static Object revalue(Evaluation evaluation, String path,
 			Parameter... entries) throws ContextException {
 		Object obj = value(evaluation, path, entries);
 		if (obj instanceof Evaluation) {
 			obj = value((Evaluation) obj, entries);
 		}
 		return obj;
 	}
 
 	public static Object revalue(Object object, Parameter... entries)
 			throws EvaluationException {
 		Object obj = null;
 		if (object instanceof Evaluation) {
 			obj = value((Evaluation) object, entries);
 		}
 		if (obj == null) {
 			obj = object;
 		} else {
 			if (obj instanceof Evaluation) {
 				obj = value((Evaluation) obj, entries);
 			}
 		}
 		return obj;
 	}
 
 	public static String path(String... attributes) {
 		if (attributes.length == 0)
 			return null;
 		if (attributes.length > 1) {
 			StringBuilder spr = new StringBuilder();
 			for (int i = 0; i < attributes.length - 1; i++) {
 				spr.append(attributes[i]).append(SorcerConstants.CPS);
 			}
 			spr.append(attributes[attributes.length - 1]);
 			return spr.toString();
 		}
 		return attributes[0];
 	}
 
 	public static Complement<String, Object> subject(String path, Object value)
 			throws SignatureException {
 		return new Complement<String, Object>(path, value);
 	}
 
 	public static <T extends Context> T put(T context, Tuple2... entries)
 			throws ContextException {
 		for (int i = 0; i < entries.length; i++) {
 			if (context != null) {
 				context.putValue(((Tuple2<String, ?>) entries[i])._1,
 						((Tuple2<String, ?>) entries[i])._2);
 			}
 		}
 		return context;
 	}
 
 	public static void put(Exertion exertion, Tuple2<String, ?>... entries)
 			throws ContextException {
 		put(exertion.getDataContext(), entries);
 	}
 
 	public static Exertion setContext(Exertion exertion, Context context) {
 		((ServiceExertion) exertion).setContext(context);
 		return exertion;
 	}
 
 	public static ControlContext control(Exertion exertion)
 			throws ContextException {
 		return exertion.getControlContext();
 	}
 
 	public static ControlContext control(Exertion exertion, String childName)
 			throws ContextException {
 		return exertion.getExertion(childName).getControlContext();
 	}
 
 	public static <T extends Object> Context cxt(T... entries)
 			throws ContextException {
 		return context(entries);
 	}
 
 	public static Context jCxt(Job job) throws ContextException {
 		return job.getJobContext();
 	}
 
 	public static Context jobContext(Exertion job) throws ContextException {
 		return ((Job) job).getJobContext();
 	}
 
 	public static DataEntry data(Object data) {
 		return new DataEntry(Context.DSD_PATH, data);
 	}
 
 	public static <T extends Object> Context context(T... entries)
 			throws ContextException {
 		if (entries[0] instanceof Exertion) {
 			Exertion xrt = (Exertion) entries[0];
 			if (entries.length >= 2 && entries[1] instanceof String)
 				xrt = ((Job) xrt).getComponentExertion((String) entries[1]);
 			return xrt.getDataContext();
 		} else if (entries[0] instanceof String
 				&& entries[1] instanceof Exertion) {
 			return ((Job) entries[1]).getComponentExertion((String) entries[0])
 					.getDataContext();
 		}
 		String name = getUnknown();
 		List<Tuple2<String, ?>> entryList = new ArrayList<Tuple2<String, ?>>();
 		// List<inEntry> inEntries = new ArrayList<inEntry>();
 		// List<outEntry> outEntries = new ArrayList<outEntry>();
 		// List<entry> cxtEntries = new ArrayList<entry>();
 		List<Context.Type> types = new ArrayList<Context.Type>();
 		Complement subject = null;
 		ReturnPath returnPath = null;
 		Args cxtArgs = null;
 		ParameterTypes parameterTypes = null;
 		target target = null;
 		for (T o : entries) {
 			if (o instanceof Complement) {
 				subject = (Complement) o;
 			} else if (o instanceof Args
 					&& ((Args) o).args.getClass().isArray()) {
 				cxtArgs = (Args) o;
 			} else if (o instanceof ParameterTypes
 					&& ((ParameterTypes) o).parameterTypes.getClass().isArray()) {
 				parameterTypes = (ParameterTypes) o;
 			} else if (o instanceof target) {
 				target = (target) o;
 			} else if (o instanceof Tuple2) {
 				entryList.add((Tuple2) o);
 			} else if (o instanceof ReturnPath) {
 				returnPath = (ReturnPath) o;
 			} else if (o instanceof Context.Type) {
 				types.add((Context.Type) o);
 			} else if (o instanceof String) {
 				name = (String) o;
 			}
 		}
 		Context cxt = null;
 		if (types.contains(Context.Type.ARRAY)) {
 			if (subject != null)
 				cxt = new ArrayContext(name, subject.path(), subject.value());
 			else
 				cxt = new ArrayContext(name);
 		} else if (types.contains(Context.Type.LIST)) {
 			if (subject != null)
 				cxt = new ListContext(name, subject.path(), subject.value());
 			else
 				cxt = new ListContext(name);
 		} else if (types.contains(Context.Type.SHARED)
 				&& types.contains(Context.Type.INDEXED)) {
 			cxt = new SharedIndexedContext(name);
 		} else if (types.contains(Context.Type.SHARED)) {
 			cxt = new SharedAssociativeContext(name);
 		} else if (types.contains(Context.Type.ASSOCIATIVE)) {
 			if (subject != null)
 				cxt = new ServiceContext(name, subject.path(), subject.value());
 			else
 				cxt = new ServiceContext(name);
 		} else {
 			if (subject != null) {
 				cxt = new PositionalContext(name, subject.path(),
 						subject.value());
 			} else {
 				cxt = new PositionalContext(name);
 			}
 		}
 		if (cxt instanceof PositionalContext) {
 			PositionalContext pcxt = (PositionalContext) cxt;
 			if (entryList.size() > 0) {
 				for (int i = 0; i < entryList.size(); i++) {
 					if (entryList.get(i) instanceof InEntry) {
 						pcxt.putInValueAt(((InEntry) entryList.get(i)).path(),
 								((InEntry) entryList.get(i)).value(), i + 1);
 					} else if (entryList.get(i) instanceof OutEntry) {
 						pcxt.putOutValueAt(
 								((OutEntry) entryList.get(i)).path(),
 								((OutEntry) entryList.get(i)).value(), i + 1);
 					} else if (entryList.get(i) instanceof InoutEntry) {
 						pcxt.putInoutValueAt(
 								((InoutEntry) entryList.get(i)).path(),
 								((InoutEntry) entryList.get(i)).value(), i + 1);
 					} else if (entryList.get(i) instanceof Entry) {
 						pcxt.putValueAt(((Entry) entryList.get(i)).path(),
 								((Entry) entryList.get(i)).value(), i + 1);
 					} else if (entryList.get(i) instanceof DataEntry) {
 						pcxt.putValueAt(Context.DSD_PATH,
 								((DataEntry) entryList.get(i)).value(), i + 1);
 					} else if (entryList.get(i) instanceof Tuple2) {
 						pcxt.putValueAt(
 								entryList.get(i)._1,
 								entryList.get(i)._2,
 								i + 1);
 					}
 				}
 			}
 		} else {
 			if (entryList.size() > 0) {
 				for (int i = 0; i < entryList.size(); i++) {
 					if (entryList.get(i) instanceof InEntry) {
 						cxt.putInValue(((Entry) entryList.get(i)).path(),
 								((Entry) entryList.get(i)).value());
 					} else if (entryList.get(i) instanceof OutEntry) {
 						cxt.putOutValue(((Entry) entryList.get(i)).path(),
 								((Entry) entryList.get(i)).value());
 					} else if (entryList.get(i) instanceof InoutEntry) {
 						cxt.putInoutValue(((Entry) entryList.get(i)).path(),
 								((Entry) entryList.get(i)).value());
 					} else if (entryList.get(i) instanceof Entry) {
 						cxt.putValue(((Entry) entryList.get(i)).path(),
 								((Entry) entryList.get(i)).value());
 					} else if (entryList.get(i) instanceof DataEntry) {
 						cxt.putValue(Context.DSD_PATH,
 								((Entry) entryList.get(i)).value());
 					} else if (entryList.get(i) instanceof Tuple2) {
 						cxt.putValue(
 								entryList.get(i)._1,
 								entryList.get(i)._2);
 					}
 				}
 			}
 		}
 
 		if (returnPath != null)
 			((ServiceContext) cxt).setReturnPath(returnPath);
 		if (cxtArgs != null) {
 			if (cxtArgs.path() != null) {
 				((ServiceContext) cxt).setArgsPath(cxtArgs.path());
 			} else {
 				((ServiceContext) cxt).setArgsPath(Context.PARAMETER_VALUES);
 			}
 			((ServiceContext) cxt).setArgs(cxtArgs.args);
 		}
 		if (parameterTypes != null) {
 			if (parameterTypes.path() != null) {
 				((ServiceContext) cxt).setParameterTypesPath(parameterTypes
 						.path());
 			} else {
 				((ServiceContext) cxt)
 						.setParameterTypesPath(Context.PARAMETER_TYPES);
 			}
 			((ServiceContext) cxt)
 					.setParameterTypes(parameterTypes.parameterTypes);
 		}
 		if (target != null) {
 			if (target.path() != null) {
 				((ServiceContext) cxt).setTargetPath(target.path());
 			}
 			((ServiceContext) cxt).setTarget(target.target);
 		}
 		return cxt;
 	}
 
 	public static List<String> names(List<? extends Identifiable> list) {
 		List<String> names = new ArrayList<String>(list.size());
 		for (Identifiable i : list) {
 			names.add(i.getName());
 		}
 		return names;
 	}
 
 	public static String name(Object identifiable) {
 		if (identifiable instanceof Identifiable)
 			return ((Identifiable) identifiable).getName();
 		else
 			return null;
     }
 
 	public static List<String> names(Identifiable... array) {
 		List<String> names = new ArrayList<String>(array.length);
 		for (Identifiable i : array) {
 			names.add(i.getName());
 		}
 		return names;
 	}
 
 	public static List<Entry> attributes(Entry... entries) {
 		List<Entry> el = new ArrayList<Entry>(entries.length);
 		for (Entry e : entries)
 			el.add(e);
 		return el;
 	}
 
 	/**
 	 * Makes this Revaluation revaluable, so its return value is to be again
 	 * evaluated as well.
      *
 	 * @return an revaluable Evaluation
 	 * @throws EvaluationException
 	 */
 	public static Revaluation revaluable(Revaluation evaluation,
 			Parameter... entries) throws EvaluationException {
 		if (entries != null && entries.length > 0) {
 			try {
 				((Evaluation) evaluation).substitute(entries);
 			} catch (RemoteException e) {
 				throw new EvaluationException(e);
 			}
 		}
 		evaluation.setRevaluable(true);
 		return evaluation;
 	}
 
 	public static Revaluation unrevaluable(Revaluation evaluation) {
 		evaluation.setRevaluable(false);
 		return evaluation;
 	}
 
 	/**
 	 * Returns the Evaluation with a realized substitution for its arguments.
 	 * 
 	 * @param evaluation
 	 * @param entries
 	 * @return an evaluation with a realized substitution
 	 * @throws EvaluationException
 	 * @throws RemoteException
 	 */
 	public static Evaluation substitute(Evaluation evaluation,
 			Parameter... entries) throws EvaluationException, RemoteException {
 		return evaluation.substitute(entries);
 	}
 
 	public static Signature sig(Class<?> serviceType, String providerName,
 			Object... parameters) throws SignatureException {
 		return sig(null, serviceType, providerName, parameters);
 	}
 
 	public static Signature sig(String operation, Class<?> serviceType,
 			String providerName, Object... parameters)
 			throws SignatureException {
 		Signature sig = null;
 		List<Object> typeList = Arrays.asList(parameters);
 		if (serviceType.isInterface()) {
 			sig = new NetSignature(operation, serviceType, providerName);
 		} else {
 			sig = new ObjectSignature(operation, serviceType);
 		}
 		// default Operation type = SERVICE
 		sig.setType(Type.SRV);
 		if (parameters.length > 0) {
 			for (Object o : parameters) {
 				if (o instanceof Type) {
 					sig.setType((Type) o);
 				} else if (o instanceof ReturnPath) {
 					sig.setReturnPath((ReturnPath) o);
 				}
 			}
 		}
 		return sig;
 	}
 
 	public static Signature sig(String selector) throws SignatureException {
 		return new ServiceSignature(selector);
 	}
 
 	public static Signature sig(String name, String selector)
 			throws SignatureException {
 		return new ServiceSignature(name, selector);
 	}
 
 	public static Signature sig(String operation, Class<?> serviceType,
 			Type type) throws SignatureException {
 		return sig(operation, serviceType, (String) null, type);
 	}
 
	public static Signature sig(String operation, Class serviceType) throws SignatureException {
		return sig(operation, serviceType, null, Type.SRV);
	}

 	public static Signature sig(String operation, Class<?> serviceType,
 			Provision type) throws SignatureException {
 		return sig(operation, serviceType, (String) null, type);
 	}
 
 	public static Signature sig(String operation, Class<?> serviceType,
 			List<net.jini.core.entry.Entry> attributes)
 			throws SignatureException {
 		NetSignature op = new NetSignature();
 		op.setAttributes(attributes);
 		op.setServiceType(serviceType);
 		op.setSelector(operation);
 		return op;
 	}
 
 	public static Signature sig(Class<?> serviceType) throws SignatureException {
 		return sig(serviceType, null);
 	}
 
 	public static Signature sig(Class<?> serviceType, ReturnPath returnPath)
 			throws SignatureException {
 		Signature sig = null;
 		if (serviceType.isInterface()) {
 			sig = new NetSignature("service", serviceType);
 		} else if (Executor.class.isAssignableFrom(serviceType)) {
 			sig = new ObjectSignature("execute", serviceType);
 		} else {
 			sig = new ObjectSignature(serviceType);
 		}
 		if (returnPath != null)
 			sig.setReturnPath(returnPath);
 		return sig;
 	}
 
 	public static Signature sig(String operation, Class<?> serviceType,
 			ReturnPath resultPath) throws SignatureException {
 		Signature sig = sig(operation, serviceType, Type.SRV);
 		sig.setReturnPath(resultPath);
 		return sig;
 	}
 
 	public static Signature sig(Exertion exertion, String componentExertionName) {
 		Exertion component = exertion.getExertion(componentExertionName);
 		return component.getProcessSignature();
 	}
 
 	public static String selector(Signature sig) {
 		return sig.getSelector();
 	}
 
 	public static Signature type(Signature signature, Signature.Type type) {
 		signature.setType(type);
 		return signature;
 	}
 
 	public static ObjectSignature sig(String operation, Object object,
 			Class... types) throws SignatureException {
 		try {
 			if (object instanceof Class && ((Class) object).isInterface()) {
 				return new NetSignature(operation, (Class) object);
 			} else {
 				return new ObjectSignature(operation, object,
 						types.length == 0 ? null : types);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new SignatureException(e);
 		}
 	}
 
 	public static ObjectSignature sig(String selector, Object object,
 			Class<?>[] types, Object[] args) throws SignatureException {
 		try {
 			return new ObjectSignature(selector, object, types, args);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new SignatureException(e);
 		}
 	}
 
 	public static ObjectTask task(ObjectSignature signature)
 			throws SignatureException {
 		return new ObjectTask(signature.getSelector(),
 				signature);
 	}
 
     public static Task task(String name, Signature signature, Context context)
             throws SignatureException {
         if (signature instanceof NetSignature) {
             return new NetTask(name, signature, context);
         } else if (signature instanceof ObjectSignature) {
             return new ObjectTask(name, signature, context);
         } else
             return new Task(name, signature, context);
     }
 
 	public static <T> Task batch(String name, T... elems)
 			throws ExertionException {
 		Task batch = task(name, elems);
 		if (batch.getSignatures().size() > 1)
 			return batch;
 		else
 			throw new ExertionException(
 					"A batch task should comprise of more than one signature.");
 	}
 
 	public static <T> Task task(String name, T... elems)
 			throws ExertionException {
 		Context context = null;
 		List<Signature> ops = new ArrayList<Signature>();
 		String tname;
 		if (name == null || name.length() == 0)
 			tname = getUnknown();
 		else
 			tname = name;
 		Task task = null;
 		Access access = null;
 		Flow flow = null;
 		ControlContext cc = null;
 		for (Object o : elems) {
 			if (o instanceof ControlContext) {
 				cc = (ControlContext) o;
 			} else if (o instanceof Context) {
 				context = (Context) o;
 			} else if (o instanceof Signature) {
 				ops.add((Signature) o);
 			} else if (o instanceof String) {
 				tname = (String) o;
 			} else if (o instanceof Access) {
 				access = (Access) o;
 			} else if (o instanceof Flow) {
 				flow = (Flow) o;
 			}
 		}
 		Signature ss = null;
 		if (ops.size() == 1) {
 			ss = ops.get(0);
 		} else if (ops.size() > 1) {
 			for (Signature s : ops) {
 				if (s.getType() == Signature.SRV) {
 					ss = s;
 					break;
 				}
 			}
 		}
 		if (ss != null) {
 			if (ss instanceof NetSignature) {
                 try {
                     task = new NetTask(tname, ss);
                 } catch (SignatureException e) {
                     throw new ExertionException(e);
                 }
 			} else if (ss instanceof ObjectSignature) {
 				try {
 					task = task((ObjectSignature) ss);
 				} catch (SignatureException e) {
 					throw new ExertionException(e);
 				}
 				task.setName(tname);
 			} else if (ss instanceof ServiceSignature) {
 				task = new Task(tname, ss);
 			}
 			ops.remove(ss);
 		}
 		for (Signature signature : ops) {
 			task.addSignature(signature);
 		}
 
 		if (context == null) {
 			context = new ServiceContext();
 		}
 		task.setContext(context);
 
 		if (access != null) {
 			task.setAccess(access);
 		}
 		if (flow != null) {
 			task.setFlow(flow);
 		}
 		if (cc != null) {
 			task.updateStrategy(cc);
 		}
 		return task;
 	}
 
 	public static <T extends Object, E extends Exertion> E srv(String name,
 			T... elems) throws ExertionException, ContextException,
 			SignatureException {
 		return (E) exertion(name, elems);
 	}
 
 	public static <T extends Object, E extends Exertion> E xrt(String name,
 			T... elems) throws ExertionException, ContextException,
 			SignatureException {
 		return (E) exertion(name, elems);
 	}
 
 	public static <T extends Object, E extends Exertion> E exertion(
 			String name, T... elems) throws ExertionException,
 			ContextException, SignatureException {
 		List<Exertion> exertions = new ArrayList<Exertion>();
 		for (int i = 0; i < elems.length; i++) {
 			if (elems[i] instanceof Exertion) {
 				exertions.add((Exertion) elems[i]);
 			}
 		}
 		if (exertions.size() > 0) {
 			Job j = job(elems);
 			j.setName(name);
 			return (E) j;
 		} else {
 			Task t = task(name, elems);
 			return (E) t;
 		}
 	}
 
 	public static <T> Job job(T... elems) throws ExertionException,
 			ContextException, SignatureException {
 		String name = getUnknown();
 		Signature signature = null;
 		ControlContext control = null;
 		Context<?> data = null;
 		List<Exertion> exertions = new ArrayList<Exertion>();
 		List<Pipe> pipes = new ArrayList<Pipe>();
 
 		for (int i = 0; i < elems.length; i++) {
 			if (elems[i] instanceof String) {
 				name = (String) elems[i];
 			} else if (elems[i] instanceof Exertion) {
 				exertions.add((Exertion) elems[i]);
 			} else if (elems[i] instanceof ControlContext) {
 				control = (ControlContext) elems[i];
 			} else if (elems[i] instanceof Context) {
 				data = (Context<?>) elems[i];
 			} else if (elems[i] instanceof Pipe) {
 				pipes.add((Pipe) elems[i]);
 			} else if (elems[i] instanceof Signature) {
 				signature = ((Signature) elems[i]);
 			}
 		}
 
 		Job job = null;
 		if (signature == null) {
 			signature = sig("service", Jobber.class);
 		}
 		if (signature instanceof NetSignature) {
 			job = new NetJob(name);
 		} else if (signature instanceof ObjectSignature) {
 			job = new ObjectJob(name);
 		}
 		job.addSignature(signature);
 		if (data != null)
 			job.setContext(data);
 
 		if (job instanceof NetJob && control != null) {
 			job.setControlContext(control);
 			if (control.getAccessType().equals(Access.PULL)) {
 				Signature procSig = job.getProcessSignature();
 				procSig.setServiceType(Spacer.class);
 				job.getSignatures().clear();
 				job.addSignature(procSig);
 				if (data != null)
 					job.setContext(data);
 				else
 					job.getDataContext().setExertion(job);
 			}
 		}
 		if (exertions.size() > 0) {
 			for (Exertion ex : exertions) {
 				job.addExertion(ex);
 			}
 			for (Pipe p : pipes) {
 				logger.finer("from dataContext: " + p.in.getDataContext().getName()
 						+ " path: " + p.inPath);
 				logger.finer("to dataContext: " + p.out.getDataContext().getName()
 						+ " path: " + p.outPath);
 				p.out.getDataContext().connect(p.outPath, p.inPath,
 						p.in.getDataContext());
 			}
 		} else
 			throw new ExertionException("No component exertion defined.");
 
 		return job;
 	}
 
 	public static Object get(Context<?> context, String path)
 			throws ContextException {
 		return context.getValue(path);
 	}
 
 	public static Object get(Context context) throws ContextException {
 		return context.getReturnValue();
 	}
 
 	public static Object get(Context context, int index)
 			throws ContextException {
 		if (context instanceof PositionalContext)
 			return ((PositionalContext) context).getValueAt(index);
 		else
 			throw new ContextException("Not PositionalContext, index: " + index);
 	}
 
 	public static Object get(Exertion exertion) throws ContextException {
 		return exertion.getContext().getReturnValue();
 	}
 
 	public static <V> V asis(Object evaluation) throws EvaluationException {
 		if (evaluation instanceof Evaluation) {
 			try {
 				synchronized (evaluation) {
 					return ((Evaluation<V>) evaluation).asis();
 				}
 			} catch (RemoteException e) {
 				throw new EvaluationException(e);
 			}
 		} else {
 			throw new EvaluationException(
 					"asis value can only be determined for objects of the "
 							+ Evaluation.class + " type");
 		}
 	}
 
 	public static Object get(Exertion exertion, String component, String path)
 			throws ExertionException {
 		Exertion c = exertion.getExertion(component);
 		return get(c, path);
 	}
 
 	public static Object value(URL url) throws IOException {
 		return url.getContent();
 	}
 
 	public static Object value(SosURL url) throws IOException {
 		return url.getTarget().getContent();
 	}
 
 	public static SosURL set(SosURL url, Object value)
 			throws EvaluationException {
 		URL target = url.getTarget();
 		if (target != null && value != null) {
 			try {
 				if (target.getRef() == null) {
 					url.setTarget(SdbUtil.store(value));
 				} else {
 					SdbUtil.update(target, value);
 				}
 			} catch (Exception e) {
 				throw new EvaluationException(e);
 			}
 		}
 		return url;
 	}
 	
 	public static <T> T value(Evaluation<T> evaluation, Parameter... entries)
 			throws EvaluationException {
 		try {
 			synchronized (evaluation) {
 				if (evaluation instanceof Exertion) {
 					return (T) exec((Exertion) evaluation, entries);
 				} else {
 					return evaluation.getValue(entries);
 				}
 			}
 		} catch (Exception e) {
 			throw new EvaluationException(e);
 		}
 	}
 
 	public static <T> T value(Evaluation<T> evaluation, String evalSelector,
 			Parameter... entries) throws EvaluationException {
 		synchronized (evaluation) {
 			if (evaluation instanceof Exertion) {
 				try {
 					return (T) exec((Exertion) evaluation, entries);
 				} catch (Exception e) {
 					e.printStackTrace();
 					throw new EvaluationException(e);
 				}
 			} else if (evaluation instanceof Context) {
 				try {
 					return (T) ((Context) evaluation).getValue(evalSelector,
 							entries);
 				} catch (Exception e) {
 					e.printStackTrace();
 					throw new EvaluationException(e);
 				}
 			}
 		}
 		return null;
 	}
 
 	public static Object asis(Context context, String path)
 			throws ContextException {
 		return ((ServiceContext) context).getAsis(path);
 	}
 
 	public static List<Exertion> exertions(Exertion xrt) {
 		return xrt.getAllExertions();
 	}
 
 	public static Exertion exertion(Exertion xrt, String componentExertionName) {
 		return ((Job) xrt).getComponentExertion(componentExertionName);
 	}
 
 	public static List<String> trace(Exertion xrt) {
 		return xrt.getControlContext().getTrace();
 	}
 
 	public static void print(Object obj) {
 		System.out.println(obj.toString());
 	}
 
 	public static Object exec(Context context, Parameter... entries)
 			throws ExertionException, ContextException {
 		try {
 			context.substitute(entries);
 		} catch (RemoteException e) {
 			throw new ContextException(e);
 		}
 		ReturnPath returnPath = context.getReturnPath();
 		if (returnPath != null) {
 			return context.getValue(returnPath.path);
 		} else
 			throw new ExertionException("No return path in the dataContext: "
 					+ context.getName());
 	}
 
 	public static Object exec(Exertion exertion, Parameter... entries)
 			throws ExertionException, ContextException {
 		Exertion xrt;
 		try {
 			if (exertion.getClass() == Task.class) {
 				if (((Task) exertion).getInnerTask() != null)
 					xrt = exert(((Task) exertion).getInnerTask(), null, entries);
 				else
 					xrt = exertOpenTask(exertion, entries);
 			} else {
 				xrt = exert(exertion, null, entries);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ExertionException(e);
 		}
 		ReturnPath returnPath = xrt.getDataContext().getReturnPath();
 		if (returnPath != null) {
 			if (xrt instanceof Task) {
 				return xrt.getDataContext().getValue(returnPath.path);
 			} else if (xrt instanceof Job) {
 				return ((Job) xrt).getValue(returnPath.path);
 			}
 		} else {
 			if (xrt instanceof Task) {
 				return xrt.getDataContext();
 			} else if (xrt instanceof Job) {
 				return ((Job) xrt).getJobContext();
 			}
 		}
 		throw new ExertionException("No return path in the exertion: "
 				+ xrt.getName());
 	}
 
 	public static Exertion exertOpenTask(Exertion exertion,
 			Parameter... entries) throws ExertionException {
 		Exertion closedTask = null;
 		List<Parameter> params = Arrays.asList(entries);
 		List<Object> items = new ArrayList<Object>();
 		for (Parameter param : params) {
 			if (param instanceof ControlContext
 					&& ((ControlContext) param).getSignatures().size() > 0) {
 				List<Signature> sigs = ((ControlContext) param).getSignatures();
 				ControlContext cc = (ControlContext) param;
 				cc.setSignatures(null);
 				Context tc = exertion.getDataContext();
 				items.add(tc);
 				items.add(cc);
 				items.addAll(sigs);
 				closedTask = task(exertion.getName(), items.toArray());
 			}
 		}
 		try {
 			closedTask = closedTask.exert(entries);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ExertionException(e);
 		}
 		return closedTask;
 	}
 
 	public static Object get(Exertion xrt, String path)
 			throws ExertionException {
 		try {
 			return ((ServiceExertion) xrt).getValue(path);
 		} catch (ContextException e) {
 			throw new ExertionException(e);
 		}
 	}
 
 	public static List<ThrowableTrace> exceptions(Exertion exertion) {
 		return exertion.getExceptions();
 	}
 
 	public static Task exert(Task input, Entry... entries)
 			throws ExertionException {
 		try {
 			return ((Task) input).exert(null, entries);
 		} catch (Exception e) {
 			throw new ExertionException(e);
 		}
 	}
 
 	public static <T extends Exertion> T exert(Exertion input,
 			Parameter... entries) throws ExertionException {
 		try {
 			return (T) exert(input, null, entries);
 		} catch (Exception e) {
 			throw new ExertionException(e);
 		}
 	}
 
 	public static <T extends Exertion> T exert(T input,
 			Transaction transaction, Parameter... entries)
 			throws ExertionException {
 		try {
 			ExertProcessor esh = new ExertProcessor(input);
 			Exertion result = null;
 			try {
 				result = esh.exert(transaction, null, entries);
 			} catch (Exception e) {
 				e.printStackTrace();
 				if (result != null)
 					((ServiceExertion) result).reportException(e);
 			}
 			return (T) result;
 		} catch (Exception e) {
 			throw new ExertionException(e);
 		}
 	}
 
 	public static OutEntry output(Object value) {
 		return new OutEntry(null, value, 0);
 	}
 
 	public static ReturnPath self() {
 		return new ReturnPath();
 	}
 
 	public static ReturnPath result(String path, String... paths) {
 		return new ReturnPath(path, paths);
 	}
 
 	public static ReturnPath result(String path, Direction direction,
 			String... paths) {
 		return new ReturnPath(path, direction, paths);
 	}
 
 	public static ReturnPath result(String path, Class type, String... paths) {
 		return new ReturnPath(path, Direction.OUT, type, paths);
 	}
 
 	public static OutEntry output(String path, Object value) {
 		return new OutEntry(path, value, 0);
 	}
 
 	public static OutEntry out(String path, Object value) {
 		return new OutEntry(path, value, 0);
 	}
 
 	public static OutEntry entry(String path, IndexedTriplet fidelity) {
 		return new OutEntry(path, fidelity);
 	}
 
 	public static OutEntry out(String path, IndexedTriplet fidelity) {
 		return new OutEntry(path, fidelity);
 	}
 
 	public static OutEndPoint output(Exertion outExertion, String outPath) {
 		return new OutEndPoint(outExertion, outPath);
 	}
 
 	public static OutEndPoint out(Exertion outExertion, String outPath) {
 		return new OutEndPoint(outExertion, outPath);
 	}
 
 	public static InEndPoint input(Exertion inExertion, String inPath) {
 		return new InEndPoint(inExertion, inPath);
 	}
 
 	public static InEndPoint in(Exertion inExertion, String inPath) {
 		return new InEndPoint(inExertion, inPath);
 	}
 
 	public static OutEntry output(String path, Object value, int index) {
 		return new OutEntry(path, value, index);
 	}
 
 	public static OutEntry out(String path, Object value, int index) {
 		return new OutEntry(path, value, index);
 	}
 
 	public static OutEntry output(String path, Object value, boolean flag) {
 		return new OutEntry(path, value, flag);
 	}
 
 	public static OutEntry out(String path, Object value, boolean flag) {
 		return new OutEntry(path, value, flag);
 	}
 
 	public static InEntry input(String path) {
 		return new InEntry(path, null, 0);
 	}
 
 	public static OutEntry out(String path) {
 		return new OutEntry(path, null, 0);
 	}
 
 	public static OutEntry output(String path) {
 		return new OutEntry(path, null, 0);
 	}
 
 	public static InEntry in(String path) {
 		return new InEntry(path, null, 0);
 	}
 
 	public static Entry at(String path, Object value) {
 		return new Entry(path, value, 0);
 	}
 
 	public static Entry at(String path, Object value, int index) {
 		return new Entry(path, value, index);
 	}
 
 	public static InEntry input(String path, Object value) {
 		return new InEntry(path, value, 0);
 	}
 
 	public static InEntry in(String path, Object value) {
 		return new InEntry(path, value, 0);
 	}
 
 	public static InEntry input(String path, Object value, int index) {
 		return new InEntry(path, value, index);
 	}
 
 	public static InEntry in(String path, Object value, int index) {
 		return new InEntry(path, value, index);
 	}
 
 	public static InEntry inout(String path) {
 		return new InEntry(path, null, 0);
 	}
 
 	public static InEntry inout(String path, Object value) {
 		return new InEntry(path, value, 0);
 	}
 
 	public static InoutEntry inout(String path, Object value, int index) {
 		return new InoutEntry(path, value, index);
 	}
 
 	private static String getUnknown() {
 		return "unknown" + count++;
 	}
 
 	public static class OutEntry<T> extends IndexedTriplet implements Parameter {
 		private static final long serialVersionUID = 1L;
 		public boolean flag;
 
 		OutEntry(String path, T value, boolean flag) {
 			T v = value;
 			if (v == null)
 				v = (T) Context.Value.NULL;
 
 			this._1 = path;
 			this._2 = v;
 			this.flag = flag;
 		}
 
 		OutEntry(String path, T value, int index) {
 			T v = value;
 			if (v == null)
 				v = (T) Context.Value.NULL;
 
 			this._1 = path;
 			this._2 = v;
 			this.index = index;
 		}
 
 		OutEntry(String path, Object fidelity) {
 			this._1 = path;
 			this._3 = fidelity;
 		}
 	}
 
 	public static class Range extends Tuple2<Integer, Integer> implements
 			Parameter {
 		private static final long serialVersionUID = 1L;
 		public Integer[] range;
 
 		public Range(Integer from, Integer to) {
 			this._1 = from;
 			this._2 = to;
 		}
 
 		public Range(Integer[] range) {
 			this.range = range;
 		}
 
 		public Integer[] range() {
 			return range;
 		}
 
 		public int from() {
 			return _1;
 		}
 
 		public int to() {
 			return _2;
 		}
 
 		public String toString() {
 			if (range != null)
 				return Arrays.toString(range);
 			else
 				return "[" + _1 + "-" + _2 + "]";
 		}
 	}
 
 	private static class Pipe {
 		String inPath;
 		String outPath;
 		Exertion in;
 		Exertion out;
 
 		Pipe(Exertion out, String outPath, Exertion in, String inPath) {
 			this.out = out;
 			this.outPath = outPath;
 			this.in = in;
 			this.inPath = inPath;
 		}
 
 		Pipe(OutEndPoint outEndPoint, InEndPoint inEndPoint) {
 			this.out = outEndPoint.out;
 			this.outPath = outEndPoint.outPath;
 			this.in = inEndPoint.in;
 			this.inPath = inEndPoint.inPath;
 		}
 	}
 
 	public static Pipe pipe(OutEndPoint outEndPoint, InEndPoint inEndPoint) {
 		return new Pipe(outEndPoint, inEndPoint);
 	}
 
 	public static <T> ControlContext strategy(T... entries) {
 		ControlContext cc = new ControlContext();
 		List<Signature> sl = new ArrayList<Signature>();
 		for (Object o : entries) {
 			if (o instanceof Access) {
 				cc.setAccessType((Access) o);
 			} else if (o instanceof Flow) {
 				cc.setFlowType((Flow) o);
 			} else if (o instanceof Monitor) {
 				cc.isMonitorable((Monitor) o);
 			} else if (o instanceof Provision) {
 				cc.isProvisionable((Provision) o);
 			} else if (o instanceof Wait) {
 				cc.isWait((Wait) o);
 			} else if (o instanceof Signature) {
 				sl.add((Signature) o);
 			}
 		}
 		cc.setSignatures(sl);
 		return cc;
 	}
 
 	public static URL dbURL() throws MalformedURLException {
 		return new URL(Sorcer.getDatabaseStorerUrl());
 	}
 
 	public static URL dsURL() throws MalformedURLException {
 		return new URL(Sorcer.getDataspaceStorerUrl());
 	}
 
 	public static URL dbURL(Object object) throws ExertionException,
 			SignatureException, ContextException {
 		return store(object);
 	}
 
 	public static SosURL sosURL(Object object) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.sosStore(object);
 	}
 
 	public static URL store(Object object) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.store(object);
 	}
 
 	public static Object retrieve(URL url) throws IOException {
 		return url.getContent();
 	}
 
 	public static URL update(Object object) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.update(object);
 	}
 
 	public static List<String> list(URL url) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.list(url);
 	}
 
 	public static List<String> list(Store store) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.list(store);
 	}
 
 	public static URL delete(Object object) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.delete(object);
 	}
 
 	public static int clear(Store type) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.clear(type);
 	}
 
 	public static int size(Store type) throws ExertionException,
 			SignatureException, ContextException {
 		return SdbUtil.size(type);
 	}
 
 	private static class InEndPoint {
 		String inPath;
 		Exertion in;
 
 		InEndPoint(Exertion in, String inPath) {
 			this.inPath = inPath;
 			this.in = in;
 		}
 	}
 
 	private static class OutEndPoint {
 		String outPath;
 		Exertion out;
 
 		OutEndPoint(Exertion out, String outPath) {
 			this.outPath = outPath;
 			this.out = out;
 		}
 	}
 
 	public static Object target(Object object) {
 		return new target(object);
 	}
 
 	public static class target extends Path {
 		private static final long serialVersionUID = 1L;
 		Object target;
 
 		target(Object target) {
 			this.target = target;
 		}
 
 		target(String path, Object target) {
 			this.target = target;
 			this._1 = path;
 		}
 
 		@Override
 		public String toString() {
 			return "target: " + target;
 		}
 	}
 
 	public static class result extends Path implements Parameter {
 		private static final long serialVersionUID = 1L;
 		Class returnType;
 
 		result(String path) {
 			this._1 = path;
 		}
 
 		result(String path, Class returnType) {
 			this._1 = path;
 			this.returnType = returnType;
 		}
 
 		@Override
 		public String toString() {
 			return "return path: " + _1;
 		}
 	}
 
 	public static ParameterTypes parameterTypes(Class... parameterTypes) {
 		return new ParameterTypes(parameterTypes);
 	}
 
 	public static class ParameterTypes extends Path {
 		private static final long serialVersionUID = 1L;
 		Class[] parameterTypes;
 
 		public ParameterTypes(Class... parameterTypes) {
 			this.parameterTypes = parameterTypes;
 		}
 
 		public ParameterTypes(String path, Class... parameterTypes) {
 			this.parameterTypes = parameterTypes;
 			this._1 = path;
 		}
 
 		@Override
 		public String toString() {
 			return "parameterTypes: " + Arrays.toString(parameterTypes);
 		}
 	}
 
 	public static Args parameterValues(Object... args) {
 		return new Args(args);
 	}
 
 	public static Args args(Object... args) {
 		return new Args(args);
 	}
 
 	public static class Args extends Path {
 		private static final long serialVersionUID = 1L;
 
 		Object[] args;
 
 		public Args(Object... args) {
 			this.args = args;
 		}
 
 		public Args(String path, Object... args) {
 			this.args = args;
 			this._1 = path;
 		}
 
 		@Override
 		public String toString() {
 			return "args: " + Arrays.toString(args);
 		}
 	}
 
 	public static class InoutEntry<T> extends IndexedTriplet implements
 			Parameter {
 		private static final long serialVersionUID = 1L;
 
 		InoutEntry(String path, T value, int index) {
 			T v = value;
 			if (v == null)
 				v = (T) Context.Value.NULL;
 
 			this._1 = path;
 			this._2 = v;
 			this.index = index;
 		}
 
 		InoutEntry(String path, Object fidelity) {
 			this._1 = path;
 			this._3 = fidelity;
 		}
 	}
 
 	public static class DataEntry<T2> extends Tuple2<String, T2> {
 		private static final long serialVersionUID = 1L;
 
 		DataEntry(String path, T2 value) {
 			T2 v = value;
 			if (v == null)
 				v = (T2) Context.Value.NULL;
 
 			this._1 = path;
 			this._2 = v;
 		}
 	}
 
 	public static class InEntry<T> extends IndexedTriplet implements Parameter {
 		private static final long serialVersionUID = 1L;
 
 		InEntry(String path, T value, int index) {
 			T v = value;
 			if (v == null)
 				v = (T) Context.Value.NULL;
 
 			this._1 = path;
 			this._2 = v;
 			this.index = index;
 		}
 
 		InEntry(String path, Object fidelity) {
 			this._1 = path;
 			this._3 = fidelity;
 		}
 	}
 
 	public static class Complement<T1, T2> extends Entry<T1, T2> implements
 			Parameter {
 		private static final long serialVersionUID = 1L;
 
 		Complement(T1 path, T2 value) {
 			this._1 = path;
 			this._2 = value;
 		}
 	}
 
 	public static List<Service> providers(Signature signature)
 			throws SignatureException {
 		ServiceTemplate st = new ServiceTemplate(null,
 				new Class[] { signature.getServiceType() }, null);
 		ServiceItem[] sis = ServiceAccessor.getServiceItems(st, null,
 				Sorcer.getLookupGroups());
 		if (sis == null)
 			throw new SignatureException("No available providers of type: "
 					+ signature.getServiceType().getName());
 		List<Service> servicers = new ArrayList<Service>(sis.length);
 		for (ServiceItem si : sis) {
 			servicers.add((Service) si.service);
 		}
 		return servicers;
 	}
 
 	public static List<Class<?>> interfaces(Object obj) {
 		if (obj == null)
 			return null;
 		return Arrays.asList(obj.getClass().getInterfaces());
 	}
 
 	public static Object provider(Signature signature)
 			throws SignatureException {
 		Object target = null;
 		Service provider = null;
 		Class<?> providerType = null;
 		if (signature instanceof NetSignature) {
 			providerType = ((NetSignature) signature).getServiceType();
 		} else if (signature instanceof ObjectSignature) {
 			providerType = ((ObjectSignature) signature).getProviderType();
 			target = ((ObjectSignature) signature).getTarget();
 		}
 		try {
 			if (signature instanceof NetSignature) {
 				provider = ((NetSignature) signature).getServicer();
 				if (provider == null) {
 					provider = Accessor.getServicer(signature);
 					((NetSignature) signature).setServicer(provider);
 				}
 			} else if (signature instanceof ObjectSignature) {
 				if (target != null) {
 					return target;
 				} else if (Provider.class.isAssignableFrom(providerType)) {
 					return providerType.newInstance();
 				} else {
 					return instance((ObjectSignature) signature);
 				}
 			} else if (signature instanceof EvaluationSignature) {
 				return ((EvaluationSignature) signature).getEvaluator();
 			}
 		} catch (Exception e) {
 			throw new SignatureException("No signature provider avaialable", e);
 		}
 		return provider;
 	}
 
 	/**
 	 * Returns an instance by constructor method initialization or by
 	 * instance/class method initialization.
 	 * 
 	 * @param signature
 	 * @return object created
 	 * @throws SignatureException
 	 */
 	public static Object instance(ObjectSignature signature)
 			throws SignatureException {
 		if (signature.getSelector() == null
 				|| signature.getSelector().equals("new"))
 			return signature.newInstance();
 		else
 			return signature.initInstance();
 	}
 
 	/**
 	 * Returns an instance by class method initialization with a service
 	 * dataContext.
 	 * 
 	 * @param signature
 	 * @return object created
 	 * @throws SignatureException
 	 */
 	public static Object instance(ObjectSignature signature, Context context)
 			throws SignatureException {
 		return signature.build(context);
 	}
 
 	public static String[] pathToArray(String arg) {
 		StringTokenizer token = new StringTokenizer(arg, SorcerConstants.CPS);
 		String[] array = new String[token.countTokens()];
 		int i = 0;
 		while (token.hasMoreTokens()) {
 			array[i] = token.nextToken();
 			i++;
 		}
 		return (array);
 	}
 
 	public static String toPath(String[] array) {
 		if (array.length > 0) {
 			StringBuilder sb = new StringBuilder(array[0]);
 			for (int i = 1; i < array.length; i++) {
 				sb.append(SorcerConstants.CPS).append(array[i]);
 			}
 			return sb.toString();
 		} else
 			return null;
 	}
 
 }
