package idv.fhm.demo.runner;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import idv.fhm.demo.util.Constants;
import idv.fhm.demo.util.DatabaseUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StartupRunner implements CommandLineRunner {


    public StartupRunner() {
    }
	private boolean isDatabaseInitialized(Connection conn) {
		try (ResultSet rs = conn.getMetaData().getTables(null, null, "ACCOUNT", null)) {
			
			return rs.next(); // 如果表存在，表示已初始化
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
    @Override
    public void run(String... args) throws Exception {
    	System.out.println(Constants.CONFIG_DB_BASE+Constants.CONFIG_DB_NAME);

        // 1️⃣ 建立資料夾
        File folder = new File("/tmp/initFolder");
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            System.out.println(created ? "✅ 資料夾建立成功: " + folder.getAbsolutePath() : "⚠️ 資料夾建立失敗");
        } else {
            System.out.println("📁 資料夾已存在: " + folder.getAbsolutePath());
        }
        try(Connection con=DatabaseUtil.getConnection("derby", "", "", Constants.CONFIG_DB_BASE+Constants.CONFIG_DB_NAME, null, null )){
        	if(!isDatabaseInitialized(con)) {
        		System.out.println("create table");
        		DatabaseUtil.executeSQLUpdate(con, Constants.INIT_ACCOUNT_TABLE, null, null);
        		DatabaseUtil.executeSQLUpdate(con, Constants.INIT_ACCOUNTHISTORY_TABLE, null, null);
        	}
        }catch (SQLException e) {
        	System.err.println("DB Error cause:"+e.getMessage());
		}

        System.out.println("🎉 初始化完成！");
    }
}