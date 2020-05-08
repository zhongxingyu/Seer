 package supersql.codegenerator;
 
 import supersql.codegenerator.HTML.HTMLFactory;
 import supersql.codegenerator.Mobile_HTML5.Mobile_HTML5Factory;
 import supersql.codegenerator.PDF.PDFFactory;
 import supersql.codegenerator.SWF.SWFFactory;
 import supersql.codegenerator.X3D.X3DFactory;
 import supersql.codegenerator.XML.XMLFactory;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 import supersql.parser.SSQLparser;
 
 /**
  * �����ɥ�����?�����ޥ͡����㥯�饹 ���ꤵ?�����Τˤ�äƥ����ɥ����
  * �����ι������ʤ�������? ��?(Factory)��������?
  */
 public class CodeGenerator{
 	private Factory factory;
 
 	public int TFEid;
 
 	public Manager manager;
 	
 	private String media;
 	/**
 	 * ���󥹥ȥ饯��
 	 */
 	public CodeGenerator() {
 	}
 
 	public CodeGenerator(int id){
 		TFEid = id;
 	}
 	/**
 	 * ��?����Ԥ	 */
 	public void initiate() {
 		if (factory != null) {
 			Log.out("factory is " + factory);
 			factory.createLocalEnv();
 			manager = factory.createManager();
 		}
 	}
 
 	/**
 	 * ���ꤵ?�����Τˤ�äƺ��Ȥ�Ѿ�?����ι(Factory)? �����������֤�?
 	 */
 	public void setFactory(String media) {
 		this.media = media;
 		if (media.toLowerCase().equals("pdf")) {
 			factory = new PDFFactory();
 		} else if (media.toLowerCase().equals("html")) {
 			factory = new HTMLFactory();
 		} else if (media.toLowerCase().equals("x3d")) {
 			factory = new X3DFactory();
 		} else if(media.toLowerCase().equals("xml")){
 			factory = new XMLFactory();
 		} else if (media.toLowerCase().equals("swf")) {
 		 	factory = new SWFFactory();
 		} else if (media.toLowerCase().equals("mobile_html5")) {	//added by goto 20121217
 		 	factory = new Mobile_HTML5Factory();
 		}  else if (media.toLowerCase().equals("csv")) {
 			factory = new SWFFactory();
 		}
 		/*
 		 * else if(media.toLowerCase().equals("xml")){ factory = new
 		 * XMLFactory(); }
 		 */
 		else {
 			System.err.println("Error[Media]: valid medium not found");
 			GlobalEnv.addErr("Error[Media]: valid medium not found");
 			//comment out  by chie
 			//System.exit(-1);
 		}
 	}
 
 	/**
 	 * �黻��Connector�β��̥��饹���󥹥��󥹤����������֤�
 	 */
 	public Connector createConnector(int dim) {
 		Connector connector = null;
 		if (dim == 0)
 			connector = factory.createC0(manager);
 		else if (dim == 1)
 			connector = factory.createC1(manager);
 		else if (dim == 2)
 			connector = factory.createC2(manager);
 		else if (dim == 3 || dim == -3)
 			connector = factory.createC3(manager);
 		else if (dim == 4)
 			connector = factory.createC4(manager);
 		else {
 		    /* undefine Operator */
 		    System.err.println("*** Illegal Operator for Connector ***");
 		    throw (new IllegalStateException());
 
 		}
 		connector.setId(TFEid++);
 		return connector;
 	}
 
 	/**
 	 * �黻��Grouper�β��̥��饹���󥹥��󥹤����������֤�
 	 */
 	public Grouper createGrouper(int dim) {
 		Grouper grouper = null;
 		if (dim == 0)
 			grouper = factory.createG0(manager);
 		else if (dim == 1)
 			grouper = factory.createG1(manager);
 		else if (dim == 2)
 			grouper = factory.createG2(manager);
 		else if (dim == 3)
 			grouper = factory.createG3(manager);
 		else if (dim == 4)
 			grouper = factory.createG4(manager);
 		else {
 		    /* undefine Operator */
 		    System.err.println("*** Illegal Operator for Grouper ***");
 		    throw (new IllegalStateException());
 		}
 		grouper.setId(TFEid++);
 		return grouper;
 	}
 
 	/**
 	 * Attribute�β��̥��饹���󥹥��󥹤����������֤�
 	 */
 	public Attribute createAttribute() {
 	    Attribute attribute = factory.createAttribute(manager);
 		attribute.setId(TFEid++);
 		return attribute;
 	}
 	
 	public Attribute createConditionalAttribute(){
 		Attribute condAttribute = factory.createConditionalAttribute(manager);
 		condAttribute.setId(TFEid++);
 		return condAttribute;
 	}
 	
 	public IfCondition createIfCondition(Attribute condition, TFE thenTfe, TFE elseTfe){
 		IfCondition ifCondition = factory.createIfCondition(manager, condition, thenTfe, elseTfe);
 		ifCondition.setId(TFEid++);
 		return ifCondition;
 	}
 
 	/**
 	 * Function�β��̥��饹���󥹥��󥹤����������֤�
 	 */
 	public Function createFunction() {
 	    Function function = factory.createFunction(manager);
 		function.setId(TFEid++);
 		return function;
 	}
 
 	/**
 	 * ��¤������?����Relation���饹�Υ��֥������Ȥ���O2C, SQL������ ��������?
 	 */
 
 	public void generateCode(SSQLparser parser, ExtList data_info) {
 		ITFE tfe_info = parser.get_TFEschema();
 
 		//	ɬ�פʤ饳���ȥ����ȳ�����Manager������ѹ
 		//	manager.preProcess(tab,le,le1,le2,le3);
 		//	manager.createSchema(tab,le,le1,le2,le3);
 		// ?�ֳ��� Grouper�ΤȤ���data_info��Ĵ����?
 		if (tfe_info instanceof Grouper && data_info.size() != 0) {
 			data_info = (ExtList) data_info.get(0);
 		}
 
 		manager.generateCode(tfe_info, data_info); 
 
 		manager.finish();
 
 
 	};
 
 	//tk start//////////////////////////////////////////////////////////////////////////////
 	public StringBuffer generateCode2(SSQLparser parser, ExtList data_info) {
 		ITFE tfe_info = parser.get_TFEschema();
 
 		//	ɬ�פʤ饳���ȥ����ȳ�����Manager������ѹ
 		//	manager.preProcess(tab,le,le1,le2,le3);
 		//	manager.createSchema(tab,le,le1,le2,le3);
 
 		Log.out("===============================");
 		Log.out("     generateCode2 is start     ");
 		Log.out("===============================");
 
 
 		// ?�ֳ��� Grouper�ΤȤ���data_info��Ĵ����?
 		if (tfe_info instanceof Grouper && data_info.size() != 0) {
 			data_info = (ExtList) data_info.get(0);
 		}
 		Log.out("data_info.size " + data_info.size());
 
 		if(data_info.size() == 0)
 			return manager.generateCodeNotuple(tfe_info);
 		else
 			return manager.generateCode2(tfe_info, data_info);
 
 	};
 	public StringBuffer generateCode3(SSQLparser parser, ExtList data_info) {
 		ITFE tfe_info = parser.get_TFEschema();
 
 		//	ɬ�פʤ饳���ȥ����ȳ�����Manager������ѹ
 		//	manager.preProcess(tab,le,le1,le2,le3);
 		//	manager.createSchema(tab,le,le1,le2,le3);
 
 		Log.out("===============================");
 		Log.out("     generateCode3 is start     ");
 		Log.out("===============================");
 
 		// ?�ֳ��� Grouper�ΤȤ���data_info��Ĵ����?
 		if (tfe_info instanceof Grouper && data_info.size() != 0) {
 			data_info = (ExtList) data_info.get(0);
 		}
 		Log.out("data_info.size " + data_info.size());
 
 		if(data_info.size() == 0)
 			return manager.generateCodeNotuple(tfe_info);
 		else
 			return manager.generateCode3(tfe_info, data_info);
 
 	};
 	public StringBuffer generateCode4(SSQLparser parser, ExtList data_info) {
 		ITFE tfe_info = parser.get_TFEschema();
 
 		//	ɬ�פʤ饳���ȥ����ȳ�����Manager������ѹ
 		//	manager.preProcess(tab,le,le1,le2,le3);
 		//	manager.createSchema(tab,le,le1,le2,le3);
 
 		Log.out("===============================");
 		Log.out("     generateCode4 is start     ");
 		Log.out("===============================");
 
 		// ?�ֳ��� Grouper�ΤȤ���data_info��Ĵ����?
 		if (tfe_info instanceof Grouper && data_info.size() != 0) {
 			data_info = (ExtList) data_info.get(0);
 		}
 		Log.out("data_info.size " + data_info.size());
 
 		if(data_info.size() == 0)
 			return manager.generateCodeNotuple(tfe_info);
 		else
 			return manager.generateCode4(tfe_info, data_info);
 
 	};
 	public StringBuffer generateCssfile(SSQLparser parser, ExtList data_info) {
 		ITFE tfe_info = parser.get_TFEschema();
 
 		//	ɬ�פʤ饳���ȥ����ȳ�����Manager������ѹ
 		//	manager.preProcess(tab,le,le1,le2,le3);
 		//	manager.createSchema(tab,le,le1,le2,le3);
 
 		Log.out("==================================");
 		Log.out("     generateCssfile is start     ");
 		Log.out("==================================");
 
 		// ?�ֳ��� Grouper�ΤȤ���data_info��Ĵ����?
 		if (tfe_info instanceof Grouper && data_info.size() != 0) {
 			data_info = (ExtList) data_info.get(0);
 		}
 		Log.out("data_info.size " + data_info.size());
 
 		if(data_info.size() == 0)
 			return manager.generateCodeNotuple(tfe_info);
 		else
 			return manager.generateCssfile(tfe_info, data_info);
 
 	}
 
 	public Factory getFactory() {
 		return factory;
 	}
 
 	public String getMedia() {
 		return media;
 	}
 
 	public void setMedia(String media) {
 		this.media = media;
 	}
 
 	public void setFactory(Factory factory) {
 		this.factory = factory;
 	};
 
 	//tk end///////////////////////////////////////////////////////////////////////////////
 }
