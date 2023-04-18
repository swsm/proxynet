package com.swsm.proxynet.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liujie
 * @date 2023-04-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandRespMessage {

    private String userId;
    private byte[] respInfo;


}
