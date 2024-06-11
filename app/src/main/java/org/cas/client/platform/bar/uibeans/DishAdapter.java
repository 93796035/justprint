package org.cas.client.platform.bar.uibeans;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.just.print.R;
import com.just.print.db.bean.Dish;

import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.cascustomize.CustOpts;

import java.util.List;

public class DishAdapter extends ArrayAdapter<Dish> {
    private Context context;
    private List<Dish> dishes;

    public DishAdapter(Context context, List<Dish> dishes) {
        super(context, 0, dishes);
        this.context = context;
        this.dishes = dishes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Dish dish = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dish_item, parent, false);
        }

        TextView dishCount = convertView.findViewById(R.id.dish_count);
        TextView dishName = convertView.findViewById(R.id.dish_name);
        TextView dishSize = convertView.findViewById(R.id.dish_size);
        TextView dishPrice = convertView.findViewById(R.id.dish_price);

        dishCount.setText("1x");
        dishName.setText(dish.getLanguage(CustOpts.custOps.getUserLang()));
        dishSize.setText(dish.getSize() > 1 ? String.valueOf(dish.getSize()) : "");
        dishPrice.setText(BarOption.getMoneySign() + BarUtil.formatMoney(dish.getPrice() / 100f));

        return convertView;
    }
}
