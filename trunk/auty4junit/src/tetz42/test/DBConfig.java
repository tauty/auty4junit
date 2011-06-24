package tetz42.test;

import java.util.ResourceBundle;

public class DBConfig {

	public static final String DB_CONNECT_STRING;
	public static final String DB_CONNECT_USER;
	public static final String DB_CONNECT_PASS;

	static {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("auty");
			DB_CONNECT_STRING = bundle.getString("DB_CONNECT_STRING");
			DB_CONNECT_USER = bundle.getString("DB_CONNECT_USER");
			DB_CONNECT_PASS = bundle.getString("DB_CONNECT_PASS");
		} catch (Throwable e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
