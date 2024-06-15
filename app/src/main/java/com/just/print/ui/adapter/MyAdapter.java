package com.just.print.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.just.print.R;
import com.just.print.db.bean.Dish;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.net.NetUtil;
import org.cas.client.platform.bar.panel.BillPanel;
import org.cas.client.platform.cascustomize.CustOpts;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    public String[][] data;
    private int selectedPosition = -1;

    public MyAdapter(String[][] data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_row layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Bind data to the item view
        String[] row = data[position];
        holder.column1.setText(row[0]);
        holder.column2.setText(row[1]);
        holder.column3.setText(row[2]);
        holder.column4.setText(row[3]);

        // Set the background color based on the selection state
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(android.R.color.transparent));
        }

        // Set the click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);

                // Trigger the external callback if set
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(selectedPosition);
                }
            }
        });
    }

    public void addDish(Dish newDish) {
        // Add the new dish to the data array
        String[][] newData = new String[data.length + 1][4];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length][0] = "1x";
        newData[data.length][1] = NetUtil.trimContentFromHTML(newDish.getLanguage(CustOpts.custOps.getUserLang()));
        newData[data.length][2] = newDish.getSize() > 1 ? newDish.getSize().toString() : "";
        newData[data.length][3] = BarOption.getMoneySign() + BarUtil.formatMoney(newDish.getPrice() / 100f);
        data = newData;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public String getValueAt(int r, int c) {
        return data[r][c];
    }

    public void setValueAt(int row, int column, String value) {
        data[row][column] = value;
        notifyItemChanged(row);
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(selectedPosition);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView column1, column2, column3, column4;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID
            column1 = itemView.findViewById(R.id.column1);
            column2 = itemView.findViewById(R.id.column2);
            column3 = itemView.findViewById(R.id.column3);
            column4 = itemView.findViewById(R.id.column4);
        }
    }

    // Listener interface for item click events
    private BillPanel.OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(BillPanel.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
