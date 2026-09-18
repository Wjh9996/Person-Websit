package com.wjh.interviewbacked.component;

import com.wjh.interviewbacked.common.LlmProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型调用客户端：锁定 DeepSeek（OpenAI 兼容协议 /v1/chat/completions）。
 * 通过 RestClient 调用，无需新增依赖；api-key 缺失时明确报错，便于排查配置。
 */
@Component
public class LlmClient {

    private final LlmProperties props;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmClient(LlmProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * 发起一次对话补全，返回模型输出的文本内容（通常为 JSON 字符串）。
     */
    public String analyze(String systemPrompt, String userPrompt) {
        return analyze(systemPrompt, userPrompt, props.getMaxTokens());
    }

    /** 可指定本次最大 token（知识库问答需要更长的回答空间） */
    public String analyze(String systemPrompt, String userPrompt, int maxTokens) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalStateException("LLM_API_KEY 未配置，请在环境变量中设置后重启后端");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.getModel());
        body.put("temperature", 0.3);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        if (maxTokens > 0) {
            body.put("max_tokens", maxTokens);
        }

        String resp = restClient.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(String.class);

        return extractContent(resp);
    }

    /** 从 OpenAI 兼容响应中提取 choices[0].message.content */
    @SuppressWarnings("unchecked")
    private String extractContent(String resp) {
        if (resp == null || resp.isBlank()) {
            throw new IllegalStateException("大模型返回空响应");
        }
        try {
            Map<String, Object> root = objectMapper.readValue(resp, Map.class);
            Object choicesObj = root.get("choices");
            if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                Object first = choices.get(0);
                if (first instanceof Map<?, ?> firstMap) {
                    Object message = firstMap.get("message");
                    if (message instanceof Map<?, ?> msgMap) {
                        Object content = msgMap.get("content");
                        if (content instanceof String s && !s.isBlank()) {
                            return s;
                        }
                    }
                }
            }
            // 兜底：从响应体中抽取第一个 {...} 块
            int start = resp.indexOf('{');
            int end = resp.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return resp.substring(start, end + 1);
            }
            throw new IllegalStateException("响应中未找到有效内容");
        } catch (Exception e) {
            throw new IllegalStateException("解析大模型响应失败: " + e.getMessage());
        }
    }
}
