# AikaPos Security POS Gap Report

Generated for branch `AikaPos`.

## Android entry and layout

- Present: Android launches `com.just.print.app.BarFrame` from `AndroidManifest.xml`.
- Present: `BarFrame` initializes the POS frame, status bar, table panel, bill list panel, sales panel, settings panel, menu panel, bill panel, payment dialog, discount dialog, number dialog, printer/web-SRM helpers, login, and counter mode.
- Present: `activity_main.xml` uses a POS frame layout similar to the security subframe flow: top status/action row, center fragment container, bottom status area.

## Functions already present in Android

- Login and operator/admin roles.
- Table mode, bill list mode, sales mode, settings mode.
- Table open/close flow.
- Add bill, split/equal bill, combine bill, move/split item.
- Product/menu display from `DISH` and `CATEGORY`.
- Add/remove/change quantity, change price, discount item, discount bill.
- Temporary bill, print bill, print all, send/kitchen print, suspend.
- Cash/debit/visa/master/other payment dialog flow.
- Refund/return/void oriented actions already exist in the Android action package.
- Printer setup and basic local printer records.
- MEV/Web-SRM related Android classes are present.
- Basic statistics/report dialog classes are present.
- User/operator and operation log tables exist.

## Security functions now wired into Android command registry

The Android command registry now has entries for these security POS commands:

- Sales history
- End shift / settlement
- Stock in
- Hold
- Inventory
- Statistic
- Bottle pay
- Loan out
- Repayment
- Category sell
- Instant pay
- Loto pay

The report-oriented commands route to the existing Android report dialog. The rest are intentionally visible placeholders so the Android UI exposes the same command surface while each dedicated screen is migrated.

## Schema compatibility added in Android

Startup now safely re-runs table creation and schema evolution. Added compatibility fields/tables for:

- `BILL`: customer/VIP/cloud-sync/terminal/purchase-plan fields.
- `DINING_TABLE`, `CATEGORY`, `DISH`: cloud-sync and inventory-oriented fields.
- `DISH`: product-like fields for code, unit, brand, cost, stock, cover days, weekly/monthly sales qty, and case qty.
- `CUSTOMIZATIONS`, `CUSTOMIZED_RULE`, `USER`, `PRINTER`, `SYSTEMINFO`: cloud-sync status fields.
- New tables: `MODIFICATION`, `TODO`, `CLOUDSYNC_META`, `PRODUCT_PRICE_HISTORY`.
- New compatibility tables for security naming: `PRODUCT`, `OUTPUT`, `EMPLOYEE`, `HARDWARE`.
- Added indexes used by bill lookup, sale record lookup, product/category display, and price history.

## Major differences still remaining

- Security POS has Swing desktop-only panels/dialogs that cannot be copied directly into Android without UI rewrites.
- Security uses desktop/PIM/HSQLDB naming such as `PRODUCT`, `OUTPUT`, `EMPLOYEE`, and Swing components; Android currently uses SQLite tables such as `DISH`, `SALE_RECORD`, and Android fragments/dialogs.
- Security cloud sync classes depend on JDBC/PIMDBModel and cannot run unchanged on Android.
- Customer, coupon, gift card, VIP account, stock-in workflow, purchase planning, sales history detail screens, price history UI, offline log UI, and advanced statistics exist in security but are not fully native Android screens yet.
- OpenAI voice command and camera capture flows exist only in the security desktop code path.

## Implementation status

- Done: identified Android POS entry and active UI path.
- Done: mapped the main security-only command surface.
- Done: added Android command classes for the security-only POS actions.
- Done: registered those commands in `CmdBtnsManagerDlg`.
- Done: added non-destructive database schema compatibility for the next migration steps.
- Remaining: native Android implementations for the larger Swing-only workflows.
