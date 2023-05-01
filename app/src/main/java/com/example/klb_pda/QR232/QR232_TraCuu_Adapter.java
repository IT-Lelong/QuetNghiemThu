package com.example.klb_pda.QR232;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.klb_pda.Constant_Class;
import com.example.klb_pda.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QR232_TraCuu_Adapter extends RecyclerView.Adapter<QR232_TraCuu_Adapter.DataViewHolder> {
    private final Context context;
    private final int layout_resource;
    private final List<QR232_TraCuu_Model> objects;
    private Activity qr232_traCuu;
    String g_server = "";

    DecimalFormat decimalFormat;

    public QR232_TraCuu_Adapter(Context context, int layout_resource, List<QR232_TraCuu_Model> objects, Activity qr232_traCuu) {
        this.context = context;
        this.layout_resource = layout_resource;
        this.objects = objects;
        this.qr232_traCuu = qr232_traCuu;

        String pattern = "#,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);

        g_server = Constant_Class.server;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout_resource, parent, false);

        // khởi tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
        builder.setTitle("Thông tin chi tiết");
        builder.setView(R.layout.activity_qr232_tracuu_item_row_dialog);
        builder.setNegativeButton("Đóng", null);
        AlertDialog dialog = builder.create();

        return new QR232_TraCuu_Adapter.DataViewHolder(itemView, dialog);
    }

    @Override
    public void onBindViewHolder(@NonNull QR232_TraCuu_Adapter.DataViewHolder holder, int position) {
        holder.tv_stt.setText(String.valueOf(position + 1));
        holder.tv_MaDon.setText(objects.get(position).getG_qr_imo002());
        holder.tv_xuong.setText(objects.get(position).getG_qr_imo001());
        holder.tv_ngay.setText(objects.get(position).getG_qr_imo008());
        holder.tv_MVL.setText(objects.get(position).getG_qr_imo003());
        holder.tv_tenVatLieu.setText(objects.get(position).getG_ta_ima02_1());
        holder.tv_QuyCach.setText(objects.get(position).getG_ta_ima021_1());
        holder.tv_soluong.setText(String.valueOf(decimalFormat.format(Double.parseDouble(objects.get(position).getG_qr_imo005()))));
        holder.tv_soluongdaxuly.setText(String.valueOf(decimalFormat.format(Double.parseDouble(objects.get(position).getG_qr_imo006()))));
        holder.tv_MaNV.setText(objects.get(position).getG_qr_imo009());
        holder.tv_TenNV.setText(objects.get(position).getG_ta_cpf001());

        if (Double.parseDouble(objects.get(position).getG_qr_imo005()) != Double.parseDouble(objects.get(position).getG_qr_imo006())) {
            holder.tv_status.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.tv_status.setText("N/G");
        } else {
            holder.tv_status.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.tv_status.setText("OK");
        }

        // hiển thị dữ liệu lên dialog
        TextView dialog_tv_stt = holder.mDialog.findViewById(R.id.tv_stt);
        TextView dialog_tv_ngay = holder.mDialog.findViewById(R.id.tv_ngay);
        TextView dialog_tv_soluong = holder.mDialog.findViewById(R.id.tv_soluong);
        TextView dialog_tv_MaNV = holder.mDialog.findViewById(R.id.tv_MaNV);


        holder.itemView.setOnClickListener(v -> {
            Log.d("RecyclerViewClick", "Item clicked: " + position);
            ListView lv_hisData;
            List<QR232_TraCuu_Dialog_Model> list = new ArrayList<QR232_TraCuu_Dialog_Model>();

            Dialog dialog = new Dialog(v.getContext(), android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
            dialog.setContentView(R.layout.activity_qr232_tracuu_item_row_dialog);
            String g_madon = objects.get(position).getG_qr_imo002();
            lv_hisData = dialog.findViewById(R.id.lv_hisData);

            // Sử dụng runOnUiThread để đưa dữ liệu vào ListView
            QR232_TraCuu_Dialog_Model_Adapter dialogAdapter = new QR232_TraCuu_Dialog_Model_Adapter(v.getContext(),
                    R.layout.activity_qr232_tracuu_item_row_dialog_row,
                    (ArrayList<QR232_TraCuu_Dialog_Model>) list);

            lv_hisData.setAdapter(dialogAdapter);

            new get_HisItemData(list, dialogAdapter).execute("http://172.16.40.20/" + g_server + "/PDA_QR232/query_History_Data.php?donKoDat=" + g_madon);
            /*Thread hisqry = new Thread(new Runnable() {
                @Override
                public void run() {
                    list = new ArrayList<QR232_TraCuu_Dialog_Model>();
                    String result = get_HisItemData("http://172.16.40.20/" + g_server + "/PDA_QR232/query_History_Data.php?donKoDat=" + g_madon);
                    if (!result.equals("FALSE")) {
                        try {
                            JSONArray jsonarray = new JSONArray(result);
                            if (jsonarray.length() > 0) {
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                                    String g_qr_imp005 = jsonObject.getString("QR_IMP005");
                                    String g_qr_imp007 = jsonObject.getString("QR_IMP007");
                                    String g_qr_imp004 = jsonObject.getString("QR_IMP004");
                                    String g_qr_imp006 = jsonObject.getString("QR_IMP006");
                                    String g_ta_cpf001 = jsonObject.getString("TA_CPF001");

                                    list.add(new QR232_TraCuu_Dialog_Model(g_qr_imp005, g_qr_imp007, g_qr_imp004, g_qr_imp006, g_ta_cpf001));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }
            });
            hisqry.start();*/

            dialog.show();
        });
    }

    private String get_HisItemData(String s) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(s);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            return "FALSE";
        }
    }


    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView tv_stt, tv_xuong, tv_ngay, tv_MaNV, tv_TenNV, tv_MaDon, tv_MVL, tv_tenVatLieu, tv_soluong, tv_soluongdaxuly, tv_QuyCach, tv_GhiChu, tv_status;

        private AlertDialog mDialog;

        public DataViewHolder(View itemView, AlertDialog dialog) {
            super(itemView);
            mDialog = dialog;

            tv_MaDon = (TextView) itemView.findViewById(R.id.tv_MaDon);
            tv_MaNV = (TextView) itemView.findViewById(R.id.tv_MaNV);
            tv_TenNV = (TextView) itemView.findViewById(R.id.tv_TenNV);
            tv_stt = (TextView) itemView.findViewById(R.id.tv_stt);
            tv_xuong = (TextView) itemView.findViewById(R.id.tv_xuong);
            tv_ngay = (TextView) itemView.findViewById(R.id.tv_ngay);
            tv_MVL = (TextView) itemView.findViewById(R.id.tv_MVL);
            tv_tenVatLieu = (TextView) itemView.findViewById(R.id.tv_tenVatLieu);
            tv_soluong = (TextView) itemView.findViewById(R.id.tv_soluong);
            tv_soluongdaxuly = (TextView) itemView.findViewById(R.id.tv_soluongdaxuly);
            tv_QuyCach = (TextView) itemView.findViewById(R.id.tv_QuyCach);
            tv_GhiChu = (TextView) itemView.findViewById(R.id.tv_GhiChu);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);

            // set sự kiện onClick cho item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.show();
                }
            });
        }
    }

    private class get_HisItemData extends AsyncTask<String, Integer, String> {
        List<QR232_TraCuu_Dialog_Model> mList;
        QR232_TraCuu_Dialog_Model_Adapter madapter;

        public get_HisItemData(List<QR232_TraCuu_Dialog_Model> list, QR232_TraCuu_Dialog_Model_Adapter dialogAdapter) {
            mList = list;
            madapter = dialogAdapter;
        }

        @Override
        protected String doInBackground(String... strings) {
            return docNoiDung_Tu_URL(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONArray jsonarray = new JSONArray(s);
                if (jsonarray.length() > 0) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonObject = jsonarray.getJSONObject(i);
                        String g_qr_imp005 = jsonObject.getString("QR_IMP005");
                        String g_qr_imp007 = jsonObject.getString("QR_IMP007");
                        String g_qr_imp004 = jsonObject.getString("QR_IMP004");
                        String g_qr_imp006 = jsonObject.getString("QR_IMP006");
                        String g_ta_cpf001 = jsonObject.getString("TA_CPF001");

                        mList.add(new QR232_TraCuu_Dialog_Model(g_qr_imp005, g_qr_imp007, g_qr_imp004, g_qr_imp006, g_ta_cpf001));
                    }
                    madapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String docNoiDung_Tu_URL(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                //content.append(line + "\n");
                content.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
