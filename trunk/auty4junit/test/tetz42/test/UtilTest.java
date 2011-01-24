/*
 * Copyright 2011 tetsuo.ohta[at]gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tetz42.test;

import static tetz42.test.Util.*;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author tetz
 */
public class UtilTest {
	
	Map<String, Object> map;
	
	@Before
	public void setUp(){
		map = new HashMap<String, Object>();
		map.put("tako", "octopus");
		map.put("ika", "squid");
		map.put("namako", "sea cucumber");
		map.put("2011", 2011);
		map.put("Math.PI", Math.PI);
	}
	
	@Test
	public void first(){
		assertEqualsWithFile(map, getClass(), "first");
	}

	@Test
	public void second(){
		assertEqualsWithFile(map, getClass(), "second");
	}

	@Test
	public void third(){
		assertEqualsWithFile(map, getClass(), "third");
	}

	@Test
	public void fourth(){
		assertEqualsWithFile(map, getClass(), "fourth");
	}

}
