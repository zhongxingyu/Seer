 package com.asu.edu.base.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.web.bind.ServletRequestBindingException;
 import org.springframework.web.bind.ServletRequestUtils;
 
 import com.asu.edu.base.dao.BaseDAO;
 import com.asu.edu.base.dao.intrf.FileDAOImplInterface;
 import com.asu.edu.base.vo.FileVO;
 import com.asu.edu.constants.SQLConstants;
 
 public class FileDAOImpl extends BaseDAO implements FileDAOImplInterface {
 	String calledFunction;
 
 	@Override
 	protected Object toDataObject(ResultSet rs) throws SQLException {
 
 		if (calledFunction == "getFile") {
 			FileVO fileVO = new FileVO();
 			fileVO.setFileName(rs.getString("FILE_NAME"));
 			fileVO.setDeptId(rs.getInt("DEPT_ID"));
 			fileVO.setId(rs.getInt("FILE_ID"));
 			fileVO.setOwnerId(rs.getInt("OWNER_ID"));
 			fileVO.setPath(rs.getString("PATH"));
 			fileVO.setType(rs.getString("TYPE"));
 			return fileVO;
 		}
 		if (calledFunction == "getParentFilePath") {
 			return rs.getString("PATH");
 		}
 
 		return null;
 	}
 
 	public FileVO getFile(HttpServletRequest request) {
 		calledFunction = "getFile";
 		String sql = SQLConstants.GET_FILE_FOR_DOWNLOAD;
 		Object[] params = new Object[1];
 		try {
 			params[0] = ServletRequestUtils.getRequiredIntParameter(request,
 					"id");
 		} catch (ServletRequestBindingException e) {
 			e.printStackTrace();
 		}
 		FileVO vo = (FileVO) getRowByCriteria(sql, params);
 
 		return vo;
 	}
 
 	public String getParentFilePath(int id) {
 		calledFunction = "getParentFilePath";
 		Object[] param = new Object[1];
 		param[0] = id;
 		String sql = SQLConstants.GET_FILE_PATH;
 		String path = (String) getRowByCriteria(sql, param);
 		return path;
 	}
 
 	public boolean saveFile(FileVO fileVO) {
 		Object[] param = new Object[7];
 		param = new Object[7];
 		param[0] = fileVO.getPath();
 		param[1] = fileVO.getOwnerId();
 		param[2] = fileVO.getDeptId();
 		param[3] = fileVO.getParentId();
 		param[4] = fileVO.getFileName();
 		param[5] = new java.sql.Timestamp(new java.util.Date().getTime());
 		param[6] = fileVO.getContentType();
		param[7] = new java.sql.Timestamp(new java.util.Date().getTime());
 		String sql = SQLConstants.SAVE_FILE;
 		return preparedStatementUpdate(sql, param, true) > 0;
 
 	}
 
 }
