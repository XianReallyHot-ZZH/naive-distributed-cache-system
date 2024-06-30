package cn.youyou.naivedistributedcachesystem.core;

import cn.youyou.naivedistributedcachesystem.request.RebalanceRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存的实现类
 */
@Slf4j
public class Storage {

    // 本地节点id
    private final UUID localId;

    // 缓存, key -> partitionId, value -> [缓存键值对]
    private final Map<Integer, Map<String, String>> cache = new ConcurrentHashMap<>();

    // 分区映射
    private final Mapper mapper;

    // 集群通讯工具
    private final Communication comm;

    public Storage(UUID localId, Discovery discovery, Communication comm) {
        this.localId = localId;
        this.mapper = new Mapper(discovery);
        this.comm = comm;
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    public String get(String key) {
        log.info("[Storage] get method, key:{}", key);
        // 根据key计算对应的分区id，进而获取对应的分区数据，最后获取对应的value
        return cache.get(mapper.partition(key)).get(key);
    }

    /**
     * 添加缓存
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        log.info("[Storage] put method, key:{}, value:{}", key, value);
        // 根据key计算对应的分区id，进而获取对应的分区数据，最后添加对应的value
        cache.get(mapper.partition(key)).put(key, value);
    }

    /**
     * 分区数据接收,触发分区对应数据的覆盖
     * @param partitionId
     * @param partitionData
     */
    public void onPartitionReceived(int partitionId, Map<String, String> partitionData) {
        cache.put(partitionId, partitionData);
    }

    /**
     * 检测到集群节点发生变化，触发分区数据重映射，进而引发data exchange（数据交换）, 即rebalance
     */
    public void partitionExchange() {
        // 获取当前分区对应的所有节点，分区-节点-映射关系
        Discovery.Node[] mapping = mapper.partitionMapping();

        for (int partition = 0; partition < mapping.length; partition++) {
            Discovery.Node node = mapping[partition];

            // 如果当前节点是分区对应的节点, 那么如果之前该分区id已经分配在该节点上了，那么跳过，否则就new一个空的map
            if (node.getId().equals(localId)) {
                cache.putIfAbsent(partition, new ConcurrentHashMap<>());
            } else {
                // 如果当前节点不是分区对应的节点, 那么如果之前该分区id已经分配在该节点上了，那么就要发出一个rebalance请求,将数据exchange至对应的节点上
                Map<String, String> partitionData = cache.remove(partition);
                if (partitionData != null) {
                    try {
                        comm.execute(new RebalanceRequest(partition, partitionData), node.getAddress());
                    } catch (Exception e) {
                        // 先简单报错，后续再优化
                        log.error("[Storage] rebalance error, data exchange failed, partition:{}, node(remote):{}", partition, node, e);
                    }
                }
            }
        }

        List<Integer> partitions = new ArrayList<>(cache.keySet());
        Collections.sort(partitions);
        log.info("[Storage] partition exchange completed, local partitions:{}", partitions);

    }

}
