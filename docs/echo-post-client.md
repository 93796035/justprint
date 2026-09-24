# AikaPOS SIM Client

当前分支：`SimClient`（Git 不允许分支名含空格）。继续使用原有 Android Studio 项目和 `app` 运行配置。

启动入口为 `com.just.print.simclient.SimClientActivity`，直接显示库存编辑对话框，不初始化原 POS Application、登录、数据库或打印服务。

## 使用

1. 电脑上的艾卡 POS 需要包含配套的 `/AikaPos/stockProducts` 接口。本次配套源码位于 `C:\AI\workspace\security\simonstyle`，需要重新构建并运行更新后的 POS 才能使用。
2. 更新后的 POS 启动完成 36 秒后自动开启 HTTP 服务，关闭库存窗口不会停止服务，不需要连接令牌。
3. 手机点击 左上角的 Settings，输入电脑 IP，例如 `192.168.1.20`，自动使用 `8880` 端口；也可填写 `192.168.1.20:8881` 或完整 HTTP(S) 根地址。保存 IP 即可。手机与电脑应能通过局域网互相访问。
4. 先点击 Settings 下方的 Refresh，一次下载接口提供的全部未删除商品资料并持久保存到手机。本地库按服务器地址隔离，启动时直接载入内存。名称框上方的 Category 下拉框列出下载数据中的各个类别，默认选择啤酒类（支持 BIÈRE/BEER/啤酒名称）；没有啤酒类时选择第一个类别，空类别显示 Uncategorized。切换类别清除当前产品选择，只在该类别内进行名称匹配及共同文字补全，不发 HTTP 请求；选中也不请求。下拉项附条码或 ID，以区分重名产品。
5. 编辑第一行 Stock1、Stock2、Stock3，以及第二行 Monque、Cost、Price，点击 Submit。Cost/Price 接受最多两位小数的非负金额，提交到主程序并保存回手机。成功或失败均有可见提示；焦点回到名称框并全选。BM Moving Plan 入口在状态信息下方的页面底部，以分割线与编辑区隔开。

Monque 沿用文本接口的缺货数量含义：数据库 `STOCK1_SHORTAGE=-2` 显示为 `2`，正值存储显示为负数；不改变库存单位。允许有符号整数，以便完整保留 POS 的现有库存值。

提交携带产品 ID、修改值和搜索时的原始值；服务端用单条条件 UPDATE 验证四个原始数量，存在并发变更时返回 409，提示点击 Refresh，避免直接覆盖。失败或响应超时后不自动重试，应点击 Refresh 确认当前库存。

## 配套服务端文件

- `StockProductStore.java`：全类别商品查询、完整商品字段响应、按 ID 原值校验更新。
- `InventoryCommandServer.java`：新增 GET/POST `/AikaPos/stockProducts`，无需令牌，使用程序级生命周期。
- `BeerMovingReviewDlg.java`：接入新接口并刷新桌面库存缓存。
- `StockProductStoreCheck.java`：独立内存数据库与本地 HTTP 验证，不写真实店铺数据。

原 `POST /AikaPos/stockCommand` 文本接口仍保留。

## Android Studio

打开 `C:\AI\workspace\justprint`，确认分支为 `SimClient`。Gradle 同步完成后，使用 `app` 配置，Launch 选择 Default Activity，再选择连接的手机并 Run。

使用独立应用包名 `com.just.print.simclient`，桌面名称为 `SimClient`，可与其他包名的 JustPrint / AikaPos 同时安装。源码 namespace 保持 `com.just.print`。首次安装需重新设置 POS 地址并 Refresh，原包名应用的设置和商品缓存不会自动迁移。APK：`app\build\outputs\apk\debug\app-debug.apk`。

## 本地持久化

名称输入支持共同后缀自动补全：例如 `BUD` 匹配 `BUDWEISER 355` 和 `BUDWEISER 473` 时，自动补为 `BUDWEISER `，到规格分歧处停止。保留名称包含匹配；重复出现的片段只有在所有出现位置的后续文字一致时才补全。删除、中间编辑及输入法正在组字时不自动补全。补全后仍需点击候选项选择产品，才会载入库存供修改。

提交成功后，采用服务端返回的产品完整记录更新内存和手机私有 SharedPreferences 本地库；确认同步写盘成功才提示 Local data saved。退出或重启后重新加载保存值。提交前持久记录 pending 标志，与新数据保存一起清除；遇到进程中断或未确认结果时，重新启动不会使用旧库编辑，而会要求 Refresh。Refresh 失败保留之前完整数据，成功时整体替换，并包含服务器新增或删除的产品。

## 完整商品资料

手机选中商品后，在 Monque 右侧显示 Cost、Price 输入框，样式与 Stock 字段一致；Settings → View Local Inventory 也显示价格。价格读取手机已保存的商品资料，离线和重启后仍可查看。点击 Refresh 从主程序更新。界面将整数分转换为两位小数金额（例如 875 → 8.75）。旧缓存缺少价格时输入框禁用并提示 Refresh；数据库价格为空时可以输入新价格。Submit 只附加有修改的价格字段，服务器对库存与这些价格作原值校验，以一条条件 UPDATE 一起保存。旧版四个库存字段请求仍兼容；编辑价格需要配套新版 Windows 程序。

Refresh 下载商品表全部字段，JSON 键为小写数据库列名；包括 mnemonic、subject、code、category、price、cost、stock1/2/3、stock1_shortage 等。price 为售价，cost 为成本价，传输和本地保存使用数据库整数分单位（1299 = 12.99）；输入金额通过十进制定点运算转换，不使用浮点数。monque 为 stock1_shortage 的相反数。所有附加字段随本地缓存持久化；提交修改库存及有变化的价格，成功响应保留其他字段。

升级手机端和电脑端后点击 Refresh，才能把以前仅啤酒的缓存换成完整商品库。一次响应上限调整为 64 MiB，超限或下载失败保留之前的完整缓存。

Android Studio 若仍提示旧包名 com.just.print 下 Activity 不存在，请执行 Sync Project with Gradle Files，再运行 app（Default Activity）。当前应用 ID 为 com.just.print.simclient，入口为 com.just.print.simclient.SimClientActivity。

## Menu Panel 类别分组

选择第一列总类时包含截图同一行的全部类别，搜索和共同文字补全使用同一分组：

- BIERE：BIERE、MOLSON、LABATT、SLEEMAN、POPPERS。
- TABAC：TABAC、IMPERIAL、BENSON、JTI MD、TABAC KIT。
- EPICERIE TAX：EPICERIE TAX、FRITO、QUECAN、MONDOUX、BREYERS。
- EPICERIE：EPICERIE、LAIT、PAIN、INSTANT、LOTO。

单独选择品牌或其他子类别时仍只匹配该类别。类别匹配忽略大小写和重音；支持 BEER/BIA、TOBACCO 等总类别别名。规则按本次确认的布局固定，不随桌面按钮重新排序自动变化。

Category 下拉框按当前 Menu Panel 顺序显示：BIERE、MOLSON、LABATT、SLEEMAN、POPPERS，然后 TABAC、IMPERIAL、BENSON、JTI MD、TABAC KIT，接着两组 EPICERIE，其他类别排在后面。主类别采用产品修改对话框配色（金黄、蓝、红、绿、灰）并加粗，子类别缩进；排序和颜色不改变原有筛选规则。

## Product Editing sync verification

After POST /AikaPos/stockProducts, SimClient reads the product again by ID and checks every submitted stock/price value. Success is shown only after this read matches. A mismatch or transport/server failure shows a persistent dialog with the actual server error and POS address, plus Refresh. HTTP reads disable caching. An uncertain result keeps the recovery marker so stale stock cannot be reused.

Regression check: tools/checks/ProductSyncCheck.java uses an isolated in-memory HSQLDB database and the server StockProductStore. It checks a stock update from a second database connection, optimistic-concurrency rejection, wrong product/value detection, and price updates. It never changes live inventory.

Older AikaPos endpoints reject requests containing Cost/Price with `HTTP 400: 需要四个库存字段`. For that exact response, and only when the original request contains more than the four stock fields, SimClient retries once with `stock1`, `stock2`, `stock3`, and `monque`. It then reads the product back and verifies those four values. The success message explicitly says Cost/Price were not saved by that AikaPos version. Other HTTP 400 errors and conflicts are never retried. `ProductSyncCompatibilityCheck.java` covers the fallback payload and retry guard.

Product Editing has no submit button. Stock 1/2/3, Monque, Cost, and Price compare their semantic value with the selected server snapshot when focus leaves the field. Only a changed field starts synchronization; equivalent forms such as `003` versus `3` and `14,58` versus `14.58` do not. Each request contains only the edited value plus the four fields required by the server. The six value fields remain visible and enabled during the request; subsequent blur events queue behind the in-flight save. A verified response updates only the submitted field and the selected product snapshot, so Product Name never gains focus and other edits are not erased. Product name is search-only and never triggers a write. `ProductFieldChangeCheck.java` covers the comparison rules.

Every editable text field selects all of its content when it gains focus. Selection is posted after the touch event so Android's cursor placement cannot immediately collapse it. The product search, six product values, settings address, local-inventory filter, camera recovery search, birth-date fields, and Stock Moving quantities follow the same behavior.

Product search automatically selects a result when the selected category contains exactly one match. If the user began editing from a displayed product and that sole match is the same product, automatic selection and completion are suppressed so the user can continue typing toward a different product. A different sole match is selected immediately; zero or multiple matches retain the dropdown workflow. `ProductAutoSelectionCheck.java` covers this policy.
