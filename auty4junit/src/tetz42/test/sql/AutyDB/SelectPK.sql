select 
	COLUMN_NAME
from 
	USER_CONSTRAINTS c
	,USER_CONS_COLUMNS uc
where 
	c.TABLE_NAME = /* @TABLE */'T_MST_BUMON'
	and c.CONSTRAINT_TYPE = 'P'
	and c.CONSTRAINT_NAME = uc.CONSTRAINT_NAME
