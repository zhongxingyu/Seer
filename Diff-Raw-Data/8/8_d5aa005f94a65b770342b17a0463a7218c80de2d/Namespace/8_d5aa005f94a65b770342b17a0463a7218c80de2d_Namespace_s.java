 package de.skuzzle.polly.parsing.declarations;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.TokenType;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.tree.literals.ResolvableIdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.operators.BinaryOperatorOverload;
 import de.skuzzle.polly.parsing.tree.operators.TernaryOperatorOverload;
 import de.skuzzle.polly.parsing.tree.operators.UnaryOperatorOverload;
 import de.skuzzle.polly.parsing.util.CopyTool;
 
 
 public class Namespace {
 
     private Declarations global;
     private Declarations reserved;
     private FunctionDeclaration forbidden;
     private List<UnaryOperatorOverload> unaryOperators;
     private List<BinaryOperatorOverload> binaryOperators;
     private List<TernaryOperatorOverload> ternaryOperators;
     private Map<String, Declarations> namespaces;
     private String rootNS;
     private String currentNS;
 
     
     
     public Namespace(String rootNamespace) {
         this.rootNS = rootNamespace;
         this.currentNS = rootNamespace;
         this.global = new Declarations();
         this.reserved = new Declarations();
         this.unaryOperators = new LinkedList<UnaryOperatorOverload>();
         this.binaryOperators = new LinkedList<BinaryOperatorOverload>();
         this.ternaryOperators = new LinkedList<TernaryOperatorOverload>();
         this.namespaces = new HashMap<String, Declarations>();
         this.create(rootNamespace);
     }
     
     
     
     public Namespace copyFor(String rootNamespace) {
     	Namespace copy = new Namespace(rootNamespace);
     	copy.global = this.global;
     	copy.reserved = this.reserved;
     	copy.forbidden = this.forbidden;
     	copy.unaryOperators = this.unaryOperators;
     	copy.binaryOperators = this.binaryOperators;
     	copy.ternaryOperators = this.ternaryOperators;
     	copy.namespaces = this.namespaces;
     	return copy;
     }
     
     
     
     public String getRootNS() {
 		return rootNS;
 	}
     
     
     
     public void forbidFunction(FunctionDeclaration function) {
         this.forbidden = function;
     }
     
     
     
     public void allowFunction() {
         this.forbidden = null;
     }
     
     
     
     public void switchTo(String namespace, Position nsPosition) throws ParseException {
         this.checkNamespace(namespace, nsPosition);
         this.currentNS = namespace;
     }
     
     
     
     public void switchToRoot() {
         this.currentNS = this.rootNS;
     }
     
     
     
     public Declarations getNamespaceFor(String name) {
         return this.namespaces.get(name);
     }
     
     
     
     public Declarations checkNamespace(String namespace, Position nsPosition) 
             throws ParseException {
         Declarations ns = this.getNamespaceFor(namespace);
         if (ns == null) {
             throw new ParseException("Unbekannter Namespace: " + namespace, nsPosition);
         }
         return ns;
     }
     
     
     
     public void enter() {
         Declarations declarations = this.namespaces.get(this.currentNS);
         declarations.enter();
     }
     
     
     
     public void leave() {
         Declarations declarations = this.namespaces.get(this.currentNS);
         declarations.leave();
     }
     
     
     
     public void reserve(Declaration declaration) {
         this.reserved.add(declaration);
     }
     
     
     
     public void create(String name) {
         Declarations decl = this.namespaces.get(name);
         if (decl == null) {
             this.namespaces.put(name, new Declarations());
         }
     }
     
     
     
     public void add(Declaration declaration) throws ParseException {
         this.add(declaration, this.currentNS);
     }
     
     
     
     public void add(Declaration declaration, String namespace) throws ParseException {
         if (this.reserved.tryResolve(
                 new ResolvableIdentifierLiteral(declaration.getName())) != null) {
             throw new ParseException("'" + declaration.getName().getIdentifier() + 
                 "' ist ein reservierter Bezeichner", 
                 declaration.getName().getPosition());
         }
         
         
         if (declaration.isGlobal()) {
             this.global.add(declaration);
             return;
         }
         
         Declarations ns = this.getNamespaceFor(namespace);
         if (ns == null) {
             // if namespace not yet exists, create one!
             ns = new Declarations();
             this.namespaces.put(namespace, ns);
         }
         
         ns.add(declaration);
     }
     
     
     
     public void add(UnaryOperatorOverload operator) {
         this.unaryOperators.add(operator);
     }
     
     
     
     public void add(BinaryOperatorOverload operator) {
         this.binaryOperators.add(operator);
     }
     
     
     
     public void add(TernaryOperatorOverload operator) {
         this.ternaryOperators.add(operator);
     }
     
     
     
     public void remove(String name) throws ParseException {
         this.remove(name, this.currentNS);
     }
     
     
     
     public void remove(String name, String namespace) throws ParseException {
         Declarations ns = this.checkNamespace(namespace, Position.EMPTY);
         ns.remove(name);
     }
     
     
     
     public Declaration resolve(ResolvableIdentifierLiteral id) throws ParseException {
         if (this.forbidden != null && 
                 id.getIdentifier().equals(this.forbidden.getName().getIdentifier())) {
             throw new ParseException("Unglter rekursiver Aufruf von " + id, 
                 id.getPosition());
     }
         
         Declarations ns = this.namespaces.get(this.currentNS);
         
         Declaration decl = ns.tryResolve(id);
         if (decl == null) {
             decl = this.global.tryResolve(id);
         }
         
         if (decl == null) {
            throw new ParseException("Unbekannter Bezeichner: '"
                + id.getIdentifier() + "'", id.getPosition());
         }
         return decl;
     }
     
     
     
     public VarDeclaration resolveVar(ResolvableIdentifierLiteral id) 
             throws ParseException {
         
         Declaration decl = this.resolve(id);
         if (!(decl instanceof VarDeclaration)) {
             throw new ParseException("'" + decl.getName()
                 + "' ist keine Variable.", id.getPosition());
         }
         return (VarDeclaration) decl;
     }
 
 
 
     public FunctionDeclaration resolveFunction(ResolvableIdentifierLiteral id) 
     		throws ParseException {
         
         Declaration decl = this.resolve(id);
         if (!(decl instanceof FunctionDeclaration)) {
             throw new ParseException("'" + decl.getName()
                 + "' ist keine Funktion.", id.getPosition());
         }
         return (FunctionDeclaration) decl;
     }
     
     
     
     public TypeDeclaration resolveType(ResolvableIdentifierLiteral id) 
             throws ParseException {
         TypeDeclaration decl = (TypeDeclaration) this.reserved.tryResolve(id);
         if (decl != null) {
             return decl;
         }
         
         throw new ParseException("Unbekannter Typ: " + id.getIdentifier(), 
             id.getPosition());
     }
     
 
     
     public UnaryOperatorOverload resolveOperator(TokenType op, Type operandType, 
             Position position) throws ParseException {
         for (UnaryOperatorOverload overload : this.unaryOperators) {
             UnaryOperatorOverload result = overload.match(op, operandType);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf den Typ " + 
                 operandType, position);
     }
     
     
     
     public BinaryOperatorOverload resolveOperator(TokenType op, Type operandType1,
         Type operandType2, Position position) throws ParseException {
         
         for (BinaryOperatorOverload overload : this.binaryOperators) {
             BinaryOperatorOverload result = overload.match(op, operandType1, operandType2);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf die Typen " + 
                 operandType1 + " und " + operandType2, position);
     }
     
     
     
     public TernaryOperatorOverload resolveOperator(TokenType op, Type operandType1,
         Type operandType2, Type operandType3, Position position) throws ParseException {
         
         for (TernaryOperatorOverload overload : this.ternaryOperators) {
             TernaryOperatorOverload result = overload.match(op, operandType1, 
                     operandType2, operandType3);
             if (result != null) {
                 return CopyTool.copyOf(result);
             }
         }
         throw new ParseException("Operator " + op + " nicht anwendbar auf die Typen " + 
                 operandType1 + ", " + operandType2 + " und " + operandType3, position);
     }
     
     
     
     public void store(File directory) throws IOException {
         if (!directory.isDirectory()) {
             throw new IOException(directory + " is no directory");
         }
         this.global.store(new File(directory, "~global.declaration"));
         for (Entry<String, Declarations> ns : this.namespaces.entrySet()) {
             ns.getValue().store(new File(directory, ns.getKey() + ".declaration"));
         }
     }
     
     
     
     public void restore(File directory) throws IOException {
         if (!directory.isDirectory()) {
             throw new IOException(directory + " is no directory");
         }
         this.global = Declarations.restore(new File(directory, "~global.declaration"));
         
         File[] files = directory.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.getName().toLowerCase().endsWith(".declaration");
             }
         });
         
         for (File file : files) {
             Declarations ns = Declarations.restore(file);
             String name = file.getName().substring(0, 
                     file.getName().length() - ".declaration".length());
             this.namespaces.put(name, ns);
         }
     }
     
     
     
     public String toString() {
         StringBuilder result = new StringBuilder();
         result.append("RESERVED\n");
         result.append(this.reserved.toString());
         result.append("GLOABL\n");
         result.append(this.global.toString());
         result.append("NAMESPACES\n");
         for (Entry<String, Declarations> ns : this.namespaces.entrySet()) {
             result.append(ns.getKey());
             result.append(":\n");
             result.append(ns.getValue());
         }
         return result.toString();
     }
     
     
     
     
 }
