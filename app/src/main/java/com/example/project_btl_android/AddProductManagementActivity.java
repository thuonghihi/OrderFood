package com.example.project_btl_android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/** @noinspection ALL*/
public class AddProductManagementActivity extends AppCompatActivity {
    ArrayList<Category> myListCategory;
    EditText edtProductNameInAddProduct, edtProductPriceInAddProduct, edtProductDescriptionInAddProduct, edtProductQuantityInAddProduct;
    TextView txtBackToProductManagementFromAdd;
    Spinner spnCategoryInAddProduct;
    Button btnChooseImageInAddProduct, btnAdd;
    SQLiteDatabase database = null;
    private int idcat = 1;
    ImageView imgVProductImage;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtProductNameInAddProduct = findViewById(R.id.edtProductNameInAddProduct);
        edtProductPriceInAddProduct = findViewById(R.id.edtProductPriceInAddProduct);
        edtProductDescriptionInAddProduct = findViewById(R.id.edtProductDescriptionInAddProduct);
        edtProductQuantityInAddProduct = findViewById(R.id.edtProductQuantityInAddProduct);
        txtBackToProductManagementFromAdd = findViewById(R.id.txtBackToProductManagementFromAdd);
        spnCategoryInAddProduct = findViewById(R.id.spnCategoryInAddProduct);
        btnChooseImageInAddProduct = findViewById(R.id.btnChooseImageInAddProduct);
        imgVProductImage = findViewById(R.id.imgVProductImage);
        imgVProductImage.setVisibility(View.GONE);
        btnAdd = findViewById(R.id.btnAdd);
        myListCategory = new ArrayList<>();
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);

        txtBackToProductManagementFromAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Cursor cursor = database.query("Category", null, null, null, null, null, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Category category = new Category(cursor.getInt(0), cursor.getString(1));
            myListCategory.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, myListCategory) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position trong danh sách drop-down
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoryInAddProduct.setAdapter(adapter);

        btnChooseImageInAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                pickImageActivityResult.launch(intent);
            }
        });

        spnCategoryInAddProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                idcat = category.getId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtProductNameInAddProduct.getText().toString().trim();
                String price = edtProductPriceInAddProduct.getText().toString().trim();
                String des = edtProductDescriptionInAddProduct.getText().toString().trim();
                String quant = edtProductQuantityInAddProduct.getText().toString().trim();
                ContentValues myvalue = new ContentValues();
                myvalue.put("CategoryID", idcat);


                if (name.isEmpty()) {
                    edtProductNameInAddProduct.setError("Vui lòng nhập tên sản phẩm");
                    edtProductNameInAddProduct.requestFocus();
                    return;
                } else {
                    myvalue.put("Name", name);
                }

                if (price.isEmpty()) {
                    edtProductPriceInAddProduct.setError("Vui lòng nhập giá sản phẩm");
                    edtProductPriceInAddProduct.requestFocus();
                    return;
                } else {
                    float gia = Float.parseFloat(price);
                    myvalue.put("Price", gia);
                }

                myvalue.put("Description", des);

                if (quant.isEmpty()) {
                    edtProductQuantityInAddProduct.setError("Vui lòng nhập số lượng sản phẩm");
                    edtProductQuantityInAddProduct.requestFocus();
                    return;
                } else {
                    int sl = Integer.parseInt(quant);
                    myvalue.put("Quantity", sl);
                }

                if(bitmap != null){
                    bitmap = resizeBitmap(bitmap, 700, 700);
                    byte[] image = convertImageToByte(bitmap);
                    myvalue.put("Path", image);
                }
                else{
                    Toast.makeText(AddProductManagementActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }

                String msg = "";
                if (database.insert("Product", null, myvalue) == -1) {
                    msg = "Thêm thất bại";
                } else {
                    msg = "Thêm thành công";
                    Intent intenttoProductManagement = new Intent(AddProductManagementActivity.this, ProductManagementActivity.class);
                    startActivity(intenttoProductManagement);
                }
                Toast.makeText(AddProductManagementActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private byte[] convertImageToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public ActivityResultLauncher<Intent> pickImageActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            // Kiểm tra dung lượng của ảnh
                            int fileSize = getFileSize(selectedImageUri);
                            if (fileSize <= 4 * 1024 * 1024) { // Kiểm tra nếu dung lượng <= 5MB
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                                    bitmap = BitmapFactory.decodeStream(inputStream);
                                    imgVProductImage.setVisibility(View.VISIBLE);
                                    imgVProductImage.setImageBitmap(bitmap);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Hiển thị thông báo nếu dung lượng vượt quá 5MB
                                Toast.makeText(AddProductManagementActivity.this, "Dung lượng ảnh vượt quá  4MB. Vui lòng chọn ảnh khác.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    // Phương thức để lấy dung lượng của file từ Uri
    private int getFileSize(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        int fileSize = cursor.getInt(sizeIndex);
        cursor.close();
        return fileSize;
    }
    public Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (maxWidth > 0 && maxHeight > 0 && image != null) {
            int width = image.getWidth();
            int height = image.getHeight();

            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }

            return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        } else {
            return null; // Trả về null nếu có lỗi
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddProductManagementActivity.this, ProductManagementActivity.class);
        startActivity(intent);
    }
}