package com.andycugb.cron.web;

import com.andycugb.cron.model.Cron;
import com.andycugb.cron.service.ICronService;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jbcheng on 2016-04-26.
 */
@Controller
@RequestMapping(value = "/cron")
public class CronController {
    @Autowired
    private ICronService cronService;
    private PropertyUtil property = new PropertyUtil("config", Locale.CHINA);

    /**
     * load cron list by given condition.
     * @param cronQueryCond query condition
     * @param map model map
     * @return exec result
     */
    @RequestMapping(value = "/list.htm")
    public String listAll(Cron cronQueryCond, ModelMap map) {
        try {
            String cronGroupNames = property.getStringProperty("cronGroupName");
            String cronGroupNamesConfirm = property.getStringProperty("cronGroupNamesConfirm");
            map.put("cronGroupNames", cronGroupNames);
            map.put("cronGroupNamesConfirm", cronGroupNamesConfirm);
            Map<String, String> cronExecMachine = new HashMap<String, String>();
            String[] cronGroup = cronGroupNames.split(";");
            String[] envSuffix = {"_machines", "_confirm_machines"};

            for (String group : cronGroup) {
                for (String suffix : envSuffix) {
                    String configKey = group + suffix;
                    String config = property.getStringProperty(configKey);
                    if (StringUtils.isNotBlank(config)) {
                        cronExecMachine.put(configKey, config);
                    }
                }
            }
            List<Cron> cronList = cronService.listCronByCond(cronQueryCond);
            for (Cron cron : cronList) {
                cron.setExecMachines(cronExecMachine.get(cron.getGroupName() + envSuffix[0]));
                cron.setExecMachineConfirm(cronExecMachine.get(cron.getGroupName() + envSuffix[1]));
            }
            map.addAttribute("cronList", cronList);
        } catch (Exception e) {
            Constant.LOG_CRON.error("error when load cron list:" + e);
        }
        map.put("cronQueryCond",cronQueryCond);
        return "cron/list";
    }

    /**
     * update given cron by cron id.
     * @param id cron id
     * @param map model map
     * @return exec result
     */
    @RequestMapping(value = "/update.htm")
    public String update(String id,ModelMap map) {
        Cron cron = cronService.selectCron(id);
        map.addAttribute("cron",cron);

        String cronGroupNames = property.getStringProperty("cronGroupNames");
        if (StringUtils.isNotBlank(cronGroupNames)) {
            map.put("cronGroupNames",cronGroupNames);
        }
        return "cron/update";
    }
}
