 /**
  * Copyright (C) 2013 VCNC, inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package kr.co.vcnc.haeinsa;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import javax.annotation.Nullable;
 
 import kr.co.vcnc.haeinsa.exception.DanglingRowLockException;
 import kr.co.vcnc.haeinsa.thrift.generated.TRowKey;
 import kr.co.vcnc.haeinsa.thrift.generated.TRowLock;
 import kr.co.vcnc.haeinsa.thrift.generated.TRowLockState;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 
 /**
  * Manager class of {@link HaeinsaTransaction}.
  * This class contains {@link HaeinsaTablePool} inside to provide tablePool when user want to access
  * HBase through {@link HaeinsaTransaction} with {@link HaeinsaTable} and execute transaction.
  * <p>
  * HaeinsaTransactionManager also provides method to recover failed transaction from TRowLock in HBase
  * which can be used to clear it up or complete it.
  */
 public class HaeinsaTransactionManager {
 	private final HaeinsaTablePool tablePool;
 
 	/**
 	 * Constructor for TransactionManager
 	 *
 	 * @param tablePool HaeinsaTablePool to access HBase.
 	 */
 	public HaeinsaTransactionManager(HaeinsaTablePool tablePool) {
 		this.tablePool = tablePool;
 	}
 
 	/**
 	 * Get {@link HaeinsaTransaction} instance which can be used to start new
 	 * transaction.
 	 * <p>
 	 * This method is thread-safe.
 	 *
 	 * @return new Transaction instance have reference to this manager instance.
 	 */
 	public HaeinsaTransaction begin() {
 		return new HaeinsaTransaction(this);
 	}
 
 	/**
 	 * Make new {@link HaeinsaTransaction} instance which can be used to recover
 	 * other failed/uncompleted transaction. Also read and recover primaryRowKey and primaryRowLock
 	 * from failed transaction on HBase.
 	 * <p>
 	 * This method is thread-safe.
 	 *
 	 * @param tableName TableName of Transaction to recover.
 	 * @param row Row of Transaction to recover.
 	 * @return Transaction instance if there is any ongoing Transaction on row,
 	 *         return null otherwise.
 	 * @throws IOException
 	 */
 	@Nullable
 	protected HaeinsaTransaction getTransaction(byte[] tableName, byte[] row) throws IOException {
 		TRowLock unstableRowLock = getUnstableRowLock(tableName, row);
 
 		if (unstableRowLock == null) {
 			// There is no on-going transaction on row.
 			return null;
 		}
 
 		TRowLock primaryRowLock = null;
 		TRowKey primaryRowKey = null;
 		if (!unstableRowLock.isSetPrimary()) {
 			// this row is primary row, because primary field is not set.
 			primaryRowKey = new TRowKey(ByteBuffer.wrap(tableName), ByteBuffer.wrap(row));
 			primaryRowLock = unstableRowLock;
 		} else {
 			primaryRowKey = unstableRowLock.getPrimary();
 			primaryRowLock = getUnstableRowLock(primaryRowKey.getTableName(), primaryRowKey.getRow());
 		}
 		if (primaryRowLock == null) {
 			checkDanglingRowLock(tableName, row, unstableRowLock);
 			return null;
 		}
 		return getTransactionFromPrimary(primaryRowKey, primaryRowLock);
 	}
 
 	/**
 	 * Get {@link TRowLock} from given row.
 	 *
 	 * @param tableName Table name of the row
 	 * @param row Row key of the row
 	 * @return RowLock of given row from HBase
 	 * @throws IOException When error occurs in HBase.
 	 */
 	private TRowLock getRowLock(byte[] tableName, byte[] row) throws IOException {
 		TRowLock rowLock = null;
 		try (HaeinsaTableIfaceInternal table = tablePool.getTableInternal(tableName)) {
 			// access to HBase
 			rowLock = table.getRowLock(row);
 		}
 		return rowLock;
 	}
 
 	/**
 	 * Get Unstable state of {@link TRowLock} from given row. Returns null if
 	 * {@link TRowLock} is {@link TRowLockState#STABLE}.
 	 *
 	 * @param tableName Table name of the row
 	 * @param row Row key of the row
 	 * @return null if TRowLock is {@link TRowLockState#STABLE}, otherwise
 	 *         return rowLock from HBase.
 	 * @throws IOException When error occurs in HBase.
 	 */
 	private TRowLock getUnstableRowLock(byte[] tableName, byte[] row) throws IOException {
 		TRowLock rowLock = getRowLock(tableName, row);
 		if (rowLock.getState() == TRowLockState.STABLE) {
 			return null;
 		} else {
 			return rowLock;
 		}
 	}
 
 	/**
 	 * Check if given {@link TRowLock} is dangling RowLock. RowLock is in
 	 * dangling if the RowLock is secondary lock and the primary of the RowLock
 	 * doesn't have the RowLock as secondary.
 	 *
 	 * @param tableName TableName of Transaction to check dangling RowLock.
 	 * @param row Row of Transaction to check dangling RowLock.
 	 * @param rowLock RowLock to check if it is dangling
 	 * @throws IOException When error occurs. Especially throw
 	 *         {@link DanglingRowLockException}if given RowLock is dangling.
 	 */
 	private void checkDanglingRowLock(byte[] tableName, byte[] row, TRowLock rowLock) throws IOException {
 		TRowLock previousRowLock = rowLock;
 		TRowLock currentRowLock = getRowLock(tableName, row);
 
 		// It is not a dangling RowLock if RowLock is changed.
 		if (Objects.equal(previousRowLock, currentRowLock)) {
 			if (currentRowLock.isSetPrimary()) {
 				TRowKey primaryRowKey = currentRowLock.getPrimary();
 				TRowLock primaryRowLock = getRowLock(primaryRowKey.getTableName(), primaryRowKey.getRow());
 
				TRowKey rowKey = new TRowKey().setTableName(tableName).setRow(row);
				if (!containsSecondaryRowLock(primaryRowLock, rowKey)) {
					throw new DanglingRowLockException(rowKey, "Primary lock doesn't have rowLock as secondary.");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Check if given rowLock has secondaryRowKey in secondaries.
 	 *
 	 * @param rowLock given RowLock
 	 * @param secondaryRowKey secondaryRowKey to check if given RowLock contains as secondary.
 	 * @return true if given rowLock contains given secondaryRowKey as secondary.
 	 */
 	private boolean containsSecondaryRowLock(TRowLock rowLock, TRowKey secondaryRowKey) {
 		Preconditions.checkNotNull(rowLock);
 		Preconditions.checkNotNull(secondaryRowKey);
 
 		// Check if secondaries of rowLock contains secondaryRowKey as element.
 		if (rowLock.isSetSecondaries()) {
 			for (TRowKey rowKey : rowLock.getSecondaries()) {
 				boolean match = rowKey != null
 						&& rowKey.isSetTableName()
 						&& secondaryRowKey.isSetTableName()
 						&& Arrays.equals(rowKey.getTableName(), secondaryRowKey.getTableName())
 						&& rowKey.isSetRow()
 						&& secondaryRowKey.isSetRow()
 						&& Arrays.equals(rowKey.getRow(), secondaryRowKey.getRow());
 				if (match) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Recover TRowLocks of failed HaeinsaTransaction from primary row on HBase.
 	 * Transaction information about secondary rows are recovered with {@link #addSecondaryRowLock()}.
 	 * HaeinsaTransaction made by this method do not assign proper values on mutations variable.
 	 *
 	 * @param rowKey
 	 * @param primaryRowLock
 	 * @return
 	 * @throws IOException
 	 */
 	private HaeinsaTransaction getTransactionFromPrimary(TRowKey rowKey, TRowLock primaryRowLock) throws IOException {
 		HaeinsaTransaction transaction = new HaeinsaTransaction(this);
 		transaction.setPrimary(rowKey);
 		transaction.setCommitTimestamp(primaryRowLock.getCommitTimestamp());
 		HaeinsaTableTransaction primaryTableTxState = transaction.createOrGetTableState(rowKey.getTableName());
 		HaeinsaRowTransaction primaryRowTxState = primaryTableTxState.createOrGetRowState(rowKey.getRow());
 		primaryRowTxState.setCurrent(primaryRowLock);
 		if (primaryRowLock.getSecondariesSize() > 0) {
 			for (TRowKey secondaryRow : primaryRowLock.getSecondaries()) {
 				addSecondaryRowLock(transaction, secondaryRow);
 			}
 		}
 		return transaction;
 	}
 
 	/**
 	 * Recover TRowLock of secondary row inferred from {@link TRowLock#secondaries} field of primary row lock.
 	 * <p>
 	 * If target secondary row is in stable state, the row does not included in recovered HaeinsaTransaction
 	 * because it suggest that this secondary row is already stabled by previous failed transaction.
 	 * <p>
 	 * Secondary row is not included in recovered transaction neither when commitTimestamp is different with primary row's,
 	 * because it implicates that the row is locked by other transaction.
 	 * <p>
 	 * As similar to {@link #getTransactionFromPrimary()}, rowTransaction added by this method do not have
 	 * proper mutations variable.
 	 *
 	 * @param transaction
 	 * @param rowKey
 	 * @throws IOException
 	 */
 	private void addSecondaryRowLock(HaeinsaTransaction transaction, TRowKey rowKey) throws IOException {
 		TRowLock unstableRowLock = getUnstableRowLock(rowKey.getTableName(), rowKey.getRow());
 		if (unstableRowLock == null) {
 			return;
 		}
 		// commitTimestamp가 다르면, 다른 Transaction 이므로 추가하면 안됨
 		if (unstableRowLock.getCommitTimestamp() != transaction.getCommitTimestamp()) {
 			return;
 		}
 		HaeinsaTableTransaction tableState = transaction.createOrGetTableState(rowKey.getTableName());
 		HaeinsaRowTransaction rowState = tableState.createOrGetRowState(rowKey.getRow());
 		rowState.setCurrent(unstableRowLock);
 	}
 
 	/**
 	 * @return HaeinsaTablePool contained in TransactionManager
 	 */
 	public HaeinsaTablePool getTablePool() {
 		return tablePool;
 	}
 }
