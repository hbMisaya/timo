package com.leyou.sms.listener;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Map;

@Component
@EnableConfigurationProperties({SmsProperties.class})
@AllArgsConstructor
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    private SmsProperties smsProperties;

    @RabbitListener(bindings = @QueueBinding (
            value = @Queue(value = "leyou.sms.queue",durable = "true"),
            exchange = @Exchange(value = "leyou.sms.exchange",ignoreDeclarationExceptions = "true" ,type = ExchangeTypes.TOPIC),
            key = {"verifycode.sms"}
    ))
    public void sendsms(Map<String, String> msg) throws ClientException {
        if (CollectionUtils.isEmpty (msg)) {
            return;
        }
        String phone = msg.get ("phone");
        String code =msg.get("code");
        if (StringUtils.isNotBlank (phone) && StringUtils.isNotBlank (code)){
            this.smsUtils.sendSms (phone, code, this.smsProperties.getSignName(),this.smsProperties.getVerifyCodeTemplate());
        }

    }
}
