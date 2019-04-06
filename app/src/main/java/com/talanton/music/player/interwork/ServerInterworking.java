package com.talanton.music.player.interwork;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talanton.music.player.utils.Constants;
import com.talanton.music.player.utils.MusicUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class ServerInterworking {
	private static Context mContext;
	private static ServerInterworking instance = new ServerInterworking();
	private ServerInterworking() {	}
	public static ServerInterworking getInstance(Context context) {
		mContext = context;
		return instance;
	}

	private final String USER_AGENT = "Mozilla/5.0";

	// SignUp interworking
	public interface ISignUpResult {
		void getSignUpResult(String result);
	}

	ISignUpResult mSignUpResult;

	public void registerISignUpResult(ISignUpResult callback) {
		mSignUpResult = callback;
	}

	public void initSignUp(String... params) {
		SignUpProcessingTask myTask = new SignUpProcessingTask();
		myTask.execute(params);
	}

	public class SignUpProcessingTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StringBuilder output = new StringBuilder();

			try {
				URL url = new URL(Constants.makeURL(params[0]));
				JSONObject postDataParams = new JSONObject();
				postDataParams.put("id", params[1]);
				postDataParams.put("password", params[2]);
				postDataParams.put("name", params[3]);
				postDataParams.put("mobile", params[4]);
				postDataParams.put("kind", 1);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if (conn != null) {
					conn.setReadTimeout(15000);
					conn.setConnectTimeout(30000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(getPostDataString(postDataParams));

					writer.flush();
					writer.close();
					os.close();

					int resCode = conn.getResponseCode();

					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
					String line = null;
					while(true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						output.append(line + "\n");
					}

					reader.close();
					conn.disconnect();
				}
			} catch(Exception ex) {
				Log.e(Constants.TAG, "Exception in processing response.", ex);
				//ex.printStackTrace();
			}

			return output.toString().trim();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(Constants.TAG, "result = " + result);

			// call callback method
			mSignUpResult.getSignUpResult(result);
		}
	}

	public String getPostDataString(JSONObject params) throws Exception {

		StringBuilder result = new StringBuilder();
		boolean first = true;

		Iterator<String> itr = params.keys();

		while(itr.hasNext()){

			String key= itr.next();
			Object value = params.get(key);

			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(value.toString(), "UTF-8"));

		}
		return result.toString();
	}

	// 아이디 중복 검사
	public interface IDuplicateCheckID {
		void getDuplicateCheckIDResult(String result);
	}

	IDuplicateCheckID mDuplicateCheckIDResult;

	public void registerIDuplicateCheckIDResult(IDuplicateCheckID callback) {
		mDuplicateCheckIDResult = callback;
	}

	public void initDuplicateCheckID(String... params) {
		DuplicateCheckIDProcessingTask myTask = new DuplicateCheckIDProcessingTask();
		myTask.execute(params);
	}

	public class DuplicateCheckIDProcessingTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StringBuilder output = new StringBuilder();

			try {
				URL url = new URL(Constants.makeURL(params[0]));
				JSONObject postDataParams = new JSONObject();
				postDataParams.put("id", params[1]);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if (conn != null) {
					conn.setReadTimeout(15000);
					conn.setConnectTimeout(30000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(getPostDataString(postDataParams));

					writer.flush();
					writer.close();
					os.close();

					int resCode = conn.getResponseCode();

					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
					String line = null;
					while(true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						output.append(line + "\n");
					}

					reader.close();
					conn.disconnect();
				}
			} catch(Exception ex) {
				Log.e(Constants.TAG, "Exception in processing response.", ex);
				//ex.printStackTrace();
			}

			return output.toString().trim();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(Constants.TAG, "result = " + result);

			// call callback method
			mDuplicateCheckIDResult.getDuplicateCheckIDResult(result);
		}
	}

	// 로그인 처리
	public interface ISignInResult {
		void getSignInResult(String result);
	}

	ISignInResult mSignInResult;

	public void registerISignInResult(ISignInResult callback) {
		mSignInResult = callback;
	}

	public void initSignIn(String... params) {
		SignInProcessingTask myTask = new SignInProcessingTask();
		myTask.execute(params);
	}

	public class SignInProcessingTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StringBuilder output = new StringBuilder();

			try {
				URL url = new URL(Constants.makeURL(params[0]));
				JSONObject postDataParams = new JSONObject();
				postDataParams.put("id", params[1]);
				postDataParams.put("password", params[2]);
				postDataParams.put("useCookie", true);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if (conn != null) {
					conn.setReadTimeout(15000);
					conn.setConnectTimeout(30000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(getPostDataString(postDataParams));

					writer.flush();
					writer.close();
					os.close();

					int resCode = conn.getResponseCode();
					if(resCode == 200) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
						String line = null;
						while(true) {
							line = reader.readLine();
							if (line == null) {
								break;
							}
							output.append(line + "\n");
						}

						reader.close();
					}
					conn.disconnect();
				}
			} catch(Exception ex) {
				Log.e(Constants.TAG, "Exception in processing response.", ex);
				//ex.printStackTrace();
			}

			return output.toString().trim();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(Constants.TAG, "result = " + result);

			// call callback method
			mSignInResult.getSignInResult(result);
		}
	}

	// 로그아웃 처리
	public interface ISignOutResult {
		void getSignOutResult(String result);
	}

	ISignOutResult mSignOutResult;

	public void registerISignOutResult(ISignOutResult callback) {
		mSignOutResult = callback;
	}

	public void initSignOut(String... params) {
		SignOutProcessingTask myTask = new SignOutProcessingTask();
		myTask.execute(params);
	}

	public class SignOutProcessingTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StringBuilder output = new StringBuilder();

			try {
				URL url = new URL(Constants.makeURL(params[0]));
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if (conn != null) {
					conn.setReadTimeout(15000);
					conn.setConnectTimeout(30000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);

					int resCode = conn.getResponseCode();

					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
					String line = null;
					while(true) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						output.append(line + "\n");
					}

					reader.close();
					conn.disconnect();
				}
			} catch(Exception ex) {
				Log.e(Constants.TAG, "Exception in processing response.", ex);
				//ex.printStackTrace();
			}

			return output.toString().trim();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(Constants.TAG, "result = " + result);

			// call callback method
			mSignOutResult.getSignOutResult(result);
		}
	}

	// 데이터베이스 테이블에 대한 정보를 얻기 위해 시스템 파라미터 정보를 가져온다.
	public interface GetServiceParameterInfo {
		void callbackReturnServiceParameter(String result);
	}

	GetServiceParameterInfo mParameterInfoClass;
	
	public void registerCallback(GetServiceParameterInfo callbackClass) {
		mParameterInfoClass = callbackClass;
	}
	
	public void initGettingServiceParameterInfo(String urlPath) {
		// do something here
		GetServiceParameterInfoTask myTask = new GetServiceParameterInfoTask();
    	myTask.execute(urlPath);
	}

	public class GetServiceParameterInfoTask extends AsyncTask<String, Void, String> {
		@Override
    	protected String doInBackground(String... urlString) {
    		StringBuilder output = new StringBuilder();
        	try {
        		URL urlValue = new URL(Constants.makeURL(urlString[0]));
        		HttpURLConnection conn = (HttpURLConnection)urlValue.openConnection();
        		if (conn != null) {
        			conn.setConnectTimeout(10000);
        			conn.setRequestProperty("User-Agent", USER_AGENT);
        			conn.setRequestMethod("GET");

        			int resCode = conn.getResponseCode();
        			if (resCode == HttpURLConnection.HTTP_OK) {
        				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
        				String line = null;
        				while(true) {
        					line = reader.readLine();
        					if (line == null) {
        						break;
        					}
        					output.append(line);
        				}
        				
        				reader.close();
        				conn.disconnect();
        			}
        		}
        	} catch(Exception ex) {
        		Log.e(Constants.TAG, "Exception in processing response.", ex);
        	}

    		return output.toString();
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
    		Log.i(Constants.TAG, "result = " + result);
    		
    		// call callback method
			mParameterInfoClass.callbackReturnServiceParameter(result);
    	}
    }

	public interface DownloadFileContent {
		void callbackReturnDownloadFinish();
	}

	// 서버로부터 파일 다운로드 (여기서는 DB)
	DownloadFileContent mDownloadFileClass;
	
	public void registerCallbackForDownloadFile(DownloadFileContent callbackClass) {
		mDownloadFileClass = callbackClass;
	}

	// parameter[0]: URL, parameter[1]: id, parameter[2]: key, parameter[3]: 데이터베이스 파일 이름
	public void initDownloadFileContent(String... parameter) {
		Log.i(Constants.TAG, "ServerInterworking:initDownloadFileContent()");
		DownloadFileTask myTask = new DownloadFileTask();
    	myTask.execute(parameter);
	}

    public class DownloadFileTask extends AsyncTask<String, Void, String> {
		@Override
    	protected String doInBackground(String... parameter) {
			BufferedReader reader = null;
			HttpURLConnection conn = null;
			FileOutputStream fos = null;
			String filePath = null;
			try {
				String destination = Constants.makeURL(parameter[0]);
           		Log.i(Constants.TAG, "download url = " + destination);
           		URL url = new URL(destination);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", parameter[1]);
                postDataParams.put("key", parameter[2]);
           		conn = (HttpURLConnection)url.openConnection();
           		if (conn != null) {
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(30000);
           			conn.setRequestMethod("POST");
           			conn.setDoInput(true);
           			conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(postDataParams));

                    writer.flush();
                    writer.close();
                    os.close();

           			int resCode = conn.getResponseCode();
           			if (resCode == HttpURLConnection.HTTP_CREATED) {
                        File directory = new File(MusicUtils.getDownloadExternalStorageDirectoy());
						if(!directory.exists())
							directory.mkdirs();
           				File dbFile = new File(directory, parameter[3]);
           				fos = new FileOutputStream(dbFile);
            				
           				InputStream is = conn.getInputStream();
           				byte[] buffer = new byte[8096];
           				int length = 0;
           				while((length = is.read(buffer)) > 0)
           					fos.write(buffer, 0, length);
           			}
           		}
			} catch(Exception ex) {
				Log.e("SampleHTTP", "Exception in processing response.", ex);
			}
			finally {
				try {
					if(reader != null)
						reader.close();
					if(conn != null)
						conn.disconnect();
					if(fos != null)
						fos.close();
				}
				catch (IOException ec) {}
			}
        	return null;
    	}

    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
    		
    		// call callback method
    		mDownloadFileClass.callbackReturnDownloadFinish();
    	}
    }

    // Login Session Check
    public interface LoginSessionCheck {
        void callbackLoginSessionCheck(String result);
    }

    LoginSessionCheck mLoginSessionCheck;

    public void registerSessionCheckCallback(LoginSessionCheck callback) {
        mLoginSessionCheck = callback;
    }

    public void initSessionCheck(String... parameter) {
        // do something here
        LoginSessionCheckTask myTask = new LoginSessionCheckTask();
        myTask.execute(parameter);
    }

    public class LoginSessionCheckTask extends AsyncTask<String, Void, String> {
    	private String called;

        @Override
        protected String doInBackground(String... parameter) {
            StringBuilder output = new StringBuilder();
            called = parameter[2];
            try {
                URL urlValue = new URL(Constants.makeURL(parameter[0]));
                HttpURLConnection conn = (HttpURLConnection)urlValue.openConnection();
                if (conn != null) {
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Cookie", "loginCookie="+ parameter[1]);
                    conn.setRequestProperty("User-Agent", USER_AGENT);
                    conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
                        String line = null;
                        while(true) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            output.append(line);
                        }

                        reader.close();
                        conn.disconnect();
                    }
                }
            } catch(Exception ex) {
                Log.e(Constants.TAG, "Exception in processing response.", ex);
            }

            return output.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(Constants.TAG, "result = " + result);

            // call callback method
			if(result.equals("ok")) {
				mLoginSessionCheck.callbackLoginSessionCheck(called);
			} else {
				mLoginSessionCheck.callbackLoginSessionCheck("0");
			}
        }
    }

    /*
	public interface ReportDownloadStatus {
		void callbackReturnReportDownloadStatus(String result);
	}

	ReportDownloadStatus mDownloadStatusClass;

	public void registerCallbackDownload(ReportDownloadStatus callbackClass) {
		mDownloadStatusClass = callbackClass;
	}

	public void reportDownloadStateToServer(String urlPath, String parameter) {
		// do something here
		ReportDownloadStatusTask myTask = new ReportDownloadStatusTask();
		myTask.execute(urlPath, parameter);
	}

	public class ReportDownloadStatusTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urlString) {
			StringBuilder output = new StringBuilder();
			try {
				URL urlValue = new URL(urlString[0]);
				HttpURLConnection conn = (HttpURLConnection)urlValue.openConnection();
				if (conn != null) {
					conn.setRequestMethod("POST");
					conn.setRequestProperty("User-Agent", USER_AGENT);
					conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					conn.setDoOutput(true);

					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
					wr.writeBytes(urlString[1]);
					wr.flush();
					wr.close();

					int resCode = conn.getResponseCode();
					if (resCode == HttpURLConnection.HTTP_OK) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
						String line = null;
						while(true) {
							line = reader.readLine();
							if (line == null) {
								break;
							}
							output.append(line);
						}

						reader.close();
						conn.disconnect();
					}
				}
			} catch(Exception ex) {
				Log.e(TAG, "Exception in processing response.", ex);
			}

			return output.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(TAG, "result = " + result);

			// call callback method
			mDownloadStatusClass.callbackReturnReportDownloadStatus(result);
		}
	}

	public interface ReportPlayStatus {
		void callbackReturnReportPlayStatus(String result);
	}

	ReportPlayStatus mPlayStatusClass;

	public void registerCallbackPlay(ReportPlayStatus callbackClass) {
		mPlayStatusClass = callbackClass;
	}

	public void reportPlayStateToServer(String urlPath, String parameter) {
		// do something here
		ReportPlayStatusTask myTask = new ReportPlayStatusTask();
		myTask.execute(urlPath, parameter);
	}

	public class ReportPlayStatusTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urlString) {
			StringBuilder output = new StringBuilder();
			try {
				URL urlValue = new URL(urlString[0]);
				HttpURLConnection conn = (HttpURLConnection)urlValue.openConnection();
				if (conn != null) {
					conn.setRequestMethod("POST");
					conn.setRequestProperty("User-Agent", USER_AGENT);
					conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					conn.setDoOutput(true);

					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
					wr.writeBytes(urlString[1]);
					wr.flush();
					wr.close();

					int resCode = conn.getResponseCode();
					if (resCode == HttpURLConnection.HTTP_OK) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
						String line = null;
						while(true) {
							line = reader.readLine();
							if (line == null) {
								break;
							}
							output.append(line);
						}

						reader.close();
						conn.disconnect();
					}
				}
			} catch(Exception ex) {
				Log.e(TAG, "Exception in processing response.", ex);
			}

			return output.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(TAG, "result = " + result);

			// call callback method
			mPlayStatusClass.callbackReturnReportPlayStatus(result);
		}
	}
	*/
}