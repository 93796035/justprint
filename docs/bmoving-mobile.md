# 手机 Stock Moving 计划

SimClient 有 Product Editing、Stock Moving、ID Checking 三个页面。

- 每次进入 Stock Moving，从 AikaPos 的 `/AikaPos/stockProducts` 获取最新商品、库存及销量，再在手机重新计算建议。旧的手工数量和排除状态不覆盖新建议；刷新失败时显示错误并禁用提交。
- 左侧勾选表示该项已搬完：立即仅提交该项，成功后从当前列表移除。每项使用独立 UUID 和服务器事务，生成一张已完成移动单并更新 Stock3 → Stock1、MANQUE。
- 已移除整单同步按钮、Show removed items、每行 × 和删除/恢复操作。排序支持数量、名称、分类。
- QTY 为箱数，可编辑正整数；实际库存单位为 QTY × unitsPerCase。点击商品可查看换算，双击可编辑商品。
- 提交前持久保存原始请求。结果不明时保留该项并显示 Retry sync；重试及重启恢复使用相同 UUID，防止重复扣库存。
- 同步后刷新手机库存缓存。其余建议保留原计算输入指纹，服务器拒绝库存或商品已经变化的建议，重新进入页面即可获取新建议。
- 旧版遗留的未确认整单请求会按原始内容恢复确认，不改写已发送请求。

## 配套服务器

源码位于 `C:\AI\workspace\security\simonstyle`。需更新并重启 Windows POS，手机与电脑使用同一个现有库存 HTTP 服务地址。提交时电脑端需要已登录，并关闭桌面库存审核窗口。

`GET /AikaPos/movingDraft` 返回原始计算输入（products、movingSales、箱规/分组元数据），不返回预建计划，也不创建计划。

`POST /AikaPos/movingDraft` 接收 `action=submit`、稳定的 `draftId`、各行状态/箱数/单位数/商品分组原值指纹。成功返回 `billId` 与 `draftId`。`MOBILE_MOVING_RECEIPT` 保存原始规范化内容和单据 ID，和库存、BILL、OUTPUT 一起提交；重复请求返回原单据。

最终单据使用 `STOCKIN_PLAN_COMPLETED` 状态、`STOCK_MOVE:3_TO_1` 备注和当前 POS 操作员，能从 Windows 进货/移动历史查阅。使用唯一单据关联，避免同秒提交串单。

## 验证

`tools/checks/MovingPlanCheck.java` 使用独立 HSQLDB 内存数据库，覆盖本地生成、箱规、最近销售日下限、序列化后勾选/删除保留、未完成数量重算、事务回滚、同组库存冲突、历史单据、幂等提交与 HTTP。不会连接店铺数据库。

手机及服务端各保存同一份纯 Java `org.cas.shared.inventory.MovingPlan`；改动算法时需同步两处并运行上述检查。Windows 的商品分组/箱规元数据来自原有 TodoDialog 辅助方法。
