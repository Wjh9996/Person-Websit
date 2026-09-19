/**
 * 公开内容浏览（端到端）
 *
 * 游客可读：首页、讨论广场、笔记详情、简历页。
 * 需要登录的页面由 auth-guard.cy.ts 覆盖。
 */
describe('公开内容浏览', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('首页展示笔记统计与卡片', () => {
    cy.visit('/')
    cy.get('body').should('contain.text', '笔记')
  })

  it('讨论广场未登录可浏览', () => {
    cy.visit('/plaza')
    cy.location('pathname').should('eq', '/plaza')
  })

  it('从广场点开第一条笔记能进入详情页', () => {
    cy.visit('/plaza')
    cy.get('a[href^="/notes/"]')
      .first()
      .then(($link) => {
        const href = $link.attr('href') as string
        cy.visit(href)
        cy.location('pathname').should('eq', href)
      })
  })

  it('简历页公开可读，且未登录时只保留一个编辑入口', () => {
    cy.visit('/resume')
    cy.location('pathname').should('eq', '/resume')
    cy.contains('新增').should('not.exist')
  })

  it('关于页展示 GitHub 源码入口', () => {
    cy.visit('/about')
    cy.contains('GitHub 源码').should('have.attr', 'href', 'https://github.com/Wjh9996/Person-Websit')
  })
})
