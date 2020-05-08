 package fi.lolcatz.profiler;
 
 import fi.lolcatz.profiledata.ProfileData;
 import org.apache.log4j.Logger;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.*;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 import java.util.*;
 
 import static org.objectweb.asm.tree.AbstractInsnNode.*;
 
 /**
  * Transformer that inserts counter increment code in the beginning of basic blocks.
  */
 public class ProfilerTransformer implements ClassFileTransformer, Opcodes {
 
     public String className;
     private static Logger logger = Logger.getLogger(ProfilerTransformer.class.getName());
 
     /**
      * Transform class using tree API. asm4-guide.pdf pg. 96 {@inheritDoc}
      */
     @Override
     public byte[] transform(
             ClassLoader loader,
             String className,
             Class<?> classBeingRedefined,
             ProtectionDomain protectionDomain,
             byte[] classfileBuffer) throws IllegalClassFormatException {
         this.className = className;
         try {
             ProfileData.disallowProfiling();
             // Don't touch the class if it's blacklisted
             if (ClassBlacklist.isBlacklisted(className)) {
                 ProfileData.allowProfiling();
                 return null;
             }
             logger.info("Class: " + className);
             ClassNode classNode = Util.initClassNode(classfileBuffer);
 
             // Add counter increment codes in the beginning of basic blocks
             for (MethodNode methodNode : (List<MethodNode>) classNode.methods) {
                 boolean isNative = (methodNode.access & ACC_NATIVE) != 0;
                 if (isNative) {
                     logger.info("Native method found: " + methodNode.name);
                     continue;
                 }
                 logger.info("  Method: " + methodNode.name + methodNode.desc);
                 InsnList insns = methodNode.instructions;
                 logger.trace(Util.getInsnListString(insns));
 
                 // Increase max stack size to allow counter increments
                 methodNode.maxStack += 1;
                 Set<AbstractInsnNode> basicBlockBeginnings = findBasicBlockBeginnings(insns);
                 ArrayList<LinkedList<AbstractInsnNode>> basicBlocks = findBasicBlockInsns(basicBlockBeginnings);
                 insertCountersToBasicBlocks(methodNode, basicBlocks);
             }
             byte[] bytecode = Util.generateBytecode(classNode);
 
             //String filename = className.substring(className.lastIndexOf('/') + 1);
             //Util.writeByteArrayToFile(filename + ".class", bytecode);
 
             // Initialize counter arrays
             if (!Agent.isRetransforming()) ProfileData.initialize();
 
             ProfileData.allowProfiling();
             
             return bytecode;
         } catch (Exception e) { // Catch all exceptions because they are silenced otherwise.
             logger.fatal(e.getMessage(), e);
         } catch (Error e) {
             logger.fatal(e.getMessage(), e);
         }
         ProfileData.allowProfiling();
         return null;
     }
 
     /**
      * Insert counter addition bytecode to the beginning of all given basic blocks in basicBlocks.
      *
      * @param methodNode MethodNode where counter bytecode is added.
      * @param basicBlocks List of basic blocks instructions where to add the counter bytecode.
      */
     public void insertCountersToBasicBlocks(MethodNode methodNode, ArrayList<LinkedList<AbstractInsnNode>> basicBlocks) {
         InsnList insns = methodNode.instructions;
         for (LinkedList<AbstractInsnNode> basicBlockInsns : basicBlocks) {
             long cost = calculateCost(basicBlockInsns);
             int index = ProfileData.addBasicBlock(cost, className + "." + methodNode.name + methodNode.desc);
             AbstractInsnNode basicBlockBeginning = basicBlockInsns.getFirst();
             if (basicBlockBeginning.getType() == LABEL)
                 insns.insert(basicBlockBeginning, createCounterIncrementInsnList(index));
             else
                 insns.insertBefore(basicBlockBeginning, createCounterIncrementInsnList(index));
         }
     }
 
     /**
      * Calculate total cost of bytecode instructions in given list.
      *
      * @param basicBlockInsns List of AbstractInsnNode to calculate cost from.
      * @return Total cost of instructions.
      */
     public long calculateCost(LinkedList<AbstractInsnNode> basicBlockInsns) {
         long cost = 0;
         for (AbstractInsnNode node : basicBlockInsns) {
             int type = node.getType();
             switch (type) {
                 case LABEL:
                 case LINE:
                 case FRAME:
                     break;
                 case METHOD_INSN:
                     MethodInsnNode n = (MethodInsnNode)node;
                    if (CostlyMethodList.isMethodCostly(n.owner+ "." +n.name+n.desc))
                         cost += CostlyMethodList.getCostOfCostlyMethods();
                 default:
                     cost += ComplexityCost.getCost(type);
             }
         }
         return cost;
     }
 
     /**
      * Find instructions that belong to the basic blocks that are started by instructions in basicBlockBeginnings and
      * add create the returned list.
      *
      * @param basicBlockBeginnings Set of AbstractInsnNode objects that start a new basic block.
      * @return List where every ArrayList represents a basic block and contains the instruction of the basic block as a
      *         linked list.
      */
     public ArrayList<LinkedList<AbstractInsnNode>> findBasicBlockInsns(Set<AbstractInsnNode> basicBlockBeginnings) {
         ArrayList<LinkedList<AbstractInsnNode>> basicBlocks = new ArrayList<LinkedList<AbstractInsnNode>>();
         for (AbstractInsnNode basicBlockBeginning : basicBlockBeginnings) {
             if (basicBlockBeginning == null) continue;
             LinkedList<AbstractInsnNode> basicBlock = new LinkedList<AbstractInsnNode>();
             basicBlock.add(basicBlockBeginning);
             AbstractInsnNode nextInsn = basicBlockBeginning.getNext();
             while (nextInsn != null && !basicBlockBeginnings.contains(nextInsn)) {
                 basicBlock.add(nextInsn);
                 nextInsn = nextInsn.getNext();
             }
             basicBlocks.add(basicBlock);
         }
         return basicBlocks;
     }
 
     /**
      * Find AbstractInsnNode objects that represent an instruction that starts a new basic block.
      *
      * @param insns List of instructions where basic blocks are searched from.
      * @return Set of AbstractInsnNode objects that represent first instruction in basic blocks.
      */
     public Set<AbstractInsnNode> findBasicBlockBeginnings(InsnList insns) {
         Set<AbstractInsnNode> basicBlocksBeginnings = new HashSet<AbstractInsnNode>();
         basicBlocksBeginnings.add(insns.getFirst());
         for (Iterator<AbstractInsnNode> iter = insns.iterator(); iter.hasNext(); ) {
             AbstractInsnNode insn = iter.next();
             int type = insn.getType();
             switch (type) {
                 case JUMP_INSN:
                     JumpInsnNode jumpInsnNode = (JumpInsnNode) insn;
                     basicBlocksBeginnings.add(jumpInsnNode.label);
                     if (jumpInsnNode.getNext() != null) basicBlocksBeginnings.add(jumpInsnNode.getNext());
                     break;
                 case METHOD_INSN:
                     basicBlocksBeginnings.add(insn.getNext());
                     break;
                 case TABLESWITCH_INSN:
                     TableSwitchInsnNode switchInsnNode = (TableSwitchInsnNode) insn;
                     for (LabelNode label : (List<LabelNode>) switchInsnNode.labels) {
                         basicBlocksBeginnings.add(label);
                     }
                     basicBlocksBeginnings.add(switchInsnNode.dflt);
                     break;
             }
         }
         return basicBlocksBeginnings;
     }
 
     /**
      * Creates new InsnList containing bytecode instructions to increment counter.
      *
      * @return Counter increment InsnList
      */
     private InsnList createCounterIncrementInsnList(int basicBlockIndex) {
         InsnList counterIncrementInsnList = new InsnList();
         counterIncrementInsnList.add(intPushInsn(basicBlockIndex));
         counterIncrementInsnList.add(new MethodInsnNode(INVOKESTATIC, "fi/lolcatz/profiledata/ProfileData",
                 "incrementCallsToBasicBlock", "(I)V"));
         return counterIncrementInsnList;
     }
 
     /**
      * Creates an instruction that can be used to push any Int value to stack.
      *
      * @param i Int to push to stack.
      * @return Instruction to push <code>i</code> to stack.
      */
     private AbstractInsnNode intPushInsn(int i) {
         if (i < 128) {
             return new IntInsnNode(BIPUSH, i);
         } else if (i < 32768) {
             return new IntInsnNode(SIPUSH, i);
         } else {
             return new LdcInsnNode(i);
         }
     }
 }
