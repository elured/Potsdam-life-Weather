package de.potsdam.life.dgmv2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

class ReadForecastTask extends AsyncTask<Object, Object, String> 
{
   private String zipcodeString; // the zipcode of the forecast's city
   private Resources resources;
   
   // receives weather information
   private ForecastListener weatherForecastListener; 
   private static final String TAG = "ReadForecastTask.java";
   
   private String temperatureString; // the temperature
   private String feelsLikeString; // the "feels like" temperature
   private String humidityString; // the humidity
   private String chanceOfPrecipitationString; // chance of precipitation
   private Bitmap iconBitmap; // image of the sky condition
   
   private int bitmapSampleSize = -1;
     
   // interface for receiver of weather information
   public interface ForecastListener 
   {
      public void onForecastLoaded(Bitmap image, String temperature, 
         String feelsLike, String humidity, String precipitation);
   } // end interface ForecastListener

   // creates a new ReadForecastTask
   public ReadForecastTask(String zipcodeString, 
      ForecastListener listener, Context context)
   {
      this.zipcodeString = zipcodeString;
      this.weatherForecastListener = listener;
      this.resources = context.getResources();
   } // end constructor ReadForecastTask
   
   // set the sample size for the forecast's Bitmap
   public void setSampleSize(int sampleSize)
   {
      this.bitmapSampleSize = sampleSize;
   } // end method setSampleSize
     
   // load the forecast in a background thread
   protected String doInBackground(Object... args) 
   {
      try 
      {
			
			// begin http://api.openweathermap.org
			URL webServiceURL = new URL(resources.getString(R.string.pre_zipcode_url)+zipcodeString);
//			Log.v(TAG, zipcodeString);
			Reader forecastReader = new InputStreamReader(webServiceURL.openStream());
//			Log.v(TAG, "forecastReader");
			JsonReader forecastJsonReader = new JsonReader(forecastReader);
//			Log.v(TAG, "forecastJsonReader");
			forecastJsonReader.beginObject();

			 while (forecastJsonReader.hasNext()) {

			String name = forecastJsonReader.nextName();

			if(name.equals("list")){//resources.getString(R.string.hourly_forecast)
				readForecast(forecastJsonReader);
			} else {
	        	 forecastJsonReader.skipValue();
	        }

			// end http://api.openweathermap.org
			 }
			 forecastJsonReader.endObject();
			forecastJsonReader.close();
			
			
		} // end try
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      catch (IOException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      catch (IllegalStateException e)
      {
        Log.v(TAG, e.toString() + zipcodeString);
      } // end catch
      return null;  
   } // end method doInBackground

   // update the UI back on the main thread
   protected void onPostExecute(String forecastString) 
   {
      // pass the information to the ForecastListener
      weatherForecastListener.onForecastLoaded(iconBitmap, 
         temperatureString, feelsLikeString, humidityString, 
         chanceOfPrecipitationString);
   } // end method onPostExecute
    
   // get the sky condition image Bitmap
 	public static Bitmap getIconBitmap(String conditionString, Resources resources, int bitmapSampleSize){
		Bitmap iconBitmap = null;
		try {
			URL weatherURL	 = new URL(resources.getString(R.string.pre_condition_url) + conditionString + resources.getString(R.string.post_condition_url));
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (bitmapSampleSize!=-1){
				options.inSampleSize = bitmapSampleSize;
			}
			iconBitmap = BitmapFactory.decodeStream(weatherURL.openStream(),null,options);
			
		} catch (MalformedURLException e) {
			Log.v(TAG, e.toString());
		} catch (NotFoundException e) {
			Log.v(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, e.toString());
		}
		return iconBitmap;
		} // end method getIconBitmap
    
   // read the forecast information using the given JsonReader
   private String readForecast(JsonReader reader) {
		try {// name = list
			reader.beginArray();
			reader.beginObject();
			
			while(reader.hasNext()){
				String name = reader.nextName();
				if(name.equals(resources.getString(R.string.json_object_main))){
					readMain(reader);
				}else if(name.equals(resources.getString(R.string.json_object_weather))){
					readWeather(reader);
				}else{
					reader.skipValue();
				}
				
			}
			reader.endObject();
			reader.endArray();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	} // end method readForecast
   private String readWeather(JsonReader reader) {
		try {
			reader.beginArray();
			reader.beginObject();
			
			while(reader.hasNext()){
				String name = reader.nextName();
				if(name.equals(resources.getString(R.string.description))){// R.string.chance_of_precipitation   <string name="chance_of_precipitation">chancePrecip</string>
					chanceOfPrecipitationString = reader.nextString();// weather - description
				}else if(name.equals(resources.getString(R.string.icon))){
					iconBitmap	 = getIconBitmap(reader.nextString(), resources, bitmapSampleSize);// weather - icon
				}else {
					reader.skipValue();
				}
			}
			reader.endObject();
			reader.endArray();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	private String readMain(JsonReader reader) {
		try {
			reader.beginObject();
			
			while(reader.hasNext()){
				String name = reader.nextName();
				if(name.equals(resources.getString(R.string.temperature))){
					temperatureString = reader.nextString(); // main - temp
					feelsLikeString = temperatureString;
				}else if(name.equals(resources.getString(R.string.humidity))){
					humidityString= reader.nextString();// main - humidity
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}
} 
