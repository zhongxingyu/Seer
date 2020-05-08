 package org.zwobble.shed.parser.parsing;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class RuleValues {
     private final Map<Rule<?>, Object> values = new HashMap<Rule<?>, Object>();
     
     public <T> void add(Rule<T> rule, Object value) {
         values.put(rule, value);
     }
     
    @SuppressWarnings("unchecked")
     public <T> T get(Rule<T> rule) {
         return (T) values.get(rule);
     }
 }
