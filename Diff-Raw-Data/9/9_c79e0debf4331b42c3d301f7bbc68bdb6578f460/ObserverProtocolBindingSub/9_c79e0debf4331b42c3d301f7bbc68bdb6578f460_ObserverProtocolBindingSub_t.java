 package generated;
 
public crosscutting clean class ObserverProtocolBindingSub extends ObserverProtocolBinding
 {
 	public ObserverProtocolBindingSub(String a)
 	{
 		super(a);
 	}
 	public ObserverProtocolBindingSub()
 	{
 		this("");
 	}
 	public Object getNameParentExpected(String child)
 	{
 		System.out.println("ObserverProtocolBindingSub.getNameParentExpected(" + child + ")");
 		//super.getNameParent("ObserverProtocolImpl");
 		return "ObserverProtocolBinding";
 	}
 
 	override class SubjectBinding
 	{
 		public void setObserver(Observer o)
 		{
 			System.out.println("SubjectBindingSub.setObserver(" + o + ")");
 		}
 		public Object getState()
 		{
 			System.out.println("SubjectBindingSub.getState()");
 			return "MyState";
 		}
     }
 	
 	override class ObserverBindings
 	{
 		public void notify(Subject s)
 		{
 			System.out.println("ObserverBindingSub.notify(" + s + ")");
 			System.out.println(s.getState());
 		}
 	}
 }
