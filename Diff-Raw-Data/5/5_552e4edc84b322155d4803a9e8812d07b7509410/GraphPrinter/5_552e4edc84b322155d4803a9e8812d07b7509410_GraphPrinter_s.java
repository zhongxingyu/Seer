 package org.javaan.print;
 
 import java.io.PrintStream;
 
 import org.javaan.graph.ObjectVisitor;
 
 public class GraphPrinter<V, E> implements ObjectVisitor<V, E> {
 	
 	private final ObjectFormatter<V> vertexFormatter;
 	
 	private final ObjectFormatter<E> edgeFormatter;
 	
 	private final PrintStream output;
 	
 	public GraphPrinter(PrintStream output, ObjectFormatter<V> vertexFormatter, ObjectFormatter<E> edgeFormatter) {
 		this.output = output;
 		this.vertexFormatter = vertexFormatter;
 		this.edgeFormatter = edgeFormatter;
 	}
 	
 	
 	@Override
 	public boolean finished() {
 		return false;
 	}
 
 	@Override
 	public void visitVertex(V vertex, int level) {
		PrintUtil.indent(output, vertexFormatter, vertex, level * 2);
 	}
 
 	@Override
 	public void visitEdge(E edge, int level) {
		PrintUtil.indent(output, edgeFormatter, edge, level * 2 + 1);
 	}
 }
