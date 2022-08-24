package bgu.spl.mics;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static class MessageBusImplHolder{
		private static final MessageBusImpl instance=new MessageBusImpl();
	}

	private final ConcurrentHashMap<MicroService, Queue<Message>> MicroServiceMessageQueue;
	//private final ConcurrentHashMap<Class<? extends Event<?>>,Queue<MicroService>> EventsSubscribers;
	private final ConcurrentHashMap<Class<? extends Message>,Queue<MicroService>> MessageSubscribers;
	private final ConcurrentHashMap<Event<?>,Future> eventFutureConcurrentHashMap;
	private final Object MicroServiceLock =new Object();
	private final Object SubscribersLock=new Object();
	private MessageBusImpl(){
		MicroServiceMessageQueue =new ConcurrentHashMap<>();
		//EventsSubscribers=new ConcurrentHashMap<>();
		MessageSubscribers=new ConcurrentHashMap<>();
		eventFutureConcurrentHashMap=new ConcurrentHashMap<>();
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (SubscribersLock){
			Queue<MicroService> queue=MessageSubscribers.get(type);
			if(queue == null){
				queue=new LinkedBlockingQueue<>();
				queue.add(m);
				MessageSubscribers.put(type,queue);
			}
			else
				queue.add(m);
			SubscribersLock.notifyAll();
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (SubscribersLock){
			Queue<MicroService> queue=MessageSubscribers.get(type);
			if(queue == null){
				queue=new LinkedBlockingQueue<>();
				queue.add(m);
				MessageSubscribers.put(type,queue);
			}
			else
				queue.add(m);
			SubscribersLock.notifyAll();
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		if (e instanceof TrainModelEvent  && ((TrainModelEvent) e).getStudent().getName().equals("Nala")){
			System.out.println("************\n"+" got complete for training in complete"+"\n ************");
		}else if(e instanceof TestModelEvent && ((TestModelEvent) e).getStudent().getName().equals("Nala")){
			System.out.println("************\n"+" got complete for testing in complete"+"\n ************");
		}
		//System.out.println("*********\n"+e.toString()+"\n ************");
		eventFutureConcurrentHashMap.get(e).resolve(result);
	}


	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (SubscribersLock){
			synchronized (MicroServiceLock) {
				for (MicroService microService : MessageSubscribers.get(b.getClass())) {
					MicroServiceMessageQueue.get(microService).add(b);
				}
				MicroServiceLock.notifyAll();
			}
			SubscribersLock.notifyAll();
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		MicroService microService = null;
		synchronized (SubscribersLock) {
			synchronized (MicroServiceLock) {
				if (MessageSubscribers.containsKey(e.getClass()) && !MessageSubscribers.get(e.getClass()).isEmpty()) {
					microService = MessageSubscribers.get(e.getClass()).remove();

					if (e instanceof TrainModelEvent  && ((TrainModelEvent) e).getStudent().getName().equals("Nala")){
						System.out.println("************\n"+microService.getName()+" got Training Job in sendEvent"+"\n ************");
					}else if(e instanceof TestModelEvent && ((TestModelEvent) e).getStudent().getName().equals("Nala")){
						System.out.println("************\n"+microService.getName()+" got Testing Job in sendEvent"+"\n ************");
					}

					MessageSubscribers.get(e.getClass()).add(microService);
					MicroServiceMessageQueue.get(microService).add(e);
					eventFutureConcurrentHashMap.put(e,new Future<>());
				}
				MicroServiceLock.notifyAll();
			}
			SubscribersLock.notifyAll();
		}
		return eventFutureConcurrentHashMap.get(e);
	}

	@Override
	public void register(MicroService m) {
		synchronized (MicroServiceLock) {
			//System.out.println(m.getName()+"which is "+ Thread.currentThread().getName()+" has registered");
			if (!MicroServiceMessageQueue.containsKey(m)) {
				MicroServiceMessageQueue.put(m,new LinkedBlockingQueue<>());
			}
			MicroServiceLock.notifyAll();
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (SubscribersLock) {
			synchronized (MicroServiceLock) {
				/*
				Queue<Message> MessageQueue = MicroServiceMessageQueue.remove(m);
				if (MessageQueue != null) {
					for (Message message : MessageQueue) {
						MessageSubscribers.get(message.getClass()).remove(m);
					}
				}

				 */
				MicroServiceMessageQueue.remove(m);
				MicroServiceLock.notifyAll();
			}
			for(Queue<MicroService> microServiceQueue:MessageSubscribers.values()){
				microServiceQueue.remove(m);
			}
			SubscribersLock.notifyAll();
		}
	}

	@Override
	// note that callback have to be implemented when taking a massage!!!!
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (MicroServiceLock) {
			Message toReturn = null;
			if(MicroServiceMessageQueue.containsKey(m)) {
				while (MicroServiceMessageQueue.get(m).isEmpty()){
					MicroServiceLock.wait();
				}
				toReturn = MicroServiceMessageQueue.get(m).poll();

				if (toReturn instanceof TrainModelEvent  && ((TrainModelEvent) toReturn).getStudent().getName().equals("Nala")){
					System.out.println("************\n"+Thread.currentThread().getName()+" got Training Job in awaitMessage"+"\n ************");
				}else if(toReturn instanceof TestModelEvent && ((TestModelEvent) toReturn).getStudent().getName().equals("Nala")){
					System.out.println("************\n"+Thread.currentThread().getName()+" got Testing Job in awaitMessage"+"\n ************");
				}

			}
			MicroServiceLock.notifyAll();
			return toReturn;
		}
	}

	@Override
	public boolean isMicroServiceRegistered(MicroService m) {
		boolean output;
		synchronized (MicroServiceMessageQueue){
			output=MicroServiceMessageQueue.containsKey(m);
		}
		return output;
	}

	@Override
	public <T> boolean isEventInMessageQueue(Event<T> event, MicroService m) {
		boolean output;
		synchronized (MicroServiceMessageQueue){
			output= MicroServiceMessageQueue.get(m).contains(event);
		}
		return output;
	}

	@Override
	public boolean isBroadcastReceived(Broadcast broadcast,MicroService m) {
		boolean output;
		synchronized (MicroServiceMessageQueue){
			output=MicroServiceMessageQueue.get(m).contains(broadcast);
		}
		return output;
	}

	@Override
	public <T> boolean didComplete(Event<T> event) {
		synchronized (eventFutureConcurrentHashMap){
			return eventFutureConcurrentHashMap.get(event).isDone();
		}
	}

	@Override
	public <T> boolean isEventSubscribed(Event<T> event,MicroService m) {
		boolean output;
		synchronized (MessageSubscribers){
			output=MessageSubscribers.get(event.getClass()).contains(m);
		}
		return output;
	}

	@Override
	public boolean isBroadcastSubscribed(Broadcast broadcast,MicroService m) {
		boolean output;
		synchronized (MessageSubscribers){
			output=MessageSubscribers.get(broadcast.getClass()).contains(m);
		}
		return output;
	}
	public static MessageBusImpl getInstance(){
		return MessageBusImplHolder.instance;
	}

}
