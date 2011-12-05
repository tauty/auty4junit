package tetz42.tableobject;

import tetz42.tableobject.tables.TableObject3;
import tetz42.tableobject.tables.TableObject4;

public class TableObjectFactory {

	public static <T1, T2, T3> TableObject3<T1, T2, T3> create(Class<T1> cls1,
			Class<T2> cls2, Class<T3> cls3) {
		return new TableObject3<T1, T2, T3>(cls1, cls2, cls3);
	}

	public static <T1, T2, T3, T4> TableObject4<T1, T2, T3, T4> create(Class<T1> cls1,
			Class<T2> cls2, Class<T3> cls3, Class<T4> cls4) {
		return new TableObject4<T1, T2, T3, T4>(cls1, cls2, cls3, cls4);
	}

}
