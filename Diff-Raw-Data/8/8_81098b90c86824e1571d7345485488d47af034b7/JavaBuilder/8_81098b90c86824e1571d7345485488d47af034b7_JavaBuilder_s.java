 package org.oobium.build.esp.parser.internal.parsers;
 
 import static org.oobium.build.esp.parser.internal.parsers.Scanner.EOL;
 
 import org.oobium.build.esp.dom.EspPart;
 import org.oobium.build.esp.dom.EspPart.Type;
 import org.oobium.build.esp.dom.elements.Constructor;
 import org.oobium.build.esp.dom.elements.ImportElement;
 import org.oobium.build.esp.dom.elements.JavaElement;
 import org.oobium.build.esp.parser.exceptions.EspEndException;
 
 public class JavaBuilder extends Builder {
 
 	public JavaBuilder(Scanner scanner) {
 		super(scanner);
 	}
 
 	public Constructor parseConstructorElement() throws EspEndException {
 		return push(new Constructor(), new BuildRunner<Constructor>() {
 			public void parse(Constructor element) throws EspEndException {
 				scanner.setContainmentToEOL();
 				EspPart name = scanner.push(Type.JavaKeyword);
 				scanner.find('(');
 				scanner.pop(name);
 				parseMethodSigArgs(element);
 			}
 		});
 	}
 	
 	public ImportElement parseImportElement() throws EspEndException {
 		return push(new ImportElement(), new BuildRunner<ImportElement>() {
 			public void parse(ImportElement element) throws EspEndException {
 				scanner.setContainmentToEOL();
 				EspPart keyword = scanner.push(Type.JavaKeyword);
 				scanner.findEndOfWord();
 				scanner.pop(keyword);
 				scanner.forward();
 				if(scanner.isCharSequence('s', 't', 'a', 't', 'i', 'c', ' ')) {
 					element.setStatic(true);
 					keyword = scanner.push(Type.JavaKeyword);
 					scanner.move(7);
 					scanner.pop(keyword);
 					scanner.forward();
 				}
 				element.setImportPart(scanner.push(Type.JavaContainer));
 				scanner.findAny(';', EOL);
 				scanner.pop(element.getImportPart());
 			}
 		});
 	}
 
 	public JavaElement parseJavaElement() throws EspEndException {
 		return push(new JavaElement(), new BuildRunner<JavaElement>() {
 			public void parse(JavaElement element) throws EspEndException {
 				scanner.setContainmentToEOE();
 				scanner.move(1);
 				element.setSource(scanner.push(Type.JavaContainer));
 				scanner.setContainmentToEOL();
 				try {
 					scanner.findEndOfContainment();
 				} catch(EspEndException e) {
					scanner.pop(element.getSource());
 				}
 				scanner.parseChildren();
 			}
 		});
 	}
 	
 }
