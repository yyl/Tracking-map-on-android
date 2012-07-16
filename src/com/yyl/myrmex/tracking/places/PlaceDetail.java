package com.yyl.myrmex.tracking.places;

import com.google.api.client.util.Key;

public class PlaceDetail {

	@Key
	public Place result;
	
	@Override
	public String toString() {
		if (result!=null) {
			return result.name + "|types: " + result.types + "|address: " + result.formatted_address;
		}
		return super.toString();
	}
}

