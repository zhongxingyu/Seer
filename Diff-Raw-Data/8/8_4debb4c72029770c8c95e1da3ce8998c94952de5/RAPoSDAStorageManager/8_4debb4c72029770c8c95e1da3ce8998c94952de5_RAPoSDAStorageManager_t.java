 package sim.storage.manager;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import sim.Block;
 import sim.Request;
 import sim.Response;
 import sim.storage.CacheResponse;
 import sim.storage.DiskResponse;
 import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
 import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
 import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
 import sim.storage.manager.ddm.RAPoSDADataDiskManager;
 import sim.storage.util.DiskInfo;
 import sim.storage.util.DiskState;
 import sim.storage.util.ReplicaLevel;
 
 public class RAPoSDAStorageManager {
 
 	/**
 	 * TODO
 	 * 繝��繧ｿ繝�ぅ繧ｹ繧ｯ縺ｯ�後◎繧後◇繧梧球蠖薙☆繧毅lockID縺ｮ遽�峇繧呈戟縺｣縺ｦ縺�ｋ縺薙→��
 	 * 縺昴≧縺励↑縺�→�梧眠縺励＞譖ｸ縺崎ｾｼ縺ｿ縺ｯ蟶ｸ縺ｫ繝ｩ繧ｦ繝ｳ繝峨Ο繝薙Φ縺ｧ繝�ぅ繧ｹ繧ｯ縺ｫ蜑ｲ繧雁ｽ薙※繧峨ｌ��
 	 * 繧ｭ繝｣繝�す繝･繝｡繝｢繝ｪ縺ｮ繧ｭ繝･繝ｼ縺ｮ蠅励∴譁ｹ縺ｯ��蜷�Γ繝｢繝ｪ縺ｧ蟶ｸ縺ｫ蜷御ｸ�→縺ｪ縺｣縺ｦ縺励∪縺�ｼ�
 	 */
 
 	private RAPoSDACacheMemoryManager cmm;
 	private RAPoSDACacheDiskManager cdm;
 	private RAPoSDADataDiskManager ddm;
 
 	protected final int blockSize;
 	protected final int numReplica;
 
 	private BigInteger blockNumber = new BigInteger("0");
 
 	/**
 	 * A map between client request and corresponding blocks.
 	 * key; request key
 	 * value: corresponding blocks
 	 */
 	protected HashMap<Long, Block[]> requestMap;
 
 	public RAPoSDAStorageManager(
 			RAPoSDACacheMemoryManager cmm,
 			RAPoSDACacheDiskManager cdm,
 			RAPoSDADataDiskManager ddm,
 			int blockSize,
 			int numReplica) {
 
 		this.cmm = cmm;
 		this.cdm = cdm;
 		this.ddm = ddm;
 		this.blockSize = blockSize;
 		this.numReplica = numReplica;
 
 		this.requestMap = new HashMap<Long, Block[]>();
 	}
 
 	public Response read(Request request) {
 		Block[] blocks = requestMap.get(request.getKey());
 		if (blocks == null) throw new IllegalArgumentException();
 
 		updateArrivalTimeOfBlocks(blocks, request.getArrvalTime());
 
 		double respTime = Double.MIN_VALUE;
 
 		for (Block b : blocks) {
 			// retrieve from cache memory
 			CacheResponse cmResp = cmm.read(b);
 			if (!Block.NULL.equals(cmResp.getResult())) {
 				respTime =
 					respTime < cmResp.getResponseTime()
 					? cmResp.getResponseTime() : respTime;
 			} else {
 				// retrieve from cache disk
 				CacheResponse cdResp = cdm.read(b);
 				if (!Block.NULL.equals(cdResp.getResult())) {
 					respTime =
 						respTime < cdResp.getResponseTime()
 						? cdResp.getResponseTime() : respTime;
 				} else {
 					// read from data disk.
 					DiskResponse ddResp = readFromDataDisk(b);
 					assert ddResp.getResults().length == 1;
 					respTime =
 						respTime < ddResp.getResponseTime()
 						? ddResp.getResponseTime() : respTime;
 
 					// write to cache disk asynchronously
 					// after read from data disk.
 					writeToCacheDisk(
 							ddResp.getResults(),
 							ddResp.getResponseTime() + b.getAccessTime());
 				}
 			}
 		}
 		return new Response(request.getKey(), respTime);
 	}
 
 	private DiskResponse readFromDataDisk(Block block) {
 
 		List<DiskInfo> relatedDiskStates = ddm.getRelatedDisksInfo(block);
 		List<DiskInfo> activeDiskStates = extractActiveDisks(relatedDiskStates);
 
 		// case 1. one of n disks is spinning.
 		if (activeDiskStates.size() == 1)
 			return actualRead(block, activeDiskStates.get(0));
 
 		// case 2. some of n disks are spinning.
 		if (activeDiskStates.size() > 1
 				&& activeDiskStates.size() <= relatedDiskStates.size()) {
 			DiskInfo diskState = cmm.getMaxBufferDisk(activeDiskStates);
 			return actualRead(block, diskState);
 		}
 
 		// case 3. all of n disks are stopping.
 		assert activeDiskStates.size() == 0;
 		DiskInfo diskState =
 				ddm.getLongestStandbyDiskInfo(
 						activeDiskStates,
 						block.getAccessTime());
 		return actualRead(block, diskState);
 	}
 
 	private DiskResponse actualRead(Block block, DiskInfo diskInfo) {
 		int ownerDiskId = assignOwnerDiskId(
 				block.getPrimaryDiskId(), diskInfo.getRepLevel());
 		block.setOwnerDiskId(ownerDiskId);
 		block.setRepLevel(diskInfo.getRepLevel());
 		return ddm.read(new Block[]{block});
 	}
 
 	private List<DiskInfo> extractActiveDisks(List<DiskInfo> diskInfos) {
 		List<DiskInfo> result = new ArrayList<DiskInfo>();
 		for (DiskInfo dInfo : diskInfos) {
 			if (DiskState.ACTIVE.equals(dInfo.getDiskState())
 					|| DiskState.IDLE.equals(dInfo.getDiskState()))
 				result.add(dInfo);
 		}
 		return result;
 	}
 
 	public Response write(Request request) {
 		Block[] blocks = requestMap.get(request.getKey());
 		if (blocks == null) {
 			// new request
 			blocks = divideRequest(request);
 			requestMap.put(request.getKey(), blocks);
 		} else {
 			// update(override) request
 			updateArrivalTimeOfBlocks(blocks, request.getArrvalTime());
 		}
 
 		double respTime = Double.MIN_VALUE;
 
 		for (Block block : blocks) {
 			Block[] replicas = createReplicas(block);
 			for (Block b : replicas) {
 				RAPoSDACacheWriteResponse response = cmm.write(b);
 				if (response.getOverflows().length == 0) {
 					respTime =
 						response.getResponseTime() > respTime
 						? response.getResponseTime() : respTime;
 				} else { // cache overflow
 					double arrivalTime =
 						request.getArrvalTime() + response.getResponseTime();
 
 					// write to data disk
 					DiskResponse ddResp =
 							writeToDataDisk(response, arrivalTime);
 
 					// write to cache disk asynchronously
 					// after data disk write.
 					writeToCacheDisk(
 							ddResp.getResults(),
 							ddResp.getResponseTime() + arrivalTime);
 
 					// delete blocks on the cache already written in disks.
 					double cmDeletedTime = deleteBlocksOnCache(
 							ddResp.getResults(),
 							ddResp.getResponseTime() + arrivalTime);
 
 					// return least response time;
 					double tempResp =
 						ddResp.getResponseTime() + cmDeletedTime;
 
 					respTime = respTime < tempResp ? tempResp : respTime;
 				}
 			}
 		}
 		return new Response(request.getKey(), respTime);
 	}
 
 	private double deleteBlocksOnCache(Block[] blocks, double arrivalTime) {
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		double response = Double.MIN_VALUE;
 		for (Block block : blocks) {
 			CacheResponse cmResp = cmm.remove(block);
 			response =
 				response < cmResp.getResponseTime()
 				? cmResp.getResponseTime() : response;
 		}
 		return response;
 	}
 
 	private DiskResponse writeToDataDisk(
 			RAPoSDACacheWriteResponse cmResp, double arrivalTime) {
 		int maxBufferDiskId = cmResp.getMaxBufferDiskId();
 		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime))
 			ddm.spinUp(maxBufferDiskId, arrivalTime);
 		Block[] blocks = cmResp.getOverflows();
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		return ddm.write(blocks);
 	}
 
 	private DiskResponse writeToCacheDisk(Block[] blocks, double arrivalTime) {
 		updateArrivalTimeOfBlocks(blocks, arrivalTime);
 		return cdm.write(blocks);
 	}
 
 	private Block[] createReplicas(Block block) {
 		Block[] replicas = new Block[numReplica];
 		block.setRepLevel(ReplicaLevel.ZERO);
 		block.setOwnerDiskId(block.getPrimaryDiskId());
 		replicas[0] = block;
 		int repCounter = numReplica;
 		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
 			repCounter--;
 			if (repLevel.equals(ReplicaLevel.ZERO)) continue;
 			if (repCounter < 0) break;
 			Block replica = new Block(
 					nextBlockId(),
 					block.getAccessTime(),
 					block.getPrimaryDiskId());
 			replica.setRepLevel(repLevel);
 			replica.setOwnerDiskId(
 					assignOwnerDiskId(
 							replica.getPrimaryDiskId(),
 							replica.getRepLevel()));
 			replicas[repLevel.getValue()] = replica;
 		}
 		return replicas;
 	}
 
 	private Block[] divideRequest(Request request) {
 		Block[] blocks = null;
 		int numBlocks = (int)Math.ceil(request.getSize() / blockSize);
 		assert numBlocks > 0;
 		blocks = new Block[numBlocks];
 		for (int i=0; i < numBlocks; i++) {
 			BigInteger blockId = nextBlockId();
 			blocks[i] = new Block(
 					blockId,
 					request.getArrvalTime(),
 					assignPrimaryDiskId(blockId));
 		}
 		return blocks;
 	}
 
 	private int assignPrimaryDiskId(BigInteger blockId) {
 		BigInteger numDataDisk =
 			new BigInteger(String.valueOf(ddm.getNumberOfDataDisks()));
 		// TODO try to separate assignor class.
 		// There is not only roundrobin favor assign algorithm.
 		return (blockId.mod(numDataDisk)).intValue();
 	}
 
 	private int assignOwnerDiskId(int primaryDiskId, ReplicaLevel repLevel) {
		return (primaryDiskId + repLevel.getValue()) % ddm.getNumberOfDataDisks();
 	}
 
 	protected BigInteger nextBlockId() {
 		BigInteger next = new BigInteger(blockNumber.toString());
		blockNumber = blockNumber.add(BigInteger.ONE);
 		return next;
 	}
 
 	private void updateArrivalTimeOfBlocks(Block[] blocks, double arrivalTime) {
 		for (Block block : blocks) {
 			block.setAccessTime(arrivalTime);
 		}
 	}
 
 	public void register(long requestId, int size) {
 		// TODO refactoring. integrate with divideRequest method
 		Block[] blocks = null;
 		int numBlocks = (int)Math.ceil(size / blockSize);
 		assert numBlocks > 0;
 		blocks = new Block[numBlocks];
 		for (int i=0; i < numBlocks; i++) {
 			BigInteger blockId = nextBlockId();
 			blocks[i] = new Block(
 					blockId,
 					0.0,
 					assignPrimaryDiskId(blockId));
 		}
 		requestMap.put(requestId, blocks);
 	}

 	public void close(double closeTime) {
 		cdm.close(closeTime);
 		ddm.close(closeTime);
 	}
 
 }
