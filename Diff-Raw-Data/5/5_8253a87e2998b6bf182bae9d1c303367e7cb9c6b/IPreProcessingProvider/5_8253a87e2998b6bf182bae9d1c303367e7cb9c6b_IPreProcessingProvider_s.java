 package de.thatsich.bachelor.preprocessing.intern.command.provider;
 
 import org.encog.neural.networks.BasicNetwork;
 
import de.thatsich.bachelor.preprocessing.api.entities.IPreProcessing;
 import de.thatsich.bachelor.preprocessing.intern.command.preprocessor.core.PreProcessorConfiguration;
 import de.thatsich.core.guice.ICommandProvider;
 
 public interface IPreProcessingProvider extends ICommandProvider
 {
	IPreProcessing createAANNPreProcessing( BasicNetwork rebuildNetwork, PreProcessorConfiguration config );
 }
 
