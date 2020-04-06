package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class CreateActivityGenerate extends AppCompatActivity {

    Button btnGenerate, btnContinue, btnCancel;
    TextView tvGameCode;

    private static final String GEN_API = "http://192.168.50.126:3000/generate";
    private static final String SERVER = "http://192.168.50.126:3000";
    private Socket socket;
    private String gameCode;
    private String uniqueID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_generate);

        try {
            //point to server
            socket = IO.socket(SERVER);

            //create connection
            socket.connect();

            // emit the event join alongside with a uniqueID
            socket.emit("connect to server", uniqueID);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        tvGameCode = findViewById(R.id.tvGameCode);
        tvGameCode.setText(""); //sets game code to blank initially - code not yet generated

        //Generate Game Code Button
        btnGenerate = findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                requestGameCode();
                CreateActivityGenerate.HttpGetRequest request = new CreateActivityGenerate.HttpGetRequest();
                request.execute(); //Makes a GET request to our generate API

            }
        });

        //Continue Button from Generate Page
        btnContinue = findViewById(R.id.btnContinueGen);
        btnContinue.setAlpha(.5f);
        btnContinue.setClickable(false);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(CreateActivityGenerate.this, CreateActivityUsername.class);
                intent.putExtra("game-code", gameCode);
                intent.putExtra("uid", uniqueID);
                startActivity(intent); //switch to game code screen
                finish();
            }
        });

        //Cancel Button from Generate Page
        btnCancel = findViewById(R.id.btnCancelGen);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

    }

    //Request a game code from the server
    private void requestGameCode() {
        if (socket.connected()){
            socket.emit("generate code");
        }else{
            Toast.makeText(CreateActivityGenerate.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    public class HttpGetRequest extends AsyncTask<Void, Void, String> {

        static final String REQUEST_METHOD = "GET";
        static final int READ_TIMEOUT = 500;
        static final int CONNECTION_TIMEOUT = 500;

        @Override
        protected String doInBackground(Void... params){
            String result;
            String inputLine;

            try {
                // connect to the server
                URL myUrl = new URL(GEN_API);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.connect();

                // get the string from the input stream
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                reader.close();
                streamReader.close();
                result = stringBuilder.toString();

            } catch(IOException e) {
                e.printStackTrace();
                result = "error";
            }

            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            gameCode = result;
            tvGameCode.setText(gameCode);

            if(gameCode.equals("error") || gameCode.equals("undefined")){
                Toast.makeText(CreateActivityGenerate.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
            }else{
                btnGenerate.setAlpha(.5f);
                btnGenerate.setClickable(false);
                btnContinue.setAlpha(1f);
                btnContinue.setClickable(true);
            }
        }
    }

}
