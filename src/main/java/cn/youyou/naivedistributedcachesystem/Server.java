package cn.youyou.naivedistributedcachesystem;

import cn.youyou.naivedistributedcachesystem.core.Communication;
import cn.youyou.naivedistributedcachesystem.core.Discovery;
import cn.youyou.naivedistributedcachesystem.core.Storage;

import java.util.UUID;

public class Server {

    private UUID localId = UUID.randomUUID();

    private Discovery discovery = new Discovery();

    private Communication communication = new Communication();

    private Storage storage = new Storage(localId, discovery, communication);

    public void start() throws Exception {
        // server端启动通信绑定
        int port = communication.start();
        // 加入集群
        discovery.join(localId, port, () -> storage.partitionExchange());
        // server端监听请求
        communication.onRequest(storage);
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }

}
