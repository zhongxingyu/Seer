 package ro.iasi.assigner;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import ro.iasi.communication.api.LinksCallback;
 import ro.iasi.communication.api.LinksDTO;
 
 public class Assigner implements LinksCallback {
 
 	private Queue<Node> domains;
 
 	public Assigner() throws URISyntaxException {
 		this.domains = new PriorityQueue<Node>(1000, new Comparator<Node>() {
 			@Override
 			public int compare(Node n1, Node n2) {
 				return (n1.getReferers().size() < n2.getReferers().size()) ? 1
 						: -1;
 			}
 
 		});
 		String str = "http://192.168.243.80/riw";
 		List<String> l=new LinkedList<String>();
 		this.addLinks(getDomain(str), l);
 	}
 
 	public void addLinks(String domain, List<String> links)
 			throws URISyntaxException {
 		Node node = null;
 		for (Node n : domains) {
 			if (n.getDomain().equals(domain)) {
 				node = n;
 				break;
 			}
 		}
 		if (node == null)
 			node = new Node(domain, domain);
 		for (String link : links)
 			// TODO: regex to only use absolute paths
 			if (link.substring(0, domain.length()).equals(domain))
 				node.addPage(link);
 			else {
 				Node toAdd = new Node(getDomain(link), link);
 				toAdd.addReferer(domain);
 				domains.add(toAdd);
 			}
 		domains.add(node);
 	}
 
 	public static String getDomain(String link) throws URISyntaxException {
 		URI u = new URI(link);
 		return u.getHost();
 	}
 
 	public Node getNextDomain() {
		Date now = new Date(System.currentTimeMillis() - 3600000);
 		Node domain = null;
 		for (Node d : domains)
			if (d.getTimestamp().after(now)) {
 				domain = d;
 				break;
 			}
 		if (domain == null)
 			return null;
 		domain.updateTimestamp();
 		return domain;
 	}
 
 	@Override
 	public LinksDTO getLinks() {
 		Node n = getNextDomain();
 		LinksDTO l = new LinksDTO();
 		l.setUrls(n.getPages());
 		return l;
 	}
 
 	@Override
 	public void sendLinks(LinksDTO linksDTO) {
 
 		try {
 			this.addLinks(getDomain(linksDTO.getUrls().get(0)),
 					linksDTO.getUrls());
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
