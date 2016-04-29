package com.andycugb.cron.web;

import com.andycugb.cron.model.Cron;
import com.andycugb.cron.service.ICronService;
import com.andycugb.cron.util.Constant;
import com.andycugb.cron.util.HttpUtil;
import com.andycugb.cron.util.IpUtil;
import com.andycugb.cron.util.PropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     *
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
        map.put("cronQueryCond", cronQueryCond);
        return "cron/list";
    }

    /**
     * update given cron by cron id.
     *
     * @param id cron id
     * @param map model map
     * @return redirect view
     */
    @RequestMapping(value = "/update.htm")
    public String update(String id, ModelMap map) {
        if (StringUtils.isBlank(id)) {
            map.put("opResult", "请选择一个合法的Cron对象！");
            return "cron/update";
        }
        Cron cron = cronService.selectCron(id);
        map.addAttribute("cron", cron);

        String cronGroupNames = property.getStringProperty("cronGroupNames");
        if (StringUtils.isNotBlank(cronGroupNames)) {
            map.put("cronGroupNames", cronGroupNames);
        }
        return "cron/update";
    }

    /**
     * update cron`s attribute.
     *
     * @param cron cron been modified
     * @param map model map
     * @return cron list page
     */
    @RequestMapping(value = "saveUpdate.htm")
    public String saveUpdate(Cron cron, ModelMap map) {
        if (!limitIpVerify(cron.getLimitIp())) {
            map.put("opResult", "Cron更新失败，IP限制字段输入有误！");
            return "cron/update";
        }
        Cron temp = cronService.selectCron(cron.getId());
        if (null != temp && cron.getCronName().equals(temp.getCronName())) {
            map.put("opResult", "你输入的Cron名称已存在，请重新输入！");
            return "cron/update";
        }
        if (StringUtils.isBlank(cron.getGroupName())) {
            cron.setGroupName(null);
        }
        cronService.update(cron);
        return "redirect:/cron/list.htm";
    }

    /**
     * add new cron method.
     *
     * @param map model map param from client request
     * @return redirect view
     */
    @RequestMapping(value = "add.htm")
    public String add(ModelMap map) {
        if (null == map.get("cron")) {
            map.put("opResult", "参数有误，请重新填写！");
            return "cron/add";
        }
        String cronGroupNames = property.getStringProperty("cronGroupNames");
        if (StringUtils.isNotBlank(cronGroupNames)) {
            map.put("cronGroupNames", cronGroupNames);
        }
        return "cron/add";
    }

    /**
     * add new cron.
     *
     * @param cron cron been added
     * @param map model map
     * @return cron list page
     */
    @RequestMapping(value = "saveAdd.htm")
    public String saveAdd(Cron cron, ModelMap map) {
        if (!limitIpVerify(cron.getLimitIp())) {
            map.put("opResult", "Cron添加失败，IP限制字段输入有误！");
            return add(map);
        }
        Cron temp = cronService.selectCron(cron.getId());
        if (null != temp && cron.getCronName().equals(temp.getCronName())) {
            map.put("opResult", "你输入的Cron名称已存在，请重新输入！");
            return "cron/update";
        }
        if (StringUtils.isBlank(cron.getGroupName())) {
            cron.setGroupName(null);
        }
        cronService.addCron(cron);
        return "redirect:/cron/list.htm";
    }

    /**
     * delete one cron by given cron id.
     *
     * @param id cron id
     * @param map model map
     * @return cron list page
     */
    @RequestMapping(value = "delete.htm")
    public String detele(@RequestParam("id") String id, ModelMap map) {
        if (StringUtils.isBlank(id)) {
            map.put("opResult", "请选择合法的Cron对象");
            return listAll(new Cron(), map);
        }
        cronService.deleteCron(id);
        return listAll(new Cron(), map);
    }

    /**
     * exec given cron job.
     *
     * @param url job url
     * @param groupInfo group of job
     * @param map model map
     * @return exec result
     */
    @RequestMapping(value = "executeCron.htm")
    public String execCron(@RequestParam("cronRefreshUrlInfo") String url, String groupInfo,
            ModelMap map) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(groupInfo)) {
            map.put("opResult", "参数有误，请选合法的Cron任务执行！");
            return listAll(new Cron(), map);
        }
        url = url.replace(".html", ".htm");
        String limitIp = url.substring(0, url.indexOf("/bg"));
        String cronExcludeIp = url.substring(url.indexOf("/bg"));
        String[] cronIps;

        if (limitIp.equals(Constant.CRON_DO_IP)) {
            String cronIpsConfig = property.getStringProperty(groupInfo);
            cronIpsConfig = cronIpsConfig.substring(cronIpsConfig.indexOf(";") + 1);
            cronIps = cronIpsConfig.split(";");
        } else {
            cronIps = limitIp.split(";");
        }
        String[] cronExecUrls = new String[cronIps.length];
        for (int len = cronIps.length, index = 0; index < len; index++) {
            cronExecUrls[index] = "http://" + cronIps[index] + cronExcludeIp;
        }
        String execResult = cronExecution(cronExecUrls);
        map.put("opResult", execResult);
        return listAll(new Cron(), map);
    }

    /**
     * refresh given cron by name.
     *
     * @param cronGroupName cron name
     * @param map model map
     * @return cron list page
     */
    @RequestMapping(value = "refresh.htm")
    public String refresh(String cronGroupName, ModelMap map) {
        String cronIp = property.getStringProperty(cronGroupName + "_machines");
        cronIp = cronIp.substring(cronIp.indexOf(";") + 1);
        String[] cronIps = cronIp.split(";");

        String[] cronUrls = new String[cronIps.length];
        for (int len = cronIps.length, index = 0; index < len; index++) {
            cronUrls[index] = "http://" + cronIps[index] + "/bg/cron/refresh.htm";
        }
        String execResult = cronExecution(cronUrls);
        map.put("opResult", execResult);
        return listAll(new Cron(), map);
    }

    /**
     * exec given cron by name and group.
     *
     * @param cronName cron name
     * @param groupInfo group of cron
     * @param cronExectuionIp exec machine ip
     * @return exec result map
     */
    public Map<String, Object> execCronByNameAndGroup(String cronName, String groupInfo,
            String cronExectuionIp) {
        Map<String, Object> result = new HashMap<String, Object>();
        String[] cronIps;
        if (StringUtils.isBlank(cronExectuionIp) || Constant.CRON_DO_IP.equals(cronExectuionIp)) {
            String cronIp = property.getStringProperty(groupInfo + "_machines");
            cronIp = cronIp.substring(cronIp.indexOf(";") + 1);
            cronIps = cronIp.split(";");
        } else {
            cronIps = new String[1];
            cronIps[0] = cronExectuionIp;
        }
        String actionSuffix = ".htm";
        String[] cronExecutionUrls = new String[cronIps.length];
        for (int len = cronIps.length, index = 0; index < len; index++) {
            cronExecutionUrls[index] =
                    "http://" + cronIps[index] + "/bg/cron/call/" + cronName + actionSuffix;
        }
        String execResult = cronExecution(cronExecutionUrls);
        result.put("retCode", 200);
        result.put("retDesc", "操作成功");
        result.put("opResult", execResult);
        return result;
    }

    // invoke given urlcrons
    private String cronExecution(String[] urls) {
        Object statusCode;
        String execResult = "";
        Map<String, Object> execStatus;
        for (int len = urls.length, index = 0; index < len; index++) {
            execStatus = HttpUtil.execRequest(urls[index]);
            statusCode = execStatus.get("status");
            if (statusCode != null && (Integer) statusCode == HttpStatus.SC_OK) {
                execResult = execResult + urls[index] + "调用成功!详情如下：\n";
                execResult = execResult + String.valueOf(execStatus.get("entity")) + "\n";
            } else {
                execResult = execResult + urls[index] + "调用失败：" + statusCode;
                Constant.LOG_CRON.info(execResult);
            }
        }
        return execResult;
    }

    // ip pattern check
    private boolean limitIpVerify(String cronIp) {
        if (StringUtils.isBlank(cronIp)) {
            return false;
        }
        String[] cronIps = cronIp.split(";");
        if ((cronIp.indexOf(Constant.CRON_DO_IP) != -1 || cronIp.indexOf(Constant.CRON_LIMIT_IP) != -1)
                && cronIps.length > 1) {
            return false;
        }
        for (int len = cronIps.length, index = 0; index < len; index++) {
            if (!IpUtil.ipAddressVerify(cronIps[index])
                    && !StringUtils.equals(Constant.CRON_DO_IP, cronIps[index])) {
                return false;
            }
        }
        return true;
    }

}
