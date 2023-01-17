package com.ed.geospatial.reader.shared;

import java.util.List;

public class ResponseData<T> {

	private final Integer total;
	private final List<T> data;

	public ResponseData(final Integer total, final List<T> data) {
		this.total = total;
		this.data = data;
	}

	public Integer getTotal() {
		return total;
	}

	public List<T> getData() {
		return data;
	}
}
