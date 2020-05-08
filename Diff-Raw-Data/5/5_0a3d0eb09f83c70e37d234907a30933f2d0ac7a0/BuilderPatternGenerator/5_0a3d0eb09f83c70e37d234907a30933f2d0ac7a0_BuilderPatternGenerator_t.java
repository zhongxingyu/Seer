 package se.byggarmonster.lib.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.v4.runtime.ParserRuleContext;
 
 import se.byggarmonster.lib.impl.data.MethodMapping;
 import se.byggarmonster.lib.parser.JavaBaseListener;
 import se.byggarmonster.lib.parser.JavaParser.BlockStatementContext;
 import se.byggarmonster.lib.parser.JavaParser.ConstructorBodyContext;
 import se.byggarmonster.lib.parser.JavaParser.ConstructorDeclaratorRestContext;
 import se.byggarmonster.lib.parser.JavaParser.ExpressionContext;
 import se.byggarmonster.lib.parser.JavaParser.FieldDeclarationContext;
 import se.byggarmonster.lib.parser.JavaParser.FormalParameterDeclsContext;
 import se.byggarmonster.lib.parser.JavaParser.MemberDeclContext;
 import se.byggarmonster.lib.parser.JavaParser.MemberDeclarationContext;
 import se.byggarmonster.lib.parser.JavaParser.MethodBodyContext;
 import se.byggarmonster.lib.parser.JavaParser.NormalClassDeclarationContext;
 import se.byggarmonster.lib.parser.JavaParser.QualifiedNameContext;
 
 import com.google.common.base.Optional;
 
 public class BuilderPatternGenerator extends JavaBaseListener {
 	private final ClassDataBuilder classDataBuilder = new ClassDataBuilder();
 
 	@Override
 	public void exitConstructorBody(final ConstructorBodyContext ctx) {
 		classDataBuilder
 		        .withConstructorMemberMappings(findMemberMappingsInBlocks(ctx
 		                .blockStatement()));
 	}
 
 	/**
 	 * Constructor arguments
 	 */
 	@Override
 	public void exitFormalParameterDeclsRest(
 	        final se.byggarmonster.lib.parser.JavaParser.FormalParameterDeclsRestContext ctx) {
 		if (hasParent(ctx, ConstructorDeclaratorRestContext.class))
 			classDataBuilder.withConstructorParameter(new NameTypePair(ctx
 			        .variableDeclaratorId().getText(),
 			        ((FormalParameterDeclsContext) ctx.getParent()).type()
 			                .getText()));
 	}
 
 	@Override
 	public void exitMemberDeclaration(final MemberDeclarationContext mdc) {
 		if (mdc.getChild(1) instanceof FieldDeclarationContext) {
 			final FieldDeclarationContext fieldDeclarationContext = (FieldDeclarationContext) mdc
 			        .getChild(1);
 			classDataBuilder.withMember(new NameTypePair(
 			        fieldDeclarationContext.variableDeclarators().getText(),
 			        mdc.type().getText()));
 		}
 	}
 
 	@Override
 	public void exitMethodBody(final MethodBodyContext ctx) {
 		final Map<String, String> foundMappings = findMemberMappingsInBlocks(ctx
 		        .block().blockStatement());
 		if (foundMappings.size() == 1) {
 			final MemberDeclContext memberDeclContext = (MemberDeclContext) ctx
 			        .getParent().getParent();
 			classDataBuilder.withSetterMapping(memberDeclContext.Identifier()
 			        .getText(),
 			        getMember(foundMappings.values().iterator().next())
 			                .getName());
 		}
 
 		final Optional<String> returnMember = findReturnInBlocks(ctx.block()
 		        .blockStatement());
 		if (returnMember.isPresent()) {
 			classDataBuilder.withGetterMapping(ctx.getParent().getParent()
 			        .getChild(0).getText(), returnMember.get());
 		}
 	}
 
 	@Override
 	public void exitNormalClassDeclaration(
 	        final NormalClassDeclarationContext ctx) {
 		classDataBuilder.withClassName(ctx.Identifier().getText());
 	}
 
 	/**
 	 * package name
 	 */
 	@Override
 	public void exitQualifiedName(final QualifiedNameContext ctx) {
 		classDataBuilder.withPackageName(ctx.getText());
 	}
 
 	private Map<String, String> findMemberMappingsInBlocks(
 	        final List<BlockStatementContext> blocks) {
 		final Map<String, String> foundMappings = new HashMap<String, String>();
 		for (final BlockStatementContext bsc : blocks) {
 			if (bsc.statement().statementExpression() == null)
 				continue;
 			final ExpressionContext exprContext = bsc.statement()
 			        .statementExpression().expression();
 			final String memberName = removeThis(exprContext.getChild(0)
 			        .getText());
 			final String constructorName = exprContext.getChild(2).getText();
 			foundMappings.put(constructorName, memberName);
 		}
 		return foundMappings;
 	}
 
 	private Optional<String> findReturnInBlocks(
 	        final List<BlockStatementContext> blocks) {
 		for (final BlockStatementContext bsc : blocks) {
 			if (bsc.statement().expression().size() > 0)
 				return Optional.of(bsc.statement().expression().get(0)
 				        .getChild(0).getText());
 		}
 		return Optional.absent();
 	}
 
 	private NameTypePair getMember(final String name) {
 		for (final NameTypePair p : classDataBuilder.build().getMembers())
 			if (p.getName().equals(name))
 				return p;
 		throw new RuntimeException(name + " not found");
 	}
 
 	private boolean hasParent(final ParserRuleContext ctx, final Class<?> clazz) {
 		if (ctx.getParent() == null)
 			return false;
 		if (ctx.getParent().getClass().equals(clazz))
 			return true;
 		return hasParent(ctx.getParent(), clazz);
 	}
 
 	private List<NameTypePair> mapToMembers(final List<NameTypePair> pairs) {
 		final List<NameTypePair> mapped = new ArrayList<NameTypePair>();
 		for (final NameTypePair constructorParameter : pairs) {
 			new HashMap<String, Object>();
 			mapped.add(new NameTypePair(checkNotNull(
 			        classDataBuilder.build().getConstructorMemberMapping()
 			                .getAttribute(constructorParameter.getName()),
 			        constructorParameter.getName() + " has no memberMapping"),
 			        checkNotNull(constructorParameter.getType(),
 			                constructorParameter.getName() + " has null type")));
 		}
 		return mapped;
 	}
 
 	private String removeThis(final String text) {
 		if (!text.startsWith("this."))
 			return text;
 		return text.substring("this.".length());
 	}
 
 	public String render(final String templatePath) {
 		final ClassData classData = classDataBuilder.build();
 		final Map<String, Object> context = new HashMap<String, Object>();
		context.put(
		        "packageName",
		        checkNotNull(classData.getPackageName(),
		                "Package name was not parsed."));
 		context.put("className", checkNotNull(classData.getClassName()));
 		context.put("members", toListOfNameTypeMap(classData.getMembers()));
 		context.put("constructorParameters",
 		        toListOfNameTypeMap(mapToMembers(classData
 		                .getConstructorParameters())));
 		context.put("setters", toList(classData.getSetterMapping()));
 		context.put("getters", toList(classData.getGetterMapping()));
 		return TemplateHelper.render(templatePath, context);
 	}
 
 	private List<Map<String, Object>> toList(final MethodMapping map) {
 		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 		for (final String key : map.getMethods()) {
 			final Map<String, Object> variables = new HashMap<String, Object>();
 			variables.put("key", key);
 			variables.put("value", map.getAttribute(key));
 			list.add(variables);
 		}
 		return list;
 	}
 
 	private List<Map<String, Object>> toListOfNameTypeMap(
 	        final List<NameTypePair> list) {
 		final ArrayList<Map<String, Object>> constructorParameters = new ArrayList<Map<String, Object>>();
 		for (final NameTypePair constructorParameter : list) {
 			final Map<String, Object> map = new HashMap<String, Object>();
 			map.put("name",
 			        checkNotNull(constructorParameter.getName(),
 			                constructorParameter.getName() + " is null"));
 			map.put("type",
 			        checkNotNull(constructorParameter.getType(),
 			                constructorParameter.getName() + " has null type"));
 			constructorParameters.add(map);
 		}
 		return constructorParameters;
 	}
 
 }
