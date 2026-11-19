package com.sports.modules.workbench.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sports.modules.event.entity.Event;
import com.sports.modules.event.service.EventService;
import com.sports.modules.event.vo.EventVO;
import com.sports.modules.news.service.NewsService;
import com.sports.modules.registration.entity.Registration;
import com.sports.modules.registration.service.RegistrationService;
import com.sports.modules.result.entity.CompetitionResult;
import com.sports.modules.result.service.CompetitionResultService;
import com.sports.modules.schedule.entity.Schedule;
import com.sports.modules.schedule.service.ScheduleService;
import com.sports.modules.system.service.SysUserService;
import com.sports.modules.workbench.service.WorkbenchService;
import com.sports.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色工作台 Service 实现
 * 所有统计口径统一按当前登录人角色取值，学生仅能拿到与本人相关的数据。
 *
 * @author sports
 */
@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl implements WorkbenchService {

    private static final int EVENT_ONGOING = 3;
    private static final int REG_PENDING = 1;
    private static final int REG_APPROVED = 2;

    /** 工作台列表默认展示条数 */
    private static final int PREVIEW_SIZE = 5;
    private static final int EVENT_PREVIEW_SIZE = 6;

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CompetitionResultService competitionResultService;
    private final ScheduleService scheduleService;
    private final NewsService newsService;
    private final SysUserService sysUserService;

    @Override
    public Map<String, Object> getSummary() {
        String role = SecurityUtils.getRole();
        LocalDate today = LocalDate.now();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", role);
        data.put("roleName", roleName(role));
        data.put("today", today.toString());

        switch (role == null ? "" : role) {
            case "ADMIN" -> fillAdmin(data, today);
            case "TEACHER" -> fillTeacher(data, today);
            default -> fillStudent(data);
        }
        return data;
    }

    private void fillAdmin(Map<String, Object> data, LocalDate today) {
        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("totalEvents", eventService.count());
        cards.put("ongoingEvents", eventService.count(new LambdaQueryWrapper<Event>()
                .eq(Event::getStatus, EVENT_ONGOING)));
        cards.put("totalRegistrations", registrationService.count());
        cards.put("pendingRegistrations", countRegistrationsByStatus(REG_PENDING));
        cards.put("totalResults", competitionResultService.count());
        cards.put("totalUsers", sysUserService.count());
        cards.put("todaySchedules", countSchedules(today));

        data.put("cards", cards);
        data.put("todaySchedules", previewSchedules(today));
        data.put("recentPendingRegistrations",
                registrationService.pageRegistrations(1, PREVIEW_SIZE, null, null, REG_PENDING).getRecords());
    }

    private void fillTeacher(Map<String, Object> data, LocalDate today) {
        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("pendingRegistrations", countRegistrationsByStatus(REG_PENDING));
        cards.put("todaySchedules", countSchedules(today));
        cards.put("pendingResults", countPendingResults());
        cards.put("totalResults", competitionResultService.count());

        data.put("cards", cards);
        data.put("todaySchedules", previewSchedules(today));
        data.put("recentPendingRegistrations",
                registrationService.pageRegistrations(1, PREVIEW_SIZE, null, null, REG_PENDING).getRecords());
    }

    private void fillStudent(Map<String, Object> data) {
        Long userId = SecurityUtils.getUserId();

        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("myRegistrations", registrationService.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .ne(Registration::getStatus, 4)));
        cards.put("approvedRegistrations", registrationService.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getStatus, REG_APPROVED)));
        cards.put("pendingRegistrations", registrationService.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, userId)
                .eq(Registration::getStatus, REG_PENDING)));
        cards.put("myResults", competitionResultService.count(new LambdaQueryWrapper<CompetitionResult>()
                .eq(CompetitionResult::getUserId, userId)));
        cards.put("goldMedals", countMedals(userId, 1));
        cards.put("silverMedals", countMedals(userId, 2));
        cards.put("bronzeMedals", countMedals(userId, 3));

        List<EventVO> available = eventService.getAvailableEvents();
        if (available != null && available.size() > EVENT_PREVIEW_SIZE) {
            available = available.subList(0, EVENT_PREVIEW_SIZE);
        }

        data.put("cards", cards);
        data.put("availableEvents", available == null ? List.of() : available);
        data.put("myRecentRegistrations",
                registrationService.pageMyRegistrations(1, PREVIEW_SIZE, null, null).getRecords());
        data.put("myRecentResults",
                competitionResultService.pageMyResults(1, PREVIEW_SIZE, null).getRecords());
        data.put("recentNews", newsService.pageNewsVO(1, PREVIEW_SIZE, null, null, null).getRecords());
    }

    private long countRegistrationsByStatus(int status) {
        return registrationService.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getStatus, status));
    }

    private long countSchedules(LocalDate date) {
        return scheduleService.count(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getMatchDate, date));
    }

    private List<?> previewSchedules(LocalDate date) {
        return scheduleService.pageSchedulesVO(1, PREVIEW_SIZE, null, date.toString()).getRecords();
    }

    private long countMedals(Long userId, int ranking) {
        return competitionResultService.count(new LambdaQueryWrapper<CompetitionResult>()
                .eq(CompetitionResult::getUserId, userId)
                .eq(CompetitionResult::getRanking, ranking));
    }

    /**
     * 待录入成绩：已通过审核但尚未录入成绩的报名数量
     */
    private long countPendingResults() {
        List<Registration> approved = registrationService.list(new LambdaQueryWrapper<Registration>()
                .select(Registration::getId)
                .eq(Registration::getStatus, REG_APPROVED));
        if (approved.isEmpty()) {
            return 0L;
        }
        Set<Long> registrationIds = approved.stream()
                .map(Registration::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> recorded = competitionResultService.list(new LambdaQueryWrapper<CompetitionResult>()
                        .select(CompetitionResult::getRegistrationId)
                        .in(CompetitionResult::getRegistrationId, registrationIds))
                .stream()
                .map(CompetitionResult::getRegistrationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return registrationIds.stream().filter(id -> !recorded.contains(id)).count();
    }

    private String roleName(String role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case "ADMIN" -> "管理员";
            case "TEACHER" -> "教师";
            case "STUDENT" -> "学生";
            default -> role;
        };
    }
}
