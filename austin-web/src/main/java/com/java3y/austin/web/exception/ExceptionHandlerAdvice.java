package com.java3y.austin.web.exception;

import com.google.common.base.Throwables;
import com.java3y.austin.common.enums.RespStatusEnum;
import com.java3y.austin.common.vo.BasicResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author kl
 * @version 1.0.0
 * @description 拦截异常统一返回
 * @date 2023/2/9 19:03
 */
@Slf4j
@ControllerAdvice(basePackages = "com.java3y.austin.web.controller")
@ResponseBody
public class ExceptionHandlerAdvice {


    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<String> exceptionResponse(Exception e) {
        String errStackStr = Throwables.getStackTraceAsString(e);
        log.error("Unhandled exception occurred", e);
        return BasicResultVO.fail(RespStatusEnum.ERROR_500, String.format("%n%s%n", errStackStr));
    }

    @ExceptionHandler({CommonException.class})
    @ResponseStatus(HttpStatus.OK)
    public BasicResultVO<RespStatusEnum> commonResponse(CommonException ce) {
        log.error("Common exception occurred: {}", ce.getMessage(), ce);
        return new BasicResultVO<>(ce.getCode(), ce.getMessage(), ce.getRespStatusEnum());
    }
}

