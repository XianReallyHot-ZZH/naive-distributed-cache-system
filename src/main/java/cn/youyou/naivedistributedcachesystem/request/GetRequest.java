package cn.youyou.naivedistributedcachesystem.request;

import cn.youyou.naivedistributedcachesystem.core.Storage;

/**
 * 缓存获取请求
 */
public class GetRequest implements Request<String>{

    // 缓存键
    private final String key;

    public GetRequest(String key) {
        this.key = key;
    }

    /**
     * 根据缓存键获取缓存值
     *
     * @param storage
     * @return
     */
    @Override
    public String handle(Storage storage) {
        return storage.get(key);
    }
}
