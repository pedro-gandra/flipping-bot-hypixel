package me.pedrogandra.bazaarbot.utils;

import java.util.LinkedList;
import java.util.Queue;

public class DelayManager {

	public static DelayManager instance = new DelayManager();
	private final Queue<Task> tasks = new LinkedList<>();
	
	private static class Task {
        Runnable action;
        long executeAt;

        Task(Runnable action, long executeAt) {
            this.action = action;
            this.executeAt = executeAt;
        }
    }
	
	public synchronized void schedule(Runnable runnable, long delayMillis) {
        long executeTime = System.currentTimeMillis() + delayMillis;

        if (!tasks.isEmpty()) {
            Task last = null;
            for (Task t : tasks) last = t;
            if (last != null && last.executeAt > executeTime) {
                executeTime = last.executeAt + delayMillis;
            }
        }

        tasks.add(new Task(runnable, executeTime));
    
	}
	
	public synchronized void onTick() {
        long now = System.currentTimeMillis();
        while (!tasks.isEmpty() && tasks.peek().executeAt <= now) {
            tasks.poll().action.run();
        }
    }
	
	public synchronized void clear() {
        tasks.clear();
    }
	
}
