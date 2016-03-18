package com.andycugb.cron.db;

import com.andycugb.cron.util.Constant;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by jbcheng on 2016-03-17.
 */
@Repository
public class CronJobDao {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public CronJobModel getCronByName(Connection conn, String cronName) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        CronJobModel model = null;
        try {
            pst =
                    conn.prepareStatement("SELECT CRON_NAME,SERVICE_NAME,CRON_EXPRESSION,LIMIT_IP,CRON_DESC,ID,FIRE_ON_STARTUP,GROUP_NAME,LAST_RUN_TIME FROM TB_CRON_INI WHERE CRON_NAME=?");
            pst.setString(1, cronName);
            rs = pst.executeQuery();
            if (rs.next()) {
                model = this.createCronJonModel(rs, true);
            }
        } catch (SQLException e) {
            Constant.LOG_CRON.error("Exception occurs while reading data from database" + e);

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON.error("Exception occurs while close ResultSet connection"
                            + e);
                }
            }
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON
                            .error("Exception occurs while close PreparedStatement connection"
                                    + e);
                }
            }
        }
        return model;
    }

    public void updateLastRunTime(Connection conn, String jobName, Timestamp runTime) {
        PreparedStatement pst = null;
        try {
            pst =
                    conn.prepareStatement("UPDATE TB_CRON_INI SET LAST_RUN_TIME =? WHERE CRON_NAME=?");
            pst.setTimestamp(1, runTime);
            pst.setString(2, jobName);
            int size = pst.executeUpdate();
            if (size > 0) {
                Constant.LOG_CRON.debug("Success when update cron+[" + jobName
                        + "] last run time to" + new java.util.Date(runTime.getTime()));
            }
        } catch (SQLException e) {
            Constant.LOG_CRON.error("Exception occurs while reading data from database" + e);

        } finally {
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON
                            .error("Exception occurs while close PreparedStatement connection"
                                    + e);
                }
            }
        }
    }

    private CronJobModel createCronJonModel(ResultSet rst, boolean isSetLastRunTime)
            throws SQLException {
        CronJobModel cronModel = new CronJobModel();
        cronModel.setCronName(rst.getString("CRON_NAME"));
        cronModel.setServiceName(rst.getString("SERVICE_NAME"));
        cronModel.setCronExpression(rst.getString("CRON_EXPRESSION"));
        cronModel.setLimitIp(rst.getString("LIMIT_IP"));
        cronModel.setCronDesc(rst.getString("CRON_DESC"));
        cronModel.setId(rst.getString("ID"));
        cronModel.setFireOnStartUp(rst.getInt("FIRE_ON_STARTUP"));
        cronModel.setGroup(rst.getString("GROUP_NAME"));
        if (isSetLastRunTime) {
            cronModel.setLastRunTime(rst.getTimestamp("LAST_RUN_TIME"));
        }
        return cronModel;
    }
}
