package com.yyl.myrmex.tracking.places;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.yyl.myrmex.tracking.R;
import com.yyl.myrmex.tracking.places.PlaceDetail;
import com.yyl.myrmex.tracking.places.Place;
import com.yyl.myrmex.tracking.places.PlacesList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class MyPlaces {
	// Create our transport.
	// For maximum efficiency, applications should use a single globally-shared instance of the HTTP transport.
	private static final HttpTransport transport = new ApacheHttpTransport();
	private static final JacksonFactory myJacksonFac = new JacksonFactory();
	private Context ctx;
	
	public MyPlaces(Context context) {
		ctx = context;
	}
	// Fill in the API key you want to use.
	private String API_KEY = ctx.getResources().getString(R.string.google_api);

	// The different Places API endpoints.
	private static final String PLACES_SEARCH_URL =  
			"https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_AUTOCOMPLETE_URL = 
			"https://maps.googleapis.com/maps/api/place/autocomplete/json?";
	private static final String PLACES_DETAILS_URL = 
			"https://maps.googleapis.com/maps/api/place/details/json?";
	private static final String DEBUG_TAG = "MyPlaces";

	// search nearby places given coordinates and radius.
	// use the return list of places to search detailed places, concatenated by ", "
	public String searchPlaces(double longitude, double latitude, int radius) throws Exception {
		String output = "";
		try {
			System.out.println("Perform Search ....");
			System.out.println("-------------------");
			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location", latitude + "," + longitude);
			request.getUrl().put("radius", radius);
			request.getUrl().put("sensor", "false");

			PlacesList places = request.execute().parseAs(PlacesList.class);
			Log.d(DEBUG_TAG, "STATUS = " + places.status);
			for (Place place : places.results) {
				output = output + performDetails(place.reference) + ", ";
			}
			return output;
			
		} catch (HttpResponseException e) {
			Log.w(DEBUG_TAG, e.getStatusMessage());
			throw e;
		}
	}
	
	public String performDetails(String reference) throws Exception {
		try {
			System.out.println("-------------------");
			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("reference", reference);
			request.getUrl().put("sensor", "false");
			
			PlaceDetail place = request.execute().parseAs(PlaceDetail.class);
//			System.out.println(place);
			return place.toString();

		} catch (HttpResponseException e) {
			Log.w(DEBUG_TAG, e.getStatusMessage());
			throw e;
		}
	}
	
	
	private static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
		   
		  return transport.createRequestFactory(new HttpRequestInitializer() {
		   // request Implementation is not thread-safe.
		   public void initialize(HttpRequest request) {
		    GoogleHeaders headers = new GoogleHeaders();
		    headers.setApplicationName("Google-Places-DemoApp");
		    request.setHeaders(headers);
		    JsonObjectParser parser = new JsonObjectParser(myJacksonFac);
		    request.setParser(parser);
		   }
		});
	}

}