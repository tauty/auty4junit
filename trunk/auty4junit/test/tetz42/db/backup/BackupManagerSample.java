package tetz42.db.backup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tetz42.test.DBConfig;
import tetz42.test.Proc;

public class BackupManagerSample {

	private static Connection con;
	private static String backUpName;

	public static void main(String[] args) throws Exception {

		useConnection(new Proc() {

			@Override
			public void run(Connection con) throws Exception {
				printProductName(con);
				searchTables(con);
				searchIndexInfo(con);
				searchPrimaryKeys(con);
				searchImportedKeys(con);
				searchExportedKeys(con);
			}

		});

	}

	private static void printProductName(Connection con) throws SQLException {
		System.out.println("----------- printProductName ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData m = con.getMetaData();
		System.out.println("getDatabaseProductName|" + m.getDatabaseProductName());
		System.out.println("getDatabaseProductVersion|" + m.getDatabaseProductVersion());
		System.out.println("getDatabaseMajorVersion|" + m.getDatabaseMajorVersion());
		System.out.println("getDatabaseMinorVersion|" + m.getDatabaseMinorVersion());
		System.out.println("getDriverName|" + m.getDriverName());
		System.out.println("getDriverVersion|" + m.getDriverVersion());
		System.out.println("getDriverMajorVersion|" + m.getDriverMajorVersion());
		System.out.println("getDriverMinorVersion|" + m.getDriverMinorVersion());
		System.out.println("usesLocalFiles|" + m.usesLocalFiles());
		System.out.println("usesLocalFilePerTable|" + m.usesLocalFilePerTable());
		System.out.println("getIdentifierQuoteString|" + m.getIdentifierQuoteString());
		System.out.println("getSQLKeywords|" + m.getSQLKeywords());
		System.out.println("getNumericFunctions|" + m.getNumericFunctions());
		System.out.println("getSearchStringEscape|" + m.getSearchStringEscape());
		System.out.println("getExtraNameCharacters|" + m.getExtraNameCharacters());
		System.out.println("getCatalogTerm|" + m.getCatalogTerm());
		System.out.println("isCatalogAtStart|" + m.isCatalogAtStart());
		System.out.println("getCatalogSeparator|" + m.getCatalogSeparator());
		System.out.println("supportsSelectForUpdate|" + m.supportsSelectForUpdate());
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");
	}

	private static void searchIndexInfo(Connection con) throws SQLException {
		System.out.println("----------- searchIndexInfo ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = con.getMetaData();

		ResultSet rs = metaData.getIndexInfo(null, metaData.getUserName(),
				"T_ANKEN", false, true);
		ResultSetMetaData rsMeta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				System.out.print(rsMeta.getColumnLabel(i));
				System.out.print(":");
				System.out.print(rs.getObject(i));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");
	}

	private static void searchTables(Connection con) throws SQLException {
		System.out.println("----------- searchTables ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = con.getMetaData();
		// ResultSet rs = metaData.getCatalogs();
		// ResultSet rs = metaData.getSchemas(null, null);
		System.out.println(metaData.getUserName());
		ResultSet rs = metaData.getTables(null, metaData.getUserName(), null,
				null);
		// ResultSet rs = metaData.getTables(null, null, null, null);
		ResultSetMetaData rsMeta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				System.out.print(rsMeta.getColumnLabel(i));
				System.out.print(":");
				System.out.print(rs.getObject(i));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");
	}

	private static void searchPrimaryKeys(Connection con) throws SQLException {
		System.out.println("----------- searchPrimaryKeys ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = con.getMetaData();

		ResultSet rs = metaData.getPrimaryKeys(null, metaData.getUserName(),
				"T_ANKEN_RRK");
		ResultSetMetaData rsMeta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				System.out.print(rsMeta.getColumnLabel(i));
				System.out.print(":");
				System.out.print(rs.getObject(i));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");
	}

	private static void searchImportedKeys(Connection con) throws SQLException {
		System.out.println("----------- searchImportedKeys ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = con.getMetaData();

		ResultSet rs = metaData.getImportedKeys(null, metaData.getUserName(),
				"T_ANKEN_RRK");
		ResultSetMetaData rsMeta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				System.out.print(rsMeta.getColumnLabel(i));
				System.out.print(":");
				System.out.print(rs.getObject(i));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");

		// 1 by 1
		// ResultSet rs = metaData.getCrossReference(null,
		// metaData.getUserName(),
		// "T_ANKEN", null, metaData.getUserName(), "T_ANKEN_RRK");
		// ResultSetMetaData rsMeta = rs.getMetaData();
		// while (rs.next()) {
		// for (int i = 1; i < rsMeta.getColumnCount(); i++) {
		// System.out.print(rsMeta.getColumnLabel(i));
		// System.out.print(":");
		// System.out.print(rs.getObject(i));
		// System.out.print(", ");
		// }
		// System.out.println();
		// }
	}

	/**
	 * Too late!!
	 *
	 * @param con
	 * @throws SQLException
	 */
	private static void searchExportedKeys(Connection con) throws SQLException {
		System.out.println("----------- searchExportedKeys ---------------");
		final long startTime = System.currentTimeMillis();
		DatabaseMetaData metaData = con.getMetaData();

		ResultSet rs = metaData.getExportedKeys(null, metaData.getUserName(),
				"T_ANKEN");
		ResultSetMetaData rsMeta = rs.getMetaData();
		while (rs.next()) {
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				System.out.print(rsMeta.getColumnLabel(i));
				System.out.print(":");
				System.out.print(rs.getObject(i));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("[Time:" + (System.currentTimeMillis() - startTime)
				+ "ms]");
	}

	public static synchronized void useConnection(Proc proc) throws Exception {
		if (con == null) {
			try {
				con = DriverManager.getConnection(DBConfig.DB_CONNECT_STRING,
						DBConfig.DB_CONNECT_USER, DBConfig.DB_CONNECT_PASS);
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} catch (ExceptionInInitializerError e) {
				throw new RuntimeException(e.getCause());
			}
		}
		if (backUpName == null) {
			backUpName = "bk"
					+ new SimpleDateFormat("yyyyMMdd-HHmmss.SSS")
							.format(new Date());
		}
		proc.run(con);
	}
}
