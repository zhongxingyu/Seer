 package org.opensixen.dev.omvc.server;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.opensixen.dev.omvc.interfaces.IRevisionDownloader;
 import org.opensixen.dev.omvc.model.Project;
 import org.opensixen.dev.omvc.model.Revision;
 import org.opensixen.dev.omvc.model.Script;
 import org.opensixen.dev.omvc.util.HSession;
 
 public class RevisionDownloader extends AbstractRienaServer implements IRevisionDownloader {
 
 	
 	public RevisionDownloader() {
 		super(IRevisionDownloader.class.getName(), IRevisionDownloader.path);
 	}
 
 	@Override
 	public List<Revision> getRevisions(int project_ID, int from) {
 		Project project = HSession.get(Project.class, project_ID);
 		Criteria crit = HSession.getCriteria(Revision.class);
 		crit.add(Restrictions.eq("project", project));
 		crit.add(Restrictions.gt("revision_ID", from));
		crit.addOrder(Order.asc("revision_ID"));
 		return crit.list();
 	}
 
 	@Override
 	public List<Script> getScripts(Revision revision, String[] engines) {
 		Criteria crit = HSession.getCriteria(Script.class);
 		crit.add(Restrictions.eq("revision", revision));
 		crit.add(Restrictions.in("engine", engines));
		crit.addOrder(Order.asc("script_ID"));
 		return crit.list();
 	}
 
 }
