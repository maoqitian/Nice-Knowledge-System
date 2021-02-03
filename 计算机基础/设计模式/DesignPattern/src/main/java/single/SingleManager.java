package single;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 使用容器实现单例
 * @Author: maoqitian
 * @CreateDate: 2021/2/2 23:05
 */
public class SingleManager {

    private static final Map<String,Object> objectMap = new HashMap<>();
    private SingleManager(){}

    public static void registerService(String key,Object instance){

        if (key.length()>0 && instance != null) {
            if (!objectMap.containsKey(key)) {
                objectMap.put(key, instance);
            }
        }

    }

    public static Object getService(String key){
        return objectMap.get(key);
    }
}
