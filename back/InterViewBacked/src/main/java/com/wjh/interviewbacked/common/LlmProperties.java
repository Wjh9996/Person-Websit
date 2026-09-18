package com.wjh.interviewbacked.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型连接配置（OpenAI 兼容协议，默认锁定 DeepSeek 国内厂商）。
 * 真实 api-key 通过环境变量 LLM_API_KEY 注入，application.yml 已被 gitignore，不入库。
 */
@Component
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmProperties {
    /** 厂商地址，默认 DeepSeek 兼容端点 */
    private String baseUrl = "https://api.deepseek.com";
    /** API 密钥，运行时由环境变量 LLM_API_KEY 注入 */
    private String apiKey = "";
    /** 模型标识，默认 deepseek-chat */
    private String model = "deepseek-chat";
    /** 读取超时（秒） */
    private int timeoutSeconds = 30;
    /** 单次生成最大 token */
    private int maxTokens = 800;
}
