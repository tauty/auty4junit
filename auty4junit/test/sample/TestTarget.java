package sample;

import static tetz42.clione.SQLManager.*;
import java.sql.Connection;
import java.sql.SQLException;

import tetz42.clione.SQLManager;

public class TestTarget {

	public static void moveDatasFromTableAToTableB(Connection con) throws SQLException {
		SQLManager sqlManager = sqlManager(con);
		sqlManager.useSQL("insert into TABLE_B select * from TABLE_A").update();
		sqlManager.useSQL("delete from TABLE_A").update();
		con.commit();
	}

}
