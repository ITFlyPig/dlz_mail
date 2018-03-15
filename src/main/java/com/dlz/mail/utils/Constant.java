package com.dlz.mail.utils;

public class Constant {
	public static int ERROR_CODE = -1;//规定所有的方法返回-1表示不正常的执行

	/**
	 * 邮件应该被怎么处理
	 */
	public interface MailShouleStatus{
		int TIMER = 1;//定时
		int SEND = 2;//发送
		int ABANDON = 3;//抛弃

	}

	public interface SQL{
		String GET_TASKS = "select * from mail where status = ?";//获取邮件任务
		String GET_SEND_MAIL = "select * from send_mail";//获取邮件发送者的邮件账户配置
		String GET_TASK_BY_ID = "select * from mail where id = ?";//据id查询对应的邮件任务
		String UPDATE_TASK_STATUS = "update mail set status = ? where id = ?";//更新邮件任务的状态
	}

	public interface FileConfig{
		String CSV_DIR = "/files/csv";
	}

	/**
	 * 邮件任务的状态
	 * 0：新建  1：正在执行  2：执行成功  3：执行失败  4：待发送  5：发送成成功   6：发送失败
	 */
	public interface EmailStatus{
		int NEW = 0;
		int EXECUTE_ING = 1;
		int EXECUTE_SUCCESS = 2;
		int EXECUTE_FAIL = 3;
		int WAIT_SEND = 4;
		int SEND_SUCCESS = 5;
		int SEND_FAIL = 6;
	}

}
