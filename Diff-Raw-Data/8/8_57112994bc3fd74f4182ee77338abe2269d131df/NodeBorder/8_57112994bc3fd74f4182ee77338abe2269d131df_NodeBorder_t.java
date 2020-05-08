 package rocketview;
 
 import cansocket.*;
 
 import java.util.*;
 import javax.swing.border.*;
 
 class NodeBorder extends TitledBorder implements CanObserver
 {
 	protected String name;
 	protected Map states;		// maps codes to labels
 	protected int id;
 	
 	public NodeBorder(CanDispatch dispatch, String name, int id)
 	{
 		super(name + ": -");
 		this.name = name;
 		this.id = id;
 		this.states = new HashMap();
 		dispatch.add(this);
 	}
 	
 	public NodeBorder addState(int code, String label)
 	{
		states.put(new Byte((byte)code), label);
 		return this;
 	}
 	
 	public void message(CanMessage msg)
 	{
 		if (msg.getId() == id)
 		{
			Byte state = new Byte(msg.getData8(0));
 			if (states.containsKey(state))
 				setTitle(name + ": " + states.get(state));
 			else
				setTitle(name + ": 0x" + Integer.toHexString(state.byteValue()));
 		}
 	}
 }
