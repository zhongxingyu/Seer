 package org.ebag.runtime.handler;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.mina.common.IoSession;
 import org.ebag.net.obj.I;
 import org.ebag.net.obj.exam.ExamObj;
 import org.ebag.net.obj.exam.ProblemInfoObj;
 import org.ebag.net.request.ExamRequet;
 import org.ebag.net.response.ExamResponse;
 
 import ebag.pojo.Eexam;
 import ebag.pojo.EexamDAO;
 import ebag.pojo.Eproblem;
 import ebag.pojo.EproblemDAO;
 import ebag.pojo.EuserDAO;
 import ebag.pojo.Examactivity;
 import ebag.pojo.ExamactivityDAO;
 
 public class ExamRequestHandler extends BasicHandler<ExamRequet> {
 
 	public ExamRequestHandler(IoSession session, Object message) {
 		super(session, message);
 	}
 
 	@Override
 	public void handle() {
 		ExamResponse res = new ExamResponse();
 		List<ExamObj> lst = new ArrayList<ExamObj>();
 		res.setExamList(lst);
 		EexamDAO edao = new EexamDAO();
 		ExamactivityDAO adao = new ExamactivityDAO();
 		EuserDAO udao=new EuserDAO();
		try {
 			if (message.getIdList() == null || message.getIdList().size() == 0) {
 				if(message.isTeacher){
 					Iterator<Eexam> it=udao.findById(message.getUid()).getEexams().iterator();
 					while(it.hasNext()){
 						Eexam curr=it.next();
 						res.getExamList().add(getExamObj(curr));
 					}
 				}else{
 					Iterator<Examactivity> it=udao.findById(message.getUid()).getExamactivities().iterator();
 					while(it.hasNext()){
 						Eexam curr=it.next().getEexam();
 						res.getExamList().add(getExamObj(curr));
 					}
 				}
 			}else {
 				for(int id:message.getIdList()){
 					Eexam curr=edao.findById(id);
 					res.getExamList().add(getExamObj(curr));
 				}
 			}
		} catch (Exception e) {
			System.out.println(e);
		}
 		session.write(res);
 	}
 
 	private ExamObj getExamObj(Eexam curr){
 		ExamObj obj=new ExamObj();
 		obj.setId(curr.getId());
 		obj.setName(curr.getName());
 		obj.setTime(curr.getTime());
 		obj.setType(curr.getType());
 		obj.setpInfoList(new ArrayList<ProblemInfoObj>());
 		fillProblems(curr,obj);
 		return obj;
 	}
 	
 	private void fillProblems(Eexam curr,ExamObj obj){
 		Iterator it=curr.getEproblems().iterator();
 		while(it.hasNext()){
 			Eproblem p=(Eproblem) it.next();
 			ProblemInfoObj pobj=new ProblemInfoObj();
 			pobj.setId(p.getId());
 			if(p.getType()==0||p.getType()==I.choice.problemType_xz){
 				pobj.setAnswer("A");//TODO fix
 				pobj.setType(I.choice.problemType_xz);
 			}else{
 				pobj.setType(I.choice.problemType_jd);
 			}
 			pobj.setPoint(1);//TODO fix
 			obj.setPoints(obj.getPoints()+pobj.getPoint());
 		}
 	}
 }
