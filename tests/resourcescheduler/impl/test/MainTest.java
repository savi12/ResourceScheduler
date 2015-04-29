package resourcescheduler.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import resourcescheduler.external.Gateway;
import resourcescheduler.external.Message;
import resourcescheduler.impl.MessageImpl;
import resourcescheduler.impl.ResourceScheduler;

public class MainTest {

	Gateway testGateway = Mockito.mock(Gateway.class);
	
	ResourceScheduler scheduler = new ResourceScheduler(2);
	private static final int GROUP_1_ID = 1;
	private static final int GROUP_2_ID = 2;
	private static final int GROUP_3_ID = 3;

	MessageImpl m1_g1 = new MessageImpl(GROUP_1_ID);	
	
	Message m2_g2 = new MessageImpl(GROUP_2_ID);
	Message m3_g3 = new MessageImpl(GROUP_3_ID);
	Message m4_g2 = new MessageImpl(GROUP_2_ID);
	Message m5_g1 = new MessageImpl(GROUP_1_ID);
	Message m6_g2 = new MessageImpl(GROUP_2_ID);
	Message m7_g1 = new MessageImpl(GROUP_1_ID);


	@BeforeClass
	public static void onlyOnce(){
		BasicConfigurator.configure();
	}
	@Before
	public void init(){
		scheduler.setGateway(testGateway);
	}
	
	@Test
	public void isQueueEmptyAfterReceive() {
		scheduler.receive(m1_g1);
		verify(testGateway,times(1)).send(m1_g1);;
		assertEquals(Collections.EMPTY_MAP, scheduler.getMessageQueu());
	}

	@Test
	public void isQueueNotEmptyAfterReceive() {
		scheduler.receive(m1_g1);
		scheduler.receive(m2_g2);
		scheduler.receive(m3_g3);
		scheduler.receive(m3_g3);
		verify(testGateway,times(1)).send(m1_g1);;
		verify(testGateway,times(1)).send(m2_g2);;
		verify(testGateway,times(0)).send(m3_g3);;

		assertNotEquals(Collections.EMPTY_MAP, scheduler.getMessageQueu());
	}
	
	@Test
	public void isQueuEmptyAfterAllComplete(){
		scheduler.receive(m1_g1);
		scheduler.receive(m2_g2);
		assertEquals(Collections.EMPTY_MAP, scheduler.getMessageQueu());
		scheduler.receive(m5_g1);
		assertEquals(m5_g1, scheduler.getMessageQueu().get(m5_g1.getGroupId()).get(0));

		m1_g1.completed();
		for (ArrayList<Message> queu : scheduler.getMessageQueu().values()) {
			assertEquals(Collections.EMPTY_LIST, queu);
		}
		

	}
	
	@Test
	public void correctSendOrder(){
		scheduler.receive(m1_g1);
		scheduler.receive(m5_g1);
		scheduler.receive(m4_g2);
		scheduler.receive(m7_g1);
				
		m1_g1.completed();
		verify(testGateway,times(1)).send(m7_g1);
		verify(testGateway,times(0)).send(m4_g2);		
	}
	
	@Test
	public void correctSendOrderMultipleComplete(){
		scheduler.receive(m1_g1);
		scheduler.receive(m5_g1);
		scheduler.receive(m4_g2);
		scheduler.receive(m7_g1);
		scheduler.receive(m3_g3);
				
		m1_g1.completed();
		verify(testGateway,times(1)).send(m7_g1);
		verify(testGateway,times(0)).send(m4_g2);
		m5_g1.completed();
		verify(testGateway,times(1)).send(m4_g2);
	}
	
	@Test
	public void terminatedMessageNotSent(){
		//The first terminated of the group should be sent
		m1_g1.setTerminated(true);
		scheduler.receive(m1_g1);
		verify(testGateway,times(1)).send(m1_g1);
		//The second terminated of the group should not send the message
		scheduler.receive(m5_g1);
		verify(testGateway,times(0)).send(m5_g1);
		//The group should be terminated
		assertEquals(true, scheduler.isMessageGroupTerminated(m5_g1));
	}

	@Test
	public void cancelledMessageNotSent(){
		Set<Integer> cancelledMsgs = new HashSet<>();
		cancelledMsgs.add(GROUP_2_ID);
		scheduler.setCancelledGroups(cancelledMsgs);
		scheduler.receive(m2_g2);
		verify(testGateway,times(0)).send(m2_g2);
	}
}
