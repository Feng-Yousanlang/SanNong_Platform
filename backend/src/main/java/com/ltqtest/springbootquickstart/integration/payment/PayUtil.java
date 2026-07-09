package com.ltqtest.springbootquickstart.integration.payment;

import org.springframework.stereotype.Component;

@Component
public class PayUtil {

    public String sendRequestToAlipay(Double amount, Integer orderId, String subject) {
        return "local-dev: alipay not configured (orderId=" + orderId + ", amount=" + amount + ")";
    }
}
