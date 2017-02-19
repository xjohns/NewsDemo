package com.xiong.newsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiong on 2017/2/18.
 * 多线程实现图片的加载
 */

public class ImageLoader {
    private LruCache<String, Bitmap> mCaches;//创建Cache，通过键值对的形式保存数据
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;

    public ImageLoader(ListView listView) {
        mListView = listView;
        mTask = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 4;
        mCaches = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存时调用
                return value.getByteCount();
            }
        };
    }

    /**
     * 将Bitmap增加到缓存
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapByCache(url) == null) {
            mCaches.put(url, bitmap);
        }
    }

    /**
     * 从缓存中获取Bitmap
     */
    public Bitmap getBitmapByCache(String url) {
        return mCaches.get(url);
    }

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
    public Bitmap showBitmapFromURL(String urlString) {
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
    public void showImageByAsyncTask(ImageView imageView, String url) {
        //从缓存中取出图片
        Bitmap bitmap = getBitmapByCache(url);
        //如果缓存中没有则通过异步线程下载
        if (bitmap == null) {
//            new NewsAsyncTask(url).execute(url);
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 取消所有task
     */
    public void cancelAllTasks(){
        if (mTask != null){
            for (NewsAsyncTask task : mTask){
                /*
                官方解释是false if the task could not be cancelled, typically because it has already
                completed normally; true otherwise。
                 */
                task.cancel(false);
            }
        }
    }

    /**
     * 将显示图片的控制权从showImageByAsyncTask转交给loadImages，
     * 使得不再当getView时触发AsyncTask下载，而是当ListView滚动时。
     * 用来加载从start到end的所有图片
     */
    public void loadImages(int start, int end){
        for (int i = start; i < end; i ++){
            String url = NewsAdapter.URLS[i];
            //从缓存中取出图片
            Bitmap bitmap = getBitmapByCache(url);
            //如果缓存中没有则通过异步线程下载
            if (bitmap == null) {
//                new NewsAsyncTask(imageView, url).execute(url);
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);//启动之后将task传入task管理表mTask中
            } else {
                //将url作为唯一标识
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private String mUrl;
//        private ImageView mImageView;

        public NewsAsyncTask(String url) {//不再需要传入ImageView，可从ListView中去获取
//            mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            //从网络上获取图片
            Bitmap bitmap = showBitmapFromURL(url);
            if (bitmap != null){
                //将图片加入缓存
                addBitmapToCache(url, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if (mImageView.getTag().equals(mUrl)) {
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            //设置完bitmap后从集合中移除这个task
            mTask.remove(this);
        }
    }
}
