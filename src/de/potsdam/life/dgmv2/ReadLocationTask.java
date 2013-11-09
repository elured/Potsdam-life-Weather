package de.potsdam.life.dgmv2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

class ReadLocationTask extends AsyncTask<Object, Object, String>
{
   private static final String TAG = "ReadLocatonTask.java";   
   
   private String zipcodeString; // the zipcode for the location 
   private Context context; // launching Activity's Context
   private Resources resources; // used to look up String from xml
   
   // Strings for each type of data retrieved
   private String cityString;
   private String stateString;
   private String countryString;
   
   // listener for retrieved information
   private LocationLoadedListener weatherLocationLoadedListener; 
   
   // interface for receiver of location information
   public interface LocationLoadedListener 
   {
      public void onLocationLoaded(String cityString, String stateString, 
         String countryString);
   } // end interface LocationLoadedListener
   
   // public constructor
   public ReadLocationTask(String zipCodeString, Context context, 
      LocationLoadedListener listener)
   {
      this.zipcodeString = zipCodeString;
      this.context = context;
      this.resources = context.getResources();
      this.weatherLocationLoadedListener = listener;
   } // end constructor ReadLocationTask
   
   // load city name in background thread
   @Override
   protected String doInBackground(Object... params) 
   {
      try 
      {
			URL url = new URL(resources.getString(R.string.location_url_pre_zipcode) + zipcodeString);
			Reader foreacastReader = new InputStreamReader(url.openStream());
//			String cityName = null;
			JsonReader forecastJsonReader = new JsonReader(foreacastReader);
			forecastJsonReader.beginObject();
		     while (forecastJsonReader.hasNext()) {
		         String name = forecastJsonReader.nextName();
		         if (name.equals(resources.getString(R.string.city))) {
		           cityString = forecastJsonReader.nextString();
		         } else if((name).equals(resources.getString(R.string.state))){
						stateString = forecastJsonReader.nextString();
					}else if((name).equals(resources.getString(R.string.read_country))){
						countryString = readCountry(forecastJsonReader);//forecastJsonReader.nextString();
					} else {
		        	 forecastJsonReader.skipValue();
		         }
		       }
		     forecastJsonReader.endObject();
			forecastJsonReader.close();
			
//			if((name).equals(resources.getString(R.string.country))){
//				countryString =forecastJsonReader.nextString();
//			} 
			/*public String readCity(JsonReader reader) throws IOException {
     String cityName = null;
     
     reader.beginObject();
     while (reader.hasNext()) {
       String name = reader.nextName();
       if (name.equals("name")) {
         cityName = reader.nextString();
       } else {
         reader.skipValue();
       }
     }
     reader.endObject();
     return cityName;
   }*/   } // end try
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString()); // print the exception to the LogCat
      } // end catch
      catch (IOException e) 
      {
         Log.v(TAG, e.toString()); // print the exception to the LogCat
      } // end catch
     
      return null; // return null if the city name couldn't be found
   } // end method doInBackground

	private String readCountry(JsonReader forecastJsonReader) throws IOException{
		String coutryName = null;
	     int sunRise = -1;
	     int sunSet = -1;
	     
	     forecastJsonReader.beginObject();
	     while (forecastJsonReader.hasNext()) {
	       String name = forecastJsonReader.nextName();
	       if (name.equals(resources.getString(R.string.country))) {
	    	   coutryName = forecastJsonReader.nextString();
	       } else if (name.equals("sunrise")) {
	    	   sunRise = forecastJsonReader.nextInt();
	       }else if (name.equals("sunset")) {
	    	   sunSet = forecastJsonReader.nextInt();
	       } else {
	    	   forecastJsonReader.skipValue();
	       }
	     }
	     forecastJsonReader.endObject();

		return coutryName;
	}

// executed back on the UI thread after the city name loads
   protected void onPostExecute(String nameString)
   {
      // if a city was found to match the given zipcode
      if (cityString != null)
      {
         // pass the information back to the LocationLoadedListener
         weatherLocationLoadedListener.onLocationLoaded(cityString, 
            stateString, countryString);
      } // end if
      else 
      {
         // display Toast informing that location information 
         // couldn't be found
         Toast errorToast = Toast.makeText(context, resources.getString(
            R.string.invalid_zipcode_error), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0); // center the Toast
         errorToast.show(); // show the Toast
      } // end else
   } // end method onPostExecute
}
