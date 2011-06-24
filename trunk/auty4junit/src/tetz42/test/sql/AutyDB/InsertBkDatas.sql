INSERT INTO /* %STR(@BK_TABLE) */ZUTY_BK_T_MST_BUMON
select
	/* @bkName */'bk20110623_1814.234'
	,t.*
from
	/* %STR(@TABLE) */T_MST_BUMON t
