package com.andycugb.cron.invoke;

import com.andycugb.cron.CronJobGenerator;
import com.andycugb.cron.StartUpListener;
import com.andycugb.cron.db.CronJobModel;
import com.andycugb.cron.db.QuartzManager;
import com.andycugb.cron.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jbcheng on 2016-04-20.
 */
@Controller
@RequestMapping(value = "/cron")
public class InvokeController {

    private final CronJobGenerator generator = CronJobGenerator.getInstance();
    @Autowired
    private StartUpListener startUpCron;
    @Autowired
    private QuartzManager quartzManager;

    /**
     * refresh cron list.
     * 
     * @return exec result
     */
    @RequestMapping(value = "/refresh")
    public String refresh() {
        StringBuilder result = new StringBuilder();
        try {
            long start = System.currentTimeMillis();
            this.startUpCron.refreshCron();
            long end = System.currentTimeMillis();
            result.append("Success to refresh crons, cost_time=").append(end - start)
                    .append("ms");
        } catch (Exception e) {
            result.append("Exception occurs while refreshing cron," + e);
        }

        return result.toString();
    }

    /**
     * exec cron by given name.
     * 
     * @param cronName cron name
     * @return exec result
     */
    @RequestMapping(value = "/call/{cronName}")
    public String call(@PathVariable("cronName") String cronName) {
        StringBuilder result = new StringBuilder();
        CronJobModel cron = this.quartzManager.getJobByName(cronName);
        if (cron != null) {
            try {
                long start = System.currentTimeMillis();
                String desc = this.generator.executeJob(cron, "invoke", false);
                long end = System.currentTimeMillis();
                result.append("Success to fire Job[").append(cronName).append("], result={")
                        .append(desc).append("}, cost_time=").append(end - start).append("ms");
            } catch (Exception e) {
                Constant.LOG_CRON.error("Exception occurs while executing job " + cronName, e);
                result.append("Exception occurs while executing job " + cronName + ": "
                        + e.toString());
            }
        } else {
            result.append("There\'s no such loaded cron, cronName=" + cronName);
        }
        return result.toString();
    }
}
