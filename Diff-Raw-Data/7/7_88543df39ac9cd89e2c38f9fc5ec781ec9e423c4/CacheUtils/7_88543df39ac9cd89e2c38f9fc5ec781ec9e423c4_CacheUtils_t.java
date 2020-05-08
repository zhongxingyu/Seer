 /*
  * Icegem, Extensions library for VMWare vFabric GemFire
  * 
  * Copyright (c) 2010-2011, Grid Dynamics Consulting Services Inc. or third-party  
  * contributors as indicated by the @author tags or express copyright attribution
  * statements applied by the authors.  
  * 
  * This copyrighted material is made available to anyone wishing to use, modify,
  * copy, or redistribute it subject to the terms and conditions of the GNU
  * Lesser General Public License v3, as published by the Free Software Foundation.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * You should have received a copy of the GNU Lesser General Public License v3
  * along with this distribution; if not, write to:
  * Free Software Foundation, Inc.
  * 51 Franklin Street, Fifth Floor
  * Boston, MA  02110-1301  USA
  */
 package com.googlecode.icegem.utils;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gemstone.gemfire.cache.Region;
 import com.gemstone.gemfire.cache.execute.Function;
 import com.gemstone.gemfire.cache.execute.FunctionService;
 import com.gemstone.gemfire.cache.execute.ResultCollector;
 import com.googlecode.icegem.utils.function.ClearRegionFunction;
 import com.googlecode.icegem.utils.function.RemoveAllFunction;
 
 /**
  * Help class for common operations with regions.
  * 
  * @author Andrey Stepanov aka standy
  */
 public class CacheUtils {
 	/** */
 	private static Logger log = LoggerFactory.getLogger(CacheUtils.class);
 
 	/** */
 	private static long BACKOFF_BASE = 10L;
 
 	/**
 	 * Limits query results.
 	 * 
 	 * @param queryString
 	 *            Query string.
 	 * @return Query string with injected "LIMIT" clause.
 	 */
 	public static String addQueryLimit(String queryString, int queryLimit) {
 		int limitIndex = queryString.lastIndexOf("limit");
 
 		if (limitIndex == -1) {
 			limitIndex = queryString.lastIndexOf("LIMIT");
 		}
 
 		if (limitIndex == -1) {
 			return queryString + " LIMIT " + (queryLimit + 1);
 		}
 
 		int limitNumber = Integer.parseInt(queryString
 				.substring(limitIndex + 5).trim());
 
 		return (limitNumber > queryLimit) ? queryString
 				.substring(0, limitIndex) + " LIMIT " + (queryLimit + 1)
 				: queryString;
 	}
 
 	/**
 	 * Returns first locator host and port from locators string.
 	 * 
 	 * @param locatorsString
 	 *            of type String
 	 * @return String[0] - locator host String[1] - locator port
 	 */
 	public static String[] getFirstLocatorFromLocatorsString(
 			String locatorsString) {
 		if (locatorsString == null || locatorsString.length() == 0) {
 			return new String[2];
 		}
 
 		String[] firstLocator = new String[2];
 
 		firstLocator[0] = locatorsString.substring(0,
 				locatorsString.indexOf('[')).trim();
 
 		locatorsString = locatorsString
 				.substring(locatorsString.indexOf('[') + 1);
 
 		firstLocator[1] = locatorsString.substring(0,
 				locatorsString.indexOf(']'));
 
 		return firstLocator;
 	}
 
 	/**
 	 * Clears all types of regions. This method can clean both types of regions
 	 * (REPLICATED, PARTITIONED). It can be used both on client and server side.
 	 * 
 	 * Note: if this method is used from client configured as CACHING_PROXY:
 	 * clearing of PARTITIONED regions: - if client's data should be cleared
 	 * together with server's data, client interest should be registered on
 	 * entries changes; clearing of REPLICATED regions: - if client's data
 	 * should be cleared together with server's data, this method should be
 	 * invoked from the client together with region.localClear() method.
 	 * 
 	 * @param region
 	 *            partitioned region
 	 */
 	public static void clearRegion(Region<?, ?> region) {
 		ClearRegionFunction cleaner = new ClearRegionFunction();
 
 		FunctionService.registerFunction(cleaner);
 
 		ResultCollector rc = FunctionService.onRegion(region)
 				.withArgs(region.getName()).execute(cleaner);
 
 		rc.getResult();
 	}
 
 	/**
 	 * Returns approximate number of entries in the region. As the function
 	 * counts number of entries on different nodes in parallel, per node 
 	 * values may be captured on different moments. So the value may never 
 	 * be valid.
 	 * 
 	 * @param region
 	 *            Region.
 	 * @return Size of the given region.
 	 */
 	public static int getRegionSize(Region<?, ?> region) {
 		Function function = new RegionSizeFunction();
 
 		FunctionService.registerFunction(function);
 
 		ResultCollector rc = FunctionService.onRegion(region)
 				.withCollector(new RegionSizeResultCollector())
 				.execute(function);
 
 		return (Integer) rc.getResult();
 	}
 
 	/**
 	 * Removes several entries from region in a single hop. On partitioned
 	 * region execution is done simultaneously on all partitions.
 	 * 
 	 * @param <K> key type.
 	 * @param region the region to remove entries.
 	 * @param keys the keys of entries to remove.
 	 */
 	public static <K>  void removeAll(Region<K, ?> region, Set<K> keys) {
		if(keys == null) {
			throw new NullPointerException();
		}
		if(keys.isEmpty()) {
			// Nothing to do
			return;
		}
 		Function function = new RemoveAllFunction();
 
 		FunctionService.registerFunction(function);
 
 		ResultCollector rc = FunctionService.onRegion(region)
 				.withFilter(keys)
 				.withArgs(region.getName())
 				.execute(function);
 		rc.getResult();
 	}
 
 	
 	/**
 	 * Retries passed operation with random exponential back off delay.
 	 * 
 	 * @param <T>
 	 *            Type of returned value.
 	 * @param runnable
 	 *            the operation.
 	 * @param maxRetries
 	 *            the maximum number of retries.
 	 * @return the value returned by operation
 	 * @throws OperationRetryFailedException
 	 * @throws InterruptedException
 	 */
 	public static <T> T retryWithExponentialBackoff(Retryable<T> runnable,
 			int maxRetries) throws InterruptedException,
 			OperationRetryFailedException {
 		int retry = 0;
 
 		while (retry < maxRetries) {
 			retry++;
 
 			try {
 				return runnable.execute();
 			} catch (OperationRequireRetryException e) {
 				// No-op.
 			} catch (InterruptedException e) {
 				throw e;
 			}
 
 			if (retry > 1) {
 				long delay = (long) ((BACKOFF_BASE << retry) * Math.random());
 
 				log.debug("Operation requested retry. Sleep for {} millis",
 						delay);
 
 				Thread.sleep(delay);
 			}
 		}
 
 		throw new OperationRetryFailedException(
 				"Maximum number of operation retries reached");
 	}
 }
