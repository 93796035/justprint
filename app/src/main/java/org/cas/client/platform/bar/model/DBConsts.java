package org.cas.client.platform.bar.model;

/**
 * don't use negative number, because bill uses negative status to indicate the refund money.
 * @author Simon&Nicholas
 *
 */
public interface DBConsts {
	int ORIGINAL = 0;
	int OPENNED = 1;
	int SAVED = 2;
	int BILL_PRINTED = 5;
	int ITEM_PRICE_CHANGED = 6;
	int SUSPENDED = 8;
	int COMPLETED = 10;
	int VOIDED = 100;	//voided bill, removed SALE_RECORD(if some dish was removed from a "billprinted" status bill, the SALE_RECORD record should have voided sgtatus.
	int EXPIRED = 500;	//put it here because it might be more closer to deleted. and may be we can choose to don't show it in check list.
	int DELETED = 1000;
	
	int LOGO_TYPE = 10000;
}
