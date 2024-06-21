package com.seckillflow.domain.model;

import lombok.Data;

import java.util.Date;

/**
 * @author 周子斐
 * @date 2021/9/22
 * @Description
 */
@Data
public class BatchFileReg {

    private String pathFileName;
    private String fileName;
    private String downloadTime;
    private String dataType;
    private String length;
    private Integer id;
}
