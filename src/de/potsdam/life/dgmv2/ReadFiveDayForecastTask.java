package de.potsdam.life.dgmv2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

class ReadFiveDayForecastTask extends AsyncTask<Object, Object, String> 
{
   private static final String TAG = "ReadFiveDayForecastTask";   
   
   private String zipcodeString;
   private FiveDayForecastLoadedListener weatherFiveDayForecastListener;
   private Resources resources;
   private DailyForecast[] forecasts;
   private static final int NUMBER_OF_DAYS = 5;
   
   // interface for receiver of weather information
   public interface FiveDayForecastLoadedListener 
   {
      public void onForecastLoaded(DailyForecast[] forecasts);
   } // end interface FiveDayForecastLoadedListener
   
   // creates a new ReadForecastTask
   public ReadFiveDayForecastTask(String zipcodeString, 
      FiveDayForecastLoadedListener listener, Context context)
   {
      this.zipcodeString = zipcodeString;
      this.weatherFiveDayForecastListener = listener;
      this.resources = context.getResources();
      this.forecasts = new DailyForecast[NUMBER_OF_DAYS];
   } // end constructor ReadFiveDayForecastTask
      
   @Override
   protected String doInBackground(Object... params) 
   {
      // the url for the WeatherBug JSON service
      try 
      {
         URL webServiceURL = new URL("http://api.openweathermap.org/data/2.5/forecast?lang=de&units=metric&id="+ zipcodeString);
         
         // create a stream Reader from the WeatherBug url
         Reader forecastReader = new InputStreamReader(
            webServiceURL.openStream());
             
         // create a JsonReader from the Reader
         JsonReader forecastJsonReader = new JsonReader(forecastReader);
             
         forecastJsonReader.beginObject(); // read the next Object


		 while (forecastJsonReader.hasNext()) {

		String name = forecastJsonReader.nextName();

		if(name.equals("list")){//resources.getString(R.string.hourly_forecast)
			readDailyForecast(forecastJsonReader);
		} else {
        	 forecastJsonReader.skipValue();
        }

		// end http://api.openweathermap.org
		 }
		 forecastJsonReader.endObject();
         forecastJsonReader.close(); // close the JsonReader
         
      } // end try 
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString());
      } // end catch
      catch (NotFoundException e) 
      {
    	  Log.v(TAG, e.toString());
      } // end catch 
      catch (IOException e) 
      {
    	  Log.v(TAG, e.toString());
      } // end catch
      return null;
   } // end method doInBackground
   
   // read a single daily forecast
   private DailyForecast readDailyForecast(JsonReader forecastJsonReader) throws IOException
   {
       String[] dailyForecast = new String[4]; 
       Bitmap iconBitmap = null; // store the forecast's image
      
      forecastJsonReader.beginArray();
      forecastJsonReader.beginObject();
      while (forecastJsonReader.hasNext()) {
        // Anfang von der Parsing der WeterDaten
    	  
                String name = forecastJsonReader.nextName(); // read next name

                if (name.equals(resources.getString(R.string.read_day)))
                {
                	int date=forecastJsonReader.nextInt();
                   dailyForecast[DailyForecast.DAY_INDEX] = readDay(date);
//                      forecastJsonReader.nextString(); //		1
                } // end if
                else if (name.equals(resources.getString(
                   R.string.day_prediction)))  
                {
                   dailyForecast[DailyForecast.PREDICTION_INDEX] = 
                    forecastJsonReader.nextString(); //			2
                } // end else if
                else if (name.equals(resources.getString(R.string.high))) 
                {
                   dailyForecast[DailyForecast.HIGH_TEMP_INDEX] = 
                      forecastJsonReader.nextString(); //		3
                } // end else if 
                else if (name.equals(resources.getString(R.string.low))) 
                {
                   dailyForecast[DailyForecast.LOW_TEMP_INDEX] = 
                      forecastJsonReader.nextString(); //		4
                } // end else if
                // if the next item is the icon name
                else if (name.equals(resources.getString(R.string.day_icon))) 
                {
                   // read the icon name
                   iconBitmap = ReadForecastTask.getIconBitmap(
                      forecastJsonReader.nextString(), resources, 0);
                } // end else if
                else // there is an unexpected element
                {
                   forecastJsonReader.skipValue(); // skip the next element
                } // end else
             
       
    

      }
      forecastJsonReader.endObject();
      forecastJsonReader.endArray();
      
  
      
      return new DailyForecast(dailyForecast, iconBitmap);
   } // end method readDailyForecast

   private String readDay(int day) {
	   Calendar getDay = Calendar.getInstance();//	TO DO getTimezone
	   getDay.setTimeInMillis((long)Integer.valueOf(day) * 1000);
	   String time = String.valueOf(getDay.get(Calendar.DAY_OF_MONTH))+"."+String.valueOf(getDay.get(Integer.valueOf(Calendar.LONG)))+"."+String.valueOf(getDay.get(Calendar.YEAR));
//	   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//	    String time = sdf.format(new Date(day*1000));
	return time;
}

// update the UI back on the main thread
   protected void onPostExecute(String forecastString) 
   {
      weatherFiveDayForecastListener.onForecastLoaded(forecasts);
   } // end method onPostExecute
} // end class ReadFiveDayForecastTask
