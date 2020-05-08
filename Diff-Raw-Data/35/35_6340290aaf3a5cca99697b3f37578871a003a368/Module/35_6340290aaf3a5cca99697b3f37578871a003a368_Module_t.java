 package org.kilar.hybridIS.abstractions;
 
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
