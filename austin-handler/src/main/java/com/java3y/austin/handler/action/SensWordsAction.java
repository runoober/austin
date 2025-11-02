package com.java3y.austin.handler.action;

import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.common.dto.model.*;
import com.java3y.austin.common.pipeline.BusinessProcess;
import com.java3y.austin.common.pipeline.ProcessContext;
import com.java3y.austin.handler.config.SensitiveWordsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 敏感词过滤
 *
 * @author xiaoxiamao
 * @date 2024/08/17
 */
@Service
public class SensWordsAction implements BusinessProcess<TaskInfo> {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 过滤逻辑
     *
     * @param context
     *
     * @see com.java3y.austin.common.enums.ChannelType
     */
    @Override
    public void process(ProcessContext<TaskInfo> context) {
        Set<String> sensDict = Optional.ofNullable(redisTemplate.opsForSet().members(SensitiveWordsConfig.SENS_WORDS_DICT))
                .orElse(Collections.emptySet());
        if (ObjectUtils.isEmpty(sensDict)) {
            return;
        }
        
        ContentModel contentModel = context.getProcessModel().getContentModel();
        switch (context.getProcessModel().getMsgType()) {
            case 10, 50, 60, 120 -> {
                // IM, OFFICIAL_ACCOUNT, MINI_PROGRAM, ALIPAY_MINI_PROGRAM: 无文本内容，暂不做过滤处理
            }
            case 20 -> {
                if (contentModel instanceof PushContentModel pushContentModel) {
                    pushContentModel.setContent(filter(pushContentModel.getContent(), sensDict));
                }
            }
            case 30 -> {
                if (contentModel instanceof SmsContentModel smsContentModel) {
                    smsContentModel.setContent(filter(smsContentModel.getContent(), sensDict));
                }
            }
            case 40 -> {
                if (contentModel instanceof EmailContentModel emailContentModel) {
                    emailContentModel.setContent(filter(emailContentModel.getContent(), sensDict));
                }
            }
            case 70 -> {
                if (contentModel instanceof EnterpriseWeChatContentModel enterpriseWeChatContentModel) {
                    enterpriseWeChatContentModel.setContent(filter(enterpriseWeChatContentModel.getContent(), sensDict));
                }
            }
            case 80 -> {
                if (contentModel instanceof DingDingRobotContentModel dingDingRobotContentModel) {
                    dingDingRobotContentModel.setContent(filter(dingDingRobotContentModel.getContent(), sensDict));
                }
            }
            case 90 -> {
                if (contentModel instanceof DingDingWorkContentModel dingDingWorkContentModel) {
                    dingDingWorkContentModel.setContent(filter(dingDingWorkContentModel.getContent(), sensDict));
                }
            }
            case 100 -> {
                if (contentModel instanceof EnterpriseWeChatRobotContentModel enterpriseWeChatRobotContentModel) {
                    enterpriseWeChatRobotContentModel.setContent(filter(enterpriseWeChatRobotContentModel.getContent(), sensDict));
                }
            }
            case 110 -> {
                if (contentModel instanceof FeiShuRobotContentModel feiShuRobotContentModel) {
                    feiShuRobotContentModel.setContent(filter(feiShuRobotContentModel.getContent(), sensDict));
                }
            }
            default -> {
            }
        }
    }

    /**
     * 敏感词替换成对应长度'*'
     *
     * @param content
     * @param sensDict
     * @return
     */
    private String filter(String content, Set<String> sensDict) {
        if (ObjectUtils.isEmpty(content) || ObjectUtils.isEmpty(sensDict)) {
            return content;
        }
        // 构建字典树
        TrieNode root = buildTrie(sensDict);
        StringBuilder result = new StringBuilder();
        int n = content.length();
        int i = 0;

        while (i < n) {
            TrieNode node = root;
            int j = i;
            int lastMatchEnd = -1;

            while (j < n && node != null) {
                node = node.children.get(content.charAt(j));
                if (node != null && node.isEnd) {
                    lastMatchEnd = j;
                }
                j++;
            }

            if (lastMatchEnd != -1) {
                // 找到敏感词，用'*'替换
                for (int k = i; k <= lastMatchEnd; k++) {
                    result.append('*');
                }
                i = lastMatchEnd + 1;
            } else {
                result.append(content.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    /**
     * 构建字典树
     *
     * @param sensDict
     * @return
     */
    private TrieNode buildTrie(Set<String> sensDict) {
        var root = new TrieNode();
        for (var word : sensDict) {
            var node = root;
            for (var c : word.toCharArray()) {
                node = node.children.computeIfAbsent(c, _ -> new TrieNode());
            }
            node.isEnd = true;
        }
        return root;
    }

    /**
     * 树节点
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        // 是否为叶子节点
        boolean isEnd = false;
    }

}
