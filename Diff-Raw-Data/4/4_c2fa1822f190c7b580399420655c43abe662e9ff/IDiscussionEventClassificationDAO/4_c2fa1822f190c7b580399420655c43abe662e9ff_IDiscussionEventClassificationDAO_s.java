 package org.computer.knauss.reqtDiscussion.io;
 
 import org.computer.knauss.reqtDiscussion.model.DiscussionEventClassification;
 
 public interface IDiscussionEventClassificationDAO {
 
 	public DiscussionEventClassification[] getClassificationsForDiscussionEvent(
			int discEventID) throws DAOException;
 
 	public void storeDiscussionEventClassification(
 			DiscussionEventClassification classification) throws DAOException;
 
 }
