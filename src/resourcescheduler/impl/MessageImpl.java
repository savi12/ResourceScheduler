package resourcescheduler.impl;

import resourcescheduler.external.Message;
import resourcescheduler.services.MessageCallback;

public class MessageImpl implements Message {

	
	private int groupId;
	private MessageCallback<Message> callback;
	private boolean terminated;
	public MessageImpl(int groupId) {
		this.groupId = groupId;
	}

	@Override
	public void completed() {
		callback.executeCallback(this);
	}

	@Override
	public int getGroupId() {
		return groupId;
	}

	@Override
	public void setPostCompleteCallback(MessageCallback<Message> callback) {
		this.callback = callback;
	}

	@Override
	public String toString() {
		return "MessageImpl [groupId=" + groupId + "]";
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}
	
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

}
