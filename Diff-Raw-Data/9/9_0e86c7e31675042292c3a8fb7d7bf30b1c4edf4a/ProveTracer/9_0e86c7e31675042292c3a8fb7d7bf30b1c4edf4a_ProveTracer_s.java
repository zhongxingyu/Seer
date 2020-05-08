 package suite.lp.doer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import suite.lp.doer.ProverConfig.TraceLevel;
 import suite.node.Data;
 import suite.node.Node;
 import suite.node.Tree;
 import suite.node.io.Formatter;
 import suite.node.io.TermParser.TermOp;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Source;
 
 public class ProveTracer {
 
 	private ProverConfig proverConfig;
 	private List<Record> records = new ArrayList<>();
 	private Record currentRecord = null;
 	private int currentDepth;
 
 	private class Record {
 		private int start, end = -1;
 		private Record parent;
 		private Node query;
 		private int depth;
 		private int nOkays;
 
 		private Record(Record parent, Node query, int depth) {
 			this.parent = parent;
 			this.query = query;
 			this.depth = depth;
 		}
 
 		private void appendTo(StringBuilder sb) {
 			TraceLevel traceLevel = proverConfig.getTraceLevel();
 
 			if (traceLevel == TraceLevel.SIMPLE)
 				sb.append(String.format("[%4s:%-2d]  ", nOkays > 0 ? "OK__" : "FAIL", depth));
 			else if (traceLevel == TraceLevel.DETAIL)
 				sb.append(String.format("%-4d[up=%-4d|oks=%-2d|end=%-4s]  " //
 						, start //
 						, parent != null ? parent.start : 0 //
 						, nOkays //
 						, end >= 0 ? String.valueOf(end) : "" //
 				));
 
 			for (int i = 1; i < depth; i++)
 				sb.append("| ");
 			sb.append(Formatter.dump(query));
 			sb.append("\n");
 		}
 	}
 
 	public ProveTracer(ProverConfig proverConfig) {
 		this.proverConfig = proverConfig;
 	}
 
 	public Node expandWithTrace(Node query, Prover prover, Fun<Node, Node> expand) {
 		Node query1 = new Cloner().clone(query);
 
 		if (currentDepth < 64) {
 			final Record record0 = currentRecord;
 			final int depth0 = currentDepth;
 			final Record record = new Record(record0, query1, currentDepth + 1);
 
 			final Data<Source<Boolean>> enter = new Data<Source<Boolean>>(new Source<Boolean>() {
 				public Boolean source() {
 					currentRecord = record;
 					currentDepth = record.depth;
 					record.start = records.size();
 					records.add(record);
 					return Boolean.TRUE;
 				}
 			});
 
 			final Data<Source<Boolean>> leaveOk = new Data<Source<Boolean>>(new Source<Boolean>() {
 				public Boolean source() {
 					currentRecord = record0;
 					currentDepth = depth0;
 					record.nOkays++;
 					return Boolean.TRUE;
 				}
 			});
 
 			final Data<Source<Boolean>> leaveFail = new Data<Source<Boolean>>(new Source<Boolean>() {
 				public Boolean source() {
 					currentRecord = record0;
 					currentDepth = depth0;
 					record.end = records.size();
 					return Boolean.FALSE;
 				}
 			});
 
 			Node alt = prover.getAlternative();
 			Node rem = prover.getRemaining();
 
			alt = Tree.create(TermOp.OR____, leaveFail, alt);
			rem = Tree.create(TermOp.AND___, leaveOk, rem);
 			query = expand.apply(query);
 			query = Tree.create(TermOp.AND___, enter, query);

			prover.setAlternative(alt);
			prover.setRemaining(rem);
 		} else
 			query = expand.apply(query);
 
 		return query;
 	}
 
 	public String getDump() {
 		StringBuilder sb = new StringBuilder();
 		for (Record record : records)
 			record.appendTo(sb);
 		return sb.toString();
 	}
 
 	public String getStackTrace() {
 		List<Node> traces = new ArrayList<>();
 		Record record = currentRecord;
 
 		while (record != null) {
 			traces.add(record.query);
 			record = record.parent;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = traces.size(); i > 0; i--)
 			sb.append(traces.get(i - 1) + "\n");
 
 		return sb.toString();
 	}
 
 }
