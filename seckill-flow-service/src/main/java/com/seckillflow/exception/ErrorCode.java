package com.seckillflow.exception;

import com.zhouzifei.common.exception.ServiceException;

public enum ErrorCode{

    SCHEDULED_ERROR(999001, "调度失败"),
    DATA_NOT_FOUND(999002, "数据不存在");

    ErrorCode(Integer code, String message) {
        this.code= code;
        this.message = message;
    }

    public ServiceException toException() {
        return new ServiceException(code, message);
    }

    public Integer getCode()
    {
        return code;
    }
    public String getMessage()
    {
        return message;
    }

    private Integer code;
    private String message;
}
