package org.loudermilk.tempmon.util;

import okhttp3.mockwebserver.QueueDispatcher;

public class ResettableQueueDispatcher extends QueueDispatcher {

	public void clear() {
		getResponseQueue().clear();
	}
	
	public int size() {
		return getResponseQueue().size();
	}

}
