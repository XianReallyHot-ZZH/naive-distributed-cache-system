package cn.youyou.naivedistributedcachesystem.core;

import cn.youyou.naivedistributedcachesystem.request.Request;

import java.net.InetSocketAddress;

/**
 * 集群通讯模块
 * 用于在集群间，发送各种request请求
 */
public class Communication {

    /**
     * 向指定的节点发送Request
     *
     * @param request
     * @param address
     * @return
     * @param <R>
     */
    public <R> R execute(Request<R> request, InetSocketAddress address) {

    }


}
