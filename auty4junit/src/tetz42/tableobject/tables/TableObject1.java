package tetz42.tableobject.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tetz42.tableobject.Column;
import tetz42.tableobject.Row;

public class TableObject1<T1> implements Cloneable {
	private final Class<T1> cls1;
	protected final LinkedHashMap<String, Class<?>> headerClsMap;
	private final List<Row> rowList;
	private final Map<String, Integer> indexMap;
	private final List<Row> tailRowList;
	private final Map<String, Integer> tailIndexMap;

	protected Row currentRow;

	public TableObject1(Class<T1> cls1) {
		this.cls1 = cls1;
		this.headerClsMap = new LinkedHashMap<String, Class<?>>();
		this.rowList = new ArrayList<Row>();
		this.indexMap = new HashMap<String, Integer>();
		this.tailRowList = new ArrayList<Row>();
		this.tailIndexMap = new HashMap<String, Integer>();
	}

	public void setHeader(String... keys) {
		setHeaderAs1(keys);
	}

	public void setHeaderAs1(String... keys) {
		for (String key : keys)
			headerClsMap.put(key, cls1);
	}

	public Column<T1> get(String key) {
		return getAs1(key);
	}

	public Column<T1> getAs1(String key) {
		return currentRow.get(cls1, key);
	}

	public void newRow() {
		setRow(rowList.size());
	}

	public void setRow(int index) {
		currentRow = fillRow(rowList, index);
	}

	public void setRow(String rowKey) {
		if (!indexMap.containsKey(rowKey)) {
			indexMap.put(rowKey, rowList.size());
			fillRow(rowList, rowList.size());
		}
		currentRow = rowList.get(indexMap.get(rowKey));
	}

	public TableObject1<T1> row(int index) {
		TableObject1<T1> clone = this.clone();
		clone.currentRow = fillRow(rowList, index);
		return clone;
	}

	public TableObject1<T1> row(String rowKey) {
		if (!indexMap.containsKey(rowKey)) {
			indexMap.put(rowKey, rowList.size());
		}
		return row(indexMap.get(rowKey));
	}

	public void newTailRow() {
		setTailRow(tailRowList.size());
	}

	public void setTailRow(int index) {
		currentRow = fillRow(tailRowList, index);
	}

	public void setTailRow(String rowKey) {
		if (!tailIndexMap.containsKey(rowKey)) {
			tailIndexMap.put(rowKey, tailRowList.size());
			fillRow(tailRowList, tailRowList.size());
		}
		currentRow = tailRowList.get(tailIndexMap.get(rowKey));
	}

	public TableObject1<T1> tail() {
		return tail(0);
	}

	public TableObject1<T1> tail(int index) {
		TableObject1<T1> clone = this.clone();
		clone.currentRow = fillRow(tailRowList, index);
		return clone;
	}

	public TableObject1<T1> tail(String rowKey) {
		if (!tailIndexMap.containsKey(rowKey)) {
			tailIndexMap.put(rowKey, tailRowList.size());
		}
		return tail(tailIndexMap.get(rowKey));
	}

	public List<Column<String>> headers() {
		ArrayList<Column<String>> list = new ArrayList<Column<String>>();
		for(Column<String> col:rowList.get(0).each())
			list.add(col);
		return list;
	}

	public List<Row> rows() {
		ArrayList<Row> list = new ArrayList<Row>();
		list.addAll(rowList);
		list.addAll(tailRowList);
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TableObject1<T1> clone() {
		try {
			return (TableObject1<T1>) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO better exception
			throw new RuntimeException(e);
		}
	}

	private Row fillRow(List<Row> rowList, int index) {
		for (int i = rowList.size(); i <= index; i++) {
			rowList.add(genRow());
		}
		return rowList.get(index);
	}

	private Row genRow() {
		Row row = new Row();
		for (Map.Entry<String, Class<?>> e : headerClsMap.entrySet())
			row.get(e.getValue(), e.getKey());
		return row;
	}

}
