package ensa.ma.sensors.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/** Vue personnalisée : courbe temps réel, thème sombre, dégradé vert néon. */
public class LiveGraphView extends View {

    private final List<Float> dataPoints = new ArrayList<>();
    private static final int MAX_SAMPLES = 80;

    private final Paint gridPaint  = new Paint();
    private final Paint curvePaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint bgPaint    = new Paint();
    private final Paint fillPaint  = new Paint();

    // Palette thème sombre
    // Remplacer les couleurs sombres par :
    private static final int COLOR_BG       = Color.parseColor("#FFFFFF");
    private static final int COLOR_GRID     = Color.parseColor("#E8ECF0");
    private static final int COLOR_CURVE    = Color.parseColor("#3B82F6");
    private static final int COLOR_LABEL    = Color.parseColor("#9AA5B4");
    private static final int COLOR_FILL_TOP = Color.parseColor("#403B82F6");
    private static final int COLOR_FILL_BOT = Color.parseColor("#003B82F6");
    public LiveGraphView(Context ctx) {
        super(ctx);
        initPaints();
    }

    private void initPaints() {
        bgPaint.setColor(COLOR_BG);
        bgPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(1.5f);

        curvePaint.setColor(COLOR_CURVE);
        curvePaint.setStrokeWidth(4f);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setAntiAlias(true);

        labelPaint.setColor(COLOR_LABEL);
        labelPaint.setTextSize(28f);
        labelPaint.setAntiAlias(true);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
    }

    /** Ajoute une mesure et force le redessin */
    public void pushValue(float val) {
        if (dataPoints.size() >= MAX_SAMPLES) {
            dataPoints.remove(0);
        }
        dataPoints.add(val);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // Fond arrondi sombre
        RectF bounds = new RectF(0, 0, w, h);
        canvas.drawRoundRect(bounds, 16f, 16f, bgPaint);

        float padL = 50f, padR = 20f, padT = 30f, padB = 40f;

        // Grille horizontale (4 lignes)
        for (int i = 0; i <= 4; i++) {
            float y = padT + i * ((h - padT - padB) / 4f);
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
        }
        canvas.drawLine(padL, h - padB, w - padR, h - padB, gridPaint);

        if (dataPoints.size() < 2) {
            labelPaint.setTextSize(32f);
            canvas.drawText("En attente...", padL + 10, h / 2f, labelPaint);
            return;
        }

        // Min / Max pour normaliser
        float minVal = Float.MAX_VALUE, maxVal = -Float.MAX_VALUE;
        for (float v : dataPoints) {
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        if (maxVal == minVal) maxVal = minVal + 1f;

        float rangeY = h - padT - padB;
        float rangeX = w - padL - padR;

        // Étiquettes min / max
        labelPaint.setTextSize(26f);
        canvas.drawText(String.format("%.1f", maxVal), 0, padT + 8, labelPaint);
        canvas.drawText(String.format("%.1f", minVal), 0, h - padB + 8, labelPaint);

        // Tracé de la courbe + remplissage dégradé
        Path curve = new Path();
        Path fill  = new Path();

        for (int i = 0; i < dataPoints.size(); i++) {
            float px   = padL + i * (rangeX / (MAX_SAMPLES - 1));
            float norm = (dataPoints.get(i) - minVal) / (maxVal - minVal);
            float py   = h - padB - norm * rangeY;

            if (i == 0) {
                curve.moveTo(px, py);
                fill.moveTo(px, h - padB);
                fill.lineTo(px, py);
            } else {
                curve.lineTo(px, py);
                fill.lineTo(px, py);
            }
        }

        // Fermeture du chemin de remplissage
        float lastX = padL + (dataPoints.size() - 1) * (rangeX / (MAX_SAMPLES - 1));
        fill.lineTo(lastX, h - padB);
        fill.close();

        // Dégradé vertical sous la courbe
        fillPaint.setShader(new LinearGradient(
                0, padT, 0, h - padB,
                COLOR_FILL_TOP, COLOR_FILL_BOT,
                Shader.TileMode.CLAMP));

        canvas.drawPath(fill, fillPaint);
        canvas.drawPath(curve, curvePaint);

        // Valeur courante en haut à droite
        float last = dataPoints.get(dataPoints.size() - 1);
        labelPaint.setColor(COLOR_CURVE);
        labelPaint.setTextSize(30f);
        canvas.drawText(String.format("%.2f", last), w - padR - 90, padT + 10, labelPaint);
        labelPaint.setColor(COLOR_LABEL);
    }
}