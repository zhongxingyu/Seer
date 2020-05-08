 //package com.nbcedu.function.schoolmaster2.utils;
 //
 //import java.util.ArrayList;
 //import java.util.Collection;
 //import java.util.HashMap;
 //import java.util.List;
 //import java.util.Map;
 //
 //import org.apache.log4j.Logger;
 //
 //import com.nbcedu.function.schoolmaster2.vo.DepartmentVo;
 //import com.nbcedu.integration.uc.client.facade.BaseClient;
 //import com.nbcedu.integration.uc.client.vo.NbcUcDepartment;
 //import com.nbcedu.integration.uc.client.vo.NbcUcPerson;
 //import com.nbcedu.integration.uc.client.vo.NbcUcTreeNode;
 //
//public class UCService {
 //	private static final BaseClient client = BaseClient.getInstance();
 //	
 //
//	private static final Logger logger = Logger.getLogger(UCService.class);
 //
 //	public static String getPersonJson(final Collection<String> checkedUids,final boolean b){
 //		
 //		class CloumnTree {
 //			
 //			StringBuilder jsonString = new StringBuilder();
 //			
 //			public String getCloumnTreeXmlString() {
 //				jsonString.append("[");
 //				List<NbcUcTreeNode> list = getTreeNode("root");
 //				for (NbcUcTreeNode nbcUcTreeNode : list) {
 //					disk_NbcUcTreeNode(nbcUcTreeNode);
 //				}
 //				jsonString.append("]");
 //				return jsonString.toString();
 //			}
 //
 //			private void disk_NbcUcTreeNode(NbcUcTreeNode treeNode) {
 //				List<NbcUcTreeNode> list = getTreeNode(treeNode.getId());
 //				if(Checked(treeNode.getId().replace("u|", "").replace("ne|", "")) && b){
 //					
 //				}else{
 //					jsonString.append("{");
 //					jsonString.append("\"id\":");
 //					jsonString.append("\"");
 //					jsonString.append(treeNode.getId().replace("u|", ""));
 //					jsonString.append("\"");
 //					jsonString.append(",\"text\":");
 //					jsonString.append("\"");
 //					jsonString.append(treeNode.getTitle());
 //					jsonString.append("\"");
 //					if(Checked(treeNode.getId().replace("u|", ""))){
 //						jsonString.append(",\"checked\":true");
 //					}
 //					
 //					if(list!=null && list.size()>0){
 //						jsonString.append(",\"children\":[");
 //						for (NbcUcTreeNode tNode : list) {
 //							disk_NbcUcTreeNode(tNode);
 //						}
 //						jsonString.append("]");
 //					}
 //					jsonString.append("},");
 //				}
 //			}
 //
 //			List<NbcUcTreeNode> getTreeNode(String pid) {
 //				List<NbcUcTreeNode> list = client.queryDepartTree(pid, Boolean.TRUE);
 //				return list;
 //			}
 //			
 //			boolean Checked(String id){
 //				return checkedUids!=null
 //					&&!checkedUids.isEmpty()
 //					&& checkedUids.contains(id);
 //			}
 //			
 //		}
 //		
 //		String userJson = new CloumnTree().getCloumnTreeXmlString();
 //		if(userJson!=null){
 //			userJson=userJson.replaceAll(",]","]");
 //		}
 //		return userJson;
 //		
 //	}
 //	
 //	/**
 //	 * 教师组织机构树
 //	 * @return
 //	 * @author xuechong
 //	 */
 //	public static String getPersonJsonString() {
 //		return getPersonJson(null,false);
 //	}
 //	
 //	
 //	public static String findNameByUid(String uid){
 //		return uid.equals("1") ? "admin" : new Object(){
 //			public String getName(String uid){
 //				NbcUcPerson p = client.queryPerson(uid);
 //				logger.info(p);
 //				return p!=null?p.getName():"";
 //			}
 //		}.getName(uid);
 //	}
 //	
 //	public static Map<String,String> findDepartmentByUid(String uid){
 //		NbcUcDepartment l= client.queryDepartmentByUid(uid).get(0);
 //		Map<String,String> m = new HashMap<String,String>();
 //		m.put("id",l.getId() );
 //		m.put("name", l.getName());
 //		return m;
 //	}
 //	public static List<DepartmentVo> findDepartment(){
 //		List<NbcUcTreeNode> l = client.queryDepartTree("root", Boolean.FALSE);
 //		List<DepartmentVo> list = new ArrayList<DepartmentVo>();
 //		DepartmentVo d;
 //		for(NbcUcTreeNode n : l){
 //			d = new DepartmentVo();
 //			d.setId(n.getId().replace("ne|", ""));
 //			d.setName(n.getTitle());
 //			list.add(d);
 //		}
 //		return list;
 //	}
 //}
