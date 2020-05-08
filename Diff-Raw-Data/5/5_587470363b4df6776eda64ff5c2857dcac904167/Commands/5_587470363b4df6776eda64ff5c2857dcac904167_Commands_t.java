 
 
 /**
  * Has different commands that should be called from the main method.
  * 
  * @author Daniel
  *
  */
 public class Commands {
 	
 	private static String dbName; //DB name
 	private static String user; //DB username
 	private static String pwd; //DB password
 	private static final String emptyGenderCond = 
 			"ct_genre IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')";
 	private static final String emptyFirstnameCond = 
 			"ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')";
 	private static final String emptyLastnameCond = 
 			"ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')";
 	public static final String emptyEmailCond = 
 			"ct_mail IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', '84/12/39/44', " +
 			"'05/65/77/85/37', 'z', 'Z', 'nc', 'NC', 'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 			"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', '01.01.1981', 'fr', " +
 			"'REMPLACE MME SUINOT EN MALADIE', '01 47 68 12 63', '661261090', 'b', '603707107', " +
 			"'pas d\\'adresse e-mail', '630108886', 'pas adresse', 'aucune', '664998228', " +
 			"'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer')";
 	private static final String emptyRowsCond = 
 			"("+emptyFirstnameCond+") AND ("+emptyLastnameCond+") AND ("+emptyEmailCond+")"; 
 	
 //	private static final String emptyRowsCondFull = 
 //			"(ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 //			"AND (ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 //			"AND (ct_mail IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC'," +
 //			"'84/12/39/44', '05/65/77/85/37',  'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 //			"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', '01.01.1981', 'fr', " +
 //			"'REMPLACE MME SUINOT EN MALADIE', '01 47 68 12 63', '661261090', 'b', '603707107', " +
 //			"'pas d\\'adresse e-mail', '630108886', 'pas adresse', 'aucune', '664998228', " +
 //			"'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer'))";
 	
 	/**
 	 * Has different commands that should be called from the main method.
 	 * 
 	 * @param dbName the url with the database name
 	 * @param user the username
 	 * @param pwd the password
 	 */
 	public Commands(String dbName, String user, String pwd) {
 		super();
 		Commands.dbName = dbName;
 		Commands.user = user;
 		Commands.pwd = pwd;
 	}
 	
 	public void createMoreClref(String sourceTable, String clrefDomainsTable) {
 		Select select = new Select(dbName, user, pwd);
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		InsertRows insert = new InsertRows(dbName, user, pwd);
 //		int totalClref = counter.countDistinct("cl_ref", sourceTable, null);
 		int[] companies = select.selectIntFromWhere("cl_ref", sourceTable, null);
 		String[] domains = select.selectStringFromWhere("domaines", sourceTable, null);
 		
 		System.out.println("Total cl_ref entries in table "+sourceTable+" : "+companies.length);
 		System.out.println("Total domaines entries in table "+sourceTable+" : "+domains.length);
 		
 		if (duplicates(companies)) {
 			System.out.println("There are some duplicate cl_refs.");
 		}else{
 			System.out.println("There are not any duplicate cl_refs.");
 		}
 		
 		int insertedNewClref = 0;
 	    for(int i=0; i < companies.length; i++){
 	    	if(counter.countFromWhere("isp_domains_2", "`name`='"+domains[i]+"'")==0){
	    		if(counter.countFromWhere(clrefDomainsTable, "cl_ref="+companies[i])==0){
 	    			domains[i] = domains[i].replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 	    			insert.insertRow(clrefDomainsTable, "`cl_ref_original`, `nom_domaine`", "'"+companies[i]+"', '"+domains[i]+"'");
 	    			insertedNewClref++;
	    		} else if ((counter.countFromWhere(clrefDomainsTable, "(`cl_ref`='"+companies[i]+"' AND `nom_domaine`='"+domains[i]+"')")==0)) {
 	    			domains[i] = domains[i].replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 	    			insert.insertRow(clrefDomainsTable, "`cl_ref`, `cl_ref_original`, `nom_domaine`", "'"+companies[i]+"', '"+companies[i]+"', '"+domains[i]+"'");
 	    		}
 	    	}
 	    }
 	    System.out.println("Inserted new cl_ref : "+insertedNewClref);
 	}
 	
 	public static boolean duplicates(final int[] myArray)
 	{
 	   final int MAXZIP = 99999;
 	   boolean[] bitmap = new boolean[MAXZIP+1];  // Java guarantees init to false
 	   for (int item : myArray)
 	     if (!(bitmap[item] ^= true)) return true;
 	   return false;
 	}
 	
 	/**
 	 * Writes the number of users of each domain into the domains table.
 	 * 
 	 * @param sourceTable  the table with a list of all users
 	 * @param ispDomainsTable the table with a list of all domains
 	 */
 	public void updateIspDomainUsersNumber(String sourceTable, String ispDomainsTable) {
 		Select select = new Select(dbName, user, pwd);
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		Modify modify = new Modify(dbName, user, pwd);
 		int totalDomains = counter.countAll(ispDomainsTable);
 		
 		for(int i=0;i<totalDomains;i++){
 			String whereId = "id="+(i+1);
 			String domainName = select.selectField("name", ispDomainsTable, whereId);
 			domainName = domainName.replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 			String whereDomainName="domaines='"+domainName+"'";
 			int totalUsers = counter.countFromWhere(sourceTable, whereDomainName);
 			modify.modifyWhere(ispDomainsTable, whereId, "number_of_users", Integer.toString(totalUsers));
 		}
 		
 	}
 	
 	public void countDomainsWithOneAddress(String sourceTable, String domainsTable) {
 		Select select = new Select(dbName, user, pwd);
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		int totalDomains = counter.countAll(domainsTable);
 		int result=0;
 		for(int i=0;i<totalDomains;i++){
 			String whereId = "id="+(i+1);
 			String domainName = select.selectField("name", domainsTable, whereId);
 			domainName = domainName.replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 			String whereDomainName="domaines='"+domainName+"'";
 			int totalEmails = counter.countFromWhere(sourceTable, whereDomainName);
 			if(totalEmails==1){
 				result++;
 			}
 		}
 		System.out.println("Total domains in "+domainsTable+
 				" with one address in "+sourceTable+" : "+result);
 	}
 	
 	/**
 	 * Counts distinct values in a column of a given table. 
 	 * 
 	 * @param table the table to count from
 	 * @param column the column whose distinct values should be counted
 	 */
 	public void countDistinct(String table, String column) {
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		int distinctValues = counter.countDistinct(column, table, null);
 		System.out.println("Dinstinct values in column "+column+" of table "+table+" : "+distinctValues);
 	}
 	
 	/**
 	 * Counts domains with a given number of users.
 	 * 
 	 * @param table the table with the list of domains
 	 * @param numberOfUsers the number of users to check
 	 */
 	public void countDomainsWithXCompanies(String table, int numberOfCompanies) {
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		String where = "number_of_companies="+numberOfCompanies;
 		int result = counter.countFromWhere(table, where);
 		System.out.println("Number of domains (in table "+table+") with "+numberOfCompanies+" companies: "+result);
 	}
 	
 	/**
 	 * Writes the number of distinct companies of each domain into the domains table.
 	 * 
 	 * @param sourceTable  the table with a list of all users
 	 * @param domainsTable the table with a list of all domains
 	 */
 	public void updateDomainCompaniesNumber(String sourceTable, String domainsTable){
 		Select select = new Select(dbName, user, pwd);
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		Modify modify = new Modify(dbName, user, pwd);
 		int totalDomains = counter.countAll(domainsTable);
 		for(int i=0;i<totalDomains;i++){
 			String whereId = "id="+(i+1);
 			String domainName = select.selectField("name", domainsTable, whereId);
 			domainName = domainName.replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 			String whereDomainName="domaines='"+domainName+"'";
 			int totalUsers = counter.countDistinct("cl_ref", sourceTable, whereDomainName);
 			modify.modifyWhere(domainsTable, whereId, "number_of_companies", Integer.toString(totalUsers));
 		}
 	}
 	
 	/**
 	 * Counts domains with a given number of users.
 	 * 
 	 * @param table the table with the list of domains
 	 * @param numberOfUsers the number of users to check
 	 */
 	public void countDomainsWithXUsers(String table, int numberOfUsers) {
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		String where = "number_of_users="+numberOfUsers;
 		int result = counter.countFromWhere(table, where);
 		System.out.println("Number of domains (in table "+table+") with "+numberOfUsers+" users: "+result);
 	}
 	
 	/**
 	 * Writes the number of users of each domain into the domains table.
 	 * 
 	 * @param sourceTable  the table with a list of all users
 	 * @param domainsTable the table with a list of all domains
 	 */
 	public void updateDomainUsersNumber(String sourceTable, String domainsTable) {
 		Select select = new Select(dbName, user, pwd);
 		RowCounter counter = new RowCounter(dbName, user, pwd);
 		Modify modify = new Modify(dbName, user, pwd);
 		int totalDomains = counter.countAll(domainsTable);
 		for(int i=0;i<totalDomains;i++){
 			String whereId = "id="+(i+1);
 			String domainName = select.selectField("name", domainsTable, whereId);
 			domainName = domainName.replaceAll("'", "\\\\'"); // the char ' is replaced with \'
 			String whereDomainName="domaines='"+domainName+"'";
 			int totalUsers = counter.countFromWhere(sourceTable, whereDomainName);
 			modify.modifyWhere(domainsTable, whereId, "number_of_users", Integer.toString(totalUsers));
 		}
 		
 	}
 	
 	public void cloneTable(String source, String cloned){
 		CopyTable copy = new CopyTable(dbName, user, pwd);
 		copy.copyTableFromWhere(source, cloned, null);
 	}
 	
 	/**
 	 * Copies distinct domains to a new table
 	 * 
 	 * @param sourceTable the table to take empty rows from
 	 * @param newTable the table to be created with the same structure and with the empty rows copied
 	 */
 	public void insertDistinctDomains(String sourceTable, String newTable){
 		InsertRows insert = new InsertRows(dbName, user, pwd);
 		insert.insertDistinct(sourceTable, newTable, "domaines", "name");
 	}
 	
 	/**
 	 * Copies empty rows to a new table.
 	 * 
 	 * @param sourceTable the table to take empty rows from
 	 * @param newTable the table to be created with the same structure and with the empty rows copied
 	 */
 	public void copyEmpty(String sourceTable, String newTable){
 		
 		//condition for copying
 		String where = emptyRowsCond; 
 		CopyTable copy = new CopyTable(dbName, user, pwd);
 		copy.copyTableFromWhere(sourceTable, newTable, where);
 	}
 	
 	/**
 	 * Counts all rows from a table.
 	 * 
 	 * @param table the table whose rows to count
 	 */
 	public void countAll(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//no condition, select all
 //		String where = ""; 
 //		boolean printRows = false;
 		
 		System.out.println("Total rows in "+table+" : " 
 				+ counter.countAll(table));
 		
 	}
 	
 	/**
 	 * Counts the number of rows without gender from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countEmptyGender(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = emptyGenderCond; 
 		
 		System.out.println("Total rows with empty gender in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 	
 	/**
 	 * Counts the number of rows without email from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countEmptyEmail(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = emptyEmailCond; 
 		
 		
 		System.out.println("Total rows with empty email in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 	
 	/**
 	 * Counts the number of rows without first name from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countEmptyLastname(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = emptyLastnameCond; 
 		
 		System.out.println("Total rows with empty last name in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 	
 	/**
 	 * Counts the number of rows without last name from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countEmptyFirstname(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = emptyFirstnameCond; 
 		
 		System.out.println("Total rows with empty first name in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 
 	/**
 	 * Prints clients with empty <b>first name</b>, <b>family name</b> and <b>email</b> at the same time. 
 	 * <br />Needs a table which is a version of bd_emails and has at least the columns 
 	 * <b>ct_prenom</b>, <b>ct_nom</b> and <b>ct_mail</b>.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countEmpty(String table){
 		
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = emptyRowsCond; 
 		
 		System.out.println("Total empty rows in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 
 	/**
 	 * Prints clients with empty <b>first name</b>, <b>family name</b> and <b>email</b> at the same time. 
 	 * <br />Needs a table which is a version of bd_emails and has at least the columns 
 	 * <b>ct_prenom</b>, <b>ct_nom</b> and <b>ct_mail</b>.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	public void countNonEmpty(String table){
 		
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = "NOT ("+emptyRowsCond+")";
 		
 		System.out.println("Total non-empty rows in "+table+" : " 
 				+ counter.countFromWhere(table, where));
 	}
 	
 	/**
 	 * Gives total columns number and prints each column's name of a given table from the database. 
 	 * 
 	 * @param table the table whose columns should be taken into consideration
 	 */
 	public void showColumnNames(String table){
 		Select select = new Select(dbName, user, pwd);
 		String[] columnsList = select.getColumnsAsArray(table);
 		System.out.println("Total number of columns in "+table+" : " + (columnsList.length-1));
 		for (int i=1;i<columnsList.length;i++){
 			System.out.println("Column " + i + " : " + columnsList[i]);
 		}
 	}
 	
 	/**
 	 * Takes non-empty clients and writes them to a new table, thus avoiding deletion.
 	 * <br />Should be run once per destination table 
 	 * 
 	 * @param fromTable the source table to takes values from
 	 * @param toTable the destination table to write values into
 	 */
 	public void copyNonEmpty(String fromTable, String toTable){
 		
 		//we define the condition to select specific rows and insert them in another table
 		String where = "NOT ("+emptyRowsCond+")"; 
 		
 		CopyTable copy = new CopyTable(dbName, user, pwd);
 		copy.copyTableFromWhere(fromTable, toTable, where);
 		
 	}
 	
 	/**
 	 * Takes empty clients and writes them to a new table, allowing export of empty rows.
 	 * <br />Should be <b>run once</b> per destination table.
 	 * 
 	 * @param fromTable
 	 * @param toTable
 	 */
 	public void insertEmpty(String fromTable, String toTable){
 		
 		//we define the condition to select specific rows and insert them in another table
 		String where = emptyRowsCond; 
 		
 		InsertRows insert = new InsertRows(dbName, user, pwd);
 		insert.copyRowsFromWhere(fromTable, toTable, where);
 		
 	}
 	
 }
