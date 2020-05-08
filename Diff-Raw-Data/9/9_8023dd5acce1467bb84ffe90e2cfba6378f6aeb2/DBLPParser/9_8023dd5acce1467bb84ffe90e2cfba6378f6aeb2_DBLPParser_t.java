 /**
  * 
  */
 package edu.cmu.DBLPProcessor;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.sax.SAXSource;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 /**
  * @author NASA-Trust-Team
  * 
  */
 public class DBLPParser {
 	private static int publicationcount = 0;
 	private static boolean debugMode = false;
 	private static List<Article> articleList = new ArrayList<Article>();
 	private static List<Book> bookList = new ArrayList<Book>();
 	private static List<Incollection> incollectionList = new ArrayList<Incollection>();
 	private static List<Inproceedings> inproceedingsList = new ArrayList<Inproceedings>();
 	private static List<Mastersthesis> mastersthesisList = new ArrayList<Mastersthesis>();
 	private static List<Phdthesis> phdthesisList = new ArrayList<Phdthesis>();
 	private static List<Proceedings> proceedingsList = new ArrayList<Proceedings>();
 	private static List<Www> wwwList = new ArrayList<Www>();
 	public static List<Publication> publicationList = new ArrayList<Publication>();
 	private static Map<String,Integer> citationList = new HashMap<String,Integer>();
 	private static Map<String,DBLPUser> dblpUserList = new HashMap<String,DBLPUser>();
 	public static Map<String,Integer> mapUserNameId = new HashMap<String,Integer>();
 	public static Map<String,String> mapKeyTitle = new HashMap<String,String>();
 
 	public static Map<String,DBLPUser> parseDBLP() throws JAXBException, IOException {
 		//This is main code for DBLP parser
 		DBLPParser dblpParser = new DBLPParser();
 		dblpParser.parseDBLPXML("dblp_example.xml");
 		//		dblpParser.parseDBLPXML("xaa.xml");
 		//		dblpParser.parseDBLP("xab.xml");
 		//		dblpParser.parseDBLP("xac.xml");
 		//		dblpParser.parseDBLP("xad.xml");
 		//		dblpParser.parseDBLP("xae.xml");
 		printParseDBLPXML();
 		parseAuthor(); //without citations
 		System.out.println("Author : "+dblpUserList.size());
 		Set<String> hs = dblpUserList.keySet();
 		Iterator it = hs.iterator();
 		while (it.hasNext())
 			System.out.println(it.next() );
 
 		return dblpUserList;
 	}
 
 	public static void xmlWriter(Publication publication, String filename) throws JAXBException
 	{
 		JAXBContext context = JAXBContext.newInstance(Publication.class);
 		Marshaller m = context.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		File file = null;
 		file = new File(filename);
 		m.marshal(publication, file);		
 	}
 
 	public static void xmlWriter(DBLPElement dblp, String filename, String type) 
 			throws JAXBException, IOException
 			{
 		JAXBContext context = JAXBContext.newInstance(Publication.class);
 		//		JAXBContext context = null;
 		//		if(type.equalsIgnoreCase("article"))context = JAXBContext.newInstance(ConvertedArticle.class);
 		//		else if(type.equalsIgnoreCase("book"))context = JAXBContext.newInstance(ConvertedBook.class);
 		//		else if(type.equalsIgnoreCase("incollection"))context = JAXBContext.newInstance(ConvertedIncollection.class);
 		//		else if(type.equalsIgnoreCase("inproceedings"))context = JAXBContext.newInstance(ConvertedInproceedings.class);
 		//		else if(type.equalsIgnoreCase("mastersthesis"))context = JAXBContext.newInstance(ConvertedMastersthesis.class);
 		//		else if(type.equalsIgnoreCase("phdthesis"))context = JAXBContext.newInstance(ConvertedPhdthesis.class);
 		//		else if(type.equalsIgnoreCase("proceedings"))context = JAXBContext.newInstance(ConvertedProceedings.class);
 		//		else if(type.equalsIgnoreCase("www"))context = JAXBContext.newInstance(ConvertedWww.class);
 		Marshaller m = context.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		File file = null;
 		file = new File(filename);
 		if(type.equalsIgnoreCase("article"))
 		{
 			Article article = (Article) dblp;
 			Publication publication = new Publication(article);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("book"))
 		{
 			Book book = (Book) dblp;
 			Publication publication = new Publication(book);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("incollection"))
 		{
 			Incollection incollection = (Incollection) dblp;
 			Publication publication = new Publication(incollection);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("inproceedings"))
 		{
 			Inproceedings inproceedings = (Inproceedings) dblp;
 			Publication publication = new Publication(inproceedings);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("mastersthesis"))
 		{
 			Mastersthesis mastersthesis = (Mastersthesis) dblp;
 			Publication publication = new Publication(mastersthesis);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("phdthesis"))
 		{
 			Phdthesis phdthesis = (Phdthesis) dblp;
 			Publication publication = new Publication(phdthesis);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("proceedings"))
 		{
 			Proceedings proceedings = (Proceedings) dblp;
 			Publication publication = new Publication(proceedings);
 			m.marshal(publication, file);
 		}
 		else if(type.equalsIgnoreCase("www"))
 		{
 			Www www = (Www) dblp;
 			Publication publication = new Publication(www);
 			m.marshal(publication, file);
 		}
 			}
 
 	public static void parseAuthor() throws JAXBException, IOException
 	{
 		//TODO Optimize by Reflection later
 		fillMapUserNameID();
 
 		for(int i=0; i<articleList.size(); i++)
 		{
 			publicationcount++;
 			Article article = articleList.get(i);
 			article.setId(publicationcount);
 			List<String> authorName = article.getAuthor(); 
 			if(authorName==null) authorName = article.getEditor();
 			String key = article.getKey();
 			mapKeyTitle.put(key, article.getTitle());
 			if(citationList.containsKey(key))article.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(article);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<bookList.size(); i++)
 		{
 			publicationcount++;
 			Book book = bookList.get(i);
 			book.setId(publicationcount);
 			List<String> authorName = book.getAuthor(); 
 			if(authorName==null) authorName = book.getEditor();
 			String key = book.getKey();
 			mapKeyTitle.put(key, book.getTitle());
 			if(citationList.containsKey(key))book.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(book);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);		
 		}
 
 		for(int i=0; i<incollectionList.size(); i++)
 		{
 			publicationcount++;
 			Incollection incollection = incollectionList.get(i);
 			incollection.setId(publicationcount);
 			List<String> authorName = incollection.getAuthor(); 
 			if(authorName==null) authorName = incollection.getEditor();
 			String key = incollection.getKey();
 			mapKeyTitle.put(key, incollection.getTitle());
 			if(citationList.containsKey(key))incollection.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(incollection);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<inproceedingsList.size(); i++)
 		{
 			publicationcount++;
 			Inproceedings inproceedings = inproceedingsList.get(i);
 			inproceedings.setId(publicationcount);
 			List<String> authorName = inproceedings.getAuthor(); 
 			if(authorName==null) authorName = inproceedings.getEditor();
 			String key = inproceedings.getKey();
 			mapKeyTitle.put(key, inproceedings.getTitle());
 			if(citationList.containsKey(key))inproceedings.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					//this is where the problem is coming, the publication is not traversing through the inproceedings properly
 					//at this time, mapUserNameId has fewer elements, so it is unable to add properly
 					publication = new Publication(inproceedings);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<mastersthesisList.size(); i++)
 		{
 			publicationcount++;
 			Mastersthesis mastersthesis = mastersthesisList.get(i);
 			mastersthesis.setId(publicationcount);
 			List<String> authorName = mastersthesis.getAuthor(); 
 			if(authorName==null) authorName = mastersthesis.getEditor();
 			String key = mastersthesis.getKey();
 			mapKeyTitle.put(key, mastersthesis.getTitle());
 			if(citationList.containsKey(key))mastersthesis.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(mastersthesis);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<phdthesisList.size(); i++)
 		{
 			publicationcount++;
 			Phdthesis phdthesis = phdthesisList.get(i);
 			phdthesis.setId(publicationcount);
 			List<String> authorName = phdthesis.getAuthor(); 
 			if(authorName==null) authorName = phdthesis.getEditor();
 			String key = phdthesis.getKey();
 			mapKeyTitle.put(key, phdthesis.getTitle());
 			if(citationList.containsKey(key))phdthesis.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(phdthesis);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<proceedingsList.size(); i++)
 		{
 			publicationcount++;
 			Proceedings proceedings = proceedingsList.get(i);
 			proceedings.setId(publicationcount);
 			List<String> authorName = proceedings.getAuthor(); 
 			if(authorName==null) authorName = proceedings.getEditor();
 			String key = proceedings.getKey();
 			mapKeyTitle.put(key, proceedings.getTitle());
 			if(citationList.containsKey(key))proceedings.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(proceedings);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 		for(int i=0; i<wwwList.size(); i++)
 		{
 			publicationcount++;
 			Www www = wwwList.get(i);
 			www.setId(publicationcount);
 			List<String> authorName = www.getAuthor(); 
 			if(authorName==null) authorName = www.getEditor();
 			String key = www.getKey();
 			mapKeyTitle.put(key, www.getTitle());
 			if(citationList.containsKey(key))www.setCited(citationList.get(key));
 			Publication publication=null;
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					if(dblpUserList.containsKey(authorName.get(j)))
 						dblpuser = dblpUserList.get(authorName.get(j));
 					else
 					{
 						dblpuser = new DBLPUser();
 						dblpuser.setName(authorName.get(j));
 						mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 					}
 					publication = new Publication(www);
 					dblpuser.setPublication(publication);
 					dblpUserList.put(authorName.get(j),dblpuser);
 					publicationList.add(publication);
 				}
 			}
 			//String filename = "DBLP_XML/publication"+publication.getId()+".xml";
 			//xmlWriter(publication,filename);
 		}
 
 
 	}
 
 	/**
 	 * This function is necessary because in parseAuthor, while instantiating publication, the mapUserNameID is not complete
 	 * so it always gives the author list as <first ID>, null, null
 	 */
 	private static void fillMapUserNameID() {
 		for(int i=0; i<inproceedingsList.size(); i++)
 		{
 			Inproceedings inproceedings = inproceedingsList.get(i);
 			List<String> authorName = inproceedings.getAuthor(); 
 			if(authorName==null) authorName = inproceedings.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<wwwList.size(); i++)
 		{
 			Www www = wwwList.get(i);
 			List<String> authorName = www.getAuthor(); 
 			if(authorName==null) authorName = www.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<proceedingsList.size(); i++)
 		{
 			Proceedings proceedings = proceedingsList.get(i);
 			List<String> authorName = proceedings.getAuthor(); 
 			if(authorName==null) authorName = proceedings.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<phdthesisList.size(); i++)
 		{
 			Phdthesis phdthesis = phdthesisList.get(i);
 			List<String> authorName = phdthesis.getAuthor(); 
 			if(authorName==null) authorName = phdthesis.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<mastersthesisList.size(); i++)
 		{
 			Mastersthesis mastersthesis = mastersthesisList.get(i);
 			List<String> authorName = mastersthesis.getAuthor(); 
 			if(authorName==null) authorName = mastersthesis.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<incollectionList.size(); i++)
 		{
 			Incollection incollection = incollectionList.get(i);
 			List<String> authorName = incollection.getAuthor(); 
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<bookList.size(); i++)
 		{
 			Book book = bookList.get(i);
 			List<String> authorName = book.getAuthor(); 
 			if(authorName==null) authorName = book.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 
 		for(int i=0; i<articleList.size(); i++)
 		{
 			Article article = articleList.get(i);
 			List<String> authorName = article.getAuthor(); 
 			if(authorName==null) authorName = article.getEditor();
 			if(authorName != null)
 			{
 				for(int j=0; j<authorName.size(); j++)
 				{
 					if(debugMode) System.out.println(authorName.get(j));
 					DBLPUser dblpuser;
 					dblpuser = new DBLPUser();
 					dblpuser.setName(authorName.get(j));
 					mapUserNameId.put(dblpuser.getName(), dblpuser.getId());
 				}
 			}
 		}
 	}
 
 	public static void printParseDBLPXML()
 	{
 		System.out.println("============ Summary ============");
 		System.out.println("Article : "+articleList.size());
 		System.out.println("Book : "+bookList.size());
 		System.out.println("Incollection : "+incollectionList.size());
 		System.out.println("Inproceedings : "+inproceedingsList.size());
 		System.out.println("Mastersthesis : "+mastersthesisList.size());
 		System.out.println("Phdthesis : "+phdthesisList.size());
 		System.out.println("Proceedings : "+proceedingsList.size());
 		System.out.println("Www : "+wwwList.size());
 	}
 
 	public void parseDBLPXML(String filename) {
 		try {
 			System.out.println("============ "+filename+" ============");
 			JAXBContext jaxbContext = JAXBContext.newInstance(DBLPElement.class);
 
 			SAXParserFactory spf = SAXParserFactory.newInstance();
//			spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
 			XMLReader xmlReader = spf.newSAXParser().getXMLReader();
 			InputSource inputSource = new InputSource(new FileReader(filename));
 			SAXSource source = new SAXSource(xmlReader, inputSource);
 
 			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
 			DBLPElement dblp = (DBLPElement) jaxbUnmarshaller.unmarshal(source);
 
 			List<Article> parse_articleList = (List<Article>)dblp.getArticleList();
 			if(parse_articleList!=null)
 			{
 				System.out.println("article : "+parse_articleList.size());
 				articleList.addAll(parse_articleList);
 				for(int i=0; i<articleList.size(); i++)
 				{
 					Article article = articleList.get(i);
 					List<String> cite = article.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Book> parse_bookList = (List<Book>)dblp.getBookList();
 			if(parse_bookList!=null)
 			{
 				System.out.println("book : "+parse_bookList.size());
 				bookList.addAll(parse_bookList);
 				for(int i=0; i<bookList.size(); i++)
 				{
 					Book book = bookList.get(i);
 					List<String> cite = book.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Incollection> parse_incollectionList = (List<Incollection>)dblp.getIncollectionList();
 			if(parse_incollectionList!=null)
 			{
 				System.out.println("incollection : "+parse_incollectionList.size());
 				incollectionList.addAll(parse_incollectionList);
 				for(int i=0; i<incollectionList.size(); i++)
 				{
 					Incollection incollection = incollectionList.get(i);
 					List<String> cite = incollection.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Inproceedings> parse_inproceedingsList = (List<Inproceedings>)dblp.getInproceedingsList();
 			if(parse_inproceedingsList!=null)
 			{
 				System.out.println("inproceeding : "+parse_inproceedingsList.size());
 				inproceedingsList.addAll(parse_inproceedingsList);
 				for(int i=0; i<inproceedingsList.size(); i++)
 				{
 					Inproceedings inproceedings = inproceedingsList.get(i);
 					List<String> cite = inproceedings.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Mastersthesis> parse_mastersthesisList = (List<Mastersthesis>)dblp.getMastersthesisList();
 			if(parse_mastersthesisList!=null)
 			{
 				System.out.println("masterthesis : "+parse_mastersthesisList.size());
 				mastersthesisList.addAll(parse_mastersthesisList);
 				for(int i=0; i<mastersthesisList.size(); i++)
 				{
 					Mastersthesis mastersthesis = mastersthesisList.get(i);
 					List<String> cite = mastersthesis.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Phdthesis> parse_phdthesisList = (List<Phdthesis>)dblp.getPhdthesisList();
 			if(parse_phdthesisList!=null)
 			{
 				System.out.println("phdthesis : "+parse_phdthesisList.size());
 				phdthesisList.addAll(parse_phdthesisList);
 				for(int i=0; i<phdthesisList.size(); i++)
 				{
 					Phdthesis phdthesis = phdthesisList.get(i);
 					List<String> cite = phdthesis.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Proceedings> parse_proceedingsList = (List<Proceedings>)dblp.getProceedingsList();
 			if(parse_proceedingsList!=null)
 			{
 				System.out.println("proceedings : "+parse_proceedingsList.size());
 				proceedingsList.addAll(parse_proceedingsList);
 				for(int i=0; i<proceedingsList.size(); i++)
 				{
 					Proceedings proceedings = proceedingsList.get(i);
 					List<String> cite = proceedings.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 			List<Www> parse_wwwList = (List<Www>)dblp.getWwwList();
 			if(parse_wwwList!=null)
 			{
 				System.out.println("www : "+parse_wwwList.size());
 				wwwList.addAll(parse_wwwList);
 				for(int i=0; i<wwwList.size(); i++)
 				{
 					Www www = wwwList.get(i);
 					List<String> cite = www.getCite();
 					if(cite!=null)
 					{
 						for(int j=0; j<cite.size();j++)
 						{
 							String key = cite.get(j);
 							if(citationList.containsKey(key))
 								citationList.put(key, citationList.get(key)+1);
 							else citationList.put(key, 1);
 						}
 					}
 				}
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public static DBLPUser getDBLPUserFromID(int id) {
 		Iterator<String> it = dblpUserList.keySet().iterator();
 		while (it.hasNext())
 		{
 			String key = it.next();
 			DBLPUser author = dblpUserList.get(key);
 
 			if(author.getId()== id)
 				return author;
 			//System.out.println(author);
 		}
 
 		return null;
 	}
 }
