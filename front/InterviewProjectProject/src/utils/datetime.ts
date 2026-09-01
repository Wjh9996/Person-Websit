/** 日期格式化与相对时间 */

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n)
}

/** 2026/09/01 21:30 */
export function formatDateTime(input: string | number | Date): string {
  const d = new Date(input)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 2026/09/01 */
export function formatDate(input: string | number | Date): string {
  const d = new Date(input)
  if (Number.isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())}`
}

/** 3 分钟前 / 2 天前 */
export function fromNow(input: string | number | Date): string {
  const d = new Date(input).getTime()
  if (Number.isNaN(d)) return '-'
  const diff = Date.now() - d
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 30 * day) return `${Math.floor(diff / day)} 天前`
  return formatDate(input)
}
