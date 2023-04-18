package com.swsm.proxynet.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectRespMessage {
    
    private Boolean result;
    private String message;
    
    
    
}
