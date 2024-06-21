package com.seckillflow.controller.scheduler.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String originPassword;
    private String newPassword;
    private String confirmPassword;
}
