package com.nekofast.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.IOException;

public class MyVpnService extends VpnService {
    private static final String TAG = "NekoFastVPN";
    private ParcelFileDescriptor vpnInterface = null;
    private boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if ("STOP".equals(action)) {
            stopVpn();
            return START_NOT_STICKY;
        }

        startVpn();
        return START_STICKY;
    }

    private void startVpn() {
        if (isRunning) return;

        try {
            Builder builder = new Builder();
            // ভার্চুয়াল TUN আইপি অ্যাড্রেস
            builder.addAddress("10.0.0.2", 24);
            // সম্পূর্ণ ইন্টারনেট ট্রাফিক (0.0.0.0/0) এই ভিপিএন দিয়ে যাবে
            builder.addRoute("0.0.0.0", 0);
            // ডিএনএস সার্ভার
            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("1.1.1.1");
            builder.setSession("NekoFastVPN");

            // ভিপিএন ইন্টারফেস চালু করা
            vpnInterface = builder.establish();
            isRunning = true;
            Log.i(TAG, "VPN ইন্টারফেস সফলভাবে তৈরি হয়েছে।");

            // সার্ভিসটি চালু রাখার জন্য একটি ব্যাকগ্রাউন্ড থ্রেড
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "VPN চালু করতে ব্যর্থ হয়েছে", e);
            stopVpn();
        }
    }

    private void stopVpn() {
        isRunning = false;
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "VPN ইন্টারফেস বন্ধ করার সময় সমস্যা", e);
            }
            vpnInterface = null;
        }
        stopSelf();
        Log.i(TAG, "VPN সার্ভিস বন্ধ হয়েছে।");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVpn();
    }
}
