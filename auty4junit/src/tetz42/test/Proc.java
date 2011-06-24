package tetz42.test;

import java.sql.Connection;

public interface Proc {
	
	void run(Connection con) throws Exception;

}
