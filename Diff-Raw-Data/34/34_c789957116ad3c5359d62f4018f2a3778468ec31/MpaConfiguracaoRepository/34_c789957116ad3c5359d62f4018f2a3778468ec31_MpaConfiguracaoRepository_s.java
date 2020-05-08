 package mpa.manager.repository;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import mpa.manager.bean.MpaConfiguracao;
 
 
 public class MpaConfiguracaoRepository extends Repository<MpaConfiguracao> {
 
 	private static MpaConfiguracaoRepository instance;
 
 	MpaConfiguracaoRepository(Connection connection) throws SQLException {
 		super(connection);
 	}
 
 	@Override
 	public MpaConfiguracao materializa(ResultSet rs) throws SQLException {
 		return new MpaConfiguracao(rs.getInt("id"), rs.getDate("data_inicio"),
 				rs.getDate("data_fim"));
 	}
 
 	@Override
 	public String getSql() {
 		return "select * from mpa_configuracao";
 	}
 
 	public static void createInstance(Connection connection)
 			throws SQLException {
 		instance = new MpaConfiguracaoRepository(connection);
 	}
 
 	public static MpaConfiguracaoRepository getInstance() {
 		if (instance == null)
 			throw new IllegalStateException("MpaConfiguracaoRepository não foi instanciado ainda. Utilize createInstance(connection)");
 
 		return instance;
 	}
 
 	public void insert(MpaConfiguracao mpa) throws SQLException {
 		validaMpa(mpa);
 
 		PreparedStatement statement = null;
 		try {
 			statement = preparaStatement(mpa,"INSERT INTO MPA_CONFIGURACAO VALUES (NULL, ?, ?)");
 			statement.execute();
 		} finally {
 			statement.close();
 		}
 
 		statement = null;
 		ResultSet rs = null;
 		try {
 			statement = preparaStatement(mpa,"SELECT * FROM MPA_CONFIGURACAO WHERE DATA_INICIO = ? AND DATA_FIM = ?");
 			rs = statement.executeQuery();
 			rs.next();
 			mpa.setId(rs.getInt("ID"));
 		} finally {
 			statement.close();
 			rs.close();
 		}
 	}
 
 	private void validaMpa(MpaConfiguracao mpa) throws SQLException {
 		String select = "SELECT COUNT(*) FROM MPA_CONFIGURACAO WHERE DATA_FIM >= ?";
 
 		PreparedStatement statement = null;
 		ResultSet rs = null;
 		try {
 			statement = connection.prepareStatement(select);
 			statement.setDate(1, new Date(mpa.getDataInicio().getTime()));
 			rs = statement.executeQuery();
 			rs.next();
 			if (rs.getInt(1) != 0)
				throw new IllegalStateException("Já existe um Mpa cobrindo esta faixa de datas.");
 		} finally {
 			rs.close();
 			statement.close();
 		}
 	}
 
 	private PreparedStatement preparaStatement(MpaConfiguracao mpa, String query) throws SQLException {
 		PreparedStatement statement = connection.prepareStatement(query);
 		statement.setDate(1, new Date(mpa.getDataInicio().getTime()));
 		statement.setDate(2, new Date(mpa.getDataFim().getTime()));
 		return statement;
 	}
 }
