package sample;

import static tetz42.test.AutyDB.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Test;

import tetz42.test.Proc;

public class ADBTest {

	@Test
	public void atest() throws Exception {

		// 呪文･･･
		useConnection(new Proc() {

			@Override
			// 呪文･･･
			public void run(Connection con) throws Exception {

				// 指定されたテーブルのデータバックアップ ＆ テストデータ読込
				prepareDB(ADBTest.class, "atest", "TABLE_A");

				// 指定されたテーブルのデータバックアップ ＆ データ削除
				deleteDB(ADBTest.class, "atest", "TABLE_B");

				// テスト対象の実行
				TestTarget.moveDatasFromTableAToTableB(con);

				// 指定されたテーブルが空っぽであることの検証
				assertZero(ADBTest.class, "atest", "TABLE_A");

				// 指定されたテーブルが保存されたテスト結果と一致しているか検証
				assertDB(ADBTest.class, "atest", "TABLE_B");
			}
		});
	}

	@AfterClass
	public static void bye() throws SQLException {
		// バックアップされたデータの復旧
		restoreAll();
	}
}
