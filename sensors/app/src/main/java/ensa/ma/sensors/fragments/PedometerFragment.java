package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.R;

public class PedometerFragment extends Fragment implements SensorEventListener {

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");
    private static final int ACCENT_2   = Color.parseColor("#10B981");

    private SensorManager sensorMgr;
    private Sensor stepSensor;
    private TextView stepsSessionTv;
    private TextView stepsSinceBootTv;
    private TextView statusTv;

    private float sessionBaseline = -1f;
    private final Handler simHandler = new Handler(Looper.getMainLooper());
    private int simTotal = 1000;

    private final ActivityResultLauncher<String> permissionRequest =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) connectSensor();
                        else statusTv.setText("Permission refusée.");
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {

        sensorMgr  = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        root.addView(buildPageHeader("Compteur de pas", "Suivi de votre activité physique"));
        root.addView(buildSpacer(32));

        // Grande carte session
        LinearLayout cardSession = buildCard(requireContext());
        cardSession.setGravity(Gravity.CENTER);

        TextView labelSession = buildSectionLabel("Pas cette session");
        labelSession.setGravity(Gravity.CENTER);

        stepsSessionTv = new TextView(requireContext());
        stepsSessionTv.setText("0");
        stepsSessionTv.setTextColor(ACCENT_2);
        stepsSessionTv.setTextSize(80f);
        stepsSessionTv.setTypeface(null, Typeface.BOLD);
        stepsSessionTv.setGravity(Gravity.CENTER);
        stepsSessionTv.setPadding(0, 16, 0, 16);

        cardSession.addView(labelSession);
        cardSession.addView(stepsSessionTv);
        root.addView(cardSession);

        // Carte détails
        LinearLayout cardDetails = buildCard(requireContext());
        cardDetails.addView(buildSectionLabel("Historique et Statut"));
        cardDetails.addView(buildSpacer(24));

        stepsSinceBootTv = new TextView(requireContext());
        stepsSinceBootTv.setTextColor(TEXT_TITLE);
        stepsSinceBootTv.setTextSize(16f);
        stepsSinceBootTv.setText("Depuis le démarrage : --");
        cardDetails.addView(stepsSinceBootTv);

        cardDetails.addView(buildSpacer(12));

        statusTv = new TextView(requireContext());
        statusTv.setTextColor(TEXT_MUTED);
        statusTv.setTextSize(14f);
        statusTv.setText("Initialisation...");
        cardDetails.addView(statusTv);

        root.addView(cardDetails);

        return scroll;
    }

    private LinearLayout buildCard(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(ctx, R.drawable.card_bg));
        card.setPadding(40, 40, 40, 40);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        card.setLayoutParams(lp);
        return card;
    }

    private TextView buildSectionLabel(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text.toUpperCase());
        tv.setTextColor(TEXT_MUTED);
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setLetterSpacing(0.1f);
        return tv;
    }

    private View buildPageHeader(String title, String subtitle) {
        LinearLayout block = new LinearLayout(requireContext());
        block.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTextColor(TEXT_TITLE);
        t.setTextSize(28f);
        t.setTypeface(null, Typeface.BOLD);
        TextView s = new TextView(requireContext());
        s.setText(subtitle);
        s.setTextColor(TEXT_MUTED);
        s.setTextSize(14f);
        s.setPadding(0, 8, 0, 0);
        block.addView(t);
        block.addView(s);
        return block;
    }

    private View buildSpacer(int dp) {
        View v = new View(requireContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stepSensor == null) {
            statusTv.setText("Mode simulation actif (capteur absent)");
            startSimulation();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            connectSensor();
        }
    }

    private void connectSensor() {
        sensorMgr.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        statusTv.setText("Capteur réel actif");
    }

    private void startSimulation() {
        sessionBaseline = simTotal;
        simHandler.postDelayed(new Runnable() {
            @Override public void run() {
                simTotal++;
                int session = simTotal - (int) sessionBaseline;
                stepsSessionTv.setText(String.valueOf(session));
                stepsSinceBootTv.setText("Depuis le démarrage : " + simTotal);
                simHandler.postDelayed(this, 1500);
            }
        }, 1500);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
        simHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        float total = ev.values[0];
        if (sessionBaseline < 0) sessionBaseline = total;
        stepsSessionTv.setText(String.valueOf((int)(total - sessionBaseline)));
        stepsSinceBootTv.setText("Depuis le démarrage : " + (int) total);
    }

    @Override
    public void onAccuracyChanged(Sensor s, int acc) { }
}