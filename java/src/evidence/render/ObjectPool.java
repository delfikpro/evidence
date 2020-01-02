package evidence.render;

import java.io.Serializable;
import java.util.ArrayList;

public class ObjectPool<T> implements Serializable {
	static final long serialVersionUID = -8519013691660936643L;
	private final Class<T> objectType;
	private final ArrayList<T> freeStack;

	public ObjectPool(Class<T> type) {
		this.objectType = type;
		this.freeStack = new ArrayList<>();
	}

	public ObjectPool(Class<T> type, int size) {
		this.objectType = type;
		this.freeStack = new ArrayList<>(size);
	}

	public synchronized Object getInstanceIfFree() {
		return this.freeStack.isEmpty() ? null : this.freeStack.remove(this.freeStack.size() - 1);
	}

	public synchronized Object getInstance() {
		if (this.freeStack.isEmpty()) {
			try {
				return this.objectType.newInstance();
			} catch (InstantiationException | IllegalAccessException ignored) {}

			throw new RuntimeException("Can't create object instance (no empty constructor)");
		} else {
			return this.freeStack.remove(this.freeStack.size() - 1);
		}
	}

	public synchronized void freeInstance(T obj) {
		this.freeStack.add(obj);
	}
}
