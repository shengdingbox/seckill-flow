package com.seckillflow.controller.scheduler.request;

import lombok.Data;

/**
 * @author 周子斐
 * @date 2021/9/22
 * @Description
 */
@Data
public class BatchFileQueryRequest {
    private String filePath;
    private CharSequence source;
    private CharSequence dataType;

    public CharSequence getSource() {
        return source;
    }

    public void setSource(CharSequence source) {
        this.source = source;
    }

    public CharSequence getDataType() {
        return dataType;
    }

    public void setDataType(CharSequence dataType) {
        this.dataType = dataType;
    }
}
