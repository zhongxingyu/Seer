 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.table.*;
 
 import org.apache.commons.lang3.StringUtils;
 import java.sql.*;
 
 class Assignment1
 {
 	final static String REGEXP_SPLIT_TOKENS = "\\s+";	// \s+ , multiple white spaces TODO: check if text contains markup tags
     final static String INDEX_TABLE_NAME = "index_doc";
     final static String TOKEN_INDEX = "token_index";
     final static String DOCUMENTS_TABLE_NAME = "documents";
 	final static String indexFileDBPath = "index.db";
 	static String indexFilePath = "inverted_index";
 	static int maxTokenLength = 0;	// stores the longest found token's length
 
 	public static void main(String[] args)
 	{
		long startTime = System.currentTimeMillis();

 		if(args.length == 0)
 		{
 			printUsage();
 			return;
 		}
 		else if(args.length >= 2 && args[0].equals("-index"))
 		{
 			// ------------------------------ create index ----------------------------------
 			String filename = args[1];
 			try {
 				HashMap<String, Vector<MedlineTokenLocation>> invertedIndex  = buildIndex(filename);
 				  
 				if(args.length >= 3)
 				{
 					if (args[2].equals("-charArr"))
 					{
 						indexFilePath = "charArr_" + indexFilePath;
 						buildCharArrIndex(invertedIndex);
 					}
 					// else if(args[2].equals())
 					else
 					{
 						// any other index type, including -sql
 						buildSQLIndex(invertedIndex, filename);
 					}
 				}
 				else
 				{
 					// no index type given, assume sql
 					buildSQLIndex(invertedIndex, filename);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			// ------------------------------ search ----------------------------------
 			if(args[0].indexOf(" ") == -1)
 			{
 				//System.out.println("token search:");
 				
 				// token search
 				if(args.length >= 2)
 				{
 					if(args[args.length-1].equals("-charArr"))
 					{
 						// TODO delete
 						//System.out.println("using charArr index");
 						
 						String[] tokens = new String[args.length-1];
 						System.arraycopy(args, 0, tokens, 0, args.length-1);
 						File indexFile = new File("charArr_" + indexFilePath);
 						if(!indexFile.exists())
 						{
 							System.out.println("ERROR: must build index first");
 							printUsage();
 							System.exit(1);
 						}
 						try{
 							Set<Integer> pmids = boolQueryCharArrIndex(indexFile, tokens);
 							System.out.println("found " + pmids.size() + " containing documents");
 						}
 						catch(ClassNotFoundException e)
 						{
 							e.printStackTrace();
 						}
 						catch(IOException e)
 						{
 							e.printStackTrace();
 						}
 					}
 					else if(args[args.length-1].equals("-sql"))
 					{
 						// TODO delete
 						//System.out.println("using sql index");
 						
 						String[] tokens = new String[args.length-1];
 						System.arraycopy(args, 0, tokens, 0, args.length-1);
 						defaultBoolQuery(tokens);
 					}
 					else
 					{
 						// the last arg (possibly unsupported index type) is treated as a token
 						defaultBoolQuery(args);
 					}
 				}
 				else
 				{
 					// numArgs = 1
 					defaultBoolQuery(args);
 				}
 			}
 			else
 			{
 				// phrase search
 				//System.out.println("phrase search");
 				if(args.length > 1)
 				{
 					if(args[args.length-1].equals("-charArr"))
 					{
 						// using charArr index
 						// TODO
 					}
 					else if(args[args.length-1].equals("-sql"))
 					{
 						// using sql index
 						phraseQuerySQL(args[0].split(" "));
 					}
 					else
 					{
 						System.out.println("WARNING: unsupported index type: " + args[args.length-1]);
 						System.out.println("Using default index: sql");
 						// using sql index
 						phraseQuerySQL(args[0].split(" "));
 					}
 				}
 				else
 				{
 					// using sql index
 					phraseQuerySQL(args[0].split(" "));
 				}
 			}
 		}
		System.out.println((System.currentTimeMillis() - startTime)/1000.0f + "s");
 	}
 	
 	public static void defaultBoolQuery(String[] tokens)
 	{
 		//System.out.println("using default index [sql]");
 		boolQuerySQLNative(tokens);
 	}
 	
 	public static Set<Integer> boolQueryCharArrIndex(File indexFile, String[] tokens) throws IOException, ClassNotFoundException
 	{
 		FileInputStream fis = new FileInputStream(indexFile);
 		ObjectInputStream objI = new ObjectInputStream(new BufferedInputStream(fis));
 		char[][] hashFunc = (char[][])objI.readObject();
 		int[][] pmidsPerToken = (int[][])objI.readObject();
 		objI.close();
 		fis.close();
 		
 		// TODO: use a TreeSet or HashSet for set intersection?
 		HashSet<Integer> pmids = new HashSet<Integer>(32768, 1.0f);	// 2^15, avoid re-hashing
 		
 		//--------------------------- determine intersection order -------------------------------------
 		
 		// indexed by its index in tokens[]
 		Vector<HashSet<Integer>> pmidsForEachToken = new Vector<HashSet<Integer>>(tokens.length);
 		// used to determine intersection order - sort tokenIndex'es by their number of found pmids
 		// i.e. {{0, [numOfFoundPmids]}, {1, [numOfFoundPmids]}, ...}
 		Vector<Vector<Integer>> tokenIndexWithPmidLength = new Vector<Vector<Integer>>(tokens.length);
 		// fill pmidsForEachToken
 		for(int j = 0; j < tokens.length; j++)
 		{
 			String token = tokens[j];
 			// get hash value
 			int tokenIndex = binSearchIndex(hashFunc, token);
 			if(tokenIndex == -1)
 			{
 				System.out.println("warning: token " + token + " could not be found, is ignored");
 				break;	// this is ok. no empty hashset has to be inserted into pmidsForEachToken becuase of intersection order algorithm
 			}
 			// query hash table
 			int[] pmidsForToken = pmidsPerToken[tokenIndex];
 			// convert int[] to HashSet<Integer> to be able to perform intersection
 			int len = pmidsForToken.length;
 			HashSet<Integer> pmidsForTokenSet = new HashSet<Integer>(len*2, 1.0f);
 			for (int i = 0; i < len; i++)
 			{
 				pmidsForTokenSet.add(pmidsForToken[i]);
 			}
 			pmidsForEachToken.add(pmidsForTokenSet);
 			
 			// fill tokenIndexWithPmidLength
 			Vector<Integer> tmp = new  Vector<Integer>(2);
 			tmp.add(j);
 			tmp.add(len);
 			tokenIndexWithPmidLength.add(tmp);
 		}
 		// sort pmidsForEachToken by descending number of pmids
 		Collections.sort(tokenIndexWithPmidLength, new PmidListIntersectionOrderComparator<Vector<Integer>>());
 		// the intersection order is now determined by the order of elements in tokenIndexWithPmidLength
 		// each entry in tokenIndexWithPmidLength has the form {tokenIndex, numberOfFoundPmids}
 		
 		// perform intersections
 		boolean wasInitiallyFilled = false;
 		for(int j = 0; j < tokens.length; j++)
 		{
 			int sortedByPmidSizeIndex = tokenIndexWithPmidLength.get(j).get(0);
 			HashSet<Integer> nextIntersectionSet = pmidsForEachToken.get(sortedByPmidSizeIndex);
 			// fill initially or intersect
 			if(!wasInitiallyFilled && pmids.size() == 0)
 			{
 				pmids.addAll(nextIntersectionSet);
 				wasInitiallyFilled = true;
 			}
 			else
 				pmids.retainAll(nextIntersectionSet);
 			if(pmids.size() == 0)
 				break;
 		}
 		return pmids;
 	}
 	
 	public static int binSearchIndex(char[][] hashFunc, String token)
 	{
 		int l = 0, r = hashFunc.length-1, m;
 		
 		while(l <= r)
 		{
 			m = (l+r)/2;
 			int comp = new String(hashFunc[m]).compareTo(token);
 			if(comp < 0)
 			{
 				l = m+1;
 			}
 			else if(comp > 0)
 			{
 				r = m-1;
 			}
 			else return m;
 		}
 		
 		return -1;
 	}
 	
 	public static void boolQuerySQLNative(String[] querytokens)
 	{
 		File dbFile = new File(indexFileDBPath);
 		if(!dbFile.exists())
 		{
 			System.out.println("ERROR: must build index first");
 			printUsage();
 			System.exit(1);
 		}
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
         Vector<Integer> documents = new Vector<Integer>();
         //boolean wasInitiallyFilled = false;
 		try {
 			Connection conn =
 			      DriverManager.getConnection("jdbc:sqlite:"+indexFileDBPath);
 			//Statement stat = conn.createStatement();
 			StringBuilder sb = new StringBuilder(128);
 			sb.append("(");
 			for(int i = 0; i < querytokens.length; i++)
 			{
 				sb.append("?,");
 			}
 			String tokenList = sb.substring(0, sb.length()-1) + ")";	 // = (?,?,?)
 			
 			PreparedStatement prep = conn.prepareStatement(
 					"select distinct doc_id from "+INDEX_TABLE_NAME+" where token IN "+tokenList+";");
 			for(int i = 0; i < querytokens.length; i++)
 			{
 				prep.setString(i+1, querytokens[i]);
 			}
 			
 				try
 				{
 					//ResultSet rs = stat.executeQuery("select doc_id from index_doc where token IN \""+ token + "\";");
 					ResultSet rs = prep.executeQuery();
 					try {		            	
 	            		while (rs.next()) {
 	            			documents.add(rs.getInt("doc_id"));
 	                	}
 	            	} finally {
 	            		rs.close();
 	            	}
 					
 					
 					/*	
 					for (String token: querytokens)
 					{
 
 		            	//documents for this token
 	            		Vector<Integer> current_docs = new Vector<Integer>();     
 
 						ResultSet rs = stat.executeQuery("select doc_id from index_doc where token = \""+ token + "\";");
 		            	try {		            	
 		            		while (rs.next()) {
 		            				current_docs.add(rs.getInt("doc_id"));
 		                	}
 		            				            		
 		            	} finally {
 		            		rs.close();
 		            	}
 		            	
 		            	if (!wasInitiallyFilled && documents.size() == 0)
 			            {
 			            	//keep current documents for initial set
 			            	documents = current_docs;
 			            	wasInitiallyFilled = true;
 			            } else {
 			            	//intersect with documents from before (AND)
 			            	HashSet<Integer> current_docs_set = new HashSet<Integer>(current_docs);
 			            	documents.retainAll(current_docs_set);
 			            }
 					}*/
 				} finally {
 					prep.close();
 					//stat.close();
 					conn.close();	
 				}
 				
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		for (Integer doc : documents)
 		{
 			System.out.println(doc);
 		}
 
 		System.out.print("Found "+ documents.size()+ " document(s) matching your query");
 		if (documents.size() == 0) { System.out.println(".");} else { System.out.println(":");}
 		
 	}
 
 	public static Vector<Integer> boolQuerySQL(String[] querytokens, boolean talkative)
 	{
 		File dbFile = new File(indexFileDBPath);
 		if(!dbFile.exists())
 		{
 			System.out.println("ERROR: must build index first");
 			printUsage();
 			System.exit(1);
 		}
 				
 		SqlJetDb db;
 
         Vector<Integer> documents = new Vector<Integer>();
         boolean wasInitiallyFilled = false;
 		try {
 			db = SqlJetDb.open(dbFile, true);
 					
 			for (String token: querytokens)
 			{
 				//TODO: create array of document ids per token plus count, then sort by
 				//count and then intersect starting with smallest set 
 		        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
 				try {
 					ISqlJetTable table = db.getTable(INDEX_TABLE_NAME);
 		            ISqlJetCursor cursor = table.lookup(TOKEN_INDEX, token);
 		            
 		            //documents for this token
 		            Vector<Integer> current_docs = new Vector<Integer>();            
 
 		            try {
 		                if (!cursor.eof()) {
 		                    do {
 		                    	current_docs.add((int)cursor.getInteger("doc_id"));
 		                    } while(cursor.next());
 		                }
 		            } finally {
 		                cursor.close();		        
 		            }
 		            
 		            if (!wasInitiallyFilled && documents.size() == 0)
 		            {
 		            	//keep current documents for initial set
 		            	documents = current_docs;
 		            	wasInitiallyFilled = true;
 		            } else {
 		            	//intersect with documents from before (AND)
 		            	HashSet<Integer> current_docs_set = new HashSet<Integer>(current_docs);
 		            	documents.retainAll(current_docs_set);
 		            }
 		            
 				} finally { 
 					db.commit();
 				}
 			}
 		    db.close();
 		    
 		} catch (SqlJetException e) {
 			e.printStackTrace();
 		}
 		
 		if(talkative)
 		{
 			for (Integer doc : documents)
 			{
 				System.out.println(doc);
 			}
 
 			System.out.print("Found "+ documents.size()+ " document(s) matching your query");
 			if (documents.size() == 0) { System.out.println(".");} else { System.out.println(":");}
 			
 		}
 		
 		return documents;
 	}
 
 	
 	public static void phraseQuerySQL(String[] querytokens) {
 		
 		//Vector<Integer> documents = boolQuerySQL(querytokens, false);
 		File dbFile = new File(indexFileDBPath);      
 		//SqlJetDb db;
 		
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		try {			
 			Connection conn =
 			      DriverManager.getConnection("jdbc:sqlite:"+indexFileDBPath);
 			Statement stat = conn.createStatement();
 			//PreparedStatement prep = conn.prepareStatement(
 			//	    		"select * from index_doc where token = (?);");
 		
 			//for(Integer pmid : documents) {
 			// TODO: prepared statement / escaping of tokens
			HashSet<Integer> documents = new HashSet<Integer>(128);
 				try {
					String phraseSearch = StringUtils.join(querytokens, " ");
 					ResultSet rs = stat.executeQuery(
							"select pmid, "+
							"(LENGTH(doc_body) - LENGTH(REPLACE(doc_body, \""+phraseSearch+"\", ''))) / "+phraseSearch.length()+" AS numOccurrences" +
							" from "+ DOCUMENTS_TABLE_NAME +
 							" inner join ( select distinct doc_id from index_doc where token in (\""+
 							StringUtils.join(querytokens, "\", \"")+
							"\")) on pmid = doc_id where doc_body like \"%"+
							phraseSearch+ "%\";");
 					
 					try {
						int count = 0;	// total number of phrase occurrences
 		            	while (rs.next()) {
	            			//System.out.println(rs.getInt("pmid"));
	            			documents.add(rs.getInt("pmid"));
	            			count+=rs.getInt("numOccurrences");
 		                }
		            	System.out.println("Found "+ documents.size() +" document(s) matching your query, containting phrase " + count + " times");
 		            } finally {
 			            rs.close();
 		            }
 				} finally { 
 					//prep.close();
 					conn.close();	
 				}
 			//}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static String readCorpus(String filename) throws IOException {
 		File f = new File(filename);
 		char[] cbuf = new char[(int)f.length()];
 		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
 		in.read(cbuf);
 		return (new String(cbuf));
 	}
 	
 	public static HashMap<String, Vector<MedlineTokenLocation>> buildIndex(String filename) throws IOException
 	{
 		String xml = readCorpus(filename);
 		// don't confuse <MedlineCitationSet> with <MedlineCitation owner=""...>
 		Pattern pCitation = Pattern.compile("<MedlineCitation( .+?)?>(.+?)</MedlineCitation>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pArticleTitle = Pattern.compile("<ArticleTitle.*?>(.+?)</ArticleTitle>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pAbstractText = Pattern.compile("<AbstractText.*?>(.+?)</AbstractText>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pPmid = Pattern.compile("<pmid>(\\d+)</pmid>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Matcher mCitation = pCitation.matcher(xml);
 		
 		String medlineCitation;
 		Matcher mArticleTitle, mAbstractText, mPmid;
 		// naive tokenization: 99084 different tokens
 		HashMap<String, Vector<MedlineTokenLocation>> invertedIndex = new HashMap<String, Vector<MedlineTokenLocation>>(100000, 1.0f);
 		int numPmids = 0;	// 30000 => 2^15 = 32768, important for size of hashset
 		
 		while(mCitation.find())
 		{
 			medlineCitation = mCitation.group(2);
 			mPmid = pPmid.matcher(medlineCitation);
 			if(mPmid.find() == false)
 				continue;
 			numPmids++;
 			int pmid = Integer.parseInt(mPmid.group(1));
 			
 			// search for tokens in <ArticleTitle>
 			mArticleTitle = pArticleTitle.matcher(medlineCitation);
 			while(mArticleTitle.find())
 			{
 				String title = mArticleTitle.group(1).toLowerCase();
 				
 				extractTokens(
 							title,
 							MedlineTokenParentTag.ABSTRACT_TITLE,
 							pmid,
 							invertedIndex
 				);
 			}
 			
 			// search for tokens in <AbstractText>
 			mAbstractText = pAbstractText.matcher(medlineCitation);
 			while(mAbstractText.find())
 			{	
 				String body = mAbstractText.group(1).toLowerCase();
 				extractTokens(
 							body,
 							MedlineTokenParentTag.ABSTRACT_TEXT,
 							pmid,
 							invertedIndex
 				);
 			}
 		}
 
 		//System.out.println(invertedIndex.size());
 		return invertedIndex;
 	}
 	
 	// splits text to tokens and organizes them in a hash map
 	public static void extractTokens(String text, MedlineTokenParentTag parentTag, int pmid, HashMap<String, Vector<MedlineTokenLocation>> invertedIndex)
 	{
 		String[] tokens = text.split(REGEXP_SPLIT_TOKENS);
 		int len = tokens.length;
 		String token;
 		Vector<MedlineTokenLocation> locations;
 		for(int i = 0; i < len; i++)
 		{
 			token = tokens[i];
 			if(token.length() > maxTokenLength)
 				maxTokenLength = token.length();
 			if((locations = invertedIndex.get(token)) != null)
 			{
 				// existing token, add location
 				locations.add(new MedlineTokenLocation(pmid, parentTag));
 			}
 			else
 			{
 				// new token - create first location
 				locations = new Vector<MedlineTokenLocation>();
 				locations.add(new MedlineTokenLocation(pmid, parentTag));
 				invertedIndex.put(token, locations);
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public static void buildSQLIndex(HashMap<String, Vector<MedlineTokenLocation>> invertedIndex, String filename) throws IOException
 	{
 		File dbFile = new File(indexFileDBPath);
         dbFile.delete();       
 		SqlJetDb db;
 
 		// store contents of token index into an SQLite DB
 		try {
 			db = SqlJetDb.open(dbFile, true);
 			db.getOptions().setAutovacuum(true);
 			
 			db.runTransaction(new ISqlJetTransaction() {
 			    public Object run(SqlJetDb db) throws SqlJetException {
 					db.getOptions().setUserVersion(1);
 					return true;
 			    }
 	        }, SqlJetTransactionMode.WRITE);
 			
 			db.beginTransaction(SqlJetTransactionMode.WRITE);
 			try {
 				db.createTable("CREATE TABLE " + INDEX_TABLE_NAME + " (token VARCHAR(128), doc_id INTEGER)");				
 				db.createTable("CREATE TABLE " + DOCUMENTS_TABLE_NAME + " (pmid INTEGER, doc_title TEXT, doc_body TEXT)");
 			} finally {
 				db.commit();
 			}
 						        
 	        //insert token index into db
 	        db.beginTransaction(SqlJetTransactionMode.WRITE);        
 			try {            
 	            ISqlJetTable table = db.getTable(INDEX_TABLE_NAME);
 	            Set<String> keys = invertedIndex.keySet();
 	    		for(String key : keys)
 	    		{
 	    			//put locations in set so we only insert unique pairs
 	    			Vector<MedlineTokenLocation> locations = invertedIndex.get(key);
 	    			HashSet<Integer> locationSet = new HashSet<Integer>(locations.size());
 	    			for ( Iterator<MedlineTokenLocation> i = locations.iterator(); i.hasNext();)
 	    			{    				
 	    				int location = i.next().pmid;
 	    				if(locationSet.add(new Integer(location))) { table.insert(key, location); }
 	    			}
 	    		}
 	    		
 	    		//create after inserts for speed up 
 				db.createIndex("CREATE INDEX " + TOKEN_INDEX + " ON "+ INDEX_TABLE_NAME +" (token)");
				db.createIndex("CREATE INDEX documents_index ON "+ DOCUMENTS_TABLE_NAME +" (pmid)");
 			} finally { db.commit();}		
 
 	        db.close();
 	        
 		} catch (SqlJetException e) {
 			e.printStackTrace();
 		}
 		
 		//also store documents in DB 	
 		//TODO: this is ugly code duplication, documents should be inserted while getting the tokens already
 		
 		String xml = readCorpus(filename);
 		// don't confuse <MedlineCitationSet> with <MedlineCitation owner=""...>
 		Pattern pCitation = Pattern.compile("<MedlineCitation( .+?)?>(.+?)</MedlineCitation>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pArticleTitle = Pattern.compile("<ArticleTitle.*?>(.+?)</ArticleTitle>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pAbstractText = Pattern.compile("<AbstractText.*?>(.+?)</AbstractText>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Pattern pPmid = Pattern.compile("<pmid>(\\d+)</pmid>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
 		Matcher mCitation = pCitation.matcher(xml);
 		
 		String medlineCitation;
 		Matcher mArticleTitle, mAbstractText, mPmid;
 		// naive tokenization: 99084 different tokens
 		
 		Vector<MedlineDocument> fullDocuments = new Vector<MedlineDocument>();
 				
 		try {
 			db = SqlJetDb.open(dbFile, true);
 			
 			while(mCitation.find())
 			{
 				medlineCitation = mCitation.group(2);
 				mPmid = pPmid.matcher(medlineCitation);
 				if(mPmid.find() == false)
 					continue;
 				int pmid = Integer.parseInt(mPmid.group(1));
 				
 				MedlineDocument document = null;
 				
 				mArticleTitle = pArticleTitle.matcher(medlineCitation);
 				while(mArticleTitle.find())
 				{
 					String title = mArticleTitle.group(1).toLowerCase();
 		
 					document = new MedlineDocument(pmid, title, "");
 					fullDocuments.add(document);
 				}
 				
 				mAbstractText = pAbstractText.matcher(medlineCitation);
 				while(mAbstractText.find())
 				{	
 					String body = mAbstractText.group(1).toLowerCase();
 					
 					if (document == null) { document = new MedlineDocument(pmid, "", body); fullDocuments.add(document);}
 					else { 	document.body = body; }			
 				}
 			}
 
 			db.beginTransaction(SqlJetTransactionMode.WRITE);        
 			try {            
 				ISqlJetTable table = db.getTable(DOCUMENTS_TABLE_NAME);
 
 				for(MedlineDocument document: fullDocuments)
 				//if(document != null)
 				{
 					//insert document into db
 		            	    	
 					table.insert(document.pmid, document.title, document.body);
 					
 				}
 			} finally { db.commit();}
 			db.close();
 		} catch (SqlJetException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static void buildCharArrIndex(HashMap<String, Vector<MedlineTokenLocation>> invertedIndex) throws IOException
 	{
 		TreeSet<String> keys = new TreeSet<String>(invertedIndex.keySet());
 		Iterator<String> it = keys.iterator();
 		char[][] tokens = new char[keys.size()][maxTokenLength];
 		int[][] pmidsPerToken = new int[keys.size()][];
 		int tokenIndex = 0;
 		while(it.hasNext())
 		{
 			String key = (String)it.next();
 			tokens[tokenIndex] = key.toCharArray();
 			Vector<MedlineTokenLocation> locations = invertedIndex.get(key);
 			int lsize = locations.size();
 			pmidsPerToken[tokenIndex] = new int[lsize];
 			for (int i = 0; i < lsize; i++)
 			{
 				pmidsPerToken[tokenIndex][i] = locations.get(i).pmid;
 			}
 			tokenIndex++;
 		}
 		
 		FileOutputStream fos = new FileOutputStream(indexFilePath);
 		ObjectOutputStream obj = new ObjectOutputStream(new BufferedOutputStream(fos));
 		obj.writeObject(tokens);
 		obj.writeObject(pmidsPerToken);
 		obj.close();
 		fos.close();
 	}
 	
 	public static void printUsage()
 	{
 		System.out.println("usage:\nAssignment1 -index xmlfile [-sql|-charArr]\nor\nAssignment1 token1 token2 ... [-sql|-charArr]\nor\nAssignment1 \"token1 token2...\" [-sql|-charArr]");
 	}
 }
