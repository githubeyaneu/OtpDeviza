package eu.eyan.logging;

public class Logging {
	public static void logInfo(String �zenet) {
		System.err.flush();
		pause();
		System.out.println("LOG: " + �zenet);
	}

	public static void logError(String hiba�zenet, Exception... e) {
		System.out.flush();
		pause();
		System.err.println("HIBA: " + (hiba�zenet == null ? "" : hiba�zenet));
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
		} catch (InterruptedException e) { /* nem t�rp�nik semmi */
		}
	}

}
