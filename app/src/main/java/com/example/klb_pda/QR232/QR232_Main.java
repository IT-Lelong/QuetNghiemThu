package com.example.klb_pda.QR232;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.Constant_Class;
import com.example.klb_pda.QRprint.qrcode_print;
import com.example.klb_pda.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class QR232_Main extends AppCompatActivity {
    DecimalFormat decimalFormat;
    String g_server = "";
    String ID, g_Factory;
    Button btn_hangkodat, btn_doihang, btn_tracuu;
    ListView lv_tinhtrang;
    TextView tv_hmcht;
    private QR232DB qr232DB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr232_main);
        AddControls();
        AddEvents();
        getDonkhongdat_table();
        //loadListViewData();
    }

    private void getDonkhongdat_table() {
        Thread getDonkhongdat_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                String res = get_DataTable("http://172.16.40.20/" + g_server + "/PDA_QR232/getdata.php?xuong=" + g_Factory);
                if (!res.equals("FALSE")) {
                    try {
                        JSONArray jsonarray = new JSONArray(res);
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonObject = jsonarray.getJSONObject(i);
                            String g_qr_imo001 = jsonObject.getString("QR_IMO001");
                            String g_qr_imo002 = jsonObject.getString("QR_IMO002");
                            String g_qr_imo003 = jsonObject.getString("QR_IMO003");
                            String g_qr_imo004 = jsonObject.getString("QR_IMO004");
                            String g_qr_imo005 = jsonObject.getString("QR_IMO005");
                            String g_qr_imo006 = jsonObject.getString("QR_IMO006");
                            String g_qr_imo007 = jsonObject.getString("QR_IMO007");
                            String g_qr_imo008 = jsonObject.getString("QR_IMO008");
                            String g_qr_imo009 = jsonObject.getString("QR_IMO009");
                            String g_qr_imo010 = jsonObject.getString("QR_IMO010");
                            String g_ta_ima02_1 = jsonObject.getString("TA_IMA02_1");
                            String g_ta_ima021_1 = jsonObject.getString("TA_IMA021_1");

                            qr232DB.insertData(g_qr_imo001, g_qr_imo002, g_qr_imo003, g_qr_imo004,
                                    g_qr_imo005,g_qr_imo006,g_qr_imo007,g_qr_imo008,
                                    g_qr_imo009,g_qr_imo010,g_ta_ima02_1,g_ta_ima021_1);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadListViewData();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Thread.currentThread().interrupt();
                Looper.loop();
            }
        });
        getDonkhongdat_thread.start();
    }

    private String get_DataTable(String s) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(s);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                return jsonstring;
            } else {
                return "FALSE";
            }
        } catch (Exception e) {
            return "FALSE";
        }
    }

    private void AddEvents() {
        lv_tinhtrang.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_maVatLieu = view.findViewById(R.id.tv_vatlieu);
                TextView tv_tenVatLieu = view.findViewById(R.id.tv_tenVatLieu);
                TextView tv_quycachVatLieu = view.findViewById(R.id.tv_QuyCach);

                Intent cl_xuly = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("ID", ID);
                bundle.putString("Factory", g_Factory);
                bundle.putString("Item", tv_maVatLieu.getText().toString().trim());
                bundle.putString("ItemName", tv_tenVatLieu.getText().toString().trim());
                bundle.putString("ItemSpec", tv_quycachVatLieu.getText().toString().trim());
                cl_xuly.setClass(QR232_Main.this, QR232_xuLyDonKhongDat.class);
                cl_xuly.putExtras(bundle);
                startActivity(cl_xuly);
            }
        });
    }

    private void AddControls() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        qr232DB = new QR232DB(this);
        qr232DB.open();
        qr232DB.createTable();
        qr232DB.del_hangkhongdat_table();
        qr232DB.del_temp_table();
        qr232DB.del_history_table();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_Factory = getbundle.getString("Factory");
        g_server = Constant_Class.server;

        String pattern = "#,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);

        /*int color = ContextCompat.getColor(QR232_Main.this,R.color.MidnightGreen);
        tv_tinhtrang.setBackgroundTintList(ColorStateList.valueOf(color));*/

        btn_hangkodat = findViewById(R.id.btn_hangkodat);
        btn_doihang = findViewById(R.id.btn_doihang);
        btn_tracuu = findViewById(R.id.btn_tracuu);
        lv_tinhtrang = findViewById(R.id.lv_tinhtrang);
        tv_hmcht = findViewById(R.id.tv_hmcht);

        btn_hangkodat.setOnClickListener(btnlistener);
        btn_doihang.setOnClickListener(btnlistener);
        btn_tracuu.setOnClickListener(btnlistener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadListViewData();
    }

    private void loadListViewData() {
        //Hạng mục chưa hoàn thành
        tv_hmcht.setText(String.valueOf(qr232DB.getCountData(g_Factory)));
        //Danh sách top 10 chưa hoàn thành
        Cursor cursor = qr232DB.getTopData(g_Factory);
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_qr232_main_item_row, cursor,
                new String[]{"dkd01", "dkd08", "dkd03", "sl", "dkd11", "dkd12"},
                new int[]{R.id.tv_xuong, R.id.tv_ngay, R.id.tv_vatlieu, R.id.tv_soluong, R.id.tv_tenVatLieu, R.id.tv_QuyCach},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        simpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tv_soluong) {
                    ((TextView) view).setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                    return true;
                }

                return false;
            }
        });
        lv_tinhtrang.setAdapter(simpleCursorAdapter);
    }

    private View.OnClickListener btnlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_hangkodat: {
                    try {
                        Intent cl_hangkodat = new Intent();
                        cl_hangkodat.setClass(QR232_Main.this, QR232_hangKhongDat.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("Factory", g_Factory);
                        cl_hangkodat.putExtras(bundle);
                        startActivity(cl_hangkodat);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(QR232_Main.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }

                case R.id.btn_doihang: {
                    try {
                        Intent cl_xuly = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("Factory", g_Factory);
                        bundle.putString("Item", null);
                        cl_xuly.setClass(QR232_Main.this, QR232_xuLyDonKhongDat.class);
                        cl_xuly.putExtras(bundle);
                        startActivity(cl_xuly);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(QR232_Main.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }

                case R.id.btn_tracuu: {
                    try {
                        Intent cl_tracuu = new Intent();
                        cl_tracuu.setClass(QR232_Main.this, QR232_TraCuu.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("Factory", g_Factory);
                        cl_tracuu.putExtras(bundle);
                        startActivity(cl_tracuu);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(QR232_Main.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }
            }
        }
    };
}