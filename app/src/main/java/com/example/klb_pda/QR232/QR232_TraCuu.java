package com.example.klb_pda.QR232;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
    CheckBox cB_status01, cB_status02, cB_status03;
    String qry_xuong, qry_mvl, qry_bdate_TraHang, qry_edate_TraHang, qry_bdate_NhanHang, qry_edate_NhanHang, qry_status;
    EditText edt_mvl;
    TextView tv_bdate_TraHang, tv_edate_TraHang, tv_bdate_NhanHang, tv_edate_NhanHang, tv_QuyCach;
    Button btn_query;
    List dataList;
    QR232_TraCuu_Adapter qr232TraCuuAdapter;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr232_tracuu);

        AddControls();
        AddEvents();
        //loadQueryData(qry_xuong, qry_mvl, qry_bdate_TraHang, qry_edate_TraHang, qry_bdate_NhanHang, qry_edate_NhanHang);
    }

    private void loadQueryData(String qry_xuong, String qry_mvl, String qry_bdate_TraHang, String qry_edate_TraHang, String qry_bdate_NhanHang, String qry_edate_NhanHang) {
        Thread loadQueryData_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = load_QueryData("http://172.16.40.20/" + g_server + "/PDA_QR232/query_Data.php?xuong=" + qry_xuong + "&mvl=" + qry_mvl + "&bdateTraHang=" + qry_bdate_TraHang + "&edateTraHang=" + qry_edate_TraHang + "&bdateNhanHang=" + qry_bdate_NhanHang + "&edateNhanHang=" + qry_edate_NhanHang + "&trangthai=" + qry_status);
                if (!result.equals("FALSE")) {
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

                                dataList.add(new QR232_TraCuu_Model(g_qr_imo001, g_qr_imo008, g_qr_imo009, g_ta_cpf001, g_qr_imo002, g_qr_imo003, g_ta_ima02_1, g_ta_ima021_1, g_qr_imo005, g_qr_imo006, g_qr_imo007));
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
                                    dataList.clear();
                                    qr232TraCuuAdapter.notifyDataSetChanged();
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

        edt_mvl.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edt_mvl.getRight() - edt_mvl.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (edt_mvl.getText().toString().trim().length() > 0) {
                        chkItemInfo(edt_mvl.getText().toString().trim());
                        return true;
                    }
                }
            }
            return false;
        });

        CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.cB_status01:
                        qry_status = getSelectedCheckBoxes();
                        break;
                    case R.id.cB_status02:
                        qry_status = getSelectedCheckBoxes();
                        break;
                    /*case R.id.cB_status03:
                        qry_status = getSelectedCheckBoxes();
                        break;*/
                }
            }
        };

        cB_status01.setOnCheckedChangeListener(checkBoxListener);
        cB_status02.setOnCheckedChangeListener(checkBoxListener);
        //cB_status03.setOnCheckedChangeListener(checkBoxListener);

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

    private void chkItemInfo(String g_item) {
        new get_ItemData().execute("http://172.16.40.20/" + g_server + "/get_ima_file.php?IMA01=" + g_item);
    }

    private String getSelectedCheckBoxes() {
        String result = "";
        if (cB_status01.isChecked()) {
            result += "'0',";
        }
        if (cB_status02.isChecked()) {
            result += "'2',";
        }
        /*if (cB_status03.isChecked()) {
            result += "'3',";
        }*/
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1); // remove the last comma
        }
        return result;
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
        tv_QuyCach = findViewById(R.id.tv_QuyCach);
        tv_bdate_TraHang = findViewById(R.id.tv_bdate_TraHang);
        tv_edate_TraHang = findViewById(R.id.tv_edate_TraHang);
        tv_bdate_NhanHang = findViewById(R.id.tv_bdate_NhanHang);
        tv_edate_NhanHang = findViewById(R.id.tv_edate_NhanHang);
        cB_status01 = findViewById(R.id.cB_status01);
        cB_status02 = findViewById(R.id.cB_status02);
        //cB_status03 = findViewById(R.id.cB_status03);

        btn_query = findViewById(R.id.btn_query);

        lv_tracuu = findViewById(R.id.lv_tracuu);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(QR232_TraCuu.this);
        lv_tracuu.setLayoutManager(linearLayoutManager);

        String[] g_factory = {"A", "B", "C", "D", "I"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(QR232_TraCuu.this, android.R.layout.simple_spinner_dropdown_item, g_factory);
        factory_spinner.setAdapter(adapter);
        int index = Arrays.asList(g_factory).indexOf(g_Factory);
        factory_spinner.setSelection(index);

        dataList = new ArrayList<QR232_TraCuu_Model>();
        qr232TraCuuAdapter = new QR232_TraCuu_Adapter(getApplicationContext(), R.layout.activity_qr232_tracuu_item_row, dataList, QR232_TraCuu.this);
        lv_tracuu.setAdapter(qr232TraCuuAdapter);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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

    //取得料號資訊
    private class get_ItemData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            return docNoiDung_Tu_URL(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            String g_quyCach = "";
            try {
                JSONArray jsonarray = new JSONArray(s);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                    g_quyCach = jsonObject.getString("TA_IMA021_1");  //Quy cách tiếng việt
                }

                String finalG_quyCach = g_quyCach;
                runOnUiThread(() -> tv_QuyCach.setText(finalG_quyCach));
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
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}