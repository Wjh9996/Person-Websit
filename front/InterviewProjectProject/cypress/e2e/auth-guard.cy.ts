/**
 * 未登录权限收口（端到端）
 *
 * 关注点不是"点了会不会报错"，而是"未登录时这些入口压根不该出现"。
 * 前端隐藏只解决体验，真正的拦截在后端，这里验证的是体验层是否收口干净。
 */
describe('未登录权限收口', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('首页不提供编辑类入口', () => {
    cy.visit('/')
    cy.contains('写笔记').should('not.exist')
    cy.contains('新建简历').should('not.exist')
  })

  it('未登录访问「我的笔记」会被重定向到登录页', () => {
    cy.visit('/notes')
    cy.location('pathname').should('eq', '/login')
    cy.location('search').should('include', 'redirect')
  })

  it('未登录访问「个人中心」会被重定向到登录页', () => {
    cy.visit('/profile')
    cy.location('pathname').should('eq', '/login')
  })

  it('未登录访问「AI 助手」会被重定向到登录页', () => {
    cy.visit('/assistant')
    cy.location('pathname').should('eq', '/login')
  })

  it('登录页可切换到注册态，并展示邮箱与验证码输入', () => {
    cy.visit('/login')
    cy.contains('注册').click()
    cy.get('#email').should('be.visible')
    cy.get('#code').should('be.visible')
    cy.contains('获取验证码').should('be.visible')
  })
})
