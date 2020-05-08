 package org.kilar.hybridIS.abstractions;
 
import java.util.List;

 /**
  * @author hkyten
  * 
  */
 enum ModuleType {
 	Production, Fuzzy, Neural, Hybrid
 }
 
 public abstract class Module implements CertaintyCalculator {
 	protected int inputLength, outputLength;
 	protected ModuleType type;
 };
