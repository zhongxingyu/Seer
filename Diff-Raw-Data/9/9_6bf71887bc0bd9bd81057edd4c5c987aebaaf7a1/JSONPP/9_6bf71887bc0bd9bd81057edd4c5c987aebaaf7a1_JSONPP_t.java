 package jsonpp;
 
 import jsonpp.encode.DefaultCodec;
 import jsonpp.encode.Null;
 import jsonpp.layout.Layout;
 import jsonpp.util.MethodBox;
 import static jsonpp.util.MethodBox.mm;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Iterator;
 import java.util.Map;
 
 public class JSONPP {
 	private Writer writer;
 	private Layout layout;
 	private MethodBox<String> encodeKey;
 	private MethodBox<String> encodeValue;
 	private MethodBox<Iterator> iterator;
 	private MethodBox<Iterator<Map.Entry>> entries;
 
 	public JSONPP() {
 		this(new StringWriter());
 	}
 
 	public JSONPP(Writer writer) {
 		this(writer, new DefaultCodec(), new Layout());
 	}
 
 	public JSONPP(Writer writer, Codec codec, Layout layout) {
 		this.writer = writer;
 		this.layout = layout;
 		iterator = mm(codec, "iterator");
 		entries = mm(codec, "entries");
 		encodeKey = mm(codec, "encodeKey", "key");
 		encodeValue = mm(codec, "encodeValue", "value");
 	}
 
 	public void pp(Object o) {
 		pp(o,0);
 	}
 
 
 	public void pp(Object o, int depth) {
 		if (o == null) {
 			o = Null.INSTANCE;
 		}
 		Iterator<Map.Entry> entries = this.entries.call(o);
 		if (entries != null) {
 			pptree(entries, depth);
 		} else {
 			Iterator i = iterator.call(o);
 			if (i != null) {
 				ppseq(i, depth);
 			} else {
 				append(encodeValue.call(o));
 			}
 		}
 	}
 
 	private void ppseq(Iterator i, int depth) {
 		append("[");
		if (i.hasNext()) {
			carriageReturn(depth + 1);
		}
 		while (i.hasNext()) {
 			pp(i.next(), depth + 1);
 			if (i.hasNext()) {
 				append(",");
 				carriageReturn(depth + 1);
 			} else {
 				carriageReturn(depth);
 			}
 		}
 		append("]");
 	}
 
 	private void pptree(Iterator<Map.Entry> entries, int depth) {
 		append("{");
		if (entries.hasNext()) {
			carriageReturn(depth + 1);
		}
 		while (entries.hasNext()) {
 			Map.Entry entry = entries.next();
 			append(encodeKey.call(entry.getKey()) + ": ");
 			pp(entry.getValue(), depth + 1);
 			if (entries.hasNext()) {
 				append(",");
 				carriageReturn(depth + 1);
 			} else {
 				carriageReturn(depth);
 			}
 		}
 		append("}");
 	}
 
 	private void carriageReturn(int depth) {
 		append(this.layout.newline(), indent(depth));
 	}
 
 	private String indent(int depth) {
 		String seq = this.layout.indentWith ();
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < depth; i++) {
 			buffer.append(seq);
 		}
 		return buffer.toString();
 	}
 
 	private void append(String... strings) {
 		try {
 			for (String s: strings) {
 				writer.append(s);
 			}
 		} catch (Exception e) {
 			throw new PPException(e);
 		}
 	}
 
 	public Writer getWriter() {
 		return writer;
 	}
 
 }
