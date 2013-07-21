package eu.eyan.logging;

public class Logging {
	public static void logInfo(String üzenet) {
		System.err.flush();
		pause();
		System.out.println("LOG: " + üzenet);
	}

	public static void logError(String hibaÜzenet, Exception... e) {
		System.out.flush();
		pause();
		System.err.println("HIBA: " + (hibaÜzenet == null ? "" : hibaÜzenet));
		for (Exception exception : e) {
			exception.printStackTrace();
		}
		System.err.flush();
	}

	private static void pause() {
		pause(10);
	}

	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) { /* nem törpénik semmi */
		}
	}

}
