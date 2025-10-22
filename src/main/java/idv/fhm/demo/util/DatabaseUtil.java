package idv.fhm.demo.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {

	public static Connection getConnection(String type, String host, String port, String dbName, String user,
			String password) throws SQLException, ClassNotFoundException {
		String url;
		String driver;

		switch (type.toLowerCase()) {
		case "oracle":
			driver = "oracle.jdbc.OracleDriver";
			url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
			break;
		case "db2":
			driver = "com.ibm.db2.jcc.DB2Driver";
			url = "jdbc:db2://" + host + ":" + port + "/" + dbName;
			break;
		case "mysql":
			driver = "com.mysql.cj.jdbc.Driver";
			url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
			break;
		case "sqlserver":
		case "sql server":
			driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName;
			break;

		case "postgresql": // 容錯拼寫
			driver = "org.postgresql.Driver";
			url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
			break;
		case "derby":
			
			driver = "org.apache.derby.jdbc.EmbeddedDriver";
			// Derby 不使用 host/port，所以我們忽略它們
			url = "jdbc:derby:" + dbName + ";create=true";
			break;
		default:
			throw new IllegalArgumentException("不支援的資料庫類型: " + type);
		}

		Class.forName(driver);

		if ("derby".equalsIgnoreCase(type)) {
			return DriverManager.getConnection(url);
		} else {
			return DriverManager.getConnection(url, user, password);
		}
	}

	public static boolean executeSQLUpdate(Connection con, String SQL, List<String> vars, List<Map> values) {
		boolean result = false;
		if (values != null) {
			try (PreparedStatement ps = con.prepareStatement(SQL);) {
				
				for (Iterator iterator = values.iterator(); iterator.hasNext();) {
					Map map = (Map) iterator.next();
					if (vars != null && vars.size() > 0) {
						for (int i = 0; i < vars.size(); i++) {
							ps.setObject(i + 1, map.get(vars.get(i)));
						}
					}
					ps.addBatch();
				}
				int[] data = ps.executeBatch();
				int count = 0;
				for (int i : data) {
					count += i;
				}
				return count > 0;
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.print("error occur:" + e.getMessage());
				return false;

			}

		} else {
			try (PreparedStatement ps = con.prepareStatement(SQL);) {
				int count=ps.executeUpdate();
				return count>0;
			} catch (SQLException e) {
				
				System.err.print("error occur:" + e.getMessage());
				return false;

			}
		}
	

	}
    /**
     * 判斷 SQL 是否為「更新操作」(INSERT, UPDATE, DELETE, MERGE)
     * @param sql 傳入的 SQL 字串
     * @return true = 更新語句；false = 不是（例如 SELECT）
     */
    public static boolean isUpdateSql(String sql) {
        if (sql == null) return false;

        String normalized = sql.trim().toUpperCase();

 
        if (normalized.startsWith("INSERT") ||
            normalized.startsWith("UPDATE") ||
            normalized.startsWith("DELETE") ||
            normalized.startsWith("MERGE")) {
            return true;
        }
        return false;
    }
    public static Object executeSQL(Connection con, String sql, List<String> vars,Object condition) {
    	
    	if(isUpdateSql(sql)) {
    		if(condition instanceof List) {
    			return executeSQLUpdate(con, sql, vars, (List)condition);
    		}else {
    			List<Map> multi=Arrays.asList(new Map[]{(Map<String, Object>)condition});
    			return executeSQLUpdate(con, sql, vars, multi);
    		}
    		
    	}else {
    		return executeSQLQuery(con, sql, vars, (Map<String, Object>) condition);
    	}
    
    }
    /** 將單引號外的 ${var} 全部替換為 ?，其餘不動。 */
	/**
	 * 
	 * @param sql
	 * @return
	 */
    
	public static String toPreparedSql(String sql) {
		StringBuilder out = new StringBuilder();
		boolean inSingleQuote = false;
		int i = 0, n = sql.length();

		while (i < n) {
			char c = sql.charAt(i);

			if (c == '\'') {
				out.append(c);
				// 處理 '' (字面上的單引號)
				if (i + 1 < n && sql.charAt(i + 1) == '\'') {
					out.append('\'');
					i += 2;
				} else {
					inSingleQuote = !inSingleQuote;
					i++;
				}
				continue;
			}

			if (!inSingleQuote && c == '$' && i + 1 < n && sql.charAt(i + 1) == '{') {
				int end = sql.indexOf('}', i + 2);
				if (end != -1) {
					out.append('?'); // 以 ? 取代 ${...}
					i = end + 1;
					continue;
				}
			}

			out.append(c);
			i++;
		}

		return out.toString();
	}
	/**
	 * Executes a SQL query using a prepared statement and returns the result as a
	 * list of maps.
	 * <p>
	 * Each row of the result set is represented as a {@code Map<String, Object>},
	 * where the keys are column names and the values are the corresponding column
	 * values.
	 * </p>
	 *
	 * @param con  the active JDBC {@link Connection} used to prepare and execute
	 *             the query
	 * @param SQL  the SQL query string with parameter placeholders (e.g., "SELECT *
	 *             FROM table WHERE id = ?")
	 * @param vars a list of values to bind to the SQL query's placeholders; can be
	 *             null if no parameters are used
	 * @return a list of rows as maps, or {@code null} if an exception occurred or
	 *         no rows were returned
	 */
	public static List<Map<String, Object>> executeSQLQuery(Connection con, String sql, List<String> vars,Map<String,Object> condition) {
		
		List<Map<String, Object>> result = new ArrayList<>();
		try (PreparedStatement ps = con.prepareStatement(sql);) {
			// 設定參數
			if (vars != null&&condition!=null) {
				for (int i = 0; i < vars.size(); i++) {
					ps.setObject(i + 1, condition.get(vars.get(i)));
				}
			}
			
			// 執行查詢
			try (ResultSet rs = ps.executeQuery()) {
				List<String> colNames = null;
				
				while (rs.next()) {
					if (colNames == null) {
						colNames = getColumns(rs);
					}
					
					result.add(getResultSet(rs, colNames));
					
				}
			}
			

		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
		}
		
		return result;
	}
	public static Map<String,Object> listToMap(List<Map<String,Object>> data,String primaryKey){
		Map<String,Object> result=new HashMap<String, Object>();
		
		for (Map<String, Object> map : data) {
			result.put((String)map.get(primaryKey), map);
		}
		return result;
		
		
	}
	/** 擷取單引號外的 ${var} 名稱（依出現順序）。 */
	public static List<String> extractParams(String sql) {
		List<String> params = new ArrayList<>();
		boolean inSingleQuote = false;
		int i = 0, n = sql.length();

		while (i < n) {
			char c = sql.charAt(i);

			if (c == '\'') {
				if (i + 1 < n && sql.charAt(i + 1) == '\'') {
					i += 2; // 跳過 '' 轉義
				} else {
					inSingleQuote = !inSingleQuote;
					i++;
				}
				continue;
			}

			if (!inSingleQuote && c == '$' && i + 1 < n && sql.charAt(i + 1) == '{') {
				int end = sql.indexOf('}', i + 2);
				if (end != -1) {
					String name = sql.substring(i + 2, end).trim();
					params.add(name);
					i = end + 1;
					continue;
				}
			}

			i++;
		}

		return Collections.unmodifiableList(params);
	}
	/**
	 * Get SQL query result columns name from result set
	 * 
	 * @param rs result set
	 * @return
	 * @throws SQLException
	 */
	private static List<String> getColumns(ResultSet rs) throws SQLException {
		List<String> result = null;
		if (rs != null) {
			result = new ArrayList<String>();
			ResultSetMetaData meta = rs.getMetaData();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				result.add(meta.getColumnName(i));
			}
		}
		return result;

	}

	private static Map<String, Object> getResultSet(ResultSet rs, List<String> columns) throws SQLException {
		Map<String, Object> row = new HashMap<>();
		for (String colName : columns) {
			if(rs.getObject(colName) instanceof InputStream) {
				row.put(colName, "LOB OBJECT");	
			}else {
				row.put(colName, rs.getObject(colName));
			}
			
		}
		return row;

	}
}
