package com.wjh.interviewbacked.config;

import com.wjh.interviewbacked.common.HashUtils;
import com.wjh.interviewbacked.common.JacksonUtils;
import com.wjh.interviewbacked.dto.ResumeData;
import com.wjh.interviewbacked.entity.Note;
import com.wjh.interviewbacked.entity.NoteContent;
import com.wjh.interviewbacked.entity.NoteTagRel;
import com.wjh.interviewbacked.entity.Resume;
import com.wjh.interviewbacked.entity.Tag;
import com.wjh.interviewbacked.entity.User;
import com.wjh.interviewbacked.mapper.NoteContentMapper;
import com.wjh.interviewbacked.mapper.NoteMapper;
import com.wjh.interviewbacked.mapper.NoteTagRelMapper;
import com.wjh.interviewbacked.mapper.ResumeMapper;
import com.wjh.interviewbacked.mapper.TagMapper;
import com.wjh.interviewbacked.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 启动时初始化演示数据（仅当库为空时插入，幂等）：
 * 1. 演示账号 admin / 123456（role=admin）
 * 2. 7 篇学习笔记（正文拆入 note_content；标签规范化入 tag + note_tag_rel；填 user_id/version）
 * 3. 3 份简历（填 user_id/version）
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final NoteContentMapper noteContentMapper;
    private final TagMapper tagMapper;
    private final NoteTagRelMapper noteTagRelMapper;
    private final ResumeMapper resumeMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserMapper userMapper, NoteMapper noteMapper, NoteContentMapper noteContentMapper,
                           TagMapper tagMapper, NoteTagRelMapper noteTagRelMapper, ResumeMapper resumeMapper) {
        this.userMapper = userMapper;
        this.noteMapper = noteMapper;
        this.noteContentMapper = noteContentMapper;
        this.tagMapper = tagMapper;
        this.noteTagRelMapper = noteTagRelMapper;
        this.resumeMapper = resumeMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        String adminId = seedUser();
        seedNotesIfEmpty(adminId);
        seedResumesIfEmpty(adminId);
    }

    private String seedUser() {
        User existing = userMapper.selectByUsername("admin");
        if (existing != null) return existing.getId();
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("admin");
        user.setPassword(encoder.encode("123456"));
        user.setNickname("王建豪");
        user.setEmail("18727990870@163.com");
        user.setAvatar("王");
        user.setBio("计算机科学与技术专业，方向为软件测试 / 全栈开发。这里记录我的学习笔记与项目经历。");
        user.setRole("admin");
        user.setStatus(1);
        user.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user.getId();
    }

    private void seedNotesIfEmpty(String adminId) {
        if (!noteMapper.listNotes(adminId, null, null, null).isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        buildNote(adminId, "note-router-lazy", "Vue Router 路由懒加载的原理与配置",
            "为什么要写 component: () => import(...)，它到底优化了什么？",
            "frontend", true, 128, "2026-08-21T10:12:00", "2026-08-21T10:12:00",
            Arrays.asList("Vue", "Vue Router", "性能优化"),
            """
            ## 背景

            项目里路由是这样写的：

            ```ts
            {
              path: '/resume/:type',
              name: 'resume',
              component: () => import('../views/ResumeView.vue')
            }
            ```

            而不是直接 `import ResumeView from '../views/ResumeView.vue'`。这两者的差别，就是**懒加载**。

            ## 它解决了什么问题

            ### 1. 减少首屏加载时间

            不使用懒加载时，所有路由组件都会被打包进同一个 `app.js`，用户打开首页就要把整个站点的代码全部下载完，哪怕他根本不会访问简历页。

            懒加载通过动态 `import()`，让组件只在该路由被真正访问时才去下载对应的 JS 分片。

            ### 2. 代码分割（Code Splitting）

            Vite / Webpack 会把动态 `import` 识别为分割点，单独打成一个 chunk。构建产物类似：

            | 文件 | 说明 |
            | --- | --- |
            | `index-[hash].js` | 主包，首屏必需 |
            | `ResumeView-[hash].js` | 访问 /resume 时才加载 |

            好处是：

            - 主包体积更小，首屏更快
            - 浏览器可并行加载
            - 缓存粒度更细，只改了简历页就只重新下载那一个 chunk

            ## 实践注意点

            > 懒加载不是越多越好。首屏就要用到的页面不要懒加载，否则会多一次往返请求，反而更慢。

            判断标准很简单：**首屏可见 → 直接引入；需要点击跳转才能看到 → 懒加载。**
            """);
        buildNote(adminId, "note-playwright", "Playwright 端到端测试入门笔记",
            "微软出的 E2E 框架，比 Selenium 好用在哪？核心特性速记。",
            "testing", true, 96, "2026-08-21T11:20:00", "2026-08-22T09:05:00",
            Arrays.asList("Playwright", "自动化测试", "E2E"),
            """
            ## 是什么

            Playwright 是微软开源的现代化 Web 自动化测试框架，主要用于端到端（E2E）测试，也能胜任爬虫和自动化脚本。

            ## 核心优势

            ### 跨浏览器与跨平台

            一套 API 驱动 Chromium、Firefox、WebKit 三大引擎，原生支持 Windows / Linux / macOS。

            ### 自动等待，告别不稳定

            这是它最大的卖点。传统框架要手写 `sleep`，Playwright 在执行点击前会**自动等待元素变为可操作状态**。

            ```js
            // 不需要 sleep，框架自己会等
            await page.getByRole('button', { name: '保存' }).click()
            ```

            ### Web 优先断言

            断言会自动重试直到条件满足或超时，大大减少因网络延迟导致的偶发失败。

            ### 隔离与并行

            - 每个测试运行在独立的浏览器上下文中，完全隔离，创建开销极低
            - 默认并行执行

            ## 内置工具链

            - **Codegen**：录制浏览器操作，自动生成测试代码
            - **Trace Viewer**：完整时间轴，含 DOM 快照、网络请求、控制台日志
            - **VS Code 扩展**：直接在编辑器里运行调试

            ## 上手命令

            ```bash
            npm init playwright@latest
            npx playwright test
            npx playwright show-report
            ```

            > 项目里已经装了 Cypress（create-vue 脚手架自带），后续如果要对比，可以从"自动等待"和"并行速度"两个维度实测一下。
            """);
        buildNote(adminId, "note-vue-naming", "Vue 组件命名规范与 ESLint 踩坑",
            "为什么组件叫 Resume 会报错？vue/multi-word-component-names 规则解析。",
            "frontend", false, 74, "2026-08-21T18:44:00", "2026-08-21T18:44:00",
            Arrays.asList("Vue", "ESLint", "规范"),
            """
            ## 报错现场

            把组件命名成 `Resume`，ESLint 直接报错：

            ```
            vue/multi-word-component-names
            ```

            ## 为什么

            规则要求 Vue 组件名**必须由多个单词组成**，原因是：

            1. **避免与原生 HTML 元素冲突** —— 未来 Web 标准可能新增同名元素
            2. **提高可读性** —— `UserResume`、`ResumeEditor` 明显比 `Resume` 表意更清晰
            3. **官方风格指南推荐** —— 属于 Vue 官方 Essential 级别规则

            ## 另一个常见报错

            `<script>` 标签缺少 `lang` 属性会触发 `vue/block-lang`：

            ```vue
            <!-- 错误 -->
            <script setup>

            <!-- 正确 -->
            <script setup lang="ts">
            ```

            显式声明语言可以让工具链正确识别语法。

            ## 本项目约定的规范

            1. 组件文件统一带 `Component` 后缀，如 `ModuleCardComponent.vue`
            2. 组件内不写重复样式，公共部分抽到 `src/assets/styles/`
            3. 样式分三层：`global.css` 全局、`common/` 跨组件通用、`components/` 组件专属

            > 规范一旦定下来就要全局一致，不然后期重构成本很高。
            """);
        buildNote(adminId, "note-jmeter", "JMeter 并发压测实战记录",
            "100 并发下的响应时间分析与连接池问题定位。",
            "testing", false, 63, "2026-08-22T14:30:00", "2026-08-22T14:30:00",
            Arrays.asList("JMeter", "性能测试", "压测"),
            """
            ## 场景

            在线测评系统，模拟多用户同时提交代码，验证接口在 100 并发下的表现。

            ## 压测配置

            - 线程数：100
            - Ramp-Up：10 秒
            - 循环次数：持续 5 分钟

            ## 关注的指标

            | 指标 | 含义 | 可接受范围 |
            | --- | --- | --- |
            | Average | 平均响应时间 | < 800ms |
            | 90% Line | 90% 请求的响应时间 | < 1500ms |
            | Error % | 错误率 | < 0.1% |
            | Throughput | 吞吐量 | 越高越好 |

            ## 发现的问题

            首轮压测 90% Line 超过 3 秒，错误率 2.3%。排查方向：

            1. **数据库连接池打满** —— 默认配置连接数过小，请求排队
            2. **Redis 缓存未命中** —— 热点数据没有预热，全部打到数据库
            3. **没有做限流** —— 突发流量直接压垮服务

            ## 优化后

            调整连接池配置、补充 Redis 缓存策略后：

            - 平均响应时间从 2100ms 降到 420ms
            - 错误率降到 0

            > 结论：压测不是跑完看个数字，重点是**定位瓶颈**。响应时间长的时候，先看是数据库、缓存还是网络。
            """);
        buildNote(adminId, "note-vite-structure", "Vite + Vue3 项目目录结构规范",
            "一套能撑住项目长大的目录组织方式，附职责划分说明。",
            "tools", false, 88, "2026-08-22T16:40:00", "2026-08-22T16:40:00",
            Arrays.asList("Vite", "工程化", "项目结构"),
            """
            ## 目录约定

            ```text
            src/
            ├── assets/        # 资源：styles / images
            ├── components/    # 可复用组件（按业务域分子目录）
            ├── data/          # 静态数据 & 种子数据
            ├── router/        # 路由配置
            ├── stores/        # Pinia 状态管理
            ├── types/         # TypeScript 类型定义
            ├── utils/         # 工具函数
            ├── views/         # 页面级视图
            ├── App.vue
            └── main.ts
            ```

            ## 样式分层

            这一层最容易乱，建议一开始就定死：

            ```text
            assets/styles/
            ├── global.css       # 重置、字体、基础色
            ├── common/          # 跨组件通用：buttons / cards / badges
            ├── components/      # 组件专属样式
            └── views/           # 页面专属样式
            ```

            判断标准：**用到两次以上的样式就往上抽一层。**

            ## 为什么 types 要单独放

            类型定义分散在各处时，接口变更要改好几个文件。集中放 `types/` 之后：

            - 领域模型一目了然
            - 与后端对接时照着类型写 DTO 即可
            - 避免循环依赖

            ## 一个小建议

            `data/` 目录在纯前端阶段用来放 mock 数据，等后端接口就绪，直接把 `data/` 换成 `services/`，页面代码基本不用动。

            > 前提是：**页面永远不直接 import data，而是通过 service 层拿数据。**
            """);
        buildNote(adminId, "note-mysql-slow", "MySQL 慢查询排查思路",
            "从开启慢日志到 EXPLAIN 分析，一条完整的排查链路。",
            "backend", false, 55, "2026-08-25T09:15:00", "2026-08-25T09:15:00",
            Arrays.asList("MySQL", "性能优化", "数据库"),
            """
            ## 第一步：找到慢 SQL

            开启慢查询日志：

            ```sql
            SET GLOBAL slow_query_log = 'ON';
            SET GLOBAL long_query_time = 1;
            ```

            超过 1 秒的语句会被记录下来。

            ## 第二步：EXPLAIN 看执行计划

            ```sql
            EXPLAIN SELECT * FROM note WHERE category = 'frontend';
            ```

            重点看三列：

            | 字段 | 关注点 |
            | --- | --- |
            | type | 至少到 `range`，出现 `ALL` 说明全表扫描 |
            | key | 是否命中索引，为 NULL 就是没走索引 |
            | rows | 扫描行数，越小越好 |

            ## 常见优化手段

            1. **补索引** —— WHERE / ORDER BY / JOIN 涉及的字段
            2. **避免 SELECT *** —— 只取需要的列，减少回表
            3. **注意最左前缀** —— 联合索引必须从最左列开始匹配
            4. **分页优化** —— 深分页用子查询先定位 id

            ```sql
            -- 深分页优化：先查 id 再回表
            SELECT * FROM note
            WHERE id >= (SELECT id FROM note LIMIT 100000, 1)
            LIMIT 20;
            ```

            ## 索引失效的坑

            - 在索引列上用函数
            - 隐式类型转换（字符串字段传了数字）
            - 前置通配符 `LIKE '%abc'`
            - OR 连接非索引列

            > 排查顺序建议：慢日志定位 → EXPLAIN 分析 → 补索引 → 改写 SQL → 复查执行计划。
            """);
        buildNote(adminId, "note-http-https", "面试高频：HTTP 与 HTTPS 的区别",
            "面试高频：不仅要知道更安全，还要能讲清楚 TLS 握手过程。",
            "interview", false, 142, "2026-08-28T20:00:00", "2026-08-28T20:00:00",
            Arrays.asList("HTTP", "HTTPS", "计算机网络"),
            """
            ## 基础区别

            | 对比项 | HTTP | HTTPS |
            | --- | --- | --- |
            | 默认端口 | 80 | 443 |
            | 传输方式 | 明文 | TLS 加密 |
            | 身份认证 | 无 | CA 证书验证身份 |
            | 数据完整性 | 无校验 | 有校验，防篡改 |

            ## HTTPS 怎么保证安全

            三个要点：**机密性、完整性、身份认证**。

            ### 混合加密

            - 握手阶段用**非对称加密**交换密钥
            - 传输阶段用**对称加密**加密数据（性能考虑）

            ### TLS 握手简化流程

            1. 客户端发送支持的加密套件 + 随机数
            2. 服务端返回证书 + 选定的加密套件 + 随机数
            3. 客户端验证证书合法性，生成预主密钥并用证书公钥加密发送
            4. 双方根据随机数 + 预主密钥生成会话密钥
            5. 后续通信使用会话密钥对称加密

            ## 常考追问

            **Q：HTTPS 一定安全吗？**

            不一定。存在中间人攻击场景，比如用户主动信任了伪造证书。另外数据到达服务端之后就是明文了。

            **Q：为什么不全用非对称加密？**

            非对称加密计算复杂度高，性能比对称加密慢几个数量级，只适合用来交换密钥。

            **Q：HTTP/2 相比 1.1 有哪些改进？**

            - 二进制分帧
            - 多路复用，解决队头阻塞
            - 头部压缩 HPACK
            - 服务端推送

            > 回答这类问题，先给结论，再展开细节，最后补充边界情况，层次感比背细节更重要。
            """);
    }

    private void buildNote(String adminId, String id, String title, String summary, String category,
                           boolean pinned, int views, String createdAt, String updatedAt,
                           List<String> tags, String content) {
        LocalDateTime created = LocalDateTime.parse(createdAt);
        LocalDateTime updated = LocalDateTime.parse(updatedAt);
        Note note = new Note();
        note.setId(id);
        note.setUserId(adminId);
        note.setTitle(title);
        note.setSummary(summary);
        note.setCategory(category);
        note.setPinned(pinned);
        note.setViews(views);
        note.setVersion(0);
        note.setContentHash(HashUtils.md5Hex(content));
        // 演示笔记默认发布到讨论广场（visibility=1），保证公开广场有内容
        note.setVisibility(1);
        note.setDeleted(0);
        note.setCreatedAt(created);
        note.setUpdatedAt(updated);
        note.setTags(JacksonUtils.toJson(tags));
        noteMapper.insert(note);

        noteContentMapper.insert(new NoteContent(id, content));
        LocalDateTime now = LocalDateTime.now();
        for (String raw : tags) {
            String name = raw.trim();
            if (name.isEmpty()) continue;
            Tag tag = tagMapper.selectByUserIdAndName(adminId, name);
            if (tag == null) {
                tag = new Tag();
                tag.setId(UUID.randomUUID().toString());
                tag.setUserId(adminId);
                tag.setName(name);
                tag.setCreatedAt(now);
                tagMapper.insert(tag);
            }
            noteTagRelMapper.insert(new NoteTagRel(id, tag.getId()));
        }
    }

    private void seedResumesIfEmpty(String adminId) {
        if (!resumeMapper.selectAll().isEmpty()) return;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("seed-resumes.json")) {
            if (in == null) return;
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<SeedResume> seeds = JacksonUtils.fromJson(json, new TypeReference<List<SeedResume>>() {});
            LocalDateTime now = LocalDateTime.now();
            for (SeedResume s : seeds) {
                Resume r = new Resume();
                r.setId(s.id());
                r.setUserId(adminId);
                r.setLabel(s.label());
                r.setIcon(s.icon());
                r.setPath(s.path());
                r.setContent(JacksonUtils.toJson(s.content()));
                r.setVersion(0);
                r.setDeleted(0);
                r.setCreatedAt(now);
                r.setUpdatedAt(now);
                resumeMapper.insert(r);
            }
        } catch (Exception e) {
            throw new RuntimeException("加载简历种子数据失败", e);
        }
    }

    private record SeedResume(String id, String label, String icon, String path, ResumeData content) {
    }
}
