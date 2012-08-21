package sample;

import static tetz42.test.Auty.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ATest {

	static class Sample {
		int intField1;
		int intField2;
		String strField1;
		String strField2;
		Map<String, String> mapField = new HashMap<String, String>();
	}

	@Test
	public void atest() {
		Sample sample = new Sample();
		sample.intField1 = 101;
		sample.intField2 = 102;
		sample.strField1 = "string1";
		sample.strField2 = "文字列２";
		sample.mapField.put("key1", "value1");
		sample.mapField.put("key2", "value2");
		sample.mapField.put("key3", "value3");

		assertEqualsWithFile(sample, getClass(), "atest");
	}
}
