package com.mehul.newsreader;
/*
    Title: News Reader
    Author: Mehul Patel
    Date: 04/19/2020
    Description: This app will list the top stories from  Hacker News using the Hacker News API,
                    view those stores in app through a web view, and allow to view them offline using
                    SQLite to store the articles
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //array lists for titles and content to hoold the titles and content so we can add them to the listview
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();

    ArrayAdapter arrayAdapter;

    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create listview variable to link to the listview created
        ListView listView = (ListView) findViewById(R.id.listView);

        //setup array adapter for this activity using a simple list item format using array list notes as the array for the list
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);

        //display the contents in the titles arraylist via array adapter in the listview
        listView.setAdapter(arrayAdapter);

        //when user taps an item in the listview, screen jumps to the articleactivity to display the article
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //when item is clicked run the below method, the onItemClick method
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //create intent variable to jump to the ArticleActivity class
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);

                //let the ArticleActivity know which item was tapped/selected giving it a variable
                //name content and the value of int i which is passed to this method stating which row was tapped
                intent.putExtra("content", content.get(i));

                startActivity(intent);

            }
        });

        articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);

        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY," +
                        "articleID INTEGER," +
                        "title VARCHAR," +
                        "content VARCHAR)");

        updateListView();

        //create download task
        DownloadTask task = new DownloadTask();

        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    //method to update the listview
    public void updateListView(){

        //retrieve data from database using cursor loop through a particular query and do something with it.
        //in this case retrieve all from the articles table
        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

        //create integer variable to hold the column indexes for the content and title
        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        //check to see if we have an initial value/if db has anything and if so clear the titles and contents array list
        if (c.moveToFirst()){
            titles.clear();
            content.clear();

            //set titles as the title from the table via titleIndex
            //set content as the content from the table via contentIndex
            //keep doing that as long as or while we can go to the next item
            do{
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));
            }while(c.moveToNext());

            //update the array adapter to say the data set has changed
            arrayAdapter.notifyDataSetChanged();
        }

    }

    //set up download task class
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            //get the content of the top stories api url
            //create string to hold the results from the top stories json api
            String result = "";

            //create a url variable in order to use it as a URL
            URL url;

            //create HttpURLConnection variable to fetch the contents of the website
            HttpURLConnection urlConnection = null;

            //convert url that is passed to the class as string
            try {
                //assigning url variable the url that is passed down to this doInBackground method as strings
                url = new URL(strings[0]);

                //open a url connection from the url
                urlConnection = (HttpURLConnection) url.openConnection();

                //create InputStream variable hold the input of data that comes in
                InputStream inputStream = urlConnection.getInputStream();

                //create InputStreamReader variable to read the data that comes in in the InputStream to read the contents of the URL
                InputStreamReader reader = new InputStreamReader(inputStream);

                //create variable to keep track of location throughout the HTML in order to read the data from the InputStreamReader one character at a time
                int data = reader.read();

                //loop to read the data. Once it reads through all the data will equal -1 so in this case the loop will end once data is read to -1.
                while(data != -1){
                    //create variable to obtain current character that is being downloaded
                    char current = (char) data;

                    //add current to the result as it is being read.
                    result += current;

                    //update data variable to read the next character
                    data = reader.read();
                }

                //log url content result
                Log.i("URLContent", result);

                //convert the id numbers/content in the results to extract into individual ids
                //create json array to hold those results/id numbers
                JSONArray jsonArray = new JSONArray(result);

                //we want to display only 20 items/id numbers
                int numberOfItems = 20;

                //check if JSON array has less than 20 items/id numbers then set that number to what will be displayed
                if(jsonArray.length() < 20){
                    numberOfItems = jsonArray.length();
                }

                //clear sql table
                articlesDB.execSQL("DELETE FROM articles");

                //loop through each item/id numbers in the array for number of items/id numbers there are max being 20 items/id numbers
                for(int i = 0; i < numberOfItems; i++){
                    //log to show each item in the JSON array that we got from the results as an item in an array
                    //Log.i("JSONItem", jsonArray.getString(i));

                    //load content/article url using the item api url from the id numbers in the JSON array
                    String articleId = jsonArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    reader = new InputStreamReader(inputStream);
                    data = reader.read();

                    //create string to hold content of the json API for the item
                    String articleInfo = "";

                    while(data != -1){
                        char current = (char) data;
                        articleInfo += current;
                        data = reader.read();
                    }

                    //create Json object to extract specific info from the articleInfo
                    JSONObject jsonObject = new JSONObject(articleInfo);

                    //check if the title and url in the article json api exists and show the ones that do exist
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                        //create string to hold the name of the title from the jsonObject
                        String articleTitle = jsonObject.getString("title");
                        String articleURL = jsonObject.getString("url");

                        //Log.i("Info", articleTitle + articleURL);

                        url = new URL(articleURL);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        inputStream = urlConnection.getInputStream();
                        reader = new InputStreamReader(inputStream);
                        data = reader.read();

                        //create string to hold content of the json API for the item
                        String articleContent = "";

                        while(data != -1){
                            char current = (char) data;
                            articleInfo += current;
                            data = reader.read();
                        }

                        //Log.i("ArticleContent", articleContent);

                        //add the articles to the sql table
                        //create string with the sql queries to add variables
                        String sql =  "INSERT INTO articles (articleID, title, content) VALUES(?, ?, ?)";

                        //use SQLiteStatement class to setup a statement in the articlesDB using the sql string with the queries
                        //represents a statement that can be executed against a database
                        //basically takes the sql string containing the query and executes it against the database using and SQLiteStatement
                        SQLiteStatement statement = articlesDB.compileStatement(sql);

                        //bind string articleID to the statement for the first index(?) of VALUES
                        statement.bindString(1, articleId);
                        //bind string articleTitle to the statement for the second index(?) of VALUES
                        statement.bindString(2, articleTitle);
                        //bind string articleContent to the statement for the third index(?) of VALUES
                        statement.bindString(3, articleContent);

                        //commit/apply/execute the statement to the database
                        statement.execute();

                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        //onPostExecute method used to run when the process in download task has completed
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }

}
