package com.example.socketiosample;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

	private EditText editText;
	private ArrayAdapter<String> adapter;
	private SocketIO socket;
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// ListViewの設定
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		ListView listView = (ListView)findViewById(R.id.listView1);
		listView.setAdapter(adapter);

		editText = (EditText)findViewById(R.id.editText1);

		try {
			connect();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    private void connect() throws MalformedURLException{
		socket = new SocketIO("http://10.0.2.2:3000/");
		socket.connect(iocallback);
    }

	private IOCallback iocallback = new IOCallback() {

		@Override
		public void onConnect() {
		    System.out.println("onConnect");
		}

		@Override
		public void onDisconnect() {
		    System.out.println("onDisconnect");
		}

		@Override
		public void onMessage(JSONObject json, IOAcknowledge ack) {
			System.out.println("onMessage");
		}

		@Override
		public void onMessage(String data, IOAcknowledge ack) {
		    System.out.println("onMessage");
		}

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			final JSONObject message = (JSONObject)args[0];

			new Thread(new Runnable() {
				public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							if(message.getString("message") != null) {
								// メッセージが空でなければ追加
								adapter.insert(message.getString("message"), 0);
							}

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}).start();
		}

		@Override
		public void onError(SocketIOException socketIOException) {
		    System.out.println("onError");
		    socketIOException.printStackTrace();
		}
    };

    public void sendEvent(View view){
		// 文字が入力されていなければ何もしない
		if (editText.getText().toString().length() == 0) {
		    return;
		}

		try {
		// イベント送信
			JSONObject json = new JSONObject();
			json.put("message", editText.getText().toString());
			socket.emit("message:send", json);

		} catch (JSONException e) {
			e.printStackTrace();
		}

    	// テキストフィールドをリセット
    	editText.setText("");
    }
}
