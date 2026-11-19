package com.sports.modules.workbench.controller;

import com.sports.common.result.Result;
import com.sports.modules.workbench.service.WorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 角色工作台 Controller
 * 同一接口按登录用户角色返回管理员、教师、学生三套工作台数据，
 * 角色由服务端从 JWT 中判定，前端不能通过参数切换。
 *
 * @author sports
 */
@Tag(name = "角色工作台", description = "管理员、教师、学生差异化首页数据")
@RestController
@RequestMapping("/api/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @Operation(summary = "获取当前角色工作台数据")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        return Result.success(workbenchService.getSummary());
    }
}
