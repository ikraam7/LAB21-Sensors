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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import ensa.ma.sensors.R;

public class ActivityDetectorFragment extends Fragment implements SensorEventListener {

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");

    private SensorManager sensorMgr;
    private Sensor accelSensor;

    private TextView activityTv;
    private TextView activityBadge;
    private TextView statsTv;
    private TextView rawTv;

    private static final float ALPHA  = 0.80f;
    private static final int   WINDOW = 30;
    private final float[]  grav   = new float[3];
    private final Deque<Float> win = new ArrayDeque<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {

        sensorMgr   = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        root.addView(buildPageHeader("Activité", "Reconnaissance de mouvement"));
        root.addView(buildSpacer(32));

        // Carte activite principale
        LinearLayout cardActivity = buildCard(requireContext());
        cardActivity.setGravity(Gravity.CENTER_HORIZONTAL);

        cardActivity.addView(buildSectionLabel("État détecté"));
        cardActivity.addView(buildSpacer(24));

        activityTv = new TextView(requireContext());
        activityTv.setText("CALIBRATION...");
        activityTv.setTextColor(TEXT_TITLE);
        activityTv.setTextSize(32f);
        activityTv.setTypeface(null, Typeface.BOLD);
        activityTv.setGravity(Gravity.CENTER);
        cardActivity.addView(activityTv);

        activityBadge = new TextView(requireContext());
        activityBadge.setTextSize(12f);
        activityBadge.setTypeface(null, Typeface.BOLD);
        activityBadge.setPadding(24, 8, 24, 8);
        activityBadge.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.setMargins(0, 16, 0, 0);
        activityBadge.setLayoutParams(badgeLp);
        
        cardActivity.addView(activityBadge);
        root.addView(cardActivity);

        // Carte stats
        LinearLayout cardStats = buildCard(requireContext());
        cardStats.addView(buildSectionLabel("Statistiques du signal"));
        cardStats.addView(buildSpacer(24));
        
        statsTv = new TextView(requireContext());
        statsTv.setTextColor(TEXT_BODY);
        statsTv.setTextSize(14f);
        statsTv.setText("--");
        cardStats.addView(statsTv);
        
        root.addView(cardStats);

        // Carte Guide
        LinearLayout cardGuide = buildCard(requireContext());
        cardGuide.addView(buildSectionLabel("Légende"));
        cardGuide.addView(buildSpacer(16));
        
        cardGuide.addView(buildLegendItem("SAUT", "Mouvement brusque"));
        cardGuide.addView(buildLegendItem("MARCHE", "Mouvement rythmique"));
        cardGuide.addView(buildLegendItem("PLAT", "Position horizontale"));
        cardGuide.addView(buildLegendItem("POSTURE", "Position verticale"));

        root.addView(cardGuide);

        return scroll;
    }

    private View buildLegendItem(String title, String desc) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textLayout = new LinearLayout(requireContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTextColor(TEXT_TITLE);
        t.setTextSize(14f);
        t.setTypeface(null, Typeface.BOLD);

        TextView d = new TextView(requireContext());
        d.setText(desc);
        d.setTextColor(TEXT_MUTED);
        d.setTextSize(12f);

        textLayout.addView(t);
        textLayout.addView(d);
        row.addView(textLayout);
        return row;
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
        if (accelSensor != null)
            sensorMgr.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        else activityTv.setText("INDISPONIBLE");
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        float rx = ev.values[0], ry = ev.values[1], rz = ev.values[2];

        grav[0] = ALPHA*grav[0] + (1-ALPHA)*rx;
        grav[1] = ALPHA*grav[1] + (1-ALPHA)*ry;
        grav[2] = ALPHA*grav[2] + (1-ALPHA)*rz;

        float linX = rx-grav[0], linY = ry-grav[1], linZ = rz-grav[2];
        float intensity = (float)Math.sqrt(linX*linX+linY*linY+linZ*linZ);

        if (win.size() >= WINDOW) win.pollFirst();
        win.addLast(intensity);

        if (win.size() < WINDOW) return;

        float sum=0, peak=0;
        for (float v : win) { sum+=v; if(v>peak) peak=v; }
        float avg = sum/win.size();
        float var=0;
        for (float v : win) var += (v-avg)*(v-avg);
        float std = (float)Math.sqrt(var/win.size());

        statsTv.setText(String.format(Locale.ROOT,
                "Intensité : %.2f m/s²\nMoyenne : %.2f | Max : %.2f | σ : %.2f",
                intensity, avg, peak, std));

        String label; int color;
        if (peak > 10f) {
            label="SAUT !"; color=Color.parseColor("#EF4444");
        } else if (std > 1.2f) {
            label="MARCHE"; color=Color.parseColor("#10B981");
        } else if (Math.abs(rz) > 8f) {
            label="TÉLÉPHONE À PLAT"; color=Color.parseColor("#3B82F6");
        } else if (Math.abs(ry)>7f || Math.abs(rx)>7f) {
            label="POSTURE ACTIVE"; color=Color.parseColor("#F59E0B");
        } else {
            label="STABLE"; color=Color.parseColor("#3B82F6");
        }

        activityTv.setText(label);
        activityTv.setTextColor(color);
        
        activityBadge.setText("SIGNAL DÉTECTÉ");
        activityBadge.setTextColor(color);
        activityBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.card_bg));
    }

    @Override public void onAccuracyChanged(Sensor s, int a) { }
}