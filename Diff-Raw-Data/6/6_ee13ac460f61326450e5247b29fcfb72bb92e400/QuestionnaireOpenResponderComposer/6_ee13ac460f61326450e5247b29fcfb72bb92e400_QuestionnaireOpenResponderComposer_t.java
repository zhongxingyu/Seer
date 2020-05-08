 package org.dongq.analytics.ui;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dongq.analytics.model.Responder;
 import org.dongq.analytics.utils.DbHelper;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.util.GenericForwardComposer;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Grid;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Row;
 import org.zkoss.zul.RowRenderer;
 
 public class QuestionnaireOpenResponderComposer extends GenericForwardComposer {
 
 	private static final Log logger = LogFactory.getLog(QuestionnaireOpenResponderComposer.class);
 	private static final long serialVersionUID = 1L;
 
 	private Grid grid;
 	private String version;
 	
 	@Override
 	public void doAfterCompose(Component comp) throws Exception {
 		super.doAfterCompose(comp);
 
 		version = (String)requestScope.get("version");
 		if(comp instanceof Grid) {
 			grid = (Grid)comp;
 			logger.debug(grid);
 			logger.debug(requestScope.containsKey("version"));
 			logger.debug(requestScope.get("version"));
 			
 			grid.setModel(new ListModelList(getList()));
 			grid.setRowRenderer(new ResponderRowRenderer());
 		}
 	}
 	
 	List<Responder> getList() {
 		List<Responder> list = new ArrayList<Responder>();
 		try {
 			QueryRunner query = new QueryRunner();
 			String sql = "select * from responder where responder_person is null and version = " + version;
 			logger.debug(sql);
 			list = query.query(DbHelper.getConnection(), sql, new ResultSetHandler<List<Responder>>() {
 				@Override
 				public List<Responder> handle(ResultSet rs) throws SQLException {
 					List<Responder> list = new ArrayList<Responder>();
 					while(rs.next()) {
 						Responder e = new Responder();
 						e.setId(rs.getLong("responder_id"));
 						e.setName(rs.getString("responder_name"));
 						e.setVersion(rs.getLong("version"));
 						list.add(e);
 					}
 					return list;
 				}
 			});
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	class ResponderRowRenderer implements RowRenderer {
 		final String pattern = "yyyy-MM-dd HH:mm:ss";
 		@Override
 		public void render(Row row, Object data) throws Exception {
 			Responder e = (Responder)data;
 			row.appendChild(new Label(e.getName()));
 			row.appendChild(new Label(DateFormatUtils.format(Long.valueOf(e.getId()), pattern)));
 			row.appendChild(new Label(String.valueOf(e.getVersion())));
 			
			Button btn = new Button("查看");
			btn.setVisible(false);
			btn.setHref("open.zul?rid="+e.getId());
			btn.setTarget("blank");
			row.appendChild(btn);
 		}
 		
 	}
 }
