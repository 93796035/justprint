package com.just.print.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.just.print.R;

import org.cas.client.platform.bar.panel.BillPanel;

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
