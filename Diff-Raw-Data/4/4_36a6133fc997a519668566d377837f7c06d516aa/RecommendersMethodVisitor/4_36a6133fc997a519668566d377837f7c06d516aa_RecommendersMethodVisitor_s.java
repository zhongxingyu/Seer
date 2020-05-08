 package ru.spbau.jps.incremental.recommenders;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.jetbrains.asm4.Label;
 import org.jetbrains.asm4.MethodVisitor;
 import org.jetbrains.asm4.Opcodes;
 import org.jetbrains.asm4.commons.AnalyzerAdapter;
 import org.jetbrains.asm4.signature.SignatureReader;
 
 import java.util.*;
 
 /**
  * @author Osipov Stanislav
  */
 public final class RecommendersMethodVisitor extends AnalyzerAdapter {
 
     @NotNull
     private Map<String, Map<List<String>, Integer>> sequences;
     @NotNull
     private Map<Integer, Integer> localVariablesMap = new HashMap<Integer, Integer>();
     @Nullable
     private Map<Integer, List<List<String>>> methodCallSequence = new HashMap<Integer, List<List<String>>>();
     @NotNull
     private Map<Integer, String> localVariableTypes = new HashMap<Integer, String>();
     @NotNull
     private Map<Label, Map<Integer, List<List<String>>>> cachedSequences = new HashMap<Label, Map<Integer, List<List<String>>>>();
     @NotNull
     private Set<Label> visitedLabels = new HashSet<Label>();
 
     public RecommendersMethodVisitor(String owner, int access, String name, String desc, @NotNull Map<String, Map<List<String>, Integer>> sequences) {
         super(Opcodes.ASM4, owner, access, name, desc, new MethodVisitor(Opcodes.ASM4) {
         });
         this.sequences = sequences;
     }
 
     @Override
     public void visitMethodInsn(int opcode, @NotNull String owner, @NotNull String name, @NotNull String sign) {
         List<String> signature = parseSignature(sign);
         int varIndex = stack.size() - signature.size() + 1;
         String representation = name + toStringRepresentation(signature);
         saveResult(varIndex, representation, owner);
         super.visitMethodInsn(opcode, owner, name, sign);
     }
 
     private void saveResult(int varIndex, @NotNull String representation, @NotNull String owner) {
         if (methodCallSequence == null) {
             return;
         }
         Integer localVarNumber = localVariablesMap.remove(varIndex);
         if (localVarNumber != null) {
             if (!owner.equals(localVariableTypes.get(localVarNumber))) {
                 return;
             }
             List<List<String>> active = methodCallSequence.get(localVarNumber);
             if (active == null) {
                 active = new ArrayList<List<String>>();
                 active.add(new ArrayList<String>());
             }
             for (List<String> sequence : active) {
                 sequence.add(representation);
             }
             methodCallSequence.put(localVarNumber, active);
         }
     }
 
 
     @NotNull
     private List<String> parseSignature(@NotNull String desc) {
         SignatureReader reader = new SignatureReader(desc);
         List<String> signature = new ArrayList<String>();
         reader.accept(new RecommendersSignatureVisitor(signature));
         return signature;
     }
 
     @NotNull
     private String toStringRepresentation(@NotNull List<String> signature) {
         StringBuilder signatureBuilder = new StringBuilder();
         signatureBuilder.append('(');
         for (String s : signature.subList(0, signature.size() - 1)) {
             signatureBuilder.append(s);
             signatureBuilder.append(',');
         }
         if (signatureBuilder.length() != 1) {
             signatureBuilder.deleteCharAt(signatureBuilder.length() - 1);
         }
         signatureBuilder.append(')');
         signatureBuilder.append(signature.get(signature.size() - 1));
         return signatureBuilder.toString();
     }
 
     @Override
     public void visitVarInsn(int i, int i2) {
         int prevSize = stack.size();
         super.visitVarInsn(i, i2);
         if (stack.size() > prevSize) {
             localVariablesMap.put(stack.size(), i2);
             Object stackPeek = stack.get(stack.size() - 1);
             String varType = (stackPeek instanceof String) ? (String) stackPeek : stackPeek.getClass().getName();
             localVariableTypes.put(i2, varType);
         }
     }
 
     @Override
     public void visitJumpInsn(int opcode, Label label) {
         super.visitJumpInsn(opcode, label);
         if (visitedLabels.add(label)) {
             cachedSequences.put(label, getMethodSequencesCopy());
             if (opcode == Opcodes.GOTO) {
                 methodCallSequence = null;
             }
         }
     }
 
     @Nullable
     private Map<Integer, List<List<String>>> getMethodSequencesCopy() {
         if (methodCallSequence == null) {
             return null;
         }
         Map<Integer, List<List<String>>> copy = new HashMap<Integer, List<List<String>>>();
         for (Map.Entry<Integer, List<List<String>>> sequenceEntry : methodCallSequence.entrySet()) {
             List<List<String>> sequences = new ArrayList<List<String>>();
             for (List<String> sequence : sequenceEntry.getValue()) {
                 sequences.add(new ArrayList<String>(sequence));
             }
             copy.put(sequenceEntry.getKey(), sequences);
         }
         return copy;
     }
 
     @Override
     public void visitLabel(Label label) {
         super.visitLabel(label);
         visitedLabels.add(label);
         if (cachedSequences.containsKey(label)) {
             Map<Integer, List<List<String>>> beforeJumpSequences = cachedSequences.remove(label);
             mergeSequences(beforeJumpSequences);
         }
     }
 
     private void mergeSequences(@Nullable Map<Integer, List<List<String>>> beforeJumpSequences) {
         if (methodCallSequence == null) {
             methodCallSequence = beforeJumpSequences;
             return;
         }
         if (beforeJumpSequences == null) {
             return;
         }
         for (Map.Entry<Integer, List<List<String>>> sequenceEntry : beforeJumpSequences.entrySet()) {
             List<List<String>> sequences = methodCallSequence.get(sequenceEntry.getKey());
             if (sequences == null) {
                 sequences = new ArrayList<List<String>>();
             }
             sequences.addAll(sequenceEntry.getValue());
             methodCallSequence.put(sequenceEntry.getKey(), sequences);
         }
     }
 
     @Override
     public void visitEnd() {
         super.visitEnd();
         if (methodCallSequence == null) {
             return;
         }
         for (Map.Entry<Integer, List<List<String>>> sequenceEntry : methodCallSequence.entrySet()) {
             Integer varNumber = sequenceEntry.getKey();
             List<List<String>> sequencesList = sequenceEntry.getValue();
             String type = localVariableTypes.get(varNumber);
             if (type == null) {
                 continue;
             }
             for (List<String> sequence : sequencesList) {
                 updateSequences(type, sequence);
             }
 
         }
     }
 
     private void updateSequences(@NotNull String type, @NotNull List<String> sequence) {
         if (sequence.size() < 1) {
             return;
         }
         Map<List<String>, Integer> currentSequences = sequences.get(type);
         if (currentSequences == null) {
             currentSequences = new HashMap<List<String>, Integer>();
         }
         Integer sequenceCounter = currentSequences.get(sequence);
         if (sequenceCounter == null) {
             sequenceCounter = 0;
         }
         currentSequences.put(sequence, ++sequenceCounter);
         sequences.put(type, currentSequences);
     }
 
 }
