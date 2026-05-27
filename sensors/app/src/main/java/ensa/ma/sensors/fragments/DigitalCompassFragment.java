package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import ensa.ma.sensors.R;

public class DigitalCompassFragment extends Fragment implements SensorEventListener {

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");
    private static final int COLOR_N    = Color.parseColor("#EF4444");

    private SensorManager sensorMgr;
    private Sensor accelSensor, magSensor;
    private TextView degreeTv, directionTv, arrowTv, statusTv;

    private final float[] accelBuf = new float[3];
    private final float[] magBuf   = new float[3];
    private boolean hasAccel = false, hasMag = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {

        sensorMgr  = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor   = sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        root.addView(buildPageHeader("Boussole", "Orientation magnétique"));
        root.addView(buildSpacer(32));

        // Carte Boussole
        LinearLayout cardCompass = buildCard(requireContext());
        cardCompass.setGravity(Gravity.CENTER);

        arrowTv = new TextView(requireContext());
        arrowTv.setText("↑");
        arrowTv.setTextColor(COLOR_N);
        arrowTv.setTextSize(100f);
        arrowTv.setTypeface(null, Typeface.BOLD);
        arrowTv.setGravity(Gravity.CENTER);

        degreeTv = new TextView(requireContext());
        degreeTv.setText("--°");
        degreeTv.setTextColor(TEXT_TITLE);
        degreeTv.setTextSize(56f);
        degreeTv.setTypeface(null, Typeface.BOLD);
        degreeTv.setGravity(Gravity.CENTER);

        directionTv = new TextView(requireContext());
        directionTv.setText("---");
        directionTv.setTextColor(ACCENT);
        directionTv.setTextSize(24f);
        directionTv.setTypeface(null, Typeface.BOLD);
        directionTv.setGravity(Gravity.CENTER);
        directionTv.setPadding(0, 0, 0, 16);

        cardCompass.addView(arrowTv);
        cardCompass.addView(degreeTv);
        cardCompass.addView(directionTv);
        root.addView(cardCompass);

        // Carte Statut
        LinearLayout cardStatus = buildCard(requireContext());
        cardStatus.addView(buildSectionLabel("Capteurs utilisés"));
        cardStatus.addView(buildSpacer(24));

        statusTv = new TextView(requireContext());
        statusTv.setTextColor(TEXT_BODY);
        statusTv.setTextSize(14f);
        updateStatusText();
        cardStatus.addView(statusTv);
        
        root.addView(cardStatus);

        return scroll;
    }

    private void updateStatusText() {
        String msg = String.format("Accéléromètre : %s\nMagnétomètre : %s",
                accelSensor != null ? "OK" : "ABSENT",
                magSensor != null ? "OK" : "ABSENT");
        statusTv.setText(msg);
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

    private View buildSectionLabel(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text.toUpperCase(Locale.ROOT));
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
        if (accelSensor != null) sensorMgr.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
        if (magSensor != null)   sensorMgr.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        if (ev.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(ev.values, 0, accelBuf, 0, 3);
            hasAccel = true;
        } else if (ev.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(ev.values, 0, magBuf, 0, 3);
            hasMag = true;
        }
        if (hasAccel && hasMag) computeAzimuth();
    }

    private void computeAzimuth() {
        float[] rot = new float[9], ori = new float[3];
        if (!SensorManager.getRotationMatrix(rot, null, accelBuf, magBuf)) return;
        SensorManager.getOrientation(rot, ori);
        float deg = (float) Math.toDegrees(ori[0]);
        if (deg < 0) deg += 360f;
        arrowTv.setRotation(-deg);
        degreeTv.setText(String.format(Locale.ROOT, "%.0f°", deg));
        directionTv.setText(toCardinal(deg));
    }

    private String toCardinal(float d) {
        if (d >= 337.5f || d < 22.5f) return "NORD";
        if (d < 67.5f) return "NORD-EST";
        if (d < 112.5f) return "EST";
        if (d < 157.5f) return "SUD-EST";
        if (d < 202.5f) return "SUD";
        if (d < 247.5f) return "SUD-OUEST";
        if (d < 292.5f) return "OUEST";
        return "NORD-OUEST";
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}
}