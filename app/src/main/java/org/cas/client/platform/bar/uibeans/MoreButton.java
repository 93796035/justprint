package org.cas.client.platform.bar.uibeans;

import android.graphics.Color;
import android.view.View;

import com.just.print.app.BarFrame;
import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.dialog.MoreButtonsDlg;

import java.util.ArrayList;

public class MoreButton extends FunctionButton implements ISButton, View.OnClickListener {
	
	public MoreButton(String title) {
		super(title);
		setText(title);
		String bg = BarOption.getBK("Function");
    	if(bg == null) {
    		bg = "#666666";//"#9999ff";
    	}
		setBackgroundColor(Color.parseColor(bg));

//		setBorder(BorderFactory.createRaisedBevelBorder());
		addActionListener(this);
	}
	
	public void addButton(ISButton button) {
		buttons.add(button);
	}
	
	ArrayList<ISButton> buttons = new ArrayList<ISButton>();
	
	//implements the interface ISButton---------------------

	private String text;
	public MoreButton clone() {
		MoreButton btn = new MoreButton(BarFrame.consts.MORE());
		btn.addActionListener(actionListener);
		if(actionListener == null) {
			L.e("FunctionToggleButton", "actionListener is null!", null);
		}
//		actionListener.setSourceBtn(btn);
		return btn;
	}
	
	public void addActionListener( View.OnClickListener actionListener) {
		if(actionListener == null) {
			L.e("FunctionToggleButton", "receieved a null as actionListener", null);
		}
		this.setOnClickListener(actionListener);
		//this.actionListener.setSourceBtn(this);
	}

   //for satisfy JToogleButton interfaces
   public void setSelected(boolean b) {}
   public boolean isSelected() {return false;}
   
   //for satisfy JButton
   public String getText() {
	   return this.text;
   };
   public void setText(String text) {
	   this.text = text;
   };

	@Override
	public void onClick(View v) {
		new MoreButtonsDlg(buttons).show((MoreButton)v);
	}
}
