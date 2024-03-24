package com.swsm.test.mysql;

import com.alibaba.fastjson.JSON;
import com.swsm.test.RowToModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liujie
 * @date 2023-04-13
 */
public class JdbcCurTest {
    
    public static ExecutorService executorService = Executors.newFixedThreadPool(3);
    

    public static void main(String[] args) throws Exception {
        List<Connection> connectionList = new ArrayList<>();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:7788/jeecg-boot?serverTimezone=Asia/Shanghai", "root", "admin123");
        Connection connection2 = DriverManager.getConnection("jdbc:mysql://localhost:7788/jeecg-boot?serverTimezone=Asia/Shanghai", "root", "admin123");
        Connection connection3 = DriverManager.getConnection("jdbc:mysql://localhost:7788/jeecg-boot?serverTimezone=Asia/Shanghai", "root", "admin123");
        connectionList.add(connection);
        connectionList.add(connection2);
        connectionList.add(connection3);

        CountDownLatch countDownLatch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            Connection conn = connectionList.get(i);
            executorService.submit(new Worker(conn, countDownLatch));
        }
        countDownLatch.await();
        System.out.println("所有任务都完成了...");
        executorService.shutdown();
        
    }
    
    public static class Worker implements Runnable{
        
        private Connection connection;
        private CountDownLatch countDownLatch;
        
        public Worker(Connection connection, CountDownLatch countDownLatch) {
            this.connection = connection;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(new Random().nextInt(3) * 1000);
                List<SysRole> query = query(connection);
                System.out.println("当前线程" + Thread.currentThread().getName() + "开始...");
                for (SysRole sysRole : query) {
                    System.out.println("  当前线程" + Thread.currentThread().getName() + " sysRole:" + JSON.toJSONString(sysRole));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("当前线程" + Thread.currentThread().getName() + "结束.countDownLatch=" + countDownLatch.getCount());
                countDownLatch.countDown();
            }
            
        }

        public List<SysRole> query(Connection connection) {
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


}
