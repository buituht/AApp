package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiRecommendationService {
    private static final String TAG = "GeminiRecommendation";
    // Vui lòng điền API Key của bạn tại đây hoặc cấu hình qua BuildConfig/local.properties
    private static final String API_KEY = "AIzaSyB6xKi4HSGnloL94dM41alG1O2UKpayOms";
    
    private GenerativeModelFutures model;
    private Executor executor;

    public interface RecommendationCallback {
        void onRecommendationReceived(List<String> recommendedProductIds);
        void onError(Throwable t);
    }

    public GeminiRecommendationService() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    public void getRecommendations(List<Product> viewedHistory, List<Product> allProducts, RecommendationCallback callback) {
        if (API_KEY.equals("YOUR_GEMINI_API_KEY")) {
            callback.onError(new Exception("Vui lòng cấu hình GEMINI_API_KEY trong GeminiRecommendationService.java"));
            return;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Dựa trên lịch sử xem sản phẩm của người dùng sau đây:\n");
        for (Product p : viewedHistory) {
            prompt.append("- ").append(p.getName()).append(" (Danh mục: ").append(p.getCategory()).append(")\n");
        }
        
        prompt.append("\nHãy chọn tối đa 6 sản phẩm phù hợp nhất từ danh sách sản phẩm dưới đây để gợi ý cho họ:\n");
        for (Product p : allProducts) {
            prompt.append("ID: ").append(p.getId()).append(" | Tên: ").append(p.getName()).append(" | Danh mục: ").append(p.getCategory()).append("\n");
        }
        
        prompt.append("\nChỉ trả về danh sách các ID sản phẩm, cách nhau bởi dấu phẩy. Không kèm theo lời giải thích.");

        Content content = new Content.Builder()
                .addText(prompt.toString())
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String responseText = result.getText();
                if (responseText != null) {
                    List<String> ids = parseIds(responseText);
                    callback.onRecommendationReceived(ids);
                } else {
                    callback.onError(new Exception("Phản hồi từ AI trống"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t);
            }
        }, executor);
    }

    private List<String> parseIds(String response) {
        List<String> ids = new ArrayList<>();
        String[] split = response.split(",");
        for (String s : split) {
            String id = s.trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }
}
