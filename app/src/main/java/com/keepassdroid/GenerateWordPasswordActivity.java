/*
 * Copyright 2010 Tolga Onbay, Brian Pellin.
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
package com.keepassdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.keepass.KeePass;
import com.android.keepass.R;
import com.keepassdroid.app.App;
import com.keepassdroid.password.LoadWordList;
import com.keepassdroid.password.PasswordGenerator;

import org.json.JSONArray;

public class GenerateWordPasswordActivity extends LockCloseActivity {
    private static final int[] WORDS_BUTTON_IDS = new int [] {R.id.btn_words2, R.id.btn_words3, R.id.btn_words4};
    private static JSONArray words;

    public static void Launch(Activity act) {
        Intent i = new Intent(act, GenerateWordPasswordActivity.class);
        words = App.getWords();
        act.startActivityForResult(i, 0);
    }

    private OnClickListener wordsButtonListener = new OnClickListener() {
        public void onClick(View v) {
            Button button = (Button) v;

            EditText editText = (EditText) findViewById(R.id.words);
            editText.setText(button.getText());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_word_password);
        setResult(KeePass.EXIT_NORMAL);

        for (int id : WORDS_BUTTON_IDS) {
            Button button = (Button) findViewById(id);
            button.setOnClickListener(wordsButtonListener);
        }

        Button genPassButton = (Button) findViewById(R.id.generate_password_button);
        genPassButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                fillPassword();
            }
        });

        Button acceptButton = (Button) findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                EditText password = (EditText) findViewById(R.id.password);

                Intent intent = new Intent();
                intent.putExtra("com.keepassdroid.password.generated_password", password.getText().toString());

                setResult(EntryEditActivity.RESULT_OK_PASSWORD_GENERATOR, intent);

                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_CANCELED);

                finish();
            }
        });

        // Load word list if we don't have it due to in app setting change
        Handler handler = new Handler();
        if (this.words == null)
        {
            LoadWordList task = new LoadWordList(this, new LoadWordList.AfterLoad(handler));
            ProgressTask pt = new ProgressTask((Activity) this, task, R.string.loading_wordlist);
            pt.run();
        }

        // Pre-populate a password to possibly save the user a few clicks
        fillPassword();
    }

    private void fillPassword() {
        EditText txtPassword = (EditText) findViewById(R.id.password);
        txtPassword.setText(generatePassword());
    }

    private void fillEntropy(int entropy) {
        TextView txtEntropy = (TextView) findViewById((R.id.entropy_value));

        /*
            < 28 bits = Very Weak; might keep out family members
            28 – 35 bits = Weak; should keep out most people, often good for desktop login passwords
            36 – 59 bits = Reasonable; fairly secure passwords for network and company passwords
            60 – 127 bits = Strong; can be good for guarding financial information
            128+ bits = Very Strong; often overkill
        */
        if(entropy < 28)
        {
            txtEntropy.setText("VERY WEAK");
            txtEntropy.setTextColor(Color.RED);
        }
        else if(entropy < 36)
        {
            txtEntropy.setText("WEAK");
            txtEntropy.setTextColor(Color.RED);
        }
        else if(entropy < 60)
        {
            txtEntropy.setText("GOOD");
            txtEntropy.setTextColor(Color.YELLOW);
        }
        else if(entropy < 128)
        {
            txtEntropy.setText("STRONG");
            txtEntropy.setTextColor(Color.GREEN);
        }
        else
        {
            txtEntropy.setText("VERY STRONG");
            txtEntropy.setTextColor(Color.GREEN);
        }
        txtEntropy.append(String.format(" - %d", entropy));
    }

    public String generatePassword() {
        String password = "";

        try {
            int words = Integer.valueOf(((EditText) findViewById(R.id.words)).getText().toString());
            boolean camelCase = ((CheckBox) findViewById(R.id.cb_camelcase)).isChecked();
            boolean digits = ((CheckBox) findViewById(R.id.cb_digits)).isChecked();
            boolean specials = ((CheckBox) findViewById(R.id.cb_specials)).isChecked();

            PasswordGenerator generator = new PasswordGenerator(this);
            password = generator.generateWordPassword(words, camelCase, digits, specials);

            fillEntropy(generator.calculateWordEntropy(words, camelCase, digits, specials));
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_wrong_length, Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return password;
    }
}
