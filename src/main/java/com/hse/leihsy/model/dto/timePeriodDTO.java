package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class timePeriodDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
