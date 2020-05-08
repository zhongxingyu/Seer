 package app.util;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import app.util.conexao.DAOUtil;
 
 /**
  * Classe abstrata que efetua as operações básicas de uma DAO: - Adicionar,
  * Editar, Excluir, Buscar e Listar.
  * 
  * @author Daniel Bonfim <daniel.fb88@gmail.com>
  * @since 10-08-2012
  * 
  */
 public abstract class AbstractDAO {
 
 	/**
 	 * Nome da tabela
 	 */
 	protected String nomeDaTabela;
 
 	/**
 	 * Nome do(s) campo(s) primaryKey
 	 */
 	protected String[] primaryKey;
 
 	/**
 	 * Nome do(s) campo(s) da tabela
 	 */
 	protected String[] campos;
 
 	/**
 	 * Prepara os dados contra SQL Injection.
 	 * 
 	 * @param ps
 	 *            Objeto PreparedStatement com a query já inserida.
 	 * @param parametros
 	 *            Lista com os valores inseridos na mesma ordem dos caracteres
 	 *            curinga na query do PreparedStatemet
 	 */
 	protected void prepareStatement(PreparedStatement ps,
 			List<Object> parametros, Boolean useLike) {
 		try {
 			for (int i = 0; i < parametros.size(); i++) {
 				int indexPS = i + 1;
 				switch (parametros.get(i).getClass().getName()) {
 				case "java.lang.String":
 					if (useLike)
 						ps.setString(indexPS, "%" + (String) parametros.get(i)
 								+ "%");
 					else
 						ps.setString(indexPS, (String) parametros.get(i));
 					break;
 				case "java.lang.Integer":
 					ps.setInt(indexPS, (Integer) parametros.get(i));
 					break;
 				case "java.lang.Double":
 					ps.setDouble(indexPS, (Double) parametros.get(i));
 					break;
 				case "java.lang.Boolean":
 					ps.setBoolean(indexPS, (Boolean) parametros.get(i));
 					break;
 				case "java.lang.Date":
 					ps.setDate(indexPS, (Date) parametros.get(i));
 					break;
 				default:
 					throw new Exception("Tipo do parâmetro não especificado");
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 	}
 
 	/**
 	 * Verifica se o nome da tabela foi informado na subclasse
 	 */
 	private void verificaNomeTabela() {
 		if (this.nomeDaTabela == null || this.nomeDaTabela.isEmpty()) {
 			try {
 				throw new Exception(
 						"Nome da tabela não informado na sub-classe");
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 	}
 
 	/**
 	 * Verifica se o nome da(s) Primary Key(s) foi informado na subclasse
 	 */
 	private void verificaPK() {
 		try {
 			if (this.primaryKey == null || this.primaryKey.length == 0) {
 				throw new Exception(
 						"Nome da coluna de ID não informado na sub-classe");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Verifica se a string de entrada é igual ao nome do campo de alguma
 	 * primary key
 	 * 
 	 * @param campo
 	 * @return
 	 */
 	private boolean is_campoIgualPrimaryKey(String campo) {
 		boolean igualAlgumaPK = false;
 		for (int npk = 0; npk < this.primaryKey.length; npk++) {
 			if (campo.equalsIgnoreCase(this.primaryKey[npk])) {
 				igualAlgumaPK = true;
 				break;
 			}
 		}
 		return igualAlgumaPK;
 	}
 
 	/**
 	 * Retorna o nome da Tabela
 	 * 
 	 * @return
 	 */
 	public String getNomeDaTabela() {
 		return nomeDaTabela;
 	}
 
 	/**
 	 * Retorna o nome das primary keys
 	 * 
 	 * @return
 	 */
 	public String[] getPrimaryKey() {
 		return primaryKey;
 	}
 
 	/**
 	 * Retorna o nome dos campos da Tabela
 	 * 
 	 * @return
 	 */
 	public String[] getCampos() {
 		return campos;
 	}
 
 	/**
 	 * Retorna os atributos da Tabela. PrimaryKey + Campos
 	 * 
 	 * @return
 	 */
 	public String[] getAtributos() {
 		String[] pk = this.getPrimaryKey();
 		String[] campos = this.getCampos();
 
 		String[] atributos = new String[pk.length + campos.length];
 		for (int i = 0; i < pk.length; i++) {
 			atributos[i] = pk[i];
 		}
 		int indiceCampos = 0;
 		for (int i = pk.length; i < pk.length + campos.length; i++) {
 			atributos[i] = campos[indiceCampos++];
 		}
 		return atributos;
 	}
 
 	/**
 	 * Monta pedaço da query onde é inserido os IDs. Ex:
 	 * 
 	 * UPDATE tabela SET campo1 = 'valor1', campo2 = 'valor2' WHERE id = 5;
 	 * 
 	 * A parte da query "id = 5" é o que será retornado por este método.
 	 * 
 	 * @param campoValor
 	 *            Map contendo a(s) primary key(s) como chave
 	 * @return String da parte da query relacionada aos IDs
 	 */
 	private String montaParteQueryID(Map<Object, Object> campoValor) {
 		StringBuilder builder = new StringBuilder();
 
 		for (int i = 0; i < this.primaryKey.length; i++) {
 			builder.append(this.primaryKey[i] + " = "
 					+ campoValor.get(this.primaryKey[i]));
 			if (i != this.primaryKey.length - 1)
 				builder.append(", ");
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * Efetua um:
 	 * 
 	 * "INSERT INTO 'tabela' (campos) VALUES (valores) WHERE (filtros)"
 	 * 
 	 * utilizando o(s) Ids informados na subclasse e o HashMap enviado como
 	 * parâmetro.
 	 * 
 	 * @param campoValor
 	 *            HashMap com o nome do campo e o valor relacionado.
 	 * @return Retorno do executeUpdate
 	 */
 	protected int _adicionar(Map<Object, Object> campoValor) {
 		this.verificaNomeTabela();
 
 		int linhasAfetadas = 0;
 		int i = 0;
 
 		ArrayList<Object> ordem = new ArrayList<Object>();
 		StringBuilder builder = new StringBuilder();
 
 		try {
 			builder.append("INSERT INTO ");
 			builder.append(this.nomeDaTabela + " ");
 			builder.append("(");
 
 			// Iterando os objetos para pegar a chave
 			Iterator<Map.Entry<Object, Object>> it = campoValor.entrySet()
 					.iterator();
 
 			while (it.hasNext()) {
 				Map.Entry<Object, Object> me = (Map.Entry<Object, Object>) it
 						.next();
 
 				// inserindo os valores em um arraylist
 				ordem.add(me.getValue());
 
 				// inserindo a virgula depois do primeiro elemento
 				if (i++ != 0)
 					builder.append(",");
 				builder.append((String) me.getKey());
 			}
 
 			builder.append(") ");
 			builder.append("VALUES ");
 			builder.append("(");
 
 			i--;
 			for (; i >= 0; i--) {
 				builder.append("?");
 				if (i != 0)
 					builder.append(",");
 			}
 
 			builder.append(")");
 			builder.append(";");
 
 			PreparedStatement preparedStatement = DAOUtil.getInstance()
 					.getPreparedStatement(builder.toString());
 
 			// preparando o statement
 			this.prepareStatement(preparedStatement, ordem, false);
 
 			// executando
 			linhasAfetadas = preparedStatement.executeUpdate();
 			preparedStatement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return linhasAfetadas;
 	}
 
 	/**
 	 * Efetua um:
 	 * 
 	 * "UPDATE 'tabela' SET 'campos/valores' WHERE filtros"
 	 * 
 	 * utilizando o(s) Ids informados na subclasse e o HashMap enviado como
 	 * parâmetro.
 	 * 
 	 * @param campoValor
 	 *            Map com a primary key e os campos alterados
 	 * @return Retorno do executeUpdate
 	 */
 	protected int _editar(Map<Object, Object> campoValor) {
 		this.verificaNomeTabela();
 		this.verificaPK();
 
 		int linhasAfetadas = 0;
 		int i = 0;
 
 		/*
 		 * O tamanho do array a ser criado será o tamanho do hashmap -1
 		 * excluindo a primary key
 		 */
 		ArrayList<Object> ordem = new ArrayList<Object>();
 		StringBuilder builder = new StringBuilder();
 
 		try {
 			builder.append("UPDATE ");
 			builder.append(this.nomeDaTabela + " ");
 			builder.append("SET ");
 
 			// Iterando os objetos para pegar a chave
 			Iterator<Map.Entry<Object, Object>> it = campoValor.entrySet()
 					.iterator();
 
 			while (it.hasNext()) {
 				Map.Entry<Object, Object> map = (Map.Entry<Object, Object>) it
 						.next();
 
				// Verificando se o valor é diferente de nulo
				if (map.getValue() != null) {
 
					// verificando se é igual a alguma PK
					// ignorando a(s) primary key(s)
					if (!this.is_campoIgualPrimaryKey((String) map.getKey())) {
						// inserindo a virgula depois do primeiro elemento
						if (i++ != 0)
							builder.append(", ");
 
						builder.append((String) map.getKey() + " = ?");

						// inserindo os valores em um array
						ordem.add(map.getValue());
					}
 				}
 			}
 
 			builder.append(" WHERE ");
 
 			// Inserindo os Ids
 			builder.append(this.montaParteQueryID(campoValor));
 			builder.append(";");
 
 			PreparedStatement preparedStatement = DAOUtil.getInstance()
 					.getPreparedStatement(builder.toString());
 
 			// preparando o statement
 			this.prepareStatement(preparedStatement, ordem, false);
 
 			// executando
 			linhasAfetadas = preparedStatement.executeUpdate();
 			preparedStatement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return linhasAfetadas;
 	}
 
 	/**
 	 * Método de exclusão que recebe um ou vários parâmetros PrimaryKey para
 	 * serem inseridos ao filtro. A ordem dos parâmetros é a mesma do array
 	 * informado na subclasse.
 	 * 
 	 * @param id
 	 *            Primary Key
 	 * @return
 	 */
 	protected int _excluir(Integer... primaryKey) {
 		Map<Object, Object> campoValor = new HashMap<Object, Object>(
 				primaryKey.length);
 
 		// mapeando id - valor
 		for (int i = 0; i < primaryKey.length; i++) {
 			campoValor.put(this.primaryKey[i], primaryKey[i]);
 		}
 
 		return this.__excluir(campoValor);
 	}
 
 	/**
 	 * Recupera o valor da Primary Key (Quando unitária) inserindo as
 	 * foreignkeys.
 	 * 
 	 * Este método deve ser usado APENAS em classes que representem tabelas de
 	 * relacionamentos *muitos para muitos*.
 	 * 
 	 * A ordem dos parâmetros deve ser a mesma definida no array 'campos' do DAO
 	 * 
 	 * @param foreignKey
 	 *            os ids do relacionamento
 	 * @return
 	 */
 	public int getValuePrimaryKey(Integer... foreignKey) {
 		Map<Object, Object> campoValor = new HashMap<Object, Object>(
 				campos.length);
 
 		for (int i = 0; i < campos.length; i++) {
 			campoValor.put(this.campos[i], foreignKey[i]);
 		}
 
 		Map<String, Object> map = this._listarPor(campoValor).get(0);
 		return (Integer) map.get(this.primaryKey[0]);
 	}
 
 	/**
 	 * Efetua um:
 	 * 
 	 * "DELETE FROM 'tabela' WHERE filtros"
 	 * 
 	 * utilizando o(s) Ids informados na subclasse e o HashMap enviado como
 	 * parâmetro.
 	 * 
 	 * @param campoValor
 	 *            Map apenas com a(s) Primary key(s)
 	 * @return Retorno do executeUpdate
 	 */
 	private int __excluir(Map<Object, Object> campoValor) {
 		this.verificaNomeTabela();
 		this.verificaPK();
 
 		int linhasAfetadas = 0;
 		StringBuilder builder = new StringBuilder();
 
 		try {
 			builder.append("DELETE FROM ");
 			builder.append(this.nomeDaTabela);
 			builder.append(" WHERE ");
 
 			// Inserindo os Ids
 			builder.append(this.montaParteQueryID(campoValor));
 			builder.append(";");
 
 			PreparedStatement preparedStatement = DAOUtil.getInstance()
 					.getPreparedStatement(builder.toString());
 
 			// executando
 			linhasAfetadas = preparedStatement.executeUpdate();
 			preparedStatement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return linhasAfetadas;
 	}
 
 	/**
 	 * Método de busca que recebe um ou vários parâmetros PrimaryKey para serem
 	 * inseridos ao filtro. A ordem dos parâmetros é a mesma do array informado
 	 * na subclasse.
 	 * 
 	 * @param primaryKey
 	 * @return
 	 */
 	protected Map<String, Object> _buscarPorId(Integer... primaryKey) {
 		Map<Object, Object> campoValor = new HashMap<Object, Object>(
 				primaryKey.length);
 
 		// mapeando id - valor
 		for (int i = 0; i < primaryKey.length; i++) {
 			campoValor.put(this.primaryKey[i], primaryKey[i]);
 		}
 
 		return this.__buscarPorId(campoValor);
 	}
 
 	/**
 	 * Efetua uma busca por ID
 	 * 
 	 * "SELECT * FROM WHERE primaryKey(s)"
 	 * 
 	 * utilizando o(s) Id(s) informados na subclasse e o HashMap enviado como
 	 * parâmetro.
 	 * 
 	 * @param campoValor
 	 *            HashMap com o nome da primary key e o valor do id
 	 * @return Map com os valores vindos do ResultSet
 	 */
 	private Map<String, Object> __buscarPorId(Map<Object, Object> campoValor) {
 		this.verificaNomeTabela();
 		this.verificaPK();
 
 		ResultSet resultSet = null;
 		StringBuilder builder = new StringBuilder();
 		Map<String, Object> map = new HashMap<String, Object>();
 
 		try {
 			builder.append("SELECT * FROM ");
 			builder.append(this.nomeDaTabela);
 			builder.append(" WHERE ");
 
 			// Inserindo os Ids
 			builder.append(this.montaParteQueryID(campoValor));
 			builder.append(";");
 
 			PreparedStatement preparedStatement = DAOUtil.getInstance()
 					.getPreparedStatement(builder.toString());
 
 			resultSet = preparedStatement.executeQuery();
 
 			if (!resultSet.next()) {
 				resultSet.close();
 				preparedStatement.close();
 				return null;
 			}
 
 			// inserindo atributos no hashMap
 			this.preencherMap(map, resultSet, this.getAtributos());
 
 			resultSet.close();
 			preparedStatement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return map;
 	}
 
 	/**
 	 * Retorna uma Lista de Map de cada registro
 	 * 
 	 * @param campoValor
 	 *            Map com os campos e seus respectivos valores (Não insira a
 	 *            Primary Key)
 	 * @return
 	 */
 	protected List<Map<String, Object>> _listarPor(
 			Map<Object, Object> campoValor) {
 		this.verificaNomeTabela();
 		this.verificaPK();
 
 		ResultSet resultSet = null;
 		StringBuilder builder = new StringBuilder();
 		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
 		List<Object> ordem = new ArrayList<Object>();
 
 		try {
 			builder.append("SELECT * FROM ");
 			builder.append(this.nomeDaTabela);
 			builder.append(" WHERE true");
 
 			for (int i = 0; i < this.campos.length; i++) {
 				// inserindo os valores em um array
 				if (campoValor.get(campos[i]) != null) {
 					ordem.add(campoValor.get(campos[i]));
 
 					builder.append(" AND ");
 					// se for string use o LIKE
 					if (campoValor.get(campos[i]).getClass().getName()
 							.equals("java.lang.String")) {
 						builder.append(campos[i] + " LIKE ?");
 					} else {
 						builder.append(campos[i] + " = ?");
 					}
 				}
 			}
 
 			builder.append(";");
 
 			PreparedStatement preparedStatement = DAOUtil.getInstance()
 					.getPreparedStatement(builder.toString());
 
 			// preparando o statement
 			this.prepareStatement(preparedStatement, ordem, true);
 
 			resultSet = preparedStatement.executeQuery();
 
 			// Iterando os valores retornados no resultSet
 			while (resultSet.next()) {
 				Map<String, Object> map = new HashMap<String, Object>();
 
 				// inserindo atributos no Map
 				this.preencherMap(map, resultSet, this.getAtributos());
 
 				// inserindo o hashMap no arrayList
 				listMap.add(map);
 			}
 
 			resultSet.close();
 			preparedStatement.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return listMap;
 	}
 
 	/**
 	 * Preenche Map com os dados vindos do ResultSet, usando array como chave
 	 * para busca
 	 */
 	protected void preencherMap(Map<String, Object> map, ResultSet rs,
 			String[] chaves) {
 
 		try {
 			for (int i = 0; i < chaves.length; i++) {
 				map.put(chaves[i], rs.getObject(chaves[i]));
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Executa query retornando um Map do ResultSet
 	 * 
 	 * @param query
 	 *            SQL
 	 * @param atributos
 	 *            Atributos que serão procurados no ResultSet para inserir no
 	 *            Map
 	 * @return
 	 */
 	protected List<Map<String, Object>> executarQuery(String query,
 			String[] atributos) {
 
 		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
 
 		try {
 			Statement s = DAOUtil.getInstance().getStatement();
 			ResultSet rs = s.executeQuery(query);
 
 			while (rs.next()) {
 				// preencher map com primary key e campos vindos do resultSet
 				Map<String, Object> map = new HashMap<String, Object>();
 				this.preencherMap(map, rs, atributos);
 
 				listMap.add(map);
 			}
 
 			rs.close();
 			s.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		// lista
 		return listMap;
 	}
 
 	/**
 	 * Executa query retornando um Map do ResultSet utilizando os atributos
 	 * desta DAO filha para o mapeamento.
 	 * 
 	 * @param query
 	 *            SQL
 	 * @return
 	 */
 	protected List<Map<String, Object>> executarQuery(String query) {
 		return this.executarQuery(query, this.getAtributos());
 	}
 
 	protected List<Map<String, Object>> executarQueryPreparada(String query,
 			Object[] ordem, String[] primaryKey, String[] campos) {
 		/**
 		 * TODO: DESENVOLVER = Executar query Preparada com caracters curingas,
 		 * recebendo um array com a ordem dos valores a serem inseridos e
 		 * retornando um map dos seus registros. Isto será bastante usado para
 		 * relatórios
 		 */
 		return null;
 	}
 
 	/**
 	 * Executa Query livre retornando a quantidade de linhas afetadas
 	 * 
 	 * @param query
 	 *            SQL
 	 * @return
 	 */
 	protected int executarUpdate(String query) {
 		int linhasAfetadas = 0;
 		try {
 			Statement s = DAOUtil.getInstance().getStatement();
 			linhasAfetadas = s.executeUpdate(query);
 			s.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return linhasAfetadas;
 	}
 
 }
