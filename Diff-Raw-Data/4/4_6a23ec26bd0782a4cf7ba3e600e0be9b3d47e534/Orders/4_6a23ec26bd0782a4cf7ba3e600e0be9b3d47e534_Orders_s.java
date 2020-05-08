 
 
 
 package overwatch.db;
 
 import java.util.ArrayList;
 import overwatch.gui.NameRefPair;
 import overwatch.gui.NameRefPairList;
 
 
 
 
 
 public class Orders
 {
 	
 	/**
 	 * Create a new order and an associated message.
 	 * The returned integer is guaranteed to be the orderNo associated.
 	 * WARNING: This function locks the Message table, then the Orders table, but not both.  Be aware of deadlocks.
 	 * @param subject
 	 * @param body
 	 * @param sentBy
 	 * @param sentTo
 	 * @return orderNo
 	 */
 	public static Integer create( String subject, String body, Integer sentBy, Integer sentTo )
 	{
 		return Common.createWithUniqueLockingAutoInc(
 			"Orders",
 			"default",
 			Messages.create( subject, body, sentBy, sentTo ).toString(),
 			"false",
 			"false"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get orders sent to this person
 	 * Note: the string part is for display only
 	 * @param personNo
 	 * @return ArrayList<NameRefPair<Integer>>
 	 */
 	public static ArrayList<NameRefPair<Integer>> getOrdersAndSubjectsSentTo( Integer personNo ) {
 		return getOrdersAndSubjects( "sentTo", personNo );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get orders sent by this person
 	 * Note: the string part is for display only
 	 * @param personNo
 	 * @return ArrayList<NameRefPair<Integer>>
 	 */
 	public static ArrayList<NameRefPair<Integer>> getOrdersAndSubjectsSentBy( Integer personNo ) {
 		return getOrdersAndSubjects( "sentBy", personNo );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get message contents for a given order.
 	 * @param orderNo
 	 * @return EnhancedResultSet
 	 */
 	public static EnhancedResultSet getMessageContents( Integer orderNo )
 	{
 		return Database.query(
 			"select sentDate,                " +
 			"       subject,                 " +
 			"       body,                    " +
 			"       sentBy,                  " +
 			"       sentTo                   " +
 			"                                " +
 			"from Orders   o,                " +
 			"     Messages m                 " +
 			"                                " +
 			"where o.messageNo = m.messageNo " +
 			"  and o.orderNo   = " + orderNo  + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Mark an order as done.
 	 * Implicitly marks it as read as well.
 	 * @param orderNo
 	 * @return succeeded
 	 */
 	public static boolean markAsDone( Integer orderNo )
 	{
 		return 0 != Database.update(
 			"update Orders     " +
			"set isDone = true " +
			"set isRead = true " +
 			"where orderNo =   " + orderNo + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Mark an order as read.
 	 * @param orderNo
 	 * @return succeeded
 	 */
 	public static boolean markAsRead( Integer orderNo )
 	{
 		return 0 != Database.update(
 			"update Orders     " +
 			"set isRead = true " +
 			"where orderNo =   " + orderNo + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private static ArrayList<NameRefPair<Integer>> getOrdersAndSubjects( String sentByOrTo, Integer personNo )
 	{
 		EnhancedResultSet ers = Database.query(
 			"select orderNo, subject, isDone, isRead " +
 			"                                        " +
 			"from Orders   o,                        " +
 			"     Messages m                         " +
 			"                                        " +
 			"where o.messageNo = m.messageNo         " +
 			"  and m." + sentByOrTo + " = " + personNo +
 			"                                  " +
 			"order by o.isRead, o.isDone, sentDate; "
 		);
 		
 		Integer[] orderNo = ers.getColumnAs( "orderNo", Integer[].class );
 		String [] subject = ers.getColumnAs( "subject", String [].class );
 		Boolean[] isDone  = ers.getColumnAs( "isDone",  Boolean[].class );
 		Boolean[] isRead  = ers.getColumnAs( "isRead",  Boolean[].class );
 		String [] display = new String[ subject.length ];
 		
 		for (int i=0; i<orderNo.length; i++)
 		{
 			String  pre = "";
 			boolean forSender = sentByOrTo.equals( "sentBy" );
 			
 			if ( ! isRead[i]) {
 				pre = (!forSender)  ?  "[New]"  :  "[Unread]";
 			} else {
 				if ( ! isDone[i])
 					 pre = (!forSender)  ?  "[Todo]"  :  "[Not done]";
 			    else pre = "[Done]";
 			}
 			
 			display[i] = new String( pre + " " + subject[i] );
 		}
 		
 		return new NameRefPairList<Integer>( orderNo, display );
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
