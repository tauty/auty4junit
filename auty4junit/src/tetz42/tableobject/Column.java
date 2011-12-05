package tetz42.tableobject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import tetz42.tableobject.annotation.Title;

public class Column<T> {

	protected static final Set<String> primitiveSet;
	static {
		HashSet<String> map = new HashSet<String>();
		map.add(Object.class.getName());
		map.add(Class.class.getName());
		map.add(Boolean.class.getName());
		map.add(Character.class.getName());
		map.add(Number.class.getName());
		map.add(Byte.class.getName());
		map.add(Short.class.getName());
		map.add(Integer.class.getName());
		map.add(Long.class.getName());
		map.add(Float.class.getName());
		map.add(Double.class.getName());
		map.add(BigInteger.class.getName());
		map.add(BigDecimal.class.getName());
		map.add(AtomicInteger.class.getName());
		map.add(AtomicLong.class.getName());
		map.add(String.class.getName());
		primitiveSet = Collections.unmodifiableSet(map);
	}

	private final Class<T> cls;
	private final String key;
	private T value;

	public Column(Class<T> cls, String key) {
		this.cls = cls;
		this.key = key;
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		if (this.value == null) {
			try {
				this.value = this.cls.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this.value;
	}

	public String getKey() {
		return key;
	}

	@SuppressWarnings("unchecked")
	public void add(T another) {
		if (another == null) {
			return;
		} else if (value == null) {
			this.value = another;
		} else if (cls == Integer.class) {
			int iValue = (Integer) value;
			int iAnother = (Integer) another;
			value = (T) new Integer(iValue + iAnother);
		} else {
			value = (T) ("" + value + another);
		}
	}

	public Iterable<Column<String>> each() {
		return new Iterable<Column<String>>() {

			@Override
			public Iterator<Column<String>> iterator() {
				if (primitiveSet.contains(cls.getName())) {
					return new Iterator<Column<String>>() {

						boolean returned = false;

						@Override
						public boolean hasNext() {
							return !returned;
						}

						@Override
						public Column<String> next() {
							Column<String> column = new Column<String>(
									String.class, key);
							if (value == null)
								column.set("");
							else
								column.set("" + value);
							returned = true;
							return column;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
									"'remove' is not supported.");
						}
					};
				} else {

					return new Iterator<Column<String>>() {

						int index = 0;
						Field[] fields = cls.getDeclaredFields();

						@Override
						public boolean hasNext() {
							return index < fields.length;
						}

						@Override
						public Column<String> next() {
							String key = fields[index].getName();
							Title def = fields[index].getAnnotation(Title.class);
							if(def != null)
								key = def.value();
							Column<String> column = new Column<String>(
									String.class, key);
							try {
								if (value == null)
									column.set("");
								else
									column.set("" + fields[index].get(value));
							} catch (Exception e) {
								// TODO Better Exception
								throw new RuntimeException(e);
							}
							index++;
							return column;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
									"'remove' is not supported.");
						}
					};
				}
			}
		};
	}
}
