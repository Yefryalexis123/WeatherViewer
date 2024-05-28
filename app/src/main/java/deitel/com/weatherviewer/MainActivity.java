package deitel.com.weatherviewer;

import static android.app.ProgressDialog.show;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Weather> weatherList = new ArrayList<>();
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView;
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contentLayout = findViewById(R.id.contentLayout);

        weatherListView = findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText locationEditText = findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());

                if (url != null) {
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getWeatherTask = new GetWeatherTask();
                    getWeatherTask.execute(url);
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorlayout), R.string.URL_invalida, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private URL createURL(String city) {
        String apikey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") + "&units=imperial&cnt=16&APPID=" + apikey;
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        runOnUiThread(() -> Snackbar.make(findViewById(R.id.coordinatorlayout), R.string.error_de_lectura, Snackbar.LENGTH_LONG).show());
                        e.printStackTrace();
                    }
                    return new JSONObject(builder.toString());
                } else {
                    runOnUiThread(() -> Snackbar.make(findViewById(R.id.coordinatorlayout), R.string.conectar_error, Snackbar.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Snackbar.make(findViewById(R.id.coordinatorlayout), R.string.conectar_error, Snackbar.LENGTH_LONG).show());
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJSONtoArrayList(weather);
            weatherArrayAdapter.notifyDataSetChanged();
            weatherListView.smoothScrollToPosition(0);
        }
    }

    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear();

        try {
            JSONArray list = forecast.getJSONArray("list");
            for (int i = 0; i < list.length(); ++i) {
                JSONObject day = list.getJSONObject(i);

                JSONObject temperatures = day.getJSONObject("main");

                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                weatherList.add(new Weather(
                        day.getLong("dt"), // timestamp
                        temperatures.getDouble("temp_min"), // minimal temperature
                        temperatures.getDouble("temp_max"), // maximal temperature
                        temperatures.getDouble("humidity"), // humidity
                        weather.getString("description"), // weather conditions
                        weather.getString("icon")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
