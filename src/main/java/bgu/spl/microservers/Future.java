package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	T result;
	/**
	 * This should be the only public constructor in this class.
	 */
	public Future() {
		result=null;
	}
	private final Object lock=new Object();
	private boolean done=false;
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * @pre @pre(get)==null
	 * @post @post(get())!=null
     */
	public synchronized T get() {
		while (!done) {
			try {
				wait();
			} catch (InterruptedException ignored) {
			}
		}
		return result;
	}
	
	/**
     * Resolves the result of this Future object.
	 * @param result the result of the object
     */
	public synchronized void resolve (T result) {
		this.result = result;
		done = true;
		notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
	 * @pre this.isDone==false
	 * @post this.isDone==true
     */
	public boolean isDone() {
		return done;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
	 * @pre (this.getResult == null)
	 * @post (this.getResult !=null) && @post(this.getResult)!=@pre(this.getResult)
     */
	public T get(long timeout, TimeUnit unit) {
		synchronized (lock) {
			if (!done)
				try {
					unit.timedWait(this, timeout);
				} catch (Exception e) {
					return result;
				}
			lock.notifyAll();
			return result;
		}
	}

	/**
	 *
	 * @return the result of the future.
	 */
	private T getResult(){
		return result;
	}
}
