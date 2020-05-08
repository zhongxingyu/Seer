 package dao;
 
 import java.lang.reflect.Field;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import model.Entity;
 import model.reflection.EntityField;
 import util.BeanUtils;
 import util.StringUtils;
 import annotation.Table;
 import connection.SingleConnection;
 import exception.MissingAnnotationException;
 import exception.SQLException;
 
 public class AbstractDAO<T extends Entity> implements DAO<T> {
 	private final SingleConnection conn;
 	private final Class<T> entity;
 	private final String entityName;
 
 	public AbstractDAO(final Class<T> clazz) throws MissingAnnotationException,
 			ClassNotFoundException, SQLException {
 		this.entity = clazz;
 		if (!entity.isAnnotationPresent(annotation.Entity.class)) {
 			throw new MissingAnnotationException("Entity");
 		}
 
 		if (entity.isAnnotationPresent(annotation.Table.class)) {
 			Table t = this.entity.getAnnotation(annotation.Table.class);
 			entityName = t.name();
 		} else {
 			entityName = StringUtils.camelCaseToUnderscore(this.entity
 					.getSimpleName().toLowerCase());
 		}
 
 		try {
 			this.conn = SingleConnection.getInstance();
 		} catch (java.sql.SQLException e) {
 			throw new SQLException(e.getMessage());
 		}
 	}
 
 	@Override
 	public T getById(final Integer id) throws SQLException {
 		return getByField("id", id).get(0);
 	}
 
 	@Override
 	public List<T> list() throws Exception {
 		final String query = "SELECT * FROM " + this.entityName;
 		PreparedStatement ps = this.conn.prepareStatement(query);
 
 		ResultSet rs = ps.executeQuery();
 
 		List<T> results = this.getListOfBeansFromResultSet(rs);
 
 		return results;
 	}
 
 	@Override
 	public void saveOrUpdate(final T obj) throws SQLException {
 
 		try {
 			Map<String, Object> columNamesWithValues = obj
 					.getAllColumnsWithValue();
 
 			String query = obj.isNew() ? getInsertQuery(columNamesWithValues)
 					: getUpdateQuery(obj, columNamesWithValues);
 
 			PreparedStatement ps = this.conn.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			Integer parametroAtual = 1;
 			for (Map.Entry<String, Object> entry : columNamesWithValues
 					.entrySet()) {
 				ps.setObject(parametroAtual, entry.getValue());
 				parametroAtual++;
 			}
 
 			if (!obj.isNew())
 				ps.setObject(parametroAtual, obj.getId());
 
 			int affectedRows = ps.executeUpdate();
 			if (affectedRows == 0) {
 				throw new SQLException("Houve um erro ao gravar o registro");
 			}
 
 			ResultSet generatedKeys = ps.getGeneratedKeys();
 			if (generatedKeys.next()) {
				if (obj.getId() != generatedKeys.getInt("id")) {
 					obj.setId(generatedKeys.getInt("id"));
 					persistAssociations(obj);
 				}
 			} else {
 				throw new SQLException(
 						"Houve um erro ao recuperar a chave o registro");
 			}
 
 			if (ps != null)
 				ps.close();
 			if (generatedKeys != null)
 				generatedKeys.close();
 		} catch (java.sql.SQLException e) {
 			rollback();
 			throw new SQLException(e.getMessage());
 		} catch (IllegalArgumentException e) {
 			rollback();
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			rollback();
 			e.printStackTrace();
 		}
 		try {
 			this.conn.commit();
 		} catch (java.sql.SQLException e) {
 			throw new SQLException(e.getMessage());
 		}
 	}
 
 	private void rollback() throws SQLException {
 		try {
 			conn.rollback();
 		} catch (java.sql.SQLException e1) {
 			throw new SQLException(e1.getMessage());
 		}
 	}
 
 	private void persistAssociations(T obj) throws SQLException,
 			IllegalArgumentException, IllegalAccessException {
 		try {
 			PreparedStatement associationPs = null;
 
 			for (Map.Entry<String, Collection<Entity>> entry : obj
 					.getAllManyToManyFieldsWithValue().entrySet()) {
 				for (Entity e : entry.getValue()) {
 					StringBuilder associateQuery = new StringBuilder(
 							"INSERT INTO "
 									+ entry.getKey()
 									+ " ( "
 									+ entityName
 									+ "_id, "
 									+ e.getClass().getSimpleName()
 											.toLowerCase() + "_id ) values ( "
 									+ obj.getId() + ", " + e.getId() + " )");
 					associationPs = this.conn.prepareStatement(associateQuery
 							.toString());
 
 					int affectedAssociationRows = associationPs.executeUpdate();
 					if (affectedAssociationRows == 0) {
 						throw new SQLException(
 								"Houve um erro ao gravar as associaes");
 					}
 				}
 			}
 
 			if (associationPs != null)
 				associationPs.close();
 		} catch (java.sql.SQLException e1) {
 			throw new SQLException(e1.getMessage());
 		}
 	}
 
 	private String getUpdateQuery(T obj,
 			Map<String, Object> columNamesWithValues) {
 		StringBuilder query = new StringBuilder();
 		query.append("UPDATE " + entityName + " SET ");
 
 		StringBuilder insertColumns = new StringBuilder();
 
 		for (Map.Entry<String, Object> entry : columNamesWithValues.entrySet()) {
 			insertColumns.append(", " + entry.getKey() + " = ? ");
 		}
 
 		insertColumns.setCharAt(0, ' ');
 		insertColumns.append(" WHERE id = ? ");
 
 		query.append(insertColumns);
 		return query.toString();
 	}
 
 	private String getInsertQuery(Map<String, Object> columNamesWithValues) {
 		StringBuilder query = new StringBuilder();
 		query.append("INSERT INTO " + entityName + " ");
 
 		StringBuilder insertColumns = new StringBuilder();
 		StringBuilder insertValues = new StringBuilder();
 
 		for (Map.Entry<String, Object> entry : columNamesWithValues.entrySet()) {
 			insertColumns.append(", " + entry.getKey() + " ");
 			insertValues.append(", ? ");
 		}
 
 		insertColumns.setCharAt(0, '(');
 		insertValues.setCharAt(0, '(');
 
 		insertColumns.append(" ) ");
 		insertValues.append(" ) ");
 
 		query.append(insertColumns + " VALUES " + insertValues);
 		return query.toString();
 	}
 
 	private Field[] getEntityDeclaredFields() {
 		List<Field> allFields = new ArrayList<Field>(Arrays.asList(this.entity
 				.getDeclaredFields()));
 		List<Field> superclassFields = new ArrayList<Field>(
 				Arrays.asList(this.entity.getSuperclass().getDeclaredFields()));
 		allFields.addAll(superclassFields);
 		return allFields.toArray(new Field[allFields.size()]);
 	}
 
 	protected List<T> getByField(String field, Object value)
 			throws SQLException {
 		List<T> results = null;
 		try {
 			String query = "SELECT * FROM " + this.entityName + " WHERE "
 					+ StringUtils.camelCaseToUnderscore(field) + " = (?)";
 			PreparedStatement ps = this.conn.prepareStatement(query);
 			ps.setObject(1, value);
 
 			ResultSet rs = ps.executeQuery();
 			results = getListOfBeansFromResultSet(rs);
 
 			if (results.size() == 0)
 				throw new SQLException("Objeto no encontrado no banco");
 		} catch (java.sql.SQLException e1) {
 			throw new SQLException(e1.getMessage());
 		}
 		return results;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private List<T> getListOfBeansFromResultSet(final ResultSet rs)
 			throws SQLException {
 		final List<T> list = new ArrayList<T>();
 		try {
 			final ResultSetMetaData rsmd = rs.getMetaData();
 
 			if (rs != null) {
 				while (rs.next()) {
 					T bean = null;
 					bean = this.entity.newInstance();
 					for (int _iterator = 0; _iterator < rsmd.getColumnCount(); _iterator++) {
 						final String columnName = StringUtils
 								.underscoreToCamelCase(rsmd
 										.getColumnName(_iterator + 1));
 						Object columnValue = rs.getObject(_iterator + 1);
 
 						for (final Field field : this.getEntityDeclaredFields()) {
 							String fieldNameInDB = field.getName();
 
 							if (field
 									.isAnnotationPresent(annotation.BelongsTo.class)) {
 								fieldNameInDB = fieldNameInDB + "Id";
 							}
 
 							if (fieldNameInDB.equalsIgnoreCase(columnName)
 									&& columnValue != null
 									&& !field
 											.isAnnotationPresent(annotation.Transient.class)) {
 
 								if (field
 										.isAnnotationPresent(annotation.BelongsTo.class)) {
 									AbstractDAO absDAO = null;
 									absDAO = new AbstractDAO(field.getType());
 
 									List relation = absDAO
 											.getByField("id", new Integer(
 													columnValue.toString()));
 									columnValue = relation.size() >= 1 ? relation
 											.get(0) : null;
 								}
 
 								BeanUtils.setProperty(bean, field.getName(),
 										columnValue);
 								break;
 							}
 						}
 					}
 
 					for (final Field field : this.getEntityDeclaredFields()) {
 						List columnValue = new ArrayList();
 						if (field
 								.isAnnotationPresent(annotation.ManyToMany.class)
 								&& !field
 										.isAnnotationPresent(annotation.Transient.class)) {
 
 							EntityField eField = new EntityField(field);
 							String query = "SELECT produto_id FROM "
 									+ eField.getJoinTableName() + " WHERE "
 									+ this.entityName + "_id = (?)";
 
 							PreparedStatement ps = this.conn
 									.prepareStatement(query);
 							ps.setObject(1, rs.getInt("id"));
 
 							ResultSet rsRel = ps.executeQuery();
 
 							while (rsRel.next()) {
 								AbstractDAO absDAORel = new AbstractDAO(
 										eField.getAssociationClass());
 								((List) columnValue).add(absDAORel
 										.getById(rsRel.getInt("produto_id")));
 							}
 
 							BeanUtils.setProperty(bean, field.getName(),
 									columnValue);
 						}
 					}
 
 					list.add(bean);
 				}
 			}
 		} catch (java.sql.SQLException e1) {
 			throw new SQLException(e1.getMessage());
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (MissingAnnotationException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 }
