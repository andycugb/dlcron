package com.andycugb.cron.db;

import com.andycugb.cron.util.Constant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Created by jbcheng on 2016-03-17.
 */
@Repository
public class CronJobDao {
    @Autowired
    private DataSource dataSource;

    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * get crons by group.
     * 
     * @param group group name
     * @return cron model list
     */
    public List<CronJobModel> getAllCronByGroup(String group) {
        List<CronJobModel> models = new ArrayList<CronJobModel>();
        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet rst = null;

        try {
            connection = getDataSource().getConnection();
            String sql;
            if (StringUtils.isBlank(group)) {
                sql =
                        "select ID,CRON_NAME, SERVICE_NAME, CRON_EXPRESSION, LIMIT_IP, CRON_DESC, FIRE_ON_STARTUP,"
                                + " GROUP_NAME from tb_cron_ini  where GROUP_NAME is null or GROUP_NAME=\'\'";
                Constant.LOG_CRON.debug("[getAllCron] select cron sql :" + sql);
                pst = connection.prepareStatement(sql);
            } else {
                sql =
                        "select ID, CRON_NAME, SERVICE_NAME, CRON_EXPRESSION, LIMIT_IP, CRON_DESC, FIRE_ON_STARTUP,"
                                + " GROUP_NAME from tb_cron_ini  where GROUP_NAME=? ";
                pst = connection.prepareStatement(sql);
                Constant.LOG_CRON.debug("[getAllCronByGroup] select cron sql :" + sql);
                pst.setString(1, group);
            }

            rst = pst.executeQuery();

            while (rst.next()) {
                CronJobModel e1 = this.createCronJonModel(rst, false);
                models.add(e1);
                Constant.LOG_CRON.debug("[getAllCronByGroup] select a cron from DB :"
                        + e1.toString());
            }

            Constant.LOG_CRON
                    .debug("[getAllCronByGroup] select some cron from DB. [cronModelListSize = "
                            + models.size() + "].");
        } catch (Exception e) {
            Constant.LOG_CRON.error("Exception occurs while reading data from database" + e);
        } finally {
            if (rst != null) {
                try {
                    rst.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON.error("error when close result set," + e);
                }
            }

            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON.error("error when close prepared statement," + e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    Constant.LOG_CRON.error("error when close connection," + e);
                }
            }

        }

        return models;
    }

    /**
     * get cron by name.
     * 
     * @param conn datasource connection
     * @param cronName cron name
     * @return cron model
     */
    public CronJobModel getCronByName(Connection conn, String cronName) {
        PreparedStatement pst = null;
        ResultSet rs = null;
        CronJobModel model = null;
        try {
            pst =
                    conn.prepareStatement("SELECT CRON_NAME,SERVICE_NAME,CRON_EXPRESSION,LIMIT_IP,"
                            + "CRON_DESC,ID,FIRE_ON_STARTUP,GROUP_NAME,LAST_RUN_TIME FROM TB_CRON_INI WHERE CRON_NAME=?");
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

    /**
     * update cron`s last run time.
     * 
     * @param conn datasource connection
     * @param jobName cron name
     * @param runTime running time
     */
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
