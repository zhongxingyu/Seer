 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.command;
 
 /**
  * Copyright Universite Joseph Fourier (www.ujf-grenoble.fr)
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 //import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.ServiceProperty;
 
 import fr.imag.adele.apam.Apam;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Relation;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.Link;
 import fr.imag.adele.apam.apform.Apform2Apam;
 import fr.imag.adele.apam.apform.Apform2Apam.Request;
 import fr.imag.adele.apam.declarations.ResourceReference;
 import fr.imag.adele.apam.impl.ComponentImpl;
 import fr.imag.adele.apam.impl.CompositeImpl;
 import fr.imag.adele.apam.impl.RelationImpl;
 
 @Instantiate
 @org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "apam.universal.shell")
 @Provides(specifications = ApamCommand.class)
 public class ApamCommand {
 
 	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "apam")
 	String universalShell_groupID;
 
 	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "ApAM Commands")
 	String universalShell_groupName;
 
 	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
 	String[] universalShell_groupCommands = new String[] {
 		"spec#inspect an apam specification",
 		"implem#inspect an apam implementation",
 		"inst#inspect an apam instance",
 		"dump#display the full apam state model",
 		"compoType#inspect an apam composite type",
 		"compo#inspect an apam composite instance",
 		"app#inspect an apam application",
 		"displaywires#display all the relations of an instance",
 		"charge#creates and start a new instance of the target implementation",
 		"l#creates and start a new instance of the target implementation",
 		"pending#display all pending installations in apam platform",
 		"updatecomponent#updates target component (Warning: updates the whole Bundle)",
		"changeproperty#set properties of an instance" };
 
 	// Apam injected
 	@Requires
 	Apam apam;
 
 	/**
 	 * Updates the target component. Warning: updates the whole Bundle.
 	 */
 	public void updatecomponent(PrintWriter out, String... args) {
 		if (args.length <= 0) {
 			argumentMessageError(out,
 					"a component name should be specified, example : up acomponent");
 			return;
 		}
 		for (String arg : args) {
 			// TODO make update synchronized and show message if the update was
 			// performed or not
 			// TODO should return null if the update is not performed , or the
 			// object in case of success
 			CST.apamResolver.updateComponent(arg);
 		}
 
 	}
 
 	/**
 	 * Change properties of an instance take as arguments : an instance name an
 	 * URL file.properties or a list of value=property, ex :
 	 * "lang=french vendor=adele" "
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
	public void changeproperty(PrintWriter out, String... args) {
 		if (args.length <= 1) {
 			argumentMessageError(
 					out,
 					"the command should be followed by an instance name and the properties or an url to file.properties, \n "
 							+ " example : \n "
 							+ "   setproperty aninstance vendor=adele version=1.0 \n"
 							+ "   setproperty aninstance file://path/to/file.properties");
 			return;
 		}
 
 		URL url;
 		String instanceName = args[0];
 		String urlArg = args[1];
 		String[] propsArgs = Arrays.copyOfRange(args, 1, args.length);
 
 		Properties properties = new Properties();
 
 		try {
 			url = new URL(args[1]);
 			properties.load(url.openStream());
 		} catch (MalformedURLException e) {
 			for (String arg : propsArgs) {
 				String[] property = arg.split("=");
 				properties.put(property[0], property[1]);
 			}
 
 		} catch (IOException e) {
 			out.println("Error while reading the file : " + urlArg);
 			e.printStackTrace();
 		}
 		if (properties.isEmpty())
 			return;
 
 		Instance inst = CST.componentBroker.getInst(instanceName);
 
 		if (inst != null) {
 			// TODO verify values of properties with some types (string, int,
 			// double, string[])
 			// TODO for now only string are used
 
 			inst.setAllProperties((Map) properties);
 		} else {
 			out.println("The instance " + instanceName + " not exist !");
 		}
 
 	}
 
 	/**
 	 * inspect a specification. arguments : - the specification name
 	 */
 	public void spec(PrintWriter out, String... args) {
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: spec [specification name]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Set<Specification> specifications = CST.componentBroker.getSpecs();
 			for (Specification specification : specifications)
 				out.println("spec " + specification.getName());
 			return;
 		}
 
 		String specificationName = args[0];
 		Specification specification = CST.componentBroker
 				.getSpec(specificationName);
 		if (specification == null) {
 			out.println("No such specification : " + specificationName);
 			return;
 		}
 		printSpecification(out, "", specification);
 	}
 
 	/**
 	 * inspect an implementation arguments: - the implementation name
 	 */
 	public void implem(PrintWriter out, String... args) {
 
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: implem [implementation name]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Set<Implementation> implementations = CST.componentBroker.getImpls();
 			for (Implementation implementation : implementations) {
 				out.println("implem " + implementation.getName());
 			}
 			return;
 		}
 
 		String implementationName = args[0];
 		Implementation implementation = CST.componentBroker
 				.getImpl(implementationName);
 		if (implementation == null) {
 			out.println("No such implementation : " + implementationName);
 			return;
 		}
 		printImplementation(out, "", implementation);
 	}
 
 	/**
 	 * inspect an instance. arguments : - the instance name
 	 */
 
 	public void inst(PrintWriter out, String[] args) {
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: inst [component name] [composite]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Set<Instance> instances = CST.componentBroker.getInsts();
 			for (Instance instance : instances) {
 				out.println("inst " + instance.getName());
 			}
 			return;
 		}
 
 		String instanceName = args[0];
 		Instance instance = CST.componentBroker.getInst(instanceName);
 		if (instance == null) {
 			out.println("No such instance : " + instanceName);
 			return;
 		}
 
 		printInstance(out, "", instance);
 	}
 
 	private CommandInvocationType checkCommandInvocationType(PrintWriter out,
 			int minimum, String... args) {
 
 		int length = args == null ? 0 : args.length;
 
 		if (length < minimum) {
 			return CommandInvocationType.INVALID;
 		}
 
 		if (length == 1 && args[0].equals("--help"))
 			return CommandInvocationType.HELP;
 
 		if (length == 0){
 			return CommandInvocationType.DEFAULT;
 		}		
 
 		return CommandInvocationType.OTHER;
 	}
 
 	enum CommandInvocationType {
 		INVALID, DEFAULT, HELP,OTHER
 	}
 
 	public void l(PrintWriter out, String... args) {
 		load(out, args);
 	}
 	/**
 	 * Start a new instance of the target implementation in a composite
 	 * arguments : - an implementation name - (optional) a composite name
 	 */
 	public void load(PrintWriter out, String... args) {
 
 		String componentName = null;
 		String compositeName = null;
 		Composite composite = null;
 
 		if (args.length == 0) {
 			argumentMessageError(out, "charge <component-name> [composite]\n");
 			return;
 		}
 
 		if (args.length >= 1) {
 			componentName = args[0];
 		}
 
 		if (args.length > 1) {
 			compositeName = args[1];
 			composite = checkComposite(out, compositeName, componentName);
 		} else {
 			composite = CompositeImpl.getRootAllComposites();
 		}
 
 		if (composite != null) {
 			Thread t = new Thread(new AsyncFind(out, composite, componentName,
 					true, args));
 			t.start();
 		}
 	}
 
 	/**
 	 * Display information about the target application"
 	 */
 	public void app(PrintWriter out, String... args) {
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: app [application name]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Collection<Composite> apps = apam.getComposites();
 			for (Composite app : apps) {
 				out.println("app " + app.getName());
 			}		
 			return;
 		}
 
 		String appName = args[0];
 		Composite app = apam.getComposite(appName);
 		if (app == null) {
 			out.println("No such root composite : " + appName);
 			return;
 		}
 		out.println("Application " + app);
 		for (Composite compo : app.getSons()) {
 			out.println("Son Composites : " + compo.getName());
 		}
 		for (Composite compo : app.getDepend()) {
 			out.println("Depends on composites : " + compo.getName());
 		}
 	}
 
 	/**
 	 * Display the full Apam state model"
 	 */
 	public void dump(PrintWriter out, String... args) {
 		dumpApam(out);
 	}
 
 	/** Display the pending platform installations */
 	public void pending(PrintWriter out, String... args) {
 		out.println("APAM platform pernding requests : ");
 		for (Request pendingRequest : Apform2Apam.getPending()) {
 			out.println("\t" + pendingRequest.getDescription()
 					+ " is waiting for component "
 					+ pendingRequest.getRequiredComponent());
 			List<StackTraceElement> stack = pendingRequest.getStack();
 
 			if (stack == null)
 				continue;
 
 			for (StackTraceElement frame : stack) {
 				out.println("\t\t " + frame);
 			}
 		}
 
 	}
 
 	/** Display an Apam composite type */
 	public void compoType(PrintWriter out, String... args) {
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: compoType [composite name]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Collection<CompositeType> compositeTypes = apam.getCompositeTypes();
 			for (CompositeType compositeType : compositeTypes) {
 				out.println("    " + compositeType);
 			}
 			return;
 		}
 
 		String compoTypeName = args[0];
 		CompositeType compo = apam.getCompositeType(compoTypeName);
 		if (compoTypeName == null) {
 			out.println("No such compositeType : " + compoTypeName);
 			return;
 		}
 		printCompositeType(out, compo, "");
 		out.println("");
 	}
 
 	/** Display an Apam composites */
 	public void compo(PrintWriter out, String... args) {
 
 		CommandInvocationType typeCall = checkCommandInvocationType(out, 0,
 				args);
 
 		if (typeCall == CommandInvocationType.INVALID) {
 			argumentMessageError(out,
 					"invalid number of arguments, type --help to obtain more information about the syntax");
 			return;
 		} else if (typeCall == CommandInvocationType.HELP) {
 			out.println("syntax: compo [composite name]");
 			return;
 		} else if (typeCall == CommandInvocationType.DEFAULT) {
 			Collection<Composite> comps = apam.getRootComposites();
 			for (Composite compo : comps) {
 				out.println("    " + compo);
 			}
 			return;
 		}
 
 		String compoName = args[0];
 		Composite compo = compoName.equals("root")? CompositeImpl.getRootAllComposites(): apam.getComposite(compoName);
 		if (compo == null) {
 			out.println("No such composite : " + compoName);
 			return;
 		}
 		printComposite(out, compo, "");
 		out.println("");
 	}
 
 	private void argumentMessageError(PrintWriter out, String message) {
 		out.println("Error : Invalid number of arguments \n ---> " + message);
 	}
 
 	private Composite checkComposite(PrintWriter out, String compositeTarget,
 			String componentName) {
 		if (compositeTarget == null) {// if the composite target is null, use
 			// the root composite
 			compositeTarget = "root";
 		}
 
 		Composite target = apam.getComposite(compositeTarget);
 		if (target == null) {
 			out.println("Invalid composite target name : " + compositeTarget);
 			return null;
 		}
 		return target;
 	}
 
 	private void printCompositeType(PrintWriter out, CompositeType compo,
 			String indent) {
 		out.println(indent + "Composite Type " + compo.getName()
 				+ ". Main implementation : " + compo.getMainImpl()
 				+ ". Models : " + compo.getModels());
 
 		indent += "   ";
 		out.print(indent + "Provides resources : ");
 		for (ResourceReference ref : compo.getCompoDeclaration()
 				.getProvidedResources()) {
 			out.print(ref + " ");
 		}
 		out.println("");
 
 		out.print(indent + "Embedded in composite types : ");
 		for (CompositeType comType : compo.getInvEmbedded()) {
 			out.print(comType.getName() + " ");
 		}
 		out.println("");
 
 		out.print(indent + "Contains composite types : ");
 		for (CompositeType comType : compo.getEmbedded()) {
 			out.print(comType.getName() + " ");
 		}
 		out.println("");
 		out.print(indent + "Imports composite types : ");
 		for (CompositeType comDep : compo.getImport()) {
 			out.print(comDep.getName() + " ");
 		}
 		out.println("");
 
 		//		out.print(indent + "Uses composite types : ");
 		//		for (Implementation comDep : compo.getUses()) {
 		//			out.print(comDep.getName() + " ");
 		//		}
 		//		out.println("");
 
 		out.print(indent + "Contains Implementations: ");
 		for (Implementation impl : compo.getImpls()) {
 			out.print(impl + " ");
 		}
 		out.println("");
 
 		out.print(indent + "Composite Instances : ");
 		for (Instance inst : compo.getInsts()) {
 			out.print(inst + " ");
 		}
 		out.println("");
 
 		out.println(compo.getApformImpl().getDeclaration()
 				.printDeclaration(indent));
 
 		for (Instance compInst : compo.getInsts()) {
 			printComposite(out, (Composite) compInst, indent + "   ");
 		}
 
 		for (CompositeType comType : compo.getEmbedded()) {
 			out.println("\n");
 			printCompositeType(out, comType, indent);
 		}
 	}
 
 	private void printComposite(PrintWriter out, Composite compo, String indent) {
 		out.println(indent + "Composite " + compo.getName()
 				+ " Composite Type : " + compo.getCompType().getName()
 				+ " Father : " + compo.getFather());
 		out.println(indent + "   In application : " + compo.getAppliComposite());
 		out.print(indent + "   Son composites : ");
 		for (Composite comDep : compo.getSons()) {
 			out.print(comDep.getName() + " ");
 		}
 		out.println("");
 
 		out.print(indent + "   Depends on composites : ");
 		for (Composite comDep : compo.getDepend()) {
 			out.print(comDep.getName() + " ");
 		}
 		out.println("");
 
 		if (!compo.getContainInsts().isEmpty()) {
 			out.print(indent + "   Contains instances : ");
 			for (Instance inst : compo.getContainInsts()) {
 				out.print(inst + " ");
 			}
 			out.println("");
 		}
 
 		indent += "  ";
 		out.println(indent + " State " + compo);
 		dumpState(out, compo.getMainInst(), indent, " ");
 		// out.println("");
 
 		out.println(compo.getApformInst().getDeclaration()
 				.printDeclaration(indent));
 
 		if (!compo.getSons().isEmpty()) {
 			out.println("\n");
 			for (Composite comp : compo.getSons()) {
 				printComposite(out, comp, indent + "   ");
 			}
 		}
 	}
 
 	/**
 	 * Display all the relations of an instance
 	 */
 
 	public void displaywires(PrintWriter out, String... args) {
 		String instName = args[0];
 		Instance inst = CST.componentBroker.getInst(instName);
 		if (inst != null) {
 			dumpState(out, inst, "  ", null);
 		}
 	}
 
 	private void printComponent(PrintWriter out, String indent, Component elem) {
 
 		out.println(indent + "----- [ " + elem.getKind() + " " + elem.getName() + " ] -----");
 
 		out.println(indent + "Relation declarations :");
 		for (Relation relation : elem.getRelations()) {
 			out.println(indent + "       " + relation.getName() + ": " + relation);
 		}
 		//Links
 		String w;
 		out.println(indent + "Links towards:");
 		for (Link wire : elem.getRawLinks()) {
 			if (wire.isInjected()) {
 				w = wire.isWire() ? "IW-" : "IL-";
 			} else w="";
 			out.println(indent + "       " + w + wire.getName() + ": " + wire.getDestination());
 		}
 
 		//Reverse Links
 		out.println(indent + "Used by:");
 		for (Link wire : elem.getInvLinks()) {
 			if (wire.isInjected()) {
 				w = wire.isWire() ? "IW-" : "IL-";
 			} else w="";
 			out.println(indent + "       (" + w + wire.getName() + ") " + wire.getSource());
 		}
 
 		//Properties
 		printProperties(out, indent, (ComponentImpl) elem);
 
 		out.println();
 	}
 
 	/**
 	 * Prints the specification.
 	 * 
 	 * @param indent
 	 *            the indent
 	 * @param specification
 	 *            the specification
 	 */
 	private void printSpecification(PrintWriter out, String indent,
 			Specification specification) {
 		//		out.println(indent + "----- [ ASMSpec : " + specification.getName()
 		//				+ " ] -----");
 
 		printComponent(out, indent, specification);
 
 		indent += "   ";
 		out.println(indent + "Interfaces:");
 		for (ResourceReference res : specification.getDeclaration()
 				.getProvidedResources()) {
 			out.println(indent + "      " + res);
 		}
 
 		out.println(specification.getDeclaration().getDependencies());
 
 		//		out.println(indent + "Effective Required specs:");
 		//		for (Specification spec : specification.getRequires()) {
 		//			out.println(indent + "      " + spec);
 		//		}
 		//
 		//		out.println(indent + "Required by:");
 		//
 		//		for (Specification spec : specification.getInvRequires()) {
 		//			out.println(indent + "      " + spec);
 		//		}
 
 		out.println(indent + "Implementations:");
 		for (Implementation impl : specification.getImpls()) {
 			out.println(indent + "      " + impl);
 		}
 		//		printProperties(out, indent, (ComponentImpl) specification);
 		out.println(specification.getApformSpec().getDeclaration()
 				.printDeclaration(indent));
 
 	}
 
 	/**
 	 * Prints the instance.
 	 * 
 	 * @param out
 	 *            the printWriter
 	 * 
 	 * @param indent
 	 *            the indent
 	 * @param instance
 	 *            the instance
 	 */
 	private void printInstance(PrintWriter out, String indent, Instance instance) {
 		//		
 		//		if (instance == null)
 		//			return;		
 		printComponent(out, indent, instance);
 
 		indent += "   ";
 
 		//		out.println(indent + "links:");
 		//		for (Link wire : instance.getLinks()) {
 		//			out.println(indent + "   " + wire.getName() + ": "
 		//					+ wire.getDestination());
 		//		}
 		//
 		//		out.println(indent + "Called by:");
 		//		for (Link wire : instance.getInvLinks())
 		//			out.println(indent + "   (" + wire.getName() + ") "
 		//					+ wire.getSource());
 
 		if (instance.getImpl() == null) {
 			out.println(indent + "warning :  no factory for this instance");
 		} else {
 			out.println(indent + "specification  : " + instance.getSpec());
 			out.println(indent + "implementation : " + instance.getImpl());
 			out.println(indent + "in composite   : " + instance.getComposite());
 			out.println(indent + "in application : " + instance.getAppliComposite());
 			//			printProperties(out, indent, (ComponentImpl) instance);
 		}
 
 
 		out.println(instance.getApformInst().getDeclaration().printDeclaration(indent));
 	}
 
 
 	/**
 	 * Prints the implementation.
 	 * 
 	 * @param out
 	 *            the printWriter
 	 * 
 	 * @param indent
 	 *            the indent
 	 * @param impl
 	 *            the impl
 	 */
 	private void printImplementation(PrintWriter out, String indent,
 			Implementation impl) {
 
 		printComponent(out, indent, impl);
 
 		indent += "  ";
 		out.println(indent + "specification : " + impl.getSpec());
 
 		out.println(indent + "In composite types:");
 		for (CompositeType compo : impl.getInCompositeType()) {
 			out.println(indent + "      " + compo.getName());
 		}
 
 		//		out.println(indent + "Uses:");
 		//		for (Implementation implem : impl.getUses()) {
 		//			out.println(indent + "      " + implem);
 		//		}
 		//
 		//		out.println(indent + "Used by:");
 		//		for (Implementation implem : impl.getInvUses()) {
 		//			out.println(indent + "      " + implem);
 		//		}
 
 		out.println(indent + "Instances:");
 		for (Instance inst : impl.getInsts()) {
 			out.println(indent + "      " + inst);
 		}
 
 		//		printProperties(out, indent, (ComponentImpl) impl);
 
 		out.println(impl.getApformImpl().getDeclaration()
 				.printDeclaration(indent));
 	}
 
 	/**
 	 * Prints the properties.
 	 * 
 	 * @param out
 	 *            the printWriter
 	 * 
 	 * @param indent
 	 *            the indent
 	 * @param properties
 	 *            the properties
 	 */
 	private void printProperties(PrintWriter out, String indent,
 			ComponentImpl comp) {
 		Map<String, Object> properties = comp.getAllProperties();
 		out.println(indent + "Properties : ");
 		for (String key : properties.keySet()) {
 			out.print(indent + "   " + key + " = " + comp.getProperty(key));
 			Object value = properties.get(key);
 			if ((value instanceof String)
 					&& (((String) value).charAt(0) == '$'
 					|| ((String) value).charAt(0) == '@' || ((String) value)
 					.charAt(0) == '\\')) {
 				out.print(" (" + value + ")");
 			}
 			out.println();
 		}
 	}
 
 	private void dumpCompoType(PrintWriter out, String name) {
 		CompositeType compType = apam.getCompositeType(name);
 		if (compType == null) {
 			out.println("No such application :" + name);
 			return;
 		}
 		printCompositeType(out, compType, "");
 	}
 
 	private void dumpApam(PrintWriter out) {
 		for (CompositeType compo : apam.getRootCompositeTypes()) {
 			dumpCompoType(out, compo.getName());
 			out.println("\n");
 		}
 	}
 
 	private void dumpState(PrintWriter out, Instance inst, String indent, String dep) {
 		if (inst == null)
 			return;
 		Set<Component> insts = new HashSet<Component>();
 		insts.add(inst);
 		out.println(indent + dep + ": " + inst + " " + inst.getImpl() + " " + inst.getSpec());
 		indent = indent + "  ";
 		for (Link wire : inst.getRawLinks()) {
 			out.println(indent + wire.getName() + ": "
 					+ wire.getDestination() + " " ) ;
 //					+ ((Instance)wire.getDestination()).getImpl() + " "
 //					+ ((Instance)wire.getDestination()).getSpec());
 			dumpState0(out, (wire.getDestination()), indent, wire.getName(),
 					insts);
 		}
 	}
 
 	private void dumpState0(PrintWriter out, Component inst, String indent, String dep, Set<Component> insts) {
 		if (insts.contains(inst)) {
 			out.println(indent + "*" + dep + ": " + inst.getName());
 			return;
 		}
 		insts.add(inst);
 		indent = indent + "  ";
 		for (Link wire : inst.getRawLinks()) {
 			out.println(indent + wire.getName() + ": "
 					+ wire.getDestination() + " " ) ;
 //					+ ((Instance)wire.getDestination()).getImpl() + " "
 //					+ ((Instance)wire.getDestination()).getSpec());
 			dumpState0(out, ((Instance)wire.getDestination()), indent, wire.getName(),
 					insts);
 		}
 	}
 
 }
