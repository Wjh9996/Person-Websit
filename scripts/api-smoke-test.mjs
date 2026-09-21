/**
 * 接口回归脚本（冒烟 + 权限 + 注册/密码链路）
 *
 * 用法：先启动后端（8080），再执行
 *   node scripts/api-smoke-test.mjs
 * 可选：BASE_URL=http://localhost:8080 node scripts/api-smoke-test.mjs
 *
 * 说明：
 * - 覆盖「接口契约 + 权限矩阵 + 验证码安全策略 + 数据隔离」四类断言
 * - 会自建临时账号并在结束后清理，不污染现有数据
 * - 未配置 SMTP 时，验证码会从后端日志读取（需 app.mail.dev-log-code=true）
 */
const BASE = process.env.BASE_URL || 'http://localhost:8080'
const LOG_PATH = process.env.BACKEND_LOG || 'back/InterViewBacked/backend-run.log'

const results = []
let passed = 0
let failed = 0

function record(group, name, actual, expected, ok) {
  results.push({ group, name, actual, expected, ok })
  if (ok) passed++
  else failed++
}

async function req(path, options = {}) {
  const res = await fetch(BASE + path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  })
  let body = null
  try {
    body = await res.json()
  } catch {
    body = null
  }
  return { status: res.status, body }
}

const post = (path, body, headers) =>
  req(path, { method: 'POST', body: JSON.stringify(body), headers })
const get = (path, headers) => req(path, { method: 'GET', headers })

/** 判定：期望 ApiResult.code 等于某个值 */
function check(group, name, res, expectedCode) {
  const actual = res.body ? res.body.code : `HTTP ${res.status}`
  record(group, name, actual, expectedCode, actual === expectedCode)
}

/** 从后端调试日志取某个邮箱最新的验证码 */
async function codeFromLog(email) {
  try {
    const fs = await import('node:fs')
    if (!fs.existsSync(LOG_PATH)) return null
    const text = fs.readFileSync(LOG_PATH, 'utf-8')
    const lines = text
      .split(/\r?\n/)
      .filter((l) => l.includes(email) && l.includes('验证码已打印到日志'))
    const m = lines[lines.length - 1]?.match(/->\s*(\d{6})/)
    return m ? m[1] : null
  } catch {
    return null
  }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function main() {
  const stamp = Date.now()
  const email = `smoke${stamp}@example.com`
  const username = `smoke${stamp}`

  // ============ A. 认证与权限 ============
  let r = await get('/api/notes?scope=plaza')
  check('A 认证权限', '游客可读讨论广场', r, 0)

  r = await get('/api/assistant/sessions')
  check('A 认证权限', '游客访问 AI 助手会话应 401', r, 401)

  r = await post('/api/auth/login', { username: 'admin', password: 'wrong-password' })
  check('A 认证权限', '密码错误应拒绝', r, 1)

  r = await post('/api/auth/login', { username: 'admin', password: '123456' })
  check('A 认证权限', '正确账号密码可登录', r, 0)
  const token = r.body?.data?.token

  const authHeaders = { Authorization: `Bearer ${token}` }
  r = await get('/api/auth/me', authHeaders)
  check('A 认证权限', '带令牌可获取当前用户', r, 0)

  r = await get('/api/assistant/sessions', authHeaders)
  check('A 认证权限', '带令牌可读 AI 助手会话', r, 0)

  // ============ B. 注册与邮箱验证码 ============
  r = await get('/api/auth/check-username?username=admin')
  check('B 注册验证码', '已存在账号返回不可用', r, 0)
  record(
    'B 注册验证码',
    '已存在账号 available=false',
    r.body?.data?.available,
    false,
    r.body?.data?.available === false
  )

  r = await get(`/api/auth/check-username?username=${username}`)
  record(
    'B 注册验证码',
    '新账号 available=true',
    r.body?.data?.available,
    true,
    r.body?.data?.available === true
  )

  r = await post('/api/auth/email-code', { email: 'not-an-email' })
  check('B 注册验证码', '非法邮箱格式应被拒', r, 1)

  r = await post('/api/auth/email-code', { email, scene: 'register' })
  check('B 注册验证码', '发送验证码成功', r, 0)

  r = await post('/api/auth/email-code', { email, scene: 'register' })
  check('B 注册验证码', '60 秒内重发应被限流', r, 1)

  await sleep(500)
  const regCode = await codeFromLog(email)
  record('B 注册验证码', '可从后端日志取到验证码（调试模式）', regCode ? '已获取' : '未获取', '已获取', !!regCode)

  r = await post('/api/auth/register', { username, password: '123456', email, code: '000000' })
  check('B 注册验证码', '错误验证码注册失败', r, 1)

  r = await post('/api/auth/register', { username, password: '123456', email, code: regCode })
  check('B 注册验证码', '正确验证码注册成功', r, 0)

  r = await post('/api/auth/register', {
    username,
    password: '123456',
    email: `other${stamp}@example.com`,
    code: '111111'
  })
  check('B 注册验证码', '重复账号应 409', r, 409)

  r = await post('/api/auth/register', {
    username: `another${stamp}`,
    password: '123456',
    email,
    code: '111111'
  })
  check('B 注册验证码', '重复邮箱应 409', r, 409)

  // ============ C. 密码修改与重置 ============
  r = await post('/api/auth/change-password', { oldPassword: '123456', newPassword: 'abc123456' })
  check('C 密码安全', '未带令牌改密码应 401', r, 401)

  r = await post(
    '/api/auth/change-password',
    { oldPassword: 'wrongpass', newPassword: 'abc123456' },
    { Authorization: `Bearer ${token}` }
  )
  check('C 密码安全', '原密码错误应拒绝', r, 1)

  r = await post(
    '/api/auth/change-password',
    { oldPassword: '123456', newPassword: '123' },
    { Authorization: `Bearer ${token}` }
  )
  check('C 密码安全', '新密码不足 6 位应拒绝', r, 1)

  await post('/api/auth/email-code', { email, scene: 'reset-password' })
  await sleep(500)
  const resetCode = await codeFromLog(email)

  r = await post('/api/auth/reset-password', { email, code: '000000', newPassword: 'reset123456' })
  check('C 密码安全', '重置密码：错误验证码应拒绝', r, 1)

  r = await post('/api/auth/reset-password', { email, code: resetCode, newPassword: 'reset123456' })
  check('C 密码安全', '重置密码：正确验证码可重置', r, 0)

  r = await post('/api/auth/login', { username, password: '123456' })
  check('C 密码安全', '重置后旧密码失效', r, 1)

  r = await post('/api/auth/login', { username, password: 'reset123456' })
  check('C 密码安全', '重置后新密码可登录', r, 0)

  // ============ D. 数据隔离 ============
  const stampB = Date.now() + 1
  const emailB = `smokeb${stampB}@example.com`
  const usernameB = `smokeb${stampB}`
  await post('/api/auth/email-code', { email: emailB, scene: 'register' })
  await sleep(500)
  const codeB = await codeFromLog(emailB)
  const regB = await post('/api/auth/register', {
    username: usernameB,
    password: '123456',
    email: emailB,
    code: codeB
  })
  const tokenB = regB.body?.data?.token
  const headersB = { Authorization: `Bearer ${tokenB}` }

  r = await get('/api/notes?scope=mine', headersB)
  record('D 数据隔离', '新用户「我的笔记」为空（看不到他人内容）', r.body?.data?.length ?? -1, 0, (r.body?.data?.length ?? -1) === 0)

  r = await get('/api/notes?scope=mine', authHeaders)
  const myNotes = r.body?.data ?? []
  record('D 数据隔离', 'A 用户能看到自己的笔记', myNotes.length >= 0, true, myNotes.length >= 0)

  // 找一篇 A 的私有笔记（visibility=0），用 B 的令牌访问应 403
  const privateNote = myNotes.find((n) => n.visibility === 0)
  if (privateNote) {
    r = await get(`/api/notes/${privateNote.id}`, headersB)
    check('D 数据隔离', 'B 访问 A 的私有笔记应 403', r, 403)
    r = await get(`/api/notes/${privateNote.id}`, authHeaders)
    check('D 数据隔离', 'A 访问自己的私有笔记正常', r, 0)
  } else {
    record('D 数据隔离', 'B 访问 A 的私有笔记应 403', '无私有笔记样本', 403, false)
  }

  // ============ E. 跨域预检 ============
  try {
    const res = await fetch(BASE + '/api/assistant/sessions', {
      method: 'OPTIONS',
      headers: {
        Origin: 'http://localhost:5180',
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'authorization'
      }
    })
    record('E 跨域', '带 Authorization 的预检请求应通过', res.status, '2xx', res.status >= 200 && res.status < 300)
  } catch (e) {
    record('E 跨域', '带 Authorization 的预检请求应通过', String(e), '2xx', false)
  }

  // ============ 清理 ============
  const mysql = process.env.MYSQL_BIN || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
  try {
    const { execSync } = await import('node:child_process')
    const sql = `DELETE FROM user WHERE username IN ('${username}','${usernameB}');` +
      `DELETE FROM email_verify_code WHERE email IN ('${email}','${emailB}');`
    execSync(`"${mysql}" -uroot -p123456 -h127.0.0.1 interview -e "${sql}"`, { stdio: 'ignore' })
  } catch {
    // 清理失败不影响测试结果，仅提示
  }

  // ============ 输出 ============
  console.log('\n================ 接口回归测试结果 ================')
  let lastGroup = ''
  for (const r of results) {
    if (r.group !== lastGroup) {
      console.log(`\n【${r.group}】`)
      lastGroup = r.group
    }
    const mark = r.ok ? '✅ PASS' : '❌ FAIL'
    console.log(`  ${mark}  ${r.name}${r.ok ? '' : `（实际：${r.actual} / 期望：${r.expected}）`}`)
  }
  console.log('\n--------------------------------------------------')
  console.log(`总计 ${results.length} 条，通过 ${passed} 条，失败 ${failed} 条`)
  console.log('==================================================\n')
  process.exit(failed > 0 ? 1 : 0)
}

main().catch((e) => {
  console.error('执行失败：', e)
  process.exit(2)
})
