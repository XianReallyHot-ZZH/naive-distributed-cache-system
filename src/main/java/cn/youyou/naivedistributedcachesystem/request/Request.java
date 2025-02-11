package cn.youyou.naivedistributedcachesystem.request;


import cn.youyou.naivedistributedcachesystem.core.Storage;

import java.io.Serializable;

/**
 * interface 分布式请求指令
 *
 * @param <T>
 */
public interface Request<T> extends Serializable {
    /**
     * 处理请求,完成对缓存的相应操作
     *
     * @param storage
     * @return
     */
    T handle(Storage storage);
}
