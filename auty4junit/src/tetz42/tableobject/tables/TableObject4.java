package tetz42.tableobject.tables;

import tetz42.tableobject.Column;

public class TableObject4<T1, T2, T3, T4> extends TableObject3<T1, T2, T3> {

	private final Class<T4> cls4;

	public TableObject4(Class<T1> cls1, Class<T2> cls2, Class<T3> cls3,
			Class<T4> cls4) {
		super(cls1, cls2, cls3);
		this.cls4 = cls4;
	}

	public void setHeaderAs4(String... keys) {
		for (String key : keys)
			headerClsMap.put(key, cls4);
	}

	public Column<T4> getAs4(String key) {
		return currentRow.get(cls4, key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TableObject4<T1, T2, T3, T4> row(int index) {
		return (TableObject4<T1, T2, T3, T4>) super.row(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TableObject4<T1, T2, T3, T4> row(String rowKey) {
		return (TableObject4<T1, T2, T3, T4>) super.row(rowKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TableObject4<T1, T2, T3, T4> tail() {
		return (TableObject4<T1, T2, T3, T4>) super.tail();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TableObject4<T1, T2, T3, T4> tail(int index) {
		return (TableObject4<T1, T2, T3, T4>) super.tail(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TableObject4<T1, T2, T3, T4> tail(String rowKey) {
		return (TableObject4<T1, T2, T3, T4>) super.tail(rowKey);
	}
}
