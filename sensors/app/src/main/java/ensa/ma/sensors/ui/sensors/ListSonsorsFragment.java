package ensa.ma.sensors.ui.sensors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
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

import java.util.List;
import ensa.ma.sensors.R;
import ensa.ma.sensors.beans.SensorItem;

public class ListSonsorsFragment extends Fragment {

    private static final int BG_PAGE    = Color.parseColor("#F4F6F9");
    private static final int BG_CARD    = Color.WHITE;
    private static final int TEXT_TITLE = Color.parseColor("#1A1F36");
    private static final int TEXT_BODY  = Color.parseColor("#4A5568");
    private static final int TEXT_MUTED = Color.parseColor("#9AA5B4");
    private static final int ACCENT     = Color.parseColor("#3B82F6");
    private static final int DIVIDER    = Color.parseColor("#E8ECF0");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(BG_PAGE);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 48, 40, 48);
        scroll.addView(root);

        // En-tête page
        root.addView(buildPageHeader("Capteurs disponibles",
                "Liste des capteurs détectés sur cet appareil"));
        root.addView(buildSpacer(24));

        SensorManager mgr = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);

        // Badge nombre total
        root.addView(buildBadge(sensors.size() + " capteurs détectés"));
        root.addView(buildSpacer(24));

        for (Sensor s : sensors) {
            root.addView(buildSensorCard(s));
        }

        return scroll;
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

    private View buildBadge(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(ACCENT);
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(24, 12, 24, 12);
        
        // Utilisation du background avec ombre simulée
        tv.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.card_bg));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        
        return tv;
    }

    private View buildSensorCard(Sensor s) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.card_bg));
        card.setPadding(32, 28, 32, 28);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);

        // Nom du capteur
        TextView name = new TextView(requireContext());
        name.setText(s.getName());
        name.setTextColor(TEXT_TITLE);
        name.setTextSize(16f);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 0, 0, 16);

        // Ligne séparatrice fine
        View line = new View(requireContext());
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 2);
        lineLp.setMargins(0, 0, 0, 16);
        line.setLayoutParams(lineLp);
        line.setBackgroundColor(DIVIDER);

        // Détails en grille
        LinearLayout grid = buildInfoGrid(s);

        card.addView(name);
        card.addView(line);
        card.addView(grid);

        return card;
    }

    private LinearLayout buildInfoGrid(Sensor s) {
        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.VERTICAL);

        String minDelay = s.getMinDelay() == 0 ? "N/A" : s.getMinDelay() + " µs";

        String[][] rows = {
            {"Type",         String.valueOf(s.getType())},
            {"Résolution",   String.valueOf(s.getResolution())},
            {"Énergie",      s.getPower() + " mA"},
            {"Portée max",   String.valueOf(s.getMaximumRange())},
            {"Délai min",    minDelay},
            {"Fabricant",    s.getVendor()}
        };

        for (String[] row : rows) {
            LinearLayout rowView = new LinearLayout(requireContext());
            rowView.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 0, 0, 8);
            rowView.setLayoutParams(rowLp);

            TextView label = new TextView(requireContext());
            label.setText(row[0]);
            label.setTextColor(TEXT_MUTED);
            label.setTextSize(12f);
            label.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView value = new TextView(requireContext());
            value.setText(row[1]);
            value.setTextColor(TEXT_BODY);
            value.setTextSize(12f);
            value.setTypeface(null, Typeface.BOLD);
            value.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));

            rowView.addView(label);
            rowView.addView(value);
            grid.addView(rowView);
        }

        return grid;
    }

    private View buildSpacer(int dp) {
        View v = new View(requireContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp));
        return v;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(SensorItem item);
    }
}
