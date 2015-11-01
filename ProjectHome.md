# auty4junit #
'auty4junit' is an Utility for JUnit.
Now, it contains one method as follows.

## assertEqualsWithFile ##
Assertion method for complicated object.
It provides you an easy test environment.

### How to use ###
write the test code as follows:

```
package tetz42.sample;
import static tetz42.test.Util.*;

class TestClass{

	@Test
	public void test(){
		ComplicatedObject object = Factory.createComplicatedObject();
		assertEqualsWithFile(object, getClass(), "test");
	}
}
```

Perform this test class, and the test will fail.
At the time, the dumped string of parameter object is output to the file.
The file path is :
> expected/tetz42/sample/test.txt
The stacktrace of AssertionFailedError also contains the dumped string as follows.

```
java.lang.AssertionError: No file found. The actual string has been output to the path:
expected/tetz42/sample/test.txt
 The contents is below:
ArrayList[
	Tameshi{
		id = 3
		name = "Hiromitsu Hara"
		age = 31
	}
	Tameshi{
		id = 4
		name = "Hiroko Hara"
		age = 31
	}
]
	at org.junit.Assert.fail(Assert.java:91)
	at tetz42.test.Util.assertEqualsWithFile(Unknown Source)
	at tetz42.clione.SQLManagerTest.findAll_by_1_param(SQLManagerTest.java:106)
		:
		:
```

Check the dumped string.
If it is correct, it means the test is finished.
Because the test class will read the dumped string from the file for the assertion, so the test will be success.