 package fr.imag.adele.apam.ASMImpl;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 
 import fr.imag.adele.am.exception.ConnectionException;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.CompositeTypeImpl;
 import fr.imag.adele.apam.apamAPI.ASMImpl;
 import fr.imag.adele.apam.apamAPI.ASMImplBroker;
 import fr.imag.adele.apam.apamAPI.ASMInst;
 import fr.imag.adele.apam.apamAPI.ASMSpec;
 import fr.imag.adele.apam.apamAPI.Composite;
 import fr.imag.adele.apam.apamAPI.CompositeType;
 import fr.imag.adele.apam.util.Attributes;
 import fr.imag.adele.apam.util.AttributesImpl;
 import fr.imag.adele.apam.util.Util;
 import fr.imag.adele.sam.Implementation;
 import fr.imag.adele.sam.Instance;
 import fr.imag.adele.sam.broker.ImplementationBroker;
 
 public class ASMImplImpl extends AttributesImpl implements ASMImpl {
 
     protected final Set<ASMImpl>       uses         = new HashSet<ASMImpl>();      // all relations uses
     protected final Set<ASMImpl>       invUses      = new HashSet<ASMImpl>();      // all reverse relations uses
 
     protected final Set<CompositeType> inComposites = new HashSet<CompositeType>(); // composite it is contained in.
 
     protected String                   name;
     protected ASMSpec                  mySpec;
     protected Implementation           samImpl      = null;
 
     protected Set<ASMInst>             instances    = new HashSet<ASMInst>();      // the instances of that impl.
 
     protected Set<DependencyModel>     dependencyModel;                            // The dependencies of that Impl, as
                                                                                     // known by the handler.
 
     /**
      * Instantiate a new service implementation.
      * 
      * @param instance the ASM instance
      * 
      * 
      */
 
     // used ONLY when creating a composite type
     protected ASMImplImpl() {
 
     }
 
     public ASMImplImpl(CompositeType compo, ASMSpecImpl spec, Implementation impl, Attributes props) {
         if (impl == null) {
             new Exception("Sam Implementation cannot be null when creating an imple").printStackTrace();
         }
         if (compo == null) { // compo is null for the root composite AND its main implem.
             // done in create composite type when compo is null
             new Exception("compo is null").printStackTrace();
         }
 
         name = impl.getName(); // warning, for composites, it is a different name. Overloaded in createCOmpositeType
         mySpec = spec;
         spec.addImpl(this);
         ((ASMImplBrokerImpl) CST.ASMImplBroker).addImpl(this);
         samImpl = impl;
         initializeNewImpl(compo, props);
 
     }
 
     // warning : for setting composite name, which is different from sam name.
     public void setName(String name) {
         this.name = name;
     }
 
     public void initializeNewImpl(CompositeType compo, Attributes props) {
         // myComposites.add(compo);
         compo.addImpl(this);
         if (props == null) {
             props = new AttributesImpl();
         }
         props.setProperty(Attributes.APAMCOMPO, compo.getName());
         // initialize properties. A fusion of SAM and APAM values
         try {
             this.setProperties(Util.mergeProperties(this, props, getSamImpl().getProperties()));
         } catch (ConnectionException e) {
             e.printStackTrace();
         }
         dependencyModel = (Set<DependencyModel>) getProperty(CST.A_DEPENDENCIES);
     }
 
     @Override
     public String toString() {
         return name;
         // String ret;
         // if (name == null)
         // ret = " (" + samImpl.getName() + ") ";
         // else
         // ret = name + " (" + samImpl.getName() + ") ";
         // return ret;
     }
 
     /**
      * From an implementation, create an instance. Creates both the SAM and ASM instances with the same properties.
      * 
      * @throws IllegalArgumentException
      * @throws UnsupportedOperationException
      * @throws ConnectionException
      */
     @Override
     public ASMInst createInst(Composite instCompo, Attributes initialproperties) {
         if ((getProperty(CST.A_INSTANTIABLE) != null) && getProperty(CST.A_INSTANTIABLE).equals(CST.V_FALSE)) {
             System.out.println("Implementation " + this + " is not instantiable");
             return null;
         }
         try {
             Instance samInst;
             if (initialproperties == null)
                 samInst = CST.SAMImplBroker.createInstance(samImpl.getImplPid(), null);
             else
                 samInst = CST.SAMImplBroker.createInstance(samImpl.getImplPid(),
                         initialproperties.attr2Properties());
             ASMInstImpl inst = new ASMInstImpl(this, instCompo, initialproperties, samInst, false);
             // addInst(inst);
             return inst;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     // WARNING : no control ! Only called by the instance Broker.
     public void addInst(ASMInst inst) {
         if (inst != null)
             instances.add(inst);
     }
 
     @Override
     public ASMSpec getSpec() {
         return mySpec;
     }
 
     @Override
     public ASMInst getInst(String targetName) {
         if (targetName == null)
             return null;
         for (ASMInst inst : instances) {
             if (inst.getName().equals(targetName))
                 return inst;
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see fr.imag.adele.sam.Implementation#getInstances()
      */
     @Override
     public Set<ASMInst> getInsts() {
         return Collections.unmodifiableSet(instances);
         // return new HashSet <ASMInst> (instances) ;
     }
 
     /**
      * returns the first instance only.
      */
     @Override
     public ASMInst getInst() {
         if ((instances == null) || (instances.size() == 0))
             return null;
         return (ASMInst) instances.toArray()[0];
     }
 
     @Override
     public Set<ASMInst> getInsts(Filter query) throws InvalidSyntaxException {
         if (query == null)
             return getInsts();
         Set<ASMInst> ret = new HashSet<ASMInst>();
         for (ASMInst inst : instances) {
             if (query.match((AttributesImpl) inst))
                 ret.add(inst);
         }
         return ret;
     }
 
     @Override
     public Set<ASMInst> getInsts(Set<Filter> constraints) {
         if ((constraints == null) || constraints.isEmpty())
             return Collections.unmodifiableSet(instances);
         Set<ASMInst> ret = new HashSet<ASMInst>();
         for (ASMInst inst : instances) {
             for (Filter filter : constraints) {
                if (filter.match((AttributesImpl) inst)) {
                     ret.add(inst);
                 }
             }
         }
         return ret;
     }
 
     @Override
     public ASMInst getInst(Set<Filter> constraints, List<Filter> preferences) {
         Set<ASMInst> insts = null;
         if ((preferences != null) && !preferences.isEmpty()) {
             insts = getInsts(constraints);
         } else
             insts = instances;
         if ((constraints == null) || constraints.isEmpty())
             return ((ASMInst) insts.toArray()[0]);
 
         return getPreferedInst(insts, preferences);
     }
 
     @Override
     public ASMInst getPreferedInst(Set<ASMInst> candidates, List<Filter> preferences) {
         if ((preferences == null) || preferences.isEmpty()) {
             return (ASMInst) candidates.toArray()[0];
         }
         ASMInst winner = null;
         int maxMatch = -1;
         for (ASMInst inst : candidates) {
             int match = 0;
             for (Filter filter : preferences) {
                 if (!filter.match((AttributesImpl) inst))
                     break;
                 match++;
             }
             if (match > maxMatch) {
                 maxMatch = match;
                 winner = inst;
             }
         }
         System.out.println("   Selected : " + winner);
         return winner;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     /**
      * returns the visibility.
      * WARNING : an implem can pertain to various composite types that can overload (reduce) the visibility.
      * Is returned only the intrinsic visibility.
      */
     @Override
     public String getVisible() {
         String visible = (String) getProperty(CST.A_VISIBLE);
         if (visible == null)
             visible = CST.V_GLOBAL;
 //        String compoVisible;
 //        for (CompositeType compoDest : getInCompositeType()) {
 //            // Check if the composite type overloads the implementation scope
 //            compoVisible = ((CompositeTypeImpl) compoDest).getVisibleInCompoType(this);
 //            visible = Util.getEffectiveScope(visible, compoVisible);
 //        }
         return visible;
     }
 
     @Override
     public String getShared() {
         String shared = (String) getProperty(CST.A_SHARED);
         if (shared == null)
             shared = CST.V_TRUE;
         return shared;
     }
 
     // @Override
     // public void setScope(String scope) {
     // if ((scope.equals(CST.V_TRUE) || (scope.equals(CST.V_FALSE))))
     // setProperty(CST.A_SCOPE, scope);
     // }
 
     // relation uses control
     public void addUses(ASMImpl dest) {
         if (uses.contains(dest))
             return;
         uses.add(dest);
         ((ASMImplImpl) dest).addInvUses(this);
         ((ASMSpecImpl) getSpec()).addRequires(dest.getSpec());
     }
 
     public void removeUses(ASMImpl dest) {
         for (ASMInst inst : instances) {
             for (ASMInst instDest : inst.getWireDests())
                 if (instDest.getImpl() == dest) {
                     return; // it exists another instance that uses that destination. Do nothing.
                 }
         }
         uses.remove(dest);
         ((ASMImplImpl) dest).removeInvUses(this);
         ((ASMSpecImpl) getSpec()).removeRequires(dest.getSpec());
     }
 
     private void addInvUses(ASMImpl orig) {
         invUses.add(orig);
     }
 
     private void removeInvUses(ASMImpl orig) {
         invUses.remove(orig);
     }
 
     public void addInComposites(CompositeType compo) {
         inComposites.add(compo);
     }
 
     public void removeInComposites(CompositeType compo) {
         inComposites.remove(compo);
     }
 
     @Override
     public Set<ASMImpl> getUses() {
         return Collections.unmodifiableSet(uses);
     }
 
     @Override
     public Set<ASMImpl> getInvUses() {
         return Collections.unmodifiableSet(invUses);
     }
 
     //
     @Override
     public void remove() {
         for (ASMInst inst : instances) {
             ((ASMInstImpl) inst).remove();
         }
         CST.ASMImplBroker.removeImpl(this);
         ((ASMSpecImpl) getSpec()).removeImpl(this);
         // remove the APAM specific attributes in SAM
         if (samImpl != null) {
             try {
                 samImpl.removeProperty(Attributes.APAMCOMPO);
             } catch (ConnectionException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void removeInst(ASMInst inst) {
         instances.remove(inst);
     }
 
     @Override
     public Implementation getSamImpl() {
         return samImpl;
     }
 
     @Override
     public boolean isInstantiable() {
         return samImpl.isInstantiator();
     }
 
     @Override
     public Set<CompositeType> getInCompositeType() {
         return Collections.unmodifiableSet(inComposites);
     }
 
     @Override
     public Set<DependencyModel> getDependencies() {
         Set<DependencyModel> deps = (Set<DependencyModel>) getProperty(CST.A_DEPENDENCIES);
         if (deps == null)
             return Collections.EMPTY_SET;
         return deps;
     }
 }
