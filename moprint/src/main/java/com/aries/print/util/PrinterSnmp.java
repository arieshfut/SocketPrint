package com.aries.print.util;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


/**
 * @author : Crazy.Mo
 */
public class PrinterSnmp {
    private final static String TAG = "PrinterSnmp";

    private final String mRemoteIp;
    private final String mSelfIp;
    private final int mPort;
    private final String mCommunity; // SNMP团体名

    public PrinterSnmp(String ip, int port) {
        mCommunity = "public"; // SNMP团体名
        mSelfIp = "192.168.10.156"; // 打印机的IP地址
        mRemoteIp = ip;
        mPort = port;

        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");

        Log.i(TAG, "constructor done.");
    }

    public void printerInfo() {
        try {
            ResponseEvent response;
            OctetString community1 = new OctetString(mCommunity);
            String host = mRemoteIp + "/161"; //  + mPort;
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(community1);
            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(new UdpAddress(host));
            comtarget.setRetries(2);
            comtarget.setTimeout(5000);
            PDU pdu = new PDU();
            String oidValue = "1.3.6.1.2.1.25.3.2.1.5.1";
            pdu.add(new VariableBinding(new OID(oidValue)));
            pdu.setType(PDU.GET);
            Snmp snmp = new Snmp(transport);
            response = snmp.get(pdu, comtarget);
            if (response != null) {
                PDU responsePDU = response.getResponse();
                if (responsePDU != null) {
                    Log.i(TAG, "Got Response from Agent  "
                            + response.getResponse().toString());
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();

                    if (errorStatus == PDU.noError) {
                        Log.i(TAG, "Snmp Get Response = " + responsePDU.getVariableBindings());
                        Log.i(TAG, "Snmp Get Response = " + responsePDU.getErrorStatusText());
                        Log.i(TAG, "--" + responsePDU.getVariableBindings());

                    } else {
                        Log.e(TAG, "Error: Request Failed");
                        Log.e(TAG, "Error Status = " + errorStatus);
                        Log.e(TAG, "Error Index = " + errorIndex);
                        Log.e(TAG, "Error Status Text = " + errorStatusText);
                    }

                } else {
                    Log.e(TAG, "Error: Response PDU is null");
                }
            } else {
                Log.e(TAG, "Error: Agent Timeout... ");
            }
            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
                return new Thread(r, "PrinterSnmp #" + this.mCount.getAndIncrement());
            }
        };
        public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, WORKQUEUE, THREADFACTORY);
    }
}



