package com.mr.telephonymanagersimple;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NetworkListener.NetworkStateChangedListener {
	TelephonyManager telephonyManager;
	private int MY_PERMISSION_REQUEST_ACCESS = 101;
	View rootView;
	ListView cellInfoListView;
	TextView signalStrengthTextView;

	List<CellInfo> cellInfoList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Prepare the layout variables
		setContentView(R.layout.activity_main);
		rootView = findViewById(R.id.rootView);
		cellInfoListView = findViewById(R.id.cellInfoListView);
		signalStrengthTextView = findViewById(R.id.signalStrengthTextView);

		// Check the required permissions
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// Request the missing permissions
			requestPermission();
			return;
		}
		// Initialize TelephonyManager when permissions are granted
		initTelephonyManager();

	}
	protected void requestPermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSION_REQUEST_ACCESS);
	}
	@SuppressLint("MissingPermission")
	private void initTelephonyManager() {
		// Get the TelephonyManager instance
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// Get the NetworkListener instance.
		// Network listener is used to receive updates from the telephonyManager
		NetworkListener networkListener = NetworkListener.getInstance(this);
		// Specify the recipient of the updates from NetworkListener.
		// In this case MainActivity is the listener of the updates
		networkListener.addListener(this);

		// Attach the networkListener to the telephonyManager.
		// The flags PhoneStateListener.LISTEN_CELL_INFO and PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
		// indicates that the listener will only receive updates about changes in cell info and signal strengths
		telephonyManager.listen(networkListener,
				PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		//Display the current cellInfo from the telephony manager in the cellInfoList View.
	    cellInfoList = telephonyManager.getAllCellInfo();
		cellInfoListView.setAdapter(new ArrayAdapter<CellInfo>(this,android.R.layout.simple_list_item_1,android.R.id.text1,cellInfoList));
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == MY_PERMISSION_REQUEST_ACCESS){
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 1
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission was granted! Initialize TelephonyManager
				initTelephonyManager();
			} else {
				// permission denied
				Snackbar.make(rootView,"Permission is required to continue",Snackbar.LENGTH_LONG)
						.setAction("RETRY", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Retry permission request
								requestPermission();
							}
						}).show();
			}
			return;
		}
	}

	@Override
	public void cellInfoChangedHandler(List<CellInfo> cellInfo) {
		// Method called when a CellInfo update is received by the NetworkListener
		Snackbar.make(rootView,"Cell info changed!",Snackbar.LENGTH_SHORT).show();
		//Update the displayed list
		cellInfoList = cellInfo;
		((ArrayAdapter)cellInfoListView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void signalStrengthsChangedHandler(SignalStrength signalStrength) {
		// Method called when a signal strength update is received by the NetworkListener
		Snackbar.make(rootView,"Signal strength changed!",Snackbar.LENGTH_SHORT).show();
		signalStrengthTextView.setText(signalStrength.toString());
	}
}
