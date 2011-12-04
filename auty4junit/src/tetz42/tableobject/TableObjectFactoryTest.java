package tetz42.tableobject;

import org.junit.Test;

import tetz42.tableobject.TableObjectFactory.TableObject3;

public class TableObjectFactoryTest {
	
	@Test
	public void tableObj3() {
		TableObject3<String, ColumnSet1, ColumnSet2> to3 = TableObjectFactory
				.create(String.class, ColumnSet1.class, ColumnSet2.class);
		
		to3.setHeaderAs1("Header1", "Header2");
		to3.setHeaderAs2("Header3", "Header4", "Header5");
		to3.setHeaderAs3("LastColumn");
		
		to3.newRow();
		to3.getAs1("Header1").set("111");
		to3.getAs1("Header2").set("222");
		to3.getAs2("Header3").get().column1 = "333";
		to3.getAs2("Header3").get().column2 = "444";
		to3.getAs2("Header3").get().column3 = 10;
		
//		to3.tailRow().getAs1("Header1").set("-------");
//		to3.tailRow().getAs1("Header2").set("Summary");
//		to3.tailRow().getAs1("Header3").get().column3 += 10;

	}

	private static class ColumnSet1 {
		String column1;
		String column2;
		int column3;
	}

	private static class ColumnSet2 {
		int column1;
		int column2;
		String column3;
	}

}
