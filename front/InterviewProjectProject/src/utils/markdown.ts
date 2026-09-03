import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import json from 'highlight.js/lib/languages/json'
import xml from 'highlight.js/lib/languages/xml'
import java from 'highlight.js/lib/languages/java'
import python from 'highlight.js/lib/languages/python'

/**
 * Markdown 渲染工具
 *
 * 按需注册语言，避免把 highlight.js 全量语言打进包里。
 * html 关闭：笔记内容属于用户输入，禁止内联 HTML 以防 XSS。
 */

// 注册常用语言（够用即止，新增语言在这里补）
const languages = { javascript, typescript, bash, sql, json, xml, java, python }
Object.entries(languages).forEach(([name, lang]) => hljs.registerLanguage(name, lang))

const md = new MarkdownIt({
  html: false, // 安全优先：不解析内联 HTML
  linkify: true,
  breaks: true,
  highlight(code: string, lang: string): string {
    const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
    const body =
      language === 'plaintext'
        ? md.utils.escapeHtml(code)
        : hljs.highlight(code, { language, ignoreIllegals: true }).value

    return `<pre class="hljs-pre"><code class="hljs language-${md.utils.escapeHtml(language)}">${body}</code></pre>`
  }
})

export interface TocItem {
  id: string
  text: string
  level: number
}

export interface RenderResult {
  html: string
  toc: TocItem[]
}

/** 中文友好的锚点生成：保留中英文与数字，其余转为连字符 */
function slugify(text: string): string {
  const base = text
    .trim()
    .toLowerCase()
    .replace(/[^\w一-龥\- ]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
  return base || 'section'
}

/**
 * 标题 id 分配器
 * 传入新 Map 可从 0 开始；两次调用只要遍历顺序一致，生成的 id 就一致，
 * 从而保证「目录锚点」与「正文标题 id」能对上。
 */
function createIdAllocator() {
  const used = new Map<string, number>()
  return (text: string): string => {
    const base = slugify(text)
    const seen = used.get(base) ?? 0
    used.set(base, seen + 1)
    return seen === 0 ? base : `${base}-${seen}`
  }
}

// 为正文标题注入 id，使目录可跳转
md.renderer.rules.heading_open = (tokens, idx, options, _env, self) => {
  const token = tokens[idx]
  if (!token) return self.renderToken(tokens, idx, options)

  const text = tokens[idx + 1]?.content ?? ''
  token.attrSet('id', allocateForRender(text))
  return self.renderToken(tokens, idx, options)
}

let allocateForRender = createIdAllocator()

/** 渲染 Markdown，同时返回目录结构 */
export function renderMarkdown(source: string): RenderResult {
  const text = source ?? ''
  const tokens = md.parse(text, {})
  const allocate = createIdAllocator()
  const toc: TocItem[] = []

  tokens.forEach((token, index) => {
    if (token.type === 'heading_open' && (token.tag === 'h2' || token.tag === 'h3')) {
      const text = tokens[index + 1]?.content ?? ''
      const id = allocate(text)
      toc.push({ id, text, level: Number(token.tag.slice(1)) })
    }
  })

  // 正文使用同序分配器，保证 id 与目录一致
  allocateForRender = createIdAllocator()
  const html = md.render(text)

  return { html, toc }
}

/** 截取纯文本摘要：剔除代码块、表格与标记符号 */
export function excerpt(source: string, length = 90): string {
  const plain = (source ?? '')
    .replace(/```[\s\S]*?```/g, '') // 代码块
    .replace(/^\s*\|.*\|\s*$/gm, '') // 表格行
    .replace(/^\s*[-:| ]+$/gm, '') // 表格分隔行
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '') // 图片
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1') // 链接保留文字
    .replace(/^#{1,6}\s+/gm, '') // 标题符号
    .replace(/[*_`>[\]()#-]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return plain.length > length ? `${plain.slice(0, length)}…` : plain
}

/** 估算阅读时长（分钟） */
export function readingTime(source: string): number {
  const words = (source ?? '').replace(/\s+/g, '').length
  return Math.max(1, Math.round(words / 400))
}
