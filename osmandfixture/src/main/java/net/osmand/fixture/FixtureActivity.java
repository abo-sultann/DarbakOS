package net.osmand.fixture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

/** CI-only external application fixture for package visibility and OsmAnd URI integration. */
public final class FixtureActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Intent intent = getIntent();
        Uri data = intent == null ? null : intent.getData();
        if (data != null && "osmand.api".equals(data.getScheme())
                && "get_info".equals(data.getHost())) {
            Intent result = new Intent();
            result.putExtra("destination_lat", 24.7136d);
            result.putExtra("destination_lon", 46.6753d);
            result.putExtra("eta", 1700003600000L);
            result.putExtra("time_left", 600);
            result.putExtra("time_distance_left", 4200);
            result.putExtra("current_turn_name", "Fixture turn");
            result.putExtra("current_turn_type", "left");
            result.putExtra("next_turn_distance", 350);
            setResult(RESULT_OK, result);
            finish();
            return;
        }
        TextView label = new TextView(this);
        label.setText("OsmAnd Fixture");
        setContentView(label);
    }
}
