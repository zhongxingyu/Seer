 package org.vfny.geoserver.issues;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 import org.vfny.geoserver.issues.dao.IssuesDao;
 
 
 public class IssueService implements IIssueService {
 
     private IssuesDao issuesDao;
     
     public Collection<IIssue> getIssues(){
         return issuesDao.getAllIssues();
     }
     
     public Collection<IIssue> getIssues(Target target ){
         return issuesDao.findByTarget(target);
     }
 
     public void addIssues(List<IIssue> issues){
         issuesDao.insertIssues(issues);
     }
 
     public void modifyIssue(IIssue issue){
         issuesDao.updateIssue(issue);
     }
 
     public void removeIssues(Collection<IIssue> issues){
         issuesDao.removeIssues(issues);
     }
     
    public void setIssuesDao(IssuesDao issuesDao){
        this.issuesDao = issuesDao;
     }
 
    public IssuesDao getIssuesDao(){
        return issuesDao;
    }
 
     
 }
