 package org.dawb.common.gpu;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 public interface IOperation {
 
 	/**
 	 * Processes the operator on the GPU making a Kernel if required and
	 * storing it as local data. Call deactivate on this IOperation to 
 	 * clean up memory being used by the kernel
 	 * 
 	 * @param a
 	 * @param b
 	 * @param operation
 	 * @return
 	 */
 	public AbstractDataset process(AbstractDataset a, double b, Operator operation);
 
 	/**
 	 * Processes the operator on the GPU making a Kernel if required and
	 * storing it as local data. Call deactivate on this IOperation to 
 	 * clean up memory being used by the kernel
 	 * 
 	 * @param a
 	 * @param b
 	 * @param operation
 	 * @return
 	 */
 	public AbstractDataset process(AbstractDataset a, AbstractDataset b, Operator operation);
 	
 	/**
 	 * Disposes any GPU Kernels which the operation may have.
 	 */
 	public void deactivate();
 }
