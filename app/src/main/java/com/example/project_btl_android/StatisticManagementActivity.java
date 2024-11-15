package com.example.project_btl_android;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** @noinspection ALL*/
public class StatisticManagementActivity extends AppCompatActivity {
    TextView txtBackToProductManagementFromStatistic, txtName, txtRevenue, txtRevenueTotal;
    EditText edtTimeFrom, edtTimeTo;
    ListView lvProductInStatistic;
    ArrayAdapterProductInStatistic myAdapterInStatistic;
    ArrayList<Product> myProductListInStatistic;
    SQLiteDatabase database = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date fromDate = null, toDate = null, currentDate = null;
    Calendar minDate = Calendar.getInstance(), maxDate = Calendar.getInstance();
    Double revenueTotal = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistic_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtBackToProductManagementFromStatistic = findViewById(R.id.txtBackToProductManagementFromStatistic);
        edtTimeFrom = findViewById(R.id.edtTimeFrom);
        edtTimeTo = findViewById(R.id.edtTimeTo);
        txtName = findViewById(R.id.txtName);
        txtRevenue = findViewById(R.id.txtRevenue);
        txtRevenueTotal = findViewById(R.id.txtRevenueTotal);
        txtName.setVisibility(View.GONE);
        txtRevenue.setVisibility(View.GONE);
        lvProductInStatistic = findViewById(R.id.mainInViewProduct);
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        myProductListInStatistic = new ArrayList<>();
        Cursor cursor = database.query("Product", null, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false){
            Product product = new Product(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getBlob(6));
            myProductListInStatistic.add(product);
            cursor.moveToNext();
        }
        cursor.close();

        if(edtTimeFrom.getText().toString().isEmpty()){
            edtTimeTo.setEnabled(false);
        }
        txtBackToProductManagementFromStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(edtTimeTo.getText().toString().isEmpty()) {
                        currentDate = sdf.parse(String.valueOf(LocalDate.now()));
                        maxDate.setTime(currentDate);
                    }
                    else{
                        toDate = sdf.parse(edtTimeTo.getText().toString());
                        maxDate.setTime(toDate);
                    }
                    showDatePickerDialog(edtTimeFrom, null, maxDate);
                } catch (ParseException e) {
                    Log.d("bug1", e.getMessage());
                }
            }
        });


        edtTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fromDate = sdf.parse(edtTimeFrom.getText().toString());
                    minDate.setTime(fromDate);
                    showDatePickerDialog(edtTimeTo, minDate, null);
                    toDate = sdf.parse(edtTimeTo.getText().toString());
                } catch (ParseException e) {
                    Log.d("bug2: ", e.getMessage());
                }
            }
        });


        edtTimeFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Statistic();
            }
        });

        edtTimeTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtName.setVisibility(View.VISIBLE);
                txtRevenue.setVisibility(View.VISIBLE);
                Statistic();
            }
        });
    }

    private void showDatePickerDialog(EditText edt, @Nullable Calendar minDate,@Nullable Calendar maxDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Xử lý ngày được chọn ở đây
                        String selectedDate = String.format(year + "-" + String.format("%02d", (month + 1)) + "-" + "%02d", dayOfMonth);
                        edt.setText(selectedDate); // Hiển thị ngày đã chọn trong EditText
                        if(!edtTimeFrom.getText().toString().isEmpty()){
                            edtTimeTo.setEnabled(true);
                        }
                    }
                },
                year, month, dayOfMonth);
        if(minDate != null){
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }
        if(maxDate != null){
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        }
        datePickerDialog.show();
    }

    public void Statistic(){
        if(!edtTimeFrom.getText().toString().isEmpty() && !edtTimeTo.getText().toString().isEmpty()){
            myAdapterInStatistic = new ArrayAdapterProductInStatistic(StatisticManagementActivity.this, R.layout.layout_product_statistic, myProductListInStatistic);
            lvProductInStatistic.setAdapter(myAdapterInStatistic);
            revenueTotal = 0.0;
            Cursor cursor = database.rawQuery(
                    "SELECT Total " +
                    "FROM Bill " +
                    "WHERE CreateDay >= ? AND CreateDay <= ? ",
                    new String[]{
                            edtTimeFrom.getText().toString(),
                            edtTimeTo.getText().toString()
                    });
            if(cursor != null){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    revenueTotal += cursor.getDouble(0);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            txtRevenueTotal.setText(revenueTotal + " VND");
        }
    }

}