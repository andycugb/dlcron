package com.andycugb.cron.invoke;

import com.andycugb.cron.CronJobGenerator;
import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.util.Constant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jbcheng on 2016-04-20.
 */
public class InvokeServlet extends HttpServlet {
    private QuartzManager quartzManager = QuartzManager.getInstance();
    private CronJobGenerator generator = CronJobGenerator.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder res = new StringBuilder();

        try {
            String out = request.getParameter("method");
            if ("call".equals(out)) {
                String cronName = request.getParameter("cronName");
                CronJobModel cron = this.quartzManager.getJobByName(cronName);
                if (cron != null) {
                    try {
                        long start = System.currentTimeMillis();
                        String result = this.generator.executeJob(cron, "invoke", false);
                        long end = System.currentTimeMillis();
                        res.append("Success to fire Job[").append(cronName).append("], result={")
                                .append(result).append("}, cost_time=").append(end - start).append("ms");
                    } catch (Throwable e) {
                        Constant.LOG_CRON.error("Exception occurs while executing job "
                                + cronName, e);
                        res.append("Exception occurs while executing job " + cronName + ": "
                                + e.toString());
                    }
                } else {
                    res.append("There\'s no such loaded cron, cronName=" + cronName);
                }
            } else {
                res.append("There\'s no such method, method=" + out);
            }
        } catch (Exception e) {
            Constant.LOG_CRON.error(e);
            res.append(e);
        }

        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.println(res.toString());
        pw.flush();
        pw.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
