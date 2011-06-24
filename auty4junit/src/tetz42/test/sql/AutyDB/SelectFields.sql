select 
	* 
from 
	USER_TAB_COLUMNS 
where 
	TABLE_NAME = /* @TABLE */'T_MST_BUMON' 
order by COLUMN_ID
