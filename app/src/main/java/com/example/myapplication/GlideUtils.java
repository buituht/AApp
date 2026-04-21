package com.example.myapplication;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

public class GlideUtils {
    /**
     * Trả về đối tượng load cho Glide. 
     * Nếu là URL (http/https), trả về GlideUrl với User-Agent để tránh lỗi 403.
     * Nếu là đường dẫn cục bộ, trả về chính đường dẫn đó.
     */
    public static Object getGlideUrlWithUserAgent(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.startsWith("http")) {
            return new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36")
                    .build());
        }
        return url;
    }
}
