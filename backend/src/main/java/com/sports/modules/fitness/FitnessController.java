package com.sports.modules.fitness;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sports.common.exception.BusinessException;
import com.sports.common.result.Result;
import com.sports.modules.system.mapper.SysUserMapper;
import com.sports.modules.fitness.mapper.FitnessMapper;
import com.sports.security.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@Validated
@RestController
@RequestMapping("/api/fitness")
@RequiredArgsConstructor
public class FitnessController {
    private final FitnessMapper mapper;
    private final SysUserMapper userMapper;

    @GetMapping
    public Result<Page<FitnessRecord>> list(
            @RequestParam(defaultValue = "1") @Min(1) int current,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate testDate) {
        // 学生只能查自己，不能通过传入他人ID越权。
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isTeacher()) userId = SecurityUtils.getUserId();
        return Result.success(mapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<FitnessRecord>()
                        .eq(userId != null, FitnessRecord::getUserId, userId)
                        .eq(testDate != null, FitnessRecord::getTestDate, testDate)
                        .orderByDesc(FitnessRecord::getTestDate).orderByDesc(FitnessRecord::getId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Long> create(@Valid @RequestBody FitnessRecord record) {
        record.setId(null);
        checkStudent(record.getUserId());
        try { mapper.insert(record); }
        catch (DuplicateKeyException e) { throw new BusinessException("该学生当天已有体测记录，请修改原记录"); }
        return Result.success(record.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> update(@PathVariable @Positive Long id, @Valid @RequestBody FitnessRecord record) {
        checkStudent(record.getUserId());
        record.setId(id);
        try {
            if (mapper.updateById(record) == 0) throw new BusinessException("体测记录不存在");
        } catch (DuplicateKeyException e) { throw new BusinessException("该学生当天已有体测记录"); }
        return Result.success();
    }

    @GetMapping("/clusters")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Map<String, Object>> clusters(@RequestParam LocalDate testDate,
            @RequestParam(defaultValue = "3") @Min(1) @Max(10) int k) {
        // 一次分析一个测试日期，避免同一学生多次测试产生重复权重。
        List<FitnessRecord> rows = mapper.selectList(new LambdaQueryWrapper<FitnessRecord>()
                .eq(FitnessRecord::getTestDate, testDate).orderByAsc(FitnessRecord::getId).last("LIMIT 5001"));
        if (rows.size() > 5000) throw new BusinessException("单次最多分析5000条记录");
        List<FitnessRecord> valid = rows.stream().filter(FitnessController::usable).toList();
        double[][] features = valid.stream().map(r -> new double[]{
                r.getWeightKg() / Math.pow(r.getHeightCm() / 100, 2),
                r.getVitalCapacity(), r.getRun50Seconds(), r.getJumpCm()}).toArray(double[][]::new);
        KMeans.Analysis analysis;
        try { analysis = KMeans.fit(features, k); }
        catch (IllegalArgumentException e) { throw new BusinessException(e.getMessage()); }
        return Result.success(Map.of("recordIds", valid.stream().map(FitnessRecord::getId).toList(),
                "features", List.of("BMI", "肺活量ml", "50米秒数", "立定跳远cm"),
                "analysis", analysis, "excludedCount", rows.size() - valid.size()));
    }

    private void checkStudent(Long id) {
        var user = userMapper.selectById(id);
        if (user == null || !"STUDENT".equals(user.getRole())) throw new BusinessException("请选择有效学生");
    }

    static boolean usable(FitnessRecord r) {
        return inRange(r.getHeightCm(), 50, 250) && inRange(r.getWeightKg(), 10, 400)
                && inRange(r.getVitalCapacity(), 100, 10000) && inRange(r.getRun50Seconds(), 4, 60)
                && inRange(r.getJumpCm(), 10, 400);
    }

    private static boolean inRange(Double v, double min, double max) {
        return v != null && Double.isFinite(v) && v >= min && v <= max;
    }
}
