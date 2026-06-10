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
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
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
    private UserDAO userDAO;
    private WarrantyDAO warrantyDAO;
    private OrderDAO orderDAO;
    private VoucherDAO voucherDAO;
    private Voucher appliedVoucher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        userDAO = new UserDAO();
        warrantyDAO = new WarrantyDAO();
        orderDAO = new OrderDAO();
        voucherDAO = new VoucherDAO();
        
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

        String bankId = "vcb";
        String accountNo = "1052593134";
        String accountName = "BUI THANH TU";
        String description = "Thanh toan " + UUID.randomUUID().toString().substring(0, 8);
        
        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s&accountName=%s",
                bankId, accountNo, amount, description, accountName);

        Glide.with(this).load(qrUrl).placeholder(R.drawable.ic_ball).into(imgQR);

        ObjectAnimator animator = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, 600f);
        animator.setDuration(2000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();

        AlertDialog dialog = builder.setCancelable(false)
                .setNegativeButton("Hủy", (d, w) -> animator.cancel())
                .create();

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
                }, 2500);
            }
        }, 6000);

        dialog.show();
    }

    private void completeOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        long finalTotal = calculateTotal();
        
        Order newOrder = new Order(
                UUID.randomUUID().toString(),
                MainActivity.currentUser.getUsername(),
                MainActivity.currentUser.getEmail(),
                name, phone, address,
                new ArrayList<>(cartItemList),
                finalTotal, "Đang xử lý", System.currentTimeMillis()
        );

        orderDAO.placeOrder(newOrder).addOnSuccessListener(aVoid -> {
            updateLoyaltyInfo(finalTotal);
            createWarrantyCards(newOrder);
            showSuccessDialog(newOrder.getOrderId().substring(0, 8).toUpperCase());
            cartItemList.clear();
            appliedVoucher = null;
            updateTotal();
            adapter.notifyDataSetChanged();
        });
    }

    private void createWarrantyCards(Order order) {
        for (Product p : order.getItems()) {
            if (p.getWarranty() == null || p.getWarranty().isEmpty()) continue;
            long now = System.currentTimeMillis();
            WarrantyCard card = new WarrantyCard(null, order.getOrderId(), p.getId(), p.getName(),
                    order.getUserEmail(), order.getUsername(), now, calculateExpiryDate(now, p.getWarranty()), "Active", p.getWarranty());
            warrantyDAO.createWarrantyCard(card);
        }
    }

    private long calculateExpiryDate(long start, String info) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        try {
            String lower = info.toLowerCase();
            int num = Integer.parseInt(lower.replaceAll("[^0-9]", ""));
            if (lower.contains("tháng")) cal.add(Calendar.MONTH, num);
            else if (lower.contains("năm")) cal.add(Calendar.YEAR, num);
            else cal.add(Calendar.YEAR, 1);
        } catch (Exception e) { cal.add(Calendar.YEAR, 1); }
        return cal.getTimeInMillis();
    }

    private void updateLoyaltyInfo(long amount) {
        if (MainActivity.currentUser != null) {
            MainActivity.currentUser.setPoints(MainActivity.currentUser.getPoints() + (int)(amount/100000));
            MainActivity.currentUser.setTotalSpent(MainActivity.currentUser.getTotalSpent() + amount);
            userDAO.updateUser(MainActivity.currentUser);
        }
    }

    private void showSuccessDialog(String id) {
        new AlertDialog.Builder(this).setTitle("Thành công!").setMessage("Đơn hàng #" + id + " đã được đặt.")
                .setPositiveButton("OK", (d, w) -> finish()).setCancelable(false).show();
    }

    private void applyVoucherCode() {
        String code = etVoucherCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) return;
        voucherDAO.getVoucherByCode(code).addOnSuccessListener(snaps -> {
            if (!snaps.isEmpty()) {
                Voucher v = snaps.getDocuments().get(0).toObject(Voucher.class);
                if (v != null && System.currentTimeMillis() <= v.getExpiryDate() && calculateSubtotal() >= v.getMinOrderAmount()) {
                    appliedVoucher = v;
                    tvAppliedVoucherInfo.setText("Đã áp dụng: " + v.getCode());
                    updateTotal();
                } else { Toast.makeText(this, "Mã không đủ điều kiện", Toast.LENGTH_SHORT).show(); }
            } else { Toast.makeText(this, "Mã không hợp lệ", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void showVoucherSelectionDialog() {
        voucherDAO.getAllVouchers().addOnSuccessListener(snaps -> {
            List<Voucher> list = snaps.toObjects(Voucher.class);
            if (list.isEmpty()) return;
            String[] names = new String[list.size()];
            for(int i=0; i<list.size(); i++) names[i] = list.get(i).getCode() + " (Giảm " + list.get(i).getDiscountValue() + 
                    ("PERCENT".equals(list.get(i).getType()) ? "%" : " VNĐ") + ")";
            new AlertDialog.Builder(this).setTitle("Chọn Voucher").setItems(names, (d, w) -> {
                etVoucherCode.setText(list.get(w).getCode());
                applyVoucherCode();
            }).show();
        });
    }

    private void loadUserInfo() {
        if (MainActivity.currentUser != null) {
            etName.setText(MainActivity.currentUser.getFullName());
            etPhone.setText(MainActivity.currentUser.getPhoneNumber());
            etAddress.setText(MainActivity.currentUser.getAddress());
        }
    }

    private void setupAddressSelection() {
        rgAddress.setOnCheckedChangeListener((g, id) -> {
            if (MainActivity.currentUser != null && id == R.id.rb_address_default) 
                etAddress.setText(MainActivity.currentUser.getAddress());
        });
    }

    private long calculateSubtotal() {
        long sum = 0;
        for (Product p : cartItemList) sum += (p.getDiscountPrice() > 0 ? p.getDiscountPrice() : p.getPrice());
        return sum;
    }

    private long calculateTotal() {
        long sub = calculateSubtotal();
        if (appliedVoucher == null) return sub;
        long disc = "PERCENT".equals(appliedVoucher.getType()) ? (sub * appliedVoucher.getDiscountValue() / 100) : appliedVoucher.getDiscountValue();
        if (appliedVoucher.getMaxDiscount() > 0) disc = Math.min(disc, appliedVoucher.getMaxDiscount());
        return Math.max(0, sub - disc);
    }

    private void updateTotal() {
        long sub = calculateSubtotal();
        long total = calculateTotal();
        tvSubtotal.setText(String.format("%,d VNĐ", sub));
        tvDiscountAmount.setText(String.format("- %,d VNĐ", sub - total));
        tvTotalPrice.setText(String.format("%,d VNĐ", total));
    }

    private class CartAdapter extends BaseAdapter {
        @Override public int getCount() { return cartItemList.size(); }
        @Override public Object getItem(int i) { return cartItemList.get(i); }
        @Override public long getItemId(int i) { return i; }
        @Override public View getView(int i, View v, ViewGroup p) {
            if (v == null) v = LayoutInflater.from(CartActivity.this).inflate(R.layout.item_cart, p, false);
            Product prod = cartItemList.get(i);
            ((TextView)v.findViewById(R.id.tv_cart_item_name)).setText(prod.getName());
            long price = prod.getDiscountPrice() > 0 ? prod.getDiscountPrice() : prod.getPrice();
            ((TextView)v.findViewById(R.id.tv_cart_item_price)).setText(String.format("%,d VNĐ", price));
            ImageView img = v.findViewById(R.id.img_cart_product);
            if (!prod.getImages().isEmpty()) Glide.with(CartActivity.this).load(GlideUtils.getGlideUrlWithUserAgent(prod.getImages().get(0))).placeholder(R.drawable.ic_ball).into(img);
            v.findViewById(R.id.btn_remove_cart).setOnClickListener(view -> {
                cartItemList.remove(i); notifyDataSetChanged(); updateTotal();
            });
            return v;
        }
    }
}
