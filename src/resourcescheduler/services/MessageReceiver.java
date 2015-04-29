package resourcescheduler.services;

import resourcescheduler.external.Message;

public interface MessageReceiver {
	public void receive(Message msg);
}
