 package eu.stratosphere.sopremo.operator;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javolution.text.TypeFormat;
 
 import com.esotericsoftware.kryo.DefaultSerializer;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.serializers.FieldSerializer;
 
 import eu.stratosphere.pact.common.plan.PactModule;
 import eu.stratosphere.sopremo.AbstractSopremoType;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.ISopremoType;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
 import eu.stratosphere.util.CollectionUtil;
 import eu.stratosphere.util.reflect.ReflectUtil;
 
 /**
  * Base class for all Sopremo operators. Every operator consumes and produces a specific number of {@link JsonStream}s.
  * The operator groups input json objects accordingly to its semantics and transforms the partitioned objects to one or
  * more outputs.<br>
  * Each Sopremo operator may be converted to a {@link PactModule} with the {@link #asPactModule(EvaluationContext)}
  * method.<br>
  * Implementations of an operator should either extend {@link ElementaryOperator} or {@link CompositeOperator}.
  * 
  * @author Arvid Heise
  */
 //@DefaultSerializer(Operator.OperatorSerializer.class)
 public abstract class Operator<Self extends Operator<Self>> extends ConfigurableSopremoType implements
 		ISopremoType, JsonStream, Cloneable {
 
 	public final static List<? extends EvaluationExpression> ALL_KEYS =
 		Collections.singletonList(EvaluationExpression.VALUE);
 
 	public final static int STANDARD_DEGREE_OF_PARALLELISM = -1;
 
 	private List<JsonStream> inputs = new ArrayList<JsonStream>();
 
 	private String name;
 
 	private transient List<JsonStream> outputs = new ArrayList<JsonStream>();
 
 	private int minInputs, maxInputs, minOutputs, maxOutputs;
 
 	private int degreeOfParallelism = STANDARD_DEGREE_OF_PARALLELISM;
 
 	private final boolean fixedDegreeOfParallelism;
 
 	/**
 	 * Initializes the Operator with the annotations.
 	 */
 	public Operator() {
 		final InputCardinality inputs = ReflectUtil.getAnnotation(this.getClass(), InputCardinality.class);
 		if (inputs == null)
 			throw new IllegalStateException("No InputCardinality annotation found @ " + this.getClass());
 		final OutputCardinality outputs = ReflectUtil.getAnnotation(this.getClass(), OutputCardinality.class);
 		if (outputs == null)
 			throw new IllegalStateException("No OutputCardinality annotation found @ " + this.getClass());
 		this.setNumberOfInputs(inputs.value() != -1 ? inputs.value() : inputs.min(),
 			inputs.value() != -1 ? inputs.value() : inputs.max());
 		this.setNumberOfOutputs(outputs.value() != -1 ? outputs.value() : outputs.min(),
 			outputs.value() != -1 ? outputs.value() : outputs.max());
 
 		final DegreeOfParallelism degreeOfParallelism =
 			ReflectUtil.getAnnotation(this.getClass(), DegreeOfParallelism.class);
 		if (degreeOfParallelism == null)
 			this.fixedDegreeOfParallelism = false;
 		else {
 			this.fixedDegreeOfParallelism = true;
 			this.degreeOfParallelism = degreeOfParallelism.value();
 		}
 	}
 
 	/**
 	 * Initializes the Operator with the given number of inputs and outputs.
 	 * 
 	 * @param minInputs
 	 *        the minimum number of inputs
 	 * @param maxInputs
 	 *        the maximum number of inputs
 	 * @param minOutputs
 	 *        the minimum number of outputs
 	 * @param maxOutputs
 	 *        the maximum number of outputs
 	 */
 	public Operator(final int minInputs, final int maxInputs, final int minOutputs, final int maxOutputs) {
 		this.setNumberOfInputs(minInputs, maxInputs);
 		this.setNumberOfOutputs(minOutputs, maxOutputs);
 
 		final DegreeOfParallelism degreeOfParallelism =
 			ReflectUtil.getAnnotation(this.getClass(), DegreeOfParallelism.class);
 		if (degreeOfParallelism == null)
 			this.fixedDegreeOfParallelism = false;
 		else {
 			this.fixedDegreeOfParallelism = true;
 			this.degreeOfParallelism = degreeOfParallelism.value();
 		}
 	}
 
 	public abstract ElementarySopremoModule asElementaryOperators(EvaluationContext context);
 
 	/**
 	 * Converts this operator to a {@link PactModule} using the provided {@link EvaluationContext}.
 	 * 
 	 * @param context
 	 *        the context in which the evaluation should be conducted
 	 * @return the {@link PactModule} representing this operator
 	 */
 	public abstract PactModule asPactModule(EvaluationContext context, SopremoRecordLayout layout);
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Operator<Self> clone() {
 		return (Operator<Self>) super.clone();
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (this.getClass() != obj.getClass())
 			return false;
 		final Operator<?> other = (Operator<?>) obj;
 		return this.degreeOfParallelism == other.degreeOfParallelism;
 	}
 
 	/**
 	 * Returns the output of an operator producing the {@link JsonStream} that is the input to this operator at the
 	 * given position.
 	 * 
 	 * @param index
 	 *        the index of the output
 	 * @return the output that produces the input of this operator at the given position
 	 */
 	public JsonStream getInput(final int index) {
 		return this.inputs.get(index);
 	}
 
 	/**
 	 * Returns a list of operators producing the {@link JsonStream}s that are the inputs to this operator.<br>
 	 * If multiple outputs of an operator are used as inputs for this operator, the operator appears several times.
 	 * 
 	 * @return a list of operators that produce the input of this operator
 	 */
 	public List<Operator<?>> getInputOperators() {
 		return new AbstractList<Operator<?>>() {
 
 			@Override
 			public Operator<?> get(final int index) {
 				return Operator.this.inputs.get(index) == null ? null
 					: Operator.this.inputs.get(index).getSource().getOperator();
 			}
 
 			@Override
 			public int indexOf(final Object o) {
 				final ListIterator<JsonStream> e = Operator.this.inputs.listIterator();
 				while (e.hasNext())
 					if (o == e.next())
 						return e.previousIndex();
 				return -1;
 			}
 
 			@Override
 			public int size() {
 				return Operator.this.inputs.size();
 			}
 		};
 	}
 
 	/**
 	 * Returns a list of outputs of operators producing the {@link JsonStream}s that are the inputs to this operator.<br>
 	 * If an output is used multiple times as inputs for this operator, the output appears several times (for example in
 	 * a self-join).
 	 * 
 	 * @return a list of outputs that produce the input of this operator
 	 */
 	public List<JsonStream> getInputs() {
 		return new ArrayList<JsonStream>(this.inputs);
 	}
 
 	/**
 	 * Returns the number of inputs.
 	 * 
 	 * @return the number of inputs
 	 */
 	public int getNumInputs() {
 		int numInputs = this.getMinInputs();
 		for (int index = numInputs; index < this.getMaxInputs() && index < this.inputs.size(); index++)
 			if (this.inputs.get(index) != null)
 				numInputs++;
 		return numInputs;
 	}
 
 	/**
 	 * Returns the number of outputs.
 	 * 
 	 * @return the number of outputs
 	 */
 	public int getNumOutputs() {
 		int numOutputs = this.getMinOutputs();
 		for (int index = numOutputs; index < this.getMaxOutputs() && index < this.outputs.size(); index++)
 			if (this.outputs.get(index) != null)
 				numOutputs++;
 		return numOutputs;
 	}
 
 	/**
 	 * Returns the maxInputs.
 	 * 
 	 * @return the maxInputs
 	 */
 	public int getMaxInputs() {
 		return this.maxInputs;
 	}
 
 	/**
 	 * Returns the minInputs.
 	 * 
 	 * @return the minInputs
 	 */
 	public int getMinInputs() {
 		return this.minInputs;
 	}
 
 	/**
 	 * Returns the minOutputs.
 	 * 
 	 * @return the minOutputs
 	 */
 	public int getMinOutputs() {
 		return this.minOutputs;
 	}
 
 	/**
 	 * Returns the maxOutputs.
 	 * 
 	 * @return the maxOutputs
 	 */
 	public int getMaxOutputs() {
 		return this.maxOutputs;
 	}
 
 	/**
 	 * The name of this operator, which is the class name by default.
 	 * 
 	 * @return the name of this operator.
 	 * @see #setName(String)
 	 */
 	public String getName() {
 		if (this.name == null)
 			return this.getDefaultName();
 		return this.name;
 	}
 
 	protected String getDefaultName() {
 		return this.getClass().getSimpleName();
 	}
 
 	/**
 	 * Returns the output at the specified index.
 	 * 
 	 * @param index
 	 *        the index to lookup
 	 * @return the output at the given position
 	 */
 	public JsonStream getOutput(final int index) {
 		this.checkSize(index, this.maxOutputs, this.outputs);
 		JsonStream output = this.outputs.get(index);
 		if (output == null)
 			this.outputs.set(index, output = new Output(this, index));
 		return output;
 	}
 
 	/**
 	 * Returns all outputs of this operator.
 	 * 
 	 * @return all outputs of this operator
 	 */
 	public List<JsonStream> getOutputs() {
		final ArrayList<JsonStream> outputs = new ArrayList<JsonStream>(this.maxOutputs);
		for (int index = 0; index < this.maxOutputs; index++)
 			outputs.add(this.getOutput(index));
 		return outputs;
 	}
 
 	/**
 	 * Returns the first output of this operator.
 	 */
 	@Override
 	public Output getSource() {
 		return (Output) this.getOutput(0);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + this.degreeOfParallelism;
 		return result;
 	}
 
 	/**
 	 * Replaces the input at the given location with the given {@link JsonStream}s.
 	 * 
 	 * @param index
 	 *        the index of the input
 	 * @param input
 	 *        the new input
 	 */
 	public void setInput(final int index, final JsonStream input) {
 		this.checkSize(index, this.maxInputs, this.inputs);
 
 		this.checkInput(input);
 		this.inputs.set(index, input == null ? null : input.getSource());
 	}
 
 	/**
 	 * Replaces the current list of inputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param inputs
 	 *        the new inputs
 	 */
 	public void setInputs(final JsonStream... inputs) {
 		this.setInputs(Arrays.asList(inputs));
 	}
 
 	/**
 	 * Replaces the current list of inputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param inputs
 	 *        the new inputs
 	 */
 	public void setInputs(final List<? extends JsonStream> inputs) {
 		if (inputs == null)
 			throw new NullPointerException("inputs must not be null");
 		if (this.minInputs > inputs.size() || inputs.size() > this.maxInputs)
 			throw new IndexOutOfBoundsException();
 
 		this.inputs.clear();
 		for (final JsonStream input : inputs) {
 			this.checkInput(input);
 			this.inputs.add(input == null ? null : input.getSource());
 		}
 	}
 
 	/**
 	 * Sets the name of this operator.
 	 * 
 	 * @param name
 	 *        the new name of this operator
 	 */
 	public void setName(final String name) {
 		if (name == null)
 			throw new NullPointerException("name must not be null");
 
 		this.name = name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.SopremoType#toString(java.lang.StringBuilder)
 	 */
 	@Override
 	public void appendAsString(final Appendable appendable) throws IOException {
 		appendable.append(this.getName());
 	}
 
 	public void validate() throws IllegalStateException {
 		for (int index = 0; index < this.inputs.size(); index++)
 			if (this.inputs.get(index) == null)
 				throw new IllegalStateException("unconnected input " + index);
 	}
 
 	/**
 	 * Replaces the current list of inputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param inputs
 	 *        the new inputs
 	 * @return this
 	 */
 	public Self withInputs(final JsonStream... inputs) {
 		this.setInputs(inputs);
 		return this.self();
 	}
 
 	/**
 	 * Replaces the current list of inputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param inputs
 	 *        the new inputs
 	 * @return this
 	 */
 	public Self withInputs(final List<? extends JsonStream> inputs) {
 		this.setInputs(inputs);
 		return this.self();
 	}
 
 	/**
 	 * Sets the name of this operator.
 	 * 
 	 * @param name
 	 *        the new name of this operator
 	 */
 	public Self withName(final String name) {
 		this.setName(name);
 		return this.self();
 	}
 
 	protected void checkInput(final JsonStream input) {
 		// current constraint, may be removed later
 		if (input != null && input.getSource().getOperator() == this)
 			throw new IllegalArgumentException("Cyclic reference");
 	}
 
 	protected void checkOutput(final JsonStream input) {
 		// current constraint, may be removed later
 		if (input != null && input.getSource().getOperator() == this)
 			throw new IllegalArgumentException("Cyclic reference");
 	}
 
 	protected int getSafeInputIndex(final JsonStream input) {
 		final int index = this.inputs.indexOf(input);
 		if (index == -1)
 			throw new IllegalStateException("unknown input " + input);
 		return index;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected final Self self() {
 		return (Self) this;
 	}
 
 	protected void setNumberOfInputs(final int num) {
 		this.setNumberOfInputs(num, num);
 	}
 
 	protected void setNumberOfInputs(final int min, final int max) {
 		if (min > max)
 			throw new IllegalArgumentException();
 		if (min < 0 || max < 0)
 			throw new IllegalArgumentException();
 		this.minInputs = min;
 		this.maxInputs = max;
 		CollectionUtil.ensureSize(this.inputs, this.minInputs);
 	}
 
 	/**
 	 * Sets the number of outputs of this operator retaining all old outputs if possible (increased number of outputs).
 	 * 
 	 * @param numberOfOutputs
 	 *        the number of outputs
 	 */
 	protected final void setNumberOfOutputs(final int numberOfOutputs) {
 		if (numberOfOutputs < this.outputs.size())
 			this.outputs.subList(numberOfOutputs, this.outputs.size()).clear();
 		else
 			for (int index = this.outputs.size(); index < numberOfOutputs; index++)
 				this.outputs.add(new Output(this, index));
 	}
 
 	protected void setNumberOfOutputs(final int min, final int max) {
 		if (min > max)
 			throw new IllegalArgumentException();
 		if (min < 0 || max < 0)
 			throw new IllegalArgumentException();
 		this.minOutputs = min;
 		this.maxOutputs = max;
 		CollectionUtil.ensureSize(this.outputs, this.minOutputs);
 	}
 
 	/**
 	 * Replaces the output at the given location with the given {@link JsonStream}s.
 	 * 
 	 * @param index
 	 *        the index of the output
 	 * @param output
 	 *        the new output
 	 */
 	protected void setOutput(final int index, final JsonStream output) {
 		this.checkSize(index, this.maxOutputs, this.outputs);
 
 		this.checkOutput(output);
 		this.outputs.set(index, output == null ? null : output.getSource());
 	}
 
 	/**
 	 * Replaces the current list of outputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param outputs
 	 *        the new outputs
 	 */
 	protected void setOutputs(final JsonStream... outputs) {
 		this.setOutputs(Arrays.asList(outputs));
 	}
 
 	/**
 	 * Replaces the current list of outputs with the given list of {@link JsonStream}s.
 	 * 
 	 * @param outputs
 	 *        the new outputs
 	 */
 	protected void setOutputs(final List<? extends JsonStream> outputs) {
 		if (outputs == null)
 			throw new NullPointerException("outputs must not be null");
 		if (this.minOutputs > outputs.size() || outputs.size() > this.maxOutputs)
 			throw new IndexOutOfBoundsException();
 
 		this.outputs.clear();
 		for (final JsonStream output : outputs) {
 			this.checkOutput(output);
 			this.outputs.add(output == null ? null : output.getSource());
 		}
 	}
 
 	private void checkSize(final int index, final int max, final List<?> list) {
 		if (index >= max)
 			throw new IndexOutOfBoundsException(String.format("index %s >= max %s", index, max));
 		CollectionUtil.ensureSize(list, index + 1);
 	}
 
 	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
 		ois.defaultReadObject();
 		this.outputs = new ArrayList<JsonStream>();
 		CollectionUtil.ensureSize(this.outputs, this.minOutputs);
 	}
 
 	@Property
 	@Name(adjective = "parallel")
 	public void setDegreeOfParallelism(int degree) {
 		if (this.degreeOfParallelism == degree)
 			return;
 		if (degree < 1)
 			throw new RuntimeException("Degree of Parallelism cannot be set below 1");
 		if (this.fixedDegreeOfParallelism)
 			throw new RuntimeException("This operator has a fixed degree of parallelism of " + this.degreeOfParallelism);
 		this.degreeOfParallelism = degree;
 	}
 
 	/**
 	 * Returns the degreeOfParallelism.
 	 * 
 	 * @return the degreeOfParallelism
 	 */
 	public int getDegreeOfParallelism() {
 		return this.degreeOfParallelism;
 	}
 
 	/**
 	 * Represents one output of this {@link Operator}. The output should be connected to another Operator to create a
 	 * directed acyclic graph of Operators.
 	 * 
 	 * @author Arvid Heise
 	 */
//	@DefaultSerializer(OperatorOutputSerializer.class)
 	public static class Output extends AbstractSopremoType implements JsonStream {
 		private final int index;
 
 		private final Operator<?> operator;
 
 		private Output(Operator<?> operator, final int index) {
 			this.operator = operator;
 			this.index = index;
 		}
 
 		/**
 		 * Initializes Operator.Output.
 		 */
 		Output() {
 			this.operator = null;
 			this.index = 0;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (this.getClass() != obj.getClass())
 				return false;
 			final Operator.Output other = (Operator.Output) obj;
 			return this.index == other.index && this.getOperator() == other.getOperator();
 		}
 
 		/**
 		 * Returns the index of this output in the list of outputs of the associated operator.
 		 * 
 		 * @return the index of this output
 		 */
 		public int getIndex() {
 			return this.index;
 		}
 
 		/**
 		 * Returns the associated operator.
 		 * 
 		 * @return the associated operator
 		 */
 		public Operator<?> getOperator() {
 			return this.operator;
 		}
 
 		@Override
 		public Output getSource() {
 			return this;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + this.index;
 			result = prime * result + this.getOperator().hashCode();
 			return result;
 		}
 
 		@Override
 		public void appendAsString(final Appendable appendable) throws IOException {
 			appendable.append(this.getOperator().toString()).append('@');
 			TypeFormat.format(this.index, appendable);
 		}
 	}
 
 	public static class OperatorSerializer extends com.esotericsoftware.kryo.Serializer<Operator<?>> {
 		private final FieldSerializer<Operator<?>> fieldSerializer;
 
 		private final static ThreadLocal<OperatorSerializationPool> OperatorSerializationStack =
 			new ThreadLocal<OperatorSerializationPool>() {
 				@Override
 				protected OperatorSerializationPool initialValue() {
 					return new OperatorSerializationPool();
 				};
 			};
 
 		private static class OperatorSerializationPool {
 			private final Map<Operator<?>, Integer> operatorSerializedId = new IdentityHashMap<Operator<?>, Integer>();
 
 			private final List<Operator<?>> operatorDeserializedId = new ArrayList<Operator<?>>();
 
 			// private final Map<Operator<?>, Operator<?>> copies = new IdentityHashMap<Operator<?>, Operator<?>>();
 			//
 			private int stackDepth;
 		}
 
 		public OperatorSerializer(Kryo kryo, Class<Operator<?>> type) {
 			this.fieldSerializer = new FieldSerializer<Operator<?>>(kryo, type);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Class)
 		 */
 		@Override
 		public Operator<?> read(Kryo kryo, Input input, Class<Operator<?>> type) {
 			final OperatorSerializationPool stack = OperatorSerializationStack.get();
 			List<Operator<?>> operatorDeserializedAt = stack.operatorDeserializedId;
 			if (input.readBoolean())
 				return operatorDeserializedAt.get(input.readByteUnsigned());
 			stack.stackDepth++;
 			Operator<?> object = kryo.newInstance(type);
 			operatorDeserializedAt.add(object);
 			FieldSerializer<?>.CachedField<?>[] fields = this.fieldSerializer.getFields();
 			for (int i = 0, n = fields.length; i < n; i++)
 				fields[i].read(input, object);
 			if (--stack.stackDepth == 0)
 				operatorDeserializedAt.clear();
 			return object;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#copy(com.esotericsoftware.kryo.Kryo, java.lang.Object)
 		 */
 		@Override
 		public Operator<?> copy(Kryo kryo, Operator<?> original) {
 			return this.fieldSerializer.copy(kryo, original);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#write(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Output, java.lang.Object)
 		 */
 		@Override
 		public void write(Kryo kryo, com.esotericsoftware.kryo.io.Output output, Operator<?> object) {
 			final OperatorSerializationPool stack = OperatorSerializationStack.get();
 			Map<Operator<?>, Integer> operatorSerializationId = stack.operatorSerializedId;
 			Integer serializationId = operatorSerializationId.get(object);
 			output.writeBoolean(serializationId != null);
 			if (serializationId != null) {
 				output.writeByte(serializationId);
 			} else {
 				operatorSerializationId.put(object, operatorSerializationId.size());
 				stack.stackDepth++;
 				this.fieldSerializer.write(kryo, output, object);
 				if (--stack.stackDepth == 0)
 					operatorSerializationId.clear();
 			}
 		}
 	}
 
 	public static class OperatorOutputSerializer extends com.esotericsoftware.kryo.Serializer<Output> {
 		/**
 		 * Initializes Operator.OperatorOutputSerializer.
 		 */
 		public OperatorOutputSerializer() {
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#write(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Output, java.lang.Object)
 		 */
 		@Override
 		public void write(Kryo kryo, com.esotericsoftware.kryo.io.Output output, Output object) {
 			kryo.writeClassAndObject(output, object.getOperator());
 			output.writeInt(object.getIndex(), true);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#copy(com.esotericsoftware.kryo.Kryo, java.lang.Object)
 		 */
 		@Override
 		public Output copy(Kryo kryo, Output original) {
 			return (Output) kryo.copy(original.getOperator()).getOutput(original.getIndex());
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Class)
 		 */
 		@Override
 		public Output read(Kryo kryo, Input input, Class<Output> type) {
 			return (Output) ((Operator<?>) kryo.readClassAndObject(input)).getOutput(input.readInt(true));
 		}
 
 	}
 }
