package org.cas.client.platform.bar.dialog;

import static com.just.print.app.AikaPos.db;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;
import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.ui.panel.TablesPanel;
import com.just.print.util.StringUtils;

import org.cas.client.platform.bar.uibeans.TableButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 微软的TextField的长度限制是228（约）， 本类的限制因为没有规格约束，暂定为912（约） // ID I NTEGER IDENTITY PRIMARY KEY // DELETED BIT DEFAULT false //
 * language1 CODE VARCHAR(255) // language2 MNEMONIC VARCHAR(255) // language3 SUBJECT VARCHAR(255) // price PRICE
 * INTEGER // gst FOLDERID INTEGER // qst STORE INTEGER // size COST INTEGER // printer BRAND VARCHAR(255) // CATEGORY
 * CATEGORY VARCHAR(255) // prompmenu UNIT VARCHAR(255) // prompprice CONTENT VARCHAR(255) // ModifyPomp
 * VARCHAR(255)
 */
public class TableDlg extends DialogFragment {

	private TabbleSettingDlg settingTableDlg;
	private List<TableButton> btnTables = new ArrayList<TableButton>();	//@NOTE: don't let the logo button inside the list. 
	public static Integer[] typeAry = new Integer[]{0,  11, 12, 13, 14, 21, 22, 31, 32, 33, 34, 41, 42, 43, 44, 61,  62, 63,  64, 81, 82, 83, 84 };

	public static int[] widthAry    = new int[] {   69, 42, 69, 42, 69, 96, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 138, 96, 96, 168,168,96};
	public static int[] heightAry   = new int[] {   69, 69, 42, 69, 42, 42, 96, 69, 96, 69, 96, 84, 96, 96, 96, 124, 96, 96, 138, 168,96, 96, 168};


	private EditText tfdName, tfdX, tfdY, tfdWidth, tfdHeight;
	private Spinner cmbCategory;
	private Button btnOk, btnCancel;

	//if called by cmd_AddTable, the settingTabbleDlg will be null!
    public TableDlg(TabbleSettingDlg pFrame, TableButton button) {
    	settingTableDlg = pFrame;
    	if(button == null) {
    		button =  new TableButton();
    		button.setTypeStatusAndName("", "", 0, 0, "");
    		button.setId(-1);
    		btnTables.add(button);
    	}else {
    		for (TableButton tableButton : TabbleSettingDlg.btnTables) {
				btnTables.add(tableButton);
			}
    	}
    }

	@Nullable
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_table, null);
		builder.setView(view);
		builder.setTitle(BarFrame.consts.TABLE());

		tfdName = view.findViewById(R.id.tfdName);
		tfdX = view.findViewById(R.id.tfdX);
		tfdY = view.findViewById(R.id.tfdY);
		tfdWidth = view.findViewById(R.id.tfdWidth);
		tfdHeight = view.findViewById(R.id.tfdHeight);
		cmbCategory = view.findViewById(R.id.cmbCategory);
		btnOk = view.findViewById(R.id.btnOk);
		btnCancel = view.findViewById(R.id.btnCancel);

		tfdName.setSingleLine(true);
		tfdName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		tfdName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					// Hide the keyboard when the done button is pressed
					v.clearFocus();
					return true;
				}
				return false;
			}
		});

		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeAry);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbCategory.setAdapter(adapter);
		cmbCategory.setEnabled(false);

		if (btnTables.size() == 1 && !StringUtils.isBlank(btnTables.get(0).getTableName())) {
			TableButton button = btnTables.get(0);
			tfdName.setText(button.getTableName());
			tfdX.setText(String.valueOf(button.getX()));
			tfdY.setText(String.valueOf(button.getY()));
			tfdWidth.setText(String.valueOf(button.getWidth()));
			tfdHeight.setText(String.valueOf(button.getHeight()));
			cmbCategory.setSelection(adapter.getPosition(button.getType()));
		}

		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btnTables.size() == 1) {
					String name = tfdName.getText().toString();
					if (name.isEmpty()) {
						tfdName.setError("Name cannot be empty");
						return;
					}
					updateTable(name, btnTables.get(0).getId(), settingTableDlg == null);
				} else {
					for (TableButton tableButton : btnTables) {
						updateTable(tableButton.getTableName(), tableButton.getId(), settingTableDlg == null);
					}
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Dialog dialog = builder.create();

		// 设置对话框高度
		dialog.setOnShowListener(dialogInterface -> {
			if(LoginDlg.getUSERTYPE() >= LoginDlg.ADMIN_STATUS){
				tfdX.setEnabled(true);
				tfdY.setEnabled(true);
				tfdWidth.setEnabled(true);
				tfdHeight.setEnabled(true);
			}
//			int height =  ? 420 : 360;
//			dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);
		});

		return dialog;
	}

	private void updateTable(String name, int id, boolean isTemporallyCreatedTbl) {
		try {
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("posX", Integer.parseInt(tfdX.getText().toString()));
			values.put("posY", Integer.parseInt(tfdY.getText().toString()));
			values.put("type", (Integer)cmbCategory.getSelectedItem());

			if (id == -1) {
				values.put("displayIdx", isTemporallyCreatedTbl ? -1 : 0);
				long newRowId = db.insert("DINING_TABLE", null, values);
				if (newRowId == -1) {
					Toast.makeText(getContext(), "Error inserting new table", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getContext(), "Table inserted with ID: " + newRowId, Toast.LENGTH_SHORT).show();
				}
			} else {
				int rowsAffected = db.update("DINING_TABLE", values, "_id = ?", new String[]{String.valueOf(id)});
				if (rowsAffected == 0) {
					Toast.makeText(getContext(), "Error updating table", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getContext(), "Table updated successfully", Toast.LENGTH_SHORT).show();
				}
			}
			if(settingTableDlg != null)
				settingTableDlg.initContent();
			else
				((TablesPanel)BarFrame.instance.panels[0]).initContent();
		} catch (Exception e) {
			e.printStackTrace();

			Toast.makeText(getContext(), "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		} finally {
			dismiss();
		}
	}

//    private void initDialog(TableButton btnTable) {
//    	setModal(true);
//        setTitle(btnTable.getTableName().length() > 0 ? btnTable.getTableName() : BarFrame.consts.TABLE());
//        setResizable(false);
//        //
//        sptName = new PIMSeparator(BarFrame.consts.Name());
//        lblName = new JLabel(BarFrame.consts.TableName());
//        tfdName = new JTextField(btnTables.size() == 1 ? btnTable.getTableName() : "");//----------
//
//        cmbCategory.setModel(new DefaultComboBoxModel<Integer>(typeAry));
//        cmbCategory.setSelectedItem(btnTable.getType());
//
//        if(btnTables.size() > 1) {
//        	tfdX.setText("");
//	        tfdY.setText("");
//	        tfdWidth.setText("");
//	        tfdHeight.setText("");
//        }else if(btnTable.getTableName().length() > 0) {
//	        tfdX.setText(String.valueOf(btnTable.getX()));
//	        tfdY.setText(String.valueOf(btnTable.getY()));
//	        tfdWidth.setText(String.valueOf(btnTable.getWidth()));
//	        tfdHeight.setText(String.valueOf(btnTable.getHeight()));
//        }else {
//	        tfdX.setText(String.valueOf(getX() + getWidth() / 2 - 21));
//	        tfdY.setText(String.valueOf(getY() - 60));
//	        tfdWidth.setText("42");
//	        tfdHeight.setText("42");
//        }
//        SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				tfdName.requestFocus();
//			}
//		});
//    }



}
