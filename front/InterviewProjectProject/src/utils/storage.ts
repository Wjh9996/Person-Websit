/**
 * localStorage 封装
 * 统一前缀，避免与同域下其他应用冲突；存取失败时降级为内存态，不阻塞页面
 */
const PREFIX = 'interview-site:'

/** 内存兜底，localStorage 不可用（隐私模式 / 配额超限）时启用 */
const memoryFallback = new Map<string, string>()

function getKey(key: string): string {
  return PREFIX + key
}

export function readStorage<T>(key: string, fallback: T): T {
  const fullKey = getKey(key)
  try {
    const raw = window.localStorage.getItem(fullKey) ?? memoryFallback.get(fullKey)
    if (raw === null || raw === undefined) return fallback
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

export function writeStorage<T>(key: string, value: T): void {
  const fullKey = getKey(key)
  const raw = JSON.stringify(value)
  try {
    window.localStorage.setItem(fullKey, raw)
  } catch {
    memoryFallback.set(fullKey, raw)
  }
}

export function removeStorage(key: string): void {
  const fullKey = getKey(key)
  try {
    window.localStorage.removeItem(fullKey)
  } catch {
    memoryFallback.delete(fullKey)
  }
}

/** 模拟网络延迟，让 loading 态在纯前端阶段也可验证 */
export function delay(ms = 240): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/** 生成简单唯一 id */
export function createId(prefix = 'id'): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
}
