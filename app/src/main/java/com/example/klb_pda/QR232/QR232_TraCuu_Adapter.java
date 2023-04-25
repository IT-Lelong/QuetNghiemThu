package com.example.klb_pda.QR232;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.klb_pda.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class QR232_TraCuu_Adapter extends RecyclerView.Adapter<QR232_TraCuu_Adapter.DataViewHolder> {
    private final Context context;
    private final int layout_resource;
    private final List<QR232_TraCuu_Model> objects;

    DecimalFormat decimalFormat;

    public QR232_TraCuu_Adapter(Context context, int layout_resource, List<QR232_TraCuu_Model> objects) {
        this.context = context;
        this.layout_resource = layout_resource;
        this.objects = objects;

        String pattern = "#,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout_resource, parent, false);
        return new QR232_TraCuu_Adapter.DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QR232_TraCuu_Adapter.DataViewHolder holder, int position) {
        holder.tv_stt.setText(String.valueOf(position + 1));
        holder.tv_xuong.setText(objects.get(position).getG_qr_imo001());
        holder.tv_ngay.setText(objects.get(position).getG_qr_imo008());
        holder.tv_MVL.setText(objects.get(position).getG_qr_imo003());
        holder.tv_tenVatLieu.setText(objects.get(position).getG_ta_ima02_1());
        holder.tv_QuyCach.setText(objects.get(position).getG_ta_ima021_1());
        holder.tv_soluong.setText(String.valueOf(decimalFormat.format(Double.parseDouble(objects.get(position).getG_qr_imo005()))));
        holder.tv_soluongdaxuly.setText(objects.get(position).getG_qr_imo006());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.activity_qr232_tracuu_item_row_info);

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView tv_stt,tv_xuong, tv_ngay, tv_MaNV, tv_MaDon, tv_MVL, tv_tenVatLieu, tv_soluong, tv_soluongdaxuly, tv_QuyCach, tv_GhiChu;

        public DataViewHolder(View itemView) {
            super(itemView);

            tv_stt = (TextView) itemView.findViewById(R.id.tv_stt);
            tv_xuong = (TextView) itemView.findViewById(R.id.tv_xuong);
            tv_ngay = (TextView) itemView.findViewById(R.id.tv_ngay);
            tv_MVL = (TextView) itemView.findViewById(R.id.tv_MVL);
            tv_tenVatLieu = (TextView) itemView.findViewById(R.id.tv_tenVatLieu);
            tv_soluong = (TextView) itemView.findViewById(R.id.tv_soluong);
            tv_soluongdaxuly = (TextView) itemView.findViewById(R.id.tv_soluongdaxuly);
            tv_QuyCach = (TextView) itemView.findViewById(R.id.tv_QuyCach);
        }
    }
}
