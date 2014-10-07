import java.util.ArrayList;

public abstract class Communicator {
	ArrayList<IListener> listeners = new ArrayList<IListener>();

	public void emit(String eventName, Object value) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).notify(eventName, value);
		}
	}

	public void subscribe(IListener subscriber) {
		listeners.add(subscriber);
	}
}
