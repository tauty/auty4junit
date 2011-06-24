package tetz42.test;

import static org.junit.Assert.*;
import static tetz42.clione.SQLManager.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import tetz42.clione.SQLManager;
import tetz42.clione.exception.SQLRuntimeException;
import tetz42.clione.exception.WrapException;
import tetz42.clione.util.ResultMap;
import tetz42.exception.TableNotFoundException;

public class AutyDB {

	private static final String BK_PREFIX = "ZUTY_BK_";
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
			String bkName = BK_PREFIX + tableName;
			createIf(tableName, bkName);
			backUpIf(tableName, bkName);
			restore(tableName, bkName, clazz.getName() + "#" + testCaseName
					+ "|prepare");
		}
	}

	public static void assertDB(Class<?> clazz, String testCaseName,
			String... tableNames) throws SQLException {
		for (String tableName : tableNames) {
			String bkName = BK_PREFIX + tableName;
			assertDB(tableName, bkName, clazz.getName() + "#" + testCaseName
					+ "|expected");
		}
	}

	private static void assertDB(String tableName, String bkName,
			String testCaseName) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		int count = sqlManager.useSQL(
				"SELECT COUNT(*) FROM " + bkName + " WHERE " + TESTCASE_FIELD
						+ " = /* @testCase */").find(Integer.class,
				params("testCase", testCaseName));
		if (count == 0) {
			count = sqlManager.useSQL(getRestoreSQL(tableName, bkName)).update(
					params("tcName", testCaseName).$("bkName", backUpName));
			fail("No expected data found. Tried to insert some datas as expected data."
					+ CRLF
					+ "Please check it by the SQL below:"
					+ CRLF
					+ "SELECT * FROM "
					+ bkName
					+ " WHERE "
					+ TESTCASE_FIELD
					+ " = '"
					+ testCaseName
					+ "'"
					+ CRLF
					+ "If you don't like it, delete it by the SQL below:"
					+ CRLF
					+ "DELETE FROM "
					+ bkName
					+ " WHERE "
					+ TESTCASE_FIELD + " = '" + testCaseName + "'");
		}
		List<ResultMap> list = sqlManager.useSQL(
				getAssertSQL(tableName, bkName)).findAll(
				params("testCaseName", testCaseName));
		if (list.size() == 0)
			return;
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (ResultMap map : list) {
			sb.append(map).append(CRLF);
			if (i++ > 10) {
				sb.append(CRLF).append("Too much data!").append(CRLF);
				break;
			}
		}
		fail("Unmatch!" + CRLF + sb.toString());
	}

	public static synchronized void restoreAll() throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		List<String> list = sqlManager
				.useFile(AutyDB.class, "SelectTables.sql")
				.findAll(String.class);
		for (String tableName : list) {

			if (!backUpMap.containsKey(tableName))
				continue; // ignore the table has not backup.
			restore(tableName, BK_PREFIX + tableName, backUpName);
			sqlManager.useSQL(
					"DELETE FROM " + BK_PREFIX + tableName + " WHERE "
							+ TESTCASE_FIELD + " = /* @bkName */").update(
					params("bkName", backUpName));
		}
		backUpMap.clear();
	}

	private static void restore(String tableName, String bkName,
			String testCaseName) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		sqlManager.useSQL("DELETE FROM " + tableName).update();
		String sql = getRestoreSQL(tableName, bkName);
		int count = sqlManager.useSQL(sql).update(
				params("TABLE", tableName).$("bkName", testCaseName));
		if (count == 0) {
			count = sqlManager.useSQL(sql).update(
					params("tcName", testCaseName).$("bkName", backUpName));
			fail("No test case data found. Tried to insert some datas as test case data."
					+ CRLF
					+ "Please check it by the SQL below:"
					+ CRLF
					+ "SELECT * FROM "
					+ bkName
					+ " WHERE "
					+ TESTCASE_FIELD
					+ " = '"
					+ testCaseName
					+ "'"
					+ CRLF
					+ "If you don't like it, delete it by the SQL below:"
					+ CRLF
					+ "DELETE FROM "
					+ bkName
					+ " WHERE "
					+ TESTCASE_FIELD + " = '" + testCaseName + "'");
		}
	}

	private static String getRestoreSQL(String tableName, String bkName)
			throws SQLException {
		if (!restoreSQLMap.containsKey(tableName)) {
			SQLManager sqlManager = sqlManager(con);
			List<ResultMap> list = sqlManager.useFile(AutyDB.class,
					"SelectFields.sql").findAll(params("TABLE", tableName));
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO /*%if TABLE %STR(TABLE) */").append(bkName)
					.append(CRLF);
			sb.append("SELECT").append(CRLF).append("\t").append(
					"/* $tcName */'tetz42.test.AutyDB#test'").append(CRLF);
			for (ResultMap map : list) {
				sb.append("\t,").append(map.get("COLUMN_NAME")).append(CRLF);
			}
			sb.append("FROM").append(CRLF).append("\t").append(bkName).append(
					CRLF);
			sb.append("WHERE").append(CRLF).append("\t").append(TESTCASE_FIELD)
					.append(" = /* @bkName */'bk20110623-120000.000'");
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
				sb2.append("o.").append(pk).append(" = e.").append(pk).append(
						CRLF);
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
			if ("DATE".equals(map.get("DATA_TYPE")))
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
		for (String pk : pkList)
			sb.append("\t\t\tAND o.").append(pk).append(" = e.").append(pk)
					.append(CRLF);
	}

	private static void buildWhere(StringBuilder sb, List<String> pkList,
			List<ResultMap> fieldList) {
		// where clause
		String aPK = pkList.get(0);
		sb.append("WHERE").append(CRLF);
		sb.append("\te.").append(aPK).append(" IS NULL").append(CRLF);

		sb.append("\tOR (").append(CRLF);
		sb.append("\t\to.").append(aPK).append(" = e.").append(aPK)
				.append(CRLF);
		for (int i = 1; i < pkList.size(); i++)
			sb.append("\t\tAND o.").append(pkList.get(i)).append(" = e.")
					.append(pkList.get(i)).append(CRLF);
		sb.append("\t\tAND (").append(CRLF);
		for (int i = 0; i < fieldList.size(); i++) {
			ResultMap map = fieldList.get(i);
			if ("DATE".equals(map.get("DATA_TYPE")))
				continue;
			String colName = "" + map.get("COLUMN_NAME");
			sb.append("\t\t\t");
			if (i != 0) {
				sb.append("OR ");
			}
			sb.append("o.").append(colName).append(" <> e.").append(colName)
					.append(CRLF);
		}
		sb.append("\t\t)").append(CRLF);
		sb.append("\t)").append(CRLF);
	}

	private static void createIf(String tableName, String bkName)
			throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		try {
			List<ResultMap> list = sqlManager.useFile(AutyDB.class,
					"SelectFields.sql").findAll(params("TABLE", bkName));
			if (!list.isEmpty())
				return; // exists

			list = sqlManager.useFile(AutyDB.class, "SelectFields.sql")
					.findAll(params("TABLE", tableName));
			if (list.isEmpty())
				throw new TableNotFoundException("The table you specified, '"
						+ tableName + "' is not found.");

			// create
			StringBuilder createSQL = new StringBuilder();
			createSQL.append("CREATE TABLE ").append(bkName).append("(")
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
			createSQL.append("\t").append("PRIMARY\tKEY(").append(
					TESTCASE_FIELD);
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

	private static void backUpIf(String tableName, String bkName)
			throws SQLException {
		if (backUpMap.containsKey(tableName)) {
			System.out.println(tableName + " is already backup.");
			return;
		}

		// backup!
		SQLManager sqlManager = sqlManager(con);
		int count = sqlManager.useFile(AutyDB.class, "InsertBkDatas.sql")
				.update(
						params("BK_TABLE", bkName).$("TABLE", tableName).$(
								"bkName", backUpName));
		System.out.println(tableName + " is backup to " + bkName + " as '"
				+ backUpName + "'. count:" + count);

		backUpMap.put(tableName, Boolean.TRUE);
	}

	private static boolean isEmpty(Object obj) {
		return obj == null ? true : "".equals(obj);
	}

}
