package com.xiong.newsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Message;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by xiong on 2017/2/18.
 * 多线程实现图片的加载
 */

public class ImageLoader {
//    private ImageView mImageView;
//    private String mUrl;
//    /**
//     * 通过Handler获取消息，处理ImageView的图像设置
//     */
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            //避免缓存图片对正确图片造成跳动影响
//            if (mImageView.getTag().equals(mUrl)){
//                mImageView.setImageBitmap((Bitmap) msg.obj);
//            }
//        }
//    };
//
//    /**
//     * 在url中通过Thread异步线程的模式将Bitmap解析出来，并以消息的形式传给主线程的Handler
//     */
//    public void showImageByThread (ImageView imageView, final String url){
//        mImageView = imageView;
//        mUrl = url;
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                Bitmap bitmap = showBitmapFromURL(url);
//                Message message = Message.obtain();
//                message.obj = bitmap;
//                mHandler.sendMessage(message);
//            }
//        }.start();
//    }

    /**
     * 从网络地址中解析图像
     * @param urlString url网络地址的字符串
     * @return 解析后的图像
     */
    public Bitmap showBitmapFromURL (String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * AsyncTask异步线程
     */
    public void showImageByAsyncTask (ImageView imageView, String url){
        new NewsAsyncTask(imageView, url).execute(url);
    }
    private class NewsAsyncTask extends AsyncTask <String, Void, Bitmap>{
        private String mUrl;
        private ImageView mImageView;

        public NewsAsyncTask (ImageView imageView, String url){
            mImageView = imageView;
            mUrl = url;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            return showBitmapFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
