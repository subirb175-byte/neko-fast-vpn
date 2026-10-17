package com.nekofast.vpn;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Neko Fast VPN");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 50);

        Button connectBtn = new Button(this);
        connectBtn.setText("Connect VPN");
        connectBtn.setTextSize(18);
        connectBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Connecting to VPN...", Toast.LENGTH_SHORT).show();
        });

        layout.addView(title);
        layout.addView(connectBtn);

        setContentView(layout);
    }
}
