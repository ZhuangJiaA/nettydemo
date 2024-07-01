package com.sixeco.nettydemo.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ycy
 * @program: netty-dev
 * @description: 消息数据
 * @date 2022-01-11 17:02:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageData {
    private String name;
    private String message;
}
