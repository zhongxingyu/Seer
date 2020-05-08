 package yapto.datasource.sqlfile;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 
 import yapto.datasource.IDataSource;
 import yapto.datasource.IPicture;
 import yapto.datasource.sqlfile.config.ISQLFileDataSourceConfiguration;
 import yapto.datasource.tag.Tag;
 
 /**
  * Object holding the connection to the database and the prepared statements.
  * 
  * @author benobiwan
  * 
  */
 public final class SQLFileListConnection
 {
 	/**
 	 * Configuration for this {@link SQLFileDataSource}.
 	 */
 	private final ISQLFileDataSourceConfiguration _conf;
 
 	// tag table
 	/**
 	 * Name for the 'tag' table.
 	 */
 	public static final String TAG_TABLE_NAME = "tag";
 
 	/**
 	 * Name for the 'id' column of the 'tag' table.
 	 */
 	public static final String TAG_ID_COLUMN_NAME = "id";
 
 	/**
 	 * Name for the 'name' column of the 'tag' table.
 	 */
 	public static final String TAG_NAME_COLUMN_NAME = "name";
 
 	/**
 	 * Name for the 'description' column of the 'tag' table.
 	 */
 	public static final String TAG_DESCRIPTION_COLUMN_NAME = "description";
 
 	/**
 	 * Name for the 'parentId' column of the 'tag' table.
 	 */
 	public static final String TAG_PARENT_ID_COLUMN_NAME = "parentId";
 
 	/**
 	 * Name for the 'selectable' column of the 'tag' table.
 	 */
 	public static final String TAG_SELECTABLE_COLUMN_NAME = "selectable";
 
 	// picture table
 	/**
 	 * Name for the 'picture' table.
 	 */
 	public static final String PICTURE_TABLE_NAME = "picture";
 
 	/**
 	 * Name for the 'id' column of the 'picture' table.
 	 */
 	public static final String PICTURE_ID_COLUMN_NAME = "id";
 
 	/**
 	 * Name for the'original_name' column of the 'picture' table.
 	 */
 	public static final String PICTURE_ORIGINAL_NAME = "original_name";
 
 	/**
 	 * Name for the 'grade' column of the 'picture' table.
 	 */
 	public static final String PICTURE_GRADE_COLUMN_NAME = "grade";
 
 	/**
 	 * Name for the 'width' column of the 'picture' table.
 	 */
 	public static final String PICTURE_WIDTH_COLUMN_NAME = "width";
 
 	/**
 	 * Name for the 'mark' column of the 'picture' table.
 	 */
 	public static final String PICTURE_HEIGTH_COLUMN_NAME = "height";
 
 	/**
 	 * Name for the 'modified_timestamp' column of the 'picture' table.
 	 */
 	public static final String PICTURE_MODIFIED_TIMESTAMP_COLUMN_NAME = "modified_timestamp";
 
 	/**
 	 * Name for the 'creation_timestamp' column of the 'picture' table.
 	 */
 	public static final String PICTURE_CREATION_TIMESTAMP_COLUMN_NAME = "creation_timestamp";
 
 	/**
 	 * Name for the 'adding_timestamp' column of the 'picture' table.
 	 */
 	public static final String PICTURE_ADDING_TIMESTAMP_COLUMN_NAME = "adding_timestamp";
 
 	// picture_tag table
 	/**
 	 * Name for the 'picture_tag' table.
 	 */
 	public static final String PICTURE_TAG_TABLE_NAME = "picture_tag";
 
 	/**
 	 * Name for the 'tagId' column of the 'picture_tag' table.
 	 */
 	public static final String PICTURE_TAG_TAG_ID_COLUMN_NAME = "tagId";
 
 	/**
 	 * Name for the 'pictureId' column of the 'picture_tag' table.
 	 */
 	public static final String PICTURE_TAG_PICTURE_ID_COLUMN_NAME = "pictureId";
 
 	/**
 	 * Connection to the database.
 	 */
 	private final Connection _connection;
 
 	/**
 	 * Statement to insert a {@link Tag}.
 	 */
 	private final PreparedStatement _psInsertTag;
 
 	/**
 	 * Statement to count the number of {@link IPicture}s having a given
 	 * {@link Tag}.
 	 */
 	private final PreparedStatement _psCountPicturesByTag;
 
 	/**
 	 * Statement to count the total number of {@link IPicture}s in this
 	 * {@link SQLFileDataSource}.
 	 */
 	private final PreparedStatement _psCountPictures;
 
 	/**
 	 * Statement to select all the {@link IPicture}s having a given {@link Tag}.
 	 */
 	private final PreparedStatement _psSelectPicturesByTag;
 
 	/**
 	 * Statement to insert an {@link IPicture} in the database.
 	 */
 	private final PreparedStatement _psInsertPicture;
 
 	/**
 	 * Statement to update the mark and the timestamp of the {@link IPicture}.
 	 */
 	private final PreparedStatement _psUpdatePictureMarkAndTimestamp;
 
 	/**
 	 * Statement to insert a {@link Tag} for an {@link IPicture}.
 	 */
 	private final PreparedStatement _psInsertTagForPicture;
 
 	/**
 	 * Statement to remove all the {@link Tag}s of an {@link IPicture}.
 	 */
 	private final PreparedStatement _psRemoveTagsForPicture;
 
 	/**
 	 * Statement to load a picture.
 	 */
 	private final PreparedStatement _psLoadPicture;
 
 	/**
 	 * Statement to load all the {@link Tag}s of an {@link IPicture}.
 	 */
 	private final PreparedStatement _psLoadTagsOfPicture;
 
 	/**
 	 * Statement to load a {@link Tag}.
 	 */
 	private final PreparedStatement _psLoadTag;
 
 	/**
 	 * Statement to load the list of {@link IPicture} names.
 	 */
 	private final PreparedStatement _psListPicture;
 
 	/**
 	 * creates a new SQLFileListConnection.
 	 * 
 	 * @param conf
 	 *            configuration for this {@link SQLFileListConnection}.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the connection to the
 	 *             database.
 	 * @throws ClassNotFoundException
 	 *             if the database driver class can't be found.
 	 */
 	public SQLFileListConnection(final ISQLFileDataSourceConfiguration conf)
 			throws ClassNotFoundException, SQLException
 	{
 		_conf = conf;
 		Class.forName("org.sqlite.JDBC");
 		_connection = DriverManager.getConnection("jdbc:sqlite:"
 				+ _conf.getDatabaseFileName());
 		createTables();
 
 		_psInsertTag = _connection.prepareStatement("INSERT INTO "
 				+ TAG_TABLE_NAME + " (" + TAG_ID_COLUMN_NAME + ", "
 				+ TAG_NAME_COLUMN_NAME + ", " + TAG_DESCRIPTION_COLUMN_NAME
 				+ ", " + TAG_PARENT_ID_COLUMN_NAME + ", "
 				+ TAG_SELECTABLE_COLUMN_NAME + ") VALUES(?, ?, ?, ?, ?)");
 		_psCountPicturesByTag = _connection.prepareStatement("SELECT COUNT("
 				+ PICTURE_TAG_PICTURE_ID_COLUMN_NAME + ") FROM "
 				+ PICTURE_TAG_TABLE_NAME + " WHERE "
 				+ PICTURE_TAG_TAG_ID_COLUMN_NAME + "=?");
 		_psSelectPicturesByTag = _connection.prepareStatement("SELECT "
 				+ PICTURE_TAG_PICTURE_ID_COLUMN_NAME + " FROM "
 				+ PICTURE_TAG_TABLE_NAME + " WHERE "
 				+ PICTURE_TAG_TAG_ID_COLUMN_NAME + "=?");
 		_psCountPictures = _connection.prepareStatement("SELECT COUNT("
 				+ PICTURE_ID_COLUMN_NAME + ") FROM " + PICTURE_TABLE_NAME);
 		_psInsertPicture = _connection.prepareStatement("INSERT INTO "
 				+ PICTURE_TABLE_NAME + " (" + PICTURE_ID_COLUMN_NAME + ", "
 				+ PICTURE_ORIGINAL_NAME + ", " + PICTURE_GRADE_COLUMN_NAME
 				+ ", " + PICTURE_WIDTH_COLUMN_NAME + ", "
 				+ PICTURE_HEIGTH_COLUMN_NAME + ", "
 				+ PICTURE_MODIFIED_TIMESTAMP_COLUMN_NAME + ", "
 				+ PICTURE_CREATION_TIMESTAMP_COLUMN_NAME + ", "
 				+ PICTURE_ADDING_TIMESTAMP_COLUMN_NAME
 				+ ") VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
 		_psUpdatePictureMarkAndTimestamp = _connection
 				.prepareStatement("UPDATE " + PICTURE_TABLE_NAME + " SET "
 						+ PICTURE_GRADE_COLUMN_NAME + "=?, "
 						+ PICTURE_MODIFIED_TIMESTAMP_COLUMN_NAME + "=? WHERE "
 						+ PICTURE_ID_COLUMN_NAME + "=?");
 		_psInsertTagForPicture = _connection.prepareStatement("INSERT INTO "
 				+ PICTURE_TAG_TABLE_NAME + " ("
 				+ PICTURE_TAG_TAG_ID_COLUMN_NAME + ", "
 				+ PICTURE_TAG_PICTURE_ID_COLUMN_NAME + ") VALUES(?, ?)");
 		_psRemoveTagsForPicture = _connection.prepareStatement("DELETE FROM "
 				+ PICTURE_TAG_TABLE_NAME + " WHERE "
 				+ PICTURE_TAG_TAG_ID_COLUMN_NAME + "=?");
 		_psLoadPicture = _connection.prepareStatement("SELECT "
 				+ PICTURE_GRADE_COLUMN_NAME + ", " + PICTURE_ORIGINAL_NAME
 				+ ", " + PICTURE_WIDTH_COLUMN_NAME + ", "
 				+ PICTURE_HEIGTH_COLUMN_NAME + ", "
				+ PICTURE_MODIFIED_TIMESTAMP_COLUMN_NAME + ", "
				+ PICTURE_CREATION_TIMESTAMP_COLUMN_NAME + ", "
				+ PICTURE_ADDING_TIMESTAMP_COLUMN_NAME + " FROM "
 				+ PICTURE_TABLE_NAME + " WHERE " + PICTURE_ID_COLUMN_NAME
 				+ " =?");
 		_psLoadTagsOfPicture = _connection.prepareStatement("SELECT "
 				+ PICTURE_TAG_TAG_ID_COLUMN_NAME + " FROM "
 				+ PICTURE_TAG_TABLE_NAME + " WHERE "
 				+ PICTURE_TAG_PICTURE_ID_COLUMN_NAME + " =?");
 		_psLoadTag = _connection.prepareStatement("SELECT "
 				+ TAG_NAME_COLUMN_NAME + ", " + TAG_DESCRIPTION_COLUMN_NAME
 				+ ", " + TAG_PARENT_ID_COLUMN_NAME + ", "
 				+ TAG_SELECTABLE_COLUMN_NAME + " FROM " + TAG_TABLE_NAME
 				+ " where " + TAG_ID_COLUMN_NAME + " =?");
 		_psListPicture = _connection.prepareStatement("SELECT "
 				+ PICTURE_ID_COLUMN_NAME + " FROM " + PICTURE_TABLE_NAME);
 	}
 
 	/**
 	 * Properly close the connection to the database.
 	 */
 	public void close()
 	{
 		// TODO
 	}
 
 	/**
 	 * Save the given {@link Tag} to the database.
 	 * 
 	 * @param tag
 	 *            the {@link Tag} to save.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the saving of the {@link Tag}
 	 *             .
 	 */
 	public void saveTagToDatabase(final Tag tag) throws SQLException
 	{
 		synchronized (_psInsertTag)
 		{
 			_psInsertTag.clearParameters();
 			_psInsertTag.setInt(1, tag.getTagId());
 			_psInsertTag.setString(2, tag.getName());
 			_psInsertTag.setString(3, tag.getDescription());
 			if (tag.getParent() == null)
 			{
 				_psInsertTag.setInt(4, -1);
 			}
 			else
 			{
 				_psInsertTag.setInt(4, tag.getParent().getTagId());
 			}
 			_psInsertTag.setBoolean(5, tag.isSelectable());
 			_psInsertTag.executeUpdate();
 		}
 	}
 
 	/**
 	 * Create the tables of this database.
 	 * 
 	 * @throws SQLException
 	 *             if an SQL error occurred during the creation of the tables.
 	 */
 	public void createTables() throws SQLException
 	{
 		Statement statement = null;
 		try
 		{
 			statement = _connection.createStatement();
 			// Tag table
 			statement.executeUpdate("create table if not exists "
 					+ TAG_TABLE_NAME + " (" + TAG_ID_COLUMN_NAME + " integer, "
 					+ TAG_NAME_COLUMN_NAME + " text, "
 					+ TAG_DESCRIPTION_COLUMN_NAME + " text, "
 					+ TAG_PARENT_ID_COLUMN_NAME + " integer, "
 					+ TAG_SELECTABLE_COLUMN_NAME + " boolean)");
 			// picture table
 			statement.executeUpdate("create table if not exists "
 					+ PICTURE_TABLE_NAME + " (" + PICTURE_ID_COLUMN_NAME
 					+ " text, " + PICTURE_ORIGINAL_NAME + " text, "
 					+ PICTURE_GRADE_COLUMN_NAME + " integer, "
 					+ PICTURE_WIDTH_COLUMN_NAME + " integer, "
 					+ PICTURE_HEIGTH_COLUMN_NAME + " integer, "
 					+ PICTURE_MODIFIED_TIMESTAMP_COLUMN_NAME + " integer, "
 					+ PICTURE_CREATION_TIMESTAMP_COLUMN_NAME + " integer, "
 					+ PICTURE_ADDING_TIMESTAMP_COLUMN_NAME + " integer)");
 			// picture_tag table
 			statement.executeUpdate("create table if not exists "
 					+ PICTURE_TAG_TABLE_NAME + " ("
 					+ PICTURE_TAG_TAG_ID_COLUMN_NAME + " integer, "
 					+ PICTURE_TAG_PICTURE_ID_COLUMN_NAME + " integer)");
 		}
 		finally
 		{
 			if (statement != null)
 			{
 				statement.close();
 			}
 		}
 	}
 
 	/**
 	 * Load the list of tags.
 	 * 
 	 * @return a {@link ResultSet} containing the list of tags.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public ResultSet loadTagList() throws SQLException
 	{
 		Statement statement = null;
 		try
 		{
 			statement = _connection.createStatement();
 			return statement.executeQuery("select " + TAG_ID_COLUMN_NAME + ", "
 					+ TAG_NAME_COLUMN_NAME + ", " + TAG_DESCRIPTION_COLUMN_NAME
 					+ ", " + TAG_SELECTABLE_COLUMN_NAME + " from "
 					+ TAG_TABLE_NAME);
 		}
 		finally
 		{
 			if (statement != null)
 			{
 				statement.close();
 			}
 		}
 	}
 
 	/**
 	 * Load the list of tag id and there parent id.
 	 * 
 	 * @return a {@link ResultSet} containing the list of tag id and there
 	 *         parent id.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public ResultSet loadParents() throws SQLException
 	{
 		Statement statement = null;
 		try
 		{
 			statement = _connection.createStatement();
 			return statement.executeQuery("select " + TAG_ID_COLUMN_NAME + ", "
 					+ TAG_PARENT_ID_COLUMN_NAME + " from " + TAG_TABLE_NAME);
 		}
 		finally
 		{
 			if (statement != null)
 			{
 				statement.close();
 			}
 		}
 	}
 
 	/**
 	 * Insert a {@link FsPicture} in the database.
 	 * 
 	 * @param picture
 	 *            the {@link FsPicture} to insert.
 	 * @return true if the picture was correctly inserted.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the insertion in the
 	 *             database.
 	 */
 	public boolean insertPicture(final FsPicture picture) throws SQLException
 	{
 		synchronized (_psInsertPicture)
 		{
 			_psInsertPicture.clearParameters();
 			_psInsertPicture.setString(1, picture.getId());
 			_psInsertPicture.setInt(2, picture.getPictureGrade());
 			_psInsertPicture.setInt(3, picture.getWidth());
 			_psInsertPicture.setInt(4, picture.getHeight());
 			_psInsertPicture.setLong(5, picture.getModifiedTimestamp());
 			return _psInsertPicture.executeUpdate() > 0;
 		}
 	}
 
 	/**
 	 * Update the {@link Tag} list of a {@link FsPicture}.
 	 * 
 	 * @param picture
 	 *            the {@link FsPicture} to update.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the insertion in the
 	 *             database.
 	 */
 	public void updateTags(final FsPicture picture) throws SQLException
 	{
 		synchronized (_psRemoveTagsForPicture)
 		{
 			_psRemoveTagsForPicture.clearParameters();
 			_psRemoveTagsForPicture.setString(1, picture.getId());
 			_psRemoveTagsForPicture.execute();
 		}
 		synchronized (_psInsertTagForPicture)
 		{
 			for (final Tag tag : picture.getTagSet())
 			{
 				_psInsertTagForPicture.clearParameters();
 				_psInsertTagForPicture.setInt(1, tag.getTagId());
 				_psInsertTagForPicture.setString(2, picture.getId());
 				_psInsertTagForPicture.executeUpdate();
 			}
 		}
 	}
 
 	/**
 	 * Update the meta-informations of the specified picture.
 	 * 
 	 * @param picture
 	 *            the {@link FsPicture} to update.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the insertion in the
 	 *             database.
 	 */
 	public void updatePicture(final FsPicture picture) throws SQLException
 	{
 		synchronized (_psUpdatePictureMarkAndTimestamp)
 		{
 			_psUpdatePictureMarkAndTimestamp.clearParameters();
 			_psUpdatePictureMarkAndTimestamp.setInt(1,
 					picture.getPictureGrade());
 			_psUpdatePictureMarkAndTimestamp.setLong(2,
 					picture.getModifiedTimestamp());
 			_psUpdatePictureMarkAndTimestamp.setString(3, picture.getId());
 			_psUpdatePictureMarkAndTimestamp.executeUpdate();
 		}
 		updateTags(picture);
 	}
 
 	/**
 	 * Count the number of pictures which have a given {@link Tag}.
 	 * 
 	 * @param tag
 	 *            the {@link Tag} to consider.
 	 * @return the number of pictures which have a given {@link Tag}.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public int countPictures(final Tag tag) throws SQLException
 	{
 		synchronized (_psCountPicturesByTag)
 		{
 			_psCountPicturesByTag.clearParameters();
 			_psCountPicturesByTag.setInt(1, tag.getTagId());
 			ResultSet res = null;
 			try
 			{
 				res = _psCountPicturesByTag.executeQuery();
 				if (res.next())
 				{
 					return res.getInt(PICTURE_TAG_PICTURE_ID_COLUMN_NAME);
 				}
 				return 0;
 			}
 			finally
 			{
 				if (res != null)
 				{
 					res.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Count the total number of pictures.
 	 * 
 	 * @return the total number of pictures.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public int countPictures() throws SQLException
 	{
 		synchronized (_psCountPictures)
 		{
 			ResultSet res = null;
 			try
 			{
 				res = _psCountPictures.executeQuery();
 				if (res.next())
 				{
 					return res.getInt(PICTURE_ID_COLUMN_NAME);
 				}
 				return 0;
 			}
 			finally
 			{
 				if (res != null)
 				{
 					res.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Select the list of pictures which have a given {@link Tag}.
 	 * 
 	 * @param tag
 	 *            the {@link Tag} to consider.
 	 * @return the list of pictures which have a given {@link Tag}.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public String[] selectPictures(final Tag tag) throws SQLException
 	{
 		final LinkedList<String> pictureList = new LinkedList<String>();
 		synchronized (_psSelectPicturesByTag)
 		{
 			_psSelectPicturesByTag.clearParameters();
 			_psSelectPicturesByTag.setInt(1, tag.getTagId());
 			ResultSet response = null;
 			try
 			{
 				response = _psSelectPicturesByTag.executeQuery();
 				while (response.next())
 				{
 					pictureList.add(response
 							.getString(PICTURE_TAG_PICTURE_ID_COLUMN_NAME));
 				}
 			}
 			finally
 			{
 				if (response != null)
 				{
 					response.close();
 				}
 			}
 		}
 		final String[] res = new String[pictureList.size()];
 		return pictureList.toArray(res);
 	}
 
 	/**
 	 * Load the information about the specified picture.
 	 * 
 	 * @param strPictureId
 	 *            the id of the picture.
 	 * @return the information about the specified picture.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public ResultSet loadPicture(final String strPictureId) throws SQLException
 	{
 		synchronized (_psLoadPicture)
 		{
 			_psLoadPicture.clearParameters();
 			_psLoadPicture.setString(1, strPictureId);
 			return _psLoadPicture.executeQuery();
 		}
 	}
 
 	/**
 	 * Load all the {@link Tag}s of an {@link IPicture}.
 	 * 
 	 * @param strPictureId
 	 *            the id of the picture.
 	 * @return the list of {@link Tag}s of the picture.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public Integer[] loadTagsOfPicture(final String strPictureId)
 			throws SQLException
 	{
 		final LinkedList<Integer> tagList = new LinkedList<Integer>();
 		synchronized (_psLoadTagsOfPicture)
 		{
 			_psLoadTagsOfPicture.clearParameters();
 			_psLoadTagsOfPicture.setString(1, strPictureId);
 			ResultSet response = null;
 			try
 			{
 				response = _psLoadTagsOfPicture.executeQuery();
 				while (response.next())
 				{
 					tagList.add(Integer.valueOf(response
 							.getInt(PICTURE_TAG_TAG_ID_COLUMN_NAME)));
 				}
 			}
 			finally
 			{
 				if (response != null)
 				{
 					response.close();
 				}
 			}
 		}
 		final Integer[] res = new Integer[tagList.size()];
 		return tagList.toArray(res);
 	}
 
 	/**
 	 * Load the information about the specified {@link Tag}.
 	 * 
 	 * @param iTagId
 	 *            the id of the {@link Tag}.
 	 * @return the information about the specified {@link Tag}.
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public ResultSet loadTag(final int iTagId) throws SQLException
 	{
 		synchronized (_psLoadTag)
 		{
 			_psLoadTag.clearParameters();
 			_psLoadTag.setInt(1, iTagId);
 			return _psLoadTag.executeQuery();
 		}
 	}
 
 	/**
 	 * Load the list of {@link IPicture} of this {@link IDataSource}.
 	 * 
 	 * @return the
 	 * @throws SQLException
 	 *             if an SQL error occurred during the interrogation of the
 	 *             database.
 	 */
 	public ResultSet loadPictureList() throws SQLException
 	{
 		synchronized (_psListPicture)
 		{
 			return _psListPicture.executeQuery();
 		}
 	}
 }
