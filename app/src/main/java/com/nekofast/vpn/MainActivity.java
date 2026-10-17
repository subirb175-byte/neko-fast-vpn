package com.nekofast.vpn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST_CODE = 1001;
    private boolean isConnected = false;
    private Button connectBtn;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("Neko Fast VPN");
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
        connectBtn.setText("Connect VPN");
        connectBtn.setTextSize(18);
        connectBtn.setOnClickListener(v -> toggleVpn());

        layout.addView(title);
        layout.addView(statusText);
        layout.addView(connectBtn);

        setContentView(layout);
    }

    private void toggleVpn() {
        if (!isConnected) {
            // অ্যান্ড্রয়েড সিস্টেমের কাছে VPN অনুমতির অনুরোধ
            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent != null) {
                // অনুমতি না থাকলে সিস্টেম পারমিশন ডায়ালগ দেখাবে
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            } else {
                // আগে থেকেই অনুমতি থাকলে সরাসরি সার্ভিস শুরু হবে
                startVpnService();
            }
        } else {
            stopVpnService();
        }
    }

    private void startVpnService() {
        Intent intent = new Intent(this, MyVpnService.class);
        startService(intent);
        isConnected = true;
        updateUI();
        Toast.makeText(this, "VPN Connected", Toast.LENGTH_SHORT).show();
    }

    private void stopVpnService() {
        Intent intent = new Intent(this, MyVpnService.class);
        intent.setAction("STOP");
        startService(intent);
        isConnected = false;
        updateUI();
        Toast.makeText(this, "VPN Disconnected", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (isConnected) {
            statusText.setText("Status: Connected");
            statusText.setTextColor(Color.GREEN);
            connectBtn.setText("Disconnect");
        } else {
            statusText.setText("Status: Disconnected");
            statusText.setTextColor(Color.RED);
            connectBtn.setText("Connect VPN");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVpnService();
            } else {
                Toast.makeText(this, "VPN-এর অনুমতি দেওয়া হয়নি", Toast.LENGTH_SHORT).show();
            }
        }
    }
        }
