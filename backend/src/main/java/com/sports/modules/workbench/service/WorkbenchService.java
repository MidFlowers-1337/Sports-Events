package com.sports.modules.workbench.service;

import java.util.Map;

/**
 * 角色工作台 Service
 *
 * @author sports
 */
public interface WorkbenchService {

    /**
     * 按当前登录用户角色返回对应的工作台数据
     */
    Map<String, Object> getSummary();
}
