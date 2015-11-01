# Introduction #
auty4junitは、テスト結果を自動的に生成することでテストの高い生産性を実現する、JUnit補助ツールです。<br>
TDDやBDDには向きませんが、先にコーディングを行うスタイルで開発を進める場合や、テストコードのないレガシーコードをリファクタリングする場合に力強い味方となります。<br>
auty4junitでは、通常のテスト向けのAutyクラスと、RDBMSを使ったテスト向けのAutyDBクラスの二つを使って開発を進めていきます。<br>
<br>
<h1>Autyクラスの使い方</h1>

<h2>基本的な使い方</h2>

Autyクラスは、RDBMSを使わないテストで使用します。<br>
Autyクラスの使い方は極めて簡単です。例えば下記のようなコードを書いたとします。<br>

<pre><code>package sample;<br>
<br>
import static tetz42.test.Auty.*;<br>
<br>
import java.util.HashMap;<br>
import java.util.Map;<br>
<br>
import org.junit.Test;<br>
<br>
public class ATest {<br>
<br>
	static class Sample {<br>
		int intField1;<br>
		int intField2;<br>
		String strField1;<br>
		String strField2;<br>
		Map&lt;String, String&gt; mapField = new HashMap&lt;String, String&gt;();<br>
	}<br>
<br>
	@Test<br>
	public void atest() {<br>
		Sample sample = new Sample();<br>
		sample.intField1 = 101;<br>
		sample.intField2 = 102;<br>
		sample.strField1 = "文字列１";<br>
		sample.strField2 = "文字列２";<br>
		sample.mapField.put("key1", "value1");<br>
		sample.mapField.put("key2", "value2");<br>
		sample.mapField.put("key3", "value3");<br>
<br>
		assertEqualsWithFile(sample, getClass(), "atest");<br>
	}<br>
}<br>
</code></pre>

Autyクラスのメソッドは、最後から3行目の_assertEqualsWithFile <i>です。</i><br>
このテストクラスを実行すると、一回目はテストケースが失敗して下記のような失敗メッセージが出力されます。<br>
<pre><code>java.lang.AssertionError: No file found. The actual string has been output to the path:<br>
test\sample\expected\ATest\atest.txt<br>
 The contents of the file are as follows:<br>
Sample{<br>
	intField1 = 101<br>
	intField2 = 102<br>
	strField1 = "文字列１"<br>
	strField2 = "文字列２"<br>
	mapField = HashMap{<br>
		"key1": "value1"<br>
		"key2": "value2"<br>
		"key3": "value3"<br>
	}<br>
}<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.Auty.assertEqualsWithFile(Auty.java:132)<br>
	at sample.ATest.atest(ATest.java:31)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                       :<br>
                       :<br>
</code></pre>

このとき、あなたがやるべきことは、上記メッセージの「The contents of the file are as follows:」の下に表示された内容をチェックすることです。<br>
見ての通り、こちらには_assertEqualsWithFile <i>の第一パラメータとして渡したオブジェクトのdump結果が表示されています。もし内容に問題がなければ、そのままテストケースを再実行してください。</i><br>
今度はテストが成功するはずです。<br>
<br>
それでは、今度はテストをわざと失敗させてみましょう。<br>
テストクラスの「atest」メソッドのsample.strField1に値を代入する処理を、下記のように書き換えて見ます。<br>
<br>
<pre><code>		sample.strField1 = "string1";<br>
</code></pre>

この状態でテストを再度実行するとテストが失敗して、下記のような失敗メッセージが表示されます。<br>
<br>
<pre><code>java.lang.AssertionError: Actual data does not match! Check the diff message below:<br>
  expected -&gt; '-', actual -&gt; '+' <br>
<br>
00001|Sample{<br>
00002|	intField1 = 101<br>
00003|	intField2 = 102<br>
00004|-	strField1 = "文字列１"<br>
00004|+	strField1 = "string1"<br>
00005|	strField2 = "文字列２"<br>
00006|	mapField = HashMap{<br>
00007|		"key1": "value1"<br>
00008|		"key2": "value2"<br>
00009|		"key3": "value3"<br>
00010|	}<br>
00011|}<br>
<br>
You can ignore this assertion error to append parameters at 'assertEqualsWithFile' method like below:<br>
assertEqualsWithFile(foo, getClass(), "file_name", 4);<br>
<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.Auty.assertSameStrings(Auty.java:240)<br>
	at tetz42.test.Auty.assertEqualsWithFile(Auty.java:122)<br>
	at sample.ATest.atest(ATest.java:31)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                       :<br>
                       :<br>
</code></pre>

メッセージ内の行番号の右に「-」が表示されているのが期待値、「+」が表示されているのが実際の値です。<br>
実際の開発ではこのdiff情報を元に、テスト対象のソースを直すのか、テストクラスを直すのか、テスト結果を直すのかを選択して対処を行うことになります。<br>
auty4junitを使った開発でもテスト対象やテストクラスの修正は通常と全く同じですので、ここでは、テスト結果を直す対処法について説明します。<br>
<br>
<i>assertEqualsWithFile</i>によりテスト結果は、<br>
<i>test/第二パラメータのクラスオブジェクトのパッケージ/expected/第二パラメータのクラスオブジェクトのクラス名/第三パラメータのファイル名.txt</i><br>
にテキストファイルとして出力されます。<br>
今回のケースでは、<br>
<i>test/sample/expected/ATest/atest.txt</i><br>
が出力結果となります。ここで取り得る手は三つあります。<br>
最初が上記ファイルを普通にエディタで開いて、手作業で修正する手です。今回のケースのように修正量が少なければ、この方法が手っ取り早いでしょう。<br>

次に、上記ファイルを削除してやり直す方法です。auty4junitでは上記ファイルが存在しなければ初回のテストだと判断しますので、再度dump結果を上記ファイルとして出力させることができます。上記diffメッセージのみで新しく出力されるテスト結果が正しいと判断できる場合に有効です。<br>

最後が、失敗時に出力されるdumpファイルを利用する方法です。<br>
<i>assertEqualsWithFile</i>では生成されたdumpファイルとのマッチングでテストケースが失敗した場合、<br>
<i>test/第二パラメータのクラスオブジェクトのパッケージ/failed/第二パラメータのクラスオブジェクトのクラス名/第三パラメータのファイル名.txt</i><br>
というパスに新しくdumpした結果のテキストファイルを出力します。<br>
今回のケースでは、<br>
<i>test/sample/failed/ATest/atest.txt</i><br>
というファイルがそれになります。<br>
これをテキストエディタで開いて、内容に問題がなければこのファイルで上記expectedフォルダ以下のファイルを上書きします。<br>

上記三つのいずれかの方法でテスト結果を修正あとに再度テストケースを実行すると、今度はテストが成功するはずです。<br>
<br>
<h2>特定行の無視</h2>

次に、下記のようなコードを例に考えてみます。<br>
<br>
<pre><code>package sample2;<br>
<br>
import static tetz42.test.Auty.*;<br>
<br>
import java.util.Date;<br>
import java.util.HashMap;<br>
import java.util.Map;<br>
<br>
import org.junit.Test;<br>
<br>
public class ATest {<br>
<br>
	static class Sample {<br>
		int intField1;<br>
		int intField2;<br>
		String strField1;<br>
		String strField2;<br>
		Map&lt;String, String&gt; mapField = new HashMap&lt;String, String&gt;();<br>
	}<br>
<br>
	@Test<br>
	public void atest() {<br>
		Sample sample = new Sample();<br>
		sample.intField1 = 101;<br>
		sample.intField2 = 102;<br>
		sample.strField1 = "文字列１";<br>
		sample.strField2 = "文字列２";<br>
		sample.mapField.put("key1", "value1");<br>
		sample.mapField.put("key2", "value2");<br>
		sample.mapField.put("key3", "value3");<br>
		sample.mapField.put("now", new Date().toString());<br>
<br>
		assertEqualsWithFile(sample, getClass(), "atest");<br>
	}<br>
}<br>
</code></pre>

こちらを実行すると、下記のような失敗メッセージが表示されます。<br>
<br>
<pre><code>java.lang.AssertionError: No file found. The actual string has been output to the path:<br>
test\sample2\expected\ATest\atest.txt<br>
 The contents is below:<br>
Sample{<br>
	intField1 = 101<br>
	intField2 = 102<br>
	strField1 = "文字列１"<br>
	strField2 = "文字列２"<br>
	mapField = HashMap{<br>
		"key1": "value1"<br>
		"key2": "value2"<br>
		"key3": "value3"<br>
		"now": "Mon Aug 20 10:28:52 JST 2012"<br>
	}<br>
}<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.Auty.assertEqualsWithFile(Auty.java:133)<br>
	at sample2.ATest.atest(ATest.java:33)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                       :<br>
                       :<br>
</code></pre>

ここまでは前回の例と同じですが、再度テストケースを実行すると前回とは違いテストが失敗してしまい、下記のような失敗メッセージが表示されます。<br>
<br>
<pre><code>java.lang.AssertionError: Actual data does not match! Check the diff message below:<br>
  expected -&gt; '-', actual -&gt; '+' <br>
<br>
00001|Sample{<br>
00002|	intField1 = 101<br>
00003|	intField2 = 102<br>
00004|	strField1 = "文字列１"<br>
00005|	strField2 = "文字列２"<br>
00006|	mapField = HashMap{<br>
00007|		"key1": "value1"<br>
00008|		"key2": "value2"<br>
00009|		"key3": "value3"<br>
00010|-		"now": "Mon Aug 20 10:28:52 JST 2012"<br>
00010|+		"now": "Mon Aug 20 10:31:54 JST 2012"<br>
00011|	}<br>
00012|}<br>
<br>
You can ignore this assertion error to append parameters at 'assertEqualsWithFile' method like below:<br>
assertEqualsWithFile(foo, getClass(), "file_name", 10);<br>
<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.Auty.assertSameStrings(Auty.java:241)<br>
	at tetz42.test.Auty.assertEqualsWithFile(Auty.java:123)<br>
	at sample2.ATest.atest(ATest.java:33)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                       :<br>
                       :<br>
</code></pre>

原因はソースの最後から5行目の、<br>
<br>
<pre><code>		sample.mapField.put("now", new Date().toString());<br>
</code></pre>
です。システム日時を文字列化した値を代入しているので、実行の度に値が変わってしまっています。<br>
テスト対象オブジェクト内部に上記のようにシステム日時由来の値やランダム値、新規採番されたIDなど実行の度に変わってしまう値が含まれていることは通常の開発で良くあることです。<br>
auty4junitではこのような事態に対応するために、特定の行を無視してマッチングを行わないようにする手段を設けています。<br>
上記失敗メッセージ中に、<i>「You can ignore this assertion error to append parameters at 'assertEqualsWithFile' method like below:」</i> と書かれている箇所があるのに気が付いたでしょうか？この下に、下記のように書かれています。<br>
<pre><code>assertEqualsWithFile(foo, getClass(), "file_name", 10);<br>
</code></pre>

この第4パラメータの数値は、無視する行番号を表しています。この指示に従い、テストクラスの_assertEqualsWithFile <i>を使用している箇所を下記のように書き換えます。</i>

<pre><code>		assertEqualsWithFile(sample, getClass(), "atest", 10);<br>
</code></pre>

上記のようにしてテストを再実行すると、今度はテストが成功するはずです。<br>
なお無視されるのは上記の例では10行目だけですので、他の値はちゃんとチェックされます。<br>


<h1>AutyDBクラスの使い方</h1>
※AutyDBは現在ベータ版です。現状はOracleでしか動作しませんし、将来インターフェースが大きく変わる可能性があるので、ご注意下さい。<br>

RDBMSと連携するJUnitのテストクラスを作成する場合には、AutyDBを使います。<br>
<blockquote>※現在のバージョンではOracleのみに対応しています。<br></blockquote>

AutyDBをRDBMSに接続させる必要があるので、auty.propertiesというファイルを用意して、下記のように編集してクラスパスの通った場所に置いてください。<br>
<pre><code>DB_CONNECT_STRING=jdbc:oracle:thin:@dbserver:1521:SAMPLEDB<br>
DB_CONNECT_USER=DB_USER<br>
DB_CONNECT_PASS=DB_PASS<br>
</code></pre>
「DB_CONNECT_STRING」には接続文字列、「DB_CONNECT_USER」には接続ユーザ、「DB_CONNECT_PASS」にはパスワードを、それぞれ環境に合わせて設定してください。<br>
<br>
RDBMSを使った単体テストは、一般に下記のような流れでテストを実施します。<br>
<ol><li>現在スキーマにあるデータをバックアップしてから削除。<br>
</li><li>テストデータをスキーマにロードする。<br>
</li><li>テスト対象コードの実行。<br>
</li><li>テスト結果の検証。<br>
</li><li>テストデータを削除し、1. にてバックアップしたデータを復元</li></ol>

AutyDBクラスを使うと上記は、下記のようなコードで実現することになります。<br>
<br>
<pre><code>package sample;<br>
<br>
import static tetz42.test.AutyDB.*;<br>
<br>
import java.sql.Connection;<br>
import java.sql.SQLException;<br>
<br>
import org.junit.AfterClass;<br>
import org.junit.Test;<br>
<br>
import tetz42.test.Proc;<br>
<br>
public class ADBTest {<br>
<br>
	@Test<br>
	public void atest() throws Exception {<br>
<br>
		// 呪文･･･<br>
		useConnection(new Proc() {<br>
<br>
			@Override<br>
			// 呪文･･･<br>
			public void run(Connection con) throws Exception {<br>
<br>
				// 上記1, 2 指定されたテーブルのデータバックアップ ＆ テストデータ読込<br>
				prepareDB(ADBTest.class, "atest", "TABLE_A");<br>
				<br>
				// 上記1, 2 指定されたテーブルのデータバックアップ ＆ データ削除<br>
				deleteDB(ADBTest.class, "atest", "TABLE_B");<br>
<br>
				// 上記3 テスト対象の実行<br>
				TestTarget.moveDatasFromTableAToTableB(con);<br>
<br>
				// 上記4 指定されたテーブルが空っぽであることの検証<br>
				assertZero(ADBTest.class, "atest", "TABLE_A");<br>
				<br>
				// 上記4 指定されたテーブルが保存されたテスト結果と一致しているか検証<br>
				assertDB(ADBTest.class, "atest", "TABLE_B");<br>
			}<br>
		});<br>
	}<br>
<br>
	@AfterClass<br>
	public static void bye() throws SQLException {<br>
		// 上記5 バックアップされたデータの復元<br>
		restoreAll();<br>
	}<br>
}<br>
</code></pre>

prepareDB, deleteDB, assertZero, assertDBはAutyDBクラスのメソッドで、パラメータは全て、<br>
<ul><li>第一パラメータ ･･･ クラスオブジェクト(テストデータの特定用)<br>
</li><li>第二パラメータ ･･･ テストケース名(テストデータの特定用)<br>
</li><li>第三パラメータ以降 ･･･ テーブル名(複数指定可)<br>
となっています。<br></li></ul>

ここでは上記で指定されたTABLE_A, TABLE_Bがともにプライマリキーの「id」と、テキストデータが入った「text」という列を持っていて、それぞれ下記に示すデータが格納されていると仮定して、開発の流れを説明します。<br>
<br>
<table border='1'>
<blockquote><tr><th>TABLE_A</th></tr>
<tr><th>id</th><th>text</th></tr>
<tr><td>1</td><td>いち</td></tr>
<tr><td>2</td><td>にい</td></tr>
<tr><td>3</td><td>秋刀魚の</td></tr>
<tr><td>4</td><td>尻尾</td></tr>
<tr><td>5</td><td>ゴリラの</td></tr>
<tr><td>6</td><td>息子</td></tr>
<tr><td>7</td><td>菜っ葉</td></tr>
<tr><td>8</td><td>葉っぱ</td></tr>
<tr><td>9</td><td>腐った</td></tr>
<tr><td>10</td><td>豆腐</td></tr>
<table></blockquote>

<table border='1'>
<blockquote><tr><th>TABLE_B</th></tr>
<tr><th>id</th><th>text</th></tr>
<tr><td>1</td><td>一献</td></tr>
<tr><td>2</td><td>二献</td></tr>
<tr><td>3</td><td>三献です。</td></tr>
<table></blockquote>

<h2>テストデータの準備</h2>
まず最初にテストクラスを実行すると、テストケースが失敗して下記失敗メッセージが出力されます。<br>
<br>
<pre><code>java.lang.AssertionError: No test case data found. Tried to insert some datas as test case data.<br>
Please check it by the SQL below:<br>
SELECT * FROM ZUTY_TABLE_A WHERE ZUTY_TESTCASE_NAME = 'atest.prepare#sample.ADBTest'<br>
If you do not like it, delete it by the SQL below:<br>
DELETE FROM ZUTY_TABLE_A WHERE ZUTY_TESTCASE_NAME = 'atest.prepare#sample.ADBTest'<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.AutyDB.restore(AutyDB.java:219)<br>
	at tetz42.test.AutyDB.restore(AutyDB.java:206)<br>
	at tetz42.test.AutyDB.prepareDB(AutyDB.java:62)<br>
	at sample.ADBTest$1.run(ADBTest.java:26)<br>
	at tetz42.test.AutyDB.useConnection(AutyDB.java:53)<br>
	at sample.ADBTest.atest(ADBTest.java:19)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                      :<br>
                      :<br>
</code></pre>

この時、スキーマを確認すると「ZUTY_TABLE_A」というテーブルが新たに生成されているはずです。これはprepareDBメソッド内の処理にて、テストデータ及びバックアップデータを格納するために自動的に生成したテーブルです。<br>
<br>
上記テスト失敗メッセージの中に、<br>
<pre><code>SELECT * FROM ZUTY_TABLE_A WHERE ZUTY_TESTCASE_NAME = 'atest.prepare#sample.ADBTest'<br>
</code></pre>
というSELECT文が出力されています。これをスキーマ上で実行すると下記のようなデータが得られるはずです。<br>
<br>
<table border='1'>
<blockquote><tr><th>ZUTY_TABLE_A</th></tr>
<tr><th>zuty_testcase_name</th><th>id</th><th>text</th></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>1</td><td>いち</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>2</td><td>にい</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>3</td><td>秋刀魚の</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>4</td><td>尻尾</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>5</td><td>ゴリラの</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>6</td><td>息子</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>7</td><td>菜っ葉</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>8</td><td>葉っぱ</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>9</td><td>腐った</td></tr>
<tr><td>atest.prepare#sample.ADBTest</td><td>10</td><td>豆腐</td></tr>
<table></blockquote>

ここで、上記例でのprepareDBメソッドの動作を詳しく説明します。<br>
<ul><li>TABLE_Aに対するテストデータ＆バックアップデータ保存用のテーブルの「ZUTY_TABLE_A」が存在しないとき、生成する。<br>
</li><li>TABLE_Aの現在のデータを、テスト起動時のシステム日時を文字列化した値をzuty_testcase_nameに指定して、バックアップとしてZUTY_TABLE_Aに保存する。<br>
</li><li>『[テストケース名].prepare#[クラスオブジェクトのフルパス名]』という文字列を生成し、ZUTY_TESTCASE_NAMEに生成した文字列が格納されているデータがあれば、TABLE_Aのデータを削除してからTABLE_Aにコピーする。データがなければ、TABLE_Aの現在のデータを生成した文字列をZUTY_TESTCASE_NAMEに指定してZUTY_TABLE_Aに保存して、上記のテスト失敗エラーを発生させる。</li></ul>

この動作により、上記の自動的に保存されたデータは次回テスト実行時にはテストデータとして自動的に読み込まれるようになります。<br>
なお、ZUTY_TABLE_Aに保存されたデータに問題が発覚した場合には、直接編集するか、上記テスト失敗メッセージ内にある、<br>
<pre><code>DELETE FROM ZUTY_TABLE_A WHERE ZUTY_TESTCASE_NAME = 'atest.prepare#sample.ADBTest'<br>
</code></pre>
というDELETE文を実行し、テストデータを削除してやり直してください。<br>
<br>
deleteDBメソッドは指定されたテーブルに対して「ZUTY<code>_</code>...」というテーブルを自動生成してバックアップを取るところまではprepareDBと同じですが、その後に指定されたテーブルのデータを削除するところが違います。テスト開始時点でデータを0件にしたいテーブルに対して使用します。<br>
ここではTABLE_Bに対してZUTY_TABLE_Bというテーブルが生成されて、バックアップデータが保存されます。<br>
<br>
テストデータの準備が終わったら、テスト対象クラスを実行します。<br>
上記テストクラスより、テスト対象の実行を行っている箇所を抜き出します。<br>
<pre><code>				// 上記3 テスト対象の実行<br>
				TestTarget.moveDatasFromTableAToTableB(con);<br>
</code></pre>

ここでは_TestTarget.moveDatasFromTableAToTableB <i>というメソッドがテスト対象です。</i><br>
テスト対象クラスの動作内容は、メソッド名の通りTABLE_Aのデータを全てTABLE_Bにコピーし、TABLE_Aのデータを削除するものだとして以下の説明を続けます。<br>
<br>
<h2>テスト結果の検証</h2>

上記テストクラスより、テスト結果の検証を行っている箇所を抜き出します。<br>
<pre><code>				// 上記4 指定されたテーブルが空っぽであることの検証<br>
				assertZero(ADBTest.class, "atest", "TABLE_A");<br>
				<br>
				// 上記4 指定されたテーブルが保存されたテスト結果と一致しているか検証<br>
				assertDB(ADBTest.class, "atest", "TABLE_B");<br>
</code></pre>

assertZeroは指定されたテーブルがレコード0件であることを検証するメソッドです。テスト対象の実行により、TABLE_AのデータはTABLE_Bに移動されているので、TABLE_Aは空になるはずなので、上記のようにassertZeroにて検証を行っています。<br>
<br>
ここでテストクラスを再度実行すると、またテストが失敗して下記のような失敗メッセージが表示されるはずです。<br>
<pre><code>java.lang.AssertionError: No expected data found. Tried to insert some datas as expected data.<br>
Please check it by the SQL below:<br>
SELECT * FROM ZUTY_TABLE_B WHERE ZUTY_TESTCASE_NAME = 'atest.expected#sample.ADBTest'<br>
If you do not like it, delete it by the SQL below:<br>
DELETE FROM ZUTY_TABLE_B WHERE ZUTY_TESTCASE_NAME = 'atest.expected#sample.ADBTest'<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.AutyDB.assertDB(AutyDB.java:100)<br>
	at tetz42.test.AutyDB.assertDB(AutyDB.java:82)<br>
	at sample.ADBTest$1.run(ADBTest.java:38)<br>
	at tetz42.test.AutyDB.useConnection(AutyDB.java:53)<br>
	at sample.ADBTest.atest(ADBTest.java:19)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
                      :<br>
                      :<br>
</code></pre>

これは、assertZeroの次のassertDBメソッドによる検証で発生しています。<br>
メッセージ中に、<br>
<pre><code>SELECT * FROM ZUTY_TABLE_B WHERE ZUTY_TESTCASE_NAME = 'atest.expected#sample.ADBTest'<br>
</code></pre>
というSELECT文がありますが、これを実行すると下記のような結果が得られます。<br>
<br>
<table border='1'>
<blockquote><tr><th>ZUTY_TABLE_B</th></tr>
<tr><th>zuty_testcase_name</th><th>id</th><th>text</th></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>1</td><td>いち</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>2</td><td>にい</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>3</td><td>秋刀魚の</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>4</td><td>尻尾</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>5</td><td>ゴリラの</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>6</td><td>息子</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>7</td><td>菜っ葉</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>8</td><td>葉っぱ</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>9</td><td>腐った</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>10</td><td>豆腐</td></tr>
<table></blockquote>

assertDBメソッドは、次のように動作します。<br>
<ul><li>TABLE_Bに対するテストデータ＆バックアップデータ保存用のテーブルの「ZUTY_TABLE_B」が存在しないとき、生成する。<br>
</li><li>TABLE_Bに格納されているテスト結果を、<br>
</li><li>『[テストケース名].expected#[クラスオブジェクトのフルパス名]』という文字列を生成し、ZUTY_TESTCASE_NAMEに生成した文字列が格納されているデータがあれば、そのデータとTABLE_Bに格納されているテスト結果とを比較して検証を行う(※1)。データがなければ、TABLE_Bのテスト結果を生成した文字列をZUTY_TESTCASE_NAMEに指定してZUTY_TABLE_Bに保存して、上記のテスト失敗エラーを発生させる。<br>
</li><li>上記※1のケースでは、ZUTY_TABLE_Bとテスト結果データとの間に違いがあれば、テストケースを失敗させる。</li></ul>

つまり、assertDBが初回に実行されたときにはまだ『[テストケース名].expected#[クラスオブジェクトのフルパス名]』の文字列でZUTY<code>_</code>･･･に保存されたデータがまだないので、初回のテスト結果を保存します。<br>
そして２回目以降の実行では、保存された値とテスト結果との比較を行う仕組みになっています。<br>
これにより、DB上に普通にテストデータを構築しておけば、後はprepareDB, deleteDB, assertDB, assertZeroなどにテストに使用するテーブルを記載しておけば、何度かテストクラスを実行するだけで、JUnitのテストが完成する仕組みになっています。<br>
<br>
なお、ZUTY_TABLE_Bに保存された値に問題があった場合には、上記エラーメッセージ内のDELETE文の、<br>
<pre><code>DELETE FROM ZUTY_TABLE_B WHERE ZUTY_TESTCASE_NAME = 'atest.expected#sample.ADBTest'<br>
</code></pre>
を実行することで、再度データを削除してテスト結果データの保存からやり直すことができます。<br>
<br>
ここでもう一度テストクラスを実行してみましょう。テストの準備データ・検証用データが既に揃っているため、テストが成功するはずです。<br>
それでは次に、わざとテストを失敗させてみましょう。上記ZUTY_TABLE_Bに格納された検証用データを、下記のように「息子」⇒「ロケット」に書き換えます。<br>
<br>
<table border='1'>
<blockquote><tr><th>ZUTY_TABLE_B</th></tr>
<tr><th>zuty_testcase_name</th><th>id</th><th>text</th></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>1</td><td>いち</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>2</td><td>にい</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>3</td><td>秋刀魚の</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>4</td><td>尻尾</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>5</td><td>ゴリラの</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>6</td><td>ロケット</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>7</td><td>菜っ葉</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>8</td><td>葉っぱ</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>9</td><td>腐った</td></tr>
<tr><td>atest.expected#sample.ADBTest</td><td>10</td><td>豆腐</td></tr>
<table></blockquote>

この状態でテストクラスを実行するとテストが失敗し、下記失敗メッセージが出力されます。<br>
<pre><code>java.lang.AssertionError: Unmatch!<br>
expected data's key:atest.expected#sample.ADBTest<br>
PK:ID=6	Not Equals[TEXT] expected:ロケット, but was:息子	<br>
<br>
SELECT<br>
	COALESCE(o.ID, e.ID) A_ID<br>
	,e.TEXT E_TEXT<br>
	,o.TEXT O_TEXT<br>
FROM<br>
	TABLE_B o<br>
		LEFT OUTER JOIN ZUTY_TABLE_B e ON<br>
			e.ZUTY_TESTCASE_NAME = /* @testCaseName */'tetz42.test.AutyDBTest$1#test_sample|prepare'<br>
			AND ( ( o.ID IS NULL AND e.ID IS NULL) OR (o.ID IS NOT NULL AND e.ID IS NOT NULL AND o.ID = e.ID) )<br>
WHERE<br>
	e.ID IS NULL<br>
	OR (<br>
		( ( o.ID IS NULL AND e.ID IS NULL) OR (o.ID IS NOT NULL AND e.ID IS NOT NULL AND o.ID = e.ID) )<br>
		AND (<br>
			NOT( ( o.TEXT IS NULL AND e.TEXT IS NULL) OR (o.TEXT IS NOT NULL AND e.TEXT IS NOT NULL AND o.TEXT = e.TEXT) )<br>
		)<br>
	)<br>
UNION ALL<br>
SELECT<br>
	COALESCE(o.ID, e.ID) A_ID<br>
	,e.TEXT E_TEXT<br>
	,o.TEXT O_TEXT<br>
FROM<br>
	ZUTY_TABLE_B e<br>
		LEFT OUTER JOIN TABLE_B o ON<br>
			( ( o.ID IS NULL AND e.ID IS NULL) OR (o.ID IS NOT NULL AND e.ID IS NOT NULL AND o.ID = e.ID) )<br>
WHERE<br>
	e.ZUTY_TESTCASE_NAME = /* @testCaseName */'tetz42.test.AutyDBTest$1#test_sample|prepare'<br>
	AND o.ID IS NULL<br>
<br>
<br>
testCaseName:atest.expected#sample.ADBTest<br>
	at org.junit.Assert.fail(Assert.java:91)<br>
	at tetz42.test.AutyDB.assertDB(AutyDB.java:154)<br>
	at tetz42.test.AutyDB.assertDB(AutyDB.java:87)<br>
	at sample.ADBTest$1.run(ADBTest.java:38)<br>
	at tetz42.test.AutyDB.useConnection(AutyDB.java:52)<br>
	at sample.ADBTest.atest(ADBTest.java:19)<br>
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)<br>
　　　　　　　　　　　:<br>
　　　　　　　　　　　:<br>
</code></pre>

上記のように、違いが検出された行のプライマリキー、違っていた列の列名及び期待値とテスト結果の値が表示されます。<br>
その後ろに表示されているSELECT文は、データの検証用に使ったクエリーです。<br>
<br>
なお、日付型の列に関しては検証を行わないようにしてあります。これにより、登録日時・更新日時など実行するたびに変わってしまう値があるテーブルであってもテストできるようにしてあります。<br>
<blockquote>※ この日付無視の動作は暫定対応です。将来のバージョンでは変更予定です。</blockquote>

<h2>データの復旧</h2>
最後に、テスト前のデータ復旧についてです。この部分を担当しているソースを上記より抜き出します。<br>
<br>
<pre><code>	@AfterClass<br>
	public static void bye() throws SQLException {<br>
		// 上記5 バックアップされたデータの復元<br>
		restoreAll();<br>
	}<br>
</code></pre>

「@AfterClass」とはJUnit4以降に導入されたアノテーションで、このアノテーションが付与されたメソッドは、テストクラスに実装された全てのテストメソッド(言い換えると、テストケース)が実行された後に、最後に実行されます。<br>
<br>
そして、上記restoreAllメソッドは、テストメソッド内部で実行されたprepareDB, deleteDBなどにより保存されたバックアップデータを全て復元します。<br>
これにより、テスト開始前にdeleteDBメソッドにより削除されていたTABLE_Bの下記データも、元通り復元されていることが確認できるはずです。<br>
<br>
<table border='1'>
<blockquote><tr><th>TABLE_B</th></tr>
<tr><th>id</th><th>text</th></tr>
<tr><td>1</td><td>一献</td></tr>
<tr><td>2</td><td>二献</td></tr>
<tr><td>3</td><td>三献です。</td></tr>
<table></blockquote>


<h1>最後に</h1>
以上、auty4junitの動作について、簡単に説明しました。<br>
上記はversion 0.4.6をベースに説明しておりますが、auty4junitは将来多くのRDBMSをサポートしたり、AutyDBの一部をDBバックアップツールとして独立させたり、また全体をScalaで書き直すなどのバージョンアップが予定されており、インターフェースも大きく変わることが予想されます。<br>
<br>
それでは最後に、auty4junitと一緒に使用すると幸せになれそうなツールを上げておきます。<br>
<br>
<ul><li>WinMerge, P4MergeなどのMergeツール(expectedとfailedのテキスト比較に便利)<br>
</li><li>djUnit(カバレッジ率の測定、テスト漏れの検出)<br>
</li><li>djUnitのVirtual Mock Object, jMockit等(単体テストにMockは必須でしょ！)<br>
</li><li>Oracle SQL Developer, OSqlEdit, CSE等(テストデータ作成・テストデータ編集に。)