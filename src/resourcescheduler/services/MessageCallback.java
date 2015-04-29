package resourcescheduler.services;

import resourcescheduler.external.Message;

public interface MessageCallback<T extends Message> {
	public void executeCallback(T message);
}
