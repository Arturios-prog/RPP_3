package com.example.RPP_3_Fedodeev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.RPP_3_Fedodeev.DBData.FeedReaderDbHelper;
import com.example.RPP_3_Fedodeev.DBData.FeedEntry;
import com.example.laba_three.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    Button but1;
    Button but2;
    Button but3;
    Intent intent;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    long lastRow;
    Toast toast;
    int currentFIO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Текущее имя в списке
        currentFIO = 0;
        lastRow = 0;

        // Создадим связь со вторым активити
        intent = new Intent(this, Activity_2.class);

        // Найдем кнопки
        but1 = (Button) findViewById(R.id.button1);
        but2 = (Button) findViewById(R.id.button2);
        but3 = (Button) findViewById(R.id.button3);

        // Создание БД
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        dbHelper.deleteData(db);


        // Запись в БД 5 студентов
        for (int i=0; i<5; i++) {
            lastRow = addToDB();
            if (i == 4) {
                toast = Toast.makeText(this, "5 записей добавлено", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


        // Описываем первую кнопку
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на новое активити
                startActivity(intent);
            }
        });

        // Описываем вторую кнопку
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastRow = addToDB();
                toast = Toast.makeText(getApplicationContext(), "запись добавлена", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // Описываем третью кнопку
        but3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(BaseColumns._ID, lastRow);
                values.put(FeedEntry.COLUMN_NAME_FIO, "Иванов Иван Иванович");
                values.put(FeedEntry.COLUMN_NAME_TIME, getCurrentTime());

                db.replaceOrThrow(FeedEntry.TABLE_NAME, null, values);

                Toast toast = Toast.makeText(MainActivity.this, lastRow+"-ая запись была изменена", Toast.LENGTH_SHORT);
                toast.show();

            }
        });

    }

    @Override
    protected void onDestroy() {
        dbHelper.deleteData(db);
        dbHelper.close();
        super.onDestroy();
    }

    // Функция для считывания содержимого из файла с ФИО
    private String readFromRaw(int numFIO)throws ClassCastException, IOException {
        // Создаем читатель файла
        Resources res = this.getResources();
        AssetManager.AssetInputStream stream = (AssetManager.AssetInputStream) res.openRawResource(R.raw.students);
        InputStreamReader buffer = new InputStreamReader(stream,"UTF-8");

        // Записываем содержимое файла в буфер
        int findEn = 0;
        int c;
        StringBuilder strFIO = new StringBuilder("");
        while((c=buffer.read())!=-1){
            if((char)c == '\n'){
                findEn++;
            }
            else if(findEn == numFIO) {
                strFIO.append((char)c);
            }
        }

        Log.d("MyTag", strFIO.toString());
        // Возвращаем содержимое
        return strFIO.toString();
    }

    // Получение текущего времени
    private String getCurrentTime(){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private long addToDB(){


        if (currentFIO<30) {
            ContentValues values = new ContentValues();
            // Считываем ФИО студента из файла
            String full_name = "";
            try {
                full_name = readFromRaw(currentFIO);
            } catch (IOException e) {
                e.printStackTrace();
                full_name = "Произошла ошибка чтения.";
            }

            values.put(FeedEntry.COLUMN_NAME_FIO, full_name);
            values.put(FeedEntry.COLUMN_NAME_TIME, getCurrentTime());

            long keyRow = db.insert(FeedEntry.TABLE_NAME, null, values);

            values.clear();

            currentFIO++;



            return keyRow;
        }

        toast = Toast.makeText(this, "Добавлять больше некого!", Toast.LENGTH_SHORT);
        toast.show();

        return lastRow;
    }
}
