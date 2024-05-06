package com.just.print.db.expand;

public interface State {
    int ORIGINAL = 0;
    int OPENNED = 1;
    int SAVED = 2;
    int BILL_PRINTED = 5;
    int ITEM_PRICE_CHANGED = 6;
    int SUSPENDED = 8;
    int COMPLETED = 10;
    int VOIDED = 100;	//voided bill, removed output(if some dish was removed from a "billprinted" status bill, the output record should have voided sgtatus.
    int EXPIRED = 500;	//put it here because it might be more closer to deleted. and may be we can choose to don't show it in check list.
    Integer DELETED = 1000;

    int LOGO_TYPE = 10000;
}
