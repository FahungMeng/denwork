package idv.fhm.demo.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;



public class AccessDataObject {
	
	public Object executeQuery(String sql,Map<String,Object> vars) {
		
		try(Connection con=DatabaseUtil.getConnection("derby", "", "", Constants.CONFIG_DB_BASE+Constants.CONFIG_DB_NAME, null, null )){
			return DatabaseUtil.executeSQLQuery(con, DatabaseUtil.toPreparedSql(sql), DatabaseUtil.extractParams(sql), vars);
		} catch (ClassNotFoundException e) {
			System.err.println("Can't found this class cause:"+e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Query error cause:"+e.getMessage());
			
		}
		return null;
		
	}
	public boolean executeUpdate(String sql,List<Map> vars) {
		try(Connection con=DatabaseUtil.getConnection("derby", "", "", Constants.CONFIG_DB_BASE+Constants.CONFIG_DB_NAME, null, null )){
			return DatabaseUtil.executeSQLUpdate(con, DatabaseUtil.toPreparedSql(sql), DatabaseUtil.extractParams(sql), vars);
		} catch (ClassNotFoundException e) {
			System.err.println("Can't found this class cause:"+e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Query error cause:"+e.getMessage());
			
		}
		return false;
		
	}
	
	

}
