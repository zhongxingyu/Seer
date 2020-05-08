 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.java;
 
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.csharp.MethodBuilderFromPropertyAST;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ASTParseException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BlockScopeBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ClassBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ConstructorBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.DataBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.FieldBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.InheritanceBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.LocalVariableBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.MethodBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.MethodParameterBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ModifiersBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ModifiersInterpriter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.NameBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.NameSpaceBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ThrowsBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ArrayInitializerBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionDescriptionBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElementManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.InstanceElementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.MethodCallBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.OperatorExpressionBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ParenthesesExpressionBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.SingleIdentifierBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.AssertStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.BreakStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.ContinueStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.ExpressionStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.LabelBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.LocalVariableDeclarationStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.ReturnStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.ThrowStatementBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.CatchBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.DoBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.ElseBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.FinallyBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.ForBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.IfBlockBuilder;
import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.SimpleBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.SwitchBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.SynchronizedBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.TryBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.statement.block.WhileBlockBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitorManager;
 
 
 /**
  * Javap̃f[^o^NXQK؂ɃrW^[ɓo^CrW^[ɑ΂鑀s\bh񋟂D
  * 
  * @author kou-tngt
  *
  * @param <T> ǗVisitorK₷AST̃m[ȟ^
  */
 public class JavaAstVisitorManager<T> implements AstVisitorManager<T> {
 
     /**
      * Javap̃rW^[\z
      * 
      * @param visitor
      */
     public JavaAstVisitorManager(final AstVisitor<T> visitor, final Settings settings) {
 
         this.builders.add(new BlockScopeBuilder(this.buildDataManager));
         this.builders.add(new NameSpaceBuilder(this.buildDataManager));
         this.builders.add(new JavaImportBuilder(this.buildDataManager));
         this.builders.add(new ClassBuilder(this.buildDataManager, this.modifiersInterpriter));
         this.builders.add(new InheritanceBuilder(this.buildDataManager, new JavaTypeBuilder(
                 this.buildDataManager)));
         this.builders.add(new ThrowsBuilder(this.buildDataManager, 
                 new JavaTypeBuilder(this.buildDataManager)));
         this.builders.add(new JavaAnonymousClassBuilder(this.buildDataManager));
         this.builders.add(new JavaEnumElementBuilder(this.buildDataManager));
         this.builders.add(new JavaIntefaceMarker(this.buildDataManager));
         this.builders.add(new JavaTypeParameterBuilder(this.buildDataManager));
 
         this.builders.add(new ConstructorBuilder(this.buildDataManager, this.modifiersInterpriter,
                 new ModifiersBuilder(), new JavaTypeBuilder(this.buildDataManager),
                 new NameBuilder(), new MethodParameterBuilder(this.buildDataManager,
                         new ModifiersBuilder(), new JavaTypeBuilder(this.buildDataManager),
                         new NameBuilder(), modifiersInterpriter)));
         this.builders.add(new MethodBuilder(this.buildDataManager, this.modifiersInterpriter,
                 new ModifiersBuilder(), new JavaTypeBuilder(this.buildDataManager),
                 new NameBuilder(), new MethodParameterBuilder(this.buildDataManager,
                         new ModifiersBuilder(), new JavaTypeBuilder(this.buildDataManager),
                         new NameBuilder(), this.modifiersInterpriter)));
         this.builders.add(new MethodBuilderFromPropertyAST(this.buildDataManager,
                 modifiersInterpriter, new ModifiersBuilder(), new JavaTypeBuilder(
                         this.buildDataManager), new NameBuilder()));
         //this.builders.add(new Initializerbuilder());
         //this.builders.add(new StaticInitializerBuilder());
 
         this.builders.add(new FieldBuilder(this.buildDataManager, this.expressionManager,
                 new ModifiersBuilder(), new JavaTypeBuilder(this.buildDataManager),
                 new NameBuilder(), modifiersInterpriter));
         final LocalVariableBuilder localVariableBuilder = new LocalVariableBuilder(
                 this.buildDataManager, this.expressionManager, new ModifiersBuilder(),
                 new JavaTypeBuilder(this.buildDataManager), new NameBuilder(), this.modifiersInterpriter);
         this.builders.add(localVariableBuilder);
 
         if (settings.isStatement()) {
             // for expressions
             this.addExpressionBuilder();
 
             this.addStatementBuilder(localVariableBuilder);
         }
 
         for (final DataBuilder<?> builder : this.builders) {
             visitor.addVisitListener(builder);
         }
 
         this.visitor = visitor;
     }
 
     private void addExpressionBuilder() {
         this.builders.add(new ExpressionDescriptionBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders
                 .add(new SingleIdentifierBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new JavaCompoundIdentifierBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders
                 .add(new JavaTypeElementBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new MethodCallBuilder(this.expressionManager, this.buildDataManager));
         this.builders
                 .add(new InstanceElementBuilder(this.buildDataManager, this.expressionManager));
         this.builders.add(new OperatorExpressionBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders.add(new JavaConstructorCallBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders.add(new JavaArrayInstantiationBuilder(this.expressionManager,
                 this.buildDataManager));
 
         this.builders.add(new JavaExpressionElementBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders.add(new ArrayInitializerBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new ParenthesesExpressionBuilder(this.expressionManager, this.buildDataManager));
     }
 
     private void addStatementBuilder(final LocalVariableBuilder localVariableBuilder) {
         final LocalVariableDeclarationStatementBuilder localVariableDeclarationBuilder = new LocalVariableDeclarationStatementBuilder(
                 localVariableBuilder, this.buildDataManager);
 
         this.builders.add(localVariableDeclarationBuilder);
 
         this.builders.add(new ExpressionStatementBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders
                 .add(new ReturnStatementBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new ThrowStatementBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new BreakStatementBuilder(this.expressionManager, this.buildDataManager));
         this.builders.add(new ContinueStatementBuilder(this.expressionManager,
                 this.buildDataManager));
         this.builders.add(new AssertStatementBuilder(this.expressionManager, this.buildDataManager));
 
         this.addInnerBlockBuilder(localVariableDeclarationBuilder);
 
         this.builders.add(new LabelBuilder(this.buildDataManager));
     }
 
     private void addInnerBlockBuilder(final LocalVariableDeclarationStatementBuilder variableBuilder) {
         this.builders.add(new CatchBlockBuilder(this.buildDataManager, new LocalVariableBuilder(
                 this.buildDataManager, this.expressionManager, new ModifiersBuilder(),
                 new JavaTypeBuilder(this.buildDataManager), new NameBuilder(), this.modifiersInterpriter)));
         this.builders.add(new DoBlockBuilder(this.buildDataManager, this.expressionManager,
                 variableBuilder));
         this.builders.add(new ElseBlockBuilder(this.buildDataManager));
         this.builders.add(new FinallyBlockBuilder(this.buildDataManager));
         this.builders.add(new ForBlockBuilder(this.buildDataManager, this.expressionManager,
                 variableBuilder));
         this.builders.add(new IfBlockBuilder(this.buildDataManager, this.expressionManager,
                 variableBuilder));
        this.builders.add(new SimpleBlockBuilder(this.buildDataManager));
         this.builders.add(new SwitchBlockBuilder(this.buildDataManager, this.expressionManager,
                 variableBuilder));
         this.builders.add(new SynchronizedBlockBuilder(this.buildDataManager, this.expressionManager));
         this.builders.add(new TryBlockBuilder(this.buildDataManager));
         this.builders.add(new WhileBlockBuilder(this.buildDataManager, this.expressionManager,
                 variableBuilder));
     }
 
     /* (non-Javadoc)
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.ast.java.AstVisitorManager#visitStart(antlr.collections.AST)
      */
     public void visitStart(final T node) throws ASTParseException {
         this.reset();
 
         this.visitor.startVisiting(node);
     }
 
     /**
      * rW^[̏Ԃƍ\z̃f[^ZbgD
      */
     public void reset() {
         for (final DataBuilder<?> builder : this.builders) {
             builder.reset();
         }
         this.expressionManager.reset();
         this.buildDataManager.reset();
     }
 
     /**
      * ǗrW^[
      */
     private final AstVisitor<T> visitor;
 
     /**
      * \z̃f[^̊Ǘ
      */
     private final JavaBuildManager buildDataManager = new JavaBuildManager();
 
     /**
      * ͒̎f[^̊Ǘ
      */
     private final ExpressionElementManager expressionManager = new ExpressionElementManager();
 
     private final ModifiersInterpriter modifiersInterpriter = new JavaModifiersInterpriter();
 
     /**
      * rW^[ɃZbgr_[Q̃Zbg
      */
     private final Set<DataBuilder<?>> builders = new LinkedHashSet<DataBuilder<?>>();
 }
