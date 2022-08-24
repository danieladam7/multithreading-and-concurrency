package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusTest {
    MessageBus messageBus;
    MicroService microService1,microService2;
    @Before
    public void setUp() throws Exception {
        messageBus=MessageBusImpl.getInstance();
        String[] arg={"50"};
        microService1=new ExampleEventHandlerService("test",arg);
        microService2= new ExampleEventHandlerService("test2",arg);
    }

    @After
    public void tearDown() throws Exception {
        messageBus.unregister(microService1);
        messageBus.unregister(microService2);
        messageBus=null;
        microService2=null;
        microService1=null;
    }
    @Test
    public void subscribeEventTest(){
        messageBus.register(microService1);
        ExampleEvent event= new ExampleEvent("subscribeEventTest");
        messageBus.subscribeEvent(event.getClass(),microService1);
        assertTrue(messageBus.isEventSubscribed(event,microService1));
        messageBus.unregister(microService1);
    }
    @Test
    public void subscribeBroadcastTest(){
        messageBus.register(microService1);
        ExampleBroadcast broadcast= new ExampleBroadcast("subscribeEventTest");
        messageBus.subscribeBroadcast(broadcast.getClass(),microService1);
        assertTrue(messageBus.isBroadcastSubscribed(broadcast,microService1));
        messageBus.unregister(microService1);
    }
    @Test
    public void complete(){
        messageBus.register(microService1);
        messageBus.register(microService2);
        ExampleEvent event=new ExampleEvent("test1");
        messageBus.complete(event,"PASS");
        assertTrue(messageBus.didComplete(event));
    }
    @Test
    public void sendBroadcastTest(){
        ExampleBroadcast test1=new ExampleBroadcast("test");
        messageBus.register(microService1);
        messageBus.subscribeBroadcast(ExampleBroadcast.class,microService1);
        messageBus.sendBroadcast(test1);
        assertTrue(messageBus.isBroadcastReceived(test1,microService1));
        messageBus.unregister(microService1);
    }
    @Test
    public void sendEventTest(){
        ExampleEvent event= new ExampleEvent("microService1");
        messageBus.register(microService1);
        messageBus.subscribeEvent(ExampleEvent.class,microService1);
        messageBus.sendEvent(event);
        assertTrue(messageBus.isEventInMessageQueue(event,microService1));
        messageBus.unregister(microService1);
    }
    @Test
    public void registerTest(){
        messageBus.register(microService1);
        assertTrue(messageBus.isMicroServiceRegistered(microService1));
        messageBus.unregister(microService1);
    }
    @Test
    public void unregisterTest(){
        messageBus.register(microService1);
        messageBus.register(microService2);
        messageBus.unregister(microService1);
        assertFalse(messageBus.isMicroServiceRegistered(microService1));
        messageBus.unregister(microService2);
    }
    public void awaitMessageTest(){
        try{
            messageBus.register(microService1);
            messageBus.register(microService2);
            ExampleEvent event=new ExampleEvent("microService1");
            ExampleEvent event2=new ExampleEvent("microService2");
            messageBus.subscribeEvent(event.getClass(),microService2);
            messageBus.subscribeEvent(event2.getClass(),microService1);
            messageBus.awaitMessage(microService1);
            messageBus.awaitMessage(microService2);
            messageBus.unregister(microService1);
            messageBus.unregister(microService2);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(true);
    }
}