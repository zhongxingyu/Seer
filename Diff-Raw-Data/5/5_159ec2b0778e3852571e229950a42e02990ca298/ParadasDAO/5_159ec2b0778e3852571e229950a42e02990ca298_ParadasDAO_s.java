 package br.com.paxtecnologia.pma.relatorio.dao;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import br.com.paxtecnologia.pma.relatorio.util.FormataData;
 import br.com.paxtecnologia.pma.relatorio.vo.ParadasPorTipoVO;
 import br.com.paxtecnologia.pma.relatorio.vo.UltimoAnoVO;
 
 public class ParadasDAO {
 	private DataSourcePMA connection;
 
 	public Calendar getDataUltimoPNP(Integer idCliente, String mesRelatorio) {
 		Date data = null;
 		Calendar cal = Calendar.getInstance();
 		connection = new DataSourcePMA();
 		PreparedStatement pstmt;
 		String sql = "SELECT data_ult_parada FROM pmp_sem_parada "+
 			         "WHERE cliente_id = ? "+
 			         "  and trunc(data_insercao,'MM') = trunc(?,'MM')";
 		pstmt = connection.getPreparedStatement(sql);
 		try {
 			pstmt.setInt(1, idCliente);
 			pstmt.setDate(2, FormataData.formataDataInicio(mesRelatorio));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ResultSet rs = connection.executaQuery(pstmt);
 		try {
 			while (rs.next()) {
 				data = rs.getDate("data_ult_parada");
 
 				cal.setTimeInMillis(data.getTime());
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		connection.closeConnection(pstmt);
 		return cal;
 	}
 
 	public Integer getQtdeParadaEvitadasTotal(Integer idCliente,
 			String mesRelatorio) {
 		Integer retorno = null;
 		connection = new DataSourcePMA();
 		PreparedStatement pstmt;
 		String sql = "SELECT qtd_pe FROM pmp_sem_parada WHERE cliente_id = ? and trunc(data_insercao,'MM') = trunc(?,'MM')";
 		pstmt = connection.getPreparedStatement(sql);
 		try {
 			pstmt.setInt(1, idCliente);
 			pstmt.setDate(2, FormataData.formataDataInicio(mesRelatorio));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ResultSet rs = connection.executaQuery(pstmt);
 		try {
 			while (rs.next()) {
 				retorno = rs.getInt("qtd_pe");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		connection.closeConnection(pstmt);
 		return retorno;
 	}
 
 	public List<ParadasPorTipoVO> getListaParadasPorTipo(
 			Integer idCliente, String mesRelatorio, String tipo) {
 		connection = new DataSourcePMA();
 		PreparedStatement pstmt;
 		String sql = "SELECT c.chamado, " +
 					 "to_char(c.data_criacao, 'dd/mm/yyyy') data, " +
 					 "to_char(a.data_inicio_parada, 'dd/mm/yyyy') data_parada, " +
 					 "round(to_number(a.data_fim_parada - a.data_inicio_parada) * 24,2) segundos_trabalhados, " +
 					 "pmp_get_hosts_task(c.task_id) nome_fantasia, " +
 					 "c.titulo " +
 					 "FROM pmp_task_parada a, pmp_parada b, pmp_task c, pmp_task_host d, pmp_host e, pmp_host_ambiente f " +
 					 "WHERE a.parada_id = b.parada_id " +
 					 "AND a.task_id = c.task_id " +
 					 "AND a.task_id = d.task_id " +
 					 "AND d.host_id = e.host_id " +
 					 "AND e.host_id = f.host_id " +
 					 "AND f.ambiente_id = 3 " + //producao
 					 "AND c.cliente_id = ? " +
 					 "AND c.data_insercao between ? and ? " +
 					 "AND trunc(c.data_insercao,'MM') = trunc(a.data_inicio_parada,'MM') " +
 					 "AND regexp_like(b.tipo_parada,?)";
 		pstmt = connection.getPreparedStatement(sql);
 		try {
 			pstmt.setInt(1, idCliente);
 			pstmt.setDate(2, FormataData.formataAnoInicio(mesRelatorio));
 			pstmt.setDate(3, FormataData.formataDataInicio(mesRelatorio));
			pstmt.setString(4, "^"+tipo);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ResultSet rs = connection.executaQuery(pstmt);
 		List<ParadasPorTipoVO> listaParadasPorTipoVO  = new ArrayList<ParadasPorTipoVO>();
 		try {
 			while (rs.next()) {
 				ParadasPorTipoVO paradasPorTipoVO = new ParadasPorTipoVO();
 				paradasPorTipoVO.setIdchamado(rs.getString("chamado"));
 				paradasPorTipoVO.setData(rs.getString("data"));
 				paradasPorTipoVO.setDataParada(rs.getString("data_parada"));
 				paradasPorTipoVO.setHoras(rs.getDouble("segundos_trabalhados"));
 				paradasPorTipoVO.setHost(rs.getString("nome_fantasia"));
 				paradasPorTipoVO.setDescricao(rs.getString("titulo"));
 				listaParadasPorTipoVO.add(paradasPorTipoVO);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		connection.closeConnection(pstmt);
 		return listaParadasPorTipoVO;
 	}
 
 	public List<UltimoAnoVO> getListaUltimosAnosHoras(Integer idCliente, String tipo, String mesRelatorio) {
 		connection = new DataSourcePMA();
 		PreparedStatement pstmt;
 		String sql = "SELECT to_char(c.data_insercao, 'yyyy') data, " +
 					 "round(sum(to_number(a.data_fim_parada - a.data_inicio_parada)) * 24,2) horas_trabalhadas " +
 					 "FROM pmp_task_parada a, pmp_parada b, pmp_task c, pmp_task_host d, pmp_host e, pmp_host_ambiente f " +
 					 "WHERE a.parada_id = b.parada_id " +
 					 "AND a.task_id = c.task_id " +
 					 "AND a.task_id = d.task_id " +
 					 "AND d.host_id = e.host_id " +
 					 "AND e.host_id = f.host_id " +
 					 "AND f.ambiente_id = 3 " + //producao
 					 "AND c.cliente_id = ? " +
 					 "AND regexp_like(b.tipo_parada,?) " +
 					 "AND c.data_insercao <= ?" +
 					 "group by to_char(c.data_insercao, 'yyyy')";
 		pstmt = connection.getPreparedStatement(sql);
 		try {
 			pstmt.setInt(1, idCliente);
			pstmt.setString(2, "^"+tipo);
 			pstmt.setDate(3, FormataData.formataDataInicio(mesRelatorio));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ResultSet rs = connection.executaQuery(pstmt);
 		List<UltimoAnoVO> listaUltimoAnoVO  = new ArrayList<UltimoAnoVO>();
 		try {
 			while (rs.next()) {
 				UltimoAnoVO ultimoAnoVO = new UltimoAnoVO();
 				ultimoAnoVO.setAno(rs.getString("data"));
 				ultimoAnoVO.setHoras(rs.getDouble("horas_trabalhadas"));
 				listaUltimoAnoVO.add(ultimoAnoVO);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		connection.closeConnection(pstmt);
 		return listaUltimoAnoVO;
 	}
 
 }
