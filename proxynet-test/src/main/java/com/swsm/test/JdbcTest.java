package com.swsm.test;

import com.alibaba.fastjson.JSON;
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
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:7788/jeecg-boot?serverTimezone=Asia/Shanghai", "root", "admin123");

        List<SysRole> query = query(connection);
        System.out.println("当前线程" + Thread.currentThread().getName() + "开始...");
        for (SysRole sysRole : query) {
            System.out.println("    sysRole:" + JSON.toJSONString(sysRole));
        }
        
    }

    public static List<SysRole> query(Connection connection) {
        try {
            PreparedStatement preparedStatement2 = connection.prepareStatement(
                    "select id as 'id', role_name as 'roleName',role_code as 'roleCode', description as 'description'," +
                            "create_by as 'createBy', create_time as 'createTime', update_by as 'updateBy', update_time as 'updateTime' " +
                            " from sys_role");
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            RowToModel<SysRole> rowToModel2 = new RowToModel<>();
            List<SysRole> sysRoles2 = rowToModel2.toBean(resultSet2, SysRole.class);
            resultSet2.close();
            preparedStatement2.close();
            return sysRoles2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    
    


}
