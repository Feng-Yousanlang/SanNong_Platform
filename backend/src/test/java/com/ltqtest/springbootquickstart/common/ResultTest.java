package com.ltqtest.springbootquickstart.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void success_shouldUseDefaultMessageAndAttachData() {
        Result<String> result = Result.success("ok");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    void error_shouldKeepCustomCodeAndMessage() {
        Result<Void> result = Result.error(403, "拒绝访问");

        assertEquals(403, result.getCode());
        assertEquals("拒绝访问", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void successWithCode_shouldOverrideMessage() {
        Result<Integer> result = Result.success(201, "已创建", 42);

        assertEquals(201, result.getCode());
        assertEquals("已创建", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void successWithoutData_shouldReturnNullData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void errorWithMessageOnly_shouldUse500() {
        Result<Void> result = Result.error("服务器错误");

        assertEquals(500, result.getCode());
        assertEquals("服务器错误", result.getMessage());
    }
}
