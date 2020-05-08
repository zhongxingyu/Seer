 /*
  * The MIT License
  *
  * Copyright 2013 Marcin Zarebski <zarebski.m[AT]gmail.com>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package calculator;
 
 import calculator.evaluator.Evaluator;
 import calculator.evaluator.rpn.RPNEvaluator;
 import calculator.exception.execute.ExpressionExecuteException;
 import calculator.exception.parse.FunctionParseException;
 import calculator.function.FunctionRepository;
 import calculator.function.rpn.RPNFunctionRepository;
 import calculator.function.rpn.custom.CustomConstant;
 import calculator.function.rpn.custom.CustomFunction;
 import calculator.function.rpn.custom.FunctionExecutor;
 import calculator.parser.FunctionParser;
 import calculator.parser.SimpleFunctionParser;
 import com.google.common.annotations.VisibleForTesting;
 
 public class Calculator {
     private Evaluator evaluator;
 
    private Evaluator singleEvaluator;

     private FunctionRepository functionRepository;
 
     private FunctionParser functionParser;
 
     public Calculator() {
         functionRepository = new RPNFunctionRepository();
         evaluator = new RPNEvaluator(functionRepository);
        singleEvaluator = new RPNEvaluator(functionRepository);
        functionParser = new SimpleFunctionParser(singleEvaluator);
     }
 
     private double actualResult = 0.0;
 
     public void execute(final String command) throws ExpressionExecuteException {
         try {
             actualResult = evaluator.execute(command);
         } catch (ExpressionExecuteException ex) {
             actualResult = Double.NaN;
             throw ex;
         }
     }
 
     public double getResult() {
         return actualResult;
     }
 
     public void addFunction(final String name, final String functionBody) throws FunctionParseException {
         final FunctionExecutor executor = functionParser.parse(functionBody);
         functionRepository.add(name, new CustomFunction(executor));
     }
 
     public void addConstant(final String name, final String expression) throws FunctionParseException {
         final FunctionExecutor executor = functionParser.parse(expression);
         functionRepository.add(name, new CustomConstant(executor));
     }
 
     @VisibleForTesting
     void setEvaluator(final Evaluator evaluator) {
         this.evaluator = evaluator;
     }
 
     @VisibleForTesting
     void setFunctionRepository(final FunctionRepository repository) {
         this.functionRepository = repository;
     }
 
     @VisibleForTesting
     void setFunctionParser(final FunctionParser parser) {
         this.functionParser = parser;
     }
 }
