package com.taan.hasani.moein.guess_it.game;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taan.hasani.moein.guess_it.appcontroller.AppController;
import com.taan.hasani.moein.volley.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class single_Player extends AppCompatActivity {

    private EditText entered_word;
    private Button check_bt, next_word_bt;
    private TextView word_TextView, message, timer;
    private String MY_PREFS_NAME = "username and password";

    private String incompleteWord, id, completeWord, game_ID,
            url = "http://online6732.tk/guessIt.php";

    private SharedPreferences prefs;
    private String recivedTime;
    private CountDownTimer countDownTimer;
    private int spent_time;
    private String flag__nextWord_Timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_player);

        next_word_bt = (Button) findViewById(R.id.next_word_bt);
        message = (TextView) findViewById(R.id.message);
        word_TextView = (TextView) findViewById(R.id.word);
        entered_word = (EditText) findViewById(R.id.enterd_word);
        check_bt = (Button) findViewById(R.id.check);
        timer = (TextView) findViewById(R.id.timer);

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        id = prefs.getString("userID", null);

        newSinglePlayerGame();

        ////////////////////////////////

        check_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Player_time = timer.getText().toString();
                String Player_score = Integer.toString(15 - Integer.parseInt(Player_time));

                if (entered_word.getText().toString().equals(completeWord)) {

                    countDownTimer.cancel();

                    word_TextView.setText(completeWord);
                    message.setText("Congratulations !!! Your guess was RIGHT !");
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.success);
                    mediaPlayer.start();

                } else {

                    message.setText("No,Guess again !");

                }

                if (timer.getText().toString().equals("0")) {
                    Toast.makeText(getApplicationContext(), "Times up!", Toast.LENGTH_SHORT).show();
                }

                setAnswer(entered_word.getText().toString(),
                        Player_time, Player_score);

            }
        });


        next_word_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (flag__nextWord_Timer == "yes") {

                    countDownTimer.cancel();

                    if (timer.getText().toString() != recivedTime) {
                        spent_time = 0;

                        sendNextWord();

                    } else {
                        spent_time = Integer.parseInt(recivedTime) - Integer.parseInt(timer.getText().toString());

                        sendNextWord();
                    }

                } else {

                    sendNextWord();

                }
            }
        });

    }

    public void newSinglePlayerGame() {
        HashMap<String, String> info = new HashMap<>();

        info.put("action", "newGame");
        info.put("userID", id);
        info.put("mode", "singlePlayer");

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {

                    Toast.makeText(getApplicationContext(),
                            response.toString(), Toast.LENGTH_LONG).show();
                    game_ID = response.getString("gameID");

                    if (response.getString("dataIsRight").equals("yes")) {
                        setGameSettings();
                    } else {

                        Toast.makeText(getApplicationContext(), " data is right=no ,sth went wrong..."
                                , Toast.LENGTH_SHORT).show();
                        newSinglePlayerGame();
                    }


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "newSinglePlayerGame " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "*newSinglePlayerGame**Volley  :" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void setGameSettings() {
        HashMap<String, String> info = new HashMap<>();

        try {
            info.put("categories", URLEncoder.encode("1", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
        }

        info.put("action", "setGameSetting");
        info.put("userID", id);
        info.put("gameID", game_ID);

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.getString("dataIsRight").equals("yes")) {

                        sendNextWord();

                    } else {

                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            e.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "setGameSetting***Volley  :" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);


    }

    public void sendNextWord() {

        HashMap<String, String> info = new HashMap<>();

        info.put("action", "sendNextWord");
        info.put("gameID", game_ID);
        info.put("userID", id);

        word_TextView.setText("");
        message.setText("");
        // timer.setText("");

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // Toast.makeText(getApplicationContext(),
                //         response.toString(), Toast.LENGTH_LONG).show();

                try {

                    if (response.getString("dataIsRight").equals("yes")) {

                        flag__nextWord_Timer = "yes";

                        incompleteWord = response.getJSONObject("word").getString("incompleteWord");
                        //  .getBytes("ISO-8859-1"), "UTF-8");

                        completeWord = response.getJSONObject("word").getString("word");
                        //     .getBytes("ISO-8859-1"), "UTF-8");

                        recivedTime = response.getJSONObject("word").getString("time");

                        ////////////////////////////////////////////
                        countDownTimer = new CountDownTimer((Integer.parseInt(recivedTime) - spent_time) * 1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                timer.setText("" + millisUntilFinished / 1000);
                            }

                            public void onFinish() {
                                timer.setText("0");
                                setAnswer(entered_word.getText().toString(),
                                        "0", "0");
                                Toast.makeText(getApplicationContext(), "Time's Up!", Toast.LENGTH_SHORT).show();
                            }
                        };
                        ////////////////////////////////////////////
                        countDownTimer.start();

                        word_TextView.setText(incompleteWord);

                    } else {


                        Toast.makeText(getApplicationContext(),
                                "next word dataIsRight =no ", Toast.LENGTH_LONG).show();


                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            e.toString(), Toast.LENGTH_LONG).show();
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "sendNextWord***Volley  :" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);


    }

    public void setAnswer(String entered_word, String player_time,
                          String player_score) {

        HashMap<String, String> info = new HashMap<>();
        HashMap<String, String> answer_hashmap = new HashMap<>();
        /////////////////////////
        answer_hashmap.put("time", player_time);
        answer_hashmap.put("score", player_score);
        answer_hashmap.put("answer", entered_word);

        JSONObject answer = new JSONObject(answer_hashmap);

        /////////////////////////
        info.put("action", "setAnswer");
        info.put("gameID", game_ID);
        info.put("userID", id);
        info.put("answer", answer.toString());
        /////////////////////////

        JSONObject jsonObject = new JSONObject(info);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Toast.makeText(getApplicationContext(),
                        "setAnswer response  :" + response.toString(), Toast.LENGTH_LONG).show();

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "setAnswer***Volley  :" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

}
