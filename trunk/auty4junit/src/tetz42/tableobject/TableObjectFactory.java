package tetz42.tableobject;

import java.util.HashMap;
import java.util.Map;

public class TableObjectFactory {

	public static <T1, T2, T3> TableObject3<T1, T2, T3> create(Class<T1> cls1,
			Class<T2> cls2, Class<T3> cls3) {
		return new TableObject3<T1, T2, T3>(cls1, cls2, cls3);
	}

	public static class Holder<T> {
		private final Class<T> cls;
		private T value;

		public Holder(Class<T> cls) {
			this.cls = cls;
		}

		public void set(T value) {
			this.value = value;
		}

		public T get() {
			if (this.value == null) {
				try {
					this.value = this.cls.newInstance();
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
			return this.value;
		}
	}

	// public class Row<T1, T2, T3> {
	// private final Class<T1> cls1;
	// private final Class<T2> cls2;
	// private final Class<T3> cls3;
	//
	// private Map<Object, Holder<T1>> map1 = new HashMap<Object, Holder<T1>>();
	// private Map<Object, Holder<T2>> map2 = new HashMap<Object, Holder<T2>>();
	// private Map<Object, Holder<T3>> map3 = new HashMap<Object, Holder<T3>>();
	//
	// public Row(Class<T1> cls1, Class<T2> cls2, Class<T3> cls3) {
	// this.cls1 = cls1;
	// this.cls2 = cls2;
	// this.cls3 = cls3;
	// }
	//
	// public Holder<T1> getAs1(String key) {
	// if(!map1.containsKey(key)){
	// map1.put(key, new Holder<T1>(cls1));
	// }
	// return map1.get(key);
	// }
	//
	// public Holder<T1> getAs1(String key) {
	// if(!map1.containsKey(key)){
	// map1.put(key, new Holder<T1>(cls1));
	// }
	// return map1.get(key);
	// }
	// }

	public static class Row {

		private Map<Object, Holder<Object>> map1 = new HashMap<Object, Holder<Object>>();

		@SuppressWarnings("unchecked")
		public <T> Holder<T> get(Class<T> clazz, String key) {
			if (!map1.containsKey(key)) {
				map1.put(key, (Holder<Object>) new Holder<T>(clazz));
			}
			return (Holder<T>) map1.get(key);
		}
	}

	public static class TableObject3<T1, T2, T3> {

		private final Class<T1> cls1;
		private final Class<T2> cls2;
		private final Class<T3> cls3;

		public TableObject3(Class<T1> cls1, Class<T2> cls2, Class<T3> cls3) {
			this.cls1 = cls1;
			this.cls2 = cls2;
			this.cls3 = cls3;
		}

		public void setHeaderAs1(String... string) {
			// TODO Auto-generated method stub

		}

		public void setHeaderAs2(String... string) {
			// TODO Auto-generated method stub

		}

		public void setHeaderAs3(String... string) {
			// TODO Auto-generated method stub

		}

		public void newRow() {
			// TODO Auto-generated method stub

		}
		
		public Holder<T1> getAs1(String key) {
			return new Row().get(cls1, key);
		}

		public Holder<T2> getAs2(String key) {
			return new Row().get(cls2, key);
		}

		public Holder<T3> getAs3(String key) {
			return new Row().get(cls3, key);
		}

	}

}
