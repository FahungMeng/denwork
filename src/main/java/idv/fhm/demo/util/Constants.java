package idv.fhm.demo.util;

public interface Constants {
	String CONFIG_DB_BASE="/tmp/initFolder";
	String CONFIG_DB_NAME="/denden";
	String[] CHECK_CLASS_TABLE=new String[] {"DYNA_CLASS"};

	String INIT_ACCOUNT_TABLE="CREATE TABLE ACCOUNT (\r\n"
			+ "    email       VARCHAR(255)    NOT NULL,\r\n"
			+ "    password    VARCHAR(1024)    NOT NULL,\r\n"
			+ "    isActivate  BOOLEAN         NOT NULL DEFAULT FALSE,\r\n"
			+ "    createdt    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,\r\n"
			+ "    CONSTRAINT pk_account PRIMARY KEY (email)\r\n"
			+ ")";
	String INIT_ACCOUNTHISTORY_TABLE="CREATE TABLE ACCOUNTHISTORY (\r\n"
			+ "    email       VARCHAR(255)    NOT NULL,\r\n"
			+ "    login_date  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,\r\n"
			+ "    CONSTRAINT fk_accounthistory_email FOREIGN KEY (email)\r\n"
			+ "        REFERENCES account (email)\r\n"
			+ "        ON DELETE CASCADE\r\n"
			+ ")";
	String CHECK_ACCOUNT_SQL="SELECT * FROM APP.ACCOUNT WHERE email=${email}";
	String INSERT_ACCOUNT_SQL="INSERT INTO APP.ACCOUNT (email, password, isActivate,createdt) VALUES (${email}, ${password}, false,CURRENT_TIMESTAMP)";
	String ACTIVATE_ACCOUNT_SQL="UPDATE APP.ACCOUNT set isActivate=true WHERE email=${email}";
	String INSERT_LOGIN_HIST_SQL="INSERT INTO ACCOUNTHISTORY (email, login_date) VALUES (${email}, CURRENT_TIMESTAMP)";
	String QUERY_LOGIN_HIST_SQL="SELECT * FROM ACCOUNTHISTORY WHERE email=${email} ORDER BY LOGIN_DATE DESC";
	
}
