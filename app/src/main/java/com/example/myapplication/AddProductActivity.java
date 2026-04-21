package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private TextInputEditText etName, etPrice, etDiscountPrice, etDescription, etImages;
    private TextInputEditText etScreen, etCpu, etRam, etRom, etBattery;
    private CheckBox cbBestSeller, cbNewArrival, cbHotDiscount;
    private Spinner spinnerCategory;
    private MaterialButton btnSave, btnCancel;
    private MaterialButton btnSelectImage;
    private RecyclerView rvSelectedImages;
    private SelectedImageAdapter imageAdapter;
    private List<String> selectedImagesList = new ArrayList<>();
    
    private DatabaseHelper dbHelper;
    private boolean isEdit = false;
    private Product productToEdit;
    private ProgressDialog progressDialog;
    private List<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        String localPath = saveImageToInternalStorage(uri);
                        if (localPath != null) {
                            selectedImagesList.add(localPath);
                            imageAdapter.notifyItemInserted(selectedImagesList.size() - 1);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupSpinner();
        setupRecyclerView();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (getIntent().hasExtra("is_edit")) {
            setupEditMode();
        }

        etImages.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String url = etImages.getText().toString().trim();
                if (!url.isEmpty()) {
                    selectedImagesList.add(url);
                    imageAdapter.notifyItemInserted(selectedImagesList.size() - 1);
                    etImages.setText("");
                    return true;
                }
            }
            return false;
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> validateAndSave());
        btnCancel.setOnClickListener(v -> finish());
        
        loadCategories();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etDiscountPrice = findViewById(R.id.et_discount_price);
        etDescription = findViewById(R.id.et_description);
        etImages = findViewById(R.id.et_images);
        
        etScreen = findViewById(R.id.et_spec_screen);
        etCpu = findViewById(R.id.et_spec_cpu);
        etRam = findViewById(R.id.et_spec_ram);
        etRom = findViewById(R.id.et_spec_rom);
        etBattery = findViewById(R.id.et_spec_battery);

        cbBestSeller = findViewById(R.id.cb_best_seller);
        cbNewArrival = findViewById(R.id.cb_new_arrival);
        cbHotDiscount = findViewById(R.id.cb_hot_discount);

        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSelectImage = findViewById(R.id.btn_select_image);
        rvSelectedImages = findViewById(R.id.rv_selected_images);
    }

    private void setupRecyclerView() {
        imageAdapter = new SelectedImageAdapter(this, selectedImagesList, position -> {
            selectedImagesList.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImagesList.size());
        });
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imageAdapter);
    }

    private void setupSpinner() {
        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        List<Category> cats = dbHelper.getAllCategories();
        categoryList.clear();
        for (Category c : cats) {
            categoryList.add(c.getName());
        }
        if (categoryList.isEmpty()) {
            categoryList.add("Điện thoại");
            categoryList.add("Máy tính");
            categoryList.add("Phụ kiện");
        }
        categoryAdapter.notifyDataSetChanged();
        
        if (isEdit && productToEdit != null && productToEdit.getCategory() != null) {
            int position = categoryAdapter.getPosition(productToEdit.getCategory());
            if (position >= 0) spinnerCategory.setSelection(position);
        }
    }

    private void setupEditMode() {
        isEdit = true;
        productToEdit = (Product) getIntent().getSerializableExtra("product_data");
        if (productToEdit != null) {
            etName.setText(productToEdit.getName());
            etPrice.setText(String.valueOf(productToEdit.getPrice()));
            etDiscountPrice.setText(String.valueOf(productToEdit.getDiscountPrice()));
            etDescription.setText(productToEdit.getDescription());
            
            selectedImagesList.clear();
            if (productToEdit.getImages() != null) {
                selectedImagesList.addAll(productToEdit.getImages());
            }
            imageAdapter.notifyDataSetChanged();
            
            etScreen.setText(productToEdit.getScreen());
            etCpu.setText(productToEdit.getCpu());
            etRam.setText(productToEdit.getRam());
            etRom.setText(productToEdit.getRom());
            etBattery.setText(productToEdit.getBattery());

            cbBestSeller.setChecked(productToEdit.isBestSeller());
            cbNewArrival.setChecked(productToEdit.isNewArrival());
            cbHotDiscount.setChecked(productToEdit.isHotDiscount());

            TextView tvTitle = findViewById(R.id.tv_add_product_title);
            if (tvTitle != null) tvTitle.setText("CẬP NHẬT SẢN PHẨM");
            btnSave.setText("CẬP NHẬT");
        }
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String discountPriceStr = etDiscountPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        Object selectedItem = spinnerCategory.getSelectedItem();
        String category = selectedItem != null ? selectedItem.toString() : "Khác";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImagesList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        long price, discountPrice = 0;
        try {
            price = Long.parseLong(priceStr);
            if (!TextUtils.isEmpty(discountPriceStr)) {
                discountPrice = Long.parseLong(discountPriceStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        finalizeSave(name, price, discountPrice, description, category);
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            File folder = new File(getFilesDir(), "product_images");
            if (!folder.exists()) folder.mkdirs();
            String fileName = "prod_" + System.currentTimeMillis() + ".jpg";
            File file = new File(folder, fileName);
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            is.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("SaveLocal", "Error: " + e.getMessage());
            return null;
        }
    }

    private void finalizeSave(String name, long price, long discountPrice, String description, String category) {
        if (isEdit && productToEdit != null) {
            fillProductData(productToEdit, name, price, discountPrice, description, category);
            dbHelper.updateProduct(productToEdit);
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Product newProduct = new Product();
            fillProductData(newProduct, name, price, discountPrice, description, category);
            newProduct.setRating(5);
            newProduct.setSoldQuantity(0);
            
            long result = dbHelper.addProduct(newProduct);
            if (result != -1) {
                Log.d(TAG, "Product saved successfully with ID: " + result);
                Toast.makeText(this, "Đã thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to save product to database");
                Toast.makeText(this, "Lỗi: Không thể lưu sản phẩm vào database!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fillProductData(Product product, String name, long price, long discountPrice, String description, String category) {
        product.setName(name);
        product.setPrice(price);
        product.setDiscountPrice(discountPrice);
        product.setDescription(description);
        
        // Copy the list to avoid reference issues
        product.setImages(new ArrayList<>(selectedImagesList));

        product.setCategory(category);
        product.setScreen(etScreen.getText().toString().trim());
        product.setCpu(etCpu.getText().toString().trim());
        product.setRam(etRam.getText().toString().trim());
        product.setRom(etRom.getText().toString().trim());
        product.setBattery(etBattery.getText().toString().trim());
        
        product.setBestSeller(cbBestSeller.isChecked());
        product.setNewArrival(cbNewArrival.isChecked());
        product.setHotDiscount(cbHotDiscount.isChecked());
    }
}
