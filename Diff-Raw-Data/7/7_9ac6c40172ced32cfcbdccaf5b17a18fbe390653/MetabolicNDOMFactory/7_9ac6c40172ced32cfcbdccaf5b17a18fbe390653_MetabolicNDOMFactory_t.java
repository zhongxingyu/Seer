 package uk.ac.ed.inf.Metabolic.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.pathwayeditor.businessobjectsAPI.ILink;
 import org.pathwayeditor.businessobjectsAPI.IMapObject;
 import org.pathwayeditor.businessobjectsAPI.IRootMapObject;
 import org.pathwayeditor.businessobjectsAPI.IShape;
 import org.pathwayeditor.businessobjectsAPI.Location;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition;
 import org.pathwayeditor.contextadapter.toolkit.ndom.GeometryUtils;
 import org.pathwayeditor.contextadapter.toolkit.ndom.ModelObject;
 import org.pathwayeditor.contextadapter.toolkit.ndom.NdomException;
 
 import uk.ac.ed.inf.Metabolic.ndomAPI.ERelType;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IReaction;
 
 public class MetabolicNDOMFactory extends NDOMFactory {
 
 	private Map<IShape, MetabolicMolecule> shape2Molecule = new HashMap<IShape, MetabolicMolecule>();
 	Map<MetabolicReaction, IShape> reaction2Shape = new HashMap<MetabolicReaction, IShape>();
 	LinkedList<Map<String, MetabolicCompound>> name2Compound = new LinkedList<Map<String, MetabolicCompound>>();
 	List<ILink> prodLinks;
 
 	public MetabolicNDOMFactory(IRootMapObject rmo) {
 		super(rmo);
 	}
 
 	public MetabolicNDOMFactory() {
 		super();
 	}
 
 	@Override
 	protected void semanticValidation() {
 		checkOrphanCompounds();
 		checkOrphanReactions();
 	}
 
 	void checkOrphanReactions() {
 		for (IReaction r : ndom.getReactionList()) {
 			if ((r.getSubstrateList().size() == 0)) {
 
 				IValidationRuleDefinition rd = getReportBuilder()
 						.getRuleStore().getRuleById(
 								MetabolicRuleLoader.ORPHAN_PROCESS_ERROR_ID);
 				IShape el = reaction2Shape.get(r);
 				getReportBuilder().setRuleFailed(el, rd, "Reaction has no substrates");
 			}
 			if ((r.getProductList().size() == 0)) {
 
 				IValidationRuleDefinition rd = getReportBuilder()
 						.getRuleStore().getRuleById(
 								MetabolicRuleLoader.ORPHAN_PROCESS_ERROR_ID);
 				IShape el = reaction2Shape.get(r);
 				getReportBuilder().setRuleFailed(el, rd, "Reaction has no products");
 			}
 		}
 	}
 
 	void checkOrphanCompounds() {
//		ModelProcessor proc=new ModelProcessor(ndom);
 		for(Entry<IShape,MetabolicMolecule> e:shape2Molecule.entrySet()){
 			IShape el=e.getKey();
 			MetabolicMolecule c=e.getValue();
 			if((c.getInhibitoryRelationList().size()+c.getActivatoryRelationList().size()+c.getCatalyticRelationList().size()+c.getSinkList().size()+c.getSourceList().size())==0){
 				IValidationRuleDefinition rd = getReportBuilder()
 						.getRuleStore().getRuleById(
 								MetabolicRuleLoader.ORPHAN_COMPOUND_ERROR_ID);
 				getReportBuilder().setRuleFailed(el, rd, "Reaction has no products");
 				
 			}
 		}
 
 	}
 
 	protected void rmo() {
 		name2Compound.addFirst(new HashMap<String, MetabolicCompound>());
 		super.rmo();
 		name2Compound.removeFirst();
 	}
 
 	void processProdLinks(MetabolicReaction r) {
 		if (r.isReversible()) {
 			IShape s = reaction2Shape.get(r);
 			// Set<ILink> substr = new HashSet<ILink>();
 			// Set<ILink> prod = new HashSet<ILink>();
 			Location srcLoc = null;
 			// double srcAngle;
 			for (ILink l : prodLinks) {
 				if (srcLoc == null) {
 					// first link
 					srcLoc = GeometryUtils.getSrcLocation(l, s);
 					substrate(l, r);
 				} else {
 					// all consequitive links
 					Location newLoc = GeometryUtils.getSrcLocation(l, s);
 					if (GeometryUtils.getAngle(srcLoc, newLoc) > 0) {
 						substrate(l, r);
 					} else {
 						productsIrr(l, r);
 					}
 				}
 			}
 		}
 		prodLinks = null;
 	}
 
 	@Override
 	protected void products(ILink el, MetabolicReaction r) {
 		if (r.isReversible()) {
 			prodLinks.add(el);
 		} else {
 			productsIrr(el, r);
 		}
 	}
 
 	private void productsIrr(ILink el, MetabolicReaction r) {
 		// TODO separate substrates from products
 		MetabolicRelation rel = production(el);
 		r.addProduct(rel);
 		IShape targ = el.getTarget();
 		MetabolicMolecule mol = shape2Molecule.get(targ);
 		if (mol == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Molecule in Production relation is not registered in the model");
 			// error("Molecule in Production relation is not registered in the
 			// model");
 		}
 		try {
 			mol.addSource(rel);
 			String vn = rel.getVarName();
 			String id = mol.getId();
 			updateKL(r, vn, id);
 
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 	}
 
 	void updateKL(MetabolicReaction r, String vn, String id) {
 		String kineticLaw = r.getKineticLaw();
 		if (kineticLaw != null) {
 			kineticLaw = kineticLaw.replaceAll(vn, id);
 			r.setKineticLaw(kineticLaw);
 		}
 	}
 
 	@Override
 	protected void substrate(ILink el, MetabolicReaction r) {
 		IShape targ =null;
 		MetabolicRelation rel = null;
 		if (r.isReversible()){
 				if( "Consume".equals(el.getObjectType().getTypeName())) {
 			// error("Consumption link to reversible reaction");
 			IValidationRuleDefinition rd = getReportBuilder()
 					.getRuleStore()
 					.getRuleById(
 							MetabolicRuleLoader.CONSUMPTION_TO_REVERSIBLE_ERROR_ID);
 			getReportBuilder().setRuleFailed(el, rd,
 					"Consumption link to reversible reaction");
 			return;
 			}else{
 				 rel = consumptionRev(el);
 				 targ = el.getTarget();
 			}
 		}else{
 		 rel = consumption(el);
 		 targ = el.getSource();
 		}
 		 r.addSubstrate(rel);
 		MetabolicMolecule mol = shape2Molecule.get(targ);
 		if (mol == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Molecule in Consumption relation is not registered in the model");
 			// error("Molecule in Consumption relation is not registered in the
 			// model");
 		}
 		try {
 			mol.addSink(rel);
 			String vn = rel.getVarName();
 			String id = mol.getId();
 			updateKL(r, vn, id);
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 	}
 
 	private MetabolicRelation consumptionRev(ILink el) {
 		MetabolicRelation rel = relation(el, ERelType.Consumption);
 		rel.setRole(el.getSrcPort().getPropertyByName("ROLE").getValue());
 //		rel.setStoichiometry(getInt(el.getTargetPort().getPropertyByName(
 //				"STOICH").getValue(), "Wrong stoichiometry\t"));
 		setStoichiometry(el, el.getSrcPort(),rel);
 		return rel;
 	}
 
 	@Override
 	protected void activate(ILink el, MetabolicReaction r) {
 		MetabolicRelation rel = activation(el);
 		if (r == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Reaction for Activation relation is not registered in the model");
 			// error("Reaction for Activation relation is not registered in the
 			// model");
 		}
 		r.addActivator(rel);
 		IShape src = el.getSource();
 		MetabolicMolecule mol = shape2Molecule.get(src);
 		if (mol == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Molecule for Activation relation is not registered in the model");
 		}
 		try {
 			mol.addActivatoryRelation(rel);
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void inhibit(ILink el, MetabolicReaction r) {
 		MetabolicRelation rel = inhibition(el);
 		if (r == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Reaction for Inhibiton relation is not registered in the model");
 		}
 		r.addInhibitor(rel);
 		IShape src = el.getSource();
 		MetabolicMolecule mol = shape2Molecule.get(src);
 		if (mol == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Molecule for Inhibiton relation is not registered in the model");
 		}
 		try {
 			mol.addInhibitoryRelation(rel);
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void catalysis(ILink el, MetabolicReaction r) {
 		MetabolicRelation rel = catalysis(el);
 		r.addCatalyst(rel);
 		IShape src = el.getSource();
 		MetabolicMolecule mol = shape2Molecule.get(src);
 		if (mol == null) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.NOT_REGISTERED_ERROR_ID);
 			getReportBuilder()
 					.setRuleFailed(el, rd,
 							"Molecule for Catalysis relation is not registered in the model");
 		}
 		try {
 			mol.addCatalyticRelation(rel);
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void macromolecule(MetabolicCompartment comaprtment,
 			IMapObject mapObject) {
 		MetabolicMacromolecule m = macromolecule(mapObject);
 		shape2Molecule.put((IShape) mapObject, m);
 		comaprtment.addMacromolecule(m);
 	}
 
 	@Override
 	protected void macromolecule(MetabolicMacromolecule parent,
 			IMapObject mapObject) {
 		MetabolicMacromolecule m = macromolecule(mapObject);
 		shape2Molecule.put((IShape) mapObject, m);
 		parent.addSubunit(m);
 	}
 
 	@Override
 	protected void compound(MetabolicMacromolecule m, IMapObject mapObject) {
 		MetabolicCompound comp = compound(mapObject);
 		m.addCompound(comp);
 		shape2Molecule.put((IShape) mapObject, comp);
 
 	}
 
 	@Override
 	protected void compound(MetabolicCompartment compartment,
 			IMapObject mapObject) {
 		MetabolicCompound comp = compound(mapObject);
 		if (name2Compound.getFirst().containsKey(comp.getName())) {
 			MetabolicCompound comp1 = name2Compound.getFirst().get(
 					comp.getName());
 			compareCompounds(comp1, comp, compartment, mapObject);
 			comp = comp1;
 		} else {
 			name2Compound.getFirst().put(comp.getName(), comp);
 			compartment.addCompound(comp);
 		}
 		comp.setCloneNumber(comp.getCloneNumber() + 1);
 		shape2Molecule.put((IShape) mapObject, comp);
 	}
 
 	/**
 	 * Check external references for two compound definitions. Validate that two
 	 * compounds, having the same name have the same set of references to
 	 * external databases.
 	 * 
 	 * @param comp1
 	 *            reference compound
 	 * @param comp
 	 *            compound under creation
 	 */
 	private void compareCompounds(MetabolicCompound comp1,
 			MetabolicCompound comp, ModelObject parent, IMapObject mapObject) {
 		String name = comp1.getASCIIName();
 		// TODO replace with rules
 		if (!comp1.getDescription().equals(comp.getDescription())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of Descriptions: '"
 							+ comp1.getDescription() + "' and '"
 							+ comp.getDescription() + "'");
 		}
 		if (!comp1.getDetailedDescription().equals(
 				comp.getDetailedDescription())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name
 							+ "has two sets of DetailedDescription: '"
 							+ comp1.getDetailedDescription() + "' and '"
 							+ comp.getDetailedDescription() + "'");
 		}
 		if (!comp1.getCID().equals(comp.getCID())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of CID: '"
 							+ comp1.getCID() + "' and '" + comp.getCID() + "'");
 		}
 		if (!comp1.getChEBIId().equals(comp.getChEBIId())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of ChEBIId: '"
 							+ comp1.getChEBIId() + "' and '"
 							+ comp.getChEBIId() + "'");
 		}
 		if (!comp1.getInChI().equals(comp.getInChI())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of InChI: '"
 							+ comp1.getInChI() + "' and '" + comp.getInChI()
 							+ "'");
 		}
 		if (comp1.getIC() != (comp.getIC())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of getIC: '"
 							+ comp1.getIC() + "' and '" + comp.getIC() + "'");
 		}
 		if (!comp1.getPubChemId().equals(comp.getPubChemId())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of PubChemId: '"
 							+ comp1.getPubChemId() + "' and '"
 							+ comp.getPubChemId() + "'");
 		}
 		if (!comp1.getSmiles().equals(comp.getSmiles())) {
 			IValidationRuleDefinition rd = getReportBuilder().getRuleStore()
 					.getRuleById(MetabolicRuleLoader.COMP_DEF_ERROR_ID);
 			getReportBuilder().setRuleFailed(
 					mapObject,
 					rd,
 					"Compound " + name + "has two sets of Smiles: '"
 							+ comp1.getSmiles() + "' and '" + comp.getSmiles()
 							+ "'");
 		}
 	}
 
 	@Override
 	protected void compartment(MetabolicCompartment parent, IMapObject mapObject) {
 		MetabolicCompartment compartment = compartment(mapObject);
 		try {
 			name2Compound.addFirst(new HashMap<String, MetabolicCompound>());
 			parent.addChildCompartment(compartment);
 			List<IMapObject> ch = mapObject.getChildren();
 			for (IMapObject el : ch) {
 				String ot = el.getObjectType().getTypeName();
 				if ("Compartment".equals(ot)) {
 					compartment(compartment, el);
 				} else if ("Process".equals(ot)) {
 					process(compartment, el);
 				} else if ("Compound".equals(ot)) {
 					compound(compartment, el);
 				} else if ("Macromolecule".equals(ot)) {
 					macromolecule(compartment, el);
 				}
 			}
 			name2Compound.removeFirst();
 		} catch (NdomException e) {
 			report(e);
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	protected void connectivity() {
 		for (MetabolicReaction r : reaction2Shape.keySet()) {
 			IShape s = reaction2Shape.get(r);
 			prodLinks = new ArrayList<ILink>();
 			Set<ILink> lset = s.getSourceLinks();
 			if (lset.size() == 0) {
 			}
 			for (ILink el : lset) {
 				String ot = el.getObjectType().getTypeName();
 				if ("Produce".equals(ot)) {
 					products(el, r);
 				}
 			}
 			lset = s.getTargetLinks();
 			int nsubstr = 0;
 			for (ILink el : lset) {
 				String ot = el.getObjectType().getTypeName();
 				if ("Consume".equals(ot)) {
 					substrate(el, r);
 					nsubstr++;
 				} else if ("Activation".equals(ot)) {
 					activate(el, r);
 				} else if ("Catalysis".equals(ot)) {
 					catalysis(el, r);
 				} else if ("Inhibition".equals(ot)) {
 					inhibit(el, r);
 				}
 			}
 			if (nsubstr == 0 && !r.isReversible()) {
 				// TODO replace with rules
 				IValidationRuleDefinition rd = getReportBuilder()
 						.getRuleStore().getRuleById(
 								MetabolicRuleLoader.RE_DEF_ERROR_ID);
 				getReportBuilder().setRuleFailed(
 						s,
 						rd,
 						"Reaction is not reversible and do not have substrates \t"
 								+ s.getId());
 			}
 			processProdLinks(r);
 		}
 	}
 
 	@Override
 	protected void process(ModelObject parent, IMapObject mapObject) {
 		MetabolicReaction re = process(mapObject);
 		// checkParameters(re);
 		ndom.addReaction(re);
 		reaction2Shape.put(re, (IShape) mapObject);
 	}
 
 	/**
 	 * @deprecated replaced by
 	 *             {@link NDOMFactory#setReParam(IMapObject, MetabolicReaction)}
 	 * @param re
 	 *            process node;
 	 */
 	void checkParameters(MetabolicReaction re) {
 		// String parameters = re.getParameters();
 		// //TODO replace with rules
 		// if(parameters.trim().length()>0 &&
 		// !parameters.matches("^(\\s*\\w+\\s*=\\s*[0-9eE\\-+.]+\\s*;)+$")){
 		// // !parameters.matches("^(\\s*\\w+\\s*=\\s*[0-9eE-+.]+\\s*;)+$")){
 		// error("Invalid parameter definition"+parameters);
 		// }
 	}
 
 }
 
 /*
  * $Log: MetabolicNDOMFactory.java,v $ Revision 1.8 2008/07/15 11:14:32 smoodie
  * Refactored so code compiles with new Toolkit framework.
  * 
  * Revision 1.7 2008/06/24 10:07:40 radams moved rule builder upt oAbstractNDOM
  * parsers
  * 
  * Revision 1.6 2008/06/23 14:42:35 radams added report builder to parser
  * 
  * Revision 1.5 2008/06/23 14:22:34 radams added report builder to parser
  * 
  * Revision 1.4 2008/06/20 22:48:19 radams imports
  * 
  * Revision 1.3 2008/06/09 13:26:29 asorokin Bug fixes for SBML export
  * 
  * Revision 1.2 2008/06/02 15:14:31 asorokin KineticLaw parameters parsing and
  * validation
  * 
  * Revision 1.1 2008/06/02 10:31:42 asorokin Reference to Service provider from
  * all Service interfaces
  * 
  */
