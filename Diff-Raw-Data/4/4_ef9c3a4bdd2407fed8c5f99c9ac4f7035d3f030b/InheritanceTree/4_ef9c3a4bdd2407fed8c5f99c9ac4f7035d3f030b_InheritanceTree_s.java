 package xtc.oop;
 import java.util.ArrayList;
 import java.util.LinkedList;
import java.util.Arrays;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 /**
  * InheritanceTree builds a tree structure so subclasses 
  *  can access superclass's vtables when building their own vtables.
  * links all the classes together in a heiarchy for easy searchability
  *
  */
 public class InheritanceTree{
 	public InheritanceTree root;
 	public final String className;
 	public ArrayList<InstanceField> fields;
 	public ArrayList<Declaration> constructors;
 	public ArrayList<Declaration> local;//all methods defined IN THIS CLASS virtual or not!
 	public ArrayList<Declaration> Vt_ptrs;//all methods able to be inherited by children
 	public InheritanceTree superclass;
 	public ArrayList<InheritanceTree> subclasses;
 	public ArrayList<String> packages;
 
 
 	/**
 	 * The constructor for creating the Object class Inheritance tree.
 	 * @param none
 	 * 
 	 */
 	InheritanceTree(){
 		//defines class name
 		className = "Object";
 		
 		//no superclass for Object
 		superclass = null;
 		
 		//no instancefields in Object 
 		fields = new ArrayList<InstanceField>(0);
 		
 		//creates Vtable arraylist
 		Vt_ptrs = new ArrayList<Declaration>(0);
 		
 		//add __isa to Vtable
 		Vt_ptrs.add(new Declaration("Class", "__isa",
 									 className,new ArrayList<Fparam>(0)));
 		
 		//add __delete to Vtable
 		ArrayList<Fparam> p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"__Object*","__this"));
 		Vt_ptrs.add(new Declaration("void","__delete",
 									"Object",p));
 		
 		
 		//add hashcode to Vtable
 		p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("int32_t","hashCode",
 									 "Object",p));
 		//add equals to Vtable
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		p.add(new Fparam(new ArrayList<String>(),"Object","other"));
 
 		Vt_ptrs.add(new Declaration("bool","equals",
 									 "Object",p));
 		//add getClass to Vtable
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("Class","getClass",
 									 "Object",p));
 		//add toString to Vtable
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Object","__this"));
 		Vt_ptrs.add(new Declaration("String","toString",
 									 "Object",p));
 
 		//subclass are initalized to a 0 element arraylist
 		subclasses = new ArrayList<InheritanceTree>(0);
 		
 		this.root = this;
 
 	}
 	
 	/**
 	 * The constructor for creating the Class class.
 	 * @param InheritanceTree (the Object inheritance tree)
 	 */
 	InheritanceTree(InheritanceTree supr){
 		this.root = supr.root;
 		
 		//defines superclass
 		superclass = supr;
 		//defines class name
 		className = "Class";
 		
 		//copies the superclass's Vtable into virtual Vtable
 		Vt_ptrs = new ArrayList<Declaration>(supr.Vt_ptrs);
 		
 		//change __isa field to point to this class's feild
 		Vt_ptrs.get(0).ownerClass = className;
 		
 		//change __delete field to point to this class's feild
 		Vt_ptrs.get(1).ownerClass = className;
 		Vt_ptrs.get(1).params.get(0).type ="__"+className+"*";
 		
 		//change toString to point to class name
 		Vt_ptrs.get(5).ownerClass = className;
 		Vt_ptrs.get(5).params.get(0).type = className;
 
 		
 		//adds virtual Class methods ptrs to virtual Vtable
 		ArrayList<Fparam> p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		Vt_ptrs.add(new Declaration("String","getName",
 									 "Class",p));
 		
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		Vt_ptrs.add(new Declaration("String","getSuperclass",
 									 "Class",p));
 		
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		Vt_ptrs.add(new Declaration("String","getComponentType",
 									 "Class",p));
 		
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		Vt_ptrs.add(new Declaration("String","isPrimitive",
 									 "Class",p));
 		
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		Vt_ptrs.add(new Declaration("String","isArray",
 									 "Class",p));
 		
 		 p = new ArrayList<Fparam>(0);
 		p.add(new Fparam(new ArrayList<String>(),"Class","__this"));
 		p.add(new Fparam(new ArrayList<String>(),"Object","o"));
 		Vt_ptrs.add(new Declaration("String","isInstance",
 									 "Class",p));
 		
 		//subclass are initalized to a 0 element arraylist
 		subclasses = new ArrayList<InheritanceTree>(0);
 		
 		//add this tree to the superclasses' subclasses
 		supr.subclasses.add(this);
 		
 		
 	}
 	/**
 	 * The constructor for all InheritanceTree's that do not represent Object or Class.
 	 * will create vtable for class and then attach the tree to the superclass tree.
 	 * @param GNode (classdeclaration), InheritanceTree (superclass's).
 	 */
 	InheritanceTree(GNode n, InheritanceTree supr){
 		this.root = supr.root;
 		superclass = supr;//superclass defined
 		className = n.getString(1);//className defined from node
 		
 		//field arraylist defined with all field declarations
 		fields = addfielddeclarations(n);
 		
 		//constructors arraylist initialized
 		constructors = new ArrayList<Declaration>(0);
 		
 		//local arraylist initialized
 		local = new ArrayList<Declaration>(0);
 		
 
 		//copies the superclass's Vtable into virtual arraylist
 		Vt_ptrs = new ArrayList<Declaration>(supr.Vt_ptrs);
 
 		
 		//change __isa methods to point to this class
 		Vt_ptrs.get(0).ownerClass = className;
 		
 		//change __delete field to point to this class's feild
 		Vt_ptrs.get(1).ownerClass = className;
 		Vt_ptrs.get(1).params.get(0).type ="__"+className+"*";
 		
 		//add __class to local methods
 		local.add(new Declaration("Class","__class",className,
 								  new ArrayList<Fparam>(0)));
 		
 		//add __delete to local methods
 		ArrayList<Fparam> d= new ArrayList<Fparam>(0);
 		d.add(new Fparam(new ArrayList<String>(0),className,"__this"));
 		local.add(Vt_ptrs.get(1));
 		
 		//constructors defined
 		//local methods defined
 		//virtual methods defined
 		ArrayList<Declaration> virtual = addmethoddeclarations(n);
 		
 		//checks if virtual methods overwrite superclass methods
 		check_for_overrides(virtual);
 		
 		//add virtual methods to vtable
 		Vt_ptrs.addAll(virtual);
 
 			
 		//subclass are initalized to a 0 element arraylist
 		subclasses = new ArrayList<InheritanceTree>(0);
 		
 		//add this tree to the superclasses' subclasses
 		supr.subclasses.add(this);
 
 	}
 	/**
 	 *cycles through all Declarations to see if virtual methods 
 	 * overwrite the superclass method.
 	 *@param ArrayList<Declaration> ... always the virtual Declarations
 	 */
 	private void check_for_overrides(ArrayList<Declaration> virtual){
 		for(int l = 0; l<virtual.size();l++){
 			int index = contains(virtual.get(l));
 			if(index!=-1)
 				//remove the pointer that points to 
 				//a method that is redefined in this class
 				Vt_ptrs.remove(index);
 			
 		}
 	}
 	/**
 	 *looks in Vt_ptrs and local for methods with same name but diff params
 	 * if multiple are found keeps method with max overloadNum stored
 	 * returns max overloadNum
 	 */
 	private int overloaded_check(String method,ArrayList<Fparam> params,boolean is_virtual){
 		int max=-1;
 		for(int i =0;i<Vt_ptrs.size();i++){
 			//same name test
 			if(method.equals(Vt_ptrs.get(i).name)){
 				//diff params test
 				
 				if(params.size()==Vt_ptrs.get(i).params.size())
 				   if(!typetest(params,Vt_ptrs.get(i).params))
 					//store max overloadNum
 					max = Math.max(Vt_ptrs.get(i).overloadNum,max);
 				   else if(is_virtual){
 					   Vt_ptrs.get(i).ownerClass = this.className;
 					   //signify to NOT ADD TO VIRTUAL
 					   return Vt_ptrs.get(i).overloadNum-1;
 		
 				   }
 			}	//delete vt_ptr bc its overwritten
 		}
 		for(int j=0;j<local.size();j++){
 			//same name test
 			if(method.equals(local.get(j).name)){
 				//diff params test
 				if(!typetest(params,local.get(j).params))
 					//store max overloadNum
 					max = Math.max(local.get(j).overloadNum,max);
 				else;
 			}
 		}
 			return max;
 	}
 
 	/**
 	 * helper method used in check_for_overrides() for testing for equal Declarations.
 	 *@param Declaration
 	 */
 	private int contains(Declaration virtual){
 		//starts at i =1 to ignore __isa feild that was already replaced
 		for(int i = 1; i < Vt_ptrs.size();i++){
 			if((Vt_ptrs.get(i).name.equals(virtual.name))
 			   &&(Vt_ptrs.get(i).returntype.equals(virtual.returntype))
 			   &&(Vt_ptrs.get(i).params.size()==(virtual.params.size()))
 			   &&(typetest(Vt_ptrs.get(i).params,virtual.params)))
 				return i;
 		}
 		return -1;
 	}
 	/**
 	 *helper method used in contains 
 	 *  returns true if both params arraylist have same types through and through
 	 */
 	private boolean typetest(ArrayList<Fparam> vptrs, ArrayList<Fparam> local){
 		int size =vptrs.size();
 		for(int j =1;j<size;j++){//ignore __this param checking
 			//if types do not match up return FALSE
 			if(!(vptrs.get(j).type.equals(local.get(j).type)))
 				return false;//***this means that this is an overloaded method
 		}
 		return true;
 	}
 	/**
 	 * uses visitors to create the Declarations for virtual methods.
 	 * also documents local methods that are not virtual for definition later
 	 *@param GNode (always a classDeclaration)
 	 */
 	private ArrayList<Declaration> addmethoddeclarations(GNode n){
 		// cast to a node
 		Node node = n;
 		//declare a virutal arraylist<Declaration>
 		final ArrayList<Declaration> virtual = new ArrayList<Declaration>(0);
 
 		new Visitor(){
 			
 			//declare initalize variables 
 			ArrayList<String> modifiers = new ArrayList<String>(0);
 			String retrn="";
 			String className= "";
 			String methodname="";
 			ArrayList<Fparam> params = new ArrayList<Fparam>(0);
 			ArrayList<String> mods = new ArrayList<String>(0);
 			String fptype ="";
 			String fpname ="";
 			int overloaded = 0;
 			GNode block;
 
 			boolean is_fparam=false;
 			boolean is_virtual=false;
 			boolean is_constructor =false;
 			
 			public void visitClassDeclaration(GNode n){
 				className = n.getString(1);
 				visit(n);
 			}
 			public void visitConstructorDeclaration(GNode n){
 				is_constructor=true;
 				
 				//clear variables
 				modifiers.clear();
 				retrn="";
 				methodname = n.getString(2);
 				params.clear();
 				block=null;
 				is_virtual = true;
 				overloaded=0;
 				
 				//get info from tree
 				visit(n);
 				overloaded = constructors.size();
 				//add declaration to constructor
 				constructors.add(new Declaration(overloaded ,modifiers,is_virtual,retrn,methodname,className,params,
 												 block,new ArrayList<local_variable>(0)));
 				
 				is_constructor =false;
 			}
 			public void visitMethodDeclaration(GNode n){
 				//clear variables
 				modifiers.clear();
 				retrn="";
 				methodname = n.getString(3);
 				params.clear();
 				block=null;
 				is_virtual = true;
 				overloaded=0;
 				
 				//go into tree to get info
 				visit(n);
 				
 				//search in Vt_ptrs and local for same name diff params
 				overloaded = overloaded_check(methodname,params,is_virtual)+1;
 				
 				//test to see if the modifier was public or protected(if it should be virtual)
 				if(is_virtual) 
 					virtual.add(new Declaration(overloaded ,modifiers,is_virtual,retrn,methodname,className,params,
 												block,new ArrayList<local_variable>(0)));
 					//add it to local with true as isvirtual field
 					local.add(new Declaration(overloaded ,modifiers,is_virtual,retrn,methodname,className,params,
 											  block,new ArrayList<local_variable>(0)));
 
 				
 					
 							
 			}
 			public void visitBlock(GNode n){
 				block = n;
 			}
 			public void visitModifiers(GNode n){
 				visit(n);
 			}
 			public void visitModifier(GNode n){
 				if(is_fparam) mods.add(n.getString(0));
 				 //notes that private/staic methods are not virtual and should not go into Vtable
 				else if((n.getString(0).equals("private"))||(n.getString(0).equals("static")))
 					 is_virtual=false;
 				else modifiers.add(n.getString(0));
 			}
 			public void visitVoidType(GNode n){
 				retrn = "void";
 			}
 			public void visitQualifiedIdentifier(GNode n){
 				String type=n.getString(0);
 				if(is_fparam) fptype=type;
 				else retrn = type;
 			}
 			
 			public void visitPrimitiveType(GNode n){
 				String type=n.getString(0);
 				if(n.getString(0).equals("int")) type="int32_t";
 				if(n.getString(0).equals("boolean")) type="bool";
 				if(is_fparam) fptype =type;
 				else retrn = type;
 			}
 			public void visitDimensions(GNode n){
 				if(is_fparam){ 
 					if(fptype.equals("Class"))fptype ="ArrayOfClass";
 					if(fptype.equals("int32_t"))fptype ="ArrayOfInt";
 					if(fptype.equals("Object"))fptype ="ArrayOfObject";
 				
 				}
 				else{ 
 					if(retrn.equals("Class"))retrn ="ArrayOfClass";
 					if(retrn.equals("int32_t"))retrn ="ArrayOfInt";
 					if(retrn.equals("Object"))retrn ="ArrayOfObject";
 				}
 			}
 			public void visitFormalParameters(GNode n){
 				//add __this parameter for virtual methods
 				if((is_virtual)&&(!is_constructor)) params.add(new Fparam(new ArrayList<String>(0),className,"__this"));
 				visit(n);
 			}
 			public void visitFormalParameter(GNode n){//variable name
 				is_fparam =true;
 				mods.clear();
 				fpname =n.getString(3);
 				fptype="";
 				visit(n);
 				params.add(new Fparam(mods,fptype,fpname));
 				is_fparam=false;
 					
 			}
 			public void visit(Node n) {
 				for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 			}
 			
 		}.dispatch(n);
 	
 		return virtual;
 	}
 	/**
 	 * returns ArrayList<InstanceField> that holds all the fields fieldDeclaration info.
 	 * @param GNode n
 	 * however instance fields are defined or declared outside the constructor 
 	 *		is how they will be stored here.
 	 *
 	 */
 	private ArrayList<InstanceField> addfielddeclarations(GNode n){
 		Node node = n;
 	
 		final ArrayList<InstanceField> f = new ArrayList<InstanceField>(0);
 		
 		//---adds all non-private/static fields from superclass to this class
 		for(int i=0;i<superclass.fields.size();i++){
 			for(int j=0;j<superclass.fields.get(i).modifiers.size();j++){
 				String mod = superclass.fields.get(i).modifiers.get(j);
 				
 				if((mod.equals("private"))||(mod.equals("static"))){}//do nothing
 				else f.add(superclass.fields.get(i));//inherit the field 
 			}
 		}
 		
 		
 		new Visitor(){
 			
 			ArrayList<String> mods= new ArrayList<String>(0);
 			String type="";
 			ArrayList<String> names= new ArrayList<String>(0);
 			String val;
 			boolean is_field = false;
 			boolean is_selectExp = false;
 			boolean is_arg = false;
 			
 			
 			public void visitClassBody(GNode n){
 				visit(n);
 			}
 			public void visitConstructorDeclaration(GNode n){
 				//do not look in constructor
 			}
 			public void visitMethodDeclaration(GNode n){
 				//do not look in methods
 			}
 			public void visitFieldDeclaration(GNode n){
 				is_field = true;
 				
 				//clear variables
 				mods.clear();
 				type ="";
 				names.clear();
 				val="";
 				
 				visit(n);
 				//add instancefield for each name in names
 				for(int i=0; i<names.size();i++){
 					f.add(new InstanceField(mods,type,names.get(i),val));
 				}
 				
 				is_field = false;
 			}
 			//if not looking in FieldDeclaration's subtree ignore nodes
 			public void visitModifier(GNode n){
 				if(is_field)mods.add(n.getString(0));
 				
 			}
 			public void visitPrimitiveType(GNode n){
 				if(is_field){
 					String type = n.getString(0);
 					if(type.equals("int"))type="int32_t";
 					if(type.equals("boolean"))type="bool";
 				}
 				visit(n);
 			}
 	
 			public void visitDeclarator(GNode n){//variable name
 				if(is_field) names.add(n.getString(0));
 					
 			}
 			public void visitQualifiedIdentifier(GNode n){//type
 				if(is_field) type=n.getString(0);
 				
 			}
 			public void visitAdditiveExpression(GNode n){
 				visit(n.getNode(0));
 				val = val+" "+n.getString(1);
 				visit(n.getNode(2));
 			}
 			public void visitIntegerLiteral(GNode n){
 				val = val+n.getString(0);
 			}
 			public void visitStringLiteral(GNode n){
 				val = val+n.getString(0);
 			}
 			public void visitCallExpression(GNode n){
 				if(is_field){
 					visit(n.getNode(0));
 					visit(n.getNode(1));
 					val = val+n.getString(2);
 					visit(n.getNode(3));
 				}	
 			}
 			public void visitArguments(GNode n){
 				if(is_field){
 					is_arg =true;
 					val=val+"(";
 					visit(n);
 					//val.  take off last ,
 					val=val+")";
 					is_arg=false;
 				}
 			}
 			public void visitSelectionExpression(GNode n){
 				if(is_field){
 					is_selectExp=true;
 					//get primary identifer
 					visit(n);
 					//add selectionExp member
 					val = val+n.getString(1);
 					is_selectExp=false;
 				}
 			}
 			public void visitPrimaryIdentifier(GNode n){
 				if(is_field){
 					if(is_selectExp)
 						val = val+n.getString(0)+".";
 					if(is_arg)
 						val = val+n.getString(0)+",";
 				}
 			}
 			public void visit(Node n) {
 				for (Object o : n) if (o instanceof Node) dispatch((Node)o);
 			}
 		
 		}.dispatch(node);
 		return f;
 	
 	}
 		/**
 	 * search will return a InheritanceTree of matching className
 	 *	or a null InheritanceTree if no tree exists yet as child of the root.
 	 *@param String name
 	 * this is meant to be used only on the root Object Tree so that the entire tree is searched
 	 *
 	 */
 	public InheritanceTree search(ArrayList<String> packages, String name){
 		InheritanceTree found = null;
 		if((this.className.equals(name))&&(this.packages.containsAll(packages)))
 			return this;
 		
 		for(int i=0; i< this.subclasses.size();i++){
 			found = subclasses.get(i).search(packages,name);
 			if(found!=null) return found;
 		}
 		return found;
 	
 	}
 	public String search_for_constructor(ArrayList<String> paramtyps){
 		//list of possible constructors
 		LinkedList<Declaration> possible= new LinkedList<Declaration>();
 		
 		//add all constructors to list that have same #params
 		int size= constructors.size();
 		for(int i=0;i<size;i++){
 			if(constructors.get(i).params.size()==paramtyps.size())
 			possible.add(constructors.get(i));
 		}
 		//return if only one
 		if(possible.size()==1)
 			return possible.get(0).name+"_"+possible.get(0).overloadNum;
 		
 		//need to zero out specificity for next check
 		for(Declaration d : possible){
 			d.specificity =0;
 		}
 		
 		//check for specificity
 		int ptsize = paramtyps.size();
 		for(int m = 0;m<ptsize;m++){
 			String type=paramtyps.get(m);
 			for(int c=0;c<possible.size();c++){
 				String pos_type = possible.get(c).params.get(m).type;
 				if(!pos_type.equals(type));
 				else{
 					int casting = gouptree(pos_type,type);
 					if(casting == -1){ possible.remove(c);}
 					else possible.get(c).specificity =possible.get(c).specificity+ casting;
 				}
 			}
 		}
 		Declaration min =possible.get(0);
 		for(int n=0;n<possible.size();n++){
 			if(min.specificity>possible.get(n).specificity)
 				min=possible.get(n);
 		
 		}
 		return min.name+"_"+min.overloadNum;
 	
 	}
 	public String[] search_for_method(boolean on_instance, Declaration method, 
 									ArrayList<String> paramtyps, String method_name){
 		String[] result= new String[2];
 		String accessor;
 		
 		int paramsize=paramtyps.size();
 		//-----ACCESSABLE/SAME NAME/SAME #PARAMS CHECK
 		//looks in local(non virtual) and VT_ptrs for same named methods and same number of parameters
 		LinkedList<Declaration> possible= new LinkedList<Declaration>();
 		int vsize = Vt_ptrs.size();
 		for(int j=0;j<vsize;j++){
 			if((Vt_ptrs.get(j).name.equals(method_name))&&(Vt_ptrs.get(j).params.size()==paramsize))
 				possible.add(Vt_ptrs.get(j));
 		}
 		int lsize = local.size();
 		for(int i=0;i<lsize;i++){
 			
 			if((local.get(i).equals(method_name))&&(!local.get(i).isVirtual)&&(local.get(i).params.size()==paramsize)) 
 				possible.add(local.get(i));
 		}
 		
 		//if only one left return it with accessor!!
 		if(possible.size()==1){
 			Declaration choosen = possible.get(0);
 			result[0] = choosen.returntype;
 			accessor= make_accessor(on_instance,choosen.isVirtual);
 			result[1]= accessor+choosen.name+"_"+choosen.overloadNum;
 			return result;
 		}
 		
 		//need to zero out specificity for next check
 		for(Declaration d : possible){
 			d.specificity =0;
 		}
 		
 		//----SPECIFICITY CHECK
 		
 		for(int m=0;m<paramsize;m++){
 			String type=paramtyps.get(m);
 			for(int c=0;c<possible.size();c++){
 				String pos_type = possible.get(c).params.get(m).type;
 				if(!pos_type.equals(type));
 				else{
 					int casting = gouptree(pos_type,type);
 					if(casting == -1){ possible.remove(c);}
 					else possible.get(c).specificity =possible.get(c).specificity+ casting;
 				}
 			}
 		}
 		Declaration min =possible.get(0);
 		for(int n=0;n<possible.size();n++){
 			if(min.specificity>possible.get(n).specificity)
 				min=possible.get(n);
 		}
 		//find method with smallest number MUST BE ONLY ONE (MIN)
 		//RETURN accessor+NAME+_number
 		accessor = make_accessor(on_instance,min.isVirtual);
 		result[0]= min.returntype;
 		result[1]= accessor+min.name+"_"+min.overloadNum;
 		return result;
 
 		
 	}
 	private String make_accessor(boolean on_instance,boolean isVirtual){
 		if((on_instance)&&(isVirtual)) return "->vtpr->";
 		else if((on_instance)&&(!isVirtual)) return".";
 		else return"";
 	}
 
 	private int gouptree(String casted_to, String castee){
 
             /*
                       *  WRONG: will fix
                       */
             if (castee.equals("char")) {
                 if (casted_to.equals("short"))
                     return -1;
                 else
                     castee = "short"; // has same precidence as a short,
             } else if (casted_to.equals("char"))
                 return -1;
             
 
            final ArrayList<String> primatives = (ArrayList) Arrays.asList(
                     new String[] {"double", "float", "long",
                     "int", "short", "byte" });
             
             if (primatives.contains(casted_to) && primatives.contains(castee))
                 return primatives.indexOf(casted_to) - primatives.indexOf(castee);
 
 		int distance =0;
 		
 		//search on root of tree for castee class
 		InheritanceTree type = this.root.search(this.packages,castee);
 		
 		while(type.className!=casted_to){
 			distance++;
 			type = type.superclass;
 		}
 		
 		return distance;
 	}
 
 	
 }
