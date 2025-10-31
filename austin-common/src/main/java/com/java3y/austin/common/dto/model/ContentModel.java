package com.java3y.austin.common.dto.model;

import com.alibaba.fastjson2.annotation.JSONType;

import java.io.Serializable;

/**
 * 发送内容的模型
 * (不同的渠道会有不同的消息体)
 *
 * @author 3y
 */
@JSONType(seeAlso = {
        EmailContentModel.class,
        SmsContentModel.class,
        PushContentModel.class,
        OfficialAccountsContentModel.class,
        MiniProgramContentModel.class,
        AlipayMiniProgramContentModel.class,
        EnterpriseWeChatContentModel.class,
        EnterpriseWeChatRobotContentModel.class,
        FeiShuRobotContentModel.class,
        DingDingRobotContentModel.class,
        DingDingWorkContentModel.class,
        ImContentModel.class
})
public class ContentModel implements Serializable {


}
