package com.bh.ikonjugate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	private ArrayList<String> subject, presentTense, pastTense, perfectTense;
	private EditText editText;
	private Button button;
	private boolean parsed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Generated code
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		//My additions
		presentTense = new ArrayList<String>();
		pastTense = new ArrayList<String>();
		perfectTense = new ArrayList<String>();
		subject = new ArrayList<String>(Arrays.asList("ich","du","er/sie/es","wir","ihr","sie/Sie"));
		parsed = false;
		editText = (EditText) findViewById(R.id.editText1);
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = editText.getText().toString();
				if(!url.isEmpty()) {
					url = "http://m.glot.com/mconjugate.php?word="+editText.getText().toString()+"&submit=Conjugate&srclang=de";
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
					ParseAsyncTask task = new ParseAsyncTask();
					task.execute(url);
				}
				else {
					AlertDialog.Builder diag = new AlertDialog.Builder(MainActivity.this);
					diag.setTitle("Eintrag nicht da");
					diag.setMessage("Ich brauche einer gültiger Eintrag").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					AlertDialog d = diag.create();
					d.show();
				}
			}
		});
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				if(parsed) setTheTextView(position);
			}
		});
	}
	//Generated
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	//Generated
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
		if(parsed) setTheTextView(tab.getPosition());
	}
	//Generated
	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	//Generated
	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	//Generated
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public Fragment getItem(int position) {
			Fragment fragment = new DummySectionFragment();
			return fragment;
		}

		public int getCount() {
			return 3;
		}

		public CharSequence getPageTitle(int position) {

			switch (position) {
			case 0:
				return getString(R.string.title_section1);
			case 1:
				return getString(R.string.title_section2);
			case 2:
				return getString(R.string.title_section3);
			}
			return null;
		}
	}

	//My addition
	public void setTheTextView(int id) {
		DummySectionFragment dummy = (DummySectionFragment) mViewPager.getAdapter().instantiateItem(mViewPager, id);
		if(id==0) dummy.setTheText(subject, presentTense);
		else if(id==1) dummy.setTheText(subject, pastTense); 
		else dummy.setTheText(subject, perfectTense);
	}

	//My addition
	private class ParseAsyncTask extends AsyncTask<String, Integer, String> {
		ProgressDialog progressDialog;

		@Override
		protected String doInBackground(String... params) {
			String response = "";
			for (String url : params) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		protected void onPreExecute() {
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Bitte warten Sie");
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setCancelable(false);
		}

		@Override
		protected void onPostExecute(String result)	{
			if(progressDialog.isShowing()) progressDialog.dismiss();
			try {
				presentTense.clear();
				pastTense.clear();
				perfectTense.clear();
				Log.d("bh", "Res:"+result);
				Document doc = Jsoup.parse(result);
				Elements conjs = doc.select(".conjliststyle li");
				for(int i=0;i<6;i++) presentTense.add(conjs.get(i).text());
				for(int i=6;i<12;i++) pastTense.add(conjs.get(i).text());
				for(int i=12;i<18;i++) perfectTense.add(conjs.get(i).text());
				parsed = true;
				setTheTextView(mViewPager.getCurrentItem());
			} catch (Exception e) {
				AlertDialog.Builder diag = new AlertDialog.Builder(MainActivity.this);
				diag.setTitle("Ausfall");
				diag.setMessage("Ich konnte nicht konjugieren").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				AlertDialog d = diag.create();
				d.show();
			}	
		}
	}

	public static class DummySectionFragment extends Fragment {
		TextView tView;
		View rView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			rView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
			tView = (TextView)rView.findViewById(R.id.section_label);
			return rView;
		}

		public void setTheText(ArrayList<String> a, ArrayList<String> b) {
			Log.d("bh",a.get(0)+" "+b.get(0));
			if(tView != null) {
				tView.setText("");
				for(int i=0;i<a.size();i++) tView.append(a.get(i)+" "+b.get(i)+"\n\n");
			}
		}
	}
}