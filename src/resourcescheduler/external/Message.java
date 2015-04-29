package resourcescheduler.external;

import resourcescheduler.services.MessageCallback;

public interface Message {
	public void completed();
	public int getGroupId();
	public void setPostCompleteCallback(MessageCallback<Message> message);
	public boolean isTerminated();
}
