package org.cas.client.platform.bar.action;

import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import com.just.print.app.AikaPos;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.model.DBUtil;
import org.cas.client.platform.bar.uibeans.ISButton;
import org.cas.client.platform.casutil.L;

import java.util.Date;

public class Cmd_GiftCard implements SamActionListener {

	private static Cmd_GiftCard instance;
	private Cmd_GiftCard() {}
	public static Cmd_GiftCard getInstance() {
		if(instance == null)
			instance = new Cmd_GiftCard();
		return instance;
	}
	
	private ISButton sourceBtn;
	
	public ISButton getSourceBtn() {
		return sourceBtn;
	}
	@Override
	public void setSourceBtn(ISButton sourceBtn) {
		this.sourceBtn = sourceBtn;
	}

	@Override
	public void onClick(View o) {

		final String giftCardNumber = JOptionPane.showInputDialog(null, BarFrame.consts.Account());
		if(giftCardNumber == null || giftCardNumber.length() == 0)
			return;
		
		StringBuilder sql = new StringBuilder("SELECT * from hardware where category = 2 and name = '").append(giftCardNumber)
				.append("' and (status is null or status = 0)");
		try {
			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
            int tmpPos = rs.getCount();
            if(tmpPos == 0) {	//if there's no this coupon number in database, then warning and return.
            	JOptionPane.showMessageDialog(BarFrame.consts.InvalidCoupon());
            	return;
            }else {			//if the number is OK.
            	//get out every field of first matching record.
                tmpPos = 0;
                rs.moveToNext();
                int id = DBUtil.getCursorInt(rs,"_id");
                int category = DBUtil.getCursorInt(rs,"style");
                String productCode = DBUtil.getCursorString(rs,"IP");
                int value = DBUtil.getCursorInt(rs,"langType");
                if(value <=0) {
                    JOptionPane.showMessageDialog(BarFrame.consts.InvalidCoupon());
                    return;
                }
                //show up the payDialog, waiting for user to input money, after confirm, the money should be deduct from the account of this card
                BarFrame.payDlg.maxInput = (float)(value / 100.0);
                BarFrame.setStatusMes(BarFrame.consts.CurrentBalanceMsg() + BarFrame.payDlg.maxInput);
                Cmd_Pay.getInstance().onClick(o);
                
                if (BarFrame.payDlg.inputedContent != null && BarFrame.payDlg.inputedContent.length() > 0) {
                    float newBalance = (float)(value / 100.0) - Float.valueOf(BarFrame.payDlg.inputedContent);
	                sql = new StringBuilder("update hardware set status = ").append(DBConsts.EXPIRED)
                    		.append(" where _id = ").append(id);
                    AikaPos.db.execSQL(sql.toString());
					sql = new StringBuilder("INSERT INTO Hardware (name, category, langType, ip, style, status) VALUES ('").append(giftCardNumber)
					    .append("', 2, ").append(Math.round(newBalance * 100)).append(", '")
					    .append(BarUtil.df.format(new Date())).append("', ").append(LoginDlg.USERID).append(", ")
					    .append(DBConsts.ORIGINAL).append(")");
					AikaPos.db.execSQL(sql.toString());
                }
            }
		}catch(Exception exp) {
			L.e("Redeem Coupon", "exception happend when redeem coupon: " + sql, exp);
		}
    	
	}
}
