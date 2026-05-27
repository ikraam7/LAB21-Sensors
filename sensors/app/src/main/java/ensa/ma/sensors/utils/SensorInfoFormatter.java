package ensa.ma.sensors.utils;


import android.hardware.Sensor;

/** Formate les propriétés techniques d'un capteur en texte lisible. */
public class SensorInfoFormatter {

    public static String buildSensorCard(Sensor s) {
        String minDelay = s.getMinDelay() == 0
                ? "N/A" : s.getMinDelay() + " µs";

        return "● ID         : " + s.getId() + "\n"
                + "● Nom        : " + s.getName() + "\n"
                + "● Fabricant  : " + s.getVendor() + "\n"
                + "● Version    : " + s.getVersion() + "\n"
                + "● Type str.  : " + s.getStringType() + "\n"
                + "● Int Type   : " + s.getType() + "\n"
                + "● Résolution : " + s.getResolution() + "\n"
                + "● Énergie    : " + s.getPower() + " mA\n"
                + "● Portée max : " + s.getMaximumRange() + "\n"
                + "● Délai min  : " + minDelay + "\n";
    }
}
