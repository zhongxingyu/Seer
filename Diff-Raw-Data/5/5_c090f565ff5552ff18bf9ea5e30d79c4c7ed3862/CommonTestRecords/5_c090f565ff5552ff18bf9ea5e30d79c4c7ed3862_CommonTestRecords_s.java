 package net.metadata.auselit.lorestore.servlet;
 
 /**
  * This class defines some test ORE records.
  * 
  * @author uqdayers
  */
 public abstract class CommonTestRecords {
 
 	private CommonTestRecords() {}
 	
 	public static final String ORE_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
 	"<rdf:RDF\r\n" + 
 	"	xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\r\n" + 
 	"	xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\r\n" + 
 	"	xmlns:annoreply=\"http://www.w3.org/2001/12/replyType#\"\r\n" + 
 	"	xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\r\n" + 
 	"	xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\r\n" + 
 	"	xmlns:annotea=\"http://www.w3.org/2000/10/annotation-ns#\"\r\n" + 
 	"	xmlns:vanno=\"http://austlit.edu.au/ontologies/2009/03/lit-annotation-ns#\"\r\n" + 
 	"	xmlns:sparql=\"http://www.w3.org/2005/sparql-results#\"\r\n" + 
 	"	xmlns:dcterms=\"http://purl.org/dc/terms/\"\r\n" + 
 	"	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\r\n" + 
 	"	xmlns:http=\"http://www.w3.org/1999/xx/http#\"\r\n" + 
 	"	xmlns:layout=\"http://maenad.itee.uq.edu.au/lore/layout.owl#\"\r\n" + 
 	"	xmlns:thread=\"http://www.w3.org/2001/03/thread#\"\r\n" + 
 	"	xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n" + 
 	"	xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\r\n" + 
 	"	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n" + 
 	"	xmlns:austlit=\"http://austlit.edu.au/owl/austlit.owl#\"\r\n" + 
 	"	xmlns:oac=\"http://www.openannotation.org/ns/\"\r\n" + 
 	"	xmlns:annotype=\"http://www.w3.org/2000/10/annotationType#\">\r\n" + 
 	"\r\n" + 
 	"<rdf:Description rdf:about=\"http://doc.localhost/rem/7d5d612e-1965-f6de-1d90-d3a10db2de1c\">\r\n" + 
 	"	<ore:describes rdf:resource=\"http://doc.localhost/rem/7d5d612e-1965-f6de-1d90-d3a10db2de1c#aggregation\"/>\r\n" + 
 	"	<rdf:type rdf:resource=\"http://www.openarchives.org/ore/terms/ResourceMap\"/>\r\n" + 
 	"	<dcterms:modified rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-02-23T17:02:49+10:00</dcterms:modified>\r\n" + 
 	"	<dcterms:created rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-02-23T17:02:14+10:00</dcterms:created>\r\n" + 
 	"	<dc:creator>Damien Ayers</dc:creator>\r\n" + 
 	"	<dc:title>Local Test CO</dc:title>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
 	"<rdf:Description rdf:about=\"http://doc.localhost/rem/7d5d612e-1965-f6de-1d90-d3a10db2de1c#aggregation\">\r\n" + 
 	"	<rdf:type rdf:resource=\"http://www.openarchives.org/ore/terms/Aggregation\"/>\r\n" + 
 	"	<dcterms:modified>2011-02-23T17:02:49+10:00</dcterms:modified>\r\n" + 
 	"	<ore:aggregates rdf:resource=\"http://omad.net/\"/>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
 	"<rdf:Description rdf:about=\"http://omad.net/\">\r\n" + 
 	"	<dc:title>omad.net</dc:title>\r\n" + 
 	"	<dc:format>text/html; charset=UTF-8</dc:format>\r\n" + 
 	"	<layout:x>40</layout:x>\r\n" + 
 	"	<layout:y>40</layout:y>\r\n" + 
 	"	<layout:width>220</layout:width>\r\n" + 
 	"	<layout:height>170</layout:height>\r\n" + 
 	"	<layout:originalHeight>-1</layout:originalHeight>\r\n" + 
 	"	<layout:orderIndex>1</layout:orderIndex>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
 	"<rdf:Description rdf:about=\"http://doc.localhost/rem/96ae9ffb-905a-79a6-b5bb-b927d94ef8b1\">\r\n" + 
 	"	<ore:describes rdf:resource=\"http://doc.localhost/rem/96ae9ffb-905a-79a6-b5bb-b927d94ef8b1#aggregation\"/>\r\n" + 
 	"	<rdf:type rdf:resource=\"http://www.openarchives.org/ore/terms/ResourceMap\"/>\r\n" + 
 	"	<dcterms:modified rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-02T13:46:40+10:00</dcterms:modified>\r\n" + 
 	"	<dcterms:created rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-02T13:43:33+10:00</dcterms:created>\r\n" + 
 	"	<dc:creator>Damien Ayers</dc:creator>\r\n" + 
 	"	<dc:title>Test CO</dc:title>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
 	"<rdf:Description rdf:about=\"http://doc.localhost/rem/96ae9ffb-905a-79a6-b5bb-b927d94ef8b1#aggregation\">\r\n" + 
 	"	<rdf:type rdf:resource=\"http://www.openarchives.org/ore/terms/Aggregation\"/>\r\n" + 
 	"	<dcterms:modified>2011-03-02T13:46:40+10:00</dcterms:modified>\r\n" + 
	"	<ore:aggregates rdf:resource=\"https://doc.localhost/oreservlet/secure/login.html\"/>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
	"<rdf:Description rdf:about=\"https://doc.localhost/oreservlet/secure/login.html\">\r\n" + 
 	"	<dc:title>503 Service Temporarily Unavailable</dc:title>\r\n" + 
 	"	<dc:format>text/html;charset=utf-8</dc:format>\r\n" + 
 	"	<layout:x>40</layout:x>\r\n" + 
 	"	<layout:y>40</layout:y>\r\n" + 
 	"	<layout:width>383</layout:width>\r\n" + 
 	"	<layout:height>285</layout:height>\r\n" + 
 	"	<layout:originalHeight>-1</layout:originalHeight>\r\n" + 
 	"	<layout:orderIndex>1</layout:orderIndex>\r\n" + 
 	"</rdf:Description>\r\n" + 
 	"\r\n" + 
 	"</rdf:RDF>";
 
 	public static final String SIMPLE_ORE_EXAMPLE = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dc10=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:layout=\"http://maenad.itee.uq.edu.au/lore/layout.owl#\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:annotea=\"http://www.w3.org/2000/10/annotation-ns#\" xmlns:annotype=\"http://www.w3.org/2000/10/annotationType#\" xmlns:thread=\"http://www.w3.org/2001/03/thread#\" xmlns:annoreply=\"http://www.w3.org/2001/12/replyType#\" xmlns:vanno=\"http://austlit.edu.au/ontologies/2009/03/lit-annotation-ns#\" xmlns:sparql=\"http://www.w3.org/2005/sparql-results#\" xmlns:http=\"http://www.w3.org/1999/xx/http#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:oac=\"http://www.openannotation.org/ns/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:austlit=\"http://austlit.edu.au/owl/austlit.owl#\">\r\n" + 
 			"	<ore:ResourceMap rdf:about=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773\">\r\n" + 
 			"		<ore:describes rdf:resource=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773#aggregation\"/>\r\n" + 
 			"		<dcterms:modified rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-09T16:47:22+10:00</dcterms:modified>\r\n" + 
 			"		<dcterms:created rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-09T16:39:33+10:00</dcterms:created>\r\n" + 
 			"		<dc:creator>Damien Ayers</dc:creator>\r\n" + 
 			"	</ore:ResourceMap>\r\n" + 
 			"	<ore:Aggregation rdf:about=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773#aggregation\">\r\n" + 
 			"		<dcterms:modified>2011-03-09T16:47:22+10:00</dcterms:modified>\r\n" + 
 			"		<ore:aggregates rdf:resource=\"http://omad.net/\"/>\r\n" + 
 			"	</ore:Aggregation>\r\n" + 
 			"	<rdf:Description rdf:about=\"http://omad.net/\">\r\n" + 
 			"		<dc:title>omad.net</dc:title>\r\n" + 
 			"		<dc:format>text/html; charset=UTF-8</dc:format>\r\n" + 
 			"		<layout:x>40</layout:x>\r\n" + 
 			"		<layout:y>40</layout:y>\r\n" + 
 			"		<layout:width>220</layout:width>\r\n" + 
 			"		<layout:height>170</layout:height>\r\n" + 
 			"		<layout:originalHeight>-1</layout:originalHeight>\r\n" + 
 			"		<layout:orderIndex>1</layout:orderIndex>\r\n" + 
 			"	</rdf:Description>\r\n" + 
 			"</rdf:RDF>";
 	
 	public static final String BAD_ORE_BROKEN_XML = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dc10=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:layout=\"http://maenad.itee.uq.edu.au/lore/layout.owl#\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:annotea=\"http://www.w3.org/2000/10/annotation-ns#\" xmlns:annotype=\"http://www.w3.org/2000/10/annotationType#\" xmlns:thread=\"http://www.w3.org/2001/03/thread#\" xmlns:annoreply=\"http://www.w3.org/2001/12/replyType#\" xmlns:vanno=\"http://austlit.edu.au/ontologies/2009/03/lit-annotation-ns#\" xmlns:sparql=\"http://www.w3.org/2005/sparql-results#\" xmlns:http=\"http://www.w3.org/1999/xx/http#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:oac=\"http://www.openannotation.org/ns/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:austlit=\"http://austlit.edu.au/owl/austlit.owl#\">\r\n" + 
 	"	<ore:ResourceMap rdf:about=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773\">\r\n" + 
 	"		<ore:describes rdf:resource=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773#aggregation\"/>\r\n" + 
 	"		<dcterms:modified rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-09T16:47:22+10:00</dcterms:modified>\r\n" + 
 	"		<dcterms:created rdf:datatype=\"http://purl.org/dc/terms/W3CDTF\">2011-03-09T16:39:33+10:00</dcterms:created>\r\n" + 
 	"		<dc:creator>Damien Ayers</dc:creator>\r\n" + 
 	"	</ore:ResourceMap>\r\n" + 
 	"	<ore:Aggregation rdf:about=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773#aggregation\">\r\n" + 
 	"		<dcterms:modified>2011-03-09T16:47:22+10:00</dcterms:modified>\r\n" + 
 	"		<ore:aggregates rdf:resource=\"http://omad.net/\"/>\r\n" + 
 	"	</ore:Aggregation>\r\n" + 
 	"	<rdf:Description rdf:about=\"http://omad.net/\">\r\n" + 
 	"		<dc:title>omad.net</dc:title>\r\n" + 
 	"		<dc:format>text/html; charset=UTF-8</dc:format>\r\n" + 
 	"		<layout:x>40</layout:x>\r\n" + 
 	"		<layout:y>40</layout:y>\r\n" + 
 	"		<layout:width>220</layout:width>\r\n" + 
 	"		<layout:height>170</layout:height>\r\n" + 
 	"		<layout:originalHeight>-1</layout:originalHeight>\r\n" + 
 	"		<layout:orderIndex>1</layout:orderIndex>\r\n" + 
 	"	</rdf:Description>\r\n";
 	
 	
 	public static final String BAD_ORE_NO_RESOURCEMAP = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dc10=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:layout=\"http://maenad.itee.uq.edu.au/lore/layout.owl#\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:annotea=\"http://www.w3.org/2000/10/annotation-ns#\" xmlns:annotype=\"http://www.w3.org/2000/10/annotationType#\" xmlns:thread=\"http://www.w3.org/2001/03/thread#\" xmlns:annoreply=\"http://www.w3.org/2001/12/replyType#\" xmlns:vanno=\"http://austlit.edu.au/ontologies/2009/03/lit-annotation-ns#\" xmlns:sparql=\"http://www.w3.org/2005/sparql-results#\" xmlns:http=\"http://www.w3.org/1999/xx/http#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:oac=\"http://www.openannotation.org/ns/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:austlit=\"http://austlit.edu.au/owl/austlit.owl#\">\r\n" + 
 	"	<ore:Aggregation rdf:about=\"http://doc.localhost/rem/344385ed-2a79-4598-8a99-27be35e0b773#aggregation\">\r\n" + 
 	"		<dcterms:modified>2011-03-09T16:47:22+10:00</dcterms:modified>\r\n" + 
 	"		<ore:aggregates rdf:resource=\"http://omad.net/\"/>\r\n" + 
 	"	</ore:Aggregation>\r\n" + 
 	"	<rdf:Description rdf:about=\"http://omad.net/\">\r\n" + 
 	"		<dc:title>omad.net</dc:title>\r\n" + 
 	"		<dc:format>text/html; charset=UTF-8</dc:format>\r\n" + 
 	"		<layout:x>40</layout:x>\r\n" + 
 	"		<layout:y>40</layout:y>\r\n" + 
 	"		<layout:width>220</layout:width>\r\n" + 
 	"		<layout:height>170</layout:height>\r\n" + 
 	"		<layout:originalHeight>-1</layout:originalHeight>\r\n" + 
 	"		<layout:orderIndex>1</layout:orderIndex>\r\n" + 
 	"	</rdf:Description>\r\n" + 
 	"</rdf:RDF>";
 }
