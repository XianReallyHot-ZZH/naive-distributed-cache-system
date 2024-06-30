package cn.youyou.naivedistributedcachesystem.request;

import cn.youyou.naivedistributedcachesystem.core.Storage;

import java.util.Map;

/**
 * 负载均衡请求
 */
public class RebalanceRequest implements Request<Void>{

    /**
     * 分区ID
     */
    private final int partitionId;

    /**
     * 分区数据
     */
    private final Map<String, String> partitionData;

    public RebalanceRequest(int partitionId, Map<String, String> partitionData) {
        this.partitionId = partitionId;
        this.partitionData = partitionData;
    }

    /**
     * 根据分区id和分区数据，将数据写入到storage中
     *
     * @param storage
     * @return
     */
    @Override
    public Void handle(Storage storage) {
        storage.onPartitionReceived(partitionId, partitionData);
        return null;
    }
}
