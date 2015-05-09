package com.manvir.SnapColors;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class DonateActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donate);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	public static class PlaceholderFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_donate, container, false);
			final String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=Z6KRUW9W77J2W&lc=CA&item_name=SnapColors&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";
			final Button btnViaGooglePlay = (Button)rootView.findViewById(R.id.btnViaGooglePlay);
			final Button btnViaPayPal = (Button)rootView.findViewById(R.id.btnViaPayPal);
			
			btnViaPayPal.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				}
			});
			
			btnViaGooglePlay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.manvir.programming4lifedonate"));
					startActivity(i);
				}
			});
			
			return rootView;
		}
	}
}
