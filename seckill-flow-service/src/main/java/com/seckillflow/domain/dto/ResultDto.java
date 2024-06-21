package com.seckillflow.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class ResultDto {

    private String label;
    private String path="";
    private List<ResultDto> children = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResultDto resultDto = (ResultDto) o;
        return Objects.equals(label, resultDto.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
