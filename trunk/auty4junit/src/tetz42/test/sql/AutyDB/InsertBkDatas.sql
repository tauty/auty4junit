INSERT INTO /* %STR(@BK_TABLE) */ZUTY_BK_T_MST_BUMON (
	ZUTY_TESTCASE_NAME,	/* %STR(@fields) */BUMON_CD
)
select
	/* @bkName */'bk20110623_1814.234', /* %STR(@fields) */BUMON_CD
from
	/* %STR(@TABLE) */T_MST_BUMON
