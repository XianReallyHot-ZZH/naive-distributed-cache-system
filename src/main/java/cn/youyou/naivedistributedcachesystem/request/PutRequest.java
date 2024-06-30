package cn.youyou.naivedistributedcachesystem.request;

import cn.youyou.naivedistributedcachesystem.core.Storage;

/**
 * 缓存添加请求
 */
public class PutRequest implements Request<Void>{

    // 缓存键
    private final String key;
    // 缓存值
    private final String value;

    public PutRequest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 将缓存键值对添加到缓存中
     *
     * @param storage
     * @return
     */
    @Override
    public Void handle(Storage storage) {
        storage.put(key, value);
        return null;
    }
}
