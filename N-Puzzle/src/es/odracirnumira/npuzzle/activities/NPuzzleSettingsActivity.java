package es.odracirnumira.npuzzle.activities;

import es.odracirnumira.npuzzle.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * Activity to edit the application's settings.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleSettingsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.npuzzle_settings_activity);
	}
}
