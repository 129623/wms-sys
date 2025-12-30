package com.wms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Result handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        return Result.forbidden();
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("Duplicate entry")) {
                return Result.error("该数据已存在，请勿重复添加");
            }
            if (msg.contains("foreign key constraint fails")) {
                if (msg.contains("Cannot add or update a child row")) {
                    // Extract column name if possible, or provide a generic message
                    // Example message: ... FOREIGN KEY (`warehouse_id`) REFERENCES ...
                    int start = msg.indexOf("FOREIGN KEY (`");
                    if (start > -1) {
                        int end = msg.indexOf("`)", start);
                        if (end > -1) {
                            String column = msg.substring(start + 14, end);
                            return Result.error("关联的 " + column + " 值不存在");
                        }
                    }
                    return Result.error("关联的数据值不存在");
                }
                if (msg.contains("Cannot delete or update a parent row")) {
                    return Result.error("该数据已被其他数据引用，无法删除或修改");
                }
            }
        }
        return Result.error("数据库操作异常，请联系管理员");
    }
}
