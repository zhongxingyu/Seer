 package io.zig.data;
 
 import org.fressian.handlers.ILookup;
 import org.fressian.FressianReader;
 import org.fressian.Reader;
 import org.fressian.handlers.ReadHandler;
 
 import java.io.ByteArrayInputStream;
 import java.io.PushbackInputStream;
 import java.io.IOException;
 
 import java.io.StringWriter;
 import us.bpsm.edn.Keyword;
 import us.bpsm.edn.Symbol;
 import us.bpsm.edn.printer.Printer;
 import us.bpsm.edn.printer.Printer.Fn;
 import us.bpsm.edn.printer.Printers;
 import us.bpsm.edn.protocols.Protocol;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URI;
 
 final class Deserialize {
 
 	static final Printer.Fn<Object[]> vectorPrintFn = new Printer.Fn<Object[]>() {
 		@Override
 		public void eval(Object[] self, Printer writer) {
 			writer.append('[');
 			for (Object o : self) {
 				writer.printValue(o);
 			}
 			writer.append(']');
 		}
 	};
 
 	static final Printer.Fn<List<?>> listPrintFn = new Printer.Fn<List<?>>() {
 		@Override
 		public void eval(List<?> self, Printer writer) {
 			writer.append('(');
 			for (Object o : self) {
 				writer.printValue(o);
 			}
 			writer.append(')');
 		}
 	};
 
 	static final Printer.Fn<URI> uriPrintFn = new Printer.Fn<URI>() {
 		@Override
 		public void eval(URI self, Printer writer) {
 			writer.append("#uri\"");
 			writer.append(self.toString());
 			writer.append("\"");
 		}
 	};
 
 	static Protocol<Fn<?>> createPrinterProtocol() {
 		return Printers.defaultProtocolBuilder()
 				.put(Object[].class, vectorPrintFn)
 				.put(List.class, listPrintFn).put(URI.class, uriPrintFn)
 				.build();
 	}
 
 	static final Protocol<Fn<?>> protocol = createPrinterProtocol();
 
 	static Map<Object, ReadHandler> createHandlers() {
 
 		Map<Object, ReadHandler> handlers = new HashMap<Object, ReadHandler>();
 
 		handlers.put("key", new ReadHandler() {
 			public Object read(Reader r, Object tag, int componentCount)
 					throws IOException {
 				assert (componentCount == 2);
 				return Keyword.newKeyword((String) r.readObject(),
 						(String) r.readObject());
 			}
 		});
 
 		handlers.put("sym", new ReadHandler() {
 			public Object read(Reader r, Object tag, int componentCount)
 					throws IOException {
 				assert (componentCount == 2);
 				return Symbol.newSymbol((String) r.readObject(),
 						(String) r.readObject());
 			}
 		});
 
 		handlers.put("char", new ReadHandler() {
 			public Object read(Reader r, Object tag, int componentCount)
 					throws IOException {
 				assert (componentCount == 1);
 				Integer codePoint = ((Long) (r.readInt())).intValue();
 				return (Character) Character.toChars(codePoint)[0];
 			}
 		});
 
 		return Collections.unmodifiableMap(handlers);
 	}
 
 	static final ILookup<Object, ReadHandler> lookup = new ILookup<Object, ReadHandler>() {
 		final Map<Object, ReadHandler> handlers = createHandlers();
 
 		public ReadHandler valAt(Object key) {
 			return handlers.get(key);
 		}
 	};
 
 	public static final List<Object> toObjectsFromFressian(byte[] bytes)
 			throws IOException {
 		List<Object> objects = new ArrayList<Object>();
 		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
 		PushbackInputStream pb = new PushbackInputStream(bais);
 		Reader reader = new FressianReader(pb, lookup);
 		int token = 0;
 		boolean done = false;
 		while (!done) {
 			objects.add(reader.readObject());
 			token = pb.read();
 			if (token == 0xCF || token == -1) {
 				done = true;
 			}
			pb.unread(token);
 		}
 		return Collections.unmodifiableList(objects);
 	}
 
 	public static final String toEdnFromObjects(List<Object> objects)
 			throws IOException {
 		StringWriter sw = new StringWriter();
 		Printer ew = Printers.newPrinter(protocol, sw);
 		for (Object o : objects) {
 			ew.printValue(o);
 		}
 		return sw.toString();
 	}
 
 	public static final String toEdnFromFressian(byte[] bytes)
 			throws IOException {
 		return toEdnFromObjects(toObjectsFromFressian(bytes));
 	}
 
 }
