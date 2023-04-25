package com.example.klb_pda.QR232;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.Constant_Class;
import com.example.klb_pda.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QR232_TraCuu extends AppCompatActivity {
    private QR232DB qr232DB = null;
    DecimalFormat decimalFormat;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    String g_server = "";
    String ID, g_Factory;
    NavigationView navigationView;
    RecyclerView lv_tracuu;
    Spinner factory_spinner;
    String qry_xuong, qry_mvl, qry_bdate_TraHang, qry_edate_TraHang, qry_bdate_NhanHang, qry_edate_NhanHang;
    EditText edt_mvl;
    TextView tv_bdate_TraHang, tv_edate_TraHang, tv_bdate_NhanHang, tv_edate_NhanHang;
    Button btn_query;
    List dataList;
    QR232_TraCuu_Adapter qr232TraCuuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr232_nav);

        AddControls();
        AddEvents();
        //loadQueryData(qry_xuong, qry_mvl, qry_bdate_TraHang, qry_edate_TraHang, qry_bdate_NhanHang, qry_edate_NhanHang);
    }

    private void loadQueryData(String qry_xuong, String qry_mvl, String qry_bdate_TraHang, String qry_edate_TraHang, String qry_bdate_NhanHang, String qry_edate_NhanHang) {
        Thread loadQueryData_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = load_QueryData("http://172.16.40.20/" + g_server + "/PDA_QR232/query_Data.php?xuong=" + qry_xuong + "&mvl=" + qry_mvl + "&bdateTraHang=" + qry_bdate_TraHang + "&edateTraHang=" + qry_edate_TraHang + "&bdateNhanHang=" + qry_bdate_NhanHang + "&edateNhanHang=" + qry_edate_NhanHang);
                if(!result.equals("FALSE")){
                    try {
                        dataList.clear();
                        JSONArray jsonarray = new JSONArray(result);
                        if (jsonarray.length() > 0) {
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonObject = jsonarray.getJSONObject(i);
                                String g_qr_imo001 = jsonObject.getString("QR_IMO001");
                                String g_qr_imo008 = jsonObject.getString("QR_IMO008");
                                String g_qr_imo009 = jsonObject.getString("QR_IMO009");
                                String g_ta_cpf001 = jsonObject.getString("TA_CPF001");
                                String g_qr_imo002 = jsonObject.getString("QR_IMO002");
                                String g_qr_imo003 = jsonObject.getString("QR_IMO003");
                                String g_ta_ima02_1 = jsonObject.getString("TA_IMA02_1");
                                String g_ta_ima021_1 = jsonObject.getString("TA_IMA021_1");
                                String g_qr_imo005 = jsonObject.getString("QR_IMO005");
                                String g_qr_imo006 = jsonObject.getString("QR_IMO006");
                                String g_qr_imo007 = jsonObject.getString("QR_IMO007");

                                dataList.add(new QR232_TraCuu_Model(g_qr_imo001,g_qr_imo008,g_qr_imo009,g_ta_cpf001,g_qr_imo002,g_qr_imo003,g_ta_ima02_1,g_ta_ima021_1,g_qr_imo005,g_qr_imo006,g_qr_imo007));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qr232TraCuuAdapter.notifyDataSetChanged();
                                }
                            });
                            Thread.currentThread().interrupt();
                        } else {
                            // Tạo một luồng con và sử dụng Handler để hiển thị Toast thông qua luồng con đó
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Không có thông tin tra cứu", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Thread.currentThread().interrupt();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        loadQueryData_Thread.start();// Khởi động luồng để thực hiện
    }

    private void AddEvents() {



        btn_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qry_xuong = factory_spinner.getSelectedItem().toString();
                qry_mvl = edt_mvl.getText().toString().trim();
                qry_bdate_TraHang = tv_bdate_TraHang.getText().toString().trim();
                qry_edate_TraHang = tv_edate_TraHang.getText().toString().trim();
                qry_bdate_NhanHang = tv_bdate_NhanHang.getText().toString().trim();
                qry_edate_NhanHang = tv_edate_NhanHang.getText().toString().trim();

                loadQueryData(qry_xuong, qry_mvl, qry_bdate_TraHang, qry_edate_TraHang, qry_bdate_NhanHang, qry_edate_NhanHang);
            }
        });

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int id = v.getId();
                final int DRAWABLE_RIGHT = 2;
                switch (id) {
                    case R.id.tv_bdate_TraHang:
                        // xử lý khi nhấn vào TextView tv_bdate_TraHang
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (event.getRawX() >= (tv_bdate_TraHang.getRight() - tv_bdate_TraHang.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                showDatePickerDialog(tv_bdate_TraHang, v);
                                return true;
                            }
                        }
                        break;
                    case R.id.tv_edate_TraHang:
                        // xử lý khi nhấn vào TextView tv_edate_TraHang
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (event.getRawX() >= (tv_edate_TraHang.getRight() - tv_edate_TraHang.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                showDatePickerDialog(tv_edate_TraHang, v);
                                return true;
                            }
                        }
                        break;
                    case R.id.tv_bdate_NhanHang:
                        // xử lý khi nhấn vào TextView tv_bdate_NhanHang
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (event.getRawX() >= (tv_bdate_NhanHang.getRight() - tv_bdate_NhanHang.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                showDatePickerDialog(tv_bdate_NhanHang, v);
                                return true;
                            }
                        }
                        break;
                    case R.id.tv_edate_NhanHang:
                        // xử lý khi nhấn vào TextView tv_edate_NhanHang
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (event.getRawX() >= (tv_edate_NhanHang.getRight() - tv_edate_NhanHang.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                showDatePickerDialog(tv_edate_NhanHang, v);
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        };

        // set sự kiện onTouchListener cho 4 TextView
        tv_bdate_TraHang.setOnTouchListener(touchListener);
        tv_edate_TraHang.setOnTouchListener(touchListener);
        tv_bdate_NhanHang.setOnTouchListener(touchListener);
        tv_edate_NhanHang.setOnTouchListener(touchListener);
    }

    private void showDatePickerDialog(TextView g_textView, View v) {
        DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear,
                                  int dayOfMonth) {
                //Mỗi lần thay đổi ngày tháng năm thì cập nhật lại TextView Date
                g_textView.setText(year + "/" + (monthOfYear) + "/" + (dayOfMonth));

                if (g_textView.getId() == R.id.tv_bdate_TraHang && tv_edate_TraHang.getText().toString().length() == 0) {
                    tv_edate_TraHang.setText(tv_bdate_TraHang.getText().toString().trim());
                }

                if (g_textView.getId() == R.id.tv_edate_TraHang) {
                    // Chuyển đổi chuỗi thành LocalDate
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                    LocalDate bdate = LocalDate.parse(tv_bdate_TraHang.getText().toString().trim(), formatter);
                    LocalDate edate = LocalDate.parse(tv_edate_TraHang.getText().toString().trim(), formatter);

                    // So sánh hai ngày
                    if (bdate.compareTo(edate) > 0) {
                        // bdate lớn hơn edate
                        Toast.makeText(QR232_TraCuu.this, "Ngày bắt đẩu không thể lớn hơn ngày kết thúc", Toast.LENGTH_SHORT).show();
                        tv_edate_TraHang.setText(tv_bdate_TraHang.getText().toString().trim());
                    } else if (bdate.compareTo(edate) < 0) {
                        // bdate nhỏ hơn edate
                    } else {
                        // bdate bằng edate
                    }
                }

                if (g_textView.getId() == R.id.tv_bdate_NhanHang && tv_edate_NhanHang.getText().toString().length() == 0) {
                    tv_edate_NhanHang.setText(tv_bdate_NhanHang.getText().toString().trim());
                }

                if (g_textView.getId() == R.id.tv_edate_NhanHang) {
                    // Chuyển đổi chuỗi thành LocalDate
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                    LocalDate bdate = LocalDate.parse(tv_bdate_NhanHang.getText().toString().trim(), formatter);
                    LocalDate edate = LocalDate.parse(tv_edate_NhanHang.getText().toString().trim(), formatter);

                    // So sánh hai ngày
                    if (bdate.compareTo(edate) > 0) {
                        // bdate lớn hơn edate
                        Toast.makeText(QR232_TraCuu.this, "Ngày bắt đẩu không thể lớn hơn ngày kết thúc", Toast.LENGTH_SHORT).show();
                        tv_edate_NhanHang.setText(tv_bdate_NhanHang.getText().toString().trim());
                    } else if (bdate.compareTo(edate) < 0) {
                        // bdate nhỏ hơn edate
                    } else {
                        // bdate bằng edate
                    }
                }


            }
        };
        //các lệnh dưới này xử lý ngày giờ trong DatePickerDialog
        //sẽ giống với trên TextView khi mở nó lên
        /*String s = g_textView.getText() + "";
        String strArrtmp[] = s.split("/");
        int ngay = Integer.parseInt(strArrtmp[2]);
        int thang = Integer.parseInt(strArrtmp[1]);
        int nam = Integer.parseInt(strArrtmp[0]);*/
        // Lấy ngày hiện tại
        LocalDate currentDate = LocalDate.now();

        int ngay = currentDate.getDayOfMonth();
        int thang = currentDate.getMonthValue();
        int nam = currentDate.getYear();
        ;
        DatePickerDialog pic = new DatePickerDialog(
                v.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK,
                callback, nam, thang, ngay);
        pic.show();
    }

    private void AddControls() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        qr232DB = new QR232DB(this);
        qr232DB.open();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_Factory = getbundle.getString("Factory");
        g_server = Constant_Class.server;

        String pattern = "#,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);

        navigationView = findViewById(R.id.navigation_view);
        factory_spinner = findViewById(R.id.factory_spinner);
        edt_mvl = findViewById(R.id.edt_mvl);
        tv_bdate_TraHang = findViewById(R.id.tv_bdate_TraHang);
        tv_edate_TraHang = findViewById(R.id.tv_edate_TraHang);
        tv_bdate_NhanHang = findViewById(R.id.tv_bdate_NhanHang);
        tv_edate_NhanHang = findViewById(R.id.tv_edate_NhanHang);
        btn_query = findViewById(R.id.btn_query);

        lv_tracuu = findViewById(R.id.lv_tracuu);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(QR232_TraCuu.this);
        lv_tracuu.setLayoutManager(linearLayoutManager);

        String[] g_factory = {"A", "B", "C", "D", "I"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(QR232_TraCuu.this, android.R.layout.simple_spinner_dropdown_item, g_factory);
        factory_spinner.setAdapter(adapter);
        factory_spinner.setSelection(0);

        dataList = new ArrayList<QR232_TraCuu_Model>();
        qr232TraCuuAdapter = new QR232_TraCuu_Adapter(getApplicationContext(),R.layout.activity_qr232_tracuu_item_row,dataList);
        lv_tracuu.setAdapter(qr232TraCuuAdapter);
    }

    private String load_QueryData(String s) {
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
}