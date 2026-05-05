package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdminActivity extends AppCompatActivity {

    private ListView lvVouchers;
    private TextView tvEmpty;
    private View btnBack;
    private ImageButton btnAdd;
    private VoucherDAO voucherDAO;
    private List<Voucher> voucherList = new ArrayList<>();
    private VoucherAdminAdapter adapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_admin);

        voucherDAO = new VoucherDAO();
        initViews();
        setupAdapter();
        loadVouchers();
    }

    private void initViews() {
        lvVouchers = findViewById(R.id.lv_vouchers_admin);
        tvEmpty = findViewById(R.id.tv_empty_vouchers);
        btnBack = findViewById(R.id.btn_back_voucher_admin);
        btnAdd = findViewById(R.id.btn_add_voucher);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showVoucherDialog(null));
    }

    private void setupAdapter() {
        adapter = new VoucherAdminAdapter(this, voucherList, new VoucherAdminAdapter.OnVoucherActionListener() {
            @Override
            public void onEdit(Voucher voucher) {
                showVoucherDialog(voucher);
            }

            @Override
            public void onDelete(Voucher voucher) {
                showDeleteConfirmation(voucher);
            }
        });
        lvVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        voucherDAO.getAllVouchers().addOnSuccessListener(queryDocumentSnapshots -> {
            voucherList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Voucher v = doc.toObject(Voucher.class);
                if (v != null) {
                    v.setId(doc.getId());
                    voucherList.add(v);
                }
            }
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(voucherList.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showVoucherDialog(Voucher voucher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_voucher, null);
        builder.setView(view);

        EditText etCode = view.findViewById(R.id.et_voucher_code);
        EditText etDesc = view.findViewById(R.id.et_voucher_desc);
        EditText etValue = view.findViewById(R.id.et_voucher_value);
        EditText etMinOrder = view.findViewById(R.id.et_voucher_min_order);
        EditText etStartDate = view.findViewById(R.id.et_voucher_start_date);
        EditText etExpiryDate = view.findViewById(R.id.et_voucher_expiry_date);
        Spinner spType = view.findViewById(R.id.sp_voucher_type);

        String[] types = {"PERCENT", "FIXED"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 30);

        if (voucher != null) {
            builder.setTitle("Sửa Voucher");
            etCode.setText(voucher.getCode());
            etDesc.setText(voucher.getDescription());
            etValue.setText(String.valueOf(voucher.getDiscountValue()));
            etMinOrder.setText(String.valueOf(voucher.getMinOrderAmount()));
            spType.setSelection(voucher.getType().equals("PERCENT") ? 0 : 1);
            
            startCal.setTimeInMillis(voucher.getStartDate());
            endCal.setTimeInMillis(voucher.getExpiryDate());
        } else {
            builder.setTitle("Thêm Voucher");
        }

        etStartDate.setText(sdf.format(startCal.getTime()));
        etExpiryDate.setText(sdf.format(endCal.getTime()));

        etStartDate.setOnClickListener(v -> showDatePicker(startCal, etStartDate));
        etExpiryDate.setOnClickListener(v -> showDatePicker(endCal, etExpiryDate));

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String valueStr = etValue.getText().toString().trim();
            String minOrderStr = etMinOrder.getText().toString().trim();

            if (code.isEmpty() || desc.isEmpty() || valueStr.isEmpty() || minOrderStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startCal.after(endCal)) {
                Toast.makeText(this, "Ngày bắt đầu phải trước ngày hết hạn", Toast.LENGTH_SHORT).show();
                return;
            }

            Voucher v = (voucher == null) ? new Voucher() : voucher;
            v.setCode(code);
            v.setDescription(desc);
            v.setDiscountValue(Long.parseLong(valueStr));
            v.setMinOrderAmount(Long.parseLong(minOrderStr));
            v.setType(spType.getSelectedItem().toString());
            v.setStartDate(startCal.getTimeInMillis());
            v.setExpiryDate(endCal.getTimeInMillis());
            
            if (voucher == null) {
                voucherDAO.addVoucher(v).addOnSuccessListener(aVoid -> {
                    loadVouchers();
                    Toast.makeText(VoucherAdminActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                });
            } else {
                voucherDAO.updateVoucher(v).addOnSuccessListener(aVoid -> {
                    loadVouchers();
                    Toast.makeText(VoucherAdminActivity.this, "Đã cập nhật thành công", Toast.LENGTH_SHORT).show();
                });
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showDatePicker(Calendar calendar, EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showDeleteConfirmation(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Voucher")
                .setMessage("Bạn có chắc chắn muốn xóa voucher này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    voucherDAO.deleteVoucher(voucher.getId()).addOnSuccessListener(aVoid -> {
                        loadVouchers();
                        Toast.makeText(VoucherAdminActivity.this, "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
