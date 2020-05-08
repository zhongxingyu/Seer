 package br.com.puc.sispol.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.puc.sispol.ConnectionFactory;
 import br.com.puc.sispol.modelo.Resposta;
 import br.com.puc.sispol.modelo.Resultado;
 import br.com.puc.sispol.modelo.Simulado;
 import br.com.puc.sispol.modelo.Usuario;
 
 public class ResultadoDAO {
 	private final Connection connection;
 	private UsuarioDAO daoUsuario;
 	
 	public ResultadoDAO() {
 		
 		try {
 			this.connection = new ConnectionFactory().getConnection();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Resultado populaResultado(ResultSet rs) throws SQLException {
 		Resultado resultado = new Resultado();
 		try {
 			// popula o objeto tarefa
 			System.out.println("Nota do Simulado: "+rs.getInt("NotaDoSimulado"));
 			resultado.setCodResultado(rs.getLong("CodResultado"));
 			resultado.setNotaDoSimulado(rs.getInt("NotaDoSimulado"));
 			
 			resultado.setUsuario(new Usuario());
 			resultado.getUsuario().setNome(rs.getString("Nome"));
 			
 			return resultado;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	// UCS - Realizar Simulado Inscrito
 	public Resultado busca(Long codUsuario, Simulado simulado) {
 		Resultado resultado = new Resultado();
 		try {
 			System.out.println("Consulta Resultado...");
 			PreparedStatement stmt = this.connection.prepareStatement("select "
 					+ "    * " + " from " + "     Resultado AS r INNER JOIN Usuario AS u ON (r.CodUsuario = u.CodUsuario)" + " WHERE "
 					+ " r.CodUsuario = ?   " + " AND r.CodSimulado = ?");
 			// popula o objeto tarefa
 
 			stmt.setLong(1, codUsuario);
 			stmt.setLong(2, simulado.getCodSimulado());
 			
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 				resultado = populaResultado(rs);
 			}
 
 			System.out.println("Consulta respostas...");
 			List<Resposta> respostas = new ArrayList<Resposta>();
 			stmt = this.connection.prepareStatement("	SELECT " + " * "
 					+ " FROM " + "		sispol.Resposta AS r " + "		 " + " WHERE "
 					+ "		CodResultado = ? " + "");
 			stmt.setLong(1, resultado.getCodResultado());
 			// System.out.println(stmt);
 			rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				Resposta resposta = new Resposta();
 
 				// popula o objeto resposta
 				resposta.setCodResposta(rs.getLong("CodResposta"));
 				resposta.setOpcaoEscolhida(rs.getString("OpcaoEscolhida"));
 
 				respostas.add(resposta);
 			}
 
 			resultado.setRespostas(respostas);
 
 			rs.close();
 			stmt.close();
 
 			return resultado;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void adicionaResposta(Resposta resposta) {
 		String sql = "insert into Resposta (CodQuestao, CodResultado, OpcaoEscolhida) values (?,?,?)";
 		PreparedStatement stmt;
 		try {
 			stmt = connection.prepareStatement(sql);
 
 			stmt.setLong(1, resposta.getQuestao().getCodQuestao());
 			stmt.setLong(2, resposta.getResultado().getCodResultado());
 			stmt.setString(3, resposta.getOpcaoEscolhida());
 
 			stmt.execute();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void apagaRespostas(Resultado resultado) {
 
 		String sql = "DELETE FROM sispol.Resposta WHERE CodResultado = ?";
 		PreparedStatement stmt;
 
 		try {
 
 			stmt = connection.prepareStatement(sql);
 			stmt.setLong(1, resultado.getCodResultado());
 			stmt.executeUpdate();
 
 		} catch (SQLException e) {
 
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void apura() {
 
 		try {
 			System.out.println("Apura resultados...");
 			PreparedStatement stmt = this.connection
 					.prepareStatement(" SELECT "
 							+ "		tbResul.CodResultado,"
 							+ "		COUNT(*) AS NotaDoSimulado "
 							+ "	FROM "
 							+ " 	sispol.Resultado tbResul "
 							+ "   	INNER JOIN sispol.Resposta tbResp "
 							+ "   		ON (tbResul.CodResultado = tbResp.CodResultado) "
 							+ "    	INNER JOIN sispol.Questao tbQ "
 							+ "       	ON (tbResp.CodQuestao = tbQ.CodQuestao) "
 							+ "		INNER JOIN sispol.Simulado tbS "
 							+ "			ON (tbResul.CodSimulado = tbS.CodSimulado) "
 							+ "	WHERE "
 							+ "    	tbResp.OpcaoEscolhida = tbQ.OpcaoCorreta "
							+ "    	AND tbResul.NotaDoSimulado = '0' "
 							+ "		AND concat(curdate(), ' ' , curtime()) > DATE_ADD(TIMESTAMP(concat(tbS.DataDeRealizacao, ' ' , tbS.HoraDeRealizacao)), INTERVAL tbS.Duracao HOUR) "
 							+ "	GROUP BY 1 ");
			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 
 				System.out.println("Armazena Nota Simulado em Resultado.");
 
 				String sql = "UPDATE sispol.Resultado SET NotaDoSimulado = ? WHERE CodResultado = ?";
 
 				stmt = connection.prepareStatement(sql);
 
 				stmt.setLong(1, rs.getLong("NotaDoSimulado"));
 				stmt.setLong(2, rs.getLong("CodResultado"));
 
 				stmt.execute();
 			}
 
 			rs.close();
 			stmt.close();
 
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// UCS - Consultar Classificacao oficial
 	public List<Resultado> buscaResultadosDoSimulado(Simulado simulado) {
 
 		try {
 			List<Resultado> resultados = new ArrayList<Resultado>();
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select * from Resultado AS s INNER JOIN Usuario AS u ON s.CodUsuario = u.CodUsuario  WHERE CodSimulado = ? AND NotaDoSimulado IS NOT NULL ORDER BY s.NotaDoSimulado DESC,u.Nome ASC");
 			stmt.setLong(1, simulado.getCodSimulado());
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				resultados.add(populaResultado(rs));
 			}
 			
 			
 			rs.close();
 			stmt.close();
 
 			return resultados;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 }
