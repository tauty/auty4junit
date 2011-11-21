package tetz42.test;

import static org.junit.Assert.*;
import static tetz42.clione.SQLManager.*;

import java.io.ObjectInputStream.GetField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import tetz42.clione.SQLManager;
import tetz42.clione.exception.SQLRuntimeException;
import tetz42.clione.exception.WrapException;
import tetz42.clione.util.ResultMap;
import tetz42.exception.TableNotFoundException;

public class AutyDB {

	private static final String BK_PREFIX = "ZUTY_";
	private static final String TESTCASE_FIELD = "ZUTY_TESTCASE_NAME";

	public static final String CRLF = System.getProperty("line.separator");

	private static final ConcurrentHashMap<String, String> restoreSQLMap = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<String, String> assertSQLMap = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<String, Object> backUpMap = new ConcurrentHashMap<String, Object>();
	private static Connection con;
	private static String backUpName;

	public static synchronized void useConnection(Proc proc) throws Exception {
		if (con == null) {
			try {
				con = DriverManager.getConnection(DBConfig.DB_CONNECT_STRING,
						DBConfig.DB_CONNECT_USER, DBConfig.DB_CONNECT_PASS);
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLRuntimeException(e);
			} catch (ExceptionInInitializerError e) {
				throw new WrapException(e.getCause());
			}
		}
		if (backUpName == null) {
			backUpName = "bk"
					+ new SimpleDateFormat("yyyyMMdd-HHmmss.SSS")
							.format(new Date());
		}
		proc.run(con);
	}

	public static void prepareDB(Class<?> clazz, String testCaseName,
			String... tableNames) throws SQLException {
		for (String tableName : tableNames) {
			String bkTableName = convBkTable(tableName);
			createIf(tableName, bkTableName);
			backUpIf(tableName, bkTableName);
			restore(tableName, bkTableName, testCaseName + ".prepare#"
					+ clazz.getName());
		}
	}

	public static void deleteDB(Class<?> clazz, String testCaseName,
			String... tableNames) throws SQLException {
		for (String tableName : tableNames) {
			String bkName = convBkTable(tableName);
			createIf(tableName, bkName);
			backUpIf(tableName, bkName);
			sqlManager(con).useSQL("DELETE FROM " + tableName).update();
		}
	}

	public static void assertDB(Class<?> clazz, String testCaseName,
			String... tableNames) throws SQLException {
		for (String tableName : tableNames) {
			String bkTableName = convBkTable(tableName);
			createIf(tableName, bkTableName);
			assertDB(tableName, bkTableName, testCaseName + ".expected#"
					+ clazz.getName());
		}
	}

	private static void assertDB(String tableName, String bkTableName,
			String testCaseName) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		int count = sqlManager.useSQL(
				"SELECT COUNT(*) FROM " + bkTableName + " WHERE "
						+ TESTCASE_FIELD + " = /* @testCase */").find(
				Integer.class, params("testCase", testCaseName));
		if (count == 0) {
			// TODO this implementation doesn't allow the result record is 0.
			count = sqlManager.useSQL(getRestoreSQL(tableName, bkTableName))
					.update(
							params("tcName", testCaseName).$("FROM_TABLE",
									tableName));
			fail("No expected data found. Tried to insert some datas as expected data."
					+ CRLF
					+ "Please check it by the SQL below:"
					+ CRLF
					+ "SELECT * FROM "
					+ bkTableName
					+ " WHERE "
					+ TESTCASE_FIELD
					+ " = '"
					+ testCaseName
					+ "'"
					+ CRLF
					+ "If you don't like it, delete it by the SQL below:"
					+ CRLF
					+ "DELETE FROM "
					+ bkTableName
					+ " WHERE "
					+ TESTCASE_FIELD + " = '" + testCaseName + "'");
		}
		String assertSQL = getAssertSQL(tableName, bkTableName);
		List<ResultMap> list = sqlManager.useSQL(assertSQL).findAll(
				params("testCaseName", testCaseName));
		if (list.size() == 0)
			return;
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (ResultMap map : list) {
			for (Entry<String, Object> entry : map.entrySet()) {
				if (entry.getKey().startsWith("A_"))
					sb.append("PK:").append(entry.getKey().substring(2))
							.append("=").append(entry.getValue()).append("\t");
				if (entry.getKey().startsWith("E_")) {
					Object anotherValue = map.get("O_"
							+ entry.getKey().substring(2));
					if (isEquals(entry.getValue(), anotherValue)) {
						continue;
					}
					sb.append("Not Equals[")
							.append(entry.getKey().substring(2)).append(
									"] expected:").append(entry.getValue())
							.append(", but was:").append(anotherValue).append(
									"\t");
				}
			}
			sb.append(CRLF);
			if (i++ > 10) {
				sb.append(CRLF).append("Too much unmatch data!").append(CRLF);
				break;
			}
		}
		fail("Unmatch!" + CRLF + "expected data's key:" + testCaseName + CRLF
				+ sb.toString() + CRLF + assertSQL + CRLF + CRLF
				+ "testCaseName:" + testCaseName);
	}

	public static void assertZero(Class<?> clazz, String testCaseName,
			String... tableNames) throws SQLException {
		for (String tableName : tableNames) {
			String bkTableName = convBkTable(tableName);
			assertZero(tableName, bkTableName, testCaseName + ".expected#"
					+ clazz.getName());
		}
	}

	private static void assertZero(String tableName, String bkTableName,
			String testCaseName) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		int count = sqlManager.useSQL(
				"SELECT COUNT(*) FROM " + bkTableName + " WHERE "
						+ TESTCASE_FIELD + " = /* @testCase */").find(
				Integer.class, params("testCase", testCaseName));
		if (count != 0) {
			fail("The record of " + tableName + " must be zero.");
		}
	}

	private static boolean isEquals(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return true;
		else if (o1 == null || o2 == null)
			return false;
		else
			return o1.equals(o2);
	}

	public static synchronized void restoreAll() throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		List<String> list = sqlManager
				.useFile(AutyDB.class, "SelectTables.sql")
				.findAll(String.class);
		for (String tableName : list) {

			if (!backUpMap.containsKey(tableName))
				continue; // ignore the table has not backup.
			String bkTableName = convBkTable(tableName);
			restore(tableName, bkTableName, backUpName, false);
			sqlManager.useSQL(
					"DELETE FROM " + bkTableName + " WHERE " + TESTCASE_FIELD
							+ " = /* @bkName */").update(
					params("bkName", backUpName));
		}
		backUpMap.clear();
	}

	private static void restore(String tableName, String bkTableName,
			String testCaseName) throws SQLException {
		restore(tableName, bkTableName, testCaseName, true);
	}

	private static void restore(String tableName, String bkTableName,
			String testCaseName, boolean isZeroFail) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		sqlManager.useSQL("DELETE FROM " + tableName).update();
		String sql = getRestoreSQL(tableName, bkTableName);
		int count = sqlManager.useSQL(sql).update(
				params("TABLE", tableName).$("bkName", testCaseName));
		if (count == 0 && isZeroFail) {
			count = sqlManager.useSQL(sql).update(
					params("tcName", testCaseName).$("bkName", backUpName));
			fail("No test case data found. Tried to insert some datas as test case data."
					+ CRLF
					+ "Please check it by the SQL below:"
					+ CRLF
					+ "SELECT * FROM "
					+ bkTableName
					+ " WHERE "
					+ TESTCASE_FIELD
					+ " = '"
					+ testCaseName
					+ "'"
					+ CRLF
					+ "If you don't like it, delete it by the SQL below:"
					+ CRLF
					+ "DELETE FROM "
					+ bkTableName
					+ " WHERE "
					+ TESTCASE_FIELD + " = '" + testCaseName + "'");
		}
		System.out.println("" + count + " records has been inserted to "
				+ tableName);
	}

	private static String getRestoreSQL(String tableName, String bkTableName)
			throws SQLException {
		if (!restoreSQLMap.containsKey(tableName)) {
			SQLManager sqlManager = sqlManager(con);
			List<ResultMap> list = sqlManager.useFile(AutyDB.class,
					"SelectFields.sql").findAll(params("TABLE", tableName));
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO /*%if TABLE %STR(TABLE) */").append(
			// bkTableName).append(CRLF);
					bkTableName).append("(").append(CRLF);
			sb.append("\tZUTY_TESTCASE_NAME /* &tcName */,").append(CRLF);
			String fields = getFieldsString(list);
			sb.append("\t").append(fields).append(CRLF);
//			for (ResultMap map : list) {
//				sb.append("\t,").append(map.get("COLUMN_NAME")).append(CRLF);
//			}
			sb.append(")").append(CRLF);
			sb.append("SELECT").append(CRLF).append("\t").append(
					"/* $tcName */'tetz42.test.AutyDB#test',").append(CRLF);
			sb.append("\t").append(fields).append(CRLF);
			sb.append("FROM").append(CRLF).append("\t").append(
					"/*%if FROM_TABLE %STR(FROM_TABLE) */").append(bkTableName)
					.append(CRLF);
			sb.append("WHERE").append(CRLF).append("\t").append(TESTCASE_FIELD)
					.append(" = /* $bkName */'bk20110623-120000.000'");
			restoreSQLMap.put(tableName, sb.toString());
		}
		return restoreSQLMap.get(tableName);
	}

	private static String getAssertSQL(String tableName, String bkName)
			throws SQLException {
		if (!assertSQLMap.containsKey(tableName)) {
			SQLManager sqlManager = sqlManager(con);

			// generate pkList and fieldList
			List<String> pkList = sqlManager.useFile(AutyDB.class,
					"SelectPK.sql").findAll(String.class,
					params("TABLE", tableName));
			List<ResultMap> fieldList = new ArrayList<ResultMap>();
			FIELD: for (ResultMap map : sqlManager.useFile(AutyDB.class,
					"SelectFields.sql").each(params("TABLE", tableName))) {
				for (String pk : pkList) {
					if (pk.equals(map.get("COLUMN_NAME"))) {
						continue FIELD;
					}
				}
				fieldList.add(map);
			}

			StringBuilder sb = new StringBuilder();

			// SQL(get difference)
			buildSelect(sb, pkList, fieldList);
			StringBuilder sb2 = new StringBuilder().append(sb);
			buildFrom(sb, tableName, bkName, pkList, fieldList);
			buildWhere(sb, pkList, fieldList);

			// SQL(expected:exists, original:not exists)
			sb2.append("FROM").append(CRLF).append("\t").append(bkName).append(
					" e").append(CRLF);
			sb2.append("\t\tLEFT OUTER JOIN ").append(tableName)
					.append(" o ON").append(CRLF);
			for (int i = 0; i < pkList.size(); i++) {
				String pk = pkList.get(i);
				sb2.append("\t\t\t");
				if (i != 0)
					sb2.append("AND ");
				sameQuery(sb2, pk);
			}

			sb2.append("WHERE").append(CRLF);
			sb2.append("\te.").append(TESTCASE_FIELD).append(
					" = /* @testCaseName */").append(
					"'tetz42.test.AutyDBTest$1#test_sample|prepare'").append(
					CRLF);
			sb2.append("\tAND o.").append(pkList.get(0)).append(" IS NULL")
					.append(CRLF);

			// union
			sb.append("UNION ALL").append(CRLF).append(sb2);

			assertSQLMap.put(tableName, sb.toString());
		}
		return assertSQLMap.get(tableName);
	}

	private static void buildSelect(StringBuilder sb, List<String> pkList,
			List<ResultMap> fieldList) {
		// select clause
		sb.append("SELECT").append(CRLF);
		for (int i = 0; i < pkList.size(); i++) {
			String pk = pkList.get(i);
			sb.append("\t");
			if (i != 0)
				sb.append(",");
			sb.append("COALESCE(o.").append(pk).append(", e.").append(pk)
					.append(")").append(" A_").append(pk).append(CRLF);
		}
		for (ResultMap map : fieldList) {
			if ("DATE".equals(map.get("DATA_TYPE"))
					|| "CLOB".equals(map.get("DATA_TYPE"))
					|| "BLOB".equals(map.get("DATA_TYPE")))
				continue;
			String colName = "" + map.get("COLUMN_NAME");
			sb.append("\t,e.").append(colName).append(" E_").append(colName)
					.append(CRLF);
			sb.append("\t,o.").append(colName).append(" O_").append(colName)
					.append(CRLF);
		}
	}

	private static void buildFrom(StringBuilder sb, String tableName,
			String bkName, List<String> pkList, List<ResultMap> fieldList) {
		// from clause
		sb.append("FROM").append(CRLF).append("\t").append(tableName).append(
				" o").append(CRLF);
		sb.append("\t\tLEFT OUTER JOIN ").append(bkName).append(" e ON")
				.append(CRLF);
		sb.append("\t\t\te.").append(TESTCASE_FIELD).append(
				" = /* @testCaseName */").append(
				"'tetz42.test.AutyDBTest$1#test_sample|prepare'").append(CRLF);
		for (String pk : pkList) {
			sb.append("\t\t\tAND ");
			sameQuery(sb, pk);
		}
	}

	private static void buildWhere(StringBuilder sb, List<String> pkList,
			List<ResultMap> fieldList) {
		// where clause
		String aPK = pkList.get(0);
		sb.append("WHERE").append(CRLF);
		sb.append("\te.").append(aPK).append(" IS NULL").append(CRLF);

		sb.append("\tOR (").append(CRLF);
		sameQuery(sb.append("\t\t"), aPK);
		for (int i = 1; i < pkList.size(); i++) {
			sameQuery(sb.append("\t\tAND "), pkList.get(i));
		}
		sb.append("\t\tAND (").append(CRLF);
		for (int i = 0; i < fieldList.size(); i++) {
			ResultMap map = fieldList.get(i);
			if ("DATE".equals(map.get("DATA_TYPE"))
					|| "CLOB".equals(map.get("DATA_TYPE"))
					|| "BLOB".equals(map.get("DATA_TYPE")))
				continue;
			String colName = "" + map.get("COLUMN_NAME");
			sb.append("\t\t\t");
			if (i != 0) {
				sb.append("OR ");
			}
			sameQuery(sb.append("NOT"), colName);
		}
		sb.append("\t\t)").append(CRLF);
		sb.append("\t)").append(CRLF);
	}

	private static void createIf(String tableName, String bkTableName)
			throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		try {
			List<ResultMap> list = sqlManager.useFile(AutyDB.class,
					"SelectFields.sql").findAll(params("TABLE", bkTableName));
			if (!list.isEmpty())
				return; // exists

			list = sqlManager.useFile(AutyDB.class, "SelectFields.sql")
					.findAll(params("TABLE", tableName));
			if (list.isEmpty())
				throw new TableNotFoundException("The table you specified, '"
						+ tableName + "' is not found.");

			// create
			StringBuilder createSQL = new StringBuilder();
			createSQL.append("CREATE TABLE ").append(bkTableName).append("(")
					.append(CRLF);
			createSQL.append("\t").append(TESTCASE_FIELD).append("\t").append(
					"VARCHAR2(100) NOT NULL,").append(CRLF);

			for (ResultMap map : list) {
				Object type = map.get("DATA_TYPE");
				createSQL.append("\t").append(map.get("COLUMN_NAME")).append(
						"\t").append(type);
				if (!"DATE".equals(type) && !"BLOB".equals(type)
						&& !"CLOB".equals(type))
					createSQL.append("(").append(map.get("DATA_LENGTH"))
							.append(")");
				if (!isEmpty(map.get("DATA_DEFAULT")))
					createSQL.append("\t").append("DEFAULT ").append(
							map.get("DATA_DEFAULT"));
				if ("N".equals(map.get("NULLABLE")))
					createSQL.append("\t").append("NOT NULL");
				createSQL.append(",").append(CRLF);
			}
			createSQL.append("\t").append("UNIQUE (").append(TESTCASE_FIELD);
			List<String> pkList = sqlManager.useFile(AutyDB.class,
					"SelectPK.sql").findAll(String.class,
					params("TABLE", tableName));
			for (String key : pkList) {
				createSQL.append(", ").append(key);
			}
			createSQL.append(")").append(CRLF).append(")");
			System.out.println(createSQL);
			sqlManager.useSQL(createSQL.toString()).update();
			sqlManager.con().commit();
		} finally {
			sqlManager.closeStatement();
		}
	}

	private static void backUpIf(String tableName, String bkTableName)
			throws SQLException {
		if (backUpMap.containsKey(tableName)) {
			System.out.println(tableName + " is already backup.");
			return;
		}

		// get fields
		SQLManager sqlManager = sqlManager(con);
		List<ResultMap> list = sqlManager.useFile(AutyDB.class,
				"SelectFields.sql").findAll(params("TABLE", tableName));
		String fields = getFieldsString(list);

		// backup!
		int count = sqlManager.useFile(AutyDB.class, "InsertBkDatas.sql")
				.update(
						params("BK_TABLE", bkTableName).$("TABLE", tableName)
								.$("bkName", backUpName).$("fields", fields));
		System.out.println(tableName + " is backup to " + bkTableName + " as '"
				+ backUpName + "'. count:" + count);

		backUpMap.put(tableName, Boolean.TRUE);
	}

	private static String getFieldsString(List<ResultMap> list) {
		List<String> fields = map(list, new MyProc<String, ResultMap>() {

			@Override
			public String call(ResultMap param) {
				return String.valueOf(param.get("COLUMN_NAME"));
			}
		});
		return joinByComma(fields);
	}

	private static String joinByComma(List<?> list) {
		return join(list, ", ");
	}

	private static String join(List<?> list, String delimiter) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(list.get(0));
		for (int i = 1; i < list.size(); i++) {
			sb.append(delimiter).append(list.get(i));
		}
		return sb.toString();
	}

	private static interface MyProc<R, P> {
		R call(P param);
	}

	private static <R, P> List<R> map(List<P> params, MyProc<R, P> proc) {
		ArrayList<R> results = new ArrayList<R>();
		for (P param : params) {
			results.add(proc.call(param));
		}
		return results;
	}

	private static String convBkTable(String tableName) throws SQLException {
		String bkTableName = BK_PREFIX + tableName;
		if (bkTableName.length() > 30) {
			bkTableName = bkTableName.substring(0, 28) + "_" + 1;
			// TODO
			// for (int i = 1;; i++) {
			// bkTableName += "_" + i;
			// if (null == sqlManager(con).useFile(AutyDB.class,
			// "SelectTables.sql").find(
			// params("tableName", bkTableName)))
			// break;
			// }
		}
		return bkTableName;
	}

	private static void sameQuery(StringBuilder sb, String fieldName) {
		// sb.append("( ( o.").append(fieldName).append(" IS NULL AND e.").append(
		// fieldName).append(" IS NULL) OR o.").append(fieldName).append(
		// " = e.").append(fieldName).append(" )").append(CRLF);
		sb.append("( ( o.").append(fieldName).append(" IS NULL AND e.").append(
				fieldName).append(" IS NULL) OR (o.").append(fieldName).append(
				" IS NOT NULL AND e.").append(fieldName).append(
				" IS NOT NULL AND o.").append(fieldName).append(" = e.")
				.append(fieldName).append(") )").append(CRLF);
	}

	private static boolean isEmpty(Object obj) {
		return obj == null ? true : "".equals(obj);
	}

}
