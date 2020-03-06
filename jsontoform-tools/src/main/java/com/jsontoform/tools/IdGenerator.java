package com.jsontoform.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID生成器
 * 
 * @author zhangshuchang
 * @date 2019-5-14
 * 
 */
public class IdGenerator {
	private int sid;// 服务器id
	private AtomicLong id = new AtomicLong();
	private int segment = 100;// 每x个id，保存一下最大id
	private int step = 0;

	private final static IdGenerator instance = new IdGenerator();

	private static Map<Long, Integer> ids = new HashMap<>();

	private IdGenerator() {
	}

	public static synchronized void recordId(long id) {
		Integer count = ids.get(id);
		if (count == null) {
			count = 0;
		}
		count++;
		ids.put(id, count);
		if (count >= 2) {
			System.out.println(id);
		}
	}

	public void init(long maxId, int sid) {
		this.sid = sid;
		if (maxId == 0) {
			maxId = 1;
		} else {
			maxId += segment + 1;// 保证即使上一次没有保存进数据库，现在的maxid也是一定是唯一的
		}
		id.set(maxId);
	}

	public static IdGenerator instance() {
		return instance;
	}

	public synchronized long getUniqueId() {
		step++;
		if (step >= segment) {
			step = 0;
		}

		long uniqueId = id.incrementAndGet();
		uniqueId = (uniqueId << IdConfig.bit) | sid;
		return uniqueId;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			new Thread(new AddJiYuanThread()).start();
		}
		// System.out.println(ids);
	}
}

class IdConfig {
	public final static int bit = 14;// GameServerId在生成id中占后14位
	private final static int maxServerId = (int) Math.round(Math.pow(2, bit) - 1);// 最
}

class AddJiYuanThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			long id = IdGenerator.instance().getUniqueId();
			IdGenerator.instance().recordId(id);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
