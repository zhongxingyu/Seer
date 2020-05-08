 package eu.stratosphere.sopremo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import eu.stratosphere.core.fs.Path;
 import eu.stratosphere.sopremo.aggregation.Aggregation;
 import eu.stratosphere.sopremo.aggregation.AssociativeAggregation;
 import eu.stratosphere.sopremo.aggregation.FixedTypeAssociativeAggregation;
 import eu.stratosphere.sopremo.cache.ArrayCache;
 import eu.stratosphere.sopremo.cache.NodeCache;
 import eu.stratosphere.sopremo.cache.PatternCache;
 import eu.stratosphere.sopremo.expressions.AggregationExpression;
 import eu.stratosphere.sopremo.expressions.ArithmeticExpression;
 import eu.stratosphere.sopremo.expressions.ArithmeticExpression.ArithmeticOperator;
 import eu.stratosphere.sopremo.expressions.ArrayProjection;
 import eu.stratosphere.sopremo.expressions.ChainedSegmentExpression;
 import eu.stratosphere.sopremo.expressions.ComparativeExpression;
 import eu.stratosphere.sopremo.expressions.ConstantExpression;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.expressions.InputSelection;
 import eu.stratosphere.sopremo.expressions.TernaryExpression;
 import eu.stratosphere.sopremo.function.ExpressionFunction;
 import eu.stratosphere.sopremo.function.SopremoFunction;
 import eu.stratosphere.sopremo.function.SopremoFunction1;
 import eu.stratosphere.sopremo.function.SopremoFunction2;
 import eu.stratosphere.sopremo.function.SopremoFunction3;
 import eu.stratosphere.sopremo.function.SopremoVarargFunction1;
 import eu.stratosphere.sopremo.operator.Name;
 import eu.stratosphere.sopremo.packages.BuiltinProvider;
 import eu.stratosphere.sopremo.tokenizer.RegexTokenizer;
 import eu.stratosphere.sopremo.type.ArrayNode;
 import eu.stratosphere.sopremo.type.BooleanNode;
 import eu.stratosphere.sopremo.type.CachingArrayNode;
 import eu.stratosphere.sopremo.type.IArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.INumericNode;
 import eu.stratosphere.sopremo.type.IStreamNode;
 import eu.stratosphere.sopremo.type.IntNode;
 import eu.stratosphere.sopremo.type.MissingNode;
 import eu.stratosphere.sopremo.type.NullNode;
 import eu.stratosphere.sopremo.type.TextNode;
 
 /**
  * Core functions.
  */
 public class CoreFunctions implements BuiltinProvider {
 	@Name(verb = "concat", noun = "concatenation")
 	public static final Aggregation CONCAT = new FixedTypeAssociativeAggregation<TextNode>(new TextNode()) {
 		@Override
 		protected void aggregateInto(final TextNode aggregator, final IJsonNode element) {
 			try {
 				element.appendAsString(aggregator);
 			} catch (final IOException e) {
 			}
 		}
 	};
 
 	@Name(verb = "array_concat")
 	public static final Aggregation ARRAY_CONCAT = new FixedTypeAssociativeAggregation<CachingArrayNode<IJsonNode>>(
 		new CachingArrayNode<IJsonNode>()) {
 		@Override
 		public void initialize() {
 			this.aggregator.setSize(0);
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void aggregateInto(final CachingArrayNode<IJsonNode> aggregator, final IJsonNode array) {
 			for (final IJsonNode element : (IArrayNode<IJsonNode>) array)
 				aggregator.addClone(element);
 		}
 	};
 
 	/**
 	 * Repeatedly applies the {@link ArithmeticOperator#ADDITION} to the
 	 * children of the given node.
 	 */
 	@Name(verb = "sum", noun = "sum")
 	public static final Aggregation SUM = new AssociativeAggregation<INumericNode>(IntNode.ZERO) {
 		private final transient NodeCache nodeCache = new NodeCache();
 
 		@Override
 		protected INumericNode aggregate(final INumericNode aggregator,
 				final IJsonNode element) {
 			return ArithmeticExpression.ArithmeticOperator.ADDITION.evaluate(
 				aggregator, (INumericNode) element, this.nodeCache);
 		}
 	};
 
 	@Name(noun = "count")
 	public static final ExpressionFunction COUNT =
 		new ExpressionFunction(1, new AggregationExpression(SUM).withInputExpression(
 			new ArrayProjection(new ConstantExpression(1)).withInputExpression(new InputSelection(0))));
 
 	@Name(noun = "first")
 	public static final Aggregation FIRST = new AssociativeAggregation<IJsonNode>(NullNode.getInstance()) {
 		@Override
 		protected IJsonNode aggregate(final IJsonNode aggregator, final IJsonNode element) {
 			return aggregator == NullNode.getInstance() ? element : aggregator;
 		}
 	};
 
 	// naive: all = fn(array) { array }
 	// combinable: all = fn(array) { array_concat(map(array, fn(x) { [x] })) }
 	@Name(noun = "all")
 	public static final ExpressionFunction ALL =
 		new ExpressionFunction(1,
 			ARRAY_CONCAT.asExpression().withInputExpression(
 				// optimized version of map(array, fn(x) { [x] })
 				new ArrayProjection(new EvaluationExpression() {
 					@Override
 					public IJsonNode evaluate(final IJsonNode node) {
 						return new ArrayNode<IJsonNode>(node.clone());
 					}
 				}).withInputExpression(new InputSelection(0)))
 		);
 
 	@Name(verb = "sort")
 	public static final ExpressionFunction SORT = new ExpressionFunction(1,
 		new ChainedSegmentExpression(ALL.getDefinition(), new EvaluationExpression() {
 			@Override
 			public void appendAsString(final Appendable appendable) throws IOException {
 				appendable.append("sort");
 			}
 
 			@Override
 			public IJsonNode evaluate(final IJsonNode node) {
 				final ArrayNode<?> arrayNode = (ArrayNode<?>) node;
 				final Object[] elements = arrayNode.getBackingArray();
 				Arrays.sort(elements, 0, arrayNode.size());
 				return node;
 			}
 		}));
 
 	@Name(noun = "mean")
 	public static final ExpressionFunction MEAN = new ExpressionFunction(1, new TernaryExpression(
 		EvaluationExpression.VALUE,
 		new ArithmeticExpression(SUM.asExpression(), ArithmeticOperator.DIVISION,
 			CoreFunctions.COUNT.inline(EvaluationExpression.VALUE)),
 		ConstantExpression.MISSING));
 
 	@Name(noun = "min")
 	public static final Aggregation MIN = new AssociativeAggregation<IJsonNode>(NullNode.getInstance()) {
 		@Override
 		public IJsonNode aggregate(final IJsonNode aggregator, final IJsonNode node) {
 			if (aggregator == NullNode.getInstance())
 				return node.clone();
 			else if (ComparativeExpression.BinaryOperator.LESS.evaluate(node, aggregator))
 				return node;
 			return aggregator;
 		}
 	};
 
 	@Name(noun = "max")
 	public static final Aggregation MAX = new AssociativeAggregation<IJsonNode>(NullNode.getInstance()) {
 		@Override
 		public IJsonNode aggregate(final IJsonNode aggregator, final IJsonNode node) {
 			if (aggregator == NullNode.getInstance())
 				return node.clone();
 			else if (ComparativeExpression.BinaryOperator.LESS.evaluate(aggregator, node))
 				aggregator.copyValueFrom(node);
 			return aggregator;
 		}
 	};
 
 	/**
 	 * Creates a new array by combining sparse array information.<br />
 	 * For example: [[0, "a"], [3, "d"], [2, "c"]] -&lt; ["a", missing, "c",
 	 * "d"]
 	 */
 	@Name(verb = "assemble")
 	public static final Aggregation ASSEMBLE_ARRAY = new FixedTypeAssociativeAggregation<ArrayNode<IJsonNode>>(
 		new ArrayNode<IJsonNode>()) {
 		@Override
 		protected void aggregateInto(final ArrayNode<IJsonNode> aggregator, final IJsonNode element) {
 			final IArrayNode<?> part = (IArrayNode<?>) element;
 			aggregator.add(((INumericNode) part.get(0)).getIntValue(), part.get(1));
 		}
 	};
 
 	@Name(verb = "add")
 	public static final SopremoFunction ADD = new SopremoFunction3<IArrayNode<IJsonNode>, IntNode, IJsonNode>() {
 		/**
 		 * Adds the specified node to the array at the given index
 		 * 
 		 * @param array
 		 *        the array that should be extended
 		 * @param index
 		 *        the position of the insert
 		 * @param node
 		 *        the node to add
 		 * @return array with the added node
 		 */
 		@Override
 		protected IJsonNode call(final IArrayNode<IJsonNode> array,
 				final IntNode index, final IJsonNode node) {
 			array.add(resolveIndex(index.getIntValue(), array.size()), node);
 			return array;
 		}
 	};
 
 	@Name(noun = "camelCase")
 	public static final SopremoFunction CAMEL_CASE = new SopremoFunction1<TextNode>() {
		private transient StringBuilder builder = new StringBuilder();
 
 		private final transient TextNode result = new TextNode();
 
 		@Override
 		protected IJsonNode call(final TextNode input) {
			builder = new StringBuilder();
 			this.builder.append(input);
 
 			boolean capitalize = true;
 			for (int index = 0, length = this.builder.length(); index < length; index++) {
 				final char ch = this.builder.charAt(index);
 				if (Character.isWhitespace(ch))
 					capitalize = true;
 				else if (capitalize) {
 					this.builder.setCharAt(index, Character.toUpperCase(ch));
 					capitalize = false;
 				} else {
 					final char lowerCh = Character.toLowerCase(ch);
 					if (lowerCh != ch)
 						this.builder.setCharAt(index, lowerCh);
 				}
 			}
 			this.result.setValue(this.builder);
 			return this.result;
 		}
 	};
 
 	@Name(verb = "extract")
 	public static final SopremoFunction EXTRACT = new SopremoFunction3<TextNode, TextNode, IJsonNode>() {
 
 		private final transient PatternCache patternCache = new PatternCache();
 
 		private final transient TextNode stringResult = new TextNode();
 
 		private final transient CachingArrayNode<IJsonNode> arrayResult = new CachingArrayNode<IJsonNode>();
 
 		@Override
 		protected IJsonNode call(final TextNode input, final TextNode pattern,
 				final IJsonNode defaultValue) {
 			final Pattern compiledPattern = this.patternCache
 				.getPatternOf(pattern);
 			final Matcher matcher = compiledPattern.matcher(input);
 
 			if (!matcher.find())
 				return defaultValue;
 
 			if (matcher.groupCount() == 0) {
 				this.stringResult.setValue(matcher.group(0));
 				return this.stringResult;
 			}
 
 			if (matcher.groupCount() == 1) {
 				this.stringResult.setValue(matcher.group(1));
 				return this.stringResult;
 			}
 
 			this.arrayResult.clear();
 			for (int index = 1; index <= matcher.groupCount(); index++) {
 				TextNode group = (TextNode) this.arrayResult.reuseUnusedNode();
 				if (group == null)
 					this.arrayResult.add(group = new TextNode());
 				group.setValue(matcher.group(index));
 			}
 			return this.arrayResult;
 		}
 	}.withDefaultParameters(NullNode.getInstance());
 
 	@Name(noun = "format", verb = "format")
 	public static final SopremoFunction FORMAT = new SopremoVarargFunction1<TextNode>() {
 		private final transient TextNode result = new TextNode();
 
 		private final transient ArrayCache<Object> arrayCache = new ArrayCache<Object>(
 			Object.class);
 
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.function.SopremoVarargFunction1#call(eu.
 		 * stratosphere.sopremo.type.IJsonNode,
 		 * eu.stratosphere.sopremo.type.IArrayNode<IJsonNode>)
 		 */
 		@Override
 		protected IJsonNode call(final TextNode format, final IArrayNode<IJsonNode> varargs) {
 			final Object[] paramsAsObjects = this.arrayCache.getArray(varargs
 				.size());
 			for (int index = 0; index < paramsAsObjects.length; index++)
 				paramsAsObjects[index] = varargs.get(index).toString();
 
 			this.result.clear();
 			this.result.asFormatter().format(format.toString(),
 				paramsAsObjects);
 			return this.result;
 		}
 	};
 
 	@Name(verb = "subtract")
 	public static final SopremoFunction SUBTRACT = new SopremoVarargFunction1<IArrayNode<IJsonNode>>() {
 
 		private final transient HashSet<IJsonNode> filterSet = new HashSet<IJsonNode>();
 
 		private final transient IArrayNode<IJsonNode> result = new ArrayNode<IJsonNode>();
 
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.function.SopremoVarargFunction1#call(eu.
 		 * stratosphere.sopremo.type.IJsonNode,
 		 * eu.stratosphere.sopremo.type.IArrayNode<IJsonNode>)
 		 */
 		@Override
 		protected IJsonNode call(final IArrayNode<IJsonNode> input,
 				final IArrayNode<IJsonNode> elementsToRemove) {
 			this.filterSet.clear();
 			for (final IJsonNode elementToFilter : elementsToRemove)
 				this.filterSet.add(elementToFilter);
 
 			this.result.clear();
 			for (int index = 0; index < input.size(); index++)
 				if (!this.filterSet.contains(input.get(index)))
 					this.result.add(input.get(index));
 			return this.result;
 		}
 	};
 
 	@Name(noun = "like")
 	public static final SopremoFunction LIKE = new SopremoFunction2<TextNode, TextNode>() {
 		private static final transient String PLACEHOLDER = "%%";
 
 		private static final transient String REGEX = ".*";
 
 		@Override
 		protected IJsonNode call(final TextNode inputNode, final TextNode patternNode) {
 			final String pattern = patternNode.toString().replaceAll(PLACEHOLDER, REGEX);
 			final String value = inputNode.toString();
 
 			return BooleanNode.valueOf(value.matches(pattern));
 		}
 	}.withDefaultParameters(TextNode.valueOf(""));
 
 	@Name(noun = "length")
 	public static final SopremoFunction LENGTH = new SopremoFunction1<TextNode>() {
 		private final transient IntNode result = new IntNode();
 
 		@Override
 		protected IJsonNode call(final TextNode node) {
 			this.result.setValue(node.length());
 			return this.result;
 		}
 	};
 
 	@Name(verb = "replace")
 	public static final SopremoFunction REPLACE = new SopremoFunction3<TextNode, TextNode, TextNode>() {
 		private final transient PatternCache patternCache = new PatternCache();
 
 		private final transient TextNode result = new TextNode();
 
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.function.SopremoFunction3#call(eu.stratosphere
 		 * .sopremo.type.IJsonNode, eu.stratosphere.sopremo.type.IJsonNode,
 		 * eu.stratosphere.sopremo.type.IJsonNode)
 		 */
 		@Override
 		protected IJsonNode call(final TextNode input, final TextNode search,
 				final TextNode replace) {
 			final Pattern compiledPattern = this.patternCache
 				.getPatternOf(search);
 			final Matcher matcher = compiledPattern.matcher(input);
 			this.result.setValue(matcher.replaceAll(replace.toString()));
 			return this.result;
 		}
 	};
 
 	private static final TextNode WHITESPACES = TextNode
 		.valueOf("\\p{javaWhitespace}+");
 
 	@Name(verb = "split")
 	public static final SopremoFunction SPLIT = new SopremoFunction2<TextNode, TextNode>() {
 		private final transient PatternCache patternCache = new PatternCache();
 
 		private final transient CachingArrayNode<TextNode> result = new CachingArrayNode<TextNode>();
 
 		private final transient Map<Pattern, RegexTokenizer> tokenizers =
 			new IdentityHashMap<Pattern, RegexTokenizer>();
 
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.function.SopremoFunction3#call(eu.stratosphere
 		 * .sopremo.type.IJsonNode, eu.stratosphere.sopremo.type.IJsonNode,
 		 * eu.stratosphere.sopremo.type.IJsonNode)
 		 */
 		@Override
 		protected IJsonNode call(final TextNode input,
 				final TextNode splitString) {
 			final Pattern searchPattern = this.patternCache
 				.getPatternOf(splitString);
 			RegexTokenizer regexTokenizer = this.tokenizers.get(searchPattern);
 			if (regexTokenizer == null)
 				this.tokenizers.put(searchPattern,
 					regexTokenizer = new RegexTokenizer(searchPattern));
 			regexTokenizer.tokenizeInto(input, this.result);
 			return this.result;
 		}
 	}.withDefaultParameters(WHITESPACES);
 
 	@Name(noun = "substring")
 	public static final SopremoFunction SUBSTRING = new SopremoFunction3<TextNode, IntNode, IntNode>() {
 		private final transient TextNode result = new TextNode();
 
 		@Override
 		protected IJsonNode call(final TextNode input, final IntNode from,
 				final IntNode to) {
 			final int length = input.length();
 			final int fromPos = resolveIndex(from.getIntValue(), length);
 			final int toPos = resolveIndex(to.getIntValue(), length);
 			this.result.setValue(input, fromPos, toPos);
 			return this.result;
 		}
 	}.withDefaultParameters(new IntNode(-1));
 
 	@Name(verb = "trim")
 	public static final SopremoFunction TRIM = new SopremoFunction1<TextNode>() {
 		private final transient TextNode result = new TextNode();
 
 		@Override
 		protected IJsonNode call(final TextNode input) {
 			int start = 0, end = input.length() - 1;
 			while (start < end && input.charAt(start) == ' ')
 				start++;
 			while (end > start && input.charAt(end) == ' ')
 				end--;
 			this.result.setValue(input, start, end + 1);
 			return this.result;
 		}
 	};
 
 	@Name(verb = "unionAll")
 	public static final SopremoFunction UNION_ALL = new SopremoVarargFunction1<IStreamNode<?>>() {
 		private final transient IArrayNode<IJsonNode> union = new ArrayNode<IJsonNode>();
 
 		@Override
 		protected IJsonNode call(final IStreamNode<?> firstArray, final IArrayNode<IJsonNode> moreArrays) {
 			this.union.clear();
 			this.union.addAll(firstArray);
 			for (final IJsonNode param : moreArrays)
 				for (final IJsonNode child : (IStreamNode<?>) param)
 					this.union.add(child);
 			return this.union;
 		}
 	};
 
 	@Name(noun = { "indexOf", "strpos" })
 	public static final SopremoFunction STRPOS = new SopremoFunction2<TextNode, TextNode>() {
 		private final transient IntNode result = new IntNode();
 
 		@Override
 		protected IJsonNode call(final TextNode input, final TextNode needle) {
 			this.result.setValue(input.indexOf(needle));
 			return this.result;
 		}
 	};
 
 	public static int resolveIndex(final int index, final int size) {
 		if (index < 0)
 			return size + index;
 		return index;
 	}
 
 	@Name(verb = "setWorkingDirectory")
 	public static MissingNode setWorkingDirectory(final TextNode node) {
 		String path = node.toString();
 		if (!path.startsWith("hdfs://"))
 			path = new File(path).toURI().toString();
 		SopremoEnvironment.getInstance().getEvaluationContext().setWorkingPath(new Path(path));
 		return MissingNode.getInstance();
 	}
 }
