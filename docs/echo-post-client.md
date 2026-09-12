# Echo Post Client

当前分支：`Echo-Post-Client`（Git 不允许分支名含空格）。继续使用原有 Android Studio 项目和 `app` 运行配置。

启动入口为 `com.just.print.echo.EchoPostActivity`，直接显示库存编辑对话框，不初始化原 POS Application、登录、数据库或打印服务。

## 使用

1. 电脑上的艾卡 POS 需要包含配套的 `/AikaPos/stockProducts` 接口。本次配套源码位于 `C:\AI\workspace\security\simonstyle`，需要重新构建并运行更新后的 POS 才能使用。
2. 在 POS 打开 Beer Moving 的 Inventory Adjustment 模式，窗口显示 HTTP 地址和 `X-AikaPos-Token`。此接口随库存窗口开启/关闭，重新开启后令牌会变化。
3. 手机右上角点“设置”，输入电脑 IP，例如 `192.168.1.20`，自动使用 `8880` 端口；也可填写 `192.168.1.20:8881` 或完整 HTTP(S) 根地址。填入库存窗口的令牌并保存。手机与电脑应能通过局域网互相访问。
4. 第一行输入名称，按数据库 `MNEMONIC` 包含匹配。一次响应包含全部匹配产品的 ID、名称、条码、分类及四项库存值，保存在手机内存中。下拉项附条码或 ID，以区分重名产品。选中时直接填充缓存数据，不发送额外 HTTP 请求。
5. 修改第二行 Stock1、Stock2、Stock3 和第三行 Monque，点第四行“提交”。成功或失败均有可见提示；焦点回到名称框并全选。

Monque 沿用文本接口的缺货数量含义：数据库 `STOCK1_SHORTAGE=-2` 显示为 `2`，正值存储显示为负数；不改变库存单位。允许有符号整数，以便完整保留 POS 的现有库存值。

提交携带产品 ID、修改值和搜索时的原始值；服务端用单条条件 UPDATE 验证四个原始数量，存在并发变更时返回 409，提示重新搜索，避免直接覆盖。失败或响应超时后不自动重试，应重新搜索确认当前库存。

## 配套服务端文件

- `StockProductStore.java`：啤酒名称查询、完整库存响应、按 ID 原值校验更新。
- `InventoryCommandServer.java`：新增 GET/POST `/AikaPos/stockProducts`，沿用令牌验证和库存窗口生命周期。
- `BeerMovingReviewDlg.java`：接入新接口并刷新桌面库存缓存。
- `StockProductStoreCheck.java`：独立内存数据库与本地 HTTP 验证，不写真实店铺数据。

原 `POST /AikaPos/stockCommand` 文本接口仍保留。

## Android Studio

打开 `C:\AI\workspace\justprint`，确认分支为 `Echo-Post-Client`。Gradle 同步完成后，使用 `app` 配置，Launch 选择 Default Activity，再选择连接的手机并 Run。

仍使用原包名 `com.just.print`，Run 会更新手机上同包名应用；数据库文件保留，本分支不读取或更新它。APK：`app\build\outputs\apk\debug\app-debug.apk`。
