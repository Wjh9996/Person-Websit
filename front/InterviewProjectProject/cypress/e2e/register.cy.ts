/**
 * 注册全流程（端到端）：邮箱验证码 + 账号唯一
 *
 * 验证码怎么拿？
 * - 本地/联调环境未配置 SMTP 时，后端开启 app.mail.dev-log-code 会把验证码打进日志，
 *   这里直接从后端日志里读（仅调试模式可用，生产环境该开关必须关闭）；
 * - 若已配置真实邮件服务，本用例需要改成从测试邮箱取码或改用固定测试账号。
 */
describe('注册：邮箱验证码 + 账号唯一', () => {
  const stamp = Date.now()
  const username = `cypress${stamp}`
  const email = `cypress${stamp}@example.com`

  /** 从后端调试日志里取该邮箱最新一条验证码 */
  function codeFromDevLog(): Cypress.Chainable<string> {
    return cy
      .readFile('../../back/InterViewBacked/backend-run.log', 'utf-8', { timeout: 15000 })
      .then((text: string) => {
        const lines = text
          .split(/\r?\n/)
          .filter((line) => line.includes(email) && line.includes('验证码已打印到日志'))
        const last = lines[lines.length - 1] ?? ''
        const matched = last.match(/->\s*(\d{6})/)
        expect(matched, `后端日志中未找到 ${email} 的验证码`).to.not.equal(null)
        return String((matched as RegExpMatchArray)[1])
      })
  }

  it('验证码不正确时不能注册成功', () => {
    cy.visit('/login?mode=register')
    cy.get('#username').type(username)
    cy.get('#email').type(email)
    cy.get('#password').type('123456')
    cy.get('#confirm').type('123456')
    cy.contains('获取验证码').click()
    cy.contains('分钟内有效').should('be.visible')

    cy.get('#code').type('000000')
    cy.contains('注册并登录').click()
    cy.contains('验证码不正确').should('be.visible')
    cy.location('pathname').should('eq', '/login')
  })

  it('账号唯一性检查会提示已被注册', () => {
    cy.visit('/login?mode=register')
    cy.get('#username').type('admin')
    cy.contains('该账号已被注册').should('be.visible')
  })

  it('正确验证码可完成注册并自动登录', () => {
    cy.visit('/login?mode=register')
    cy.get('#username').type(username)
    cy.get('#email').type(email)
    cy.get('#password').type('123456')
    cy.get('#confirm').type('123456')
    cy.contains('获取验证码').click()

    codeFromDevLog().then((code) => {
      cy.get('#code').type(code)
      cy.contains('注册并登录').click()
      // 注册成功后跳转个人中心，且本地已存令牌
      cy.location('pathname', { timeout: 15000 }).should('eq', '/profile')
      cy.window().then((win) => {
        expect(win.localStorage.getItem('user-token')).to.not.equal(null)
      })
    })
  })
})
