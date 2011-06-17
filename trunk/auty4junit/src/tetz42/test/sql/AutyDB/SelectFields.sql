select 
	* 
from 
	USER_TAB_COLUMNS 
where 
	TABLE_NAME = /* SQL(@TABLE) */'T_MST_BUMON' 
order by COLUMN_ID
