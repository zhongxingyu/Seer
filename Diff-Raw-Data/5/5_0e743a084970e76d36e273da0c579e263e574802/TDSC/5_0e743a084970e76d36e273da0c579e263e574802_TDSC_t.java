 /*
  * Copyright (c) Fabien Hermenier
  *
  *         This file is part of Entropy.
  *
  *         Entropy is free software: you can redistribute it and/or modify
  *         it under the terms of the GNU Lesser General Public License as published by
  *         the Free Software Foundation, either version 3 of the License, or
  *         (at your option) any later version.
  *
  *         Entropy is distributed in the hope that it will be useful,
  *         but WITHOUT ANY WARRANTY; without even the implied warranty of
  *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *
  *         GNU Lesser General Public License for more details.
  *         You should have received a copy of the GNU Lesser General Public License
  *         along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package microDSN;
 
 import btrpsl.BtrPlaceVJob;
 import btrpsl.BtrPlaceVJobBuilder;
 import btrpsl.constraint.ConstraintsCatalog;
 import btrpsl.constraint.ConstraintsCatalogBuilderFromProperties;
 import btrpsl.includes.BasicIncludes;
 import choco.kernel.common.logging.ChocoLogging;
 import choco.kernel.common.logging.Verbosity;
 import entropy.PropertiesHelper;
 import entropy.configuration.*;
 import entropy.configuration.parser.FileConfigurationSerializerFactory;
 import entropy.plan.Plan;
 import entropy.plan.TimedReconfigurationPlan;
 import entropy.plan.choco.CustomizableSplitablePlannerModule;
 import entropy.plan.durationEvaluator.DurationEvaluator;
 import entropy.plan.durationEvaluator.MockDurationEvaluator;
 import entropy.template.DefaultVirtualMachineTemplateFactory;
 import entropy.template.VirtualMachineTemplate;
 import entropy.template.VirtualMachineTemplateFactory;
 import entropy.template.stub.StubVirtualMachineTemplate;
 import entropy.vjob.DefaultVJob;
 import entropy.vjob.Fence;
 import entropy.vjob.PlacementConstraint;
 import entropy.vjob.VJob;
 import entropy.vjob.builder.DefaultVJobElementBuilder;
 import entropy.vjob.builder.VJobBuilder;
 import entropy.vjob.builder.VJobElementBuilder;
 import gnu.trove.THashSet;
 import instancesMaker.ConfigurationAlterer;
 import instancesMaker.Generator;
 import instancesMaker.VJobAlterer;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 /**
  * Workload generator for TDSC.
  * @author Fabien Hermenier
  */
 public class TDSC {
 
     //Duration of actions.
     private final static int apacheBoot = 4;
     private final static int apacheHalt = 5;
     private final static int tomcatBoot = 9;
     private final static int tomcatHalt = 10;
     private final static int mysqlBoot = 5;
     private final static int mysqlHalt = 6;
 
     private final static double LONELY_RATIO = 0;
 
     private final static int MIN_APP_SIZE = 6;
 
     private final static int MAX_APP_SIZE = 30;
 
     private static final int SWITCH_SIZE = 250;
 
     private static final int SERVER_UCPU = 200;
 
     private static final int SERVER_MEM = 1600;
 
     private static final int NB_SERVERS = 5000;
 
     private static final int NB_CPUS = 8;
 
     private static final double FAIL_RATIO = 0.05;
 
     private static final double NB_INCR_APPS = 0.1;
 
    private static final int NB_INSTANCES = 2;
 
 
     public static void main(String[] args) {
         DurationEvaluator durations = new MockDurationEvaluator(1, 2, 3, 4, 5, 6, 7, 8, 9);
         ChocoLogging.setVerbosity(Verbosity.SOLUTION);
 
         try {
 
             VirtualMachineTemplateFactory tplFactory = makeTemplateFactory();
             VJobElementBuilder eb = new DefaultVJobElementBuilder(tplFactory);
             ConstraintsCatalog cat = new ConstraintsCatalogBuilderFromProperties(new PropertiesHelper("config/btrpVjobs.properties")).build();
             BtrPlaceVJobBuilder b = new BtrPlaceVJobBuilder(eb, cat);
             b.setIncludes(new BasicIncludes());
             int [] ratios = {3,4,5,6};
             for (int i  : ratios) {
                generate(b, durations, "wkld-simple" + File.separator + "r" + i, i, NB_INSTANCES);
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println(e.getMessage());
         }
     }
 
     private static void dumpPart(VJobBuilder b, String output, List<VJob> vjobs, List<String> scriptsWithConstraints, VJob fencer, List<ManagedElementSet<Node>> splits, String infra, int size) throws Exception {
 
             dumpScripts(b, output, vjobs, scriptsWithConstraints, infra);
             basicFencerDump(mergeFences(fencer, mergeBy(splits, size), "fencer" + size), output);
 
             PrintWriter out = null;
             try {
                 out = new PrintWriter(new FileWriter(output + File.separator + "datacenter.btrp"));
                 out.print(infra);
                 out.close();
             } finally {
                 if (out != null) {
                     out.close();
                 }
             }
     }
 
     public static VirtualMachineTemplateFactory makeTemplateFactory() {
         DefaultVirtualMachineTemplateFactory tplFactory = new DefaultVirtualMachineTemplateFactory();
         int[] cpu = {30, 40, 50, 60};
         int[] mem = {100, 200, 300};
         for (int c : cpu) {
             for (int m : mem) {
                 StubVirtualMachineTemplate st = new StubVirtualMachineTemplate("c" + c + "m" + m, 1, c, m, new THashSet<String>());
                 tplFactory.add(st);
             }
         }
         return tplFactory;
     }
 
     private static String generateScript(String ns, VirtualMachineTemplate tpl1, int nbT1, VirtualMachineTemplate tpl2, int nbT2, VirtualMachineTemplate tpl3, int nbT3, boolean useCstrs, boolean isolated) {
         String str = "namespace " + ns + ";\n\n"
                 + "import datacenter;\n\n"
                 + "VM[1.." + nbT1 + "]: " + tpl1.getIdentifier() + "<clone,boot=" + apacheBoot + ", halt=" + apacheHalt + ">;\n"
                 + "VM[" + (nbT1 + 1) + ".." + (nbT1 + nbT2) + "]: " + tpl2.getIdentifier() + "<clone,boot=" + tomcatBoot + ", halt=" + tomcatHalt + ">;\n"
                 + "VM[" + (nbT1 + nbT2 + 1) + ".." + (nbT1 + nbT2 + nbT3) + "]: " + tpl3.getIdentifier() + "<clone,boot=" + mysqlBoot + ", halt=" + mysqlHalt + ">;\n"
                 + "\n"
                 + "$T1 = VM[1.." + nbT1 + "];\n"
                 + "$T2 = VM[" + (nbT1 + 1) + ".." + (nbT1 + nbT2) + "];\n"
                 + "$T3 = VM[" + (nbT1 + nbT2 + 1) + ".." + (nbT1 + nbT2 + nbT3) + "];\n";
         if (useCstrs) {
             str += "for $t in $T[1..3] {\n"
                     + "\tspread($t);\n"
                     + "}\n"
                     + "among($T3, $leafs);\n";
             if (isolated) {
                 str += "lonely($T1 + $T2 + $T3);\n";
             }
         }
         str += "export $me to *;";
         return str;
     }
 
     private static String storeInfra(Configuration cfg, int sizeSwitch) {
         int nbNodes = cfg.getOnlines().size();
         StringBuilder b = new StringBuilder();
         b.append("namespace datacenter;\n");
         b.append("$leafs = @N[1..");
         b.append(nbNodes);
         b.append("] % ");
         b.append(sizeSwitch);
         b.append(";\n");
         b.append("export $leafs to *;\n");
         return b.toString();
     }
 
     private static int[] generateSizing(int max) {
         int curMax = Math.min(max, MAX_APP_SIZE);
         int t1 = curMax == 6 ? 2 : rnd.nextInt(curMax - 4 - 2) + 2; //at least 2 VMs, at most max - 4 (2 per tiers)
         curMax -= t1;
         int t2 = curMax == 4 ? 2 : rnd.nextInt(curMax - 2 - 2) + 2;
         curMax -= t2;
         return new int[]{t1, t2, curMax};
     }
 
 
     private static final Random rnd = new Random();
 
     private static Configuration createInfra(int nbNodes, int nbCPUs, int capaCPU, int capaMem) {
         Configuration cfg = new SimpleConfiguration();
         for (int i = 1; i <= nbNodes; i++) {
             Node n = new SimpleNode("N" + i, nbCPUs, capaCPU, capaMem);
             cfg.addOnline(n);
         }
         return cfg;
     }
 
     /**
      * Change the demand to have a non-viable configuration
      *
      * @param vjobs the vjobs to manipulate
      * @param ratio the ratio of vjobs that must have their demand increased by 30% (at most)
      */
     private static void setNonViable(List<VJob> vjobs, double ratio) {
         int nbToModify = (int) (ratio * vjobs.size());
         while (nbToModify > 0) {
             nbToModify--;
             int idx = rnd.nextInt(vjobs.size());
             VJob v = vjobs.get(idx);
             double r = VJobAlterer.getCPUDemandRatio(v);
             r = r >= 0.8 ? 1.0 : (r + 0.3);
             VJobAlterer.setCPUDemandRatio(v, r);
         }
     }
 
     private static String statistics(Configuration cfg) {
         StringBuilder b = new StringBuilder();
         b.append("uCPU: ");
         b.append(100 * ConfigurationAlterer.getCPUConsumptionLoad(cfg) + "% ->" +
                 (100 * ConfigurationAlterer.getCPUDemandLoad(cfg)) + "%  " +
                 "mem: " + (100 * ConfigurationAlterer.getMemoryConsumptionLoad(cfg)) + "%");
         return b.toString();
     }
 
     private static List<ManagedElementSet<Node>> split(ManagedElementSet<Node> all, int splitSize) {
         List<ManagedElementSet<Node>> splits = new ArrayList<ManagedElementSet<Node>>();
         ManagedElementSet<Node> cur = new SimpleManagedElementSet<Node>();
         splits.add(cur);
         for (Node n : all) {
             cur.add(n);
             if (cur.size() == splitSize) {
                 cur = new SimpleManagedElementSet<Node>();
                 splits.add(cur);
             }
         }
         if (cur.isEmpty()) {
             splits.remove(cur);
         }
         return splits;
     }
 
     private static VJob generateFencer(List<VJob> vjobs, List<ManagedElementSet<Node>> splits, int fenceSize) {
         VJob fencer = new DefaultVJob("fencer");
         int i = 0;
         for (VJob v : vjobs) {
             Fence f = new Fence(v.getVirtualMachines(), splits.get(i));
             fencer.addConstraint(f);
             i = (i + 1) % splits.size();
         }
         return fencer;
     }
 
     private static void generate(BtrPlaceVJobBuilder b, DurationEvaluator durations, String output, int ratio, int nbInstances) {
         System.out.println(nbInstances + " RP(s) with a consolidation ratio of " + ratio + ":1 ...");
         String root = output;
         ChocoLogging.setVerbosity(Verbosity.SILENT);
         SplitableVJobLauncher plan = new SplitableVJobLauncher(durations);
         plan.setPartitioningMode(SplitableVJobLauncher.PartitioningMode.sequential);
         Configuration cfg = createInfra(NB_SERVERS, NB_CPUS, SERVER_UCPU, SERVER_MEM);
         b.getElementBuilder().useConfiguration(cfg);
         BtrPlaceVJob infra = null;
         String dc = storeInfra(cfg, SWITCH_SIZE);
         try {
             infra = b.build(dc);
         } catch (Exception e) {
             System.out.println(e.getMessage());
             System.exit(1);
         }
         ((BasicIncludes) b.getIncludes()).add(infra);
         int maxVMs = cfg.getAllNodes().size() * ratio;
         int nbApps = 0;
         int nbVMs = 0;
         List<String> scriptsWithConstraints = new ArrayList<String>();
         List<String> scriptsWoConstraints = new ArrayList<String>();
         List<VJob> vjobs = new ArrayList<VJob>();
 
         while (nbVMs < maxVMs) {
             int size = generateAppSize(maxVMs - nbVMs);
             if (maxVMs - nbVMs - size < MIN_APP_SIZE) {
                 size += (maxVMs - nbVMs - size);
             }
 
             VirtualMachineTemplate[] tpls = generateTypes(b.getElementBuilder().getTemplates());
             int[] distrib = generateSizing(size);
             String scr1 = generateScript("clients.c" + nbApps, tpls[0], distrib[0], tpls[1], distrib[1], tpls[2], distrib[2], false, isIsolated());
             String scr2 = generateScript("clients.c" + nbApps, tpls[0], distrib[0], tpls[1], distrib[1], tpls[2], distrib[2], true, isIsolated());
             try {
                 VJob v = b.build(scr2);
                 vjobs.add(v);
                 scriptsWoConstraints.add(scr1);
                 scriptsWithConstraints.add(scr2);
             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
 
             nbVMs += size;
             nbApps++;
         }
 
         if (nbVMs != NB_SERVERS * ratio) {
             System.err.println(nbVMs + " VMs, expected " + (NB_SERVERS * ratio));
             System.exit(1);
         }
         Configuration dst = null;
         VJob fencer = null;
         List<ManagedElementSet<Node>> splits = split(cfg.getOnlines(), SWITCH_SIZE);
         List<VJob> allVJobs = new ArrayList<VJob>();
         allVJobs.addAll(vjobs);
         fencer = generateFencer(vjobs, splits, SWITCH_SIZE);
         allVJobs.add(fencer);
         for (int i = 0; i < nbInstances; i++) {
             if (i % 5 == 0) {
                 VJobAlterer.setRandomCPUDemand(0.2, 0.9, vjobs);
                 //Stay inside the fence
                 dst = Generator.generate(plan, cfg.getOnlines(), cfg.getOfflines(), allVJobs);
                 VJobAlterer.setCPUConsumptionToDemand(vjobs);
             }
             ConfigurationAlterer.shuffle(dst, allVJobs, 100);
             System.out.print("RP " + i + "/" + nbInstances);
 
             try {
                 dumpHF(root + File.separator + "nr", dst, i, FAIL_RATIO);
                 System.out.print(" NR");
                 Configuration x = dumpLI(root + File.separator + "li", vjobs, dst, i, NB_INCR_APPS);
                 if (x == null) {
                     System.out.println(" !LI -> retry");
                     i--;
                 } else {
                     System.out.println(" LI (" + statistics(x) + ")");
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
         }
 
         System.out.println("Writing the scripts");
         try {
             //% of active constraints
             dumpScripts(b, root + File.separator + "c0p" + NB_SERVERS, vjobs, scriptsWoConstraints, dc);
             dumpScripts(b, root + File.separator + "c33p" + NB_SERVERS, vjobs, merge(scriptsWoConstraints, scriptsWithConstraints, 0.33), dc);
             dumpScripts(b, root + File.separator + "c66p" + NB_SERVERS, vjobs, merge(scriptsWoConstraints, scriptsWithConstraints, 0.66), dc);
             int[] parts = new int[]{250, 500, 1000, 2500, 5000};
             for (int i = 0; i < parts.length; i++) {
                 dumpPart(b, root + File.separator + "c100p" + parts[i], vjobs, scriptsWithConstraints, fencer, splits, dc, parts[i]);
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private static void basicFencerDump(VJob v, String output) throws IOException {
 
         String path = v.id().replaceAll("\\.", File.separator);
         File f = new File(output + File.separator + path + ".btrp");
         f.getParentFile().mkdirs();
         PrintWriter out = null;
         int lastId = 0;
         Map<ManagedElementSet<Node>, String> variables = new HashMap<ManagedElementSet<Node>, String>();
 
         try {
             out = new PrintWriter(new FileWriter(f));
             out.println("namespace " + v.id() + ";");
             out.println("import datacenter;");
             out.println("import clients.*;");
 
             for (PlacementConstraint c : v.getConstraints()) {
                 ManagedElementSet<Node> part = c.getNodes();
                 if (!variables.containsKey(part)) {
                     String var = "$P" + (lastId++);
                     variables.put(part, var);
                     out.println(var + " = " + expand(part) + ";");
                 }
             }
 
 
             for (PlacementConstraint c : v.getConstraints()) {
                 VirtualMachine vm = c.getAllVirtualMachines().get(0);
                 String id = vm.getName().substring(0, vm.getName().lastIndexOf("."));
                 out.print("fence(");
                 out.print("$" + id);
                 out.print(",");
                 out.print(variables.get(c.getNodes()));
                 out.println(");");
             }
             out.close();
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
 
 
     private static String expand(ManagedElementSet<Node> s) {
         StringBuilder b = new StringBuilder("{");
         for (Iterator<Node> ite = s.iterator(); ite.hasNext(); ) {
             ManagedElement e = ite.next();
             b.append("@" + e.getName());
             if (ite.hasNext()) {
                 b.append(", ");
             }
         }
         b.append("}");
         return b.toString();
     }
 
     private static VJob mergeFences(VJob fencer, List<ManagedElementSet<Node>> splits, String id) {
         VJob f = new DefaultVJob(id);
         for (PlacementConstraint cstr : fencer.getConstraints()) {
             boolean found = false;
             for (ManagedElementSet<Node> part : splits) {
                 if (part.containsAll(cstr.getNodes())) {
                     f.addConstraint(new Fence(cstr.getAllVirtualMachines(), part));
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 System.err.println("Unable to merge constraints in the fencer");
                 System.exit(1);
             }
         }
         return f;
     }
 
 
     private static List<ManagedElementSet<Node>> mergeBy(List<ManagedElementSet<Node>> splits, int size) {
         List<ManagedElementSet<Node>> merge = new ArrayList<ManagedElementSet<Node>>();
         ManagedElementSet<Node> cur = new SimpleManagedElementSet<Node>();
         for (ManagedElementSet<Node> split : splits) {
             cur.addAll(split);
             if (cur.size() == size) {
                 merge.add(cur);
                 cur = new SimpleManagedElementSet<Node>();
             }
             assert cur.size() < size;
         }
         if (!cur.isEmpty()) { //Last part, may be partially fulfilled
             merge.add(cur);
         }
         return merge;
     }
 
     private static void dumpHF(String output, Configuration cfg, int id, double ratio) throws IOException {
         Configuration dst = cfg.clone();
         ManagedElementSet<VirtualMachine> vms = ConfigurationAlterer.applyNodeFailureRatio(dst, ratio); //VMs were removed from dst
         Configuration src = dst.clone();
         for (VirtualMachine vm : vms) {
             src.addWaiting(vm);
             if (!dst.setRunOn(vm, dst.getOnlines().get(0))) {
                 System.out.println("Unable to force " + vm.getName() + " at being running in the future");
                 System.exit(1);
             }
         }
         if (!Configurations.currentlyOverloadedNodes(src).isEmpty()) {
             System.out.println("Error: " + Configurations.currentlyOverloadedNodes(src) + " are overloaded on the source configuration");
             System.exit(1);
         }
 
         FileConfigurationSerializerFactory.getInstance().write(src, output + File.separator + id + "-src" + FileConfigurationSerializerFactory.PROTOBUF_EXTENSION);
         FileConfigurationSerializerFactory.getInstance().write(dst, output + File.separator + id + "-dst" + FileConfigurationSerializerFactory.PROTOBUF_EXTENSION);
     }
 
     private static Configuration dumpLI(String output, List<VJob> vjobs, Configuration cfg, int id, double ratio) throws IOException {
         setNonViable(vjobs, ratio);
         if (Configurations.futureOverloadedNodes(cfg).isEmpty()) {
             return null;
         }
         File root = new File(output);
         root.mkdirs();
         FileConfigurationSerializerFactory.getInstance().write(cfg, output + File.separator + id + "-src" + FileConfigurationSerializerFactory.PROTOBUF_EXTENSION);
         VJobAlterer.setCPUDemandToConsumption(vjobs);
         return cfg;
     }
 
     private static void dumpScripts(VJobBuilder b, String output, List<VJob> vjobs, List<String> scripts, String infra) throws Exception {
         int nbVMs = 0;
         for (int i = 0; i < scripts.size(); i++) {
             String path = vjobs.get(i).id().replaceAll("\\.", File.separator);
             File f = new File(output + File.separator + path + ".btrp");
             nbVMs += vjobs.get(i).getVirtualMachines().size();
             f.getParentFile().mkdirs();
             PrintWriter out = null;
             try {
                 out = new PrintWriter(new FileWriter(f));
                 out.print(scripts.get(i));
                 out.close();
             } finally {
                 if (out != null) {
                     out.close();
                 }
             }
         }
         PrintWriter out = null;
         try {
             out = new PrintWriter(new FileWriter(output + File.separator + "datacenter.btrp"));
             out.print(infra);
             out.close();
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
 
     private static List<String> merge(List<String> l1, List<String> l2, double l2Ratio) {
         List<String> res = new ArrayList<String>();
         int nb = (int) (l2Ratio * l2.size());
         for (int i = 0; i < l1.size(); i++) {
             res.add(nb-- > 0 ? l2.get(i) : l1.get(i));
         }
         return res;
     }
 
     private static VirtualMachineTemplate[] generateTypes(VirtualMachineTemplateFactory fct) {
         String[] keys = fct.getAvailables().toArray(new String[fct.getAvailables().size()]);
         VirtualMachineTemplate[] tpls = new VirtualMachineTemplate[3];
         tpls[0] = fct.getTemplate(keys[rnd.nextInt(keys.length - 1)]);
         tpls[1] = fct.getTemplate(keys[rnd.nextInt(keys.length - 1)]);
         tpls[2] = fct.getTemplate(keys[rnd.nextInt(keys.length - 1)]);
         return tpls;
     }
 
     private static int generateAppSize(int remainder) {
         return Math.min(remainder, rnd.nextInt(MAX_APP_SIZE - MIN_APP_SIZE) + MIN_APP_SIZE);
     }
 
     public static boolean isIsolated() {
         return rnd.nextInt(100) < 100 * LONELY_RATIO;
     }
 }
