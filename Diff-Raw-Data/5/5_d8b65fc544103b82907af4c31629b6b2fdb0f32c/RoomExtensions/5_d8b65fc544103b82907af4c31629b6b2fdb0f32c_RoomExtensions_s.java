 package org.eclipse.etrice.generator.extensions;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.Attribute;
 import org.eclipse.etrice.core.room.BaseState;
 import org.eclipse.etrice.core.room.DataClass;
 import org.eclipse.etrice.core.room.DetailCode;
 import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.Guard;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.MessageHandler;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.PortClass;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.RefableType;
 import org.eclipse.etrice.core.room.RefinedState;
 import org.eclipse.etrice.core.room.RoomClass;
 import org.eclipse.etrice.core.room.RoomModel;
 import org.eclipse.etrice.core.room.SAPRef;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.ServiceImplementation;
 import org.eclipse.etrice.core.room.StandardOperation;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.Trigger;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.VarDecl;
 import org.eclipse.etrice.generator.base.DetailCodeTranslator;
 import org.eclipse.etrice.generator.etricegen.ActiveTrigger;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.etricegen.TransitionChain;
 import org.eclipse.etrice.generator.extensions.RoomNameProv;
 import org.eclipse.xtext.xbase.lib.BooleanExtensions;
 import org.eclipse.xtext.xbase.lib.CollectionExtensions;
 import org.eclipse.xtext.xbase.lib.ComparableExtensions;
 import org.eclipse.xtext.xbase.lib.Functions.Function1;
 import org.eclipse.xtext.xbase.lib.IntegerExtensions;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.eclipse.xtext.xbase.lib.ObjectExtensions;
 import org.eclipse.xtext.xbase.lib.StringExtensions;
 
 @SuppressWarnings("all")
 @Singleton
 public class RoomExtensions {
   @Inject
   private RoomNameProv nameProvider;
   
   public <T extends Object> List<T> union(final List<T> l1, final List<T> l2) {
       ArrayList<T> _arrayList = new ArrayList<T>();
       ArrayList<T> ret = _arrayList;
       ret.addAll(l1);
       ret.addAll(l2);
       return ret;
   }
   
   public <T extends Object> Iterable<T> union(final Iterable<T> l1, final Iterable<T> l2) {
       ArrayList<T> _arrayList = new ArrayList<T>();
       ArrayList<T> ret = _arrayList;
       CollectionExtensions.<T>addAll(ret, l1);
       CollectionExtensions.<T>addAll(ret, l2);
       return ret;
   }
   
   public List<Port> punion(final List<Port> in1, final List<ExternalPort> in2) {
       ArrayList<Port> _arrayList = new ArrayList<Port>();
       ArrayList<Port> ret = _arrayList;
       for (final ExternalPort ele : in2) {
         Port _ifport = ele.getIfport();
         ret.add(_ifport);
       }
       ret.addAll(in1);
       return ret;
   }
   
   public String getGenerationPathSegment() {
     return "/src-gen/";
   }
   
   public String getDocGenerationPathSegment() {
     return "/doc-gen/";
   }
   
   public String getModelPath(final EObject e) {
       Resource _eResource = e.eResource();
       Resource res = _eResource;
       boolean _operator_equals = ObjectExtensions.operator_equals(res, null);
       if (_operator_equals) {
         return "";
       } else {
         URI _uRI = res.getURI();
         String _fileString = _uRI.toFileString();
         return _fileString;
       }
   }
   
   public String getPackage(final RoomClass rc) {
     EObject _eContainer = rc.eContainer();
     String _name = ((RoomModel) _eContainer).getName();
     return _name;
   }
   
   public String getPathFromPackage(final String packageName) {
     String _replaceAll = packageName.replaceAll("\\.", "/");
     String _operator_plus = StringExtensions.operator_plus(_replaceAll, "/");
     return _operator_plus;
   }
   
   public String getPath(final RoomClass rc) {
     String _package = this.getPackage(rc);
     String _pathFromPackage = this.getPathFromPackage(_package);
     return _pathFromPackage;
   }
   
   public String getProjectPath(final EObject e) {
       Resource _eResource = e.eResource();
       Resource res = _eResource;
       boolean _operator_equals = ObjectExtensions.operator_equals(res, null);
       if (_operator_equals) {
         return "";
       } else {
         {
           File _file = new File("");
           File tmpf = _file;
           URI _uRI = res.getURI();
           String _fileString = _uRI.toFileString();
           File _file_1 = new File(_fileString);
           tmpf = _file_1;
           boolean _isFile = tmpf.isFile();
           boolean _operator_not = BooleanExtensions.operator_not(_isFile);
           if (_operator_not) {
             return "";
           }
           boolean isProject = false;
           Boolean _xdowhileexpression;
           do {
             {
               File _parentFile = tmpf.getParentFile();
               tmpf = _parentFile;
               String[] _list = tmpf.list();
               String[] contents = _list;
               for (final String f : contents) {
                 boolean _equals = f.equals(".project");
                 if (_equals) {
                   isProject = true;
                 }
               }
             }
             boolean _operator_and = false;
             boolean _operator_not_1 = BooleanExtensions.operator_not(isProject);
             if (!_operator_not_1) {
               _operator_and = false;
             } else {
               File _parentFile_1 = tmpf.getParentFile();
               boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_parentFile_1, null);
               _operator_and = BooleanExtensions.operator_and(_operator_not_1, _operator_notEquals);
             }
             _xdowhileexpression = _operator_and;
           } while(_xdowhileexpression);
           String _absolutePath = tmpf.getAbsolutePath();
           return _absolutePath;
         }
       }
   }
   
   public String getGenerationTargetPath(final EObject e) {
     String _projectPath = this.getProjectPath(e);
     String _generationPathSegment = this.getGenerationPathSegment();
     String _operator_plus = StringExtensions.operator_plus(_projectPath, _generationPathSegment);
     return _operator_plus;
   }
   
   public String getDocGenerationTargetPath(final EObject e) {
     String _projectPath = this.getProjectPath(e);
     String _docGenerationPathSegment = this.getDocGenerationPathSegment();
     String _operator_plus = StringExtensions.operator_plus(_projectPath, _docGenerationPathSegment);
     return _operator_plus;
   }
   
   public List<Port> getEndPorts(final ActorClass ac) {
     EList<Port> _intPorts = ac.getIntPorts();
     EList<ExternalPort> _extPorts = ac.getExtPorts();
     List<Port> _punion = this.punion(_intPorts, _extPorts);
     return _punion;
   }
   
   public List<Port> getAllEndPorts(final ActorClass ac) {
     List<Port> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       List<Port> _endPorts = this.getEndPorts(ac);
       return _endPorts;
     } else {
       ActorClass _base_1 = ac.getBase();
       List<Port> _allEndPorts = this.getAllEndPorts(_base_1);
       List<Port> _endPorts_1 = this.getEndPorts(ac);
       List<Port> _union = this.<Port>union(_allEndPorts, _endPorts_1);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public List<SAPRef> getAllSAPs(final ActorClass ac) {
     List<SAPRef> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       EList<SAPRef> _strSAPs = ac.getStrSAPs();
       return _strSAPs;
     } else {
       ActorClass _base_1 = ac.getBase();
       EList<SAPRef> _strSAPs_1 = _base_1.getStrSAPs();
       EList<SAPRef> _strSAPs_2 = ac.getStrSAPs();
       List<SAPRef> _union = this.<SAPRef>union(_strSAPs_1, _strSAPs_2);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public List<ServiceImplementation> getAllServiceImplementations(final ActorClass ac) {
     List<ServiceImplementation> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       EList<ServiceImplementation> _serviceImplementations = ac.getServiceImplementations();
       return _serviceImplementations;
     } else {
       ActorClass _base_1 = ac.getBase();
       EList<ServiceImplementation> _serviceImplementations_1 = _base_1.getServiceImplementations();
       EList<ServiceImplementation> _serviceImplementations_2 = ac.getServiceImplementations();
       List<ServiceImplementation> _union = this.<ServiceImplementation>union(_serviceImplementations_1, _serviceImplementations_2);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public String getPathName(final String path) {
     String _replaceAll = path.replaceAll("/", "_");
     return _replaceAll;
   }
   
   public List<Attribute> getAllAttributes(final DataClass dc) {
     List<Attribute> _xifexpression = null;
     DataClass _base = dc.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       EList<Attribute> _attributes = dc.getAttributes();
       return _attributes;
     } else {
       DataClass _base_1 = dc.getBase();
       EList<Attribute> _attributes_1 = _base_1.getAttributes();
       List<Attribute> _allAttributes = this.getAllAttributes(dc);
       List<Attribute> _union = this.<Attribute>union(_attributes_1, _allAttributes);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public List<Attribute> getAllAttributes(final ActorClass ac) {
     List<Attribute> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       EList<Attribute> _attributes = ac.getAttributes();
       return _attributes;
     } else {
       ActorClass _base_1 = ac.getBase();
       EList<Attribute> _attributes_1 = _base_1.getAttributes();
       List<Attribute> _allAttributes = this.getAllAttributes(ac);
       List<Attribute> _union = this.<Attribute>union(_attributes_1, _allAttributes);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public String getPortClassName(final ProtocolClass p, final boolean conj) {
     String _portClassName = this.getPortClassName(p, conj, false);
     return _portClassName;
   }
   
   public String getPortClassName(final ProtocolClass p, final boolean conj, final boolean repl) {
     String _name = p.getName();
     String _xifexpression = null;
     if (conj) {
       _xifexpression = "Conj";
     } else {
       _xifexpression = "";
     }
     String _operator_plus = StringExtensions.operator_plus(_name, _xifexpression);
     String _xifexpression_1 = null;
     if (repl) {
       _xifexpression_1 = "Repl";
     } else {
       _xifexpression_1 = "";
     }
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _xifexpression_1);
     String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, "Port");
     return _operator_plus_2;
   }
   
   public String getPortClassName(final Port p) {
     ProtocolClass _protocol = p.getProtocol();
     boolean _isConjugated = p.isConjugated();
     boolean _isReplicated = p.isReplicated();
     String _portClassName = this.getPortClassName(_protocol, _isConjugated, _isReplicated);
     return _portClassName;
   }
   
   public String getPortClassName(final ExternalPort p) {
     Port _ifport = p.getIfport();
     String _portClassName = this.getPortClassName(_ifport);
     return _portClassName;
   }
   
   public String getPortClassName(final SAPRef sap) {
     ProtocolClass _protocol = sap.getProtocol();
     String _portClassName = this.getPortClassName(_protocol, true);
     return _portClassName;
   }
   
   public String getPortClassName(final ServiceImplementation svc) {
     SPPRef _spp = svc.getSpp();
     ProtocolClass _protocol = _spp.getProtocol();
    String _portClassName = this.getPortClassName(_protocol, false);
    String _operator_plus = StringExtensions.operator_plus(_portClassName, "Repl");
    return _operator_plus;
   }
   
   public List<Message> getAllIncomingMessages(final ProtocolClass pc) {
     ProtocolClass _base = pc.getBase();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_base, null);
     if (_operator_notEquals) {
       ProtocolClass _base_1 = pc.getBase();
       List<Message> _allIncomingMessages = this.getAllIncomingMessages(_base_1);
       EList<Message> _incomingMessages = pc.getIncomingMessages();
       List<Message> _union = this.<Message>union(_allIncomingMessages, _incomingMessages);
       return _union;
     } else {
       EList<Message> _incomingMessages_1 = pc.getIncomingMessages();
       return _incomingMessages_1;
     }
   }
   
   public List<Message> getAllOutgoingMessages(final ProtocolClass pc) {
     EList<Message> _xifexpression = null;
     ProtocolClass _base = pc.getBase();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_base, null);
     if (_operator_notEquals) {
       ProtocolClass _base_1 = pc.getBase();
       List<Message> _allOutgoingMessages = this.getAllOutgoingMessages(_base_1);
       EList<Message> _outgoingMessages = pc.getOutgoingMessages();
       List<Message> _union = this.<Message>union(_allOutgoingMessages, _outgoingMessages);
       return _union;
     } else {
       EList<Message> _outgoingMessages_1 = pc.getOutgoingMessages();
       _xifexpression = _outgoingMessages_1;
     }
     return _xifexpression;
   }
   
   public List<Message> getIncoming(final ProtocolClass pc, final boolean conj) {
     if (conj) {
       List<Message> _allOutgoingMessages = this.getAllOutgoingMessages(pc);
       return _allOutgoingMessages;
     } else {
       List<Message> _allIncomingMessages = this.getAllIncomingMessages(pc);
       return _allIncomingMessages;
     }
   }
   
   public List<Message> getOutgoing(final ProtocolClass pc, final boolean conj) {
     if (conj) {
       List<Message> _allIncomingMessages = this.getAllIncomingMessages(pc);
       return _allIncomingMessages;
     } else {
       List<Message> _allOutgoingMessages = this.getAllOutgoingMessages(pc);
       return _allOutgoingMessages;
     }
   }
   
   public PortClass getPortClass(final ProtocolClass pc, final boolean conj) {
     if (conj) {
       PortClass _conjugate = pc.getConjugate();
       return _conjugate;
     } else {
       PortClass _regular = pc.getRegular();
       return _regular;
     }
   }
   
   public boolean handlesSend(final ProtocolClass pc, final boolean conj) {
       PortClass _portClass = this.getPortClass(pc, conj);
       boolean _operator_equals = ObjectExtensions.operator_equals(_portClass, null);
       if (_operator_equals) {
         return false;
       } else {
         PortClass _portClass_1 = this.getPortClass(pc, conj);
         EList<MessageHandler> _msgHandlers = _portClass_1.getMsgHandlers();
         for (final MessageHandler hdlr : _msgHandlers) {
           List<Message> _outgoing = this.getOutgoing(pc, conj);
           Message _msg = hdlr.getMsg();
           boolean _contains = _outgoing.contains(_msg);
           if (_contains) {
             return true;
           }
         }
       }
       return false;
   }
   
   public boolean handlesReceive(final ProtocolClass pc, final boolean conj) {
       PortClass _portClass = this.getPortClass(pc, conj);
       boolean _operator_equals = ObjectExtensions.operator_equals(_portClass, null);
       if (_operator_equals) {
         return false;
       } else {
         PortClass _portClass_1 = this.getPortClass(pc, conj);
         EList<MessageHandler> _msgHandlers = _portClass_1.getMsgHandlers();
         for (final MessageHandler hdlr : _msgHandlers) {
           List<Message> _incoming = this.getIncoming(pc, conj);
           Message _msg = hdlr.getMsg();
           boolean _contains = _incoming.contains(_msg);
           if (_contains) {
             return true;
           }
         }
       }
       return false;
   }
   
   public List<MessageHandler> getReceiveHandlers(final ProtocolClass pc, final boolean conj) {
     PortClass _portClass = this.getPortClass(pc, conj);
     boolean _operator_equals = ObjectExtensions.operator_equals(_portClass, null);
     if (_operator_equals) {
       ArrayList<MessageHandler> _arrayList = new ArrayList<MessageHandler>();
       return _arrayList;
     } else {
       {
         ArrayList<MessageHandler> _arrayList_1 = new ArrayList<MessageHandler>();
         ArrayList<MessageHandler> res = _arrayList_1;
         PortClass _portClass_1 = this.getPortClass(pc, conj);
         EList<MessageHandler> _msgHandlers = _portClass_1.getMsgHandlers();
         for (final MessageHandler hdlr : _msgHandlers) {
           List<Message> _incoming = this.getIncoming(pc, conj);
           Message _msg = hdlr.getMsg();
           boolean _contains = _incoming.contains(_msg);
           if (_contains) {
             res.add(hdlr);
           }
         }
         return res;
       }
     }
   }
   
   public List<MessageHandler> getSendHandlers(final ProtocolClass pc, final boolean conj) {
     PortClass _portClass = this.getPortClass(pc, conj);
     boolean _operator_equals = ObjectExtensions.operator_equals(_portClass, null);
     if (_operator_equals) {
       ArrayList<MessageHandler> _arrayList = new ArrayList<MessageHandler>();
       return _arrayList;
     } else {
       {
         ArrayList<MessageHandler> _arrayList_1 = new ArrayList<MessageHandler>();
         ArrayList<MessageHandler> res = _arrayList_1;
         PortClass _portClass_1 = this.getPortClass(pc, conj);
         EList<MessageHandler> _msgHandlers = _portClass_1.getMsgHandlers();
         for (final MessageHandler hdlr : _msgHandlers) {
           List<Message> _outgoing = this.getOutgoing(pc, conj);
           Message _msg = hdlr.getMsg();
           boolean _contains = _outgoing.contains(_msg);
           if (_contains) {
             res.add(hdlr);
           }
         }
         return res;
       }
     }
   }
   
   public MessageHandler getSendHandler(final Message m, final boolean conj) {
     EObject _eContainer = m.eContainer();
     List<MessageHandler> _sendHandlers = this.getSendHandlers(((ProtocolClass) _eContainer), conj);
     final Function1<MessageHandler,Boolean> _function = new Function1<MessageHandler,Boolean>() {
         public Boolean apply(final MessageHandler e) {
           Message _msg = e.getMsg();
           boolean _operator_equals = ObjectExtensions.operator_equals(_msg, m);
           return ((Boolean)_operator_equals);
         }
       };
     MessageHandler _findFirst = IterableExtensions.<MessageHandler>findFirst(_sendHandlers, _function);
     return _findFirst;
   }
   
   public boolean isIncoming(final Message m) {
     EObject _eContainer = m.eContainer();
     List<Message> _allIncomingMessages = this.getAllIncomingMessages(((ProtocolClass) _eContainer));
     boolean _contains = _allIncomingMessages.contains(m);
     return _contains;
   }
   
   public String getCodeName(final Message m) {
     boolean _isIncoming = this.isIncoming(m);
     if (_isIncoming) {
       String _name = m.getName();
       String _operator_plus = StringExtensions.operator_plus("IN_", _name);
       return _operator_plus;
     } else {
       String _name_1 = m.getName();
       String _operator_plus_1 = StringExtensions.operator_plus("OUT_", _name_1);
       return _operator_plus_1;
     }
   }
   
   public boolean isLeaf(final State s) {
     StateGraph _subgraph = s.getSubgraph();
     boolean _operator_equals = ObjectExtensions.operator_equals(_subgraph, null);
     return _operator_equals;
   }
   
   public List<State> getLeafStateList(final StateGraph sg) {
       ArrayList<State> _arrayList = new ArrayList<State>();
       ArrayList<State> res = _arrayList;
       EList<State> _states = sg.getStates();
       for (final State s : _states) {
         List<State> _leafStateList = this.getLeafStateList(s);
         res.addAll(_leafStateList);
       }
       return res;
   }
   
   public List<State> getLeafStateList(final State s) {
     boolean _isLeaf = this.isLeaf(s);
     if (_isLeaf) {
       {
         ArrayList<State> _arrayList = new ArrayList<State>();
         ArrayList<State> res = _arrayList;
         res.add(s);
         return res;
       }
     } else {
       StateGraph _subgraph = s.getSubgraph();
       List<State> _leafStateList = this.getLeafStateList(_subgraph);
       return _leafStateList;
     }
   }
   
   public List<State> getStateList(final StateGraph sg) {
       ArrayList<State> _arrayList = new ArrayList<State>();
       ArrayList<State> ret = _arrayList;
       EList<State> _states = sg.getStates();
       for (final State e : _states) {
         {
           ret.add(e);
           StateGraph _subgraph = e.getSubgraph();
           StateGraph tmp = _subgraph;
           boolean _operator_notEquals = ObjectExtensions.operator_notEquals(tmp, null);
           if (_operator_notEquals) {
             StateGraph _subgraph_1 = e.getSubgraph();
             List<State> _stateList = this.getStateList(_subgraph_1);
             ret.addAll(_stateList);
           }
         }
       }
       return ret;
   }
   
   public List<State> getBaseStateList(final StateGraph sg) {
       ArrayList<State> _arrayList = new ArrayList<State>();
       ArrayList<State> ret = _arrayList;
       List<State> _stateList = this.getStateList(sg);
       for (final State e : _stateList) {
         if ((e instanceof BaseState)) {
           ret.add(e);
         }
       }
       return ret;
   }
   
   public ArrayList<State> getLeafStatesFirst(final List<State> states) {
       ArrayList<State> _arrayList = new ArrayList<State>();
       ArrayList<State> leaf = _arrayList;
       ArrayList<State> _arrayList_1 = new ArrayList<State>();
       ArrayList<State> nonLeaf = _arrayList_1;
       for (final State state : states) {
         boolean _isLeaf = this.isLeaf(state);
         if (_isLeaf) {
           leaf.add(state);
         } else {
           nonLeaf.add(state);
         }
       }
       leaf.addAll(nonLeaf);
       return leaf;
   }
   
   public List<State> getAllBaseStates(final ActorClass ac) {
     List<State> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       StateGraph _stateMachine = ac.getStateMachine();
       List<State> _baseStateList = this.getBaseStateList(_stateMachine);
       return _baseStateList;
     } else {
       ActorClass _base_1 = ac.getBase();
       List<State> _allBaseStates = this.getAllBaseStates(_base_1);
       StateGraph _stateMachine_1 = ac.getStateMachine();
       List<State> _baseStateList_1 = this.getBaseStateList(_stateMachine_1);
       List<State> _union = this.<State>union(_allBaseStates, _baseStateList_1);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public List<State> getAllBaseStatesLeavesFirst(final ActorClass ac) {
     List<State> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       StateGraph _stateMachine = ac.getStateMachine();
       List<State> _baseStateList = this.getBaseStateList(_stateMachine);
       ArrayList<State> _leafStatesFirst = this.getLeafStatesFirst(_baseStateList);
       return _leafStatesFirst;
     } else {
       ActorClass _base_1 = ac.getBase();
       List<State> _allBaseStates = this.getAllBaseStates(_base_1);
       ArrayList<State> _leafStatesFirst_1 = this.getLeafStatesFirst(_allBaseStates);
       StateGraph _stateMachine_1 = ac.getStateMachine();
       List<State> _baseStateList_1 = this.getBaseStateList(_stateMachine_1);
       ArrayList<State> _leafStatesFirst_2 = this.getLeafStatesFirst(_baseStateList_1);
       List<State> _union = this.<State>union(_leafStatesFirst_1, _leafStatesFirst_2);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public List<State> getAllLeafStates(final ActorClass ac) {
     List<State> _xifexpression = null;
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       StateGraph _stateMachine = ac.getStateMachine();
       List<State> _leafStateList = this.getLeafStateList(_stateMachine);
       return _leafStateList;
     } else {
       ActorClass _base_1 = ac.getBase();
       List<State> _allLeafStates = this.getAllLeafStates(_base_1);
       StateGraph _stateMachine_1 = ac.getStateMachine();
       List<State> _leafStateList_1 = this.getLeafStateList(_stateMachine_1);
       List<State> _union = this.<State>union(_allLeafStates, _leafStateList_1);
       _xifexpression = _union;
     }
     return _xifexpression;
   }
   
   public boolean overridesStop(final ActorClass ac) {
     boolean _operator_or = false;
     EList<StandardOperation> _operations = ac.getOperations();
     final Function1<StandardOperation,Boolean> _function = new Function1<StandardOperation,Boolean>() {
         public Boolean apply(final StandardOperation e) {
           boolean _operator_and = false;
           boolean _operator_and_1 = false;
           String _name = e.getName();
           boolean _operator_equals = ObjectExtensions.operator_equals(_name, "stop");
           if (!_operator_equals) {
             _operator_and_1 = false;
           } else {
             EList<VarDecl> _arguments = e.getArguments();
             boolean _isEmpty = _arguments.isEmpty();
             _operator_and_1 = BooleanExtensions.operator_and(_operator_equals, _isEmpty);
           }
           if (!_operator_and_1) {
             _operator_and = false;
           } else {
             RefableType _returntype = e.getReturntype();
             boolean _operator_equals_1 = ObjectExtensions.operator_equals(_returntype, null);
             _operator_and = BooleanExtensions.operator_and(_operator_and_1, _operator_equals_1);
           }
           return ((Boolean)_operator_and);
         }
       };
     boolean _exists = IterableExtensions.<StandardOperation>exists(_operations, _function);
     if (_exists) {
       _operator_or = true;
     } else {
       boolean _operator_and = false;
       ActorClass _base = ac.getBase();
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_base, null);
       if (!_operator_notEquals) {
         _operator_and = false;
       } else {
         ActorClass _base_1 = ac.getBase();
         boolean _overridesStop = this.overridesStop(_base_1);
         _operator_and = BooleanExtensions.operator_and(_operator_notEquals, _overridesStop);
       }
       _operator_or = BooleanExtensions.operator_or(_exists, _operator_and);
     }
     return _operator_or;
   }
   
   public int getNumberOfInheritedStates(final ActorClass ac) {
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       return 0;
     } else {
       ActorClass _base_1 = ac.getBase();
       StateGraph _stateMachine = _base_1.getStateMachine();
       List<State> _stateList = this.getStateList(_stateMachine);
       int _size = _stateList.size();
       ActorClass _base_2 = ac.getBase();
       int _numberOfInheritedStates = this.getNumberOfInheritedStates(_base_2);
       int _operator_plus = IntegerExtensions.operator_plus(((Integer)_size), ((Integer)_numberOfInheritedStates));
       return _operator_plus;
     }
   }
   
   public int getNumberOfInheritedBaseStates(final ActorClass ac) {
     ActorClass _base = ac.getBase();
     boolean _operator_equals = ObjectExtensions.operator_equals(_base, null);
     if (_operator_equals) {
       return 0;
     } else {
       ActorClass _base_1 = ac.getBase();
       StateGraph _stateMachine = _base_1.getStateMachine();
       List<State> _baseStateList = this.getBaseStateList(_stateMachine);
       int _size = _baseStateList.size();
       ActorClass _base_2 = ac.getBase();
       int _numberOfInheritedBaseStates = this.getNumberOfInheritedBaseStates(_base_2);
       int _operator_plus = IntegerExtensions.operator_plus(((Integer)_size), ((Integer)_numberOfInheritedBaseStates));
       return _operator_plus;
     }
   }
   
   public String getStateId(final State s) {
     String _genStateId = RoomNameProv.getGenStateId(s);
     return _genStateId;
   }
   
   public String getStatePathName(final State s) {
     String _genStatePathName = RoomNameProv.getGenStatePathName(s);
     return _genStatePathName;
   }
   
   public String getChainId(final TransitionChain t) {
     String _genChainId = RoomNameProv.getGenChainId(t);
     return _genChainId;
   }
   
   public boolean hasGuard(final Trigger tr) {
     boolean _operator_and = false;
     boolean _operator_and_1 = false;
     Guard _guard = tr.getGuard();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_guard, null);
     if (!_operator_notEquals) {
       _operator_and_1 = false;
     } else {
       Guard _guard_1 = tr.getGuard();
       DetailCode _guard_2 = _guard_1.getGuard();
       boolean _operator_notEquals_1 = ObjectExtensions.operator_notEquals(_guard_2, null);
       _operator_and_1 = BooleanExtensions.operator_and(_operator_notEquals, _operator_notEquals_1);
     }
     if (!_operator_and_1) {
       _operator_and = false;
     } else {
       Guard _guard_3 = tr.getGuard();
       DetailCode _guard_4 = _guard_3.getGuard();
       EList<String> _commands = _guard_4.getCommands();
       int _size = _commands.size();
       boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size), ((Integer)0));
       _operator_and = BooleanExtensions.operator_and(_operator_and_1, _operator_greaterThan);
     }
     return _operator_and;
   }
   
   public boolean hasGuard(final ExpandedActorClass ac, final ActiveTrigger at) {
       boolean hasGuard = false;
       EList<TriggeredTransition> _transitions = at.getTransitions();
       for (final TriggeredTransition t : _transitions) {
         EList<Trigger> _triggers = t.getTriggers();
         final Function1<Trigger,Boolean> _function = new Function1<Trigger,Boolean>() {
             public Boolean apply(final Trigger e) {
               boolean _operator_and = false;
               String _trigger = at.getTrigger();
               boolean _isMatching = ac.isMatching(e, _trigger);
               if (!_isMatching) {
                 _operator_and = false;
               } else {
                 boolean _hasGuard = RoomExtensions.this.hasGuard(e);
                 _operator_and = BooleanExtensions.operator_and(_isMatching, _hasGuard);
               }
               return ((Boolean)_operator_and);
             }
           };
         boolean _exists = IterableExtensions.<Trigger>exists(_triggers, _function);
         if (_exists) {
           hasGuard = true;
         }
       }
       return hasGuard;
   }
   
   public boolean hasEntryCode(final State s) {
     boolean _operator_and = false;
     DetailCode _entryCode = s.getEntryCode();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_entryCode, null);
     if (!_operator_notEquals) {
       _operator_and = false;
     } else {
       DetailCode _entryCode_1 = s.getEntryCode();
       EList<String> _commands = _entryCode_1.getCommands();
       int _size = _commands.size();
       boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size), ((Integer)0));
       _operator_and = BooleanExtensions.operator_and(_operator_notEquals, _operator_greaterThan);
     }
     return _operator_and;
   }
   
   public boolean hasExitCode(final State s) {
     boolean _operator_and = false;
     DetailCode _exitCode = s.getExitCode();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_exitCode, null);
     if (!_operator_notEquals) {
       _operator_and = false;
     } else {
       DetailCode _exitCode_1 = s.getExitCode();
       EList<String> _commands = _exitCode_1.getCommands();
       int _size = _commands.size();
       boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size), ((Integer)0));
       _operator_and = BooleanExtensions.operator_and(_operator_notEquals, _operator_greaterThan);
     }
     return _operator_and;
   }
   
   public String getEntryCode(final ExpandedActorClass ac, final State s, final DetailCodeTranslator dct) {
     String _xifexpression = null;
     if ((s instanceof RefinedState)) {
       String _entryCodeOperationName = RoomNameProv.getEntryCodeOperationName(s);
       String _operator_plus = StringExtensions.operator_plus("super.", _entryCodeOperationName);
       String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "();\n");
       DetailCode _entryCode = s.getEntryCode();
       String _code = ac.getCode(_entryCode);
       String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, _code);
       _xifexpression = _operator_plus_2;
     } else {
       DetailCode _entryCode_1 = s.getEntryCode();
       String _translateDetailCode = dct.translateDetailCode(_entryCode_1);
       _xifexpression = _translateDetailCode;
     }
     return _xifexpression;
   }
   
   public String getExitCode(final ExpandedActorClass ac, final State s, final DetailCodeTranslator dct) {
     String _xifexpression = null;
     if ((s instanceof RefinedState)) {
       DetailCode _exitCode = s.getExitCode();
       String _code = ac.getCode(_exitCode);
       String _operator_plus = StringExtensions.operator_plus(_code, "super.");
       String _entryCodeOperationName = RoomNameProv.getEntryCodeOperationName(s);
       String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _entryCodeOperationName);
       String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, "();\n");
       _xifexpression = _operator_plus_2;
     } else {
       DetailCode _exitCode_1 = s.getExitCode();
       String _translateDetailCode = dct.translateDetailCode(_exitCode_1);
       _xifexpression = _translateDetailCode;
     }
     return _xifexpression;
   }
   
   public boolean hasActionCode(final Transition t) {
     boolean _operator_and = false;
     DetailCode _action = t.getAction();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_action, null);
     if (!_operator_notEquals) {
       _operator_and = false;
     } else {
       DetailCode _action_1 = t.getAction();
       EList<String> _commands = _action_1.getCommands();
       int _size = _commands.size();
       boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size), ((Integer)0));
       _operator_and = BooleanExtensions.operator_and(_operator_notEquals, _operator_greaterThan);
     }
     return _operator_and;
   }
   
   public String getActionCode(final ExpandedActorClass ac, final Transition t, final DetailCodeTranslator dct) {
     DetailCode _action = t.getAction();
     String _translateDetailCode = dct.translateDetailCode(_action);
     return _translateDetailCode;
   }
   
   public String getContextId(final TransitionChain tc) {
     State _stateContext = tc.getStateContext();
     String _stateId = this.getStateId(_stateContext);
     return _stateId;
   }
   
   public Transition getInitTransition(final StateGraph sg) {
       EList<Transition> _transitions = sg.getTransitions();
       for (final Transition tr : _transitions) {
         if ((tr instanceof InitialTransition)) {
           return tr;
         }
       }
       return null;
   }
   
   public boolean hasInitTransition(final StateGraph sg) {
       EList<Transition> _transitions = sg.getTransitions();
       for (final Transition tr : _transitions) {
         if ((tr instanceof InitialTransition)) {
           return true;
         }
       }
       return false;
   }
   
   public boolean hasNonEmptyStateMachine(final ActorClass ac) {
     boolean _operator_and = false;
     StateGraph _stateMachine = ac.getStateMachine();
     boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_stateMachine, null);
     if (!_operator_notEquals) {
       _operator_and = false;
     } else {
       StateGraph _stateMachine_1 = ac.getStateMachine();
       EList<State> _states = _stateMachine_1.getStates();
       boolean _isEmpty = _states.isEmpty();
       boolean _operator_not = BooleanExtensions.operator_not(_isEmpty);
       _operator_and = BooleanExtensions.operator_and(_operator_notEquals, _operator_not);
     }
     return _operator_and;
   }
   
   public List<Transition> getTransitionList(final State s) {
     boolean _isLeaf = this.isLeaf(s);
     if (_isLeaf) {
       ArrayList<Transition> _arrayList = new ArrayList<Transition>();
       return _arrayList;
     } else {
       StateGraph _subgraph = s.getSubgraph();
       List<Transition> _transitionList = this.getTransitionList(_subgraph);
       return _transitionList;
     }
   }
   
   public List<Transition> getTransitionList(final StateGraph sg) {
       EList<Transition> _transitions = sg.getTransitions();
       ArrayList<Transition> _arrayList = new ArrayList<Transition>(_transitions);
       ArrayList<Transition> res = _arrayList;
       EList<State> _states = sg.getStates();
       for (final State s : _states) {
         List<Transition> _transitionList = this.getTransitionList(s);
         res.addAll(_transitionList);
       }
       return res;
   }
 }
