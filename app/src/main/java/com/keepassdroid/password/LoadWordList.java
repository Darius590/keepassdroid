/*
 * Copyright 2009-2016 Brian Pellin.
 *
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.password;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.android.keepass.R;
import com.keepassdroid.Database;
import com.keepassdroid.app.App;
import com.keepassdroid.database.edit.OnFinish;
import com.keepassdroid.database.edit.RunnableOnFinish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class LoadWordList extends RunnableOnFinish {
    private Context mCtx;

    public LoadWordList(Context ctx, OnFinish finish) {
        super(finish);

        mCtx = ctx;

    }

    @Override
    public void run() {
        //load the json dictionary
        String json = null;
        try {
            InputStream is = mCtx.getAssets().open("dictionary/words_dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            JSONObject wordsObject = new JSONObject(json);
            JSONArray wordsArray = wordsObject.names();
            JSONArray filtered = new JSONArray();

            for (int i = 0; i < wordsArray.length(); ++i) {
                String id = wordsArray.getString(i);

                // dynamic filtering - config option?
                if (id.length() < 4) {
                    continue;
                }

                if (id.length() > 8) {
                    continue;
                }

                filtered.put(wordsArray.get(i));

            }

            App.setWords(filtered);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        finish(true);
    }

    public static final class AfterLoad extends OnFinish {

        private Database db;

        public AfterLoad(
                Handler handler) {
            super(handler);
        }
    }

}
