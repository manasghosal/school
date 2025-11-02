package com.quicksoft.school.connection.callback;

public interface SyncCompleteCallback {
	 void onSyncComplete(int syncPage, int response, Object data);
}
