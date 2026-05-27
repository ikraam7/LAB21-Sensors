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

public class TriAxisFragment extends Fragment implements SensorEventListener {

    private static final String KEY_TYPE  = "ta_type";
    private static final String KEY_TITLE = "ta_title";
    private static final String KEY_UNIT  = "ta_unit";

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");
    private static final int COLOR_X    = Color.parseColor("#EF4444");
    private static final int COLOR_Y    = Color.parseColor("#10B981");
    private static final int COLOR_Z    = Color.parseColor("#F59E0B");

    private SensorManager sensorMgr;
    private Sensor targetSensor;

    private TextView tvX, tvY, tvZ, tvNorm;
    private LiveGraphView graphView;

    private int    sensorType;
    private String screenTitle;
    private String unit;

    public static TriAxisFragment newInstance(int type, String title, String unit) {
        TriAxisFragment frag = new TriAxisFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE,     type);
        args.putString(KEY_TITLE, title);
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
        unit        = requireArguments().getString(KEY_UNIT, "");

        sensorMgr    = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);
        targetSensor = sensorMgr.getDefaultSensor(sensorType);

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        root.addView(buildPageHeader(screenTitle, "Unité : " + unit));
        root.addView(buildSpacer(32));

        // Carte des valeurs
        LinearLayout cardValues = buildCard(requireContext());
        cardValues.addView(buildSectionLabel("Mesures en temps réel"));
        cardValues.addView(buildSpacer(24));

        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.VERTICAL);

        tvX = new TextView(requireContext());
        tvY = new TextView(requireContext());
        tvZ = new TextView(requireContext());
        tvNorm = new TextView(requireContext());

        grid.addView(buildValueRow("Axe X", tvX, COLOR_X));
        grid.addView(buildValueRow("Axe Y", tvY, COLOR_Y));
        grid.addView(buildValueRow("Axe Z", tvZ, COLOR_Z));
        grid.addView(buildValueRow("Norme", tvNorm, ACCENT));

        cardValues.addView(grid);
        root.addView(cardValues);

        // Carte du graphique
        LinearLayout cardGraph = buildCard(requireContext());
        cardGraph.addView(buildSectionLabel("Graphique de la norme"));
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
        card.setPadding(32, 32, 32, 32);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        card.setLayoutParams(lp);
        return card;
    }

    private View buildValueRow(String label, TextView valueTv, int color) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView labelTv = new TextView(requireContext());
        labelTv.setText(label);
        labelTv.setTextColor(TEXT_BODY);
        labelTv.setTextSize(14f);
        labelTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        valueTv.setText("--");
        valueTv.setTextColor(color);
        valueTv.setTextSize(16f);
        valueTv.setTypeface(null, Typeface.BOLD);
        valueTv.setGravity(android.view.Gravity.END);
        valueTv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        row.addView(labelTv);
        row.addView(valueTv);
        return row;
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
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent ev) {
        float x = ev.values[0];
        float y = ev.values[1];
        float z = ev.values[2];
        float norm = (float) Math.sqrt(x*x + y*y + z*z);

        tvX.setText(String.format("%.2f %s", x, unit));
        tvY.setText(String.format("%.2f %s", y, unit));
        tvZ.setText(String.format("%.2f %s", z, unit));
        tvNorm.setText(String.format("%.2f %s", norm, unit));

        graphView.pushValue(norm);
    }

    @Override
    public void onAccuracyChanged(Sensor s, int acc) { }
}