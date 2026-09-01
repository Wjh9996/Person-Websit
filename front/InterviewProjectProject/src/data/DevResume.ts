import type { ResumeData } from '@/types/resume'

export const devResume: ResumeData = {
  basicInfo: {
    name: '王建豪',
    title: 'Java 全栈开发工程师',
    phone: '18727990870',
    email: '18727990870@163.com'
  },
  education: {
    school: '荆楚理工学院',
    major: '计算机科学与技术 本科',
    period: '2022.9 – 2026.6'
  },
  summary: [
    '具备 Java 后端与 Vue3 前端全栈开发经验，熟悉 SpringBoot 生态与 RESTful API 设计。',
    '参与智能在线测评系统从 0 到 1 开发，独立完成核心业务模块与代码沙箱集成。',
    '熟悉 MySQL、Redis 等常用中间件，具备 Docker 容器化部署与 Linux 服务器运维经验。',
    '学习能力强，善于在项目中快速定位问题并推动功能落地。'
  ],
  campus: [
    { period: '2023.10 – 2024.04', description: '第十五届蓝桥杯省赛三等奖' },
    { period: '2023.06 – 2024.06', description: '第十七届中国大学生计算机设计大赛二等奖' },
    { period: '2024.10 – 2025.04', description: '第十六届蓝桥杯省赛二等奖' }
  ],
  skills: [
    '熟练掌握 Java 基础与面向对象设计，熟悉 SpringBoot、MyBatis-Plus 进行后端业务开发',
    '熟悉 Vue3、TypeScript、HTML/CSS，能独立完成前端页面开发与前后端联调',
    '熟悉 MySQL 数据库设计与 SQL 优化，了解 Redis 缓存策略与常见使用场景',
    '了解 Docker 容器化部署，具备 Linux 环境下服务部署与基础运维能力',
    '熟悉 Git 版本管理，了解 Maven 项目构建与接口文档协作流程',
    '了解代码沙箱、AI 接口集成等扩展能力，具备全栈项目端到端交付经验'
  ],
  internships: [
    {
      company: '杭州一朵云科技有限公司',
      position: '售前工程师助理 / 运维实施',
      period: '2026.07 – 2026.08',
      duties: [
        '参与客户现场业务梳理，将业务需求转化为可落地的系统功能说明',
        '负责线下 Linux 服务器环境搭建与项目部署，保障服务稳定运行',
        '协助完成大模型预训练数据标注与处理流程，支持 AI 相关功能接入'
      ]
    },
    {
      company: '博众精工科技股份有限公司',
      position: '售后工程师',
      period: '2025.08 – 2025.12',
      duties: [
        '负责产线设备数据汇总与异常分析，形成结构化日报支撑问题排查',
        '在产线异常时快速响应并恢复设备运行，积累现场问题定位与协作经验'
      ]
    }
  ],
  projects: [
    {
      name: '智能在线测评系统 – 全栈开发负责人',
      techStack: 'SpringBoot 2.7.x + MyBatis-Plus + Vue3 + ArcoDesign + Redis + Docker + MySQL',
      description: '面向编程学习者的在线判题与智能分析平台，包含用户刷题、代码沙箱、AI 错误分析、个性化推荐及数据看板等核心功能。',
      duties: [
        '<strong>系统架构与后端开发：</strong>基于 SpringBoot 搭建后端服务，设计用户、题目、提交记录等核心数据模型，实现 RESTful API 20+ 个，支持登录鉴权、题目管理与判题流程。',
        '<strong>代码沙箱模块：</strong>集成 Docker 容器实现代码隔离执行，配置 CPU/内存资源限制，支持多语言判题结果回传，保障宿主机安全。',
        '<strong>前端开发：</strong>使用 Vue3 + ArcoDesign 完成刷题页、提交记录、管理后台等页面，对接后端接口并实现路由与状态管理。',
        '<strong>性能优化：</strong>引入 Redis 缓存热点题目与判题队列，配合数据库连接池调优，支撑 100 并发提交场景下的稳定响应。',
        '<strong>部署运维：</strong>编写 Docker Compose 配置，完成 MySQL、Redis、后端与前端的一键部署，支持开发环境与生产环境切换。'
      ]
    }
  ],
  projectDutyTitle: '开发工作职责与成果'
}
