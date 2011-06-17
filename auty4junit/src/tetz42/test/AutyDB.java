package tetz42.test;

import static tetz42.clione.SQLManager.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import tetz42.clione.SQLManager;
import tetz42.clione.util.ResultMap;
import tetz42.exception.TableNotFoundException;

public class AutyDB {

	private static final String tablePrefix = "ZUTY_BK_";
	private static final String testFiled = "ZUTY_TESTCASE_NAME";

	public static final String CRLF = System.getProperty("line.separator");

	public static void prepareDB(Connection con, String... tableNames)
			throws SQLException {
		for (String tableName : tableNames) {
			String bkName = tablePrefix + tableName;
			createIf(con, tableName, bkName);
		}
	}

	private static void createIf(Connection con, String tableName, String bkName)
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
			createSQL.append("\t").append(testFiled).append("\t").append(
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
				if ("Y".equals(map.get("NULLABLE")))
					createSQL.append("\t").append("NOT NULL");
				createSQL.append(",").append(CRLF);
			}
			createSQL.append("\t").append("PRIMARY\tKEY(").append(testFiled);
			List<String> pkList = sqlManager.useFile(AutyDB.class,
					"SelectPK.sql").findAll(String.class,
					params("TABLE", bkName));
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

	private static boolean isEmpty(Object obj) {
		return obj == null ? true : "".equals(obj);
	}

}
