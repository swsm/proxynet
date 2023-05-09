package com.swsm.test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RowToModel<T> {
    public List<T> toBean(ResultSet rs, Class<T> type) throws Exception {
        if (rs == null) return new ArrayList<>();
        List<T> resultList = new ArrayList<>();
        while (rs.next()) {
            //获取class中定义的属性
            Field[] fields = type.getDeclaredFields();
            //创建java对象
            T obj = type.newInstance();
            //循环对象属性值
            for (Field f : fields) {
                Object value = rs.getObject(f.getName());
                if (value != null) {
                    //oracle中number类型对应java中的BigDecimal类型。
                    if (value.getClass() == BigDecimal.class && (f.getType() == Integer.class || f.getType() == int.class)) {
                        value = ((BigDecimal) value).intValue();
                    }
                    f.setAccessible(true);
                    f.set(obj, value);
                }
            }
            resultList.add(obj);
        }
        return resultList;

    }
}
