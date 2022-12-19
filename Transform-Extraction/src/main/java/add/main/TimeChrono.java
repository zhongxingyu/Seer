package add.main;

public class TimeChrono {

	private long begin, end;

	public void start() {
		begin = System.currentTimeMillis();
		end = begin;
	}

	public void stop() {
		end = System.currentTimeMillis();
	}

	public long getTime() {
		return end - begin;
	}

	public long getMilliseconds() {
		return end - begin;
	}

	public double getSeconds() {
		stop();
		return (end - begin) / 1000.0;
	}

	public double stopAndGetSeconds() {
		stop();
		return (end - begin) / 1000.0;
	}

	public double getMinutes() {
		return (end - begin) / 60000.0;
	}

	public double getHours() {
		return (end - begin) / 3600000.0;
	}

}
