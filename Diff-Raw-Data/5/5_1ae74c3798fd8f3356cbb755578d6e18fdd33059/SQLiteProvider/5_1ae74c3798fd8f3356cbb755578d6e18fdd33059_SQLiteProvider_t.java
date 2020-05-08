 /***************************************************************************
     begin........: January 2012
     copyright....: Sebastian Fedrau
     email........: lord-kefir@arcor.de
  ***************************************************************************/
 
 /***************************************************************************
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
  ***************************************************************************/
 package accounting.data.sqlite;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 import org.picocontainer.MutablePicoContainer;
 import accounting.*;
 import accounting.application.*;
 import accounting.application.Currency;
 import accounting.data.*;
 
 public class SQLiteProvider implements IProvider
 {
 	public static long DB_REVISION = 2;
 	
 	private MutablePicoContainer pico;
 	private static boolean initialized = false;
 	private ConnectionPool pool;
 	private String connectionString;
 
 	private class UpgradeUtil
 	{
 		private Connection conn;
 
 		public UpgradeUtil(Connection connection)
 		{
 			this.conn = connection;
 		}
 		
 		public void upgrade() throws ProviderException
 		{
 			long revision;
 			
 			revision = detectVersion();
 
 			try
 			{
 				if(revision < SQLiteProvider.DB_REVISION)
 				{
 					while(revision != SQLiteProvider.DB_REVISION)
 					{
 						if(revision == 0)
 						{
 							upgradeToRevision1();
 						}
 						else if(revision == 1)
 						{
 							upgradeToRevision2();
 						}
 						else
 						{
 							throw new ProviderException(String.format("Found nvalid revision: %d", revision));
 						}
 						
 						revision++;
 					}
 				}
 				else if(revision > SQLiteProvider.DB_REVISION)
 				{
 					throw new ProviderException("Invalid database revision.");
 				}
 			}
 			catch(SQLException e)
 			{
 				throw new ProviderException("Couldn't upgrade database.", e);
 			}
 		}
 		
 		public long detectVersion() throws ProviderException
 		{
 			DatabaseMetaData meta;
 			ResultSet res;
 			Statement stmt;
 			boolean tableExists = false;
 			long version = 0;
 			
 			try
 			{
 				// test if version table exists:
 				meta = conn.getMetaData();
 				res = meta.getTables(null, null, null, new String[] { "TABLE" });
 	
 				while(res.next())
 				{
 					if(res.getString("TABLE_NAME").equals("version"))
 					{
 						tableExists = true;
 						break;
 					}
 				}
 				
 				res.close();
 				
 				// read revision:
 				if(tableExists)
 				{
 					stmt = conn.createStatement();
 					res = stmt.executeQuery("SELECT revision FROM version");
 					
 					if(res.next())
 					{
 						version = res.getLong(1);
 					}
 					
 					res.close();
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				throw new ProviderException("Couldn't detect database revision.", e);
 			}
 
 			return version;
 		}
 
 		void upgradeToRevision1() throws SQLException
 		{
 			Statement stmt;
 			
 			stmt = conn.createStatement();
 			
 			stmt.execute("CREATE TABLE version (revision INTEGER NOT NULL)");
 			stmt.execute("INSERT INTO version (revision) VALUES (1)");	
 		}
 
 		void upgradeToRevision2() throws SQLException
 		{
 			Statement stmt;
 			
 			stmt = conn.createStatement();
 			stmt.execute("CREATE TABLE IF NOT EXISTS exchange_rate (currency_from INTEGER, currency_to INTEGER, ex_rate REAL, PRIMARY KEY(currency_from, currency_to))");
 			stmt.execute("ALTER TABLE template ADD COLUMN currency_id INTEGER");
 			stmt.execute("UPDATE template SET currency_id=(SELECT id FROM currency WHERE deleted=0 LIMIT 1)");
 			stmt.execute("CREATE TEMPORARY TABLE template_backup (id INTEGER PRIMARY KEY, name VARCHAR(32), category_id INT NOT NULL, amount REAL, currency_id INT NOT NULL, remarks VARCHAR(512), deleted INTEGER)");
 			stmt.execute("INSERT INTO template_backup SELECT id, name, category_id, amount, currency_id, remarks, deleted FROM template");
 			stmt.execute("DROP TABLE template");
 			stmt.execute("CREATE TABLE template (id INTEGER PRIMARY KEY, name VARCHAR(32), category_id INT NOT NULL, amount REAL, currency_id INT NOT NULL, remarks VARCHAR(512), deleted INTEGER)");
 			stmt.execute("INSERT INTO template SELECT * FROM template_backup");
 			stmt.execute("DROP TABLE template_backup");
 			stmt.execute("UPDATE version set revision=2");
 		}
 	}
 
 	public SQLiteProvider(String connectionString) throws ClassNotFoundException, SQLException, ProviderException
 	{
 		UpgradeUtil util;
 		Connection conn = null;
 		
 		pico = Injection.getContainer();
 		pool = ConnectionPool.getInstance();
 		this.connectionString = connectionString;
 
 		if(!initialized)
 		{
 			init();
 			
 			createInitialData();
 
 			try
 			{
 				conn = pool.getConnection(connectionString);
 
 				util = new UpgradeUtil(conn);
 				util.upgrade();
 			}
 			catch(Exception e)
 			{
 				throw e;
 			}
 			finally
 			{
 				pool.closeConnection(conn);
 			}			
 		}
 	}
 
 	/*
 	 * currencies:
 	 */
 	@Override
 	public Currency createCurrency(String name) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		Currency currency = null;
 
 		try
 		{
 			currency = pico.getComponent(Currency.class);
 			currency.setName(name);
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "INSERT INTO currency (description, deleted) VALUES (?, 0)", new Object[]{ name });
 			stat.execute();
 			currency.setId(getLastInsertId(conn));
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return currency;
 	}
 
 	@Override
 	public Currency getCurrency(long id) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Currency currency = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT description FROM currency WHERE id=? AND deleted=0", new Object[] { id });
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				currency = pico.getComponent(Currency.class);
 				currency.setId(id);
 				currency.setName(result.getString(1));
 			}
 			else
 			{
 				throw new ProviderException("Couldn't find currency.");
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return currency;
 	}
 
 	@Override
 	public void updateCurrency(Currency currency) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			prepareAndExecuteStatement(conn, "UPDATE currency SET description=? WHERE id=?", new Object[]{ currency.getName(), currency.getId() });
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 
 	@Override
 	public void deleteCurrency(long id) throws ProviderException
 	{
 		deleteEntity("currency", id);
 	}
 
 	@Override
 	public List<Currency> getCurrencies() throws ProviderException
 	{
 		Connection conn = null;
 		Statement stat;
 		ResultSet result;
 		List<Currency> currencies = new LinkedList<Currency>();
  
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = conn.createStatement();
 			result = stat.executeQuery("SELECT id FROM currency WHERE deleted=0 ORDER BY description");
 	
 			while(result.next())
 			{
 				currencies.add(getCurrency(result.getLong(1)));
 			}
 	
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 
 		return currencies;
 	}
 
 	@Override
 	public int countCurrencyReferences(long id) throws ProviderException
 	{
 		Object result;
 		int refs = 0;
 
 		if((result = executeScalar("SELECT COUNT(id) FROM account WHERE account.deleted=0 AND currency_id=?", new Object[]{ id })) != null)
 		{
 			refs = (Integer)result;
 		}
 
 		if((result = executeScalar("SELECT COUNT(id) FROM template WHERE template.deleted=0 AND currency_id=?", new Object[]{ id })) != null)
 		{
 			refs += (Integer)result;
 		}
 
 		return refs;
 	}
 
 	/*
 	 * categories:
 	 */
 	@Override
 	public Category createCategory(String name, boolean expenditure) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		Category category = null;
 
 		try
 		{
 			category = pico.getComponent(Category.class);
 			category.setName(name);
 			category.setExpenditure(expenditure);
 			
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "INSERT INTO category (description, expenditure, deleted) VALUES (?, ?, 0)", new Object[]{ name, expenditure });
 			stat.execute();
 			category.setId(getLastInsertId(conn));
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return category;
 	}
 
 	@Override
 	public Category getCategory(long id) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Category category = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT description, expenditure FROM category WHERE id=? AND deleted=0", new Object[] { id });
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				category = pico.getComponent(Category.class);
 				category.setId(id);
 				category.setName(result.getString(1));
 				category.setExpenditure(result.getBoolean(2));
 			}
 			else
 			{
 				throw new ProviderException("Couldn't find category.");
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return category;
 	}
 
 	@Override
 	public void updateCategory(Category category) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			prepareAndExecuteStatement(conn, "UPDATE category SET description=?, expenditure=? WHERE id=?", new Object[]{ category.getName(), category.isExpenditure(), category.getId() });
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 
 	@Override
 	public void deleteCategory(long id) throws ProviderException
 	{
 		deleteEntity("category", id);
 	}
 
 	@Override
 	public List<Category> getCategories(boolean expenditure) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		List<Category> categories = new LinkedList<Category>();
  
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT id FROM category WHERE deleted=0 AND expenditure=? ORDER BY \"name\"", new Object[] { expenditure });
 			result = stat.executeQuery();
 	
 			while(result.next())
 			{
 				categories.add(getCategory(result.getLong(1)));
 			}
 	
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 
 		return categories;
 	}
 
 	@Override
 	public int countCategoryReferences(long id) throws ProviderException
 	{
 		Object result;
 		int refs = 0;
 
 		if((result = executeScalar("SELECT COUNT(id) FROM record WHERE deleted=0 AND category_id=?", new Object[]{ id })) != null)
 		{
 			refs = (Integer)result;
 		}
 
 		if((result = executeScalar("SELECT COUNT(id) FROM template WHERE deleted=0 AND category_id=?", new Object[]{ id })) != null)
 		{
 			refs = (Integer)result;
 		}
 
 		return refs;
 	}
 
 	@Override
 	public int countCategories(boolean expenditure) throws ProviderException
 	{
 		Object result;
 
 		if((result = executeScalar("SELECT COUNT(id) FROM category WHERE deleted=0 AND expenditure=?", new Object[]{ expenditure })) != null)
 		{
 			return (Integer)result;
 		}
 
 		return 0;
 	}
 
 	/*
 	 * accounts:
 	 */
 	@Override
 	public Account createAccount(String name, String remarks, Currency currency, String noPrefix, int currentNo) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		Account account = null;
 
 		try
 		{
 			account = pico.getComponent(Account.class);
 			account.setName(name);
 			account.setRemarks(remarks);
 			account.setCurrency(currency);
 			account.setNoPrefix(noPrefix);
 			account.setCurrentNo(currentNo);
 			
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "INSERT INTO account (name, remarks, currency_id, no_prefix, no_current, deleted) VALUES (?, ?, ?, ?, ?, 0)",
 					                       new Object[]{ name, remarks, currency == null ? null : currency.getId(), noPrefix, currentNo });
 			stat.execute();
 			account.setId(getLastInsertId(conn));
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return account;
 	}
 
 	@Override
 	public Account getAccount(long id) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Account account = null;
 		Currency currency = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT account.name, remarks, no_prefix, no_current, currency_id, description FROM account LEFT JOIN currency ON account.currency_id=currency.id WHERE account.id=? AND account.deleted=0", new Object[] { id });
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				account = pico.getComponent(Account.class);
 				account.setId(id);
 				account.setName(result.getString(1));
 				account.setRemarks(result.getString(2));
 				account.setNoPrefix(result.getString(3));
 				account.setCurrentNo(result.getInt(4));
 
 				if(result.getObject(5) != null)
 				{
 					currency = pico.getComponent(Currency.class);
 					currency.setId(result.getLong(5));
 					currency.setName(result.getString(6));
 					account.setCurrency(currency);
 				}
 			}
 			else
 			{
 				throw new ProviderException("Couldn't find account.");
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return account;
 	}
 
 	@Override
 	public void updateAccount(Account account) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			prepareAndExecuteStatement(conn, "UPDATE account SET name=?, remarks=?, currency_id=?, no_prefix=?, no_current=? WHERE id=?",
 					                         new Object[]{ account.getName(), account.getRemarks(), account.getCurrency() == null ? null : account.getCurrency().getId(), account.getNoPrefix(), account.getCurrentNo(), account.getId() });
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 
 	@Override
 	public void deleteAccount(long id) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			conn.setAutoCommit(false);
 
 			stat = prepareStatement(conn, "UPDATE account SET deleted=1 WHERE id=?", new Object[]{ id });
 			stat.execute();
 			stat.close();
 
 			stat = prepareStatement(conn, "UPDATE record SET deleted=1 WHERE account_id=?", new Object[]{ id });
 			stat.execute();
 			stat.close();
 
 			conn.commit();
 		}
 		catch(SQLException e0)
 		{
 			try
 			{
 				conn.rollback();
 			}
 			catch(Exception e1) { }
 			throw new ProviderException(e0);
 		}
 		finally
 		{
 			try
 			{
 				conn.setAutoCommit(true);
 			}
 			catch(Exception e) { };
 			pool.closeConnection(conn);
 		}
 	}
 
 	@Override
 	public List<Account> getAccounts() throws ProviderException
 	{
 		Connection conn = null;
 		Statement stat;
 		ResultSet result;
 		LinkedList<Account> accounts = new LinkedList<Account>();
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = conn.createStatement();
 			result = stat.executeQuery("SELECT id FROM account WHERE deleted=0 ORDER BY name");
 
 			while(result.next())
 			{
 				accounts.add(getAccount(result.getLong(1)));
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return accounts;
 	}
 
 	/*
 	 * transactions:
 	 */
 	@Override
 	public Transaction createTransaction(Account account, Category category, Date date, Double amount, String no, String remarks) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		Transaction transaction = null;
 
 		try
 		{
 			transaction = pico.getComponent(Transaction.class);
 			transaction.setAccount(account);
 			transaction.setCategory(category);
 			transaction.setDate(date);
 			transaction.setAmount(amount);
 			transaction.setRemarks(remarks);
 			transaction.setNo(no);
 			
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "INSERT INTO record (account_id, category_id, date, amount, no, remarks, deleted) VALUES (?, ?, ?, ?, ?, ?, 0)",
 			                              new Object[]{ account.getId(), category.getId(), date, category.isExpenditure() ? amount * -1 : amount, no, remarks });
 			stat.execute();
 			transaction.setId(getLastInsertId(conn));
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return transaction;
 	}
 
 	@Override
 	public Transaction getTransaction(long id, Account account) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Transaction transaction = null;
 		Category category;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT record.\"date\", record.amount, record.no, record.remarks, category_id, category.description, category.expenditure " +
 			                              "FROM record INNER JOIN category ON category_id=category.id WHERE record.deleted=0 AND record.id=?", new Object[] { id });
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				transaction = pico.getComponent(Transaction.class);
 				transaction.setId(id);
 				transaction.setDate(new Date(result.getLong(1)));
 				transaction.setAmount(result.getDouble(2));
 				transaction.setNo(result.getString(3));
 				transaction.setRemarks(result.getString(4));
 
 				category = pico.getComponent(Category.class);
 				category.setId(result.getLong(5));
 				category.setName(result.getString(6));
 				category.setExpenditure(result.getBoolean(7));
 				transaction.setCategory(category);
 
 				transaction.setAccount(account);
 			}
 			else
 			{
 				throw new ProviderException("Couldn't find transaction.");
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return transaction;
 	}
 
 	@Override
 	public List<Transaction> getTransactions(Account account, long begin, long end) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		LinkedList<Transaction> transactions = new LinkedList<Transaction>();
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT id FROM record WHERE deleted=0 AND account_id=? AND \"date\">=? AND \"date\"<=?", new Object[] { account.getId(), begin, end  });
 			result = stat.executeQuery();
 
 			while(result.next())
 			{
 				transactions.add(getTransaction(result.getLong(1), account));
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return transactions;
 	}
 
 	@Override
 	public void updateTransaction(Transaction transaction) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			prepareAndExecuteStatement(conn, "UPDATE record SET account_id=?, category_id=?, \"date\"=?, amount=?, remarks=?, no=? WHERE id=?",
 			                                 new Object[]{ transaction.getAccount().getId(), transaction.getCategory().getId(), transaction.getDate(),
                                                            transaction.getCategory().isExpenditure() ? transaction.getRebate() * -1 : transaction.getIncome(),
                                                            transaction.getRemarks(), transaction.getNo(), transaction.getId() });
 
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 
 	@Override
 	public void deleteTransaction(long id) throws ProviderException
 	{
 		deleteEntity("record", id);
 	}
 
 	@Override
 	public List<Long> getTimestamps(long accountId) throws ProviderException
 	{
 		Connection conn = null;
 		Statement stat;
 		ResultSet result;
 		List<Long> timestamps = new LinkedList<Long>();
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = conn.createStatement();
 			result = stat.executeQuery("SELECT \"date\" FROM record WHERE deleted=0 ORDER BY \"date\" DESC");
 
 			while(result.next())
 			{
 				timestamps.add(result.getLong(1));
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return timestamps;
 	}
 
 	/*
 	 * balances:
 	 */
 	@Override
 	public double getBalance(long accountId, Date date) throws ProviderException
 	{
 		Object result;
 
 		if((result = executeScalar("SELECT SUM(amount) FROM record WHERE deleted=0 AND account_id=? AND \"date\"<?", new Object[]{ accountId, date.getTime() })) != null)
 		{
 			return (Double)result;
 		}
 
 		return 0;
 	}
 
 	/*
 	 * transactions nos:
 	 */
 	@Override
 	public boolean transactionNoExists(String no) throws ProviderException
 	{
 		Object result;
 
 		if((result = executeScalar("SELECT COUNT(id) FROM record WHERE deleted=0 AND no=?", new Object[]{ no })) != null)
 		{
 			if((int)result > 0)
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/*
 	 * templates:
 	 */
 	@Override
 	public Template createTemplate(String name, Category category, Double amount, Currency currency, String remarks) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		Template template = null;
 
 		try
 		{
 			template = pico.getComponent(Template.class);
 			template.setName(name);
 			template.setCategory(category);
 			template.setAmount(amount);
 			template.setRemarks(remarks);
 
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "INSERT INTO template (name, category_id, amount, currency_id, remarks, deleted) VALUES (?, ?, ?, ?, ?, 0)",
 			                              new Object[]{ name, category.getId(), category.isExpenditure() ? amount * -1 : amount, currency.getId(), remarks });
 			stat.execute();
 			template.setId(getLastInsertId(conn));
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return template;
 	}
 	
 	@Override
 	public Template getTemplate(long id) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Template template = null;
 		Category category;
 		Currency currency;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, "SELECT template.name, template.amount, template.remarks, category_id, category.description, category.expenditure, currency.id, currency.description " +
 			                              "FROM template " +
 					                      "INNER JOIN category ON category_id=category.id " +
 					                      "INNER JOIN currency ON currency.id=currency_id WHERE template.deleted=0 AND template.id=?", new Object[] { id });
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				template = pico.getComponent(Template.class);
 				template.setId(id);
 				template.setName(result.getString(1));
 				template.setAmount(result.getDouble(2));
 				template.setRemarks(result.getString(3));
 
 				category = pico.getComponent(Category.class);
 				category.setId(result.getLong(4));
 				category.setName(result.getString(5));
 				category.setExpenditure(result.getBoolean(6));
 				template.setCategory(category);
 				
 				currency = pico.getComponent(Currency.class);
 				currency.setId(result.getLong(7));
 				currency.setName(result.getString(7));
 				template.setCurrency(currency);
 			}
 			else
 			{
 				throw new ProviderException("Couldn't find template.");
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		catch(AttributeException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return template;
 	}
 	
 	@Override
 	public void updateTemplate(Template template) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			prepareAndExecuteStatement(conn, "UPDATE template SET name=?, category_id=?, amount=?, currency_id=?, remarks=? WHERE id=?",
                     new Object[]{ template.getName(), template.getCategory().getId(),
 					              template.getCategory().isExpenditure() ? template.getRebate() * -1 : template.getIncome(),
 					              template.getCurrency().getId(), template.getRemarks(), template.getId() });
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 	
 	@Override
 	public void deleteTemplate(long id) throws ProviderException
 	{
 		deleteEntity("template", id);
 	}
 	
 	@Override
 	public List<Template> getTemplates() throws ProviderException
 	{
 		Connection conn = null;
 		Statement stat;
 		ResultSet result;
 		LinkedList<Template> templates = new LinkedList<Template>();
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = conn.createStatement();
 			result = stat.executeQuery("SELECT id FROM template WHERE deleted=0 ORDER BY name");
 
 			while(result.next())
 			{
 				templates.add(getTemplate(result.getLong(1)));
 			}
 
 			result.close();
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return templates;
 	}
 
 	@Override
 	public boolean exchangeRateExists(Currency from, Currency to) throws ProviderException
 	{
 		Object result;
 		
 		result = executeScalar("SELECT COUNT(ex_rate) FROM exchange_rate WHERE (currency_from=? AND currency_to=?) OR (currency_from=? AND currency_to=?)",
 		                        new Object[] { from.getId(), to.getId(), to.getId(), from.getId() });
 		
 		if((Integer)result == 0)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public double getExchangeRate(Currency from, Currency to) throws ProviderException
 	{
 		Object result;
 
 		result = executeScalar("SELECT ex_rate FROM exchange_rate WHERE currency_from=? AND currency_to=?", new Object[] { from.getId(), to.getId() });

 		if(result != null)
 		{
 			return (Double)result;
 		}
 		
 		result = executeScalar("SELECT ex_rate FROM exchange_rate WHERE currency_from=? AND currency_to=?", new Object[] { to.getId(), from.getId() });
 		
		if(result != null && (Double)result > 0.0)
 		{
 			return 1.0 / (Double)result;
 		}
 		
 		return 0.0;
 	}
 	
 	@Override
 	public void updateExchangeRate(Currency from, Currency to, double rate) throws ProviderException
 	{
 		Connection conn = null;
 		
 		try
 		{
 			conn = pool.getConnection(connectionString);
 
 			prepareAndExecuteStatement(conn, "DELETE FROM exchange_rate WHERE currency_from=? AND currency_to=?", new Object[] { to.getId(), from.getId() });
 			prepareAndExecuteStatement(conn, "REPLACE INTO exchange_rate (currency_from, currency_to, ex_rate) VALUES (?, ?, ?)", new Object[] { from.getId(), to.getId(), rate });
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new ProviderException("Couldn't update exchange rate.", e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 
 	/*
 	 *	database initialization:
 	 */
 	private void init() throws ClassNotFoundException, SQLException
 	{
 		Connection conn;
 		Statement stat;
 
 		// load JDBC driver:
 		Class.forName("org.sqlite.JDBC");
 	
 		// create tables:
 		conn = pool.getConnection(connectionString);
 		stat = conn.createStatement();
 		stat.execute("CREATE TABLE IF NOT EXISTS account (id INTEGER PRIMARY KEY, name VARCHAR(32) NOT NULL, remarks VARCHAR(512), no_prefix VARCHAR(32), no_current INTEGER, currency_id INT, deleted INTEGER)");
 		stat.execute("CREATE TABLE IF NOT EXISTS currency (id INTEGER PRIMARY KEY, description VARCHAR(32) NOT NULL, deleted INTEGER)");
 		stat.execute("CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY, description VARCHAR(32) NOT NULL, expenditure BIT, deleted INTEGER)");
 		stat.execute("CREATE TABLE IF NOT EXISTS record (id INTEGER PRIMARY KEY, account_id INT NOT NULL, category_id INT NOT NULL, date INT, amount REAL, no VARCHAR(32), remarks VARCHAR(512), deleted INTEGER)");
 		stat.execute("CREATE TABLE IF NOT EXISTS template (id INTEGER PRIMARY KEY, name VARCHAR(32), category_id INT NOT NULL, amount REAL, remarks VARCHAR(512), deleted INTEGER)");
 
 		// close connection:
 		stat.close();
 		pool.closeConnection(conn);
 
 		// set success state:
 		initialized = true;
 	}
 
 	private void createInitialData() throws ProviderException
 	{
 		Translation translation = new Translation();
 		Currency currency;
 
 		if(((Integer)executeScalar("SELECT COUNT(id) FROM account", null)) == 0)
 		{
 			currency = createCurrency("Euro");
 			createAccount(translation.translate("Cash"), "", currency, null, 1);
 			createCategory(translation.translate("Books"), true);
 			createCategory(translation.translate("Car"), true);
 			createCategory(translation.translate("Clothes"), true);
 			createCategory(translation.translate("Computer"), true);
 			createCategory(translation.translate("Entertainment"), true);
 			createCategory(translation.translate("Gifts"), true);
 			createCategory(translation.translate("Hobbies"), true);
 			createCategory(translation.translate("Insurance"), true);
 			createCategory(translation.translate("Miscellaneous"), true);
 			createCategory(translation.translate("Phone"), true);
 			createCategory(translation.translate("Salary"), false);
 			createCategory(translation.translate("Gifts received"), false);
 		}
 	}
 
 	/*
 	 *	JDBC helpers:
 	 */
 	private PreparedStatement prepareStatement(Connection connection, String query, Object[] args) throws SQLException
 	{
 		PreparedStatement stat;
 
 		stat = connection.prepareStatement(query);
 
 		if(args != null)
 		{
 			for(int i = 0; i < args.length; ++i)
 			{
 				if(args[i] == null)
 				{
 					stat.setObject(i + 1, null);
 				}
 				if(args[i] instanceof Long)
 				{
 					stat.setLong(i + 1, (Long)args[i]);
 				}
 				if(args[i] instanceof Integer)
 				{
 					stat.setInt(i + 1, (Integer)args[i]);
 				}
 				else if(args[i] instanceof String)
 				{
 					stat.setString(i + 1, (String)args[i]);
 				}
 				else if(args[i] instanceof Boolean)
 				{
 					stat.setInt(i + 1, (Boolean)args[i] ? 1 : 0);
 				}
 				else if(args[i] instanceof Date)
 				{
 					stat.setLong(i + 1, ((Date)args[i]).getTime());
 				}
 				else if(args[i] instanceof Double)
 				{
 					stat.setDouble(i + 1, (Double)args[i]);
 				}
 			}
 		}
 
 		return stat;
 	}
 
 	private Object executeScalar(String query, Object[] args) throws ProviderException
 	{
 		Connection conn = null;
 		PreparedStatement stat;
 		ResultSet result;
 		Object value = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);
 			stat = prepareStatement(conn, query, args);
 			result = stat.executeQuery();
 
 			if(result.next())
 			{
 				value = result.getObject(1);
 			}
 
 			stat.close();
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 
 		return value;
 	}
 
 	private void prepareAndExecuteStatement(Connection connection, String query, Object[] args) throws SQLException
 	{
 		PreparedStatement stat;
 
 		stat = prepareStatement(connection, query, args);
 		stat.execute();
 		stat.close();
 	}
 
 	private long getLastInsertId(Connection conn) throws SQLException, ProviderException
 	{
 		Statement stat = null;
 		ResultSet result = null;
 
 		try
 		{
 			stat = conn.createStatement();
 			result = stat.executeQuery("SELECT LAST_INSERT_ROWID()");
 	
 			if(result.next())
 			{
 				return result.getLong(1);
 			}
 		}
 		catch(SQLException e)
 		{
 			throw e;
 		}
 		finally
 		{
 			if(stat != null)
 			{
 				stat.close();
 			}
 			
 			if(result != null)
 			{
 				result.close();
 			}
 		}
 
 		throw new ProviderException("Couldn't get last insert id.");
 	}
 
 	/*
 	 * helpers:
 	 */
 	private void deleteEntity(String table, long id) throws ProviderException
 	{
 		Connection conn = null;
 
 		try
 		{
 			conn = pool.getConnection(connectionString);;
 			prepareAndExecuteStatement(conn, "UPDATE \"" + table + "\" SET deleted=1 WHERE id=?", new Object[]{ id });
 		}
 		catch(SQLException e)
 		{
 			throw new ProviderException(e);
 		}
 		finally
 		{
 			pool.closeConnection(conn);
 		}
 	}
 }
