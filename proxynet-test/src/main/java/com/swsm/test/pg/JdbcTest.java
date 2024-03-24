package com.swsm.test.pg;

import com.alibaba.fastjson.JSON;
import com.swsm.test.RowToModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author liujie
 * @date 2023-04-13
 */
public class JdbcTest {
    
    

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:7788/postgres?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&stringtype=unspecified", "postgres", "admin123");

        List<User> query = query(connection);
        System.out.println("当前线程" + Thread.currentThread().getName() + "开始...");
        for (User user : query) {
            System.out.println("    user:" + JSON.toJSONString(user));
        }
        
    }

    public static List<User> query(Connection connection) {
        try {
            PreparedStatement preparedStatement2 = connection.prepareStatement(
                    "select id as id, name as name,age as age" +
                            " from public.user");
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            RowToModel<User> rowToModel2 = new RowToModel<>();
            List<User> userList = rowToModel2.toBean(resultSet2, User.class);
            resultSet2.close();
            preparedStatement2.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    
    


}
