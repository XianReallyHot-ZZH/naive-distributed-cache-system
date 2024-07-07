package cn.youyou.naivedistributedcachesystem.core;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 集群discovery
 * 集群节点的发现+加入+维护
 */
public class Discovery {

    /**
     * 集群拓扑
     * 这里面简化了一手，都是本地的节点，用本地上不同的端口代表不同的节点
     * key: 节点id, value: 节点的端口
     */
    private final Map<UUID, Integer> topology = new LinkedHashMap<>();

    /**
     * etcd客户端
     */
    private static Client client;
    // etcd key前缀
    private static final String PREFIX = "nodes/";
    // etcd key前缀ByteSequence数据类型
    private static final ByteSequence PREFIX_BS = bs(PREFIX);

    private static ByteSequence bs(Object o) {
        return ByteSequence.from(o.toString(), StandardCharsets.UTF_8);
    }

    /**
     * 获取集群拓扑,其实就是获取集群中的所有节点信息
     *
     * @return
     */
    public List<Node> getTopology() {
        synchronized (topology) {
            return topology.entrySet().stream()
                    .map(entry -> new Node(entry.getKey(), new InetSocketAddress("127.0.0.1", entry.getValue())))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 节点加入
     * 将应用对应的节点id，本地端口所代表的节点加入到集群拓扑中，并且在适当的时机执行onChange回调（集群节点发生变化时执行）
     *
     * @param nodeId
     * @param localPort
     * @param onChange
     */
    public void join(UUID nodeId, Integer localPort, Runnable onChange) throws Exception {
        client = Client.builder().endpoints("http://127.0.0.1:2379").build();

        // 从注册中心获取当前集群的拓扑，节点信息
        synchronized (topology) {
            client.getKVClient().get(PREFIX_BS, GetOption.newBuilder().withPrefix(PREFIX_BS).build()).get().getKvs().forEach(this::add);
        }

        // 创建监听器，监听注册中心etcd，监听到有节点变化时执行onChange回调
        client.getWatchClient().watch(PREFIX_BS, WatchOption.newBuilder().withPrefix(PREFIX_BS).build(), watchResponse -> {
            synchronized (topology) {
                // 响应获取到的所有事件
                watchResponse.getEvents().forEach(event -> {
                    // 如果是put事件，则将key-value数据加入到topology中
                    if (event.getEventType() == WatchEvent.EventType.PUT) {
                        add(event.getKeyValue());
                    }
                    // TODO（待完善）：后续增加其他事件类型，比如删除事件的处理
                });

                // server端才需要进行集群拓扑变化后的回调
                if (onChange != null) {
                    onChange.run();
                }

                printTopology();
            }
        });

        // 将自己注册至注册中心etcd中
        if (nodeId != null && localPort != null) {
            // 是server端了，那么进行注册
            client.getKVClient().put(PREFIX_BS.concat(bs(nodeId)), bs(localPort)).get();
        } else {
            // client端就不用进行注册了
            printTopology();
        }


    }

    /**
     * 打印topology
     */
    private void printTopology() {
        System.out.println("============================================================");
        System.out.println("Topology:");

        for (Map.Entry<UUID, Integer> entry : topology.entrySet()) {
            System.out.println("   " + entry.getKey() + " -> 127.0.0.1:" + entry.getValue());
        }
        System.out.println("============================================================");
    }

    /**
     * 解析etcd的key-value数据，将其加入到topology中
     *
     * @param kv
     */
    private void add(KeyValue kv) {
        UUID uuid = UUID.fromString(kv.getKey().toString(StandardCharsets.UTF_8).substring(PREFIX.length()));
        int port = Integer.parseInt(kv.getValue().toString(StandardCharsets.UTF_8));
        topology.put(uuid, port);
    }


    /**
     * 集群中的节点对象
     */
    public static class Node {
        // 节点id
        private final UUID id;

        // 节点地址: ip+port
        private final InetSocketAddress address;

        public Node(UUID id, InetSocketAddress address) {
            this.id = id;
            this.address = address;
        }

        public UUID getId() {
            return id;
        }

        public InetSocketAddress getAddress() {
            return address;
        }
    }

}
