package com.sports.modules.statistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sports.common.result.Result;
import com.sports.modules.event.entity.Event;
import com.sports.modules.event.service.EventService;
import com.sports.modules.registration.entity.Registration;
import com.sports.modules.registration.service.RegistrationService;
import com.sports.modules.result.entity.CompetitionResult;
import com.sports.modules.result.service.CompetitionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计 Controller
 * 状态说明:
 * - Event.status: 1-草稿 2-报名中 3-进行中 4-已结束
 * - Registration.status: 1-待审核 2-已通过 3-已拒绝 4-已取消
 *
 * @author sports
 */
@Tag(name = "统计分析", description = "数据统计与图表")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class StatisticsController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CompetitionResultService competitionResultService;

    @Operation(summary = "获取概览统计数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>();

        // 赛事总数
        data.put("totalEvents", eventService.count());

        // 参赛人数（已通过的报名数）
        LambdaQueryWrapper<Registration> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Registration::getStatus, 2);  // 2-已通过
        data.put("totalParticipants", registrationService.count(approvedWrapper));

        // 报名总数
        data.put("totalRegistrations", registrationService.count());

        // 成绩记录数
        data.put("totalResults", competitionResultService.count());

        return Result.success(data);
    }

    @Operation(summary = "获取赛事报名人数Top10")
    @GetMapping("/registration-top")
    public Result<List<Map<String, Object>>> getRegistrationTop() {
        // 获取所有赛事
        List<Event> events = eventService.list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Event event : events) {
            LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Registration::getEventId, event.getId())
                    .ne(Registration::getStatus, 4);  // 4-已取消
            long count = registrationService.count(wrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("eventId", event.getId());
            item.put("eventName", event.getName());
            item.put("count", count);
            result.add(item);
        }

        // 按报名人数排序，取前10
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }

        return Result.success(result);
    }

    @Operation(summary = "获取成绩分布统计")
    @GetMapping("/score-distribution")
    public Result<List<Map<String, Object>>> getScoreDistribution(@RequestParam Long eventId) {
        // 获取该赛事的所有成绩
        LambdaQueryWrapper<CompetitionResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionResult::getEventId, eventId)
                .isNotNull(CompetitionResult::getRanking)
                .orderByAsc(CompetitionResult::getRanking);
        List<CompetitionResult> results = competitionResultService.list(wrapper);

        // 按名次范围分组统计
        Map<String, Long> rankDistribution = new LinkedHashMap<>();
        rankDistribution.put("第1-3名", results.stream().filter(r -> r.getRanking() != null && r.getRanking() >= 1 && r.getRanking() <= 3).count());
        rankDistribution.put("第4-8名", results.stream().filter(r -> r.getRanking() != null && r.getRanking() >= 4 && r.getRanking() <= 8).count());
        rankDistribution.put("第9名以后", results.stream().filter(r -> r.getRanking() != null && r.getRanking() > 8).count());

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, Long> entry : rankDistribution.entrySet()) {
            if (entry.getValue() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("rankRange", entry.getKey());
                item.put("count", entry.getValue());
                data.add(item);
            }
        }

        return Result.success(data);
    }

    @Operation(summary = "获取月度趋势统计")
    @GetMapping("/monthly-trend")
    public Result<List<Map<String, Object>>> getMonthlyTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 获取最近6个月
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthStr = monthStart.format(formatter);

            // 该月赛事数
            LambdaQueryWrapper<Event> eventWrapper = new LambdaQueryWrapper<>();
            eventWrapper.ge(Event::getStartDate, monthStart)
                    .le(Event::getStartDate, monthEnd);
            long eventCount = eventService.count(eventWrapper);

            // 该月报名数
            LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.ge(Registration::getCreateTime, monthStart.atStartOfDay())
                    .le(Registration::getCreateTime, monthEnd.atTime(23, 59, 59));
            long registrationCount = registrationService.count(regWrapper);

            // 该月参赛人数（已通过的报名）
            LambdaQueryWrapper<Registration> participantWrapper = new LambdaQueryWrapper<>();
            participantWrapper.ge(Registration::getCreateTime, monthStart.atStartOfDay())
                    .le(Registration::getCreateTime, monthEnd.atTime(23, 59, 59))
                    .eq(Registration::getStatus, 2);  // 2-已通过
            long participantCount = registrationService.count(participantWrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("eventCount", eventCount);
            item.put("registrationCount", registrationCount);
            item.put("participantCount", participantCount);
            result.add(item);
        }

        return Result.success(result);
    }

    @Operation(summary = "获取赛事类型分布统计")
    @GetMapping("/type-distribution")
    public Result<List<Map<String, Object>>> getTypeDistribution() {
        List<Event> events = eventService.list();

        // 按类型分组
        Map<String, Long> typeCount = events.stream()
                .filter(e -> e.getType() != null)
                .collect(Collectors.groupingBy(Event::getType, Collectors.counting()));

        // 类型名称映射
        Map<String, String> typeNames = Map.of(
                "TRACK_FIELD", "田径运动会",
                "BASKETBALL", "篮球联赛",
                "FOOTBALL", "足球联赛",
                "BADMINTON", "羽毛球比赛",
                "TABLE_TENNIS", "乒乓球比赛",
                "SWIMMING", "游泳比赛",
                "OTHER", "其他"
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());
            item.put("typeName", typeNames.getOrDefault(entry.getKey(), entry.getKey()));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return Result.success(result);
    }

    @Operation(summary = "获取赛事状态统计")
    @GetMapping("/event-status")
    public Result<List<Map<String, Object>>> getEventStatusStats() {
        List<Event> events = eventService.list();

        // 按 Integer status 分组
        Map<Integer, Long> statusCount = events.stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        // 状态名称映射: 1-草稿 2-报名中 3-进行中 4-已结束
        Map<Integer, String> statusNames = Map.of(
                1, "草稿",
                2, "报名中",
                3, "进行中",
                4, "已结束"
        );

        for (Map.Entry<Integer, Long> entry : statusCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("statusName", statusNames.getOrDefault(entry.getKey(), String.valueOf(entry.getKey())));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return Result.success(result);
    }

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> data = new HashMap<>();

        // 赛事总数
        data.put("totalEvents", eventService.count());

        // 进行中的赛事 (3-进行中)
        LambdaQueryWrapper<Event> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Event::getStatus, 3);
        data.put("ongoingEvents", eventService.count(ongoingWrapper));

        // 报名总数
        data.put("totalRegistrations", registrationService.count());

        // 待审核报名 (1-待审核)
        LambdaQueryWrapper<Registration> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Registration::getStatus, 1);
        data.put("pendingRegistrations", registrationService.count(pendingWrapper));

        // 成绩记录数
        data.put("totalResults", competitionResultService.count());

        return Result.success(data);
    }
}
