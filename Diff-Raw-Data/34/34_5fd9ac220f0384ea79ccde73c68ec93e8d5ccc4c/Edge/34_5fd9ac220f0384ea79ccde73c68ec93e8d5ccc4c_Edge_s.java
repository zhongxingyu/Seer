 package cz.nuc.wheelgo.dijkstra;
 
 import cz.nuc.wheelgo.NavigationParameters;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 /**
  * 
  * @source http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
  * 
  */
 
 public class Edge {
 	private final String id;
 	private final Vertex source;
 	private final Vertex destination;
 	private final float weight;
 
 	public Edge(String id, Vertex source, Vertex destination, float weight) {
 		this.id = id;
 		this.source = source;
 		this.destination = destination;
 		this.weight = weight;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public Vertex getDestination() {
 		return destination;
 	}
 
 	public Vertex getSource() {
 		return source;
 	}
 
 	public float getWeight() {
 		return weight;
 	}
 
 	@Override
 	public String toString() {
 		return "ID=" + id + " -> " + source + " " + destination;
 	}
 
 	public static float calculateWeight(Vertex vertex, Vertex oldVertex, Element edge, NavigationParameters params) {
 		double earthRadius = 6369;
 		double dLat = Math.toRadians(vertex.getLatitude()
 				- oldVertex.getLatitude());
 		double dLng = Math.toRadians(vertex.getLongitude()
 				- oldVertex.getLongitude());
 		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
 				+ Math.cos(Math.toRadians(oldVertex.getLatitude()))
 				* Math.cos(Math.toRadians(vertex.getLatitude()))
 				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = earthRadius * c;
 
 		float incline = 1;
 		if (edge != null)
 		{
 			incline = getManualInclineForTesting(edge.getAttribute("id"));
 			
 			NodeList tags = edge.getElementsByTagName("tag");
 			for (int j = 0; j < tags.getLength(); j++) {
 				Element tag = (Element) tags.item(j);
				if (tag.hasAttribute("incline")
 						&& tag.getAttribute("k").equals("incline")) {
 					incline = calculateWeightIncline(Integer.parseInt(tag.getAttribute("value")), params.maxIncline);
 				}
 			}
 		}
 		float price = new Float(dist) * incline;
 		
 		return price;
 	}
 	
 	private static float getManualInclineForTesting(String wayId)
 	{
		if (wayId.equalsIgnoreCase("31001316"))
		{
			return 50;
		}
		return 0;
 	}
 	
 	private static float calculateWeightIncline(int incline, int maxIncline)
 	{
 		// <20 %
 		if (incline < maxIncline * 20 / 100 )
 			return 1;
 		// <50 %
 		if (incline < maxIncline * 50 / 100 )
 			return 1.1f;
 		// <80 %
 		if (incline < maxIncline * 80 / 100 )
 			return 1.1f;
 		// <100 %
 		if (incline < maxIncline * 100 / 100 )
 			return 1.1f;
 		// otherwise
 		if (incline >= maxIncline)
 			return Float.MAX_VALUE;
 		
 		return 1;
 	}
 
 }
