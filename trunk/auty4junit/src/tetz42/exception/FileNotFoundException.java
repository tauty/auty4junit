package tetz42.exception;

public class FileNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -189797682768117672L;

	public FileNotFoundException(String msg) {
		super(msg);
	}

	public FileNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}

}
