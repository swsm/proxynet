package com.swsm.proxynet.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandMessage {
    private String type;
    private String message;
}
