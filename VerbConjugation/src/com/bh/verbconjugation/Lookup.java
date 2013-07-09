package com.bh.verbconjugation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

public class Lookup extends FragmentActivity implements TabHost.OnTabChangeListener {
	
	private EditText editText;
	private Button button;
	public ArrayList<String> presentTense,pastTense,perfectTense,subject;
	private boolean flag = false;
	
	/*Author: Hari - Begin */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lookup);
		
		/*Author: Andy - Begin*/
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
		}
		/*Author: Andy - End*/
		
		presentTense = new ArrayList<String>(6);	//Array list to hold the present tense
		pastTense = new ArrayList<String>(6);		//Array list to hold the past tense
		perfectTense = new ArrayList<String>(6);	//Array list to hold the perfect tense
		subject = new ArrayList<String>(6);			//Array list to hold subject forms
		subject.add("ich");							//I
		subject.add("du");							//you
		subject.add("er/sie/es");					//He/she/it
		subject.add("wir");							//We
		subject.add("ihr");							//You (plural)
		subject.add("sie/Sie");						//they/You(respect)

		editText = (EditText) findViewById(R.id.editText1);
		button = (Button) findViewById(R.id.button1);
		TextView tv = (TextView)findViewById(R.id.textView1);
		tv.setText("");
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
					AlertDialog.Builder diag = new AlertDialog.Builder(Lookup.this);
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
	}
	
	public void setTextView(TabInfo newTab) {
		TextView tv = (TextView)findViewById(R.id.textView1);
		if(flag==false) {
			tv.setText("");
			tv.setText("Bitte warten Sie");
		} else {
			tv.setText("");
			if(newTab.tag=="Tab1") {
				for(int i=0;i<presentTense.size();i++)
					tv.append(subject.get(i)+" "+presentTense.get(i)+"\n\n");
			} else if(newTab.tag=="Tab2") {
				for(int i=0;i<pastTense.size();i++)
					tv.append(subject.get(i)+" "+pastTense.get(i)+"\n\n");
			} else if(newTab.tag=="Tab3") {		
				for(int i=0;i<perfectTense.size();i++)
					tv.append(subject.get(i)+" "+perfectTense.get(i)+"\n\n");
			}
		}
	}
	
	private class ParseAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) 
		{
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

		protected void onPostExecute(String result) 
		{
			try {
				Document doc = Jsoup.parse(result);
				Elements conjs = doc.select(".conjliststyle li");
				for(int i=0;i<6;i++) presentTense.add(i,conjs.get(i).text()); 		
				for(int i=6,j=0;i<12;i++,j++) pastTense.add(j,conjs.get(i).text()); 	
				for(int i=12,j=0;i<18;i++,j++) perfectTense.add(j,conjs.get(i).text());	
				flag=true;
				TextView tv = (TextView)findViewById(R.id.textView1);
				tv.setText("");
				for(int i=0;i<presentTense.size();i++)
					tv.append(subject.get(i)+" "+presentTense.get(i)+"\n\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*Author: Hari - End */
	/*Author: Andy - Begin*/
	private TabHost mTabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, Lookup.TabInfo>();
	private TabInfo mLastTab = null;
	
	private class TabInfo {
		private String tag;
		private Class<?> clss;
		private Bundle args;
		private Fragment fragment;
		TabInfo(String tag, Class<?> clazz, Bundle args) {
			this.tag = tag;
			this.clss = clazz;
			this.args = args;
		}
	}
	
	class TabFactory implements TabContentFactory {
		private final Context mContext;
		public TabFactory(Context context) {
			mContext = context;
		}
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}
	
	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabInfo tabInfo = null;									//my modifications on setIndicator below
		Lookup.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Präsens"), ( tabInfo = new TabInfo("Tab1", Tab1.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		Lookup.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Präteritum"), ( tabInfo = new TabInfo("Tab2", Tab2.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		Lookup.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab3").setIndicator("Perfekt"), ( tabInfo = new TabInfo("Tab3", Tab3.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		this.onTabChanged("Tab1");
		mTabHost.setOnTabChangedListener(this);
	}
	
	private static void addTab(Lookup activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		tabSpec.setContent(activity.new TabFactory(activity));
		String tag = tabSpec.getTag();
		tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
		if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
			FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
			ft.detach(tabInfo.fragment);
			ft.commit();
			activity.getSupportFragmentManager().executePendingTransactions();
		}
		tabHost.addTab(tabSpec);
	}
	
	public void onTabChanged(String tag) {
		TabInfo newTab = this.mapTabInfo.get(tag);		
		if (mLastTab != newTab) {
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			if (mLastTab != null) {
				if (mLastTab.fragment != null) {
					ft.detach(mLastTab.fragment);
				}
			}
			if (newTab != null) {
				if (newTab.fragment == null) {
					newTab.fragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
					ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
				} else {
					ft.attach(newTab.fragment);
				}
			}
			mLastTab = newTab;
			ft.commit();
			this.getSupportFragmentManager().executePendingTransactions();
			
			this.setTextView(newTab);//my addition
		}
	}
	/*Author: Andy - End*/ 
}
