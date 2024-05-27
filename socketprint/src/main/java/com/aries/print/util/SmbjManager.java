package com.aries.print.util;

import android.util.Log;

import com.hierynomus.msfscc.fileinformation.*;
import com.hierynomus.smbj.*;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.PrinterShare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


public class SmbjManager implements Runnable {
    private final static String TAG = "SmbjManager";

    private static final SmbjManager instance;
    private static final String SHARE_IP = "192.168.10.210";
    private static final String SHARE_DOMAIN = "DESKTOP-A4MGP70";
    private static final String SHARE_USER_NAME = "Aries";
    private static final String SHARE_USER_PASSWORD = "aries";

    static {
        instance = new SmbjManager();
    }

    public static SmbjManager getInstance() {
        return instance;
    }

    /**
     * Constructor
     */
    public SmbjManager() {

    }

    public void showSharedFiles () {
        Log.i(TAG, "showSharedFiles start.");
        SmbConfig config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
                .withTimeout(120, TimeUnit.SECONDS) // 超时设置读，写和Transact超时(默认为60秒)
                .withSoTimeout(180, TimeUnit.SECONDS) // Socket超时(默认为0秒)
                .build();
        SMBClient client = new SMBClient(config);

        try (Connection connection = client.connect(SHARE_IP)) {
            Session session = connection.authenticate(new AuthenticationContext(SHARE_USER_NAME, SHARE_USER_PASSWORD.toCharArray(), null));

            // Connect to Share
            try (DiskShare share = (DiskShare) session.connectShare("share")) {
                for (FileIdBothDirectoryInformation f : share.list("", "*.TXT")) {
                    Log.i(TAG, "File : " + f.getFileName());
                }
            } catch (IOException e) {
                Log.e(TAG, "ConnectShare Error: " + e.getMessage());
            }

            // Connect to Share
            try (PrinterShare share = (PrinterShare) session.connectShare("fuji_xerox_c3373_t2")) {
                String text = "just do IT";
                InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
                share.print(stream);
                Log.w(TAG, "PrinterShare success.");
            } catch (IOException e) {
                Log.e(TAG, "PrinterShare Error: " + e.getMessage());
            }
        } catch (IOException e) {
            Log.e(TAG, "Connect Error: " + e.getMessage());
        }

        Log.i(TAG, "showSharedFiles end.");
    }

    @Override
    public void run() {
        showSharedFiles();
    }
}
