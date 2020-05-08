 package org.kalibro.service;
 
 import java.util.List;
 
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 
 import org.kalibro.Kalibro;
 import org.kalibro.core.persistence.dao.ProjectDao;
 import org.kalibro.service.entities.ProjectXml;
 import org.kalibro.service.entities.RawProjectXml;
 
 @WebService
 public class ProjectEndpointImpl implements ProjectEndpoint {
 
 	private ProjectDao dao;
 
 	public ProjectEndpointImpl() {
 		this(Kalibro.getProjectDao());
 	}
 
 	protected ProjectEndpointImpl(ProjectDao projectDao) {
 		dao = projectDao;
 	}
 
 	@Override
 	public void saveProject(@WebParam(name = "project") RawProjectXml project) {
 		dao.save(project.convert());
 	}
 
 	@Override
 	@WebResult(name = "projectName")
 	public List<String> getProjectNames() {
 		return dao.getProjectNames();
 	}
 
 	@Override
 	@WebResult(name = "hasProject")
	public boolean hasProject(@WebParam(name = "projectName") String projectName) {
 		return dao.hasProject(projectName);
 	}
 
 	@Override
 	@WebResult(name = "project")
 	public ProjectXml getProject(@WebParam(name = "projectName") String projectName) {
 		return new ProjectXml(dao.getProject(projectName));
 	}
 
 	@Override
 	public void removeProject(@WebParam(name = "projectName") String projectName) {
 		dao.removeProject(projectName);
 	}
 }
