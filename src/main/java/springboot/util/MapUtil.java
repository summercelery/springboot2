package springboot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springboot.anno.NotToMap;
import sun.net.util.IPAddressUtil;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * map与对象的转换，将对象放置入jedis中使用
 */
public class MapUtil {

    private static Logger logger = LoggerFactory.getLogger(MapUtil.class);

    public static Map<String, String> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();

        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {


                String key = property.getName();
                // 过滤class属性
                if (!"class".equals(key)) {
                    NotToMap notToMap = null;
                    try {
                        notToMap = obj.getClass().getDeclaredField(key).getAnnotation(NotToMap.class);
                    }catch (NoSuchFieldException e){
                        if(!"id".equals(key)){
                            continue;
                        }
                    }
                    if (null == notToMap) {
                        Method getter = property.getReadMethod();
                        Object value = getter.invoke(obj);
                        if(null != value){
                            if((value instanceof String)){
                                map.put(key, value.toString());
                            }
//                            else if(value instanceof Date){
//
//                                map.put(key,DateUtil.getFormatString((Date) value));
//
//                            }
                            else{
                                map.put(key,JsonUtil.objectToJson(value));

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("transBean2Map Error {}", e);
        }
        return map;

    }

    public static <T> T mapToObject(Map<String, String> map, Class<T> beanClass) throws Exception {
        if (map == null)
            return null;

        Object obj = beanClass.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {

            Method setter = property.getWriteMethod();
            if (setter != null) {
                String o = map.get(property.getName());
                if("java.lang.String".equals(property.getPropertyType().getTypeName())||"java.io.Serializable".equals(property.getPropertyType().getTypeName())){
                    setter.invoke(obj, o);
                }
//                else if ("java.util.Date".equals(property.getPropertyType().getTypeName())){
//                    if(null != o){
//                        setter.invoke(obj,DateUtil.getDateByString(o));
//                    }
//                }
                else{
                    if(null != o){
                        setter.invoke(obj,JsonUtil.jsonToObject(o,property.getPropertyType()));
                    }
                }
            }
        }

        return (T)obj;
    }
}
