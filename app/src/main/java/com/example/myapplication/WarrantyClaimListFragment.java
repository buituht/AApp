package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class WarrantyClaimListFragment extends Fragment {

    private String status;
    private RecyclerView rvClaims;
    private TextView tvNoClaims;
    private FirebaseFirestore db;
    private List<WarrantyClaim> claimList = new ArrayList<>();
    private WarrantyClaimAdminAdapter adapter;
    private WarrantyDAO warrantyDAO;

    public static WarrantyClaimListFragment newInstance(String status) {
        WarrantyClaimListFragment fragment = new WarrantyClaimListFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString("status");
        }
        db = FirebaseFirestore.getInstance();
        warrantyDAO = new WarrantyDAO();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng lại layout fragment_order_list vì nó chỉ chứa RecyclerView và TextView thông báo
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvClaims = view.findViewById(R.id.rv_orders);
        tvNoClaims = view.findViewById(R.id.tv_no_orders);
        tvNoClaims.setText("Không có yêu cầu bảo hành nào");

        rvClaims.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WarrantyClaimAdminAdapter(getContext(), claimList);
        
        adapter.setOnClaimUpdateListener(this::showUpdateStatusDialog);
        
        rvClaims.setAdapter(adapter);

        loadClaims();
        return view;
    }

    private void loadClaims() {
        Query query = db.collection("warranty_claims").orderBy("timestamp", Query.Direction.DESCENDING);
        
        if (status != null && !status.equals("Tất cả")) {
            query = query.whereEqualTo("status", status);
        }

        query.addSnapshotListener((value, error) -> {
            if (!isAdded()) return;
            
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi tải yêu cầu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (value != null) {
                claimList.clear();
                claimList.addAll(value.toObjects(WarrantyClaim.class));
                for (int i = 0; i < value.getDocuments().size(); i++) {
                    claimList.get(i).setClaimId(value.getDocuments().get(i).getId());
                }
                adapter.notifyDataSetChanged();
                tvNoClaims.setVisibility(claimList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showUpdateStatusDialog(WarrantyClaim claim) {
        String[] statuses = {"Pending", "Processing", "Resolved", "Rejected"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật trạng thái yêu cầu");
        
        int checkedItem = -1;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(claim.getStatus())) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(statuses, checkedItem, (dialog, which) -> {
            String newStatus = statuses[which];
            updateClaimStatus(claim, newStatus);
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateClaimStatus(WarrantyClaim claim, String newStatus) {
        if (claim.getClaimId() == null) return;
        
        warrantyDAO.updateClaimStatus(claim.getClaimId(), newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
