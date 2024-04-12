package com.aries.print.util;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aries.print.MainActivity;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class IPv6SocketClient {
    private final static String TAG = "IPv6SocketClient";

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ReadMassageThread readThread;
    private boolean isConnected = false;

    public IPv6SocketClient(String ip, int port) {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);

            socket = new Socket();
            socket.connect(socketAddress);
            isConnected = socket.isConnected();
            if (isConnected) {
                Log.i(TAG, ip + " connect done.");
                readThread = new ReadMassageThread();
                readThread.start();
            } else {
                Log.w(TAG, ip + " connect failed.");
            }
        } catch (Exception e) {
            Log.e(TAG, "socket connect error.");
            e.printStackTrace();
        }
    }

    public static boolean supportIpv6() {
        NetworkInterface ni = null;
        try {
            ni = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
        } catch (SocketException e) {
            Log.e(TAG, "SocketException error.");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.e(TAG, "UnknownHostException error.");
            e.printStackTrace();
        }
        boolean hasIpv6 = false;
        if (ni != null) {
            for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                if (addr instanceof Inet6Address) {
                    hasIpv6 = true;
                    break;
                }
            }
        }
        return hasIpv6;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void sendMessage(String message) {
        if (isConnected) {
            try {
                if (socket != null) {
                    outputStream = socket.getOutputStream();
                    byte[] bytesToSend = message.getBytes();
                    outputStream.write(bytesToSend);
                    outputStream.flush();
                    Log.i(TAG, "send message " + message + " done.");
                }
            } catch (IOException e) {
                Log.e(TAG, "send message " + message + " error.");
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "can not send message while socket not connect.");
        }
    }

    public void release() {
        Log.w(TAG, "release socket now.");
        readThread.release();
        readThread.interrupt();
        try {
            readThread.join(50);
        } catch (InterruptedException e) {
            Log.w(TAG, "can not release read thread.");
        }
        disconnect();
    }

    private void disconnect() {
        Log.w(TAG, "disconnect socket now.");
        if (isConnected) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e(TAG, "socket disconnect error.");
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "not do disconnect.");
        }
        isConnected = false;
    }

    class ReadMassageThread extends Thread {
        private volatile boolean needRead = true;

        @Override
        public void run() {
            // super.run();
            Log.i(TAG, "ReadMassageThread start.");
            try {
                if (socket != null) {
                    inputStream = socket.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    String message;
                    while (needRead) {
                        if ((message = in.readLine()) != null) {
                            if (message.startsWith("@PJL INFO ID")) {
                                if ((message = in.readLine()) != null) {
                                    Log.w(TAG, "receive message:" + message);
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.getContext(), "Printer type:" + message, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "read error.");
                e.printStackTrace();
            }
            Log.i(TAG, "ReadMassageThread stop.");
        }

        public void release() {
            needRead = false;
        }
    }

    /**
     * 使用：CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable(){});
     */
    public static class CrazyThreadPool {
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = (CPU_COUNT + 1);
        private static final int KEEP_ALIVE = 1;
        private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
        private static final BlockingQueue<Runnable> WORKQUEUE = new LinkedBlockingQueue<>(64);
        private static final ThreadFactory THREADFACTORY = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "IPv6SocketClient #" + this.mCount.getAndIncrement());
            }
        };
        public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, WORKQUEUE, THREADFACTORY);
    }
}
