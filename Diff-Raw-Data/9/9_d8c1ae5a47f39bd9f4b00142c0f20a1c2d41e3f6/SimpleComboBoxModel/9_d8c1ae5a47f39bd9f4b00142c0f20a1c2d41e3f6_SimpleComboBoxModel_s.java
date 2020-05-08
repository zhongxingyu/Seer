 /**
  * Created by IntelliJ IDEA.
  * User: Guillaume
 * Date: 26 fvr. 2003
  * Time: 16:28:32
  */
 package zz.utils;
 
 import java.util.List;
 
 import javax.swing.ComboBoxModel;
 
 /**
  * A simple combo box model based on a {@link List}.
  */
 public class SimpleComboBoxModel extends SimpleListModel implements ComboBoxModel
 {
 	private Object itsSelectedItem;
 
 	public SimpleComboBoxModel ()
 	{
 		this (null);
 	}
 	
 	public SimpleComboBoxModel (List aList)
 	{
 		super (aList);
 	}
 
 	public SimpleComboBoxModel (List aList, Object aSelectedItem)
 	{
 		super (aList);
 		itsSelectedItem = aSelectedItem;
 	}
 
 	public Object getSelectedItem ()
 	{
 		return itsSelectedItem;
 	}
 
 	public void setSelectedItem (Object aSelectedItem)
 	{
 		itsSelectedItem = aSelectedItem;
 	}
 }
