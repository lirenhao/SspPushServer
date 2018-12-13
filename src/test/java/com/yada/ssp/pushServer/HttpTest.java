package com.yada.ssp.pushServer;


import okhttp3.*;

import java.io.IOException;

public class HttpTest {

    public static void main(String[] args) {
        Headers headers = new Headers.Builder().add("Cookie", "huiyuan%5Fid=189954").build();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=GBK");
        String requestBody = "huiyuan_pass_two=190912&shuliang=1&zhekou=1&id=185";
        Request request = new Request.Builder()
                .headers(headers)
                .url("http://cnydvip.net/shop/gp_cart.aspx")
                .post(RequestBody.create(mediaType, requestBody)).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Headers headers = response.headers();
                System.out.println(new String(response.body().bytes(), "GBK"));
            }
        });
    }
}
