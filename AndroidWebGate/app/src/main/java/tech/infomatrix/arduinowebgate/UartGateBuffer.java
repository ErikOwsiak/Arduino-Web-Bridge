
package tech.infomatrix.arduinowebgate;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.String.format;


public class UartGateBuffer {

    private String name;
    private Queue<UartMsg> msgs;
    private int maxsize;
    private int tmpsize;
    private SQLiteDatabase sqLiteDatabase;
    private File db;


    public UartGateBuffer(String key, int maxsize) {
        this.name = key.replaceAll(":", "_") + ".db";
        this.maxsize = (maxsize == 0) ? 3600 : maxsize;
        this.db = new File(WebGate.appDir, format("%s/%s", "data", this.name));
        this.msgs = new LinkedList<UartMsg>();
        this.tmpsize = 512;
        this.initStore();
    }

    public void addUartMsg(String str) {
        this.pushMsg(new UartMsg(str));
        str = str.replace("'", "''");
        String qry = format("insert into uart_data values('%s', datetime());", str);
        this.sqLiteDatabase.execSQL(qry);
    }

    public UartMsg read() {
        if (this.msgs.size() > 0)
            return this.msgs.remove();
        return null;
    }

    public void close(){
        this.sqLiteDatabase.close();
    }

    private void pushMsg(UartMsg uartMsg){
        if(this.msgs.size() >= this.maxsize)
            this.msgs.remove();
        this.msgs.add(uartMsg);
    }

    public UartMsg peek() {
        return this.msgs.peek();
    }

    public int size() {
        return this.msgs.size();
    }

    private void initStore() {
        String tbl_name = "uart_data";
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(this.db, null);
        String sqlstr = format("select count(name) from sqlite_master where" +
                " type='table' and name='%s';", tbl_name);
        Cursor cursor = sqLiteDatabase.rawQuery(sqlstr, null);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 1) {
            cursor.close();
            this.sqLiteDatabase = sqLiteDatabase;
        } else {
            sqlstr = format("CREATE TABLE %s (buff TEXT, dts TEXT);", tbl_name);
            sqLiteDatabase.execSQL(sqlstr);
            this.sqLiteDatabase = sqLiteDatabase;
        }
    }

}
