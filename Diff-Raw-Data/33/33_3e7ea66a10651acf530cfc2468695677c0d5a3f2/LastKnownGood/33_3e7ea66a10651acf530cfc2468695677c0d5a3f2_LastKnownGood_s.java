 package niagara.logical;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import niagara.connection_server.Catalog;
 import niagara.connection_server.InvalidPlanException;
 import niagara.logical.predicates.Predicate;
 import niagara.logical.predicates.True;
 import niagara.optimizer.colombia.Attribute;
 import niagara.optimizer.colombia.Attrs;
 import niagara.optimizer.colombia.ICatalog;
 import niagara.optimizer.colombia.LogicalProperty;
 import niagara.optimizer.colombia.Op;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * The <code>Select</code> operator does a selection - so much for useful
  * comments!
  */
 @SuppressWarnings("unchecked")
 public class LastKnownGood extends UnaryOperator {
 
 	/** Number of tuples to allow through */
 	private Predicate pred;
 	private Boolean logging = false;
 	private Boolean propagate = false;
 	private Boolean exploit = false;
 	private String groupBy;
 	private String ts;
 	private Vector<Attribute> groupByAttrs;
 	private Attrs tsAttrs;
 
 	public LastKnownGood() {
 	}
 
 	public LastKnownGood(Predicate pred, String groupAttr) {
 		this.pred = pred;
 		this.groupBy = groupAttr;
 	}
 
 	public LastKnownGood(LastKnownGood selOp, String groupAttr) {
 		this(selOp.pred, groupAttr);
 	}
 
 	public Op opCopy() {
 		LastKnownGood op = new LastKnownGood();
 		op.groupBy = this.groupBy;
 		op.groupByAttrs = this.groupByAttrs;
 		op.pred = this.pred;
 		op.logging = this.logging;
 		op.propagate = this.propagate;
 		op.exploit = this.exploit;
		op.tsAttrs = tsAttrs;
 		return op;
 	}
 
 	/**
 	 * print the operator to the standard output
 	 */
 	public void dump() {
 		System.out.println("Last Known Good:");
 		pred.dump(1);
 	}
 
 	public String toString() {
 		return " LastKnownGood " + pred.toString();
 	}
 
 	public void dumpAttributesInXML(StringBuffer sb) {
 		sb.append(" ");
 	}
 
 	public void dumpChildrenInXML(StringBuffer sb) {
 		sb.append(">");
 		pred.toXML(sb);
 		sb.append("</LastKnownGood>");
 	}
 
 	public void loadFromXML(Element e, LogicalProperty[] inputProperties,
 			Catalog catalog) throws InvalidPlanException {
 		
 		groupBy = e.getAttribute("groupby");
 		
 		ts = e.getAttribute("tsattr");
 		
 		NodeList children = e.getChildNodes();
 		Element predElt = null;
 		for (int i = 0; i < children.getLength(); i++) {
 			if (children.item(i) instanceof Element) {
 				predElt = (Element) children.item(i);
 				break;
 			}
 		}
 		pred = Predicate.loadFromXML(predElt, inputProperties);
 		
 		groupByAttrs = new Vector<Attribute>();
 		StringTokenizer st = new StringTokenizer(groupBy);
 		while (st.hasMoreTokens()) {
 			String varName = st.nextToken();
 			Attribute attr = Variable.findVariable(inputProperties, varName);
 			groupByAttrs.addElement(attr);
 		}
 		
 		tsAttrs = new Attrs();
 		StringTokenizer stts = new StringTokenizer(ts);
 		while (stts.hasMoreTokens()) {
 			String varName = stts.nextToken();
 			Attribute attr = Variable.findVariable(inputProperties, varName);
 			tsAttrs.add(attr);
 		}
 		
 
 		String l = e.getAttribute("log");
 		if(l.equals("yes"))
 			logging = true;
 		else logging = false;
 		
 		String p = e.getAttribute("propagate");
 		if(p.equals("yes"))
 			propagate = true;
 		else propagate = false;
 
 		String ex = e.getAttribute("exploit");
 		if(ex.equals("yes"))
 			exploit = true;
 		else exploit = false;
 
 		
 	}
 
 	public Boolean getLogging() {
 		return logging;
 	}
 	
 	public Boolean getPropagate(){
 		return propagate;
 	}
 	
 	public Boolean getExploit(){
 		return exploit;
 	}
 	
 	public Vector<Attribute> getGroupByAttrs(){
 		return groupByAttrs;
 	}
 	
 	public Attrs getTSAttr() {
 		return tsAttrs;
 	}
 	
 	
 	public LogicalProperty findLogProp(ICatalog catalog, LogicalProperty[] input) {
 		LogicalProperty result = input[0].copy();
 		result.setCardinality(result.getCardinality() * pred.selectivity());
 		return result;
 	}
 
 	/**
 	 * @see niagara.xmlql_parser.op_tree.op#requiredInputAttributes(Attrs)
 	 */
 	public Attrs requiredInputAttributes(Attrs inputAttrs) {
 		ArrayList al = new ArrayList();
 		pred.getReferencedVariables(al);
 		Attrs reqd = new Attrs(al);
 		assert inputAttrs.contains(reqd);
 		return reqd;
 	}
 
 	public boolean equals(Object o) {
 		if (o == null || !(o instanceof LastKnownGood))
 			return false;
 		if(((LastKnownGood)o).logging != logging)
 			return false;
 		if(((LastKnownGood)o).propagate != propagate)
 			return false;
 		if(((LastKnownGood)o).exploit != exploit)
 			return false;
 		if (o.getClass() != LastKnownGood.class)
 			return o.equals(this);
 		LastKnownGood s = (LastKnownGood) o;
 		return pred.equals(s.pred);
 	}
 
 	public int hashCode() {
 		return pred.hashCode();// + logging.hashCode();
 	}
 
 	public Predicate getPredicate() {
 		return pred;
 	}
 
 	public boolean isEmpty() {
 		return pred.equals(True.getTrue());
 	}
 
 	/* Can we combine the selection with this unnesting? */
 	public boolean isPushableInto(Unnest unnest) {
 		ArrayList al = new ArrayList();
 		pred.getReferencedVariables(al);
 		return (al.size() == 1 && ((Variable) al.get(0)).equals(unnest
 				.getVariable()));
 	}
 
 	/**
 	 * used to set the predicate of Select operator. A list of predicate are
 	 * ANDed to produce single predicate for this operator
 	 * 
 	 * @param list
 	 *            of predicates KT - this is used by the trigger_engine *@#$*
 	 */
 	public void setSelect(Vector _preds) {
 		pred = niagara.xmlql_parser.Util.andPredicates(_preds);
 	}
 
 }
