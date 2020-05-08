 package com.gmail.seizans.graph;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 public class DijkstraTest {
 
 	@Test
 	public void totalTest1() {
 		Vertex s = new Vertex("s", Arrays.asList(new Edge(10, "t"), new Edge(5, "y")));
 		Vertex t = new Vertex("t", Arrays.asList(new Edge(1, "x"), new Edge(2, "y")));
 		Vertex x = new Vertex("x", Arrays.asList(new Edge(4, "z")));
 		Vertex y = new Vertex("y", Arrays.asList(new Edge(3, "t"), new Edge(9, "x")));
 		Vertex z = new Vertex("z", Arrays.asList(new Edge(7, "s"), new Edge(6, "x")));
 		List<Vertex> vertexes = new ArrayList<Vertex>();
 		vertexes.add(s);
 		vertexes.add(t);
 		vertexes.add(x);
 		vertexes.add(y);
 		vertexes.add(z);
 		Dijkstra dijkstra = new Dijkstra(vertexes);
 		dijkstra.dijkstra("s");
 		assertThat(dijkstra.getVertex("s").getDist(), is(0));
		assertThat(dijkstra.getVertex("y").getDist(), is(5));
 		assertThat(dijkstra.getVertex("t").getDist(), is(8));
 		assertThat(dijkstra.getVertex("x").getDist(), is(9));
		assertThat(dijkstra.getVertex("z").getDist(), is(13));
 		// TODO ROUTE LIST の Assert
 	}
 
 }
