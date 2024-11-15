package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** @noinspection ALL*/
public class ManagementHomepageActivity extends AppCompatActivity {
    CardView cVCategoryManagement;
    CardView cVProductManagement;
    CardView cVStatisticManagement;
    long backPressTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_management_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cVCategoryManagement = findViewById(R.id.cVCategoryManagement);
        cVProductManagement = findViewById(R.id.cVProductManagement);
        cVStatisticManagement = findViewById(R.id.cVStatisticManagement);
        cVCategoryManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagementHomepageActivity.this, CategoryManagementActivity.class);
                startActivity(intent);
            }
        });

        cVProductManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagementHomepageActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            }
        });
        cVStatisticManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(ManagementHomepageActivity.this, StatisticManagementActivity.class);
                    startActivity(intent);
                }
                catch (Exception e){
                    Toast.makeText(ManagementHomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 3000 > System.currentTimeMillis()){
            Intent intent = new Intent(ManagementHomepageActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        else{
            Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
        }
        backPressTime = System.currentTimeMillis();
    }
}