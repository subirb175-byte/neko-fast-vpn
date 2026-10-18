package com.nekofast.vpn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;

import java.io.InputStream;

public class MainActivity extends Activity {
    private static final String TAG = "WireGuardVPN";
    private static final int VPN_REQUEST_CODE = 1001;

    private Backend backend;
    private Tunnel tunnel;
    private Config config;
    private boolean isConnected = false;

    private TextView statusText;
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ১. UI সেটআপ
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("Neko Fast WireGuard");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);

        statusText = new TextView(this);
        statusText.setText("Status: Disconnected");
        statusText.setTextSize(16);
        statusText.setTextColor(Color.RED);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 50);

        connectBtn = new Button(this);
        connectBtn.setText("Connect WireGuard");
        connectBtn.setTextSize(18);
        connectBtn.setOnClickListener(v -> toggleVpn());

        layout.addView(title);
        layout.addView(statusText);
        layout.addView(connectBtn);
        setContentView(layout);

        // ২. WireGuard ব্যাকএন্ড এবং টানেল ইনিশিয়ালাইজেশন
        backend = new GoBackend(this);
        tunnel = new Tunnel() {
            @Override
            public String getName() {
                return "wg0";
            }

            @Override
            public void onStateChange(State newState) {
                runOnUiThread(() -> {
                    if (newState == State.UP) {
                        isConnected = true;
                        statusText.setText("Status: Connected");
                        statusText.setTextColor(Color.GREEN);
                        connectBtn.setText("Disconnect");
                    } else if (newState == State.DOWN) {
                        isConnected = false;
                        statusText.setText("Status: Disconnected");
                        statusText.setTextColor(Color.RED);
                        connectBtn.setText("Connect WireGuard");
                    }
                });
            }
        };

        // ৩. assets/wg.conf থেকে কনফিগারেশন লোড করা
        try (InputStream is = getAssets().open("wg.conf")) {
            config = Config.parse(is);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load wg.conf", e);
            Toast.makeText(this, "wg.conf ফাইল পাওয়া যায়নি!", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleVpn() {
        if (config == null) {
            Toast.makeText(this, "কনফিগারেশন লোড হয়নি", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isConnected) {
            // অ্যান্ড্রয়েড VPN পারমিশন চেক
            Intent intent = GoBackend.VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, VPN_REQUEST_CODE);
            } else {
                startWireGuard();
            }
        } else {
            stopWireGuard();
        }
    }

    private void startWireGuard() {
        new Thread(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.UP, config);
            } catch (Exception e) {
                Log.e(TAG, "Error starting tunnel", e);
            }
        }).start();
    }

    private void stopWireGuard() {
        new Thread(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.DOWN, null);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping tunnel", e);
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startWireGuard();
        }
    }
}
