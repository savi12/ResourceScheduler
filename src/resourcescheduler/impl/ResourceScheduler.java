package resourcescheduler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import resourcescheduler.external.Gateway;
import resourcescheduler.external.Message;
import resourcescheduler.services.MessageCallback;
import resourcescheduler.services.MessageReceiver;

public class ResourceScheduler implements MessageReceiver,
		MessageCallback<Message> {

	private final Map<Integer, ArrayList<Message>> messageQueue = new HashMap<Integer, ArrayList<Message>>();
	private ArrayList<Integer> groupIdQueue = new ArrayList<>();

	private Set<Integer> terminatedMessages = new HashSet<>();
	private Set<Integer> cancelledGroups = new HashSet<>();

	private int availableResources;
	private Gateway gateway;

	static Logger log = Logger.getLogger(ResourceScheduler.class);

	/**
	 * Only for testing
	 * 
	 * @return
	 */
	public Map<Integer, ArrayList<Message>> getMessageQueu() {
		return messageQueue;
	}

	public ResourceScheduler(int maximumAvailableResources) {
		this.availableResources = maximumAvailableResources;
	}

	/**
	 * public for testing
	 */
	public boolean isMessageGroupTerminated(Message msg){
		if (terminatedMessages.contains(msg.getGroupId())) {
			log.error("Message group " + msg + " is terminated");
			return true;
		}
		return false;
	}
	@Override
	public void receive(Message msg) {
		log.info("Message received from group " + msg);
		if(isMessageGroupTerminated(msg)){
			return;
		}
		if (msg.isTerminated()) {
			terminatedMessages.add(msg.getGroupId());
		}
		msg.setPostCompleteCallback(this);
		if (availableResources > 0) {
			sendToGateway(msg);
			availableResources--;
		} else {
			ArrayList<Message> messagesWithSameGroupId = messageQueue.get(msg
					.getGroupId());
			if (messagesWithSameGroupId == null) {
				messagesWithSameGroupId = new ArrayList<>();
				messageQueue.put(msg.getGroupId(), messagesWithSameGroupId);
			}
			messagesWithSameGroupId.add(msg);
			groupIdQueue.add(msg.getGroupId());
		}
	}

	@Override
	public void executeCallback(Message message) {
		ArrayList<Message> messagesWithSameGroupId = messageQueue.get(message
				.getGroupId());
		if (messagesWithSameGroupId != null
				&& !messagesWithSameGroupId.isEmpty()) {
			sendToGateway(messagesWithSameGroupId.remove(0));
		} else if (!groupIdQueue.isEmpty()) {
			messagesWithSameGroupId = messageQueue.get(groupIdQueue.remove(0));
			sendToGateway(messagesWithSameGroupId.remove(0));
		} else {
			availableResources++;
		}
	}

	private void sendToGateway(Message msg) {
		if (cancelledGroups.contains(msg.getGroupId())) {
			log.info("Message group " + msg
					+ " is cancelled, no further messages will be sent");
		} else {
			gateway.send(msg);
			log.info("Message sent to gateway from group " + msg);
		}
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	public void setCancelledGroups(Set<Integer> cancelledGroups) {
		this.cancelledGroups = cancelledGroups;
	}

}
