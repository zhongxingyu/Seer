 /*
 	Copyright: Marcus Cobden (2011)
 	This file is part of AllocationCurve.
 
 	AllocationCurve is free software: you can redistribute it and/or modify
 	it under the terms of version 3 of the GNU Lesser General Public License
 	as published by the Free Software Foundation.
 
 	AllocationCurve is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public License
 	along with AllocationCurve. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.marcuscobden.allocationcurve.exception;
 
 import java.net.InetAddress;
 
 import uk.co.marcuscobden.allocationcurve.AllocationRecord;
 import uk.co.marcuscobden.allocationcurve.allocation.InetNetworkAllocationBlock;
 
 public abstract class AllocationDeclarationException extends Exception
 {
 	public static class InetNetworkBlockDeclarationException extends
 			RuntimeException
 	{
 
 		public InetNetworkBlockDeclarationException(final String message)
 		{
 			super(message);
 		}
 
 	}
 
 	public static class NonEncompassedBlockException extends
 			AllocationDeclarationException
 	{
 		private InetNetworkAllocationBlock<InetAddress> block;
 
 		public NonEncompassedBlockException(
 				final AllocationRecord allocationRecord,
 				final InetNetworkAllocationBlock<InetAddress> block)
 		{
			super(allocationRecord);
 			this.block = block;
 		}
 	}
 
 	public static class IPVersionMismatch extends
 			AllocationDeclarationException
 	{
 		public IPVersionMismatch(final AllocationRecord allocationRecord,
 				final Class<? extends InetAddress> expected,
 				final InetAddress mismatch)
 		{
			super(allocationRecord);
 		}
 	}
 
 	public static class NoBlocksDeclaredException extends
 			AllocationDeclarationException
 	{
 		public NoBlocksDeclaredException(final AllocationRecord allocationRecord)
 		{
			super(allocationRecord);
 		}
 
 	}
 
 	protected AllocationRecord allocationRecord;
 
 	public AllocationDeclarationException(
			final AllocationRecord allocationRecord)
 	{
 		this.allocationRecord = allocationRecord;
 	}
 }
