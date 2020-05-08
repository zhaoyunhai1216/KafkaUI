package application.element;

import com.alibaba.fastjson.JSONObject;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author qdyk
 * @title: Column
 * @projectName KafkaUI
 * @description: TODO
 * @date 2020/3/2116:24
 */
public class Row extends HashMap<String, SimpleObjectProperty> {

    /**
     * 构造方法
     *
     * @param fileds
     */
    public Row(String id, String... fileds) {
        super();
        init(id, fileds);
    }

    /**
     * 构造方法
     */
    public Row() {
        super();
    }

    /**
     * 添加字段信息
     *
     * @param fileds
     */
    void init(String id, String... fileds) {
        putObject("id", id);
        Arrays.stream(fileds).forEach(x -> {
            putObject(x, "<空值>");
        });
    }

    /**
     * 是否为主页
     *
     * @return
     */
    public boolean isHome() {
        return get("id").getValue().toString().equals("-1");
    }

    /**
     * 获取指定key的字符串信息
     *
     * @return
     */
    public String getString(String key) {
        SimpleObjectProperty property = get(key);
        if(property == null){
            return "";
        }
        Object o = property.getValue();
        if(o == null){
            return "";
        }
        return o.toString();
    }

    /**
     * 获取指定key的字符串信息
     *
     * @return
     */
    public void putObject(String key, Object o) {
        put(key, new SimpleObjectProperty(o));
    }


    /**
     * 查看是否包含某个字段
     *
     * @param filter
     * @return
     */
    public boolean contains(String filter) {
        return entrySet().stream().anyMatch(x->{
            String v = getString(x.getKey());
            return v.contains(filter);
        });
    }

    /**
     * 创建主页
     *
     * @return
     */
    public static Row home() {
        Row row = new Row();
        row.putObject("id", -1);
        row.putObject("name", "HOME");
        return row;
    }


    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        for (String key : this.keySet()) {
            jsonObject.put(key, this.get(key).getValue().toString());
        }
        return jsonObject.toJSONString();
    }


}
