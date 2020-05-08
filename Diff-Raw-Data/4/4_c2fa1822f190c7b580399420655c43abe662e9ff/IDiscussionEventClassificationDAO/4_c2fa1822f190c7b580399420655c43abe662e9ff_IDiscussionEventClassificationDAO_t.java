 package org.computer.knauss.reqtDiscussion.io;
 
import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEventClassification;
 
 public interface IDiscussionEventClassificationDAO {
 
 	public DiscussionEventClassification[] getClassificationsForDiscussionEvent(
			DiscussionEvent de) throws DAOException;
 
 	public void storeDiscussionEventClassification(
 			DiscussionEventClassification classification) throws DAOException;
 
 }
