package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartActivity extends AppCompatActivity {

    public static List<Product> cartItemList = new ArrayList<>();
    private NonScrollListView lvCart;
    private TextView tvTotalPrice, tvSubtotal, tvDiscountAmount, tvAppliedVoucherInfo;
    private EditText etName, etPhone, etAddress, etVoucherCode;
    private Button btnApplyVoucher;
    private RadioGroup rgAddress;
    private RadioButton rbDefault, rbHome, rbCompany;
    private CartAdapter adapter;
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    private Voucher appliedVoucher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(this);
        initViews();
        loadUserInfo();
        setupAddressSelection();
        
        findViewById(R.id.btn_back_cart).setOnClickListener(v -> finish());
        findViewById(R.id.btn_checkout).setOnClickListener(v -> startPaymentProcess());

        btnApplyVoucher.setOnClickListener(v -> applyVoucherCode());
        tvAppliedVoucherInfo.setOnClickListener(v -> showVoucherSelectionDialog());

        adapter = new CartAdapter();
        lvCart.setAdapter(adapter);
        updateTotal();
    }

    private void initViews() {
        lvCart = findViewById(R.id.lv_cart_items);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscountAmount = findViewById(R.id.tv_discount_amount);
        tvAppliedVoucherInfo = findViewById(R.id.tv_applied_voucher_info);
        
        etName = findViewById(R.id.et_shipping_name);
        etPhone = findViewById(R.id.et_shipping_phone);
        etAddress = findViewById(R.id.et_shipping_address);
        etVoucherCode = findViewById(R.id.et_voucher_code);
        btnApplyVoucher = findViewById(R.id.btn_apply_voucher_code);
        
        rgAddress = findViewById(R.id.rg_shipping_address);
        rbDefault = findViewById(R.id.rb_address_default);
        rbHome = findViewById(R.id.rb_address_home);
        rbCompany = findViewById(R.id.rb_address_company);
    }

    private void startPaymentProcess() {
        if (MainActivity.currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        showQRPaymentDialog();
    }

    private void showQRPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment_qr, null);
        builder.setView(view);

        TextView tvAmount = view.findViewById(R.id.tv_payment_amount);
        ImageView imgQR = view.findViewById(R.id.img_qr_code);
        View scanLine = view.findViewById(R.id.view_scan_line);
        ProgressBar pbProcessing = view.findViewById(R.id.pb_payment_processing);
        TextView tvStatus = view.findViewById(R.id.tv_payment_status);

        long amount = calculateTotal();
        tvAmount.setText(String.format("%,d VNĐ", amount));

        // Sử dụng VietQR API để tạo mã QR thật (Vui lòng thay bằng STK của bạn)
        String bankId = "vcb"; // Vietcombank
        String accountNo = "1052593134"; // Thay bằng số tài khoản thật của bạn
        String accountName = "BUI THANH TU"; // Tên chủ tài khoản
        String description = "Thanh toan don hang " + UUID.randomUUID().toString().substring(0, 8);
        
        // URL format VietQR: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<ACCOUNT_NAME>
        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s&accountName=%s",
                bankId, accountNo, amount, description, accountName);

        Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.ic_ball)
                .into(imgQR);

        // Animation cho đường quét QR
        ObjectAnimator animator = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, 600f);
        animator.setDuration(2000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();

        AlertDialog dialog = builder.setCancelable(false)
                .setNegativeButton("Hủy giao dịch", (d, w) -> animator.cancel())
                .create();

        // Giả lập xác nhận thanh toán (Trong thực tế bạn cần lắng nghe biến động số dư qua Webhook/API)
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                animator.cancel();
                scanLine.setVisibility(View.GONE);
                pbProcessing.setVisibility(View.VISIBLE);
                tvStatus.setText("Đang xác thực giao dịch...");

                new Handler().postDelayed(() -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        completeOrder();
                    }
                }, 3000);
            }
        }, 8000); // Tăng thời gian chờ để trông thực tế hơn

        dialog.show();
    }

    private void completeOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        long finalTotal = calculateTotal();
        
        String fullOrderId = UUID.randomUUID().toString();
        String shortOrderId = fullOrderId.substring(0, 8).toUpperCase();
        
        Order newOrder = new Order(
                fullOrderId,
                MainActivity.currentUser.getUsername(),
                name,
                phone,
                address,
                new ArrayList<>(cartItemList),
                finalTotal,
                "Đang xử lý",
                System.currentTimeMillis()
        );

        long result = dbHelper.addOrder(newOrder);
        if (result != -1) {
            updateLoyaltyInfo(finalTotal);
            showSuccessDialog(shortOrderId);
            cartItemList.clear();
            appliedVoucher = null;
            updateTotal();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Lỗi hệ thống khi lưu đơn hàng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoyaltyInfo(long amountSpent) {
        if (MainActivity.currentUser != null) {
            int newPoints = (int) (amountSpent / 100000);
            MainActivity.currentUser.setPoints(MainActivity.currentUser.getPoints() + newPoints);
            MainActivity.currentUser.setTotalSpent(MainActivity.currentUser.getTotalSpent() + amountSpent);
            userDAO.updateUser(MainActivity.currentUser);
        }
    }

    private void showSuccessDialog(String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thanh toán thành công!");
        builder.setMessage("Đơn hàng #" + orderId + " của bạn đã được thanh toán qua QR Code.\n\n" +
                "Cảm ơn bạn đã mua sắm!");
        builder.setPositiveButton("Về trang chủ", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private void applyVoucherCode() {
        String code = etVoucherCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) return;

        Voucher voucher = dbHelper.getVoucherByCode(code);
        if (voucher != null) {
            if (System.currentTimeMillis() > voucher.getExpiryDate()) {
                Toast.makeText(this, "Mã giảm giá đã hết hạn", Toast.LENGTH_SHORT).show();
                return;
            }
            long subtotal = calculateSubtotal();
            if (subtotal < voucher.getMinOrderAmount()) {
                Toast.makeText(this, "Đơn hàng tối thiểu " + String.format("%,d", voucher.getMinOrderAmount()) + " VNĐ", Toast.LENGTH_SHORT).show();
                return;
            }
            appliedVoucher = voucher;
            tvAppliedVoucherInfo.setText("Đã áp dụng: " + voucher.getCode());
            updateTotal();
        }
    }

    private void showVoucherSelectionDialog() {
        List<Voucher> vouchers = dbHelper.getAllVouchers();
        if (vouchers.isEmpty()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vouchers, null);
        RecyclerView rvVouchers = dialogView.findViewById(R.id.rv_vouchers);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn mã giảm giá")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .create();

        VoucherAdapter voucherAdapter = new VoucherAdapter(this, vouchers, voucher -> {
            etVoucherCode.setText(voucher.getCode());
            applyVoucherCode();
            dialog.dismiss();
        });
        rvVouchers.setAdapter(voucherAdapter);
        dialog.show();
    }

    private void loadUserInfo() {
        User user = MainActivity.currentUser;
        if (user != null) {
            etName.setText(user.getFullName());
            etPhone.setText(user.getPhoneNumber());
            etAddress.setText(user.getAddress());
        }
    }

    private void setupAddressSelection() {
        rgAddress.setOnCheckedChangeListener((group, checkedId) -> {
            User user = MainActivity.currentUser;
            if (user == null) return;
            if (checkedId == R.id.rb_address_default) etAddress.setText(user.getAddress());
            else if (checkedId == R.id.rb_address_home) etAddress.setText(user.getHomeAddress());
            else if (checkedId == R.id.rb_address_company) etAddress.setText(user.getCompanyAddress());
        });
    }

    private long calculateSubtotal() {
        long total = 0;
        for (Product p : cartItemList) {
            total += (p.getDiscountPrice() > 0) ? p.getDiscountPrice() : p.getPrice();
        }
        return total;
    }

    private long calculateDiscount(long subtotal) {
        if (appliedVoucher == null) return 0;
        return "PERCENT".equals(appliedVoucher.getType()) 
                ? subtotal * appliedVoucher.getDiscountValue() / 100 
                : appliedVoucher.getDiscountValue();
    }

    private long calculateTotal() {
        long subtotal = calculateSubtotal();
        return Math.max(0, subtotal - calculateDiscount(subtotal));
    }

    private void updateTotal() {
        long subtotal = calculateSubtotal();
        long discount = calculateDiscount(subtotal);
        tvSubtotal.setText(String.format("%,d VNĐ", subtotal));
        tvDiscountAmount.setText(String.format("-%,d VNĐ", discount));
        tvTotalPrice.setText(String.format("%,d VNĐ", subtotal - discount));
        findViewById(R.id.tv_empty_cart).setVisibility(cartItemList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class CartAdapter extends BaseAdapter {
        @Override
        public int getCount() { return cartItemList.size(); }
        @Override
        public Object getItem(int position) { return cartItemList.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LayoutInflater.from(CartActivity.this).inflate(R.layout.item_cart, parent, false);
            Product p = cartItemList.get(position);
            ((TextView)convertView.findViewById(R.id.tv_cart_item_name)).setText(p.getName());
            long price = (p.getDiscountPrice() > 0) ? p.getDiscountPrice() : p.getPrice();
            ((TextView)convertView.findViewById(R.id.tv_cart_item_price)).setText(String.format("%,d VNĐ", price));
            ImageView img = convertView.findViewById(R.id.img_cart_product);
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                Glide.with(CartActivity.this).load(GlideUtils.getGlideUrlWithUserAgent(p.getImages().get(0))).into(img);
            }
            convertView.findViewById(R.id.btn_remove_cart).setOnClickListener(v -> {
                cartItemList.remove(position);
                updateTotal();
                notifyDataSetChanged();
            });
            return convertView;
        }
    }
}
