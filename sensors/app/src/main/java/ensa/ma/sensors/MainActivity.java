package ensa.ma.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import ensa.ma.sensors.fragments.*;
import ensa.ma.sensors.ui.sensors.ListSonsorsFragment;
import com.google.android.material.navigation.NavigationView;

/** Activité principale : gère le menu latéral et le chargement des fragments. */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Fragment affiché au lancement
        if (savedInstanceState == null) {
            switchFragment(new ListSonsorsFragment());
            navView.setCheckedItem(R.id.nav_catalog);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if      (id == R.id.nav_catalog)
            switchFragment(new ListSonsorsFragment());
        else if (id == R.id.nav_temperature)
            switchFragment(SingleValueGraphFragment.newInstance(
                    Sensor.TYPE_AMBIENT_TEMPERATURE, "Température", "SINGLE", "°C"));
        else if (id == R.id.nav_humidity)
            switchFragment(SingleValueGraphFragment.newInstance(
                    Sensor.TYPE_RELATIVE_HUMIDITY, "Humidité", "SINGLE", "%"));
        else if (id == R.id.nav_proximity)
            switchFragment(SingleValueGraphFragment.newInstance(
                    Sensor.TYPE_PROXIMITY, "Proximité", "SINGLE", "cm"));
        else if (id == R.id.nav_magnetic)
            switchFragment(SingleValueGraphFragment.newInstance(
                    Sensor.TYPE_MAGNETIC_FIELD, "Magnétique", "NORM3D", "µT"));
        else if (id == R.id.nav_accelerometer)
            switchFragment(TriAxisFragment.newInstance(
                    Sensor.TYPE_ACCELEROMETER, "Accéléromètre", "m/s²"));
        else if (id == R.id.nav_gravity)
            switchFragment(TriAxisFragment.newInstance(
                    Sensor.TYPE_GRAVITY, "Gravité", "m/s²"));
        else if (id == R.id.nav_gyroscope)
            switchFragment(TriAxisFragment.newInstance(
                    Sensor.TYPE_GYROSCOPE, "Gyroscope", "rad/s"));
        else if (id == R.id.nav_steps)
            switchFragment(new PedometerFragment());
        else if (id == R.id.nav_compass)
            switchFragment(new DigitalCompassFragment());
        else if (id == R.id.nav_activity)
            switchFragment(new ActivityDetectorFragment());

        drawerLayout.closeDrawers();
        return true;
    }

    /** Remplace le fragment courant dans le conteneur principal */
    private void switchFragment(Fragment target) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, target)
                .commit();
    }
}
