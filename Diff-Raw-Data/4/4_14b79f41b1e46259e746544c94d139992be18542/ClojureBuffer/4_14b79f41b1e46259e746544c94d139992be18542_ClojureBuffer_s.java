 package cascading.clojure;
 
 import cascading.operation.BaseOperation;
 import cascading.operation.Buffer;
 import cascading.operation.OperationCall;
 import cascading.operation.BufferCall;
 import cascading.flow.FlowProcess;
 import cascading.tuple.TupleEntry;
 import cascading.tuple.TupleEntryCollector;
 import cascading.tuple.Tuple;
 import cascading.tuple.Fields;
 import clojure.lang.IFn;
 import clojure.lang.RT;
 import clojure.lang.ISeq;
 import java.util.Collection;
 
 public class ClojureBuffer extends BaseOperation<Object>
                                implements Buffer<Object> {
   private Object[] fn_spec;
   private IFn fn;
 
   public ClojureBuffer(Fields out_fields, Object[] fn_spec) {
     super(out_fields);
     this.fn_spec = fn_spec;
   }
   
   public void prepare(FlowProcess flow_process, OperationCall<Object> op_call) {
     this.fn = Util.bootFn(fn_spec);
   }
 
   public void operate(FlowProcess flow_process, BufferCall<Object> buff_call) {
     try {
 	Collection coll = (Collection) this.fn.invoke(buff_call.getArgumentsIterator());
      buff_call.getOutputCollector().add(Util.coerceToTuple(coll));
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 }
