package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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

import ensa.ma.sensors.R;
import ensa.ma.sensors.views.LiveGraphView;

public class SingleValueGraphFragment extends Fragment implements SensorEventListener {

    private static final String KEY_TYPE  = "sv_type";
    private static final String KEY_TITLE = "sv_title";
    private static final String KEY_MODE  = "sv_mode";
    private static final String KEY_UNIT  = "sv_unit";

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");

    private SensorManager sensorMgr;
    private Sensor targetSensor;
    private TextView valueTv;
    private LiveGraphView graphView;

    private int    sensorType;
    private String screenTitle;
    private String mode;
    private String unit;

    public static SingleValueGraphFragment newInstance(int type, String title, String mode, String unit) {
        SingleValueGraphFragment frag = new SingleValueGraphFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE,     type);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MODE,  mode);
        args.putString(KEY_UNIT,  unit);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {

        sensorType  = requireArguments().getInt(KEY_TYPE);
        screenTitle = requireArguments().getString(KEY_TITLE);
        mode        = requireArguments().getString(KEY_MODE);
        unit        = requireArguments().getString(KEY_UNIT);

        sensorMgr    = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        targetSensor = sensorMgr.getDefaultSensor(sensorType);

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        root.addView(buildPageHeader(screenTitle, "Suivi en temps réel"));
        root.addView(buildSpacer(32));

        // Carte Valeur Actuelle
        LinearLayout cardValue = buildCard(requireContext());
        cardValue.addView(buildSectionLabel("Mesure actuelle"));
        cardValue.addView(buildSpacer(24));

        valueTv = new TextView(requireContext());
        valueTv.setText("-- " + unit);
        valueTv.setTextColor(ACCENT);
        valueTv.setTextSize(48f);
        valueTv.setTypeface(null, Typeface.BOLD);
        valueTv.setGravity(android.view.Gravity.CENTER);
        cardValue.addView(valueTv);
        
        root.addView(cardValue);

        // Carte Graphique
        LinearLayout cardGraph = buildCard(requireContext());
        cardGraph.addView(buildSectionLabel("Historique"));
        cardGraph.addView(buildSpacer(24));

        graphView = new LiveGraphView(requireContext());
        LinearLayout.LayoutParams graphLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500);
        graphView.setLayoutParams(graphLp);
        cardGraph.addView(graphView);

        root.addView(cardGraph);

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

    private View buildSectionLabel(String text) {
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
        if (targetSensor != null)
            sensorMgr.registerListener(this, targetSensor, SensorManager.SENSOR_DELAY_UI);
        else
            valueTv.setText("Capteur non détecté");
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        float val = ev.values[0];
        if ("NORM3D".equals(mode)) {
            val = (float) Math.sqrt(ev.values[0]*ev.values[0] + ev.values[1]*ev.values[1] + ev.values[2]*ev.values[2]);
        }
        valueTv.setText(String.format("%.2f %s", val, unit));
        graphView.pushValue(val);
    }

    @Override
    public void onAccuracyChanged(Sensor s, int acc) { }
}