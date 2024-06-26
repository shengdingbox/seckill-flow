package com.seckillflow.api.mail.Impl;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.seckillflow.api.mail.MailConfigRead;
import com.seckillflow.api.mail.MailSendApi;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailServiceImpl implements MailSendApi {

    @Async//异步发送邮件
    @Override
    public void sendQQMail(List<String> tos, String subject, String content, Boolean isHtml) {

        String host = MailConfigRead.getSmtpHost();
        String port = MailConfigRead.getSmtpPort();
        String account = MailConfigRead.getSendAccount();
        String password = MailConfigRead.getPassword();

        MailAccount mailAccount = new MailAccount();
        mailAccount.setHost(host);
        mailAccount.setPort(Integer.parseInt(port));
        mailAccount.setAuth(true);
        mailAccount.setFrom(account);
        mailAccount.setPass(password);
        //在使用QQ或Gmail邮箱时，需要强制开启SSL支持
        mailAccount.setSslEnable(true);

        MailUtil.send(mailAccount, tos, subject, content, isHtml);

    }


}
