package com.andycugb.cron.service;

import com.andycugb.cron.model.Cron;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jbcheng on 2016-04-27.
 */
@Service(value = "cronService")
public class CronService implements ICronService {
    /**
     * 查找所有任务.
     *
     * @param cron 查询条件
     * @return 任务列表
     */
    public List<Cron> listCronByCond(Cron cron) {
        return null;
    }

    /**
     * 新增任务.
     *
     * @param cron 待添加的任务
     */
    public void addCron(Cron cron) {

    }

    /**
     * 修改任务.
     *
     * @param cron 修改后的任务
     */
    public void update(Cron cron) {

    }

    /**
     * 根据任务ID查询.
     *
     * @param cronId 任务ID
     * @return 任务对象
     */
    public Cron selectCron(String cronId) {
        return null;
    }

    /**
     * 根据指定ID，删除cron任务.
     *
     * @param cronId 任务ID
     */
    public void deleteCron(String cronId) {

    }

    /**
     * 按名称超找指定cron任务.
     *
     * @param cronName cron任务名称
     * @return cron对象
     */
    public Cron selectCronByName(String cronName) {
        return null;
    }

    /**
     * 执行指定cron任务.
     *
     * @param cronName  cron任务名称
     * @param groupInfo cron任务所属系统名称
     * @return 执行结果
     */
    public boolean execCron(String cronName, String groupInfo) {
        return false;
    }
}
