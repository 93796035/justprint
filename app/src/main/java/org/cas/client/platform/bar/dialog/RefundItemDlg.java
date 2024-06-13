package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.ui.panel.SalesPanel;

import org.cas.client.platform.bar.action.Cmd_Refund;
import org.cas.client.platform.bar.model.DBConsts;
import org.cas.client.platform.bar.panel.BillPanel;

public class RefundItemDlg extends DialogFragment {

	private SalesPanel salesPanel;
	private BillPanel billPanel;
	private Button ok;
	private float oldSubTotal;

	public RefundItemDlg(SalesPanel salesPanel) {
		this.salesPanel = salesPanel;
		this.billPanel = salesPanel.billPanel;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(BarFrame.consts.Refund());
		return dialog;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_refund_item, container, false);

		ok = view.findViewById(R.id.okButton);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performRefund();
			}
		});

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
		transaction.replace(R.id.billPanelContainer, billPanel);
		transaction.commit();

		// 获取旧的总价
		oldSubTotal = Float.parseFloat(billPanel.valTotlePrice.getText().toString());

		return view;
	}

	private void performRefund() {
		String curContent = String.valueOf(billPanel.subTotal);
		if (curContent == null || curContent.isEmpty()) {
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

		// 将 BillPanel 返回到 SalesPanel
		// 使用 FragmentTransaction 将 BillPanel 返回到 SalesPanel
		FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
		transaction.remove(billPanel);
		transaction.commit();

		// 假设 salesPanel 是一个容器 Fragment，可以通过父 Activity 获取
		SalesPanel salesPanel = (SalesPanel) BarFrame.instance.panels[2];
		if (salesPanel != null) {
			salesPanel.billPanel = billPanel; // 你需要在 SalesPanel 中实现 addBillPanel 方法来处理 Fragment 的添加
		}

		dismiss();
	}


	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		// 将 BillPanel 返回到 SalesPanel
		salesPanel.reLayout();
	}
}
