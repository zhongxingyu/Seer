 /**
  *    Copyright 2013 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.optimizer;
 
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.junit.Ignore;
 import org.junit.Test;
 import ru.histone.HistoneException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class SafeASTEvaluationTest extends AbstractOptimizersTest {
 
 //    @Ignore("TODO")
     @Test
     public void expr_int() throws IOException, HistoneException {
         String input = "A{{1+2}}{{x+3}}{{3}}";
        ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[[\"A\"],[\"3\"],[9,[105,[\"x\"]],[\"3\"]],[\"3\"]]]");
         ObjectNode context = getNodeFactory().jsonObject();
         ArrayNode initialAST = getHistone().parseTemplateToAST(input);
         ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context,OptimizationTypes.SAFE_CODE_EVALUATION);
 
         assertEquals(expectedAST.toString(), optimizedAST.toString());
     }
 
     @Test
     public void expr_if() throws IOException, HistoneException {
         String input = "{{var b = false}}{{if a}}AAA{{elseif b}}BBB{{/if}}";
         ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[[1001,\"b\",[17]],[1000,[[[16],[\"AAA\"]],[[17],[\"BBB\"]]]]]");
         ObjectNode context = getNodeFactory().jsonObject();
         context.put("a", true);
         ArrayNode initialAST = getHistone().parseTemplateToAST(input);
         ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context, OptimizationTypes.CONSTANTS_SUBSTITUTION);
 
         assertEquals(expectedAST.toString(), optimizedAST.toString());
     }
 
 }
