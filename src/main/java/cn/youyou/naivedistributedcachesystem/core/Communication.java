package cn.youyou.naivedistributedcachesystem.core;

import cn.youyou.naivedistributedcachesystem.request.Request;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 集群通讯模块
 * 用于在集群间，发送各种request请求
 */
public class Communication {

    private ServerSocket serverSocket;

    // 线程池
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    /**
     * start: 完成启动，绑定端口
     * server端需要用到
     *
     * @return
     */
    public int start() {
        int port = 4000;
        // 绑定端口，不断往后重试，直至找到可用的port
        while (true) {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(">>> server process started on: " + port);
                break;
            } catch (Exception e) {
                System.err.println(">>> server start error, port:" + port + " has used.");
                port++;
            }
        }

        return port;
    }

    /**
     * 向指定的节点发送Request
     * server端和client都需要用到
     *
     * @param request
     * @param address
     * @param <R>
     * @return
     */
    public <R> R execute(Request<R> request, InetSocketAddress address) throws Exception {
        Socket socket = new Socket(address.getAddress(), address.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(request);

        return (R) in.readObject();
    }

    /**
     * 监听请求，在对应的storage对象上做响应处理
     * server端需要用到
     *
     * @param storage
     * @throws Exception
     */
    public void onRequest(Storage storage) throws Exception {
        while (true) {
            Socket socket = serverSocket.accept();

            // 完成接收请求，解析request对象，并调用对应的处理方法
            threadPool.execute(() -> {
                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    Request<?> request = (Request<?>) in.readObject();
                    Object result = request.handle(storage);
                    out.writeObject(result);
                } catch (Exception e) {
                    System.err.println(">>> Communication listen socket error. ");
                    e.printStackTrace();
                }
            });
        }
    }


}
