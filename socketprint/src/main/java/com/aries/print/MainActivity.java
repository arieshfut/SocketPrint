package com.aries.print;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aries.print.util.ActivityHelper;
import com.aries.print.util.IPv6SocketClient;
import com.aries.print.util.SmbjManager;
import com.aries.print.util.WifiPrinter;
import com.aries.print.util.SNMPManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author aries
 */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    public static Context context = null;
    public static String dumpPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        requestAppPermissions();
        requestLogPath();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestAppPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        // API Level >= 23
        permissions.addAll(Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ));

        String[] permissionsArray = new String[permissions.size()];
        for (int i = 0; i < permissions.size(); ++i) {
            permissionsArray[i] = permissions.get(i);
        }
        ActivityCompat.requestPermissions(this, permissionsArray, 1);
    }

    public void openPrint(View view) {
        if (view.getId() == R.id.btn_open_hp) {
            ActivityHelper.startActivityByComponentName(this, ActivityHelper.APPID_HP, ActivityHelper.MAIN_HP);
        } else if (view.getId() == R.id.btn_open_print_hand) {
            ActivityHelper.startActivityByComponentName(this, ActivityHelper.APPID_PRINTHAND, ActivityHelper.MAIN_PRINTHAND);
        } else if (view.getId() == R.id.btn_wifi_print) {
            ///wifiPrint("");
            WifiPrinter.CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    WifiPrinter wifiPrintHelper = new WifiPrinter("172.18.2.51", 9100);
                    wifiPrintHelper.printText("676810029020988879217932789789989879797978798178668");
                    wifiPrintHelper.printLine(1, "\n");
                    wifiPrintHelper.printText("android wifi print!");
                    ////wifiPrintHelper.qrCode("hello world hahhahahahahahahahh");
                    wifiPrintHelper.closeIOAndSocket();
                }
            });
            ///WifiPrintHelper.getInstance().qrCode("hello world !!");
        } else if (view.getId() == R.id.btn_pdf_print) {
            WifiPrinter.CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    WifiPrinter wifiPrintHelper = new WifiPrinter("192.168.10.211", 9100);
                    // wifiPrintHelper.openClientThread();//开启客户端接收消息线程
                    wifiPrintHelper.connect();
                    wifiPrintHelper.printerInfo();
                    String filePath = getDumpPath() + "/table3.pdf";
                    Log.i(TAG, "pdf file path:" + filePath);
                    wifiPrintHelper.printPDF(filePath);
                }
            });
        } else if (view.getId() == R.id.btn_snmp_state) {
            new Thread(new SNMPManager()).start();
        } else if (view.getId() == R.id.btn_ipv6_print) {
            boolean supportIPv6 = IPv6SocketClient.supportIpv6();
            Toast.makeText(MainActivity.getContext(), "Current device" + (supportIPv6 ? "" : " not") + " support IPv6", Toast.LENGTH_SHORT).show();
            if (supportIPv6) {
                IPv6SocketClient.CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String ipv6Address = "fe80::a00:37ff:fed4:f176";
                            String message = "@PJL INFO ID\r\n";
                            IPv6SocketClient client = new IPv6SocketClient(ipv6Address, 9100);

                            if (client.isConnected()) {
                                client.sendMessage(message);
                            }

                            Thread.sleep(30000);
                            client.release();
                        } catch (Exception e) {
                            Log.i(TAG, "something error now.");
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else if (view.getId() == R.id.btn_ipp_print) {
            printIppInfo();
        } else if (view.getId() == R.id.btn_smb_print) {
            new Thread(SmbjManager.getInstance()).start();;
        }
    }

    /**
     * 把View转为PDF，必须要在View 渲染完毕之后
     * 1.使用LayoutInflater反射出来的View不行；
     * 2. 将要转换成pdf的xml view文件include到一个界面中，将其设置成android:visibility=”invisible”就可以实现，不显示，但是能转换成PDF；
     * 3. 设置成gone不行；
     *
     * @param view
     * @param pdfName
     */
    private void createPdfFromView(@NonNull View view, @NonNull final String pdfName) {
        //1, 建立PdfDocument
        final PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                .Builder(view.getMeasuredWidth(), view.getMeasuredHeight(), 1)
                //设置绘制的内容区域，此处我预留了10的内边距
                .setContentRect(new Rect(10, 10, view.getMeasuredWidth() - 10, view.getMeasuredHeight() - 10))
                .create();
        PdfDocument.Page page = document.startPage(pageInfo);
        view.draw(page.getCanvas());
        //必须在close 之前调用，通常是在最后一页调用
        document.finishPage(page);
        //保存至SD卡
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = Environment.getExternalStorageDirectory() + File.separator + pdfName;
                    File e = new File(path);
                    if (e.exists()) {
                        e.delete();
                    }
                    document.writeTo(new FileOutputStream(e));
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 用系统框架打印PDF
     *
     * @param filePath
     */
    private void doPdfPrint(String filePath) {
        String jobName = "jobName";
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        MoPrintPdfAdapter myPrintAdapter = new MoPrintPdfAdapter(filePath);
        // 设置打印参数
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("id", Context.PRINT_SERVICE, 480, 320))
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
        printManager.print(jobName, myPrintAdapter, attributes);
    }

    private void printIppInfo() {
        OkHttpClient client = new OkHttpClient(); // 创建OkHttpClient实例
        Request request = new Request.Builder()
                .url("http://192.168.10.130:631") // 设置请求的URL
                .build(); // 创建Request对象

        Log.i(TAG, "printIppInfo start");
        try (Response response = client.newCall(request).execute()) { // 执行请求并获取Response
            if (response.isSuccessful()) { // 检查请求是否成功
                // 请求成功，处理响应体，例如转换为字符串
                String responseBody = response.body().string();
                Log.i(TAG, "printIppInfo:" + responseBody);
            } else {
                // 请求失败，处理错误情况
                Log.e(TAG, "printIppInfo Unexpected error:" + response);
            }
        } catch (Exception e) {
            // 处理异常，例如网络错误等
            e.printStackTrace();
        }
        Log.i(TAG, "printIppInfo end");
    }

    /**
     * android是可以通过wifi调用打印机打印图片或者文档的，在API19之前，调用打印机是通过Socket通信然后打印东西的，Socket是比较原始的通信模式，
     * 也是相对比较底层的，一般通过端口连接是可以连接任意两台机器进行数据传输并操作的
     * 先下载Hp print service 插件或Mopria PrintService”打印服务插件等其他打印服务插件，
     * 这是个移动端的service类型的app，提供搜索wifi下同一网段的打印机，
     * 安装后没有界面。如果没用这个插件的话，会显示一直在搜索中，安装完成后
     *
     * @param path
     */
    /*private void printPhoto(String path) {
        PrintHelperPrintHelper photoPrinter = new PrintHelper(this);
        //设置填充的类型，填充的类型指的是在A4纸上打印时的填充类型，两种模式
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //这里的第一个参数是打印的jobName 随便取值
        photoPrinter.printBitmap("jpgTestPrint", bitmap);
    }*/

    /**
     * 设置PNG图片的打印分辨率
     *
     * @param bitmap
     * @param file
     * @param dpi
     */
    public void save(Bitmap bitmap, File file, int dpi) {
        try {
            ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageByteArray);
            byte[] imageData = imageByteArray.toByteArray();
            imageByteArray.close();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(setDpi(imageData, dpi));
            fileOutputStream.close();
            imageData = null;
            Log.e("aaa", "saved");
        } catch (Exception e) {
            Log.e("aaa", "Wrong in Class 'BitmapToPng'");
        }
    }

    private byte[] setDpi(byte[] imageData, int dpi) {
        byte[] imageDataCopy = new byte[imageData.length + 21];
        System.arraycopy(imageData, 0, imageDataCopy, 0, 33);
        System.arraycopy(imageData, 33, imageDataCopy, 33 + 21, imageData.length - 33);

        int[] pHYs = new int[]{0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 23, 18, 0, 0, 23, 18, 1, 103, 159, 210, 82};

        for (int i = 0; i < 21; i++) {
            imageDataCopy[i + 33] = (byte) (pHYs[i] & 0xff);
        }

        dpi = (int) (dpi / 0.0254);
        imageDataCopy[41] = (byte) (dpi >> 24);
        imageDataCopy[42] = (byte) (dpi >> 16);
        imageDataCopy[43] = (byte) (dpi >> 8);
        imageDataCopy[44] = (byte) (dpi & 0xff);

        imageDataCopy[45] = (byte) (dpi >> 24);
        imageDataCopy[46] = (byte) (dpi >> 16);
        imageDataCopy[47] = (byte) (dpi >> 8);
        imageDataCopy[48] = (byte) (dpi & 0xff);
        return imageDataCopy;
    }

    public static Context getContext() {
        return context;
    }

    private void requestLogPath() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            String filePath = Environment.getExternalStorageDirectory().toString() + "/print";
            dumpPath = filePath;
            boolean flags = true;
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    flags = file.mkdirs();
                }
            } catch (Exception e) {
                Log.e(TAG, "create Dump dir=" + filePath + " with exception " + e);
                flags = false;
            }
            Log.i(TAG, "Create dir=" + flags + ", filePath=" + filePath);
        } else {
            dumpPath = Objects.requireNonNull(MainActivity.getContext().getExternalFilesDir(null)).getAbsolutePath();
        }
    }

    public static String getDumpPath() {
        Log.d(TAG, "getDumpPath: " + dumpPath);
        return dumpPath;
    }
}
