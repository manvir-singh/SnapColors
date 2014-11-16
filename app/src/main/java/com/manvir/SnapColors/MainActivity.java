package com.manvir.SnapColors;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import com.manvir.SnapColors.ColorPickerDialog.OnColorSelectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class MainActivity extends Activity {
    static final String TAG = "SnapColors";
	static boolean DEBUG = true;
	static SharedPreferences prefs;
	static String fontsDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.snapchat.android/files";

    public static void sofReboot(){
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("pkill zygote");
            outputStream.flush();

        }catch(IOException e){

        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(resultCode != 0 && data != null){
				try {
					File ttfFile = new File(Uri.decode(data.getDataString()));
					FileUtils.copyFile(ttfFile, new File(fontsDir+"/"+ttfFile.getName()));
                    Toast.makeText(getActivity(), "Import successful.", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
                    Toast.makeText(getActivity(), "Import failed! Something went wrong =0", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

            //This is the debug msg's show it if the debug bool is true
            TextView debugModeTextView = (TextView)rootView.findViewById(R.id.debugModeTextView);
            if(DEBUG){
                debugModeTextView.setVisibility(View.VISIBLE);
            }else{
                debugModeTextView.setVisibility(View.GONE);
            }

			//Setup Stuff.
			prefs = getActivity().getSharedPreferences("settings", Context.MODE_WORLD_READABLE);
			try {
				String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
				if(!prefs.getString("lastVer", "0").equals(versionName)){
					final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
					alert.setTitle("SnapColors - Donate/Subscribe PlayStore/PayPal");
					alert.setMessage("Please consider donating to help support this tweak/utility. You can donate what ever amount you want. Your donation is appreciated. Thanks =)");
					alert.setNegativeButton("I want to Donate!", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							intent.setComponent(new ComponentName("com.manvir.SnapColors", "com.manvir.SnapColors.DonateActivity"));
							startActivity(intent);
						}
					});
					alert.setPositiveButton("Get this outta my face!", null);
					alert.show();
					prefs.edit().putString("lastVer", versionName).apply();
				}
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
			
			// Making the buttons and what not.
			final CheckBox autoRandomize = (CheckBox)rootView.findViewById(R.id.autoRandomize);
            final CheckBox showFeed = (CheckBox)rootView.findViewById(R.id.showFeed);
			final CheckBox settextColor = (CheckBox)rootView.findViewById(R.id.settextColor);
			final TextView textColorPrev = (TextView)rootView.findViewById(R.id.textColorPrev);
			final CheckBox setBGColor = (CheckBox)rootView.findViewById(R.id.setBGColor);
			final TextView textBGPrev = (TextView)rootView.findViewById(R.id.textBGPrev);
			final CheckBox setFont = (CheckBox)rootView.findViewById(R.id.setFont);
			final TextView fontPrev = (TextView)rootView.findViewById(R.id.fontPrev);
			final Button importFont = (Button)rootView.findViewById(R.id.importFont);
			final Button btnDonate = (Button)rootView.findViewById(R.id.btnDonate);
			final Button btnTwitter = (Button)rootView.findViewById(R.id.btnTwitter);
			final Button btnXdaThread = (Button)rootView.findViewById(R.id.btnXdaThread);
            final CheckBox newVerBox = (CheckBox)rootView.findViewById(R.id.newVerBox);
			
			//Work on fixing this
            showFeed.setVisibility(View.GONE);
			
			//Set the states of the views.
            if(prefs.getBoolean("checkForVer", true)){
                newVerBox.setChecked(true);
            }
			if(prefs.getInt("TextColor", Color.WHITE) != Color.WHITE){
				settextColor.setChecked(true);
				textColorPrev.setBackgroundColor(prefs.getInt("TextColor", Color.WHITE));
			}
			if(prefs.getInt("BGColor", -1728053248) != -1728053248){
				setBGColor.setChecked(true);
				textBGPrev.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
			}
			if(prefs.getBoolean("setFont", false)){
				setFont.setChecked(true);
				final String font = fontsDir+"/" + prefs.getString("Font", "0");
				Typeface face = Typeface.createFromFile(font);
				fontPrev.setTypeface(face);
			}
			if(prefs.getBoolean("autoRandomize", false)){
				autoRandomize.setChecked(true);
			}
			if(prefs.getBoolean("showFeedOnStart", false)){
				showFeed.setChecked(true);
			}
			
			// Listeners
			showFeed.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						prefs.edit().putBoolean("showFeedOnStart", true).apply();
					}else{
						prefs.edit().putBoolean("showFeedOnStart", false).apply();
					}
				}
			});
			btnXdaThread.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("http://forum.xda-developers.com/nexus-4/orig-development/add-colors-to-snapchat-captions-change-t2716416"));
					startActivity(i);
				}
			});
			btnTwitter.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("https://twitter.com/iphone4life4"));
					startActivity(i);
				}
			});
			importFont.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				    intent.setType("font/opentype");
				    startActivityForResult(intent, 0);
				}
			});
			autoRandomize.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(!settextColor.isChecked() && !setBGColor.isChecked()){
						if(isChecked){
							prefs.edit().putBoolean("autoRandomize", true).apply();
						}else {
							prefs.edit().putBoolean("autoRandomize", false).apply();
						}
					}else {
						autoRandomize.setChecked(false);
						prefs.edit().putBoolean("autoRandomize", false).apply();
						Toast.makeText(getActivity(), "You can't use this with a custom color already set. Disable them and then use this.", Toast.LENGTH_LONG).show();
					}
				}
			});
			setFont.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						try {
							prefs.edit().putBoolean("setFont", true).apply();
							Resources res = getActivity().getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
							copyAssets(res);
							
							final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
							
							File file[] = new File(fontsDir).listFiles();
                            for (File aFile : file) {
                                arrayAdapter.add(aFile.getName());
                            }
							
					    	AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
				            builderSingle.setIcon(R.drawable.ic_launcher);
				            builderSingle.setTitle("Select A Font:");
				            builderSingle.setNegativeButton("Cancel", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									prefs.edit().putBoolean("setFont", false).apply();
									setFont.setChecked(false);
								}
							});

				            builderSingle.setAdapter(arrayAdapter, new OnClickListener() {
					            @Override
					            public void onClick(DialogInterface dialog, int which) {
					            	String strName = arrayAdapter.getItem(which).replace(".TTF", ".ttf");
					            	Typeface face = Typeface.createFromFile(fontsDir+ "/" + strName);
					            	prefs.edit().putString("Font", strName).apply();
					            	prefs.edit().putBoolean("setFont", true).apply();
					            	fontPrev.setTypeface(face);
					            }
				            });
				            builderSingle.show();
						} catch (NameNotFoundException e) {
							AlertDialog.Builder al = new AlertDialog.Builder(getActivity());
							final TextView message = new TextView(getActivity());
							final SpannableString s = new SpannableString("You need to download fonts they are not included. Just download and install the apk.(Note no icon will be added) Fonts apk can be downloaded from this page: http://forum.xda-developers.com/devdb/project/?id=3684#downloads");
							Linkify.addLinks(s, Linkify.WEB_URLS);
							message.setText(s);
							message.setMovementMethod(LinkMovementMethod.getInstance());
							al.setTitle("SnapColors");
							al.setView(message);
							al.setNegativeButton("Close", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setFont.setChecked(false);
                                }
                            });
							al.setNeutralButton("Why", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String whyText = "The reason why fonts are not included with the tweak are simple.\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
									AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
									alertDialog.setTitle("SnapColors");
									alertDialog.setMessage(whyText);
                                    alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "Okay", new OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            setFont.setChecked(false);
                                        }
                                    });
									alertDialog.show();
                                    setFont.setChecked(false);
								}
							});
							al.show();
						}
					}else {
						prefs.edit().putBoolean("setFont", false).apply();
						fontPrev.setTypeface(Typeface.DEFAULT);
					}
				}
			});
			setBGColor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, new OnColorSelectedListener() {
							@Override
	            	        public void onColorSelected(int color) {
								prefs.edit().putInt("BGColor", color).apply();
								textBGPrev.setBackgroundColor(color);
	            	        }
	            	    });
                        colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putInt("BGColor", -1728053248).apply();
                                textBGPrev.setBackgroundColor(0);
                                setBGColor.setChecked(false);
                            }
                        });
                        colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putInt("BGColor", -1728053248).apply();
                                textBGPrev.setBackgroundColor(0);
                                setBGColor.setChecked(false);
                            }
                        });
	            		colorPickerDialog.setTitle("Background Color");
	            	    colorPickerDialog.show();
					}else {
						prefs.edit().putInt("BGColor", -1728053248).apply();
						textBGPrev.setBackgroundColor(0);
					}
				}
			});
			settextColor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, new OnColorSelectedListener() {
	            	        @Override
	            	        public void onColorSelected(int color) {
	            	        	prefs.edit().putInt("TextColor", color).apply();
	            	        	textColorPrev.setBackgroundColor(prefs.getInt("TextColor", Color.WHITE));
	            	        }
	            	    });
	            		colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new OnClickListener(){
	            			public void onClick(DialogInterface dialog, int which) {
	            				prefs.edit().putInt("TextColor", Color.WHITE).apply();
                                settextColor.setChecked(false);
	            			}
	            		});
	            		colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putInt("TextColor", Color.WHITE).apply();
                                settextColor.setChecked(false);
                            }
                        });
	            		colorPickerDialog.setTitle("Text Color");
	            	    colorPickerDialog.show();
					}else{
						prefs.edit().putInt("TextColor", Color.WHITE).apply();
						textColorPrev.setBackgroundColor(0);
					}
				}
			});
			btnDonate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().startActivity(new Intent(getActivity(), DonateActivity.class));
				}
			});
            newVerBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    prefs.edit().putBoolean("checkForVer", b).apply();
                }
            });
			return rootView;
		}
		public void copyAssets(Resources res){
			if(!new File(fontsDir).exists()){
                if (!new File(fontsDir).mkdirs())
                	throw new RuntimeException("SnapColors was unable to create fontsDir.");
			}
			AssetManager assetManager = res.getAssets();
		    String[] files = null;
		    try {
		        files = assetManager.list("");
		    } catch (IOException e) {
		        Log.e("tag", "Failed to get asset file list.", e);
		    }
		    for(String filename : files) {
		        try {
                  InputStream in;
                  OutputStream out;
		          in = assetManager.open(filename);
		          File outFile = new File(new File(fontsDir), filename);
		          out = new FileOutputStream(outFile);
		          copyFile(in, out);
		          in.close();
		          in = null;
		          out.flush();
		          out.close();
		          out = null;
		        } catch(IOException e) {
		            Log.e("SnapColors", "Failed to copy asset file: " + filename, e);
		        }       
		    }
		}
		
		private void copyFile(InputStream in, OutputStream out) throws IOException {
		    byte[] buffer = new byte[1024];
		    int read;
		    while((read = in.read(buffer)) != -1){
		      out.write(buffer, 0, read);
		    }
		}
		
		public void log(String text){
			if(DEBUG){
				Log.i("SnapColors: ", text);
			}
		}
		
	}
}