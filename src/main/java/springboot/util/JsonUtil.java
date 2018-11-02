package springboot.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * json转换工具
 */
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {

        //序列化时只序列化不为空的
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //Json序列化增加类别标识就能准确判断子类类型。
//        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        //如果是空对象的时候,不抛异常,也就是对应的属性没有get方法
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //对应的属性没有set方法
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //去掉默认的时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //设置为中国上海时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
//        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

        //反序列化时，属性不存在的兼容处理
        objectMapper.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //序列化时，日期的统一格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        //单引号处理
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /**
     * 类转json
     * @param o
     * @return
     * @throws JsonProcessingException
     */
    public static String objectToJson(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

    /**
     * json转类
     * @param stringObject
     * @param c
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T jsonToObject(String stringObject, Class<T> c) throws IOException {

        return objectMapper.readValue(stringObject,c);
    }

    /**
     * 返回object实例
     * @return
     */
    public static ObjectMapper getInstance(){
        return objectMapper;
    }
}
