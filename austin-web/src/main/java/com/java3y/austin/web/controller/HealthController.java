package com.java3y.austin.web.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检测
 *
 * @author 3y
 */
@Slf4j
@RestController
@Tag(name = "健康检测")
public class HealthController {
    @GetMapping("/")
    @Operation(summary = "/健康检测")
    public String health() {
        return "success";
    }
}
