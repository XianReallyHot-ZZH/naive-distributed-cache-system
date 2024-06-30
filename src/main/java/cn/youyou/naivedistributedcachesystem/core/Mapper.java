package cn.youyou.naivedistributedcachesystem.core;

import com.google.common.hash.Hashing;

import java.util.List;

/**
 * 分区映射计算器
 * 1、分区和key的映射关系
 * 2、分区和集群节点的映射关系
 */
public class Mapper {

    // 分区数量
    private static final int PARTITIONS = 10;

    // 集群发现discovery
    private Discovery discovery;


    public Mapper(Discovery discovery) {
        this.discovery = discovery;
    }


    /**
     * 根据key计算分区
     * @param key
     * @return
     */
    public int partition(String key) {
        // 根据key的hash值计算分区
        return Math.abs(key.hashCode()) % PARTITIONS;
    }

    /**
     * 在最新的集群topology下，当前分区对应的集群节点，数组索引值为分区id，对应的数组元素为节点信息
     * @return
     */
    public Discovery.Node[] partitionMapping() {
        Discovery.Node[] mapping = new Discovery.Node[PARTITIONS];
        for (int partition = 0; partition < PARTITIONS; partition++) {
            // 获取分区对应的集群节点
            mapping[partition] = node(partition);
        }
        return mapping;
    }

    /**
     * 根据key计算对应的集群节点
     * @param key
     * @return
     */
    public Discovery.Node node(String key) {
        return node(partition(key));
    }

    /**
     * 根据分区id计算对应的集群节点
     * @param partition
     * @return
     */
    public Discovery.Node node(int partition) {
        List<Discovery.Node> nodes = discovery.getTopology();

        // 找出当前分区对应的唯一节点，只要保证该计算方式的计算结果是唯一的即可，那么在所有的节点上计算的结果就能都是一致的
        long maxHash = Long.MIN_VALUE;
        Discovery.Node maxHashNode = null;
        // 比较节点id和分区id的组合hash值，找出最大的hash值对应的节点就是该分区对应的节点
        for (Discovery.Node node : nodes) {
            long hash = Math.abs(
                    Hashing.murmur3_128()
                            .newHasher()
                            .putInt(partition)
                            .putLong(node.getId().getLeastSignificantBits())
                            .putLong(node.getId().getMostSignificantBits())
                            .hash()
                            .asLong());

            if (hash > maxHash) {
                maxHash = hash;
                maxHashNode = node;
            }
        }

        return maxHashNode;
    }
}
