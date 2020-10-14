package com.example.dogzear.openapi;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 변환된 좌표계를 가져오는 스레드
 * 요청 메시지 명세에  TM_X 좌표와 TM_Y 좌표 값을 요구하고 있다.
 * TM 좌표에 대한 자세한 정보 => https://woof.tistory.com/4
 * GPS로 얻어온 WGS84를 TM좌표계로 변환 해야 한다.
 */

/*
 * 다음API를 연결하는 스레드
 * 이곳에서 풀파서를 이용하여 카카오API에서 정보를 받아와 각각의 array변수에 넣어줌
 *
 */
class GetTransCoord2Thread extends Thread {	// 스레드
	static public boolean active=false;
	//파서용 변수
	int data=0;			//이건 파싱해서 array로 넣을때 번지
	public boolean isreceiver;
	String getX,getY;	//결과값
	String gridx,gridy,coordfrom,coordto;
	Handler handler;	//값 핸들러
	String key="KakaoAK 2bfedcbe1fc8de5beeeb6f8cd5645849\n";
	String url="https://dapi.kakao.com/v2/local/geo/transcoord.json?";
    String x,y;

	boolean parserEnd=false;
	public GetTransCoord2Thread(boolean receiver, String x, String y, String from, String to){

		Log.e("스레드 받은 파라메터", x +" " +y +" " + from +" " + to);
		handler=new Handler();
		isreceiver=receiver;
		gridx=x;
		gridy=y;
		coordfrom=from;
		coordto=to;

		getX=getY=null;


	}
	public void run(){
		
		if(active){
			InputStream is = null;
			try{
				parserEnd=false;
				data=0;
				String geocode=url+"x="+gridx+"&y="+gridy+"&input_coord="+coordfrom+"&output_coord="+coordto;
				URL urlCon = new URL(geocode);
				Log.e("get transcoord",geocode);
				HttpURLConnection httpCon = (HttpURLConnection)urlCon.openConnection();

				String json = "";

				// build jsonObject

				JSONObject jsonObject = new JSONObject();

				// convert JSONObject to JSON to String

				json = jsonObject.toString();


				// Set some headers to inform server about the type of the content
				httpCon.setRequestProperty("Authorization",key);
				httpCon.setRequestProperty("Accept", "application/json");

				httpCon.setRequestProperty("Content-type", "application/json");

				httpCon.setRequestMethod("GET");
				// 컨트롤 캐쉬 설정
				//httpCon.setRequestProperty("Cache-Control","no-cache");

				// 타입길이 설정(Request Body 전달시 Data Type의 길이를 정함.)
				//httpCon.setRequestProperty("Content-Length", "length");

				// User-Agent 값 설정
				//httpCon.setRequestProperty("User-Agent", "test");


				// OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.

				httpCon.setDoOutput(true);


				// InputStream으로 서버로 부터 응답을 받겠다는 옵션.

				httpCon.setDoInput(true);



				OutputStream os = httpCon.getOutputStream();

				os.write(json.getBytes("euc-kr"));

				os.flush();
				// receive response as inputStream
				String str, receiveMsg;

				if (httpCon.getResponseCode() == httpCon.HTTP_OK) {
					is = httpCon.getInputStream();
					InputStreamReader responseBodyReader =
							new InputStreamReader(is, "UTF-8");
					// convert inputstream to string
					Log.e("responseBodyReader", responseBodyReader.toString().length() + "");
					BufferedReader reader = new BufferedReader(responseBodyReader);
					StringBuffer buffer = new StringBuffer();
					while ((str = reader.readLine()) != null) {
						buffer.append(str);
					}
					receiveMsg = buffer.toString();
					//Log.i("receiveMsg : ", receiveMsg);
					reader.close();
					JSONObject jobject=new JSONObject(receiveMsg);

					String filter=jobject.getString("documents");
					Log.e("filter",filter);

					JSONArray jArray=new JSONArray(filter);
					jobject=jArray.getJSONObject(0);

					x=jobject.getString("x");
					y=jobject.getString("y");
					parserEnd=true;
					Log.e("검색결과", x+" "+y);
				}else{
					Log.e("통신 결과", httpCon.getResponseCode() + "에러");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			if(parserEnd){

				showtext();
			}
		}

	}
	
	/*
	 * 이 부분이 뿌려주는곳
	 * 뿌리는건 핸들러가~
	 *
	 */
	private void showtext(){
		
		handler.post(new Runnable() {	//기본 핸들러니깐 handler.post하면됨
			
			@Override
			public void run() {
				
				active=false;

				DustOpenAPIActivity.TransCoordThreadResponse(x, y);

			}
		});
	}
}
