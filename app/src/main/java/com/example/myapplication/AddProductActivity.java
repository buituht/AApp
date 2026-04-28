package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private TextInputEditText etName, etPrice, etDiscountPrice, etDescription, etImages, etWarrantyMonths;
    private TextInputLayout tilImages;
    private TextInputEditText etScreen, etCpu, etRam, etRom, etBattery;
    private CheckBox cbBestSeller, cbNewArrival, cbHotDiscount;
    private Spinner spinnerCategory;
    private MaterialButton btnSave, btnCancel;
    private MaterialButton btnSelectImage;
    private RecyclerView rvSelectedImages;
    private SelectedImageAdapter imageAdapter;
    private List<String> selectedImagesList = new ArrayList<>();
    
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private boolean isEdit = false;
    private Product productToEdit;
    private List<String> categoryNamesList = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        String localPath = saveImageToInternalStorage(uri);
                        if (localPath != null) {
                            addImageToList(localPath);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productDAO = new ProductDAO(this);
        categoryDAO = new CategoryDAO(this);
        
        initViews();
        setupSpinner();
        setupRecyclerView();

        if (getIntent().hasExtra("is_edit")) {
            setupEditMode();
        }

        tilImages.setEndIconOnClickListener(v -> {
            String url = etImages.getText().toString().trim();
            if (!url.isEmpty()) {
                addImageToList(url);
                etImages.setText("");
            } else {
                Toast.makeText(this, "Vui lòng dán link hình ảnh", Toast.LENGTH_SHORT).show();
            }
        });

        etImages.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String url = etImages.getText().toString().trim();
                if (!url.isEmpty()) {
                    addImageToList(url);
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
        
        loadCategoriesFromFirebase();
    }

    private void addImageToList(String path) {
        selectedImagesList.add(path);
        imageAdapter.notifyItemInserted(selectedImagesList.size() - 1);
        rvSelectedImages.scrollToPosition(selectedImagesList.size() - 1);
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etDiscountPrice = findViewById(R.id.et_discount_price);
        etDescription = findViewById(R.id.et_description);
        etImages = findViewById(R.id.et_images);
        etWarrantyMonths = findViewById(R.id.et_warranty_months);
        tilImages = (TextInputLayout) etImages.getParent().getParent();
        
        tilImages.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        tilImages.setEndIconDrawable(android.R.drawable.ic_input_add);

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
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNamesList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void loadCategoriesFromFirebase() {
        categoryDAO.getAllCategories().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryNamesList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                if (cat != null) categoryNamesList.add(cat.getName());
            }
            if (categoryNamesList.isEmpty()) {
                categoryNamesList.add("Điện thoại");
                categoryNamesList.add("Máy tính");
                categoryNamesList.add("Phụ kiện");
            }
            categoryAdapter.notifyDataSetChanged();
            
            if (isEdit && productToEdit != null && productToEdit.getCategory() != null) {
                int position = categoryAdapter.getPosition(productToEdit.getCategory());
                if (position >= 0) spinnerCategory.setSelection(position);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading categories", e);
            Toast.makeText(this, "Không thể tải danh mục từ Firebase", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupEditMode() {
        isEdit = true;
        productToEdit = (Product) getIntent().getSerializableExtra("product_data");
        if (productToEdit != null) {
            etName.setText(productToEdit.getName());
            etPrice.setText(String.valueOf(productToEdit.getPrice()));
            etDiscountPrice.setText(String.valueOf(productToEdit.getDiscountPrice()));
            etDescription.setText(productToEdit.getDescription());
            
            if (productToEdit.getWarranty() != null) {
                etWarrantyMonths.setText(productToEdit.getWarranty().replace(" tháng", ""));
            }

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
        String warrantyMonths = etWarrantyMonths.getText().toString().trim();
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

        finalizeSave(name, price, discountPrice, description, category, warrantyMonths);
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

    private void finalizeSave(String name, long price, long discountPrice, String description, String category, String warrantyMonths) {
        if (isEdit && productToEdit != null) {
            fillProductData(productToEdit, name, price, discountPrice, description, category, warrantyMonths);
            productDAO.updateProduct(productToEdit).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi cập nhật Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Product newProduct = new Product();
            fillProductData(newProduct, name, price, discountPrice, description, category, warrantyMonths);
            newProduct.setRating(5);
            newProduct.setSoldQuantity(0);
            
            productDAO.addProduct(newProduct).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã thêm sản phẩm thành công vào Firebase!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi thêm vào Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void fillProductData(Product product, String name, long price, long discountPrice, String description, String category, String warrantyMonths) {
        product.setName(name);
        product.setPrice(price);
        product.setDiscountPrice(discountPrice);
        product.setDescription(description);
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
        
        if (!TextUtils.isEmpty(warrantyMonths)) {
            product.setWarranty(warrantyMonths + " tháng");
        } else {
            product.setWarranty("12 tháng"); // Mặc định nếu không nhập
        }
    }
}
