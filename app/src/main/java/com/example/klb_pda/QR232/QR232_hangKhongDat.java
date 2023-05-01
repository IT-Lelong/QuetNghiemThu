package com.example.klb_pda.QR232;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.Constant_Class;
import com.example.klb_pda.R;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QR232_hangKhongDat extends AppCompatActivity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    DecimalFormat decimalFormat;
    String ID, g_server, g_Factory, g_madon;
    TextView tv_MaDon, tv_MaXuong, tv_MaNV, tv_TenNV, tv_tenVatLieu, tv_QuyCach, tv_SoLo, tv_scannerSTT;
    EditText edt_soLuong, edt_ghichu, edt_MVL;
    Button btncancel, btnok;

    private QR232DB qr232DB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr232_hang_khong_dat);
        AddControls();
        AddEvents();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void AddEvents() {

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_MVL.getText().toString().length() > 0 && edt_soLuong.getText().toString().length() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Xác nhận");
                    builder.setMessage("Bạn có chắc chắn muốn phát sinh đơn nguyên liệu không đạt?");
                    builder.setPositiveButton(getApplicationContext().getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread insData_Thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    // Xử lý khi người dùng chọn "Có"
                                    // Lấy ngày hiện tại
                                    LocalDate currentDate = LocalDate.now();
                                    // Định dạng ngày thành chuỗi với định dạng yyyy/MM/dd
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                                    String formattedDate = currentDate.format(formatter);

                                    String res = null;
                                    res = qr232DB.insertData(tv_MaXuong.getText().toString(),
                                            g_madon,
                                            edt_MVL.getText().toString(),
                                            tv_SoLo.getText().toString(),
                                            edt_soLuong.getText().toString().replace(",", ""),
                                            String.valueOf(0),
                                            edt_ghichu.getText().toString(),
                                            formattedDate,
                                            tv_MaNV.getText().toString(),
                                            "0",
                                            tv_tenVatLieu.getText().toString(),
                                            tv_QuyCach.getText().toString());


                                    if (res.equals("TRUE")) {
                                        JSONObject jObeject = new JSONObject();
                                        try {
                                            jObeject.put("QR_IMO001", tv_MaXuong.getText().toString());
                                            jObeject.put("QR_IMO002", g_madon);
                                            jObeject.put("QR_IMO003", edt_MVL.getText().toString());
                                            jObeject.put("QR_IMO004", tv_SoLo.getText().toString());
                                            jObeject.put("QR_IMO005", edt_soLuong.getText().toString().replace(",", ""));
                                            jObeject.put("QR_IMO006", 0);
                                            jObeject.put("QR_IMO007", edt_ghichu.getText().toString());
                                            jObeject.put("QR_IMO008", formattedDate);
                                            jObeject.put("QR_IMO009", tv_MaNV.getText().toString());
                                            jObeject.put("QR_IMO010", 0);
                                            res = upDataToServer(g_madon, jObeject);

                                            if (res.equals("TRUE")) {
                                                Thread.currentThread().interrupt();
                                                finish();
                                                Toast.makeText(QR232_hangKhongDat.this, "cập nhật Thành công", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Thread.currentThread().interrupt();
                                                Toast.makeText(QR232_hangKhongDat.this, "cập nhật THẤT BẠI", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        Thread.currentThread().interrupt();
                                        Toast.makeText(QR232_hangKhongDat.this, "Đã phát sinh THẤT BẠI", Toast.LENGTH_SHORT).show();
                                    }
                                    Looper.loop();
                                }
                            });
                            insData_Thread.start();

                        }
                    }).setNegativeButton(getApplicationContext().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xử lý khi người dùng chọn "Không"
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edt_MVL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt_MVL.getRight() - edt_MVL.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        edt_MVL.setText("");
                        tv_tenVatLieu.setText("");
                        tv_QuyCach.setText("");
                        tv_SoLo.setText("");
                        edt_soLuong.setText("");
                        edt_ghichu.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private String upDataToServer(String g_madon, JSONObject jObeject) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR232/insert_QR_IMO_FILE.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);

            writer.write(jObeject.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            return "FALSE";
        }
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

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }

        tv_MaDon = findViewById(R.id.tv_MaDon);
        tv_MaXuong = findViewById(R.id.tv_MaXuong);
        tv_MaNV = findViewById(R.id.tv_MaNV);
        tv_TenNV = findViewById(R.id.tv_TenNV);
        tv_tenVatLieu = findViewById(R.id.tv_tenVatLieu);
        tv_QuyCach = findViewById(R.id.tv_QuyCach);
        tv_SoLo = findViewById(R.id.tv_SoLo);
        tv_scannerSTT = findViewById(R.id.tv_scannerSTT);

        edt_MVL = findViewById(R.id.edt_MVL);
        edt_soLuong = findViewById(R.id.edt_soLuong);
        edt_ghichu = findViewById(R.id.edt_ghichu);


        btncancel = findViewById(R.id.btncancel);
        btnok = findViewById(R.id.btnok);

        g_madon = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        g_madon = sdf.format(new Date());
        g_madon = "HKD" + g_Factory + g_madon;
        tv_MaDon.setText("Mã đơn : " + g_madon);
        tv_MaXuong.setText(g_Factory);
        tv_MaNV.setText(ID);
        tv_TenNV.setText(Constant_Class.UserName_vn);

    }

    private void getMVL(String datastr) {
        try {
            if (datastr.startsWith("new")) {
                //重印、拆單標籤格式 new_料號_批號_數量
                String qr01 = "", qr02 = "";
                Double qr03 = 0.0;

                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_

                //料號
                qr01 = datastr.substring(4, index2);
                if (qr01.equals("T")) {
                    qr01 = datastr.substring(4, index3);
                    qr02 = datastr.substring(index3 + 1, index4);
                    qr03 = Double.valueOf(datastr.substring(index4 + 1));
                } else {
                    //批號
                    qr02 = datastr.substring(index2 + 1, index3);
                    //數量
                    qr03 = Double.valueOf(datastr.substring(index3 + 1));
                }

                setupData(datastr, qr01, qr02, qr03);

            } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") || datastr.substring(0, 5).equals("BB525") || datastr.substring(0, 5).equals("BB527")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //極版標籤 BC527-2101000198_1_07030333C_29568
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;

                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1); //預備給 測試料號會多出 T_
                        //取得料號
                        qr01 = datastr.substring(index2 + 1, index3);
                        //取得批號
                        qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 2);
                        if (qr01.equals("T")) {
                            qr01 = datastr.substring(index2 + 1, index4);
                            qr03 = Double.valueOf(datastr.substring(index4 + 1));
                        } else {
                            //取得數量
                            qr03 = Double.valueOf(datastr.substring(index3 + 1));
                        }
                        setupData(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();
            } else if (datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") || datastr.substring(0, 5).equals("CC513") || datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") || datastr.substring(0, 5).equals("CC518") || datastr.substring(0, 8).equals("OLDSTAMP")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                        //特殊標籤 CC514-2207000073-1492_CC514-2206000253-236(CC5A4_KD91-2_NULL)_(03010374D_vỏ bình ắc quy_WP8.5-12)_20220711059001
                        Double qr03 = 0.0;

                        int index1 = datastr.indexOf("-", 6);
                        int index2 = datastr.indexOf("("); //第一個(位置
                        int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                        int index4 = datastr.indexOf("_", index3); //料號後的_位置
                        //取得料號
                        String qr01 = datastr.substring(index3 + 1, index4);
                        //取得批號
                        String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 1);
                        //取得數量
                        if (index2 > 32) {
                            String g_qr03_datastr = datastr.substring(0, index2);
                            Integer g_len_g_qr03_datastr = g_qr03_datastr.length();
                            Integer l_sl1 = 0, l_sl2 = 0, l_sl3 = 0, k = 0;

                            for (int i = 0; i <= g_len_g_qr03_datastr; i++) {
                                String l_code = "";
                                String j = "";
                                if (i < g_len_g_qr03_datastr) {
                                    j = g_qr03_datastr.substring(i, i + 1);
                                }

                                if (j.equals("_") || i == g_len_g_qr03_datastr) {
                                    if (i == g_len_g_qr03_datastr) {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        l_sl3 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                    } else {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        if (l_sl1 == 0) {
                                            l_sl1 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        } else {
                                            l_sl2 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        }
                                    }
                                    k = i + 1;
                                }
                            }

                            qr03 = Double.valueOf(l_sl1 + l_sl2 + l_sl3);
                        } else {
                            qr03 = Double.valueOf(datastr.substring(index1 + 1, index2));
                        }

                        setupData(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();
            } else if (datastr.startsWith("-", 5)) {
                //供應商條碼 BB421-2101000169_2_07100071A_20211007_792
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);
                //取得料號
                String qr01 = datastr.substring(index2 + 1, index3);
                //取得批號
                String qr02 = datastr.substring(index3 + 1, index4);
                //取得數量
                Double qr03 = Double.valueOf(datastr.substring(index4 + 1));

                setupData(datastr, qr01, qr02, qr03);
            } else {
                QR232_hangKhongDat.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(QR232_hangKhongDat.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg02));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    private void setupData(String datastr, String qr01, String qr02, Double qr03) {
        if (qr01.length() > 0) {
            String g_tenSP = "", g_quyCach = "";
            String res = get_ItemData("http://172.16.40.20/" + g_server + "/get_ima_file.php?IMA01=" + qr01);
            if (!res.equals("FALSE")) {
                try {
                    JSONArray jsonarray = new JSONArray(res);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonObject = jsonarray.getJSONObject(i);
                        g_tenSP = jsonObject.getString("TA_IMA02_1"); //Tên tiếng việt
                        g_quyCach = jsonObject.getString("TA_IMA021_1");  //Quy cách tiếng việt
                    }

                    String finalG_tenSP = g_tenSP;
                    String finalG_quyCach = g_quyCach;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edt_MVL.setText(qr01);
                            tv_tenVatLieu.setText(finalG_tenSP);
                            tv_QuyCach.setText(finalG_quyCach);
                            edt_soLuong.setText(String.valueOf(decimalFormat.format(qr03)));
                            tv_SoLo.setText(qr02);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "Mã vật liệu không tồn tại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //取得批號
    private String getdatecode(String apiUrl) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(apiUrl);
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
                return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    //取得料號資訊
    private String get_ItemData(String s) {
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

    private String upload_all(JSONObject ujobject, String uriURL) {
        String res = null;
        if (ujobject.length() > 0) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(uriURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(999999);
                conn.setReadTimeout(999999);
                conn.setDoInput(true); //允許輸入流，即允許下載
                conn.setDoOutput(true); //允許輸出流，即允許上傳

                OutputStream os = conn.getOutputStream();
                DataOutputStream writer = new DataOutputStream(os);
                writer.write(ujobject.toString().getBytes("UTF-8"));
                writer.flush();
                writer.close();
                os.close();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String result = reader.readLine();
                reader.close();
                res = result;
            } catch (Exception ex) {
                res = "FALSE";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        return res;
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Get a reference to EMDKManager
        this.emdkManager = emdkManager;

        // Get a  reference to the BarcodeManager feature object
        initBarcodeManager();

        // Initialize the scanner
        initScanner();
    }

    @Override
    public void onClosed() {

        // The EMDK closed unexpectedly. Release all the resources.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {

            // Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        String dataStr = "";
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            for (ScanDataCollection.ScanData data : scanData) {
                String a = data.getData();
                //ScanDataCollection.LabelType labelType=data.getLabelType();
                dataStr = a;
            }
            if (edt_MVL.getText().toString().length() == 0) {
                getMVL(dataStr);
            }
        }
    }

    @Override
    public void onStatus(StatusData statusData) {
        // The status will be returned on multiple cases. Check the state and take the action.
        // Get the current state of scanner in background
        StatusData.ScannerStates state = statusData.getState();
        String statusStr = "";
        // Different states of Scanner
        switch (state) {
            case IDLE:
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = statusData.getFriendlyName() + " is   enabled and idle...";
                // Change scanner configuration. This should be done while the scanner is in IDLE state.
                setConfig();
                try {
                    // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                    //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                    scanner.read();
                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                }
                break;
            case WAITING:
                // Scanner is waiting for trigger press to scan...
                statusStr = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                // Scanning is in progress...
                statusStr = "Scanning...";
                break;
            case DISABLED:
                // Scanner is disabledstatusStr = statusData.getFriendlyName()+" is disabled.";
                break;
            case ERROR:
                // Error has occurred during scanning
                statusStr = "An error has occurred.";
                break;
            default:
                break;
        }
        // Updates TextView with scanner state on UI thread.
        updateStatus(statusStr);
    }

    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner.release();
            } catch (Exception e) {
                updateStatus(e.getMessage());
            }
            scanner = null;
        }
    }

    private void setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                ScannerConfig config = scanner.getConfig();
                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true;
                }
                // Set scanner config
                scanner.setConfig(config);
            } catch (ScannerException e) {
                updateStatus(e.getMessage());
            }
        }
    }

    private void updateStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Update the status text view on UI thread with current scanner state
                tv_scannerSTT.setText("" + status);
            }
        });
    }

    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initScanner() {
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType = Scanner.TriggerType.HARD;

                try {
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner.enable();

                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                    deInitScanner();
                }
            } else {
                updateStatus("Failed to   initialize the scanner device.");
            }
        }

    }
}