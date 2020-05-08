 package org.zwobble.shed.compiler.parsing.nodes;
 
 import java.util.List;
 
 import lombok.Data;
 
 import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;
 
 import static java.util.Arrays.asList;
 import static org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes.subScope;
 
 @Data
 public class ObjectDeclarationNode implements TypeDeclarationNode {
     private final String identifier;
     private final List<ExpressionNode> superTypes;
     private final BlockNode statements;
     
     @Override
     public SyntaxNodeStructure describeStructure() {
        return SyntaxNodeStructure.build(subScope(asList(statements)));
     }
 }
