package tetz42.db.backup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tetz42.clione.exception.SQLRuntimeException;
import tetz42.clione.exception.WrapException;
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
		System.out.println(m.getDatabaseProductName());
		System.out.println(m.getDatabaseProductVersion());
		System.out.println(m.getDatabaseMajorVersion());
		System.out.println(m.getDatabaseMinorVersion());
		System.out.println(m.getDriverName());
		System.out.println(m.getDriverVersion());
		System.out.println(m.getDriverMajorVersion());
		System.out.println(m.getDriverMinorVersion());
		System.out.println(m.usesLocalFiles());
		System.out.println(m.usesLocalFilePerTable());
		System.out.println(m.getIdentifierQuoteString());
		System.out.println(m.getSQLKeywords());
		System.out.println(m.getNumericFunctions());
		System.out.println(m.getSearchStringEscape());
		System.out.println(m.getExtraNameCharacters());
		System.out.println(m.getCatalogTerm());
		System.out.println(m.isCatalogAtStart());
		System.out.println(m.getCatalogSeparator());
		System.out.println(m.supportsSelectForUpdate());
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
			for (int i = 1; i < rsMeta.getColumnCount(); i++) {
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
			for (int i = 1; i < rsMeta.getColumnCount(); i++) {
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
			for (int i = 1; i < rsMeta.getColumnCount(); i++) {
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
			for (int i = 1; i < rsMeta.getColumnCount(); i++) {
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
			for (int i = 1; i < rsMeta.getColumnCount(); i++) {
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
}
