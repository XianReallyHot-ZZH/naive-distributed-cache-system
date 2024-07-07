package cn.youyou.naivedistributedcachesystem;

import cn.youyou.naivedistributedcachesystem.core.Communication;
import cn.youyou.naivedistributedcachesystem.core.Discovery;
import cn.youyou.naivedistributedcachesystem.core.Mapper;
import cn.youyou.naivedistributedcachesystem.request.GetRequest;
import cn.youyou.naivedistributedcachesystem.request.PutRequest;

public class Client {

    // 客户端需要有集群感知的能力，但是自己可以不用加入集群
    private Discovery discovery = new Discovery();

    // 客户端和server端都需要通信的能力
    private Communication communication = new Communication();

    // 客户端要能有对数据进行分区计算的能力，这样才能把相应的数据和指令发送到对应的节点上
    private Mapper mapper = new Mapper(discovery);


    public void start() throws Exception {
        // 感知集群，但是不加入集群
        discovery.join(null, null, null);
    }

    // =========================== 客户端操作 ===========================
    private void put(String key, String value) throws Exception {
        // 分布式缓存操作，底层依赖集群感知和通信能力
        communication.execute(new PutRequest(key, value), mapper.node(key).getAddress());
    }

    private String get(String key) throws Exception {
        // 分布式缓存操作，底层依赖集群感知和通信能力
        return communication.execute(new GetRequest(key), mapper.node(key).getAddress());
    }

    // =========================== 客户端操作 ===========================


    /**
     * 模拟一段客户端的操作
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.start();

        // 模拟操作
        String key = "1";
        String value = "Hello Server!";

        for (int i = 0; i < 10; i++) {
            key = Integer.toString(i);
            value = "Record " + i;

            // 缓存写
            client.put(key, value);

            // 缓存读
            System.out.println(">>> [client-test] Read from the server: " + client.get(key));
        }

    }


}
