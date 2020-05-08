 package at.jku.busem.diff.rules.predefinied;
 
 import at.jku.busem.diff.core.DiffEngine;
 import at.jku.busem.diff.data.DataSource;
 import at.jku.busem.diff.data.Models;
 import at.jku.busem.diff.data.matching.Match;
 import at.jku.busem.diff.data.matching.MatchType;
 import at.jku.busem.diff.results.Result;
 import at.jku.busem.diff.rules.Rule;
 import at.jku.busem.main.objects.Node;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Markus
  *         Date: 15.06.13
  */
 public class NodeDeletedRule extends Rule {
     private static final String RULE_NAME = "NodeDeletedRule";
 
     @Override
     public void execute(DataSource dataSource, DiffEngine engine) {
         for (Node n : dataSource.getModel1().getNode()) {
             List<Node> nSuc = n.getSuccessor();
             List<Node> sucOfSuc = new ArrayList<>();
             for (Node middle : nSuc) {
                 sucOfSuc.addAll(middle.getSuccessor());
             }
             List<Match> matches = dataSource.getMatches().getMatchesForNode(n);
             if (matches.size() == 1 && matches.get(0).getType() == MatchType.NO_MATCH) {
                 results.add(new Result(RULE_NAME, n.getId(), "node deletion could not be checked because no matching node was found", "standard rule", true));
                 continue;
             } else {
                 for (Match m : matches) {
                     boolean check = true;
                     for (Node n1 : sucOfSuc) {
                         boolean found = false;
                         for (Node n2 : dataSource.getNodeById(m.getId2(), Models.TWO).getSuccessor()) {
                             if (n1.getName().equals(n2.getName())) {
                                 found = true;
                                 break;
                             }
                         }
                         check = found;
                         if (!check) {
                             break;
                         }
                     }
                     if (!check) {
                         if (m.getType() == MatchType.DIRECT_MATCH) {
                            results.add(new Result(RULE_NAME, n.getId(), "a successor of node" + n.getId() + " has been deleted", "standard rule"));
                         }
                         if (m.getType() == MatchType.POSSIBLE_MATCH) {
                            results.add(new Result(RULE_NAME, n.getId(), "a successor of node" + n.getId() + " has been deleted", "standard rule", true,m.getProbability()));
                         }
                     }
                 }
             }
         }
 
     }
 
 
     @Override
     public String getRuleName() {
         return RULE_NAME;
     }
 }
