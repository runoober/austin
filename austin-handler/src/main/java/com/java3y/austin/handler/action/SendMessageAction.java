package com.java3y.austin.handler.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Sets;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.common.enums.ChannelType;
import com.java3y.austin.common.pipeline.BusinessProcess;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.handler.handler.HandlerHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 发送消息，路由到对应的渠道下发消息
 *
 * @author 3y
 */
@Slf4j
@Service
public class SendMessageAction implements BusinessProcess<TaskInfo> {
    
    private final HandlerHolder handlerHolder;
    
    public SendMessageAction(HandlerHolder handlerHolder) {
        this.handlerHolder = handlerHolder;
    }

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();
        Integer sendChannel = taskInfo.getSendChannel();

        // 参数校验
        if (sendChannel == null) {
            log.warn("Send channel is null, taskInfo: {}", taskInfo);
            return;
        }

        Set<String> receivers = taskInfo.getReceiver();
        if (CollUtil.isEmpty(receivers)) {
            log.warn("Receivers is empty, taskInfo: {}", taskInfo);
            return;
        }

        // 微信小程序&服务号只支持单人推送，为了后续逻辑统一处理，于是在这做了单发处理
        Set<Integer> singleReceiverChannels = Set.of(
            ChannelType.MINI_PROGRAM.getCode(),
            ChannelType.OFFICIAL_ACCOUNT.getCode(),
            ChannelType.ALIPAY_MINI_PROGRAM.getCode()
        );
        
        if (singleReceiverChannels.contains(sendChannel)) {
            TaskInfo taskClone = ObjectUtil.cloneByStream(taskInfo);
            for (String receiver : receivers) {
                taskClone.setReceiver(Sets.newHashSet(receiver));
                handlerHolder.route(sendChannel).doHandler(taskClone);
            }
            return;
        }
        handlerHolder.route(sendChannel).doHandler(taskInfo);
    }
}
