package com.example.klb_pda.QR232;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.klb_pda.R;

import java.util.ArrayList;
import java.util.List;

public class QR232_TraCuu_Dialog_Model_Adapter extends ArrayAdapter<QR232_TraCuu_Dialog_Model> {
    private final Context context;
    private final int layoutResourceId;
    private final List<QR232_TraCuu_Dialog_Model> datalist;

    public QR232_TraCuu_Dialog_Model_Adapter(Context context, int layoutResourceId, List<QR232_TraCuu_Dialog_Model> datalist) {
        super(context, layoutResourceId, datalist);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.datalist = datalist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ProductHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ProductHolder();
            holder.tv_stt = (TextView) row.findViewById(R.id.tv_stt);
            holder.tv_ngay = (TextView) row.findViewById(R.id.tv_ngay);
            holder.tv_soluong = (TextView) row.findViewById(R.id.tv_soluong);
            holder.tv_MaNV = (TextView) row.findViewById(R.id.tv_MaNV);
            row.setTag(holder);
        } else {
            holder = (ProductHolder) row.getTag();
        }

        QR232_TraCuu_Dialog_Model product = datalist.get(position);
        holder.tv_stt.setText(String.valueOf(position + 1));
        String g_times = product.getG_qr_imp005() + " " + product.getG_qr_imp007();
        holder.tv_ngay.setText(g_times);
        holder.tv_soluong.setText(product.getG_qr_imp004());
        String g_nv = product.getG_qr_imp006() + " " + product.getG_ta_cpf001();
        holder.tv_MaNV.setText(g_nv);

        return row;
    }

    static class ProductHolder {
        TextView tv_stt, tv_ngay, tv_soluong, tv_MaNV;
    }
}
