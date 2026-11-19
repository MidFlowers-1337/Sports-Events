package com.sports.modules.fitness;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("fitness_record")
public class FitnessRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull @Positive
    private Long userId;
    @NotNull @PastOrPresent
    private LocalDate testDate;
    @NotNull @DecimalMin("50") @DecimalMax("250")
    private Double heightCm;
    @NotNull @DecimalMin("10") @DecimalMax("400")
    private Double weightKg;
    @NotNull @DecimalMin("100") @DecimalMax("10000")
    private Double vitalCapacity;
    @NotNull @DecimalMin("4") @DecimalMax("60")
    private Double run50Seconds;
    @NotNull @DecimalMin("10") @DecimalMax("400")
    private Double jumpCm;
}
