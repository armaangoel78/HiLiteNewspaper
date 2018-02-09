package com.example.armaangoel.hilitenewspaper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView rss;
    ArrayList<String> titles;
    ArrayList<String> links;

    public class MyWebViewClient extends WebViewClient
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        public MyWebViewClient()
        {
            progressDialog.setMessage("Loading Story");
            progressDialog.show();
            // do nothing
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            progressDialog.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainMenu();

        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView web = (WebView)findViewById(R.id.web1);
        web.setWebViewClient(new MyWebViewClient());
        web.loadUrl("https://hilite.org/");*/


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            mainMenu();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void mainMenu() {
        setContentView(R.layout.feed_view);

        rss = (ListView) findViewById(R.id.rss);

        titles = new ArrayList<String>();
        links = new ArrayList<String>();

        rss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Uri uri = Uri.parse(links.get(position));

                setContentView(R.layout.activity_main);

                WebView web = (WebView)findViewById(R.id.web1);
                web.setWebViewClient(new MyWebViewClient());
                web.loadUrl(links.get(position));
            }
        });

        new ProcessInBackground().execute();
    }


    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Integer, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading HiLite.org");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try {

                URL url = new URL("https://hilite.org/feed/");

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(getInputStream(url), "UTF_8");

                boolean insideItem = false;

                int eventType = parser.getEventType();

                while (eventType!=XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (parser.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                titles.add(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                links.add(parser.nextText());
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }

                    System.out.print(eventType);
                    eventType = parser.next();
                }


            } catch (MalformedURLException e) {
                exception = e;
            } catch (XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titles);

            rss.setAdapter(adapter);

        }


    }




}
