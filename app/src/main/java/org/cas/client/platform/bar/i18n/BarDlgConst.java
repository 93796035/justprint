package org.cas.client.platform.bar.i18n;

public interface BarDlgConst {
    int SCROLLBAR_WIDTH = 40;
    int TotalAreaHeight = 70;

    String semicolon = ";"; // 分隔符
    String delimiter = ","; // 分隔符

    String CloseBatchFirst();
    String foundUnclosedTable();
	String createComboMenu();
	String ComboMenu();
    String CATEGORIES();
    String nothingToMove();
	String ChangeTable();
	String InvoiceIdx();
	String OrderIdx();
	String confirmMsgOfMovingTable();
    String discountIsRemoved();
	String Applied();
	String VoidReason();
    String ComboCreated();
    String giveAName();
    String willDiscount();
	String IfContains();
    String BatchClosedSuccefully();
    String cleanTheBatch();
	String itemAdded();
	String IgnorePrinterError();
    String OK();
    String Cancel();
    String ChangeDate();
    String ViewDetail();
    String Close();
    String Tip();
    String ServiceFee();
    String Serv();
    String SERVICEFEE();
    String ServiceFeeNotice();
    String Changes();
    String AddToTip();
    String Status();
    String Apply();
    String PRINT();

    String EnterCashPayment();
    String EnterDebitPayment();
    String EnterVisaPayment();
    String EnterMasterPayment();
    String EnterOtherPayment();
    
    String ChangeMode();
    String AddTable();
    String BILLINGHISTORY();
    String OpenDrawer();
    String VolumnDiscount();
    String DISCOUNT_BILL();
    String VolumnDiscountNotice();
    String WaiterReport();
    String Reservation();
    String Report();
    String MENU();
    String CheckOut();
    String WORK_LOG();
    
    String Menu();
    String Language1();
    String Language2();
    String Language3();
    String DSPINDEX();
    String PRICE();

    String EXACT_AMOUNT();
    String CASH();
    String PAY();
    String REMOVEITEM();
    String VOID_ITEM();
    String SPLIT_BILL();
    String Modify();
    String BillInfo();
    String BillFootInfo();
    String ChangePrice();
    String CHANGEPRICE();
    String ChangePriceNotice();
    String QTY();
    String QTYNOTICE();
    String DISC_ITEM();
    String DISCITEM();
    String DISC_ITEMNotice();
    String TEMPORARY_BILL();

    String DEBIT();
    String CREDIT();
    String VISA();
    String MASTER();
    String CANCEL_ALL();
    String VOID_ORDER();
    String SETTINGS();
    String RETURN();
    String MORE();

    String EQUL_BILL();
    String FAST_DISCOUNT();
    String QUICK_OPEN();
    String MODIFY();
    String DISC_VOLUMN();

    String PRINTER();
    String OnlyOneShouldBeSelected();
    String InvalidInput();
    String ADMIN_MODE();
    String USE_MODE();
    default String Title() {return "AikaPos";}
    String DuplicatedInput();
    String NoBillSeleted();
    String NotPayYet();
    String AlreadyRefund();
    String UnSendRecordFound();
    String Colon();
    String Operator();
    String TABLE();
    String BILL();
    String Bill();
    String UnSavedRecordFound();
    String COMFIRMDELETEACTION();
    String COMFIRMDELETEACTION2();
    String COMFIRMDELETECATEGORY();
    String COMFIRMLOSTACTION();
    String COMFIRMCLEARMONEYRECEIVED();
    String ConfirmPayAgain();
    String ClosedBillCantVoid();
    String ConvertClosedBillBack();
    String SetCatogoryFirst();
    String SendItemCanNotModify();
    String reCeivedMoneyNotEnough();
    String LeftMoney();
    String UnSavedContentWillBeLost();
    String NoNewSelectionToCancel();
    String Due();
    String OPENTIME();
    String EndTime();

    String EXACT();

    String BillPageRow();
    String BillPageCol();
	String IsTrainingMode();
    String IsSingleUser();
    String IsTaxNotAllowDiscount();
    String IsServiceFeeAffectTax();
    String IsBuffetMode();
    String IsFastFoodMode();
    String IsPrintBillWhenPay();
    String StartTimeOfDay();
    String PrinterMinReachTime();
    String ServerHost();
    String DBHost();
    String PrinterError();
    String PrinterSettingError();
    String NoSerialPrinterFound();
    String NoComPortFound();
    
    String OtherPay();
    String ProdNumber();
    String ProdName();
    String Count();
    String Price();
    String Subtotal();
    String Total();
    default String QST() {return "TVQ";};
    default String GST() {return "TPS";};
    String PricePomp();
    String MenuPomp();
    String ModifyPomp();


    String Discount();
    String Receive();
    String comment();
    String OpenTime();
    
    String Change();

    String AddBill();
    String PrintAll();
    String PrintOneBill();
	String PrintOneInvoice();
    
    String EqualBill();
    String SplitItem();
    String MoveItem();
    String CombineAll();
    String UnCombine();
    String Refund();
    String RefundNotice();
    String SuspendAll();
    String SUSPEND();
    String CancelAll();
    String ProdCodeLength();
    String Shoestring();
    String Size();
    String[] Sizes();
    String Name();
    String TableName();
    String IPAddress();
    String Categary();
    String CreateTime();
    
    String PrinterSupport();
    
    String SaleReport();
    String BillingHistory();
    String FROM();
    String TO();

    String Code();
    String PHOTO();
    String LASTNAME();
    String FIRSTNAME();
    String DISPLAYAS();
    String NICKNAME();
    String JOB();// job
    String RELATION();
    String IS_LADY();
    String PASSWORD();
    String Anniversary1();
    String Anniversary2();
    String JOIN_TIME();
    String ADDRESS();
    String PHONE();
    String MOBILE();
    String EMAIL();
    static String lang1(){return "lang1";}
    static String lang2(){return "lang2";}
    static String lang3(){return "lang3";}
    static String lang4(){return "lang4";}
    String SALARY();
    String WEBPAGE();
	public static String[] langs() {return new String[] { lang1(), lang2(), lang3(), lang4() };}
    String[] ANNIVERSARYTYPE();;
    
    String GENERAL();
    String EmployeeInfo();
    String JobTitle();
    String NickName();
    String Cellphone();
    String PhoneNum();
    String HomeAddress();
    String MailAddress();
    String Account();

    String CompName();
    String CompAddress();
    String CompTel();
    String CompFax();

    String QQ();
    String MainPage();
    String Sex();
    String Time();
    String Salary();
    String Allowances();
    String TaxNumber2();
    String IDCARD();
    String BIRTHDAY();
    String TaxNumber1();
    String Type();
    String Note();
    
    String Add();

    String WorkRecs();   
    String MoneyInBox();
    String Calculate();
    String Profit();
    
    String Product();
    String InputRecs();
    String Supplier();
    
    String PasswordMakeSure();
    String PasswordNotEqual();
    
    String MUser();
    default String[] USERTYPE() {
    	return new String[] {"Operator","Admin","SuperAdmin"};
    };

    String AddNewUser();
    String ModiFyUser();
    String DeleteUser();
    
    String UserNameInUsed();

    String FORMATERROR();
    String AvailableModification();
    String AddNewModificationItem();
    String ADD_TO_LIST();
    String Delete();
    String RESET();

    String TablePanelBK();
    String LoginBK();
    String BillPanelBK();
    String SalesPanelBK();
    String SettingsPanelBK();
    String CategoryBtnBK();
    String DishBtnBK();
    String FunctionBtnBK();
    String TableBtnBK();
    String SelectedTableBtnBK();
    String NumberBtnBk();
	String ArrowBtnBk();
    String Color();
	String IsHideRecordFromOtherWaiter();
	String IsDisplayBillInKitchen();
	String IsDisplayWaiterInKitchen();
	String TreatPricePromtAsTaxInclude();
	String IsShowCustomerFrame();
	String workOnOnlyUnclosedBills();
	String nonEmptyBillFound();
	String FONTSIZE();
	String WIDTH();
	String COUPON();
	String SKIP();
	String UBER();
	String FANTUAN();
	String DOORDASH();
	String GIFTCARD();
	String activeCode();
	String couponCode();
	String isRedeemed();
	String InvalidCoupon();
	String Copy();
	String couponNotApplyToBill();
	String IsSavePrintInvoiceWhenBilled();
	String AtLeastOneShouldBeSelected();
	String printing();
	String isWaiterAllowedToDiscount();
	String isWaiterAllowedToChangePrice();
	String isToCustomer();
	String CurrentBalanceMsg();
	String History();
	String Balance();
	String Employee();
	String ActionNotAllowed();
	String OrgonizeCommands();
	String RestartNeeded();
	String Login();
	String ApplyToList();
	String ApplyToCategory();
	String getPennyRounded();
	String AllSelectedItemWillBeDeleted();
	String ComboName();
	String COMBO();
	String SEND();
	String Position();
	String confirmRefund();
	String confirmSuspend();
	String REASON();
	String Customer();
	String Unknow();
	String Male();
	String Female();
	String ToMail();
	String ToFemale();
	String AddPrinterFirst();
	String bkMenu();
	String restoreMenu();
	String bkConfig();
	String restoreConfig();
	String moreOptions();
	String barchIndex();
	String AtLeastTwoTableShouldBeSelected();
	String showOnlyMine();
	String today();
	String thisMonth();
	String exceptionWhenSendingOutRequestForSignatureDigitalCertificate();
	String WebMevWaitTime();
	String ConfirmDeleteAllRecords();
	String PrintDuplicatedBill();
	String AutoLogoutTime();
	String  sendDocument();
	String inSilentMode();
    String thisyear();
    String lastyear();
    String ThisYearOrLastYear();
    String WholeBillRefund();
    String itemBasedRefund();
}
