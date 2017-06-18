package com.example.android.projectseven_booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by StanleyPC on 17. 6. 2017.
 */

public class Books implements Parcelable {
    private String mBookTitle;
    private ArrayList<String> mBookAuthors;

    public static final Creator<Books> CREATOR = new Creator<Books>() {
        @Override
        public Books createFromParcel(Parcel in) {
            return new Books(in);
        }

        @Override
        public Books[] newArray(int size) {
            return new Books[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(mBookTitle);
        out.writeStringList(mBookAuthors);

    }

    @Override
    public int describeContents(){
        return 0;
    }

    private Books(Parcel in){
        mBookTitle = in.readString();
        in.readStringList(mBookAuthors);
    }

    public Books(JSONObject object){
        try {
            JSONObject volumeInfo = object.getJSONObject("volumeInfo");
            this.mBookTitle = volumeInfo.getString("title");
            this.mBookAuthors = new ArrayList<>();

            JSONArray jsonArrayAuthors = volumeInfo.getJSONArray("authors");
            if (jsonArrayAuthors != null){
                for(int i=0; i<jsonArrayAuthors.length(); i++){
                    mBookAuthors.add(jsonArrayAuthors.get(i).toString());
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    public String getBookTitle() { return mBookTitle;}
    public String getBookAuthors() {
        if (mBookAuthors == null || mBookAuthors.size() == 0) return "";
        return TextUtils.join(", ", mBookAuthors);
    }

    public static ArrayList<Books> fromJson(JSONArray jsonObjects){
        ArrayList<Books> users = new ArrayList<>();
        for(int i = 0; i < jsonObjects.length(); i++){
            try{
                users.add(new Books(jsonObjects.getJSONObject(i)));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return users;
    }
}