package com.example.klb_pda.QR232;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.widget.Toast;

import com.example.klb_pda.Constant_Class;

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
import java.util.Locale;

public class QR232DB {
    public SQLiteDatabase db = null;
    DecimalFormat decimalFormat;
    String g_server = null;
    String DATABASE_NAME = "QR232DB.db";

    String TABLE_NAME = "donkhongdat_table";
    String dkd01 = "dkd01"; //Xưởng
    String dkd02 = "dkd02"; //Mã đơn xử lý
    String dkd03 = "dkd03"; //Mã vật liệu
    String dkd04 = "dkd04"; //Số Lô
    String dkd05 = "dkd05"; //Số lượng
    String dkd06 = "dkd06"; //Số lượng đã xử lý
    String dkd07 = "dkd07"; //Ghi chú
    String dkd08 = "dkd08"; //Ngày lập đơn
    String dkd09 = "dkd09"; //Người lập đơn
    String dkd10 = "dkd10"; //Trạng thái đơn (0: mới ; 1: Kho xác nhận ; 2: Hoàn thành ; 3: Hủy)
    String dkd11 = "dkd11"; //Tên vật liệu
    String dkd12 = "dkd12"; //Quy cách vật liệu

    String TEMP_TABLE = "temp_table";
    String temp01 = "temp01"; //Mã đơn xử lý
    String temp02 = "temp02"; //Ngày
    String temp03 = "temp03"; //Số lượng
    String temp04 = "temp04"; //Số lượng đã xử lý
    String temp05 = "temp05"; //Số lượng phân bổ lần này
    String temp06 = "temp06"; //Mã vật liệu
    String temp07 = "temp07"; //Tên vật liệu
    String temp08 = "temp08"; //Quy cách vật liệu

    String HISTORY_TABLE = "History_table";
    String his01 = "his01"; //Xưởng
    String his02 = "his02"; //Mã đơn xử lý
    String his03 = "his03"; //MVL
    String his04 = "his04"; //Số lượng phân bổ
    String his05 = "his05"; //Ngày nhận vật liệu
    String his06 = "his06"; //Nhân viên nhận vật liệu

    String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            dkd01 + " TEXT," + dkd02 + " TEXT," + dkd03 + " TEXT," +
            dkd04 + " TEXT," + dkd05 + " TEXT," + dkd06 + " TEXT," +
            dkd07 + " TEXT," + dkd08 + " TEXT," + dkd09 + " TEXT," +
            dkd10 + " TEXT," + dkd11 + " TEXT," + dkd12 + " TEXT," + " PRIMARY KEY(dkd02))";

    String CREATE_TEMP_TABLE = "CREATE TABLE IF NOT EXISTS " + TEMP_TABLE + " (" +
            temp01 + " TEXT," + temp02 + " TEXT," + temp03 + " TEXT," +
            temp04 + " TEXT," + temp05 + " TEXT, " + temp06 + " TEXT," +
            temp07 + " TEXT," + temp08 + " TEXT )";

    String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE + " (" +
            his01 + " TEXT," + his02 + " TEXT," + his03 + " TEXT," +
            his04 + " TEXT," + his05 + " TEXT," + his06 + " TEXT)";


    private Context mCtx = null;

    public QR232DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        String pattern = "###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);

        g_server = Constant_Class.server;
    }

    public void createTable() {
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TEMP_TABLE);
            db.execSQL(CREATE_HISTORY_TABLE);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS " + TEMP_TABLE;
            String DROP_HISTORY_TABLE = "DROP TABLE IF EXISTS " + HISTORY_TABLE;
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_TEMP_TABLE);
            db.execSQL(DROP_HISTORY_TABLE);
            db.close();
        } catch (Exception e) {

        }
    }

    public String insertData(String Xuong, String MaDon, String MaVL, String SoLo,
                             String SoLuong, String SLdaxuly, String Ghichu,
                             String NgayLap, String NguoiLap, String Stt, String TenVL, String QuyCach) {
        String res = "FALSE";
        try {
            ContentValues args = new ContentValues();
            args.put(dkd01, Xuong);
            args.put(dkd02, MaDon);
            args.put(dkd03, MaVL);
            args.put(dkd04, SoLo);
            args.put(dkd05, String.valueOf(SoLuong));
            args.put(dkd06, SLdaxuly);
            args.put(dkd07, Ghichu);
            args.put(dkd08, NgayLap);
            args.put(dkd09, NguoiLap);
            args.put(dkd10, Stt);
            args.put(dkd11, TenVL);
            args.put(dkd12, QuyCach);
            db.insert(TABLE_NAME, null, args);

            res = "TRUE";
        } catch (Exception e) {
            res = "FALSE";
        }
        return res;
    }

    private String insertTempData(String dkd02, String dkd08, String dkd05, String dkd06, String Slphanbo, String g_item) {
        String res = "FALSE";
        try {
            ContentValues args = new ContentValues();
            args.put(temp01, dkd02);
            args.put(temp02, dkd08);
            args.put(temp03, dkd05);
            args.put(temp04, dkd06);
            args.put(temp05, String.valueOf(Slphanbo));
            args.put(temp06, g_item);
            db.insert(TEMP_TABLE, null, args);
            res = "TRUE";
        } catch (Exception e) {
            res = "FALSE";
        }
        return res;
    }

    private String insertHis(String g_factory, String temp01, String temp06, String temp05, String formattedDate, String id) {
        String res = "FALSE";
        try {
            ContentValues args = new ContentValues();
            args.put(his01, g_factory);
            args.put(his02, temp01);
            args.put(his03, temp06);
            args.put(his04, temp05);
            args.put(his05, formattedDate);
            args.put(his06, id);
            db.insert(HISTORY_TABLE, null, args);
            res = "TRUE";
        } catch (Exception e) {
            res = "FALSE";
        }
        return res;
    }

    public Cursor getTopData(String g_factory) {
        try {
            return db.rawQuery("SELECT 0 as _id,dkd01,dkd08,dkd03,cast((dkd05-dkd06) as string) as sl,dkd11,dkd12 FROM donkhongdat_table " +
                    " WHERE dkd10 = 0  and dkd01 = '" + g_factory + "' order by dkd01,dkd08  LIMIT 10", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public int getCountData(String g_factory) {
        try {
            Cursor c = db.rawQuery("SELECT count(dkd01) FROM donkhongdat_table " +
                    " WHERE dkd10 = 0  and dkd01 = '" + g_factory + "'", null);
            c.moveToFirst();
            return c.getInt(0);
        } catch (Exception e) {
            return 0;
        }
    }

    public Cursor getItemsData() {
        try {
            Cursor c = db.rawQuery("SELECT 0 as _id,temp01,temp02,temp03,temp04, cast(temp03-temp04 as String) as slchuaxyly ,temp05 " +
                    " FROM temp_table  ORDER BY temp02", null);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public void setTempData(String g_factory, String g_item) {
        Cursor g_query = db.rawQuery("SELECT 0 as _id,dkd08,dkd05,dkd06, cast(dkd05-dkd06 as String) as slchuaxyly ,0 as slphanbo,dkd02 FROM donkhongdat_table " +
                " WHERE dkd10 = 0  and dkd01 = '" + g_factory + "' and dkd03 = '" + g_item + "' order by dkd08", null);
        g_query.moveToFirst();
        del_temp_table();
        for (int i = 1; i <= g_query.getCount(); i++) {
            insertTempData(g_query.getString(g_query.getColumnIndexOrThrow("dkd02")),
                    g_query.getString(g_query.getColumnIndexOrThrow("dkd08")),
                    g_query.getString(g_query.getColumnIndexOrThrow("dkd05")),
                    g_query.getString(g_query.getColumnIndexOrThrow("dkd06")),
                    "0", g_item);

            g_query.moveToNext();
        }
    }

    public void del_temp_table() {
        db.execSQL("DELETE FROM temp_table");
    }

    public void del_hangkhongdat_table() {
        db.execSQL("DELETE FROM donkhongdat_table");
    }

    public void del_history_table() {
        db.execSQL("DELETE FROM History_table");
    }

    public void phantichSL(String SLnhan) {
        Double g_SLNhan = Double.parseDouble(SLnhan);
        if (g_SLNhan > 0) {
            db.execSQL("UPDATE " + TEMP_TABLE + " SET temp05= 0 ");
            Cursor c = db.rawQuery("SELECT temp01,temp02,temp03,temp04,(temp03-temp04) as slchuaxyly,temp05 FROM temp_table  ORDER BY temp02", null);
            c.moveToFirst();
            for (int i = 1; i <= c.getCount(); i++) {
                String g_madon = c.getString(c.getColumnIndexOrThrow("temp01"));
                Double g_chuaxuly = c.getDouble(c.getColumnIndexOrThrow("slchuaxyly"));
                if (g_SLNhan > 0.0) {
                    if (g_chuaxuly >= g_SLNhan) {
                        db.execSQL("UPDATE " + TEMP_TABLE + " SET temp05= " + decimalFormat.format(g_SLNhan) + " WHERE temp01='" + g_madon + "' ");
                        g_SLNhan = 0.0;
                    } else {
                        //Số lượng chưa xử lý nhỏ hơn lượng nhận được
                        db.execSQL("UPDATE " + TEMP_TABLE + " SET temp05= " + decimalFormat.format(g_chuaxuly) + " WHERE temp01='" + g_madon + "' ");
                        g_SLNhan = g_SLNhan - g_chuaxuly;
                    }
                }
                c.moveToNext();
            }
        }else {
            db.execSQL("UPDATE " + TEMP_TABLE + " SET temp05= 0 ");
        }
    }

    public Boolean sumTemp05() {
        Cursor c = db.rawQuery("SELECT sum(temp05) as temp05 FROM temp_table ", null);
        c.moveToFirst();
        if (c.getDouble(c.getColumnIndexOrThrow("temp05")) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void updateTempToHistory(String g_Factory, String ID, String g_Date) {
        Cursor c = db.rawQuery("SELECT * FROM temp_table WHERE temp05 > 0 ORDER BY temp01 ", null);
        c.moveToFirst();
        for (int i = 1; i <= c.getCount(); i++) {
            String g_madon = c.getString(c.getColumnIndexOrThrow("temp01"));
            String g_mvl = c.getString(c.getColumnIndexOrThrow("temp06"));
            String g_SLNhan = c.getString(c.getColumnIndexOrThrow("temp05"));

            //upload history data to Server (QR_IMP_FILE) (S)
            Thread insData_Thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    JSONObject jObeject = new JSONObject();
                    try {
                        jObeject.put("QR_IMP001", g_Factory);
                        jObeject.put("QR_IMP002", g_madon);
                        jObeject.put("QR_IMP003", g_mvl);
                        jObeject.put("QR_IMP004", g_SLNhan);
                        jObeject.put("QR_IMP005", g_Date);
                        jObeject.put("QR_IMP006", ID);
                        String res = upDataToServer(jObeject);

                        if (res.equals("TRUE")) {
                            //Lưu dữ liệu lịch sử nhận hàng
                            res = insertHis(g_Factory, g_madon, g_mvl, g_SLNhan, g_Date, ID);
                            if (res.equals("TRUE")) {
                                db.execSQL("UPDATE " + TABLE_NAME + " SET dkd06 = dkd06 +" + g_SLNhan + " WHERE dkd02='" + g_madon + "' ");
                            }

                            Toast.makeText(mCtx.getApplicationContext(), "Cập nhật hệ thống thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mCtx.getApplicationContext(), "Lỗi cập nhật hệ thống", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    Looper.loop();
                }
            });
            insData_Thread.start();
            //upload history data to Server (QR_IMP_FILE) (E)

            c.moveToNext();
        }

    }

    public void updateMainTableStatus() {
        //Cập nhật lại trạng thái đơn đã hoàn thành
        Cursor upd_status = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE dkd05 = dkd06 AND dkd10 = 0 ORDER BY dkd02 ", null);
        upd_status.moveToFirst();
        for (int up = 1; up <= upd_status.getCount(); up++) {
            String g_madon = upd_status.getString(upd_status.getColumnIndexOrThrow("dkd02"));
            db.execSQL("UPDATE " + TABLE_NAME + " SET dkd10 = 2 WHERE dkd02='" + g_madon + "' ");
            upd_status.moveToNext();
        }
    }

    private String upDataToServer(JSONObject jObeject) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR232/insert_QR_IMP_FILE.php");
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

}
