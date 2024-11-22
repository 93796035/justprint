package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.action.Cmd_Refund;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.cascustomize.CustOpts;

public class RefundItemDlg extends SamDialog {

	private SalesPanel salesPanel;
	private BillPanel billPanel;
	private TextView titleView;
	private Button ok;
	private FrameLayout billContainer;

	private float oldSubTotal;

	public RefundItemDlg(SalesPanel salesPanel) {
		this.salesPanel = salesPanel;
		this.billPanel = salesPanel.billPanel;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		setTitle(BarFrame.consts.Refund());
		initComponent();
		reLayout();

		return dialog;
	}

	private void initComponent() {
		titleView = createLabel(title, 22, Gravity.LEFT, Color.WHITE);
		ok = createNumButton(BarFrame.consts.OK());
		billContainer = new FrameLayout(getContext());
		billContainer.setId(View.generateViewId());
		ok.setOnClickListener(this);
		// 获取旧的总价
		oldSubTotal = Float.parseFloat(billPanel.valTotlePrice.getText().toString());
	}

	private void reLayout() {
		FrameLayout rootLayout = new FrameLayout(BarFrame.instance);
		dialog.setContentView(rootLayout);

		rootLayout.addView(titleView, getLayoutParams(titleView, 16, 50, FrameLayout.LayoutParams.WRAP_CONTENT, 60));
		rootLayout.addView(billContainer, getLayoutParams(billContainer, titleView.getTop() + titleView.getMinimumHeight() + CustOpts.HOR_GAP,
				CustOpts.VER_GAP, billPanel.getView().getWidth(), billPanel.getView().getHeight()));
		rootLayout.addView(ok, getLayoutParams(ok, billContainer.getTop() + billContainer.getMinimumHeight() + CustOpts.VER_GAP * 2,
				billContainer.getMinimumWidth()/2 + CustOpts.HOR_GAP - BUTTON_WIDTH,
				BUTTON_WIDTH * 2, BUTTON_WIDTH));

		// 初始化 BillPanel
		if (BarFrame.menuPanel.isAdded()) { // 加载 BillPanel 和 MenuPanel
			FragmentManager fragmentManager = salesPanel.billPanel.getFragmentManager();
			if (fragmentManager != null) {
				fragmentManager.beginTransaction()
						.remove(billPanel)
						.commitNow();
			}
		}
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(billContainer.getId(), billPanel);
		transaction.commit();
		rootLayout.setPadding(20, 20, 20, 20);
	}

	private void performRefund() {
		String curContent = String.valueOf(billPanel.subTotal);
		if (curContent.isEmpty()) {
			return;
		}

		float toRefundThisTime = oldSubTotal - Float.parseFloat(billPanel.valTotlePrice.getText().toString());

		int refundedLifeTime = billPanel.getStatus();

		if (toRefundThisTime > oldSubTotal) {
			return;
		} else {
			refundedLifeTime = Cmd_Refund.checkRefundAmount(toRefundThisTime, refundedLifeTime);
			if (Integer.MAX_VALUE == refundedLifeTime) {
				return;
			}

			// 过期的输出当账单是基于项目退款时
			billPanel.expireAndRegenerateModifiedOutputs(DBConsts.COMPLETED);
		}

		Cmd_Refund.refundRoutineSteps(billPanel, toRefundThisTime, refundedLifeTime);
	}

	@Override
	public void onClick(View v) {
		if (v == ok) {
			performRefund();//should be done here or in outside?
			dismiss();
		}
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		// 如果用户点击旁边区域关闭refund dialog, 需要将 BillPanel 返回到 SalesPanel
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		salesPanel.billPanel = billPanel; // 你需要在 SalesPanel 中实现 addBillPanel 方法来处理 Fragment 的添加
		salesPanel.reLayout();
	}
}
